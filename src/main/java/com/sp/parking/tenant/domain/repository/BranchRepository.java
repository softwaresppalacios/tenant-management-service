package com.sp.parking.tenant.domain.repository;

import com.sp.parking.tenant.domain.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, Long> {
  Optional<Branch> findByUuidAndTenantUuid(UUID uuid, UUID tenantUuid);
}
