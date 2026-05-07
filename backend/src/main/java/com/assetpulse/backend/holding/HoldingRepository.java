package com.assetpulse.backend.holding;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {
    List<Holding> findByUserIdOrderBySymbolAsc(Long userId);
    Optional<Holding> findByIdAndUserId(Long id, Long userId);
    boolean existsBySymbolAndUserId(String symbol, Long userId);
}