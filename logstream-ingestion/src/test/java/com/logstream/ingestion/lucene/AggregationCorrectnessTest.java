package com.logstream.ingestion.lucene;

import com.logstream.common.search.TimeBucket;
import java.time.Instant;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.util.BytesRef;
import com.logstream.ingestion.TestIndexes;
import com.logstream.common.search.LogField;
import com.logstream.common.search.LogQuery;
import com.logstream.ingestion.config.IngestionProperties;
import org.apache.lucene.document.Document;
import org.junit.jupiter.api.Test;
import com.logstream.ingestion.lucene.LogDocumentMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AggregationCorrectnessTest {

    @Test
    void countByLevelReturnsExpectedCounts() throws Exception {

        Path indexDir = Files.createTempDirectory("aggregation-test");

        IngestionProperties properties = TestIndexes.properties(indexDir);

        LuceneIndexManager indexManager = new LuceneIndexManager(properties);

            Document error1 = new Document();
            error1.add(new org.apache.lucene.document.StringField(
                    LogDocumentMapper.LEVEL, "ERROR",
                    org.apache.lucene.document.Field.Store.YES));
            error1.add(new SortedDocValuesField(
                LogDocumentMapper.LEVEL,
                new BytesRef("ERROR")
            ));

            Document error2 = new Document();
            error2.add(new org.apache.lucene.document.StringField(
                    LogDocumentMapper.LEVEL, "ERROR",
                    org.apache.lucene.document.Field.Store.YES));
            error2.add(new SortedDocValuesField(
                LogDocumentMapper.LEVEL,
                new BytesRef("ERROR")
            ));

            Document info = new Document();
            info.add(new org.apache.lucene.document.StringField(
                    LogDocumentMapper.LEVEL, "INFO",
                    org.apache.lucene.document.Field.Store.YES));
            info.add(new SortedDocValuesField(
                LogDocumentMapper.LEVEL,
                new BytesRef("INFO")
            ));

            indexManager.addDocuments(List.of(error1, error2, info));
            indexManager.commitNow();

            indexManager.refreshNow();

            LuceneLogSearchService searchService =
                    new LuceneLogSearchService(indexManager);

            LogQuery query = new LogQuery(
                    null,
                    null,
                    null,
                    null,
                    null,
                    0,
                    100
            );

            Map<String, Long> result =
                    searchService.countBy(LogField.LEVEL, query);

            assertEquals(2L, result.get("ERROR"));
            assertEquals(1L, result.get("INFO"));
        }

    @Test
    void countReturnsExpectedTotal() throws Exception {
        Path indexDir = Files.createTempDirectory("count-test");

        IngestionProperties properties = TestIndexes.properties(indexDir);

        LuceneIndexManager indexManager =
                new LuceneIndexManager(properties);

        Document error1 = new Document();
        error1.add(new org.apache.lucene.document.StringField(
                LogDocumentMapper.LEVEL,
                "ERROR",
                org.apache.lucene.document.Field.Store.YES
        ));
        error1.add(new SortedDocValuesField(
                LogDocumentMapper.LEVEL,
                new BytesRef("ERROR")
        ));

        Document error2 = new Document();
        error2.add(new org.apache.lucene.document.StringField(
                LogDocumentMapper.LEVEL,
                "ERROR",
                org.apache.lucene.document.Field.Store.YES
        ));
        error2.add(new SortedDocValuesField(
                LogDocumentMapper.LEVEL,
                new BytesRef("ERROR")
        ));

        Document info = new Document();
        info.add(new org.apache.lucene.document.StringField(
                LogDocumentMapper.LEVEL,
                "INFO",
                org.apache.lucene.document.Field.Store.YES
        ));
        info.add(new SortedDocValuesField(
                LogDocumentMapper.LEVEL,
                new BytesRef("INFO")
        ));

        indexManager.addDocuments(List.of(error1, error2, info));
        indexManager.commitNow();
        indexManager.refreshNow();

        LuceneLogSearchService searchService =
                new LuceneLogSearchService(indexManager);

        LogQuery query = new LogQuery(
                null,
                null,
                null,
                null,
                null,
                0,
                100
        );

        long result = searchService.count(query);

        assertEquals(3L, result);

    }

    @Test
    void histogramReturnsExpectedBuckets() throws Exception {
        Path indexDir = Files.createTempDirectory("histogram-test");

        IngestionProperties properties = TestIndexes.properties(indexDir);

        LuceneIndexManager indexManager =
                new LuceneIndexManager(properties);
        Instant base = Instant.parse("2026-10-06T10:00:00Z");

        Document error1 = new Document();
        error1.add(new LongPoint(
                LogDocumentMapper.TIMESTAMP,
                base.toEpochMilli()
        ));
        error1.add(new NumericDocValuesField(
                LogDocumentMapper.TIMESTAMP,
                base.toEpochMilli()
        ));
        error1.add(new SortedDocValuesField(
                LogDocumentMapper.LEVEL,
                new BytesRef("ERROR")
        ));

        Document error2 = new Document();
        Instant secondTime = base.plusSeconds(30);
        error2.add(new LongPoint(
                LogDocumentMapper.TIMESTAMP,
                secondTime.toEpochMilli()
        ));
        error2.add(new NumericDocValuesField(
                LogDocumentMapper.TIMESTAMP,
                secondTime.toEpochMilli()
        ));
        error2.add(new SortedDocValuesField(
                LogDocumentMapper.LEVEL,
                new BytesRef("ERROR")
        ));

        Document info = new Document();
        Instant thirdTime = base.plusSeconds(90);
        info.add(new LongPoint(
                LogDocumentMapper.TIMESTAMP,
                thirdTime.toEpochMilli()
        ));
        info.add(new NumericDocValuesField(
                LogDocumentMapper.TIMESTAMP,
                thirdTime.toEpochMilli()
        ));
        info.add(new SortedDocValuesField(
                LogDocumentMapper.LEVEL,
                new BytesRef("INFO")
        ));

        indexManager.addDocuments(List.of(error1, error2, info));
        indexManager.commitNow();
        indexManager.refreshNow();

        LuceneLogSearchService searchService =
                new LuceneLogSearchService(indexManager);

        LogQuery query = new LogQuery(
                null,
                null,
                null,
                base,
                base.plusSeconds(120),
                0,
                100
        );

        List<TimeBucket> buckets =
                searchService.histogram(query, Duration.ofMinutes(1));

        assertEquals(2, buckets.size());
        assertEquals(2L, buckets.get(0).count());
        assertEquals(1L, buckets.get(1).count());

    }
}

