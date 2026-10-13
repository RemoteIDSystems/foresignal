package ai.foresignal.source.adapters.space.swpc;

import ai.foresignal.source.adapters.space.SpaceSources;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code swpc-alerts} (prompt 077): SWPC space weather messages as {@code space-weather-alert} records
 * ({@link SwpcMessageParser}) and the current NOAA scales as the single {@code space-weather-scales} entity
 * {@code current} ({@link SwpcProductsParser#scales}).
 *
 * <p><strong>Each run:</strong> a conditional GET of {@value #ALERTS_URL} (about 53 KB) and of {@value #SCALES_URL}
 * (about 1 KB); both send {@code ETag}, {@code Last-Modified} and {@code Cache-Control: max-age=60}. A file that
 * answers 304 contributes nothing; both 304 is {@link FetchResult#unchanged()}. A file that does not parse forgets
 * its validators and fails the run. The message list is a rolling window, so records expire by their 7-day TTL
 * (on receipt) or their upstream {@code Valid To}, and a cancel message tombstones the message it cancels.
 */
@Component
public class SwpcAlertsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "swpc-alerts";

    /** The messages file. */
    public static final String ALERTS_URL = "https://services.swpc.noaa.gov/products/alerts.json";

    /** The scales file. */
    public static final String SCALES_URL = "https://services.swpc.noaa.gov/products/noaa-scales.json";

    /** The conditional-GET state key of the messages. */
    public static final String ALERTS_KEY = "alerts";

    /** The conditional-GET state key of the scales. */
    public static final String SCALES_KEY = "scales";

    /** The record TTL, on receipt. */
    static final Duration TTL = Duration.ofDays(7);

    /** The keyframe: under {@code ttl - poll - 30 s}. */
    static final Duration KEYFRAME = Duration.ofDays(6);

    /** Raw retention: alerts are the {@code event} class (1825 d). */
    static final Duration RETENTION = Duration.ofDays(1825);

    private final URI alertsUri;
    private final URI scalesUri;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value SpaceSources#BASE_URL_PROPERTY}; blank in production
     */
    @Autowired
    public SwpcAlertsAdapter(@Value("${" + SpaceSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.alertsUri = SpaceSources.uri(ALERTS_URL, baseUrlOverride);
        this.scalesUri = SpaceSources.uri(SCALES_URL, baseUrlOverride);
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /**
     * Fetches both files (see the class note).
     *
     * @throws AdapterException when a request fails
     * @throws UpstreamFormatException when a changed file cannot be used
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        byte[] alerts = SpaceSources.conditional(ctx, alertsUri, ALERTS_KEY);
        byte[] scales = SpaceSources.conditional(ctx, scalesUri, SCALES_KEY);
        if (alerts == null && scales == null) {
            return FetchResult.unchanged();
        }
        List<NormalizedRecord> out = new ArrayList<>();
        if (alerts != null) {
            try {
                SwpcMessageParser.Messages messages = SwpcMessageParser.parse(alerts);
                out.addAll(messages.records());
                ctx.log().debug("{}: {} message record(s), canceled {}, skipped {}", ID, messages.records().size(),
                        messages.canceled(), messages.skipped());
            } catch (UpstreamFormatException e) {
                SourceHttp.forgetValidators(ctx.state(), ALERTS_KEY);
                throw e;
            }
        }
        if (scales != null) {
            try {
                out.add(SwpcProductsParser.scales(scales));
            } catch (UpstreamFormatException e) {
                SourceHttp.forgetValidators(ctx.state(), SCALES_KEY);
                throw e;
            }
        }
        return FetchResult.of(out);
    }

    private static SourceDescriptor buildDescriptor() {
        String alert = SwpcMessageParser.KIND;
        String scales = SwpcProductsParser.SCALES_KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("NOAA SWPC alerts, warnings and scales")
                .description("NOAA Space Weather Prediction Center messages (alerts, warnings, watches, summaries and"
                        + " cancellations, with their message code, serial, NOAA scale, K-index and validity window)"
                        + " and the current R, S and G scales with day 1-3 probabilities. No geometry. A cancel"
                        + " message tombstones the message it cancels; the message text is upstream text.")
                .kinds(alert, scales)
                .homepage("https://www.swpc.noaa.gov/content/data-access")
                .attribution("NOAA Space Weather Prediction Center")
                .licence("US-PD")
                .termsUrl("https://www.weather.gov/disclaimer")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(5), Duration.ofMinutes(5))
                .defaultBudget(Budget.of(2, 600))
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(new IngestPolicy.All(KEYFRAME))
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(TTL)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .zeroResultsNormal(true)
                .defaultRetention(RETENTION)
                .kindRetention(alert, RETENTION)
                .kindRetention(scales, RETENTION)
                .titleField("code")
                .searchFields("code", "category", "noaaScale")
                .embedFields("message");
        b.fieldSpec("code", spec(FieldType.STRING, null, "Message code", "SWPC message code (ALTK05, WARK04, WATA20)",
                alert));
        b.fieldSpec("category", spec(FieldType.STRING, null, "Category",
                "alert, warning, watch, summary, cancel or other", alert));
        b.fieldSpec("serial", spec(FieldType.INTEGER, null, "Serial", "The message serial number", alert));
        b.fieldSpec("noaaScale", spec(FieldType.STRING, null, "NOAA scale", "The NOAA scale named (G1-G5, R1-R5,"
                + " S1-S5)", alert));
        b.fieldSpec("kIndex", spec(FieldType.INTEGER, "1", "K-index", "The K-index the message names", alert));
        b.fieldSpec("message", spec(FieldType.STRING, null, "Message", "The message text, line ends normalized, at"
                + " most 4 KB; upstream text, untrusted", alert));
        for (String scale : List.of("r", "s", "g")) {
            String upper = scale.toUpperCase(java.util.Locale.ROOT);
            b.fieldSpec(scale, spec(FieldType.INTEGER, "1", upper + " scale", "The current NOAA " + upper
                    + " scale, 0-5", scales));
            b.fieldSpec(scale + "Text", spec(FieldType.STRING, null, upper + " scale text",
                    "The current " + upper + " scale's word (none, minor, moderate, strong, severe, extreme)", scales));
        }
        for (int day = 1; day <= 3; day++) {
            String p = "day" + day;
            b.fieldSpec(p + "RMinorProb", spec(FieldType.INTEGER, "%", "Day " + day + " R1-R2 probability",
                    "Probability of minor or moderate radio blackouts (R1-R2) on day " + day, scales));
            b.fieldSpec(p + "RMajorProb", spec(FieldType.INTEGER, "%", "Day " + day + " R3+ probability",
                    "Probability of strong or greater radio blackouts (R3-R5) on day " + day, scales));
            b.fieldSpec(p + "SProb", spec(FieldType.INTEGER, "%", "Day " + day + " S1+ probability",
                    "Probability of a solar radiation storm (S1 or greater) on day " + day, scales));
            b.fieldSpec(p + "GScale", spec(FieldType.INTEGER, "1", "Day " + day + " G scale",
                    "The predicted geomagnetic storm scale on day " + day, scales));
        }
        return b.build();
    }

    static FieldSpec spec(FieldType type, String unit, String label, String description, String kind) {
        return new FieldSpec(type, unit, label, description, null, Set.of(kind));
    }
}
