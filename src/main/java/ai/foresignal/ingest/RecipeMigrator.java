package ai.foresignal.ingest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Geometry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Lazy per-source hash-recipe migration (Core-Data-Model §4), run on the writer thread before a
 * batch when a source's recipe may have changed (its volatile or significant field list was
 * edited).
 *
 * <p>Each stale head is migrated in chunks of {@value #CHUNK}, each in its own transaction, so a
 * large source never holds one long transaction:
 *
 * <ul>
 *   <li>{@code h.static_hash} is recomputed from the head columns. That is exact, because the head
 *       holds the full persisted state, geometry included.
 *   <li>{@code l.kinematic} becomes the kinematic subset of the current payload
 *       {@code jsonb_strip_nulls(h.payload || l.kinematic)} under the new recipe, plus a JSON null
 *       for every key of {@code h.payload} that is kinematic under the new recipe but absent from
 *       the current payload; without that mask a key the entity dropped live would reappear.
 *   <li>{@code l.full_hash} is recomputed from the current payload and {@code l.geom}.
 *   <li>{@code h.hash_recipe} is set; {@code dirty} is left unchanged.
 * </ul>
 */
final class RecipeMigrator {

    /** Heads migrated per transaction. */
    static final int CHUNK = 2_000;

    private static final String NEEDED = "SELECT 1 FROM record_head WHERE source_id = ? AND hash_recipe <> ? LIMIT 1";

    private static final String SELECT_CHUNK =
            """
            SELECT h.external_id, h.kind, h.event_time, h.valid_until_explicit, h.valid_from, h.tombstone,
                   h.payload::text, h.persisted_geom IS NULL, l.external_id IS NOT NULL,
                   jsonb_strip_nulls(h.payload || l.kinematic)::text, ST_AsBinary(l.geom)
              FROM record_head h LEFT JOIN record_live l USING (source_id, external_id)
             WHERE h.source_id = ? AND h.hash_recipe <> ?
             LIMIT ?
            """;

    private static final String UPDATE_HEAD =
            "UPDATE record_head SET static_hash = ?, hash_recipe = ? WHERE source_id = ? AND external_id = ?";

    private static final String UPDATE_LIVE =
            "UPDATE record_live SET kinematic = ?::jsonb, full_hash = ? WHERE source_id = ? AND external_id = ?";

    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final IngestJson json;

    RecipeMigrator(JdbcTemplate jdbc, TransactionTemplate tx, IngestJson json) {
        this.jdbc = jdbc;
        this.tx = tx;
        this.json = json;
    }

    /** True when some head of the source was hashed under a different recipe. */
    boolean needed(SourceIngestProfile profile) {
        return !jdbc.queryForList(NEEDED, Integer.class, profile.sourceId(), profile.recipe()).isEmpty();
    }

    /**
     * Migrates every stale head of the source.
     *
     * @return the number of heads migrated
     */
    int migrate(SourceIngestProfile profile) {
        int total = 0;
        while (true) {
            Integer migrated = tx.execute(status -> migrateChunk(profile));
            int count = migrated == null ? 0 : migrated;
            total += count;
            if (count < CHUNK) {
                return total;
            }
        }
    }

    private int migrateChunk(SourceIngestProfile profile) {
        List<Stale> stale =
                jdbc.query(
                        SELECT_CHUNK,
                        (rs, row) -> stale(rs),
                        profile.sourceId(),
                        profile.recipe(),
                        CHUNK);
        if (stale.isEmpty()) {
            return 0;
        }
        List<Object[]> heads = new ArrayList<>(stale.size());
        List<Object[]> lives = new ArrayList<>(stale.size());
        Set<String> volatileFields = profile.volatileFields();
        for (Stale s : stale) {
            long staticHash =
                    ContentHasher.staticHash(
                            s.kind, s.eventTime, s.validUntilExplicit, s.validFrom, s.tombstone, s.geomIsNull,
                            s.payload, volatileFields, profile.significantFields());
            heads.add(new Object[] {staticHash, profile.recipe(), profile.sourceId(), s.externalId});
            if (s.hasLive) {
                Map<String, Object> current = s.tombstone ? Map.of() : s.current;
                long fullHash =
                        ContentHasher.fullHash(
                                s.kind, s.eventTime, s.validUntilExplicit, s.validFrom, s.tombstone, s.liveGeometry,
                                current, volatileFields);
                Map<String, Object> kinematic = s.tombstone ? Map.of() : kinematic(s.payload, current, profile);
                lives.add(new Object[] {json.write(kinematic), fullHash, profile.sourceId(), s.externalId});
            }
        }
        jdbc.batchUpdate(UPDATE_HEAD, heads);
        if (!lives.isEmpty()) {
            jdbc.batchUpdate(UPDATE_LIVE, lives);
        }
        return stale.size();
    }

    /**
     * The new {@code kinematic}: the kinematic subset of the current payload, plus a JSON null for
     * each kinematic key of the persisted payload the current payload lacks.
     */
    static Map<String, Object> kinematic(
            Map<String, Object> persisted, Map<String, Object> current, SourceIngestProfile profile) {
        Map<String, Object> result = new LinkedHashMap<>(ContentHasher.kinematicSubset(current, profile));
        for (String key : ContentHasher.kinematicSubset(persisted, profile).keySet()) {
            if (!current.containsKey(key)) {
                result.put(key, null);
            }
        }
        return result;
    }

    private Stale stale(ResultSet rs) throws SQLException {
        Stale s = new Stale();
        s.externalId = rs.getString(1);
        s.kind = rs.getString(2);
        s.eventTime = JdbcValues.instant(rs, 3);
        s.validUntilExplicit = JdbcValues.instant(rs, 4);
        s.validFrom = JdbcValues.instant(rs, 5);
        s.tombstone = rs.getBoolean(6);
        s.payload = json.readPayload(rs.getString(7));
        s.geomIsNull = rs.getBoolean(8);
        s.hasLive = rs.getBoolean(9);
        s.current = json.readPayload(rs.getString(10));
        s.liveGeometry = IngestJson.geometry(rs.getBytes(11));
        return s;
    }

    private static final class Stale {
        String externalId;
        String kind;
        Instant eventTime;
        Instant validUntilExplicit;
        Instant validFrom;
        boolean tombstone;
        Map<String, Object> payload;
        boolean geomIsNull;
        boolean hasLive;
        Map<String, Object> current;
        Geometry liveGeometry;
    }
}
