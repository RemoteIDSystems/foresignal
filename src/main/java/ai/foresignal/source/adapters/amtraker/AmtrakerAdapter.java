package ai.foresignal.source.adapters.amtraker;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldThreshold;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.MotionSpec;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code amtraker} (prompt 080): Amtrak, VIA Rail and Brightline train positions from the community Amtraker API, as
 * {@code train} records ({@link AmtrakerParser}).
 *
 * <p>One {@code GET /v3/trains} per poll (about 110 KB gzip, 1.3 MB decoded on 2026-10-13, 218 trains). Interval
 * <strong>60 s</strong> (minimum 60 s; a community service with no published limit), budget 2/min, 1,500/day and
 * 250 MB/day.
 *
 * <p>Policy (Core-Data-Model §5.3 "Transit and rail"): TRACK, TTL 5 min OBSERVED, SAMPLED {@code {15s, 180s, 150 m,
 * thresholds, edgeMin 8s}} with the keyframe shortened to 180 s for the 60 s poll ({@code 180 ≤ 300 − 60 − 30});
 * threshold {@code headingDeg} 30° (angular); motion {@code {speedKph, km/h, headingDeg}}.
 */
@Component
public class AmtrakerAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "amtraker";

    /** Test-only override of the origin (Adapter Guide §8); the request path is kept. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.amtraker.base-url";

    /** The production origin. */
    public static final String DEFAULT_ORIGIN = "https://api.amtraker.com";

    /** The request path. */
    static final String PATH = "/v3/trains";

    /** The record TTL. */
    public static final Duration TTL = Duration.ofMinutes(5);

    /** The poll interval, default and minimum. */
    public static final Duration INTERVAL = Duration.ofSeconds(60);

    /** The keyframe, within {@code ttl − interval − 30 s} (210 s at the 60 s interval; it holds up to a 90 s one). */
    public static final Duration KEYFRAME = Duration.ofSeconds(180);

    /** The default budget. */
    public static final Budget BUDGET = Budget.of(2, 1_500, 250);

    /** The significant fields (prompt 080). */
    public static final List<String> SIGNIFICANT = List.of("trainNumber", "route", "provider", "state",
            "destinationCode");

    /** The response cap (the decoded document was 1.3 MB on capture day). */
    static final int MAX_RESPONSE_BYTES = 8 * 1024 * 1024;

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String origin;

    /**
     * Creates the adapter.
     *
     * @param originOverride the value of {@value #BASE_URL_PROPERTY}; blank means {@code https://api.amtraker.com}
     */
    @Autowired
    public AmtrakerAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String originOverride) {
        String chosen = originOverride == null || originOverride.isBlank() ? DEFAULT_ORIGIN : originOverride.trim();
        this.origin = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The request URL. */
    public URI uri() {
        return URI.create(origin + PATH);
    }

    /**
     * Fetches every train once.
     *
     * @return the trains with an observed position, as a complete result
     * @throws AdapterException when the request fails
     * @throws UpstreamFormatException when the body is truncated or not the expected object
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse response = ctx.http().get(uri());
        if (response.truncated()) {
            throw new UpstreamFormatException("Amtraker /v3/trains exceeded maxResponseBytes", null);
        }
        AmtrakerParser.Result result = AmtrakerParser.parse(response.body());
        if (result.skipped() > 0 || result.predeparture() > 0) {
            ctx.log().debug("{}: skipped {} trains without a usable position and {} not yet departed",
                    ctx.sourceId(), result.skipped(), result.predeparture());
        }
        return FetchResult.complete(result.records());
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("Amtraker trains (Amtrak, VIA Rail, Brightline)")
                .description("Positions of Amtrak, VIA Rail and Brightline trains from Amtraker (api.amtraker.com"
                        + " /v3/trains), an unofficial community relay of the operators' map data. Amtraker licenses"
                        + " the data under ODC-By 1.0 and requires attribution with a link to amtraker.com; it"
                        + " publishes no rate limit, so the source polls every 60 s. Trains"
                        + " that have not departed are left out. TRACK, TTL 5 min, observedAt = the time of the last"
                        + " valid position, keyframe 180 s (holds up to a 90 s interval).")
                .kind(AmtrakerParser.KIND)
                .mode(Mode.POLLING)
                .homepage("https://amtraker.com")
                .attribution("Amtraker (https://amtraker.com)")
                .licence("ODC-By 1.0")
                .termsUrl("https://github.com/piemadd/amtrak#attributionlicensing")
                .egress(Egress.OPEN)
                .interval(INTERVAL, INTERVAL)
                .defaultBudget(BUDGET)
                .maxResponseBytes(MAX_RESPONSE_BYTES)
                .historyTier(HistoryTier.TRACK)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(TTL)
                .defaultIngestPolicy(new IngestPolicy.Sampled(Duration.ofSeconds(15), KEYFRAME, 150.0, true,
                        Duration.ofSeconds(8)))
                .significantFields(SIGNIFICANT.toArray(String[]::new))
                .threshold(FieldThreshold.angular("headingDeg", 30.0))
                .motion(new MotionSpec("speedKph", MotionSpec.SpeedUnit.KILOMETERS_PER_HOUR, "headingDeg"))
                .titleField("route")
                .tileProperties("trainNumber", "route", "provider", "headingDeg", "timeliness")
                .searchFields("trainNumber", "route", "provider", "originCode", "destinationCode")
                .kindDisplay(AmtrakerParser.KIND, new KindDisplay("{route} {trainNumber}", "rail", "#1864AB", null,
                        "headingDeg", null, null));
        text(b, "trainNumber", "Train number", "Train number; VIA numbers start with v, Brightline numbers with b");
        text(b, "route", "Route", "Route name, for example Sunset Limited");
        text(b, "provider", "Provider", "Amtrak, Via or Brightline");
        text(b, "state", "State", "Active or Completed (trains that have not departed are not reported)");
        text(b, "timeliness", "Timeliness", "Amtraker's on-time text; omitted when blank");
        text(b, "nextStationCode", "Next station", "Code of the station the train reports next (eventCode)");
        text(b, "originCode", "Origin", "Code of the origin station");
        text(b, "destinationCode", "Destination", "Code of the destination station");
        b.fieldSpec("speedKph", new FieldSpec(FieldType.NUMBER, "km/h", "Speed",
                "Speed converted from Amtraker's mph, rounded to 0.1", null, Set.of()));
        b.fieldSpec("headingDeg", new FieldSpec(FieldType.NUMBER, "deg", "Heading",
                "Heading from Amtraker's eight-point compass text (N = 0, E = 90), so it moves in 45-degree steps",
                null, Set.of()));
        return b.build();
    }

    private static void text(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }
}
