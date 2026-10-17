package ai.foresignal.source.adapters.derived;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.derive.LayerStatus;
import ai.foresignal.derive.Observation;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * {@code derived-ais-gap} (prompt 099): a vessel under way, outside port buffers, whose position
 * has not been reported for 30 minutes — but only when the coverage evidence supports it: three
 * other vessels reported by the same source within 50 km during the gap. Otherwise nothing is
 * emitted ("absence of evidence is not evidence of absence").
 *
 * <p>{@code externalId} is {@code {mmsi}:{gapStartEpoch}}. The gap opens with {@code gapStart,
 * lastSog, lastCog, open:true}; when the vessel reappears the same record carries {@code gapEnd,
 * gapMinutes, displacementKm, impliedKn, open:false} and is tombstoned six hours later. A vessel
 * that does not reappear within {@link #FORGET} (12 h) has its gap closed with an unknown end (no
 * {@code gapEnd}, {@code expired: true}, {@code status: insufficient-data}) and tombstoned on the same
 * schedule, so state never grows without bound. A gap not opened because the state cap is reached is
 * counted ({@link #dropped()}).
 */
@Component
public class AisGapGenerator extends LayerGenerator {

    /** The source id. */
    public static final String ID = "derived-ais-gap";

    /** The record kind. */
    public static final String KIND = "ais-gap";

    /** How long without a fix opens a gap. */
    public static final Duration GAP = Duration.ofMinutes(30);

    /** How long a vessel is remembered without a fix before it is forgotten. */
    public static final Duration FORGET = Duration.ofHours(12);

    /** The nearby-vessel radius that proves coverage. */
    public static final double DENSITY_RADIUS_KM = 50;

    /** How many other vessels prove the source was reporting. */
    public static final int DENSITY_MIN = 3;

    /** How long after a gap closes its record is tombstoned. */
    public static final Duration TOMBSTONE_AFTER = Duration.ofHours(6);

    private final SourceDescriptor descriptor = layer(ID, "AIS gaps (derived)",
            "Vessels under way that stopped reporting for 30 minutes while other vessels kept reporting"
                    + " nearby. Automated, not an accusation.",
            "derived (input licences; ODbL for keyless AIS)", KIND).build();

    private final ReferenceData reference;
    /** Open gaps by MMSI. */
    private final Map<String, Gap> gaps = new LinkedHashMap<>();

    /** Closed gaps by record external id, with when they closed; tombstoned {@link #TOMBSTONE_AFTER} later. */
    private final Map<String, Instant> closed = new LinkedHashMap<>();

    private long dropped;

    /** Creates the generator. */
    public AisGapGenerator(ObjectProvider<SourceRegistry> registry, ReferenceData reference) {
        super(registry);
        this.reference = reference;
    }

    /** One vessel's open or recently closed gap. */
    static final class Gap {

        String externalId;
        Instant gapStart;
        double lon;
        double lat;
        Double lastSog;
        Double lastCog;
        boolean open;
        Instant closedAt;
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
        gaps.clear();
        closed.clear();
        reference.refresh();
    }

    @Override
    public int stateSize() {
        return gaps.size() + closed.size();
    }

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        LayerStatus coverage = status(ctx, now.minus(GAP));
        int cap = maxEntities(ctx.params());
        // One vessel past the state cap is read, so a full state sees (and counts) a gap it cannot hold;
        // reading exactly the cap could never present more candidates than the state has room for.
        int readLimit = cap == Integer.MAX_VALUE ? cap : cap + 1;
        List<Observation> observations = ctx.observations()
                .recent(inputSources(), Set.of(DerivedSources.VESSEL), now.minus(FORGET), readLimit);
        List<ReferenceData.Port> ports = reference.ports();
        Set<String> seen = new HashSet<>();
        for (Observation observation : observations) {
            seen.add(observation.externalId());
            update(ctx, now, coverage, ports, observation, cap);
        }
        // An open gap whose vessel never came back is closed once it is past the FORGET horizon: its
        // end is unknown and never guessed, and it is tombstoned on the normal schedule.
        for (Map.Entry<String, Gap> entry : new ArrayList<>(gaps.entrySet())) {
            Gap gap = entry.getValue();
            if (!seen.contains(entry.getKey()) && Duration.between(gap.gapStart, now).compareTo(FORGET) >= 0) {
                gaps.remove(entry.getKey());
                expire(ctx, now, gap);
            }
        }
        for (Map.Entry<String, Instant> entry : new ArrayList<>(closed.entrySet())) {
            if (Duration.between(entry.getValue(), now).compareTo(TOMBSTONE_AFTER) > 0) {
                closed.remove(entry.getKey());
                ctx.tombstone(KIND, entry.getKey());
            }
        }
    }

    private void update(DerivedContext ctx, Instant now, LayerStatus coverage, List<ReferenceData.Port> ports,
            Observation observation, int cap) {
        String mmsi = observation.externalId();
        Double lon = observation.longitude();
        Double lat = observation.latitude();
        Instant fixAt = observation.observedAt();
        if (lon == null || lat == null || fixAt == null) {
            return;
        }
        Gap gap = gaps.get(mmsi);
        if (gap != null) {
            if (fixAt.isAfter(gap.gapStart)) {
                gaps.remove(mmsi);
                close(ctx, gap, lon, lat, fixAt);
            }
            return;
        }
        if (!eligible(observation, ports, lon, lat)) {
            return;
        }
        if (fixAt.isAfter(now.minus(GAP))) {
            return;
        }
        String externalId = mmsi + ":" + fixAt.getEpochSecond();
        if (closed.containsKey(externalId)) {
            // This very gap has already closed or expired; never reopen it.
            return;
        }
        if (coverage != LayerStatus.OK) {
            return;
        }
        if (gaps.size() + closed.size() >= cap) {
            dropped++;
            return;
        }
        int density = ctx.coverage().densityNear(observation.sourceId(), lon, lat, DENSITY_RADIUS_KM, GAP);
        if (density < DENSITY_MIN) {
            return;
        }
        Gap opened = new Gap();
        opened.externalId = externalId;
        opened.gapStart = fixAt;
        opened.lon = lon;
        opened.lat = lat;
        opened.lastSog = LayerSupport.sog(observation);
        opened.lastCog = LayerSupport.cog(observation);
        opened.open = true;
        gaps.put(mmsi, opened);
        Map<String, Object> payload = LayerPayload.base(ID, LayerStatus.OK, 0.7, List.of());
        payload.put("gapStart", fixAt.toString());
        payload.put("lastSog", opened.lastSog);
        payload.put("lastCog", opened.lastCog);
        payload.put("open", true);
        payload.put("nearby", density);
        ctx.emit(NormalizedRecord.builder(opened.externalId, KIND).observedAt(fixAt).point(lon, lat)
                .putAll(payload).build());
    }

    /** The vessel reappeared at {@code fixAt}: the gap closes with its displacement fields. */
    private void close(DerivedContext ctx, Gap gap, double lon, double lat, Instant fixAt) {
        double displacement = ai.foresignal.source.spi.Geo.haversineKm(gap.lat, gap.lon, lat, lon);
        long minutes = Math.max(1, Duration.between(gap.gapStart, fixAt).toMinutes());
        // Knots: kilometers over hours, converted at 1.852 km per nautical mile.
        double implied = displacement / 1.852 / (minutes / 60.0);
        gap.open = false;
        gap.closedAt = fixAt;
        closed.put(gap.externalId, fixAt);
        Map<String, Object> payload = LayerPayload.base(ID, LayerStatus.OK, 0.8, List.of());
        payload.put("gapStart", gap.gapStart.toString());
        payload.put("gapEnd", fixAt.toString());
        payload.put("gapMinutes", LayerPayload.minutes5(minutes));
        payload.put("displacementKm", LayerPayload.km05(displacement));
        payload.put("impliedKn", LayerPayload.ratio1(implied));
        payload.put("open", false);
        ctx.emit(NormalizedRecord.builder(gap.externalId, KIND).observedAt(fixAt).point(lon, lat).putAll(payload).build());
    }

    /** The vessel did not come back within {@link #FORGET}: closed with an unknown end, never a guessed one. */
    private void expire(DerivedContext ctx, Instant now, Gap gap) {
        gap.open = false;
        gap.closedAt = now;
        closed.put(gap.externalId, now);
        Map<String, Object> payload = LayerPayload.base(ID, LayerStatus.INSUFFICIENT_DATA, 0.5, List.of());
        payload.put("gapStart", gap.gapStart.toString());
        payload.put("lastSog", gap.lastSog);
        payload.put("lastCog", gap.lastCog);
        payload.put("open", false);
        payload.put("expired", true);
        ctx.emit(NormalizedRecord.builder(gap.externalId, KIND).observedAt(now).point(gap.lon, gap.lat)
                .putAll(payload).build());
    }

    /** Gaps not opened because the state cap was reached, since start. */
    public long dropped() {
        return dropped;
    }

    /** SOG > 1 kn, not moored or at anchor, and outside every port buffer. */
    static boolean eligible(Observation observation, List<ReferenceData.Port> ports, double lon, double lat) {
        Double sog = LayerSupport.sog(observation);
        if (sog == null || sog <= 1.0 || LayerSupport.mooredOrAnchored(observation)) {
            return false;
        }
        for (ReferenceData.Port port : ports) {
            if (ai.foresignal.source.spi.Geo.haversineKm(lat, lon, port.lat(), port.lon()) <= port.radiusKm()) {
                return false;
            }
        }
        return true;
    }
}
