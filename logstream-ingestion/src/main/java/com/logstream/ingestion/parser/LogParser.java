package com.logstream.ingestion.parser;

import com.logstream.common.dto.LogLevels;
import com.logstream.common.dto.LogRecord;
import com.logstream.common.proto.LogLevel;
import com.logstream.common.proto.LogMessage;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LogParser {
    static final int MAX_MESSAGE_LENGTH = 32_768;
    static final int MAX_FIELD_LENGTH = 256;
    static final int MAX_ATTRIBUTES = 50;
    static final String UNKNOWN_SERVICE = "unknown";

    private static final Pattern RAW_LINE = Pattern.compile(
            "^(?<ts>\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}:\\d{2}(?:[.,]\\d{1,9})?(?:Z|[+-]\\d{2}:?\\d{2})?)"
                    + "\\s+(?<level>[A-Za-z]+)"
                    + "(?:\\s+\\[(?<service>[^\\]]+)])?"
                    + "\\s*:?\\s+(?<msg>.*)$",
            Pattern.DOTALL);

    private static final DateTimeFormatter LENIENT_LOCAL = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .optionalStart().appendLiteral('T').optionalEnd()
            .optionalStart().appendLiteral(' ').optionalEnd()
            .appendPattern("HH:mm:ss")
            .optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true).optionalEnd()
            .toFormatter();

    private final Clock clock;

    public LogParser() {
        this(Clock.systemUTC());
    }

    LogParser(Clock clock) {
        this.clock = clock;
    }

    public LogRecord parse(LogMessage in) {
        Instant timestamp = in.getTimestampMillis() > 0 ? Instant.ofEpochMilli(in.getTimestampMillis()) : null;
        String level = in.getLevel() == LogLevel.LOG_LEVEL_UNSPECIFIED || in.getLevel() == LogLevel.UNRECOGNIZED
                ? null : in.getLevel().name();
        String service = in.getService();
        String message = in.getMessage();

        if (message.isBlank() && !in.getRaw().isBlank()) {
            String raw = in.getRaw().strip();
            Matcher m = RAW_LINE.matcher(raw);
            if (m.matches()) {
                if (timestamp == null) {
                    timestamp = parseTimestamp(m.group("ts"));
                }
                if (!LogLevels.UNKNOWN.equals(LogLevels.normalize(m.group("level")))) {
                    if (level == null) {
                        level = m.group("level");
                    }
                    if (service.isBlank() && m.group("service") != null) {
                        service = m.group("service");
                    }
                    message = m.group("msg");
                } else {
                    message = raw.substring(m.end("ts")).strip();
                }
            } else {
                message = raw;
            }
        }

        if (message.isBlank()) {
            throw new LogParseException("message is required (set 'message' or 'raw')");
        }

        return new LogRecord(
                UUID.randomUUID().toString(),
                timestamp != null ? timestamp : clock.instant(),
                LogLevels.normalize(level),
                service.isBlank() ? UNKNOWN_SERVICE : truncate(service.strip(), MAX_FIELD_LENGTH),
                truncate(in.getHost().strip(), MAX_FIELD_LENGTH),
                truncate(message, MAX_MESSAGE_LENGTH),
                attributes(in.getAttributesMap()),
                truncate(in.getTraceId().strip(), MAX_FIELD_LENGTH));
    }

    private static Instant parseTimestamp(String text) {
        String ts = text.replace(',', '.');
        try {
            return OffsetDateTime.parse(ts.replace(' ', 'T')).toInstant();
        } catch (DateTimeParseException ignored) {
        }
        Matcher offset = Pattern.compile("([+-]\\d{2}):?(\\d{2})$").matcher(ts);
        if (offset.find()) {
            ZoneOffset zone = ZoneOffset.of(offset.group(1) + ":" + offset.group(2));
            return LocalDateTime.parse(ts.substring(0, offset.start()), LENIENT_LOCAL).toInstant(zone);
        }

        return LocalDateTime.parse(ts, LENIENT_LOCAL).toInstant(ZoneOffset.UTC);
    }

    private static Map<String, String> attributes(Map<String, String> in) {
        if (in.size() > MAX_ATTRIBUTES) {
            throw new LogParseException("too many attributes (max " + MAX_ATTRIBUTES + ")");
        }
        Map<String, String> out = new LinkedHashMap<>();
        in.forEach((k, v) -> {
            if (!k.isBlank()) {
                out.put(truncate(k.strip(), MAX_FIELD_LENGTH), truncate(v, MAX_FIELD_LENGTH));
            }
        });
        return out;
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }
}
