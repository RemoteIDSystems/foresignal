package ai.foresignal.source.adapters.env.radnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The bundled EPA RadNet station list ({@value #RESOURCE}, prompt 078): the 132 fixed air monitors with a 2026 CSV
 * link on EPA's "RadNet CSV File Downloads" page (captured 2026-10-13), keyed {@code STATE/CITY} as EPA's CSV paths
 * name them (upper case, spaces kept). The page gives no coordinates; each station's point is the internal point
 * of the named place in the US Census Bureau 2024 Gazetteer, so it is approximate (the city, not the monitor
 * site).
 */
public final class RadnetStations {

    /** The classpath resource. */
    public static final String RESOURCE = "radnet/stations.json";

    /**
     * One station.
     *
     * @param id {@code STATE/CITY}, upper case
     * @param name a display name ({@code Denver, CO})
     * @param lat the approximate latitude
     * @param lon the approximate longitude
     */
    public record Station(String id, String name, double lat, double lon) {

        /** The two-letter state or territory code. */
        public String state() {
            return id.substring(0, id.indexOf('/'));
        }

        /** The city part as EPA's CSV path names it. */
        public String city() {
            return id.substring(id.indexOf('/') + 1);
        }
    }

    private static volatile Map<String, Station> stations;

    private RadnetStations() {}

    /** Every station by id, in id order. */
    public static Map<String, Station> all() {
        Map<String, Station> s = stations;
        if (s == null) {
            synchronized (RadnetStations.class) {
                s = stations;
                if (s == null) {
                    s = load();
                    stations = s;
                }
            }
        }
        return s;
    }

    /** The station with {@code id} after {@link #normalize}, if listed. */
    public static Optional<Station> find(String id) {
        return id == null ? Optional.empty() : Optional.ofNullable(all().get(normalize(id)));
    }

    /** {@code id} trimmed, upper case, with runs of spaces collapsed and spaces around the slash removed. */
    public static String normalize(String id) {
        return id.strip().toUpperCase(Locale.ROOT).replaceAll("\\s+", " ").replaceAll("\\s*/\\s*", "/");
    }

    private static Map<String, Station> load() {
        try (InputStream in = RadnetStations.class.getClassLoader().getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("The RadNet station list " + RESOURCE + " is missing");
            }
            JsonNode root = JsonMapper.builder().build().readTree(in);
            Map<String, Station> out = new LinkedHashMap<>();
            for (JsonNode s : root.path("stations")) {
                String id = s.path("id").asString();
                out.put(id, new Station(id, s.path("name").asString(), s.path("lat").asDouble(),
                        s.path("lon").asDouble()));
            }
            return Collections.unmodifiableMap(out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
