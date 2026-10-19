package com.sp.parking.tenant.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/** Sesion de login. Solo se guarda el hash SHA-256 del token, nunca el token en claro. */
@Entity
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class UserSession {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, unique = true, length = 64)
  private String tokenHash;
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private UserAccount user;
  @Column(nullable = false)
  private Instant createdAt;
  @Column(nullable = false)
  private Instant expiresAt;
  private Instant revokedAt;

  public boolean isValid(Instant now) {
    return revokedAt == null && expiresAt.isAfter(now);
  }
}
