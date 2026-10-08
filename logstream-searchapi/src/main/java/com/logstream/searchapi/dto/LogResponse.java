package com.logstream.searchapi.dto;

import java.time.Instant;
import java.util.Map;

public class LogResponse {

    private String id;
    private Instant timestamp;
    private String level;
    private String service;
    private String host;
    private String message;
    private Map<String, String> attributes;
    private String traceId;

    public LogResponse() {
    }

    public LogResponse(
            String id,
            Instant timestamp,
            String level,
            String service,
            String host,
            String message,
            Map<String, String> attributes,
            String traceId) {

        this.id = id;
        this.timestamp = timestamp;
        this.level = level;
        this.service = service;
        this.host = host;
        this.message = message;
        this.attributes = attributes;
        this.traceId = traceId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}