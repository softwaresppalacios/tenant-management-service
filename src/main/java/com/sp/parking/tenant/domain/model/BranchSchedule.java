package com.sp.parking.tenant.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Embeddable
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BranchSchedule {
  @Enumerated(EnumType.STRING)
  @Column(name = "day_of_week", nullable = false)
  private DayOfWeek dayOfWeek;

  @Column(name = "open_time")
  private LocalTime openTime;

  @Column(name = "close_time")
  private LocalTime closeTime;

  @Column(name = "closed")
  private boolean closed;
}
