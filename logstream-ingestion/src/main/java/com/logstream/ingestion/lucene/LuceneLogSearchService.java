package com.logstream.ingestion.lucene;

import com.logstream.common.dto.LogRecord;
import com.logstream.common.search.LogField;
import com.logstream.common.search.LogQuery;
import com.logstream.common.search.LogSearchService;
import com.logstream.common.search.SearchResult;
import com.logstream.common.search.TimeBucket;
import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CollectorManager;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.SimpleCollector;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.document.LongPoint;
import org.springframework.stereotype.Service;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.MatchNoDocsQuery;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Member 1 (Log Ingestion & Lucene Core) - Week 2 and Week 3
 * Lucene implementation of LogSearchService: search (Week 2), histogram and group-by counts (Week 3).
 */
@Service
public class LuceneLogSearchService implements LogSearchService {
    static final int MAX_BUCKETS = 10_000;

    private static final Sort NEWEST_FIRST = new Sort(
            new SortField(LogDocumentMapper.TIMESTAMP, SortField.Type.LONG, true),
            SortField.FIELD_DOC);

    private final LuceneIndexManager index;

    public LuceneLogSearchService(LuceneIndexManager index) {
        this.index = index;
    }

    // Week 2: full-text search with filters, newest first
    @Override
    public SearchResult search(LogQuery query) {
        long start = System.nanoTime();
        Query q = toLucene(query);
        return withSearcher(searcher -> {
            int window = query.offset() + query.limit();
            TopDocs top = searcher.search(q, window, NEWEST_FIRST);
            long total = searcher.count(q);
            StoredFields stored = searcher.storedFields();
            List<LogRecord> logs = new ArrayList<>(query.limit());
            ScoreDoc[] hits = top.scoreDocs;
            for (int i = query.offset(); i < hits.length; i++) {
                logs.add(LogDocumentMapper.fromDocument(stored.document(hits[i].doc)));
            }
            return new SearchResult(total, logs, (System.nanoTime() - start) / 1_000_000);
        });
    }

    // Week 2: number of matching logs, used by alert thresholds
    @Override
    public long count(LogQuery query) {
        Query q = toLucene(query);
        return withSearcher(searcher -> (long) searcher.count(q));
    }

    // Week 3: log counts per time slot, e.g. per minute
    @Override
    public List<TimeBucket> histogram(LogQuery query, Duration interval) {
        if (interval.compareTo(Duration.ofSeconds(1)) < 0) {
            throw new IllegalArgumentException("interval must be at least 1 second");
        }
        long step = interval.toMillis();
        if (query.from() != null && query.to() != null) {
            checkBucketCount(query.from().toEpochMilli(), query.to().toEpochMilli(), step);
        }
        Query q = toLucene(query);

        Map<Long, Map<String, Long>> counts = withSearcher(searcher ->
                searcher.search(q, new HistogramCollectorManager(step)));

        long first;
        long endExclusive;
        if (query.from() != null && query.to() != null) {
            first = floor(query.from().toEpochMilli(), step);
            endExclusive = query.to().toEpochMilli();
        } else if (!counts.isEmpty()) {
            TreeMap<Long, Map<String, Long>> sorted = new TreeMap<>(counts);
            first = query.from() != null ? floor(query.from().toEpochMilli(), step) : sorted.firstKey();
            endExclusive = query.to() != null ? query.to().toEpochMilli() : sorted.lastKey() + step;
            checkBucketCount(first, endExclusive, step);
        } else {
            return List.of();
        }

        List<TimeBucket> buckets = new ArrayList<>();
        for (long t = first; t < endExclusive; t += step) {
            Map<String, Long> byLevel = counts.getOrDefault(t, Map.of());
            long total = byLevel.values().stream().mapToLong(Long::longValue).sum();
            buckets.add(new TimeBucket(Instant.ofEpochMilli(t), total, byLevel));
        }
        return buckets;
    }

    // Week 3: log counts grouped by level, service or host
    @Override
    public Map<String, Long> countBy(LogField field, LogQuery query) {
        Query q = toLucene(query);
        Map<String, Long> counts = withSearcher(searcher ->
                searcher.search(q, new TermCountCollectorManager(field.indexName())));
        Map<String, Long> sorted = new LinkedHashMap<>();
        counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .forEach(e -> sorted.put(e.getKey(), e.getValue()));
        return sorted;
    }

    // Week 2: converts LogQuery filters into a Lucene query
    Query toLucene(LogQuery query) {
        BooleanQuery.Builder b = new BooleanQuery.Builder();
        boolean any = false;

        if (query.text() != null && !query.text().isBlank()) {
            b.add(parseText(query.text()), Occur.MUST);
            any = true;
        }
        any |= addAnyOf(b, LogDocumentMapper.LEVEL, query.levels());
        any |= addAnyOf(b, LogDocumentMapper.SERVICE, query.services());
        if (query.from() != null || query.to() != null) {
            long lo = query.from() != null ? query.from().toEpochMilli() : Long.MIN_VALUE;
            long hi = query.to() != null ? query.to().toEpochMilli() - 1 : Long.MAX_VALUE;
            b.add(LongPoint.newRangeQuery(LogDocumentMapper.TIMESTAMP, lo, hi), Occur.FILTER);
            any = true;
        }
        return any ? b.build() : new MatchAllDocsQuery();
    }


    private Query parseText(String text) {
        QueryParser parser = new QueryParser(
                LogDocumentMapper.MESSAGE,
                index.analyzer()
        );
        parser.setDefaultOperator(QueryParser.Operator.AND);


        try {
            return parser.parse(text.replaceAll("\\s+", " "));
        } catch (ParseException e) {
            try {
                return parser.parse(QueryParser.escape(text));
            } catch (ParseException ignored) {
                return new MatchNoDocsQuery();
            }
        }


    }


    private static boolean addAnyOf(BooleanQuery.Builder b, String field, Set<String> values) {
        if (values.isEmpty()) {
            return false;
        }
        BooleanQuery.Builder anyOf = new BooleanQuery.Builder();
        for (String v : values) {
            anyOf.add(new TermQuery(new Term(field, v)), Occur.SHOULD);
        }
        b.add(anyOf.build(), Occur.FILTER);
        return true;
    }

    private static long floor(long millis, long step) {
        return Math.floorDiv(millis, step) * step;
    }

    private static void checkBucketCount(long from, long to, long step) {
        if ((to - floor(from, step)) / step > MAX_BUCKETS) {
            throw new IllegalArgumentException(
                    "Too many histogram buckets (max " + MAX_BUCKETS + "): use a larger interval or a smaller time range");
        }
    }

    private interface SearcherFunction<T> {
        T apply(IndexSearcher searcher) throws IOException;
    }

    private <T> T withSearcher(SearcherFunction<T> fn) {
        IndexSearcher searcher;
        try {
            searcher = index.acquire();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        try {
            return fn.apply(searcher);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            index.release(searcher);
        }
    }

    // Week 3: collectors that compute aggregations from doc values
    private static final class HistogramCollectorManager
            implements CollectorManager<HistogramCollector, Map<Long, Map<String, Long>>> {
        private final long step;

        HistogramCollectorManager(long step) {
            this.step = step;
        }

        @Override
        public HistogramCollector newCollector() {
            return new HistogramCollector(step);
        }

        @Override
        public Map<Long, Map<String, Long>> reduce(Collection<HistogramCollector> collectors) {
            Map<Long, Map<String, Long>> merged = new HashMap<>();
            for (HistogramCollector c : collectors) {
                c.counts.forEach((bucket, levels) -> levels.forEach((level, n) ->
                        merged.computeIfAbsent(bucket, k -> new HashMap<>()).merge(level, n, Long::sum)));
            }
            return merged;
        }
    }

    private static final class HistogramCollector extends SimpleCollector {
        private final long step;
        private final Map<Long, Map<String, Long>> counts = new HashMap<>();
        private NumericDocValues timestamps;
        private SortedDocValues levels;

        HistogramCollector(long step) {
            this.step = step;
        }

        @Override
        protected void doSetNextReader(LeafReaderContext context) throws IOException {
            timestamps = DocValues.getNumeric(context.reader(), LogDocumentMapper.TIMESTAMP);
            levels = DocValues.getSorted(context.reader(), LogDocumentMapper.LEVEL);
        }

        @Override
        public void collect(int doc) throws IOException {
            if (!timestamps.advanceExact(doc)) {
                return;
            }
            long bucket = floor(timestamps.longValue(), step);
            String level = levels.advanceExact(doc) ? levels.lookupOrd(levels.ordValue()).utf8ToString() : "UNKNOWN";
            counts.computeIfAbsent(bucket, k -> new HashMap<>()).merge(level, 1L, Long::sum);
        }

        @Override
        public ScoreMode scoreMode() {
            return ScoreMode.COMPLETE_NO_SCORES;
        }
    }

    private static final class TermCountCollectorManager
            implements CollectorManager<TermCountCollector, Map<String, Long>> {
        private final String field;

        TermCountCollectorManager(String field) {
            this.field = field;
        }

        @Override
        public TermCountCollector newCollector() {
            return new TermCountCollector(field);
        }

        @Override
        public Map<String, Long> reduce(Collection<TermCountCollector> collectors) throws IOException {
            Map<String, Long> merged = new HashMap<>();
            for (TermCountCollector c : collectors) {
                c.finishSegment();
                c.counts.forEach((k, v) -> merged.merge(k, v, Long::sum));
            }
            return merged;
        }
    }

    private static final class TermCountCollector extends SimpleCollector {
        private final String field;
        private final Map<String, Long> counts = new HashMap<>();
        private SortedDocValues values;
        private long[] ordCounts;

        TermCountCollector(String field) {
            this.field = field;
        }

        @Override
        protected void doSetNextReader(LeafReaderContext context) throws IOException {
            finishSegment();
            values = DocValues.getSorted(context.reader(), field);
            ordCounts = new long[values.getValueCount()];
        }

        @Override
        public void collect(int doc) throws IOException {
            if (values.advanceExact(doc)) {
                ordCounts[values.ordValue()]++;
            }
        }

        void finishSegment() throws IOException {
            if (ordCounts == null) {
                return;
            }
            for (int ord = 0; ord < ordCounts.length; ord++) {
                if (ordCounts[ord] > 0) {
                    counts.merge(values.lookupOrd(ord).utf8ToString(), ordCounts[ord], Long::sum);
                }
            }
            ordCounts = null;
        }

        @Override
        public ScoreMode scoreMode() {
            return ScoreMode.COMPLETE_NO_SCORES;
        }
    }
}
