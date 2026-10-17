package ai.foresignal.outlook.index;

import ai.foresignal.analytics.finding.FindingEvent;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.analytics.series.SeriesData;
import ai.foresignal.analytics.series.SeriesFrequency;
import ai.foresignal.analytics.series.SeriesReader;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.baserate.reference.ReferenceCounts;
import ai.foresignal.manifest.ManifestRequest;
import ai.foresignal.manifest.ManifestService;
import ai.foresignal.outlook.series.QuantileSet;
import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.reference.CountryRef;
import ai.foresignal.security.Ulids;
import ai.foresignal.stats.RegionRollupComputer;
import ai.foresignal.stats.RegionRollupResolution;
import ai.foresignal.store.query.SeqClockQuery;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Regional indices (prompt 126, {@code regional-index} 1.0.0, Intelligence-Design §9.3): definitions (the built-ins
 * of {@code intel/regional-indices.json}, seeded disabled, and operator-defined ones), the daily computation, weights
 * changes and the reads.
 *
 * <ul>
 *   <li><b>Regions</b>: the 040 regions that have region-day rollup rows of a component's kind in the last 90 days.
 *   <li><b>Computation</b> ({@link #computeDays}): each component's window aggregate (sum for counts, mean for levels)
 *       per region and day, normalized against the pooled weekly values of the region's class (its continent) over
 *       the preceding year, combined by {@link IndexComputer}; stored in {@code regional_index_value} with deltas,
 *       the weights version, the seq and a manifest. A move of 5 points or more emits {@code index.changed}; the
 *       {@code foresignal-outlook} record follows any change of 1 point or more.
 *   <li><b>Weights</b> ({@link #applyWeights}): fitted (the version a fit proposed), operator or default weights,
 *       each change logged on the definition and bumping {@code weights_version}.
 * </ul>
 */
@Service
public class RegionalIndexService {

    private static final Logger log = LoggerFactory.getLogger(RegionalIndexService.class);

    /** The component name. */
    public static final String NAME = "regional-index";

    /** The component version. */
    public static final String VERSION = "1.0.0";

    /** The bundled built-in definitions. */
    public static final String RESOURCE = "intel/regional-indices.json";

    /** The points a move must reach to emit {@code index.changed}. */
    public static final double CHANGE_EVENT_POINTS = 5;

    /** Days of class history a normalization pools. */
    public static final int POOL_DAYS = 365;

    /** Weeks of Mondays a first computation backfills (the history a fit needs). */
    public static final int BACKFILL_WEEKS = 104;

    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};

    private final RegionalIndexDefRepository defs;
    private final RegionalIndexValueRepository values;
    private final RegionalIndexFitRepository fits;
    private final SeriesReader reader;
    private final ReferenceCounts counts;
    private final FindingService findings;
    private final ObjectProvider<ManifestService> manifests;
    private final ObjectProvider<CountryCentroids> countries;
    private final SeqClockQuery seqClock;
    private final NamedParameterJdbcTemplate jdbc;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public RegionalIndexService(RegionalIndexDefRepository defs, RegionalIndexValueRepository values,
            RegionalIndexFitRepository fits, SeriesReader reader, ReferenceCounts counts, FindingService findings,
            ObjectProvider<ManifestService> manifests, ObjectProvider<CountryCentroids> countries,
            SeqClockQuery seqClock, NamedParameterJdbcTemplate jdbc, JsonMapper json, Clock clock) {
        this.defs = defs;
        this.values = values;
        this.fits = fits;
        this.reader = reader;
        this.counts = counts;
        this.findings = findings;
        this.manifests = manifests;
        this.countries = countries;
        this.seqClock = seqClock;
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
    }

    // ------------------------------------------------------------------------- definitions

    /** Seeds the built-in definitions that are missing (disabled); existing rows are never changed. */
    @EventListener(ApplicationReadyEvent.class)
    public void ensureBuiltIns() {
        try (InputStream in = new ClassPathResource(RESOURCE).getInputStream()) {
            JsonNode root = json.readTree(in);
            for (JsonNode node : root.get("indices")) {
                String id = node.get("id").asString();
                if (defs.existsById(id)) {
                    continue;
                }
                Map<String, Object> body = json.convertValue(node, MAP);
                body.remove("id");
                body.put("enabled", false);
                IndexInputs.Definition d = IndexInputs.definition(json.valueToTree(body));
                RegionalIndexDefEntity def = new RegionalIndexDefEntity(id, clock.instant());
                apply(def, d);
                def.setBuiltIn(true);
                def.setEnabled(false);
                def.setDefaultWeights(d.weights());
                def.logWeights(logEntry("default", 1, d.weights(), "built-in defaults", "system", clock.instant()));
                try {
                    defs.saveAndFlush(def);
                } catch (DataIntegrityViolationException e) {
                    // Another node seeded it first.
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + RESOURCE, e);
        }
    }

    private static void apply(RegionalIndexDefEntity def, IndexInputs.Definition d) {
        def.setName(d.name());
        def.setDescription(d.description());
        def.setComponents(d.components());
        def.setWeights(d.weights());
        def.setFloors(d.floors());
        def.setTarget(d.target());
        def.setEnabled(d.enabled());
        def.setAutoApplyFittedWeights(d.autoApplyFittedWeights());
    }

    /**
     * The result of a PUT.
     *
     * @param def the definition
     * @param created true when it was created
     */
    public record Put(RegionalIndexDefEntity def, boolean created) {}

    /**
     * Creates or replaces a definition. Replacing keeps the weights origin when the components and weights are
     * unchanged; new weights make the origin {@code operator} and bump the version.
     */
    @Transactional
    public Put put(String id, JsonNode body, String actor) {
        IndexInputs.id(id);
        IndexInputs.Definition d = IndexInputs.definition(body);
        Instant now = clock.instant();
        RegionalIndexDefEntity def = defs.findById(id).orElse(null);
        boolean created = def == null;
        if (created) {
            def = new RegionalIndexDefEntity(id, now);
            apply(def, d);
            def.setDefaultWeights(d.weights());
            def.setWeightsOrigin("operator");
            def.logWeights(logEntry("operator", 1, d.weights(), "created", actor, now));
        } else {
            boolean weightsChanged = !sameWeights(def.getWeights(), d.weights())
                    || !def.getComponents().equals(d.components());
            apply(def, d);
            if (weightsChanged) {
                def.setWeightsVersion(def.getWeightsVersion() + 1);
                def.setWeightsOrigin("operator");
                def.logWeights(logEntry("operator", def.getWeightsVersion(), d.weights(), "definition replaced",
                        actor, now));
            }
        }
        def.setUpdatedAt(now);
        log.info("Regional index {} {} by {}", id, created ? "created" : "replaced", actor);
        return new Put(defs.save(def), created);
    }

    private static boolean sameWeights(Map<String, Double> a, Map<String, Double> b) {
        if (!a.keySet().equals(b.keySet())) {
            return false;
        }
        for (String k : a.keySet()) {
            if (Math.abs(a.get(k) - b.get(k)) > 1e-9) {
                return false;
            }
        }
        return true;
    }

    /** A {@code weights_log} entry. */
    static Map<String, Object> logEntry(String origin, int version, Map<String, Double> weights, String rationale,
            String actor, Instant at) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("at", at.toString());
        out.put("origin", origin);
        out.put("version", version);
        out.put("weights", weights);
        out.put("rationale", rationale);
        out.put("by", actor);
        return out;
    }

    /** The definition, or 404 {@code UNKNOWN_INDEX}. */
    public RegionalIndexDefEntity get(String id) {
        return defs.findById(id).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_INDEX,
                "No regional index '" + id + "'."));
    }

    /**
     * A page of definitions.
     *
     * @param items the definitions
     * @param nextCursor the next cursor, or null
     */
    public record Page(List<RegionalIndexDefEntity> items, String nextCursor) {}

    /** The definitions by id. */
    public Page list(String cursor, int limit) {
        String after = "";
        if (cursor != null && !cursor.isBlank()) {
            try {
                after = new String(Base64.getUrlDecoder().decode(cursor), java.nio.charset.StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                throw ValidationException.forField("cursor", "is not a cursor of this list");
            }
        }
        List<RegionalIndexDefEntity> page = defs.findByIdGreaterThanOrderByIdAsc(after, PageRequest.of(0, limit + 1));
        String next = null;
        if (page.size() > limit) {
            page = new ArrayList<>(page.subList(0, limit));
            next = Base64.getUrlEncoder().withoutPadding().encodeToString(page.get(page.size() - 1).getId()
                    .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        return new Page(page, next);
    }

    /**
     * Applies weights (logged): the version a fit proposed, operator weights or the defaults.
     *
     * @throws ValidationException 400 {@code INDEX_INVALID} for an unknown fitted version
     */
    @Transactional
    public RegionalIndexDefEntity applyWeights(String id, JsonNode body, String actor) {
        RegionalIndexDefEntity def = get(id);
        Set<String> keys = new java.util.LinkedHashSet<>();
        for (Map<String, Object> c : def.getComponents()) {
            keys.add(c.get("key").toString());
        }
        IndexInputs.Weights w = IndexInputs.weights(body, keys);
        return applyWeights(def, w, actor);
    }

    /** Applies a validated weights change. */
    @Transactional
    public RegionalIndexDefEntity applyWeights(RegionalIndexDefEntity def, IndexInputs.Weights w, String actor) {
        Map<String, Double> weights;
        RegionalIndexFitEntity fit = null;
        switch (w.source()) {
            case "fitted" -> {
                fit = fits.findTop1ByIndexIdAndStatusOrderByCreatedAtDescIdDesc(def.getId(), "done").orElse(null);
                if (fit == null || fit.getProposedWeights() == null || !w.version().equals(fit.getWeightsVersion())) {
                    throw IndexInputs.invalid(List.of(new FieldError("version", "is not the weights version of the"
                            + " latest finished fit" + (fit == null || fit.getWeightsVersion() == null ? ""
                                    : " (" + fit.getWeightsVersion() + ")"))));
                }
                weights = fit.getProposedWeights();
            }
            case "operator" -> weights = w.weights();
            default -> weights = def.getDefaultWeights().isEmpty() ? def.getWeights() : def.getDefaultWeights();
        }
        Instant now = clock.instant();
        def.setWeights(weights);
        def.setWeightsVersion(w.version() != null && "fitted".equals(w.source())
                ? Math.max(w.version(), def.getWeightsVersion() + 1) : def.getWeightsVersion() + 1);
        def.setWeightsOrigin(w.source());
        def.logWeights(logEntry(w.source(), def.getWeightsVersion(), weights, w.rationale(), actor, now));
        def.setUpdatedAt(now);
        if (fit != null) {
            fit.setApplied(true);
            fit.setAppliedAt(now);
            fits.save(fit);
        }
        log.info("Regional index {} weights set to {} ({} v{}) by {}", def.getId(), weights, w.source(),
                def.getWeightsVersion(), actor);
        return defs.save(def);
    }

    // ------------------------------------------------------------------------- readiness and regions

    /** True when every component series exists for at least one region. */
    public boolean ready(RegionalIndexDefEntity def) {
        Set<String> regions = regions(def);
        for (IndexComputer.Component c : IndexInputs.toComponents(def.getComponents())) {
            boolean exists = false;
            for (String region : regions) {
                try {
                    if (reader.exists(SeriesRef.parse(c.refFor(region)))) {
                        exists = true;
                        break;
                    }
                } catch (RuntimeException e) {
                    // not this region
                }
            }
            if (!exists) {
                return false;
            }
        }
        return !regions.isEmpty();
    }

    /** The regions an index is computed for: those with region-day rollups of a component's kind in 90 days. */
    public Set<String> regions(RegionalIndexDefEntity def) {
        Set<String> kinds = new TreeSet<>();
        for (IndexComputer.Component c : IndexInputs.toComponents(def.getComponents())) {
            try {
                SeriesRef ref = SeriesRef.parse(c.refFor("XX"));
                if (ref.type() == SeriesRef.Type.ROLLUP) {
                    kinds.add(ref.kind());
                }
            } catch (RuntimeException e) {
                // a template that does not parse is refused at PUT
            }
        }
        if (kinds.isEmpty()) {
            return Set.of();
        }
        List<String> rows = jdbc.queryForList("SELECT DISTINCT region_code FROM activity_rollup WHERE resolution ="
                + " :resolution AND source_id = :all AND kind IN (:kinds) AND region_code IS NOT NULL AND region_code"
                + " <> 'ZZ' AND bucket_start >= :since", new MapSqlParameterSource()
                        .addValue("resolution", RegionRollupResolution.DAY.code())
                        .addValue("all", RegionRollupComputer.ALL_SOURCES).addValue("kinds", List.copyOf(kinds))
                        .addValue("since", Timestamp.from(clock.instant().minus(Duration.ofDays(90)))), String.class);
        return new TreeSet<>(rows);
    }

    /** The class of a region: {@code continent:XX} of its country, else {@code unknown}. */
    String regionClass(String region) {
        CountryCentroids table = countries.getIfAvailable();
        if (table == null || region.length() < 2) {
            return "unknown";
        }
        return table.byIso2(region.substring(0, 2)).map(CountryRef::continent).map(c -> "continent:" + c)
                .orElse("unknown");
    }

    // ------------------------------------------------------------------------- computation

    /**
     * What a computation did.
     *
     * @param days days computed
     * @param values values written
     * @param events {@code index.changed} events emitted
     */
    public record ComputeResult(int days, int values, int events) {}

    /** Computes every enabled, ready index for the last complete day (backfilling a first computation). */
    public Map<String, ComputeResult> computeDaily(Instant now) {
        Map<String, ComputeResult> out = new LinkedHashMap<>();
        LocalDate yesterday = now.atZone(ZoneOffset.UTC).toLocalDate().minusDays(1);
        for (RegionalIndexDefEntity def : defs.findByEnabledTrueOrderByIdAsc()) {
            try {
                if (!ready(def)) {
                    continue;
                }
                List<LocalDate> days = new ArrayList<>();
                if (values.findTop1ByDefIdOrderByDayDesc(def.getId()).isEmpty()) {
                    days.addAll(backfillDays(yesterday));
                } else {
                    days.add(yesterday);
                }
                out.put(def.getId(), computeDays(def, days, now, true));
            } catch (RuntimeException e) {
                log.warn("Regional index {} failed: {}", def.getId(), e.toString());
            }
        }
        return out;
    }

    /** The Mondays of the last {@value #BACKFILL_WEEKS} weeks and the last 8 days before {@code last}. */
    static List<LocalDate> backfillDays(LocalDate last) {
        TreeSet<LocalDate> days = new TreeSet<>();
        LocalDate monday = last.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        for (int w = 0; w < BACKFILL_WEEKS; w++) {
            days.add(monday.minusWeeks(w));
        }
        for (int d = 0; d < 8; d++) {
            days.add(last.minusDays(d));
        }
        return new ArrayList<>(days);
    }

    /**
     * Computes an index for some days (ascending) as known at {@code now}.
     *
     * @param announce true to emit {@code index.changed} for a move of 5 points or more on the newest day
     */
    public ComputeResult computeDays(RegionalIndexDefEntity def, List<LocalDate> days, Instant now,
            boolean announce) {
        if (days.isEmpty()) {
            return new ComputeResult(0, 0, 0);
        }
        List<LocalDate> sorted = new ArrayList<>(new TreeSet<>(days));
        List<IndexComputer.Component> components = IndexInputs.toComponents(def.getComponents());
        List<IndexComputer.Floor> floors = IndexInputs.toFloors(def.getFloors());
        Set<String> regions = regions(def);
        LocalDate first = sorted.get(0).minusDays(POOL_DAYS + 90);
        LocalDate last = sorted.get(sorted.size() - 1);
        int span = (int) ChronoUnit.DAYS.between(first, last) + 1;
        Map<String, Map<String, double[]>> windowValues = new TreeMap<>();
        Map<String, String> classes = new HashMap<>();
        for (String region : regions) {
            Map<String, double[]> byComponent = new LinkedHashMap<>();
            for (IndexComputer.Component c : components) {
                byComponent.put(c.key(), windowSeries(c, region, first, span, now));
            }
            windowValues.put(region, byComponent);
            classes.put(region, regionClass(region));
        }
        long asOfSeq = seqClock.seqAt(jdbc, now);
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("components", def.getComponents());
        config.put("weights", def.getWeights());
        config.put("weightsVersion", def.getWeightsVersion());
        config.put("floors", def.getFloors());
        config.put("poolDays", POOL_DAYS);
        String manifestId = manifest(config, asOfSeq, def, sorted);
        Map<String, Double> computed = new HashMap<>();
        int written = 0;
        int events = 0;
        for (LocalDate day : sorted) {
            int d = (int) ChronoUnit.DAYS.between(first, day);
            Map<String, Map<String, double[]>> pools = pools(components, windowValues, classes, d);
            for (String region : regions) {
                Map<String, Double> raw = new LinkedHashMap<>();
                for (IndexComputer.Component c : components) {
                    double v = windowValues.get(region).get(c.key())[d];
                    raw.put(c.key(), Double.isFinite(v) ? v : null);
                }
                Map<String, double[]> pool = pools.getOrDefault(classes.get(region), Map.of());
                IndexComputer.Result r = IndexComputer.compute(components, def.getWeights(), raw, pool, floors,
                        condition -> referenceHolds(condition, region, day));
                if (Double.isNaN(r.value())) {
                    continue;
                }
                double value = QuantileSet.round(r.value());
                Double prev = previous(def.getId(), region, day.minusDays(1), computed);
                Double prev7 = previous(def.getId(), region, day.minusDays(7), computed);
                RegionalIndexValueEntity row = values.findByDefIdAndRegionAndDay(def.getId(), region, day)
                        .orElseGet(() -> new RegionalIndexValueEntity(Ulids.next(now), def.getId(), region, day,
                                value, now));
                row.setValue(value);
                row.setComponents(r.partsView());
                row.setFloorsApplied(r.floorsApplied());
                row.setDelta24h(IndexComputer.delta(value, prev));
                row.setDelta7d(IndexComputer.delta(value, prev7));
                row.setWeightsVersion(def.getWeightsVersion());
                row.setAsofSeq(asOfSeq);
                row.setManifestId(manifestId);
                row.setComputedAt(now);
                values.save(row);
                computed.put(region + "|" + day, value);
                written++;
                if (announce && day.equals(last) && prev != null && Math.abs(value - prev) >= CHANGE_EVENT_POINTS) {
                    try {
                        findings.announce(row.getId(), FindingEvent.INDEX_CHANGED, NAME, null);
                        events++;
                    } catch (RuntimeException e) {
                        log.warn("index.changed for {} failed: {}", row.getId(), e.toString());
                    }
                }
            }
        }
        return new ComputeResult(sorted.size(), written, events);
    }

    private Double previous(String defId, String region, LocalDate day, Map<String, Double> computed) {
        Double local = computed.get(region + "|" + day);
        if (local != null) {
            return local;
        }
        return values.findByDefIdAndRegionAndDay(defId, region, day).map(RegionalIndexValueEntity::getValue)
                .orElse(null);
    }

    /**
     * The pools of day {@code d}: per class and component, the window values of every class region on the same
     * weekday over the preceding {@value #POOL_DAYS} days.
     */
    static Map<String, Map<String, double[]>> pools(List<IndexComputer.Component> components,
            Map<String, Map<String, double[]>> windowValues, Map<String, String> classes, int d) {
        Map<String, Map<String, List<Double>>> lists = new HashMap<>();
        for (Map.Entry<String, Map<String, double[]>> e : windowValues.entrySet()) {
            String cls = classes.get(e.getKey());
            for (IndexComputer.Component c : components) {
                double[] series = e.getValue().get(c.key());
                List<Double> list = lists.computeIfAbsent(cls, k -> new HashMap<>()).computeIfAbsent(c.key(),
                        k -> new ArrayList<>());
                for (int k = 0; k * 7 <= POOL_DAYS; k++) {
                    int i = d - 7 * k;
                    if (i >= 0 && i < series.length && Double.isFinite(series[i])) {
                        list.add(series[i]);
                    }
                }
            }
        }
        Map<String, Map<String, double[]>> out = new HashMap<>();
        lists.forEach((cls, byComponent) -> {
            Map<String, double[]> m = new HashMap<>();
            byComponent.forEach((key, list) -> m.put(key, list.stream().mapToDouble(Double::doubleValue).toArray()));
            out.put(cls, m);
        });
        return out;
    }

    /**
     * A component's window aggregate per day over {@code span} days from {@code first}: the sum over the window for
     * counts (NaN when under 80% of its days are covered), the mean of covered values otherwise.
     */
    double[] windowSeries(IndexComputer.Component c, String region, LocalDate first, int span, Instant now) {
        double[] out = new double[span];
        Arrays.fill(out, Double.NaN);
        SeriesRef ref;
        try {
            ref = SeriesRef.parse(c.refFor(region));
            if (!reader.exists(ref)) {
                return out;
            }
        } catch (RuntimeException e) {
            return out;
        }
        LocalDate readFrom = first.minusDays(c.windowDays());
        Instant from = readFrom.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to = first.plusDays(span).atStartOfDay(ZoneOffset.UTC).toInstant();
        SeriesData data;
        try {
            data = reader.read(ref, from, to, now);
        } catch (RuntimeException e) {
            log.debug("Index component {} for {} unreadable: {}", c.key(), region, e.toString());
            return out;
        }
        if (data.frequency() == SeriesFrequency.HOUR) {
            data = data.resample(SeriesFrequency.DAY);
        }
        SeriesData.Grid grid = data.grid().knownBy(now);
        int total = span + c.windowDays();
        double[] daily = new double[total];
        Arrays.fill(daily, Double.NaN);
        for (int i = 0; i < grid.size(); i++) {
            if (grid.gap()[i]) {
                continue;
            }
            LocalDate day = grid.starts()[i].atZone(ZoneOffset.UTC).toLocalDate();
            int idx = (int) ChronoUnit.DAYS.between(readFrom, day);
            if (idx >= 0 && idx < total) {
                daily[idx] = grid.values()[i];
            }
        }
        boolean sums = data.counts();
        for (int d = 0; d < span; d++) {
            int end = d + c.windowDays();
            double sum = 0;
            int covered = 0;
            for (int i = end - c.windowDays() + 1; i <= end; i++) {
                if (i >= 0 && i < total && Double.isFinite(daily[i])) {
                    sum += daily[i];
                    covered++;
                }
            }
            if (sums) {
                out[d] = data.frequency() == SeriesFrequency.DAY && covered < 0.8 * c.windowDays() ? Double.NaN
                        : covered == 0 ? Double.NaN : sum;
            } else {
                out[d] = covered == 0 ? Double.NaN : sum / covered;
            }
        }
        return out;
    }

    /** Whether a {@code reference} floor condition holds for a region on a day. */
    boolean referenceHolds(Map<String, Object> condition, String region, LocalDate day) {
        String family = String.valueOf(condition.get("family"));
        int within = condition.get("withinDays") instanceof Number n ? n.intValue() : 365;
        LocalDate from = day.minusDays(within);
        LocalDate to = day.plusDays(1);
        try {
            if (condition.get("minFatalities") instanceof Number min) {
                Object source = condition.get("source");
                return counts.fatalities(family, region, from, to, source == null ? null : source.toString())
                        >= min.doubleValue();
            }
            if (condition.get("minEvents") instanceof Number min) {
                return counts.events(family, region, from, to) >= min.doubleValue();
            }
        } catch (RuntimeException e) {
            log.debug("Floor condition on {} unreadable: {}", region, e.toString());
        }
        return false;
    }

    // ------------------------------------------------------------------------- reads

    /** The values of an index: one region over a range, or the latest value per region. */
    public List<RegionalIndexValueEntity> values(String id, String region, LocalDate from, LocalDate to) {
        if (from != null && to != null && to.isBefore(from)) {
            throw ValidationException.forField("to", "must not be before from");
        }
        if (region != null) {
            LocalDate end = to != null ? to : clock.instant().atZone(ZoneOffset.UTC).toLocalDate();
            LocalDate start = from != null ? from : end.minusDays(90);
            if (ChronoUnit.DAYS.between(start, end) > 3_660) {
                throw ValidationException.forField("from", "the range holds more than ten years");
            }
            return values.findByDefIdAndRegionAndDayBetweenOrderByDayAsc(id, region, start, end);
        }
        RegionalIndexValueEntity newest = values.findTop1ByDefIdOrderByDayDesc(id).orElse(null);
        if (newest == null) {
            return List.of();
        }
        LocalDate day = to != null && to.isBefore(newest.getDay()) ? to : newest.getDay();
        return values.findByDefIdAndDayOrderByRegionAsc(id, day);
    }

    /** The newest fit of an index, or null. */
    public RegionalIndexFitEntity latestFit(String id) {
        return fits.findTop1ByIndexIdOrderByCreatedAtDescIdDesc(id).orElse(null);
    }

    /** 409 {@code FORECAST_MODEL_NOT_READY} when an index's component series do not exist yet. */
    public void requireReady(RegionalIndexDefEntity def) {
        if (!ready(def)) {
            throw new ConflictException(ErrorCodes.FORECAST_MODEL_NOT_READY, "The component series of regional index '"
                    + def.getId() + "' do not exist yet.");
        }
    }

    private String manifest(Map<String, Object> config, long asOfSeq, RegionalIndexDefEntity def,
            List<LocalDate> days) {
        ManifestService service = manifests.getIfAvailable();
        if (service == null) {
            return null;
        }
        try {
            List<String> inputs = new ArrayList<>();
            for (Map<String, Object> c : def.getComponents()) {
                inputs.add(String.valueOf(c.get("ref")));
            }
            return service.record(ManifestRequest.of(NAME, VERSION).config(config).asOf(asOfSeq).inputs(inputs)
                    .query(Map.of("indexId", def.getId(), "from", days.get(0).toString(), "to",
                            days.get(days.size() - 1).toString())));
        } catch (RuntimeException e) {
            log.warn("Could not record the manifest of index {}: {}", def.getId(), e.toString());
            return null;
        }
    }
}
