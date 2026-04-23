package com.sp.parking.tenant.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Tenant {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, unique = true, updatable = false)
  private UUID uuid;
  @Column(nullable = false)
  private String name;
  @Enumerated(EnumType.STRING)
  private TenantStatus status;
  @ManyToOne(optional = false)
  private SubscriptionPlan plan;
  private Instant createdAt;
  @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Branch> branches = new ArrayList<>();

  @PrePersist
  void prePersist() {
    if (uuid == null) uuid = UUID.randomUUID();
    if (status == null) status = TenantStatus.ACTIVE;
    if (createdAt == null) createdAt = Instant.now();
  }
}

