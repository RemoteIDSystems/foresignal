package ai.foresignal.source.adapters.space.swpc;

import ai.foresignal.source.adapters.space.SpaceSources;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import java.time.Instant;
import tools.jackson.databind.JsonNode;

/**
 * The OVATION aurora nowcast ({@code json/ovation_aurora_latest.json}, about 930 KB) sampled at points (prompt
 * 077). Pure.
 *
 * <p>The file is {@code {"Observation Time", "Forecast Time", "coordinates": [[lon 0..359, lat -90..90,
 * probability], …]}}. It is parsed once into a 360 × 181 {@code byte[]} (probability 0–100 per one-degree cell;
 * a cell the file does not list is 0). A point is sampled at the cell of its rounded longitude modulo 360 and
 * rounded latitude, and {@link #maxWithin(double, double, double)} takes the largest probability of the cells
 * whose centers lie within a great-circle distance, wrapping across the antimeridian. Rendering the grid as a
 * raster is prompt 106's.
 */
public final class OvationSampler {

    /** Longitude cells, 0..359. */
    public static final int LONS = 360;

    /** Latitude cells, -90..90. */
    public static final int LATS = 181;

    /** The radius of {@code maxWithin300Km}. */
    public static final double NEIGHBORHOOD_KM = 300.0;

    private final byte[] cells;
    private final Instant observationTime;
    private final Instant forecastTime;

    private OvationSampler(byte[] cells, Instant observationTime, Instant forecastTime) {
        this.cells = cells;
        this.observationTime = observationTime;
        this.forecastTime = forecastTime;
    }

    /**
     * Parses the file.
     *
     * @throws UpstreamFormatException when it is not the OVATION object, or a time is missing
     */
    public static OvationSampler parse(byte[] body) {
        JsonNode root = SwpcProductsParser.read(body, "ovation_aurora_latest");
        JsonNode coordinates = root.isObject() ? root.get("coordinates") : null;
        if (coordinates == null || !coordinates.isArray()) {
            throw new UpstreamFormatException("OVATION has no coordinates array", "");
        }
        Instant observation = SpaceSources.utc(JsonProps.text(root, "Observation Time"));
        Instant forecast = SpaceSources.utc(JsonProps.text(root, "Forecast Time"));
        if (observation == null || forecast == null) {
            throw new UpstreamFormatException("OVATION has no readable Observation Time and Forecast Time", "");
        }
        byte[] cells = new byte[LONS * LATS];
        for (JsonNode triple : coordinates) {
            if (!triple.isArray() || triple.size() < 3 || !triple.get(0).isNumber() || !triple.get(1).isNumber()
                    || !triple.get(2).isNumber()) {
                continue;
            }
            int lon = Math.floorMod((int) Math.round(triple.get(0).doubleValue()), LONS);
            int lat = (int) Math.round(triple.get(1).doubleValue());
            if (lat < -90 || lat > 90) {
                continue;
            }
            int p = (int) Math.round(triple.get(2).doubleValue());
            cells[index(lon, lat)] = (byte) Math.max(0, Math.min(100, p));
        }
        return new OvationSampler(cells, observation, forecast);
    }

    /** A grid built from cells directly, for tests. */
    static OvationSampler of(byte[] cells, Instant observationTime, Instant forecastTime) {
        if (cells.length != LONS * LATS) {
            throw new IllegalArgumentException("cells must be " + LONS + " x " + LATS);
        }
        return new OvationSampler(cells.clone(), observationTime, forecastTime);
    }

    /** The cell index of an integer longitude 0..359 and latitude -90..90. */
    static int index(int lon, int lat) {
        return lon * LATS + (lat + 90);
    }

    /** The 1° grid as u8 codes, rows north to south (lat 90 → −90), columns 0..359° (prompt 106). */
    public int[] rowMajorNorthSouth() {
        int[] out = new int[LONS * LATS];
        for (int row = 0; row < LATS; row++) {
            int lat = 90 - row;
            for (int lon = 0; lon < LONS; lon++) {
                out[row * LONS + lon] = cells[index(lon, lat)];
            }
        }
        return out;
    }

    /** The file's {@code Observation Time}. */
    public Instant observationTime() {
        return observationTime;
    }

    /** The file's {@code Forecast Time}. */
    public Instant forecastTime() {
        return forecastTime;
    }

    /** The probability (0–100) at the cell of a point: rounded longitude modulo 360, rounded latitude. */
    public int probability(double lon, double lat) {
        int cellLon = Math.floorMod((int) Math.round(lon), LONS);
        int cellLat = (int) Math.max(-90, Math.min(90, Math.round(lat)));
        return cells[index(cellLon, cellLat)];
    }

    /**
     * The largest probability of the cells whose centers lie within {@code radiusKm} of a point (great-circle
     * distance), including the point's own cell; longitudes wrap across 0/360 and the antimeridian.
     */
    public int maxWithin(double lon, double lat, double radiusKm) {
        int best = probability(lon, lat);
        double latSpan = Math.toDegrees(radiusKm / Geo.EARTH_RADIUS_KM);
        int minLat = (int) Math.max(-90, Math.floor(lat - latSpan));
        int maxLat = (int) Math.min(90, Math.ceil(lat + latSpan));
        for (int cellLat = minLat; cellLat <= maxLat; cellLat++) {
            double cos = Math.min(Math.cos(Math.toRadians(lat)), Math.cos(Math.toRadians(cellLat)));
            int lonSpan = cos <= 1e-6 ? 180 : (int) Math.min(180, Math.ceil(latSpan / cos) + 1);
            int center = (int) Math.round(lon);
            for (int d = -lonSpan; d <= lonSpan; d++) {
                int cellLon = Math.floorMod(center + d, LONS);
                int p = cells[index(cellLon, cellLat)];
                if (p > best && Geo.haversineKm(lat, lon, cellLat, cellLon) <= radiusKm) {
                    best = p;
                }
            }
        }
        return best;
    }
}
