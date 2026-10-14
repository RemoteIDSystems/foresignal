package ai.foresignal.source.adapters.awc;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * SIGMETs and AIRMETs from the Aviation Weather Center ({@code awc-sigmets}, prompt 087): the US
 * {@code /api/data/airsigmet} and the international {@code /api/data/isigmet} products, both as GeoJSON, read one
 * after the other each run (two requests).
 *
 * <p>One {@code aviation-hazard} record per advisory: externalId {@code icaoId:seriesId:validTimeFrom} (international:
 * {@code firId:seriesId:validTimeFrom}), {@code observedAt} = {@code validFrom} = {@code validTimeFrom},
 * {@code validUntil} = {@code validTimeTo} (asserted by the issuer). An advisory missing from the next complete pair
 * of responses is tombstoned (canceled before it expired). 5-minute poll, never under 2 minutes, budget 3/min and
 * 1,500/day, inside AWC's 100 requests/min ceiling ({@link AwcSupport}). Retention 1825 days ({@code event} class:
 * weather alerts).
 */
@Component
public class AwcSigmetsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "awc-sigmets";

    /** The record kind. */
    public static final String KIND = AwcHazardParser.KIND;

    /** The US product path. */
    public static final String AIRSIGMET_PATH = "/api/data/airsigmet";

    /** The international product path. */
    public static final String ISIGMET_PATH = "/api/data/isigmet";

    /** Retention: the {@code event} class. */
    static final Duration RETENTION = Duration.ofDays(1825);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value AwcSupport#BASE_URL_PROPERTY}; blank means the real site
     */
    public AwcSigmetsAdapter(@Value("${" + AwcSupport.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.baseUrl = AwcSupport.baseUrl(baseUrlOverride);
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The two requests of a run, US first. */
    List<URI> requests() {
        return List.of(AwcSupport.uri(baseUrl, AIRSIGMET_PATH, Map.of("format", "geojson")),
                AwcSupport.uri(baseUrl, ISIGMET_PATH, Map.of("format", "geojson")));
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        List<URI> uris = requests();
        return AwcProducts.fetch(ctx, KIND, List.of(
                new AwcProducts.Request("airsigmet", uris.get(0), AwcHazardParser::airSigmets),
                new AwcProducts.Request("isigmet", uris.get(1), AwcHazardParser::isigmets)));
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = AwcSupport.builder(ID, "AWC SIGMETs and AIRMETs",
                "US SIGMETs, convective SIGMETs and AIRMETs (airsigmet) and international SIGMETs (isigmet) from the"
                        + " NOAA Aviation Weather Center, with their polygons and validity. Two requests per run. AWC"
                        + " limits every client to 100 requests per minute and asks for at most 1 request per minute"
                        + " per endpoint; the default 5-minute poll complies.", KIND)
                .mode(Mode.POLLING)
                .interval(AwcSupport.MIN_INTERVAL, Duration.ofMinutes(5))
                .defaultBudget(Budget.of(3, 1500))
                .maxResponseBytes(8 * 1024 * 1024)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .kindRetention(KIND, RETENTION)
                .zeroResultsNormal(true)
                .titleField("hazard")
                .searchFields("hazard", "qualifier", "raw")
                .tileProperties("product", "hazard", "severity", "baseFt", "topFt")
                .kindDisplay(KIND, new KindDisplay("{product} {hazard}", "warning", "#E8590C", null));
        hazardFields(b);
        return b.build();
    }

    /** Declares the {@code aviation-hazard} payload fields (shared by the three hazard sources). */
    static void hazardFields(SourceDescriptor.Builder b) {
        AwcSupport.text(b, "product", "Product", "SIGMET, AIRMET, OUTLOOK, ISIGMET, GAIRMET or CWA");
        AwcSupport.text(b, "hazard", "Hazard", "The hazard, for example CONVECTIVE, TURB, ICE, IFR, VA, TS");
        AwcSupport.text(b, "severity", "Severity", "The upstream severity (a number for US SIGMETs, LGT/MOD/SEV for"
                + " G-AIRMETs)");
        AwcSupport.text(b, "qualifier", "Qualifier", "The upstream qualifier (international SIGMETs and CWAs) or the"
                + " G-AIRMET dueTo text");
        AwcSupport.integer(b, "baseFt", "[ft_i]", "Base", "Lowest altitude of the hazard, feet");
        AwcSupport.integer(b, "topFt", "[ft_i]", "Top", "Highest altitude of the hazard, feet");
        AwcSupport.integer(b, "movementDir", "deg", "Movement direction", "Direction the hazard moves from, degrees");
        AwcSupport.integer(b, "movementKt", "[kn_i]", "Movement speed", "Movement speed, knots");
        AwcSupport.text(b, "raw", "Text", "The raw advisory text (upstream text, untrusted)");
    }
}
