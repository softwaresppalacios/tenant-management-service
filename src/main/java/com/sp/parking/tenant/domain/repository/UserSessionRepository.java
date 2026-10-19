package com.sp.parking.tenant.domain.repository;

import com.sp.parking.tenant.domain.model.UserSession;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
  @Query("select s from UserSession s join fetch s.user where s.tokenHash = :tokenHash")
  Optional<UserSession> findByTokenHashWithUser(String tokenHash);
}
