package com.logstream.common.dto;

import java.util.List;
import java.util.Locale;

public final class LogLevels {
    public static final String TRACE = "TRACE";
    public static final String DEBUG = "DEBUG";
    public static final String INFO = "INFO";
    public static final String WARN = "WARN";
    public static final String ERROR = "ERROR";
    public static final String FATAL = "FATAL";

    public static final String UNKNOWN = "UNKNOWN";

    public static final List<String> ALL = List.of(TRACE, DEBUG, INFO, WARN, ERROR, FATAL, UNKNOWN);

    private LogLevels() {
    }

    public static String normalize(String level) {
        if (level == null || level.isBlank()) {
            return UNKNOWN;
        }
        return switch (level.trim().toUpperCase(Locale.ROOT)) {
            case "TRACE", "FINEST", "FINER" -> TRACE;
            case "DEBUG", "FINE" -> DEBUG;
            case "INFO", "INFORMATION", "NOTICE" -> INFO;
            case "WARN", "WARNING" -> WARN;
            case "ERROR", "ERR", "SEVERE" -> ERROR;
            case "FATAL", "CRITICAL", "CRIT", "EMERG", "ALERT" -> FATAL;
            default -> UNKNOWN;
        };
    }
}
