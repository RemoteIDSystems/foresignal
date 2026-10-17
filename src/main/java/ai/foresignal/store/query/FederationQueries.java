package ai.foresignal.store.query;

import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RestrictionClamp;
import ai.foresignal.stream.ChangeBus;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * The read queries of the federation feed (prompt 113): whole versions in seq order with their ingest time and
 * batch ({@code record_version}), current heads pinned to a watermark ({@code record_head ⋈ record_live}, HIST-2),
 * and versions by seq for the edge relay's push sink. Every query selects the whole payload and GeoJSON
 * geometry, because a federation item is the complete version; the caller decides egress row by row.
 */
@Component
public class FederationQueries {

    /** The GeoJSON precision of federation geometry (decimal places; about 1 cm). */
    static final int PRECISION = 7;

    private final ReadQueryRunner runner;
    private final ChangeBus changeBus;
    private final RecordQueryService queries;

    /** Creates the queries. */
    public FederationQueries(ReadQueryRunner runner, ChangeBus changeBus, RecordQueryService queries) {
        this.runner = runner;
        this.changeBus = changeBus;
        this.queries = queries;
    }

    /**
     * The row filter a federation caller passes.
     *
     * @param sources source filter, empty for all
     * @param kinds kind filter, empty for all
     * @param bbox spatial filter, or null
     * @param excludedSources sources never read (private sources and sources the caller may not see)
     * @param restriction the token's read restriction (sources, kinds, bbox, area)
     */
    public record Filter(List<String> sources, List<String> kinds, BboxFilter bbox, Set<String> excludedSources,
            ReadRestriction restriction) {

        /** Normalizes nulls. */
        public Filter {
            sources = sources == null ? List.of() : List.copyOf(sources);
            kinds = kinds == null ? List.of() : List.copyOf(kinds);
            excludedSources = excludedSources == null ? Set.of() : Set.copyOf(excludedSources);
            restriction = restriction == null ? ReadRestriction.NONE : restriction;
        }

        /** No filter at all (the push sink's by-seq read applies egress itself). */
        public static Filter none() {
            return new Filter(List.of(), List.of(), null, Set.of(), ReadRestriction.NONE);
        }
    }

    /**
     * One page of versions.
     *
     * @param rows the versions, in seq order
     * @param lastSeq pass as {@code after} next time
     * @param hasMore true when more versions follow
     * @param watermarkSeq the published watermark read before the query
     */
    public record ChangeRows(List<RecordRow> rows, long lastSeq, boolean hasMore, long watermarkSeq) {}

    /**
     * One page of current heads.
     *
     * @param rows the heads, ordered by {@code (source_id, external_id)}
     * @param hasMore true when more heads follow
     */
    public record SnapshotRows(List<RecordRow> rows, boolean hasMore) {}

    /** The published watermark. */
    public long watermark() {
        return changeBus.lastPublishedSeq();
    }

    /** The purge horizon over {@code sources} (all sources when empty): below it a feed must resync. */
    public long purgeHorizon(Collection<String> sources) {
        return queries.purgeHorizon(sources);
    }

    /**
     * The versions with {@code seq > after}, at most {@code limit}. When no more follow, {@code lastSeq} moves up
     * to the watermark read before the query, so a filtered consumer skips past the gaps (as {@code /v1/changes}).
     */
    public ChangeRows changes(long after, int limit, Filter filter) {
        long watermark = changeBus.lastPublishedSeq();
        List<RecordRow> rows = runner.read(jdbc -> {
            SqlParams p = new SqlParams();
            List<String> where = new ArrayList<>();
            where.add("v.seq > " + p.bigint(after));
            addFilter(where, filter, "v.source_id", "v.kind", "v.geom", "v.payload", p);
            String sql = "SELECT " + versionSelect(p) + " FROM record_version v WHERE " + String.join(" AND ", where)
                    + " ORDER BY v.seq LIMIT " + (limit + 1);
            return jdbc.query(sql, p.source(), RecordRowMapper.INSTANCE);
        });
        boolean hasMore = rows.size() > limit;
        if (hasMore) {
            rows = new ArrayList<>(rows.subList(0, limit));
        }
        long lastSeq = rows.isEmpty() ? after : rows.getLast().seq();
        if (!hasMore) {
            lastSeq = Math.max(lastSeq, Math.max(after, watermark));
        }
        return new ChangeRows(rows, lastSeq, hasMore, watermark);
    }

    /**
     * Current (unexpired at {@code now}), non-tombstoned heads whose seq is at most {@code atSeq}, after the keyset
     * {@code (afterSource, afterExternal)} (null for the first page), at most {@code limit}. Heads updated after
     * {@code atSeq} are left out: their newer versions follow in the change feed from {@code atSeq}.
     */
    public SnapshotRows snapshot(long atSeq, String afterSource, String afterExternal, int limit, Filter filter,
            java.time.Instant now) {
        List<RecordRow> rows = runner.read(jdbc -> {
            SqlParams p = new SqlParams();
            List<String> where = new ArrayList<>();
            where.add("h.seq <= " + p.bigint(atSeq));
            where.add("NOT h.tombstone");
            where.add("(l.valid_until IS NULL OR l.valid_until > " + p.instant(now) + ")");
            if (afterSource != null && afterExternal != null) {
                where.add("(h.source_id, h.external_id) > (" + p.text(afterSource) + ", " + p.text(afterExternal) + ")");
            }
            addFilter(where, filter, "h.source_id", "h.kind", "l.geom", RecordQueryService.CURRENT_PAYLOAD, p);
            SelectList select = new SelectList()
                    .put("source_id", "h.source_id")
                    .put("external_id", "h.external_id")
                    .put("kind", "h.kind")
                    .put("version", "h.version")
                    .put("seq", "h.seq")
                    .put("observed_at", "l.observed_at")
                    .put("persisted_observed_at", "h.persisted_observed_at")
                    .put("event_time", "h.event_time")
                    .put("valid_from", "h.valid_from")
                    .put("valid_until", "h.valid_until_explicit")
                    .put("tombstone", "h.tombstone")
                    .put("title", "h.title")
                    .put("geom_out", "ST_AsGeoJSON(l.geom, " + PRECISION + ")")
                    .put("payload_out", "(" + RecordQueryService.CURRENT_PAYLOAD + ")::text")
                    .put("quality_out", "l.quality::text")
                    .put("qa_flags", "l.qa_flags::integer")
                    .put("ingested_at", "(SELECT v.ingested_at FROM record_version v WHERE v.seq = h.seq LIMIT 1)");
            String sql = "SELECT " + select.sql() + " FROM record_head h JOIN record_live l"
                    + " ON l.source_id = h.source_id AND l.external_id = h.external_id WHERE "
                    + String.join(" AND ", where) + " ORDER BY h.source_id, h.external_id LIMIT " + (limit + 1);
            return jdbc.query(sql, p.source(), RecordRowMapper.INSTANCE);
        });
        boolean hasMore = rows.size() > limit;
        return new SnapshotRows(hasMore ? new ArrayList<>(rows.subList(0, limit)) : rows, hasMore);
    }

    /** The versions of {@code seqs}, in seq order (seqs no longer stored are absent). */
    public List<RecordRow> bySeq(Collection<Long> seqs) {
        if (seqs.isEmpty()) {
            return List.of();
        }
        return runner.read(jdbc -> {
            SqlParams p = new SqlParams();
            String sql = "SELECT " + versionSelect(p) + " FROM record_version v WHERE v.seq = ANY("
                    + p.bigintArray(new ArrayList<>(seqs)) + ") ORDER BY v.seq";
            return jdbc.query(sql, p.source(), RecordRowMapper.INSTANCE);
        });
    }

    /** The heads of {@code sourceId} that are live and not tombstoned, as {@code externalId → seq}. */
    public Map<String, Long> liveHeads(String sourceId) {
        return runner.read(jdbc -> {
            SqlParams p = new SqlParams();
            Map<String, Long> out = new java.util.LinkedHashMap<>();
            jdbc.query("SELECT h.external_id, h.seq FROM record_head h WHERE h.source_id = " + p.text(sourceId)
                    + " AND NOT h.tombstone", p.source(), (org.springframework.jdbc.core.RowCallbackHandler) rs ->
                    out.put(rs.getString(1), rs.getLong(2)));
            return out;
        });
    }

    private static String versionSelect(SqlParams p) {
        return new SelectList()
                .put("source_id", "v.source_id")
                .put("external_id", "v.external_id")
                .put("kind", "v.kind")
                .put("version", "v.version")
                .put("seq", "v.seq")
                .put("observed_at", "v.observed_at")
                .put("persisted_observed_at", "v.observed_at")
                .put("event_time", "v.event_time")
                .put("valid_from", "v.valid_from")
                .put("valid_until", "v.valid_until_explicit")
                .put("tombstone", "v.tombstone")
                .put("geom_out", "ST_AsGeoJSON(v.geom, " + PRECISION + ")")
                .put("payload_out", "v.payload::text")
                .put("quality_out", "v.quality::text")
                .put("qa_flags", "v.qa_flags::integer")
                .put("ingested_at", "v.ingested_at")
                .put("received_at", "v.received_at")
                .put("persist_reason", "v.persist_reason")
                .put("prev_seq", "v.prev_seq")
                .put("history_tier", "v.history_tier")
                .put("batch_id", "v.batch_id")
                .sql();
    }

    private static void addFilter(List<String> where, Filter filter, String sourceColumn, String kindColumn,
            String geomColumn, String payloadColumn, SqlParams p) {
        RecordQueryService.addIn(where, sourceColumn, filter.sources(), p);
        RecordQueryService.addIn(where, kindColumn, filter.kinds(), p);
        RecordQueryService.addNotIn(where, sourceColumn, filter.excludedSources(), p);
        if (filter.bbox() != null) {
            where.add(RecordQueryService.bboxClause(geomColumn, filter.bbox(), p));
        }
        RestrictionClamp.addTo(where, sourceColumn, kindColumn, geomColumn, payloadColumn, filter.restriction(), p);
    }
}
