package ai.foresignal.source.adapters.hms;

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
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * NOAA HMS analyst fire points ({@code hms-fire}, prompt 048; https://www.ospo.noaa.gov/Products/land/hms.html).
 *
 * <p>Conditional GETs of today's text file (UTC), and yesterday's during the first 6 hours after
 * midnight ({@link HmsDailyFiles}); parsed with {@link HmsFireParser} into {@code fire-detection} records,
 * the kind {@code goes-fdc} also writes, so one "fire near home" rule covers both. Points are never
 * tombstoned: they leave 24 hours after their detection time ({@code recordTtl} 24 h anchored on
 * {@code observedAt}). Outside the fire season a day can have few points; zero results are normal.
 */
@Component
public class HmsFireAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "hms-fire";

    /** The record kind. */
    public static final String KIND = HmsFireParser.KIND;

    /** Test-only override of the HMS host, shared with {@code hms-smoke}. */
    public static final String BASE_URL_PROPERTY = HmsSmokeAdapter.BASE_URL_PROPERTY;

    /** The validator key prefix. */
    static final String STATE_PREFIX = "fire";

    /** Retention: Weather-Imagery-Design §2.4 keeps {@code fire-detection} 7 days. */
    static final Duration RETENTION = Duration.ofDays(7);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String base;

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means the NESDIS host
     */
    @Autowired
    public HmsFireAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.base = HmsDailyFiles.base(baseUrlOverride);
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        HmsDailyFiles.Outcome<HmsFireParser.Result> outcome = HmsDailyFiles.fetch(ctx, HmsDailyFiles.days(ctx.now()),
                day -> HmsDailyFiles.fireUri(base, day), STATE_PREFIX, (day, body) -> HmsFireParser.parse(body));
        if (outcome.parsed().isEmpty()) {
            return FetchResult.unchanged();
        }
        List<NormalizedRecord> out = new ArrayList<>();
        for (HmsDailyFiles.Parsed<HmsFireParser.Result> parsed : outcome.parsed()) {
            out.addAll(parsed.value().records());
            ctx.log().debug("HMS fire {}: {} points from {} rows ({} skipped)", parsed.day(),
                    parsed.value().records().size(), parsed.value().rows(), parsed.value().skipped());
        }
        return FetchResult.of(out);
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID)
                .name("HMS fire points")
                .description("NOAA Hazard Mapping System analyst fire detections over North America (GOES and polar"
                        + " satellites, quality-controlled by analysts), today's file and, after midnight, yesterday's.")
                .kind(KIND)
                .homepage(HmsSmokeAdapter.TERMS)
                .attribution("NOAA NESDIS Hazard Mapping System (HMS)")
                .licence("US-PD")
                .termsUrl(HmsSmokeAdapter.TERMS)
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(30), Duration.ofMinutes(30))
                .defaultBudget(Budget.of(4, 200))
                .maxResponseBytes(16 * 1024 * 1024)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(Duration.ofHours(24))
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .kindRetention(KIND, RETENTION)
                .zeroResultsNormal(true)
                .tileProperties("satellite", "frpMw")
                .kindDisplay(KIND, new KindDisplay("Fire ({satellite})", "fire", "#E8590C", null));
        builder.fieldSpec("satellite", new FieldSpec(FieldType.STRING, null, "Satellite",
                "The detecting platform, for example GOES-EAST or NOAA 21", null, Set.of()));
        builder.fieldSpec("method", new FieldSpec(FieldType.STRING, null, "Method",
                "How the point was made, for example ANALYSIS or FDC", null, Set.of()));
        builder.fieldSpec("ecosystem", new FieldSpec(FieldType.INTEGER, "1", "Ecosystem",
                "HMS ecosystem class", null, Set.of()));
        builder.fieldSpec("frpMw", new FieldSpec(FieldType.NUMBER, "MW", "Fire radiative power",
                "2 significant figures; absent when HMS reports -999", null, Set.of()));
        builder.fieldSpec("source", new FieldSpec(FieldType.STRING, null, "Source", "Always HMS", null, Set.of()));
        return builder.build();
    }
}
