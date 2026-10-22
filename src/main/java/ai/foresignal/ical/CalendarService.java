package ai.foresignal.ical;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.briefing.BriefingEntity;
import ai.foresignal.briefing.BriefingRepository;
import ai.foresignal.briefing.BriefingService;
import ai.foresignal.briefing.BriefingValidator;
import ai.foresignal.provenance.AttributionService;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RestrictionClamp;
import ai.foresignal.security.visibility.Viewer;
import ai.foresignal.settings.ClientLinks;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.SqlParams;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Builds the events of Foresignal's iCalendar feeds (prompt 112, {@code GET /v1/calendars/{feed}.ics}):
 *
 * <ul>
 *   <li>{@code launches} ({@code launch-library}, kind {@code launch}): DTSTART = {@code net}, DTEND =
 *       {@code windowEnd} or NET + 1 h, {@code CONFIRMED} when the status is {@code Go} else {@code TENTATIVE},
 *       LOCATION = pad name, GEO = the pad (the record's point);
 *   <li>{@code tfr} ({@code faa-tfr}): effective ({@code validFrom}) → DTSTART, expiry ({@code validUntil}) →
 *       DTEND; open-ended restrictions get DTSTART only and {@code X-FORESIGNAL-OPEN-ENDED:TRUE};
 *   <li>{@code nws} ({@code nws-alerts}): onset ({@code validFrom}) → DTSTART, {@code ends} or {@code expires} →
 *       DTEND, SUMMARY = event and area;
 *   <li>{@code briefings} (029): the occurrences of each enabled, visible briefing in the next 30 days, expanded
 *       from its cron schedule and timezone (DST-correct) as individual events; cron is never translated to RRULE;
 *   <li>{@code custom}: any kind whose descriptor declares {@code startField}/{@code endField} (008).
 * </ul>
 *
 * <p>Record events: UID {@code {sourceId}:{externalId}@{instanceId}}, SEQUENCE = record version, DTSTAMP and
 * LAST-MODIFIED = when that version was ingested, URL = the record deep link, DESCRIPTION ends with the
 * attribution line (109). Tombstoned or expired records stay for {@value #CANCELED_DAYS} days as
 * {@code CANCELLED}. A source is included only when the caller may see it (private sources, restrictions) and
 * {@code EgressPolicy} allows it on the {@code FEED} path. At most {@value #MAX_EVENTS} events.
 */
@Service
public class CalendarService {

    /** The feeds. */
    public static final List<String> FEEDS = List.of("launches", "tfr", "nws", "briefings", "custom");

    /** The most events in one feed. */
    public static final int MAX_EVENTS = 2_000;

    /** Days a tombstoned or expired record stays as {@code CANCELLED}. */
    public static final int CANCELED_DAYS = 7;

    /** How far ahead briefing occurrences are expanded. */
    public static final Duration BRIEFING_HORIZON = Duration.ofDays(30);

    static final String LAUNCH_SOURCE = "launch-library";
    static final String TFR_SOURCE = "faa-tfr";
    static final String NWS_SOURCE = "nws-alerts";

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final NamedParameterJdbcTemplate jdbc;
    private final SourceRegistry registry;
    private final EgressPolicy egress;
    private final AttributionService attributions;
    private final BriefingRepository briefings;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public CalendarService(NamedParameterJdbcTemplate jdbc, SourceRegistry registry, EgressPolicy egress,
            AttributionService attributions, BriefingRepository briefings, JsonMapper json, Clock clock) {
        this.jdbc = jdbc;
        this.registry = registry;
        this.egress = egress;
        this.attributions = attributions;
        this.briefings = briefings;
        this.json = json;
        this.clock = clock;
    }

    /**
     * A calendar request.
     *
     * @param feed the feed name
     * @param sources the {@code source} filter, or empty
     * @param kinds the {@code kind} filter, or empty
     * @param bbox the bbox filter, or null
     * @param excludedSources the caller's excluded sources
     * @param restriction the caller's restriction (narrowed by {@code placeId})
     * @param viewer the caller, for the egress decision
     * @param instanceId the install id (UIDs)
     * @param recordLink the {@code clientLinks.record} template
     */
    public record Request(String feed, List<String> sources, List<String> kinds, BboxFilter bbox,
            Set<String> excludedSources, ReadRestriction restriction, Viewer viewer, String instanceId,
            String recordLink) {}

    /**
     * The events of a feed.
     *
     * @param name the calendar name
     * @param events the events, at most {@link #MAX_EVENTS}
     * @param truncated true when more events matched
     */
    public record Result(String name, List<ICalWriter.Event> events, boolean truncated) {}

    /**
     * Builds a feed.
     *
     * @throws NotFoundException for an unknown feed
     * @throws ValidationException ({@code ICAL_FIELDS_MISSING}) when a custom kind lacks interval fields
     */
    public Result build(Request r) {
        return switch (r.feed()) {
            case "launches" -> records(r, "Foresignal launches", List.of(LAUNCH_SOURCE), List.of("launch"), this::launch);
            case "tfr" -> records(r, "Foresignal TFRs", List.of(TFR_SOURCE), List.of("airspace-restriction"), this::tfr);
            case "nws" -> records(r, "Foresignal NWS alerts", List.of(NWS_SOURCE), List.of("nws-alert"), this::nws);
            case "briefings" -> briefingFeed(r);
            case "custom" -> custom(r);
            default -> throw new NotFoundException(ErrorCodes.NOT_FOUND, "Unknown calendar " + r.feed()
                    + "; one of " + String.join(", ", FEEDS) + ".");
        };
    }

    // ---------------------------------------------------------------------------------- record feeds

    /** One record row as read for a calendar. */
    record Row(String sourceId, String externalId, String kind, int version, String title, boolean tombstone,
            Instant validFrom, Instant validUntilExplicit, Instant validUntil, Instant observedAt, Instant eventTime,
            Instant ingestedAt, Double lat, Double lon, Map<String, Object> payload) {}

    /** The start, end, status and the feed-specific properties of a row. */
    record Timing(Instant start, Instant end, String summary, String location, String status,
            Map<String, String> extra) {}

    @FunctionalInterface
    interface Mapper {
        Timing map(Row row);
    }

    private Result records(Request r, String name, List<String> feedSources, List<String> feedKinds, Mapper mapper) {
        List<String> sources = new ArrayList<>();
        for (String s : feedSources) {
            if ((r.sources().isEmpty() || r.sources().contains(s)) && visible(s, r)) {
                sources.add(s);
            }
        }
        List<String> kinds = feedKinds.stream().filter(k -> r.kinds().isEmpty() || r.kinds().contains(k)).toList();
        if (sources.isEmpty() || kinds.isEmpty()) {
            return new Result(name, List.of(), false);
        }
        List<Row> rows = query(r, sources, kinds);
        boolean truncated = rows.size() > MAX_EVENTS;
        List<ICalWriter.Event> events = new ArrayList<>();
        Instant now = clock.instant();
        for (Row row : truncated ? rows.subList(0, MAX_EVENTS) : rows) {
            boolean canceled = row.tombstone() || (row.validUntil() != null && !row.validUntil().isAfter(now));
            Timing t = mapper.map(row);
            if ((t == null || t.start() == null) && canceled && row.observedAt() != null) {
                // a tombstone may carry no payload: keep the canceled event at its last observation
                t = new Timing(row.observedAt(), null, summary(row), null, "CANCELLED", Map.of());
            }
            if (t == null || t.start() == null) {
                continue;
            }
            events.add(new ICalWriter.Event(row.sourceId() + ":" + row.externalId() + "@" + r.instanceId(),
                    row.version(), row.ingestedAt(), row.ingestedAt(), t.start(), t.end(), t.summary(),
                    description(row), t.location(), row.lat() == null ? null : new double[] {row.lat(), row.lon()},
                    link(r.recordLink(), row), canceled ? "CANCELLED" : t.status(), t.extra()));
        }
        return new Result(name, events, truncated);
    }

    private Result custom(Request r) {
        if (r.sources().isEmpty() || r.kinds().isEmpty()) {
            throw ValidationException.forField(r.sources().isEmpty() ? "source" : "kind",
                    "is required for the custom calendar");
        }
        Map<String, SourceDescriptor> descriptors = new LinkedHashMap<>();
        for (String s : r.sources()) {
            SourceDescriptor d = registry.contains(s) ? registry.descriptor(s) : null;
            if (d == null) {
                throw new NotFoundException(ErrorCodes.NOT_FOUND, "Unknown source " + s + ".");
            }
            for (String kind : r.kinds()) {
                if (!d.kinds().contains(kind) || d.startField() == null || d.endField() == null) {
                    throw new ValidationException(ErrorCodes.ICAL_FIELDS_MISSING, "Kind " + kind + " of source " + s
                            + " declares no startField/endField, so it cannot be a calendar.", List.of());
                }
            }
            descriptors.put(s, d);
        }
        return records(r, "Foresignal " + String.join(", ", r.kinds()), List.copyOf(descriptors.keySet()), r.kinds(),
                row -> {
                    SourceDescriptor d = descriptors.get(row.sourceId());
                    Instant start = instant(row.payload().get(d.startField()));
                    Instant end = instant(row.payload().get(d.endField()));
                    return new Timing(start, end, summary(row), null, "CONFIRMED", Map.of());
                });
    }

    Timing launch(Row row) {
        Instant net = instant(row.payload().get("net"));
        if (net == null) {
            net = row.eventTime();
        }
        if (net == null) {
            return null;
        }
        Instant end = instant(row.payload().get("windowEnd"));
        if (end == null || !end.isAfter(net)) {
            end = net.plus(Duration.ofHours(1));
        }
        String status = "Go".equalsIgnoreCase(string(row.payload().get("status"))) ? "CONFIRMED" : "TENTATIVE";
        return new Timing(net, end, summary(row), string(row.payload().get("padName")), status, Map.of());
    }

    Timing tfr(Row row) {
        Instant start = row.validFrom() != null ? row.validFrom() : row.observedAt();
        Instant end = row.validUntilExplicit();
        Map<String, String> extra = end == null ? Map.of("X-FORESIGNAL-OPEN-ENDED", "TRUE") : Map.of();
        return new Timing(start, end, summary(row), null, "CONFIRMED", extra);
    }

    Timing nws(Row row) {
        Instant start = row.validFrom();
        if (start == null) {
            start = instant(row.payload().get("onset"));
        }
        if (start == null) {
            start = instant(row.payload().get("effective"));
        }
        if (start == null) {
            start = row.eventTime() != null ? row.eventTime() : row.observedAt();
        }
        Instant end = instant(row.payload().get("ends"));
        if (end == null) {
            end = instant(row.payload().get("expires"));
        }
        String event = string(row.payload().get("event"));
        String area = string(row.payload().get("areaDesc"));
        String summary = event == null ? summary(row) : area == null ? event : event + " — " + area;
        return new Timing(start, end, summary, area, "CONFIRMED", Map.of());
    }

    private List<Row> query(Request r, List<String> sources, List<String> kinds) {
        Instant now = clock.instant();
        Instant keep = now.minus(Duration.ofDays(CANCELED_DAYS));
        SqlParams p = new SqlParams();
        List<String> where = new ArrayList<>();
        where.add("h.source_id = ANY(" + p.textArray(sources) + ")");
        where.add("h.kind = ANY(" + p.textArray(kinds) + ")");
        where.add("(NOT h.tombstone OR v.ingested_at > " + p.instant(keep) + ")");
        where.add("(l.valid_until IS NULL OR l.valid_until > " + p.instant(keep) + ")");
        RestrictionClamp.addTo(where, "h.source_id", "h.kind", "l.geom", "jsonb_strip_nulls(h.payload || l.kinematic)",
                r.restriction(), p);
        if (r.bbox() != null) {
            where.add(RecordQueryService.bboxClause("l.geom", r.bbox(), p));
        }
        String sql = "SELECT h.source_id, h.external_id, h.kind, h.version, h.title, h.tombstone, h.valid_from,"
                + " h.valid_until_explicit, l.valid_until, l.observed_at, h.event_time, v.ingested_at,"
                + " ST_Y(ST_PointOnSurface(l.geom)) AS lat, ST_X(ST_PointOnSurface(l.geom)) AS lon,"
                + " (jsonb_strip_nulls(h.payload || l.kinematic))::text AS payload"
                + " FROM record_head h JOIN record_live l ON l.source_id = h.source_id AND l.external_id = h.external_id"
                + " LEFT JOIN record_version v ON v.seq = h.seq"
                + " WHERE " + String.join(" AND ", where)
                + " ORDER BY COALESCE(h.event_time, l.observed_at) DESC, h.source_id, h.external_id LIMIT "
                + p.add(MAX_EVENTS + 1);
        return jdbc.query(sql, p.source(), (rs, i) -> row(rs));
    }

    private Row row(ResultSet rs) throws SQLException {
        String payload = rs.getString("payload");
        Map<String, Object> map = payload == null ? Map.of() : json.readValue(payload, MAP);
        Object lat = rs.getObject("lat");
        Object lon = rs.getObject("lon");
        return new Row(rs.getString("source_id"), rs.getString("external_id"), rs.getString("kind"),
                rs.getInt("version"), rs.getString("title"), rs.getBoolean("tombstone"), ts(rs, "valid_from"),
                ts(rs, "valid_until_explicit"), ts(rs, "valid_until"), ts(rs, "observed_at"), ts(rs, "event_time"),
                ts(rs, "ingested_at"), lat == null ? null : ((Number) lat).doubleValue(),
                lon == null ? null : ((Number) lon).doubleValue(), map);
    }

    private boolean visible(String sourceId, Request r) {
        return registry.contains(sourceId) && !r.excludedSources().contains(sourceId)
                && r.restriction().allowsSource(sourceId) && egress.allows(EgressPath.FEED, sourceId, r.viewer());
    }

    private String description(Row row) {
        StringBuilder sb = new StringBuilder();
        Object text = row.payload().get("description");
        if (text instanceof String s && !s.isBlank()) {
            sb.append(s.strip()).append("\n\n");
        }
        List<AttributionService.Attribution> attrs = attributions.scope(row.sourceId(), row.kind());
        String attribution = attributions.header(attrs);
        sb.append("Source: ").append(row.sourceId());
        if (attribution != null && !attribution.isBlank()) {
            sb.append(". ").append(attribution);
        }
        return sb.toString();
    }

    private static String summary(Row row) {
        return row.title() != null && !row.title().isBlank() ? row.title() : row.kind() + " " + row.externalId();
    }

    private static String link(String template, Row row) {
        if (template == null) {
            return null;
        }
        return ClientLinks.fill(template, Map.of("source", encode(row.sourceId()), "externalId",
                encode(row.externalId())));
    }

    // -------------------------------------------------------------------------------------- briefings

    private Result briefingFeed(Request r) {
        Instant now = clock.instant();
        Instant horizon = now.plus(BRIEFING_HORIZON);
        List<ICalWriter.Event> events = new ArrayList<>();
        boolean truncated = false;
        for (BriefingEntity b : briefings.findAllByOrderByCreatedAtAscIdAsc()) {
            if (!b.isEnabled() || !BriefingService.visibleTo(b, r.restriction())) {
                continue;
            }
            for (Instant at : occurrences(b.getSchedule(), b.getTimezone(), now, horizon)) {
                if (events.size() >= MAX_EVENTS) {
                    truncated = true;
                    break;
                }
                Instant stamp = b.getUpdatedAt() != null ? b.getUpdatedAt() : b.getCreatedAt();
                events.add(new ICalWriter.Event("briefing-" + b.getId() + "-" + at.getEpochSecond() + "@" + r.instanceId(),
                        0, stamp, stamp, at, null, "Briefing: " + b.getName(), "Scheduled Foresignal briefing ("
                        + b.getSchedule() + ", " + b.getTimezone() + ").", null, null, null, "CONFIRMED", Map.of()));
            }
        }
        return new Result("Foresignal briefings", events, truncated);
    }

    /**
     * The occurrences of a 5-field cron schedule in {@code timezone} after {@code from} and up to {@code to},
     * each computed in local time so they follow daylight-saving changes.
     */
    static List<Instant> occurrences(String schedule, String timezone, Instant from, Instant to) {
        List<Instant> out = new ArrayList<>();
        Instant at = from;
        while (out.size() <= MAX_EVENTS) {
            Instant next = BriefingValidator.nextRun(schedule, timezone, at);
            if (next == null || next.isAfter(to)) {
                break;
            }
            out.add(next);
            at = next;
        }
        return out;
    }

    // ---------------------------------------------------------------------------------------- helpers

    /** An ISO-8601 string or an epoch number (seconds, or milliseconds above 10^11) as an instant. */
    static Instant instant(Object value) {
        if (value instanceof Number n) {
            long v = n.longValue();
            return v > 100_000_000_000L ? Instant.ofEpochMilli(v) : Instant.ofEpochSecond(v);
        }
        if (value instanceof String s && !s.isBlank()) {
            try {
                return OffsetDateTime.parse(s.strip()).toInstant();
            } catch (DateTimeParseException e) {
                try {
                    return Instant.parse(s.strip());
                } catch (DateTimeParseException e2) {
                    return null;
                }
            }
        }
        return null;
    }

    private static String string(Object value) {
        return value instanceof String s && !s.isBlank() ? s : null;
    }

    private static Instant ts(ResultSet rs, String column) throws SQLException {
        Timestamp t = rs.getTimestamp(column);
        return t == null ? null : t.toInstant();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
