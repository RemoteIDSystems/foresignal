package ai.foresignal.source.adapters.gdelt;

import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.reference.CountryRef;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Coverage;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code gdelt-events}: GDELT 2.0 events as {@code conflict-event} point records (prompt 066).
 *
 * <p><b>Requests</b> (two per run, both to {@code storage.googleapis.com} over HTTPS; the {@code data.gdeltproject.org}
 * CNAME's certificate does not cover that hostname):
 * <ol>
 *   <li>a conditional GET of {@code /data.gdeltproject.org/gdeltv2/lastupdate.txt}; a 304 is "not modified";
 *   <li>a GET of the newest export ZIP, whose URL is rebuilt from the validated file name alone
 *       ({@link LastUpdateParser}); the URL in {@code lastupdate.txt} is never requested.
 * </ol>
 * The ZIP's byte length and MD5 must equal the declared ones, and it is unpacked by {@link ExportZip} (one entry,
 * 200 MB uncompressed cap counted while streaming). Any failure after {@code lastupdate.txt} was read forgets its
 * validators, so the next run does not get a 304 and retries the same file.
 *
 * <p><b>Newest file only:</b> adapter state {@code lastFile} holds the export already ingested; the same file again is
 * {@link FetchResult#unchanged()} with no ZIP request. When the new file is more than 15 minutes after
 * {@code lastFile}, the files in between are skipped: they are counted in {@value #FILES_SKIPPED} and reported as
 * {@code Coverage(expected = files due, actual = 1)}. {@code lastFile} advances only after a successful hand-off: a
 * run stores {@code lastFilePending = {file}@{run time}}, and the next run promotes it when the source's last success
 * is no earlier than that run time, and discards it otherwise (the FIRMS pattern of prompt 065).
 *
 * <p><b>Never fetched:</b> {@code SOURCEURL}. It is stored as the untrusted data field {@code sourceUrl}; page metadata
 * can come from an external EDP provider instead.
 */
@Component
public class GdeltEventsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "gdelt-events";

    /** The record kind. */
    public static final String KIND = GdeltExportParser.KIND;

    /** The production host; the only host this source contacts. */
    public static final String DEFAULT_BASE_URL = "https://storage.googleapis.com";

    /** The bucket path every request is under. */
    public static final String PATH = "/data.gdeltproject.org/gdeltv2/";

    /** Test-only override of {@link #DEFAULT_BASE_URL}. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.gdelt.base-url";

    /** The root codes parameter. */
    public static final String ROOT_CODES_PARAM = "rootCodes";

    /** The minimum mentions parameter. */
    public static final String MIN_MENTIONS_PARAM = "minMentions";

    /** The require-a-point parameter. */
    public static final String REQUIRE_GEO_PARAM = "requireGeo";

    /** The countries parameter (FIPS 10-4). */
    public static final String COUNTRIES_PARAM = "countries";

    /** The skipped-files counter. */
    public static final String FILES_SKIPPED = "foresignal.gdelt.files_skipped";

    /** The publication cadence of export files. */
    public static final Duration FILE_INTERVAL = Duration.ofMinutes(15);

    /** Retention: Conventions "Analytic retention" puts GDELT events in {@code bulk-event}, 90 days. */
    static final Duration RETENTION = Duration.ofDays(90);

    /** The state key of the export already ingested. */
    static final String LAST_FILE = "lastFile";

    /** The state key of the export handed off but not yet known to be ingested. */
    static final String PENDING_FILE = "lastFilePending";

    /** The {@code getConditional} state key of {@code lastupdate.txt}. */
    static final String LASTUPDATE_STATE_KEY = "lastupdate";

    private static final Pattern FIPS = Pattern.compile("^[A-Za-z]{2}$");

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;
    private final Function<String, String> fipsToIso;
    private final Counter filesSkipped;

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means {@value #DEFAULT_BASE_URL}
     * @param countries the prompt 040 country table, for the FIPS→ISO crosswalk; null leaves
     *     {@code actionGeoCountryIso} out
     * @param meters the meter registry; null uses a private one
     */
    @Autowired
    public GdeltEventsAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            CountryCentroids countries, MeterRegistry meters) {
        String chosen = baseUrlOverride == null || baseUrlOverride.isBlank() ? DEFAULT_BASE_URL : baseUrlOverride.trim();
        this.baseUrl = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
        this.fipsToIso = countries == null ? null : code -> countries.byFips(code).map(CountryRef::iso2).orElse(null);
        this.filesSkipped = Counter.builder(FILES_SKIPPED)
                .description("GDELT export files published but never read, because only the newest file is fetched")
                .register(meters == null ? new SimpleMeterRegistry() : meters);
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** Refuses an empty root-code list, a minimum below 1 and a country that is not two letters. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        if (params.has(ROOT_CODES_PARAM) && params.stringList(ROOT_CODES_PARAM).isEmpty()) {
            problems.put(ROOT_CODES_PARAM, "must name at least one CAMEO root code from 01 to 20");
        }
        if (params.has(MIN_MENTIONS_PARAM)) {
            Integer min = params.integer(MIN_MENTIONS_PARAM);
            if (min == null || min < 1) {
                problems.put(MIN_MENTIONS_PARAM, "must be at least 1, but was " + min);
            }
        }
        if (params.has(COUNTRIES_PARAM)) {
            for (String code : params.stringList(COUNTRIES_PARAM)) {
                if (!FIPS.matcher(code).matches()) {
                    problems.put(COUNTRIES_PARAM, "must be 2-letter FIPS 10-4 country codes, but contains '" + code
                            + "'");
                    break;
                }
            }
        }
        return problems;
    }

    /**
     * Reads {@code lastupdate.txt}, and when it names a new export, downloads, verifies and parses it.
     *
     * @throws UpstreamFormatException when {@code lastupdate.txt} or the ZIP is malformed, the ZIP does not match its
     *     declared size or MD5, or more than 5% of its lines are bad
     * @throws AdapterException when the network or the budget fails
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        Instant now = ctx.now();
        promotePending(ctx);
        UpstreamResponse index = ctx.http().getConditional(lastUpdateUri(), LASTUPDATE_STATE_KEY);
        if (index.notModified()) {
            return FetchResult.unchanged();
        }
        try {
            return fetchExport(ctx, index, now);
        } catch (AdapterException | RuntimeException e) {
            SourceHttp.forgetValidators(ctx.state(), LASTUPDATE_STATE_KEY);
            throw e;
        }
    }

    private FetchResult fetchExport(FetchContext ctx, UpstreamResponse index, Instant now) throws AdapterException {
        if (index.truncated()) {
            throw new UpstreamFormatException("GDELT lastupdate.txt exceeded maxResponseBytes", null);
        }
        LastUpdateParser.ExportFile file = LastUpdateParser.export(new String(index.body(), StandardCharsets.UTF_8));
        Optional<String> lastFile = ctx.state().get(LAST_FILE);
        if (lastFile.isPresent() && lastFile.get().equals(file.fileName())) {
            return FetchResult.unchanged();
        }

        UpstreamResponse response = ctx.http().get(exportUri(file.fileName()));
        byte[] zip = response.body();
        if (response.truncated() || zip.length != file.size()) {
            throw new UpstreamFormatException("GDELT export " + file.fileName() + " is " + zip.length
                    + " bytes, but lastupdate.txt declared " + file.size(), null);
        }
        String md5 = md5(zip);
        if (!md5.equals(file.md5())) {
            throw new UpstreamFormatException("GDELT export " + file.fileName() + " has MD5 " + md5
                    + ", but lastupdate.txt declared " + file.md5(), null);
        }
        GdeltExportParser.Result parsed = ExportZip.parse(zip, file.entryName(), ExportZip.MAX_UNCOMPRESSED_BYTES,
                options(ctx.params()), fipsToIso);

        Coverage coverage = Coverage.none();
        Optional<Instant> previous = lastFile.map(GdeltEventsAdapter::timestampOrNull);
        if (previous.isPresent()) {
            int due = filesDue(previous.get(), file.timestamp());
            coverage = Coverage.of(due, 1);
            if (due > 1) {
                filesSkipped.increment(due - 1);
                ctx.log().info("{}: {} export files between {} and {} were skipped (newest file only)",
                        ctx.sourceId(), due - 1, lastFile.get(), file.fileName());
            }
        }
        ctx.state().put(PENDING_FILE, file.fileName() + "@" + now);
        ctx.log().debug("{} {}: {} lines, {} kept, {} filtered, {} malformed", ctx.sourceId(), file.fileName(),
                parsed.rows(), parsed.records().size(), parsed.filtered(), parsed.bad());
        return FetchResult.of(parsed.records()).withCoverage(coverage);
    }

    /** The files published in {@code (previous, current]}: at least 1. Package-visible for tests. */
    static int filesDue(Instant previous, Instant current) {
        long minutes = Duration.between(previous, current).toMinutes();
        long due = minutes / FILE_INTERVAL.toMinutes();
        return (int) Math.max(1, Math.min(Integer.MAX_VALUE, due));
    }

    /** The {@code lastupdate.txt} URL. */
    URI lastUpdateUri() {
        return URI.create(baseUrl + PATH + "lastupdate.txt");
    }

    /**
     * The URL of an export, rebuilt from its validated file name.
     *
     * @throws UpstreamFormatException when {@code fileName} is not {@code {14 digits}.export.CSV.zip}
     */
    URI exportUri(String fileName) {
        if (fileName == null || !LastUpdateParser.EXPORT_FILE.matcher(fileName).matches()) {
            throw new UpstreamFormatException("Not a GDELT export file name", fileName);
        }
        return URI.create(baseUrl + PATH + fileName);
    }

    /** The parser options from the source's parameters, defaults applied. */
    static GdeltExportParser.Options options(ValidatedParams params) {
        List<String> roots = params.has(ROOT_CODES_PARAM) ? params.stringList(ROOT_CODES_PARAM) : List.of();
        Integer min = params.has(MIN_MENTIONS_PARAM) ? params.integer(MIN_MENTIONS_PARAM) : null;
        boolean requireGeo = params.bool(REQUIRE_GEO_PARAM, true);
        Set<String> countries = new LinkedHashSet<>();
        if (params.has(COUNTRIES_PARAM)) {
            for (String code : params.stringList(COUNTRIES_PARAM)) {
                countries.add(code.toUpperCase(Locale.ROOT));
            }
        }
        return new GdeltExportParser.Options(new LinkedHashSet<>(roots), min == null ? 1 : min, requireGeo, countries);
    }

    /**
     * Promotes a pending file to {@code lastFile} when the source's last success covers the run that handed it off;
     * otherwise discards it and forgets the {@code lastupdate.txt} validators, so the file is fetched again.
     */
    static void promotePending(FetchContext ctx) {
        AdapterStateStore state = ctx.state();
        Optional<String> pending = state.get(PENDING_FILE);
        if (pending.isEmpty()) {
            return;
        }
        state.remove(PENDING_FILE);
        String[] parts = pending.get().split("@", 2);
        Instant runAt = parts.length == 2 ? parse(parts[1]) : null;
        Optional<Instant> lastSuccess = ctx.lastSuccessAt();
        boolean valid = LastUpdateParser.EXPORT_FILE.matcher(parts[0]).matches();
        if (valid && runAt != null && lastSuccess.isPresent() && !lastSuccess.get().isBefore(runAt)) {
            state.put(LAST_FILE, parts[0]);
        } else {
            SourceHttp.forgetValidators(state, LASTUPDATE_STATE_KEY);
        }
    }

    /** The lowercase hex MD5 of {@code bytes}. */
    static String md5(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("MD5").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("The JDK has no MD5", e);
        }
    }

    private static Instant timestampOrNull(String fileName) {
        try {
            return LastUpdateParser.timestamp(fileName);
        } catch (UpstreamFormatException e) {
            return null;
        }
    }

    private static Instant parse(String text) {
        try {
            return Instant.parse(text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID)
                .name("GDELT events")
                .description("GDELT 2.0 events machine-coded from world news, read from the newest 15-minute export"
                        + " file each run. Keeps CAMEO root codes 13 (Threaten) to 20 (Use unconventional mass"
                        + " violence) by default. An event is a news report, not a confirmed occurrence, and the same"
                        + " event can appear more than once.")
                .kind(KIND)
                .mode(Mode.POLLING)
                .homepage("https://www.gdeltproject.org")
                .attribution("The GDELT Project (https://www.gdeltproject.org)")
                .licence("GDELT: unrestricted use and redistribution with citation and a link")
                .termsUrl("https://www.gdeltproject.org/about.html")
                .param(new ParamSpec(ROOT_CODES_PARAM, ParamType.STRING_LIST, "CAMEO root codes kept, two digits each"
                        + " (01 to 20). The default keeps 13 Threaten, 14 Protest, 15 Exhibit force posture, 16 Reduce"
                        + " relations, 17 Coerce, 18 Assault, 19 Fight and 20 Use unconventional mass violence.",
                        GdeltExportParser.Options.DEFAULT_ROOT_CODES, false, CameoRoots.CODES))
                .param(ParamSpec.optional(MIN_MENTIONS_PARAM, ParamType.INTEGER, "Smallest NumMentions kept (at least"
                        + " 1).", 1))
                .param(ParamSpec.optional(REQUIRE_GEO_PARAM, ParamType.BOOLEAN, "Keep only events with an ActionGeo"
                        + " point. When false, events without one are stored with no geometry.", true))
                .param(ParamSpec.optional(COUNTRIES_PARAM, ParamType.STRING_LIST, "ActionGeo countries kept, as"
                        + " 2-letter FIPS 10-4 codes (GDELT's codes, for example UK, IS, KS); empty keeps every country.",
                        null))
                .interval(FILE_INTERVAL, FILE_INTERVAL)
                .defaultBudget(Budget.of(4, 400))
                .maxResponseBytes(64 * 1024 * 1024)
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(Duration.ofHours(24))
                .defaultRetention(RETENTION)
                .kindRetention(KIND, RETENTION)
                .zeroResultsNormal(false)
                .embedFields("summary")
                .titleField("summary")
                .searchFields("summary", "actionGeoName", "actor1Name", "actor2Name")
                .rankField("numArticles")
                .tileProperties("eventRootCode", "quadClass", "goldstein", "numArticles", "geoPrecision");
        builder.fieldSpec("summary", new FieldSpec(FieldType.STRING, null, "Summary",
                "Actor 1, CAMEO root, actor 2 and place, built from GDELT's codes and names only", null, Set.of()));
        builder.fieldSpec("eventRootCode", new FieldSpec(FieldType.STRING, null, "CAMEO root code",
                "Two digits, 01 to 20", null, Set.of()));
        builder.fieldSpec("cameoRoot", new FieldSpec(FieldType.STRING, null, "CAMEO root", null, null, Set.of()));
        builder.fieldSpec("quadClass", new FieldSpec(FieldType.INTEGER, null, "Quad class",
                "1 verbal cooperation, 2 material cooperation, 3 verbal conflict, 4 material conflict", null,
                Set.of()));
        builder.fieldSpec("goldstein", new FieldSpec(FieldType.NUMBER, null, "Goldstein scale",
                "Theoretical impact of the event type, -10 to +10", null, Set.of()));
        builder.fieldSpec("numMentions", new FieldSpec(FieldType.INTEGER, null, "Mentions", null, null, Set.of()));
        builder.fieldSpec("numSources", new FieldSpec(FieldType.INTEGER, null, "Sources", null, null, Set.of()));
        builder.fieldSpec("numArticles", new FieldSpec(FieldType.INTEGER, null, "Articles", null, null, Set.of()));
        builder.fieldSpec("avgTone", new FieldSpec(FieldType.NUMBER, null, "Average tone",
                "Tone of the documents mentioning the event, -100 to +100, usually -10 to +10", null, Set.of()));
        builder.fieldSpec("geoPrecision", new FieldSpec(FieldType.STRING, null, "Location precision",
                "country, adm1 or city", null, Set.of()));
        builder.fieldSpec("sourceUrl", new FieldSpec(FieldType.STRING, null, "First report",
                "URL or citation of the first report GDELT saw; untrusted, never fetched by Foresignal", null,
                Set.of()));
        return builder.build();
    }
}
