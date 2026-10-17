package ai.foresignal.outlook.index;

import ai.foresignal.analytics.finding.FindingEvent;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.backtest.BacktestJobEntity;
import ai.foresignal.backtest.BacktestJobService;
import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.backtest.JobRunner;
import ai.foresignal.baserate.reference.ReferenceCounts;
import ai.foresignal.manifest.ManifestRequest;
import ai.foresignal.manifest.ManifestService;
import ai.foresignal.outlook.regional.OutlookFamilies;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Fits regional index weights to outcomes (prompt 126, Intelligence-Design §9.3), as 103 jobs of type
 * {@value #TYPE}: monthly for every enabled, ready index, or on request ({@code POST /v1/indices/{id}/fit}).
 *
 * <p>Data: per region and Monday of the last {@value RegionalIndexService#BACKFILL_WEEKS} weeks, the stored normalized
 * components (backfilled first when missing) and the target count of the next four weeks: the outlook family's
 * reference counts ({@link ReferenceCounts}, one source per region), or for a {@code finding:{type}:{family}} target
 * the open or superseded findings of that type and signal family in the region. {@link IndexFitter} chooses the L2
 * penalty by rolling origin, reports the out-of-sample skill against the seasonal-naive outlook and the per-component
 * significance, and proposes weights; they are stored on the fit with the next weights version and announced
 * ({@code index.weights_proposed}), and applied only when the index sets {@code autoApplyFittedWeights}.
 */
@Service
public class IndexFitService implements JobRunner {

    private static final Logger log = LoggerFactory.getLogger(IndexFitService.class);

    /** The 103 job type. */
    public static final String TYPE = "index-fit";

    /** The method label of {@code IndexFitResult.method}: the spec's published default, a wire value kept as it is. */
    public static final String METHOD = "L2-regularised NB regression, rolling origin";

    private final RegionalIndexService indices;
    private final RegionalIndexDefRepository defs;
    private final RegionalIndexValueRepository values;
    private final RegionalIndexFitRepository fits;
    private final ReferenceCounts counts;
    private final OutlookFamilies families;
    private final FindingService findings;
    private final ObjectProvider<BacktestJobService> jobs;
    private final ObjectProvider<ManifestService> manifests;
    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;

    /** Creates the service. */
    public IndexFitService(RegionalIndexService indices, RegionalIndexDefRepository defs,
            RegionalIndexValueRepository values, RegionalIndexFitRepository fits, ReferenceCounts counts,
            OutlookFamilies families, FindingService findings, ObjectProvider<BacktestJobService> jobs,
            ObjectProvider<ManifestService> manifests, NamedParameterJdbcTemplate jdbc, Clock clock) {
        this.indices = indices;
        this.defs = defs;
        this.values = values;
        this.fits = fits;
        this.counts = counts;
        this.families = families;
        this.findings = findings;
        this.jobs = jobs;
        this.manifests = manifests;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Override
    public String type() {
        return TYPE;
    }

    /**
     * Submits a fit as a 103 job.
     *
     * @throws ai.foresignal.api.error.NotFoundException 404 {@code UNKNOWN_INDEX}
     * @throws ai.foresignal.api.error.ConflictException 409 {@code FORECAST_MODEL_NOT_READY} when the component series
     *     do not exist yet
     */
    public RegionalIndexFitEntity submit(String indexId, String tokenId) {
        RegionalIndexDefEntity def = indices.get(indexId);
        indices.requireReady(def);
        Map<String, Object> definition = new LinkedHashMap<>();
        definition.put("indexId", indexId);
        BacktestJobEntity job = jobs.getObject().submitRunnerJob(TYPE, indexId, definition, tokenId);
        RegionalIndexFitEntity fit = new RegionalIndexFitEntity(job.getId(), indexId, clock.instant());
        fit.setRequestedBy(tokenId == null ? "system" : tokenId);
        return fits.save(fit);
    }

    /** Submits the monthly fit of every enabled, ready index; returns the fits submitted. */
    public List<RegionalIndexFitEntity> submitMonthly() {
        List<RegionalIndexFitEntity> out = new ArrayList<>();
        for (RegionalIndexDefEntity def : defs.findByEnabledTrueOrderByIdAsc()) {
            try {
                if (indices.ready(def)) {
                    out.add(submit(def.getId(), null));
                }
            } catch (RuntimeException e) {
                log.warn("Monthly fit of index {} not submitted: {}", def.getId(), e.toString());
            }
        }
        return out;
    }

    @Override
    public Map<String, Object> run(BacktestJobEntity job, JobMonitor monitor) {
        String indexId = String.valueOf(job.getDefinition().get("indexId"));
        RegionalIndexFitEntity fit = fits.findById(job.getId()).orElseGet(() -> fits.save(
                new RegionalIndexFitEntity(job.getId(), indexId, clock.instant())));
        RegionalIndexFitEntity done = runFit(fit);
        return IndexViews.fit(done);
    }

    /** Runs a fit now and stores its result (the job's work; tests call it directly). */
    public RegionalIndexFitEntity runFit(RegionalIndexFitEntity fit) {
        Instant now = clock.instant();
        fit.setStatus("running");
        fit.setStartedAt(now);
        fits.save(fit);
        try {
            RegionalIndexDefEntity def = indices.get(fit.getIndexId());
            List<String> keys = new ArrayList<>();
            for (Map<String, Object> c : def.getComponents()) {
                keys.add(c.get("key").toString());
            }
            List<IndexFitter.Row> rows = rows(def, keys, now);
            if (rows.size() < IndexFitter.MIN_ROWS) {
                indices.computeDays(def, RegionalIndexService.backfillDays(now.atZone(ZoneOffset.UTC).toLocalDate()
                        .minusDays(1)), now, false);
                rows = rows(def, keys, now);
            }
            IndexFitter.Result result = IndexFitter.fit(rows, keys, def.getWeights(), now);
            fit.setLambda(result.lambda());
            fit.setRegions(result.regions());
            fit.setOrigins(result.origins());
            fit.setProposedWeights(result.proposed());
            fit.setWeightsVersion(result.proposed() == null ? null : def.getWeightsVersion() + 1);
            fit.setSkill(result.skill());
            fit.setComponents(result.components().stream().map(IndexFitter.ComponentFit::view).toList());
            Map<String, Object> config = new LinkedHashMap<>();
            config.put("method", METHOD);
            config.put("lambdas", java.util.Arrays.stream(IndexFitter.LAMBDAS).boxed().toList());
            config.put("folds", IndexFitter.FOLDS);
            config.put("target", def.getTarget());
            config.put("weightsVersion", def.getWeightsVersion());
            fit.setManifestId(manifest(config, def));
            fit.setStatus("done");
            fit.setFinishedAt(clock.instant());
            fits.save(fit);
            def.setSkill(result.skill());
            defs.save(def);
            if (result.proposed() != null) {
                try {
                    findings.announce(fit.getId(), FindingEvent.INDEX_WEIGHTS_PROPOSED, RegionalIndexService.NAME,
                            null);
                } catch (RuntimeException e) {
                    log.warn("index.weights_proposed for {} failed: {}", fit.getId(), e.toString());
                }
                if (def.isAutoApplyFittedWeights()) {
                    indices.applyWeights(indices.get(def.getId()), new IndexInputs.Weights("fitted",
                            fit.getWeightsVersion(), null, "applied automatically (autoApplyFittedWeights)"),
                            "system");
                    return fits.findById(fit.getId()).orElse(fit);
                }
            }
            return fit;
        } catch (RuntimeException e) {
            fit.setStatus("failed");
            fit.setError(e.getMessage() == null ? e.toString() : e.getMessage());
            fit.setFinishedAt(clock.instant());
            log.warn("Index fit {} failed: {}", fit.getId(), fit.getError());
            return fits.save(fit);
        }
    }

    /** The fitting rows of an index: stored Monday components and the next-four-week target counts. */
    List<IndexFitter.Row> rows(RegionalIndexDefEntity def, List<String> keys, Instant now) {
        LocalDate monday = now.atZone(ZoneOffset.UTC).toLocalDate().with(TemporalAdjusters.previousOrSame(
                DayOfWeek.MONDAY));
        LocalDate from = monday.minusWeeks(RegionalIndexService.BACKFILL_WEEKS + 1L);
        Map<String, Map<LocalDate, double[]>> components = new HashMap<>();
        for (RegionalIndexValueEntity v : values.findByDefIdAndDayBetweenOrderByDayAscRegionAsc(def.getId(), from,
                monday)) {
            if (v.getDay().getDayOfWeek() != DayOfWeek.MONDAY) {
                continue;
            }
            double[] x = new double[keys.size()];
            boolean complete = true;
            for (int k = 0; k < keys.size(); k++) {
                Double n = normalized(v.getComponents(), keys.get(k));
                if (n == null) {
                    complete = false;
                    break;
                }
                x[k] = n / 100;
            }
            if (complete) {
                components.computeIfAbsent(v.getRegion(), r -> new HashMap<>()).put(v.getDay(), x);
            }
        }
        Map<String, Map<LocalDate, Long>> target = targets(def.getTarget(), components.keySet(), monday);
        List<IndexFitter.Row> rows = new ArrayList<>();
        for (Map.Entry<String, Map<LocalDate, double[]>> e : components.entrySet()) {
            Map<LocalDate, Long> weekly = target.get(e.getKey());
            if (weekly == null) {
                continue;
            }
            for (Map.Entry<LocalDate, double[]> w : e.getValue().entrySet()) {
                Long y = window(weekly, w.getKey().plusWeeks(1));
                if (y == null) {
                    continue;
                }
                Long baseline = window(weekly, w.getKey().plusWeeks(1).minusWeeks(52));
                rows.add(new IndexFitter.Row(e.getKey(), w.getKey(), w.getValue(), y,
                        baseline == null ? null : baseline.doubleValue()));
            }
        }
        rows.sort(java.util.Comparator.comparing(IndexFitter.Row::week).thenComparing(IndexFitter.Row::region));
        return rows;
    }

    /** The sum of four weekly counts from {@code start}, or null when a week is missing. */
    static Long window(Map<LocalDate, Long> weekly, LocalDate start) {
        long sum = 0;
        for (int k = 0; k < 4; k++) {
            Long v = weekly.get(start.plusWeeks(k));
            if (v == null) {
                return null;
            }
            sum += v;
        }
        return sum;
    }

    /** A component's stored normalized value (wire member {@code normalised}), or null when it was missing. */
    static Double normalized(List<Map<String, Object>> parts, String key) {
        for (Map<String, Object> p : parts) {
            if (key.equals(p.get("key"))) {
                if (Boolean.TRUE.equals(p.get("missing"))) {
                    return null;
                }
                Object n = p.get("normalised");
                return n instanceof Number num ? num.doubleValue() : null;
            }
        }
        return null;
    }

    /** The weekly target counts per region, up to the Monday {@code until} (exclusive). */
    Map<String, Map<LocalDate, Long>> targets(String target, Set<String> regions, LocalDate until) {
        Map<String, Map<LocalDate, Long>> out = new HashMap<>();
        if (regions.isEmpty()) {
            return out;
        }
        if (target.startsWith("finding:")) {
            String[] parts = target.split(":", 3);
            LocalDate from = until.minusWeeks(RegionalIndexService.BACKFILL_WEEKS + 60L);
            Map<String, Map<LocalDate, Long>> found = new HashMap<>();
            jdbc.query("SELECT r AS region, CAST(date_trunc('week', detected_at AT TIME ZONE 'UTC') AS date) AS week,"
                    + " count(*) AS n FROM analytic_finding, unnest(region_codes) AS r WHERE finding_type = :type AND"
                    + " signal_family = :family AND status IN ('open', 'superseded') AND detected_at >= :from AND"
                    + " detected_at < :until AND r IN (:regions) GROUP BY 1, 2", new MapSqlParameterSource()
                            .addValue("type", parts[1]).addValue("family", parts[2])
                            .addValue("from", java.sql.Timestamp.from(from.atStartOfDay(ZoneOffset.UTC).toInstant()))
                            .addValue("until", java.sql.Timestamp.from(until.atStartOfDay(ZoneOffset.UTC)
                                    .toInstant()))
                            .addValue("regions", List.copyOf(regions)), rs -> {
                                found.computeIfAbsent(rs.getString("region"), k -> new HashMap<>())
                                        .put(rs.getDate("week").toLocalDate(), rs.getLong("n"));
                            });
            for (String region : regions) {
                Map<LocalDate, Long> weeks = new HashMap<>();
                for (LocalDate w = from; w.isBefore(until); w = w.plusWeeks(1)) {
                    weeks.put(w, found.getOrDefault(region, Map.of()).getOrDefault(w, 0L));
                }
                out.put(region, weeks);
            }
            return out;
        }
        String reference = families.find(target).map(OutlookFamilies.Family::referenceFamily).orElse(target);
        for (ReferenceCounts.Weekly w : counts.weekly(reference, until, regions).values()) {
            Map<LocalDate, Long> weeks = new HashMap<>();
            for (int i = 0; i < w.size(); i++) {
                weeks.put(w.weekAt(i), w.counts()[i]);
            }
            out.put(w.region(), weeks);
        }
        return out;
    }

    private String manifest(Map<String, Object> config, RegionalIndexDefEntity def) {
        ManifestService service = manifests.getIfAvailable();
        if (service == null) {
            return null;
        }
        try {
            return service.record(ManifestRequest.of(RegionalIndexService.NAME + "-fit", RegionalIndexService.VERSION)
                    .config(config).inputs(List.of("regional-index:" + def.getId())).query(Map.of("indexId",
                            def.getId(), "target", def.getTarget())));
        } catch (RuntimeException e) {
            log.warn("Could not record the manifest of an index fit: {}", e.toString());
            return null;
        }
    }
}
