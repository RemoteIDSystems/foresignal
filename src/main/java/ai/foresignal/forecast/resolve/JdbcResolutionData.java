package ai.foresignal.forecast.resolve;

import ai.foresignal.analytics.eventstudy.EventSelectorResolver;
import ai.foresignal.analytics.series.SeriesData;
import ai.foresignal.analytics.series.SeriesReader;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.baserate.reference.ReferenceFamilies;
import ai.foresignal.forecast.JsonValues;
import ai.foresignal.observability.AbsenceCheck;
import ai.foresignal.observability.ObservabilityDeclarations;
import ai.foresignal.observability.ObservabilityService;
import ai.foresignal.observability.Place;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.trigger.window.CoverageGate;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * The production {@link ResolutionData} (prompt 122): occurrences through the 119 {@link EventSelectorResolver}
 * (narrowed by region codes, or by geometry through the occurrences' stored geometries), series through the
 * {@link SeriesReader} as of the resolution time, situation states from {@code situation_log}, coverage from the 100
 * {@link CoverageGate} over {@code fetch_log} history, and detectability from 116
 * {@link ObservabilityService#canSeeAbsence}.
 *
 * <p>Observability scope of a resolver: an alert rule's sources and kinds (family of its first kind); a
 * {@code referenceFamily} selector's root family; a change-point or unexplained-move selector's family. Other forms
 * (intel events, situations, anomalies, explicit times) have no observability family, so a negative answer for them
 * cannot be confirmed and is annulled ({@code LOW_DETECTABILITY}).
 *
 * <p>Release lags: a {@code series:} or {@code rollup:} reference's {@code releaseLagDays}; 7 days for
 * {@code referenceFamily} selectors (weekly reference history and the weekly own-history fold); none otherwise.
 */
@Component
public class JdbcResolutionData implements ResolutionData {

    /** The lag of weekly reference history. */
    public static final double REFERENCE_LAG_DAYS = 7;

    private final ObjectProvider<EventSelectorResolver> selectors;
    private final ObjectProvider<SeriesReader> reader;
    private final ObjectProvider<CoverageGate> coverage;
    private final ObjectProvider<ObservabilityService> observability;
    private final ObjectProvider<ObservabilityDeclarations> declarations;
    private final ObjectProvider<TriggerRuleRepository> rules;
    private final NamedParameterJdbcTemplate jdbc;
    private final JsonMapper json;

    /** Creates the data port. */
    public JdbcResolutionData(ObjectProvider<EventSelectorResolver> selectors, ObjectProvider<SeriesReader> reader,
            ObjectProvider<CoverageGate> coverage, ObjectProvider<ObservabilityService> observability,
            ObjectProvider<ObservabilityDeclarations> declarations, ObjectProvider<TriggerRuleRepository> rules,
            NamedParameterJdbcTemplate jdbc, JsonMapper json) {
        this.selectors = selectors;
        this.reader = reader;
        this.coverage = coverage;
        this.observability = observability;
        this.declarations = declarations;
        this.rules = rules;
        this.jdbc = jdbc;
        this.json = json;
    }

    @Override
    public List<Occurrence> occurrences(Map<String, Object> selector, Map<String, Object> area, Instant from,
            Instant to) {
        EventSelectorResolver resolver = selectors.getIfAvailable();
        if (resolver == null || selector == null) {
            return List.of();
        }
        Map<String, Object> narrowed = new LinkedHashMap<>(selector);
        List<String> areaRegions = area == null ? List.of() : JsonValues.strings(area, "regionCodes");
        if (!areaRegions.isEmpty()) {
            List<String> own = JsonValues.strings(selector, "regionCodes");
            List<String> regions = new ArrayList<>();
            if (own.isEmpty()) {
                regions.addAll(areaRegions);
            } else {
                for (String r : own) {
                    if (areaRegions.contains(r) || (r.length() > 2 && areaRegions.contains(r.substring(0, 2)))) {
                        regions.add(r);
                    }
                }
                for (String r : areaRegions) {
                    if (r.length() > 2 && own.contains(r.substring(0, 2)) && !regions.contains(r)) {
                        regions.add(r);
                    }
                }
                if (regions.isEmpty()) {
                    return List.of();
                }
            }
            narrowed.put("regionCodes", regions);
        }
        List<EventSelectorResolver.Occurrence> found = resolver.resolve(narrowed, from, to);
        List<Occurrence> out = new ArrayList<>();
        for (EventSelectorResolver.Occurrence o : found) {
            String ref = o.ref();
            if (ref == null) {
                ref = "occurrence:" + JsonValues.text(selector, "referenceFamily") + "/"
                        + String.join(",", o.regions()) + "@" + o.at();
            }
            out.add(new Occurrence(o.at(), ref));
        }
        if (area != null && areaRegions.isEmpty()) {
            Set<String> inside = inside(out, area);
            out.removeIf(o -> !inside.contains(o.ref()));
        }
        return out;
    }

    /** The refs among {@code found} whose stored geometry intersects the area. */
    Set<String> inside(List<Occurrence> found, Map<String, Object> area) {
        Map<String, List<String>> idsByType = new LinkedHashMap<>();
        for (Occurrence o : found) {
            int colon = o.ref().indexOf(':');
            if (colon > 0) {
                idsByType.computeIfAbsent(o.ref().substring(0, colon), k -> new ArrayList<>())
                        .add(o.ref().substring(colon + 1));
            }
        }
        MapSqlParameterSource p = new MapSqlParameterSource();
        String areaSql = areaSql(area, p);
        Set<String> out = new HashSet<>();
        idsByType.forEach((type, ids) -> {
            String sql = switch (type) {
                case "intel-event" -> "SELECT id FROM intelligence_event WHERE id IN (:ids) AND geom IS NOT NULL AND"
                        + " ST_Intersects(geom, " + areaSql + ")";
                case "situation" -> "SELECT id FROM situation WHERE id IN (:ids) AND geom IS NOT NULL AND"
                        + " ST_Intersects(geom, " + areaSql + ")";
                case "finding" -> "SELECT id FROM analytic_finding WHERE id IN (:ids) AND geom IS NOT NULL AND"
                        + " ST_Intersects(geom, " + areaSql + ")";
                case "alert" -> "SELECT CAST(a.id AS text) FROM alert a JOIN record_head h ON h.source_id = a.source_id"
                        + " AND h.external_id = a.external_id WHERE CAST(a.id AS text) IN (:ids) AND"
                        + " h.persisted_geom IS NOT NULL AND ST_Intersects(h.persisted_geom, " + areaSql + ")";
                default -> null;
            };
            if (sql != null) {
                MapSqlParameterSource q = new MapSqlParameterSource(p.getValues()).addValue("ids", ids);
                for (String id : jdbc.queryForList(sql, q, String.class)) {
                    out.add(type + ":" + id);
                }
            }
        });
        return out;
    }

    /** The SQL geometry of an area, binding its parameters. */
    private String areaSql(Map<String, Object> area, MapSqlParameterSource p) {
        if (area.get("bbox") instanceof List<?> bbox) {
            p.addValue("w", ((Number) bbox.get(0)).doubleValue()).addValue("s", ((Number) bbox.get(1)).doubleValue())
                    .addValue("e", ((Number) bbox.get(2)).doubleValue())
                    .addValue("n", ((Number) bbox.get(3)).doubleValue());
            return "ST_MakeEnvelope(:w, :s, :e, :n, 4326)";
        }
        if (area.get("polygon") != null) {
            p.addValue("g", json.writeValueAsString(area.get("polygon")));
            return "ST_SetSRID(ST_GeomFromGeoJSON(:g), 4326)";
        }
        p.addValue("place", String.valueOf(area.get("placeId")));
        return "(SELECT area FROM place WHERE id = :place)";
    }

    @Override
    public Aggregate seriesAggregate(String ref, Instant from, Instant to, String aggregate, Instant asOf) {
        SeriesReader series = reader.getIfAvailable();
        if (series == null) {
            return new Aggregate(null, 1.0, List.of());
        }
        SeriesData data;
        try {
            data = series.read(SeriesRef.parse(ref), from, to, asOf);
        } catch (ForesignalException e) {
            return new Aggregate(null, 1.0, List.of());
        }
        SeriesData.Grid grid = data.grid().knownBy(asOf);
        int buckets = 0;
        int gaps = 0;
        List<Double> values = new ArrayList<>();
        for (int i = 0; i < grid.size(); i++) {
            if (grid.starts()[i].isBefore(from) || !grid.starts()[i].isBefore(to)) {
                continue;
            }
            buckets++;
            if (grid.gap()[i] || Double.isNaN(grid.values()[i])) {
                gaps++;
            } else {
                values.add(grid.values()[i]);
            }
        }
        double gapShare = buckets == 0 ? 1.0 : gaps / (double) buckets;
        Double value = aggregate(values, aggregate);
        return new Aggregate(value, gapShare, List.of(ref));
    }

    /** The aggregate of values in time order, or null when empty. */
    public static Double aggregate(List<Double> values, String aggregate) {
        if (values.isEmpty()) {
            return null;
        }
        return switch (aggregate) {
            case "last" -> values.get(values.size() - 1);
            case "max" -> values.stream().mapToDouble(Double::doubleValue).max().orElse(Double.NaN);
            case "sum" -> values.stream().mapToDouble(Double::doubleValue).sum();
            default -> values.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
        };
    }

    @Override
    public SituationState situationAt(String situationId, Instant at) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT status, severity, created_at FROM situation WHERE"
                + " id = :id", new MapSqlParameterSource("id", situationId));
        if (rows.isEmpty()) {
            return null;
        }
        Object created = rows.get(0).get("created_at");
        if (created instanceof Timestamp ts && ts.toInstant().isAfter(at)) {
            return null;
        }
        List<String> after = jdbc.queryForList("SELECT CAST(after AS text) FROM situation_log WHERE situation_id = :id"
                + " AND at <= :at AND after ->> 'status' IS NOT NULL ORDER BY at DESC, id DESC LIMIT 1",
                new MapSqlParameterSource("id", situationId).addValue("at", Timestamp.from(at)), String.class);
        if (!after.isEmpty() && after.get(0) != null) {
            Map<String, Object> state = JsonValues.map(json.readTree(after.get(0)));
            if (state.get("status") != null) {
                Object severity = state.get("severity");
                return new SituationState(state.get("status").toString(), severity == null
                        ? String.valueOf(rows.get(0).get("severity")) : severity.toString());
            }
        }
        return new SituationState(String.valueOf(rows.get(0).get("status")), String.valueOf(rows.get(0)
                .get("severity")));
    }

    @Override
    public Scope scope(Map<String, Object> resolver) {
        ObservabilityDeclarations decl = declarations.getIfAvailable();
        String ruleId = resolver.get("ruleId") != null ? resolver.get("ruleId").toString() : null;
        Map<String, Object> selector = JsonValues.child(resolver, "selector");
        if (ruleId == null && selector != null && selector.get("alertRuleId") != null) {
            ruleId = selector.get("alertRuleId").toString();
        }
        if (ruleId != null) {
            TriggerRuleRepository repo = rules.getIfAvailable();
            TriggerRuleEntity rule = null;
            try {
                rule = repo == null ? null : repo.findById(UUID.fromString(ruleId)).orElse(null);
            } catch (IllegalArgumentException e) {
                rule = null;
            }
            if (rule == null) {
                return new Scope(null, List.of(), List.of());
            }
            List<String> kinds = rule.getKinds() == null ? List.of() : rule.getKinds();
            String family = decl == null || kinds.isEmpty() ? null : decl.familyOfKind(kinds.get(0));
            return new Scope(family, rule.getSources() == null ? List.of() : rule.getSources(), kinds);
        }
        String family = null;
        if (selector != null && selector.get("referenceFamily") != null) {
            family = ReferenceFamilies.root(selector.get("referenceFamily").toString());
        } else if (selector != null) {
            for (String key : List.of("changePoint", "unexplainedMove")) {
                Map<String, Object> m = JsonValues.child(selector, key);
                if (m != null && m.get("family") != null) {
                    family = m.get("family").toString();
                }
            }
        }
        if (family == null) {
            return new Scope(null, List.of(), List.of());
        }
        List<String> kinds = decl == null ? List.of() : new ArrayList<>(decl.kindsOf(family));
        return new Scope(family, List.of(), kinds);
    }

    @Override
    public double coverage(Scope scope, Instant from, Instant to) {
        CoverageGate gate = coverage.getIfAvailable();
        if (gate == null || !scope.judgeable()) {
            return Double.NaN;
        }
        return gate.coverageRatioAsOf(new LinkedHashSet<>(scope.sources()), new LinkedHashSet<>(scope.kinds()), from,
                to);
    }

    @Override
    public Absence canSeeAbsence(Scope scope, Map<String, Object> area, Instant from, Instant to) {
        ObservabilityService service = observability.getIfAvailable();
        if (scope.family() == null) {
            return new Absence(false, "the selector has no observability family, so an absence cannot be confirmed");
        }
        if (service == null) {
            return new Absence(false, "the observability model is unavailable");
        }
        for (Place place : places(area)) {
            AbsenceCheck check = scope.sources().isEmpty()
                    ? service.canSeeAbsence(scope.family(), place, from, to, null)
                    : service.canSeeAbsence(scope.sources(), scope.family(), place, from, to, null);
            if (!check.ok()) {
                return new Absence(false, check.reason() == null ? AbsenceCheck.LOW_DETECTABILITY : check.reason());
            }
        }
        return new Absence(true, null);
    }

    /** The places of an area: one per region code, the polygon, the box, the place's area, or everywhere. */
    List<Place> places(Map<String, Object> area) {
        if (area == null) {
            return List.of(Place.GLOBAL);
        }
        List<String> regions = JsonValues.strings(area, "regionCodes");
        if (!regions.isEmpty()) {
            return regions.stream().map(Place::region).toList();
        }
        if (area.get("bbox") instanceof List<?> b) {
            double w = ((Number) b.get(0)).doubleValue();
            double s = ((Number) b.get(1)).doubleValue();
            double e = ((Number) b.get(2)).doubleValue();
            double n = ((Number) b.get(3)).doubleValue();
            String polygon = "{\"type\":\"Polygon\",\"coordinates\":[[[" + w + "," + s + "],[" + e + "," + s + "],["
                    + e + "," + n + "],[" + w + "," + n + "],[" + w + "," + s + "]]]}";
            return List.of(Place.polygon(new GeoJsonReader().read(polygon)));
        }
        if (area.get("polygon") != null) {
            Geometry g = new GeoJsonReader().read(json.writeValueAsString(area.get("polygon")));
            return List.of(Place.polygon(g));
        }
        List<String> geo = jdbc.queryForList("SELECT ST_AsGeoJSON(area) FROM place WHERE id = :id",
                new MapSqlParameterSource("id", String.valueOf(area.get("placeId"))), String.class);
        if (geo.isEmpty() || geo.get(0) == null) {
            return List.of(Place.GLOBAL);
        }
        return List.of(Place.polygon(new GeoJsonReader().read(geo.get(0))));
    }

    @Override
    public double releaseLagDays(Map<String, Object> resolver) {
        Object ref = resolver.get("ref");
        if (ref != null) {
            SeriesReader series = reader.getIfAvailable();
            if (series == null) {
                return 0;
            }
            try {
                return series.meta(SeriesRef.parse(ref.toString())).releaseLagDays();
            } catch (RuntimeException e) {
                return 0;
            }
        }
        Map<String, Object> selector = JsonValues.child(resolver, "selector");
        if (selector != null && selector.get("referenceFamily") != null) {
            return REFERENCE_LAG_DAYS;
        }
        return 0;
    }
}
