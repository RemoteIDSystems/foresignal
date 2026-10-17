package ai.foresignal.analytics.eventstudy;

import ai.foresignal.analytics.family.SignalFamilyRegistry;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Resolves an {@code EventSelector} to event occurrences for batch event studies (prompt 119): 103's
 * {@code alertRuleId}, {@code intelEventType} and {@code anomalyRuleId} (+ {@code classification}), plus
 * {@code alertRuleTag}, {@code situationActivated}, {@code changePoint} and {@code unexplainedMove} (by series family)
 * and explicit {@code timestamps}, and since prompt 121 {@code referenceFamily} (one occurrence per region and ISO week
 * with at least one event of the family in {@code reference_history}, at the week's Monday); narrowed by
 * {@code regionCodes} and {@code minSeverity}. The inline {@code rule} form (as in 103) is refused with
 * {@code EVENT_STUDY_INVALID}.
 */
@Component
public class EventSelectorResolver {

    /** The most events one selector yields. */
    public static final int MAX_EVENTS = 5_000;

    /** The selector forms this prompt resolves. */
    public static final List<String> FORMS = List.of("alertRuleId", "alertRuleTag", "intelEventType", "anomalyRuleId",
            "situationActivated", "changePoint", "unexplainedMove", "referenceFamily", "timestamps");

    /** Every selector form of the spec. */
    public static final List<String> ALL_FORMS = List.of("rule", "alertRuleId", "alertRuleTag", "intelEventType",
            "anomalyRuleId", "situationActivated", "changePoint", "unexplainedMove", "referenceFamily", "timestamps");

    private static final List<String> SEVERITY_ORDER = List.of("info", "notable", "warning", "significant",
            "critical");

    private final NamedParameterJdbcTemplate jdbc;
    private final SignalFamilyRegistry families;
    private final JsonMapper json;

    /** Creates the resolver. */
    public EventSelectorResolver(NamedParameterJdbcTemplate jdbc, SignalFamilyRegistry families, JsonMapper json) {
        this.jdbc = jdbc;
        this.families = families;
        this.json = json;
    }

    /**
     * One occurrence.
     *
     * @param at the event time
     * @param ref the object reference, or null
     * @param regions the event's 040 regions
     * @param severity the event's severity, or null
     */
    public record Occurrence(Instant at, String ref, List<String> regions, String severity) {}

    /**
     * Validates a selector.
     *
     * @throws ValidationException 400 {@code EVENT_STUDY_INVALID}
     */
    public void validate(Map<String, Object> selector) {
        if (selector == null || selector.isEmpty()) {
            throw invalid("selector", "is required");
        }
        long forms = selector.keySet().stream().filter(ALL_FORMS::contains).count();
        if (forms != 1) {
            throw invalid("selector", "must name exactly one of " + ALL_FORMS);
        }
        if (selector.containsKey("rule")) {
            throw invalid("selector.rule", "the inline rule selector is not supported by event studies; use"
                    + " alertRuleId, alertRuleTag or intelEventType");
        }
        if (selector.containsKey("referenceFamily")) {
            Object family = selector.get("referenceFamily");
            if (family == null || !ai.foresignal.baserate.reference.ReferenceFamilies.FAMILY.matcher(family.toString())
                    .matches()) {
                throw invalid("selector.referenceFamily", "must be a reference family such as conflict:battles");
            }
        }
        for (String key : List.of("alertRuleId", "anomalyRuleId")) {
            if (selector.get(key) != null) {
                try {
                    UUID.fromString(String.valueOf(selector.get(key)));
                } catch (IllegalArgumentException e) {
                    throw invalid("selector." + key, "must be a UUID");
                }
            }
        }
        for (String key : List.of("changePoint", "unexplainedMove")) {
            if (selector.containsKey(key)) {
                Object family = selector.get(key) instanceof Map<?, ?> m ? m.get("family") : null;
                if (family == null || families.canonical(family.toString()).isEmpty()) {
                    throw invalid("selector." + key + ".family", "must be a signal family of the registry");
                }
            }
        }
        if (selector.containsKey("timestamps")) {
            if (!(selector.get("timestamps") instanceof List<?> list) || list.isEmpty()
                    || list.size() > MAX_EVENTS) {
                throw invalid("selector.timestamps", "must be 1 to " + MAX_EVENTS + " date-times");
            }
            for (Object t : list) {
                try {
                    Instant.parse(String.valueOf(t));
                } catch (RuntimeException e) {
                    throw invalid("selector.timestamps", "'" + t + "' is not a date-time");
                }
            }
        }
        Object min = selector.get("minSeverity");
        if (min != null && !SEVERITY_ORDER.contains(min.toString())) {
            throw invalid("selector.minSeverity", "must be one of " + SEVERITY_ORDER);
        }
    }

    /** The occurrences of a validated selector in {@code [from, to]}, oldest first, at most {@value #MAX_EVENTS}. */
    public List<Occurrence> resolve(Map<String, Object> selector, Instant from, Instant to) {
        validate(selector);
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("from", Timestamp.from(from)).addValue("to", Timestamp.from(to))
                .addValue("limit", MAX_EVENTS);
        List<Occurrence> out;
        if (selector.get("intelEventType") != null) {
            p.addValue("type", selector.get("intelEventType").toString());
            out = jdbc.query("SELECT id, start_time, region_tags::text AS tags, severity FROM intelligence_event WHERE"
                    + " type = :type AND start_time >= :from AND start_time <= :to AND merged_into IS NULL ORDER BY"
                    + " start_time, id LIMIT :limit", p, (rs, n) -> new Occurrence(ts(rs, "start_time"),
                            "intel-event:" + rs.getString("id"), jsonStrings(rs.getString("tags")),
                            rs.getString("severity")));
        } else if (selector.get("alertRuleId") != null || selector.get("alertRuleTag") != null) {
            String where;
            if (selector.get("alertRuleId") != null) {
                where = "rule_id = :rule";
                p.addValue("rule", UUID.fromString(selector.get("alertRuleId").toString()));
            } else {
                where = "tags @> CAST(:tag AS jsonb)";
                p.addValue("tag", json.writeValueAsString(List.of(selector.get("alertRuleTag").toString())));
            }
            out = jdbc.query("SELECT id, fired_at, region ->> 'iso2' AS iso2, region ->> 'admin1' AS admin1, severity"
                    + " FROM alert WHERE " + where + " AND fired_at >= :from AND fired_at <= :to AND suppressed ="
                    + " false AND NOT exercise ORDER BY fired_at, id LIMIT :limit", p, (rs, n) -> {
                        List<String> regions = new ArrayList<>();
                        if (rs.getString("iso2") != null) {
                            regions.add(rs.getString("iso2"));
                        }
                        if (rs.getString("admin1") != null) {
                            regions.add(rs.getString("admin1"));
                        }
                        return new Occurrence(ts(rs, "fired_at"), "alert:" + rs.getString("id"), regions,
                                rs.getString("severity"));
                    });
        } else if (selector.get("anomalyRuleId") != null) {
            p.addValue("rule", UUID.fromString(selector.get("anomalyRuleId").toString()));
            String classes = selector.get("classification") == null ? "('activity-up', 'activity-down')"
                    : "(:classification)";
            if (selector.get("classification") != null) {
                p.addValue("classification", selector.get("classification").toString());
            }
            out = jdbc.query("SELECT bucket_start, group_key FROM anomaly_observation WHERE rule_id = :rule AND"
                    + " bucket_start >= :from AND bucket_start <= :to AND classification IN " + classes
                    + " ORDER BY bucket_start, group_key LIMIT :limit", p, (rs, n) -> {
                        String group = rs.getString("group_key");
                        List<String> regions = group != null && group.matches("^[A-Z]{2}(-[A-Z0-9]{1,3})?$")
                                ? List.of(group) : List.of();
                        return new Occurrence(ts(rs, "bucket_start"), null, regions, null);
                    });
        } else if (selector.containsKey("situationActivated")) {
            Object theme = selector.get("situationActivated") instanceof Map<?, ?> m ? m.get("theme") : null;
            String themeClause = "";
            if (theme != null) {
                themeClause = " AND s.theme = :theme";
                p.addValue("theme", theme.toString());
            }
            out = jdbc.query("SELECT s.id, l.at, array_to_string(s.region_codes, ',') AS regions, s.severity FROM"
                    + " situation_log l JOIN situation s ON s.id = l.situation_id WHERE l.event = 'situation.activated'"
                    + " AND l.at >= :from AND l.at <= :to" + themeClause + " ORDER BY l.at, l.id LIMIT :limit", p,
                    (rs, n) -> new Occurrence(ts(rs, "at"), "situation:" + rs.getString("id"),
                            csv(rs.getString("regions")), rs.getString("severity")));
        } else if (selector.containsKey("referenceFamily")) {
            p.addValue("family", selector.get("referenceFamily").toString())
                    .addValue("fromDay", java.sql.Date.valueOf(from.atZone(java.time.ZoneOffset.UTC).toLocalDate()))
                    .addValue("toDay", java.sql.Date.valueOf(to.atZone(java.time.ZoneOffset.UTC).toLocalDate()));
            out = jdbc.query("SELECT week_start, region_code FROM reference_history WHERE family = :family AND count > 0"
                    + " AND region_code <> 'ZZ' AND week_start >= :fromDay AND week_start <= :toDay GROUP BY 1, 2"
                    + " ORDER BY 1, 2 LIMIT :limit", p, (rs, n) -> new Occurrence(rs.getDate("week_start")
                            .toLocalDate().atStartOfDay(java.time.ZoneOffset.UTC).toInstant(), null,
                            List.of(rs.getString("region_code")), null));
        } else if (selector.containsKey("changePoint") || selector.containsKey("unexplainedMove")) {
            boolean changePoint = selector.containsKey("changePoint");
            Map<?, ?> spec = (Map<?, ?>) selector.get(changePoint ? "changePoint" : "unexplainedMove");
            p.addValue("type", changePoint ? "change-point" : "unexplained-move");
            p.addValue("families", List.copyOf(families.storedNames(spec.get("family").toString())));
            out = jdbc.query("SELECT id, coalesce(window_start, detected_at) AS at, array_to_string(region_codes, ',')"
                    + " AS regions, severity FROM analytic_finding WHERE finding_type = :type AND status <> 'dismissed'"
                    + " AND signal_family IN (:families) AND coalesce(window_start, detected_at) >= :from AND"
                    + " coalesce(window_start, detected_at) <= :to ORDER BY 2, id LIMIT :limit", p,
                    (rs, n) -> new Occurrence(ts(rs, "at"), "finding:" + rs.getString("id"),
                            csv(rs.getString("regions")), rs.getString("severity")));
        } else {
            out = new ArrayList<>();
            for (Object t : (List<?>) selector.get("timestamps")) {
                Instant at = Instant.parse(String.valueOf(t));
                if (!at.isBefore(from) && !at.isAfter(to)) {
                    out.add(new Occurrence(at, null, List.of(), null));
                }
            }
            out.sort(java.util.Comparator.comparing(Occurrence::at));
        }
        return filter(out, strings(selector.get("regionCodes")), selector.get("minSeverity") == null ? null
                : selector.get("minSeverity").toString());
    }

    /** Keeps the occurrences in one of {@code regions} (when given) at or above {@code minSeverity} (when given). */
    static List<Occurrence> filter(List<Occurrence> in, Collection<String> regions, String minSeverity) {
        List<Occurrence> out = new ArrayList<>();
        int min = minSeverity == null ? -1 : SEVERITY_ORDER.indexOf(minSeverity.toLowerCase(Locale.ROOT));
        for (Occurrence o : in) {
            if (!regions.isEmpty() && o.regions().stream().noneMatch(r -> regions.contains(r)
                    || (r.length() > 2 && regions.contains(r.substring(0, 2))))) {
                continue;
            }
            if (min >= 0 && (o.severity() == null || SEVERITY_ORDER.indexOf(o.severity()) < min)) {
                continue;
            }
            out.add(o);
        }
        return out;
    }

    private static Instant ts(ResultSet rs, String column) throws SQLException {
        return rs.getTimestamp(column).toInstant();
    }

    private List<String> jsonStrings(String text) {
        List<String> out = new ArrayList<>();
        if (text == null) {
            return out;
        }
        try {
            for (var node : json.readTree(text)) {
                if (node.isString()) {
                    out.add(node.asString());
                }
            }
        } catch (RuntimeException e) {
            return out;
        }
        return out;
    }

    private static List<String> csv(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return List.of(text.split(","));
    }

    private static Set<String> strings(Object value) {
        Set<String> out = new java.util.LinkedHashSet<>();
        if (value instanceof List<?> list) {
            for (Object o : list) {
                if (o != null) {
                    out.add(o.toString());
                }
            }
        }
        return out;
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(ErrorCodes.EVENT_STUDY_INVALID, "The event study is invalid: " + field + " "
                + message + ".", List.of(new FieldError(field, message)));
    }
}
