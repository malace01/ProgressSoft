package com.progresssoft.fxdeals.repository;

import com.progresssoft.fxdeals.entity.Deal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DealRepository extends JpaRepository<Deal, String> {
}
