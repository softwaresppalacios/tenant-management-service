package com.sp.parking.tenant.infraestructure.config;

import com.sp.parking.tenant.domain.model.*;
import com.sp.parking.tenant.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
  private static final java.util.UUID DEMO_TENANT_ID = java.util.UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final String DEMO_EMAIL = "admin@spparqueaderos.com";
  private static final String DEMO_PASSWORD = "admin123";
  private final SubscriptionPlanRepository planRepository;
  private final TenantRepository tenantRepository;
  private final UserAccountRepository userRepository;
  private final TenantMembershipRepository membershipRepository;
  private final PasswordEncoder passwordEncoder;

  /** En produccion va en false (DEMO_DATA_ENABLED=false): no se crea el parqueadero ni el usuario demo con clave conocida. */
  @Value("${app.demo-data.enabled:true}")
  private boolean demoDataEnabled;

  @Override
  @Transactional
  public void run(String... args) {
    // El plan se crea siempre: el registro de nuevos parqueaderos lo necesita.
    SubscriptionPlan starter = planRepository.findByName("STARTER")
      .orElseGet(() -> planRepository.save(SubscriptionPlan.builder().name("STARTER").branchLimit(2).monthlyTicketLimit(3000).active(true).build()));
    if (!demoDataEnabled) return;
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
    seedDemoUser();
  }

  /** Usuario demo real en base de datos, propietario del parqueadero demo. */
  private void seedDemoUser() {
    if (userRepository.existsByEmail(DEMO_EMAIL)) return;
    UserAccount user = userRepository.save(UserAccount.builder()
      .fullName("Administrador Principal")
      .email(DEMO_EMAIL)
      .passwordHash(passwordEncoder.encode(DEMO_PASSWORD))
      .active(true)
      .build());
    tenantRepository.findByUuid(DEMO_TENANT_ID).ifPresent(tenant -> membershipRepository.save(
      TenantMembership.builder().user(user).tenant(tenant).role(MembershipRole.OWNER).build()
    ));
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
