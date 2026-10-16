package ai.foresignal.export;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.export.pmtiles.PmtilesWriter;
import ai.foresignal.place.PlaceService;
import ai.foresignal.raster.FrameResolver;
import ai.foresignal.raster.ProductCatalogService;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.render.RasterRenderService;
import ai.foresignal.raster.render.RenderQueue;
import ai.foresignal.raster.render.StyleRegistry;
import ai.foresignal.raster.render.TileCache;
import ai.foresignal.raster.render.WebMercator;
import ai.foresignal.security.Ulids;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.history.AsOfPlan;
import ai.foresignal.store.history.AsOfResolver;
import ai.foresignal.store.tiles.AggMode;
import ai.foresignal.store.tiles.AsOfTileQueryService;
import ai.foresignal.store.tiles.TileCoordinates;
import ai.foresignal.store.tiles.TileLayer;
import ai.foresignal.store.tiles.TileLayerResolver;
import ai.foresignal.store.tiles.TileQueryService;
import ai.foresignal.stream.ChangeBus;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PMTiles v3 offline packs (prompt 111, {@code createPack}): one {@code .pmtiles} per tile type for a
 * box and zoom range, an MVT pack ({@code vector.pmtiles}) from the 016/037 tile queries and a PNG
 * pack ({@code raster.pmtiles}) from the 045 renderer, written with
 * {@link PmtilesWriter.Archive} (deduplicated, clustered, leaf directories when the root would pass
 * 16 KiB). A pack is an export job of kind {@code PACK}, run by the same bounded worker and
 * downloaded through {@code /v1/exports/{id}/download?file=…} with {@code Range}.
 *
 * <p>Limits: at most {@code packs.maxTiles} (200,000) tiles over the box and zooms
 * ({@code 413 PACK_TOO_LARGE} with the computed {@code tileCount}). License and private rules are
 * those of exports on the {@code EXPORT} path, for the vector sources and the raster product's
 * source alike. The vector layer is named {@value #VECTOR_LAYER}.
 */
@Service
public class PackService {

    private static final Logger log = LoggerFactory.getLogger(PackService.class);

    /** The MVT layer name of a vector pack. */
    public static final String VECTOR_LAYER = "foresignal";

    /** The default box: the Web Mercator world. */
    static final double[] WORLD = {-180, -85.0511, 180, 85.0511};

    private static final int RENDER_ATTEMPTS = 5;

    private final BulkExportJobRepository jobs;
    private final ExportJobService exports;
    private final ExportStore store;
    private final ExportProperties properties;
    private final ExportLicenceService licenses;
    private final ExportManifests manifests;
    private final ExportWorker worker;
    private final SourceRegistry registry;
    private final TileLayerResolver layers;
    private final TileQueryService tiles;
    private final AsOfTileQueryService asOfTiles;
    private final AsOfResolver asOf;
    private final RasterRenderService renderer;
    private final ProductCatalogService catalog;
    private final ProductRegistry products;
    private final FrameResolver frames;
    private final PlaceService places;
    private final ChangeBus changeBus;
    private final Clock clock;
    private final Map<String, ReadRestriction> restrictions = new ConcurrentHashMap<>();

    /** Creates the service. */
    public PackService(BulkExportJobRepository jobs, ExportJobService exports, ExportStore store,
            ExportProperties properties, ExportLicenceService licenses, ExportManifests manifests, ExportWorker worker,
            SourceRegistry registry, TileLayerResolver layers, TileQueryService tiles, AsOfTileQueryService asOfTiles,
            AsOfResolver asOf, RasterRenderService renderer, ProductCatalogService catalog, ProductRegistry products,
            FrameResolver frames, PlaceService places, ChangeBus changeBus, Clock clock) {
        this.jobs = jobs;
        this.exports = exports;
        this.store = store;
        this.properties = properties;
        this.licenses = licenses;
        this.manifests = manifests;
        this.worker = worker;
        this.registry = registry;
        this.layers = layers;
        this.tiles = tiles;
        this.asOfTiles = asOfTiles;
        this.asOf = asOf;
        this.renderer = renderer;
        this.catalog = catalog;
        this.products = products;
        this.frames = frames;
        this.places = places;
        this.changeBus = changeBus;
        this.clock = clock;
    }

    /**
     * Creates a pack job.
     *
     * @param excludedSources the private sources the caller may not see (for the product lookup)
     * @throws ForesignalException {@code 413 PACK_TOO_LARGE}, {@code 403} for {@code includePrivate}
     *     without admin, {@code 400 EXPORT_SPEC_INVALID} when nothing may be packed,
     *     {@code 409 EXPORT_QUOTA}
     */
    @Transactional
    public ExportJobEntity create(PackSpec spec, ExportJobService.Caller caller, Set<String> excludedSources) {
        ExportJobService.requirePrivateRights(spec.includePrivate(), caller);
        double[] bbox = bbox(spec);
        long tileCount = countTiles(bbox, spec.minZoom(), spec.maxZoom());
        if (tileCount > properties.packMaxTiles()) {
            throw new ForesignalException(HttpStatus.PAYLOAD_TOO_LARGE, ErrorCodes.PACK_TOO_LARGE,
                    "The pack covers " + tileCount + " tiles, over packs.maxTiles (" + properties.packMaxTiles()
                            + "); shrink the box or the zoom range.")
                    .withProperty("tileCount", tileCount).withProperty("maxTiles", properties.packMaxTiles());
        }
        List<SourceDescriptor> candidates = new ArrayList<>();
        if (spec.vector() != null) {
            candidates.addAll(exports.candidates(spec.vector().sources(), spec.vector().kinds()));
        }
        ProductRegistry.Registered product = null;
        String frameKey = null;
        if (spec.raster() != null) {
            product = catalog.visible(spec.raster().productId(), excludedSources);
            RasterRenderService.requireRendered(product.spec());
            catalog.requireAvailable(product);
            frameKey = resolveFrame(product, spec.raster().frame()).frameKey();
            if (product.sourceId() != null && registry.contains(product.sourceId())) {
                candidates.add(registry.descriptor(product.sourceId()));
            }
        }
        ExportLicenceService.Decision decision = licenses.decide(candidates, caller.viewer(), caller.admin(),
                spec.includePrivate(), spec.personalUse());
        List<String> vectorSources = new ArrayList<>();
        if (spec.vector() != null) {
            for (SourceDescriptor descriptor : exports.candidates(spec.vector().sources(), spec.vector().kinds())) {
                if (decision.allowed().contains(descriptor.id())) {
                    vectorSources.add(descriptor.id());
                }
            }
            if (vectorSources.isEmpty()) {
                throw new ValidationException(ErrorCodes.EXPORT_SPEC_INVALID,
                        "No requested vector source may be packed with these rights.", List.of())
                        .withProperty("excluded", decision.excluded());
            }
            if (spec.vector().asOf() != null) {
                asOf.plan(spec.vector().asOf(), vectorSources, spec.vector().kinds(), Set.of());
            }
        }
        if (product != null && product.sourceId() != null && !decision.allowed().contains(product.sourceId())) {
            throw new ValidationException(ErrorCodes.EXPORT_SPEC_INVALID,
                    "The product " + product.id() + " may not be packed with these rights.", List.of())
                    .withProperty("excluded", decision.excluded());
        }
        exports.checkQuota();
        Instant now = clock.instant();
        ExportJobEntity job = new ExportJobEntity(Ulids.next(now), ExportJobEntity.PACK, "pmtiles", now);
        job.setPrincipal(caller.principal());
        job.setPersonalUse(!decision.personalUseSources().isEmpty());
        job.setExpiresAt(now.plus(properties.ttl()));
        job.setSpec(spec.toMap());
        Map<String, Object> plan = ExportJobService.basePlan(decision);
        plan.put("bbox", List.of(bbox[0], bbox[1], bbox[2], bbox[3]));
        plan.put("tileCount", tileCount);
        plan.put("vectorSources", vectorSources);
        if (product != null) {
            plan.put("productId", product.id());
            plan.put("frameKey", frameKey);
        }
        job.setPlan(plan);
        job.setLicenceSummary(licenses.summary(decision, Map.of()));
        job.setDir(store.dirOf(job).toString());
        jobs.save(job);
        restrictions.put(job.getId(), caller.restriction() == null ? ReadRestriction.NONE : caller.restriction());
        worker.submit(() -> run(job.getId()));
        return job;
    }

    private double[] bbox(PackSpec spec) {
        if (spec.bbox() != null) {
            return spec.bbox();
        }
        if (spec.placeId() != null) {
            double[] box = places.geometry(spec.placeId()).map(ExportQueryService::envelopeOf).orElse(null);
            if (box == null) {
                throw new ValidationException(ErrorCodes.EXPORT_SPEC_INVALID, "Unknown place " + spec.placeId() + ".",
                        List.of());
            }
            return box;
        }
        return WORLD.clone();
    }

    private RasterFrameRow resolveFrame(ProductRegistry.Registered product, String token) {
        String frame = token == null || token.isBlank() ? FrameResolver.LATEST : token;
        FrameResolver.Resolution resolution = frames.resolve(product.spec(), frame, null, false, clock.instant(), k -> k);
        if (resolution instanceof FrameResolver.Redirect redirect) {
            resolution = frames.resolve(product.spec(), redirect.canonicalToken(), null, false, clock.instant(), k -> k);
        }
        if (!(resolution instanceof FrameResolver.Found found)) {
            throw new ValidationException(ErrorCodes.EXPORT_SPEC_INVALID, "The frame " + frame + " cannot be resolved.",
                    List.of());
        }
        return found.frame();
    }

    /** Runs the pack job {@code id} on this thread (the worker calls this). */
    void run(String id) {
        ExportJobEntity job = jobs.findById(id).orElse(null);
        ReadRestriction restriction = restrictions.remove(id);
        if (job == null || job.terminal()) {
            return;
        }
        if (exports.cancelRequested(id)) {
            exports.clearCancel(id);
            exports.finish(job, ExportJobEntity.CANCELLED, null);
            return;
        }
        if (restriction == null) {
            exports.finish(job, ExportJobEntity.FAILED, "The job lost its caller context (restart); submit it again.");
            return;
        }
        job.setStatus(ExportJobEntity.RUNNING);
        job.setStartedAt(clock.instant());
        job.setProgress(1);
        jobs.save(job);
        Path dir = store.dirOf(job);
        try {
            Path tmp = store.tmpDir(job);
            PackSpec spec = PackSpec.fromMap(job.getSpec());
            Map<String, Object> plan = job.getPlan();
            List<?> rawBox = (List<?>) plan.get("bbox");
            double[] bbox = {((Number) rawBox.get(0)).doubleValue(), ((Number) rawBox.get(1)).doubleValue(),
                ((Number) rawBox.get(2)).doubleValue(), ((Number) rawBox.get(3)).doubleValue()};
            long total = ((Number) plan.get("tileCount")).longValue() * (spec.vector() != null && spec.raster() != null
                    ? 2 : 1);
            long[] done = {0};
            long watermark = changeBus.lastPublishedSeq();
            job.setWatermarkSeq(watermark);
            List<ExportManifests.ExportFile> files = new ArrayList<>();
            Map<String, Long> tilesBySource = new LinkedHashMap<>();
            if (spec.vector() != null) {
                List<String> sources = ExportJobService.strings(plan.get("vectorSources"));
                files.add(vector(job, spec, sources, bbox, tmp, dir, restriction, done, total, watermark));
                sources.forEach(source -> tilesBySource.merge(source, files.getLast().rows(), Long::sum));
            }
            if (spec.raster() != null) {
                files.add(raster(job, spec, plan, bbox, tmp, dir, done, total, watermark));
                ProductRegistry.Registered product = products.require(String.valueOf(plan.get("productId")));
                if (product.sourceId() != null) {
                    tilesBySource.merge(product.sourceId(), files.getLast().rows(), Long::sum);
                }
            }
            ExportStore.deleteTree(tmp);
            Map<String, Object> summary = licenses.summary(ExportJobService.decision(plan), tilesBySource);
            job.setLicenceSummary(summary);
            job.setRowCount(files.stream().mapToLong(ExportManifests.ExportFile::rows).sum());
            job.setBytes(files.stream().mapToLong(ExportManifests.ExportFile::bytes).sum());
            job.setManifestSha256(manifests.write(dir, job, files, watermark, null, summary));
            job.setProgress(100);
            exports.finish(job, ExportJobEntity.DONE, null);
        } catch (ExportJobService.CanceledExport e) {
            ExportJobService.deleteQuietly(dir);
            exports.clearCancel(id);
            exports.finish(job, ExportJobEntity.CANCELLED, null);
        } catch (IOException | RuntimeException e) {
            log.warn("Pack {} failed", id, e);
            ExportJobService.deleteQuietly(dir);
            exports.finish(job, ExportJobEntity.FAILED, e.getMessage() == null ? e.getClass().getSimpleName()
                    : e.getMessage());
        }
    }

    private ExportManifests.ExportFile vector(ExportJobEntity job, PackSpec spec, List<String> sources, double[] bbox,
            Path tmp, Path dir, ReadRestriction restriction, long[] done, long total, long watermark) throws IOException {
        AsOfPlan plan = spec.vector().asOf() == null ? null
                : asOf.plan(spec.vector().asOf(), sources, spec.vector().kinds(), Set.of());
        TileLayer view = layers.viewLayer(VECTOR_LAYER, spec.name(), sources, Set.of());
        ReadRestriction effective = withKinds(restriction, spec.vector().kinds());
        Path target = tmp.resolve("vector.pmtiles");
        try (PmtilesWriter.Archive archive = new PmtilesWriter.Archive(target, PmtilesWriter.TYPE_MVT,
                PmtilesWriter.COMPRESSION_GZIP)) {
            forEachTile(bbox, spec.minZoom(), spec.maxZoom(), job, done, total, (z, x, y) -> {
                TileCoordinates coordinates = new TileCoordinates(z, x, y);
                byte[] mvt = plan == null ? tiles.render(view, coordinates, AggMode.NONE, effective).mvt()
                        : asOfTiles.render(view, coordinates, AggMode.NONE, plan, effective).mvt();
                if (mvt != null && mvt.length > 0) {
                    archive.add(z, x, y, gzip(mvt));
                }
            });
            Map<String, Object> metadata = metadata(job, spec, plan == null ? watermark : plan.s());
            metadata.put("vector_layers", List.of(vectorLayer(view, spec)));
            archive.finish(metadata, bbox, spec.minZoom(), spec.maxZoom());
            return publish(target, dir, archive.tiles());
        }
    }

    private ExportManifests.ExportFile raster(ExportJobEntity job, PackSpec spec, Map<String, Object> plan,
            double[] bbox, Path tmp, Path dir, long[] done, long total, long watermark) throws IOException {
        ProductRegistry.Registered product = products.require(String.valueOf(plan.get("productId")));
        RasterFrameRow frame = resolveFrame(product, String.valueOf(plan.get("frameKey")));
        StyleRegistry.Resolved style = renderer.style(product.spec(), null);
        Path target = tmp.resolve("raster.pmtiles");
        try (PmtilesWriter.Archive archive = new PmtilesWriter.Archive(target, PmtilesWriter.TYPE_PNG,
                PmtilesWriter.COMPRESSION_NONE)) {
            forEachTile(bbox, spec.minZoom(), spec.maxZoom(), job, done, total, (z, x, y) -> {
                byte[] png = render(product, frame, style, z, x, y);
                if (png != null && png.length > 0 && png != TileCache.EMPTY) {
                    archive.add(z, x, y, png);
                }
            });
            Map<String, Object> metadata = metadata(job, spec, watermark);
            metadata.put("productId", product.id());
            metadata.put("frameKey", frame.frameKey());
            archive.finish(metadata, bbox, spec.minZoom(), spec.maxZoom());
            return publish(target, dir, archive.tiles());
        }
    }

    private byte[] render(ProductRegistry.Registered product, RasterFrameRow frame, StyleRegistry.Resolved style, int z,
            int x, int y) {
        for (int attempt = 1; ; attempt++) {
            try {
                return renderer.tile(product.spec(), frame, style, z, x, y, 1, RenderQueue.Priority.PREFETCH, "pack");
            } catch (ForesignalException e) {
                if (e.getStatus() != HttpStatus.SERVICE_UNAVAILABLE || attempt >= RENDER_ATTEMPTS) {
                    throw e;
                }
                try {
                    Thread.sleep(1000L * attempt);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }
    }

    private Map<String, Object> metadata(ExportJobEntity job, PackSpec spec, long watermark) {
        Map<String, Object> summary = job.getLicenceSummary() == null ? Map.of() : job.getLicenceSummary();
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("name", spec.name());
        metadata.put("description", "Foresignal offline pack " + job.getId());
        metadata.put("type", "overlay");
        metadata.put("attribution", String.join(" · ", ExportLicenceService.attributionLines(summary)));
        metadata.put("generated", clock.instant().toString());
        metadata.put("watermarkSeq", watermark);
        if (job.isPersonalUse()) {
            metadata.put("licence", ExportLicenceService.PERSONAL_USE_NOTICE);
        }
        return metadata;
    }

    /** The TileJSON {@code vector_layers} entry of the pack's layer. */
    static Map<String, Object> vectorLayer(TileLayer view, PackSpec spec) {
        Map<String, Object> fields = new LinkedHashMap<>();
        for (String property : view.properties()) {
            fields.put(property, "version".equals(property) ? "Number" : "live".equals(property) ? "Boolean" : "String");
        }
        Map<String, Object> layer = new LinkedHashMap<>();
        layer.put("id", VECTOR_LAYER);
        layer.put("description", view.description());
        layer.put("minzoom", spec.minZoom());
        layer.put("maxzoom", spec.maxZoom());
        layer.put("fields", fields);
        return layer;
    }

    /** {@code restriction} narrowed to {@code kinds} (none when empty). */
    static ReadRestriction withKinds(ReadRestriction restriction, List<String> kinds) {
        ReadRestriction r = restriction == null ? ReadRestriction.NONE : restriction;
        if (kinds == null || kinds.isEmpty()) {
            return r;
        }
        Set<String> narrowed = new LinkedHashSet<>(kinds);
        if (r.kinds() != null) {
            narrowed.retainAll(r.kinds());
        }
        ReadRestriction out = new ReadRestriction(r.sources(), narrowed, r.bbox(), r.area(), r.endpoints(),
                r.excludePrivate(), r.maxRequestsPerMinute());
        if (r.filter() != null) {
            out = out.withFilter(r.filter());
        }
        if (r.regionFilter() != null) {
            out = out.withRegionFilter(r.regionFilter());
        }
        return out.withExcludeSensitive(r.excludeSensitive());
    }

    private ExportManifests.ExportFile publish(Path tmpFile, Path dir, long tiles) throws IOException {
        Path target = ExportJobService.publish(tmpFile, dir.resolve(tmpFile.getFileName()));
        return new ExportManifests.ExportFile(target.getFileName().toString(), "application/vnd.pmtiles",
                Files.size(target), ExportManifests.sha256(target), tiles);
    }

    /** A per-tile callback that may write. */
    @FunctionalInterface
    interface TileVisitor {
        void tile(int z, int x, int y) throws IOException;
    }

    private void forEachTile(double[] bbox, int minZoom, int maxZoom, ExportJobEntity job, long[] done, long total,
            TileVisitor visitor) throws IOException {
        for (int z = minZoom; z <= maxZoom; z++) {
            int[] range = WebMercator.tileRange(z, bbox[0], bbox[1], bbox[2], bbox[3]);
            for (int x = range[0]; x <= range[2]; x++) {
                for (int y = range[1]; y <= range[3]; y++) {
                    visitor.tile(z, x, y);
                    if (++done[0] % 256 == 0) {
                        if (exports.cancelRequested(job.getId())) {
                            throw new ExportJobService.CanceledExport();
                        }
                        job.setProgress((int) Math.min(95, 5 + 90L * done[0] / Math.max(1, total)));
                        jobs.save(job);
                    }
                }
            }
        }
    }

    /** The number of tiles over {@code bbox} from {@code minZoom} to {@code maxZoom}. */
    static long countTiles(double[] bbox, int minZoom, int maxZoom) {
        long total = 0;
        for (int z = minZoom; z <= maxZoom; z++) {
            int[] range = WebMercator.tileRange(z, bbox[0], bbox[1], bbox[2], bbox[3]);
            long width = (long) range[2] - range[0] + 1;
            long height = (long) range[3] - range[1] + 1;
            total += Math.max(0, width) * Math.max(0, height);
        }
        return total;
    }

    private static byte[] gzip(byte[] raw) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(Math.max(32, raw.length / 2));
        try (GZIPOutputStream out = new GZIPOutputStream(bytes)) {
            out.write(raw);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bytes.toByteArray();
    }
}
