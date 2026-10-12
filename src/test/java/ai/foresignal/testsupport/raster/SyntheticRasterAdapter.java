package ai.foresignal.testsupport.raster;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.RasterDtype;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * A test-only product source (prompt 042): no upstream, a latlon grid over Texas, and two products,
 * {@value #OBS} (observation, u8 codes like MRMS reflectivity) and {@value #FCST} (forecast, i16).
 *
 * <p>Each fetch runs the next scripted step, which commits frames through {@code ctx.rasters()} and
 * returns the records the pipeline hands back, exactly as a real product adapter would; with nothing
 * scripted a fetch returns an empty result. Lives under {@code testsupport}, so the built-in adapter
 * classification of {@code FamilyDefaultsTest} never sees it.
 */
public class SyntheticRasterAdapter implements PollingAdapter {

    /** The source id. */
    public static final String SOURCE = "test-synth";

    /** The observation product. */
    public static final String OBS = "test-synth-obs";

    /** The forecast product. */
    public static final String FCST = "test-synth-fcst";

    /** One scripted fetch. */
    @FunctionalInterface
    public interface Step {

        /** Commits frames and returns the fetch result. */
        FetchResult run(FetchContext ctx) throws AdapterException;
    }

    private final Deque<Step> script = new ArrayDeque<>();
    private final java.util.concurrent.atomic.AtomicInteger fetches = new java.util.concurrent.atomic.AtomicInteger();

    /** The observation product's spec. */
    public static ProductSpec observationSpec() {
        return ProductSpec.builder(OBS)
                .title("Synthetic reflectivity")
                .category("radar")
                .units("dBZ")
                .dtype(RasterDtype.U8)
                .scaleOffset(0.5, -33.0)
                .nodata(0.0)
                .code(0, "noCoverage")
                .code(1, "noEcho")
                .defaultStyle("nws-reflectivity@1")
                .temporal(ProductSpec.Temporal.observation(Duration.ofMinutes(2), Duration.ofMinutes(6)))
                .spatial(new ProductSpec.Spatial("CONUS", 8, 0, 7, null))
                .retention(new ProductSpec.RetentionLadder(
                        List.of(new ProductSpec.RetentionLadder.Rung(Duration.ofHours(6), Duration.ofMinutes(10))),
                        Duration.ofHours(48), null))
                .attribution(new ProductSpec.Attribution("Synthetic test data", null, null, "US-PD", null, false))
                .build();
    }

    /** The forecast product's spec. */
    public static ProductSpec forecastSpec() {
        return ProductSpec.builder(FCST)
                .title("Synthetic gusts")
                .category("model")
                .units("m/s")
                .dtype(RasterDtype.I16)
                .scaleOffset(0.01, 0.0)
                .nodata(-32768.0)
                .defaultStyle("wind-speed@1")
                .temporal(ProductSpec.Temporal.forecast(Duration.ofHours(1), Duration.ofHours(3)))
                .spatial(new ProductSpec.Spatial("CONUS", 6, 0))
                .retention(ProductSpec.RetentionLadder.runs(2))
                .attribution(new ProductSpec.Attribution("Synthetic model", null, null, "US-PD", null, false))
                .build();
    }

    /** The source descriptor. */
    public static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(SOURCE)
                .name("Synthetic raster source")
                .description("Test-only frames with no upstream")
                .kind(ProductSpec.RECORD_KIND)
                .interval(Duration.ofSeconds(10), Duration.ofMinutes(2))
                .defaultBudget(Budget.of(60, 10_000))
                .product(observationSpec())
                .product(forecastSpec())
                .build();
    }

    private final SourceDescriptor descriptor = buildDescriptor();

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** Queues a fetch step. */
    public synchronized SyntheticRasterAdapter script(Step step) {
        script.addLast(step);
        return this;
    }

    /** Drops every queued step. */
    public synchronized void clear() {
        script.clear();
    }

    /** How many fetches ran. */
    public int fetchCount() {
        return fetches.get();
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        fetches.incrementAndGet();
        Step next;
        synchronized (this) {
            next = script.pollFirst();
        }
        return next == null ? FetchResult.of(List.of()) : next.run(ctx);
    }
}
