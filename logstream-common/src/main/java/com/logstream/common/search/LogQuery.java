package com.logstream.common.search;

import java.time.Instant;
import java.util.Set;

public record LogQuery(
        String text,
        Set<String> levels,
        Set<String> services,
        Instant from,
        Instant to,
        int offset,
        int limit) {
    public static final int DEFAULT_LIMIT = 50;
    public static final int MAX_LIMIT = 1_000;

    public static final int MAX_WINDOW = 10_000;

    public LogQuery {
        levels = levels == null ? Set.of() : Set.copyOf(levels);
        services = services == null ? Set.of() : Set.copyOf(services);
        if (limit <= 0) {
            limit = DEFAULT_LIMIT;
        }
        if (limit > MAX_LIMIT) {
            throw new IllegalArgumentException("limit must be <= " + MAX_LIMIT);
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset must be >= 0");
        }
        if (offset + limit > MAX_WINDOW) {
            throw new IllegalArgumentException("offset + limit must be <= " + MAX_WINDOW);
        }
        if (from != null && to != null && !from.isBefore(to)) {
            throw new IllegalArgumentException("from must be before to");
        }
    }

    public static LogQuery all() {
        return new LogQuery(null, null, null, null, null, 0, DEFAULT_LIMIT);
    }

    public LogQuery withText(String text) {
        return new LogQuery(text, levels, services, from, to, offset, limit);
    }

    public LogQuery withLevels(Set<String> levels) {
        return new LogQuery(text, levels, services, from, to, offset, limit);
    }

    public LogQuery withServices(Set<String> services) {
        return new LogQuery(text, levels, services, from, to, offset, limit);
    }

    public LogQuery withRange(Instant from, Instant to) {
        return new LogQuery(text, levels, services, from, to, offset, limit);
    }

    public LogQuery withPage(int offset, int limit) {
        return new LogQuery(text, levels, services, from, to, offset, limit);
    }
}
