package ai.foresignal.source.spi;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A raster, field or external product a source publishes (Weather-Imagery-Design §2.2, §2.3).
 *
 * <p>{@code kind} is the <em>product</em> kind ({@value #KIND_RASTER}, {@value #KIND_FIELD} or
 * {@value #KIND_EXTERNAL}). The <em>record</em> kind every frame is published as is always
 * {@value #RECORD_KIND}, so the owning source must list {@value #RECORD_KIND} in its kinds; the
 * {@code ProductRegistry} checks that and every other rule at startup.
 *
 * <p>Physical value = {@code code × scale + offset}. {@code nodata} is the raw code that marks a
 * missing sample; {@code codes} names categorical and special raw codes (for MRMS reflectivity
 * {@code {0: "noCoverage", 1: "noEcho"}}).
 *
 * @param id stable kebab-case identifier, unique across sources ({@code timeline} and {@code loop}
 *     are reserved)
 * @param title shown to operators and clients
 * @param kind the product kind: {@value #KIND_RASTER}, {@value #KIND_FIELD} or {@value #KIND_EXTERNAL}
 * @param category radar, satellite, model, lightning or imagery
 * @param units physical units, or null
 * @param dtype the stored sample type, or null for an external product
 * @param scale code-to-physical scale; 1 when absent
 * @param offset code-to-physical offset; 0 when absent
 * @param nodata the raw code of a missing sample, or null when every code is a value
 * @param codes categorical and special raw codes to their meaning, ordered by code
 * @param bands band names, in file order; empty means one unnamed band
 * @param styles the versioned styles valid for this product ({@code name@v})
 * @param defaultStyle the versioned default style
 * @param defaultOpacity suggested layer opacity, 0 to 1, or null
 * @param temporal frame type, cadence and gap tolerance
 * @param spatial coverage and zoom range
 * @param retention the frame retention ladder
 * @param attribution the structured attribution clients must render
 * @param coverageMeaning a one-line meaning of the coverage, or null for the default text
 * @param legendSummary a short legend text, or null
 * @param resample how tiles sample the native grid: {@value #RESAMPLE_NEAREST} (default; codes and
 *     categories) or {@value #RESAMPLE_BILINEAR} (continuous model fields, prompt 045)
 */
public record ProductSpec(
        String id,
        String title,
        String kind,
        String category,
        String units,
        RasterDtype dtype,
        double scale,
        double offset,
        Double nodata,
        Map<Integer, String> codes,
        List<String> bands,
        List<String> styles,
        String defaultStyle,
        Double defaultOpacity,
        Temporal temporal,
        Spatial spatial,
        RetentionLadder retention,
        Attribution attribution,
        String coverageMeaning,
        String legendSummary,
        String resample) {

    /** The record kind every frame of every product is published as. */
    public static final String RECORD_KIND = "raster-frame";

    /** Rendered tiles from native-grid frames. */
    public static final String KIND_RASTER = "raster";

    /** A model vector or scalar field (also tiles). */
    public static final String KIND_FIELD = "field";

    /** A client-direct catalog entry Foresignal never fetches. */
    public static final String KIND_EXTERNAL = "external";

    /** Every product kind. */
    public static final Set<String> KINDS = Set.of(KIND_RASTER, KIND_FIELD, KIND_EXTERNAL);

    /** Every category. */
    public static final Set<String> CATEGORIES = Set.of("radar", "satellite", "model", "lightning", "imagery");

    /** Nearest-cell sampling (the default). */
    public static final String RESAMPLE_NEAREST = "nearest";

    /** Bilinear sampling of physical values, for continuous fields. */
    public static final String RESAMPLE_BILINEAR = "bilinear";

    /** Every resampling mode. */
    public static final Set<String> RESAMPLES = Set.of(RESAMPLE_NEAREST, RESAMPLE_BILINEAR);

    /** Ids that would collide with fixed routes under {@code /v1/products}. */
    public static final Set<String> RESERVED_IDS = Set.of("timeline", "loop");

    /** Normalizes the collections and applies the scale and offset defaults. */
    public ProductSpec {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ProductSpec id is required");
        }
        scale = scale == 0 || Double.isNaN(scale) ? 1.0 : scale;
        offset = Double.isNaN(offset) ? 0.0 : offset;
        codes = codes == null ? Map.of() : Collections.unmodifiableMap(new TreeMap<>(codes));
        bands = bands == null ? List.of() : List.copyOf(bands);
        styles = styles == null ? List.of() : List.copyOf(styles);
        resample = resample == null || resample.isBlank() ? RESAMPLE_NEAREST : resample;
    }

    /**
     * A minimal spec: just an id, a title and a product kind. Enough for a descriptor test; the
     * registry rejects it for lack of a category, temporal and spatial facts.
     */
    public ProductSpec(String id, String title, String kind) {
        this(id, title, kind, null, null, null, 1.0, 0.0, null, null, null, null, null, null, null, null, null,
                null, null, null, null);
    }

    /** A builder pre-filled with {@code id}. */
    public static Builder builder(String id) {
        return new Builder(id);
    }

    /** True for a forecast product (one record version per completed run). */
    public boolean isForecast() {
        return temporal != null && Temporal.FORECAST.equals(temporal.type());
    }

    /** True for an external, client-direct catalog entry, which never has frames. */
    public boolean isExternal() {
        return KIND_EXTERNAL.equals(kind);
    }

    /** True when tiles sample bilinearly. */
    public boolean bilinear() {
        return RESAMPLE_BILINEAR.equals(resample);
    }

    /** The number of bands each frame holds (at least 1). */
    public int bandCount() {
        return bands.isEmpty() ? 1 : bands.size();
    }

    /** The physical value of raw code {@code code}. */
    public double physical(double code) {
        return code * scale + offset;
    }

    /**
     * Frame type, cadence and gap tolerance.
     *
     * @param type {@value #OBSERVATION}, {@value #FORECAST} or {@value #PASS}
     * @param cadence nominal interval between frames (forecasts: between steps)
     * @param maxGap the largest gap at which a frame still counts as current; at least
     *     {@code cadence}
     */
    public record Temporal(String type, Duration cadence, Duration maxGap) {

        /** One frame per scan. */
        public static final String OBSERVATION = "observation";

        /** Runs of forecast steps. */
        public static final String FORECAST = "forecast";

        /** One frame per satellite pass. */
        public static final String PASS = "pass";

        /** Every type. */
        public static final Set<String> TYPES = Set.of(OBSERVATION, FORECAST, PASS);

        /** An observation product with {@code cadence} and {@code maxGap}. */
        public static Temporal observation(Duration cadence, Duration maxGap) {
            return new Temporal(OBSERVATION, cadence, maxGap);
        }

        /** A forecast product whose steps are {@code cadence} apart. */
        public static Temporal forecast(Duration cadence, Duration maxGap) {
            return new Temporal(FORECAST, cadence, maxGap);
        }
    }

    /**
     * Coverage and zoom range.
     *
     * @param coverage CONUS, global, station-disc or swath
     * @param nativeMaxZoom the native max zoom (clients overzoom above it)
     * @param minZoom the smallest zoom tiles are rendered at
     * @param maxZoomForAnimation the highest zoom warmed for animation, or null
     * @param bounds {@code [west, south, east, north]} in WGS84 degrees, or null to use the newest
     *     frame's grid
     */
    public record Spatial(String coverage, Integer nativeMaxZoom, Integer minZoom, Integer maxZoomForAnimation,
            List<Double> bounds) {

        /** Every coverage value. */
        public static final Set<String> COVERAGES = Set.of("CONUS", "global", "station-disc", "swath");

        /** Copies the bounds. */
        public Spatial {
            bounds = bounds == null ? null : List.copyOf(bounds);
        }

        /** Coverage and zoom range, bounds from the frames. */
        public Spatial(String coverage, Integer nativeMaxZoom, Integer minZoom) {
            this(coverage, nativeMaxZoom, minZoom, null, null);
        }
    }

    /**
     * The frame retention ladder (Weather-Imagery-Design §2.4).
     *
     * <p>Observation products: every frame is kept until the first rung's {@code olderThan}; past
     * each rung only the frame nearest each {@code keepEvery} boundary survives; nothing survives
     * {@code maxAge}. Forecast products keep the newest {@code runs} complete runs.
     *
     * @param rungs thinning rungs, ascending by {@code olderThan}
     * @param maxAge frames older than this are deleted; null keeps them (only the disk quota evicts)
     * @param runs complete forecast runs kept (forecasts; 2 when null)
     */
    public record RetentionLadder(List<Rung> rungs, Duration maxAge, Integer runs) {

        /** Complete runs a forecast product keeps when it does not say: current plus previous. */
        public static final int DEFAULT_RUNS = 2;

        /** Copies the rungs. */
        public RetentionLadder {
            rungs = rungs == null ? List.of() : List.copyOf(rungs);
        }

        /** Keeps frames for {@code maxAge}, unthinned. */
        public static RetentionLadder maxAge(Duration maxAge) {
            return new RetentionLadder(List.of(), maxAge, null);
        }

        /** Keeps the newest {@code runs} complete forecast runs. */
        public static RetentionLadder runs(int runs) {
            return new RetentionLadder(List.of(), null, runs);
        }

        /** The forecast runs kept. */
        public int runsKept() {
            return runs == null || runs < 1 ? DEFAULT_RUNS : runs;
        }

        /** A one-line description, for example "every frame for 6h, then 10m frames to 48h". */
        public String summary() {
            if (runs != null && rungs.isEmpty() && maxAge == null) {
                return "newest " + runsKept() + " complete runs";
            }
            List<String> parts = new ArrayList<>();
            if (!rungs.isEmpty()) {
                parts.add("every frame for " + shorthand(rungs.get(0).olderThan()));
                for (int i = 0; i < rungs.size(); i++) {
                    Duration until = i + 1 < rungs.size() ? rungs.get(i + 1).olderThan() : maxAge;
                    parts.add(shorthand(rungs.get(i).keepEvery()) + " frames"
                            + (until == null ? " thereafter" : " to " + shorthand(until)));
                }
                return String.join(", then ", parts);
            }
            return maxAge == null ? "every frame (disk quota only)" : "every frame for " + shorthand(maxAge);
        }

        private static String shorthand(Duration d) {
            long s = d.toSeconds();
            if (s % 86_400 == 0) {
                return s / 86_400 + "d";
            }
            if (s % 3600 == 0) {
                return s / 3600 + "h";
            }
            if (s % 60 == 0) {
                return s / 60 + "m";
            }
            return s + "s";
        }

        /**
         * One thinning rung: frames older than {@code olderThan} are kept every {@code keepEvery}.
         *
         * @param olderThan the age past which the rung applies
         * @param keepEvery the spacing of the frames kept
         */
        public record Rung(Duration olderThan, Duration keepEvery) {}
    }

    /**
     * The structured attribution clients must render (Weather-Imagery-Design §6.10).
     *
     * @param text plain-text attribution line
     * @param html HTML form, or null
     * @param url where to read about the data, or null
     * @param licence license identifier (the wire field is spelled {@code licence})
     * @param licenceUrl the license text, or null
     * @param requiredDisplay true when the license requires the attribution on the map or image
     */
    public record Attribution(String text, String html, String url, String licence, String licenceUrl,
            boolean requiredDisplay) {}

    /**
     * Fluent builder; mutable and not thread-safe, used once in an adapter's constructor.
     */
    public static final class Builder {

        private final String id;
        private String title;
        private String kind = KIND_RASTER;
        private String category;
        private String units;
        private RasterDtype dtype;
        private double scale = 1.0;
        private double offset;
        private Double nodata;
        private final Map<Integer, String> codes = new TreeMap<>();
        private final List<String> bands = new ArrayList<>();
        private final List<String> styles = new ArrayList<>();
        private String defaultStyle;
        private Double defaultOpacity;
        private Temporal temporal;
        private Spatial spatial;
        private RetentionLadder retention;
        private Attribution attribution;
        private String coverageMeaning;
        private String legendSummary;
        private String resample;

        private Builder(String id) {
            this.id = id;
            this.title = id;
        }

        /** The title. */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /** The product kind (raster by default). */
        public Builder kind(String kind) {
            this.kind = kind;
            return this;
        }

        /** The category. */
        public Builder category(String category) {
            this.category = category;
            return this;
        }

        /** The physical units. */
        public Builder units(String units) {
            this.units = units;
            return this;
        }

        /** The stored sample type. */
        public Builder dtype(RasterDtype dtype) {
            this.dtype = dtype;
            return this;
        }

        /** Code-to-physical scale and offset. */
        public Builder scaleOffset(double scale, double offset) {
            this.scale = scale;
            this.offset = offset;
            return this;
        }

        /** The raw code of a missing sample. */
        public Builder nodata(Double nodata) {
            this.nodata = nodata;
            return this;
        }

        /** Names one categorical or special raw code. */
        public Builder code(int code, String meaning) {
            this.codes.put(code, meaning);
            return this;
        }

        /** The band names, in file order. */
        public Builder bands(String... names) {
            this.bands.addAll(List.of(names));
            return this;
        }

        /** The default style, also added to the style list. */
        public Builder defaultStyle(String style) {
            this.defaultStyle = style;
            if (!styles.contains(style)) {
                styles.add(0, style);
            }
            return this;
        }

        /** Adds a valid style. */
        public Builder style(String style) {
            if (!styles.contains(style)) {
                styles.add(style);
            }
            return this;
        }

        /** The suggested layer opacity. */
        public Builder defaultOpacity(Double opacity) {
            this.defaultOpacity = opacity;
            return this;
        }

        /** Frame type, cadence and gap tolerance. */
        public Builder temporal(Temporal temporal) {
            this.temporal = temporal;
            return this;
        }

        /** Coverage and zoom range. */
        public Builder spatial(Spatial spatial) {
            this.spatial = spatial;
            return this;
        }

        /** The retention ladder. */
        public Builder retention(RetentionLadder retention) {
            this.retention = retention;
            return this;
        }

        /** The attribution. */
        public Builder attribution(Attribution attribution) {
            this.attribution = attribution;
            return this;
        }

        /** The one-line meaning of the coverage. */
        public Builder coverageMeaning(String coverageMeaning) {
            this.coverageMeaning = coverageMeaning;
            return this;
        }

        /** A short legend text. */
        public Builder legendSummary(String legendSummary) {
            this.legendSummary = legendSummary;
            return this;
        }

        /** The tile resampling mode ({@value ProductSpec#RESAMPLE_NEAREST} by default). */
        public Builder resample(String resample) {
            this.resample = resample;
            return this;
        }

        /** Builds the spec. */
        public ProductSpec build() {
            return new ProductSpec(id, title, kind, category, units, dtype, scale, offset, nodata, codes, bands,
                    styles, defaultStyle, defaultOpacity, temporal, spatial, retention, attribution, coverageMeaning,
                    legendSummary, resample);
        }
    }
}
