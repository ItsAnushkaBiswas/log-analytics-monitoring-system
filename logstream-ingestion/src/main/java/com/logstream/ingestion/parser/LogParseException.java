package com.logstream.ingestion.parser;

/**
 * Member 1 (Log Ingestion & Lucene Core) - Week 2
 * Raised when an incoming log is invalid; the reason is returned to the sender.
 */
public class LogParseException extends RuntimeException {
    public LogParseException(String message) {
        super(message);
    }
}
