package com.assetpulse.backend.market;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FutuAccountRepository extends JpaRepository<FutuAccount, Long>  {
    boolean existsByAccIdAndUserId(Long accId, Long userId);
}
