package com.sp.parking.tenant.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Branch {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, unique = true, updatable = false)
  private UUID uuid;
  @Column(nullable = false)
  private String name;
  private String nit;
  private String phone;
  private String address;
  private Integer carCapacity;
  private Integer motorcycleCapacity;
  private Integer keyRackCapacity;
  private Integer helmetLockerCapacity;
  private boolean active;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "branch_schedule", joinColumns = @JoinColumn(name = "branch_id"))
  @Builder.Default
  private List<BranchSchedule> schedules = new ArrayList<>();

  @ManyToOne(optional = false)
  private Tenant tenant;

  @PrePersist
  void prePersist() {
    if (uuid == null) uuid = UUID.randomUUID();
    active = true;
    if (carCapacity == null) carCapacity = 0;
    if (motorcycleCapacity == null) motorcycleCapacity = 0;
    if (keyRackCapacity == null) keyRackCapacity = 0;
    if (helmetLockerCapacity == null) helmetLockerCapacity = 0;
  }

  public void applyDefaults() {
    if (carCapacity == null) carCapacity = 0;
    if (motorcycleCapacity == null) motorcycleCapacity = 0;
    if (keyRackCapacity == null) keyRackCapacity = 0;
    if (helmetLockerCapacity == null) helmetLockerCapacity = 0;
    if (schedules == null) schedules = new ArrayList<>();
  }

  public void ensureWeekSchedule() {
    applyDefaults();
    for (java.time.DayOfWeek day : java.time.DayOfWeek.values()) {
      boolean exists = schedules.stream().anyMatch(schedule -> schedule.getDayOfWeek() == day);
      if (!exists) {
        schedules.add(BranchSchedule.builder()
          .dayOfWeek(day)
          .openTime(LocalTime.of(6, 0))
          .closeTime(LocalTime.of(22, 0))
          .closed(false)
          .build());
      }
    }
  }
}
