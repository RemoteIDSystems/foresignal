package ai.foresignal.impact.hazard;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Geometry;

/**
 * A hazard forecast adapted from ingested records (prompt 127 §1), the {@code HazardForecast} schema: the hazard type,
 * the pinned source record, the valid period, the geometry or probability field and the probability model the
 * producer publishes.
 *
 * <p>Only {@link #probabilistic() probabilistic} forecasts feed projections. A cone or a categorical outlook carries
 * {@value #MODEL_NONE} and is context only, never a probability.
 *
 * @param hazardType one of {@link #TYPES}
 * @param hazardKey the hazard's identity across versions (debounce, supersession and question dedup), for example
 *     {@code nhc-wind-probs:34} or {@code spc-outlooks:day1:torn}
 * @param sourceRef the pinned reference of the newest record behind it, {@code record:{sourceId}/{externalId}@{seq}}
 * @param sourceRefs every pinned record behind it
 * @param issuer {@code official} for an agency product, {@code foresignal} for a derived one
 * @param issuedAt the product's issue time
 * @param validFrom the start of the valid period
 * @param validTo the end of the valid period
 * @param geometry the hazard's overall area (the union of the bands, the cone, the gauge point), or null
 * @param probabilityField the published probability bands, highest first (empty for a non-probabilistic model)
 * @param probabilityModel one of {@link #MODELS}
 * @param category the producer's category (ERO {@code MDT}, a flood category), or null
 * @param probabilityRange the published range of the category, or null
 * @param pointRadiusKm the radius a point probability applies within (SPC and WPC: 25 miles), or null
 * @param note a caveat for the reader (for a cone: "the cone covers about two thirds of historical track errors")
 * @param details source-specific facts kept with the forecast (the gauge's basis, the calibration sample)
 */
public record HazardForecast(String hazardType, String hazardKey, String sourceRef, List<String> sourceRefs,
        String issuer, Instant issuedAt, Instant validFrom, Instant validTo, Geometry geometry,
        List<Band> probabilityField, String probabilityModel, String category, Range probabilityRange,
        Double pointRadiusKm, String note, Map<String, Object> details) {

    /** NHC 34-kt cumulative wind-speed probabilities. */
    public static final String TROPICAL_WIND_34 = "tropical-wind-34";

    /** NHC 50-kt cumulative wind-speed probabilities. */
    public static final String TROPICAL_WIND_50 = "tropical-wind-50";

    /** NHC 64-kt cumulative wind-speed probabilities. */
    public static final String TROPICAL_WIND_64 = "tropical-wind-64";

    /** An NHC forecast cone (context only). */
    public static final String TROPICAL_TRACK = "tropical-track";

    /** SPC probabilistic tornado outlook. */
    public static final String TORNADO = "tornado";

    /** SPC probabilistic damaging-wind outlook. */
    public static final String WIND = "wind";

    /** SPC probabilistic hail outlook. */
    public static final String HAIL = "hail";

    /** SPC categorical outlook (context only). */
    public static final String SEVERE_CATEGORICAL = "severe-categorical";

    /** WPC excessive rainfall outlook. */
    public static final String EXCESSIVE_RAIN = "excessive-rain";

    /** NWPS river gauge flood category. */
    public static final String RIVER_FLOOD = "river-flood";

    /** A storm cell from the 105 tracker. */
    public static final String STORM_CELL = "storm-cell";

    /** Every hazard type, in the spec's order. */
    public static final List<String> TYPES = List.of(TROPICAL_WIND_34, TROPICAL_WIND_50, TROPICAL_WIND_64,
            TROPICAL_TRACK, TORNADO, WIND, HAIL, SEVERE_CATEGORICAL, EXCESSIVE_RAIN, RIVER_FLOOD, STORM_CELL);

    /** Published probability polygons (NHC wind-speed probabilities). */
    public static final String MODEL_POLYGONS = "published-polygons";

    /** A published point probability (SPC: within 25 miles of a point). */
    public static final String MODEL_POINT = "published-point";

    /** A published range per category (WPC ERO). */
    public static final String MODEL_CATEGORY_RANGE = "published-category-range";

    /** A gauge's forecast (or current) category, calibrated by the gauge's history. */
    public static final String MODEL_GAUGE = "gauge-category";

    /** An extrapolated track with the tracker's position error. */
    public static final String MODEL_TRACK_ERROR = "track-error-ellipse";

    /** No probability: context only (cones, categorical outlooks). */
    public static final String MODEL_NONE = "none";

    /** Every probability model, in the spec's order. */
    public static final List<String> MODELS = List.of(MODEL_POLYGONS, MODEL_POINT, MODEL_CATEGORY_RANGE, MODEL_GAUGE,
            MODEL_TRACK_ERROR, MODEL_NONE);

    /** The cone caveat. */
    public static final String CONE_NOTE = "The cone covers about two thirds of historical track errors; it is not a"
            + " probability of impact and is used only for context and timing.";

    /** The hazard types whose forecasts may carry probabilities. */
    public static final Set<String> PROBABILISTIC_TYPES = Set.of(TROPICAL_WIND_34, TROPICAL_WIND_50,
            TROPICAL_WIND_64, TORNADO, WIND, HAIL, EXCESSIVE_RAIN, RIVER_FLOOD, STORM_CELL);

    /**
     * One published probability band.
     *
     * @param band the band as published ({@code 40-50%}, {@code 0.10}, {@code MDT})
     * @param pMin the probability used (the band's lower bound)
     * @param pMax the band's upper bound, or null when open
     * @param geometry the band's area
     */
    public record Band(String band, double pMin, Double pMax, Geometry geometry) {}

    /**
     * A published category range.
     *
     * @param low the lower bound (the probability used)
     * @param high the upper bound, or null when open
     */
    public record Range(double low, Double high) {}

    /** Copies the collections. */
    public HazardForecast {
        sourceRefs = sourceRefs == null ? List.of() : List.copyOf(sourceRefs);
        probabilityField = probabilityField == null ? List.of() : List.copyOf(probabilityField);
        details = details == null ? Map.of() : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(details));
        issuer = issuer == null ? "official" : issuer;
    }

    /** True when the forecast has a published (or calibrated) probability to combine. */
    public boolean probabilistic() {
        return !MODEL_NONE.equals(probabilityModel) && !probabilityField.isEmpty()
                && probabilityField.stream().anyMatch(b -> b.pMin() > 0);
    }

    /**
     * The {@code HazardForecast} view. Band geometries are included only when {@code withGeometry}.
     *
     * @param withGeometry include band and overall geometries (as GeoJSON maps)
     * @param geoJson writes a geometry as a GeoJSON map
     */
    public Map<String, Object> view(boolean withGeometry, java.util.function.Function<Geometry, Object> geoJson) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("hazardType", hazardType);
        out.put("hazardKey", hazardKey);
        out.put("sourceRef", sourceRef);
        out.put("sourceRefs", sourceRefs);
        out.put("issuer", issuer);
        out.put("issuedAt", issuedAt == null ? null : issuedAt.toString());
        out.put("validFrom", validFrom == null ? null : validFrom.toString());
        out.put("validTo", validTo == null ? null : validTo.toString());
        out.put("geometry", withGeometry && geometry != null ? geoJson.apply(geometry) : null);
        List<Map<String, Object>> bands = new ArrayList<>();
        for (Band b : probabilityField) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("band", b.band());
            m.put("pMin", b.pMin());
            m.put("pMax", b.pMax());
            if (withGeometry && b.geometry() != null) {
                m.put("geometry", geoJson.apply(b.geometry()));
            }
            bands.add(m);
        }
        out.put("probabilityField", bands);
        out.put("probabilityModel", probabilityModel);
        out.put("category", category);
        if (probabilityRange == null) {
            out.put("probabilityRange", null);
        } else {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("low", probabilityRange.low());
            r.put("high", probabilityRange.high());
            out.put("probabilityRange", r);
        }
        out.put("pointRadiusKm", pointRadiusKm);
        out.put("note", note);
        out.put("details", details);
        return out;
    }
}
