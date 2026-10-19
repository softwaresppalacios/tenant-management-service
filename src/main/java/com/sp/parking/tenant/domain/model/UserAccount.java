package com.sp.parking.tenant.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class UserAccount {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, unique = true, updatable = false)
  private UUID uuid;
  @Column(nullable = false)
  private String fullName;
  @Column(nullable = false, unique = true)
  private String email;
  @Column(nullable = false)
  private String passwordHash;
  private boolean active;
  private Instant createdAt;
  private Instant lastLoginAt;

  @PrePersist
  void prePersist() {
    if (uuid == null) uuid = UUID.randomUUID();
    if (createdAt == null) createdAt = Instant.now();
  }
}
