package ai.foresignal.source.adapters.inat;

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
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PlacePoints;
import ai.foresignal.source.spi.PlaceTimeZones;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code inat-observations} (prompt 095): iNaturalist v1 observations in an operator's area, as
 * {@code nature-observation} records ({@link InatParser}).
 *
 * <p><strong>An AOI is required.</strong> iNaturalist explicitly asks clients not to poll globally, and the
 * {@code aoi} parameter (a {@code west,south,east,north} box or a 036 place id) is required, so the source
 * cannot be enabled without one.
 *
 * <p><strong>Requests per run fit the budget</strong> (2026-10 review). One {@code updated_since} request for
 * revisions (quality-grade changes, re-identifications) and an ascending {@code id_above} walk for new
 * observations, each limited to the AOI, together send at most as many requests as the descriptor's per-minute
 * budget allows (2 by default, so one page of new observations and one of revisions; never more than
 * {@value #MAX_PAGES} pages). A walk that does not finish resumes from the stored {@code id_above} cursor on the
 * next run, and a request the budget or the network refuses part-way ends the run with what was already read
 * instead of failing it. {@code id_above}, the revision watermark and the {@code Cache-Control: max-age}
 * freshness are kept in adapter state; a run inside the upstream's {@code max-age} sends nothing.
 *
 * <p><strong>Privacy</strong> (Core-Data-Model §3, prompt 039): obscured and geoprivacy-private observations
 * are handled by {@link InatParser}; contributor usernames, descriptions and comments are never read.
 *
 * <p><strong>API version seam.</strong> iNaturalist v2 is preferred upstream and needs an explicit
 * {@code fields=}; v1 has no retirement date. This adapter implements v1 behind {@link ApiVersion} so v2 can be
 * added without changing the rest.
 */
@Component
public class InatObservationsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "inat-observations";

    /** The record kind. */
    public static final String KIND = InatParser.KIND;

    /** The production upstream. */
    public static final String DEFAULT_BASE_URL = "https://api.inaturalist.org";

    /** Test-only override of {@link #DEFAULT_BASE_URL}. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.inat.base-url";

    /** The {@code aoi} parameter: a bbox or a place id. */
    public static final String AOI_PARAM = "aoi";

    /** The {@code qualityGrade} parameter. */
    public static final String QUALITY_GRADE_PARAM = "qualityGrade";

    /** The {@code iconicTaxa} parameter. */
    public static final String ICONIC_TAXA_PARAM = "iconicTaxa";

    /** The {@code licensedOnly} parameter. */
    public static final String LICENSED_ONLY_PARAM = "licensedOnly";

    /** The {@code includeCaptive} parameter. */
    public static final String INCLUDE_CAPTIVE_PARAM = "includeCaptive";

    /** The version API path. */
    static final String PATH = "/v1/observations";

    /** The state key of the newest observation id handed off. */
    static final String MAX_ID_KEY = "inat:maxId";

    /** The state key of the last run time, the next {@code updated_since}. */
    static final String LAST_RUN_KEY = "inat:lastRun";

    /** The state key of the instant until which the last response is fresh ({@code Cache-Control: max-age}). */
    static final String FRESH_UNTIL_KEY = "inat:freshUntil";

    private static final Pattern MAX_AGE = Pattern.compile("(?:^|[,\\s])max-age\\s*=\\s*(\\d+)");

    /** The page size. */
    public static final int PER_PAGE = 200;

    /** The most pages one new-observation walk reads. */
    public static final int MAX_PAGES = 5;

    /** The half-width of the box built around a place point, degrees. */
    static final double PLACE_DEGREES = 0.25;

    /**
     * The largest AOI side, degrees of longitude or latitude (2026-10 review). iNaturalist asks not to be polled
     * globally; a box this size already covers a large state.
     */
    static final double MAX_AOI_DEGREES = 15.0;

    /** Raw retention: observations are the {@code event} analytic class (Conventions), 1825 days. */
    static final Duration RETENTION = Duration.ofDays(1825);

    private static final Pattern BBOX = Pattern.compile(
            "^\\s*-?\\d+(?:\\.\\d+)?\\s*,\\s*-?\\d+(?:\\.\\d+)?\\s*,\\s*-?\\d+(?:\\.\\d+)?\\s*,\\s*-?\\d+(?:\\.\\d+)?\\s*$");
    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    /**
     * The iNaturalist API generation. v1 is implemented; v2 is documented as preferred upstream.
     */
    public enum ApiVersion {
        /** {@code /v1/observations}; no retirement date. */
        V1,
        /** {@code /v2/observations}; needs an explicit {@code fields=}. */
        V2
    }

    private final String baseUrl;
    private final PlaceTimeZones zones;
    private final Supplier<PlacePoints> places;

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means {@value #DEFAULT_BASE_URL}
     * @param zones the 040 timezone lookup for date-only observations
     * @param places resolves a place AOI to a point
     */
    @Autowired
    public InatObservationsAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<PlaceTimeZones> zones, ObjectProvider<PlacePoints> places) {
        this(baseUrlOverride, zones == null ? PlaceTimeZones.none() : zones.getIfAvailable(PlaceTimeZones::none),
                places == null ? () -> null : (Supplier<PlacePoints>) places::getIfAvailable);
    }

    /** Creates the adapter with plain dependencies (tests). */
    public InatObservationsAdapter(String baseUrlOverride, PlaceTimeZones zones, Supplier<PlacePoints> places) {
        String chosen = baseUrlOverride == null || baseUrlOverride.isBlank() ? DEFAULT_BASE_URL : baseUrlOverride.trim();
        this.baseUrl = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
        this.zones = zones == null ? PlaceTimeZones.none() : zones;
        this.places = places == null ? () -> null : places;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The implemented API generation. */
    public ApiVersion apiVersion() {
        return ApiVersion.V1;
    }

    /** The base URL, without a trailing slash. */
    public String baseUrl() {
        return baseUrl;
    }

    /** Refuses an {@code aoi} that is neither a bbox nor a resolvable place id. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        String aoi = params.string(AOI_PARAM);
        if (aoi == null || aoi.isBlank()) {
            problems.put(AOI_PARAM, "is required: iNaturalist must not be polled globally; give a bbox"
                    + " (west,south,east,north) or a place id");
        } else if (isBbox(aoi)) {
            String problem = bboxProblem(aoi);
            if (problem != null) {
                problems.put(AOI_PARAM, problem);
            }
        } else if (resolvePlace(aoi) == null) {
            problems.put(AOI_PARAM, "'" + aoi + "' is neither a west,south,east,north box nor a known place id");
        }
        return problems;
    }

    /**
     * What is wrong with a {@code west,south,east,north} box, or null when it is usable: out-of-range
     * coordinates, south above north, a box crossing the antimeridian (west east of east; swapping the two would
     * silently turn it into a near-global box), or a side longer than {@value #MAX_AOI_DEGREES}°.
     */
    static String bboxProblem(String aoi) {
        String[] parts = aoi.split(",");
        double west = Double.parseDouble(parts[0].strip());
        double south = Double.parseDouble(parts[1].strip());
        double east = Double.parseDouble(parts[2].strip());
        double north = Double.parseDouble(parts[3].strip());
        if (Math.abs(west) > 180 || Math.abs(east) > 180 || Math.abs(south) > 90 || Math.abs(north) > 90) {
            return "the box is out of range: longitudes are -180..180 and latitudes -90..90";
        }
        if (south >= north) {
            return "south (" + south + ") must be below north (" + north + ")";
        }
        if (west >= east) {
            return "west (" + west + ") must be west of east (" + east + "); a box across the antimeridian is not"
                    + " supported, so split it into two sources";
        }
        if (east - west > MAX_AOI_DEGREES || north - south > MAX_AOI_DEGREES) {
            return "the box is larger than " + MAX_AOI_DEGREES + "° on a side; iNaturalist must not be polled"
                    + " over large areas";
        }
        return null;
    }

    /**
     * Fetches new observations (paged by {@code id_above}) and then one round of revisions.
     *
     * @throws AdapterException when a request fails or the AOI cannot be resolved
     * @throws UpstreamFormatException when a body is truncated or not the iNaturalist envelope
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        ValidatedParams params = ctx.params();
        Envelope box = resolveAoi(params);
        if (box == null) {
            throw new AdapterException(ID + ": the aoi parameter is required and must resolve to a box");
        }
        InatParser.Options options = options(params);
        Instant now = ctx.now();
        Optional<Instant> freshUntil = ctx.state().get(FRESH_UNTIL_KEY).flatMap(InatObservationsAdapter::parseInstant);
        if (freshUntil.isPresent() && now.isBefore(freshUntil.get())) {
            // iNaturalist's Cache-Control: max-age says the last answer is still current.
            return FetchResult.unchanged();
        }
        Map<String, NormalizedRecord> out = new LinkedHashMap<>();
        int allowance = requestsPerRun(ctx.descriptor());
        Instant fresh = null;

        // Revisions first: one request, whose watermark only moves when it was read.
        Optional<Instant> lastRun = ctx.state().get(LAST_RUN_KEY).flatMap(InatObservationsAdapter::parseInstant);
        if (lastRun.isPresent()) {
            UpstreamResponse response =
                    ctx.http().get(revisionUri(box, options, lastRun.get()), Map.of("Accept", "application/json"));
            allowance--;
            if (response.truncated()) {
                throw new UpstreamFormatException("iNaturalist revisions exceeded maxResponseBytes", null);
            }
            InatParser.Result parsed = InatParser.parse(response.body(), options);
            Instant newest = null;
            for (NormalizedRecord record : parsed.records()) {
                putNewest(out, record);
                if (record.observedAt() != null && (newest == null || record.observedAt().isAfter(newest))) {
                    newest = record.observedAt();
                }
            }
            // A full page may have more behind it: continue from its newest update next run, not from now.
            ctx.state().put(LAST_RUN_KEY,
                    (parsed.rows() >= PER_PAGE && newest != null ? newest : now).toString());
            fresh = later(fresh, freshUntil(response, now));
        } else {
            ctx.state().put(LAST_RUN_KEY, now.toString());
        }

        Optional<String> cursor = ctx.state().get(MAX_ID_KEY);
        long maxId = cursor.map(InatObservationsAdapter::parseLong).orElse(0L);
        // At least one page of new observations per run, even when the revisions used the allowance up.
        int pages = Math.min(MAX_PAGES, Math.max(1, allowance));
        for (int page = 0; page < pages; page++) {
            UpstreamResponse response;
            try {
                response = ctx.http().get(idUri(box, options, cursor), Map.of("Accept", "application/json"));
            } catch (AdapterException e) {
                if ((page == 0 && lastRun.isEmpty()) || e.httpStatus().isPresent()) {
                    throw e;
                }
                // Refused by the budget or the network part-way: keep what was read; the cursor resumes it.
                ctx.log().info("{}: stopped after {} page(s): {}", ID, page, e.getMessage());
                break;
            }
            if (response.truncated()) {
                throw new UpstreamFormatException("iNaturalist observations page exceeded maxResponseBytes", null);
            }
            InatParser.Result parsed = InatParser.parse(response.body(), options);
            for (NormalizedRecord record : parsed.records()) {
                putNewest(out, record);
            }
            maxId = Math.max(maxId, parsed.maxId());
            fresh = later(fresh, freshUntil(response, now));
            if (maxId > 0) {
                ctx.state().put(MAX_ID_KEY, Long.toString(maxId));
            }
            if (parsed.rows() < PER_PAGE) {
                break;
            }
            cursor = Optional.of(Long.toString(maxId));
        }
        if (fresh != null) {
            ctx.state().put(FRESH_UNTIL_KEY, fresh.toString());
        }
        ctx.log().debug("{}: {} observation(s) from box {},{},{},{}", ID, out.size(), box.getMinX(), box.getMinY(),
                box.getMaxX(), box.getMaxY());
        return FetchResult.of(new ArrayList<>(out.values()));
    }

    /**
     * The requests one run may send: the descriptor's per-minute budget (the bucket refills fully between the
     * 30-minute runs), at least 1 and at most {@value #MAX_PAGES} + 1.
     */
    static int requestsPerRun(SourceDescriptor descriptor) {
        Budget budget = descriptor.defaultBudget();
        int perMinute = budget == null ? 1 : (int) Math.min(Integer.MAX_VALUE, budget.perMinute());
        return Math.max(1, Math.min(MAX_PAGES + 1, perMinute));
    }

    /** {@code now} plus the response's {@code Cache-Control: max-age}, or null when it sends none. */
    static Instant freshUntil(UpstreamResponse response, Instant now) {
        String cacheControl = response.header("Cache-Control");
        if (cacheControl == null) {
            return null;
        }
        java.util.regex.Matcher m = MAX_AGE.matcher(cacheControl.toLowerCase(Locale.ROOT));
        if (!m.find()) {
            return null;
        }
        try {
            long seconds = Long.parseLong(m.group(1));
            return seconds <= 0 ? null : now.plusSeconds(Math.min(seconds, Duration.ofHours(1).toSeconds()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Instant later(Instant a, Instant b) {
        if (a == null) {
            return b;
        }
        return b == null || a.isAfter(b) ? a : b;
    }

    /** The new-observation request: ascending {@code id_above} once a cursor exists, else newest-first seeding. */
    public URI idUri(Envelope box, InatParser.Options options, Optional<String> cursor) {
        StringBuilder url = new StringBuilder(base(box, options));
        url.append("&order_by=id&order=").append(cursor.isPresent() ? "asc" : "desc");
        cursor.ifPresent(id -> url.append("&id_above=").append(enc(id)));
        return URI.create(url.toString());
    }

    /** The revisions request. */
    public URI revisionUri(Envelope box, InatParser.Options options, Instant lastRun) {
        return URI.create(base(box, options) + "&order_by=updated_at&updated_since="
                + enc(DateTimeFormatter.ISO_INSTANT.format(lastRun)));
    }

    private String base(Envelope box, InatParser.Options options) {
        StringBuilder url = new StringBuilder(baseUrl).append(PATH).append("?swlat=").append(box.getMinY())
                .append("&swlng=").append(box.getMinX()).append("&nelat=").append(box.getMaxY())
                .append("&nelng=").append(box.getMaxX()).append("&per_page=").append(PER_PAGE);
        if (options.qualityGrade() != null) {
            url.append("&quality_grade=").append(enc(options.qualityGrade()));
        }
        if (!options.iconicTaxa().isEmpty()) {
            url.append("&iconic_taxa=").append(enc(String.join(",", options.iconicTaxa())));
        }
        return url.toString();
    }

    private InatParser.Options options(ValidatedParams params) {
        String grade = params.has(QUALITY_GRADE_PARAM) ? params.string(QUALITY_GRADE_PARAM) : "research";
        return new InatParser.Options(grade, params.stringList(ICONIC_TAXA_PARAM),
                params.bool(LICENSED_ONLY_PARAM, true), params.bool(INCLUDE_CAPTIVE_PARAM, false), zones);
    }

    /** Resolves the {@code aoi} parameter to a box, resolving a place id to a small box around its point. */
    Envelope resolveAoi(ValidatedParams params) {
        String aoi = params.string(AOI_PARAM);
        if (aoi == null || aoi.isBlank()) {
            return null;
        }
        if (isBbox(aoi)) {
            if (bboxProblem(aoi) != null) {
                return null;
            }
            String[] parts = aoi.split(",");
            double west = Double.parseDouble(parts[0].strip());
            double south = Double.parseDouble(parts[1].strip());
            double east = Double.parseDouble(parts[2].strip());
            double north = Double.parseDouble(parts[3].strip());
            return new Envelope(west, east, south, north);
        }
        double[] point = resolvePlace(aoi);
        if (point == null) {
            return null;
        }
        return new Envelope(point[0] - PLACE_DEGREES, point[0] + PLACE_DEGREES,
                point[1] - PLACE_DEGREES, point[1] + PLACE_DEGREES);
    }

    private double[] resolvePlace(String placeId) {
        PlacePoints lookup = places.get();
        if (lookup == null) {
            return null;
        }
        return lookup.point(placeId).orElse(null);
    }

    static boolean isBbox(String value) {
        return value != null && BBOX.matcher(value).matches();
    }

    private static void putNewest(Map<String, NormalizedRecord> out, NormalizedRecord record) {
        NormalizedRecord current = out.get(record.externalId());
        if (current == null || isNewer(record, current)) {
            out.put(record.externalId(), record);
        }
    }

    private static boolean isNewer(NormalizedRecord a, NormalizedRecord b) {
        Instant ao = a.observedAt();
        Instant bo = b.observedAt();
        if (ao == null) {
            return false;
        }
        return bo == null || ao.isAfter(bo);
    }

    private static long parseLong(String value) {
        try {
            return Long.parseLong(value.strip());
        } catch (NumberFormatException | NullPointerException e) {
            return 0L;
        }
    }

    private static Optional<Instant> parseInstant(String value) {
        try {
            return Optional.of(Instant.parse(value));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("iNaturalist observations")
                .description("Recent iNaturalist observations in an operator's area, from the v1 observations API."
                        + " An AOI is required (iNaturalist asks clients not to poll globally). Obscured"
                        + " observations are placed at their randomized upstream coordinates with their positional"
                        + " accuracy; geoprivacy-private observations and observations without coordinates are"
                        + " skipped; contributor usernames, descriptions and comments are never stored. Per-record CC"
                        + " licenses; photos are linked only when their license is open and they are on the"
                        + " iNaturalist open-data host.")
                .kind(KIND)
                .homepage("https://www.inaturalist.org/observations")
                .attribution("iNaturalist contributors")
                .licence("per-record CC licence (iNaturalist; see licenseCode)")
                .termsUrl("https://www.inaturalist.org/pages/terms")
                .mode(Mode.POLLING)
                .param(ParamSpec.required(AOI_PARAM, ParamType.STRING,
                        "The area to poll: a bbox west,south,east,north or a place id. Required: no global polling."))
                .param(ParamSpec.enumerated(QUALITY_GRADE_PARAM,
                        "Observation quality grade: research, needs_id or casual", "research",
                        List.of("research", "needs_id", "casual")))
                .param(new ParamSpec(ICONIC_TAXA_PARAM, ParamType.STRING_LIST,
                        "Iconic taxa kept (for example Aves, Plantae); empty keeps every taxon", null, false,
                        List.of()))
                .param(ParamSpec.optional(LICENSED_ONLY_PARAM, ParamType.BOOLEAN,
                        "Skip observations with no license (all rights reserved)", true))
                .param(ParamSpec.optional(INCLUDE_CAPTIVE_PARAM, ParamType.BOOLEAN,
                        "Keep observations flagged captive or cultivated", false))
                .interval(Duration.ofMinutes(10), Duration.ofMinutes(30))
                .defaultBudget(Budget.of(2, 500))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .titleField("commonName")
                .searchFields("taxonName", "commonName")
                .tileProperties("iconicTaxon", "qualityGrade")
                .zeroResultsNormal(true)
                .kindDisplay(KIND, new KindDisplay("{commonName}", null, "#2B8A3E", null));
        b.fieldSpec("taxonId", new FieldSpec(FieldType.INTEGER, "1", "Taxon id", "iNaturalist taxon id", null, Set.of()));
        b.fieldSpec("taxonName", new FieldSpec(FieldType.STRING, null, "Scientific name", "Taxon name", null, Set.of()));
        b.fieldSpec("commonName", new FieldSpec(FieldType.STRING, null, "Common name", "Preferred common name", null,
                Set.of()));
        b.fieldSpec("iconicTaxon", new FieldSpec(FieldType.STRING, null, "Iconic taxon",
                "iNaturalist iconic taxon (Aves, Plantae, Insecta, ...)", null, Set.of()));
        b.fieldSpec("qualityGrade", new FieldSpec(FieldType.STRING, null, "Quality grade",
                "research, needs_id or casual", null, Set.of()));
        b.fieldSpec("licenseCode", new FieldSpec(FieldType.STRING, null, "License",
                "Per-record CC license code (cc0, cc-by, cc-by-nc, ...)", null, Set.of()));
        b.fieldSpec("obscured", new FieldSpec(FieldType.BOOLEAN, null, "Obscured",
                "True when iNaturalist randomized the coordinates for a sensitive taxon", null, Set.of()));
        b.fieldSpec("uri", new FieldSpec(FieldType.STRING, null, "Observation page", "iNaturalist observation URL",
                null, Set.of()));
        b.fieldSpec("photoUrl", new FieldSpec(FieldType.STRING, null, "Photo",
                "First photo, only when its license is open and its host is inaturalist-open-data", null, Set.of()));
        return b.build();
    }
}
