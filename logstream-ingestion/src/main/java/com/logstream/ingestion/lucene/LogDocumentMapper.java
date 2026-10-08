package com.logstream.ingestion.lucene;

import com.logstream.common.dto.LogRecord;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Member 1 (Log Ingestion & Lucene Core) - Week 2
 * Converts a log record to a Lucene document and back, defining how each field is indexed.
 */
public final class LogDocumentMapper {
    public static final String ID = "id";
    public static final String TIMESTAMP = "timestamp";
    public static final String LEVEL = "level";
    public static final String SERVICE = "service";
    public static final String HOST = "host";
    public static final String MESSAGE = "message";
    public static final String TRACE_ID = "traceId";
    public static final String ATTR_PREFIX = "attr.";

    private LogDocumentMapper() {
    }

    public static Document toDocument(LogRecord log) {
        Document doc = new Document();
        long ts = log.timestamp().toEpochMilli();

        doc.add(new StringField(ID, log.id(), Field.Store.YES));
        doc.add(new LongPoint(TIMESTAMP, ts));
        doc.add(new NumericDocValuesField(TIMESTAMP, ts));
        doc.add(new StoredField(TIMESTAMP, ts));
        keyword(doc, LEVEL, log.level());
        keyword(doc, SERVICE, log.service());
        keyword(doc, HOST, log.host());
        doc.add(new TextField(MESSAGE, log.message(), Field.Store.YES));
        doc.add(new StringField(TRACE_ID, log.traceId(), Field.Store.YES));
        log.attributes().forEach((k, v) -> doc.add(new StringField(ATTR_PREFIX + k, v, Field.Store.YES)));
        return doc;
    }

    public static LogRecord fromDocument(Document doc) {
        Map<String, String> attributes = new LinkedHashMap<>();
        for (IndexableField f : doc.getFields()) {
            if (f.name().startsWith(ATTR_PREFIX)) {
                attributes.put(f.name().substring(ATTR_PREFIX.length()), f.stringValue());
            }
        }
        return new LogRecord(
                doc.get(ID),
                Instant.ofEpochMilli(doc.getField(TIMESTAMP).numericValue().longValue()),
                doc.get(LEVEL),
                doc.get(SERVICE),
                doc.get(HOST),
                doc.get(MESSAGE),
                attributes,
                doc.get(TRACE_ID));
    }

    private static void keyword(Document doc, String field, String value) {
        doc.add(new StringField(field, value, Field.Store.YES));
        doc.add(new SortedDocValuesField(field, new BytesRef(value)));
    }
}
