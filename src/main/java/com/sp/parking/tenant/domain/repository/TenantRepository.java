package com.sp.parking.tenant.domain.repository;

import com.sp.parking.tenant.domain.model.Tenant;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
  Optional<Tenant> findByUuid(UUID uuid);
}

