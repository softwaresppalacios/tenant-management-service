package com.sp.parking.tenant.domain.repository;

import com.sp.parking.tenant.domain.model.UserSession;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
  @Query("select s from UserSession s join fetch s.user where s.tokenHash = :tokenHash")
  Optional<UserSession> findByTokenHashWithUser(String tokenHash);

  @Modifying
  @Query("update UserSession s set s.revokedAt = :now where s.user.id = :userId and s.revokedAt is null")
  int revokeAll(Long userId, Instant now);

  @Modifying
  @Query("update UserSession s set s.revokedAt = :now where s.user.id = :userId and s.revokedAt is null and s.id <> :keepSessionId")
  int revokeOthers(Long userId, Long keepSessionId, Instant now);
}
