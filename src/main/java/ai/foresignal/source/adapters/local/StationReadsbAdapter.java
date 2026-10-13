package ai.foresignal.source.adapters.local;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * One {@code station-readsb-{n}} slot (prompt 069): the health of the operator's readsb receiver from its
 * {@code stats.json}, replacing graphs1090 scraping (rejected in Gap-Analysis §9).
 *
 * <ul>
 *   <li><strong>Params:</strong> {@code url} ({@code LAN_URL}, path ending in {@code stats.json}) and
 *       {@code minMessagesPerSec} (default 1.0), the {@code low-rate} threshold.
 *   <li><strong>Schedule:</strong> 60 s (readsb rewrites {@code stats.json} once a minute), budget 2/min and
 *       2,000/day.
 *   <li><strong>Record:</strong> kind {@code receiver-status}, {@code externalId = sourceId}, from
 *       {@link ReadsbStatsParser}; {@code observedAt = last1min.end}.
 *   <li><strong>Policy:</strong> STANDARD, LATEST {@code {keyframe 20m}} with significant {@code [state]}, TTL
 *       30 min (keyframe 1200 s ≤ 1800 − 60 − 30), retention 365 d ({@code state}). A dead receiver stops
 *       answering and its record expires; one that answers with no messages turns {@code silent}, which a rule
 *       on {@code state} sees.
 * </ul>
 */
public class StationReadsbAdapter implements PollingAdapter {

    /** The id prefix of the slots. */
    public static final String ID_PREFIX = "station-readsb-";

    /** The URL parameter. */
    public static final String URL_PARAM = "url";

    /** The threshold parameter. */
    public static final String MIN_RATE_PARAM = "minMessagesPerSec";

    /** The default {@code low-rate} threshold. */
    public static final double DEFAULT_MIN_RATE = 1.0;

    static final Duration KEYFRAME = Duration.ofMinutes(20);
    static final Duration TTL = Duration.ofMinutes(30);
    static final Duration RETENTION = Duration.ofDays(365);

    private final SourceDescriptor descriptor;

    /**
     * Creates one slot.
     *
     * @param slot the slot number, from 1
     */
    public StationReadsbAdapter(int slot) {
        this.descriptor = buildDescriptor(slot);
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The URL must name {@code stats.json}; the threshold must not be negative. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems =
                LocalReceivers.checkJsonUrl(params, URL_PARAM, LocalReceivers.STATS_JSON, "stats.json");
        Double min = params.number(MIN_RATE_PARAM);
        if (min != null && min < 0) {
            problems.put(MIN_RATE_PARAM, "must not be negative, but was " + min);
        }
        return problems;
    }

    /**
     * Reads {@code stats.json} once.
     *
     * @throws AdapterException when the receiver cannot be reached or the body is not readsb's
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        ValidatedParams params = ctx.params();
        UpstreamResponse response = ctx.http().get(params.lanUrl(URL_PARAM));
        if (response.truncated()) {
            throw new UpstreamFormatException("stats.json is larger than the response cap", "");
        }
        Double min = params.number(MIN_RATE_PARAM);
        return FetchResult.complete(List.of(ReadsbStatsParser.parse(response.body(), ctx.sourceId(),
                min == null ? DEFAULT_MIN_RATE : min)));
    }

    private static SourceDescriptor buildDescriptor(int slot) {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID_PREFIX + slot)
                .name("readsb receiver health " + slot)
                .description("Health of the operator's readsb receiver from its stats.json (messages per second,"
                        + " signal, noise, new tracks), with state ok, low-rate or silent. Every 60 s."
                        + LocalReceivers.LOCAL_NOTE)
                .kind(ReadsbStatsParser.KIND)
                .mode(Mode.POLLING)
                .interval(Duration.ofSeconds(30), Duration.ofSeconds(60))
                .defaultBudget(2, 2_000)
                .fetchTimeout(Duration.ofSeconds(5))
                .maxResponseBytes(1024 * 1024)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(TTL)
                .defaultRetention(RETENTION)
                .defaultIngestPolicy(IngestPolicy.latest(KEYFRAME))
                .significantFields("state")
                .titleField("state")
                .searchFields()
                .param(ParamSpec.lanUrl(URL_PARAM, "The receiver's stats.json on the LAN, for example"
                        + " http://192.168.1.50/tar1090/data/stats.json", true, "http", "https"))
                .param(ParamSpec.optional(MIN_RATE_PARAM, ParamType.NUMBER,
                        "Messages per second below which state is low-rate", DEFAULT_MIN_RATE));
        LocalReceivers.local(builder);
        builder.fieldSpec("messagesPerSec", spec(FieldType.NUMBER, "/s", "Messages per second",
                "Messages accepted per second over the last minute"));
        builder.fieldSpec("signalDbfs", spec(FieldType.NUMBER, "dB", "Mean signal",
                "Mean signal power of received messages, dBFS (always negative)"));
        builder.fieldSpec("noiseDbfs", spec(FieldType.NUMBER, "dB", "Noise", "Noise floor, dBFS, when reported"));
        builder.fieldSpec("peakSignalDbfs", spec(FieldType.NUMBER, "dB", "Peak signal", "Peak signal power, dBFS"));
        builder.fieldSpec("strongSignals", spec(FieldType.INTEGER, "1", "Strong signals",
                "Messages above -3 dBFS in the last minute"));
        builder.fieldSpec("tracksNew", spec(FieldType.INTEGER, "1", "New tracks", "Aircraft tracks created in the last minute"));
        builder.fieldSpec("aircraftWithPos", spec(FieldType.INTEGER, "1", "Aircraft with position",
                "Aircraft with a position, when the receiver reports it"));
        builder.fieldSpec("state", spec(FieldType.STRING, null, "State", "ok, low-rate or silent"));
        builder.kindDisplay(ReadsbStatsParser.KIND, new KindDisplay("Receiver {state}", "antenna", "#2B8A3E", null));
        return builder.build();
    }

    private static FieldSpec spec(FieldType type, String unit, String label, String description) {
        return new FieldSpec(type, unit, label, description, null, Set.of());
    }
}
