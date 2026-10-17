package ai.foresignal.precursor;

import ai.foresignal.analytics.family.SignalFamilyRegistry;
import ai.foresignal.backtest.BacktestCoverage;
import ai.foresignal.backtest.PrecursorTester;
import ai.foresignal.evidence.EvidenceService;
import ai.foresignal.evidence.RecordRef;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.store.query.ReadQueryRunner;
import ai.foresignal.trigger.TriggerRuleService;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Event occurrences and event-family counts for precursor mining (prompt 120 §2).
 *
 * <p><b>Families</b> are intel event types (101), alert rule tags (017), anomaly classifications per rule (100), change
 * points and {@code unexplained-move} findings per signal family (119) and situation activations per theme (118); each
 * family is an {@code EventSelector}. Since prompt 121, reference-history families ({@code referenceFamily}) add one
 * occurrence per country and ISO week with at least one event (at the week's Monday).
 *
 * <p><b>Occurrences</b> are read per selector, each query in its own read transaction with the 103 statement timeout.
 * Mining uses non-private data only: private intel events, situations and findings and alerts or anomalies of private
 * sources are left out, so a precursor never rests on a private source (EGR-1). Alerts are collapsed to their 116
 * canonical events (the first alert per canonical event counts), so re-reports never inflate antecedent or consequent
 * counts.
 */
@Component
public class PrecursorOccurrences {

    /** The most occurrences read per selector. */
    public static final int MAX_OCCURRENCES = 100_000;

    /** The share of a family's occurrences in a region that must carry admin1 for {@code sameRegion: admin1}. */
    public static final double ADMIN1_SHARE = 0.8;

    private static final Pattern REGION = Pattern.compile("^[A-Z]{2}(-[A-Z0-9]{1,3})?$");
    private static final List<String> SEVERITY_ORDER = List.of("info", "notable", "warning", "significant",
            "critical");

    private final ReadQueryRunner reads;
    private final SignalFamilyRegistry families;
    private final TriggerRuleService rules;
    private final ObjectProvider<IntelVisibility> visibility;
    private final ObjectProvider<EvidenceService> evidence;
    private final JsonMapper json;

    /** Creates the component. */
    public PrecursorOccurrences(ReadQueryRunner reads, SignalFamilyRegistry families, TriggerRuleService rules,
            ObjectProvider<IntelVisibility> visibility, ObjectProvider<EvidenceService> evidence, JsonMapper json) {
        this.reads = reads;
        this.families = families;
        this.rules = rules;
        this.visibility = visibility;
        this.evidence = evidence;
        this.json = json;
    }

    /**
     * One occurrence.
     *
     * @param at when it happened
     * @param ref the object reference ({@code intel-event:id}, {@code alert:id}, {@code finding:id}, …), or null
     * @param country the country, or null
     * @param admin1 the first-level region, or null
     * @param sourceId the source of an alert's record (or the anomaly rule's group), or null
     * @param externalId the external id of an alert's record, or null
     * @param severity the severity, or null
     */
    public record Occurrence(Instant at, String ref, String country, String admin1, String sourceId,
            String externalId, String severity) {

        /** The occurrence as the 103 tester takes it (the reference is its entity). */
        public PrecursorTester.Occurrence tester() {
            return new PrecursorTester.Occurrence(at, null, null, country, admin1,
                    ref == null ? Set.of() : Set.of(ref));
        }

        /** The 040 region codes of the occurrence. */
        public List<String> regions() {
            List<String> out = new ArrayList<>();
            if (country != null) {
                out.add(country);
            }
            if (admin1 != null) {
                out.add(admin1);
            }
            return out;
        }
    }

    /**
     * The occurrences of a selector in {@code [from, to)}, oldest first: non-private, alerts collapsed to canonical
     * events, narrowed by the selector's {@code regionCodes} and {@code minSeverity}.
     *
     * @param selector the {@code EventSelector}
     * @param from the start
     * @param to the end (exclusive)
     * @return the occurrences
     */
    public List<Occurrence> resolve(Map<String, Object> selector, Instant from, Instant to) {
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("from", Timestamp.from(from))
                .addValue("to", Timestamp.from(to)).addValue("limit", MAX_OCCURRENCES);
        List<Occurrence> out;
        if (selector.get("intelEventType") != null) {
            p.addValue("type", selector.get("intelEventType").toString());
            out = read("SELECT id, start_time, region_tags::text AS tags, severity FROM intelligence_event WHERE type ="
                    + " :type AND start_time >= :from AND start_time < :to AND merged_into IS NULL AND private = false"
                    + " ORDER BY start_time, id LIMIT :limit", p, (rs, n) -> {
                        String[] region = regionOfTags(rs.getString("tags"));
                        return new Occurrence(ts(rs, "start_time"), "intel-event:" + rs.getString("id"), region[0],
                                region[1], null, null, rs.getString("severity"));
                    });
        } else if (selector.get("alertRuleId") != null || selector.get("alertRuleTag") != null) {
            String where;
            if (selector.get("alertRuleId") != null) {
                where = "rule_id = :rule";
                p.addValue("rule", UUID.fromString(selector.get("alertRuleId").toString()));
            } else {
                where = "tags @> CAST(:tag AS jsonb)";
                p.addValue("tag", json.writeValueAsString(List.of(selector.get("alertRuleTag").toString())));
            }
            out = read("SELECT id, fired_at, source_id, external_id, region ->> 'iso2' AS iso2, region ->> 'admin1' AS"
                    + " admin1, severity FROM alert WHERE " + where + " AND fired_at >= :from AND fired_at < :to AND"
                    + " suppressed = false AND NOT exercise ORDER BY fired_at, id LIMIT :limit", p,
                    (rs, n) -> new Occurrence(ts(rs, "fired_at"), "alert:" + rs.getLong("id"), rs.getString("iso2"),
                            rs.getString("admin1"), rs.getString("source_id"), rs.getString("external_id"),
                            rs.getString("severity")));
            out = canonical(withoutPrivateSources(out));
        } else if (selector.get("anomalyRuleId") != null) {
            UUID rule = UUID.fromString(selector.get("anomalyRuleId").toString());
            if (privateRule(rule)) {
                return List.of();
            }
            p.addValue("rule", rule);
            String classes = selector.get("classification") == null ? "('activity-up', 'activity-down')"
                    : "(:classification)";
            if (selector.get("classification") != null) {
                p.addValue("classification", selector.get("classification").toString());
            }
            out = read("SELECT id, bucket_start, group_key FROM anomaly_observation WHERE rule_id = :rule AND"
                    + " bucket_start >= :from AND bucket_start < :to AND classification IN " + classes
                    + " ORDER BY bucket_start, group_key LIMIT :limit", p, (rs, n) -> {
                        String[] region = regionOfCode(rs.getString("group_key"));
                        return new Occurrence(ts(rs, "bucket_start"), "anomaly:" + rs.getLong("id"), region[0],
                                region[1], null, null, null);
                    });
        } else if (selector.containsKey("situationActivated")) {
            Object theme = selector.get("situationActivated") instanceof Map<?, ?> m ? m.get("theme") : null;
            String themeClause = "";
            if (theme != null) {
                themeClause = " AND s.theme = :theme";
                p.addValue("theme", theme.toString());
            }
            out = read("SELECT s.id, l.at, array_to_string(s.region_codes, ',') AS regions, s.severity FROM"
                    + " situation_log l JOIN situation s ON s.id = l.situation_id WHERE l.event = 'situation.activated'"
                    + " AND s.private = false AND l.at >= :from AND l.at < :to" + themeClause
                    + " ORDER BY l.at, l.id LIMIT :limit", p, (rs, n) -> {
                        String[] region = regionOfCsv(rs.getString("regions"));
                        return new Occurrence(ts(rs, "at"), "situation:" + rs.getString("id"), region[0], region[1],
                                null, null, rs.getString("severity"));
                    });
        } else if (selector.containsKey("changePoint") || selector.containsKey("unexplainedMove")) {
            boolean changePoint = selector.containsKey("changePoint");
            Object family = selector.get(changePoint ? "changePoint" : "unexplainedMove") instanceof Map<?, ?> m
                    ? m.get("family") : null;
            if (family == null) {
                return List.of();
            }
            p.addValue("type", changePoint ? "change-point" : "unexplained-move");
            p.addValue("families", List.copyOf(families.storedNames(family.toString())));
            out = read("SELECT id, coalesce(window_start, detected_at) AS at, array_to_string(region_codes, ',') AS"
                    + " regions, severity FROM analytic_finding WHERE finding_type = :type AND status <> 'dismissed'"
                    + " AND private = false AND signal_family IN (:families) AND coalesce(window_start, detected_at)"
                    + " >= :from AND coalesce(window_start, detected_at) < :to ORDER BY 2, id LIMIT :limit", p,
                    (rs, n) -> {
                        String[] region = regionOfCsv(rs.getString("regions"));
                        return new Occurrence(ts(rs, "at"), "finding:" + rs.getString("id"), region[0], region[1],
                                null, null, rs.getString("severity"));
                    });
        } else if (selector.get("referenceFamily") != null) {
            p.addValue("family", selector.get("referenceFamily").toString())
                    .addValue("fromDay", java.sql.Date.valueOf(from.atZone(java.time.ZoneOffset.UTC).toLocalDate()))
                    .addValue("toDay", java.sql.Date.valueOf(to.atZone(java.time.ZoneOffset.UTC).toLocalDate()));
            out = read("SELECT week_start, region_code FROM reference_history WHERE family = :family AND count > 0 AND"
                    + " length(region_code) = 2 AND region_code <> 'ZZ' AND week_start >= :fromDay AND week_start <"
                    + " :toDay GROUP BY 1, 2 ORDER BY 1, 2 LIMIT :limit", p, (rs, n) -> new Occurrence(
                            rs.getDate("week_start").toLocalDate().atStartOfDay(java.time.ZoneOffset.UTC).toInstant(),
                            "reference:" + selector.get("referenceFamily") + "/" + rs.getString("region_code") + "/"
                                    + rs.getDate("week_start").toLocalDate(), rs.getString("region_code"), null, null,
                            null, null));
        } else if (selector.get("timestamps") instanceof List<?> list) {
            out = new ArrayList<>();
            for (Object t : list) {
                Instant at = Instant.parse(String.valueOf(t));
                if (!at.isBefore(from) && at.isBefore(to)) {
                    out.add(new Occurrence(at, null, null, null, null, null, null));
                }
            }
            out.sort(Comparator.comparing(Occurrence::at));
        } else {
            return List.of();
        }
        return filter(out, strings(selector.get("regionCodes")), selector.get("minSeverity") == null ? null
                : selector.get("minSeverity").toString());
    }

    /**
     * The sources whose coverage gaps matter to a selector's occurrences (alert records' sources and the anomaly rule's
     * sources); derived families (intel events, situations, findings) have none of their own.
     */
    public Set<String> sources(Map<String, Object> selector, List<Occurrence> occurrences) {
        Set<String> out = new LinkedHashSet<>();
        for (Occurrence o : occurrences) {
            if (o.sourceId() != null) {
                out.add(o.sourceId());
            }
        }
        if (selector.get("anomalyRuleId") != null) {
            try {
                out.addAll(rules.load(UUID.fromString(selector.get("anomalyRuleId").toString())).getSources());
            } catch (RuntimeException e) {
                // A deleted rule contributes no sources.
            }
        }
        if (selector.get("alertRuleId") != null) {
            try {
                out.addAll(rules.load(UUID.fromString(selector.get("alertRuleId").toString())).getSources());
            } catch (RuntimeException e) {
                // A deleted rule contributes no sources.
            }
        }
        return out;
    }

    /**
     * The event families with their occurrence counts per country over {@code [from, to)}, non-private.
     *
     * @param from the start
     * @param to the end (exclusive)
     * @return the families per region
     */
    public List<CandidateGenerator.FamilyCount> familyCounts(Instant from, Instant to) {
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("from", Timestamp.from(from))
                .addValue("to", Timestamp.from(to));
        List<CandidateGenerator.FamilyCount> out = new ArrayList<>();
        read("SELECT e.type AS fam, substr(t.tag, 1, 2) AS cc, count(DISTINCT e.id) AS n, count(DISTINCT e.id) FILTER"
                + " (WHERE t.tag LIKE '__-%') AS a1 FROM intelligence_event e CROSS JOIN LATERAL"
                + " jsonb_array_elements_text(e.region_tags) AS t(tag) WHERE e.start_time >= :from AND e.start_time"
                + " < :to AND e.merged_into IS NULL AND e.private = false AND length(t.tag) >= 2 GROUP BY 1, 2", p,
                (rs, n) -> {
                    String type = rs.getString("fam");
                    Map<String, Object> selector = Map.of("intelEventType", type);
                    out.add(count(rs.getString("cc"), selector, rs.getLong("n"), rs.getLong("a1")));
                    return null;
                });
        Map<String, long[]> tagCounts = new LinkedHashMap<>();
        read("SELECT t.tag AS fam, a.region ->> 'iso2' AS cc, a.source_id, count(*) AS n, count(*) FILTER (WHERE"
                + " a.region ->> 'admin1' IS NOT NULL) AS a1 FROM alert a CROSS JOIN LATERAL"
                + " jsonb_array_elements_text(a.tags) AS t(tag) WHERE a.fired_at >= :from AND a.fired_at < :to AND"
                + " a.suppressed = false AND NOT a.exercise AND a.region ->> 'iso2' IS NOT NULL GROUP BY 1, 2, 3", p,
                (rs, n) -> {
                    if (!privateSource(rs.getString("source_id"))) {
                        long[] c = tagCounts.computeIfAbsent(rs.getString("fam") + "\u0000" + rs.getString("cc"),
                                k -> new long[2]);
                        c[0] += rs.getLong("n");
                        c[1] += rs.getLong("a1");
                    }
                    return null;
                });
        tagCounts.forEach((key, c) -> {
            String[] parts = key.split("\u0000", 2);
            out.add(count(parts[1], Map.of("alertRuleTag", parts[0]), c[0], c[1]));
        });
        Map<String, Boolean> privateRules = new HashMap<>();
        read("SELECT rule_id::text AS rule, classification, group_key, count(*) AS n FROM anomaly_observation WHERE"
                + " bucket_start >= :from AND bucket_start < :to AND classification IN ('activity-up', 'activity-down')"
                + " GROUP BY 1, 2, 3", p, (rs, n) -> {
                    String rule = rs.getString("rule");
                    String[] region = regionOfCode(rs.getString("group_key"));
                    if (region[0] != null && !privateRules.computeIfAbsent(rule,
                            r -> privateRule(UUID.fromString(r)))) {
                        Map<String, Object> selector = new LinkedHashMap<>();
                        selector.put("anomalyRuleId", rule);
                        selector.put("classification", rs.getString("classification"));
                        out.add(count(region[0], selector, rs.getLong("n"), region[1] != null ? rs.getLong("n") : 0));
                    }
                    return null;
                });
        Map<String, long[]> findingCounts = new LinkedHashMap<>();
        read("SELECT f.finding_type AS type, f.signal_family AS fam, substr(r.code, 1, 2) AS cc, count(DISTINCT f.id) AS"
                + " n, count(DISTINCT f.id) FILTER (WHERE r.code LIKE '__-%') AS a1 FROM analytic_finding f CROSS JOIN"
                + " LATERAL unnest(f.region_codes) AS r(code) WHERE f.finding_type IN ('change-point',"
                + " 'unexplained-move') AND f.status <> 'dismissed' AND f.private = false AND f.signal_family IS NOT"
                + " NULL AND coalesce(f.window_start, f.detected_at) >= :from AND coalesce(f.window_start,"
                + " f.detected_at) < :to AND r.code <> 'ZZ' GROUP BY 1, 2, 3", p, (rs, n) -> {
                    String family = families.canonical(rs.getString("fam")).orElse(null);
                    if (family != null) {
                        String form = "change-point".equals(rs.getString("type")) ? "changePoint" : "unexplainedMove";
                        long[] c = findingCounts.computeIfAbsent(form + "\u0000" + family + "\u0000"
                                + rs.getString("cc"), k -> new long[2]);
                        c[0] += rs.getLong("n");
                        c[1] += rs.getLong("a1");
                    }
                    return null;
                });
        findingCounts.forEach((key, c) -> {
            String[] parts = key.split("\u0000", 3);
            out.add(count(parts[2], Map.of(parts[0], Map.of("family", parts[1])), c[0], c[1]));
        });
        read("SELECT family, region_code AS cc, count(DISTINCT week_start) AS n FROM reference_history WHERE count > 0"
                + " AND length(region_code) = 2 AND region_code <> 'ZZ' AND week_start >= CAST(:from AS date) AND"
                + " week_start < CAST(:to AS date) GROUP BY 1, 2", p, (rs, n) -> {
                    out.add(count(rs.getString("cc"), Map.of("referenceFamily", rs.getString("family")),
                            rs.getLong("n"), 0));
                    return null;
                });
        read("SELECT s.theme, substr(r.code, 1, 2) AS cc, count(*) AS n, count(*) FILTER (WHERE r.code LIKE '__-%') AS"
                + " a1 FROM situation_log l JOIN situation s ON s.id = l.situation_id CROSS JOIN LATERAL"
                + " unnest(s.region_codes) AS r(code) WHERE l.event = 'situation.activated' AND s.private = false AND"
                + " l.at >= :from AND l.at < :to AND r.code <> 'ZZ' GROUP BY 1, 2", p, (rs, n) -> {
                    out.add(count(rs.getString("cc"), Map.of("situationActivated", Map.of("theme",
                            rs.getString("theme"))), rs.getLong("n"), rs.getLong("a1")));
                    return null;
                });
        return out;
    }

    private CandidateGenerator.FamilyCount count(String region, Map<String, Object> selector, long n, long admin1) {
        return new CandidateGenerator.FamilyCount(region, familyKey(selector), selector, signalFamily(selector),
                label(selector), n, n > 0 && admin1 >= ADMIN1_SHARE * n);
    }

    /**
     * The 119 signal family of a selector, or null: the family of an intel event type or alert tag, the family named
     * by a change-point or unexplained-move selector.
     */
    public String signalFamily(Map<String, Object> selector) {
        if (selector == null) {
            return null;
        }
        if (selector.get("intelEventType") != null) {
            return families.familyOf(SignalFamilyRegistry.Query.intelEvent(selector.get("intelEventType").toString()))
                    .orElse(null);
        }
        if (selector.get("alertRuleTag") != null) {
            return families.familyOf(SignalFamilyRegistry.Query.tag(selector.get("alertRuleTag").toString()))
                    .orElse(null);
        }
        for (String key : List.of("changePoint", "unexplainedMove")) {
            if (selector.get(key) instanceof Map<?, ?> m && m.get("family") != null) {
                return families.canonical(m.get("family").toString()).orElse(m.get("family").toString());
            }
        }
        if (selector.get("referenceFamily") != null) {
            String root = ai.foresignal.baserate.reference.ReferenceFamilies.root(selector.get("referenceFamily")
                    .toString());
            return families.canonical(root).orElse(root);
        }
        return null;
    }

    /** A stable key of a selector's family, without regions ({@code intelEventType:maritime-disruption}, …). */
    public static String familyKey(Map<String, Object> selector) {
        if (selector.get("intelEventType") != null) {
            return "intelEventType:" + selector.get("intelEventType");
        }
        if (selector.get("alertRuleId") != null) {
            return "alertRuleId:" + selector.get("alertRuleId");
        }
        if (selector.get("alertRuleTag") != null) {
            return "alertRuleTag:" + selector.get("alertRuleTag");
        }
        if (selector.get("anomalyRuleId") != null) {
            return "anomaly:" + selector.get("anomalyRuleId") + ":"
                    + (selector.get("classification") == null ? "*" : selector.get("classification"));
        }
        if (selector.get("situationActivated") != null) {
            Object theme = selector.get("situationActivated") instanceof Map<?, ?> m ? m.get("theme") : null;
            return "situationActivated:" + (theme == null ? "*" : theme);
        }
        for (String key : List.of("changePoint", "unexplainedMove")) {
            if (selector.get(key) instanceof Map<?, ?> m) {
                return key + ":" + m.get("family");
            }
        }
        if (selector.get("referenceFamily") != null) {
            return "referenceFamily:" + selector.get("referenceFamily");
        }
        if (selector.get("timestamps") != null) {
            return "timestamps";
        }
        return "unknown";
    }

    /** A display label of a selector ({@code "intel events of type maritime-disruption"}, …). */
    public static String label(Map<String, Object> selector) {
        if (selector.get("label") != null) {
            return selector.get("label").toString();
        }
        if (selector.get("intelEventType") != null) {
            return "intel events of type " + selector.get("intelEventType");
        }
        if (selector.get("alertRuleId") != null) {
            return "alerts of rule " + selector.get("alertRuleId");
        }
        if (selector.get("alertRuleTag") != null) {
            return "alerts tagged " + selector.get("alertRuleTag");
        }
        if (selector.get("anomalyRuleId") != null) {
            return (selector.get("classification") == null ? "anomalies" : selector.get("classification") + " anomalies")
                    + " of rule " + selector.get("anomalyRuleId");
        }
        if (selector.get("situationActivated") != null) {
            Object theme = selector.get("situationActivated") instanceof Map<?, ?> m ? m.get("theme") : null;
            return theme == null ? "situations turning active" : theme + " situations turning active";
        }
        if (selector.get("changePoint") instanceof Map<?, ?> m) {
            return "change points in " + m.get("family") + " series";
        }
        if (selector.get("unexplainedMove") instanceof Map<?, ?> m) {
            return "unexplained moves in " + m.get("family") + " series";
        }
        if (selector.get("referenceFamily") != null) {
            return "weeks with " + selector.get("referenceFamily") + " events (reference history)";
        }
        return "the given times";
    }

    // ------------------------------------------------------------------------- helpers

    private <T> List<T> read(String sql, MapSqlParameterSource p,
            org.springframework.jdbc.core.RowMapper<T> mapper) {
        return reads.read(BacktestCoverage.STATEMENT_TIMEOUT, t -> t.query(sql, p, mapper));
    }

    /** Keeps the first alert per 116 canonical event; alerts whose record has none are kept. */
    List<Occurrence> canonical(List<Occurrence> in) {
        EvidenceService service = evidence.getIfAvailable();
        if (service == null || in.isEmpty()) {
            return in;
        }
        List<RecordRef> refs = new ArrayList<>();
        for (Occurrence o : in) {
            if (o.sourceId() != null && o.externalId() != null) {
                refs.add(new RecordRef(o.sourceId(), o.externalId(), null));
            }
        }
        Map<String, EvidenceService.GroupKey> keys;
        try {
            keys = service.groupKeys(refs);
        } catch (RuntimeException e) {
            return in;
        }
        Set<String> seen = new HashSet<>();
        List<Occurrence> out = new ArrayList<>();
        for (Occurrence o : in) {
            EvidenceService.GroupKey key = o.sourceId() == null ? null
                    : keys.get(EvidenceService.recordKey(o.sourceId(), o.externalId()));
            String canonical = key == null ? null : key.canonicalEventId();
            if (canonical == null || seen.add(canonical)) {
                out.add(o);
            }
        }
        return out;
    }

    private List<Occurrence> withoutPrivateSources(List<Occurrence> in) {
        List<Occurrence> out = new ArrayList<>();
        for (Occurrence o : in) {
            if (!privateSource(o.sourceId())) {
                out.add(o);
            }
        }
        return out;
    }

    private boolean privateSource(String sourceId) {
        IntelVisibility v = visibility.getIfAvailable();
        return v != null && sourceId != null && v.isPrivateSource(sourceId);
    }

    private boolean privateRule(UUID rule) {
        try {
            for (String source : rules.load(rule).getSources()) {
                if (privateSource(source)) {
                    return true;
                }
            }
            return false;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** Keeps the occurrences in one of {@code regions} (when given) at or above {@code minSeverity} (when given). */
    static List<Occurrence> filter(List<Occurrence> in, Collection<String> regions, String minSeverity) {
        int min = minSeverity == null ? -1 : SEVERITY_ORDER.indexOf(minSeverity.toLowerCase(Locale.ROOT));
        List<Occurrence> out = new ArrayList<>();
        for (Occurrence o : in) {
            if (!regions.isEmpty() && !inRegions(o.regions(), regions)) {
                continue;
            }
            if (min >= 0 && (o.severity() == null || SEVERITY_ORDER.indexOf(o.severity()) < min)) {
                continue;
            }
            out.add(o);
        }
        return out;
    }

    /** True when any code is one of {@code regions}, or its country is. */
    static boolean inRegions(List<String> codes, Collection<String> regions) {
        for (String r : codes) {
            if (regions.contains(r) || (r.length() > 2 && regions.contains(r.substring(0, 2)))) {
                return true;
            }
        }
        return false;
    }

    private static Instant ts(ResultSet rs, String column) throws SQLException {
        return rs.getTimestamp(column).toInstant();
    }

    /** {@code [country, admin1]} of a JSON array of region tags. */
    String[] regionOfTags(String tagsJson) {
        List<String> codes = new ArrayList<>();
        if (tagsJson != null) {
            try {
                for (JsonNode tag : json.readTree(tagsJson)) {
                    if (tag.isString()) {
                        codes.add(tag.asString());
                    }
                }
            } catch (RuntimeException e) {
                // Unreadable tags leave the region unknown.
            }
        }
        return regionOfList(codes);
    }

    /** {@code [country, admin1]} of a comma list of region codes. */
    static String[] regionOfCsv(String csv) {
        return regionOfList(csv == null || csv.isBlank() ? List.of() : List.of(csv.split(",")));
    }

    /** {@code [country, admin1]} of one code (an anomaly group key that is a region code), else {@code [null, null]}. */
    static String[] regionOfCode(String code) {
        if (code == null || !REGION.matcher(code).matches() || "ZZ".equals(code)) {
            return new String[] {null, null};
        }
        return regionOfList(List.of(code));
    }

    /** {@code [country, admin1]}: the first country and the first admin1 code of a list. */
    static String[] regionOfList(List<String> codes) {
        String country = null;
        String admin1 = null;
        for (String code : codes) {
            if (code == null || "ZZ".equals(code) || code.length() < 2) {
                continue;
            }
            if (code.contains("-")) {
                admin1 = admin1 == null ? code : admin1;
                country = country == null ? code.substring(0, 2) : country;
            } else if (code.length() == 2) {
                country = country == null ? code : country;
            }
        }
        return new String[] {country, admin1};
    }

    private static Set<String> strings(Object value) {
        Set<String> out = new LinkedHashSet<>();
        if (value instanceof List<?> list) {
            for (Object o : list) {
                if (o != null) {
                    out.add(o.toString());
                }
            }
        }
        return out;
    }
}
