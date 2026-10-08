package com.logstream.ingestion;

import com.logstream.ingestion.config.IngestionProperties;

import java.nio.file.Path;
import java.time.Duration;

/**
 * Member 1 (Log Ingestion & Lucene Core) - Week 4
 * Test settings for a temporary index and random port.
 */
public final class TestIndexes {
    private TestIndexes() {
    }

    public static IngestionProperties properties(Path indexDir) {
        return new IngestionProperties(0, indexDir.toString(), 1000, 64,
                Duration.ofMinutes(10), Duration.ofMinutes(10), 16);
    }
}
