package com.sp.parking.tenant.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

public final class TenantDtos {
  private TenantDtos() {}
  public record CreateTenantRequest(@NotBlank String name, String branchAddress) {}
  public record ScheduleRequest(
    @NotNull DayOfWeek dayOfWeek,
    LocalTime openTime,
    LocalTime closeTime,
    boolean closed
  ) {}
  public record BranchConfigurationRequest(
    @NotBlank String name,
    String nit,
    String phone,
    String address,
    @Min(0) Integer carCapacity,
    @Min(0) Integer motorcycleCapacity,
    @Min(0) Integer keyRackCapacity,
    @Min(0) Integer helmetLockerCapacity,
    Boolean manageResources,
    @Valid List<ScheduleRequest> schedules
  ) {}
  public record ScheduleResponse(DayOfWeek dayOfWeek, LocalTime openTime, LocalTime closeTime, boolean closed) {}
  public record BranchResponse(
    Long id,
    UUID uuid,
    String name,
    String nit,
    String phone,
    String address,
    int carCapacity,
    int motorcycleCapacity,
    int keyRackCapacity,
    int helmetLockerCapacity,
    boolean manageResources,
    boolean active,
    List<ScheduleResponse> schedules
  ) {}
  public record TenantResponse(Long id, UUID uuid, String name, String status, String planName, int branchLimit, List<BranchResponse> branches) {}
}
