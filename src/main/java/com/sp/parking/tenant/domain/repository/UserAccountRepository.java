package com.sp.parking.tenant.domain.repository;

import com.sp.parking.tenant.domain.model.UserAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
  Optional<UserAccount> findByEmail(String email);
  boolean existsByEmail(String email);
}
