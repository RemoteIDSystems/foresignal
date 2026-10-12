package ai.foresignal.search;

import ai.foresignal.ingest.PersistReason;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.PgArrays;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Loads record versions and heads for embedding (prompt 027) and turns them into
 * {@link DocInput}s.
 *
 * <p>Versions are loaded by {@code seq = ANY(?)} (one leaf per tier after runtime pruning) or by a
 * seq range restricted to eligible sources; {@code HEARTBEAT} versions are never loaded, because a
 * heartbeat repeats the persisted content.
 */
@Component
public class VersionRows {

    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};

    static final String VERSION_COLUMNS = "v.seq, v.source_id, v.external_id, v.kind, v.tombstone, v.observed_at,"
            + " v.event_time, encode(ST_AsEWKB(v.geom), 'hex') AS geom_hex, v.payload::text AS payload";

    static final String HEAD_COLUMNS = "h.seq, h.source_id, h.external_id, h.kind, h.tombstone,"
            + " h.persisted_observed_at AS observed_at, h.event_time,"
            + " encode(ST_AsEWKB(h.persisted_geom), 'hex') AS geom_hex, h.payload::text AS payload";

    /**
     * One version (or a head, which carries its persisted version).
     *
     * @param seq the seq
     * @param sourceId the source
     * @param externalId the entity
     * @param kind the kind
     * @param tombstone true for a tombstone
     * @param observedAt the observation time
     * @param eventTime the event time, or null
     * @param geomHex the geometry as hex EWKB, or null
     * @param payload the payload JSON
     */
    public record Row(long seq, String sourceId, String externalId, String kind, boolean tombstone, Instant observedAt,
            Instant eventTime, String geomHex, String payload) {

        /** {@code <source>:<externalId>}. */
        public String docId() {
            return DocInput.recordId(sourceId, externalId);
        }
    }

    static final RowMapper<Row> MAPPER = (rs, i) -> new Row(
            rs.getLong("seq"),
            rs.getString("source_id"),
            rs.getString("external_id"),
            rs.getString("kind"),
            rs.getBoolean("tombstone"),
            EmbeddingStore.instant(rs.getTimestamp("observed_at")),
            EmbeddingStore.instant(rs.getTimestamp("event_time")),
            rs.getString("geom_hex"),
            rs.getString("payload"));

    private final JdbcTemplate jdbc;
    private final JsonMapper json;

    /** The region tags appended to the embedding text (prompt 040); null in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.reference.RegionTagStore regions;

    /** Creates the loader. */
    public VersionRows(JdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    /** The versions of {@code seqs} ({@code WHERE seq = ANY(?)}), in seq order. */
    public List<Row> bySeq(Collection<Long> seqs) {
        if (seqs.isEmpty()) {
            return List.of();
        }
        return jdbc.query("SELECT " + VERSION_COLUMNS + " FROM record_version v WHERE v.seq = ANY(CAST(? AS bigint[]))"
                + " ORDER BY v.seq", MAPPER, PgArrays.bigint(seqs));
    }

    /** The non-heartbeat versions of {@code sources} with {@code from < seq <= to}, in seq order. */
    public List<Row> range(long from, long to, Set<String> sources) {
        if (sources.isEmpty() || to <= from) {
            return List.of();
        }
        return jdbc.query("SELECT " + VERSION_COLUMNS + " FROM record_version v WHERE v.seq > ? AND v.seq <= ?"
                        + " AND v.source_id = ANY(CAST(? AS text[])) AND v.persist_reason <> ? ORDER BY v.seq",
                MAPPER, from, to, PgArrays.text(sources), PersistReason.HEARTBEAT.code());
    }

    /**
     * Current, non-tombstoned heads of {@code sources} after the keyset position
     * {@code (afterSource, afterExternalId)}, in key order (the reindex backfill).
     */
    public List<Row> heads(Set<String> sources, String afterSource, String afterExternalId, int limit) {
        if (sources.isEmpty()) {
            return List.of();
        }
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT " + HEAD_COLUMNS + " FROM record_head h JOIN record_live l"
                + " ON l.source_id = h.source_id AND l.external_id = h.external_id"
                + " WHERE h.source_id = ANY(CAST(? AS text[])) AND NOT h.tombstone");
        args.add(PgArrays.text(sources));
        if (afterSource != null) {
            sql.append(" AND (h.source_id, h.external_id) > (?, ?)");
            args.add(afterSource);
            args.add(afterExternalId);
        }
        sql.append(" ORDER BY h.source_id, h.external_id LIMIT ").append(limit);
        return jdbc.query(sql.toString(), MAPPER, args.toArray());
    }

    /** The current, non-tombstoned head of one record, or null (prompt 040's re-embedding). */
    public Row head(String sourceId, String externalId) {
        List<Row> rows = jdbc.query("SELECT " + HEAD_COLUMNS + " FROM record_head h WHERE h.source_id = ?"
                + " AND h.external_id = ? AND NOT h.tombstone", MAPPER, sourceId, externalId);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    /** How many current, non-tombstoned heads {@code sources} have. */
    public long countHeads(Set<String> sources) {
        if (sources.isEmpty()) {
            return 0;
        }
        Long n = jdbc.queryForObject("SELECT count(*) FROM record_head h JOIN record_live l"
                + " ON l.source_id = h.source_id AND l.external_id = h.external_id"
                + " WHERE h.source_id = ANY(CAST(? AS text[])) AND NOT h.tombstone", Long.class, PgArrays.text(sources));
        return n == null ? 0 : n;
    }

    /** The latest row per entity of {@code rows} (which are in seq order). */
    public static List<Row> latestPerEntity(List<Row> rows) {
        Map<String, Row> latest = new LinkedHashMap<>();
        for (Row row : rows) {
            latest.merge(row.docId(), row, (a, b) -> b.seq() >= a.seq() ? b : a);
        }
        return new ArrayList<>(latest.values());
    }

    /**
     * The document of {@code row} for {@code descriptor}, or null when none of its embed fields has
     * a value (or it is a tombstone).
     */
    public DocInput document(Row row, SourceDescriptor descriptor) {
        if (row.tombstone()) {
            return null;
        }
        Map<String, Object> payload = row.payload() == null ? Map.of() : json.readValue(row.payload(), MAP);
        String text = EmbeddingText.of(row.kind(), descriptor.name() == null ? descriptor.id() : descriptor.name(),
                payload, descriptor.embedFields());
        if (text == null) {
            return null;
        }
        if (regions != null) {
            // Prompt 040: "Near: … Region: …" when the record is tagged.
            text = EmbeddingText.withRegion(text, regions.find(row.sourceId(), row.externalId())
                    .map(ai.foresignal.reference.RegionTagStore.Stored::tags).orElse(null));
        }
        Instant effective = row.eventTime() != null ? row.eventTime() : row.observedAt();
        return new DocInput(DocInput.RECORD, row.docId(), row.sourceId(), row.kind(), row.seq(), row.geomHex(),
                effective, row.observedAt(), text);
    }
}
