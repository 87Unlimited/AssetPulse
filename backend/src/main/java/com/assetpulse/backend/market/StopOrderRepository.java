package com.assetpulse.backend.market;

import com.assetpulse.backend.holding.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StopOrderRepository extends JpaRepository<StopOrder, Long> {
    List<StopOrder> findByUserId(Long userId);
    boolean existsByCodeAndUserId(String code, Long userId);
}