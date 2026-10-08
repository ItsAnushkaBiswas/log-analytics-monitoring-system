package com.logstream.ingestion.lucene;

import com.logstream.ingestion.TestIndexes;
import com.logstream.ingestion.config.IngestionProperties;
import com.logstream.common.search.LogQuery;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.util.BytesRef;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class LuceneAggregationPerformanceTest {

    @Test
    void measureAggregationPerformance() throws Exception {

        Path indexDir = Files.createTempDirectory("aggregation-performance-test");

        IngestionProperties properties = TestIndexes.properties(indexDir);

        LuceneIndexManager indexManager =
                new LuceneIndexManager(properties);

        List<Document> documents = new ArrayList<>();

        Instant base = Instant.parse("2026-10-06T10:00:00Z");

        for (int i = 0; i < 10_000; i++) {

            Document document = new Document();

            long timestamp = base.plusSeconds(i % 1000).toEpochMilli();

            document.add(new LongPoint(
                    LogDocumentMapper.TIMESTAMP,
                    timestamp
            ));

            document.add(new NumericDocValuesField(
                    LogDocumentMapper.TIMESTAMP,
                    timestamp
            ));

            String level = (i % 2 == 0) ? "ERROR" : "INFO";

            document.add(new SortedDocValuesField(
                    LogDocumentMapper.LEVEL,
                    new BytesRef(level)
            ));

            documents.add(document);
        }

        indexManager.addDocuments(documents);
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

        int iterations = 100;

        // Warm-up
        for (int i = 0; i < 20; i++) {
            searchService.count(query);
            searchService.countBy(
                    com.logstream.common.search.LogField.LEVEL,
                    query
            );
            searchService.histogram(
                    query,
                    java.time.Duration.ofSeconds(10)
            );
        }

        long startTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            searchService.count(query);
        }

        long countEndTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            searchService.countBy(
                    com.logstream.common.search.LogField.LEVEL,
                    query
            );
        }

        long countByEndTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            searchService.histogram(
                    query,
                    java.time.Duration.ofSeconds(10)
            );
        }

        long histogramEndTime = System.nanoTime();

        double countAverageNs =
                (double) (countEndTime - startTime) / iterations;

        double countByAverageNs =
                (double) (countByEndTime - countEndTime) / iterations;

        double histogramAverageNs =
                (double) (histogramEndTime - countByEndTime) / iterations;

        System.out.println("Lucene aggregation performance benchmark");
        System.out.println("Documents: 10,000");
        System.out.println("Iterations: " + iterations);
        System.out.println("Average count() time (ns): " + countAverageNs);
        System.out.println("Average countBy() time (ns): " + countByAverageNs);
        System.out.println("Average histogram() time (ns): " + histogramAverageNs);
    }
}