package ai.foresignal.source.adapters.cdc;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CountyGeometries;
import ai.foresignal.source.spi.CredentialField;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

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

    private final SodaClient soda;
    private final NwssGeometry geometry;

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means {@value #DEFAULT_BASE_URL}
     * @param counties the 040 county outlines for geometry
     */
    @Autowired
    public NwssDetectionAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            CountyGeometries counties) {
        this.soda = new SodaClient(baseUrlOverride == null || baseUrlOverride.isBlank()
                ? DEFAULT_BASE_URL : baseUrlOverride.trim());
        this.geometry = new NwssGeometry(counties);
    }

    /** Creates the adapter with a prebuilt reader (tests). */
    public NwssDetectionAdapter(SodaClient soda, CountyGeometries counties) {
        this.soda = soda;
        this.geometry = new NwssGeometry(counties);
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
     * Reads one dataset per configured pathogen and emits one record per site and target.
     *
     * @throws AdapterException when a request fails
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        List<String> pathogens = pathogens(ctx.params());
        boolean includeWastewaterScan = ctx.params().bool("includeWastewaterScan", false);
        String token = ctx.credentials() == null ? null : ctx.credentials().get(APP_TOKEN);
        Map<String, NormalizedRecord> out = new LinkedHashMap<>();
        for (String pathogen : pathogens) {
            String dataset = DATASETS.get(pathogen);
            String cursorKey = cursorKey(dataset);
            String cursor = ctx.state().get(cursorKey).orElse(null);
            JsonNode rows = soda.read(ctx, dataset, cursor, token);
            NwssDetectionParser.Result parsed =
                    NwssDetectionParser.parse(rows, new NwssDetectionParser.Options(includeWastewaterScan));
            for (NwssDetectionParser.Detection detection : parsed.detections()) {
                Point point = geometry.forCounty(detection.countyFips());
                out.putIfAbsent(detection.key(), NwssDetectionParser.toRecord(detection, point));
            }
            advanceCursor(ctx, cursorKey, rows, cursor);
            ctx.log().debug("{} {}: {} detection(s) from {} row(s), {} skipped, {} WastewaterSCAN skipped", ID,
                    pathogen, parsed.detections().size(), parsed.rows(), parsed.skipped(),
                    parsed.wastewaterScanSkipped());
        }
        return FetchResult.of(new ArrayList<>(out.values()));
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

    private void advanceCursor(FetchContext ctx, String cursorKey, JsonNode rows, String cursor) {
        String newest = null;
        Instant newestInstant = null;
        for (JsonNode row : rows) {
            String raw = CdcText.text(row.get("date_updated"));
            Instant instant = CdcDates.instant(raw);
            if (instant != null && (newestInstant == null || instant.isAfter(newestInstant))) {
                newestInstant = instant;
                newest = raw;
            }
        }
        if (newest != null && (cursor == null || newest.compareTo(cursor) > 0)) {
            ctx.state().put(cursorKey, newest);
        }
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
