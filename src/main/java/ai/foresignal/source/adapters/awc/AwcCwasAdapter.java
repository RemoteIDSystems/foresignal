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
 * Center Weather Advisories ({@code awc-cwas}, prompt 087): {@code /api/data/cwa} as GeoJSON, one request per run.
 *
 * <p>One {@code aviation-hazard} record per advisory: externalId {@code cwsu:seriesId:validTimeFrom},
 * {@code observedAt} = {@code validFrom} = {@code validTimeFrom}, {@code validUntil} = {@code validTimeTo}; an
 * advisory that leaves the feed is tombstoned. Often none are in force (an empty collection is normal). 10-minute
 * poll, never under 2 minutes, budget 1/min and 500/day. Retention 1825 days.
 */
@Component
public class AwcCwasAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "awc-cwas";

    /** The record kind. */
    public static final String KIND = AwcHazardParser.KIND;

    /** The product path. */
    public static final String PATH = "/api/data/cwa";

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value AwcSupport#BASE_URL_PROPERTY}; blank means the real site
     */
    public AwcCwasAdapter(@Value("${" + AwcSupport.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
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
        return AwcProducts.fetch(ctx, KIND, List.of(new AwcProducts.Request("cwa", request(), AwcHazardParser::cwas)));
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = AwcSupport.builder(ID, "AWC Center Weather Advisories",
                "Center Weather Advisories issued by the NWS Center Weather Service Units, from the NOAA Aviation"
                        + " Weather Center. AWC limits every client to 100 requests per minute; the default 10-minute"
                        + " poll uses one.", KIND)
                .mode(Mode.POLLING)
                .interval(AwcSupport.MIN_INTERVAL, Duration.ofMinutes(10))
                .defaultBudget(Budget.of(1, 500))
                .maxResponseBytes(4 * 1024 * 1024)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(AwcSigmetsAdapter.RETENTION)
                .kindRetention(KIND, AwcSigmetsAdapter.RETENTION)
                .zeroResultsNormal(true)
                .titleField("hazard")
                .searchFields("hazard", "qualifier", "raw")
                .tileProperties("product", "hazard", "baseFt", "topFt")
                .kindDisplay(KIND, new KindDisplay("CWA {hazard}", "warning", "#D9480F", null));
        AwcSigmetsAdapter.hazardFields(b);
        return b.build();
    }
}
