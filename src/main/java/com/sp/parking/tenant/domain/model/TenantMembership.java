package com.sp.parking.tenant.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "tenant_id"}))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TenantMembership {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private UserAccount user;
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_id")
  private Tenant tenant;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MembershipRole role;
  private Instant createdAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null) createdAt = Instant.now();
  }
}
