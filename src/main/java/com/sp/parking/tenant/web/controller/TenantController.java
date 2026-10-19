package com.sp.parking.tenant.web.controller;

import com.sp.parking.tenant.application.mapper.TenantMapper;
import com.sp.parking.tenant.application.service.AuthService;
import com.sp.parking.tenant.application.service.TenantService;
import com.sp.parking.tenant.domain.model.MembershipRole;
import com.sp.parking.tenant.domain.model.Tenant;
import com.sp.parking.tenant.domain.model.UserAccount;
import com.sp.parking.tenant.web.dto.TenantDtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {
  private final TenantService tenantService;
  private final AuthService authService;
  private final TenantMapper mapper;

  @GetMapping
  public List<TenantResponse> all() {
    return tenantService.findAll();
  }

  /** Si llega con sesion, el usuario queda como propietario del parqueadero creado. */
  @PostMapping
  @Transactional
  public TenantResponse create(
    @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
    @Valid @RequestBody CreateTenantRequest request
  ) {
    Optional<UserAccount> user = authService.optionalUser(AuthController.bearerToken(authorization));
    Tenant tenant = tenantService.createTenant(request.name(), request.branchAddress());
    user.ifPresent(owner -> authService.grantAccess(owner, tenant, MembershipRole.OWNER));
    return mapper.toResponse(tenant);
  }

  @GetMapping("/{tenantUuid}/branches/{branchUuid}/configuration")
  public BranchResponse branchConfiguration(@PathVariable("tenantUuid") UUID tenantUuid, @PathVariable("branchUuid") UUID branchUuid) {
    return tenantService.getBranchConfiguration(tenantUuid, branchUuid);
  }

  @PutMapping("/{tenantUuid}/branches/{branchUuid}/configuration")
  public BranchResponse updateBranchConfiguration(
    @PathVariable("tenantUuid") UUID tenantUuid,
    @PathVariable("branchUuid") UUID branchUuid,
    @Valid @RequestBody BranchConfigurationRequest request
  ) {
    return tenantService.updateBranchConfiguration(tenantUuid, branchUuid, request);
  }
}
