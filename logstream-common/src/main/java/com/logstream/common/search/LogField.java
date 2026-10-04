package com.logstream.common.search;

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
