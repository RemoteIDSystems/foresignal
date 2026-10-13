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
import ai.foresignal.source.spi.LiveIdSet;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * NOAA HMS analyst smoke polygons ({@code hms-smoke}, prompt 048; https://www.ospo.noaa.gov/Products/land/hms.html).
 *
 * <p>Conditional GETs of today's KML file (UTC), and yesterday's during the first 6 hours after midnight
 * ({@link HmsDailyFiles}); parsed with {@link HmsKmlParser} into {@code smoke-polygon} records. A 404 for
 * today before the first analysis is normal.
 *
 * <p><b>Analyst edits:</b> each file is complete, so ids absent from a successfully fetched and parsed
 * file are tombstoned ({@link LiveIdSet}, scoped per file day), at the fetch time. A failed, truncated or
 * unparseable file tombstones nothing. Records also expire 24 hours after their {@code End}
 * ({@code recordTtl} 24 h anchored on {@code observedAt}).
 */
@Component
public class HmsSmokeAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "hms-smoke";

    /** The record kind. */
    public static final String KIND = HmsKmlParser.KIND;

    /** Test-only override of the HMS host. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.hms.base-url";

    /** The validator key prefix. */
    static final String STATE_PREFIX = "smoke";

    /** The adapter-state key listing the file days with a live-id snapshot, for cleanup. */
    static final String DAYS_KEY = "days";

    /** Live-id snapshots of days older than this are dropped. */
    static final int KEEP_DAYS = 3;

    /** Retention: Weather-Imagery-Design §2.4 keeps {@code smoke-polygon} 7 days. */
    static final Duration RETENTION = Duration.ofDays(7);

    /** HMS terms. */
    static final String TERMS = "https://www.ospo.noaa.gov/Products/land/hms.html";

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String base;

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means the NESDIS host
     */
    @Autowired
    public HmsSmokeAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.base = HmsDailyFiles.base(baseUrlOverride);
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The scope of {@code day}'s live-id snapshot. */
    static String scope(LocalDate day) {
        return STATE_PREFIX + ":" + HmsDailyFiles.COMPACT.format(day);
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        Instant now = ctx.now();
        HmsDailyFiles.Outcome<HmsKmlParser.Result> outcome = HmsDailyFiles.fetch(ctx, HmsDailyFiles.days(now),
                day -> HmsDailyFiles.smokeUri(base, day), STATE_PREFIX, (day, body) -> HmsKmlParser.parse(body, day));
        if (outcome.parsed().isEmpty()) {
            return FetchResult.unchanged();
        }
        // Every file was received in full and parsed: only now may absent polygons be tombstoned.
        List<NormalizedRecord> out = new ArrayList<>();
        for (HmsDailyFiles.Parsed<HmsKmlParser.Result> parsed : outcome.parsed()) {
            out.addAll(parsed.value().records());
            for (String id : LiveIdSet.absentSince(ctx.state(), scope(parsed.day()), parsed.value().ids())) {
                out.add(NormalizedRecord.tombstone(id, KIND, now));
            }
            ctx.log().debug("HMS smoke {}: {} polygons from {} placemarks ({} skipped)", parsed.day(),
                    parsed.value().records().size(), parsed.value().placemarks(), parsed.value().skipped());
            remember(ctx, parsed.day());
        }
        forgetOldDays(ctx, LocalDate.ofInstant(now, java.time.ZoneOffset.UTC));
        return FetchResult.of(out);
    }

    /** Adds {@code day} to the list of days with a snapshot. */
    private static void remember(FetchContext ctx, LocalDate day) {
        Set<String> days = LiveIdSet.read(ctx.state(), DAYS_KEY);
        if (days.add(HmsDailyFiles.COMPACT.format(day))) {
            ctx.state().put(DAYS_KEY, String.join("\n", days));
        }
    }

    /** Drops the live-id snapshots of days more than {@value #KEEP_DAYS} days before {@code today}. */
    static void forgetOldDays(FetchContext ctx, LocalDate today) {
        Set<String> days = LiveIdSet.read(ctx.state(), DAYS_KEY);
        Set<String> keep = new LinkedHashSet<>();
        for (String d : days) {
            LocalDate day;
            try {
                day = LocalDate.parse(d, HmsDailyFiles.COMPACT);
            } catch (RuntimeException e) {
                continue;
            }
            if (day.isBefore(today.minusDays(KEEP_DAYS))) {
                ctx.state().remove(LiveIdSet.LIVE_PREFIX + scope(day));
                ctx.state().remove(LiveIdSet.GONE_PREFIX + scope(day));
            } else {
                keep.add(d);
            }
        }
        if (keep.size() != days.size()) {
            if (keep.isEmpty()) {
                ctx.state().remove(DAYS_KEY);
            } else {
                ctx.state().put(DAYS_KEY, String.join("\n", keep));
            }
        }
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID)
                .name("HMS smoke polygons")
                .description("NOAA Hazard Mapping System analyst smoke polygons (light, medium, heavy) from GOES and"
                        + " polar imagery, today's file and, after midnight, yesterday's while analysts finish it.")
                .kind(KIND)
                .homepage(TERMS)
                .attribution("NOAA NESDIS Hazard Mapping System (HMS)")
                .licence("US-PD")
                .termsUrl(TERMS)
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
                .titleField("label")
                .zeroResultsNormal(true)
                .tileProperties("density", "densityRank", "label")
                .rankField("densityRank")
                .kindDisplay(KIND, new KindDisplay("{label}", "smoke", "#868E96", null));
        builder.fieldSpec("density", new FieldSpec(FieldType.STRING, null, "Density", "light, medium or heavy", null,
                Set.of()));
        builder.fieldSpec("densityRank", new FieldSpec(FieldType.INTEGER, "1", "Density rank",
                "1 light, 2 medium, 3 heavy", null, Set.of()));
        builder.fieldSpec("start", new FieldSpec(FieldType.TIMESTAMP, null, "Start", "Start of the analysis window",
                null, Set.of()));
        builder.fieldSpec("end", new FieldSpec(FieldType.TIMESTAMP, null, "End", "End of the analysis window", null,
                Set.of()));
        builder.fieldSpec("satellite", new FieldSpec(FieldType.STRING, null, "Satellite",
                "The imagery the analyst used, for example GOES-EAST", null, Set.of()));
        builder.fieldSpec("label", new FieldSpec(FieldType.STRING, null, "Label",
                "Heavy smoke (HMS, GOES-EAST, 12:00-15:00 UTC)", null, Set.of()));
        return builder.build();
    }
}
