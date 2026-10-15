package ai.foresignal.source.adapters.derived;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.derive.LayerStatus;
import ai.foresignal.derive.Observation;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * {@code derived-hf-blackout} (prompt 099): a candidate HF blackout from PSKReporter FT8 spots. Per
 * amateur band and propagation path class (a continent pair) it compares the current hour's spot
 * count and median SNR against the same hour over the previous 14 days; a drop of at least 60% in
 * spots or at least 10 dB in SNR on sunlit paths, while the source is healthy, is marked. The
 * concurrent SWPC X-ray class is carried as {@code flareConcurrent} so the layer can be checked as an
 * early indicator of flare-driven absorption.
 *
 * <p>{@code externalId} is {@code {band}:{pathClass}:{utcHour}}.
 */
@Component
public class HfBlackoutGenerator extends LayerGenerator {

    /** The source id. */
    public static final String ID = "derived-hf-blackout";

    /** The record kind. */
    public static final String KIND = "hf-propagation-anomaly";

    /** The spot-count drop that marks a blackout. */
    public static final double SPOT_DROP = 0.4;

    /** The SNR drop that marks a blackout. */
    public static final double SNR_DROP = 10;

    /** The sunlit UTC hours (inclusive). */
    public static final int SUNLIT_FROM = 6;

    /** The sunlit UTC hours (inclusive). */
    public static final int SUNLIT_TO = 18;

    /** How many days of history are kept per key. */
    public static final int BASELINE_DAYS = 14;

    private final SourceDescriptor descriptor = layer(ID, "HF propagation anomaly (derived)",
            "PSKReporter spot and SNR drops on sunlit paths against a 14-day baseline, with the concurrent"
                    + " X-ray class. Automated, not a warning.",
            "derived (input licences)", KIND).build();

    private final Map<String, Deque<Hour>> history = new LinkedHashMap<>();

    /** Creates the generator. */
    public HfBlackoutGenerator(ObjectProvider<SourceRegistry> registry) {
        super(registry);
    }

    /** One hour's statistics for a key. */
    record Hour(int hourOfDay, int count, double snr) {}

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
        return Set.of(DerivedSources.PSKREPORTER, DerivedSources.SWPC_XRAY);
    }

    @Override
    protected Set<String> inputKinds() {
        return Set.of("radio-spot", "solar-flare", "xray-current");
    }

    @Override
    protected Duration tick() {
        return Duration.ofMinutes(10);
    }

    @Override
    protected void clearState() {
        history.clear();
    }

    @Override
    public int stateSize() {
        return history.size();
    }

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        int cap = maxEntities(ctx.params());
        List<Observation> spots =
                ctx.observations().recent(Set.of(DerivedSources.PSKREPORTER), Set.of("radio-spot"), now.minus(Duration.ofHours(1)), cap);
        if (spots.isEmpty()) {
            return;
        }
        Map<String, List<Double>> byKey = new LinkedHashMap<>();
        Map<String, Integer> counts = new LinkedHashMap<>();
        Map<String, String> bands = new LinkedHashMap<>();
        for (Observation spot : spots) {
            String band = spot.text("band");
            if (band == null) {
                continue;
            }
            String path = pathClass(spot);
            String key = band + ":" + path;
            counts.merge(key, 1, Integer::sum);
            Double snr = spot.number("snr");
            if (snr != null) {
                byKey.computeIfAbsent(key, k -> new ArrayList<>()).add(snr);
            }
            bands.put(key, band);
        }
        int hourOfDay = now.atZone(ZoneOffset.UTC).getHour();
        boolean sunlit = hourOfDay >= SUNLIT_FROM && hourOfDay <= SUNLIT_TO;
        boolean healthy = ctx.coverage().healthy(DerivedSources.PSKREPORTER);
        String flare = flareClass(ctx, now);
        LayerStatus coverage = status(ctx, now.minus(Duration.ofHours(1)));
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            String key = entry.getKey();
            int count = entry.getValue();
            double snr = median(byKey.getOrDefault(key, List.of()));
            Deque<Hour> past = history.computeIfAbsent(key, k -> new ArrayDeque<>());
            double baselineCount = 0;
            double baselineSnr = Double.NaN;
            int samples = 0;
            double snrSum = 0;
            for (Hour hour : past) {
                if (hour.hourOfDay() == hourOfDay) {
                    baselineCount += hour.count();
                    if (Double.isFinite(hour.snr())) {
                        snrSum += hour.snr();
                        samples++;
                    }
                }
            }
            double meanCount = 0;
            int days = 0;
            for (Hour hour : past) {
                if (hour.hourOfDay() == hourOfDay) {
                    days++;
                }
            }
            meanCount = days == 0 ? 0 : baselineCount / days;
            if (samples > 0) {
                baselineSnr = snrSum / samples;
            }
            boolean spotDrop = meanCount >= 3 && count <= meanCount * SPOT_DROP;
            boolean snrDrop = Double.isFinite(baselineSnr) && Double.isFinite(snr) && snr <= baselineSnr - SNR_DROP;
            if (sunlit && healthy && (spotDrop || snrDrop) && coverage == LayerStatus.OK) {
                emit(ctx, now, key, bands.get(key), hourOfDay, count, snr, meanCount, baselineSnr, flare, spotDrop, snrDrop);
            }
            past.addLast(new Hour(hourOfDay, count, snr));
            while (past.size() > BASELINE_DAYS) {
                past.removeFirst();
            }
        }
    }

    private void emit(DerivedContext ctx, Instant now, String key, String band, int hourOfDay, int count, double snr,
            double baselineCount, double baselineSnr, String flare, boolean spotDrop, boolean snrDrop) {
        String id = band + ":" + key.substring(key.indexOf(':') + 1) + ":" + hourOfDay;
        Map<String, Object> payload = LayerPayload.base(ID, LayerStatus.OK, 0.6, List.of());
        payload.put("band", band);
        payload.put("utcHour", hourOfDay);
        payload.put("spots", count);
        payload.put("baselineSpots", Math.round(baselineCount));
        if (Double.isFinite(snr)) {
            payload.put("medianSnr", LayerPayload.ratio1(snr));
        }
        if (Double.isFinite(baselineSnr)) {
            payload.put("baselineSnr", LayerPayload.ratio1(baselineSnr));
        }
        payload.put("spotDrop", spotDrop);
        payload.put("snrDrop", snrDrop);
        payload.put("sunlit", true);
        if (flare != null) {
            payload.put("flareConcurrent", flare);
        }
        ctx.emit(NormalizedRecord.builder(id, KIND).observedAt(now).putAll(payload).build());
    }

    private String flareClass(DerivedContext ctx, Instant now) {
        String best = null;
        for (Observation observation : ctx.observations().recent(Set.of(DerivedSources.SWPC_XRAY),
                Set.of("xray-current", "solar-flare"), now.minus(Duration.ofHours(6)), 100)) {
            String value = observation.text("currentClass");
            if (value == null) {
                value = observation.text("maxClass");
            }
            if (value != null && (best == null || value.compareTo(best) > 0)) {
                best = value;
            }
        }
        return best;
    }

    /** The path class: the sender's Maidenhead field and the receiver DXCC, or {@code **}. */
    static String pathClass(Observation spot) {
        String locator = spot.text("senderLocator");
        String field = locator != null && locator.length() >= 2 ? locator.substring(0, 2).toUpperCase(java.util.Locale.ROOT) : "**";
        String dxcc = spot.text("dxcc");
        return field + ":" + (dxcc == null ? "**" : dxcc);
    }

    static double median(List<Double> values) {
        if (values.isEmpty()) {
            return Double.NaN;
        }
        double[] sorted = new double[values.size()];
        for (int i = 0; i < sorted.length; i++) {
            sorted[i] = values.get(i);
        }
        Arrays.sort(sorted);
        int middle = sorted.length / 2;
        return sorted.length % 2 == 0 ? (sorted[middle - 1] + sorted[middle]) / 2 : sorted[middle];
    }
}
