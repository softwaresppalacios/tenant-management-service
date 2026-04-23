package com.sp.parking.tenant.web.controller;

import com.sp.parking.tenant.application.service.TenantService;
import com.sp.parking.tenant.web.dto.TenantDtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {
  private final TenantService tenantService;

  @GetMapping
  public List<TenantResponse> all() {
    return tenantService.findAll();
  }

  @PostMapping
  public TenantResponse create(@Valid @RequestBody CreateTenantRequest request) {
    return tenantService.create(request);
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
