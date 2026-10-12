package ai.foresignal.raster;

import ai.foresignal.source.spi.DecodedBands;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.RasterDtype;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

/**
 * Quantizes decoded bands per the product spec and computes the frame's statistics and footprint
 * (Weather-Imagery-Design §2.1 step ②). Pure and thread-safe; the pipeline runs it on the decode
 * pool.
 *
 * <p><b>Quantization.</b> For the integer types a physical value {@code v} becomes
 * {@code round((v − offset) / scale)}, clamped to the type's range; {@code NaN} becomes the
 * {@code nodata} code (a product that can receive missing samples must declare one). {@code f32}
 * stores {@code (v − offset) / scale} as a float, {@code NaN} included. Raw codes are stored as given
 * after a range check; {@code rgba8} takes only raw codes, one packed {@code 0xRRGGBBAA} int per pixel.
 *
 * <p><b>Statistics</b> (band 0, physical units): a sample has <em>coverage</em> unless it is the
 * nodata code, {@code NaN}, or a special code whose meaning is {@value #NO_COVERAGE} (an
 * {@code rgba8} pixel has coverage when its alpha is non-zero). {@code min}, {@code max} and
 * {@code mean} are over covered samples that are not special codes; {@code coveragePct} is the
 * covered share of the grid; categorical products ({@code codes} non-empty) also get
 * {@code codeCounts}, the cells per declared code.
 *
 * <p><b>Footprint.</b> The grid box clipped to the rows (north to south) that hold any covered
 * sample, as a MultiPolygon (two parts when the grid crosses the antimeridian). Row edges are
 * interpolated linearly in latitude, which is exact for {@code latlon} grids and an approximation for
 * projected grids until prompt 045 adds the projection maths.
 */
public final class FrameEncoder {

    /** The special-code meaning that marks a sample outside the product's coverage. */
    public static final String NO_COVERAGE = "noCoverage";

    private FrameEncoder() {}

    /**
     * Encodes one frame.
     *
     * @param spec the product
     * @param grid the native grid
     * @param bands the decoded bands
     * @param partial true for a partial frame (header flag)
     * @throws IllegalArgumentException when the bands do not fit the grid, the spec or the dtype
     */
    public static EncodedFrame encode(ProductSpec spec, GridDefinition grid, DecodedBands bands, boolean partial) {
        RasterDtype dtype = spec.dtype();
        if (dtype == null) {
            throw new IllegalArgumentException("Product " + spec.id() + " declares no dtype; it cannot store frames");
        }
        int cells = Math.toIntExact(grid.cells());
        int bandCount = bands.bandCount();
        if (bandCount != spec.bandCount()) {
            throw new IllegalArgumentException("Product " + spec.id() + " has " + spec.bandCount() + " band(s); the frame has "
                    + bandCount);
        }
        for (int b = 0; b < bandCount; b++) {
            if (bands.length(b) != cells) {
                throw new IllegalArgumentException("Band " + b + " has " + bands.length(b) + " samples; the "
                        + grid.nx() + "x" + grid.ny() + " grid needs " + cells);
            }
        }
        if (dtype == RasterDtype.RGBA8 && bands.codes() == null) {
            throw new IllegalArgumentException("rgba8 frames take raw codes (packed 0xRRGGBBAA), not physical values");
        }
        if (dtype == RasterDtype.F32 && bands.codes() != null) {
            throw new IllegalArgumentException("f32 frames take physical values, not raw codes");
        }
        double nodata = spec.nodata() == null ? Double.NaN : spec.nodata();
        ByteBuffer out = ByteBuffer.allocate(Math.multiplyExact(Math.multiplyExact(cells, bandCount),
                dtype.bytesPerSample())).order(ByteOrder.LITTLE_ENDIAN);
        double[] band0 = new double[cells];
        for (int b = 0; b < bandCount; b++) {
            for (int i = 0; i < cells; i++) {
                double code = bands.codes() != null
                        ? checkCode(dtype, bands.codes().get(b)[i], spec)
                        : quantize(dtype, bands.physical().get(b)[i], spec, nodata);
                put(out, dtype, code, bands.codes() != null ? bands.codes().get(b)[i] : 0);
                if (b == 0) {
                    band0[i] = code;
                }
            }
        }
        FsgHeader header = new FsgHeader(dtype, FsgHeader.CODEC_DEFLATE, grid.nx(), grid.ny(), bandCount, partial,
                spec.scale(), spec.offset(), nodata);
        boolean[] covered = new boolean[cells];
        Map<String, Object> stats = stats(spec, dtype, band0, nodata, covered,
                dtype == RasterDtype.RGBA8 ? bands.codes().get(0) : null);
        return new EncodedFrame(header, out.array(), stats, footprint(grid, covered));
    }

    /** The code of physical value {@code v}. */
    static double quantize(RasterDtype dtype, float v, ProductSpec spec, double nodata) {
        if (Float.isNaN(v)) {
            if (dtype == RasterDtype.F32) {
                return Double.NaN;
            }
            if (Double.isNaN(nodata)) {
                throw new IllegalArgumentException("Product " + spec.id()
                        + " received a missing sample but declares no nodata code");
            }
            return nodata;
        }
        double raw = (v - spec.offset()) / spec.scale();
        if (dtype == RasterDtype.F32) {
            return (float) raw;
        }
        double rounded = Math.rint(raw);
        return Math.max(dtype.minCode(), Math.min(dtype.maxCode(), rounded));
    }

    private static double checkCode(RasterDtype dtype, int code, ProductSpec spec) {
        if (dtype == RasterDtype.RGBA8) {
            return Integer.toUnsignedLong(code);
        }
        if (code < dtype.minCode() || code > dtype.maxCode()) {
            throw new IllegalArgumentException("Raw code " + code + " is outside " + dtype.wireValue() + " for product "
                    + spec.id());
        }
        return code;
    }

    private static void put(ByteBuffer out, RasterDtype dtype, double code, int packed) {
        switch (dtype) {
            case U8 -> out.put((byte) (int) code);
            case U16 -> out.putShort((short) (int) code);
            case I16 -> out.putShort((short) (int) code);
            case F32 -> out.putFloat((float) code);
            case RGBA8 -> {
                out.put((byte) (packed >>> 24));
                out.put((byte) (packed >>> 16));
                out.put((byte) (packed >>> 8));
                out.put((byte) packed);
            }
        }
    }

    private static Map<String, Object> stats(ProductSpec spec, RasterDtype dtype, double[] band0, double nodata,
            boolean[] covered, int[] rgba) {
        Map<Integer, String> codes = spec.codes();
        Map<Integer, Long> counts = new TreeMap<>();
        codes.keySet().forEach(code -> counts.put(code, 0L));
        long coveredCount = 0;
        long valued = 0;
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        double sum = 0;
        for (int i = 0; i < band0.length; i++) {
            double code = band0[i];
            if (rgba != null) {
                covered[i] = (rgba[i] & 0xFF) != 0;
                if (covered[i]) {
                    coveredCount++;
                }
                continue;
            }
            if (Double.isNaN(code)) {
                continue;
            }
            Integer special = dtype.isInteger() && codes.containsKey((int) code) ? (int) code : null;
            if (special != null) {
                counts.merge(special, 1L, Long::sum);
                if (code == nodata || NO_COVERAGE.equals(codes.get(special))) {
                    continue;
                }
                covered[i] = true;
                coveredCount++;
                continue;
            }
            if (code == nodata) {
                continue;
            }
            covered[i] = true;
            coveredCount++;
            double value = spec.physical(code);
            valued++;
            sum += value;
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        if (valued > 0) {
            out.put("min", round(min));
            out.put("max", round(max));
            out.put("mean", round(sum / valued));
        }
        out.put("coveragePct", band0.length == 0 ? 0.0 : Math.round(coveredCount * 10_000.0 / band0.length) / 100.0);
        if (!codes.isEmpty()) {
            Map<String, Object> codeCounts = new LinkedHashMap<>();
            counts.forEach((code, count) -> codeCounts.put(Integer.toString(code), count));
            out.put("codeCounts", codeCounts);
        }
        return out;
    }

    private static double round(double v) {
        return Math.round(v * 10_000.0) / 10_000.0;
    }

    /** The grid box clipped to the rows holding any covered sample; null when none does. */
    static MultiPolygon footprint(GridDefinition grid, boolean[] covered) {
        int first = -1;
        int last = -1;
        for (int row = 0; row < grid.ny(); row++) {
            int base = row * grid.nx();
            for (int col = 0; col < grid.nx(); col++) {
                if (covered[base + col]) {
                    if (first < 0) {
                        first = row;
                    }
                    last = row;
                    break;
                }
            }
        }
        if (first < 0) {
            return null;
        }
        double rowHeight = (grid.north() - grid.south()) / grid.ny();
        double top = grid.north() - first * rowHeight;
        double bottom = grid.north() - (last + 1) * rowHeight;
        List<Polygon> parts = new ArrayList<>();
        if (grid.west() > grid.east()) {
            parts.add(GridStore.box(grid.west(), bottom, 180, top));
            parts.add(GridStore.box(-180, bottom, grid.east(), top));
        } else {
            parts.add(GridStore.box(grid.west(), bottom, grid.east(), top));
        }
        return GridStore.GEOMETRY.createMultiPolygon(parts.toArray(Polygon[]::new));
    }
}
