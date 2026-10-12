package ai.foresignal.notify.inbox;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

/**
 * Client presence (prompt 033): heartbeats ({@code POST /v1/clients/presence}, ttl ≤ 300 s), the
 * admin listing, and the one question delivery asks: is a desktop active right now?
 *
 * <p>Stale or expired presence fails open: an expired row never counts as active, so phone
 * notifications go out. An open inbox stream counts as {@code background} presence only, never
 * {@code active} ({@link #background}).
 */
@Service
public class PresenceService {

    /** The kinds. */
    public static final Set<String> KINDS = Set.of("desktop", "mobile", "web");

    /** The states. */
    public static final Set<String> STATES = Set.of("active", "idle", "background");

    /** The longest heartbeat validity. */
    public static final int MAX_TTL = 300;

    /** The default heartbeat validity. */
    public static final int DEFAULT_TTL = 120;

    /**
     * A validated {@code ClientPresenceInput}.
     *
     * @param kind {@code desktop}, {@code mobile} or {@code web}
     * @param state {@code active}, {@code idle} or {@code background}
     * @param ttlSeconds 1–300
     */
    public record Input(String kind, String state, int ttlSeconds) {}

    /**
     * One presence row ({@code ClientPresence}).
     *
     * @param deviceKey the device
     * @param kind the kind
     * @param state the state
     * @param expiresAt when it expires
     * @param updatedAt when last updated
     */
    public record Presence(String deviceKey, String kind, String state, Instant expiresAt, Instant updatedAt) {

        /** The {@code ClientPresence} JSON. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("deviceKey", deviceKey);
            out.put("kind", kind);
            out.put("state", state);
            out.put("expiresAt", expiresAt.toString());
            out.put("updatedAt", updatedAt == null ? null : updatedAt.toString());
            return out;
        }
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;

    /** Creates the service. */
    public PresenceService(NamedParameterJdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /**
     * Parses a {@code ClientPresenceInput} body, reporting every problem in one {@code 400}.
     *
     * @throws ValidationException when it is not valid
     */
    public static Input parse(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The body must be a ClientPresenceInput object.");
        }
        List<FieldError> errors = new ArrayList<>();
        String kind = text(body, "kind", KINDS, errors);
        String state = text(body, "state", STATES, errors);
        int ttl = DEFAULT_TTL;
        JsonNode ttlNode = body.get("ttlSeconds");
        if (ttlNode != null && !ttlNode.isNull()) {
            if (!ttlNode.isIntegralNumber() || ttlNode.asLong() < 1 || ttlNode.asLong() > MAX_TTL) {
                errors.add(new FieldError("ttlSeconds", "must be an integer from 1 to " + MAX_TTL));
            } else {
                ttl = ttlNode.asInt();
            }
        }
        for (String field : body.propertyNames()) {
            if (!Set.of("kind", "state", "ttlSeconds").contains(field)) {
                errors.add(new FieldError(field, "is not a ClientPresenceInput member"));
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The presence is not valid.", errors);
        }
        return new Input(kind, state, ttl);
    }

    private static String text(JsonNode body, String field, Set<String> allowed, List<FieldError> errors) {
        JsonNode node = body.get(field);
        if (node == null || !node.isString() || !allowed.contains(node.asString())) {
            errors.add(new FieldError(field, "is required, one of " + String.join(", ", allowed.stream().sorted().toList())));
            return null;
        }
        return node.asString();
    }

    /** Stores the heartbeat of {@code deviceKey} and returns it. */
    public Presence update(String deviceKey, Input input) {
        Instant now = clock.instant();
        Instant expires = now.plusSeconds(input.ttlSeconds());
        jdbc.update("""
                insert into client_presence (device_key, kind, state, expires_at, updated_at)
                values (:key, :kind, :state, :expires, :now)
                on conflict (device_key) do update set kind = excluded.kind, state = excluded.state,
                       expires_at = excluded.expires_at, updated_at = excluded.updated_at
                """, new MapSqlParameterSource("key", deviceKey).addValue("kind", input.kind())
                .addValue("state", input.state()).addValue("expires", Timestamp.from(expires))
                .addValue("now", Timestamp.from(now)));
        return new Presence(deviceKey, input.kind(), input.state(), expires, now);
    }

    /**
     * Records {@code background} presence for an open stream of {@code deviceKey}, unless the device
     * already has unexpired presence (a stream never upgrades or downgrades a heartbeat).
     */
    public void background(String deviceKey, String kind) {
        Instant now = clock.instant();
        jdbc.update("""
                insert into client_presence (device_key, kind, state, expires_at, updated_at)
                values (:key, :kind, 'background', :expires, :now)
                on conflict (device_key) do update set kind = excluded.kind, state = 'background',
                       expires_at = excluded.expires_at, updated_at = excluded.updated_at
                 where client_presence.expires_at <= :now
                """, new MapSqlParameterSource("key", deviceKey).addValue("kind", kind)
                .addValue("expires", Timestamp.from(now.plusSeconds(MAX_TTL))).addValue("now", Timestamp.from(now)));
    }

    /** Every presence row, newest first. */
    public List<Presence> list() {
        return jdbc.query("select device_key, kind, state, expires_at, updated_at from client_presence"
                + " order by updated_at desc", (rs, n) -> new Presence(rs.getString(1), rs.getString(2),
                rs.getString(3), rs.getObject(4, OffsetDateTime.class).toInstant(),
                rs.getObject(5, OffsetDateTime.class).toInstant()));
    }

    /** True when any desktop is {@code active} with unexpired presence at {@code now}. */
    public boolean desktopActive(Instant now) {
        Boolean active = jdbc.queryForObject("""
                select exists (select 1 from client_presence
                                where kind = 'desktop' and state = 'active' and expires_at > :now)
                """, new MapSqlParameterSource("now", Timestamp.from(now)), Boolean.class);
        return Boolean.TRUE.equals(active);
    }

    /** Deletes rows that expired before {@code cutoff} (retention). */
    public int purgeExpired(Instant cutoff) {
        return jdbc.update("delete from client_presence where expires_at < :cutoff",
                new MapSqlParameterSource("cutoff", Timestamp.from(cutoff)));
    }

    /** The presence kind of a 023 device platform: desktop, mobile or web. */
    public static String kindOf(String platform) {
        if (platform == null) {
            return "web";
        }
        return switch (platform) {
            case "macos", "windows", "linux" -> "desktop";
            case "ios", "android" -> "mobile";
            default -> "web";
        };
    }
}
