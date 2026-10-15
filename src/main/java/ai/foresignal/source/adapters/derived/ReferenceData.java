package ai.foresignal.source.adapters.derived;

import ai.foresignal.derive.Observation;
import ai.foresignal.derive.ObservationFeed;
import ai.foresignal.source.adapters.worldportindex.WpiParser;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Component;

/**
 * Reference layers the derived layers test positions against (prompt 099): the World Port Index
 * ports and their buffers, OurAirports, the submarine cables and the PortWatch chokepoints. Small,
 * cached for ten minutes, read from the current records through the {@link ObservationFeed}.
 */
@Component
public class ReferenceData {

    /** The longest a cached reference layer is used before it is reloaded. */
    public static final Duration CACHE_TTL = Duration.ofMinutes(10);

    /** The most reference rows loaded at once. */
    static final int LIMIT = 200_000;

    /** A World Port Index port. */
    public record Port(String portNumber, String name, double lon, double lat, double radiusKm) {}

    /** A submarine cable route. */
    public record Cable(String id, Geometry route) {}

    /** A PortWatch chokepoint point with its buffer radius. */
    public record Chokepoint(String id, String name, double lon, double lat, double radiusKm) {}

    /** An OurAirports airport. */
    public record Airport(String ident, String name, double lon, double lat) {}

    private final ObservationFeed feed;
    private final Chokepoints chokepoints;
    private final Clock clock;

    private final Map<String, Cached<?>> caches = new LinkedHashMap<>();

    /** Creates the reference view. */
    public ReferenceData(ObservationFeed feed, Chokepoints chokepoints, Clock clock) {
        this.feed = feed;
        this.chokepoints = chokepoints;
        this.clock = clock;
    }

    /** The World Port Index ports with their buffer radii. */
    public List<Port> ports() {
        return cached("ports", () -> {
            List<Port> out = new ArrayList<>();
            for (Observation observation : load(DerivedSources.WORLD_PORT_INDEX, "port")) {
                Double lon = observation.longitude();
                Double lat = observation.latitude();
                if (lon == null || lat == null) {
                    continue;
                }
                String id = strip(observation.externalId(), "wpi:");
                String name = observation.text("portName");
                double radius = WpiParser.bufferKm(observation.text("harborSize"));
                out.add(new Port(id, name, lon, lat, radius));
            }
            return out;
        });
    }

    /** The submarine cable routes. */
    public List<Cable> cables() {
        return cached("cables", () -> {
            List<Cable> out = new ArrayList<>();
            for (Observation observation : load(DerivedSources.SUBMARINE_CABLES, "cable")) {
                Geometry route = observation.geometry();
                if (route == null || route.isEmpty()) {
                    continue;
                }
                out.add(new Cable(strip(observation.externalId(), "cable:"), route));
            }
            return out;
        });
    }

    /** The chokepoint points, one per named chokepoint, with their buffer radii. */
    public List<Chokepoint> chokepoints() {
        return cached("chokepoints", () -> {
            Map<String, Chokepoint> byName = new LinkedHashMap<>();
            for (Observation observation : load(DerivedSources.CHOKEPOINTS, "chokepoint-transit")) {
                Double lon = observation.longitude();
                Double lat = observation.latitude();
                String name = observation.text("chokepoint");
                if (lon == null || lat == null || name == null) {
                    continue;
                }
                if (byName.containsKey(name)) {
                    continue;
                }
                double radius = chokepoints.radiusFor(name);
                byName.put(name, new Chokepoint(slug(name), name, lon, lat, radius));
            }
            return new ArrayList<>(byName.values());
        });
    }

    /** The OurAirports airports. */
    public List<Airport> airports() {
        return cached("airports", () -> {
            List<Airport> out = new ArrayList<>();
            for (Observation observation : load(DerivedSources.OURAIRPORTS, "airport")) {
                Double lon = observation.longitude();
                Double lat = observation.latitude();
                if (lon == null || lat == null) {
                    continue;
                }
                out.add(new Airport(observation.externalId(), observation.text("name"), lon, lat));
            }
            return out;
        });
    }

    /** Drops the caches, so the next read reloads (called on reconcile). */
    public void refresh() {
        synchronized (caches) {
            caches.clear();
        }
    }

    private List<Observation> load(String sourceId, String kind) {
        return feed.recent(Set.of(sourceId), Set.of(kind), Instant.EPOCH, LIMIT);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> cached(String key, java.util.function.Supplier<List<T>> loader) {
        Instant now = clock.instant();
        synchronized (caches) {
            Cached<?> cached = caches.get(key);
            if (cached != null && cached.loadedAt() != null && Duration.between(cached.loadedAt(), now).compareTo(CACHE_TTL) < 0) {
                return (List<T>) cached.value();
            }
            List<T> value = loader.get();
            caches.put(key, new Cached<>(value, now));
            return value;
        }
    }

    private record Cached<T>(List<T> value, Instant loadedAt) {}

    private static String strip(String externalId, String prefix) {
        return externalId != null && externalId.startsWith(prefix) ? externalId.substring(prefix.length()) : externalId;
    }

    private static String slug(String name) {
        return name.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
