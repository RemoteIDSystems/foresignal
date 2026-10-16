package ai.foresignal.source.adapters.cdc;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CountyGeometries;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.CurrentRecords;
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
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code cdc-nwss-wval} (prompt 095): CDC NWSS site viral-activity levels for SARS-CoV-2, Influenza A and RSV,
 * from the current dataset {@code atcp-73re} (the older {@code 2ew6-ywp6}/{@code g653-rqe2} were archived on
 * 2025-09-12).
 *
 * <p>Each run reads rows after the stored resumable cursor through {@link SodaClient} (optionally with an
 * {@code X-App-Token}), at most as many pages as its per-minute budget allows, and folds them into one
 * {@code wastewater-level} record per site and pathogen: its latest {@code week_end} ({@link NwssWvalParser}). A
 * row whose week is older than the one already stored for that site and pathogen is dropped, so a re-dated older
 * sample never becomes current. Each record is placed at the interior point of the state's county union.
 * WastewaterSCAN rows are CC BY-NC 4.0 and are dropped unless {@code includeWastewaterScan} is set.
 */
@Component
public class NwssWvalAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "cdc-nwss-wval";

    /** The record kind. */
    public static final String KIND = NwssWvalParser.KIND;

    /** The production upstream. */
    public static final String DEFAULT_BASE_URL = "https://data.cdc.gov";

    /** Test-only override of {@link #DEFAULT_BASE_URL}. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.cdc.base-url";

    /** The optional Socrata app token. */
    public static final String APP_TOKEN = "appToken";

    /** The state key of the newest {@code date_updated} handed off. */
    static final String CURSOR_KEY = "nwss-wval:cursor";

    /** Raw retention: levels are sensor-like state (Conventions), 365 days. */
    static final Duration RETENTION = Duration.ofDays(365);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    /** The most current heads read to reject re-dated older weeks. */
    static final int CURRENT_LIMIT = 100_000;

    private final SodaClient soda;
    private final NwssGeometry geometry;
    private final Supplier<CurrentRecords> current;

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means {@value #DEFAULT_BASE_URL}
     * @param counties the 040 county outlines for geometry
     * @param current this source's current heads, to reject a re-dated older week; resolved lazily
     */
    @Autowired
    public NwssWvalAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride, CountyGeometries counties,
            ObjectProvider<CurrentRecords> current) {
        this(new SodaClient(baseUrlOverride == null || baseUrlOverride.isBlank()
                ? DEFAULT_BASE_URL : baseUrlOverride.trim()), counties,
                current == null ? () -> null : (Supplier<CurrentRecords>) current::getIfAvailable);
    }

    /** Creates the adapter without access to current heads (tests). */
    public NwssWvalAdapter(String baseUrlOverride, CountyGeometries counties) {
        this(new SodaClient(baseUrlOverride == null || baseUrlOverride.isBlank()
                ? DEFAULT_BASE_URL : baseUrlOverride.trim()), counties, () -> null);
    }

    /** Creates the adapter with a prebuilt reader (tests). */
    public NwssWvalAdapter(SodaClient soda, CountyGeometries counties) {
        this(soda, counties, () -> null);
    }

    /** Creates the adapter with a prebuilt reader and a current-heads view (tests). */
    public NwssWvalAdapter(SodaClient soda, CountyGeometries counties, Supplier<CurrentRecords> current) {
        this.soda = soda;
        this.geometry = new NwssGeometry(counties);
        this.current = current == null ? () -> null : current;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /**
     * Reads the rows after the cursor, at most as many pages as the per-minute budget allows (the rest resume
     * next run), and emits one record per site and pathogen: its latest week. A row whose week is older than the
     * week already stored for its site and pathogen (a re-dated older sample) is dropped.
     *
     * @throws AdapterException when a request fails
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        SodaClient.Cursor cursor = SodaClient.Cursor.load(ctx.state(), CURSOR_KEY);
        String token = ctx.credentials() == null ? null : ctx.credentials().get(APP_TOKEN);
        NwssWvalParser.Options options = new NwssWvalParser.Options(
                ctx.params().bool("includeWastewaterScan", false));

        SodaClient.Batch batch = soda.read(ctx, NwssWvalParser.DATASET, cursor, token, requestsPerRun(descriptor()));
        NwssWvalParser.Result parsed = NwssWvalParser.parse(batch.rows(), options);
        Map<String, Instant> stored = storedWeeks();

        List<NormalizedRecord> out = new ArrayList<>(parsed.levels().size());
        int stale = 0;
        for (NwssWvalParser.Level level : parsed.levels()) {
            Instant storedWeek = stored.get(level.key());
            if (storedWeek != null && level.weekEndInstant() != null && level.weekEndInstant().isBefore(storedWeek)) {
                stale++;
                continue;
            }
            Point point = geometry.forState(level.state());
            out.add(NwssWvalParser.toRecord(level, point));
        }
        batch.next().store(ctx.state(), CURSOR_KEY);
        ctx.log().debug("{}: {} level(s) from {} row(s) in {} request(s), {} skipped, {} WastewaterSCAN skipped,"
                + " {} older than the stored week{}", ID, out.size(), parsed.rows(), batch.requests(),
                parsed.skipped(), parsed.wastewaterScanSkipped(), stale, batch.exhausted() ? "" : "; more next run");
        return FetchResult.of(out);
    }

    /** The requests one run may send: the per-minute budget (the bucket refills between the 12-hour runs). */
    static int requestsPerRun(SourceDescriptor descriptor) {
        Budget budget = descriptor.defaultBudget();
        return Math.max(1, Math.min(SodaClient.MAX_PAGES, budget == null ? 1 : budget.perMinute()));
    }

    /** The week end of every stored level, by external id; empty without a current-heads view. */
    private Map<String, Instant> storedWeeks() {
        CurrentRecords view = current.get();
        if (view == null) {
            return Map.of();
        }
        Map<String, Instant> weeks = new HashMap<>();
        for (CurrentRecords.Row row : view.current(List.of(ID), List.of(KIND), CURRENT_LIMIT)) {
            Object week = row.payload() == null ? null : row.payload().get("weekEnd");
            Instant instant = week == null ? null : CdcDates.instant(week.toString());
            if (instant != null) {
                weeks.put(row.externalId(), instant);
            }
        }
        return weeks;
    }

    static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("CDC NWSS wastewater levels")
                .description("CDC National Wastewater Surveillance System site viral-activity levels for"
                        + " SARS-CoV-2, Influenza A and RSV, from the current dataset atcp-73re. Each run reads"
                        + " rows newer than the stored date_updated cursor. Every record is public-domain US"
                        + " government data except WastewaterSCAN rows, which are CC BY-NC 4.0 and dropped unless"
                        + " includeWastewaterScan is set. Sites have no coordinates: a record is placed at the"
                        + " interior point of the state's county union (geometryApproximate).")
                .kind(KIND)
                .homepage("https://www.cdc.gov/nwss/")
                .attribution("U.S. Centers for Disease Control and Prevention, National Wastewater Surveillance"
                        + " System")
                .licence("Public Domain U.S. Government")
                .termsUrl("https://data.cdc.gov/stories/s/Data-Policy/6wyh-hgvw/")
                .mode(Mode.POLLING)
                .credential(CredentialField.optional(APP_TOKEN,
                        "A Socrata app token for data.cdc.gov (recommended: anonymous requests share an IP pool and"
                                + " are throttled with 429s)"))
                .param(ParamSpec.optional("includeWastewaterScan", ParamType.BOOLEAN,
                        "Keep WastewaterSCAN rows (CC BY-NC 4.0, carried as licenceOverride) instead of dropping"
                                + " them", false))
                .interval(Duration.ofHours(6), Duration.ofHours(12))
                .defaultBudget(Budget.of(4, 100))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .titleField("pathogen")
                .searchFields("state", "countiesServed", "pathogen")
                .tileProperties("pathogen", "wval", "category")
                .rankField("wval")
                .zeroResultsNormal(true)
                .kindDisplay(KIND, new KindDisplay("{pathogen} level {wval}", null, "#0CA678", null));
        b.fieldSpec("pathogen", new FieldSpec(FieldType.STRING, null, "Pathogen",
                "SARS-CoV-2, Influenza A or RSV", null, Set.of()));
        b.fieldSpec("wval", new FieldSpec(FieldType.NUMBER, "1", "Viral activity level",
                "Site viral activity level, one decimal", null, Set.of()));
        b.fieldSpec("category", new FieldSpec(FieldType.STRING, null, "Category",
                "The site's viral-activity category (low, moderate, high, very high)", null, Set.of()));
        b.fieldSpec("weekEnd", new FieldSpec(FieldType.TIMESTAMP, null, "Week ending",
                "The week-ending date", null, Set.of()));
        b.fieldSpec("state", new FieldSpec(FieldType.STRING, null, "State", "The state", null, Set.of()));
        b.fieldSpec("countiesServed", new FieldSpec(FieldType.STRING, null, "Counties served",
                "Counties served by the site, as the CDC writes them", null, Set.of()));
        b.fieldSpec("populationServed", new FieldSpec(FieldType.INTEGER, "1", "Population served",
                "Population served by the site", null, Set.of()));
        b.fieldSpec("source", new FieldSpec(FieldType.STRING, null, "Source", "CDC or WastewaterSCAN", null,
                Set.of()));
        b.fieldSpec("geometryApproximate", new FieldSpec(FieldType.BOOLEAN, null, "Approximate geometry",
                "True: the geometry is a state interior point, not a measured site position", null, Set.of()));
        b.fieldSpec("licenceOverride", new FieldSpec(FieldType.STRING, null, "Licence override",
                "Present on WastewaterSCAN rows: CC BY-NC 4.0 (WastewaterSCAN)", null, Set.of()));
        return b.build();
    }
}
