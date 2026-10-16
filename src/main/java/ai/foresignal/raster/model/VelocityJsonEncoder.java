package ai.foresignal.raster.model;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;

/**
 * The leaflet-velocity / wind-js JSON encoding of a wind field (prompt 106; Weather-Imagery-Design
 * §6.4): a JSON array of two objects ({@code u} and {@code v}), each with a {@code header} — discipline
 * 0, parameter category 2, parameter number {@code 2|3}, {@code lo1/la1/lo2/la2}, {@code dx/dy},
 * {@code nx/ny}, {@code refTime} and {@code forecastTime} — and a {@code data} array, row-major north
 * to south and west to east.
 *
 * <p>Input is Foresignal's planar little-endian Float32 ({@code u} then {@code v}, rows north to south),
 * the same bytes the {@code .f32} endpoint serves. The JSON is gzipped by the controller (the cache
 * rules are those of {@code .f32}).
 */
public final class VelocityJsonEncoder {

    /** The most points per component; over this the request is {@code 400 FIELD_TOO_LARGE}. */
    public static final int MAX_POINTS = 300_000;

    private VelocityJsonEncoder() {}

    /**
     * Encodes planar u/v Float32 as the velocity JSON.
     *
     * @param raw planar little-endian {@code u} then {@code v}, {@code nx*ny*8} bytes
     * @param nx columns
     * @param ny rows
     * @param lo1 first column longitude, 0..360
     * @param la1 first row latitude
     * @param dx column step, degrees
     * @param dy row step, degrees (positive, rows going south)
     * @param refTime the run reference time, or null
     * @param forecastTime the forecast step in hours (0 when unknown)
     * @return the UTF-8 JSON bytes
     */
    public static byte[] encode(byte[] raw, int nx, int ny, double lo1, double la1, double dx, double dy,
            Instant refTime, int forecastTime) {
        if (raw.length != nx * ny * 8) {
            throw new IllegalArgumentException("Field data is " + raw.length + " bytes; a " + nx + "x" + ny
                    + " grid needs " + (nx * ny * 8));
        }
        ByteBuffer in = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
        int cells = nx * ny;
        double lo2 = lo1 + (nx - 1) * dx;
        double la2 = la1 - (ny - 1) * dy;
        String ref = refTime == null ? null : refTime.toString();
        StringBuilder out = new StringBuilder(cells * 12 + 512);
        out.append('[');
        component(out, 2, in, 0, cells, nx, ny, lo1, la1, lo2, la2, dx, dy, ref, forecastTime);
        out.append(',');
        component(out, 3, in, cells, cells, nx, ny, lo1, la1, lo2, la2, dx, dy, ref, forecastTime);
        out.append(']');
        return out.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private static void component(StringBuilder out, int parameterNumber, ByteBuffer in, int offset, int cells,
            int nx, int ny, double lo1, double la1, double lo2, double la2, double dx, double dy, String ref,
            int forecastTime) {
        out.append("{\"header\":{\"discipline\":0,\"parameterCategory\":2,\"parameterNumber\":")
                .append(parameterNumber).append(",\"lo1\":").append(num(lo1)).append(",\"la1\":").append(num(la1))
                .append(",\"lo2\":").append(num(lo2)).append(",\"la2\":").append(num(la2)).append(",\"dx\":")
                .append(num(dx)).append(",\"dy\":").append(num(dy)).append(",\"nx\":").append(nx).append(",\"ny\":")
                .append(ny).append(",\"refTime\":");
        if (ref == null) {
            out.append("null");
        } else {
            out.append('"').append(ref).append('"');
        }
        out.append(",\"forecastTime\":").append(forecastTime).append("},\"data\":[");
        for (int k = 0; k < cells; k++) {
            if (k > 0) {
                out.append(',');
            }
            out.append(Float.toString(in.getFloat((offset + k) * 4)));
        }
        out.append("]}");
    }

    /** Rounds a coordinate to 1e-9 so the JSON is stable. */
    static double num(double v) {
        return Math.round(v * 1e9) / 1e9;
    }
}
