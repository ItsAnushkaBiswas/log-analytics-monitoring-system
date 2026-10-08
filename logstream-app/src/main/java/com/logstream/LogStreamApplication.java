package com.logstream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Member 1 (Log Ingestion & Lucene Core) - Week 1
 * Single Spring Boot entry point that starts all backend modules.
 */
@SpringBootApplication
public class LogStreamApplication {
    public static void main(String[] args) {
        SpringApplication.run(LogStreamApplication.class, args);
    }
}
