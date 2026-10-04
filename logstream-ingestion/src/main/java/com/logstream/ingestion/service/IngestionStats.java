package com.logstream.ingestion.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.LongAdder;

/**
 * Member 1 (Log Ingestion & Lucene Core) - Week 4
 * Accepted and rejected log counters used for monitoring and benchmarks.
 */
@Component
public class IngestionStats {
    private final LongAdder accepted = new LongAdder();
    private final LongAdder rejected = new LongAdder();

    void recordAccepted(long n) {
        accepted.add(n);
    }

    void recordRejected(long n) {
        rejected.add(n);
    }

    public long accepted() {
        return accepted.sum();
    }

    public long rejected() {
        return rejected.sum();
    }
}
