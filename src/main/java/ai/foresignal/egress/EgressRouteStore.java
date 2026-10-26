package ai.foresignal.egress;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Reads and writes {@code egress_route} and the per-source assignment columns of {@code source_settings} (prompt
 * R2-018) with native SQL.
 */
@Component
public class EgressRouteStore {

    private static final String COLUMNS = "id, name, type, enabled, url, bound_interface, secrets_cipher,"
            + " secrets_nonce, exit_ip, exit_ip_checked_at, created_at, updated_at";

    private final NamedParameterJdbcTemplate jdbc;

    /** Creates the store. */
    public EgressRouteStore(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * A page of routes.
     *
     * @param items the routes, by name
     * @param nextCursor the cursor of the next page, or null
     */
    public record Page(List<EgressRoute> items, String nextCursor) {}

    /**
     * A source's assignment.
     *
     * @param sourceId the source
     * @param routeId the pinned route, or null
     * @param excluded whether it stays out of rotation
     */
    public record Assignment(String sourceId, UUID routeId, boolean excluded) {}

    /** Inserts or replaces {@code route}. */
    public void save(EgressRoute route) {
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("id", route.id()).addValue("name", route.name()).addValue("type", route.type())
                .addValue("enabled", route.enabled()).addValue("url", route.url())
                .addValue("boundInterface", route.boundInterface())
                .addValue("cipher", route.secretsCipher()).addValue("nonce", route.secretsNonce())
                .addValue("exitIp", route.exitIp()).addValue("exitIpAt", ts(route.exitIpCheckedAt()))
                .addValue("createdAt", ts(route.createdAt())).addValue("updatedAt", ts(route.updatedAt()));
        jdbc.update("""
                INSERT INTO egress_route (id, name, type, enabled, url, bound_interface, secrets_cipher, secrets_nonce,
                    exit_ip, exit_ip_checked_at, created_at, updated_at)
                VALUES (:id, :name, :type, :enabled, :url, :boundInterface, :cipher, :nonce, :exitIp, :exitIpAt,
                    :createdAt, :updatedAt)
                ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, type = EXCLUDED.type,
                    enabled = EXCLUDED.enabled, url = EXCLUDED.url, bound_interface = EXCLUDED.bound_interface,
                    secrets_cipher = EXCLUDED.secrets_cipher, secrets_nonce = EXCLUDED.secrets_nonce,
                    exit_ip = EXCLUDED.exit_ip, exit_ip_checked_at = EXCLUDED.exit_ip_checked_at,
                    updated_at = EXCLUDED.updated_at
                """, p);
    }

    /** The route {@code id}. */
    public Optional<EgressRoute> find(UUID id) {
        return jdbc.query("SELECT " + COLUMNS + " FROM egress_route WHERE id = :id",
                new MapSqlParameterSource("id", id), (rs, i) -> map(rs)).stream().findFirst();
    }

    /** The route named {@code name}, ignoring case. */
    public Optional<EgressRoute> findByName(String name) {
        return jdbc.query("SELECT " + COLUMNS + " FROM egress_route WHERE lower(name) = lower(:name)",
                new MapSqlParameterSource("name", name), (rs, i) -> map(rs)).stream().findFirst();
    }

    /** Every route, by name. */
    public List<EgressRoute> all() {
        return jdbc.query("SELECT " + COLUMNS + " FROM egress_route ORDER BY lower(name), id",
                new MapSqlParameterSource(), (rs, i) -> map(rs));
    }

    /**
     * One page, by name.
     *
     * @param cursor the previous page's cursor, or null
     * @param limit the page size
     * @throws IllegalArgumentException for a cursor this store did not issue
     */
    public Page page(String cursor, int limit) {
        MapSqlParameterSource p = new MapSqlParameterSource("limit", limit + 1);
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNS + " FROM egress_route WHERE TRUE");
        if (cursor != null) {
            String[] parts = decode(cursor);
            sql.append(" AND (lower(name), id) > (:afterName, :afterId)");
            p.addValue("afterName", parts[0]).addValue("afterId", UUID.fromString(parts[1]));
        }
        sql.append(" ORDER BY lower(name), id LIMIT :limit");
        List<EgressRoute> rows = jdbc.query(sql.toString(), p, (rs, i) -> map(rs));
        String next = null;
        if (rows.size() > limit) {
            rows = new ArrayList<>(rows.subList(0, limit));
            EgressRoute last = rows.getLast();
            next = Base64.getUrlEncoder().withoutPadding().encodeToString(
                    (last.name().toLowerCase(java.util.Locale.ROOT) + "\n" + last.id())
                            .getBytes(StandardCharsets.UTF_8));
        }
        return new Page(rows, next);
    }

    /** Deletes the route {@code id} and clears the pins to it; true when it existed. */
    public boolean delete(UUID id) {
        MapSqlParameterSource p = new MapSqlParameterSource("id", id);
        jdbc.update("UPDATE source_settings SET egress_route_id = NULL WHERE egress_route_id = :id", p);
        return jdbc.update("DELETE FROM egress_route WHERE id = :id", p) > 0;
    }

    /** Every source with a pinned route or out of rotation. */
    public List<Assignment> assignments() {
        return jdbc.query("SELECT source_id, egress_route_id, egress_excluded FROM source_settings"
                        + " WHERE egress_route_id IS NOT NULL OR egress_excluded",
                new MapSqlParameterSource(), (rs, i) -> new Assignment(rs.getString("source_id"),
                        rs.getObject("egress_route_id", UUID.class), rs.getBoolean("egress_excluded")));
    }

    private static String[] decode(String cursor) {
        try {
            String text = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = text.split("\n", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("bad cursor");
            }
            UUID.fromString(parts[1]);
            return parts;
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("The cursor is not one this endpoint issued", e);
        }
    }

    private static EgressRoute map(ResultSet rs) throws SQLException {
        return new EgressRoute(rs.getObject("id", UUID.class), rs.getString("name"), rs.getString("type"),
                rs.getBoolean("enabled"), rs.getString("url"), rs.getString("bound_interface"),
                rs.getBytes("secrets_cipher"), rs.getBytes("secrets_nonce"), rs.getString("exit_ip"),
                instant(rs, "exit_ip_checked_at"), instant(rs, "created_at"), instant(rs, "updated_at"));
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toInstant();
    }

    private static Timestamp ts(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }
}
