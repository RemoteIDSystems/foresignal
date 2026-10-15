package ai.foresignal.source.adapters.derived;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.derive.LayerStatus;
import ai.foresignal.derive.Observation;
import ai.foresignal.geo.H3Cells;
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
 * {@code derived-mil-naval} (prompt 099): military aircraft and naval vessels co-occurring within 30
 * minutes in the same H3 res-3 cell (or within 100 km) — the classic exercise/patrol signature.
 * Military aircraft are the {@code adsblol-mil}/{@code airplaneslive-mil} sources or any aircraft
 * with the readsb {@code mil} flag; naval vessels are AIS ship type 35. {@code externalId} is
 * {@code h3:{res3 cell}}, tombstoned after 30 minutes without co-occurrence.
 */
@Component
public class MilNavalGenerator extends LayerGenerator {

    /** The source id. */
    public static final String ID = "derived-mil-naval";

    /** The record kind. */
    public static final String KIND = "mil-naval-cooccurrence";

    /** The co-occurrence window. */
    public static final Duration WINDOW = Duration.ofMinutes(30);

    /** The far limit, km, beyond the same cell. */
    public static final double FAR_KM = 100;

    private final SourceDescriptor descriptor = layer(ID, "Military and naval co-occurrence (derived)",
            "Military aircraft and naval vessels within 30 minutes in the same res-3 cell. Automated, not a"
                    + " warning.",
            "derived (input licences)", KIND).build();

    private final Map<String, Instant> lastSeen = new LinkedHashMap<>();

    /** Creates the generator. */
    public MilNavalGenerator(ObjectProvider<SourceRegistry> registry) {
        super(registry);
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    protected Set<String> inputPrefixes() {
        Set<String> all = new java.util.TreeSet<>(DerivedSources.AIRCRAFT_PREFIXES);
        all.addAll(DerivedSources.VESSEL_PREFIXES);
        return all;
    }

    @Override
    protected Set<String> inputKinds() {
        return Set.of(DerivedSources.AIRCRAFT, DerivedSources.VESSEL);
    }

    @Override
    protected Duration tick() {
        return Duration.ofMinutes(5);
    }

    @Override
    protected void clearState() {
        lastSeen.clear();
    }

    @Override
    public int stateSize() {
        return lastSeen.size();
    }

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        int cap = maxEntities(ctx.params());
        List<Observation> observations =
                ctx.observations().recent(inputSources(), inputKinds(), now.minus(WINDOW), cap);
        List<Observation> aircraft = new ArrayList<>();
        List<Observation> vessels = new ArrayList<>();
        for (Observation observation : observations) {
            if (DerivedSources.AIRCRAFT.equals(observation.kind()) && military(observation)) {
                aircraft.add(observation);
            } else if (DerivedSources.VESSEL.equals(observation.kind()) && naval(observation)) {
                vessels.add(observation);
            }
        }
        LayerStatus coverage = status(ctx, now.minus(WINDOW));
        Map<String, Cell> cells = new LinkedHashMap<>();
        for (Observation plane : aircraft) {
            Double lon = plane.longitude();
            Double lat = plane.latitude();
            if (lon == null || lat == null) {
                continue;
            }
            String cell = H3Cells.cellToAddress(H3Cells.latLngToCell(lat, lon, 3));
            Cell aggregate = cells.computeIfAbsent(cell, k -> new Cell());
            if (aggregate.aircraft.size() < 20) {
                aggregate.aircraft.add(plane);
            }
            for (Observation ship : vessels) {
                Double sLon = ship.longitude();
                Double sLat = ship.latitude();
                if (sLon == null || sLat == null) {
                    continue;
                }
                String shipCell = H3Cells.cellToAddress(H3Cells.latLngToCell(sLat, sLon, 3));
                if (cell.equals(shipCell) || Geo.haversineKm(lat, lon, sLat, sLon) <= FAR_KM) {
                    if (aggregate.vessels.size() < 20) {
                        aggregate.vessels.add(ship);
                    }
                }
            }
        }
        for (Map.Entry<String, Cell> entry : cells.entrySet()) {
            Cell cell = entry.getValue();
            if (cell.aircraft.isEmpty() || cell.vessels.isEmpty()) {
                continue;
            }
            lastSeen.put(entry.getKey(), now);
            emit(ctx, now, coverage, entry.getKey(), cell);
        }
        for (Map.Entry<String, Instant> entry : new ArrayList<>(lastSeen.entrySet())) {
            if (Duration.between(entry.getValue(), now).compareTo(WINDOW) > 0) {
                lastSeen.remove(entry.getKey());
                ctx.tombstone(KIND, entry.getKey());
            }
        }
    }

    private void emit(DerivedContext ctx, Instant now, LayerStatus coverage, String cell, Cell aggregate) {
        Map<String, Object> payload = LayerPayload.base(ID, coverage, 0.6, List.of());
        payload.put("aircraftCount", aggregate.aircraft.size());
        payload.put("vesselCount", aggregate.vessels.size());
        payload.put("aircraft", aggregate.aircraft.stream().map(Observation::externalId).toList());
        payload.put("vessels", aggregate.vessels.stream().map(Observation::externalId).toList());
        ctx.emit(NormalizedRecord.builder(cell, KIND).observedAt(now).putAll(payload).build());
    }

    /** True when the aircraft is from a military source or carries the readsb {@code mil} flag. */
    static boolean military(Observation observation) {
        if (DerivedSources.MIL_AIRCRAFT_SOURCES.contains(observation.sourceId())) {
            return true;
        }
        Object mil = observation.payload().get("mil");
        return mil instanceof Boolean b && b;
    }

    /** True when the vessel is naval (AIS ship type 35). */
    static boolean naval(Observation observation) {
        Integer type = LayerSupport.shipType(observation);
        return type != null && type == DerivedSources.NAVAL_SHIP_TYPE;
    }

    /** One cell's co-occurrence. */
    static final class Cell {

        final List<Observation> aircraft = new ArrayList<>();
        final List<Observation> vessels = new ArrayList<>();
    }
}
