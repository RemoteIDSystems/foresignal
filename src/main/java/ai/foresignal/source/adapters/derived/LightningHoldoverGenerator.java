package ai.foresignal.source.adapters.derived;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.derive.LayerStatus;
import ai.foresignal.derive.Observation;
import ai.foresignal.geo.H3Cells;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.sample.FrameView;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * {@code derived-lightning-holdover} (prompt 099): GLM flash clusters over dry cells, watched for 72
 * hours for the delayed fire a lightning strike can start. A cell is dry when the recent
 * precipitation is low (MRMS QPE under 2.5 mm, or the model field when MRMS has no coverage — the
 * 24-hour product is the available proxy for the 72-hour total). A FIRMS or GOES FDC detection within
 * 5 km sets {@code ignitionConfirmed} and the delay; otherwise the record expires.
 *
 * <p>{@code externalId} is {@code h3:{res6 cell}:{utcDay}}.
 */
@Component
public class LightningHoldoverGenerator extends LayerGenerator {

    /** The source id. */
    public static final String ID = "derived-lightning-holdover";

    /** The record kind. */
    public static final String KIND = "holdover-fire-risk";

    /** How long a candidate is watched. */
    public static final Duration WATCH = Duration.ofHours(72);

    /** The confirmation radius, km. */
    public static final double CONFIRM_KM = 5;

    /** The dry QPE ceiling, mm. */
    public static final double DRY_MM = 2.5;

    private final SourceDescriptor descriptor = layer(ID, "Lightning holdover fire risk (derived)",
            "GLM flashes over dry cells, watched for a later fire detection. Automated, not a warning.",
            "derived (input licences)", KIND).build();

    private final Map<String, Candidate> candidates = new LinkedHashMap<>();

    /** Creates the generator. */
    public LightningHoldoverGenerator(ObjectProvider<SourceRegistry> registry) {
        super(registry);
    }

    /** One watched cell-day. */
    static final class Candidate {

        Instant createdAt;
        double lon;
        double lat;
        long cell;
        String utcDay;
        int flashes;
        boolean confirmed;
        Double delayHours;
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
        return Set.of(DerivedSources.GOES_GLM, DerivedSources.FIRMS, DerivedSources.GOES_FDC);
    }

    @Override
    protected Set<String> inputKinds() {
        return Set.of("lightning-cluster", "fire-detection");
    }

    @Override
    protected Duration tick() {
        return Duration.ofMinutes(10);
    }

    @Override
    protected void clearState() {
        candidates.clear();
    }

    @Override
    public int stateSize() {
        return candidates.size();
    }

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        int cap = maxEntities(ctx.params());
        List<Observation> clusters = ctx.observations().recent(Set.of(DerivedSources.GOES_GLM),
                Set.of("lightning-cluster"), now.minus(WATCH), cap);
        List<Observation> fires = new ArrayList<>();
        fires.addAll(ctx.observations().recent(Set.of(DerivedSources.FIRMS), Set.of("fire-detection"), now.minus(WATCH), cap));
        fires.addAll(ctx.observations().recent(Set.of(DerivedSources.GOES_FDC), Set.of("fire-detection"), now.minus(WATCH), cap));
        LayerStatus coverage = status(ctx, now.minus(WATCH));
        for (Observation cluster : clusters) {
            Double lon = cluster.longitude();
            Double lat = cluster.latitude();
            if (lon == null || lat == null || cluster.observedAt() == null) {
                continue;
            }
            long cell = H3Cells.latLngToCell(lat, lon, 6);
            String day = cluster.observedAt().atZone(ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS).toLocalDate().toString();
            String id = id(cell, day);
            if (candidates.size() >= cap && !candidates.containsKey(id)) {
                continue;
            }
            Candidate candidate = candidates.computeIfAbsent(id, k -> {
                Candidate fresh = new Candidate();
                fresh.createdAt = cluster.observedAt();
                fresh.lon = lon;
                fresh.lat = lat;
                fresh.cell = cell;
                fresh.utcDay = day;
                return fresh;
            });
            candidate.flashes += (int) Math.round(cluster.number("flashes5m") == null ? 0 : cluster.number("flashes5m"));
            if (!dry(ctx, lon, lat)) {
                continue;
            }
            for (Observation fire : fires) {
                if (fire.observedAt() == null || fire.observedAt().isBefore(candidate.createdAt)) {
                    continue;
                }
                Double fLon = fire.longitude();
                Double fLat = fire.latitude();
                if (fLon == null || fLat == null) {
                    continue;
                }
                if (Geo.haversineKm(lat, lon, fLat, fLon) <= CONFIRM_KM) {
                    candidate.confirmed = true;
                    candidate.delayHours = Duration.between(candidate.createdAt, fire.observedAt()).toMinutes() / 60.0;
                }
            }
            emit(ctx, now, coverage, id, candidate);
        }
        for (Map.Entry<String, Candidate> entry : new ArrayList<>(candidates.entrySet())) {
            if (Duration.between(entry.getValue().createdAt, now).compareTo(WATCH) > 0) {
                Candidate candidate = entry.getValue();
                candidates.remove(entry.getKey());
                if (!candidate.confirmed) {
                    ctx.tombstone(KIND, entry.getKey());
                }
            }
        }
    }

    private void emit(DerivedContext ctx, Instant now, LayerStatus coverage, String id, Candidate candidate) {
        Map<String, Object> payload = LayerPayload.base(ID, coverage, 0.5, List.of());
        payload.put("cell", H3Cells.cellToAddress(candidate.cell));
        payload.put("utcDay", candidate.utcDay);
        payload.put("flashes", candidate.flashes);
        payload.put("ignitionConfirmed", candidate.confirmed);
        if (candidate.delayHours != null) {
            payload.put("delayHours", LayerPayload.ratio1(candidate.delayHours));
        }
        payload.put("severity", candidate.flashes >= 50 ? "high" : candidate.flashes >= 10 ? "medium" : "low");
        ctx.emit(NormalizedRecord.builder(id, KIND).observedAt(now)
                .geometry(H3Cells.cellToPolygon(candidate.cell)).putAll(payload).build());
    }

    /** The res-6 cell of a position. */
    static long cell(double lon, double lat) {
        return H3Cells.latLngToCell(lat, lon, 6);
    }

    /** The candidate id: {@code h3:{cell}:{utcDay}}. */
    static String id(long cell, String utcDay) {
        return "h3:" + H3Cells.cellToAddress(cell) + ":" + utcDay;
    }

    /** True when the cell is dry (QPE under the ceiling); unknown coverage is not dry. */
    private boolean dry(DerivedContext ctx, double lon, double lat) {        try {
            Optional<RasterFrameRow> frame = ctx.rasters().newestFrame("mrms-qpe-24h");
            if (frame.isEmpty()) {
                return false;
            }
            Optional<FrameView> view = ctx.rasters().view(frame.get());
            if (view.isEmpty()) {
                return false;
            }
            FrameView.Sample sample = view.get().sample(lon, lat);
            return Double.isFinite(sample.value()) && sample.value() < DRY_MM;
        } catch (UnsupportedOperationException e) {
            return false;
        }
    }
}
