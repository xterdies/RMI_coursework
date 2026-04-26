package com.platform.infrastructure.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Aspect
@Component
public class MeasureExecutionTimeAspect {

    private final ExecutionTimeRecorder recorder;

    public MeasureExecutionTimeAspect(ExecutionTimeRecorder recorder) {
        this.recorder = recorder;
    }

    // Design Pattern: Decorator - adds timing/logging behavior around methods without changing their code.
    @Around("@annotation(com.platform.infrastructure.aop.MeasureExecutionTime)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();

        // Custom reflection logic: read annotation parameters at runtime.
        MeasureExecutionTime ann = method.getAnnotation(MeasureExecutionTime.class);
        String name = (ann.value() == null || ann.value().isBlank())
                ? method.getDeclaringClass().getSimpleName() + "." + method.getName()
                : ann.value();

        Instant start = Instant.now();
        try {
            return pjp.proceed();
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("method", method.toGenericString());
            if (ann.logArgs()) {
                details.put("args", pjp.getArgs());
            }
            recorder.record(name, duration, details);
        }
    }
}

