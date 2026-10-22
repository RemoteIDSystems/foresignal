package ai.foresignal.source.adapters.derived;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.derive.LayerStatus;
import ai.foresignal.derive.Observation;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * {@code derived-chokepoint} (prompt 099): vessel behavior inside the buffered PortWatch
 * chokepoints. Per vessel inside a chokepoint it flags a course change over 60° within 10 minutes,
 * a U-turn over 150°, or a speed drop over 50% to below 3 knots; per chokepoint it publishes hourly
 * banded counts of transits, U-turns and stops. Baselines and anomaly scoring are the 100 layer's.
 *
 * <p>Kinds: {@code chokepoint-activity} ({@code externalId {chokepointId}}) and
 * {@code chokepoint-uturn} ({@code externalId {mmsi}:{chokepointId}:{epoch}}).
 */
@Component
public class ChokepointGenerator extends LayerGenerator {

    /** The source id. */
    public static final String ID = "derived-chokepoint";

    /** The activity kind. */
    public static final String ACTIVITY_KIND = "chokepoint-activity";

    /** The U-turn kind. */
    public static final String UTURN_KIND = "chokepoint-uturn";

    /** The course-change threshold, degrees. */
    public static final double COURSE_DEG = 60;

    /** The U-turn threshold, degrees. */
    public static final double UTURN_DEG = 150;

    /** The slow speed, knots. */
    public static final double SLOW_KN = 3;

    /** The course-change window. */
    public static final Duration COURSE_WINDOW = Duration.ofMinutes(10);

    /** The count bands (lower bounds). */
    static final int[] BANDS = {0, 1, 3, 5, 10, 20, 50, 100};

    private final SourceDescriptor descriptor = layer(ID, "Chokepoint activity (derived)",
            "Vessel transits, U-turns and stops inside the PortWatch chokepoints, hourly. Automated, not a"
                    + " warning.",
            "derived (input licences; ODbL for keyless AIS)", ACTIVITY_KIND).kind(UTURN_KIND).build();

    private final ReferenceData reference;
    private final Map<String, Vessel> vessels = new LinkedHashMap<>();
    private final Map<String, Hour> hours = new LinkedHashMap<>();

    /** Creates the generator. */
    public ChokepointGenerator(ObjectProvider<SourceRegistry> registry, ReferenceData reference) {
        super(registry);
        this.reference = reference;
    }

    /** A vessel's chokepoint state. */
    static final class Vessel {

        Double cog;
        Double sog;
        Instant at;
        String insideId;
    }

    /** One chokepoint-hour. */
    static final class Hour {

        long epochHour;
        final Set<String> transits = new LinkedHashSet<>();
        int uTurns;
        int stopped;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    protected Set<String> inputPrefixes() {
        return DerivedSources.VESSEL_PREFIXES;
    }

    @Override
    protected Set<String> inputKinds() {
        return Set.of(DerivedSources.VESSEL);
    }

    @Override
    protected Duration tick() {
        return Duration.ofMinutes(5);
    }

    @Override
    protected void clearState() {
        vessels.clear();
        hours.clear();
        reference.refresh();
    }

    @Override
    public int stateSize() {
        return vessels.size();
    }

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        int cap = maxEntities(ctx.params());
        List<Observation> observations =
                ctx.observations().recent(inputSources(), Set.of(DerivedSources.VESSEL), now.minus(Duration.ofHours(1)), cap);
        List<ReferenceData.Chokepoint> chokepoints = reference.chokepoints();
        LayerStatus coverage = status(ctx, now.minus(COURSE_WINDOW));
        long epochHour = now.getEpochSecond() / 3600;
        for (Observation observation : observations) {
            Double lon = observation.longitude();
            Double lat = observation.latitude();
            if (lon == null || lat == null) {
                continue;
            }
            ReferenceData.Chokepoint inside = containing(chokepoints, lon, lat);
            String mmsi = observation.externalId();
            Vessel state = vessels.computeIfAbsent(mmsi, k -> new Vessel());
            if (inside == null) {
                state.insideId = null;
            } else {
                if (!inside.id().equals(state.insideId)) {
                    state.insideId = inside.id();
                    hour(inside.id(), epochHour).transits.add(mmsi);
                }
                Double sog = LayerSupport.sog(observation);
                Double cog = LayerSupport.cog(observation);
                Instant at = observation.observedAt();
                if (cog != null && state.cog != null && state.at != null
                        && Duration.between(state.at, at).compareTo(COURSE_WINDOW) <= 0) {
                    double delta = Math.abs(wrap(cog - state.cog));
                    if (delta > UTURN_DEG) {
                        hour(inside.id(), epochHour).uTurns++;
                        emitUturn(ctx, now, mmsi, inside, at, delta);
                    }
                }
                if (sog != null && state.sog != null && state.sog > 0 && sog < SLOW_KN && sog < 0.5 * state.sog) {
                    hour(inside.id(), epochHour).stopped++;
                }
                state.sog = sog;
                state.cog = cog;
                state.at = at;
            }
        }
        for (ReferenceData.Chokepoint chokepoint : chokepoints) {
            Hour hour = hours.get(chokepoint.id());
            if (hour == null) {
                continue;
            }
            emitActivity(ctx, now, chokepoint, hour, coverage, epochHour);
        }
    }

    private Hour hour(String chokepointId, long epochHour) {
        return hours.compute(chokepointId, (k, existing) -> {
            if (existing == null || existing.epochHour != epochHour) {
                Hour fresh = new Hour();
                fresh.epochHour = epochHour;
                return fresh;
            }
            return existing;
        });
    }

    private void emitUturn(DerivedContext ctx, Instant now, String mmsi, ReferenceData.Chokepoint chokepoint,
            Instant at, double delta) {
        String id = mmsi + ":" + chokepoint.id() + ":" + (at == null ? now.getEpochSecond() : at.getEpochSecond());
        Map<String, Object> payload = LayerPayload.base(ID, LayerStatus.OK, 0.6, List.of());
        payload.put("chokepoint", chokepoint.name());
        payload.put("mmsi", mmsi);
        payload.put("turnDeg", Math.round(delta));
        ctx.emit(NormalizedRecord.builder(id, UTURN_KIND).observedAt(at == null ? now : at)
                .point(chokepoint.lon(), chokepoint.lat()).putAll(payload).build());
    }

    private void emitActivity(DerivedContext ctx, Instant now, ReferenceData.Chokepoint chokepoint, Hour hour,
            LayerStatus coverage, long epochHour) {
        Map<String, Object> payload = LayerPayload.base(ID, coverage, 0.5, List.of());
        payload.put("chokepoint", chokepoint.name());
        payload.put("hour", Instant.ofEpochSecond(epochHour * 3600).toString());
        payload.put("transits", band(hour.transits.size()));
        payload.put("uTurns", band(hour.uTurns));
        payload.put("stopped", band(hour.stopped));
        ctx.emit(NormalizedRecord.builder(chokepoint.id(), ACTIVITY_KIND).observedAt(now)
                .point(chokepoint.lon(), chokepoint.lat()).putAll(payload).build());
    }

    /** The band lower bound of a count (0,1,3,5,10,20,50,100). */
    static int band(int count) {
        int out = 0;
        for (int b : BANDS) {
            if (count >= b) {
                out = b;
            }
        }
        return out;
    }

    static ReferenceData.Chokepoint containing(List<ReferenceData.Chokepoint> chokepoints, double lon, double lat) {
        for (ReferenceData.Chokepoint chokepoint : chokepoints) {
            if (Geo.haversineKm(lat, lon, chokepoint.lat(), chokepoint.lon()) <= chokepoint.radiusKm()) {
                return chokepoint;
            }
        }
        return null;
    }

    static double wrap(double degrees) {
        double value = degrees % 360;
        if (value > 180) {
            value -= 360;
        }
        if (value < -180) {
            value += 360;
        }
        return value;
    }
}
