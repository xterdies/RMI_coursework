package com.platform.domain.repository;

import com.platform.domain.entity.ClusteringRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClusteringRunRepository extends JpaRepository<ClusteringRun, Long> {
    Page<ClusteringRun> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT cr FROM ClusteringRun cr LEFT JOIN FETCH cr.assignments WHERE cr.id = :id")
    java.util.Optional<ClusteringRun> findByIdWithAssignments(@Param("id") Long id);

    List<ClusteringRun> findByYear(Integer year);
}
