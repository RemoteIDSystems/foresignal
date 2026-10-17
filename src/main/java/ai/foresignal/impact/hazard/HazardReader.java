package ai.foresignal.impact.hazard;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads hazard records for the impact projector (prompt 127): the current version of every record behind a hazard
 * key (its head joined to the pinned version), the hazard keys changed in a seq range, and a gauge's history for
 * {@link GaugeCalibration}.
 */
@Component
public class HazardReader {

    /** The flood categories at which a gauge change is worth projecting. */
    public static final List<String> GAUGE_CATEGORIES = List.of("minor", "moderate", "major");

    /** The most history rows read for one gauge. */
    static final int GAUGE_HISTORY_LIMIT = 50_000;

    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};

    private final NamedParameterJdbcTemplate jdbc;
    private final JsonMapper json;

    /** Creates the reader. */
    public HazardReader(NamedParameterJdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    /**
     * A change that may affect a hazard.
     *
     * @param hazardKey the hazard key
     * @param seq the version's seq
     */
    public record Change(String hazardKey, long seq) {}

    /**
     * The hazard keys of probability-bearing hazards changed in {@code (fromSeq, toSeq]}, in seq order: NHC wind
     * probabilities, SPC day 1–2 probabilistic layers, WPC ERO days 1–3, and gauges at minor flood or above (observed
     * or forecast) or listed in {@code activeGauges} (so a falling gauge supersedes its projections).
     *
     * @param fromSeq the exclusive start
     * @param toSeq the inclusive end
     * @param activeGauges gauge ids with open projections
     */
    public List<Change> changes(long fromSeq, long toSeq, Collection<String> activeGauges) {
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("from", fromSeq).addValue("to", toSeq)
                .addValue("wind", HazardAdapters.NHC_WIND_PROBS).addValue("spc", HazardAdapters.SPC_OUTLOOKS)
                .addValue("ero", HazardAdapters.WPC_ERO).addValue("gauges", HazardAdapters.NWPS_GAUGES)
                .addValue("categories", GAUGE_CATEGORIES)
                .addValue("active", activeGauges.isEmpty() ? List.of("") : new ArrayList<>(activeGauges));
        String sql = "SELECT source_id, kind, external_id, seq FROM record_version WHERE seq > :from AND seq <= :to AND ("
                + "source_id = :wind OR source_id IN (:ero)"
                + " OR (source_id = :spc AND (external_id LIKE 'day1:%' OR external_id LIKE 'day2:%'))"
                + " OR (source_id = :gauges AND (payload ->> 'floodCategory' IN (:categories)"
                + " OR payload ->> 'forecastCategory' IN (:categories) OR external_id IN (:active))))"
                + " ORDER BY seq";
        List<Change> out = new ArrayList<>();
        jdbc.query(sql, p, rs -> {
            String key = HazardAdapters.hazardKey(rs.getString("source_id"), rs.getString("kind"),
                    rs.getString("external_id"));
            if (key != null && !key.startsWith(HazardAdapters.SPC_OUTLOOKS + ":day1:cat")
                    && !key.startsWith(HazardAdapters.SPC_OUTLOOKS + ":day2:cat")) {
                out.add(new Change(key, rs.getLong("seq")));
            }
        });
        return out;
    }

    /**
     * The current, non-tombstone records behind a hazard key, oldest seq first.
     *
     * @param hazardKey the key
     */
    public List<HazardRecord> current(String hazardKey) {
        String source = HazardAdapters.sourceOf(hazardKey);
        String rest = hazardKey.length() > source.length() ? hazardKey.substring(source.length() + 1) : "";
        MapSqlParameterSource p = new MapSqlParameterSource("source", source);
        String filter;
        switch (source) {
            case HazardAdapters.NHC_WIND_PROBS -> {
                filter = " AND h.external_id LIKE :prefix";
                p.addValue("prefix", rest + "kt:%");
            }
            case HazardAdapters.SPC_OUTLOOKS -> {
                filter = " AND h.external_id LIKE :prefix";
                p.addValue("prefix", rest + ":%");
            }
            case HazardAdapters.NWPS_GAUGES, HazardAdapters.STORM_TRACKS, HazardAdapters.NHC_CONES -> {
                filter = " AND h.external_id = :id";
                p.addValue("id", rest);
            }
            default -> filter = "";
        }
        return read("SELECT v.source_id, v.external_id, v.seq, v.kind, v.payload::text AS payload,"
                + " ST_AsBinary(v.geom) AS geom, v.observed_at, v.event_time, v.valid_from, v.valid_until_explicit"
                + " FROM record_head h JOIN record_version v ON v.seq = h.seq WHERE h.source_id = :source"
                + " AND h.tombstone = false" + filter + " ORDER BY v.seq", p);
    }

    /** The current NHC cones (context for tropical wind hazards), oldest seq first. */
    public List<HazardRecord> currentCones() {
        return read("SELECT v.source_id, v.external_id, v.seq, v.kind, v.payload::text AS payload,"
                + " ST_AsBinary(v.geom) AS geom, v.observed_at, v.event_time, v.valid_from, v.valid_until_explicit"
                + " FROM record_head h JOIN record_version v ON v.seq = h.seq WHERE h.source_id = :source"
                + " AND h.kind = 'cyclone-cone' AND h.tombstone = false ORDER BY v.seq",
                new MapSqlParameterSource("source", HazardAdapters.NHC_CONES));
    }

    /**
     * A gauge's stored observations since {@code from}, oldest first.
     *
     * @param lid the gauge id
     * @param from the start of history
     */
    public List<GaugeCalibration.Observation> gaugeHistory(String lid, Instant from) {
        MapSqlParameterSource p = new MapSqlParameterSource("source", HazardAdapters.NWPS_GAUGES).addValue("lid", lid)
                .addValue("from", Timestamp.from(from)).addValue("limit", GAUGE_HISTORY_LIMIT);
        return jdbc.query("SELECT observed_at, payload ->> 'floodCategory' AS flood, payload ->> 'forecastCategory'"
                + " AS forecast, payload ->> 'forecastValidTime' AS valid FROM record_version WHERE source_id = :source"
                + " AND external_id = :lid AND tombstone = false AND observed_at >= :from ORDER BY observed_at LIMIT"
                + " :limit", p, (rs, n) -> new GaugeCalibration.Observation(rs.getTimestamp("observed_at").toInstant(),
                        rs.getString("flood"), rs.getString("forecast"), instant(rs.getString("valid"))));
    }

    private List<HazardRecord> read(String sql, MapSqlParameterSource p) {
        return jdbc.query(sql, p, (rs, n) -> record(rs));
    }

    private HazardRecord record(ResultSet rs) throws SQLException {
        Map<String, Object> payload;
        String text = rs.getString("payload");
        try {
            payload = text == null ? Map.of() : json.readValue(text, MAP);
        } catch (RuntimeException e) {
            payload = new LinkedHashMap<>();
        }
        Geometry geometry = null;
        byte[] wkb = rs.getBytes("geom");
        if (wkb != null) {
            try {
                geometry = new WKBReader().read(wkb);
                geometry.setSRID(4326);
            } catch (ParseException e) {
                geometry = null;
            }
        }
        return new HazardRecord(rs.getString("source_id"), rs.getString("external_id"), rs.getLong("seq"),
                rs.getString("kind"), payload, geometry, ts(rs, "observed_at"), ts(rs, "event_time"),
                ts(rs, "valid_from"), ts(rs, "valid_until_explicit"));
    }

    private static Instant ts(ResultSet rs, String column) throws SQLException {
        Timestamp t = rs.getTimestamp(column);
        return t == null ? null : t.toInstant();
    }

    private static Instant instant(String text) {
        if (text == null) {
            return null;
        }
        try {
            return Instant.parse(text);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
