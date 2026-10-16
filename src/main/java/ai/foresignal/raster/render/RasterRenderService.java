package ai.foresignal.raster.render;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.raster.ElgCodec;
import ai.foresignal.raster.FrameStore;
import ai.foresignal.raster.GridDef;
import ai.foresignal.raster.GridStore;
import ai.foresignal.raster.ProductCatalogChangedEvent;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.RasterDtype;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Renders product tiles and images (prompt 045; Weather-Imagery-Design §6.3): checks the zoom and
 * style, then serves from the tile cache or renders through the {@link RenderQueue}, the cached
 * {@link IndexMap} of the frame's grid and the style compiled for the product. Pure reads of frames
 * Foresignal already stored: no request here ever causes an upstream fetch.
 */
@Service
public class RasterRenderService {

    /** The largest {@code /image} side, pixels. */
    public static final int MAX_IMAGE_SIDE = 2048;

    private final FrameStore store;
    private final GridStore grids;
    private final StyleRegistry styles;
    private final IndexMapCache indexMaps;
    private final TileCache tiles;
    private final RenderQueue queue;
    private final Map<String, GridDef> projections = new ConcurrentHashMap<>();

    /** Creates the service. */
    public RasterRenderService(FrameStore store, GridStore grids, StyleRegistry styles, IndexMapCache indexMaps,
            TileCache tiles, RenderQueue queue) {
        this.store = store;
        this.grids = grids;
        this.styles = styles;
        this.indexMaps = indexMaps;
        this.tiles = tiles;
        this.queue = queue;
    }

    /**
     * The style a request names, or the product's default.
     *
     * @throws ForesignalException {@code 400 UNKNOWN_STYLE}; {@code 400 VALIDATION_FAILED} when a
     *     passthrough style is asked of a non-rgba8 product
     */
    public StyleRegistry.Resolved style(ProductSpec spec, String param) {
        String ref = param == null || param.isBlank() ? defaultStyle(spec) : param.strip();
        if (ref == null) {
            throw StyleRegistry.unknown("(none)");
        }
        StyleRegistry.Resolved resolved = styles.resolve(ref);
        if (resolved.style().isPassthrough() && spec.dtype() != RasterDtype.RGBA8) {
            throw ValidationException.forField("style", resolved.ref() + " draws rgba8 frames only; " + spec.id()
                    + " is " + (spec.dtype() == null ? "external" : spec.dtype().wireValue()));
        }
        return resolved;
    }

    /** The product's default style name (as declared), or null. */
    public static String defaultStyle(ProductSpec spec) {
        if (spec.defaultStyle() != null) {
            return spec.defaultStyle();
        }
        return spec.styles().isEmpty() ? null : spec.styles().get(0);
    }

    /**
     * Refuses an external product: clients fetch its template directly.
     *
     * @throws ValidationException (400 {@code VALIDATION_FAILED})
     */
    public static void requireRendered(ProductSpec spec) {
        if (spec.isExternal()) {
            throw ValidationException.forField("productId", "external product: fetch externalTemplate directly");
        }
    }

    /**
     * Checks a tile address against the product's zoom range.
     *
     * @throws ForesignalException {@code 400 TILE_OUT_OF_RANGE} above the native max zoom, below the
     *     min zoom, or outside the zoom's tile range
     */
    public static void checkTile(ProductSpec spec, int z, int x, int y) {
        int min = spec.spatial() == null || spec.spatial().minZoom() == null ? 0 : spec.spatial().minZoom();
        int max = spec.spatial() == null || spec.spatial().nativeMaxZoom() == null ? 0 : spec.spatial().nativeMaxZoom();
        if (z < min || z > max) {
            throw new ForesignalException(HttpStatus.BAD_REQUEST, ErrorCodes.TILE_OUT_OF_RANGE, "Zoom " + z + " is outside "
                    + spec.id() + "'s native range " + min + "–" + max + "; overzoom the max-zoom tiles instead.")
                    .withProperty("minzoom", min).withProperty("maxzoom", max);
        }
        int n = WebMercator.tiles(z);
        if (x < 0 || y < 0 || x >= n || y >= n) {
            throw new ForesignalException(HttpStatus.BAD_REQUEST, ErrorCodes.TILE_OUT_OF_RANGE,
                    "Tile " + z + "/" + x + "/" + y + " does not exist at zoom " + z + ".");
        }
    }

    /** Checks a tile address against the product's zoom range in a tile matrix set (prompt 108). */
    public static void checkTile(ProductSpec spec, TileMatrixSet tms, int z, int x, int y) {
        int min = spec.spatial() == null || spec.spatial().minZoom() == null ? 0 : spec.spatial().minZoom();
        int max = spec.spatial() == null || spec.spatial().nativeMaxZoom() == null ? 0 : spec.spatial().nativeMaxZoom();
        if (z < min || z > max) {
            throw new ForesignalException(HttpStatus.BAD_REQUEST, ErrorCodes.TILE_OUT_OF_RANGE, "Zoom " + z + " is outside "
                    + spec.id() + "'s native range " + min + "–" + max + "; overzoom the max-zoom tiles instead.")
                    .withProperty("minzoom", min).withProperty("maxzoom", max);
        }
        if (x < 0 || y < 0 || x >= tms.tilesX(z) || y >= tms.tilesY(z)) {
            throw new ForesignalException(HttpStatus.BAD_REQUEST, ErrorCodes.TILE_OUT_OF_RANGE,
                    "Tile " + z + "/" + x + "/" + y + " does not exist at zoom " + z + " in " + tms.id() + ".");
        }
    }

    /**
     * A tile in {@code tms} at {@code size} pixels, from the cache or rendered through the queue
     * (prompt 108): {@code size} is 256 (base) or 512 (the native 512 px pyramid; {@code z} is the same
     * as the 256 tile it covers).
     */
    public byte[] tile(ProductSpec spec, RasterFrameRow frame, StyleRegistry.Resolved style, TileMatrixSet tms, int z,
            int x, int y, int size, String format, RenderQueue.Priority priority, String client) {
        TileCache.Key key = new TileCache.Key(spec.id(), frame.frameKey(), style.ref(),
                "tms:" + tms.id() + ":" + z + "/" + x + "/" + y + "/" + size + ":" + format);
        byte[] cached = tiles.get(key);
        if (cached != null) {
            return cached;
        }
        return queue.render(key, priority, client, () -> renderTile(spec, frame, style, tms, z, x, y, size, format, key));
    }

    /** Renders one tile in a tile matrix set and caches it ({@code format} is {@code png} or {@code webp}). */
    byte[] renderTile(ProductSpec spec, RasterFrameRow frame, StyleRegistry.Resolved style, TileMatrixSet tms, int z,
            int x, int y, int size, String format, TileCache.Key key) {
        byte[] cached = tiles.get(key);
        if (cached != null) {
            return cached;
        }
        GridDef grid = projection(frame.gridId());
        byte[] out;
        if (!intersects(tms.tileBounds(z, x, y), grid.bounds())) {
            out = TileCache.EMPTY;
        } else {
            IndexMap map = indexMaps.tile(frame.gridId(), grid, tms, z, x, y, size, spec.bilinear());
            if (map.isEmpty()) {
                out = TileCache.EMPTY;
            } else {
                TileRenderer.Drawn drawn = TileRenderer.draw(store.read(frame.path()),
                        styles.compiled(style.style(), spec), map);
                out = drawn.empty() ? TileCache.EMPTY
                        : "webp".equals(format) ? drawn.webp() : drawn.png();
            }
        }
        tiles.put(key, out);
        return out;
    }

    /**
     * A data tile (prompt 108; Weather-Imagery-Design §11): band 0's physical values as RGB for
     * MapLibre {@code raster-dem} and client-side colouring, encoded by {@link DataTileEncoder} with
     * {@code nearest} sampling only. Served from the cache or rendered through the queue.
     *
     * @param encoding {@link DataTileEncoder#CUSTOM} or {@link DataTileEncoder#TERRARIUM}
     * @param signed true for an i16 frame (codes shifted into the unsigned field)
     * @return PNG bytes, or {@link TileCache#EMPTY} when every pixel has no data
     */
    public byte[] dataTile(ProductSpec spec, RasterFrameRow frame, TileMatrixSet tms, int z, int x, int y, int size,
            DataTileEncoder.Encoding encoding, RenderQueue.Priority priority, String client) {
        TileCache.Key key = new TileCache.Key(spec.id(), frame.frameKey(), "data",
                "tms:" + tms.id() + ":" + z + "/" + x + "/" + y + "/" + size + ":" + encoding.encoding());
        byte[] cached = tiles.get(key);
        if (cached != null) {
            return cached;
        }
        return queue.render(key, priority, client, () -> {
            byte[] hit = tiles.get(key);
            if (hit != null) {
                return hit;
            }
            GridDef grid = projection(frame.gridId());
            byte[] out = TileCache.EMPTY;
            if (intersects(tms.tileBounds(z, x, y), grid.bounds())) {
                IndexMap map = indexMaps.tile(frame.gridId(), grid, tms, z, x, y, size, false);
                if (!map.isEmpty()) {
                    int[] codes = TileRenderer.codes(store.read(frame.path()), map);
                    out = DataTileEncoder.encode(codes, size, size, spec.scale(), spec.offset(), encoding.signed(),
                            encoding.encoding()).png();
                }
            }
            tiles.put(key, out);
            return out;
        });
    }

    /**
     * A tile: from the cache, or rendered through the queue.
     *
     * @param scale 1 (256 px) or 2 (512 px, {@code @2x})
     * @return PNG bytes, or {@link TileCache#EMPTY} when every pixel is transparent
     * @throws ForesignalException {@code 503 RENDER_BUSY} when the queue sheds the request
     */
    public byte[] tile(ProductSpec spec, RasterFrameRow frame, StyleRegistry.Resolved style, int z, int x, int y,
            int scale, RenderQueue.Priority priority, String client) {
        TileCache.Key key = TileCache.Key.tile(spec.id(), frame.frameKey(), style.ref(), z, x, y, scale);
        byte[] cached = tiles.get(key);
        if (cached != null) {
            return cached;
        }
        return queue.render(key, priority, client, () -> renderTile(spec, frame, style, z, x, y, scale, key));
    }

    /** Queues a tile for warming without waiting; null when it is already cached. */
    public CompletableFuture<byte[]> warm(ProductSpec spec, RasterFrameRow frame, StyleRegistry.Resolved style, int z,
            int x, int y, int scale) {
        TileCache.Key key = TileCache.Key.tile(spec.id(), frame.frameKey(), style.ref(), z, x, y, scale);
        if (tiles.get(key) != null) {
            return null;
        }
        return queue.submit(key, RenderQueue.Priority.WARM, "warm",
                () -> renderTile(spec, frame, style, z, x, y, scale, key));
    }

    /** Renders one tile and caches it. */
    byte[] renderTile(ProductSpec spec, RasterFrameRow frame, StyleRegistry.Resolved style, int z, int x, int y,
            int scale, TileCache.Key key) {
        byte[] cached = tiles.get(key);
        if (cached != null) {
            return cached;
        }
        GridDef grid = projection(frame.gridId());
        byte[] out;
        if (!intersects(WebMercator.tileBounds(z, x, y), grid.bounds())) {
            out = TileCache.EMPTY;
        } else {
            IndexMap map = indexMaps.tile(frame.gridId(), grid, z, x, y, scale, spec.bilinear());
            if (map.isEmpty()) {
                out = TileCache.EMPTY;
            } else {
                TileRenderer.Drawn drawn = TileRenderer.draw(store.read(frame.path()),
                        styles.compiled(style.style(), spec), map);
                out = drawn.empty() ? TileCache.EMPTY : drawn.png();
            }
        }
        tiles.put(key, out);
        return out;
    }

    /**
     * An image request, its bounds already snapped.
     *
     * @param west snapped west, degrees
     * @param south snapped south
     * @param east snapped east
     * @param north snapped north
     * @param width pixels
     * @param height pixels
     * @param mercator EPSG:3857 output, else EPSG:4326
     * @param jpeg JPEG rather than PNG
     */
    public record ImageRequest(double west, double south, double east, double north, int width, int height,
            boolean mercator, boolean jpeg) {

        /** The cache region string. */
        String region() {
            return "img:" + (mercator ? "3857" : "4326") + ":" + west + "," + south + "," + east + "," + north + ":"
                    + width + "x" + height + ":" + (jpeg ? "jpg" : "png");
        }

        /** The {@code Foresignal-Image-Bounds} value, {@code w,s,e,n}. */
        public String boundsHeader() {
            return fmt(west) + "," + fmt(south) + "," + fmt(east) + "," + fmt(north);
        }

        private static String fmt(double v) {
            return java.math.BigDecimal.valueOf(v).stripTrailingZeros().toPlainString();
        }
    }

    /** The image's cache key. */
    public static TileCache.Key imageKey(ProductSpec spec, RasterFrameRow frame, StyleRegistry.Resolved style,
            ImageRequest request) {
        return new TileCache.Key(spec.id(), frame.frameKey(), style.ref(), request.region());
    }

    /** The cached bytes of an image or tile, or null (the loop reports sizes already known). */
    public byte[] cached(TileCache.Key key) {
        return tiles.get(key);
    }

    /**
     * An image over a box: from the cache, or rendered through the queue. An empty image is still a
     * full-size transparent image.
     *
     * @throws ForesignalException {@code 503 RENDER_BUSY}
     */
    public byte[] image(ProductSpec spec, RasterFrameRow frame, StyleRegistry.Resolved style, ImageRequest request,
            RenderQueue.Priority priority, String client) {
        return image(spec, frame, style, request, request.jpeg() ? "jpg" : "png", priority, client);
    }

    /** An image over a box in {@code format} ({@code png}, {@code jpg} or {@code webp}), prompt 108. */
    public byte[] image(ProductSpec spec, RasterFrameRow frame, StyleRegistry.Resolved style, ImageRequest request,
            String format, RenderQueue.Priority priority, String client) {
        TileCache.Key key = new TileCache.Key(spec.id(), frame.frameKey(), style.ref(), request.region() + ":" + format);
        byte[] cached = tiles.get(key);
        if (cached != null) {
            return cached;
        }
        Supplier<byte[]> work = () -> {
            GridDef grid = projection(frame.gridId());
            IndexMap map = indexMaps.image(frame.gridId(), grid, new IndexMapCache.ImageKey(frame.gridId(),
                    request.mercator(), request.west(), request.south(), request.east(), request.north(),
                    request.width(), request.height(), spec.bilinear()));
            TileRenderer.Drawn drawn = TileRenderer.draw(store.read(frame.path()), styles.compiled(style.style(), spec),
                    map);
            byte[] out = switch (format) {
                case "webp" -> drawn.webp();
                case "jpg", "jpeg" -> drawn.jpeg();
                default -> drawn.png();
            };
            tiles.put(key, out);
            return out;
        };
        return queue.render(key, priority, client, work);
    }

    /**
     * Samples band 0's physical values over an image box (prompt 108 COG exports): one value per pixel,
     * {@code NaN} where there is no cell. Nearest sampling; no queue and no cache.
     */
    public float[] values(ProductSpec spec, RasterFrameRow frame, double west, double south, double east,
            double north, int width, int height, boolean mercator) {
        GridDef grid = projection(frame.gridId());
        IndexMap map = indexMaps.image(frame.gridId(), grid, new IndexMapCache.ImageKey(frame.gridId(), mercator, west,
                south, east, north, width, height, false));
        ElgCodec.Decoded decoded = store.read(frame.path());
        RasterDtype dtype = decoded.header().dtype();
        java.nio.ByteBuffer data = java.nio.ByteBuffer.wrap(decoded.payload())
                .order(java.nio.ByteOrder.LITTLE_ENDIAN);
        int[] nearest = map.nearest();
        float[] out = new float[width * height];
        double nodata = spec.nodata() == null ? Double.NaN : spec.nodata();
        for (int p = 0; p < out.length; p++) {
            int cell = nearest[p];
            if (cell < 0) {
                out[p] = Float.NaN;
                continue;
            }
            double code = TileRenderer.code(data, dtype, cell);
            double physical = spec.physical(code);
            out[p] = !Double.isFinite(code) || code == nodata ? Float.NaN : (float) physical;
        }
        return out;
    }

    /**
     * Draws one frame over an image box on the calling thread, without the queue or the tile cache
     * (prompt 055: {@link MapRenderer} composites overlays inside its own render job, which already holds a
     * render thread; going through the queue again could wait on itself).
     *
     * @return the drawn raster (palette indices or passthrough colors)
     */
    public TileRenderer.Drawn drawDirect(ProductSpec spec, RasterFrameRow frame, StyleRegistry.Resolved style,
            ImageRequest request) {
        GridDef grid = projection(frame.gridId());
        IndexMap map = indexMaps.image(frame.gridId(), grid, new IndexMapCache.ImageKey(frame.gridId(),
                request.mercator(), request.west(), request.south(), request.east(), request.north(),
                request.width(), request.height(), spec.bilinear()));
        return TileRenderer.draw(store.read(frame.path()), styles.compiled(style.style(), spec), map);
    }

    /** The projection of a stored grid. */
    public GridDef projection(String gridId) {
        return projections.computeIfAbsent(gridId, id -> {
            GridDefinition definition = grids.find(id).orElseThrow(() -> new IllegalStateException(
                    "Grid " + id + " of a stored frame is missing from raster_grid"));
            return GridDef.of(definition);
        });
    }

    /** Drops a redefined product's cached tiles and compiled styles. */
    @EventListener
    public void onCatalogChanged(ProductCatalogChangedEvent event) {
        tiles.invalidateProduct(event.productId());
        styles.forgetProduct(event.productId());
    }

    /** True when two WGS84 boxes ({@code west > east} across the antimeridian) intersect. */
    public static boolean intersects(double[] a, double[] b) {
        for (double[] pa : split(a)) {
            for (double[] pb : split(b)) {
                if (pa[0] <= pb[2] && pb[0] <= pa[2] && pa[1] <= pb[3] && pb[1] <= pa[3]) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<double[]> split(double[] box) {
        List<double[]> out = new ArrayList<>();
        if (box[0] > box[2]) {
            out.add(new double[] {box[0], box[1], 180, box[3]});
            out.add(new double[] {-180, box[1], box[2], box[3]});
        } else {
            out.add(box);
        }
        return out;
    }
}
