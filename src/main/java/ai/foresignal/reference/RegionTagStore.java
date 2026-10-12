package ai.foresignal.reference;

import ai.foresignal.store.query.PgArrays;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Reads and writes {@code record_region} (prompt 040). The only writer of that table: the
 * {@link RegionTagWorker}, lazy tagging through {@link RegionTagger}, the orphan sweep and 039's purge
 * fan-out. Writes run in transactions of their own, so a caller's transaction (an alert insert, for
 * example) never holds a {@code record_region} lock, and a write after another transaction's commit is
 * never lost.
 *
 * <p>Reads name the regions by joining the reference tables; the country name comes from the bundled
 * {@link CountryCentroids}.
 */
@Component
public class RegionTagStore {

    /**
     * One stored row.
     *
     * @param tags the tags, with names
     * @param lon the longitude the tags were computed for
     * @param lat the latitude the tags were computed for
     * @param taggedAt when
     */
    public record Stored(RegionTags tags, double lon, double lat, Instant taggedAt) {}

    static final String SELECT = "SELECT r.source_id, r.external_id, r.iso2, r.admin1, r.admin2, r.sea, r.near_id,"
            + " r.near_name, r.near_km, r.near_bearing, r.tagged_lon, r.tagged_lat, r.tagged_at,"
            + " a.name AS admin1_name, c.name AS county_name, g.iso2 AS near_iso2, g.admin1_code AS near_admin1_code,"
            + " g.population AS near_population,"
            + " (SELECT ga.code FROM ref_admin1 ga WHERE ga.gn_code = g.iso2 || '.' || g.admin1_code LIMIT 1)"
            + " AS near_admin1_iso"
            + " FROM record_region r"
            + " LEFT JOIN ref_admin1 a ON a.code = r.admin1"
            + " LEFT JOIN ref_admin2 c ON c.fips5 = r.admin2"
            + " LEFT JOIN gazetteer_place g ON g.geonameid = r.near_id";

    static final String UPSERT = "INSERT INTO record_region (source_id, external_id, iso2, admin1, admin2, sea,"
            + " near_id, near_name, near_km, near_bearing, tagged_lon, tagged_lat, tagged_at)"
            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            + " ON CONFLICT (source_id, external_id) DO UPDATE SET iso2 = EXCLUDED.iso2, admin1 = EXCLUDED.admin1,"
            + " admin2 = EXCLUDED.admin2, sea = EXCLUDED.sea, near_id = EXCLUDED.near_id,"
            + " near_name = EXCLUDED.near_name, near_km = EXCLUDED.near_km, near_bearing = EXCLUDED.near_bearing,"
            + " tagged_lon = EXCLUDED.tagged_lon, tagged_lat = EXCLUDED.tagged_lat, tagged_at = EXCLUDED.tagged_at";

    private final JdbcTemplate jdbc;
    private final CountryCentroids countries;
    private final TransactionTemplate own;

    /** Creates the store. */
    public RegionTagStore(JdbcTemplate jdbc, CountryCentroids countries, PlatformTransactionManager transactions) {
        this.jdbc = jdbc;
        this.countries = countries;
        this.own = new TransactionTemplate(transactions);
        this.own.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /** Stores {@code tags} for the record, computed at {@code (lon, lat)}, in a transaction of its own. */
    public void upsert(String sourceId, String externalId, RegionTags tags, double lon, double lat, Instant at) {
        RegionNear near = tags.near();
        own.executeWithoutResult(status -> jdbc.update(UPSERT, sourceId, externalId, tags.iso2(), tags.admin1(),
                tags.admin2(), tags.sea(), near == null ? null : near.geonameid(), near == null ? null : near.name(),
                near == null ? null : near.distanceKm(), near == null ? null : near.bearingDeg(), lon, lat,
                Timestamp.from(at)));
    }

    /** Deletes the record's row (a tombstone, a purge); the number of rows deleted. */
    public int delete(String sourceId, String externalId) {
        Integer n = own.execute(status -> jdbc.update(
                "DELETE FROM record_region WHERE source_id = ? AND external_id = ?", sourceId, externalId));
        return n == null ? 0 : n;
    }

    /** The record's row, when it has one. */
    public Optional<Stored> find(String sourceId, String externalId) {
        List<Stored> rows = jdbc.query(SELECT + " WHERE r.source_id = ? AND r.external_id = ?",
                (rs, i) -> stored(rs), sourceId, externalId);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    /** The rows of {@code keys} (source id, external id), by {@link #key}. */
    public Map<String, Stored> findAll(Collection<Map.Entry<String, String>> keys) {
        Map<String, Stored> out = new HashMap<>();
        if (keys.isEmpty()) {
            return out;
        }
        List<String> sources = new ArrayList<>(keys.size());
        List<String> ids = new ArrayList<>(keys.size());
        for (Map.Entry<String, String> key : keys) {
            sources.add(key.getKey());
            ids.add(key.getValue());
        }
        jdbc.query(SELECT + " JOIN unnest(CAST(? AS text[]), CAST(? AS text[])) AS k(s, e)"
                        + " ON r.source_id = k.s AND r.external_id = k.e",
                rs -> {
                    out.put(key(rs.getString("source_id"), rs.getString("external_id")), stored(rs));
                }, PgArrays.text(sources), PgArrays.text(ids));
        return out;
    }

    /** The map key of a record. */
    public static String key(String sourceId, String externalId) {
        return sourceId + '\u0000' + externalId;
    }

    private Stored stored(ResultSet rs) throws SQLException {
        String iso2 = rs.getString("iso2");
        RegionNear near = null;
        long nearId = rs.getLong("near_id");
        if (!rs.wasNull() && rs.getString("near_name") != null) {
            String nearIso2 = rs.getString("near_iso2");
            String admin1Code = rs.getString("near_admin1_code");
            long population = rs.getLong("near_population");
            Long pop = rs.wasNull() ? null : population;
            String isoAdmin1 = rs.getString("near_admin1_iso");
            near = new RegionNear(nearId, rs.getString("near_name"), nearIso2,
                    isoAdmin1 != null ? isoAdmin1 : admin1Code, rs.getDouble("near_km"), rs.getDouble("near_bearing"),
                    pop, suffix(countries, nearIso2, admin1Code));
        }
        RegionTags tags = new RegionTags(iso2, rs.getString("admin1"), rs.getString("admin2"), rs.getString("sea"),
                near, iso2 == null ? null : countries.name(iso2), rs.getString("admin1_name"),
                rs.getString("county_name"));
        Timestamp at = rs.getTimestamp("tagged_at");
        return new Stored(tags, rs.getDouble("tagged_lon"), rs.getDouble("tagged_lat"),
                at == null ? null : at.toInstant());
    }

    /**
     * The text after a place's name in {@code near.text}: the state code for a US place
     * ({@code Austin, TX}), else the country's name ({@code Lyon, France}), else the ISO2 code.
     */
    static String suffix(CountryCentroids countries, String iso2, String admin1Code) {
        if ("US".equals(iso2) && admin1Code != null && !admin1Code.isBlank()) {
            return admin1Code;
        }
        if (iso2 == null) {
            return null;
        }
        String name = countries.name(iso2);
        return name != null ? name : iso2;
    }
}
