package ai.foresignal.raster.sample;

import ai.foresignal.raster.ElgCodec;
import ai.foresignal.raster.FrameEncoder;
import ai.foresignal.raster.FsgHeader;
import ai.foresignal.raster.GridDef;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.RasterDtype;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A read-only view of one decoded frame with its product and projection (prompt 052): the raw code of
 * any cell and band, what the code means, its physical value, and point sampling by the product's
 * method.
 *
 * <p>Cells are flat indices {@code row × nx + column}, rows north to south, as in the {@code .fsg}
 * payload. A code is classified in this order: a special code whose meaning is {@code noCoverage}
 * (MRMS code 0) is <em>no coverage</em>; otherwise the product's {@code nodata} code (or NaN) is
 * <em>no data</em>; anything else is a value, including other special codes such as {@code noEcho},
 * whose physical value is still defined. Thread-safe: it only reads the decoded payload.
 */
public final class FrameView {

    /** What a code is. */
    public enum Kind {
        /** A value (possibly a named special code such as {@code noEcho}). */
        VALUE,
        /** Inside the grid but missing ({@code nodata} or NaN). */
        NO_DATA,
        /** A special code meaning no product coverage there. */
        NO_COVERAGE
    }

    private final ProductSpec spec;
    private final RasterFrameRow frame;
    private final GridDef grid;
    private final FsgHeader header;
    private final ByteBuffer data;
    private final int cells;
    private final double nodata;
    private final double noCoverageCode;

    /**
     * Wraps a decoded frame.
     *
     * @param frame the frame row (for times and keys), or null in pure tests
     * @throws IllegalArgumentException when the grid does not match the frame's dimensions
     */
    public FrameView(ProductSpec spec, RasterFrameRow frame, GridDef grid, ElgCodec.Decoded decoded) {
        this.spec = spec;
        this.frame = frame;
        this.grid = grid;
        this.header = decoded.header();
        if (header.nx() != grid.nx() || header.ny() != grid.ny()) {
            throw new IllegalArgumentException("Frame is " + header.nx() + "x" + header.ny() + " but its grid is "
                    + grid.nx() + "x" + grid.ny());
        }
        this.data = ByteBuffer.wrap(decoded.payload()).order(ByteOrder.LITTLE_ENDIAN);
        this.cells = header.nx() * header.ny();
        this.nodata = spec.nodata() == null ? Double.NaN : spec.nodata();
        double noCov = Double.NaN;
        for (var e : spec.codes().entrySet()) {
            if (FrameEncoder.NO_COVERAGE.equals(e.getValue())) {
                noCov = e.getKey();
                break;
            }
        }
        this.noCoverageCode = noCov;
    }

    /** The product. */
    public ProductSpec spec() {
        return spec;
    }

    /** The frame row, or null. */
    public RasterFrameRow frame() {
        return frame;
    }

    /** The projection. */
    public GridDef grid() {
        return grid;
    }

    /** Columns. */
    public int nx() {
        return header.nx();
    }

    /** Rows. */
    public int ny() {
        return header.ny();
    }

    /** Bands. */
    public int bands() {
        return header.bands();
    }

    /** Cells per band. */
    public int cells() {
        return cells;
    }

    /** The raw code of {@code cell} in {@code band}. */
    public double code(int cell, int band) {
        int sample = band * cells + cell;
        return switch (header.dtype()) {
            case U8 -> data.get(sample) & 0xFF;
            case U16 -> data.getShort(sample * 2) & 0xFFFF;
            case I16 -> data.getShort(sample * 2);
            case F32 -> data.getFloat(sample * 4);
            case RGBA8 -> Integer.toUnsignedLong(data.getInt(sample * 4));
        };
    }

    /** What {@code code} is. */
    public Kind classify(double code) {
        if (!Double.isNaN(noCoverageCode) && code == noCoverageCode) {
            return Kind.NO_COVERAGE;
        }
        if (Double.isNaN(code) || (!Double.isNaN(nodata) && code == nodata)) {
            return Kind.NO_DATA;
        }
        return Kind.VALUE;
    }

    /** What the code of {@code cell} in band 0 is. */
    public Kind classify(int cell) {
        return classify(code(cell, 0));
    }

    /** The physical value of a code: {@code code × scale + offset}. */
    public double physical(double code) {
        return code * header.scale() + header.offset();
    }

    /** The physical value of {@code cell} in {@code band}, or NaN when it is not a value. */
    public double value(int cell, int band) {
        double c = code(cell, band);
        return classify(c) == Kind.VALUE ? physical(c) : Double.NaN;
    }

    /** The meaning of a named special code ({@code noEcho}), or null. */
    public String meaning(double code) {
        if (Double.isNaN(code) || code != Math.rint(code)) {
            return null;
        }
        return spec.codes().get((int) code);
    }

    /** The raw code at or above which a physical value reaches {@code threshold} (integer types). */
    public double codeAtLeast(double threshold) {
        double raw = (threshold - header.offset()) / header.scale();
        return header.dtype() == RasterDtype.F32 ? raw : Math.ceil(raw - 1e-9);
    }

    /** The nearest cell of a point, or -1 off the grid. */
    public int nearestCell(double lon, double lat) {
        return grid.nearestCell(lon, lat);
    }

    /**
     * The value of a point by the product's sampling method.
     *
     * @return the sample; {@link Sample#kind()} {@code NO_COVERAGE} off the grid
     */
    public Sample sample(double lon, double lat) {
        double[] ij = grid.lonLatToIndex(lon, lat);
        if (Double.isNaN(ij[0]) || Double.isNaN(ij[1])) {
            return Sample.noCoverage(method());
        }
        int cell = grid.nearestCell(lon, lat);
        if (cell < 0) {
            return Sample.noCoverage(method());
        }
        double nearestCode = code(cell, 0);
        Kind kind = classify(nearestCode);
        if (kind != Kind.VALUE) {
            return new Sample(kind, Double.NaN, Double.NaN, method(), null, nearestCode);
        }
        String meaning = meaning(nearestCode);
        if (!spec.bilinear()) {
            double v = physical(nearestCode);
            double second = bands() >= 2 ? value(cell, 1) : Double.NaN;
            return new Sample(Kind.VALUE, v, second, method(), meaning, nearestCode);
        }
        double v0 = bilinear(ij[0], ij[1], 0);
        double v1 = bands() >= 2 ? bilinear(ij[0], ij[1], 1) : Double.NaN;
        if (Double.isNaN(v0)) {
            return new Sample(Kind.NO_DATA, Double.NaN, Double.NaN, method(), null, nearestCode);
        }
        return new Sample(Kind.VALUE, v0, v1, method(), meaning, nearestCode);
    }

    /** {@code nearest} or {@code bilinear}, from the product spec. */
    public String method() {
        return spec.bilinear() ? ProductSpec.RESAMPLE_BILINEAR : ProductSpec.RESAMPLE_NEAREST;
    }

    /** Bilinear physical value of {@code band} at a continuous index over corners that are values; NaN when none. */
    double bilinear(double i, double j, int band) {
        int nx = nx();
        int ny = ny();
        int i0 = (int) Math.floor(i);
        int j0 = (int) Math.floor(j);
        double fi = i - i0;
        double fj = j - j0;
        double sum = 0;
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
                double v = value(row * nx + col, band);
                if (Double.isNaN(v)) {
                    continue;
                }
                sum += w * v;
                weight += w;
            }
        }
        return weight <= 0 ? Double.NaN : sum / weight;
    }

    /**
     * One point sample.
     *
     * @param kind value, no data or no coverage
     * @param value the physical value of band 0, NaN unless a value
     * @param second the physical value of band 1 (the v component of a u/v field), NaN when absent
     * @param method {@code nearest} or {@code bilinear}
     * @param meaning the name of a special code at the nearest cell ({@code noEcho}), or null
     * @param code the raw code at the nearest cell
     */
    public record Sample(Kind kind, double value, double second, String method, String meaning, double code) {

        /** Off the grid. */
        static Sample noCoverage(String method) {
            return new Sample(Kind.NO_COVERAGE, Double.NaN, Double.NaN, method, null, Double.NaN);
        }
    }
}
