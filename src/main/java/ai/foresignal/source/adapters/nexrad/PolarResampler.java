package ai.foresignal.source.adapters.nexrad;

import ai.foresignal.source.spi.GridDefinition;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Polar to latitude/longitude resampling of a radar scan (prompt 106 task 4): a regular grid of
 * {@code resolution}° cells centered on the radar, out to {@code maxRangeKm}. Each cell takes the sample
 * of the radial and gate that contain its center (nearest neighbor, so no value is invented between
 * radials): the great-circle range and initial bearing from the radar (spherical Earth, mean radius)
 * pick the gate ({@code floor((range − gate0) / gateWidth)}) and the radial (the one whose beam, 360°/n
 * wide, covers the bearing). Cells beyond {@code maxRangeKm}, beyond the last gate or under no radial are
 * NaN.
 */
public final class PolarResampler {

    /** The Earth's mean radius, km. */
    static final double EARTH_RADIUS_KM = 6371.0088;

    /** Azimuth lookup bins per degree. */
    static final int BINS_PER_DEGREE = 10;

    private PolarResampler() {}

    /**
     * A resampled scan.
     *
     * @param grid the latlon grid (row 0 north, column 0 west)
     * @param values the samples, row-major north to south; NaN for no data
     */
    public record Resampled(GridDefinition grid, float[] values) {}

    /**
     * Resamples {@code scan}.
     *
     * @param scan the polar scan
     * @param resolution the cell size, degrees (0.005 for nexrad-l3)
     * @param maxRangeKm the range kept, km (230 for nexrad-l3)
     */
    public static Resampled resample(PolarScan scan, double resolution, double maxRangeKm) {
        double lat0 = scan.radarLat();
        double lon0 = scan.radarLon();
        double halfLat = maxRangeKm / 111.32;
        double halfLon = maxRangeKm / (111.32 * Math.max(0.05, Math.cos(Math.toRadians(lat0))));
        int ny = (int) Math.ceil(2 * halfLat / resolution);
        int nx = (int) Math.ceil(2 * halfLon / resolution);
        double west = lon0 - nx * resolution / 2;
        double north = lat0 + ny * resolution / 2;
        int[] rayOfBin = azimuthLookup(scan.azimuths());
        float gate0 = scan.gateStarts()[0];
        double gateWidth = scan.gateWidth();
        int gates = scan.gateStarts().length;
        float[] out = new float[nx * ny];
        Arrays.fill(out, Float.NaN);
        double phi1 = Math.toRadians(lat0);
        double cosPhi1 = Math.cos(phi1);
        double sinPhi1 = Math.sin(phi1);
        for (int j = 0; j < ny; j++) {
            double lat = north - (j + 0.5) * resolution;
            double phi2 = Math.toRadians(lat);
            double cosPhi2 = Math.cos(phi2);
            double sinPhi2 = Math.sin(phi2);
            for (int i = 0; i < nx; i++) {
                double lon = west + (i + 0.5) * resolution;
                double dLambda = Math.toRadians(lon - lon0);
                double a = Math.sin((phi2 - phi1) / 2);
                double b = Math.sin(dLambda / 2);
                double h = a * a + cosPhi1 * cosPhi2 * b * b;
                double rangeKm = 2 * EARTH_RADIUS_KM * Math.asin(Math.min(1, Math.sqrt(h)));
                if (rangeKm > maxRangeKm) {
                    continue;
                }
                int gate = (int) Math.floor((rangeKm * 1000.0 - gate0) / gateWidth);
                if (gate < 0 || gate >= gates) {
                    continue;
                }
                double y = Math.sin(dLambda) * cosPhi2;
                double x = cosPhi1 * sinPhi2 - sinPhi1 * cosPhi2 * Math.cos(dLambda);
                double bearing = (Math.toDegrees(Math.atan2(y, x)) + 360.0) % 360.0;
                int ray = rayOfBin[bin(bearing)];
                if (ray < 0) {
                    continue;
                }
                out[j * nx + i] = scan.value(ray, gate);
            }
        }
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("lo1", west + resolution / 2);
        params.put("la1", north - resolution / 2);
        params.put("dx", resolution);
        params.put("dy", resolution);
        GridDefinition grid = new GridDefinition(GridDefinition.LATLON, nx, ny, west, north - ny * resolution,
                west + nx * resolution, north, params);
        return new Resampled(grid, out);
    }

    /**
     * For each azimuth bin ({@value #BINS_PER_DEGREE} per degree), the radial whose beam covers it: each
     * radial covers half the nominal spacing (360°/n) on either side of its azimuth, the closest radial
     * winning; -1 where none does.
     */
    static int[] azimuthLookup(float[] azimuths) {
        int bins = 360 * BINS_PER_DEGREE;
        int[] ray = new int[bins];
        double[] best = new double[bins];
        Arrays.fill(ray, -1);
        Arrays.fill(best, Double.POSITIVE_INFINITY);
        double half = 360.0 / azimuths.length / 2;
        for (int r = 0; r < azimuths.length; r++) {
            double az = ((azimuths[r] % 360.0) + 360.0) % 360.0;
            int from = (int) Math.floor((az - half) * BINS_PER_DEGREE);
            int to = (int) Math.ceil((az + half) * BINS_PER_DEGREE);
            for (int k = from; k <= to; k++) {
                int b = Math.floorMod(k, bins);
                double center = (b + 0.5) / BINS_PER_DEGREE;
                double d = Math.abs(((center - az) % 360.0 + 540.0) % 360.0 - 180.0);
                if (d <= half + 0.5 / BINS_PER_DEGREE && d < best[b]) {
                    best[b] = d;
                    ray[b] = r;
                }
            }
        }
        return ray;
    }

    private static int bin(double bearing) {
        return Math.min(360 * BINS_PER_DEGREE - 1, (int) (bearing * BINS_PER_DEGREE));
    }

    /**
     * u8 codes of physical values: {@code round((v − offset) / scale)} clamped to 1..255; NaN is code 0
     * (nodata).
     */
    public static int[] codes(float[] values, double scale, double offset) {
        int[] out = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            float v = values[i];
            if (Float.isNaN(v)) {
                out[i] = 0;
            } else {
                long c = Math.round((v - offset) / scale);
                out[i] = (int) Math.max(1, Math.min(255, c));
            }
        }
        return out;
    }
}
