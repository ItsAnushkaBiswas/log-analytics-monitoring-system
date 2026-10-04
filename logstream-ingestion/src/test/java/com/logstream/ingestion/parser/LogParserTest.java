package com.logstream.ingestion.parser;

import com.logstream.common.dto.LogRecord;
import com.logstream.common.proto.LogLevel;
import com.logstream.common.proto.LogMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LogParserTest {
    private static final Instant NOW = Instant.parse("2026-10-04T12:00:00Z");
    private final LogParser parser = new LogParser(Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void structuredMessageIsKept() {
        LogRecord r = parser.parse(LogMessage.newBuilder()
                .setTimestampMillis(Instant.parse("2026-10-04T10:05:00Z").toEpochMilli())
                .setLevel(LogLevel.ERROR)
                .setService("payment-service")
                .setHost("pod-1")
                .setMessage("card declined")
                .putAttributes("env", "prod")
                .setTraceId("abc123")
                .build());

        assertThat(r.id()).isNotBlank();
        assertThat(r.timestamp()).isEqualTo(Instant.parse("2026-10-04T10:05:00Z"));
        assertThat(r.level()).isEqualTo("ERROR");
        assertThat(r.service()).isEqualTo("payment-service");
        assertThat(r.host()).isEqualTo("pod-1");
        assertThat(r.message()).isEqualTo("card declined");
        assertThat(r.attributes()).containsEntry("env", "prod");
        assertThat(r.traceId()).isEqualTo("abc123");
    }

    @Test
    void missingFieldsGetDefaults() {
        LogRecord r = parser.parse(LogMessage.newBuilder().setMessage("hello").build());

        assertThat(r.timestamp()).isEqualTo(NOW);
        assertThat(r.level()).isEqualTo("UNKNOWN");
        assertThat(r.service()).isEqualTo("unknown");
        assertThat(r.host()).isEmpty();
    }

    @Test
    void rawLineIsParsed() {
        LogRecord r = parser.parse(LogMessage.newBuilder()
                .setRaw("2026-10-04T10:05:00Z ERROR [payment-service] card declined for order 42")
                .build());

        assertThat(r.timestamp()).isEqualTo(Instant.parse("2026-10-04T10:05:00Z"));
        assertThat(r.level()).isEqualTo("ERROR");
        assertThat(r.service()).isEqualTo("payment-service");
        assertThat(r.message()).isEqualTo("card declined for order 42");
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "2026-10-04 10:05:00 WARNING disk almost full          | 2026-10-04T10:05:00Z     | WARN  | disk almost full",
            "2026-10-04 10:05:00,123 err: boom                    | 2026-10-04T10:05:00.123Z | ERROR | boom",
            "2026-10-04T15:35:00+05:30 info [auth] user logged in  | 2026-10-04T10:05:00Z     | INFO  | user logged in",
            "2026-10-04T15:35:00+0530 CRITICAL db down             | 2026-10-04T10:05:00Z     | FATAL | db down",
            "2026-10-04T10:05:00Z User logged in                   | 2026-10-04T10:05:00Z     | UNKNOWN | User logged in",
    })
    void rawLineVariants(String raw, String ts, String level, String message) {
        LogRecord r = parser.parse(LogMessage.newBuilder().setRaw(raw.strip()).build());

        assertThat(r.timestamp()).isEqualTo(Instant.parse(ts.strip()));
        assertThat(r.level()).isEqualTo(level.strip());
        assertThat(r.message()).isEqualTo(message.strip());
    }

    @Test
    void unrecognisedRawLineIsKeptWhole() {
        LogRecord r = parser.parse(LogMessage.newBuilder().setRaw("something odd happened").build());

        assertThat(r.message()).isEqualTo("something odd happened");
        assertThat(r.level()).isEqualTo("UNKNOWN");
        assertThat(r.timestamp()).isEqualTo(NOW);
    }

    @Test
    void structuredFieldsWinOverRaw() {
        LogRecord r = parser.parse(LogMessage.newBuilder()
                .setLevel(LogLevel.WARN)
                .setService("billing")
                .setRaw("2026-10-04T10:05:00Z ERROR [payment-service] card declined")
                .build());

        assertThat(r.level()).isEqualTo("WARN");
        assertThat(r.service()).isEqualTo("billing");
        assertThat(r.message()).isEqualTo("card declined");
    }

    @Test
    void emptyMessageIsRejected() {
        assertThatThrownBy(() -> parser.parse(LogMessage.newBuilder().setService("x").build()))
                .isInstanceOf(LogParseException.class)
                .hasMessageContaining("message is required");
    }

    @Test
    void longMessageIsTruncated() {
        LogRecord r = parser.parse(LogMessage.newBuilder().setMessage("x".repeat(100_000)).build());

        assertThat(r.message()).hasSize(LogParser.MAX_MESSAGE_LENGTH);
    }
}
