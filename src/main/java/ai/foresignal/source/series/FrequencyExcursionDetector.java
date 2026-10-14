package ai.foresignal.source.series;

import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * The pure grid-frequency excursion detector shared by {@code grid-frequency-gb} and {@code grid-frequency-fingrid}
 * (prompt 093).
 *
 * <p>Given a nominal frequency (50 Hz), an operational band (GB 0.2 Hz, Nordic 0.1 Hz) and a statutory band (GB
 * 0.5 Hz), it turns a run of samples into:
 *
 * <ul>
 *   <li>a {@code frequency-excursion} record per run that leaves the operational band, its {@code externalId}
 *       {@code {grid}:{startEpoch}}, opened when the run starts and updated (a new version) when it closes;
 *   <li>one {@code grid-frequency} state record per poll, {@code {grid}:state}, carrying a banded
 *       {@code state} ({@code normal|low|high|no-data}) so a rule can watch the single entity.
 * </ul>
 *
 * <p>It keeps the last processed sample time so a restart does not double-count samples it already saw, and it
 * reports {@code no-data} rather than {@code normal} when the newest sample is older than {@link #STALE_AFTER}.
 */
public final class FrequencyExcursionDetector {

    /** The excursion record kind. */
    public static final String EXCURSION_KIND = "frequency-excursion";

    /** The one-per-grid state record kind. */
    public static final String STATE_KIND = "grid-frequency";

    /** Nominal frequency of a 50 Hz system. */
    public static final double NOMINAL_HZ = 50.0;

    /** Below this the excursion is {@code severe} (prompt 093). */
    public static final double SEVERE_LOW_HZ = 49.2;

    /** A newest sample older than this makes the grid read {@code no-data}. */
    public static final Duration STALE_AFTER = Duration.ofMinutes(10);

    /** The adapter-state key prefix of a grid's persisted detector state. */
    static final String STATE_KEY_PREFIX = "freq:";

    private FrequencyExcursionDetector() {}

    /** The detector's fixed settings for one grid. */
    public record Config(String grid, double operationalBandHz, double statutoryBandHz) {}

    /** One frequency sample: the measurement time and the reading in Hz. */
    public record Sample(Instant time, double hz) {}

    /**
     * The detector's persisted state.
     *
     * @param lastSampleTime the newest sample already processed, so a restart skips it
     * @param lastHz that sample's reading
     * @param open true while a run is outside the operational band
     * @param start when the open run started
     * @param extreme the most extreme reading of the open run
     * @param direction {@code low} or {@code high}
     */
    public record State(
            Instant lastSampleTime, Double lastHz, boolean open, Instant start, Double extreme, String direction) {

        /** No samples processed yet. */
        public static State empty() {
            return new State(null, null, false, null, null, null);
        }
    }

    /** The records to ingest and the state to persist. */
    public record Result(List<NormalizedRecord> records, State state) {}

    /**
     * Processes {@code samples} and returns the records to emit plus the new state.
     *
     * @param config the grid's bands
     * @param prior the state from the previous poll, or null
     * @param samples samples in any order; those at or before {@code prior.lastSampleTime} are skipped
     * @param now the poll's clock
     */
    public static Result process(Config config, State prior, List<Sample> samples, Instant now) {
        State state = prior == null ? State.empty() : prior;
        List<NormalizedRecord> records = new ArrayList<>();
        List<Sample> ordered = new ArrayList<>(samples == null ? List.of() : samples);
        ordered.sort(java.util.Comparator.comparing(Sample::time));
        for (Sample sample : ordered) {
            if (state.lastSampleTime() != null && !sample.time().isAfter(state.lastSampleTime())) {
                continue;
            }
            double deviation = sample.hz() - NOMINAL_HZ;
            boolean outOfBand = Math.abs(deviation) > config.operationalBandHz();
            String direction = deviation < 0 ? "low" : "high";
            if (outOfBand) {
                if (!state.open()) {
                    state = new State(sample.time(), sample.hz(), true, sample.time(), sample.hz(), direction);
                    records.add(excursion(config, state, null));
                } else if (!direction.equals(state.direction())) {
                    records.add(excursion(config, state, sample.time()));
                    state = new State(sample.time(), sample.hz(), true, sample.time(), sample.hz(), direction);
                    records.add(excursion(config, state, null));
                } else {
                    double extreme = "low".equals(direction)
                            ? Math.min(state.extreme(), sample.hz())
                            : Math.max(state.extreme(), sample.hz());
                    state = new State(sample.time(), sample.hz(), true, state.start(), extreme, state.direction());
                }
            } else {
                if (state.open()) {
                    records.add(excursion(config, state, sample.time()));
                }
                state = new State(sample.time(), sample.hz(), false, null, null, null);
            }
        }
        records.add(stateRecord(config, state, now));
        return new Result(records, state);
    }

    private static NormalizedRecord excursion(Config config, State state, Instant end) {
        double extreme = state.extreme();
        String severity = severity(config, state.direction(), extreme);
        String id = config.grid() + ":" + state.start().getEpochSecond();
        return NormalizedRecord.builder(id, EXCURSION_KIND)
                .observedAt(end != null ? end : state.lastSampleTime())
                .eventTime(state.start())
                .put("grid", config.grid())
                .put("start", state.start().toString())
                .put("end", end == null ? null : end.toString())
                .put("durationS", end == null ? null : Duration.between(state.start(), end).toSeconds())
                .put("extremeHz", extreme)
                .put("direction", state.direction())
                .put("severity", severity)
                .put("open", end == null)
                .build();
    }

    private static String severity(Config config, String direction, double extreme) {
        if ("low".equals(direction) && extreme < SEVERE_LOW_HZ) {
            return "severe";
        }
        return Math.abs(extreme - NOMINAL_HZ) >= config.statutoryBandHz() ? "statutory" : "operational";
    }

    private static NormalizedRecord stateRecord(Config config, State state, Instant now) {
        boolean stale = state.lastSampleTime() == null
                || state.lastSampleTime().plus(STALE_AFTER).isBefore(now);
        String value;
        if (stale) {
            value = "no-data";
        } else if (state.lastHz() != null && state.lastHz() - NOMINAL_HZ > config.operationalBandHz()) {
            value = "high";
        } else if (state.lastHz() != null && NOMINAL_HZ - state.lastHz() > config.operationalBandHz()) {
            value = "low";
        } else {
            value = "normal";
        }
        return NormalizedRecord.builder(config.grid() + ":state", STATE_KIND)
                .observedAt(state.lastSampleTime() != null ? state.lastSampleTime() : now)
                .put("grid", config.grid())
                .put("state", value)
                .put("nominalHz", NOMINAL_HZ)
                .put("operationalBandHz", config.operationalBandHz())
                .put("statutoryBandHz", config.statutoryBandHz())
                .put("latestHz", state.lastHz())
                .put("latestAt", state.lastSampleTime() == null ? null : state.lastSampleTime().toString())
                .build();
    }

    // ---------------------------------------------------------------- persistence

    /** Reads a grid's persisted detector state, or an empty one. */
    public static State read(AdapterStateStore store, String grid) {
        return store.get(STATE_KEY_PREFIX + grid).map(FrequencyExcursionDetector::decode).orElseGet(State::empty);
    }

    /** Persists a grid's detector state. */
    public static void write(AdapterStateStore store, String grid, State state) {
        store.put(STATE_KEY_PREFIX + grid, encode(state));
    }

    static String encode(State state) {
        return String.join(
                "|",
                nullToEmpty(state.lastSampleTime() == null ? null : state.lastSampleTime().toString()),
                nullToEmpty(state.lastHz()),
                Boolean.toString(state.open()),
                nullToEmpty(state.start() == null ? null : state.start().toString()),
                nullToEmpty(state.extreme()),
                nullToEmpty(state.direction()));
    }

    static State decode(String text) {
        String[] parts = text.split("\\|", -1);
        if (parts.length < 6) {
            return State.empty();
        }
        return new State(
                parseInstant(parts[0]),
                parseDouble(parts[1]),
                Boolean.parseBoolean(parts[2]),
                parseInstant(parts[3]),
                parseDouble(parts[4]),
                parts[5].isEmpty() ? null : parts[5]);
    }

    private static String nullToEmpty(Object value) {
        return value == null ? "" : value.toString();
    }

    private static Instant parseInstant(String text) {
        return text == null || text.isEmpty() ? null : Instant.parse(text);
    }

    private static Double parseDouble(String text) {
        return text == null || text.isEmpty() ? null : Double.parseDouble(text);
    }

    /** Declares the {@code fieldSpecs} of both grid-frequency kinds' payload keys. */
    public static void declareFieldSpecs(ai.foresignal.source.spi.SourceDescriptor.Builder builder) {
        builder.fieldSpec("grid", new ai.foresignal.source.spi.FieldSpec(
                ai.foresignal.source.spi.FieldType.STRING, null, "Grid", "Grid identifier, for example gb or fi",
                null, java.util.Set.of()));
        builder.fieldSpec("start", new ai.foresignal.source.spi.FieldSpec(
                ai.foresignal.source.spi.FieldType.TIMESTAMP, null, "Start", "When the excursion started", null,
                java.util.Set.of()));
        builder.fieldSpec("end", new ai.foresignal.source.spi.FieldSpec(
                ai.foresignal.source.spi.FieldType.TIMESTAMP, null, "End", "When the excursion closed; absent while open",
                null, java.util.Set.of()));
        builder.fieldSpec("durationS", new ai.foresignal.source.spi.FieldSpec(
                ai.foresignal.source.spi.FieldType.NUMBER, "s", "Duration", "How long the excursion lasted",
                null, java.util.Set.of()));
        builder.fieldSpec("extremeHz", new ai.foresignal.source.spi.FieldSpec(
                ai.foresignal.source.spi.FieldType.NUMBER, "Hz", "Extreme", "The most extreme reading of the excursion",
                null, java.util.Set.of()));
        builder.fieldSpec("direction", new ai.foresignal.source.spi.FieldSpec(
                ai.foresignal.source.spi.FieldType.STRING, null, "Direction", "low or high", null,
                java.util.Set.of()));
        builder.fieldSpec("severity", new ai.foresignal.source.spi.FieldSpec(
                ai.foresignal.source.spi.FieldType.STRING, null, "Severity", "operational, statutory or severe", null,
                java.util.Set.of()));
        builder.fieldSpec("open", new ai.foresignal.source.spi.FieldSpec(
                ai.foresignal.source.spi.FieldType.BOOLEAN, null, "Open", "True while the excursion is still open",
                null, java.util.Set.of()));
        builder.fieldSpec("state", new ai.foresignal.source.spi.FieldSpec(
                ai.foresignal.source.spi.FieldType.STRING, null, "State", "normal, low, high or no-data", null,
                java.util.Set.of()));
        builder.fieldSpec("nominalHz", new ai.foresignal.source.spi.FieldSpec(
                ai.foresignal.source.spi.FieldType.NUMBER, "Hz", "Nominal", "The system's nominal frequency",
                null, java.util.Set.of()));
        builder.fieldSpec("operationalBandHz", new ai.foresignal.source.spi.FieldSpec(
                ai.foresignal.source.spi.FieldType.NUMBER, "Hz", "Operational band",
                "The band outside which an excursion starts", null, java.util.Set.of()));
        builder.fieldSpec("statutoryBandHz", new ai.foresignal.source.spi.FieldSpec(
                ai.foresignal.source.spi.FieldType.NUMBER, "Hz", "Statutory band",
                "The band outside which an excursion is statutory", null, java.util.Set.of()));
        builder.fieldSpec("latestHz", new ai.foresignal.source.spi.FieldSpec(
                ai.foresignal.source.spi.FieldType.NUMBER, "Hz", "Latest", "The newest sample's reading", null,
                java.util.Set.of()));
        builder.fieldSpec("latestAt", new ai.foresignal.source.spi.FieldSpec(
                ai.foresignal.source.spi.FieldType.TIMESTAMP, null, "Latest at", "The newest sample's time", null,
                java.util.Set.of()));
    }
}
