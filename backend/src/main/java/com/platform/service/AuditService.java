package com.platform.service;

import com.platform.domain.entity.SystemLog;
import com.platform.domain.entity.User;
import com.platform.domain.repository.SystemLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final SystemLogRepository systemLogRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(User user, String action, String entityType, Long entityId, String details) {
        systemLogRepository.save(SystemLog.builder()
                .user(user)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .status("SUCCESS")
                .build());
    }

    @Transactional(readOnly = true)
    public Page<SystemLog> findAll(Pageable pageable) {
        return systemLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
