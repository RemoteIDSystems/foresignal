package ai.foresignal.observability;

import ai.foresignal.evidence.CanonicalRules;
import ai.foresignal.geo.H3Cells;
import ai.foresignal.manifest.ManifestRequest;
import ai.foresignal.manifest.ManifestService;
import ai.foresignal.security.Ulids;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.stats.RollupResolution;
import ai.foresignal.stream.ChangeBus;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.IsoFields;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The empirical observability estimator (prompt 116, version {@value #VERSION}): once a week, on Mondays from
 * 04:30 UTC, it adds time-versioned {@code observability_estimate} rows.
 *
 * <ol>
 *   <li><strong>Capture–recapture</strong> for every canonical-event family with at least two independence
 *       roots among its links in the trailing {@value #CAPTURE_DAYS} days: for each source A, per region and band
 *       (and over all regions and bands), {@code |A ∩ B| / |B|} where B is the canonical events also observed
 *       by an independent other source ({@link CaptureRecapture}, minimum |B| = 20).
 *   <li><strong>Presence proxy</strong> for track families from the 038 hourly rollups of the trailing
 *       {@value #PRESENCE_DAYS} days ({@link PresenceProxy}); local receivers ({@code local-*}) get their measured
 *       range instead (method {@code receiver-range}).
 * </ol>
 *
 * <p>The declared fallback needs no rows: {@link ObservabilityService} reads the declarations directly. Each run
 * records one manifest per family and method. {@code foresignal.observability.scheduled=false} turns the tick
 * off (tests call {@link #run(Instant)}).
 */
@Component
public class ObservabilityEstimator {

    private static final Logger log = LoggerFactory.getLogger(ObservabilityEstimator.class);

    /** The estimator component name in manifests. */
    public static final String NAME = "observability-estimator";

    /** The estimator version. */
    public static final String VERSION = "1.0.0";

    /** The capture–recapture window, days. */
    public static final int CAPTURE_DAYS = 365;

    /** The presence-proxy window, days. */
    public static final int PRESENCE_DAYS = 28;

    /** The weekly run's day. */
    public static final DayOfWeek RUN_DAY = DayOfWeek.MONDAY;

    /** The weekly run's earliest time of day, UTC. */
    public static final int RUN_HOUR = 4;

    /** The weekly run's earliest minute. */
    public static final int RUN_MINUTE = 30;

    private final JdbcTemplate jdbc;
    private final ObservabilityDeclarations declarations;
    private final CanonicalRules rules;
    private final ObjectProvider<SourceRegistry> sources;
    private final ObjectProvider<ManifestService> manifests;
    private final ObjectProvider<ChangeBus> changeBus;
    private final Clock clock;
    private final boolean scheduled;
    private volatile String lastRunWeek;

    /** Creates the estimator. */
    public ObservabilityEstimator(JdbcTemplate jdbc, ObservabilityDeclarations declarations, CanonicalRules rules,
            ObjectProvider<SourceRegistry> sources,
            ObjectProvider<ManifestService> manifests, ObjectProvider<ChangeBus> changeBus, Clock clock,
            @Value("${foresignal.observability.scheduled:true}") boolean scheduled) {
        this.jdbc = jdbc;
        this.declarations = declarations;
        this.rules = rules;
        this.sources = sources;
        this.manifests = manifests;
        this.changeBus = changeBus;
        this.clock = clock;
        this.scheduled = scheduled;
    }

    /** The tick: runs once per ISO week, on {@link #RUN_DAY} from 04:30 UTC (or the first tick after it). */
    @Scheduled(fixedDelayString = "PT15M", initialDelayString = "PT5M")
    public void tick() {
        if (!scheduled) {
            return;
        }
        Instant now = clock.instant();
        if (!due(now, lastRunWeek)) {
            return;
        }
        try {
            run(now);
            lastRunWeek = week(now);
        } catch (RuntimeException e) {
            log.warn("The observability estimator failed: {}", e.toString());
        }
    }

    /** True when the weekly run is due at {@code now}; pure. */
    static boolean due(Instant now, String lastRunWeek) {
        ZonedDateTime utc = now.atZone(ZoneOffset.UTC);
        if (week(now).equals(lastRunWeek)) {
            return false;
        }
        LocalDate monday = utc.toLocalDate().with(RUN_DAY);
        ZonedDateTime start = monday.atTime(RUN_HOUR, RUN_MINUTE).atZone(ZoneOffset.UTC);
        return !utc.isBefore(start);
    }

    private static String week(Instant now) {
        ZonedDateTime utc = now.atZone(ZoneOffset.UTC);
        return utc.get(IsoFields.WEEK_BASED_YEAR) + "-W" + utc.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    }

    /**
     * What one run added.
     *
     * @param captureRecapture capture–recapture rows
     * @param presence presence-proxy rows
     * @param receiverRange receiver-range rows
     */
    public record Result(int captureRecapture, int presence, int receiverRange) {}

    /** One run at {@code now}. */
    public Result run(Instant now) {
        int cr = 0;
        for (String family : declarations.eventFamilies()) {
            try {
                cr += captureRecapture(family, now);
            } catch (RuntimeException e) {
                log.warn("Capture-recapture failed for {}: {}", family, e.toString());
            }
        }
        int presence = 0;
        int range = 0;
        for (String family : declarations.trackFamilies()) {
            try {
                int[] counts = presence(family, now);
                presence += counts[0];
                range += counts[1];
            } catch (RuntimeException e) {
                log.warn("The presence proxy failed for {}: {}", family, e.toString());
            }
        }
        log.info("Observability estimates added: {} capture-recapture, {} presence-proxy, {} receiver-range", cr,
                presence, range);
        return new Result(cr, presence, range);
    }

    // ------------------------------------------------------------------------- capture–recapture

    /** Capture–recapture rows for one family; returns how many were added. */
    int captureRecapture(String family, Instant now) {
        Instant from = now.minus(Duration.ofDays(CAPTURE_DAYS));
        Integer roots = jdbc.queryForObject("SELECT count(DISTINCT l.independence_root) FROM observation_link l"
                + " JOIN canonical_event c ON c.id = l.canonical_event_id WHERE c.family = ? AND c.status <> 'merged'"
                + " AND c.event_time >= ? AND c.event_time < ?", Integer.class, family, Timestamp.from(from),
                Timestamp.from(now));
        if (roots == null || roots < 2) {
            return 0;
        }
        List<Map<String, Object>> sourceRows = jdbc.queryForList("SELECT DISTINCT l.source_id, l.independence_root"
                + " FROM observation_link l JOIN canonical_event c ON c.id = l.canonical_event_id WHERE c.family = ?"
                + " AND c.status <> 'merged' AND c.event_time >= ? AND c.event_time < ?", family, Timestamp.from(from),
                Timestamp.from(now));
        CanonicalRules.FamilyRule rule = rules.family(family);
        CanonicalRules.Bands bands = rule == null ? null : rule.bands();
        String manifestId = manifest(CaptureRecapture.METHOD, family, from, now,
                Map.of("minReference", CaptureRecapture.MIN_REFERENCE, "windowDays", CAPTURE_DAYS,
                        "bands", bands == null ? Map.of() : Map.of("prefix", bands.prefix(), "start", bands.start(),
                                "step", bands.step(), "end", bands.end())));
        int added = 0;
        for (Map<String, Object> row : sourceRows) {
            String sourceId = String.valueOf(row.get("source_id"));
            String root = String.valueOf(row.get("independence_root"));
            for (Map<String, Object> cell : overlap(family, sourceId, root, from, now, bands)) {
                long reference = ((Number) cell.get("nb")).longValue();
                long hits = ((Number) cell.get("nab")).longValue();
                Optional<CaptureRecapture.Estimate> estimate = CaptureRecapture.estimate(hits, reference);
                if (estimate.isEmpty()) {
                    continue;
                }
                insert(sourceId, family, (String) cell.get("region"), null, (String) cell.get("band"), from, now,
                        estimate.get().p(), estimate.get().lo(), estimate.get().hi(), estimate.get().n(),
                        CaptureRecapture.METHOD, estimate.get().confidence(), now, manifestId);
                added++;
            }
        }
        return added;
    }

    /**
     * |B| and |A ∩ B| per region and band, per region, per band and overall, for source A ({@code sourceId},
     * root {@code root}) against the events observed by an independent other source.
     */
    private List<Map<String, Object>> overlap(String family, String sourceId, String root, Instant from, Instant to,
            CanonicalRules.Bands bands) {
        String band = bandExpression(bands);
        return jdbc.queryForList("WITH ev AS (SELECT c.id, r.code AS region, " + band + " AS band"
                        + " FROM canonical_event c LEFT JOIN LATERAL unnest(c.region_codes) AS r(code) ON true"
                        + " WHERE c.family = ? AND c.status <> 'merged' AND c.event_time >= ? AND c.event_time < ?),"
                        + " b AS (SELECT DISTINCT l.canonical_event_id FROM observation_link l JOIN ev ON ev.id ="
                        + " l.canonical_event_id WHERE l.source_id <> ? AND l.independence_root <> ?),"
                        + " a AS (SELECT DISTINCT l.canonical_event_id FROM observation_link l WHERE l.source_id = ?)"
                        + " SELECT ev.region, ev.band, count(DISTINCT ev.id) AS nb, count(DISTINCT a.canonical_event_id)"
                        + " AS nab FROM ev JOIN b ON b.canonical_event_id = ev.id LEFT JOIN a ON a.canonical_event_id"
                        + " = ev.id GROUP BY GROUPING SETS ((ev.region, ev.band), (ev.region), (ev.band), ())",
                family, Timestamp.from(from), Timestamp.from(to), sourceId, root, sourceId);
    }

    /** The SQL expression of an event's band from {@code band_value}, or NULL without bands. */
    static String bandExpression(CanonicalRules.Bands bands) {
        if (bands == null) {
            return "CAST(NULL AS text)";
        }
        String prefix = bands.prefix().replace("'", "''");
        String start = String.valueOf(bands.start());
        String step = String.valueOf(bands.step());
        String end = String.valueOf(bands.end());
        String lower = "(" + start + " + floor((c.band_value - " + start + ") / " + step + ") * " + step + ")";
        return "CASE WHEN c.band_value IS NULL OR c.band_value < " + start + " THEN NULL"
                + " WHEN c.band_value >= " + end + " THEN '" + prefix + "' || trim_scale(" + end + "::numeric) || '+'"
                + " ELSE '" + prefix + "' || trim_scale(" + lower + "::numeric) || '-' || trim_scale((" + lower + " + "
                + step + ")::numeric) END";
    }

    // ------------------------------------------------------------------------- presence proxy

    /** Presence-proxy and receiver-range rows for one track family; returns {presence, range} counts. */
    int[] presence(String family, Instant now) {
        SourceRegistry registry = sources.getIfAvailable();
        if (registry == null) {
            return new int[] {0, 0};
        }
        Set<String> kinds = declarations.kindsOf(family);
        Instant from = now.minus(Duration.ofDays(PRESENCE_DAYS));
        String manifestId = manifest(PresenceProxy.METHOD, family, from, now,
                Map.of("windowDays", PRESENCE_DAYS, "normalization", "p90", "rangeShare", PresenceProxy.RANGE_SHARE,
                        "cellResolution", ObservabilityService.CELL_RESOLUTION));
        int presence = 0;
        int range = 0;
        for (SourceDescriptor descriptor : registry.descriptors()) {
            Set<String> sourceKinds = new LinkedHashSet<>(descriptor.kinds());
            sourceKinds.retainAll(kinds);
            if (sourceKinds.isEmpty()) {
                continue;
            }
            String[] kindArray = sourceKinds.toArray(String[]::new);
            Long active = jdbc.queryForObject("SELECT count(DISTINCT bucket_start) FROM activity_rollup WHERE"
                    + " resolution = ? AND source_id = ? AND kind = ANY(?) AND bucket_start >= ? AND bucket_start < ?",
                    Long.class, RollupResolution.HOUR.code(), descriptor.id(), kindArray, Timestamp.from(from),
                    Timestamp.from(now));
            if (active == null || active == 0) {
                continue;
            }
            Map<String, Long> hoursByCell = new LinkedHashMap<>();
            jdbc.query("SELECT cell, count(DISTINCT bucket_start) FROM activity_rollup WHERE resolution = ?"
                            + " AND source_id = ? AND kind = ANY(?) AND bucket_start >= ? AND bucket_start < ?"
                            + " AND cell > 0 AND entities > 0 GROUP BY cell ORDER BY cell",
                    rs -> {
                        long cell = rs.getLong(1);
                        if (H3Cells.isValid(cell)) {
                            hoursByCell.put(H3Cells.cellToAddress(cell), rs.getLong(2));
                        }
                    }, RollupResolution.HOUR.code(), descriptor.id(), kindArray, Timestamp.from(from),
                    Timestamp.from(now));
            boolean local = descriptor.id().startsWith("local-");
            Map<String, PresenceProxy.CellEstimate> estimates = local
                    ? PresenceProxy.receiverRange(hoursByCell, active)
                    : PresenceProxy.estimate(hoursByCell, active);
            String method = local ? PresenceProxy.RECEIVER_RANGE : PresenceProxy.METHOD;
            for (Map.Entry<String, PresenceProxy.CellEstimate> entry : estimates.entrySet()) {
                PresenceProxy.CellEstimate e = entry.getValue();
                insert(descriptor.id(), family, null, entry.getKey(), null, from, now, e.p(), local ? e.p() : 0,
                        local ? e.p() : Math.min(1, e.p() * 1.5), e.hours(), method, "low", now, manifestId);
                if (local) {
                    range++;
                } else {
                    presence++;
                }
            }
        }
        return new int[] {presence, range};
    }

    // ------------------------------------------------------------------------- writes

    private void insert(String sourceId, String family, String regionCode, String cell, String band, Instant from,
            Instant to, double p, double lo, double hi, long n, String method, String confidence, Instant computedAt,
            String manifestId) {
        jdbc.update("INSERT INTO observability_estimate (id, source_id, family, region_code, cell, band, period_start,"
                        + " period_end, p_detect, lo, hi, n, method, confidence, computed_at, manifest_id)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Ulids.next(computedAt), sourceId, family, regionCode, cell, band, Timestamp.from(from), Timestamp.from(to),
                p, lo, hi, n, method, confidence, Timestamp.from(computedAt), manifestId);
    }

    private String manifest(String method, String family, Instant from, Instant to, Map<String, Object> params) {
        ManifestService service = manifests.getIfAvailable();
        if (service == null) {
            return null;
        }
        Map<String, Object> config = new LinkedHashMap<>(params);
        config.put("method", method);
        config.put("family", family);
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("table", CaptureRecapture.METHOD.equals(method) ? "canonical_event" : "activity_rollup");
        query.put("family", family);
        query.put("from", from.toString());
        query.put("to", to.toString());
        ChangeBus bus = changeBus.getIfAvailable();
        try {
            return service.record(ManifestRequest.of(NAME, VERSION).config(config)
                    .registries("originators.json", "canonical-rules.json", "observability.json")
                    .asOf(bus == null ? null : bus.lastPublishedSeq()).query(query));
        } catch (RuntimeException e) {
            log.warn("Could not record the estimator manifest: {}", e.toString());
            return null;
        }
    }
}
