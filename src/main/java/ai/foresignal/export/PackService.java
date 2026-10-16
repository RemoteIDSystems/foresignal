package ai.foresignal.export;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.export.pmtiles.PmtilesWriter;
import ai.foresignal.place.PlaceService;
import ai.foresignal.raster.FrameResolver;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.render.RasterRenderService;
import ai.foresignal.raster.render.StyleRegistry;
import ai.foresignal.security.Ulids;
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
import ai.foresignal.store.tiles.TileResult;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/**
 * PMTiles v3 offline packs (prompt 111): a vector (MVT) and/or raster (PNG) {@code .pmtiles} file for
 * a box and zoom range, built with {@link PmtilesWriter}, the {@code 016/037} tile queries and the
 * {@code 045} renderer, and downloaded like any export.
 */
@Service
public class PackService {

    private static final Logger log = LoggerFactory.getLogger(PackService.class);

    private final ExportJobRepository jobs;
    private final ExportStore store;
    private final ExportProperties properties;
    private final ExportLicenceService licences;
    private final ExportManifests manifests;
    private final SourceRegistry registry;
    private final TileLayerResolver layers;
    private final TileQueryService tiles;
    private final AsOfTileQueryService asOfTiles;
    private final AsOfResolver asOf;
    private final RasterRenderService renderer;
    private final ProductRegistry products;
    private final FrameResolver frames;
    private final PlaceService places;
    private final Clock clock;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    /** Creates the service. */
    public PackService(ExportJobRepository jobs, ExportStore store, ExportProperties properties,
            ExportLicenceService licences, ExportManifests manifests, SourceRegistry registry, TileLayerResolver layers,
            TileQueryService tiles, AsOfTileQueryService asOfTiles, AsOfResolver asOf, RasterRenderService renderer,
            ProductRegistry products, FrameResolver frames, PlaceService places, Clock clock) {
        this.jobs = jobs;
        this.store = store;
        this.properties = properties;
        this.licences = licences;
        this.manifests = manifests;
        this.registry = registry;
        this.layers = layers;
        this.tiles = tiles;
        this.asOfTiles = asOfTiles;
        this.asOf = asOf;
        this.renderer = renderer;
        this.products = products;
        this.frames = frames;
        this.places = places;
        this.clock = clock;
    }

    /** Creates a pack job. */
    @Transactional
    public ExportJobEntity create(PackSpec spec, ExportJobService.Caller caller) {
        double[] bbox = spec.bbox();
        if (bbox == null && spec.placeId() != null) {
            bbox = places.geometry(spec.placeId()).map(ExportQueryService::envelopeOf).orElse(null);
            if (bbox == null) {
                throw ValidationException.forField("placeId", "unknown place");
            }
        }
        if (bbox == null) {
            bbox = new double[] {-180, -85, 180, 85};
        }
        long tileCount = countTiles(bbox, spec.minZoom(), spec.maxZoom());
        if (tileCount > properties.packMaxTiles()) {
            throw new ForesignalException(HttpStatus.PAYLOAD_TOO_LARGE, ErrorCodes.PACK_TOO_LARGE,
                    "The pack would cover " + tileCount + " tiles, over packs.max-tiles ("
                            + properties.packMaxTiles() + ").")
                    .withProperty("tileCount", tileCount);
        }
        List<String> allowed = allowedVectorSources(spec, caller);
        Instant now = clock.instant();
        ExportJobEntity job = new ExportJobEntity(Ulids.next(now), ExportJobEntity.PACK, "pmtiles", now);
        job.setPrincipal(caller.principal());
        job.setPersonalUse(spec.personalUse());
        job.setExpiresAt(now.plus(properties.ttl()));
        Map<String, Object> specMap = new LinkedHashMap<>();
        specMap.put("name", spec.name());
        specMap.put("bbox", List.of(bbox[0], bbox[1], bbox[2], bbox[3]));
        specMap.put("minZoom", spec.minZoom());
        specMap.put("maxZoom", spec.maxZoom());
        specMap.put("includePrivate", spec.includePrivate());
        specMap.put("personalUse", spec.personalUse());
        if (spec.vector() != null) {
            Map<String, Object> vector = new LinkedHashMap<>();
            vector.put("sources", allowed);
            vector.put("kinds", spec.vector().kinds());
            vector.put("asOf", spec.vector().asOf() == null ? null : spec.vector().asOf().toString());
            specMap.put("vector", vector);
        }
        if (spec.raster() != null) {
            Map<String, Object> raster = new LinkedHashMap<>();
            raster.put("productId", spec.raster().productId());
            raster.put("frame", spec.raster().frame());
            specMap.put("raster", raster);
        }
        job.setSpec(specMap);
        jobs.save(job);
        executor.submit(() -> run(job.getId()));
        return job;
    }

    private List<String> allowedVectorSources(PackSpec spec, ExportJobService.Caller caller) {
        List<SourceDescriptor> candidates = new ArrayList<>();
        for (SourceDescriptor descriptor : registry.descriptors()) {
            if (spec.vector() != null && !spec.vector().sources().isEmpty()
                    && !spec.vector().sources().contains(descriptor.id())) {
                continue;
            }
            if (spec.vector() != null && !spec.vector().kinds().isEmpty()
                    && descriptor.kinds().stream().noneMatch(spec.vector().kinds()::contains)) {
                continue;
            }
            if (descriptor.privateSource() && !(caller.admin() && spec.includePrivate())) {
                continue;
            }
            candidates.add(descriptor);
        }
        return licences.summarize(candidates, spec.personalUse()).allowedSources();
    }

    private void run(String id) {
        ExportJobEntity job = jobs.findById(id).orElse(null);
        if (job == null) {
            return;
        }
        job.setStatus(ExportJobEntity.RUNNING);
        job.setStartedAt(clock.instant());
        job.setProgress(1);
        jobs.save(job);
        try {
            Map<String, Object> spec = job.getSpec();
            @SuppressWarnings("unchecked")
            List<Number> bboxRaw = (List<Number>) spec.get("bbox");
            double[] bbox = {bboxRaw.get(0).doubleValue(), bboxRaw.get(1).doubleValue(), bboxRaw.get(2).doubleValue(),
                bboxRaw.get(3).doubleValue()};
            int minZoom = ((Number) spec.get("minZoom")).intValue();
            int maxZoom = ((Number) spec.get("maxZoom")).intValue();
            Path dir = store.jobDir(id);
            List<ExportManifests.ExportFile> files = new ArrayList<>();
            long tileTotal = countTiles(bbox, minZoom, maxZoom);
            if (spec.get("vector") instanceof Map<?, ?> vectorSpec) {
                files.add(writeVector(dir, job, vectorSpec, bbox, minZoom, maxZoom, tileTotal));
            }
            if (spec.get("raster") instanceof Map<?, ?> rasterSpec) {
                files.add(writeRaster(dir, job, rasterSpec, bbox, minZoom, maxZoom, tileTotal));
            }
            long rows = files.stream().mapToLong(ExportManifests.ExportFile::rows).sum();
            long bytes = files.stream().mapToLong(ExportManifests.ExportFile::bytes).sum();
            job.setRowCount(rows);
            job.setBytes(bytes);
            String sha = manifests.write(dir, job, files, 0L, null, Map.of("licences", List.of()), List.of());
            job.setManifestSha256(sha);
            job.setStatus(ExportJobEntity.DONE);
            job.setProgress(100);
            job.setFinishedAt(clock.instant());
            jobs.save(job);
        } catch (IOException | RuntimeException e) {
            log.warn("Pack {} failed", id, e);
            job.setStatus(ExportJobEntity.FAILED);
            job.setError(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
            job.setFinishedAt(clock.instant());
            jobs.save(job);
        }
    }

    private ExportManifests.ExportFile writeVector(Path dir, ExportJobEntity job, Map<?, ?> vectorSpec, double[] bbox,
            int minZoom, int maxZoom, long tileTotal) throws IOException {
        @SuppressWarnings("unchecked")
        List<String> sources = (List<String>) vectorSpec.get("sources");
        @SuppressWarnings("unchecked")
        List<String> kinds = (List<String>) vectorSpec.get("kinds");
        String asOfRaw = (String) vectorSpec.get("asOf");
        AsOfPlan plan = asOfRaw == null ? null
                : asOf.plan(Instant.parse(asOfRaw), sources, kinds == null ? List.of() : kinds, Set.of());
        TileLayer layer = layers.viewLayer("pack", (String) job.getSpec().get("name"), sources, Set.of());
        List<PmtilesWriter.Tile> packed = new ArrayList<>();
        long[] done = {0};
        iterate(bbox, minZoom, maxZoom, (z, x, y) -> {
            TileCoordinates coordinates = new TileCoordinates(z, x, y);
            TileResult result = plan == null ? tiles.render(layer, coordinates, AggMode.NONE)
                    : asOfTiles.render(layer, coordinates, AggMode.NONE, plan,
                            ai.foresignal.security.restrict.ReadRestriction.NONE);
            byte[] mvt = result.mvt();
            if (mvt != null && mvt.length > 0) {
                packed.add(new PmtilesWriter.Tile(z, x, y, gzip(mvt)));
            }
            progress(job, ++done[0], tileTotal);
        });
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("name", job.getSpec().get("name"));
        metadata.put("type", "overlay");
        metadata.put("attribution", layer.attribution());
        metadata.put("generated", clock.instant().toString());
        metadata.put("vector_layers", vectorLayers(layer));
        byte[] archive = PmtilesWriter.write(packed, metadata, bbox, minZoom, maxZoom, PmtilesWriter.TYPE_MVT,
                PmtilesWriter.COMPRESSION_GZIP);
        Path file = dir.resolve("vector.pmtiles");
        Files.write(file, archive);
        return file(job, file, "application/vnd.pmtiles", packed.size());
    }

    private ExportManifests.ExportFile writeRaster(Path dir, ExportJobEntity job, Map<?, ?> rasterSpec, double[] bbox,
            int minZoom, int maxZoom, long tileTotal) throws IOException {
        String productId = (String) rasterSpec.get("productId");
        ProductRegistry.Registered product = products.require(productId);
        FrameResolver.Resolution resolution = frames.resolve(product.spec(),
                FrameResolver.LATEST, null, false, clock.instant(), k -> k);
        if (resolution instanceof FrameResolver.Redirect redirect) {
            resolution = frames.resolve(product.spec(), redirect.canonicalToken(), null, false, clock.instant(), k -> k);
        }
        RasterFrameRow frame = ((FrameResolver.Found) resolution).frame();
        StyleRegistry.Resolved style = renderer.style(product.spec(), null);
        List<PmtilesWriter.Tile> packed = new ArrayList<>();
        long[] done = {0};
        iterate(bbox, minZoom, maxZoom, (z, x, y) -> {
            byte[] png = renderer.tile(product.spec(), frame, style, z, x, y, 1,
                    ai.foresignal.raster.render.RenderQueue.Priority.PREFETCH, "pack");
            if (png != null && png.length > 0) {
                packed.add(new PmtilesWriter.Tile(z, x, y, png));
            }
            progress(job, ++done[0], tileTotal);
        });
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("name", job.getSpec().get("name"));
        metadata.put("type", "overlay");
        metadata.put("generated", clock.instant().toString());
        byte[] archive = PmtilesWriter.write(packed, metadata, bbox, minZoom, maxZoom, PmtilesWriter.TYPE_PNG,
                PmtilesWriter.COMPRESSION_NONE);
        Path file = dir.resolve("raster.pmtiles");
        Files.write(file, archive);
        return file(job, file, "application/vnd.pmtiles", packed.size());
    }

    private ExportManifests.ExportFile file(ExportJobEntity job, Path file, String mediaType, long rows)
            throws IOException {
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.WRITE)) {
            channel.force(true);
        }
        return new ExportManifests.ExportFile(file.getFileName().toString(), mediaType, Files.size(file),
                ExportManifests.sha256(file), rows);
    }

    private static List<Map<String, Object>> vectorLayers(TileLayer layer) {
        List<Map<String, Object>> out = new ArrayList<>();
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("id", layer.id());
        entry.put("fields", layer.properties().stream().collect(java.util.stream.Collectors.toMap(f -> f, f -> "String",
                (a, b) -> a, LinkedHashMap::new)));
        out.add(entry);
        return out;
    }

    private void progress(ExportJobEntity job, long done, long total) {
        if (total > 0 && done % 256 == 0) {
            job.setProgress((int) Math.min(95, 5 + (90L * done / total)));
        }
    }

    private long countTiles(double[] bbox, int minZoom, int maxZoom) {
        long total = 0;
        for (int z = minZoom; z <= maxZoom; z++) {
            int[] range = ai.foresignal.raster.render.WebMercator.tileRange(z, bbox[0], bbox[1], bbox[2], bbox[3]);
            long width = (long) range[2] - range[0] + 1;
            long height = (long) range[3] - range[1] + 1;
            total += Math.max(0, width) * Math.max(0, height);
        }
        return total;
    }

    private void iterate(double[] bbox, int minZoom, int maxZoom, TileVisitor visitor) {
        for (int z = minZoom; z <= maxZoom; z++) {
            int[] range = ai.foresignal.raster.render.WebMercator.tileRange(z, bbox[0], bbox[1], bbox[2], bbox[3]);
            for (int x = range[0]; x <= range[2]; x++) {
                for (int y = range[1]; y <= range[3]; y++) {
                    visitor.tile(z, x, y);
                }
            }
        }
    }

    /** A per-tile callback. */
    @FunctionalInterface
    private interface TileVisitor {
        void tile(int z, int x, int y);
    }

    private static byte[] gzip(byte[] raw) {
        java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream(raw.length / 2);
        try (GZIPOutputStream out = new GZIPOutputStream(bytes)) {
            out.write(raw);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bytes.toByteArray();
    }
}
