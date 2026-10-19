package com.sp.parking.tenant.domain.repository;

import com.sp.parking.tenant.domain.model.TenantMembership;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TenantMembershipRepository extends JpaRepository<TenantMembership, Long> {
  @Query("select m from TenantMembership m join fetch m.tenant t join fetch t.plan where m.user.id = :userId order by t.name")
  List<TenantMembership> findByUserIdWithTenant(Long userId);

  @Query("select m from TenantMembership m where m.user.id = :userId and m.tenant.uuid = :tenantUuid")
  Optional<TenantMembership> findByUserIdAndTenantUuid(Long userId, UUID tenantUuid);
}
