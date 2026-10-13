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
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.net.URI;
import java.time.Duration;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code swpc-xray} (prompt 077): GOES X-ray flares as {@code solar-flare} records and the current X-ray class as the
 * single {@code xray-current} entity ({@link SwpcProductsParser#xray}).
 *
 * <p><strong>Each run:</strong> one conditional GET of {@value #FLARES_URL} (about 0.5 KB, the latest flare); a 304
 * is {@link FetchResult#unchanged()}. The source has no TTL because {@code recordTtl} is per source: flares are
 * history, and the current entity is refreshed by every poll.
 */
@Component
public class SwpcXrayAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "swpc-xray";

    /** The latest-flares file. */
    public static final String FLARES_URL = "https://services.swpc.noaa.gov/json/goes/primary/xray-flares-latest.json";

    /** The conditional-GET state key. */
    public static final String FLARES_KEY = "xray-flares";

    /** Raw retention of flares: the {@code event} class (1825 d). */
    static final Duration FLARE_RETENTION = Duration.ofDays(1825);

    /** Raw retention of the current class: SAMPLED state, the {@code state} class (365 d). */
    static final Duration CURRENT_RETENTION = Duration.ofDays(365);

    private final URI flaresUri;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value SpaceSources#BASE_URL_PROPERTY}; blank in production
     */
    @Autowired
    public SwpcXrayAdapter(@Value("${" + SpaceSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.flaresUri = SpaceSources.uri(FLARES_URL, baseUrlOverride);
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /**
     * Fetches the latest flares (see the class note).
     *
     * @throws AdapterException when the request fails
     * @throws UpstreamFormatException when the file cannot be used
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        byte[] body = SpaceSources.conditional(ctx, flaresUri, FLARES_KEY);
        if (body == null) {
            return FetchResult.unchanged();
        }
        try {
            return FetchResult.of(SwpcProductsParser.xray(body));
        } catch (UpstreamFormatException e) {
            SourceHttp.forgetValidators(ctx.state(), FLARES_KEY);
            throw e;
        }
    }

    private static SourceDescriptor buildDescriptor() {
        String flare = SwpcProductsParser.FLARE_KIND;
        String current = SwpcProductsParser.XRAY_CURRENT_KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("NOAA SWPC GOES X-ray flares")
                .description("GOES primary X-ray flares from NOAA SWPC (begin, maximum and end times and classes, one"
                        + " record per flare) and the current X-ray class and flux ratio as one entity (current). No"
                        + " geometry.")
                .kinds(flare, current)
                .homepage("https://www.swpc.noaa.gov/products/goes-x-ray-flux")
                .attribution("NOAA Space Weather Prediction Center")
                .licence("US-PD")
                .termsUrl("https://www.weather.gov/disclaimer")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(5), Duration.ofMinutes(5))
                .defaultBudget(Budget.of(2, 600))
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(new IngestPolicy.All(null))
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .defaultRetention(CURRENT_RETENTION)
                .kindRetention(flare, FLARE_RETENTION)
                .kindRetention(current, CURRENT_RETENTION)
                .titleField("maxClass")
                .searchFields("maxClass", "currentClass");
        b.fieldSpec("beginTime", spec(FieldType.TIMESTAMP, "Begin", "Flare begin time (UTC)", flare));
        b.fieldSpec("maxTime", spec(FieldType.TIMESTAMP, "Maximum", "Time of the flare maximum (UTC)", flare));
        b.fieldSpec("endTime", spec(FieldType.TIMESTAMP, "End", "Flare end time (UTC), absent while it is running",
                flare));
        b.fieldSpec("beginClass", spec(FieldType.STRING, "Begin class", "X-ray class at the beginning (B7.9)", flare));
        b.fieldSpec("maxClass", spec(FieldType.STRING, "Maximum class", "X-ray class at the maximum (C4.2, M6.4)",
                flare));
        b.fieldSpec("currentClass", new FieldSpec(FieldType.STRING, null, "Current class",
                "The X-ray class when the file was written", null, Set.of(flare, current)));
        b.fieldSpec("satellite", spec(FieldType.INTEGER, "Satellite", "The GOES satellite number", flare));
        b.fieldSpec("currentRatio", new FieldSpec(FieldType.NUMBER, "1", "Flux ratio",
                "The current short-to-long channel flux ratio, 4 decimals", null, Set.of(current)));
        return b.build();
    }

    private static FieldSpec spec(FieldType type, String label, String description, String kind) {
        return new FieldSpec(type, null, label, description, null, Set.of(kind));
    }
}
