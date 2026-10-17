package ai.foresignal.stats;

import ai.foresignal.analytics.math.Seeds;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.store.query.PgArrays;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * Computes and writes the region rows of one {@code activity_rollup} bucket from raw {@code record_version} rows
 * (prompt 119, Intelligence-Design §4.2).
 *
 * <p>Each version's region is the 040 tag of its record ({@code record_region}): the country code, or the ISO 3166-2
 * {@code CC-admin1} code when its source's {@code statsRegionLevel} is {@code admin1}; a record without a tag counts
 * in {@value #UNTAGGED}. Measures are those of prompt 038, HEARTBEAT and KEYFRAME versions skipped. Distinct entities
 * count a record linked to a 116 canonical event as that event, so re-reports of one earthquake count once.
 *
 * <p>Besides one row per {@code (source, kind, region)}, each {@code (kind, region)} has one {@value #ALL_SOURCES} row
 * over the non-private sources whose {@code entities} is exact across sources (a canonical event reported by three
 * sources counts once): the event-count series the detectors read. Writing replaces the bucket's region rows, so a
 * recomputation is idempotent.
 */
public final class RegionRollupComputer {

    /** The region of records without a 040 tag. */
    public static final String UNTAGGED = "ZZ";

    /** The pseudo source of the cross-source rows. */
    public static final String ALL_SOURCES = "*";

    /** The 040 region code pattern. */
    public static final Pattern REGION_CODE = Pattern.compile("^[A-Z]{2}(-[A-Z0-9]{1,3})?$");

    static final String SELECT = "SELECT v.source_id, v.kind, v.external_id, v.persist_reason, rr.iso2, rr.admin1,"
            + " (SELECT o.canonical_event_id FROM observation_link o WHERE o.source_id = v.source_id"
            + " AND o.external_id = v.external_id ORDER BY o.seq DESC LIMIT 1) AS canonical"
            + " FROM record_version v LEFT JOIN record_region rr ON rr.source_id = v.source_id"
            + " AND rr.external_id = v.external_id"
            + " WHERE v.seq > :lo AND v.seq <= :hi AND v.source_id = ANY(CAST(:sources AS text[]))"
            + " AND v.persist_reason NOT IN (" + RollupComputer.SKIPPED_REASONS + ")";

    static final String DELETE = "DELETE FROM activity_rollup WHERE resolution = :resolution"
            + " AND bucket_start = :bucket AND (source_id = ANY(CAST(:sources AS text[])) OR source_id = '"
            + ALL_SOURCES + "')";

    static final String INSERT = "INSERT INTO activity_rollup (resolution, bucket_start, source_id, kind, cell,"
            + " region_code, cell_lon, cell_lat, versions, entities, firsts, tombstones, edges, changes) VALUES"
            + " (:resolution, :bucket, :source, :kind, :cell, :region, NULL, NULL, :versions, :entities, :firsts,"
            + " :tombstones, :edges, :changes)";

    /**
     * One region row.
     *
     * @param sourceId the source, or {@value #ALL_SOURCES}
     * @param kind the kind
     * @param regionCode the region code
     * @param versions counted versions
     * @param entities distinct entities (canonical events count once)
     * @param firsts FIRST and REAPPEAR
     * @param tombstones TOMBSTONE
     * @param edges TRIGGER_EDGE
     * @param changes SIGNIFICANT and CHANGE
     */
    public record Row(String sourceId, String kind, String regionCode, long versions, long entities, long firsts,
            long tombstones, long edges, long changes) {}

    private record Key(String sourceId, String kind, String region) {}

    private static final class Acc {
        long versions;
        long firsts;
        long tombstones;
        long edges;
        long changes;
        final Set<String> entities = new HashSet<>();

        void add(String identity, int reason) {
            versions++;
            entities.add(identity);
            if (reason == PersistReason.FIRST.code() || reason == PersistReason.REAPPEAR.code()) {
                firsts++;
            } else if (reason == PersistReason.TOMBSTONE.code()) {
                tombstones++;
            } else if (reason == PersistReason.TRIGGER_EDGE.code()) {
                edges++;
            } else if (reason == PersistReason.SIGNIFICANT.code() || reason == PersistReason.CHANGE.code()) {
                changes++;
            }
        }
    }

    private RegionRollupComputer() {}

    /** The stable 64-bit cell value of a region row: the first eight bytes of the SHA-256 of the code. */
    public static long regionCell(String regionCode) {
        return Seeds.of("region", regionCode);
    }

    /**
     * The region of a record: its admin-1 code when {@code admin1} is wanted and valid, else its country, else
     * {@value #UNTAGGED}.
     */
    public static String region(String iso2, String admin1, boolean admin1Level) {
        if (admin1Level && admin1 != null && REGION_CODE.matcher(admin1).matches() && admin1.length() > 2) {
            return admin1;
        }
        if (iso2 != null && iso2.length() == 2 && REGION_CODE.matcher(iso2).matches()) {
            return iso2;
        }
        return UNTAGGED;
    }

    /**
     * Aggregates the versions with {@code lo < seq <= hi} of {@code sources}.
     *
     * @param admin1Sources the sources rolled up at the admin-1 level
     * @param isPrivate true for a private source (left out of the {@value #ALL_SOURCES} rows)
     */
    public static List<Row> compute(NamedParameterJdbcTemplate jdbc, long lo, long hi, Collection<String> sources,
            Set<String> admin1Sources, Predicate<String> isPrivate) {
        if (hi <= lo || sources.isEmpty()) {
            return List.of();
        }
        Map<Key, Acc> groups = new HashMap<>();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("lo", lo)
                .addValue("hi", hi)
                .addValue("sources", PgArrays.text(sources));
        jdbc.query(SELECT, params, rs -> {
            String source = rs.getString(1);
            String kind = rs.getString(2);
            String externalId = rs.getString(3);
            int reason = rs.getInt(4);
            String region = region(rs.getString(5), rs.getString(6), admin1Sources.contains(source));
            String canonical = rs.getString(7);
            String own = canonical != null ? "c:" + canonical : "e:" + externalId;
            groups.computeIfAbsent(new Key(source, kind, region), k -> new Acc()).add(own, reason);
            if (!isPrivate.test(source)) {
                String across = canonical != null ? "c:" + canonical : "e:" + source + '\u001f' + externalId;
                groups.computeIfAbsent(new Key(ALL_SOURCES, kind, region), k -> new Acc()).add(across, reason);
            }
        });
        List<Row> rows = new ArrayList<>(groups.size());
        groups.forEach((key, acc) -> rows.add(new Row(key.sourceId(), key.kind(), key.region(), acc.versions,
                acc.entities.size(), acc.firsts, acc.tombstones, acc.edges, acc.changes)));
        return rows;
    }

    /** Replaces the region rows of {@code sources} (and the {@value #ALL_SOURCES} rows) in the bucket with {@code rows}. */
    public static int write(NamedParameterJdbcTemplate jdbc, RegionRollupResolution resolution, Instant bucket,
            Collection<String> sources, List<Row> rows) {
        OffsetDateTime start = OffsetDateTime.ofInstant(bucket, ZoneOffset.UTC);
        jdbc.update(DELETE, new MapSqlParameterSource()
                .addValue("resolution", resolution.code())
                .addValue("bucket", start)
                .addValue("sources", PgArrays.text(sources)));
        if (rows.isEmpty()) {
            return 0;
        }
        SqlParameterSource[] batch = new SqlParameterSource[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            Row r = rows.get(i);
            batch[i] = new MapSqlParameterSource()
                    .addValue("resolution", resolution.code())
                    .addValue("bucket", start)
                    .addValue("source", r.sourceId())
                    .addValue("kind", r.kind())
                    .addValue("cell", regionCell(r.regionCode()))
                    .addValue("region", r.regionCode())
                    .addValue("versions", r.versions())
                    .addValue("entities", r.entities())
                    .addValue("firsts", r.firsts())
                    .addValue("tombstones", r.tombstones())
                    .addValue("edges", r.edges())
                    .addValue("changes", r.changes());
        }
        jdbc.batchUpdate(INSERT, batch);
        return rows.size();
    }
}
