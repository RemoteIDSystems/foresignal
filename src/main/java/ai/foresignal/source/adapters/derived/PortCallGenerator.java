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
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * {@code derived-port-calls} (prompt 099): a vessel inside a World Port Index buffer (radius by
 * harbor size: very small 2 km, small 3 km, medium 6 km, large 10 km) below one knot for at least
 * 30 minutes is an arrival; leaving the buffer is a departure. Without fixes for the whole stay the
 * departure duration is emitted as unknown, never guessed. {@code externalId} is
 * {@code {mmsi}:{portNumber}:{arrivalEpoch}}, tombstoned 24 hours after departure.
 */
@Component
public class PortCallGenerator extends LayerGenerator {

    /** The source id. */
    public static final String ID = "derived-port-calls";

    /** The record kind. */
    public static final String KIND = "port-call";

    /** The slow speed that starts a call. */
    public static final double SLOW_KN = 1.0;

    /** The shortest slow stay. */
    public static final Duration MIN_STAY = Duration.ofMinutes(30);

    /** How long after departure the record is tombstoned. */
    public static final Duration TOMBSTONE_AFTER = Duration.ofHours(24);

    private final SourceDescriptor descriptor = layer(ID, "Port calls (derived)",
            "Arrivals and departures at World Port Index ports, from a vessel's slow stay inside a port"
                    + " buffer. Automated, not a warning.",
            "derived (input licences; ODbL for keyless AIS)", KIND).build();

    private final ReferenceData reference;
    private final Map<String, Call> calls = new LinkedHashMap<>();

    /** Creates the generator. */
    public PortCallGenerator(ObjectProvider<SourceRegistry> registry, ReferenceData reference) {
        super(registry);
        this.reference = reference;
    }

    /** One vessel's call state. */
    static final class Call {

        String externalId;
        String portNumber;
        String portName;
        Instant arrivalAt;
        boolean arrivalEmitted;
        boolean hadFixes;
        Instant departedAt;
        Instant lastFixAt;
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
        calls.clear();
        reference.refresh();
    }

    @Override
    public int stateSize() {
        return calls.size();
    }

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        int cap = maxEntities(ctx.params());
        List<Observation> observations =
                ctx.observations().recent(inputSources(), Set.of(DerivedSources.VESSEL), now.minus(Duration.ofHours(6)), cap);
        List<ReferenceData.Port> ports = reference.ports();
        LayerStatus coverage = status(ctx, now.minus(MIN_STAY));
        Set<String> present = new java.util.HashSet<>();
        for (Observation observation : observations) {
            Double lon = observation.longitude();
            Double lat = observation.latitude();
            if (lon == null || lat == null || observation.observedAt() == null) {
                continue;
            }
            String mmsi = observation.externalId();
            present.add(mmsi);
            ReferenceData.Port port = containing(ports, lon, lat);
            Double sog = LayerSupport.sog(observation);
            Call call = calls.get(mmsi);
            if (call != null && call.departedAt == null && port == null) {
                depart(ctx, mmsi, call, now, observation.observedAt());
                continue;
            }
            if (port == null || sog == null || sog >= SLOW_KN) {
                continue;
            }
            if (call == null || call.departedAt != null) {
                if (calls.size() >= cap) {
                    continue;
                }
                call = new Call();
                call.portNumber = port.portNumber();
                call.portName = port.name();
                call.arrivalAt = observation.observedAt();
                call.externalId = mmsi + ":" + port.portNumber() + ":" + call.arrivalAt.getEpochSecond();
                calls.put(mmsi, call);
            }
            call.lastFixAt = observation.observedAt();
            call.hadFixes = true;
            if (!call.arrivalEmitted && Duration.between(call.arrivalAt, observation.observedAt()).compareTo(MIN_STAY) >= 0
                    && coverage == LayerStatus.OK) {
                call.arrivalEmitted = true;
                Map<String, Object> payload = LayerPayload.base(ID, LayerStatus.OK, 0.7, List.of());
                payload.put("portNumber", call.portNumber);
                payload.put("portName", call.portName);
                payload.put("arrival", call.arrivalAt.toString());
                payload.put("open", true);
                ctx.emit(NormalizedRecord.builder(call.externalId, KIND).observedAt(observation.observedAt())
                        .point(lon, lat).putAll(payload).build());
            }
        }
        for (Map.Entry<String, Call> entry : new ArrayList<>(calls.entrySet())) {
            Call call = entry.getValue();
            if (call.departedAt != null
                    && Duration.between(call.departedAt, now).compareTo(TOMBSTONE_AFTER) > 0) {
                calls.remove(entry.getKey());
                ctx.tombstone(KIND, call.externalId);
            }
        }
    }

    private void depart(DerivedContext ctx, String mmsi, Call call, Instant now, Instant lastFix) {
        call.departedAt = now;
        Map<String, Object> payload = LayerPayload.base(ID, LayerStatus.OK, 0.7, List.of());
        payload.put("portNumber", call.portNumber);
        payload.put("portName", call.portName);
        payload.put("arrival", call.arrivalAt.toString());
        payload.put("departure", now.toString());
        payload.put("open", false);
        if (call.hadFixes && call.lastFixAt != null) {
            payload.put("durationH", LayerPayload.ratio1(Duration.between(call.arrivalAt, now).toMinutes() / 60.0));
        } else {
            payload.put("durationH", null);
            payload.put("durationUnknown", true);
        }
        ctx.emit(NormalizedRecord.builder(call.externalId, KIND).observedAt(now).putAll(payload).build());
    }

    /** The port whose buffer contains the point, or null. */
    static ReferenceData.Port containing(List<ReferenceData.Port> ports, double lon, double lat) {
        ReferenceData.Port best = null;
        double bestRadius = Double.MAX_VALUE;
        for (ReferenceData.Port port : ports) {
            double distance = Geo.haversineKm(lat, lon, port.lat(), port.lon());
            if (distance <= port.radiusKm() && port.radiusKm() < bestRadius) {
                best = port;
                bestRadius = port.radiusKm();
            }
        }
        return best;
    }
}
