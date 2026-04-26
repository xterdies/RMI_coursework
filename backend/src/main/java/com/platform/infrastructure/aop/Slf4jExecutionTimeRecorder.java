package com.platform.infrastructure.aop;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

@Component
@Slf4j
public class Slf4jExecutionTimeRecorder implements ExecutionTimeRecorder {
    @Override
    public void record(String name, Duration duration, Map<String, Object> details) {
        log.info("ExecutionTime name='{}' took={}ms details={}", name, duration.toMillis(), details);
    }
}

