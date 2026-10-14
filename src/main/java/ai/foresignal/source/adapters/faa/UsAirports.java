package ai.foresignal.source.adapters.faa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The bundled US airport table {@value #RESOURCE} (prompt 087): FAA location identifier, ICAO code, name and position
 * of every open large, medium and small airport in the United States and its territories, generated once by
 * {@code scripts/build-us-airports.py} from OurAirports' public-domain {@code airports.csv}. Loaded once, on first
 * use; a lookup by FAA identifier ({@code ORD}) or, failing that, ICAO code ({@code KORD}).
 */
public final class UsAirports {

    /** The classpath resource. */
    public static final String RESOURCE = "reference/us-airports.csv";

    /** The header the table must start with. */
    static final String HEADER = "faa_lid,icao,name,lat,lon";

    private static volatile UsAirports instance;

    private final Map<String, Airport> byLid;
    private final Map<String, Airport> byIcao;

    /**
     * One airport.
     *
     * @param faaLid the FAA location identifier
     * @param icao the ICAO code, or null
     * @param name the name
     * @param lat the latitude
     * @param lon the longitude
     */
    public record Airport(String faaLid, String icao, String name, double lat, double lon) {}

    private UsAirports(List<Airport> airports) {
        Map<String, Airport> lid = new HashMap<>();
        Map<String, Airport> icao = new HashMap<>();
        for (Airport a : airports) {
            lid.putIfAbsent(a.faaLid(), a);
            if (a.icao() != null) {
                icao.putIfAbsent(a.icao(), a);
            }
        }
        this.byLid = Map.copyOf(lid);
        this.byIcao = Map.copyOf(icao);
    }

    /** The bundled table, loaded on first use. */
    public static UsAirports bundled() {
        UsAirports loaded = instance;
        if (loaded == null) {
            synchronized (UsAirports.class) {
                loaded = instance;
                if (loaded == null) {
                    try (InputStream in = UsAirports.class.getClassLoader().getResourceAsStream(RESOURCE)) {
                        if (in == null) {
                            throw new IllegalStateException("The airport table " + RESOURCE + " is missing; run"
                                    + " scripts/build-us-airports.py");
                        }
                        loaded = read(in);
                    } catch (IOException e) {
                        throw new UncheckedIOException("Could not read " + RESOURCE, e);
                    }
                    instance = loaded;
                }
            }
        }
        return loaded;
    }

    /**
     * Reads a table in the bundled format.
     *
     * @throws IllegalStateException when the header is not {@value #HEADER}
     */
    static UsAirports read(InputStream in) throws IOException {
        List<Airport> airports = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null || !header.strip().equals(HEADER)) {
                throw new IllegalStateException(RESOURCE + " does not start with " + HEADER);
            }
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> cells = split(line);
                if (cells.size() != 5) {
                    continue;
                }
                try {
                    double lat = Double.parseDouble(cells.get(3));
                    double lon = Double.parseDouble(cells.get(4));
                    String icao = cells.get(1).isBlank() ? null : cells.get(1).strip().toUpperCase(Locale.ROOT);
                    airports.add(new Airport(cells.get(0).strip().toUpperCase(Locale.ROOT), icao, cells.get(2), lat,
                            lon));
                } catch (NumberFormatException ignored) {
                    // A malformed row is skipped; the generator writes none.
                }
            }
        }
        return new UsAirports(airports);
    }

    /** One CSV line split into cells; double-quoted cells may hold commas and doubled quotes. */
    static List<String> split(String line) {
        List<String> cells = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cell.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    cell.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                cells.add(cell.toString());
                cell.setLength(0);
            } else {
                cell.append(c);
            }
        }
        cells.add(cell.toString());
        return cells;
    }

    /** The airport of an FAA identifier or, failing that, an ICAO code; null when unknown. */
    public Airport find(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        String key = code.strip().toUpperCase(Locale.ROOT);
        Airport a = byLid.get(key);
        return a != null ? a : byIcao.get(key);
    }

    /** How many airports the table holds. */
    public int size() {
        return byLid.size();
    }
}
