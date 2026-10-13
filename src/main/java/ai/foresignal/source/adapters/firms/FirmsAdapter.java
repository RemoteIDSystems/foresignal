package ai.foresignal.source.adapters.firms;

// Origin: sequential sources, keyless means no request, and errors as HTML or text rather than CSV, after God's
// Eye View (MIT, Copyright (c) 2026 Bilawal Sidhu): server/providers/firms.js (firmsProxy, fetchSource,
// refreshUpstream), and the 2 h forward slack of src/data/firmsCsv.js (filterTrailing24h). See THIRD_PARTY_NOTICES.md.

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.CredentialsRejectedException;
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
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code nasa-firms}: NASA FIRMS active-fire detections (prompt 065; {@code https://firms.modaps.eosdis.nasa.gov/api/area/}).
 *
 * <p><b>Requests</b>, one product after another and never in parallel: {@code GET
 * /api/area/csv/{mapKey}/{SOURCE}/{area}/1} for each configured product, and between 00:00 and 06:00 UTC also
 * {@code …/1/{yesterday}}, because late NRT granules of yesterday arrive then and day range 1 alone would miss
 * them. {@code area} is {@code world} or {@code west,south,east,north}. Verified 2026-10: DAY_RANGE accepts 1–5,
 * the MAP_KEY allows 5,000 transactions per 10 minutes, larger requests may count as several, and a bad key is
 * answered {@code Invalid MAP_KEY.} (HTTP 400). The {@code mapkey_status} endpoint is not called; the budget and
 * 429/{@code Retry-After} handling of {@code UpstreamClient} protect the quota.
 *
 * <p><b>Keyless means disabled:</b> {@code mapKey} is a required credential, so the source cannot be enabled
 * without it, and a fetch without one throws {@link CredentialsRejectedException} before any request.
 *
 * <p><b>Credential safety:</b> the MAP_KEY is a URL path segment. {@code UpstreamClient} masks the credential
 * values of the source in fetch traces, recorded probe exchanges and the fetch log, and {@code HealthTracker}
 * masks them in stored errors. The adapter adds nothing that could carry it: it never logs a URL, its own
 * exception messages name the product but not the request, and a non-CSV snippet is masked before it is
 * attached. The key must consist of URL-unreserved characters ({@link #checkCredentials}), so the URL holds it
 * verbatim and the redactor, which matches the stored value, always finds it.
 *
 * <p><b>Incremental emit:</b> adapter state {@code hw:{SOURCE}} holds the newest acquisition already handed off.
 * A run emits rows acquired at or after {@code hw − 3 h} (the overlap absorbs out-of-order granules) and within
 * {@code window} of now; rows inside the overlap re-emit identical records, which the writer turns into touches,
 * never new versions. The new mark is written as {@code hwPending:{SOURCE}} with the run time, and promoted to
 * {@code hw:{SOURCE}} at the start of the next run only if the source's last success is no earlier than that run,
 * so the mark advances only after the batch was handed off and ingested. A failed run's pending mark is
 * discarded.
 *
 * <p><b>Validation:</b> a body (or an error response) containing {@code Invalid MAP_KEY} is a
 * {@link CredentialsRejectedException}; any other body that is not FIRMS CSV (first character {@code <}, or a
 * header without {@code latitude, longitude, acq_date, acq_time}) is an {@link UpstreamFormatException} with a
 * masked snippet of at most 500 characters. A header-only CSV is zero rows and valid.
 */
@Component
public class FirmsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "nasa-firms";

    /** The record kind. */
    public static final String KIND = FirmsCsvParser.KIND;

    /** The production host. */
    public static final String DEFAULT_BASE_URL = "https://firms.modaps.eosdis.nasa.gov";

    /** Test-only override of {@link #DEFAULT_BASE_URL}. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.firms.base-url";

    /** The MAP_KEY credential. */
    public static final String MAP_KEY = "mapKey";

    /** The products parameter. */
    public static final String SOURCES_PARAM = "sources";

    /** The area parameter. */
    public static final String AREA_PARAM = "area";

    /** The window parameter. */
    public static final String WINDOW_PARAM = "window";

    /** Every product the parameter accepts (verified 2026-10; NOAA-21 has no standard-processing product). */
    public static final List<String> ALL_SOURCES =
            List.of("VIIRS_NOAA20_NRT", "VIIRS_NOAA21_NRT", "VIIRS_SNPP_NRT", "MODIS_NRT", "LANDSAT_NRT");

    /** The default products: the three VIIRS satellites and MODIS. */
    public static final List<String> DEFAULT_SOURCES = List.copyOf(ALL_SOURCES.subList(0, 4));

    /** The default area, the world. */
    public static final String DEFAULT_AREA = "-180,-90,180,90";

    /** The default and bounds of {@code window}. */
    public static final Duration DEFAULT_WINDOW = Duration.ofHours(24);

    static final Duration MIN_WINDOW = Duration.ofHours(1);
    static final Duration MAX_WINDOW = Duration.ofHours(48);

    /** The re-emit overlap behind the high-water mark. */
    public static final Duration OVERLAP = Duration.ofHours(3);

    /** How far after now an acquisition may lie (upstream and local clock skew; God's Eye View uses 2 h). */
    static final Duration FORWARD_SLACK = Duration.ofHours(2);

    /** Before this hour (UTC) yesterday is fetched as well. */
    static final int CATCH_UP_HOURS = 6;

    /** The state key prefix of the high-water mark. */
    static final String HW_PREFIX = "hw:";

    /** The state key prefix of a mark waiting for its batch to be ingested. */
    static final String PENDING_PREFIX = "hwPending:";

    /** Retention: Weather-Imagery-Design §2.4 keeps {@code fire-detection} 7 days. */
    static final Duration RETENTION = Duration.ofDays(7);

    /** A MAP_KEY the URL can carry verbatim (URL-unreserved characters) and the redactor can recognize. */
    static final Pattern KEY_PATTERN = Pattern.compile("^[A-Za-z0-9._~-]{8,128}$");

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means {@value #DEFAULT_BASE_URL}
     */
    @Autowired
    public FirmsAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        String chosen = baseUrlOverride == null || baseUrlOverride.isBlank() ? DEFAULT_BASE_URL : baseUrlOverride.trim();
        this.baseUrl = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /**
     * Refuses a product list that is empty or repeats a product, a window outside 1–48 h and an area that is
     * empty.
     */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        if (params.has(SOURCES_PARAM)) {
            List<String> sources = params.stringList(SOURCES_PARAM);
            if (sources.isEmpty()) {
                problems.put(SOURCES_PARAM, "must name at least one of " + ALL_SOURCES);
            } else if (new HashSet<>(sources).size() != sources.size()) {
                problems.put(SOURCES_PARAM, "must not repeat a product, but was " + sources);
            }
        }
        if (params.has(WINDOW_PARAM)) {
            Duration window = params.duration(WINDOW_PARAM);
            if (window == null || window.compareTo(MIN_WINDOW) < 0 || window.compareTo(MAX_WINDOW) > 0) {
                problems.put(WINDOW_PARAM, "must be between 1h and 48h, but was " + window);
            }
        }
        if (params.has(AREA_PARAM)) {
            Envelope area = params.bbox(AREA_PARAM);
            if (area == null || area.isNull() || area.getWidth() <= 0 || area.getHeight() <= 0) {
                problems.put(AREA_PARAM, "must be a box with a non-zero width and height (west,south,east,north)");
            }
        }
        return problems;
    }

    /** Refuses a MAP_KEY the URL could not carry verbatim, so the redactor always finds it. */
    @Override
    public Map<String, String> checkCredentials(Map<String, String> values) {
        String key = values == null ? null : values.get(MAP_KEY);
        if (key != null && !KEY_PATTERN.matcher(key).matches()) {
            return Map.of(MAP_KEY, "must be 8 to 128 letters, digits or . _ ~ - (the MAP_KEY FIRMS e-mailed you)");
        }
        return Map.of();
    }

    /**
     * Fetches every configured product in order, then returns the detections the high-water marks let through.
     *
     * @throws CredentialsRejectedException when there is no usable MAP_KEY (no request is made) or FIRMS refuses it
     * @throws UpstreamFormatException when FIRMS answers with something that is not FIRMS CSV
     * @throws AdapterException when the network or the budget fails
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        String key = ctx.credentials().get(MAP_KEY);
        if (key == null || key.isBlank()) {
            throw new CredentialsRejectedException("nasa-firms has no MAP_KEY; no request was made");
        }
        if (!KEY_PATTERN.matcher(key).matches()) {
            throw new CredentialsRejectedException("nasa-firms has an unusable MAP_KEY (it must be 8 to 128 letters,"
                    + " digits or . _ ~ -); no request was made");
        }
        Instant now = ctx.now();
        promotePending(ctx);
        List<String> sources = sources(ctx.params());
        String area = areaSegment(ctx.params());
        Instant windowStart = now.minus(window(ctx.params()));
        Instant ceiling = now.plus(FORWARD_SLACK);

        Map<String, NormalizedRecord> out = new LinkedHashMap<>();
        for (String source : sources) {
            Optional<Instant> hw = instant(ctx.state(), HW_PREFIX + source);
            Instant from = hw.map(mark -> later(windowStart, mark.minus(OVERLAP))).orElse(windowStart);
            Instant newest = null;
            int rows = 0;
            int skipped = 0;
            int badCoordinates = 0;
            int emitted = 0;
            for (URI uri : uris(key, source, area, now)) {
                byte[] body = get(ctx, uri, source, key);
                FirmsCsvParser.Result parsed = FirmsCsvParser.parse(source, body, from, ceiling);
                rows += parsed.rows();
                skipped += parsed.skipped();
                badCoordinates += parsed.badCoordinates();
                for (NormalizedRecord record : parsed.records()) {
                    if (out.putIfAbsent(record.externalId(), record) == null) {
                        emitted++;
                    }
                    if (newest == null || record.observedAt().isAfter(newest)) {
                        newest = record.observedAt();
                    }
                }
            }
            if (newest != null && hw.map(newest::isAfter).orElse(true)) {
                ctx.state().put(PENDING_PREFIX + source, newest + "@" + now);
            } else {
                ctx.state().remove(PENDING_PREFIX + source);
            }
            ctx.log().debug("{} {}: {} rows, {} emitted from {}, {} malformed, {} with bad coordinates",
                    ctx.sourceId(), source, rows, emitted, from, skipped, badCoordinates);
        }
        return FetchResult.of(new ArrayList<>(out.values()));
    }

    /**
     * The request URLs of one product for a run at {@code now}: day range 1, and between 00:00 and 06:00 UTC also
     * yesterday's date. Package-visible for tests.
     */
    List<URI> uris(String key, String source, String area, Instant now) {
        String base = baseUrl + "/api/area/csv/" + key + "/" + source + "/" + area + "/1";
        List<URI> uris = new ArrayList<>(2);
        uris.add(URI.create(base));
        if (now.atZone(ZoneOffset.UTC).getHour() < CATCH_UP_HOURS) {
            LocalDate yesterday = now.atZone(ZoneOffset.UTC).toLocalDate().minusDays(1);
            uris.add(URI.create(base + "/" + yesterday));
        }
        return uris;
    }

    /**
     * The area path segment: {@code world} for the default or a world box, else {@code west,south,east,north}
     * with plain numbers.
     */
    static String areaSegment(ValidatedParams params) {
        Envelope area = params.has(AREA_PARAM) ? params.bbox(AREA_PARAM) : null;
        if (area == null || area.isNull()
                || (area.getMinX() <= -180 && area.getMinY() <= -90 && area.getMaxX() >= 180 && area.getMaxY() >= 90)) {
            return "world";
        }
        return plain(area.getMinX()) + "," + plain(area.getMinY()) + "," + plain(area.getMaxX()) + ","
                + plain(area.getMaxY());
    }

    /** The configured products, in their configured order. */
    static List<String> sources(ValidatedParams params) {
        List<String> configured = params.has(SOURCES_PARAM) ? params.stringList(SOURCES_PARAM) : List.of();
        return configured.isEmpty() ? DEFAULT_SOURCES : configured;
    }

    /** The configured window, clamped to 1–48 h. */
    static Duration window(ValidatedParams params) {
        Duration window = params.has(WINDOW_PARAM) ? params.duration(WINDOW_PARAM) : null;
        if (window == null) {
            return DEFAULT_WINDOW;
        }
        if (window.compareTo(MIN_WINDOW) < 0) {
            return MIN_WINDOW;
        }
        return window.compareTo(MAX_WINDOW) > 0 ? MAX_WINDOW : window;
    }

    /**
     * Promotes each pending mark whose run the source's last success covers, and discards the others (their batch
     * was never ingested).
     */
    static void promotePending(FetchContext ctx) {
        Optional<Instant> lastSuccess = Optional.empty();
        boolean looked = false;
        for (String source : ALL_SOURCES) {
            Optional<String> pending = ctx.state().get(PENDING_PREFIX + source);
            if (pending.isEmpty()) {
                continue;
            }
            if (!looked) {
                lastSuccess = ctx.lastSuccessAt();
                looked = true;
            }
            ctx.state().remove(PENDING_PREFIX + source);
            String[] parts = pending.get().split("@", 2);
            Instant mark = parse(parts[0]);
            Instant runAt = parts.length == 2 ? parse(parts[1]) : null;
            if (mark == null || runAt == null || lastSuccess.isEmpty() || lastSuccess.get().isBefore(runAt)) {
                continue;
            }
            Optional<Instant> current = instant(ctx.state(), HW_PREFIX + source);
            if (current.isEmpty() || mark.isAfter(current.get())) {
                ctx.state().put(HW_PREFIX + source, mark.toString());
            }
        }
    }

    /** One GET, classified: a refused key, a non-CSV body and a truncated body are errors. */
    private static byte[] get(FetchContext ctx, URI uri, String source, String key) throws AdapterException {
        UpstreamResponse response;
        try {
            response = ctx.http().get(uri);
        } catch (AdapterException e) {
            if (FirmsCsvParser.isInvalidKey(e.getMessage())) {
                throw new CredentialsRejectedException("FIRMS refused the MAP_KEY for " + source + " (HTTP "
                        + e.httpStatus().orElse(0) + ": Invalid MAP_KEY)");
            }
            throw e;
        }
        byte[] body = response.body();
        String head = FirmsCsvParser.head(body);
        if (FirmsCsvParser.isInvalidKey(head)) {
            throw new CredentialsRejectedException("FIRMS refused the MAP_KEY for " + source + " (Invalid MAP_KEY)");
        }
        if (!FirmsCsvParser.isLikelyCsv(head)) {
            throw new UpstreamFormatException("FIRMS returned a response for " + source + " that is not FIRMS CSV",
                    mask(head, key));
        }
        if (response.truncated()) {
            throw new UpstreamFormatException("The FIRMS " + source + " response exceeded maxResponseBytes; set a"
                    + " smaller area", null);
        }
        return body;
    }

    /** {@code text} with every occurrence of {@code key} masked, as {@code UrlRedactor} masks it. */
    static String mask(String text, String key) {
        return text == null || key == null || key.isEmpty() ? text : text.replace(key, "***");
    }

    private static Optional<Instant> instant(AdapterStateStore state, String key) {
        return state.get(key).map(FirmsAdapter::parse);
    }

    private static Instant parse(String text) {
        try {
            return text == null ? null : Instant.parse(text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static Instant later(Instant a, Instant b) {
        return a.isAfter(b) ? a : b;
    }

    private static String plain(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID)
                .name("NASA FIRMS active fires")
                .description("NASA FIRMS near-real-time active-fire detections from VIIRS (NOAA-20, NOAA-21, Suomi-NPP),"
                        + " MODIS and optionally Landsat (US and Canada only), one record per detection pixel. Needs your"
                        + " own free FIRMS MAP_KEY. Fetched incrementally: each run emits only detections newer than"
                        + " three hours before the newest one already emitted.")
                .kind(KIND)
                .mode(Mode.POLLING)
                .homepage("https://firms.modaps.eosdis.nasa.gov")
                .attribution("NASA FIRMS")
                .licence("NASA open data (cite NASA LANCE FIRMS)")
                .termsUrl("https://www.earthdata.nasa.gov/data/tools/firms")
                .credential(CredentialField.required(MAP_KEY, "Your FIRMS MAP_KEY, requested free with"
                        + " \"Get MAP_KEY\" at https://firms.modaps.eosdis.nasa.gov/api/area/ (5,000 transactions per 10"
                        + " minutes)"))
                .param(new ParamSpec(SOURCES_PARAM, ParamType.STRING_LIST, "FIRMS products, fetched one after another"
                        + " in this order. LANDSAT_NRT covers the US and Canada only.", DEFAULT_SOURCES, false,
                        ALL_SOURCES))
                .param(ParamSpec.optional(AREA_PARAM, ParamType.BBOX, "Area as west,south,east,north (default the"
                        + " world). A smaller area downloads far fewer bytes; it must not cross the antimeridian. A small"
                        + " box can legitimately have no fires, which this source reports as a warning"
                        + " (zeroResultsNormal is false).", DEFAULT_AREA))
                .param(ParamSpec.optional(WINDOW_PARAM, ParamType.DURATION, "Oldest acquisition emitted, relative to"
                        + " now (1h to 48h). Each run fetches today, plus yesterday before 06:00 UTC.", "24h"))
                .interval(Duration.ofMinutes(10), Duration.ofMinutes(30))
                .defaultBudget(Budget.of(6, 600, 1500))
                .fetchTimeout(Duration.ofSeconds(120))
                .maxResponseBytes(64 * 1024 * 1024)
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(Duration.ofHours(24))
                .defaultRetention(RETENTION)
                .kindRetention(KIND, RETENTION)
                .zeroResultsNormal(false)
                .tileProperties("frpMw", "confidenceClass", "satellite", "daynight")
                .rankField("frpMw")
                .kindDisplay(KIND, new KindDisplay("Fire {frpMw} MW", "fire", "#F76707", null, null, "frpMw", null));
        builder.fieldSpec("frpMw", new FieldSpec(FieldType.NUMBER, "MW", "Fire radiative power",
                "Pixel-integrated fire radiative power (the FIRMS frp column)", null, Set.of()));
        builder.fieldSpec("brightK", new FieldSpec(FieldType.NUMBER, "K", "Brightness temperature",
                "VIIRS I-4 (bright_ti4) or MODIS channel 21/22 (brightness)", null, Set.of()));
        builder.fieldSpec("bright2K", new FieldSpec(FieldType.NUMBER, "K", "Second brightness temperature",
                "VIIRS I-5 (bright_ti5) or MODIS channel 31 (bright_t31)", null, Set.of()));
        builder.fieldSpec("scanKm", new FieldSpec(FieldType.NUMBER, "km", "Along-scan pixel size", null, null,
                Set.of()));
        builder.fieldSpec("trackKm", new FieldSpec(FieldType.NUMBER, "km", "Along-track pixel size", null, null,
                Set.of()));
        builder.fieldSpec("confidenceClass", new FieldSpec(FieldType.STRING, null, "Confidence",
                "low, nominal or high (VIIRS letters; MODIS below 30, 30 to 79, 80 and above)", null, Set.of()));
        builder.fieldSpec("satellite", new FieldSpec(FieldType.STRING, null, "Satellite",
                "As FIRMS sends it: N, N20 or N21 for VIIRS, A or T for MODIS", null, Set.of()));
        builder.fieldSpec("daynight", new FieldSpec(FieldType.STRING, null, "Day or night", "D or N", null,
                Set.of()));
        return builder.build();
    }
}
