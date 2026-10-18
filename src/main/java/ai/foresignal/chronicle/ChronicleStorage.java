package ai.foresignal.chronicle;

import ai.foresignal.ingest.IngestStateReset;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Native SQL over chronicle storage (prompt 131): the per-chronicle leaves of {@code chronicle_version}
 * ({@link ChronicleDdl}), the series points, rollups and frame pins, the capture counters, and the
 * {@code record_version} reads of the capture loop. Every write is idempotent, so a cursor restart captures each
 * version exactly once: versions conflict on {@code (chronicle_id, layer_id, seq)}, rollups and series points upsert.
 *
 * <p>After the test cleaner truncates the tables, {@link #resetState()} drops the leaves of chronicles that no longer
 * exist; the same sweep repairs a leaf left behind by an interrupted deletion.
 */
@Component
public class ChronicleStorage implements IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(ChronicleStorage.class);

    /** The {@code record_version} columns read by capture, with the record's 040 region. */
    static final String READ_COLUMNS = "v.seq, v.ingested_at, v.observed_at, v.received_at, v.event_time,"
            + " v.valid_until, v.valid_until_explicit, v.prev_seq, v.batch_id, v.version, v.history_tier,"
            + " v.persist_reason, v.qa_flags, v.tombstone, v.source_id, v.external_id, v.kind,"
            + " ST_AsBinary(v.geom) AS geom_wkb, v.payload::text AS payload, v.quality::text AS quality,"
            + " v.observer::text AS observer, v.valid_from";

    /** The insert of one captured version. */
    static final String INSERT_VERSION = "INSERT INTO " + ChronicleDdl.TABLE + " (chronicle_id, layer_id, seq,"
            + " ingested_at, observed_at, received_at, event_time, valid_until, valid_until_explicit, prev_seq, batch_id,"
            + " version, history_tier, persist_reason, qa_flags, tombstone, source_id, external_id, kind, geom, payload,"
            + " quality, observer, valid_from, backfilled, bucket_start, captured_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?,"
            + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ST_GeomFromWKB(?, 4326), CAST(? AS jsonb), CAST(? AS jsonb),"
            + " CAST(? AS jsonb), ?, ?, ?, ?) ON CONFLICT (chronicle_id, layer_id, seq) DO NOTHING";

    private final JdbcTemplate jdbc;

    /** Creates the storage. */
    public ChronicleStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ------------------------------------------------------------------------- leaves

    /** Creates the chronicle's leaf (idempotent). */
    public void createLeaf(String chronicleId) {
        jdbc.execute(ChronicleDdl.createLeaf(chronicleId));
    }

    /** Drops the chronicle's leaf (idempotent). */
    public void dropLeaf(String chronicleId) {
        jdbc.execute(ChronicleDdl.dropLeaf(chronicleId));
    }

    /** True when the chronicle's leaf exists. */
    public boolean leafExists(String chronicleId) {
        Boolean exists = jdbc.queryForObject("SELECT to_regclass(?) IS NOT NULL", Boolean.class,
                ChronicleDdl.leaf(chronicleId));
        return Boolean.TRUE.equals(exists);
    }

    /** The chronicle ids that have a leaf. */
    public Set<String> leafChronicles() {
        Set<String> out = new HashSet<>();
        jdbc.query(ChronicleDdl.SELECT_LEAVES, rs -> {
            String id = ChronicleDdl.chronicleOf(rs.getString(1));
            if (id != null) {
                out.add(id);
            }
        });
        return out;
    }

    /** Drops the leaves of chronicles that no longer exist. */
    @Override
    public void resetState() {
        try {
            Set<String> live = new HashSet<>(jdbc.queryForList("SELECT id FROM chronicle", String.class));
            for (String id : leafChronicles()) {
                if (!live.contains(id)) {
                    dropLeaf(id);
                }
            }
        } catch (RuntimeException e) {
            log.debug("Could not sweep orphan chronicle leaves: {}", e.toString());
        }
    }

    /** Deletes everything a chronicle captured: its leaf, series points, rollups and frame pins. */
    public void deleteCaptured(String chronicleId) {
        dropLeaf(chronicleId);
        jdbc.update("DELETE FROM chronicle_series_point WHERE chronicle_id = ?", chronicleId);
        jdbc.update("DELETE FROM chronicle_rollup WHERE chronicle_id = ?", chronicleId);
        jdbc.update("DELETE FROM chronicle_frame WHERE chronicle_id = ?", chronicleId);
    }

    // ------------------------------------------------------------------------- record_version

    /**
     * The versions with {@code lo < seq <= hi}, in seq order, optionally only from {@code sources}.
     *
     * @param lo the exclusive lower bound
     * @param hi the inclusive upper bound
     * @param sources the sources to read, or null for every source
     */
    public List<CapturedRow> readVersions(long lo, long hi, Collection<String> sources) {
        String sql = "SELECT " + READ_COLUMNS + ", rr.iso2, rr.admin1 FROM record_version v"
                + " LEFT JOIN record_region rr ON rr.source_id = v.source_id AND rr.external_id = v.external_id"
                + " WHERE v.seq > ? AND v.seq <= ?" + (sources == null ? "" : " AND v.source_id = ANY(?)")
                + " ORDER BY v.seq";
        Object[] args = sources == null ? new Object[] {lo, hi}
                : new Object[] {lo, hi, sources.toArray(String[]::new)};
        return jdbc.query(sql, (rs, n) -> row(rs, region(rs.getString("iso2"), rs.getString("admin1"))), args);
    }

    /** The region code of a {@code record_region} row: {@code CC-admin1} when tagged at admin-1, else {@code CC}. */
    static String region(String iso2, String admin1) {
        if (iso2 == null || iso2.isBlank()) {
            return null;
        }
        if (admin1 != null && !admin1.isBlank()) {
            return admin1.contains("-") ? admin1 : iso2 + "-" + admin1;
        }
        return iso2;
    }

    /** A {@link CapturedRow} from a row selected with {@link #READ_COLUMNS}. */
    static CapturedRow row(ResultSet rs, String region) throws SQLException {
        return new CapturedRow(rs.getLong("seq"), instant(rs, "ingested_at"), instant(rs, "observed_at"),
                instant(rs, "received_at"), instant(rs, "event_time"), instant(rs, "valid_until"),
                instant(rs, "valid_until_explicit"), (Long) rs.getObject("prev_seq", Long.class),
                (Long) rs.getObject("batch_id", Long.class), rs.getInt("version"), rs.getShort("history_tier"),
                rs.getShort("persist_reason"), rs.getShort("qa_flags"), rs.getBoolean("tombstone"),
                rs.getString("source_id"), rs.getString("external_id"), rs.getString("kind"), rs.getBytes("geom_wkb"),
                rs.getString("payload"), rs.getString("quality"), rs.getString("observer"), instant(rs, "valid_from"),
                region);
    }

    /** A timestamp column as an instant, or null. */
    static Instant instant(ResultSet rs, String column) throws SQLException {
        OffsetDateTime t = rs.getObject(column, OffsetDateTime.class);
        return t == null ? null : t.toInstant();
    }

    // ------------------------------------------------------------------------- writes

    /**
     * One version to write.
     *
     * @param layerId the capturing layer
     * @param row the version
     * @param backfilled true when written by a backfill
     * @param bucketStart the {@code latest-per-interval} bucket, or null
     */
    public record VersionWrite(String layerId, CapturedRow row, boolean backfilled, Instant bucketStart) {}

    /**
     * Inserts captured versions into the chronicle's leaf, skipping any already there.
     *
     * @return the bytes actually inserted (estimated) per layer
     */
    public Map<String, long[]> insertVersions(String chronicleId, List<VersionWrite> writes, Instant now) {
        Map<String, long[]> perLayer = new HashMap<>();
        if (writes.isEmpty()) {
            return perLayer;
        }
        int[][] counts = jdbc.batchUpdate(INSERT_VERSION, writes, 500, (ps, w) -> bind(ps, chronicleId, w, now));
        int i = 0;
        for (int[] batch : counts) {
            for (int c : batch) {
                VersionWrite w = writes.get(i++);
                if (c != 0) {
                    long[] t = perLayer.computeIfAbsent(w.layerId(), k -> new long[2]);
                    t[0]++;
                    t[1] += w.row().estimatedBytes();
                }
            }
        }
        return perLayer;
    }

    private static void bind(PreparedStatement ps, String chronicleId, VersionWrite w, Instant now)
            throws SQLException {
        CapturedRow r = w.row();
        int i = 1;
        ps.setString(i++, chronicleId);
        ps.setString(i++, w.layerId());
        ps.setLong(i++, r.seq());
        ps.setTimestamp(i++, ts(r.ingestedAt()));
        ps.setTimestamp(i++, ts(r.observedAt()));
        ps.setTimestamp(i++, ts(r.receivedAt()));
        ps.setTimestamp(i++, ts(r.eventTime()));
        ps.setTimestamp(i++, ts(r.validUntil()));
        ps.setTimestamp(i++, ts(r.validUntilExplicit()));
        setLong(ps, i++, r.prevSeq());
        setLong(ps, i++, r.batchId());
        ps.setInt(i++, r.version());
        ps.setShort(i++, r.historyTier());
        ps.setShort(i++, r.persistReasonCode());
        ps.setShort(i++, r.qaFlags());
        ps.setBoolean(i++, r.tombstone());
        ps.setString(i++, r.sourceId());
        ps.setString(i++, r.externalId());
        ps.setString(i++, r.kind());
        ps.setBytes(i++, r.geomWkb());
        ps.setString(i++, r.payload());
        ps.setString(i++, r.quality());
        ps.setString(i++, r.observer());
        ps.setTimestamp(i++, ts(r.validFrom()));
        ps.setBoolean(i++, w.backfilled());
        ps.setTimestamp(i++, ts(w.bucketStart()));
        ps.setTimestamp(i, ts(now));
    }

    /**
     * Replaces the stored version of one entity in one {@code latest-per-interval} bucket with a newer one.
     *
     * @return {@code [rowsDelta, bytesDelta]}: the change in rows and estimated bytes
     */
    public long[] replaceLatest(String chronicleId, VersionWrite w, Instant now) {
        CapturedRow r = w.row();
        List<Long> removed = jdbc.query("DELETE FROM " + ChronicleDdl.TABLE + " WHERE chronicle_id = ? AND layer_id = ?"
                + " AND source_id = ? AND external_id = ? AND bucket_start = ? AND seq < ?"
                + " RETURNING " + ChronicleSizes.ROW_OVERHEAD + " + octet_length(payload::text)"
                + " + COALESCE(octet_length(ST_AsBinary(geom)), 0) + COALESCE(octet_length(quality::text), 0)"
                + " + COALESCE(octet_length(observer::text), 0)", (rs, n) -> rs.getLong(1), chronicleId, w.layerId(),
                r.sourceId(), r.externalId(), ts(w.bucketStart()), r.seq());
        Map<String, long[]> inserted = insertVersions(chronicleId, List.of(w), now);
        long[] t = inserted.getOrDefault(w.layerId(), new long[2]);
        long bytesRemoved = 0;
        for (Long b : removed) {
            bytesRemoved += b == null ? 0 : b;
        }
        return new long[] {t[0] - removed.size(), t[1] - bytesRemoved};
    }

    /**
     * Adds {@code increment} to a {@code rollup-only} count.
     *
     * @return true when the region-hour row was new
     */
    public boolean upsertRollup(String chronicleId, String layerId, String region, Instant hour, long increment,
            boolean backfilled) {
        Boolean inserted = jdbc.queryForObject("INSERT INTO chronicle_rollup (chronicle_id, layer_id, region, hour_start,"
                + " versions, backfilled) VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT (chronicle_id, layer_id, region, hour_start)"
                + " DO UPDATE SET versions = chronicle_rollup.versions + EXCLUDED.versions,"
                + " backfilled = chronicle_rollup.backfilled OR EXCLUDED.backfilled RETURNING (xmax = 0)",
                Boolean.class, chronicleId, layerId, region, ts(hour), increment, backfilled);
        return Boolean.TRUE.equals(inserted);
    }

    /**
     * One captured series point.
     *
     * @param layerId the capturing layer
     * @param ref the series ref
     * @param period the upstream period label, or null
     * @param periodStart the period start
     * @param periodEnd the period end, or null
     * @param value the value, or null
     * @param unit the unit, or null
     * @param status the point status, or null
     * @param pointVersion the record version, or null
     * @param knownAt when the value became known, or null
     * @param coverageOk the coverage flag, or null
     * @param seq the supplying record seq, or null
     * @param backfilled true when written by a backfill
     */
    public record SeriesPointWrite(String layerId, String ref, String period, Instant periodStart, Instant periodEnd,
            Double value, String unit, String status, Integer pointVersion, Instant knownAt, Boolean coverageOk,
            Long seq, boolean backfilled) {}

    /**
     * Upserts a series point; a revision (a newer known value) replaces the stored one.
     *
     * @return true when the point was new
     */
    public boolean upsertSeriesPoint(String chronicleId, SeriesPointWrite p, Instant now) {
        Boolean inserted = jdbc.queryForObject("INSERT INTO chronicle_series_point (chronicle_id, layer_id, ref, period,"
                + " period_start, period_end, value, unit, status, point_version, known_at, coverage_ok, seq, backfilled,"
                + " captured_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                + " ON CONFLICT (chronicle_id, ref, period_start) DO UPDATE SET period = EXCLUDED.period,"
                + " period_end = EXCLUDED.period_end, value = EXCLUDED.value, unit = EXCLUDED.unit,"
                + " status = EXCLUDED.status, point_version = EXCLUDED.point_version, known_at = EXCLUDED.known_at,"
                + " coverage_ok = EXCLUDED.coverage_ok, seq = EXCLUDED.seq, captured_at = EXCLUDED.captured_at"
                + " WHERE chronicle_series_point.known_at IS NULL OR EXCLUDED.known_at IS NULL"
                + " OR EXCLUDED.known_at >= chronicle_series_point.known_at RETURNING (xmax = 0)", Boolean.class,
                chronicleId, p.layerId(), p.ref(), p.period(), ts(p.periodStart()), ts(p.periodEnd()), p.value(),
                p.unit(), p.status(), p.pointVersion(), ts(p.knownAt()), p.coverageOk(), p.seq(), p.backfilled(),
                ts(now));
        return Boolean.TRUE.equals(inserted);
    }

    /**
     * Pins a captured raster frame.
     *
     * @return true when the pin was new
     */
    public boolean pinFrame(String chronicleId, String layerId, String productId, String frameKey, Instant validTime,
            long seq, Instant now) {
        int n = jdbc.update("INSERT INTO chronicle_frame (chronicle_id, layer_id, product_id, frame_key, valid_time, seq,"
                + " captured_at) VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (chronicle_id, product_id, frame_key)"
                + " DO NOTHING", chronicleId, layerId, productId, frameKey, ts(validTime), seq, ts(now));
        return n > 0;
    }

    /** True when any chronicle pins the frame. */
    public boolean framePinned(String productId, String frameKey) {
        Boolean pinned = jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM chronicle_frame WHERE product_id = ?"
                + " AND frame_key = ?)", Boolean.class, productId, frameKey);
        return Boolean.TRUE.equals(pinned);
    }

    /**
     * Moves a chronicle's cursor and adds to its counters (atomic increments, so a concurrent backfill adds too).
     *
     * @param cursor the new cursor, or null to leave it
     */
    public void advance(String chronicleId, Long cursor, Instant now, long rowsDelta, long bytesDelta) {
        jdbc.update("UPDATE chronicle SET cursor_seq = COALESCE(?, cursor_seq), last_capture_at = ?,"
                + " rows_captured = GREATEST(0, rows_captured + ?), bytes_used = GREATEST(0, bytes_used + ?)"
                + " WHERE id = ?", cursor, ts(now), rowsDelta, bytesDelta, chronicleId);
    }

    /** Adds to a layer's counters and sets its frame counter when given. */
    public void layerCounters(String layerId, long rowsDelta, long bytesDelta, Long frameCounter) {
        jdbc.update("UPDATE chronicle_layer SET rows_captured = GREATEST(0, rows_captured + ?),"
                + " bytes_captured = GREATEST(0, bytes_captured + ?), frame_counter = COALESCE(?, frame_counter)"
                + " WHERE id = ?", rowsDelta, bytesDelta, frameCounter, layerId);
    }

    /** The bytes used and the size cap of a chronicle, or null when it is gone. */
    public long[] usage(String chronicleId) {
        List<long[]> rows = jdbc.query("SELECT bytes_used, size_cap_bytes FROM chronicle WHERE id = ?",
                (rs, n) -> new long[] {rs.getLong(1), rs.getLong(2)}, chronicleId);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    /** Sets the cap state when it differs; true when it changed. */
    public boolean capState(String chronicleId, String state) {
        return jdbc.update("UPDATE chronicle SET cap_state = ? WHERE id = ? AND cap_state <> ?", state, chronicleId,
                state) > 0;
    }

    /**
     * Deletes what one layer captured (its versions, series points, rollups and frame pins).
     *
     * @return {@code [rows, bytes]} removed
     */
    public long[] purgeLayer(String chronicleId, String layerId) {
        long rows = 0;
        long bytes = 0;
        if (leafExists(chronicleId)) {
            List<Long> removed = jdbc.query("DELETE FROM " + ChronicleDdl.TABLE + " WHERE chronicle_id = ? AND layer_id = ?"
                    + " RETURNING " + ChronicleSizes.ROW_OVERHEAD + " + octet_length(payload::text)"
                    + " + COALESCE(octet_length(ST_AsBinary(geom)), 0) + COALESCE(octet_length(quality::text), 0)"
                    + " + COALESCE(octet_length(observer::text), 0)", (rs, n) -> rs.getLong(1), chronicleId, layerId);
            rows += removed.size();
            for (Long b : removed) {
                bytes += b == null ? 0 : b;
            }
        }
        int points = jdbc.update("DELETE FROM chronicle_series_point WHERE chronicle_id = ? AND layer_id = ?",
                chronicleId, layerId);
        int rollups = jdbc.update("DELETE FROM chronicle_rollup WHERE chronicle_id = ? AND layer_id = ?", chronicleId,
                layerId);
        int frames = jdbc.update("DELETE FROM chronicle_frame WHERE chronicle_id = ? AND layer_id = ?", chronicleId,
                layerId);
        rows += points + rollups + frames;
        bytes += points * ChronicleSizes.SERIES_ROW + rollups * ChronicleSizes.ROLLUP_ROW
                + frames * ChronicleSizes.FRAME_ROW;
        return new long[] {rows, bytes};
    }

    /**
     * The newest captured observation time per entity of one layer among {@code keys} ({@code source\u0000external}),
     * for the {@code downsampled} decision after a restart.
     */
    public Map<String, Instant> lastCaptured(String chronicleId, String layerId, Collection<String> keys) {
        Map<String, Instant> out = new HashMap<>();
        if (keys.isEmpty() || !leafExists(chronicleId)) {
            return out;
        }
        List<String> sources = new ArrayList<>();
        List<String> externals = new ArrayList<>();
        for (String key : keys) {
            int nul = key.indexOf('\u0000');
            sources.add(key.substring(0, nul));
            externals.add(key.substring(nul + 1));
        }
        jdbc.query("SELECT v.source_id, v.external_id, max(v.observed_at) FROM " + ChronicleDdl.TABLE + " v"
                + " JOIN unnest(?::text[], ?::text[]) AS k(s, e) ON v.source_id = k.s AND v.external_id = k.e"
                + " WHERE v.chronicle_id = ? AND v.layer_id = ? GROUP BY v.source_id, v.external_id", rs -> {
                    OffsetDateTime t = rs.getObject(3, OffsetDateTime.class);
                    if (t != null) {
                        out.put(rs.getString(1) + '\u0000' + rs.getString(2), t.toInstant());
                    }
                }, sources.toArray(String[]::new), externals.toArray(String[]::new), chronicleId, layerId);
        return out;
    }

    /** A nullable instant as a timestamp. */
    static Timestamp ts(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private static void setLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.BIGINT);
        } else {
            ps.setLong(index, value);
        }
    }
}
