package ai.foresignal.source.adapters.ourairports;

import ai.foresignal.source.formats.csv.CsvReader;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * How a VFR sectional chart draws each airport, from OurAirports' {@code runways.csv} and
 * {@code airport-frequencies.csv}, after the FAA Aeronautical Chart User's Guide ("Airports"):
 *
 * <ul>
 *   <li>Color: an airport with a control tower is blue, every other airport magenta ({@link Info#towered()}: it
 *       publishes a {@code TWR} frequency).
 *   <li>Symbol ({@link #symbol}): a hard-surfaced runway longer than 8,069 ft is drawn as the runway pattern
 *       alone ({@link #MAJOR}); a hard-surfaced runway of 1,500 to 8,069 ft as a circle around the runway
 *       ({@link #HARD}); anything else as a solid circle ({@link #SOFT}). Heliports, seaplane bases and closed
 *       airports have their own symbols.
 *   <li>Orientation: the runway is drawn along the true heading of the longest runway ({@link Info#heading()}).
 * </ul>
 *
 * <p>The chart also marks fuel and services with ticks around the circle and private airports with an R;
 * OurAirports publishes neither, so neither is drawn. A real chart draws the actual runway layout of a major
 * airport; here the pattern is its longest runway.
 */
public final class AirportChart {

    /** Runway pattern alone: a hard-surfaced runway longer than {@link #MAJOR_FT}. */
    public static final String MAJOR = "major";

    /** A circle around the runway: a hard-surfaced runway of {@link #HARD_FT} to {@link #MAJOR_FT}. */
    public static final String HARD = "hard";

    /** A solid circle: no hard-surfaced runway as long as {@link #HARD_FT}. */
    public static final String SOFT = "soft";

    /** A heliport. */
    public static final String HELIPORT = "heliport";

    /** A seaplane base. */
    public static final String SEAPLANE = "seaplane";

    /** A closed airport. */
    public static final String CLOSED = "closed";

    /** The longest hard-surfaced runway a circled symbol is drawn for, in feet. */
    public static final int MAJOR_FT = 8_069;

    /** The shortest hard-surfaced runway a circled runway symbol is drawn for, in feet. */
    public static final int HARD_FT = 1_500;

    /** The {@code runwaySurface} of a paved runway. */
    public static final String SURFACE_HARD = "hard";

    /** The {@code runwaySurface} of an unpaved runway. */
    public static final String SURFACE_SOFT = "soft";

    /** The {@code runwaySurface} of a water runway. */
    public static final String SURFACE_WATER = "water";

    /**
     * Surface codes that are paved, as prefixes of the uppercased {@code surface}: asphalt, concrete, partly
     * paved ({@code PEM}), bitumen, paved, tarmac, macadam and sealed.
     */
    static final List<String> HARD_SURFACES = List.of("ASP", "CON", "PEM", "BIT", "PAV", "TAR", "MAC", "SEAL");

    /** Surface codes that are water. */
    static final List<String> WATER_SURFACES = List.of("WAT");

    /** No chart details: an airport neither file mentions. */
    public static final Info NONE = new Info(false, null, null, null, null);

    private AirportChart() {}

    /**
     * One airport's chart details.
     *
     * @param towered whether it publishes a {@code TWR} frequency
     * @param surface {@link #SURFACE_HARD}, {@link #SURFACE_SOFT} or {@link #SURFACE_WATER} for its longest
     *     open runway, or null when it lists none
     * @param longestFt its longest open runway in feet, or null
     * @param longestHardFt its longest open hard-surfaced runway in feet, or null
     * @param heading the true heading of its longest open runway, 0 to 180 degrees, or null when unknown
     */
    public record Info(boolean towered, String surface, Long longestFt, Long longestHardFt, Double heading) {}

    /**
     * The chart details of every airport the files mention, by {@code airport_ident}.
     *
     * @param runways the {@code runways.csv} body
     * @param frequencies the {@code airport-frequencies.csv} body
     */
    public static Map<String, Info> index(byte[] runways, byte[] frequencies) {
        Set<String> towered = new HashSet<>();
        read(frequencies, "airport-frequencies.csv", (row, index) -> {
            String type = CsvReader.get(row, index, "TYPE");
            String ident = CsvReader.get(row, index, "AIRPORT_IDENT");
            if (ident != null && type != null && type.strip().toUpperCase(Locale.ROOT).startsWith("TWR")) {
                towered.add(ident);
            }
        });
        Map<String, Runways> byAirport = new HashMap<>();
        read(runways, "runways.csv", (row, index) -> {
            String ident = CsvReader.get(row, index, "AIRPORT_IDENT");
            if (ident == null || "1".equals(CsvReader.get(row, index, "CLOSED"))) {
                return;
            }
            byAirport.computeIfAbsent(ident, key -> new Runways()).add(
                    surface(CsvReader.get(row, index, "SURFACE")),
                    integer(CsvReader.get(row, index, "LENGTH_FT")),
                    heading(row, index));
        });
        Map<String, Info> out = new HashMap<>();
        for (Map.Entry<String, Runways> entry : byAirport.entrySet()) {
            Runways r = entry.getValue();
            out.put(entry.getKey(), new Info(towered.contains(entry.getKey()), r.surface, r.longestFt, r.longestHardFt,
                    r.heading));
        }
        for (String ident : towered) {
            out.putIfAbsent(ident, new Info(true, null, null, null, null));
        }
        return out;
    }

    /**
     * The chart symbol of an airport: its {@code type} for a heliport, seaplane base or closed airport, else its
     * longest hard-surfaced runway. An airport whose runways are unknown is drawn as its type suggests: a large
     * airport as {@link #MAJOR}, a medium one as {@link #HARD}, any other as {@link #SOFT}.
     *
     * @param type the OurAirports {@code type}, or null
     * @param info its chart details, or {@link #NONE}
     */
    public static String symbol(String type, Info info) {
        String t = type == null ? "" : type.toLowerCase(Locale.ROOT);
        switch (t) {
            case "closed":
                return CLOSED;
            case "heliport":
                return HELIPORT;
            case "seaplane_base":
                return SEAPLANE;
            default:
                break;
        }
        if (info.longestFt() == null) {
            return switch (t) {
                case "large_airport" -> MAJOR;
                case "medium_airport" -> HARD;
                default -> SOFT;
            };
        }
        Long hard = info.longestHardFt();
        if (hard != null && hard > MAJOR_FT) {
            return MAJOR;
        }
        if (hard != null && hard >= HARD_FT) {
            return HARD;
        }
        return SOFT;
    }

    /** Whether a symbol is drawn along the runway: the runway pattern, the circled runway and the solid circle. */
    public static boolean turnsWithRunway(String symbol) {
        return MAJOR.equals(symbol) || HARD.equals(symbol) || SOFT.equals(symbol);
    }

    /** {@link #SURFACE_HARD}, {@link #SURFACE_WATER} or {@link #SURFACE_SOFT} for a surface code. */
    static String surface(String code) {
        if (code == null || code.isBlank()) {
            return SURFACE_SOFT;
        }
        String c = code.strip().toUpperCase(Locale.ROOT);
        if (WATER_SURFACES.stream().anyMatch(c::startsWith)) {
            return SURFACE_WATER;
        }
        return HARD_SURFACES.stream().anyMatch(c::startsWith) ? SURFACE_HARD : SURFACE_SOFT;
    }

    /**
     * A runway's true heading folded into 0 to 180 degrees (a runway reads the same both ways): its low end's
     * {@code le_heading_degT}, else its high end's less 180, else the bearing between its two ends.
     */
    static Double heading(List<String> row, Map<String, Integer> index) {
        Double le = number(CsvReader.get(row, index, "LE_HEADING_DEGT"));
        if (le == null) {
            Double he = number(CsvReader.get(row, index, "HE_HEADING_DEGT"));
            le = he == null ? null : he - 180;
        }
        if (le == null) {
            Double lat1 = number(CsvReader.get(row, index, "LE_LATITUDE_DEG"));
            Double lon1 = number(CsvReader.get(row, index, "LE_LONGITUDE_DEG"));
            Double lat2 = number(CsvReader.get(row, index, "HE_LATITUDE_DEG"));
            Double lon2 = number(CsvReader.get(row, index, "HE_LONGITUDE_DEG"));
            if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
                return null;
            }
            double p1 = Math.toRadians(lat1);
            double p2 = Math.toRadians(lat2);
            double dl = Math.toRadians(lon2 - lon1);
            le = Math.toDegrees(Math.atan2(Math.sin(dl) * Math.cos(p2),
                    Math.cos(p1) * Math.sin(p2) - Math.sin(p1) * Math.cos(p2) * Math.cos(dl)));
        }
        double folded = ((le % 180) + 180) % 180;
        return Math.round(folded * 10) / 10.0;
    }

    /** The runways of one airport, reduced to what the chart needs. */
    private static final class Runways {
        Long longestFt;
        Long longestHardFt;
        String surface;
        Double heading;

        void add(String runwaySurface, Long lengthFt, Double runwayHeading) {
            long length = lengthFt == null ? 0 : lengthFt;
            if (SURFACE_HARD.equals(runwaySurface) && (longestHardFt == null || length > longestHardFt)) {
                longestHardFt = length;
            }
            if (longestFt == null || length > longestFt) {
                longestFt = length;
                surface = runwaySurface;
                heading = runwayHeading;
            } else if (heading == null && length == longestFt) {
                heading = runwayHeading;
            }
        }
    }

    private static void read(byte[] body, String what, BiConsumer<List<String>, Map<String, Integer>> rows) {
        try (CsvReader reader = new CsvReader(new InputStreamReader(new ByteArrayInputStream(body),
                StandardCharsets.UTF_8))) {
            List<String> header = reader.next();
            if (header == null) {
                throw new UpstreamFormatException("OurAirports " + what + " is empty", "");
            }
            Map<String, Integer> index = CsvReader.index(header);
            List<String> row;
            while ((row = reader.next()) != null) {
                try {
                    rows.accept(row, index);
                } catch (RuntimeException e) {
                    // One unreadable row is skipped; the airport keeps whatever the others gave it.
                }
            }
        } catch (IOException | UncheckedIOException e) {
            throw new UpstreamFormatException("OurAirports " + what + " could not be read: " + e.getMessage(), "", e);
        }
    }

    private static Double number(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            double parsed = Double.parseDouble(value.strip());
            return Double.isFinite(parsed) ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Long integer(String value) {
        Double parsed = number(value);
        return parsed == null ? null : Math.round(parsed);
    }
}
