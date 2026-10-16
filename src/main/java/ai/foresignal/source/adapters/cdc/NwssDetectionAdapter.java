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
import java.util.LinkedHashMap;
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
 * {@code cdc-nwss-detections} (prompt 095): CDC NWSS wastewater detections of select pathogens, from one current
 * dataset per pathogen through {@link SodaClient}.
 *
 * <p>The {@code pathogens} parameter (default {@code h5, measles, mpox}) maps to dataset ids: {@code h5}
 * {@code mtpu-urpp}, {@code measles} {@code akvg-8vrb}, {@code mpox} {@code xpxn-rzgz}, plus the selectable
 * {@code influenza-a} {@code ymmh-divb}, {@code rsv} {@code 45cq-cw4i} and {@code sars-cov-2} {@code j9g8-acpt}.
 * Rows fold to the latest sample per site and target ({@link NwssDetectionParser}); a detection flip
 * ({@code detected == true}) is significant, so rules enter on it. Geometry is the county interior point from
 * {@code county_fips}. WastewaterSCAN rows are CC BY-NC 4.0 and dropped unless {@code includeWastewaterScan} is
 * set.
 */
@Component
public class NwssDetectionAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "cdc-nwss-detections";

    /** The record kind. */
    public static final String KIND = NwssDetectionParser.KIND;

    /** The production upstream. */
    public static final String DEFAULT_BASE_URL = "https://data.cdc.gov";

    /** Test-only override of {@link #DEFAULT_BASE_URL}. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.cdc.base-url";

    /** The optional Socrata app token. */
    public static final String APP_TOKEN = "appToken";

    /** The {@code pathogens} parameter. */
    public static final String PATHOGENS_PARAM = "pathogens";

    /** Pathogen alias to dataset id, in the order the prompt names them. */
    public static final Map<String, String> DATASETS = Map.of(
            "h5", "mtpu-urpp",
            "measles", "akvg-8vrb",
            "mpox", "xpxn-rzgz",
            "influenza-a", "ymmh-divb",
            "rsv", "45cq-cw4i",
            "sars-cov-2", "j9g8-acpt");

    /** The default pathogens. */
    public static final List<String> DEFAULT_PATHOGENS = List.of("h5", "measles", "mpox");

    /** The selectable pathogens, in a stable order. */
    public static final List<String> ALL_PATHOGENS = List.of("h5", "measles", "mpox", "influenza-a", "rsv",
            "sars-cov-2");

    /** Raw retention: detections are the {@code event} analytic class (Conventions), 1825 days. */
    static final Duration RETENTION = Duration.ofDays(1825);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    /** The state key of the index of the dataset the next run reads first (round-robin across runs). */
    static final String NEXT_DATASET_KEY = "nwss-det:next";

    /** The most current heads read to reject a re-dated older sample. */
    static final int CURRENT_LIMIT = 100_000;

    private final SodaClient soda;
    private final NwssGeometry geometry;
    private final Supplier<CurrentRecords> current;

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means {@value #DEFAULT_BASE_URL}
     * @param counties the 040 county outlines for geometry
     * @param current this source's current heads, to reject a re-dated older sample; resolved lazily
     */
    @Autowired
    public NwssDetectionAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            CountyGeometries counties, ObjectProvider<CurrentRecords> current) {
        this(new SodaClient(baseUrlOverride == null || baseUrlOverride.isBlank()
                ? DEFAULT_BASE_URL : baseUrlOverride.trim()), counties,
                current == null ? () -> null : (Supplier<CurrentRecords>) current::getIfAvailable);
    }

    /** Creates the adapter without access to current heads (tests). */
    public NwssDetectionAdapter(String baseUrlOverride, CountyGeometries counties) {
        this(new SodaClient(baseUrlOverride == null || baseUrlOverride.isBlank()
                ? DEFAULT_BASE_URL : baseUrlOverride.trim()), counties, () -> null);
    }

    /** Creates the adapter with a prebuilt reader (tests). */
    public NwssDetectionAdapter(SodaClient soda, CountyGeometries counties) {
        this(soda, counties, () -> null);
    }

    /** Creates the adapter with a prebuilt reader and a current-heads view (tests). */
    public NwssDetectionAdapter(SodaClient soda, CountyGeometries counties, Supplier<CurrentRecords> current) {
        this.soda = soda;
        this.geometry = new NwssGeometry(counties);
        this.current = current == null ? () -> null : current;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** Refuses an empty pathogen list or an unknown pathogen. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        List<String> pathogens = params.has(PATHOGENS_PARAM) ? params.stringList(PATHOGENS_PARAM) : List.of();
        if (pathogens.isEmpty()) {
            return Map.of();
        }
        for (String pathogen : pathogens) {
            if (pathogen == null || !DATASETS.containsKey(pathogen.strip().toLowerCase(java.util.Locale.ROOT))) {
                return Map.of(PATHOGENS_PARAM, "'" + pathogen + "' is not one of " + ALL_PATHOGENS);
            }
        }
        return Map.of();
    }

    /**
     * Reads the configured pathogens' datasets within the per-minute request budget and emits one record per site
     * and target: its latest sample. The datasets share the budget: each gets an equal number of pages, and the
     * run starts with the dataset after the last one the previous run reached, so every dataset is read even when
     * there are more datasets than requests; each dataset's own cursor resumes what a run did not finish. A sample
     * older than the one already stored for its site and target (a re-dated older sample) is dropped.
     *
     * @throws AdapterException when a request fails
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        List<String> pathogens = pathogens(ctx.params());
        boolean includeWastewaterScan = ctx.params().bool("includeWastewaterScan", false);
        String token = ctx.credentials() == null ? null : ctx.credentials().get(APP_TOKEN);
        Map<String, NormalizedRecord> out = new LinkedHashMap<>();
        if (pathogens.isEmpty()) {
            return FetchResult.of(List.of());
        }
        int allowance = NwssWvalAdapter.requestsPerRun(descriptor());
        int perDataset = Math.max(1, allowance / pathogens.size());
        int start = Math.floorMod(parseInt(ctx.state().get(NEXT_DATASET_KEY).orElse("0")), pathogens.size());
        Map<String, Instant> stored = storedSamples();
        int reached = 0;
        int stale = 0;
        while (reached < pathogens.size() && allowance > 0) {
            String pathogen = pathogens.get((start + reached) % pathogens.size());
            String dataset = DATASETS.get(pathogen);
            String cursorKey = cursorKey(dataset);
            SodaClient.Cursor cursor = SodaClient.Cursor.load(ctx.state(), cursorKey);
            SodaClient.Batch batch = soda.read(ctx, dataset, cursor, token, Math.min(perDataset, allowance));
            allowance -= batch.requests();
            reached++;
            NwssDetectionParser.Result parsed =
                    NwssDetectionParser.parse(batch.rows(), new NwssDetectionParser.Options(includeWastewaterScan));
            for (NwssDetectionParser.Detection detection : parsed.detections()) {
                Instant storedSample = stored.get(detection.key());
                if (storedSample != null && detection.sampleCollectDate().isBefore(storedSample)) {
                    stale++;
                    continue;
                }
                Point point = geometry.forCounty(detection.countyFips());
                out.putIfAbsent(detection.key(), NwssDetectionParser.toRecord(detection, point));
            }
            batch.next().store(ctx.state(), cursorKey);
            ctx.log().debug("{} {}: {} detection(s) from {} row(s) in {} request(s), {} skipped, {} WastewaterSCAN"
                    + " skipped{}", ID, pathogen, parsed.detections().size(), parsed.rows(), batch.requests(),
                    parsed.skipped(), parsed.wastewaterScanSkipped(), batch.exhausted() ? "" : "; more next run");
        }
        ctx.state().put(NEXT_DATASET_KEY, Integer.toString((start + reached) % pathogens.size()));
        if (stale > 0) {
            ctx.log().debug("{}: {} sample(s) older than the stored one dropped", ID, stale);
        }
        return FetchResult.of(new ArrayList<>(out.values()));
    }

    /** The sample date of every stored detection, by external id; empty without a current-heads view. */
    private Map<String, Instant> storedSamples() {
        CurrentRecords view = current.get();
        if (view == null) {
            return Map.of();
        }
        Map<String, Instant> samples = new HashMap<>();
        for (CurrentRecords.Row row : view.current(List.of(ID), List.of(KIND), CURRENT_LIMIT)) {
            Object sample = row.payload() == null ? null : row.payload().get("sampleCollectDate");
            Instant instant = sample == null ? null : CdcDates.instant(sample.toString());
            if (instant != null) {
                samples.put(row.externalId(), instant);
            }
        }
        return samples;
    }

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value.strip());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** The configured pathogens, lower-cased and de-duplicated; the defaults when none are set. */
    static List<String> pathogens(ValidatedParams params) {
        List<String> configured = params.has(PATHOGENS_PARAM) ? params.stringList(PATHOGENS_PARAM) : List.of();
        if (configured.isEmpty()) {
            return DEFAULT_PATHOGENS;
        }
        List<String> out = new ArrayList<>();
        for (String pathogen : configured) {
            String lower = pathogen == null ? null : pathogen.strip().toLowerCase(java.util.Locale.ROOT);
            if (lower != null && DATASETS.containsKey(lower) && !out.contains(lower)) {
                out.add(lower);
            }
        }
        return out;
    }

    static String cursorKey(String dataset) {
        return "nwss-det:" + dataset + ":cursor";
    }


    static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("CDC NWSS wastewater detections")
                .description("CDC National Wastewater Surveillance System detections of select pathogens (H5,"
                        + " measles, mpox by default; Influenza A, RSV and SARS-CoV-2 selectable) from the current"
                        + " per-pathogen datasets. Each row folds to the latest sample per site and target; a"
                        + " detection flip is significant, so a rule enters on detected == true. Every record is"
                        + " public-domain US government data except WastewaterSCAN rows, which are CC BY-NC 4.0"
                        + " and dropped unless includeWastewaterScan is set. Placed at the county interior point"
                        + " (geometryApproximate).")
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
                .param(new ParamSpec(PATHOGENS_PARAM, ParamType.STRING_LIST,
                        "Pathogens to read. Each maps to a current CDC NWSS detection dataset: " + DATASETS,
                        DEFAULT_PATHOGENS, false, ALL_PATHOGENS))
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
                .titleField("source")
                .searchFields("countyFips", "source")
                .tileProperties("detected", "source")
                .zeroResultsNormal(true)
                .kindDisplay(KIND, new KindDisplay("Wastewater {source} detection", null, "#E8590C", null));
        b.fieldSpec("detected", new FieldSpec(FieldType.BOOLEAN, null, "Detected",
                "True when the target was detected in the sample", null, Set.of()));
        b.fieldSpec("concentration", new FieldSpec(FieldType.NUMBER, null, "Concentration",
                "Average target concentration in the sample, in the unit declared by 'unit'", null, Set.of()));
        b.fieldSpec("unit", new FieldSpec(FieldType.STRING, null, "Concentration unit",
                "The unit of 'concentration' as the CDC writes it", null, Set.of()));
        b.fieldSpec("sampleCollectDate", new FieldSpec(FieldType.TIMESTAMP, null, "Sample collected",
                "When the sample was collected", null, Set.of()));
        b.fieldSpec("countyFips", new FieldSpec(FieldType.STRING, null, "County FIPS",
                "Five-digit county FIPS of the site", null, Set.of()));
        b.fieldSpec("populationServed", new FieldSpec(FieldType.INTEGER, "1", "Population served",
                "Population served by the site", null, Set.of()));
        b.fieldSpec("source", new FieldSpec(FieldType.STRING, null, "Source", "CDC or WastewaterSCAN", null,
                Set.of()));
        b.fieldSpec("geometryApproximate", new FieldSpec(FieldType.BOOLEAN, null, "Approximate geometry",
                "True: the geometry is a county interior point, not a measured site position", null, Set.of()));
        b.fieldSpec("licenceOverride", new FieldSpec(FieldType.STRING, null, "Licence override",
                "Present on WastewaterSCAN rows: CC BY-NC 4.0 (WastewaterSCAN)", null, Set.of()));
        return b.build();
    }
}
