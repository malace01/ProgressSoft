package com.progresssoft.fxdeals.repository;

import com.progresssoft.fxdeals.entity.Deal;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for persisted FX deals keyed by business identifier.
 *
 * <p>Using {@code dealUniqueId} as the primary key enables fast idempotency checks during import.</p>
 */
public interface DealRepository extends JpaRepository<Deal, String> {
}
