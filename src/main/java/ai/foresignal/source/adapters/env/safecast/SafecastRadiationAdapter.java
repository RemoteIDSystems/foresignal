package ai.foresignal.source.adapters.env.safecast;

import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.SkipCounter;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code safecast-radiation} (prompt 078): Safecast's fixed realtime radiation devices as {@code radiation} point
 * records ({@link SafecastDevicesParser}).
 *
 * <p><strong>Each run:</strong> one request for {@value #DEVICES_URL} (1.4 MB on 2026-10-13, no validators). The
 * historical {@code api.safecast.org/measurements.json} is not used: an unfiltered newest-first query answered 204
 * and it mixes mobile bGeigie survey points with fixed sensors.
 */
@Component
public class SafecastRadiationAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "safecast-radiation";

    /** The realtime device list. */
    public static final String DEVICES_URL = "https://tt.safecast.org/devices";

    /** The record TTL. */
    static final Duration TTL = Duration.ofHours(6);

    /** Raw retention: sensor readings are the {@code state} class (365 d). */
    static final Duration RETENTION = Duration.ofDays(365);

    private final URI devices;
    private final Supplier<MeterRegistry> meters;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value EnvSources#BASE_URL_PROPERTY}; blank in production
     * @param meters the registry of the skip counter, resolved lazily
     */
    @Autowired
    public SafecastRadiationAdapter(@Value("${" + EnvSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<MeterRegistry> meters) {
        this(baseUrlOverride, meters == null ? () -> null : (Supplier<MeterRegistry>) meters::getIfAvailable);
    }

    /**
     * Creates the adapter with a plain supplier (tests).
     *
     * @param baseUrlOverride the test base URL, or blank
     * @param meters supplies the meter registry, or null
     */
    public SafecastRadiationAdapter(String baseUrlOverride, Supplier<MeterRegistry> meters) {
        this.devices = EnvSources.uri(DEVICES_URL, baseUrlOverride);
        this.meters = meters;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The device list URL in use. */
    public URI devicesUri() {
        return devices;
    }

    /**
     * Reads the device list once.
     *
     * @throws AdapterException when the request fails
     * @throws UpstreamFormatException on a bad status, a truncated body or a body that is not a JSON array
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        EnvSources.Parsed parsed = SafecastDevicesParser.parse(
                EnvSources.body(ctx, ctx.http().get(devices, Map.of("Accept", "application/json")), null, devices),
                ctx.now(), EnvSources.maxAge(ctx.params(), SafecastDevicesParser.DEFAULT_MAX_AGE));
        SkipCounter.count(meters, EnvSources.SKIPPED_METRIC, EnvSources.SKIPPED_DESCRIPTION, ctx.sourceId(),
                parsed.skipped());
        ctx.log().debug("{}: {} radiation device(s), skipped {}", ID, parsed.records().size(), parsed.skipped());
        return FetchResult.of(parsed.records());
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = SafecastDevicesParser.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("Safecast radiation")
                .description("Fixed Safecast radiation monitors (Radnote, bGeigie-based and pointcast devices) from"
                        + " Safecast's realtime device list: counts per minute per Geiger tube and an approximate dose"
                        + " rate. Air-only devices are left out; captures older than maxAge (48 h) are skipped.")
                .kind(kind)
                .homepage("https://safecast.org/")
                .attribution("Safecast")
                .licence("CC0 1.0")
                .termsUrl("https://safecast.org/data/")
                .mode(Mode.POLLING)
                .param(EnvSources.maxAgeParam("48h"))
                .interval(Duration.ofMinutes(15), Duration.ofMinutes(30))
                .defaultBudget(Budget.of(1, 100, 100))
                .maxResponseBytes(16 * 1024 * 1024)
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(TTL)
                .defaultRetention(RETENTION)
                .kindRetention(kind, RETENTION)
                .tileProperties("doseRateUsvH")
                .searchFields("locationName", "country")
                .rankField("doseRateUsvH");
        b.fieldSpec("cpm", new FieldSpec(FieldType.OBJECT, "{counts}/min", "Counts per minute",
                "Counts per minute per Geiger tube field present (lnd_7318u, lnd_7318c, lnd_7128ec, lnd_712u)", null,
                Set.of()));
        b.fieldSpec("doseRateUsvH", new FieldSpec(FieldType.NUMBER, "uSv/h", "Dose rate",
                "Approximate ambient dose rate: lnd_7318u CPM / 334, else lnd_7128ec CPM / 108 (Safecast's"
                        + " approximate conversion factors); absent for other tubes", null, Set.of()));
        b.fieldSpec("locationName", new FieldSpec(FieldType.STRING, null, "Location", "Safecast location name", null,
                Set.of()));
        b.fieldSpec("country", new FieldSpec(FieldType.STRING, null, "Country", "ISO 3166-1 alpha-2 country code",
                null, Set.of()));
        b.fieldSpec("deviceClass", new FieldSpec(FieldType.STRING, null, "Device class", "Safecast device class", null,
                Set.of()));
        b.kindDisplay(kind, new KindDisplay("{doseRateUsvH} uSv/h", "radiation", "#E8590C", null));
        return b.build();
    }
}
