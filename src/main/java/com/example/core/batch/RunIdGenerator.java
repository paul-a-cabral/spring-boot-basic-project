package com.example.core.batch;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class RunIdGenerator {

    // No 'static' needed; Spring Singletons ensure exactly one instance exists
    private final AtomicLong runIdCounter = new AtomicLong(0);

    public long getRunId() {
        return runIdCounter.incrementAndGet();
    }
}
