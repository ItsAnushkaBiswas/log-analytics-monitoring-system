package com.logstream.ingestion.lucene;

import com.logstream.common.dto.LogRecord;
import com.logstream.common.search.LogField;
import com.logstream.common.search.LogQuery;
import com.logstream.common.search.SearchResult;
import com.logstream.common.search.TimeBucket;
import com.logstream.ingestion.TestIndexes;
import org.apache.lucene.document.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LuceneLogSearchServiceTest {
    private static final Instant T0 = Instant.parse("2026-10-04T10:00:00Z");

    @TempDir
    Path dir;

    private LuceneIndexManager index;
    private LuceneLogSearchService search;

    @BeforeEach
    void setUp() throws Exception {
        index = new LuceneIndexManager(TestIndexes.properties(dir));
        search = new LuceneLogSearchService(index);
        index(
                log("1", 0, "INFO", "auth", "pod-1", "user alice logged in"),
                log("2", 10, "ERROR", "payment", "pod-2", "card declined for order 42"),
                log("3", 70, "ERROR", "payment", "pod-2", "gateway timeout calling bank"),
                log("4", 75, "WARN", "payment", "pod-3", "retrying bank call"),
                log("5", 130, "INFO", "auth", "pod-1", "user bob logged out"),
                log("6", 190, "ERROR", "auth", "pod-1", "database connection timeout"));
    }

    @AfterEach
    void tearDown() throws Exception {
        index.close();
    }

    @Test
    void allLogsNewestFirst() {
        SearchResult r = search.search(LogQuery.all());

        assertThat(r.total()).isEqualTo(6);
        assertThat(r.logs()).extracting(LogRecord::id).containsExactly("6", "5", "4", "3", "2", "1");
    }

    @Test
    void fullTextSearch() {
        assertThat(ids(LogQuery.all().withText("timeout"))).containsExactly("6", "3");
        assertThat(ids(LogQuery.all().withText("\"card declined\""))).containsExactly("2");
        assertThat(ids(LogQuery.all().withText("timeout AND NOT database"))).containsExactly("3");

        assertThat(ids(LogQuery.all().withText("bank timeout"))).containsExactly("3");

        assertThat(ids(LogQuery.all().withText("TIMEOUT"))).containsExactly("6", "3");
    }

    @Test
    void fieldQueriesInText() {
        assertThat(ids(LogQuery.all().withText("level:ERROR AND service:payment"))).containsExactly("3", "2");
        assertThat(ids(LogQuery.all().withText("host:pod-3"))).containsExactly("4");
        assertThat(ids(LogQuery.all().withText("attr.env:prod"))).hasSize(6);
    }

    @Test
    void invalidSyntaxFallsBackToPlainWords() {
        assertThat(ids(LogQuery.all().withText("\"card declined"))).containsExactly("2");
    }

    @Test
    void filtersCombine() {
        LogQuery q = LogQuery.all()
                .withLevels(Set.of("ERROR", "WARN"))
                .withServices(Set.of("payment"))
                .withRange(T0.plusSeconds(60), T0.plusSeconds(120));

        assertThat(ids(q)).containsExactly("4", "3");
        assertThat(search.count(q)).isEqualTo(2);
    }

    @Test
    void rangeEndIsExclusive() {
        assertThat(ids(LogQuery.all().withRange(T0, T0.plusSeconds(70)))).containsExactly("2", "1");
    }

    @Test
    void paging() {
        SearchResult page2 = search.search(LogQuery.all().withPage(2, 2));

        assertThat(page2.total()).isEqualTo(6);
        assertThat(page2.logs()).extracting(LogRecord::id).containsExactly("4", "3");
        assertThat(search.search(LogQuery.all().withPage(10, 5)).logs()).isEmpty();
    }

    @Test
    void storedFieldsRoundTrip() {
        LogRecord r = search.search(LogQuery.all().withText("alice")).logs().getFirst();

        assertThat(r.timestamp()).isEqualTo(T0);
        assertThat(r.level()).isEqualTo("INFO");
        assertThat(r.service()).isEqualTo("auth");
        assertThat(r.host()).isEqualTo("pod-1");
        assertThat(r.attributes()).containsExactlyEntriesOf(Map.of("env", "prod"));
        assertThat(r.traceId()).isEqualTo("trace-1");
    }

    @Test
    void histogramPerMinuteFillsEmptyBuckets() {
        List<TimeBucket> buckets = search.histogram(
                LogQuery.all().withRange(T0, T0.plusSeconds(300)), Duration.ofMinutes(1));

        assertThat(buckets).extracting(TimeBucket::start).containsExactly(
                T0, T0.plusSeconds(60), T0.plusSeconds(120), T0.plusSeconds(180), T0.plusSeconds(240));
        assertThat(buckets).extracting(TimeBucket::count).containsExactly(2L, 2L, 1L, 1L, 0L);
        assertThat(buckets.get(0).byLevel()).isEqualTo(Map.of("INFO", 1L, "ERROR", 1L));
        assertThat(buckets.get(1).byLevel()).isEqualTo(Map.of("ERROR", 1L, "WARN", 1L));
        assertThat(buckets.get(4).byLevel()).isEmpty();
    }

    @Test
    void histogramWithoutRangeUsesDataRange() {
        List<TimeBucket> buckets = search.histogram(LogQuery.all().withLevels(Set.of("ERROR")), Duration.ofMinutes(1));

        assertThat(buckets).extracting(TimeBucket::count).containsExactly(1L, 1L, 0L, 1L);
        assertThat(buckets.getFirst().start()).isEqualTo(T0);
    }

    @Test
    void histogramRejectsTooManyBuckets() {
        assertThatThrownBy(() -> search.histogram(
                LogQuery.all().withRange(T0, T0.plus(Duration.ofDays(365))), Duration.ofSeconds(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void countByLevelAndService() {
        assertThat(search.countBy(LogField.LEVEL, LogQuery.all()))
                .containsExactly(Map.entry("ERROR", 3L), Map.entry("INFO", 2L), Map.entry("WARN", 1L));
        assertThat(search.countBy(LogField.SERVICE, LogQuery.all().withText("timeout")))
                .containsExactly(Map.entry("auth", 1L), Map.entry("payment", 1L));
    }

    @Test
    void aggregationsSpanMultipleSegments() throws Exception {
        index(log("7", 15, "ERROR", "search", "pod-9", "index corrupt"));

        assertThat(search.countBy(LogField.LEVEL, LogQuery.all())).containsEntry("ERROR", 4L);
        assertThat(search.histogram(LogQuery.all().withRange(T0, T0.plusSeconds(60)), Duration.ofMinutes(1))
                .getFirst().count()).isEqualTo(3);
    }

    @Test
    void indexSurvivesReopen() throws Exception {
        index.close();
        index = new LuceneIndexManager(TestIndexes.properties(dir));
        search = new LuceneLogSearchService(index);

        assertThat(search.count(LogQuery.all())).isEqualTo(6);
    }

    private List<String> ids(LogQuery q) {
        return search.search(q).logs().stream().map(LogRecord::id).toList();
    }

    private void index(LogRecord... logs) throws Exception {
        List<Document> docs = java.util.Arrays.stream(logs).map(LogDocumentMapper::toDocument).toList();
        index.addDocuments(docs);
        index.commitNow();
        index.refreshNow();
    }

    private static LogRecord log(String id, int secondsAfterT0, String level, String service, String host, String msg) {
        return new LogRecord(id, T0.plusSeconds(secondsAfterT0), level, service, host, msg,
                Map.of("env", "prod"), "trace-" + id);
    }
}
