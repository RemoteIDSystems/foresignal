package ai.foresignal.raster;

import ai.foresignal.ingest.FetchLogEntry;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.ingest.IngestRecord;
import ai.foresignal.retention.RetentionRun;
import ai.foresignal.retention.RetentionStep;
import ai.foresignal.source.spi.Coverage;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.store.FetchStatus;
import ai.foresignal.store.FetchTrigger;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Frame retention (prompt 042, Weather-Imagery-Design §2.4), run by the hourly {@code RetentionJob}
 * after its record steps (prompt 019's {@link RetentionStep} extension point). Deliberately not
 * {@code @Transactional}: each row delete commits on its own, and a file is unlinked only after its
 * row is gone, so the API answers {@code 410 FRAME_EXPIRED} rather than pointing at a missing file.
 *
 * <ol>
 *   <li><b>Ladders</b> ({@link FrameThinning}): thin each product's frames, mark the kept ones
 *       thinned with their new expiry, delete the rest; forecasts keep the current and previous
 *       complete runs.
 *   <li><b>Quota</b>: while the store holds more than {@code foresignal.raster.disk-quota-gb}
 *       (default 200), evict the oldest frames across products ({@code /v1/system} degrades at 90 %,
 *       {@link RasterSystemBlock}).
 *   <li><b>Orphan sweep</b>: delete files with no row and rows with no file (both only once older
 *       than {@code orphanGrace}, so a commit in flight is never touched), clear stale staging files,
 *       and <b>re-emit the {@code raster-frame} record</b> of any complete frame (or completed run)
 *       newer than its record head, which covers a crash between the frame commit and the enqueue.
 *       Re-emits go through the ingest queue as a {@code MANUAL} fetch-log row with principal
 *       {@value #SWEEPER_PRINCIPAL}.
 * </ol>
 */
@Component
public class RasterRetentionStep implements RetentionStep {

    private static final Logger log = LoggerFactory.getLogger(RasterRetentionStep.class);

    /** The table name reported in {@code foresignal.retention.deleted{table}}. */
    public static final String TABLE = "raster_frame";

    /** The principal of re-emitted records' fetch-log rows. */
    public static final String SWEEPER_PRINCIPAL = "system:raster-sweeper";

    /** Frames evicted per quota round trip. */
    static final int QUOTA_BATCH = 200;

    /** Most records re-emitted per product per sweep. */
    static final int MAX_REEMIT = 100;

    private final ProductRegistry registry;
    private final RasterFrameQueries frames;
    private final FrameStore store;
    private final GridStore grids;
    private final RasterSettings settings;
    private final IngestQueue queue;
    private final ProductUrls urls;
    private final JsonMapper json;
    private final AtomicLong arrival = new AtomicLong();

    /** Creates the step. */
    public RasterRetentionStep(ProductRegistry registry, RasterFrameQueries frames, FrameStore store, GridStore grids,
            RasterSettings settings, IngestQueue queue, ProductUrls urls, JsonMapper json) {
        this.registry = registry;
        this.frames = frames;
        this.store = store;
        this.grids = grids;
        this.settings = settings;
        this.queue = queue;
        this.urls = urls;
        this.json = json;
    }

    @Override
    public String name() {
        return "rasters";
    }

    @Override
    public int order() {
        return 800;
    }

    @Override
    public void run(RetentionRun run) {
        Instant now = run.now();
        long deleted = thin(now);
        deleted += enforceQuota();
        SweepResult sweep = sweep(now);
        deleted += sweep.rowsDeleted();
        run.deleted(TABLE, deleted);
        if (deleted > 0 || sweep.filesDeleted() > 0 || sweep.reemitted() > 0) {
            log.info("Raster retention: {} frames deleted, {} orphan files removed, {} records re-emitted", deleted,
                    sweep.filesDeleted(), sweep.reemitted());
        }
    }

    /** Applies every product's ladder; returns the frames deleted. */
    long thin(Instant now) {
        long deleted = 0;
        for (ProductRegistry.Registered product : registry.all()) {
            if (product.spec().isExternal()) {
                continue;
            }
            FrameThinning.Plan plan = FrameThinning.plan(product.spec(), frames.allFrames(product.id()), now);
            for (RasterFrameRow frame : plan.delete()) {
                if (deleteFrame(frame.productId(), frame.frameId())) {
                    deleted++;
                }
            }
            plan.thinned().forEach((frame, expiry) -> frames.markThinned(frame.productId(), frame.frameId(), expiry));
        }
        return deleted;
    }

    /** Evicts the oldest frames until the store fits the quota; returns the frames deleted. */
    long enforceQuota() {
        return enforceQuota(settings.diskQuotaBytes());
    }

    /** Evicts the oldest frames until the store fits {@code quota} bytes; returns the frames deleted. */
    long enforceQuota(long quota) {
        RasterFrameQueries.Totals totals = frames.totals();
        long bytes = totals.bytes();
        long deleted = 0;
        while (bytes > quota) {
            List<RasterFrameRow> oldest = frames.oldest(QUOTA_BATCH);
            if (oldest.isEmpty()) {
                break;
            }
            for (RasterFrameRow frame : oldest) {
                if (bytes <= quota) {
                    break;
                }
                if (deleteFrame(frame.productId(), frame.frameId())) {
                    bytes -= frame.bytes();
                    deleted++;
                }
            }
        }
        if (deleted > 0) {
            log.warn("The raster store exceeded its quota of {} bytes; evicted the {} oldest frames", quota, deleted);
        }
        return deleted;
    }

    /** Deletes the row, then the file. */
    private boolean deleteFrame(String productId, String frameId) {
        Optional<String> path = frames.delete(productId, frameId);
        path.ifPresent(p -> {
            try {
                store.delete(p);
            } catch (UncheckedIOException e) {
                log.warn("Could not unlink {}; the orphan sweep retries", p, e);
            }
        });
        return path.isPresent();
    }

    /**
     * What one sweep did.
     *
     * @param filesDeleted orphan and stale staging files removed
     * @param rowsDeleted rows removed because their file was gone
     * @param reemitted records re-emitted
     */
    public record SweepResult(long filesDeleted, long rowsDeleted, long reemitted) {}

    /** The orphan sweep. */
    SweepResult sweep(Instant now) {
        Instant graceCutoff = now.minus(settings.orphanGrace());
        long files = sweepFiles(graceCutoff);
        long rows = sweepRows(graceCutoff);
        long reemitted = reemit(graceCutoff, now);
        return new SweepResult(files, rows, reemitted);
    }

    private long sweepFiles(Instant graceCutoff) {
        long removed = 0;
        Path tmp = store.paths().tmp();
        if (Files.isDirectory(tmp)) {
            try (Stream<Path> staged = Files.list(tmp)) {
                for (Path file : staged.toList()) {
                    if (olderThan(file, graceCutoff) && Files.deleteIfExists(file)) {
                        removed++;
                    }
                }
            } catch (IOException e) {
                log.warn("Could not sweep {}", tmp, e);
            }
        }
        Path root = store.paths().frames();
        if (!Files.isDirectory(root)) {
            return removed;
        }
        try (Stream<Path> products = Files.list(root)) {
            for (Path productDir : products.filter(Files::isDirectory).toList()) {
                Set<String> known = new HashSet<>(frames.paths(productDir.getFileName().toString()));
                try (Stream<Path> walk = Files.walk(productDir)) {
                    for (Path file : walk.filter(Files::isRegularFile).toList()) {
                        String relative = store.paths().relativize(file);
                        if (!known.contains(relative) && olderThan(file, graceCutoff)) {
                            store.delete(relative);
                            removed++;
                        }
                    }
                }
            }
        } catch (IOException | UncheckedIOException e) {
            log.warn("Could not sweep {}", root, e);
        }
        return removed;
    }

    private long sweepRows(Instant graceCutoff) {
        long removed = 0;
        for (String productId : frames.productIds()) {
            for (RasterFrameRow frame : frames.allFrames(productId)) {
                if (frame.ingestedAt().isBefore(graceCutoff) && !store.exists(frame.path())
                        && frames.delete(frame.productId(), frame.frameId()).isPresent()) {
                    removed++;
                }
            }
        }
        return removed;
    }

    /** Re-emits records for frames and runs newer than their record head. */
    long reemit(Instant graceCutoff, Instant now) {
        long total = 0;
        for (ProductRegistry.Registered product : registry.all()) {
            ProductSpec spec = product.spec();
            if (spec.isExternal()) {
                continue;
            }
            Optional<Instant> head = frames.recordHeadObservedAt(product.sourceId(), spec.id());
            List<NormalizedRecord> records = spec.isForecast()
                    ? missingRuns(spec, head)
                    : missingFrames(spec, head, graceCutoff);
            if (records.isEmpty()) {
                continue;
            }
            submit(product.sourceId(), records, now);
            total += records.size();
        }
        return total;
    }

    private List<NormalizedRecord> missingFrames(ProductSpec spec, Optional<Instant> head, Instant graceCutoff) {
        List<NormalizedRecord> out = new ArrayList<>();
        for (RasterFrameRow frame : frames.allFrames(spec.id())) {
            if (!frame.complete() || !frame.ingestedAt().isBefore(graceCutoff)
                    || (head.isPresent() && !frame.validTime().isAfter(head.get()))) {
                continue;
            }
            List<Double> bounds = grids.find(frame.gridId()).map(g -> RasterPipeline.bounds(spec, g))
                    .orElse(spec.spatial() == null ? null : spec.spatial().bounds());
            out.add(RasterFrameRecords.observation(spec, frame, bounds, urls.storedBase(), json));
            if (out.size() >= MAX_REEMIT) {
                break;
            }
        }
        return out;
    }

    private List<NormalizedRecord> missingRuns(ProductSpec spec, Optional<Instant> head) {
        List<NormalizedRecord> out = new ArrayList<>();
        List<RasterFrameQueries.RunSummary> runs = new ArrayList<>(frames.runs(spec.id()));
        java.util.Collections.reverse(runs);
        for (RasterFrameQueries.RunSummary run : runs) {
            if (!run.complete() || (head.isPresent() && !run.run().isAfter(head.get()))) {
                continue;
            }
            List<RasterFrameRow> steps = frames.runFrames(spec.id(), run.run(), false);
            if (steps.isEmpty()) {
                continue;
            }
            List<Double> bounds = grids.find(steps.get(0).gridId()).map(g -> RasterPipeline.bounds(spec, g))
                    .orElse(spec.spatial() == null ? null : spec.spatial().bounds());
            out.add(RasterFrameRecords.forecastRun(spec, run.run(), steps, bounds, urls.storedBase()));
        }
        return out;
    }

    private void submit(String sourceId, List<NormalizedRecord> records, Instant now) {
        FetchLogEntry entry = new FetchLogEntry(sourceId, FetchTrigger.MANUAL, FetchStatus.OK, now, now, null, 0,
                null, null, null, null, null, null, SWEEPER_PRINCIPAL, Coverage.none(), null);
        List<IngestRecord> batch = records.stream().map(r -> new IngestRecord(r, now, arrival.incrementAndGet())).toList();
        try {
            queue.submit(sourceId, entry, batch, Duration.ofSeconds(30)).get(60, TimeUnit.SECONDS);
            log.info("Re-emitted {} raster-frame record(s) of {} newer than the record head", records.size(), sourceId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Could not re-emit raster-frame records of {}", sourceId, e);
        }
    }

    private static boolean olderThan(Path file, Instant cutoff) {
        try {
            return Files.getLastModifiedTime(file).toInstant().isBefore(cutoff);
        } catch (IOException e) {
            return false;
        }
    }
}
