package ai.foresignal.raster;

import ai.foresignal.source.spi.FlashSink;
import org.springframework.beans.factory.ObjectProvider;
import ai.foresignal.raster.lightning.FlashStore;
import ai.foresignal.collect.RasterSinkFactory;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.DecodedBands;
import ai.foresignal.source.spi.FrameInput;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.RasterSink;
import ai.foresignal.source.spi.SilentFormatChangeException;
import ai.foresignal.source.spi.SourceDescriptor;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * The raster pipeline behind {@code ctx.rasters()} (Weather-Imagery-Design §2.1, steps ② and ③):
 * decode → quantize → statistics → footprint → {@code .fsg} file → {@code raster_frame} row →
 * {@code raster-frame} record.
 *
 * <p>The heavy part (decoding a downloaded file and quantizing) runs on the {@link RasterDecodePool};
 * the rest runs on the fetch's own thread. Order matters for crash safety: the file is renamed into
 * place, then the row is upserted, then the record is returned to the adapter, which enqueues it with
 * its fetch result. A crash between the row and the enqueue leaves a frame newer than its record
 * head, which the hourly orphan sweep re-emits.
 *
 * <p>After every commit a {@link RasterFrameCommittedEvent} is published (prompt 045 warms tiles from
 * it).
 */
@Component
public class RasterPipeline implements RasterSinkFactory {

    private static final Logger log = LoggerFactory.getLogger(RasterPipeline.class);

    private final ProductRegistry registry;
    private final FrameStore store;
    private final GridStore grids;
    private final RasterFrameQueries frames;
    private final RasterDecodePool pool;
    private final ProductStatusTracker status;
    private final ProductUrls urls;
    private final ApplicationEventPublisher events;
    private final JsonMapper json;
    private final Clock clock;
    private final ObjectProvider<FlashStore> flashes;
    private final RasterPaths paths;

    /**
     * Creates the pipeline.
     *
     * @param flashes the GLM flash store behind {@code ctx.rasters().flashes()} (prompt 048), resolved
     *     lazily; null for none
     */
    public RasterPipeline(ProductRegistry registry, FrameStore store, GridStore grids, RasterFrameQueries frames,
            RasterDecodePool pool, ProductStatusTracker status, ProductUrls urls, ApplicationEventPublisher events,
            JsonMapper json, Clock clock,
            ObjectProvider<FlashStore> flashes, RasterPaths paths) {
        this.registry = registry;
        this.store = store;
        this.grids = grids;
        this.frames = frames;
        this.pool = pool;
        this.status = status;
        this.urls = urls;
        this.events = events;
        this.json = json;
        this.clock = clock;
        this.flashes = flashes;
        this.paths = paths;
    }

    /** The flash store, or throws when none is installed. */
    private FlashStore flashStore() {
        FlashStore store = flashes == null ? null : flashes.getIfAvailable();
        if (store == null) {
            throw new UnsupportedOperationException("No GLM flash store is available in this context.");
        }
        return store;
    }

    @Override
    public RasterSink forSource(SourceDescriptor descriptor, boolean probing) {
        return probing ? new ProbeSink(descriptor.id()) : new PipelineSink(descriptor.id());
    }

    /**
     * Commits one frame for {@code sourceId}; see {@link RasterSink#commit}.
     *
     * @throws AdapterException when the product is unknown or not the source's, or the frame cannot
     *     be decoded or stored
     * @throws SilentFormatChangeException when the decoder found input missing (recorded first)
     */
    public NormalizedRecord commit(String sourceId, FrameInput input) throws AdapterException {
        ProductSpec spec = owned(sourceId, input.productId());
        if (spec.isForecast() != input.isForecast()) {
            throw new AdapterException("Product " + spec.id() + " is " + (spec.isForecast() ? "a forecast" : "not a forecast")
                    + " product, but the frame " + (input.isForecast() ? "has" : "has no") + " run and step");
        }
        EncodedFrame encoded = encode(spec, input);
        String frameId = input.isForecast()
                ? FrameKeys.forecastFrameId(input.referenceTime(), input.forecastStep())
                : FrameKeys.observationFrameId(input.validTime());
        Instant now = clock.instant();
        RasterFrameRow row;
        try {
            String gridId = grids.upsert(input.grid());
            FrameStore.StoredFrame stored = store.write(spec.id(), input.validTime(), frameId, encoded.header(),
                    encoded.decoded());
            row = new RasterFrameRow(spec.id(), frameId, input.validTime(), input.referenceTime(), input.forecastStep(),
                    input.obsStart(), input.obsEnd(), gridId, stored.relativePath(), stored.bytes(), stored.sha256(),
                    spec.dtype().wireValue(), encoded.header().codec(), stats(encoded, input), input.upstreamKey(),
                    input.partial() ? RasterFrameRow.PARTIAL : RasterFrameRow.COMPLETE, false, false, now,
                    FrameThinning.initialExpiry(spec, input.validTime()));
            org.locationtech.jts.geom.MultiPolygon footprint = input.footprint() != null ? input.footprint()
                    : encoded.footprint();
            frames.upsert(row, footprint == null ? null : footprint.toText());
        } catch (UncheckedIOException | org.springframework.dao.DataAccessException e) {
            throw new AdapterException("Could not store frame " + spec.id() + "/" + frameId + ": " + e.getMessage(), e);
        }
        status.clearFormatChange(spec.id());
        events.publishEvent(new RasterFrameCommittedEvent(spec.id(), frameId, row.status()));
        log.debug("Committed frame {}/{} ({} bytes, {})", spec.id(), frameId, row.bytes(), row.status());
        List<Double> bounds = bounds(spec, input.grid());
        if (input.isForecast()) {
            List<RasterFrameRow> run = frames.runFrames(spec.id(), input.referenceTime(), false);
            return run.isEmpty() ? null
                    : RasterFrameRecords.partialRun(spec, input.referenceTime(), run, bounds, urls.storedBase());
        }
        if (input.partial()) {
            return null;
        }
        return RasterFrameRecords.observation(spec, row, bounds, urls.storedBase(), json);
    }

    /**
     * Marks a forecast run complete for {@code sourceId}; see {@link RasterSink#completeRun}.
     *
     * @throws AdapterException when the product is unknown, not the source's, or not a forecast
     */
    public List<NormalizedRecord> completeRun(String sourceId, String productId, Instant run) throws AdapterException {
        ProductSpec spec = owned(sourceId, productId);
        if (!spec.isForecast()) {
            throw new AdapterException("Product " + productId + " is not a forecast product");
        }
        frames.markRunComplete(productId, run);
        List<RasterFrameRow> steps = frames.runFrames(productId, run, false);
        if (steps.isEmpty()) {
            return List.of();
        }
        List<Double> bounds = grids.find(steps.get(0).gridId()).map(g -> bounds(spec, g)).orElse(specBounds(spec));
        return List.of(RasterFrameRecords.forecastRun(spec, run, steps, bounds, urls.storedBase()),
                RasterFrameRecords.partialTombstone(spec, run));
    }

    /**
     * Registers a dynamic product of {@code sourceId}; see {@link RasterSink#registerProduct}.
     *
     * @throws AdapterException when the registry refuses it
     */
    public void registerProduct(String sourceId, ProductSpec spec) throws AdapterException {
        try {
            ProductRegistry.Registered current = registry.find(spec.id()).orElse(null);
            if (current != null && current.dynamic() && current.sourceId().equals(sourceId)
                    && current.spec().equals(spec)) {
                return;
            }
            registry.registerDynamic(sourceId, spec);
        } catch (ai.foresignal.api.error.ForesignalException | IllegalArgumentException e) {
            throw new AdapterException("Product " + spec.id() + " was refused: " + e.getMessage(), e);
        }
    }

    /**
     * Writes a provenance copy below {@code rasters/satdump/}; see {@link RasterSink#storeProvenance}.
     *
     * @throws AdapterException when the path leaves the directory or the write fails
     */
    public void storeProvenance(String relativePath, byte[] content) throws AdapterException {
        Path base = paths.root().resolve("satdump").toAbsolutePath().normalize();
        Path target = base.resolve(relativePath).normalize();
        if (!target.startsWith(base) || target.equals(base)) {
            throw new AdapterException("Provenance path '" + relativePath + "' leaves rasters/satdump/");
        }
        try {
            Files.createDirectories(target.getParent());
            Path staged = Files.createTempFile(paths.tmp(), "provenance-", ".part");
            try {
                Files.write(staged, content);
                Files.move(staged, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                        java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            } finally {
                Files.deleteIfExists(staged);
            }
        } catch (IOException e) {
            throw new AdapterException("Could not write provenance file " + relativePath + ": " + e.getMessage(), e);
        }
    }

    /** Records a silent format change of {@code productId} for {@code sourceId}. */
    public void reportFormatChange(String sourceId, String productId, String reason) {
        registry.find(productId).filter(p -> p.sourceId().equals(sourceId))
                .ifPresent(p -> status.formatChanged(productId, reason, clock.instant()));
    }

    private ProductSpec owned(String sourceId, String productId) throws AdapterException {
        ProductRegistry.Registered product = registry.find(productId)
                .orElseThrow(() -> new AdapterException("Unknown product '" + productId + "'"));
        if (!product.sourceId().equals(sourceId)) {
            throw new AdapterException("Product '" + productId + "' belongs to " + product.sourceId() + ", not " + sourceId);
        }
        if (product.spec().isExternal()) {
            throw new AdapterException("Product '" + productId + "' is external; it has no frames");
        }
        return product.spec();
    }

    private EncodedFrame encode(ProductSpec spec, FrameInput input) throws AdapterException {
        try {
            return pool.run(() -> {
                DecodedBands bands = input.bands() != null ? input.bands() : input.decoder().decode(input.tempFile());
                return FrameEncoder.encode(spec, input.grid(), bands, input.partial());
            });
        } catch (SilentFormatChangeException e) {
            status.formatChanged(spec.id(), e.getMessage(), clock.instant());
            throw e;
        } catch (IllegalArgumentException e) {
            throw new AdapterException("Frame of " + spec.id() + " rejected: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AdapterException("Could not decode frame of " + spec.id() + ": " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AdapterException("Interrupted while decoding a frame of " + spec.id(), e);
        } catch (AdapterException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new AdapterException("Could not encode a frame of " + spec.id() + ": " + e.getMessage(), e);
        } catch (Exception e) {
            throw new AdapterException("Could not decode a frame of " + spec.id() + ": " + e.getMessage(), e);
        } finally {
            if (input.tempFile() != null) {
                try {
                    Files.deleteIfExists(input.tempFile());
                } catch (IOException e) {
                    log.debug("Could not delete {}", input.tempFile(), e);
                }
            }
        }
    }

    /** The computed statistics plus the adapter's extra entries, which never override computed ones. */
    static Map<String, Object> stats(EncodedFrame encoded, FrameInput input) {
        if (input.extraStats().isEmpty()) {
            return encoded.stats();
        }
        Map<String, Object> merged = new LinkedHashMap<>(encoded.stats());
        input.extraStats().forEach(merged::putIfAbsent);
        return merged;
    }

    /** True when {@code productId} is disabled by the decoder self-test (prompt 046). */
    public boolean disabled(String productId) {
        return status.disabled(productId).isPresent();
    }

    /** The product bounds: the spec's, else the grid box. */
    public static List<Double> bounds(ProductSpec spec, GridDefinition grid) {
        List<Double> fromSpec = specBounds(spec);
        return fromSpec != null ? fromSpec : List.of(grid.west(), grid.south(), grid.east(), grid.north());
    }

    private static List<Double> specBounds(ProductSpec spec) {
        return spec.spatial() == null ? null : spec.spatial().bounds();
    }

    /** The sink of one fetch or stream. */
    private final class PipelineSink implements RasterSink {

        private final String sourceId;

        PipelineSink(String sourceId) {
            this.sourceId = sourceId;
        }

        @Override
        public NormalizedRecord commit(FrameInput frame) throws AdapterException {
            return RasterPipeline.this.commit(sourceId, frame);
        }

        @Override
        public List<NormalizedRecord> completeRun(String productId, Instant referenceTime) throws AdapterException {
            return RasterPipeline.this.completeRun(sourceId, productId, referenceTime);
        }

        @Override
        public void reportFormatChange(String productId, String reason) {
            RasterPipeline.this.reportFormatChange(sourceId, productId, reason);
        }

        @Override
        public boolean probing() {
            return false;
        }

        @Override
        public FlashSink flashes() {
            return flashStore();
        }

        @Override
        public boolean disabled(String productId) {
            return RasterPipeline.this.disabled(productId);
        }

        @Override
        public void registerProduct(ProductSpec spec) throws AdapterException {
            RasterPipeline.this.registerProduct(sourceId, spec);
        }

        @Override
        public void storeProvenance(String relativePath, byte[] content) throws AdapterException {
            RasterPipeline.this.storeProvenance(relativePath, content);
        }
    }

    /**
     * The sink of a probe: writes nothing, decodes nothing, and returns a preview record with the
     * grid summary ({@code nx}, {@code ny}, grid type, valid time, variable).
     */
    private final class ProbeSink implements RasterSink {

        private final String sourceId;

        ProbeSink(String sourceId) {
            this.sourceId = sourceId;
        }

        @Override
        public NormalizedRecord commit(FrameInput frame) throws AdapterException {
            owned(sourceId, frame.productId());
            Map<String, Object> grid = new LinkedHashMap<>();
            grid.put("type", frame.grid().type());
            grid.put("nx", frame.grid().nx());
            grid.put("ny", frame.grid().ny());
            grid.put("bounds", List.of(frame.grid().west(), frame.grid().south(), frame.grid().east(),
                    frame.grid().north()));
            if (frame.tempFile() != null) {
                try {
                    Files.deleteIfExists(frame.tempFile());
                } catch (IOException e) {
                    log.debug("Could not delete {}", frame.tempFile(), e);
                }
            }
            return NormalizedRecord.builder(frame.productId(), ProductSpec.RECORD_KIND)
                    .observedAt(frame.isForecast() ? frame.referenceTime() : frame.validTime())
                    .put("productId", frame.productId())
                    .put("probe", true)
                    .put("time", frame.validTime().toString())
                    .put("run", frame.isForecast() ? FrameKeys.runIso(frame.referenceTime()) : null)
                    .put("step", frame.forecastStep())
                    .put("variable", frame.variable())
                    .put("grid", grid)
                    .build();
        }

        @Override
        public List<NormalizedRecord> completeRun(String productId, Instant referenceTime) {
            return List.of();
        }

        @Override
        public void reportFormatChange(String productId, String reason) {
            // A probe writes nothing, health included.
        }

        @Override
        public boolean probing() {
            return true;
        }

        @Override
        public FlashSink flashes() {
            return flashStore().probingView();
        }

        @Override
        public void registerProduct(ProductSpec spec) {
            // A probe registers nothing.
        }

        @Override
        public void storeProvenance(String relativePath, byte[] content) {
            // A probe writes nothing.
        }

        @Override
        public boolean disabled(String productId) {
            return RasterPipeline.this.disabled(productId);
        }
    }
}
