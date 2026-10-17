package ai.foresignal.notify.inbox;

import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RestrictionClamp;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.retention.RetentionResolver;
import ai.foresignal.trigger.Severity;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;

/**
 * The notification inbox (prompt 033): every persisted notification ({@code alert}, {@code briefing}
 * and {@code system} events) with per-device read state, kept for the {@code inbox} retention window
 * (default 30 days).
 *
 * <p>Read state is a cursor per device ({@code notification_read_cursor.read_up_to_id}) plus sparse
 * reads above it ({@code notification_read}). Moving the cursor deletes the sparse reads at or below
 * it; at most {@value #MAX_SPARSE} sparse reads are kept per device, the oldest dropped first (which
 * only makes them unread again, never read). <b>Reading never acknowledges an alert.</b>
 *
 * <p>Notifications about sources the caller may not see (private sources, 023) are left out of
 * listings, single reads and the stream replay.
 *
 * <p>The badge counts unread {@code system} notifications and unread {@code alert.fired}
 * notifications whose alert is still firing; the same count feeds {@code aps.badge}
 * ({@link InboxBadgeProvider}).
 */
@Service
public class InboxService {

    /** The inbox events. */
    public static final List<String> EVENTS = List.of("alert", "briefing", "system", "analyst.note",
            "situation.activated", "situation.escalated");

    /** The most sparse reads kept per device. */
    public static final int MAX_SPARSE = 500;

    /** The largest page. */
    public static final int MAX_LIMIT = 500;

    /** The default page. */
    public static final int DEFAULT_LIMIT = 100;

    /**
     * Listing filters.
     *
     * @param after only ids above this, or null
     * @param before only ids below this, or null
     * @param events the events, empty for all
     * @param minSeverity the minimum severity, or null
     * @param unread only unread on this device
     * @param limit the page size
     */
    public record Query(Long after, Long before, List<String> events, Severity minSeverity, boolean unread, int limit) {

        /** Copies the events. */
        public Query {
            events = events == null ? List.of() : List.copyOf(events);
        }
    }

    /**
     * One inbox item ({@code Notification}).
     *
     * @param id the notification id
     * @param event the event
     * @param subtype the subtype
     * @param severity the severity
     * @param title the title
     * @param body the markdown body, or null
     * @param alertId the alert, or null
     * @param briefingRunId the briefing run, or null
     * @param ruleId the rule, or null
     * @param createdAt when created
     * @param read whether read on the device
     * @param alertState the live alert state, or null
     * @param links the link buttons as {@code {label, url}}
     */
    public record Item(long id, String event, String subtype, String severity, String title, String body, Long alertId,
            UUID briefingRunId, UUID ruleId, Instant createdAt, boolean read, String alertState,
            List<Map<String, Object>> links) {

        /** The {@code Notification} JSON. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("id", id);
            out.put("event", event);
            out.put("subtype", subtype);
            out.put("severity", severity);
            out.put("title", title);
            out.put("body", body);
            out.put("alertId", alertId);
            out.put("briefingRunId", briefingRunId == null ? null : briefingRunId.toString());
            out.put("ruleId", ruleId == null ? null : ruleId.toString());
            out.put("createdAt", createdAt.toString());
            out.put("read", read);
            out.put("alertState", alertState);
            return out;
        }
    }

    /**
     * The unread counts of a device ({@code NotificationBadge}).
     *
     * @param unread unread system notifications and unread firing alerts
     * @param unreadCritical the critical ones among them
     * @param latestId the newest notification id, or null
     */
    public record Badge(int unread, int unreadCritical, Long latestId) {

        /** The {@code NotificationBadge} JSON. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("unread", unread);
            out.put("unreadCritical", unreadCritical);
            out.put("latestId", latestId);
            return out;
        }
    }

    /**
     * A validated {@code NotificationReadRequest}: exactly one of a cursor move or sparse ids.
     *
     * @param upToId the new cursor, or null
     * @param ids the sparse reads, empty with a cursor move
     */
    public record ReadRequest(Long upToId, List<Long> ids) {

        /** Copies the ids. */
        public ReadRequest {
            ids = ids == null ? List.of() : List.copyOf(ids);
        }
    }

    private static final String COLUMNS = """
            n.id, n.event, n.subtype, n.severity, n.title, n.body_markdown, n.alert_id, n.briefing_run_id, n.rule_id,
            n.created_at, n.links::text as links, a.state as alert_state,
            (n.id <= coalesce(c.read_up_to_id, 0) or r.notification_id is not null) as is_read
            """;

    private static final String FROM = """
             from notification n
             left join alert a on a.id = n.alert_id
             left join notification_read_cursor c on c.device_key = :device
             left join notification_read r on r.device_key = :device and r.notification_id = n.id
            """;

    private final NamedParameterJdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final RetentionResolver retention;
    private final ApplicationEventPublisher events;
    private final Clock clock;
    private final tools.jackson.databind.json.JsonMapper json;

    /** Creates the service. */
    public InboxService(NamedParameterJdbcTemplate jdbc, PlatformTransactionManager transactions,
            RetentionResolver retention, ApplicationEventPublisher events, Clock clock,
            tools.jackson.databind.json.JsonMapper json) {
        this.jdbc = jdbc;
        this.tx = new TransactionTemplate(transactions);
        this.retention = retention;
        this.events = events;
        this.clock = clock;
        this.json = json;
    }

    // ------------------------------------------------------------------------- reads

    /** One page for {@code deviceKey}, newest first (keyset on id; fetch {@code limit + 1} to page). */
    public List<Item> list(String deviceKey, Query query, Set<String> excludedSources) {
        return list(deviceKey, query, excludedSources, ReadRestriction.NONE);
    }

    /** {@link #list(String, Query, Set)} under {@code restriction} (prompt 035). */
    public List<Item> list(String deviceKey, Query query, Set<String> excludedSources, ReadRestriction restriction) {
        MapSqlParameterSource p = new MapSqlParameterSource("device", deviceKey);
        List<String> where = baseWhere(p);
        exclude(where, p, excludedSources);
        restrict(where, p, restriction);
        if (query.after() != null) {
            where.add("n.id > :after");
            p.addValue("after", query.after());
        }
        if (query.before() != null) {
            where.add("n.id < :before");
            p.addValue("before", query.before());
        }
        if (!query.events().isEmpty()) {
            where.add("n.event in (:events)");
            p.addValue("events", query.events());
        }
        if (query.minSeverity() != null) {
            where.add("n.severity in (:severities)");
            p.addValue("severities", query.minSeverity().atOrAbove());
        }
        if (query.unread()) {
            where.add("not (n.id <= coalesce(c.read_up_to_id, 0) or r.notification_id is not null)");
        }
        p.addValue("limit", query.limit());
        return jdbc.query("select " + COLUMNS + FROM + " where " + String.join(" and ", where)
                + " order by n.id desc limit :limit", p, this::item);
    }

    /** Items after {@code afterId}, oldest first, at most {@code limit} (the stream replay). */
    public List<Item> after(String deviceKey, long afterId, int limit, Set<String> excludedSources) {
        return after(deviceKey, afterId, limit, excludedSources, ReadRestriction.NONE);
    }

    /** {@link #after(String, long, int, Set)} under {@code restriction} (prompt 035). */
    public List<Item> after(String deviceKey, long afterId, int limit, Set<String> excludedSources,
            ReadRestriction restriction) {
        MapSqlParameterSource p = new MapSqlParameterSource("device", deviceKey).addValue("after", afterId)
                .addValue("limit", limit);
        List<String> where = baseWhere(p);
        exclude(where, p, excludedSources);
        restrict(where, p, restriction);
        where.add("n.id > :after");
        return jdbc.query("select " + COLUMNS + FROM + " where " + String.join(" and ", where)
                + " order by n.id limit :limit", p, this::item);
    }

    /**
     * One item for {@code deviceKey}.
     *
     * @throws NotFoundException when it is not in the inbox
     */
    public Item get(String deviceKey, long id, Set<String> excludedSources) {
        return get(deviceKey, id, excludedSources, ReadRestriction.NONE);
    }

    /** {@link #get(String, long, Set)} under {@code restriction}: outside it is 404 (prompt 035). */
    public Item get(String deviceKey, long id, Set<String> excludedSources, ReadRestriction restriction) {
        MapSqlParameterSource p = new MapSqlParameterSource("device", deviceKey).addValue("id", id);
        List<String> where = baseWhere(p);
        exclude(where, p, excludedSources);
        restrict(where, p, restriction);
        where.add("n.id = :id");
        List<Item> rows = jdbc.query("select " + COLUMNS + FROM + " where " + String.join(" and ", where), p,
                this::item);
        if (rows.isEmpty()) {
            throw NotFoundException.of("Notification", id);
        }
        return rows.getFirst();
    }

    /** The record snapshot (JSON text) of alert {@code alertId}, or null (restricted streams, prompt 035). */
    public String alertSnapshot(long alertId) {
        List<String> rows = jdbc.queryForList("select record_snapshot::text from alert where id = :id",
                new MapSqlParameterSource("id", alertId), String.class);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    /** The newest inbox notification id, 0 for none. */
    public long head() {
        Long head = jdbc.getJdbcTemplate().queryForObject(
                "select coalesce(max(id), 0) from notification where event in ('alert', 'briefing', 'system',"
                        + " 'analyst.note', 'situation.activated', 'situation.escalated')", Long.class);
        return head == null ? 0 : head;
    }

    /** The badge of {@code deviceKey}. */
    public Badge badge(String deviceKey) {
        return badge(deviceKey, Set.of(), ReadRestriction.NONE);
    }

    /**
     * The badge of {@code deviceKey}, counting only what the caller may see: notifications about
     * sources outside {@code excludedSources} and inside {@code restriction} (prompt 035).
     */
    public Badge badge(String deviceKey, Set<String> excludedSources, ReadRestriction restriction) {
        MapSqlParameterSource p = new MapSqlParameterSource("device", deviceKey);
        List<String> where = baseWhere(p);
        exclude(where, p, excludedSources);
        restrict(where, p, restriction);
        where.add("(n.event = 'system' or (n.event = 'alert' and n.subtype = 'alert.fired' and a.state = 'firing'))");
        where.add("not (n.id <= coalesce(c.read_up_to_id, 0) or r.notification_id is not null)");
        Map<String, Object> counts = jdbc.queryForMap("select count(*) as unread,"
                + " count(*) filter (where n.severity = 'critical') as critical" + FROM
                + " where " + String.join(" and ", where), p);
        long head = head();
        return new Badge(((Number) counts.get("unread")).intValue(), ((Number) counts.get("critical")).intValue(),
                head == 0 ? null : head);
    }

    /**
     * True when {@code notificationId} was read on a device whose presence kind is {@code desktop}
     * (presence deferral, task 6).
     */
    public boolean readOnDesktop(long notificationId) {
        Boolean read = jdbc.queryForObject("""
                select exists (
                    select 1 from client_presence p
                     left join notification_read_cursor c on c.device_key = p.device_key
                     left join notification_read r on r.device_key = p.device_key and r.notification_id = :id
                     where p.kind = 'desktop' and (coalesce(c.read_up_to_id, 0) >= :id or r.notification_id is not null))
                """, new MapSqlParameterSource("id", notificationId), Boolean.class);
        return Boolean.TRUE.equals(read);
    }

    // ------------------------------------------------------------------------- writes

    /**
     * Parses a {@code NotificationReadRequest}: exactly one of {@code upToId} (≥ 0) or {@code ids}
     * (1–500 positive ids).
     *
     * @throws ValidationException when it is not valid
     */
    public static ReadRequest parseRead(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The body must be a NotificationReadRequest object.");
        }
        boolean hasUpTo = body.hasNonNull("upToId");
        boolean hasIds = body.hasNonNull("ids");
        if (hasUpTo == hasIds) {
            throw ValidationException.forField("upToId", "give exactly one of upToId or ids");
        }
        for (String field : body.propertyNames()) {
            if (!Set.of("upToId", "ids").contains(field)) {
                throw ValidationException.forField(field, "is not a NotificationReadRequest member");
            }
        }
        if (hasUpTo) {
            JsonNode node = body.get("upToId");
            if (!node.isIntegralNumber() || node.asLong() < 0) {
                throw ValidationException.forField("upToId", "must be a non-negative notification id");
            }
            return new ReadRequest(node.asLong(), List.of());
        }
        JsonNode ids = body.get("ids");
        if (!ids.isArray() || ids.isEmpty() || ids.size() > MAX_SPARSE) {
            throw ValidationException.forField("ids", "must be an array of 1 to " + MAX_SPARSE + " notification ids");
        }
        Set<Long> out = new LinkedHashSet<>();
        List<FieldError> errors = new ArrayList<>();
        for (JsonNode id : ids) {
            if (!id.isIntegralNumber() || id.asLong() < 1) {
                errors.add(new FieldError("ids", "'" + id + "' is not a notification id"));
            } else {
                out.add(id.asLong());
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The read request is not valid.", errors);
        }
        return new ReadRequest(null, new ArrayList<>(out));
    }

    /**
     * Applies a read request for {@code deviceKey} and announces the new badge to the device's open
     * streams. Never touches an alert.
     *
     * @return the badge after the change
     */
    public Badge markRead(String deviceKey, ReadRequest request) {
        Instant now = clock.instant();
        tx.executeWithoutResult(status -> {
            MapSqlParameterSource p = new MapSqlParameterSource("device", deviceKey).addValue("now", Timestamp.from(now));
            if (request.upToId() != null) {
                p.addValue("upTo", request.upToId());
                jdbc.update("""
                        insert into notification_read_cursor (device_key, read_up_to_id, updated_at)
                        values (:device, :upTo, :now)
                        on conflict (device_key) do update
                           set read_up_to_id = greatest(notification_read_cursor.read_up_to_id, excluded.read_up_to_id),
                               updated_at = excluded.updated_at
                        """, p);
                jdbc.update("""
                        delete from notification_read r using notification_read_cursor c
                         where r.device_key = :device and c.device_key = :device and r.notification_id <= c.read_up_to_id
                        """, p);
            } else {
                jdbc.update("""
                        insert into notification_read_cursor (device_key, read_up_to_id, updated_at)
                        values (:device, 0, :now)
                        on conflict (device_key) do update set updated_at = excluded.updated_at
                        """, p);
                for (Long id : request.ids()) {
                    jdbc.update("""
                            insert into notification_read (device_key, notification_id)
                            select CAST(:device AS varchar), CAST(:id AS bigint)
                             where CAST(:id AS bigint) > (select read_up_to_id from notification_read_cursor where device_key = :device)
                            on conflict do nothing
                            """, new MapSqlParameterSource("device", deviceKey).addValue("id", id));
                }
                jdbc.update("""
                        delete from notification_read where device_key = :device and notification_id in (
                            select notification_id from notification_read where device_key = :device
                             order by notification_id desc offset :cap)
                        """, new MapSqlParameterSource("device", deviceKey).addValue("cap", MAX_SPARSE));
            }
        });
        Badge badge = badge(deviceKey);
        events.publishEvent(new InboxBadgeChangedEvent(deviceKey, badge));
        return badge;
    }

    /** Records that {@code deviceKey} was seen (an open inbox stream), keeping its cursor alive. */
    public void touch(String deviceKey) {
        jdbc.update("""
                insert into notification_read_cursor (device_key, read_up_to_id, updated_at) values (:device, 0, :now)
                on conflict (device_key) do update set updated_at = excluded.updated_at
                """, new MapSqlParameterSource("device", deviceKey).addValue("now", Timestamp.from(clock.instant())));
    }

    // ------------------------------------------------------------------------- retention

    /**
     * Inbox retention: notifications older than the {@code inbox} window with no delivery left, sparse
     * reads of notifications that are gone, and cursors of devices unseen for 90 days.
     *
     * @return rows deleted per table
     */
    public Map<String, Integer> purge(Instant now) {
        Map<String, Integer> out = new LinkedHashMap<>();
        out.put("notification", jdbc.update("""
                delete from notification n where n.created_at < :t
                   and not exists (select 1 from notification_delivery d where d.notification_id = n.id)
                """, new MapSqlParameterSource("t", Timestamp.from(now.minus(retention.settings().inbox())))));
        out.put("notification_read", jdbc.getJdbcTemplate().update("""
                delete from notification_read r
                 where not exists (select 1 from notification n where n.id = r.notification_id)
                """));
        out.put("notification_read_cursor", jdbc.update("delete from notification_read_cursor where updated_at < :t",
                new MapSqlParameterSource("t", Timestamp.from(now.minus(java.time.Duration.ofDays(90))))));
        return out;
    }

    // ------------------------------------------------------------------------- helpers

    private List<String> baseWhere(MapSqlParameterSource p) {
        List<String> where = new ArrayList<>();
        where.add("n.event in ('alert', 'briefing', 'system', 'analyst.note', 'situation.activated',"
                + " 'situation.escalated')");
        where.add("n.created_at >= :horizon");
        p.addValue("horizon", Timestamp.from(clock.instant().minus(retention.settings().inbox())));
        return where;
    }

    /**
     * Applies a read restriction (prompt 035): only alert notifications whose alert passes it
     * (source, kind and the record snapshot's geometry); briefing and system notifications are hidden
     * from a principal with data restrictions (fail closed).
     */
    private static void restrict(List<String> where, MapSqlParameterSource p, ReadRestriction restriction) {
        if (restriction == null || !restriction.hasDataRestrictions()) {
            return;
        }
        where.add("n.alert_id is not null");
        RestrictionClamp.sqlPredicate("a.source_id", "a.kind", RestrictionClamp.ALERT_GEOMETRY_SQL, restriction, p,
                "restrict").ifPresent(where::add);
    }

    /** Hides notifications about sources the caller may not see (private sources, 023/039). */
    private static void exclude(List<String> where, MapSqlParameterSource p, Set<String> excludedSources) {
        if (excludedSources != null && !excludedSources.isEmpty()) {
            where.add("(n.data is null or n.data ->> 'sourceId' is null or n.data ->> 'sourceId' not in (:excluded))");
            p.addValue("excluded", List.copyOf(excludedSources));
        }
    }

    @SuppressWarnings("unchecked")
    private Item item(ResultSet rs, int n) throws SQLException {
        long alert = rs.getLong("alert_id");
        Long alertId = rs.wasNull() ? null : alert;
        List<Map<String, Object>> links = List.of();
        String linkText = rs.getString("links");
        if (linkText != null) {
            try {
                links = json.readValue(linkText, List.class);
            } catch (RuntimeException e) {
                links = List.of();
            }
        }
        OffsetDateTime created = rs.getObject("created_at", OffsetDateTime.class);
        return new Item(rs.getLong("id"), rs.getString("event"), rs.getString("subtype"), rs.getString("severity"),
                rs.getString("title"), rs.getString("body_markdown"), alertId,
                rs.getObject("briefing_run_id", UUID.class), rs.getObject("rule_id", UUID.class),
                created == null ? Instant.EPOCH : created.toInstant(), rs.getBoolean("is_read"),
                rs.getString("alert_state"), links);
    }
}
