package ai.foresignal.raster.model;

import ai.foresignal.raster.FrameStore;
import ai.foresignal.raster.GridDef;
import ai.foresignal.raster.LatLonGrid;
import ai.foresignal.raster.RasterPaths;
import ai.foresignal.source.spi.DecodedBands;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.ProductSpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * The precompressed God's Eye View wind fields of a global forecast product (prompt 051;
 * Weather-Imagery-Design §2.4, §6.4): planar little-endian Float32, {@code u} then {@code v}, rows
 * north to south, longitudes 0..359, at 1° (360 × 181, 521,280 bytes raw) and 0.25° (1440 × 721,
 * 8,305,920 bytes raw), each gzip-compressed once at ingest under
 * {@code rasters/fields/{productId}/{run}/f{FFF}.{1p00|0p25}.f32.gz}.
 *
 * <p>The raster pipeline writes them for every forecast frame of a {@code kind=field} product on a
 * global latitude/longitude grid with two physical bands ({@code gfs}); HRRR's Lambert grid gets none
 * (fields are {@code gfs} only in v1). The values are the decoded floats, before the frame's i16
 * quantization. The 0.25° file is the native GFS grid unchanged when it already has the God's Eye
 * View layout; anything else goes through {@link WindResampler}.
 *
 * <p>Each file's raw SHA-256 and byte length are returned for the frame's {@code stats.fields}
 * (the manifest reads them from there). Run directories with no frames left are removed by the
 * raster retention step's orphan sweep ({@link #sweep}).
 */
@Component
public class FieldStore {

    private static final Logger log = LoggerFactory.getLogger(FieldStore.class);

    /** The 1° resolution. */
    public static final String RES_1 = "1";

    /** The 0.25° resolution. */
    public static final String RES_QUARTER = "0.25";

    /** Both resolutions, in the order they are written. */
    public static final List<String> RESOLUTIONS = List.of(RES_1, RES_QUARTER);

    /** The {@code stats} member holding the written files. */
    public static final String STATS_KEY = "fields";

    private final RasterPaths paths;

    /** Creates the store. */
    public FieldStore(RasterPaths paths) {
        this.paths = paths;
    }

    /**
     * True when a frame gets field files: a forecast frame of a {@code kind=field} product on a global
     * latitude/longitude grid, given as two bands of physical values.
     */
    public static boolean applies(ProductSpec spec, GridDefinition grid, DecodedBands bands, boolean forecast) {
        if (!forecast || !ProductSpec.KIND_FIELD.equals(spec.kind()) || !GridDefinition.LATLON.equals(grid.type())
                || bands == null || bands.physical() == null || bands.bandCount() != 2) {
            return false;
        }
        return GridDef.of(grid).wrapsLongitude();
    }

    /**
     * One written file.
     *
     * @param res {@value #RES_1} or {@value #RES_QUARTER}
     * @param path the file, relative to {@code rasters/}
     * @param sha256 the SHA-256 of the raw (uncompressed) bytes
     * @param byteLength the raw byte length
     * @param gzipBytes the compressed file size
     * @param nx columns
     * @param ny rows
     * @param dx column step, degrees
     * @param dy row step, degrees
     */
    public record StoredField(String res, String path, String sha256, int byteLength, long gzipBytes, int nx, int ny,
            double dx, double dy) {

        /** The {@code stats.fields} entry. */
        public Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("path", path);
            m.put("sha256", sha256);
            m.put("byteLength", byteLength);
            m.put("gzipBytes", gzipBytes);
            m.put("nx", nx);
            m.put("ny", ny);
            m.put("dx", dx);
            m.put("dy", dy);
            return m;
        }

        /** The entry read back from {@code stats.fields}, or null when malformed. */
        static StoredField fromMap(String res, Object value) {
            if (!(value instanceof Map<?, ?> m) || !(m.get("path") instanceof String p) || !(m.get("sha256") instanceof String s)) {
                return null;
            }
            return new StoredField(res, p, s, num(m.get("byteLength")).intValue(), num(m.get("gzipBytes")).longValue(),
                    num(m.get("nx")).intValue(), num(m.get("ny")).intValue(), num(m.get("dx")).doubleValue(),
                    num(m.get("dy")).doubleValue());
        }

        private static Number num(Object o) {
            return o instanceof Number n ? n : 0;
        }
    }

    /**
     * The files a frame's {@code stats} lists.
     *
     * @param stats the frame's statistics
     * @return resolution to file; empty when the frame has none
     */
    public static Map<String, StoredField> fromStats(Map<String, Object> stats) {
        Map<String, StoredField> out = new LinkedHashMap<>();
        if (stats != null && stats.get(STATS_KEY) instanceof Map<?, ?> fields) {
            for (String res : RESOLUTIONS) {
                StoredField f = StoredField.fromMap(res, fields.get(res));
                if (f != null) {
                    out.put(res, f);
                }
            }
        }
        return out;
    }

    /**
     * Writes both resolutions of one step.
     *
     * @param productId the product
     * @param run the run's reference time
     * @param step the step, hours
     * @param grid the native global latitude/longitude grid
     * @param u eastward component, m/s, rows north to south
     * @param v northward component
     * @return the {@code stats.fields} member: resolution to {@link StoredField#toMap()}
     * @throws IllegalArgumentException when the grid is not a global lat/lon grid or a value is not
     *     finite
     * @throws UncheckedIOException when a file cannot be written
     */
    public Map<String, Object> write(String productId, Instant run, int step, GridDefinition grid, float[] u,
            float[] v) {
        GridDef def = GridDef.of(grid);
        if (!(def instanceof LatLonGrid ll) || !ll.wrapsLongitude()) {
            throw new IllegalArgumentException("Wind fields need a global latitude/longitude grid");
        }
        Map<String, Object> out = new LinkedHashMap<>();
        for (String res : RESOLUTIONS) {
            double step2 = RES_1.equals(res) ? 1.0 : 0.25;
            WindResampler.Field field = nativeLayout(ll, step2)
                    ? new WindResampler.Field(ll.nx(), ll.ny(), 0, 90, step2, step2, finite(u), finite(v))
                    : WindResampler.resample(u, v, ll.nx(), ll.ny(), ll.lo1(), ll.la1(), ll.dx(), ll.dy(), step2, step2);
            byte[] raw = planar(field.u(), field.v());
            String relative = relativePath(productId, run, step, res);
            long gzipBytes = writeGzip(relative, raw);
            out.put(res, new StoredField(res, relative, FrameStore.sha256Hex(raw), raw.length, gzipBytes, field.nx(),
                    field.ny(), field.dx(), field.dy()).toMap());
        }
        return out;
    }

    /** True when the native grid already is the God's Eye View grid at {@code step}. */
    static boolean nativeLayout(LatLonGrid grid, double step) {
        return Math.abs(grid.dx() - step) < 1e-9 && Math.abs(grid.dy() - step) < 1e-9 && Math.abs(grid.lo1()) < 1e-9
                && Math.abs(grid.la1() - 90) < 1e-9 && grid.nx() == (int) Math.round(360 / step)
                && grid.ny() == (int) Math.round(180 / step) + 1;
    }

    private static float[] finite(float[] values) {
        for (float f : values) {
            if (!Float.isFinite(f)) {
                throw new IllegalArgumentException("Invalid wind grid values");
            }
        }
        return values;
    }

    /** {@code u} then {@code v}, little-endian Float32. */
    public static byte[] planar(float[] u, float[] v) {
        ByteBuffer buffer = ByteBuffer.allocate((u.length + v.length) * 4).order(ByteOrder.LITTLE_ENDIAN);
        buffer.asFloatBuffer().put(u).put(v);
        return buffer.array();
    }

    /** {@code fields/{productId}/{run}/f{FFF}.{1p00|0p25}.f32.gz}, relative to {@code rasters/}. */
    public static String relativePath(String productId, Instant run, int step, String res) {
        String suffix = RES_1.equals(res) ? "1p00" : "0p25";
        return "fields/" + productId + "/" + ModelCycleSelector.cycleKey(run) + "/f"
                + String.format(Locale.ROOT, "%03d", step) + "." + suffix + ".f32.gz";
    }

    private long writeGzip(String relative, byte[] raw) {
        Path target = paths.resolve(relative);
        try {
            Files.createDirectories(target.getParent());
            Path staged = Files.createTempFile(paths.tmp(), "field-", ".part");
            try {
                try (OutputStream out = new GZIPOutputStream(Files.newOutputStream(staged), 64 * 1024)) {
                    out.write(raw);
                }
                long size = Files.size(staged);
                Files.move(staged, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                return size;
            } finally {
                Files.deleteIfExists(staged);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write wind field " + relative + ": " + e.getMessage(), e);
        }
    }

    /**
     * The compressed bytes of a stored file.
     *
     * @param relative the path from {@link StoredField#path()}
     * @throws UncheckedIOException when the file is missing or unreadable
     */
    public byte[] readGzip(String relative) {
        try {
            return Files.readAllBytes(paths.resolve(relative));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read wind field " + relative + ": " + e.getMessage(), e);
        }
    }

    /** True when the stored file exists. */
    public boolean exists(String relative) {
        return Files.isRegularFile(paths.resolve(relative));
    }

    /**
     * The raw (uncompressed) bytes of a stored file.
     *
     * @throws UncheckedIOException when the file is missing or unreadable
     */
    public byte[] readRaw(String relative) {
        return gunzip(readGzip(relative));
    }

    /** Inflates gzip bytes. */
    public static byte[] gunzip(byte[] gz) {
        try (InputStream in = new GZIPInputStream(new java.io.ByteArrayInputStream(gz))) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not inflate a wind field: " + e.getMessage(), e);
        }
    }

    /** Compresses raw bytes with gzip. */
    public static byte[] gzip(byte[] raw) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(Math.max(64, raw.length / 2));
        try (OutputStream out = new GZIPOutputStream(bytes, 64 * 1024)) {
            out.write(raw);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not compress a wind field: " + e.getMessage(), e);
        }
        return bytes.toByteArray();
    }

    /**
     * Removes run directories whose run has no frame left (the orphan sweep), once older than the
     * grace cutoff.
     *
     * @param knownRuns product id to the cycle keys ({@link ModelCycleSelector#cycleKey}) that still have
     *     frames
     * @param graceCutoff only directories last modified before this are removed
     * @return files removed
     */
    public long sweep(Function<String, Set<String>> knownRuns, Instant graceCutoff) {
        Path root = paths.fields();
        if (!Files.isDirectory(root)) {
            return 0;
        }
        long removed = 0;
        try (Stream<Path> products = Files.list(root)) {
            for (Path productDir : products.filter(Files::isDirectory).toList()) {
                Set<String> known = knownRuns.apply(productDir.getFileName().toString());
                try (Stream<Path> runs = Files.list(productDir)) {
                    for (Path runDir : runs.filter(Files::isDirectory).toList()) {
                        if (known.contains(runDir.getFileName().toString())
                                || !Files.getLastModifiedTime(runDir).toInstant().isBefore(graceCutoff)) {
                            continue;
                        }
                        removed += deleteTree(runDir);
                    }
                }
            }
        } catch (IOException | UncheckedIOException e) {
            log.warn("Could not sweep {}", root, e);
        }
        return removed;
    }

    private static long deleteTree(Path dir) throws IOException {
        long files = 0;
        try (Stream<Path> walk = Files.walk(dir)) {
            for (Path p : walk.sorted(java.util.Comparator.reverseOrder()).toList()) {
                if (Files.isRegularFile(p)) {
                    files++;
                }
                Files.deleteIfExists(p);
            }
        }
        return files;
    }
}
