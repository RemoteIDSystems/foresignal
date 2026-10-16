package ai.foresignal.raster.export;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.raster.FrameResolver;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.security.Ulids;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The raster export job framework (prompt 108; Weather-Imagery-Design §11): submit, run and poll
 * MBTiles/PMTiles/COG exports and place packs on one table. A worker pool renders in the background;
 * files live under {@code exports/{id}/} for 48 hours.
 */
@Service
public class ExportJobService {

    private static final Logger log = LoggerFactory.getLogger(ExportJobService.class);

    /** The maximum export lifetime. */
    public static final java.time.Duration TTL = ExportPaths.TTL;

    private final ExportJobRepository jobs;
    private final RasterExportService exports;
    private final PlacePackService packs;
    private final FrameResolver resolver;
    private final ProductRegistry registry;
    private final ExportPaths paths;
    private final Clock clock;
    private final ExecutorService executor = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "foresignal-export");
        t.setDaemon(true);
        return t;
    });

    /** Creates the service. */
    public ExportJobService(ExportJobRepository jobs, RasterExportService exports, PlacePackService packs,
            FrameResolver resolver, ProductRegistry registry, ExportPaths paths, Clock clock) {
        this.jobs = jobs;
        this.exports = exports;
        this.packs = packs;
        this.resolver = resolver;
        this.registry = registry;
        this.paths = paths;
        this.clock = clock;
    }

    /** Submits a raster export. */
    @Transactional
    public ExportJobEntity submitProductExport(ProductRegistry.Registered product, RasterExportService.Request request,
            String tokenId) {
        Instant now = clock.instant();
        ExportJobEntity job = new ExportJobEntity(Ulids.next(now), "EXPORT", request.format(), now);
        job.setProductId(product.spec().id());
        job.setCreatedBy(tokenId);
        job.setSpec(specMap(request));
        job.setExpiresAt(now.plus(TTL));
        jobs.save(job);
        executor.submit(() -> runQuietly(job.getId()));
        return job;
    }

    /** Submits a place offline pack. */
    @Transactional
    public ExportJobEntity submitPlacePack(String placeId, PlacePackService.Request request, String tokenId) {
        Instant now = clock.instant();
        ExportJobEntity job = new ExportJobEntity(Ulids.next(now), "PLACE_PACK", "zip", now);
        job.setSpec(request.toMap());
        job.setCreatedBy(tokenId);
        job.setExpiresAt(now.plus(TTL));
        jobs.save(job);
        executor.submit(() -> runQuietly(job.getId()));
        return job;
    }

    private void runQuietly(String id) {
        try {
            run(id);
        } catch (RuntimeException e) {
            log.warn("Export job {} failed", id, e);
            jobs.findById(id).ifPresent(job -> {
                job.setStatus("FAILED");
                job.setError(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
                job.setFinishedAt(clock.instant());
                jobs.save(job);
            });
        }
    }

    /** Runs a job on this thread. */
    @Transactional
    public void run(String id) {
        ExportJobEntity job = get(id);
        job.setStatus("RUNNING");
        job.setStartedAt(clock.instant());
        job.setProgress(10);
        jobs.save(job);
        try {
            if ("PLACE_PACK".equals(job.getKind())) {
                runPack(job);
            } else {
                runExport(job);
            }
            job.setStatus("DONE");
            job.setProgress(100);
        } catch (ForesignalException e) {
            job.setStatus("FAILED");
            job.setError(e.getMessage());
        } catch (RuntimeException e) {
            job.setStatus("FAILED");
            job.setError(e.getMessage());
        }
        job.setFinishedAt(clock.instant());
        jobs.save(job);
    }

    private void runExport(ExportJobEntity job) {
        ProductRegistry.Registered product = product(job.getProductId());
        RasterExportService.Request request = request(job.getSpec());
        List<RasterFrameRow> frames = new ArrayList<>();
        for (String key : request.frameKeys()) {
            FrameResolver.Resolution resolution = resolver.resolve(product.spec(), key, null, false, clock.instant(),
                    k -> k);
            if (resolution instanceof FrameResolver.Redirect redirect) {
                resolution = resolver.resolve(product.spec(), redirect.canonicalToken(), null, false, clock.instant(),
                        k -> k);
            }
            frames.add(((FrameResolver.Found) resolution).frame());
        }
        RasterExportService.Result result = exports.export(product, request, frames, job.getId(), "export");
        job.setFiles(result.files().stream().map(f -> fileMap(job.getId(), f)).toList());
        job.setRowCount(result.tileCount());
        job.setBytes(result.totalBytes());
        job.setProgress(90);
    }

    private void runPack(ExportJobEntity job) {
        PlacePackService.Result result = packs.build(job.getSpec(), job.getId());
        job.setFiles(result.files());
        job.setBytes(result.totalBytes());
        job.setRowCount(result.items());
        job.setProgress(90);
    }

    /** Fetches a job. */
    public ExportJobEntity get(String id) {
        return jobs.findById(id).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_EXPORT,
                "Unknown export " + id + "."));
    }

    /** The downloadable file of a job (410 when expired or gone). */
    public Path file(String id, String name) {
        ExportJobEntity job = get(id);
        if (job.getFiles() == null) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_EXPORT, "Export " + id + " has no files.");
        }
        for (Map<String, Object> file : job.getFiles()) {
            if (name.equals(file.get("name"))) {
                Path path = paths.jobDir(id).resolve(name.replace("/", ""));
                if (!Files.isRegularFile(path)) {
                    throw new ForesignalException(HttpStatus.GONE, ErrorCodes.UNKNOWN_EXPORT,
                            "The export file " + name + " is gone.");
                }
                return path;
            }
        }
        throw new NotFoundException(ErrorCodes.UNKNOWN_EXPORT, "Export " + id + " has no file " + name + ".");
    }

    /** The JSON view of a job. */
    public Map<String, Object> view(ExportJobEntity job, String base) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", job.getId());
        out.put("kind", job.getKind());
        out.put("format", job.getFormat());
        out.put("spec", job.getSpec());
        out.put("status", job.getStatus());
        out.put("progress", job.getProgress());
        out.put("principal", job.getCreatedBy());
        out.put("rowCount", job.getRowCount());
        out.put("bytes", job.getBytes());
        out.put("manifestSha256", job.getManifestSha256());
        out.put("personalUse", false);
        out.put("createdAt", job.getCreatedAt().toString());
        out.put("startedAt", job.getStartedAt() == null ? null : job.getStartedAt().toString());
        out.put("finishedAt", job.getFinishedAt() == null ? null : job.getFinishedAt().toString());
        out.put("expiresAt", job.getExpiresAt() == null ? null : job.getExpiresAt().toString());
        out.put("error", job.getError());
        out.put("productId", job.getProductId());
        out.put("downloadUrl", job.getFiles() == null || job.getFiles().isEmpty() ? null
                : base + "/v1/exports/" + job.getId() + "/files/" + job.getFiles().get(0).get("name"));
        out.put("manifestUrl", null);
        out.put("files", job.getFiles() == null ? List.of() : job.getFiles());
        return out;
    }

    private ProductRegistry.Registered product(String productId) {
        return registry.require(productId);
    }

    /** The job map for a written file. */
    Map<String, Object> fileMap(String jobId, RasterExportService.File file) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", file.name());
        m.put("url", "/v1/exports/" + jobId + "/files/" + file.name());
        m.put("bytes", file.bytes());
        m.put("sha256", file.sha256());
        m.put("mediaType", file.mediaType());
        m.put("expiresAt", clock.instant().plus(TTL).toString());
        return m;
    }

    private static Map<String, Object> specMap(RasterExportService.Request request) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("format", request.format());
        map.put("frameKeys", request.frameKeys());
        map.put("bbox", List.of(request.bbox()[0], request.bbox()[1], request.bbox()[2], request.bbox()[3]));
        map.put("minZoom", request.minZoom());
        map.put("maxZoom", request.maxZoom());
        map.put("style", request.style());
        map.put("tms", request.tms().id());
        return map;
    }

    @SuppressWarnings("unchecked")
    static RasterExportService.Request request(Map<String, Object> spec) {
        List<Number> box = (List<Number>) spec.get("bbox");
        return new RasterExportService.Request((String) spec.get("format"),
                ((List<?>) spec.get("frameKeys")).stream().map(String::valueOf).toList(),
                new double[] {box.get(0).doubleValue(), box.get(1).doubleValue(), box.get(2).doubleValue(),
                    box.get(3).doubleValue()},
                ((Number) spec.get("minZoom")).intValue(), ((Number) spec.get("maxZoom")).intValue(),
                (String) spec.get("style"), ai.foresignal.raster.render.TileMatrixSets.resolve((String) spec.get("tms")));
    }

    /** Removes expired jobs and their files. */
    @Transactional
    public int sweep() {
        Instant now = clock.instant();
        int removed = 0;
        for (ExportJobEntity job : jobs.findByFinishedAtBefore(now.minus(TTL))) {
            Path dir = paths.jobDir(job.getId());
            try {
                if (Files.isDirectory(dir)) {
                    try (var walk = Files.walk(dir)) {
                        for (Path p : walk.sorted(java.util.Comparator.reverseOrder()).toList()) {
                            Files.deleteIfExists(p);
                        }
                    }
                }
            } catch (IOException e) {
                log.warn("Could not delete export directory {}", dir, e);
            }
            jobs.delete(job);
            removed++;
        }
        return removed;
    }
}
