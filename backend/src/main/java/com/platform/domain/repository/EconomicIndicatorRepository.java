package com.platform.domain.repository;

import com.platform.domain.entity.EconomicIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EconomicIndicatorRepository extends JpaRepository<EconomicIndicator, Long> {
    Optional<EconomicIndicator> findByCode(String code);
    Optional<EconomicIndicator> findByWorldBankCode(String worldBankCode);
    List<EconomicIndicator> findByWorldBankCodeIsNotNull();
}
