package com.sp.parking.tenant.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SubscriptionPlan {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, unique = true)
  private String name;
  private int branchLimit;
  private int monthlyTicketLimit;
  private boolean active;
}

