package ai.foresignal.raster.model;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.raster.ElgCodec;
import ai.foresignal.raster.FrameStore;
import ai.foresignal.raster.FsgHeader;
import ai.foresignal.raster.GridDef;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.render.RasterRenderService;
import ai.foresignal.raster.render.RenderQueue;
import ai.foresignal.raster.render.TileCache;
import ai.foresignal.raster.render.WebMercator;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.RasterDtype;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Wind barb vector tiles (prompt 051; Weather-Imagery-Design §6.4):
 * {@code /v1/products/{id}/vectors/{frameKey}/{z}/{x}/{y}.mvt} for the u/v products ({@code gfs},
 * {@code hrrr}).
 *
 * <p>The u/v frame is sampled bilinearly on a lattice of {@value #SPACING_PX}-pixel spacing inside the
 * 256-pixel tile ({@value #PER_SIDE} × {@value #PER_SIDE} points at pixel centers 16, 48, …), so the
 * density stays constant on screen at every zoom. Each point carries {@code speedMs} (0.1 m/s),
 * {@code fromDeg} (the meteorological direction the wind blows from, {@code (270 − atan2(v, u)·180/π)
 * mod 360}, an integer) and {@code barbKt} (the speed in knots rounded to a 5-knot bucket). The tile is
 * built by PostGIS, {@code ST_AsMVT} over {@code unnest} arrays with
 * {@code ST_AsMVTGeom(…, ST_TileEnvelope(z, x, y))}, the plumbing the record tiles use (prompt 016),
 * in layer {@value #LAYER}. Tiles are cached and render-queued like raster tiles.
 */
@Service
public class VectorTileService {

    /** The MVT layer name. */
    public static final String LAYER = "wind";

    /** The lattice spacing, pixels of a 256-pixel tile. */
    public static final int SPACING_PX = 32;

    /** Lattice points per tile side. */
    public static final int PER_SIDE = WebMercator.TILE / SPACING_PX;

    /** The MVT extent. */
    public static final int EXTENT = 4096;

    /** Knots per meter per second. */
    public static final double KNOTS_PER_MS = 3600.0 / 1852.0;

    static final String SQL = "SELECT ST_AsMVT(t.*, '" + LAYER + "', " + EXTENT + ", 'geom') FROM ("
            + "SELECT ST_AsMVTGeom(ST_SetSRID(ST_MakePoint(p.x, p.y), 3857), ST_TileEnvelope(?, ?, ?), " + EXTENT
            + ", 0, true) AS geom, p.speed AS \"speedMs\", p.dir AS \"fromDeg\", p.kt AS \"barbKt\""
            + " FROM unnest(?::float8[], ?::float8[], ?::float8[], ?::int[], ?::int[]) AS p(x, y, speed, dir, kt)"
            + ") t WHERE t.geom IS NOT NULL";

    private final FrameStore frames;
    private final RasterRenderService renderer;
    private final TileCache tiles;
    private final RenderQueue queue;
    private final JdbcTemplate jdbc;

    /** Creates the service. */
    public VectorTileService(FrameStore frames, RasterRenderService renderer, TileCache tiles, RenderQueue queue,
            JdbcTemplate jdbc) {
        this.frames = frames;
        this.renderer = renderer;
        this.tiles = tiles;
        this.queue = queue;
        this.jdbc = jdbc;
    }

    /** True when {@code spec} has u/v wind bands. */
    public static boolean hasWind(ProductSpec spec) {
        return ProductSpec.KIND_FIELD.equals(spec.kind()) && spec.bandCount() == 2;
    }

    /**
     * Refuses a product without wind.
     *
     * @throws ValidationException 400 {@code VALIDATION_FAILED}
     */
    public static void requireWind(ProductSpec spec) {
        if (!hasWind(spec)) {
            throw ValidationException.forField("productId", spec.id() + " has no u/v wind bands (product without wind)");
        }
    }

    /**
     * A tile: from the cache, or built through the render queue.
     *
     * @return the MVT bytes; empty when no lattice point falls on the product's grid
     * @throws ai.foresignal.api.error.ForesignalException {@code 503 RENDER_BUSY}
     */
    public byte[] tile(ProductSpec spec, RasterFrameRow frame, int z, int x, int y, RenderQueue.Priority priority,
            String client) {
        TileCache.Key key = new TileCache.Key(spec.id(), frame.frameKey(), "mvt", z + "/" + x + "/" + y);
        byte[] cached = tiles.get(key);
        if (cached != null) {
            return cached;
        }
        return queue.render(key, priority, client, () -> {
            byte[] hit = tiles.get(key);
            if (hit != null) {
                return hit;
            }
            byte[] built = build(spec, frame, z, x, y);
            tiles.put(key, built);
            return built;
        });
    }

    /**
     * One lattice point.
     *
     * @param x Web Mercator x, meters
     * @param y Web Mercator y, meters
     * @param speedMs speed, m/s, rounded to 0.1
     * @param fromDeg the direction the wind blows from, degrees, 0..359
     * @param barbKt the speed in knots, rounded to a multiple of 5
     */
    public record Barb(double x, double y, double speedMs, int fromDeg, int barbKt) {}

    /** Builds a tile. */
    byte[] build(ProductSpec spec, RasterFrameRow frame, int z, int x, int y) {
        GridDef grid = renderer.projection(frame.gridId());
        List<Barb> barbs = sample(spec, frames.read(frame.path()), grid, z, x, y);
        if (barbs.isEmpty()) {
            return new byte[0];
        }
        return encode(barbs, z, x, y);
    }

    /** Samples the lattice of tile {@code z/x/y} over a decoded u/v frame (pure, for tests). */
    public static List<Barb> sample(ProductSpec spec, ElgCodec.Decoded frame, GridDef grid, int z, int x, int y) {
        FsgHeader header = frame.header();
        if (header.bands() < 2) {
            throw new IllegalArgumentException("A barb tile needs two bands, the frame has " + header.bands());
        }
        ByteBuffer data = ByteBuffer.wrap(frame.payload()).order(ByteOrder.LITTLE_ENDIAN);
        int cells = header.nx() * header.ny();
        double[] m = WebMercator.tileMeters(z, x, y);
        double size = (m[2] - m[0]) / WebMercator.TILE;
        List<Barb> out = new ArrayList<>();
        for (int j = 0; j < PER_SIDE; j++) {
            double my = m[3] - (j * SPACING_PX + SPACING_PX / 2.0) * size;
            double lat = WebMercator.lat(my);
            for (int i = 0; i < PER_SIDE; i++) {
                double mx = m[0] + (i * SPACING_PX + SPACING_PX / 2.0) * size;
                double lon = WebMercator.lon(mx);
                double[] ij = grid.lonLatToIndex(lon, lat);
                if (Double.isNaN(ij[0]) || Double.isNaN(ij[1])) {
                    continue;
                }
                double[] uv = bilinear(spec, header, data, cells, grid, ij[0], ij[1]);
                if (uv == null) {
                    continue;
                }
                out.add(barb(mx, my, uv[0], uv[1]));
            }
        }
        return out;
    }

    /** The barb of a vector at a point. */
    public static Barb barb(double x, double y, double u, double v) {
        double speed = Math.hypot(u, v);
        return new Barb(x, y, Math.round(speed * 10) / 10.0, fromDeg(u, v), barbKt(speed));
    }

    /** {@code (270 − atan2(v, u)·180/π) mod 360}, rounded to an integer in 0..359. */
    public static int fromDeg(double u, double v) {
        double deg = 270 - Math.toDegrees(Math.atan2(v, u));
        long rounded = Math.round(((deg % 360) + 360) % 360);
        return (int) (rounded % 360);
    }

    /** The speed in knots, rounded to the nearest multiple of 5. */
    public static int barbKt(double speedMs) {
        return (int) (Math.round(speedMs * KNOTS_PER_MS / 5.0) * 5);
    }

    /** u and v at a continuous index, bilinear over corners whose samples are not nodata; null when none. */
    static double[] bilinear(ProductSpec spec, FsgHeader header, ByteBuffer data, int cells, GridDef grid, double i,
            double j) {
        int nx = header.nx();
        int ny = header.ny();
        int i0 = (int) Math.floor(i);
        int j0 = (int) Math.floor(j);
        double fi = i - i0;
        double fj = j - j0;
        double su = 0;
        double sv = 0;
        double weight = 0;
        for (int dj = 0; dj <= 1; dj++) {
            int row = Math.max(0, Math.min(ny - 1, j0 + dj));
            for (int di = 0; di <= 1; di++) {
                int col = grid.column(i0 + di);
                if (col < 0) {
                    col = Math.max(0, Math.min(nx - 1, i0 + di));
                }
                double w = (di == 0 ? 1 - fi : fi) * (dj == 0 ? 1 - fj : fj);
                if (w <= 0) {
                    continue;
                }
                int cell = row * nx + col;
                double cu = code(data, header.dtype(), cell);
                double cv = code(data, header.dtype(), cell + cells);
                if (missing(spec, cu) || missing(spec, cv)) {
                    continue;
                }
                su += w * header.scale() * cu + w * header.offset();
                sv += w * header.scale() * cv + w * header.offset();
                weight += w;
            }
        }
        if (weight <= 0) {
            return null;
        }
        return new double[] {su / weight, sv / weight};
    }

    private static boolean missing(ProductSpec spec, double code) {
        return Double.isNaN(code) || (spec.nodata() != null && code == spec.nodata());
    }

    private static double code(ByteBuffer data, RasterDtype dtype, int sample) {
        return switch (dtype) {
            case U8 -> data.get(sample) & 0xFF;
            case U16 -> data.getShort(sample * 2) & 0xFFFF;
            case I16 -> data.getShort(sample * 2);
            case F32 -> data.getFloat(sample * 4);
            case RGBA8 -> throw new IllegalArgumentException("An rgba8 frame has no wind components");
        };
    }

    /** The MVT of {@code barbs} through PostGIS. */
    byte[] encode(List<Barb> barbs, int z, int x, int y) {
        int n = barbs.size();
        Double[] xs = new Double[n];
        Double[] ys = new Double[n];
        Double[] speeds = new Double[n];
        Integer[] dirs = new Integer[n];
        Integer[] kts = new Integer[n];
        for (int k = 0; k < n; k++) {
            Barb b = barbs.get(k);
            xs[k] = b.x();
            ys[k] = b.y();
            speeds[k] = b.speedMs();
            dirs[k] = b.fromDeg();
            kts[k] = b.barbKt();
        }
        byte[] out = jdbc.execute((ConnectionCallback<byte[]>) con -> {
            Array ax = con.createArrayOf("float8", xs);
            Array ay = con.createArrayOf("float8", ys);
            Array as = con.createArrayOf("float8", speeds);
            Array ad = con.createArrayOf("int4", dirs);
            Array ak = con.createArrayOf("int4", kts);
            try (PreparedStatement ps = con.prepareStatement(SQL)) {
                ps.setInt(1, z);
                ps.setInt(2, x);
                ps.setInt(3, y);
                ps.setArray(4, ax);
                ps.setArray(5, ay);
                ps.setArray(6, as);
                ps.setArray(7, ad);
                ps.setArray(8, ak);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getBytes(1) : null;
                }
            } finally {
                ax.free();
                ay.free();
                as.free();
                ad.free();
                ak.free();
            }
        });
        return out == null ? new byte[0] : out;
    }
}
