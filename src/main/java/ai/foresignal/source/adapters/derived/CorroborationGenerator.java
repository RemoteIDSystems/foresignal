package ai.foresignal.source.adapters.derived;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.derive.LayerStatus;
import ai.foresignal.derive.Observation;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * {@code derived-corroboration} (prompt 099): clusters event records across <em>independent</em>
 * sources so a single event reported by several feeds becomes one corroborated record with an
 * Admiralty credibility. Families and tolerances: earthquakes (Δt ≤ 60 s, ≤ 100 km, |ΔM| ≤ 0.8:
 * USGS, EMSC, FDSN); fires (≤ 1 km, ≤ 1 h: FIRMS, GOES FDC); conflict events (same 0.1° cell and UTC
 * day: GDELT, ACLED, UCDP); outages (same country, overlapping: IODA, Cloudflare Radar).
 *
 * <p>{@code externalId} is {@code {family}:{clusterUlid}}. Credibility is 2 ("probably true", ≥ 2
 * independent sources), 3 ("possibly true", one) or 6 (unknown); {@code reliability} is {@code F}
 * until the 109 grades exist. "F" and "6" mean unknown, never false.
 */
@Component
public class CorroborationGenerator extends LayerGenerator {

    /** The source id. */
    public static final String ID = "derived-corroboration";

    /** The record kind. */
    public static final String KIND = "corroboration";

    /** One event family and its independent sources. */
    enum Family {

        EARTHQUAKE("earthquake", Set.of(DerivedSources.USGS, DerivedSources.EMSC, DerivedSources.FDSN),
                Set.of("earthquake")),
        FIRE("fire", Set.of(DerivedSources.FIRMS, DerivedSources.GOES_FDC), Set.of("fire-detection")),
        CONFLICT("conflict", Set.of(DerivedSources.GDELT, "acled", "ucdp"), Set.of("conflict-event")),
        OUTAGE("outage", Set.of(DerivedSources.IODA, DerivedSources.CLOUDFLARE_RADAR),
                Set.of("outage", "internet-outage"));

        final String id;
        final Set<String> sources;
        final Set<String> kinds;

        Family(String id, Set<String> sources, Set<String> kinds) {
            this.id = id;
            this.sources = sources;
            this.kinds = kinds;
        }
    }

    private final SourceDescriptor descriptor = layer(ID, "Event corroboration (derived)",
            "Events reported by several independent sources, clustered with an Admiralty credibility."
                    + " Automated, not a warning.",
            "derived (input licences)", KIND).build();

    /** Creates the generator. */
    public CorroborationGenerator(ObjectProvider<SourceRegistry> registry) {
        super(registry);
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    protected Set<String> inputPrefixes() {
        return Set.of();
    }

    @Override
    protected Set<String> explicitInputSources() {
        Set<String> all = new TreeSet<>();
        for (Family family : Family.values()) {
            all.addAll(family.sources);
        }
        return all;
    }

    @Override
    protected Set<String> inputKinds() {
        Set<String> all = new TreeSet<>();
        for (Family family : Family.values()) {
            all.addAll(family.kinds);
        }
        return all;
    }

    @Override
    protected Duration tick() {
        return Duration.ofMinutes(5);
    }

    @Override
    protected void clearState() {
        // Stateless: clusters are recomputed from the current window each tick.
    }

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        int cap = maxEntities(ctx.params());
        for (Family family : Family.values()) {
            List<Observation> observations =
                    ctx.observations().recent(family.sources, family.kinds, now.minus(Duration.ofHours(24)), cap);
            for (List<Observation> cluster : cluster(family, observations)) {
                emit(ctx, now, family, cluster);
            }
        }
    }

    private void emit(DerivedContext ctx, Instant now, Family family, List<Observation> cluster) {
        Set<String> independent = new LinkedHashSet<>();
        for (Observation observation : cluster) {
            independent.add(observation.sourceId());
        }
        int credibility = independent.size() >= 2 ? 2 : 3;
        List<Map<String, Object>> members = new ArrayList<>();
        Instant observed = now;
        for (Observation observation : cluster) {
            members.add(Map.of("sourceId", observation.sourceId(), "externalId", observation.externalId(),
                    "observedAt", observation.observedAt() == null ? now.toString() : observation.observedAt().toString()));
            if (observation.observedAt() != null && observation.observedAt().isBefore(observed)) {
                observed = observation.observedAt();
            }
        }
        Map<String, Object> payload = LayerPayload.base(ID, LayerStatus.OK, 0.5, List.of());
        payload.put("members", members);
        payload.put("independentSources", new ArrayList<>(independent));
        payload.put("credibility", credibility);
        payload.put("reliability", "F");
        String id = family.id + ":" + ulid(cluster);
        NormalizedRecord.Builder builder = NormalizedRecord.builder(id, KIND).observedAt(observed);
        Observation first = cluster.get(0);
        if (first.geometry() != null) {
            builder.geometry(first.geometry());
        }
        ctx.emit(builder.putAll(payload).build());
    }

    /**
     * Greedy clustering of a family: each observation joins the first cluster it is compatible with,
     * otherwise starts one.
     */
    static List<List<Observation>> cluster(Family family, List<Observation> observations) {
        List<List<Observation>> clusters = new ArrayList<>();
        for (Observation observation : observations) {
            List<Observation> target = null;
            for (List<Observation> cluster : clusters) {
                if (compatible(family, cluster.get(0), observation)) {
                    target = cluster;
                    break;
                }
            }
            if (target == null) {
                target = new ArrayList<>();
                clusters.add(target);
            }
            target.add(observation);
        }
        return clusters;
    }

    /** True when two observations of a family describe the same event. */
    static boolean compatible(Family family, Observation a, Observation b) {
        Double aLon = a.longitude();
        Double bLon = b.longitude();
        Double aLat = a.latitude();
        Double bLat = b.latitude();
        Instant aAt = a.observedAt();
        Instant bAt = b.observedAt();
        return switch (family) {
            case EARTHQUAKE -> withinKm(aLon, aLat, bLon, bLat, 100)
                    && within(aAt, bAt, Duration.ofSeconds(60))
                    && magnitudeClose(a, b, 0.8);
            case FIRE -> withinKm(aLon, aLat, bLon, bLat, 1) && within(aAt, bAt, Duration.ofHours(1));
            case CONFLICT -> sameCellAndDay(a, b);
            case OUTAGE -> countryEquals(a, b) && overlapping(a, b);
        };
    }

    private static boolean withinKm(Double aLon, Double aLat, Double bLon, Double bLat, double km) {
        return aLon != null && aLat != null && bLon != null && bLat != null
                && Geo.haversineKm(aLat, aLon, bLat, bLon) <= km;
    }

    private static boolean within(Instant a, Instant b, Duration tolerance) {
        return a == null || b == null || Duration.between(a, b).abs().compareTo(tolerance) <= 0;
    }

    private static boolean magnitudeClose(Observation a, Observation b, double tolerance) {
        Double ma = firstNumber(a, "mag", "magnitude");
        Double mb = firstNumber(b, "mag", "magnitude");
        return ma == null || mb == null || Math.abs(ma - mb) <= tolerance;
    }

    private static boolean sameCellAndDay(Observation a, Observation b) {
        if (a.longitude() == null || b.longitude() == null) {
            return false;
        }
        boolean cell = Math.floor(a.longitude() * 10) == Math.floor(b.longitude() * 10)
                && Math.floor(a.latitude() * 10) == Math.floor(b.latitude() * 10);
        boolean day = a.observedAt() != null && b.observedAt() != null
                && a.observedAt().atZone(ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS)
                        .equals(b.observedAt().atZone(ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS));
        return cell && day;
    }

    private static boolean countryEquals(Observation a, Observation b) {
        String ca = firstText(a, "country", "countryCode", "countryIso");
        String cb = firstText(b, "country", "countryCode", "countryIso");
        return ca != null && ca.equals(cb);
    }

    private static boolean overlapping(Observation a, Observation b) {
        return within(a.observedAt(), b.observedAt(), Duration.ofHours(6));
    }

    private static Double firstNumber(Observation observation, String... keys) {
        for (String key : keys) {
            Double value = observation.number(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static String firstText(Observation observation, String... keys) {
        for (String key : keys) {
            String value = observation.text(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /** A deterministic cluster id from its members. */
    static String ulid(List<Observation> cluster) {
        StringBuilder builder = new StringBuilder();
        for (Observation observation : cluster) {
            builder.append(observation.sourceId()).append('/').append(observation.externalId()).append(';');
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(builder.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                hex.append(String.format("%02x", digest[i]));
            }
            return hex.toString();
        } catch (Exception e) {
            return Integer.toHexString(builder.toString().hashCode());
        }
    }
}
