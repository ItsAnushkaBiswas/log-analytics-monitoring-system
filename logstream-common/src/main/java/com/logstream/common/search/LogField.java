package com.logstream.common.search;

/**
 * Member 1 (Log Ingestion & Lucene Core) - Week 3
 * Fields available for group-by counts: level, service and host.
 */
public enum LogField {
    LEVEL("level"),
    SERVICE("service"),
    HOST("host");

    private final String indexName;

    LogField(String indexName) {
        this.indexName = indexName;
    }

    public String indexName() {
        return indexName;
    }
}
