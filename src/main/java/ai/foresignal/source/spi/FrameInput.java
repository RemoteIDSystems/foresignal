package ai.foresignal.source.spi;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;

/**
 * One frame an adapter hands to {@link RasterSink#commit} (Weather-Imagery-Design §2.1).
 *
 * <p>Carries either decoded {@link DecodedBands} or a downloaded temporary file plus the
 * {@link RasterDecoder} that reads it; in the second form the pipeline decodes on the bounded decode
 * pool and deletes the file afterwards.
 *
 * <p>Times: {@code validTime} is the frame's valid time (the scan time for observations, reference
 * time plus step for forecasts). Forecast frames also carry {@code referenceTime} (the run) and
 * {@code forecastStep} in hours.
 *
 * @param productId the product, which the committing source must own
 * @param validTime the valid time
 * @param referenceTime the forecast run's reference time, or null for observations
 * @param forecastStep the forecast step in hours, or null for observations
 * @param obsStart start of the observation period, or null
 * @param obsEnd end of the observation period, or null
 * @param grid the native grid
 * @param bands decoded bands, or null when {@code tempFile} and {@code decoder} are given
 * @param tempFile a downloaded file to decode, or null
 * @param decoder reads {@code tempFile}, or null
 * @param partial true when the frame is incomplete (a scan still filling); partial frames are
 *     excluded from aliases and lists unless asked for, and never produce a record version
 * @param upstreamKey the upstream object key, for provenance, or null
 * @param variable the upstream variable name, for probes and health reasons, or null
 * @param extraStats adapter-supplied entries merged into the frame's {@code stats} (for example
 *     {@code flagMissing}, prompt 046); never overrides the computed ones; empty when none
 */
public record FrameInput(
        String productId,
        Instant validTime,
        Instant referenceTime,
        Integer forecastStep,
        Instant obsStart,
        Instant obsEnd,
        GridDefinition grid,
        DecodedBands bands,
        Path tempFile,
        RasterDecoder decoder,
        boolean partial,
        String upstreamKey,
        String variable,
        java.util.Map<String, Object> extraStats) {

    /** Checks the required parts and that exactly one data form is given. */
    public FrameInput {
        Objects.requireNonNull(productId, "productId");
        Objects.requireNonNull(validTime, "validTime");
        Objects.requireNonNull(grid, "grid");
        boolean hasFile = tempFile != null && decoder != null;
        if ((bands == null) == !hasFile) {
            throw new IllegalArgumentException(
                    "A frame needs either decoded bands or a temp file with its decoder, not both or neither");
        }
        if ((referenceTime == null) != (forecastStep == null)) {
            throw new IllegalArgumentException("A forecast frame needs both referenceTime and forecastStep");
        }
        if (forecastStep != null && forecastStep < 0) {
            throw new IllegalArgumentException("forecastStep must not be negative");
        }
        extraStats = extraStats == null ? java.util.Map.of()
                : java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(extraStats));
    }

    /** A builder for {@code productId}. */
    public static Builder builder(String productId) {
        return new Builder(productId);
    }

    /** True for a forecast step. */
    public boolean isForecast() {
        return referenceTime != null;
    }

    /** Fluent builder. */
    public static final class Builder {

        private final String productId;
        private Instant validTime;
        private Instant referenceTime;
        private Integer forecastStep;
        private Instant obsStart;
        private Instant obsEnd;
        private GridDefinition grid;
        private DecodedBands bands;
        private Path tempFile;
        private RasterDecoder decoder;
        private boolean partial;
        private String upstreamKey;
        private String variable;
        private final java.util.Map<String, Object> extraStats = new java.util.LinkedHashMap<>();

        private Builder(String productId) {
            this.productId = productId;
        }

        /** The valid time of an observation. */
        public Builder validTime(Instant validTime) {
            this.validTime = validTime;
            return this;
        }

        /** A forecast step: run reference time and step hours; the valid time follows from them. */
        public Builder forecast(Instant referenceTime, int stepHours) {
            this.referenceTime = referenceTime;
            this.forecastStep = stepHours;
            this.validTime = referenceTime.plusSeconds(stepHours * 3600L);
            return this;
        }

        /** The observation period. */
        public Builder observationPeriod(Instant start, Instant end) {
            this.obsStart = start;
            this.obsEnd = end;
            return this;
        }

        /** The native grid. */
        public Builder grid(GridDefinition grid) {
            this.grid = grid;
            return this;
        }

        /** Decoded bands. */
        public Builder bands(DecodedBands bands) {
            this.bands = bands;
            return this;
        }

        /** A downloaded file and its decoder. */
        public Builder file(Path tempFile, RasterDecoder decoder) {
            this.tempFile = tempFile;
            this.decoder = decoder;
            return this;
        }

        /** Marks the frame partial. */
        public Builder partial(boolean partial) {
            this.partial = partial;
            return this;
        }

        /** The upstream object key. */
        public Builder upstreamKey(String upstreamKey) {
            this.upstreamKey = upstreamKey;
            return this;
        }

        /** The upstream variable name. */
        public Builder variable(String variable) {
            this.variable = variable;
            return this;
        }

        /** Adds an entry to the frame's {@code stats} (numbers, strings and booleans). */
        public Builder stat(String key, Object value) {
            this.extraStats.put(key, value);
            return this;
        }

        /** Builds the input. */
        public FrameInput build() {
            return new FrameInput(productId, validTime, referenceTime, forecastStep, obsStart, obsEnd, grid, bands,
                    tempFile, decoder, partial, upstreamKey, variable, extraStats);
        }
    }
}
