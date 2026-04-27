package com.sp.parking.tenant.application.mapper;

import com.sp.parking.tenant.domain.model.*;
import com.sp.parking.tenant.web.dto.TenantDtos.*;
import org.springframework.stereotype.Component;
import java.util.Comparator;

@Component
public class TenantMapper {
  public TenantResponse toResponse(Tenant tenant) {
    return new TenantResponse(
      tenant.getId(),
      tenant.getUuid(),
      tenant.getName(),
      tenant.getStatus().name(),
      tenant.getPlan().getName(),
      tenant.getPlan().getBranchLimit(),
      tenant.getBranches().stream()
        .map(this::toBranchResponse)
        .toList()
    );
  }

  public BranchResponse toBranchResponse(Branch branch) {
    branch.ensureWeekSchedule();
    return new BranchResponse(
      branch.getId(),
      branch.getUuid(),
      branch.getName(),
      branch.getNit(),
      branch.getPhone(),
      branch.getAddress(),
      branch.getCarCapacity(),
      branch.getMotorcycleCapacity(),
      branch.getKeyRackCapacity(),
      branch.getHelmetLockerCapacity(),
      Boolean.TRUE.equals(branch.getManageResources()),
      branch.isActive(),
      branch.getSchedules().stream()
        .sorted(Comparator.comparing(BranchSchedule::getDayOfWeek))
        .map(schedule -> new ScheduleResponse(
          schedule.getDayOfWeek(),
          schedule.getOpenTime(),
          schedule.getCloseTime(),
          schedule.isClosed()
        ))
        .toList()
    );
  }
}
