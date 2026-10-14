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
 * Graphical AIRMETs for the conterminous US ({@code awc-gairmets}, prompt 087): {@code /api/data/gairmet} as GeoJSON,
 * one request per run.
 *
 * <p>One {@code aviation-hazard} record per snapshot: externalId {@code product:tag:hazard:validTime:forecast},
 * {@code observedAt} = {@code issueTime}, {@code validFrom} = {@code validTime}; a snapshot asserts no end, so the
 * record TTL of 3 hours (the snapshot spacing), counted from receipt, ends it; a snapshot that leaves the feed is
 * tombstoned. 10-minute poll, never under 2 minutes, budget 1/min and 500/day. Retention 1825 days.
 */
@Component
public class AwcGairmetsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "awc-gairmets";

    /** The record kind. */
    public static final String KIND = AwcHazardParser.KIND;

    /** The product path. */
    public static final String PATH = "/api/data/gairmet";

    /** A snapshot stays current 3 hours after it was last received. */
    static final Duration TTL = Duration.ofHours(3);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value AwcSupport#BASE_URL_PROPERTY}; blank means the real site
     */
    public AwcGairmetsAdapter(@Value("${" + AwcSupport.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.baseUrl = AwcSupport.baseUrl(baseUrlOverride);
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The request of a run. */
    URI request() {
        return AwcSupport.uri(baseUrl, PATH, Map.of("format", "geojson"));
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        return AwcProducts.fetch(ctx, KIND, List.of(new AwcProducts.Request("gairmet", request(),
                AwcHazardParser::gairmets)));
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = AwcSupport.builder(ID, "AWC G-AIRMETs",
                "Graphical AIRMET snapshots (SIERRA: IFR and mountain obscuration; TANGO: turbulence, low-level wind"
                        + " shear, surface wind; ZULU: icing and freezing level) for the conterminous US from the NOAA"
                        + " Aviation Weather Center. AWC limits every client to 100 requests per minute; the default"
                        + " 10-minute poll uses one.", KIND)
                .mode(Mode.POLLING)
                .interval(AwcSupport.MIN_INTERVAL, Duration.ofMinutes(10))
                .defaultBudget(Budget.of(1, 500))
                .maxResponseBytes(8 * 1024 * 1024)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(TTL)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(AwcSigmetsAdapter.RETENTION)
                .kindRetention(KIND, AwcSigmetsAdapter.RETENTION)
                .zeroResultsNormal(true)
                .titleField("hazard")
                .searchFields("hazard", "qualifier")
                .tileProperties("product", "hazard", "severity", "baseFt", "topFt")
                .kindDisplay(KIND, new KindDisplay("G-AIRMET {hazard}", "warning", "#F59F00", null));
        AwcSigmetsAdapter.hazardFields(b);
        return b.build();
    }
}
