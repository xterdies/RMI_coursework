package com.platform.domain.repository;

import com.platform.domain.entity.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
    Page<SystemLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<SystemLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
