package com.sp.parking.tenant.domain.repository;

import com.sp.parking.tenant.domain.model.SubscriptionPlan;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
  Optional<SubscriptionPlan> findByName(String name);
}

