package ai.foresignal.raster;

import org.springframework.beans.factory.ObjectProvider;
import ai.foresignal.raster.lightning.FlashStore;
import ai.foresignal.ingest.FetchLogEntry;
import ai.foresignal.media.MediaQueries;
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
 *   <li><b>Quota</b>: while the frame store plus the media store (prompt 043) hold more than
 *       {@code foresignal.raster.disk-quota-gb} (default 200), evict the oldest frames across products
 *       ({@code /v1/system} degrades at 90 %, {@link RasterSystemBlock}). Media is never evicted for
 *       the quota; it follows its owning record.
 *   <li><b>GLM archive</b> (prompt 048): delete {@code rasters/glm/{sat}/{yyyymmdd}.bin} files older
 *       than {@value ai.foresignal.raster.lightning.FlashStore#ARCHIVE_DAYS} days. Their bytes count
 *       toward the quota below, but only frames are evicted for it.
 *   <li><b>Orphan sweep</b>: delete files with no row (including the wind field files of a run with no
 *       frame left, prompt 051) and rows with no file (both only once older
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
    private final MediaQueries media;
    private final AtomicLong arrival = new AtomicLong();
    private final ObjectProvider<FlashStore> flashes;
    private final ai.foresignal.raster.model.FieldStore fieldStore;
    private final ObjectProvider<FramePin> pins;

    /**
     * Creates the step.
     *
     * @param flashes the GLM flash store (prompt 048), resolved lazily; null for none
     * @param fieldStore the wind field files (prompt 051), swept with their runs
     * @param pins the frame pins (prompt 131 chronicles); a pinned frame is never deleted by the ladder or the quota
     */
    public RasterRetentionStep(ProductRegistry registry, RasterFrameQueries frames, FrameStore store, GridStore grids,
            RasterSettings settings, IngestQueue queue, ProductUrls urls, JsonMapper json, MediaQueries media,
            ObjectProvider<FlashStore> flashes, ai.foresignal.raster.model.FieldStore fieldStore,
            ObjectProvider<FramePin> pins) {
        this.registry = registry;
        this.frames = frames;
        this.store = store;
        this.grids = grids;
        this.settings = settings;
        this.queue = queue;
        this.urls = urls;
        this.json = json;
        this.media = media;
        this.flashes = flashes;
        this.fieldStore = fieldStore;
        this.pins = pins;
    }

    /** True when a {@link FramePin} (a chronicle, prompt 131) holds {@code frame}; such a frame is never deleted here. */
    boolean pinned(RasterFrameRow frame) {
        if (pins == null) {
            return false;
        }
        for (FramePin pin : pins) {
            try {
                if (pin.pinned(frame.productId(), frame.frameKey())) {
                    return true;
                }
            } catch (RuntimeException e) {
                log.warn("A frame pin failed for {}/{}; keeping the frame", frame.productId(), frame.frameId(), e);
                return true;
            }
        }
        return false;
    }

    /** The flash store, or null when none is installed. */
    private FlashStore flashStore() {
        return flashes == null ? null : flashes.getIfAvailable();
    }

    /**
     * Deletes GLM archive files older than {@value ai.foresignal.raster.lightning.FlashStore#ARCHIVE_DAYS}
     * days; returns the bytes deleted.
     */
    long expireGlmArchive(Instant now) {
        FlashStore store = flashStore();
        if (store == null) {
            return 0;
        }
        long bytes = store.deleteArchivesBefore(now.minus(Duration.ofDays(FlashStore.ARCHIVE_DAYS)));
        if (bytes > 0) {
            log.info("GLM archive retention: {} bytes of files older than {} days deleted", bytes,
                    FlashStore.ARCHIVE_DAYS);
        }
        return bytes;
    }

    /** The GLM archive's bytes on disk, or 0 without a flash store. */
    private long glmBytes() {
        FlashStore store = flashStore();
        return store == null ? 0 : store.archiveBytes();
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
        expireGlmArchive(now);
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
                if (!pinned(frame) && deleteFrame(frame.productId(), frame.frameId())) {
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

    /**
     * Evicts the oldest frames until the frame and media stores and the GLM archive together fit
     * {@code quota} bytes; returns the frames deleted.
     */
    long enforceQuota(long quota) {
        RasterFrameQueries.Totals totals = frames.totals();
        long bytes = totals.bytes() + media.totalBytes() + glmBytes();
        long deleted = 0;
        while (bytes > quota) {
            List<RasterFrameRow> oldest = frames.oldest(QUOTA_BATCH);
            if (oldest.isEmpty()) {
                break;
            }
            long before = deleted;
            for (RasterFrameRow frame : oldest) {
                if (bytes <= quota) {
                    break;
                }
                if (pinned(frame)) {
                    continue;
                }
                if (deleteFrame(frame.productId(), frame.frameId())) {
                    bytes -= frame.bytes();
                    deleted++;
                }
            }
            if (deleted == before) {
                // Every remaining candidate is pinned by a chronicle (prompt 131): stop rather than loop.
                break;
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
        if (fieldStore != null) {
            // Prompt 051: wind field files whose run has no frame left.
            files += fieldStore.sweep(product -> frames.runs(product).stream()
                    .map(r -> ai.foresignal.raster.model.ModelCycleSelector.cycleKey(r.run()))
                    .collect(java.util.stream.Collectors.toSet()), graceCutoff);
        }
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
