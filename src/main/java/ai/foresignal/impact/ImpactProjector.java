package ai.foresignal.impact;

import ai.foresignal.impact.hazard.HazardForecast;
import ai.foresignal.impact.hazard.HazardProbability;
import ai.foresignal.impact.rate.ImpactRateService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The impact projection {@code impact-projection} 1.0.0 (prompt 127 §4, Intelligence-Design §10). Pure.
 *
 * <p>For a probabilistic hazard forecast, every admin1 region it touches and every applicable impact pair:
 *
 * <pre>P(impact) = P(hazard in the region) × P(impact | hazard class)</pre>
 *
 * <ul>
 *   <li>{@code P(hazard in the region)} is the highest published band intersecting the region, or at an exposed asset
 *       in it (published polygons count within {@code assetBufferKm} of an asset); the interval is the band's bounds.
 *   <li>{@code P(impact | hazard class)} is the historical rate of the pair in the region's class
 *       ({@link ImpactRateService}); a pair without a usable rate (n &lt; 20, or no region class) is skipped and listed,
 *       never guessed.
 *   <li>The interval is the product of the interval bounds.
 *   <li>Projections below {@code minProbability} are dropped; one projection is kept per impact family and region (the
 *       highest), and the top {@value #TOP} per hazard by probability.
 * </ul>
 *
 * A forecast without a probability (a cone, a categorical outlook, an uncalibrated gauge) projects nothing.
 */
public final class ImpactProjector {

    /** The component name. */
    public static final String NAME = "impact-projection";

    /** The component version. */
    public static final String VERSION = "1.0.0";

    /** The projections kept per hazard. */
    public static final int TOP = 20;

    /** The label every output carries. */
    public static final String LABEL = "modelled; heuristic links may be wrong; not an official warning";

    private ImpactProjector() {}

    /**
     * The projector's settings.
     *
     * @param minProbability projections below this are dropped ({@code impact.minProbability}, 0.05)
     * @param questionMinProbability projections at or above this open ledger questions
     *     ({@code impact.questionMinProbability}, 0.10)
     * @param assetBufferKm the buffer around an asset for published polygons ({@code impact.assetBufferKm}, 5)
     */
    public record Settings(double minProbability, double questionMinProbability, double assetBufferKm) {

        /** The defaults. */
        public static final Settings DEFAULTS = new Settings(0.05, 0.10, 5.0);
    }

    /** Looks up a conditional rate. */
    @FunctionalInterface
    public interface Rates {

        /** The rate, or null when no built-in pair or region class exists. */
        ImpactRateService.ImpactRate rate(String hazardClass, String impactFamily, String regionClass);
    }

    /**
     * An exposed asset with its hazard probability.
     *
     * @param asset the asset
     * @param pHazard the probability of the hazard at it
     * @param band the band it falls in
     * @param distanceKm its distance from that band (0 inside)
     */
    public record ExposedAsset(ImpactExposure.Asset asset, double pHazard, String band, double distanceKm) {}

    /**
     * One projection: an impact family in an admin1 region.
     *
     * @param pair the impact pair
     * @param family the impact family
     * @param admin1 the region
     * @param regionClass the region's class
     * @param pHazard the hazard probability in the region
     * @param pHazardLower its lower bound
     * @param pHazardUpper its upper bound
     * @param band the band giving it
     * @param rate the conditional rate
     * @param pImpact the product
     * @param lower the interval's lower bound
     * @param upper the interval's upper bound
     * @param assets the exposed assets in the region, highest probability first
     * @param paths the dependency paths from those assets
     */
    public record Projection(ImpactPairs.Pair pair, ImpactPairs.ImpactFamily family, ImpactExposure.Admin1 admin1,
            String regionClass, double pHazard, double pHazardLower, double pHazardUpper, String band,
            ImpactRateService.ImpactRate rate, double pImpact, double lower, double upper, List<ExposedAsset> assets,
            List<ImpactExposure.Path> paths) {}

    /**
     * A pair skipped for a region class.
     *
     * @param impactFamily the family
     * @param regionClass the region class, or null when unknown
     * @param n the hazard occurrences, or null
     * @param reason {@code insufficient-history}, {@code no-region-class} or {@code no-rate}
     */
    public record Skipped(String impactFamily, String regionClass, Long n, String reason) {

        /** The view. */
        public Map<String, Object> view() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("impactFamily", impactFamily);
            out.put("regionClass", regionClass);
            out.put("n", n);
            out.put("reason", reason);
            return out;
        }
    }

    /**
     * The result of one run.
     *
     * @param projections the kept projections, highest probability first
     * @param skipped the pairs skipped, once per family and region class
     * @param belowThreshold projections computed but dropped below {@code minProbability}
     */
    public record Result(List<Projection> projections, List<Skipped> skipped, int belowThreshold) {}

    /**
     * Projects one hazard forecast.
     *
     * @param forecast the forecast
     * @param regions the admin1 regions it touches
     * @param assets the exposed assets (facilities and places)
     * @param paths the facilities' dependency paths
     * @param pairs the pairs applicable to the forecast's hazard type
     * @param pairsLookup the pairs document (families)
     * @param regionClassOf the region class of an admin1 code, or null
     * @param rates the rate lookup
     * @param settings the settings
     */
    public static Result project(HazardForecast forecast, List<ImpactExposure.Admin1> regions,
            List<ImpactExposure.Asset> assets, List<ImpactExposure.Path> paths, List<ImpactPairs.Pair> pairs,
            ImpactPairs pairsLookup, Function<String, String> regionClassOf, Rates rates, Settings settings) {
        List<Skipped> skipped = new ArrayList<>();
        if (forecast == null || !forecast.probabilistic() || pairs.isEmpty()) {
            return new Result(List.of(), skipped, 0);
        }
        Map<String, List<ExposedAsset>> assetsByRegion = new LinkedHashMap<>();
        for (ImpactExposure.Asset asset : assets) {
            HazardProbability.At at = HazardProbability.at(forecast, asset.geometry(), settings.assetBufferKm())
                    .orElse(null);
            if (at == null) {
                continue;
            }
            ImpactExposure.Admin1 region = regionOf(asset, regions);
            if (region == null) {
                continue;
            }
            double distance = at.band().geometry().intersects(asset.geometry()) ? 0
                    : HazardProbability.distanceKm(at.band().geometry(), asset.geometry());
            assetsByRegion.computeIfAbsent(region.code(), k -> new ArrayList<>())
                    .add(new ExposedAsset(asset, at.pHazard(), at.band().band(), round(distance)));
        }
        List<Projection> all = new ArrayList<>();
        Map<String, Skipped> skippedByKey = new LinkedHashMap<>();
        int below = 0;
        for (ImpactExposure.Admin1 region : regions) {
            HazardProbability.At best = HazardProbability.inArea(forecast, region.geometry()).orElse(null);
            List<ExposedAsset> regionAssets = new ArrayList<>(assetsByRegion.getOrDefault(region.code(), List.of()));
            regionAssets.sort(Comparator.comparingDouble(ExposedAsset::pHazard).reversed()
                    .thenComparing(a -> a.asset().ref()));
            for (ExposedAsset a : regionAssets) {
                if (best == null || a.pHazard() > best.pHazard()) {
                    best = HazardProbability.at(forecast, a.asset().geometry(), settings.assetBufferKm())
                            .orElse(best);
                }
            }
            if (best == null || best.pHazard() <= 0) {
                continue;
            }
            String regionClass = regionClassOf.apply(region.code());
            for (ImpactPairs.Pair pair : pairs) {
                ImpactPairs.ImpactFamily family = pairsLookup.family(pair.impactFamily());
                if (regionClass == null) {
                    skippedByKey.putIfAbsent(pair.impactFamily() + "|null",
                            new Skipped(pair.impactFamily(), null, null, "no-region-class"));
                    continue;
                }
                ImpactRateService.ImpactRate rate = rates.rate(pair.hazardClass(), pair.impactFamily(), regionClass);
                if (rate == null || !rate.usable()) {
                    skippedByKey.putIfAbsent(pair.impactFamily() + "|" + regionClass, new Skipped(pair.impactFamily(),
                            regionClass, rate == null ? null : rate.n(), rate == null ? "no-rate"
                                    : "insufficient-history"));
                    continue;
                }
                double pImpact = best.pHazard() * rate.rate();
                double lower = best.lower() * rate.lower();
                double upper = Math.min(1.0, best.upper() * rate.upper());
                if (pImpact < settings.minProbability()) {
                    below++;
                    continue;
                }
                List<ImpactExposure.Path> regionPaths = new ArrayList<>();
                for (ImpactExposure.Path path : paths) {
                    if (regionAssets.stream().anyMatch(a -> a.asset().ref().equals(path.from()))) {
                        regionPaths.add(path);
                    }
                }
                all.add(new Projection(pair, family, region, regionClass, best.pHazard(), best.lower(), best.upper(),
                        best.band().band(), rate, round(pImpact), round(lower), round(upper), regionAssets,
                        regionPaths));
            }
        }
        // One projection per (family, region): the highest.
        Map<String, Projection> byKey = new LinkedHashMap<>();
        for (Projection p : all) {
            String key = p.family().id() + "|" + p.admin1().code();
            Projection existing = byKey.get(key);
            if (existing == null || p.pImpact() > existing.pImpact()) {
                byKey.put(key, p);
            }
        }
        List<Projection> kept = new ArrayList<>(byKey.values());
        kept.sort(Comparator.comparingDouble(Projection::pImpact).reversed()
                .thenComparing(p -> p.admin1().code()).thenComparing(p -> p.family().id()));
        if (kept.size() > TOP) {
            kept = new ArrayList<>(kept.subList(0, TOP));
        }
        skipped.addAll(skippedByKey.values());
        return new Result(kept, skipped, below);
    }

    /** The region containing an asset's representative point, else the first region it intersects, or null. */
    static ImpactExposure.Admin1 regionOf(ImpactExposure.Asset asset, List<ImpactExposure.Admin1> regions) {
        org.locationtech.jts.geom.Point p = HazardProbability.representative(asset.geometry());
        for (ImpactExposure.Admin1 r : regions) {
            if (r.geometry().covers(p)) {
                return r;
            }
        }
        for (ImpactExposure.Admin1 r : regions) {
            if (r.geometry().intersects(asset.geometry())) {
                return r;
            }
        }
        return null;
    }

    /** Six significant digits. */
    static double round(double v) {
        if (!Double.isFinite(v) || v == 0) {
            return v;
        }
        return new java.math.BigDecimal(v).round(new java.math.MathContext(6)).doubleValue();
    }
}
