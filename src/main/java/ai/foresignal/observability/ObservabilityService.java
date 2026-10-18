package ai.foresignal.observability;

import ai.foresignal.collect.HealthService;
import ai.foresignal.evidence.CanonicalRules;
import ai.foresignal.evidence.OriginatorRegistry;
import ai.foresignal.geo.H3Cells;
import ai.foresignal.reference.RegionTagger;
import ai.foresignal.reference.RegionTags;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.DeclaredObservability;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.trigger.window.CoverageGate;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Detection probabilities and the absence rule (prompt 116).
 *
 * <p><strong>Per source</strong>, the estimate valid at {@code at} is the newest {@code observability_estimate}
 * row whose period ended at or before {@code at} (never today's for a past time; OBS-2), preferring the most
 * specific place (H3 cell, then region, then everywhere), then the method (capture–recapture, receiver range,
 * presence proxy), then the newest period. A source with cell-level track estimates but none for the cell
 * observed nothing there ({@code p = 0}). Without any estimate the declared footprint answers (method
 * {@code declared}, low confidence): 1 inside the declared area and at or above the declared threshold for the
 * place, else 0; a source that declares nothing is taken to observe everywhere without threshold.
 *
 * <p><strong>Combination</strong>: {@code 1 − Π(1 − p_g)} over independence groups (the originator roots of
 * {@link OriginatorRegistry}); sources of one group combine by max, never by product. Intervals combine the
 * same way from their bounds.
 *
 * <p><strong>Absence</strong> ({@link #canSeeAbsence}): every involved source healthy over the window (the 100
 * {@link CoverageGate}) and {@code p ≥ absenceMinDetect} (setting {@code observability.absenceMinDetect},
 * default {@value #DEFAULT_ABSENCE_MIN_DETECT}); otherwise {@code unknown} with reason
 * {@link AbsenceCheck#LOW_DETECTABILITY} or the coverage reason.
 */
@Service
public class ObservabilityService {

    /** The default {@code absenceMinDetect}. */
    public static final double DEFAULT_ABSENCE_MIN_DETECT = 0.8;

    /** The declared-footprint method. */
    public static final String DECLARED = "declared";

    /** The H3 resolution of cell estimates. */
    public static final int CELL_RESOLUTION = 4;

    /** How old an estimate may be and still be valid. */
    public static final Duration MAX_ESTIMATE_AGE = Duration.ofDays(400);

    /** How recent {@code at} must be for current health to decide which sources are involved. */
    static final Duration CURRENT = Duration.ofDays(1);

    /** The method preference, best first. */
    static final List<String> METHOD_ORDER = List.of(CaptureRecapture.METHOD, PresenceProxy.RECEIVER_RANGE,
            PresenceProxy.METHOD, DECLARED);

    private final JdbcTemplate jdbc;
    private final ObservabilityDeclarations declarations;
    private final OriginatorRegistry originators;
    private final ObjectProvider<SourceRegistry> sources;
    private final ObjectProvider<HealthService> health;
    private final ObjectProvider<CoverageGate> gate;
    private final ObjectProvider<RegionTagger> regions;
    private final AppSettingService settings;
    private final Clock clock;

    /** Creates the service. */
    public ObservabilityService(JdbcTemplate jdbc, ObservabilityDeclarations declarations,
            OriginatorRegistry originators, ObjectProvider<SourceRegistry> sources, ObjectProvider<HealthService> health,
            ObjectProvider<CoverageGate> gate, ObjectProvider<RegionTagger> regions, AppSettingService settings,
            Clock clock) {
        this.jdbc = jdbc;
        this.declarations = declarations;
        this.originators = originators;
        this.sources = sources;
        this.health = health;
        this.gate = gate;
        this.regions = regions;
        this.settings = settings;
        this.clock = clock;
    }

    // ------------------------------------------------------------------------- views

    /**
     * One source's estimate as used.
     *
     * @param sourceId the source
     * @param family the family
     * @param regionCode the region, or null
     * @param cell the H3 cell, or null
     * @param band the band, or null
     * @param periodStart the period start, or null for a declaration
     * @param periodEnd the period end, or null for a declaration
     * @param p the detection probability
     * @param lo the lower bound
     * @param hi the upper bound
     * @param n the sample size
     * @param method the method
     * @param confidence the confidence
     * @param computedAt when computed, or null for a declaration
     * @param manifestId the run's manifest, or null
     */
    public record SourceEstimate(String sourceId, String family, String regionCode, String cell, String band,
            Instant periodStart, Instant periodEnd, double p, double lo, double hi, long n, String method,
            String confidence, Instant computedAt, String manifestId) {

        /** The {@code ObservabilityEstimate} API shape. */
        public Map<String, Object> view() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("sourceId", sourceId);
            out.put("family", family);
            out.put("regionCode", regionCode);
            out.put("cell", cell);
            out.put("band", band);
            out.put("periodStart", periodStart);
            out.put("periodEnd", periodEnd);
            out.put("pDetect", p);
            out.put("lo", lo);
            out.put("hi", hi);
            out.put("n", n);
            out.put("method", method);
            out.put("confidence", confidence);
            out.put("computedAt", computedAt);
            out.put("manifestId", manifestId);
            return out;
        }
    }

    /**
     * A combined detection probability.
     *
     * @param family the family
     * @param regionCode the region asked for, or null
     * @param cell the cell asked for, or null
     * @param band the band, or null
     * @param at the time
     * @param p the combined probability
     * @param lo the combined lower bound
     * @param hi the combined upper bound
     * @param methods the methods used
     * @param sources the sources used
     * @param confidence the best confidence among the estimates used
     * @param estimates the per-source estimates
     */
    public record Detectability(String family, String regionCode, String cell, String band, Instant at, double p,
            double lo, double hi, List<String> methods, List<String> sources, String confidence,
            List<SourceEstimate> estimates) {

        /** The {@code Detectability} API shape, with the absence verdict. */
        public Map<String, Object> view(boolean absenceOk, String reason, boolean includeEstimates) {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("family", family);
            out.put("regionCode", regionCode);
            out.put("cell", cell);
            out.put("band", band);
            out.put("at", at);
            out.put("p", p);
            out.put("lo", lo);
            out.put("hi", hi);
            out.put("methods", methods);
            out.put("sources", sources);
            out.put("confidence", confidence);
            out.put("absenceOk", absenceOk);
            out.put("reason", reason);
            if (includeEstimates) {
                out.put("estimates", estimates.stream().map(SourceEstimate::view).toList());
            }
            return out;
        }
    }

    // ------------------------------------------------------------------------- API

    /** The {@code social-attention} family (prompt 135). */
    public static final String SOCIAL_ATTENTION = "social-attention";

    /** The {@code social-attention} estimator (prompt 135), when the social package is present. */
    @org.springframework.beans.factory.annotation.Autowired
    private ObjectProvider<ai.foresignal.social.SocialObservability> socialObservability;

    /**
     * The {@code social-attention} family's absence rule (prompt 135, method {@code platform-volume}): delegated to
     * {@code SocialObservability}; null for every other family.
     */
    private AbsenceCheck socialAttention(String family, Place place, Instant from, Instant to) {
        if (!SOCIAL_ATTENTION.equals(family)) {
            return null;
        }
        ai.foresignal.social.SocialObservability social = socialObservability == null ? null
                : socialObservability.getIfAvailable();
        if (social == null) {
            return new AbsenceCheck(false, null, "no social observability is available");
        }
        return social.canSeeAbsence(place == null ? null : place.regionCode(), from, to);
    }

    /** {@code absenceMinDetect} in force. */
    public double absenceMinDetect() {
        try {
            return settings.get(SettingKeys.ABSENCE_MIN_DETECT, Double.class).orElse(DEFAULT_ABSENCE_MIN_DETECT);
        } catch (RuntimeException e) {
            return DEFAULT_ABSENCE_MIN_DETECT;
        }
    }

    /**
     * The combined detection probability of {@code family} at {@code place} and {@code at}, over the enabled,
     * healthy sources that observe the family (for a past {@code at}, the sources with estimates valid then).
     *
     * @param band the threshold band (e.g. {@code M4.5-5}), or null
     */
    public Detectability pDetect(String family, Place place, Instant at, String band) {
        Snapshot snapshot = snapshot(family, at);
        return combine(family, place, at, band, involved(family, at, snapshot), snapshot);
    }

    /** {@link #pDetect(String, Place, Instant, String)} over the given sources only (no health filter). */
    public Detectability pDetect(Collection<String> sourceIds, String family, Place place, Instant at, String band) {
        return combine(family, place, at, band, new TreeSet<>(sourceIds), snapshot(family, at));
    }

    /**
     * Whether an absence of {@code family} at {@code place} over {@code [from, to)} may be read as "nothing
     * happened" (OBS-1): every involved source healthy over the window and {@code p ≥ absenceMinDetect}.
     */
    public AbsenceCheck canSeeAbsence(String family, Place place, Instant from, Instant to, String band) {
        AbsenceCheck social = socialAttention(family, place, from, to);
        if (social != null) {
            return social;
        }
        Snapshot snapshot = snapshot(family, to);
        return canSeeAbsence(involved(family, to, snapshot), family, place, from, to, band);
    }

    /** {@link #canSeeAbsence(String, Place, Instant, Instant, String)} over the given sources. */
    public AbsenceCheck canSeeAbsence(Collection<String> sourceIds, String family, Place place, Instant from,
            Instant to, String band) {
        AbsenceCheck social = socialAttention(family, place, from, to);
        if (social != null) {
            return social;
        }
        Set<String> involved = new TreeSet<>(sourceIds);
        if (involved.isEmpty()) {
            return new AbsenceCheck(false, null, "no enabled source observes " + family);
        }
        CoverageGate coverage = gate.getIfAvailable();
        if (coverage != null) {
            CoverageGate.Gate result = coverage.evaluate(involved, declarations.kindsOf(family), from, to,
                    Duration.between(from, to), 0.8);
            if (!result.ok()) {
                return new AbsenceCheck(false, null, result.reason());
            }
        }
        return detectable(involved, family, place, to, band);
    }

    /**
     * The detectability half of the absence rule only, for callers that already checked coverage (the 100
     * windowed rules): {@code p ≥ absenceMinDetect} over the given sources.
     */
    public AbsenceCheck detectable(Collection<String> sourceIds, String family, Place place, Instant at, String band) {
        Detectability d = pDetect(sourceIds, family, place, at, band);
        double min = absenceMinDetect();
        return d.p() >= min ? new AbsenceCheck(true, d.p(), null)
                : new AbsenceCheck(false, d.p(), AbsenceCheck.LOW_DETECTABILITY);
    }

    /**
     * The coverage layer response ({@code GET /v1/observability}): one item for a region, one per H3 res-4 cell
     * of a bounding box (paged by offset), or one for everywhere.
     */
    public Map<String, Object> coverage(String family, String region, double[] bbox, Instant at, String band,
            boolean includeEstimates, int offset, int limit) {
        Snapshot snapshot = snapshot(family, at);
        Set<String> involved = involved(family, at, snapshot);
        double min = absenceMinDetect();
        List<Map<String, Object>> items = new ArrayList<>();
        String next = null;
        if (bbox != null) {
            List<Long> cells = cellsIn(bbox, CELL_RESOLUTION, offset + limit + 1);
            for (int i = offset; i < Math.min(cells.size(), offset + limit); i++) {
                long cell = cells.get(i);
                org.locationtech.jts.geom.Coordinate c = H3Cells.cellCenter(cell);
                Detectability d = combine(family, Place.point(c.x, c.y), at, band, involved, snapshot);
                items.add(d.view(d.p() >= min, d.p() >= min ? null : AbsenceCheck.LOW_DETECTABILITY, includeEstimates));
            }
            if (cells.size() > offset + limit) {
                next = String.valueOf(offset + limit);
            }
        } else {
            Place place = region == null ? Place.GLOBAL : Place.region(region);
            Detectability d = combine(family, place, at, band, involved, snapshot);
            items.add(d.view(d.p() >= min, d.p() >= min ? null : AbsenceCheck.LOW_DETECTABILITY, includeEstimates));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("family", family);
        out.put("at", at);
        out.put("band", band);
        out.put("absenceMinDetect", min);
        out.put("items", items);
        out.put("nextCursor", next);
        return out;
    }

    /**
     * The coverage-layer cells of a web-mercator tile: H3 cells (resolution by zoom) with their combined
     * detection probability.
     *
     * @return cell id → detectability, in a stable order
     */
    public Map<Long, Detectability> tileCells(String family, Instant at, String band, double[] tileBbox, int zoom) {
        Snapshot snapshot = snapshot(family, at);
        Set<String> involved = involved(family, at, snapshot);
        Map<Long, Detectability> out = new LinkedHashMap<>();
        for (long cell : cellsIn(tileBbox, resolutionForZoom(zoom), MAX_TILE_CELLS)) {
            org.locationtech.jts.geom.Coordinate c = H3Cells.cellCenter(cell);
            out.put(cell, combine(family, Place.point(c.x, c.y), at, band, involved, snapshot));
        }
        return out;
    }

    /** The most cells one coverage tile holds. */
    public static final int MAX_TILE_CELLS = 1_500;

    /** The H3 resolution drawn at a zoom: coarser hexagons at small zooms. */
    public static int resolutionForZoom(int zoom) {
        if (zoom <= 2) {
            return 1;
        }
        if (zoom <= 4) {
            return 2;
        }
        if (zoom <= 6) {
            return 3;
        }
        return CELL_RESOLUTION;
    }

    // ------------------------------------------------------------------------- combination

    /** Combines the per-source estimates at a place. */
    Detectability combine(String family, Place place, Instant at, String band, Set<String> involved,
            Snapshot snapshot) {
        List<String> placeRegions = regionsOf(place);
        String cell = place.located()
                ? H3Cells.cellToAddress(H3Cells.latLngToCell(place.lat(), place.lon(), CELL_RESOLUTION)) : null;
        List<SourceEstimate> used = new ArrayList<>();
        for (String sourceId : involved) {
            SourceEstimate estimate = snapshot.best(sourceId, cell, placeRegions, band);
            if (estimate == null) {
                estimate = declared(sourceId, family, place, placeRegions, band, at);
            }
            if (estimate != null) {
                used.add(estimate);
            }
        }
        return combineEstimates(family, place.regionCode(), cell, band, at, used, originators);
    }

    /**
     * {@code 1 − Π(1 − p_g)} over independence groups, max within a group; pure.
     */
    static Detectability combineEstimates(String family, String regionCode, String cell, String band, Instant at,
            List<SourceEstimate> estimates, OriginatorRegistry originators) {
        Map<String, double[]> byGroup = new TreeMap<>();
        Set<String> methods = new LinkedHashSet<>();
        Set<String> sourceIds = new TreeSet<>();
        String confidence = null;
        for (SourceEstimate e : estimates) {
            String root = originators.resolve(e.sourceId()).root();
            double[] g = byGroup.computeIfAbsent(root, k -> new double[] {0, 0, 0});
            g[0] = Math.max(g[0], e.p());
            g[1] = Math.max(g[1], e.lo());
            g[2] = Math.max(g[2], e.hi());
            methods.add(e.method());
            sourceIds.add(e.sourceId());
            confidence = better(confidence, e.confidence());
        }
        double missP = 1;
        double missLo = 1;
        double missHi = 1;
        for (double[] g : byGroup.values()) {
            missP *= 1 - clamp(g[0]);
            missLo *= 1 - clamp(g[1]);
            missHi *= 1 - clamp(g[2]);
        }
        List<String> orderedMethods = new ArrayList<>(methods);
        orderedMethods.sort(Comparator.comparingInt(METHOD_ORDER::indexOf));
        return new Detectability(family, regionCode, cell, band, at, round(1 - missP), round(1 - missLo),
                round(1 - missHi), orderedMethods, new ArrayList<>(sourceIds), confidence == null ? "low" : confidence,
                estimates);
    }

    private static String better(String a, String b) {
        List<String> order = List.of("low", "moderate", "high");
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return order.indexOf(b) > order.indexOf(a) ? b : a;
    }

    /**
     * The declared estimate of a source: 1 inside its declared area and at or above the threshold that applies
     * to the place, else 0; null when the source declares nothing for the family's kinds and has no kind of it.
     */
    SourceEstimate declared(String sourceId, String family, Place place, List<String> placeRegions, String band,
            Instant at) {
        ExternalCoverage external = declarations.externalCoverage();
        ExternalCoverage.Declared bound = null;
        if (external != null) {
            try {
                bound = external.coverage(sourceId, family);
            } catch (RuntimeException e) {
                bound = null;
            }
        }
        if (bound != null) {
            // Prompt 138: a bound EDP stream's manifest coverage, low confidence until it can be measured.
            double p = bound.p(place);
            return new SourceEstimate(sourceId, family, place.regionCode(), null, band, null, null, p, p, p, 0,
                    DECLARED, "low", null, null);
        }
        DeclaredObservability declaration = declarations.declared(sourceId);
        Set<String> kinds = declarations.kindsOf(family);
        DeclaredObservability.KindObservability kind = null;
        if (declaration != null) {
            for (String k : kinds) {
                kind = declaration.kind(k);
                if (kind != null) {
                    break;
                }
            }
        }
        double p = kind == null ? 1.0 : declaredP(kind, place, placeRegions, CanonicalRules.Bands.lowerBound(band));
        return new SourceEstimate(sourceId, family, place.regionCode(), null, band, null, null, p, p, p, 0, DECLARED,
                "low", null, null);
    }

    /** The declared probability: area membership and the threshold for the place; pure. */
    static double declaredP(DeclaredObservability.KindObservability kind, Place place, List<String> placeRegions,
            Double threshold) {
        DeclaredObservability.Area area = kind.area();
        boolean inside = switch (area.type()) {
            case "bbox" -> bboxContains(area.bbox(), place);
            case "regions" -> placeRegions.stream().anyMatch(area.regionCodes()::contains);
            default -> true;
        };
        if (!inside) {
            return 0;
        }
        if (threshold == null || kind.thresholds().isEmpty()) {
            return 1;
        }
        Double needed = null;
        for (DeclaredObservability.Threshold t : kind.thresholds()) {
            if ("global".equals(t.area()) || placeRegions.contains(t.area())) {
                needed = needed == null ? t.min() : Math.min(needed, t.min());
            }
        }
        return needed == null || threshold >= needed ? 1 : 0;
    }

    /** True when a declared box holds the place's point; a malformed box holds everything, everywhere nothing. */
    static boolean bboxContains(List<Double> bbox, Place place) {
        if (bbox == null || bbox.size() < 4) {
            return true;
        }
        if (!place.located()) {
            return false;
        }
        return place.lon() >= bbox.get(0) && place.lat() >= bbox.get(1) && place.lon() <= bbox.get(2)
                && place.lat() <= bbox.get(3);
    }

    // ------------------------------------------------------------------------- inputs

    /** The sources involved for {@code family} at {@code at}. */
    Set<String> involved(String family, Instant at, Snapshot snapshot) {
        Set<String> out = new TreeSet<>();
        Set<String> kinds = declarations.kindsOf(family);
        boolean current = Duration.between(at, clock.instant()).abs().compareTo(CURRENT) <= 0;
        SourceRegistry registry = sources.getIfAvailable();
        if (!current) {
            out.addAll(snapshot.sources());
            return out;
        }
        if (registry == null) {
            return out;
        }
        HealthService healthService = health.getIfAvailable();
        for (SourceDescriptor descriptor : registry.descriptors()) {
            if (descriptor.kinds().stream().noneMatch(kinds::contains)) {
                continue;
            }
            HealthStatus status;
            try {
                status = healthService == null ? HealthStatus.IDLE : healthService.health(descriptor.id()).status();
            } catch (RuntimeException e) {
                status = HealthStatus.IDLE;
            }
            if (status == HealthStatus.HEALTHY) {
                out.add(descriptor.id());
            }
        }
        return out;
    }

    private List<String> regionsOf(Place place) {
        List<String> out = new ArrayList<>();
        if (place.regionCode() != null) {
            out.add(place.regionCode());
            int dash = place.regionCode().indexOf('-');
            if (dash > 0) {
                out.add(place.regionCode().substring(0, dash));
            }
            return out;
        }
        if (!place.located()) {
            return out;
        }
        RegionTagger tagger = regions.getIfAvailable();
        if (tagger == null) {
            return out;
        }
        try {
            RegionTags tags = tagger.tag(place.lon(), place.lat());
            if (tags.iso2() != null) {
                out.add(tags.iso2());
            }
            if (tags.admin1() != null) {
                out.add(tags.admin1());
            }
        } catch (RuntimeException e) {
            // untagged: only cell and global estimates apply
        }
        return out;
    }

    /** The H3 cells at {@code res} covering a bounding box, sampled on a grid finer than a cell, capped. */
    static List<Long> cellsIn(double[] bbox, int res, int cap) {
        LinkedHashSet<Long> out = new LinkedHashSet<>();
        double stepKm = Math.max(1, H3Cells.averageEdgeLengthKm(res));
        double minLat = Math.max(-85, bbox[1]);
        double maxLat = Math.min(85, bbox[3]);
        double latStep = stepKm / 111.0;
        for (double lat = minLat; lat <= maxLat + 1e-9 && out.size() < cap; lat += latStep) {
            double lonStep = stepKm / (111.0 * Math.max(0.05, Math.cos(Math.toRadians(lat))));
            for (double lon = bbox[0]; lon <= bbox[2] + 1e-9 && out.size() < cap; lon += lonStep) {
                out.add(H3Cells.latLngToCell(lat, lon, res));
            }
        }
        return new ArrayList<>(out);
    }

    // ------------------------------------------------------------------------- snapshot

    /** The estimates of a family valid at a time, newest per source, place, band and method. */
    Snapshot snapshot(String family, Instant at) {
        List<SourceEstimate> rows = jdbc.query("SELECT DISTINCT ON (source_id, region_code, cell, band, method)"
                        + " source_id, family, region_code, cell, band, period_start, period_end, p_detect, lo, hi, n,"
                        + " method, confidence, computed_at, manifest_id FROM observability_estimate"
                        + " WHERE family = ? AND period_end <= ? AND period_end > ?"
                        + " ORDER BY source_id, region_code, cell, band, method, period_end DESC, computed_at DESC",
                (rs, i) -> new SourceEstimate(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getString(5), instant(rs.getObject(6, OffsetDateTime.class)),
                        instant(rs.getObject(7, OffsetDateTime.class)), rs.getDouble(8), rs.getDouble(9),
                        rs.getDouble(10), rs.getLong(11), rs.getString(12), rs.getString(13),
                        instant(rs.getObject(14, OffsetDateTime.class)), rs.getString(15)),
                family, Timestamp.from(at), Timestamp.from(at.minus(MAX_ESTIMATE_AGE)));
        return new Snapshot(validAt(rows, at));
    }

    /**
     * The estimates valid at {@code at} (OBS-2), pure: periods that ended at or before {@code at} and less than
     * {@link #MAX_ESTIMATE_AGE} before it, the newest per source, place, band and method. The snapshot query applies
     * the same rule in SQL; this is the reference for it.
     */
    static List<SourceEstimate> validAt(List<SourceEstimate> rows, Instant at) {
        Map<String, SourceEstimate> newest = new LinkedHashMap<>();
        for (SourceEstimate row : rows) {
            if (row.periodEnd() == null || row.periodEnd().isAfter(at)
                    || !row.periodEnd().isAfter(at.minus(MAX_ESTIMATE_AGE))) {
                continue;
            }
            String key = row.sourceId() + "|" + row.regionCode() + "|" + row.cell() + "|" + row.band() + "|"
                    + row.method();
            SourceEstimate kept = newest.get(key);
            if (kept == null || row.periodEnd().isAfter(kept.periodEnd())
                    || (row.periodEnd().equals(kept.periodEnd()) && row.computedAt() != null
                            && kept.computedAt() != null && row.computedAt().isAfter(kept.computedAt()))) {
                newest.put(key, row);
            }
        }
        return new ArrayList<>(newest.values());
    }

    private static Instant instant(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }

    /** The estimates of one family valid at one time; pure lookups. */
    static final class Snapshot {

        private final Map<String, List<SourceEstimate>> bySource = new HashMap<>();

        Snapshot(List<SourceEstimate> rows) {
            for (SourceEstimate row : rows) {
                bySource.computeIfAbsent(row.sourceId(), k -> new ArrayList<>()).add(row);
            }
        }

        /** The sources with estimates. */
        Set<String> sources() {
            return new TreeSet<>(bySource.keySet());
        }

        /**
         * The best estimate of a source for a place and band: cell, then region, then everywhere; then method;
         * then newest. A source with cell-level track estimates but none for this cell reports {@code p = 0}.
         */
        SourceEstimate best(String sourceId, String cell, List<String> regions, String band) {
            List<SourceEstimate> rows = bySource.getOrDefault(sourceId, List.of());
            SourceEstimate best = null;
            int bestScore = -1;
            boolean hasCellRows = false;
            String cellMethod = null;
            for (SourceEstimate row : rows) {
                if (row.cell() != null) {
                    hasCellRows = true;
                    cellMethod = cellMethod == null ? row.method() : cellMethod;
                }
                if (!bandMatches(row.band(), band)) {
                    continue;
                }
                int specificity;
                if (row.cell() != null) {
                    if (!row.cell().equals(cell)) {
                        continue;
                    }
                    specificity = 3;
                } else if (row.regionCode() != null) {
                    if (!regions.contains(row.regionCode())) {
                        continue;
                    }
                    specificity = 2;
                } else {
                    specificity = 1;
                }
                int score = specificity * 100 + (METHOD_ORDER.size() - METHOD_ORDER.indexOf(row.method())) * 10
                        + (band != null && band.equals(row.band()) ? 1 : 0);
                if (score > bestScore || (score == bestScore && best != null && row.periodEnd() != null
                        && best.periodEnd() != null && row.periodEnd().isAfter(best.periodEnd()))) {
                    best = row;
                    bestScore = score;
                }
            }
            if (best == null && hasCellRows && cell != null) {
                return new SourceEstimate(sourceId, rows.get(0).family(), null, cell, band, null, null, 0, 0, 0, 0,
                        cellMethod, "low", null, null);
            }
            return best;
        }

        private static boolean bandMatches(String rowBand, String band) {
            return rowBand == null || rowBand.equals(band);
        }
    }

    private static double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }

    private static double round(double value) {
        return Math.round(value * 10_000) / 10_000.0;
    }
}
