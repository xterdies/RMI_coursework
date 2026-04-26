package com.platform.infrastructure.aop;

import java.time.Duration;
import java.util.Map;

public interface ExecutionTimeRecorder {
    void record(String name, Duration duration, Map<String, Object> details);
}

