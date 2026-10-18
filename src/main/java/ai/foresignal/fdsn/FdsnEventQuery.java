package ai.foresignal.fdsn;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The parameters of one {@code GET /fdsnws/event/1/query} (fdsnws-event 1.2, prompt 141).
 *
 * <p>Long and short names are both accepted ({@code starttime}/{@code start}, {@code minlatitude}/{@code minlat},
 * {@code minmagnitude}/{@code minmag}, …), case-insensitively. Times are UTC: an ISO 8601 date-time with or without a
 * zone, or a bare date (midnight). A rectangle and a circle may be combined; an event must satisfy both. Any other
 * parameter (for example {@code eventid}, {@code includeallorigins}, {@code updatedafter}) is a 400, as the
 * specification asks of parameters a service does not support. {@code nodata} selects 204 (default) or 404 for an
 * empty result.
 *
 * @param start origin time lower bound (inclusive), or null
 * @param end origin time upper bound (inclusive), or null
 * @param minLatitude rectangle south edge (default -90)
 * @param maxLatitude rectangle north edge (default 90)
 * @param minLongitude rectangle west edge (default -180)
 * @param maxLongitude rectangle east edge (default 180)
 * @param latitude circle center latitude, or null when no circle is requested
 * @param longitude circle center longitude, or null when no circle is requested
 * @param minRadius circle inner radius in degrees (default 0)
 * @param maxRadius circle outer radius in degrees (default 180)
 * @param minMagnitude magnitude lower bound, or null
 * @param maxMagnitude magnitude upper bound, or null
 * @param orderBy the ordering
 * @param limit the most events returned
 * @param format {@code xml} or {@code text}
 * @param noData 204 or 404, the status of an empty result
 */
public record FdsnEventQuery(
        Instant start,
        Instant end,
        double minLatitude,
        double maxLatitude,
        double minLongitude,
        double maxLongitude,
        Double latitude,
        Double longitude,
        double minRadius,
        double maxRadius,
        Double minMagnitude,
        Double maxMagnitude,
        OrderBy orderBy,
        int limit,
        String format,
        int noData) {

    /** The largest {@code limit} (the spec's maximum). */
    public static final int MAX_LIMIT = 20_000;

    /** The orderings of fdsnws-event 1.2. */
    public enum OrderBy {
        /** Newest origin first (the default). */
        TIME("time"),
        /** Oldest origin first. */
        TIME_ASC("time-asc"),
        /** Largest magnitude first. */
        MAGNITUDE("magnitude"),
        /** Smallest magnitude first. */
        MAGNITUDE_ASC("magnitude-asc");

        private final String wire;

        OrderBy(String wire) {
            this.wire = wire;
        }

        /** The wire value. */
        public String wire() {
            return wire;
        }

        static OrderBy parse(String value) {
            for (OrderBy order : values()) {
                if (order.wire.equalsIgnoreCase(value.strip())) {
                    return order;
                }
            }
            throw new FdsnRequestException("Invalid orderby '" + value + "': use time, time-asc, magnitude or"
                    + " magnitude-asc");
        }
    }

    /** Parameters this service accepts besides the query parameters (a feed key, 035). */
    static final Set<String> PASS_THROUGH = Set.of("key");

    /** True when a rectangle narrower than the whole globe was requested. */
    public boolean hasBox() {
        return minLatitude > -90 || maxLatitude < 90 || minLongitude > -180 || maxLongitude < 180;
    }

    /** True when a circle was requested. */
    public boolean hasCircle() {
        return latitude != null || longitude != null || minRadius > 0 || maxRadius < 180;
    }

    /**
     * Parses the request parameters.
     *
     * @param parameters the servlet parameter map (name to values)
     * @throws FdsnRequestException for an unknown parameter, a repeated one, or a bad value
     */
    public static FdsnEventQuery parse(Map<String, String[]> parameters) {
        Instant start = null;
        Instant end = null;
        double minLat = -90;
        double maxLat = 90;
        double minLon = -180;
        double maxLon = 180;
        Double lat = null;
        Double lon = null;
        double minRadius = 0;
        double maxRadius = 180;
        Double minMag = null;
        Double maxMag = null;
        OrderBy order = OrderBy.TIME;
        int limit = MAX_LIMIT;
        String format = "xml";
        int noData = 204;
        for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
            String name = entry.getKey().toLowerCase(Locale.ROOT);
            if (PASS_THROUGH.contains(name)) {
                continue;
            }
            String[] values = entry.getValue();
            if (values == null || values.length != 1) {
                throw new FdsnRequestException("Parameter '" + entry.getKey() + "' must be given exactly once");
            }
            String value = values[0] == null ? "" : values[0].strip();
            if (value.isEmpty()) {
                throw new FdsnRequestException("Parameter '" + entry.getKey() + "' is empty");
            }
            switch (name) {
                case "starttime", "start" -> start = time(name, value);
                case "endtime", "end" -> end = time(name, value);
                case "minlatitude", "minlat" -> minLat = number(name, value, -90, 90);
                case "maxlatitude", "maxlat" -> maxLat = number(name, value, -90, 90);
                case "minlongitude", "minlon" -> minLon = number(name, value, -180, 180);
                case "maxlongitude", "maxlon" -> maxLon = number(name, value, -180, 180);
                case "latitude", "lat" -> lat = number(name, value, -90, 90);
                case "longitude", "lon" -> lon = number(name, value, -180, 180);
                case "minradius" -> minRadius = number(name, value, 0, 180);
                case "maxradius" -> maxRadius = number(name, value, 0, 180);
                case "minmagnitude", "minmag" -> minMag = number(name, value, -20, 20);
                case "maxmagnitude", "maxmag" -> maxMag = number(name, value, -20, 20);
                case "orderby" -> order = OrderBy.parse(value);
                case "limit" -> limit = (int) integer(name, value, 1, MAX_LIMIT);
                case "format" -> {
                    String f = value.toLowerCase(Locale.ROOT);
                    if (!f.equals("xml") && !f.equals("text")) {
                        throw new FdsnRequestException("Invalid format '" + value + "': use xml or text");
                    }
                    format = f;
                }
                case "nodata" -> {
                    if (!value.equals("204") && !value.equals("404")) {
                        throw new FdsnRequestException("Invalid nodata '" + value + "': use 204 or 404");
                    }
                    noData = Integer.parseInt(value);
                }
                default -> throw new FdsnRequestException("Unsupported parameter '" + entry.getKey() + "'");
            }
        }
        if (start != null && end != null && end.isBefore(start)) {
            throw new FdsnRequestException("endtime is before starttime");
        }
        if (minLat > maxLat) {
            throw new FdsnRequestException("minlatitude is greater than maxlatitude");
        }
        if (minRadius > maxRadius) {
            throw new FdsnRequestException("minradius is greater than maxradius");
        }
        if (minMag != null && maxMag != null && minMag > maxMag) {
            throw new FdsnRequestException("minmagnitude is greater than maxmagnitude");
        }
        if (lat != null || lon != null || minRadius > 0 || maxRadius < 180) {
            // A circle was asked for: fdsnws defaults a missing center coordinate to 0.
            lat = lat == null ? 0.0 : lat;
            lon = lon == null ? 0.0 : lon;
        }
        return new FdsnEventQuery(start, end, minLat, maxLat, minLon, maxLon, lat, lon, minRadius, maxRadius, minMag,
                maxMag, order, limit, format, noData);
    }

    /** An FDSN time: ISO 8601 with or without a zone (UTC when absent), or a date. */
    static Instant time(String name, String value) {
        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (DateTimeParseException ignored) {
            // not zoned
        }
        try {
            return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ignored) {
            // not a local date-time
        }
        try {
            return LocalDate.parse(value).atStartOfDay().toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            throw new FdsnRequestException("Invalid " + name + " '" + value
                    + "': use YYYY-MM-DDThh:mm:ss[.ssssss] (UTC) or YYYY-MM-DD");
        }
    }

    private static double number(String name, String value, double min, double max) {
        double parsed;
        try {
            parsed = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new FdsnRequestException("Invalid " + name + " '" + value + "': not a number");
        }
        if (!Double.isFinite(parsed) || parsed < min || parsed > max) {
            throw new FdsnRequestException("Invalid " + name + " '" + value + "': must be between " + min + " and "
                    + max);
        }
        return parsed;
    }

    private static long integer(String name, String value, long min, long max) {
        long parsed;
        try {
            parsed = Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new FdsnRequestException("Invalid " + name + " '" + value + "': not an integer");
        }
        if (parsed < min || parsed > max) {
            throw new FdsnRequestException("Invalid " + name + " '" + value + "': must be between " + min + " and "
                    + max);
        }
        return parsed;
    }
}
