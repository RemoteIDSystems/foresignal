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
 * {@code swpc-kp} (prompt 077): the planetary K-index as two {@code geomagnetic-index} entities,
 * {@code kp-estimated} (the newest one-minute estimate) and {@code kp-official} (the newest three-hour value).
 *
 * <p><strong>Each run:</strong> a conditional GET of {@value #ESTIMATED_URL} (about 28 KB) and
 * {@value #OFFICIAL_URL} (about 5 KB); a 304 file contributes nothing, both 304 is {@link FetchResult#unchanged()}.
 * Kp is a measurement: {@code eventTime} is the row's {@code time_tag}, the TTL is 3 h from {@code observedAt}
 * (see {@link SwpcProductsParser#kpOfficial} for the official row's observation time).
 */
@Component
public class SwpcKpAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "swpc-kp";

    /** The one-minute estimated Kp. */
    public static final String ESTIMATED_URL = "https://services.swpc.noaa.gov/json/planetary_k_index_1m.json";

    /** The three-hour official Kp. */
    public static final String OFFICIAL_URL = "https://services.swpc.noaa.gov/products/noaa-planetary-k-index.json";

    /** The conditional-GET state key of the estimate. */
    public static final String ESTIMATED_KEY = "kp-1m";

    /** The conditional-GET state key of the official value. */
    public static final String OFFICIAL_KEY = "kp-3h";

    /** The record TTL, from {@code observedAt}. */
    static final Duration TTL = Duration.ofHours(3);

    /** The keyframe: under {@code ttl - poll - 30 s}. */
    static final Duration KEYFRAME = Duration.ofHours(2);

    /** Raw retention: Kp is SAMPLED geophysical state, the {@code state} class (365 d). */
    static final Duration RETENTION = Duration.ofDays(365);

    private final URI estimatedUri;
    private final URI officialUri;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value SpaceSources#BASE_URL_PROPERTY}; blank in production
     */
    @Autowired
    public SwpcKpAdapter(@Value("${" + SpaceSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.estimatedUri = SpaceSources.uri(ESTIMATED_URL, baseUrlOverride);
        this.officialUri = SpaceSources.uri(OFFICIAL_URL, baseUrlOverride);
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
        byte[] estimated = SpaceSources.conditional(ctx, estimatedUri, ESTIMATED_KEY);
        byte[] official = SpaceSources.conditional(ctx, officialUri, OFFICIAL_KEY);
        if (estimated == null && official == null) {
            return FetchResult.unchanged();
        }
        List<NormalizedRecord> out = new ArrayList<>();
        if (estimated != null) {
            add(out, ctx, ESTIMATED_KEY, () -> SwpcProductsParser.kpEstimated(estimated));
        }
        if (official != null) {
            add(out, ctx, OFFICIAL_KEY, () -> SwpcProductsParser.kpOfficial(official));
        }
        return FetchResult.of(out);
    }

    private static void add(List<NormalizedRecord> out, FetchContext ctx, String key,
            java.util.function.Supplier<NormalizedRecord> parse) {
        try {
            NormalizedRecord r = parse.get();
            if (r != null) {
                out.add(r);
            }
        } catch (UpstreamFormatException e) {
            SourceHttp.forgetValidators(ctx.state(), key);
            throw e;
        }
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = SwpcProductsParser.KP_KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("NOAA SWPC planetary K-index")
                .description("The planetary K-index from NOAA SWPC as two entities: kp-estimated (the newest"
                        + " one-minute estimate, with its thirds notation such as 3Z, 2P, 3M) and kp-official (the"
                        + " newest three-hour value with the running a-index and station count). No geometry.")
                .kind(kind)
                .homepage("https://www.swpc.noaa.gov/products/planetary-k-index")
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
                .ttlAnchor(TtlAnchor.OBSERVED)
                .defaultRetention(RETENTION)
                .kindRetention(kind, RETENTION)
                .titleField("kpText")
                .searchFields()
                .rankField("kp");
        b.fieldSpec("kp", new FieldSpec(FieldType.NUMBER, "1", "Kp", "The planetary K-index, 0-9 in thirds", null,
                Set.of()));
        b.fieldSpec("kpText", new FieldSpec(FieldType.STRING, null, "Kp (thirds)",
                "Kp in SWPC thirds notation: Z on the whole number, P a third above, M a third below", null, Set.of()));
        b.fieldSpec("aRunning", new FieldSpec(FieldType.INTEGER, "1", "Running a-index",
                "The running planetary a-index (kp-official only)", null, Set.of()));
        b.fieldSpec("stationCount", new FieldSpec(FieldType.INTEGER, "1", "Stations",
                "Magnetometer stations in the official value (kp-official only)", null, Set.of()));
        return b.build();
    }
}
