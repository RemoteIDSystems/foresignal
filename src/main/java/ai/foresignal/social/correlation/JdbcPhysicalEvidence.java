package ai.foresignal.social.correlation;

import ai.foresignal.evidence.Originator;
import ai.foresignal.evidence.OriginatorRegistry;
import ai.foresignal.social.narrative.NarrativeVocabulary;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The {@link PhysicalEvidenceSource} over the 116 canonical events ({@code canonical_event} with their earliest
 * non-social {@code observation_link}), the 119 findings ({@code analytic_finding} of non-social, observed types) and
 * the negating records of {@code record_head}. A located claim matches evidence within the tolerance; evidence without
 * a geometry, and every evidence of a large-area family, matches by region.
 */
@Component
public class JdbcPhysicalEvidence implements PhysicalEvidenceSource {

    private static final Logger log = LoggerFactory.getLogger(JdbcPhysicalEvidence.class);

    /** Finding types that are not observations of the physical world (social, news, forecasts). */
    public static final Set<String> NON_PHYSICAL_FINDINGS = Set.of("social-velocity", "media-reuse",
            "social-coordination", "attention-gap", "coverage-blindspot", "coverage-divergence", "forecast-exceedance",
            "outlook-exceedance", "projected-impact", "precursor-active");

    /** The most rows read per kind. */
    static final int LIMIT = 200;

    private final JdbcTemplate jdbc;
    private final ObjectProvider<OriginatorRegistry> originators;

    /** Creates the source. */
    public JdbcPhysicalEvidence(JdbcTemplate jdbc, ObjectProvider<OriginatorRegistry> originators) {
        this.jdbc = jdbc;
        this.originators = originators;
    }

    @Override
    public List<Evidence> find(Query q) {
        List<Evidence> out = new ArrayList<>();
        try {
            out.addAll(canonical(q));
        } catch (RuntimeException e) {
            log.debug("Canonical evidence lookup failed: {}", e.toString());
        }
        try {
            out.addAll(findings(q));
        } catch (RuntimeException e) {
            log.debug("Finding evidence lookup failed: {}", e.toString());
        }
        try {
            out.addAll(negations(q));
        } catch (RuntimeException e) {
            log.debug("Negating record lookup failed: {}", e.toString());
        }
        out.removeIf(Evidence::social);
        return out;
    }

    /** The region codes a claim place matches: its own and, for a large-area family, its country. */
    static String[] regions(Query q) {
        if (q.regionCode() == null) {
            return new String[0];
        }
        if (q.rule().largeArea() && q.regionCode().length() > 2) {
            return new String[] {q.regionCode(), q.regionCode().substring(0, 2)};
        }
        return new String[] {q.regionCode()};
    }

    /** The spatial condition on {@code geomColumn} and {@code regionColumn}, with its arguments appended. */
    static String spatial(Query q, String geomColumn, String regionColumn, List<Object> args) {
        String[] regions = regions(q);
        boolean located = q.lon() != null && q.lat() != null && q.rule().toleranceKm() > 0;
        StringBuilder sql = new StringBuilder("(");
        if (located) {
            sql.append("(").append(geomColumn).append(" IS NOT NULL AND ST_DWithin(").append(geomColumn)
                    .append("::geography, ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography, ?))");
            args.add(q.lon());
            args.add(q.lat());
            args.add(q.rule().toleranceKm() * 1000.0);
        }
        if (regions.length > 0) {
            sql.append(located ? " OR (" : "(");
            if (located && !q.rule().largeArea()) {
                sql.append(geomColumn).append(" IS NULL AND ");
            }
            sql.append(regionColumn).append(" && ?)");
            args.add(regions);
        }
        if (!located && regions.length == 0) {
            sql.append("false");
        }
        return sql.append(")").toString();
    }

    /** The distance expression in km (null without a claim point). */
    static String distance(Query q, String geomColumn, List<Object> args) {
        if (q.lon() == null || q.lat() == null) {
            return "CAST(NULL AS double precision)";
        }
        args.add(q.lon());
        args.add(q.lat());
        return "ST_Distance(" + geomColumn + "::geography, ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography) / 1000.0";
    }

    List<Evidence> canonical(Query q) {
        if (q.rule().canonicalFamilies().isEmpty()) {
            return List.of();
        }
        List<Object> args = new ArrayList<>();
        String dist = distance(q, "c.geom", args);
        args.add(q.rule().canonicalFamilies().toArray(String[]::new));
        args.add(Timestamp.from(q.from()));
        args.add(Timestamp.from(q.to()));
        String where = spatial(q, "c.geom", "c.region_codes", args);
        args.add(LIMIT);
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT c.id, c.family, c.first_observed_at, " + dist
                + " AS km FROM canonical_event c WHERE c.family = ANY(?) AND c.family <> '" + NarrativeVocabulary.FAMILY
                + "' AND c.merged_into IS NULL AND c.event_time BETWEEN ? AND ? AND " + where
                + " ORDER BY c.first_observed_at, c.id LIMIT ?", args.toArray());
        List<Evidence> out = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String id = row.get("id").toString();
            Map<String, Object> origin = firstOrigin(id);
            if (origin == null) {
                continue;
            }
            out.add(new Evidence("canonical-event:" + id, String.valueOf(row.get("family")),
                    origin.get("originator").toString(), origin.get("class").toString(),
                    ((Timestamp) row.get("first_observed_at")).toInstant(), number(row.get("km")), false));
        }
        return out;
    }

    /** The earliest non-social originator of a canonical event, or null when every link is social. */
    Map<String, Object> firstOrigin(String canonicalEventId) {
        for (Map<String, Object> link : jdbc.queryForList("SELECT originator_id, source_id FROM observation_link WHERE"
                + " canonical_event_id = ? ORDER BY COALESCE(observed_at, linked_at), seq, id LIMIT 50",
                canonicalEventId)) {
            String id = String.valueOf(link.get("originator_id"));
            String cls = originatorClass(id, String.valueOf(link.get("source_id")));
            if (!"social".equals(cls)) {
                Map<String, Object> out = new LinkedHashMap<>();
                out.put("originator", id);
                out.put("class", cls);
                return out;
            }
        }
        return null;
    }

    String originatorClass(String originatorId, String sourceId) {
        if (originatorId != null && originatorId.startsWith(OriginatorRegistry.SOCIAL_PREFIX)) {
            return "social";
        }
        OriginatorRegistry registry = originators.getIfAvailable();
        if (registry == null) {
            return Originator.DEFAULT_CLASS;
        }
        return registry.originator(originatorId).map(Originator::originatorClass)
                .orElseGet(() -> registry.resolve(sourceId).originatorClass());
    }

    List<Evidence> findings(Query q) {
        if (q.rule().findingFamilies().isEmpty()) {
            return List.of();
        }
        List<Object> args = new ArrayList<>();
        String dist = distance(q, "f.geom", args);
        args.add(q.rule().findingFamilies().toArray(String[]::new));
        args.add(NON_PHYSICAL_FINDINGS.toArray(String[]::new));
        args.add(Timestamp.from(q.to()));
        args.add(Timestamp.from(q.from()));
        String where = spatial(q, "f.geom", "f.region_codes", args);
        args.add(LIMIT);
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT f.id, f.signal_family, f.detected_at, " + dist
                + " AS km FROM analytic_finding f WHERE f.signal_family = ANY(?) AND NOT (f.finding_type = ANY(?)) AND"
                + " f.status = 'open' AND COALESCE(f.window_start, f.detected_at) <= ? AND COALESCE(f.window_end,"
                + " f.detected_at) >= ? AND " + where + " ORDER BY f.detected_at, f.id LIMIT ?", args.toArray());
        List<Evidence> out = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            out.add(new Evidence("finding:" + row.get("id"), String.valueOf(row.get("signal_family")),
                    "foresignal-analytics", "derived", ((Timestamp) row.get("detected_at")).toInstant(),
                    number(row.get("km")), false));
        }
        return out;
    }

    List<Evidence> negations(Query q) {
        List<Evidence> out = new ArrayList<>();
        for (CompareRules.Negation n : q.rule().negations()) {
            if (n.values().isEmpty() || q.lon() == null || q.lat() == null) {
                continue;
            }
            List<Object> args = new ArrayList<>();
            String dist = distance(q, "h.persisted_geom", args);
            args.add(n.kind());
            args.add(n.field());
            args.add(n.values().toArray(String[]::new));
            args.add(Timestamp.from(q.from()));
            args.add(Timestamp.from(q.to()));
            args.add(q.lon());
            args.add(q.lat());
            args.add(q.rule().toleranceKm() * 1000.0);
            args.add(LIMIT);
            for (Map<String, Object> row : jdbc.queryForList("SELECT h.source_id, h.external_id,"
                    + " h.persisted_observed_at, " + dist + " AS km FROM record_head h WHERE h.kind = ? AND NOT"
                    + " h.tombstone AND h.payload ->> ? = ANY(?) AND COALESCE(h.event_time, h.persisted_observed_at)"
                    + " BETWEEN ? AND ? AND h.persisted_geom IS NOT NULL AND ST_DWithin(h.persisted_geom::geography,"
                    + " ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography, ?) LIMIT ?", args.toArray())) {
                String source = String.valueOf(row.get("source_id"));
                OriginatorRegistry registry = originators.getIfAvailable();
                Originator o = registry == null ? Originator.defaultFor(source) : registry.resolve(source);
                out.add(new Evidence("record:" + source + "/" + encode(String.valueOf(row.get("external_id"))),
                        q.rule().family(), o.id(), o.originatorClass(),
                        ((Timestamp) row.get("persisted_observed_at")).toInstant(), number(row.get("km")), true));
            }
        }
        return out;
    }

    private static Double number(Object v) {
        return v instanceof Number n ? n.doubleValue() : null;
    }

    private static String encode(String externalId) {
        return java.net.URLEncoder.encode(externalId, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
    }
}
