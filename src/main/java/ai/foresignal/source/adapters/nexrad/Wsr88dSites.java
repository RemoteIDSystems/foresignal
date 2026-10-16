package ai.foresignal.source.adapters.nexrad;

import ai.foresignal.source.spi.Geo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The bundled WSR-88D site table (prompt 106): the 160 US NEXRAD sites' 3-letter ids, names and
 * points, from {@code reference/wsr88d-sites.csv}. Used to pick the 1–3 nearest sites to the home.
 */
public final class Wsr88dSites {

    /** The resource path. */
    public static final String RESOURCE = "reference/wsr88d-sites.csv";

    /** A site. */
    public record Site(String id, String name, double lat, double lon, String state) {}

    private static volatile Wsr88dSites bundled;

    private final List<Site> sites;

    private Wsr88dSites(List<Site> sites) {
        this.sites = sites;
    }

    /** The bundled table (lazily loaded). */
    public static Wsr88dSites bundled() {
        Wsr88dSites local = bundled;
        if (local == null) {
            synchronized (Wsr88dSites.class) {
                local = bundled;
                if (local == null) {
                    local = read(Wsr88dSites.class.getClassLoader().getResourceAsStream(RESOURCE));
                    bundled = local;
                }
            }
        }
        return local;
    }

    /** Reads a table. */
    public static Wsr88dSites read(InputStream in) {
        if (in == null) {
            throw new IllegalStateException(RESOURCE + " is missing");
        }
        List<Site> out = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            if (line == null) {
                throw new IOException("empty");
            }
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(",", 5);
                if (parts.length < 5) {
                    continue;
                }
                try {
                    out.add(new Site(parts[0].toUpperCase(java.util.Locale.ROOT), parts[1],
                            Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), parts[4]));
                } catch (NumberFormatException ignored) {
                    // a malformed row is skipped
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + RESOURCE, e);
        }
        return new Wsr88dSites(out);
    }

    /** The site with {@code id}, or null. */
    public Site find(String id) {
        for (Site s : sites) {
            if (s.id().equalsIgnoreCase(id)) {
                return s;
            }
        }
        return null;
    }

    /** The {@code count} nearest sites to {@code (lon, lat)}, nearest first. */
    public List<Site> nearest(double lon, double lat, int count) {
        return sites.stream().sorted(Comparator.comparingDouble(s -> Geo.haversineKm(lat, lon, s.lat(), s.lon())))
                .limit(Math.max(1, count)).toList();
    }

    /** The number of sites. */
    public int size() {
        return sites.size();
    }
}
