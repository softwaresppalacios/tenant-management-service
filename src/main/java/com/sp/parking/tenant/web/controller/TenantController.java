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
import java.util.UUID;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {
  private final TenantService tenantService;
  private final AuthService authService;
  private final TenantMapper mapper;

  /** Parqueaderos del usuario en sesion. */
  @GetMapping
  public List<TenantResponse> mine(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
    return tenantService.findForUser(authService.requireUser(AuthController.bearerToken(authorization)));
  }

  /** El usuario en sesion queda como propietario del parqueadero creado. */
  @PostMapping
  @Transactional
  public TenantResponse create(
    @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
    @Valid @RequestBody CreateTenantRequest request
  ) {
    UserAccount owner = authService.requireUser(AuthController.bearerToken(authorization));
    Tenant tenant = tenantService.createTenant(request.name(), request.branchAddress());
    authService.grantAccess(owner, tenant, MembershipRole.OWNER);
    return mapper.toResponse(tenant);
  }

  @GetMapping("/{tenantUuid}/branches/{branchUuid}/configuration")
  public BranchResponse branchConfiguration(
    @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
    @PathVariable("tenantUuid") UUID tenantUuid,
    @PathVariable("branchUuid") UUID branchUuid
  ) {
    authService.requireMembership(AuthController.bearerToken(authorization), tenantUuid);
    return tenantService.getBranchConfiguration(tenantUuid, branchUuid);
  }

  @PutMapping("/{tenantUuid}/branches/{branchUuid}/configuration")
  public BranchResponse updateBranchConfiguration(
    @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
    @PathVariable("tenantUuid") UUID tenantUuid,
    @PathVariable("branchUuid") UUID branchUuid,
    @Valid @RequestBody BranchConfigurationRequest request
  ) {
    authService.requireMembership(AuthController.bearerToken(authorization), tenantUuid);
    return tenantService.updateBranchConfiguration(tenantUuid, branchUuid, request);
  }
}
