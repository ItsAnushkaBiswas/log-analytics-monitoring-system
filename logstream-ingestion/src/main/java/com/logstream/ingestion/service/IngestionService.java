package com.logstream.ingestion.service;

import com.logstream.common.dto.LogRecord;
import com.logstream.common.proto.IngestResponse;
import com.logstream.common.proto.LogMessage;
import com.logstream.common.search.LogEventListener;
import com.logstream.ingestion.lucene.LogDocumentMapper;
import com.logstream.ingestion.lucene.LuceneIndexManager;
import com.logstream.ingestion.parser.LogParseException;
import com.logstream.ingestion.parser.LogParser;
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Member 1 (Log Ingestion & Lucene Core) - Week 2
 * Write path: parse, index into Lucene, then notify listeners.
 */
@Service
public class IngestionService {
    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);
    public static final int MAX_REPORTED_ERRORS = 20;

    private final LogParser parser;
    private final LuceneIndexManager index;
    private final IngestionStats stats;
    private final List<LogEventListener> listeners;

    @Autowired
    public IngestionService(LogParser parser, LuceneIndexManager index, IngestionStats stats,
                            ObjectProvider<LogEventListener> listeners) {
        this(parser, index, stats, listeners.orderedStream().toList());
    }

    public IngestionService(LogParser parser, LuceneIndexManager index, IngestionStats stats,
                            List<LogEventListener> listeners) {
        this.parser = parser;
        this.index = index;
        this.stats = stats;
        this.listeners = List.copyOf(listeners);
    }

    public IngestResponse ingest(List<LogMessage> batch, long firstPosition) {
        List<LogRecord> records = new ArrayList<>(batch.size());
        IngestResponse.Builder response = IngestResponse.newBuilder();
        long rejected = 0;

        for (int i = 0; i < batch.size(); i++) {
            try {
                records.add(parser.parse(batch.get(i)));
            } catch (LogParseException e) {
                rejected++;
                if (response.getErrorsCount() < MAX_REPORTED_ERRORS) {
                    response.addErrors("#" + (firstPosition + i) + ": " + e.getMessage());
                }
            }
        }

        if (!records.isEmpty()) {
            List<Document> docs = new ArrayList<>(records.size());
            for (LogRecord r : records) {
                docs.add(LogDocumentMapper.toDocument(r));
            }
            try {
                index.addDocuments(docs);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to write logs to the index", e);
            }
            notifyListeners(records);
        }

        stats.recordAccepted(records.size());
        stats.recordRejected(rejected);
        return response.setAccepted(records.size()).setRejected(rejected).build();
    }

    private void notifyListeners(List<LogRecord> records) {
        for (LogEventListener listener : listeners) {
            try {
                listener.onLogsIngested(records);
            } catch (RuntimeException e) {
                log.warn("LogEventListener {} failed", listener.getClass().getName(), e);
            }
        }
    }
}
