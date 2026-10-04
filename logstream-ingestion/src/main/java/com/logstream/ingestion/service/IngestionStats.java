package com.logstream.ingestion.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.LongAdder;

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
