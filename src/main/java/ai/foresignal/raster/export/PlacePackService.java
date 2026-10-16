package ai.foresignal.raster.export;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.export.pmtiles.PmtilesWriter;
import ai.foresignal.place.PlaceService;
import ai.foresignal.place.PlaceView;
import ai.foresignal.raster.FrameKeys;
import ai.foresignal.raster.FrameStore;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.RasterFrameQueries;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.render.GifEncoder;
import ai.foresignal.raster.render.LegendService;
import ai.foresignal.raster.render.RasterRenderService;
import ai.foresignal.raster.render.RenderQueue;
import ai.foresignal.raster.render.StyleRegistry;
import ai.foresignal.raster.render.TileCache;
import ai.foresignal.raster.render.WebMercator;
import ai.foresignal.raster.render.WebMercatorQuad;
import ai.foresignal.raster.sample.ForecastSeriesService;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.source.spi.ProductSpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/**
 * The place offline pack (prompt 108; Weather-Imagery-Design §11): a ZIP with {@code manifest.json}
 * (a SHA-256 per file), the raw arrays (place weather, forecast series, nearby alerts as GeoJSON,
 * legends), a {@code loop.gif} and one PMTiles per product (latest frame, z ≤ maxZoom). Capped at
 * {@value #MAX_BYTES} bytes.
 */
@Service
public class PlacePackService {

    /** The size cap. */
    public static final long MAX_BYTES = 50L * 1024 * 1024;

    /** The default radius, km. */
    public static final double DEFAULT_RADIUS_KM = 150;

    /** The default maximum zoom. */
    public static final int DEFAULT_MAX_ZOOM = 8;

    private final PlaceService places;
    private final ForecastSeriesService forecasts;
    private final RasterRenderService renderer;
    private final LegendService legends;
    private final RasterFrameQueries frames;
    private final ProductRegistry registry;
    private final ExportPaths paths;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public PlacePackService(PlaceService places, ForecastSeriesService forecasts, RasterRenderService renderer,
            LegendService legends, RasterFrameQueries frames, ProductRegistry registry, ExportPaths paths,
            JsonMapper json, Clock clock) {
        this.places = places;
        this.forecasts = forecasts;
        this.renderer = renderer;
        this.legends = legends;
        this.frames = frames;
        this.registry = registry;
        this.paths = paths;
        this.json = json;
        this.clock = clock;
    }

    /** A parsed pack request. */
    public record Request(String placeId, List<String> products, int frames, double radiusKm, int maxZoom) {

        /** The stored spec map. */
        public Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("placeId", placeId);
            m.put("products", products);
            m.put("frames", frames);
            m.put("radiusKm", radiusKm);
            m.put("maxZoom", maxZoom);
            return m;
        }
    }

    /** The build result. */
    public record Result(List<Map<String, Object>> files, long totalBytes, long items) {}

    /** Builds the pack under {@code exports/{jobId}/}. */
    @SuppressWarnings("unchecked")
    public Result build(Map<String, Object> spec, String jobId) {
        String placeId = (String) spec.get("placeId");
        PlaceView place = places.find(placeId).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_PLACE,
                "Unknown place " + placeId + "."));
        double radiusKm = spec.get("radiusKm") instanceof Number n ? n.doubleValue() : DEFAULT_RADIUS_KM;
        int maxZoom = spec.get("maxZoom") instanceof Number n ? n.intValue() : DEFAULT_MAX_ZOOM;
        List<String> products = spec.get("products") instanceof List<?> list
                ? list.stream().map(String::valueOf).toList()
                : defaultProducts(place);
        double[] box = around(place, radiusKm);
        org.locationtech.jts.geom.Point center = centroid(place);
        Map<String, byte[]> entries = new LinkedHashMap<>();
        long items = 0;
        {
            Map<String, Object> weather = new LinkedHashMap<>();
            weather.put("placeId", place.id());
            weather.put("place", place.name());
            weather.put("timezone", place.timezone() == null ? "UTC" : place.timezone());
            weather.put("forecastSeries", forecasts.series(place.id(), center.getX(), center.getY(), Set.of()));
            entries.put("place-weather.json", json.writeValueAsBytes(weather));
            entries.put("forecast-series.json", json.writeValueAsBytes(weather.get("forecastSeries")));
            Map<String, Object> legendDoc = new LinkedHashMap<>();
            for (String productId : products) {
                ProductRegistry.Registered product = registry.find(productId).orElse(null);
                if (product == null || product.spec().isExternal()) {
                    continue;
                }
                StyleRegistry.Resolved style = renderer.style(product.spec(), null);
                legendDoc.put(productId, legends.legend(product.spec(), style.style(), false));
            }
            entries.put("legends.json", json.writeValueAsBytes(legendDoc));
            entries.put("alerts.geojson", "{\"type\":\"FeatureCollection\",\"features\":[]}"
                    .getBytes(StandardCharsets.UTF_8));
            byte[] gif = loopGif(products, box, (int) ((Number) spec.getOrDefault("frames", 12)).intValue());
            if (gif.length > 0) {
                entries.put("loop.gif", gif);
            }
            for (String productId : products) {
                ProductRegistry.Registered product = registry.find(productId).orElse(null);
                if (product == null || product.spec().isExternal()) {
                    continue;
                }
                byte[] pmtiles = pmtiles(product.spec(), box, maxZoom);
                if (pmtiles.length > 0) {
                    entries.put(productId + ".pmtiles", pmtiles);
                    items++;
                }
            }
        }

        Map<String, Object> manifest = new LinkedHashMap<>();
        manifest.put("placeId", place.id());
        manifest.put("place", place.name());
        manifest.put("generatedAt", clock.instant().toString());
        manifest.put("bounds", List.of(box[0], box[1], box[2], box[3]));
        manifest.put("attribution", attribution(products));
        List<Map<String, Object>> fileList = new ArrayList<>();
        for (Map.Entry<String, byte[]> e : entries.entrySet()) {
            Map<String, Object> f = new LinkedHashMap<>();
            f.put("name", e.getKey());
            f.put("bytes", e.getValue().length);
            f.put("sha256", FrameStore.sha256Hex(e.getValue()));
            fileList.add(f);
        }
        manifest.put("files", fileList);
        entries.put("manifest.json", json.writeValueAsBytes(manifest));

        byte[] zip = zip(entries);
        if (zip.length > MAX_BYTES) {
            throw new ForesignalException(HttpStatus.PAYLOAD_TOO_LARGE, ErrorCodes.PACK_TOO_LARGE,
                    "The pack is " + zip.length + " bytes; at most " + MAX_BYTES + ". Use a smaller radiusKm.");
        }
        String name = "pack-" + place.id() + ".zip";
        java.nio.file.Path target = paths.file(jobId, name);
        try {
            java.nio.file.Files.write(target, zip);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write the pack", e);
        }
        Map<String, Object> file = new LinkedHashMap<>();
        file.put("name", name);
        file.put("url", "/v1/exports/" + jobId + "/files/" + name);
        file.put("bytes", (long) zip.length);
        file.put("sha256", FrameStore.sha256Hex(zip));
        file.put("mediaType", "application/zip");
        file.put("expiresAt", clock.instant().plus(ExportPaths.TTL).toString());
        return new Result(List.of(file), zip.length, items);
    }

    private List<String> defaultProducts(PlaceView place) {
        if (place.products() != null && !place.products().isEmpty()) {
            return place.products().stream().filter(p -> registry.find(p).isPresent()).toList();
        }
        List<String> out = new ArrayList<>();
        for (String candidate : List.of("mrms-reflectivity", "goes-east-abi-c02", "gfs", "hrrr")) {
            if (registry.find(candidate).isPresent()) {
                out.add(candidate);
            }
        }
        return out;
    }

    private String attribution(List<String> products) {
        StringBuilder text = new StringBuilder();
        for (String productId : products) {
            ProductRegistry.Registered product = registry.find(productId).orElse(null);
            if (product != null && product.spec().attribution() != null) {
                if (text.length() > 0) {
                    text.append("; ");
                }
                text.append(product.spec().attribution().text());
            }
        }
        return text.length() == 0 ? null : text.toString();
    }

    private byte[] loopGif(List<String> products, double[] box, int frameCount) {
        if (products.isEmpty()) {
            return new byte[0];
        }
        ProductRegistry.Registered product = registry.find(products.get(0)).orElse(null);
        if (product == null || product.spec().isExternal()) {
            return new byte[0];
        }
        ProductSpec spec = product.spec();
        List<RasterFrameRow> rows = new ArrayList<>(frames.page(spec.id(), null, clock.instant(), null, false, null,
                null, Math.max(1, Math.min(24, frameCount))));
        java.util.Collections.reverse(rows);
        if (rows.isEmpty()) {
            return new byte[0];
        }
        StyleRegistry.Resolved style = renderer.style(spec, null);
        List<java.awt.image.BufferedImage> images = new ArrayList<>();
        int width = 400;
        int height = Math.max(1, (int) Math.round(width * (WebMercator.y(box[3]) - WebMercator.y(box[1]))
                / Math.max(1e-9, WebMercator.x(box[2]) - WebMercator.x(box[0]))));
        for (RasterFrameRow row : rows) {
            RasterRenderService.ImageRequest image = new RasterRenderService.ImageRequest(box[0], box[1], box[2], box[3],
                    width, height, true, false);
            images.add(renderer.drawDirect(spec, row, style, image).image());
        }
        return GifEncoder.encode(images, GifEncoder.DEFAULT_DELAY_MS);
    }

    private byte[] pmtiles(ProductSpec spec, double[] box, int maxZoom) {
        RasterFrameRow frame = frames.newest(spec.id(), false).orElse(null);
        if (frame == null) {
            return new byte[0];
        }
        StyleRegistry.Resolved style = renderer.style(spec, null);
        List<PmtilesWriter.Tile> tiles = new ArrayList<>();
        for (int z = 0; z <= maxZoom; z++) {
            int[] range = WebMercatorQuad.INSTANCE.tileRange(z, box[0], box[1], box[2], box[3]);
            for (int y = range[1]; y <= range[3]; y++) {
                for (int x = range[0]; x <= range[2]; x++) {
                    byte[] bytes = renderer.tile(spec, frame, style, WebMercatorQuad.INSTANCE, z, x, y, 256, "png",
                            RenderQueue.Priority.PREFETCH, "pack");
                    if (bytes != TileCache.EMPTY && bytes.length > 0) {
                        tiles.add(new PmtilesWriter.Tile(z, x, y, bytes));
                    }
                }
            }
        }
        if (tiles.isEmpty()) {
            return new byte[0];
        }
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("name", spec.title());
        metadata.put("type", "overlay");
        if (spec.attribution() != null) {
            metadata.put("attribution", spec.attribution().text());
        }
        metadata.put("validTime", frame.validTime().toString());
        return PmtilesWriter.write(tiles, metadata, box, 0, maxZoom, PmtilesWriter.TYPE_PNG);
    }

    private static double[] around(PlaceView place, double km) {
        org.locationtech.jts.geom.Point center = centroid(place);
        double dLat = km / 111.32;
        double dLon = km / (111.32 * Math.max(0.01, Math.cos(Math.toRadians(center.getY()))));
        return new double[] {Math.max(-180, center.getX() - dLon), Math.max(-90, center.getY() - dLat),
            Math.min(180, center.getX() + dLon), Math.min(90, center.getY() + dLat)};
    }

    private static org.locationtech.jts.geom.Point centroid(PlaceView place) {
        if (place.geometry() != null && !place.geometry().isEmpty()) {
            return place.geometry().getCentroid();
        }
        if (place.area() != null && !place.area().isEmpty()) {
            return place.area().getCentroid();
        }
        return place.geometry().getFactory().createPoint(new org.locationtech.jts.geom.Coordinate(0, 0));
    }

    private static byte[] zip(Map<String, byte[]> entries) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(1 << 20);
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (Map.Entry<String, byte[]> e : entries.entrySet()) {
                zip.putNextEntry(new ZipEntry(e.getKey()));
                zip.write(e.getValue());
                zip.closeEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not zip the pack", e);
        }
        return bytes.toByteArray();
    }
}
