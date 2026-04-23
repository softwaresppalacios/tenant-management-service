package com.sp.parking.tenant.infraestructure.config;

import com.sp.parking.tenant.domain.model.*;
import com.sp.parking.tenant.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
  private static final java.util.UUID DEMO_TENANT_ID = java.util.UUID.fromString("11111111-1111-1111-1111-111111111111");
  private final SubscriptionPlanRepository planRepository;
  private final TenantRepository tenantRepository;

  @Override
  @Transactional
  public void run(String... args) {
    SubscriptionPlan starter = planRepository.findByName("STARTER")
      .orElseGet(() -> planRepository.save(SubscriptionPlan.builder().name("STARTER").branchLimit(2).monthlyTicketLimit(3000).active(true).build()));
    if (tenantRepository.count() == 0) {
      Tenant tenant = Tenant.builder().uuid(DEMO_TENANT_ID).name("Centro Comercial Norte").plan(starter).build();
      Branch branch = Branch.builder()
        .name("Parqueadero Central")
        .nit("900.123.456-7")
        .phone("310 456 7890")
        .address("Cll 25 #18-40, Pasto, Nariño")
        .carCapacity(20)
        .motorcycleCapacity(20)
        .keyRackCapacity(20)
        .helmetLockerCapacity(20)
        .tenant(tenant)
        .build();
      seedSchedule(branch);
      tenant.getBranches().add(branch);
      tenantRepository.save(tenant);
    }
  }

  private void seedSchedule(Branch branch) {
    branch.getSchedules().clear();
    branch.getSchedules().add(BranchSchedule.builder().dayOfWeek(DayOfWeek.MONDAY).openTime(LocalTime.of(6, 0)).closeTime(LocalTime.of(22, 0)).closed(false).build());
    branch.getSchedules().add(BranchSchedule.builder().dayOfWeek(DayOfWeek.TUESDAY).openTime(LocalTime.of(6, 0)).closeTime(LocalTime.of(22, 0)).closed(false).build());
    branch.getSchedules().add(BranchSchedule.builder().dayOfWeek(DayOfWeek.WEDNESDAY).openTime(LocalTime.of(6, 0)).closeTime(LocalTime.of(22, 0)).closed(false).build());
    branch.getSchedules().add(BranchSchedule.builder().dayOfWeek(DayOfWeek.THURSDAY).openTime(LocalTime.of(6, 0)).closeTime(LocalTime.of(22, 0)).closed(false).build());
    branch.getSchedules().add(BranchSchedule.builder().dayOfWeek(DayOfWeek.FRIDAY).openTime(LocalTime.of(6, 0)).closeTime(LocalTime.of(22, 0)).closed(false).build());
    branch.getSchedules().add(BranchSchedule.builder().dayOfWeek(DayOfWeek.SATURDAY).openTime(LocalTime.of(6, 0)).closeTime(LocalTime.of(20, 0)).closed(false).build());
    branch.getSchedules().add(BranchSchedule.builder().dayOfWeek(DayOfWeek.SUNDAY).openTime(LocalTime.of(8, 0)).closeTime(LocalTime.of(18, 0)).closed(false).build());
  }
}
