package com.sp.parking.tenant.application.service;

import com.sp.parking.tenant.application.mapper.TenantMapper;
import com.sp.parking.tenant.domain.model.*;
import com.sp.parking.tenant.domain.repository.*;
import com.sp.parking.tenant.web.dto.TenantDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.UUID;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TenantService {
  private final TenantRepository tenantRepository;
  private final BranchRepository branchRepository;
  private final SubscriptionPlanRepository planRepository;
  private final TenantMembershipRepository membershipRepository;
  private final TenantMapper mapper;

  /** Solo los parqueaderos a los que el usuario tiene acceso. */
  @Transactional(readOnly = true)
  public List<TenantResponse> findForUser(UserAccount user) {
    return membershipRepository.findByUserIdWithTenant(user.getId()).stream()
      .map(membership -> mapper.toResponse(membership.getTenant()))
      .toList();
  }

  @Transactional
  public TenantResponse create(CreateTenantRequest request) {
    return mapper.toResponse(createTenant(request.name(), request.branchAddress()));
  }

  /** Crea el parqueadero con su sede principal y horario por defecto. */
  @Transactional
  public Tenant createTenant(String name, String branchAddress) {
    SubscriptionPlan plan = planRepository.findByName("STARTER").orElseThrow();
    Tenant tenant = Tenant.builder().name(name.trim()).plan(plan).build();
    Branch branch = Branch.builder()
      .name(name.trim())
      .address(branchAddress == null || branchAddress.isBlank() ? null : branchAddress.trim())
      .tenant(tenant)
      .build();
    branch.ensureWeekSchedule();
    tenant.getBranches().add(branch);
    return tenantRepository.save(tenant);
  }

  @Transactional(readOnly = true)
  public BranchResponse getBranchConfiguration(UUID tenantUuid, UUID branchUuid) {
    Branch branch = branchRepository.findByUuidAndTenantUuid(branchUuid, tenantUuid)
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Branch not found for tenant"));
    branch.ensureWeekSchedule();
    return mapper.toBranchResponse(branch);
  }

  @Transactional
  public BranchResponse updateBranchConfiguration(UUID tenantUuid, UUID branchUuid, BranchConfigurationRequest request) {
    Branch branch = branchRepository.findByUuidAndTenantUuid(branchUuid, tenantUuid)
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Branch not found for tenant"));
    branch.setName(request.name());
    branch.setNit(request.nit());
    branch.setPhone(request.phone());
    branch.setAddress(request.address());
    branch.setCarCapacity(zeroIfNull(request.carCapacity()));
    branch.setMotorcycleCapacity(zeroIfNull(request.motorcycleCapacity()));
    branch.setKeyRackCapacity(zeroIfNull(request.keyRackCapacity()));
    branch.setHelmetLockerCapacity(zeroIfNull(request.helmetLockerCapacity()));
    branch.setManageResources(request.manageResources() == null ? Boolean.TRUE : request.manageResources());
    branch.getSchedules().clear();
    if (request.schedules() != null) {
      request.schedules().forEach(schedule -> branch.getSchedules().add(
        BranchSchedule.builder()
          .dayOfWeek(schedule.dayOfWeek())
          .openTime(schedule.closed() ? null : schedule.openTime())
          .closeTime(schedule.closed() ? null : schedule.closeTime())
          .closed(schedule.closed())
          .build()
      ));
    }
    branch.ensureWeekSchedule();
    validateSchedules(branch);
    return mapper.toBranchResponse(branchRepository.save(branch));
  }

  private int zeroIfNull(Integer value) {
    return value == null ? 0 : value;
  }

  private void validateSchedules(Branch branch) {
    java.time.DayOfWeek[] days = java.time.DayOfWeek.values();
    for (java.time.DayOfWeek day : days) {
      long count = branch.getSchedules().stream().filter(schedule -> schedule.getDayOfWeek() == day).count();
      if (count > 1) {
        throw new ResponseStatusException(BAD_REQUEST, "Schedule for day " + day + " is duplicated");
      }
    }
    branch.getSchedules().forEach(schedule -> {
      if (!schedule.isClosed() && (schedule.getOpenTime() == null || schedule.getCloseTime() == null)) {
        throw new ResponseStatusException(BAD_REQUEST, "Open and close time are required for " + schedule.getDayOfWeek());
      }
      if (!schedule.isClosed() && !schedule.getCloseTime().isAfter(schedule.getOpenTime())) {
        throw new ResponseStatusException(BAD_REQUEST, "Close time must be after open time for " + schedule.getDayOfWeek());
      }
    });
  }
}
