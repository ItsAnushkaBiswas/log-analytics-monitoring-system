package com.logstream.ingestion.lucene;

import com.logstream.ingestion.config.IngestionProperties;
import jakarta.annotation.PreDestroy;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.DelegatingAnalyzerWrapper;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class LuceneIndexManager {
    private static final Logger log = LoggerFactory.getLogger(LuceneIndexManager.class);

    private final Directory directory;
    private final Analyzer analyzer;
    private final IndexWriter writer;
    private final SearcherManager searcherManager;
    private final ScheduledExecutorService maintenance;

    public LuceneIndexManager(IngestionProperties props) {
        try {
            Path path = Path.of(props.indexPath()).toAbsolutePath();
            this.directory = FSDirectory.open(path);
            this.analyzer = createAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(analyzer)
                    .setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND)
                    .setRAMBufferSizeMB(props.ramBufferMb());
            this.writer = new IndexWriter(directory, config);
            this.searcherManager = new SearcherManager(writer, null);
            log.info("Lucene index open at {} ({} docs)", path, writer.getDocStats().numDocs);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot open Lucene index at " + props.indexPath(), e);
        }

        this.maintenance = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "lucene-maintenance");
            t.setDaemon(true);
            return t;
        });
        long refreshMs = props.refreshInterval().toMillis();
        long commitMs = props.commitInterval().toMillis();
        maintenance.scheduleWithFixedDelay(this::refreshQuietly, refreshMs, refreshMs, TimeUnit.MILLISECONDS);
        maintenance.scheduleWithFixedDelay(this::commitQuietly, commitMs, commitMs, TimeUnit.MILLISECONDS);
    }

    static Analyzer createAnalyzer() {
        Analyzer standard = new StandardAnalyzer();
        Analyzer keyword = new KeywordAnalyzer();
        Set<String> keywordFields = Set.of(LogDocumentMapper.ID, LogDocumentMapper.LEVEL, LogDocumentMapper.SERVICE,
                LogDocumentMapper.HOST, LogDocumentMapper.TRACE_ID);
        return new DelegatingAnalyzerWrapper(Analyzer.PER_FIELD_REUSE_STRATEGY) {
            @Override
            protected Analyzer getWrappedAnalyzer(String fieldName) {
                return keywordFields.contains(fieldName) || fieldName.startsWith(LogDocumentMapper.ATTR_PREFIX)
                        ? keyword : standard;
            }
        };
    }

    public void addDocuments(List<Document> docs) throws IOException {
        writer.addDocuments(docs);
    }

    public Analyzer analyzer() {
        return analyzer;
    }

    public IndexSearcher acquire() throws IOException {
        return searcherManager.acquire();
    }

    public void release(IndexSearcher searcher) {
        try {
            searcherManager.release(searcher);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void refreshNow() throws IOException {
        searcherManager.maybeRefreshBlocking();
    }

    public void commitNow() throws IOException {
        writer.commit();
    }

    public long docCount() {
        return writer.getDocStats().numDocs;
    }

    private void refreshQuietly() {
        try {
            searcherManager.maybeRefresh();
        } catch (Exception e) {
            log.error("Index refresh failed", e);
        }
    }

    private void commitQuietly() {
        try {
            if (writer.hasUncommittedChanges()) {
                writer.commit();
            }
        } catch (Exception e) {
            log.error("Index commit failed", e);
        }
    }

    @PreDestroy
    public void close() throws IOException {
        maintenance.shutdown();
        try {
            maintenance.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        searcherManager.close();
        writer.close();
        directory.close();
        log.info("Lucene index closed");
    }
}
