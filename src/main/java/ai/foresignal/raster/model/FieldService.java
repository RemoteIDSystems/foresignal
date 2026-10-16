package ai.foresignal.raster.model;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.raster.FrameKeys;
import ai.foresignal.raster.FrameResolver;
import ai.foresignal.raster.FrameStore;
import ai.foresignal.raster.GridDef;
import ai.foresignal.raster.ProductSpecJson;
import ai.foresignal.raster.ProductUrls;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.render.ImageEncoding;
import ai.foresignal.raster.render.IndexMap;
import ai.foresignal.raster.render.TileCache;
import ai.foresignal.raster.render.TileMatrixSet;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.ProductSpec;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * The wind field endpoints (prompt 051; Weather-Imagery-Design §6.4): the God's Eye View
 * {@code schemaVersion: 1} manifest and the planar Float32 data, whole or cut to a box.
 *
 * <p>Fields exist for a {@code kind=field} product with two bands ({@code u}, {@code v}) and global
 * coverage: {@code gfs} in v1. The data is the precompressed file the pipeline wrote at ingest
 * ({@link FieldStore}), served as is with {@code Content-Encoding: gzip}. A {@code bbox} is snapped
 * outward to grid points, wraps across 0° (a box from 10° W to 10° E reads columns 350..359 then
 * 0..10), and is cut on the fly from the file of the requested resolution; cut subsets (and their
 * hashes) are cached in the tile cache ({@code tileCacheMb}).
 */
@Service
public class FieldService {

    /** The manifest's schema version (God's Eye View). */
    public static final int SCHEMA_VERSION = 1;

    /** The level of the wind fields. */
    public static final String LEVEL = "10 m above ground";

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ROOT);

    private final FieldStore store;
    private final TileCache cache;
    private final FrameResolver resolver;

    /** Creates the service. */
    public FieldService(FieldStore store, TileCache cache, FrameResolver resolver) {
        this.store = store;
        this.cache = cache;
        this.resolver = resolver;
    }

    /** True when {@code spec} has wind fields: {@code kind=field}, two bands, global coverage. */
    public static boolean hasFields(ProductSpec spec) {
        return ProductSpec.KIND_FIELD.equals(spec.kind()) && spec.bandCount() == 2 && spec.spatial() != null
                && "global".equals(spec.spatial().coverage());
    }

    /**
     * Refuses a product without wind fields.
     *
     * @throws ValidationException 400 {@code VALIDATION_FAILED}
     */
    public static void requireFields(ProductSpec spec) {
        if (!hasFields(spec)) {
            throw ValidationException.forField("productId", spec.id()
                    + " has no wind field data (fields are served for gfs only); use /vectors or style=wind-speed@1");
        }
    }

    /**
     * Parses {@code res}.
     *
     * @return {@value FieldStore#RES_1} (the default) or {@value FieldStore#RES_QUARTER}
     */
    public static String resolution(String raw) {
        if (raw == null || raw.isBlank()) {
            return FieldStore.RES_1;
        }
        String r = raw.strip();
        if (r.equals("1") || r.equals("1.0")) {
            return FieldStore.RES_1;
        }
        if (r.equals("0.25") || r.equals(".25")) {
            return FieldStore.RES_QUARTER;
        }
        throw ValidationException.forField("res", "must be 1 or 0.25");
    }

    /**
     * A subset of the God's Eye View grid, snapped outward to grid points.
     *
     * @param col0 first column (0..nx−1); columns wrap
     * @param row0 first row
     * @param cols columns
     * @param rows rows
     * @param dx column step, degrees
     * @param dy row step, degrees
     * @param whole true when the subset is the whole grid
     */
    public record Window(int col0, int row0, int cols, int rows, double dx, double dy, boolean whole) {

        /** The first column's longitude, 0..360. */
        public double lo1() {
            return round(col0 * dx);
        }

        /** The first row's latitude. */
        public double la1() {
            return round(90 - row0 * dy);
        }

        /** The snapped box {@code w,s,e,n} in −180..180 ({@code w > e} across the antimeridian). */
        public double[] box() {
            double west = normalizeLon(col0 * dx);
            double east = normalizeLon((col0 + cols - 1) * dx);
            return new double[] {west, round(90 - (row0 + rows - 1) * dy), east, la1()};
        }

        /** The snapped box as {@code bbox=} text, or null for the whole grid. */
        public String boxText() {
            if (whole) {
                return null;
            }
            double[] b = box();
            return plain(b[0]) + "," + plain(b[1]) + "," + plain(b[2]) + "," + plain(b[3]);
        }

        /** The raw byte length (u then v, Float32). */
        public int byteLength() {
            return cols * rows * 8;
        }
    }

    /**
     * The window of {@code bbox} on a grid of {@code nx × ny} points at {@code dx, dy} from
     * {@code lo1 = 0, la1 = 90}.
     *
     * @param bbox {@code [w, s, e, n]} in WGS84 degrees ({@code w > e} crosses the antimeridian), or
     *     null for the whole grid
     */
    public static Window window(double[] bbox, int nx, int ny, double dx, double dy) {
        if (bbox == null) {
            return new Window(0, 0, nx, ny, dx, dy, true);
        }
        double w = bbox[0];
        double e = bbox[2];
        double span = e > w ? e - w : e - w + 360;
        double wl = ((w % 360) + 360) % 360;
        int col0 = (int) Math.floor(wl / dx + 1e-9) % nx;
        int cols = (int) Math.ceil((wl + span) / dx - 1e-9) - (int) Math.floor(wl / dx + 1e-9) + 1;
        if (cols >= nx) {
            col0 = 0;
            cols = nx;
        }
        int row0 = Math.max(0, (int) Math.floor((90 - bbox[3]) / dy + 1e-9));
        int row1 = Math.min(ny - 1, (int) Math.ceil((90 - bbox[1]) / dy - 1e-9));
        if (row1 < row0) {
            throw ValidationException.forField("bbox", "is empty on the field grid");
        }
        return new Window(col0, row0, cols, row1 - row0 + 1, dx, dy, cols == nx && row0 == 0 && row1 == ny - 1);
    }

    /**
     * Cuts a window out of planar u/v Float32 data.
     *
     * @param raw the whole grid's bytes
     * @param nx the whole grid's columns
     * @param ny the whole grid's rows
     */
    public static byte[] cut(byte[] raw, int nx, int ny, Window window) {
        if (raw.length != nx * ny * 8) {
            throw new IllegalArgumentException("Field data is " + raw.length + " bytes; a " + nx + "x" + ny
                    + " grid needs " + (nx * ny * 8));
        }
        ByteBuffer in = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer out = ByteBuffer.allocate(window.byteLength()).order(ByteOrder.LITTLE_ENDIAN);
        int cells = nx * ny;
        for (int band = 0; band < 2; band++) {
            for (int r = 0; r < window.rows(); r++) {
                int row = window.row0() + r;
                for (int c = 0; c < window.cols(); c++) {
                    int col = (window.col0() + c) % nx;
                    out.putFloat(in.getFloat((band * cells + row * nx + col) * 4));
                }
            }
        }
        return out.array();
    }

    /**
     * The data to serve: compressed bytes, the raw hash and the window.
     *
     * @param gzip the gzip bytes
     * @param sha256 the SHA-256 of the raw bytes
     * @param window the window
     */
    public record FieldData(byte[] gzip, String sha256, Window window) {}

    /**
     * The field data of a frame.
     *
     * @param bbox the requested box, or null for the whole grid
     * @throws ForesignalException 404 {@code UNKNOWN_FRAME} when the frame has no field files
     */
    public FieldData data(ProductSpec spec, RasterFrameRow frame, String res, double[] bbox) {
        FieldStore.StoredField stored = stored(spec, frame, res);
        Window window = window(bbox, stored.nx(), stored.ny(), stored.dx(), stored.dy());
        if (window.whole()) {
            return new FieldData(store.readGzip(stored.path()), stored.sha256(), window);
        }
        String region = "field:" + res + ":" + window.boxText();
        TileCache.Key gzKey = new TileCache.Key(spec.id(), frame.frameKey(), "f32", region);
        TileCache.Key shaKey = new TileCache.Key(spec.id(), frame.frameKey(), "f32-sha256", region);
        byte[] gz = cache.get(gzKey);
        byte[] sha = cache.get(shaKey);
        if (gz == null || sha == null) {
            byte[] subset = cut(store.readRaw(stored.path()), stored.nx(), stored.ny(), window);
            gz = FieldStore.gzip(subset);
            sha = FrameStore.sha256Hex(subset).getBytes(StandardCharsets.US_ASCII);
            cache.put(gzKey, gz);
            cache.put(shaKey, sha);
        }
        return new FieldData(gz, new String(sha, StandardCharsets.US_ASCII), window);
    }

    /**
     * The stored file of a frame at {@code res}.
     *
     * @throws ForesignalException 404 {@code UNKNOWN_FRAME} when there is none
     */
    FieldStore.StoredField stored(ProductSpec spec, RasterFrameRow frame, String res) {
        FieldStore.StoredField stored = FieldStore.fromStats(frame.stats()).get(res);
        if (stored == null || !store.exists(stored.path())) {
            throw new ForesignalException(HttpStatus.NOT_FOUND, ErrorCodes.UNKNOWN_FRAME, "Frame " + frame.frameKey()
                    + " of " + spec.id() + " has no " + res + "° wind field.");
        }
        return stored;
    }

    /**
     * The God's Eye View manifest (OpenAPI {@code FieldManifest}).
     *
     * @param base the URL base
     * @param model the model name ({@code gfs})
     * @param data the data the manifest describes
     */
    public Map<String, Object> manifest(String base, ProductSpec spec, String model, RasterFrameRow frame, String res,
            FieldData data) {
        Window w = data.window();
        Instant run = frame.referenceTime();
        ZonedDateTime r = run.atZone(ZoneOffset.UTC);
        Map<String, Object> cycle = new LinkedHashMap<>();
        cycle.put("date", DAY.format(r));
        cycle.put("hour", r.getHour());
        cycle.put("forecastHour", frame.forecastStep());
        cycle.put("runIso", FrameKeys.runIso(run));
        cycle.put("validIso", frame.validTime().toString());
        Map<String, Object> grid = new LinkedHashMap<>();
        grid.put("nx", w.cols());
        grid.put("ny", w.rows());
        grid.put("lo1", w.lo1());
        grid.put("la1", w.la1());
        grid.put("dx", w.dx());
        grid.put("dy", w.dy());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("schemaVersion", SCHEMA_VERSION);
        out.put("model", model);
        out.put("cycle", cycle);
        out.put("level", LEVEL);
        out.put("units", "m/s");
        out.put("grid", grid);
        out.put("stale", stale(spec, frame));
        out.put("unavailable", false);
        out.put("reason", null);
        out.put("components", List.of(Map.of("name", "u", "offset", 0),
                Map.of("name", "v", "offset", w.cols() * w.rows() * 4)));
        out.put("byteLength", w.byteLength());
        out.put("sha256", data.sha256());
        out.put("attribution", spec.attribution() == null ? null : ProductSpecJson.attribution(spec.attribution()));
        out.put("dataUrl", dataUrl(base, spec, frame, res, w));
        out.put("encodings", encodings(base, spec, frame, res, w, data));
        return out;
    }

    /** The alternative field encodings (prompt 106): {@code velocity-json} and {@code png-uv}. */
    static Map<String, Object> encodings(String base, ProductSpec spec, RasterFrameRow frame, String res, Window w,
            FieldData data) {
        double[] uv = uvBounds(FieldStore.gunzip(data.gzip()));
        Map<String, Object> png = new LinkedHashMap<>();
        png.put("url", encodingUrl(base, spec, frame, res, w, "png-uv"));
        png.put("uMin", uv[0]);
        png.put("uMax", uv[1]);
        png.put("vMin", uv[2]);
        png.put("vMax", uv[3]);
        png.put("width", w.cols());
        png.put("height", w.rows());
        double[] box = w.box();
        png.put("bounds", List.of(box[0], box[1], box[2], box[3]));
        Map<String, Object> i16 = new LinkedHashMap<>();
        i16.put("url", encodingUrl(base, spec, frame, res, w, "i16"));
        i16.put("uScale", I16FieldEncoder.scale(uv[0], uv[1]));
        i16.put("uOffset", I16FieldEncoder.offset(uv[0], I16FieldEncoder.scale(uv[0], uv[1])));
        i16.put("vScale", I16FieldEncoder.scale(uv[2], uv[3]));
        i16.put("vOffset", I16FieldEncoder.offset(uv[2], I16FieldEncoder.scale(uv[2], uv[3])));
        i16.put("width", w.cols());
        i16.put("height", w.rows());
        Map<String, Object> tiles = new LinkedHashMap<>();
        tiles.put("url", ProductUrls.product(base, spec.id()) + "/fields/" + frame.frameKey() + "/{z}/{x}/{y}.png?run="
                + FrameKeys.runIso(frame.referenceTime()) + "&res=" + res);
        tiles.put("uMin", uv[0]);
        tiles.put("uMax", uv[1]);
        tiles.put("vMin", uv[2]);
        tiles.put("vMax", uv[3]);
        tiles.put("tileSize", 256);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("velocityJson", Map.of("url", encodingUrl(base, spec, frame, res, w, "velocity-json")));
        out.put("pngUv", png);
        out.put("i16", i16);
        out.put("pngTiles", tiles);
        return out;
    }

    /** {@code {uMin, uMax, vMin, vMax}} over planar u/v Float32 bytes. */
    static double[] uvBounds(byte[] raw) {
        ByteBuffer in = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
        int cells = raw.length / 8;
        double uMin = Double.POSITIVE_INFINITY;
        double uMax = Double.NEGATIVE_INFINITY;
        double vMin = Double.POSITIVE_INFINITY;
        double vMax = Double.NEGATIVE_INFINITY;
        for (int k = 0; k < cells; k++) {
            float u = in.getFloat(k * 4);
            float v = in.getFloat((cells + k) * 4);
            if (Float.isFinite(u)) {
                uMin = Math.min(uMin, u);
                uMax = Math.max(uMax, u);
            }
            if (Float.isFinite(v)) {
                vMin = Math.min(vMin, v);
                vMax = Math.max(vMax, v);
            }
        }
        if (!(uMax >= uMin)) {
            uMin = 0;
            uMax = 1;
        }
        if (!(vMax >= vMin)) {
            vMin = 0;
            vMax = 1;
        }
        return new double[] {uMin, uMax, vMin, vMax};
    }

    /**
     * The {@code .i16} encoding of a window (prompt 108): planar little-endian int16 {@code u} then
     * {@code v} with per-component scale/offset.
     */
    public I16FieldEncoder.Result i16(FieldData data) {
        return I16FieldEncoder.encode(FieldStore.gunzip(data.gzip()), data.window().cols(), data.window().rows());
    }

    /** The lat/lon grid of a stored field (the God's Eye View layout: {@code lo1=0, la1=90}). */
    public GridDef fieldGrid(FieldStore.StoredField stored) {
        double north = 90.0;
        double south = 90.0 - (stored.ny() - 1) * stored.dy();
        return GridDef.of(new GridDefinition(GridDefinition.LATLON, stored.nx(), stored.ny(), -180.0, south, 180.0,
                north, Map.of("lo1", 0.0, "la1", 90.0, "dx", stored.dx(), "dy", stored.dy())));
    }

    /**
     * A {@code png-uv} tile of the wind field at {@code (z, x, y)} in {@code tms} (prompt 108): sample
     * the stored field grid with {@code nearest} sampling and reuse the 106 encoder with the field's
     * global min/max.
     */
    public PngUvEncoder.Result pngTile(ProductSpec spec, RasterFrameRow frame, String res, TileMatrixSet tms, int z,
            int x, int y, int size) {
        FieldStore.StoredField stored = stored(spec, frame, res);
        GridDef grid = fieldGrid(stored);
        if (!ai.foresignal.raster.render.RasterRenderService.intersects(tms.tileBounds(z, x, y), grid.bounds())) {
            throw new ForesignalException(HttpStatus.NOT_FOUND, ErrorCodes.UNKNOWN_FRAME,
                    "Tile " + z + "/" + x + "/" + y + " does not intersect the field grid.");
        }
        IndexMap map = IndexMap.build(grid, size, size, tms.locator(z, x, y, size), false);
        byte[] raw = store.readRaw(stored.path());
        int cells = stored.nx() * stored.ny();
        double[] uv = uvBounds(raw);
        int[] nearest = map.nearest();
        int[] argb = new int[size * size];
        ByteBuffer in = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
        for (int p = 0; p < argb.length; p++) {
            int cell = nearest[p];
            if (cell < 0) {
                continue;
            }
            float u = in.getFloat(cell * 4);
            float v = in.getFloat((cells + cell) * 4);
            int r = PngUvEncoder.channel(u, uv[0], uv[1]);
            int g = PngUvEncoder.channel(v, uv[2], uv[3]);
            argb[p] = 0xFF000000 | (r << 16) | (g << 8);
        }
        return new PngUvEncoder.Result(ImageEncoding.argbPng(size, size, argb), uv[0], uv[1], uv[2], uv[3], size, size);
    }

    /** The content-addressed data URL of an alternative encoding ({@code ext} = {@code velocity-json} or {@code png-uv}). */
    public static String encodingUrl(String base, ProductSpec spec, RasterFrameRow frame, String res, Window window,
            String ext) {
        StringBuilder url = new StringBuilder(ProductUrls.product(base, spec.id())).append("/fields/")
                .append(frame.frameKey()).append('.').append(ext).append("?run=")
                .append(FrameKeys.runIso(frame.referenceTime())).append("&res=").append(res);
        String box = window.boxText();
        if (box != null) {
            url.append("&bbox=").append(box);
        }
        return url.toString();
    }

    /** The content-addressed data URL: frame key, pinned run, resolution and snapped box. */
    public static String dataUrl(String base, ProductSpec spec, RasterFrameRow frame, String res, Window window) {
        StringBuilder url = new StringBuilder(ProductUrls.product(base, spec.id())).append("/fields/")
                .append(frame.frameKey()).append(".f32?run=").append(FrameKeys.runIso(frame.referenceTime()))
                .append("&res=").append(res);
        String box = window.boxText();
        if (box != null) {
            url.append("&bbox=").append(box);
        }
        return url.toString();
    }

    /** True when the frame's run is not the newest complete run. */
    boolean stale(ProductSpec spec, RasterFrameRow frame) {
        try {
            return !resolver.selectRun(spec, FrameResolver.RUN_LATEST, false).equals(frame.referenceTime());
        } catch (ForesignalException e) {
            return true;
        }
    }

    /**
     * Parses {@code bbox} ({@code w,s,e,n}; {@code w > e} crosses the antimeridian).
     *
     * @return the box, or null when absent
     */
    public static double[] parseBox(String bbox) {
        if (bbox == null || bbox.isBlank()) {
            return null;
        }
        String[] parts = bbox.split(",");
        if (parts.length != 4) {
            throw ValidationException.forField("bbox", "must be minLon,minLat,maxLon,maxLat");
        }
        double[] out = new double[4];
        for (int k = 0; k < 4; k++) {
            try {
                out[k] = Double.parseDouble(parts[k].strip());
            } catch (NumberFormatException e) {
                throw ValidationException.forField("bbox", "must be four numbers");
            }
            if (!Double.isFinite(out[k])) {
                throw ValidationException.forField("bbox", "must be four finite numbers");
            }
        }
        if (out[0] < -180 || out[0] > 180 || out[2] < -180 || out[2] > 180 || out[1] < -90 || out[3] > 90
                || out[1] >= out[3] || out[0] == out[2]) {
            throw ValidationException.forField("bbox", "must be a WGS84 box with minLat < maxLat");
        }
        return out;
    }

    private static double normalizeLon(double lon) {
        double l = ((lon + 180.0) % 360.0 + 360.0) % 360.0 - 180.0;
        return round(l == 180.0 ? -180.0 : l);
    }

    private static double round(double v) {
        return Math.round(v * 1e9) / 1e9;
    }

    private static String plain(double v) {
        return BigDecimal.valueOf(v).stripTrailingZeros().toPlainString();
    }
}
