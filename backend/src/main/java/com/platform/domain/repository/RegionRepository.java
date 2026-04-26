package com.platform.domain.repository;

import com.platform.domain.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long>, JpaSpecificationExecutor<Region> {
    Optional<Region> findByCode(String code);
    List<Region> findByCountryCode(String countryCode);
    boolean existsByCode(String code);
}
