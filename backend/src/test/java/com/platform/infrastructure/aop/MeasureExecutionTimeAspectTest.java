package com.platform.infrastructure.aop;

import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class MeasureExecutionTimeAspectTest {

    static class TestService {
        @MeasureExecutionTime(value = "test.timer", logArgs = true)
        String hello(String name) {
            return "hello " + name;
        }
    }

    @Test
    void aspect_readsAnnotationViaReflection_and_recordsExecutionTime() {
        AtomicReference<String> recordedName = new AtomicReference<>();
        AtomicReference<Duration> recordedDuration = new AtomicReference<>();
        AtomicReference<Map<String, Object>> recordedDetails = new AtomicReference<>();

        ExecutionTimeRecorder recorder = (name, duration, details) -> {
            recordedName.set(name);
            recordedDuration.set(duration);
            recordedDetails.set(details);
        };

        MeasureExecutionTimeAspect aspect = new MeasureExecutionTimeAspect(recorder);
        AspectJProxyFactory factory = new AspectJProxyFactory(new TestService());
        factory.addAspect(aspect);
        TestService proxy = (TestService) factory.getProxy();

        String result = proxy.hello("world");

        assertThat(result).isEqualTo("hello world");
        assertThat(recordedName.get()).isEqualTo("test.timer");
        assertThat(recordedDuration.get()).isNotNull();
        assertThat(recordedDuration.get().toMillis()).isGreaterThanOrEqualTo(0);
        assertThat(recordedDetails.get()).containsKey("method");
        assertThat(recordedDetails.get()).containsKey("args");
    }
}

