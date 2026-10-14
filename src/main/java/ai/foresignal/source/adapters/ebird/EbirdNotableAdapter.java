package ai.foresignal.source.adapters.ebird;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.Egress;
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code ebird-notable} (prompt 095): notable bird sightings near an operator's point, from the eBird API 2.0
 * ({@code /v2/data/obs/geo/recent/notable}), as {@code bird-sighting} records ({@link EbirdParser}).
 *
 * <p><strong>Disabled by default</strong> and needs a per-account {@code apiKey} in the
 * {@code x-ebirdapitoken} header. eBird publishes no numeric rate limit and reserves the right to suspend
 * access that has an "adverse impact", so the budget stays low (2/min, 100/day) and the interval is an hour.
 *
 * <p><strong>Time base is RECEIVED:</strong> the feed carries no upstream update time; {@code obsDt} is a local
 * wall-clock time kept as {@code obsDtLocal} and mapped to {@code eventTime} through the 040 timezone.
 *
 * <p><strong>Licence:</strong> eBird's API terms are non-commercial with attribution; the source is
 * {@code LOCAL_ONLY} for 109/110.
 */
@Component
public class EbirdNotableAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "ebird-notable";

    /** The record kind. */
    public static final String KIND = EbirdParser.KIND;

    /** The production upstream. */
    public static final String DEFAULT_BASE_URL = "https://api.ebird.org";

    /** Test-only override of {@link #DEFAULT_BASE_URL}. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.ebird.base-url";

    /** The credential field. */
    public static final String API_KEY = "apiKey";

    /** The credential header. */
    public static final String TOKEN_HEADER = "x-ebirdapitoken";

    /** The notable path. */
    static final String NOTABLE_PATH = "/v2/data/obs/geo/recent/notable";

    /** The recent path (latest observation per species). */
    static final String RECENT_PATH = "/v2/data/obs/geo/recent";

    /** The default search radius, km. */
    public static final int DEFAULT_DIST_KM = 25;

    /** The default look-back, days. */
    public static final int DEFAULT_BACK_DAYS = 3;

    /** Raw retention: sightings are the {@code event} analytic class (Conventions), 1825 days. */
    static final Duration RETENTION = Duration.ofDays(1825);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;
    private final Supplier<PlacePoints> places;
    private final PlaceTimeZones zones;

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means {@value #DEFAULT_BASE_URL}
     * @param places resolves a {@code place} parameter to a point
     * @param zones the 040 timezone lookup for the local {@code obsDt}
     */
    @Autowired
    public EbirdNotableAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<PlacePoints> places, ObjectProvider<PlaceTimeZones> zones) {
        this(baseUrlOverride, places == null ? () -> null : (Supplier<PlacePoints>) places::getIfAvailable,
                zones == null ? PlaceTimeZones.none() : zones.getIfAvailable(PlaceTimeZones::none));
    }

    /** Creates the adapter with plain dependencies (tests). */
    public EbirdNotableAdapter(String baseUrlOverride, Supplier<PlacePoints> places, PlaceTimeZones zones) {
        String chosen = baseUrlOverride == null || baseUrlOverride.isBlank() ? DEFAULT_BASE_URL : baseUrlOverride.trim();
        this.baseUrl = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
        this.places = places == null ? () -> null : places;
        this.zones = zones == null ? PlaceTimeZones.none() : zones;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The base URL, without a trailing slash. */
    public String baseUrl() {
        return baseUrl;
    }

    /** Refuses a missing point, a dist outside 1–50 km and a back outside 1–30 days. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        if (point(params) == null) {
            problems.put("lat", "give either lat and lng or a place id");
            problems.put("lng", "give either lat and lng or a place id");
        }
        Integer dist = params.integer("distKm");
        if (dist != null && (dist < 1 || dist > 50)) {
            problems.put("distKm", "must be 1 to 50 km, but was " + dist);
        }
        Integer back = params.integer("backDays");
        if (back != null && (back < 1 || back > 30)) {
            problems.put("backDays", "must be 1 to 30 days, but was " + back);
        }
        return problems;
    }

    /**
     * Reads the notable feed, and the recent feed too when {@code includeRecent} is set.
     *
     * @throws AdapterException when the key or the point is missing, or a request fails
     * @throws UpstreamFormatException when a body is truncated or not a JSON array
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        String key = ctx.credentials() == null ? null : ctx.credentials().get(API_KEY);
        if (key == null || key.isBlank()) {
            throw new AdapterException(ID + ": the credential " + API_KEY + " is not set (a free key from"
                    + " ebird.org/api/keygen)");
        }
        ValidatedParams params = ctx.params();
        double[] point = point(params);
        if (point == null) {
            throw new AdapterException(ID + ": give either lat and lng or a place id");
        }
        Map<String, String> headers = Map.of(TOKEN_HEADER, key.strip(), "Accept", "application/json");
        int dist = params.integer("distKm", DEFAULT_DIST_KM);
        int back = params.integer("backDays", DEFAULT_BACK_DAYS);

        Map<String, NormalizedRecord> out = new LinkedHashMap<>();
        read(ctx, notableUri(point, dist, back), headers, out);
        if (params.bool("includeRecent", false)) {
            read(ctx, recentUri(point, dist, back), headers, out);
        }
        ctx.log().debug("{}: {} sighting(s) near {},{}", ID, out.size(), point[0], point[1]);
        return FetchResult.of(new ArrayList<>(out.values()));
    }

    private void read(FetchContext ctx, URI uri, Map<String, String> headers, Map<String, NormalizedRecord> out)
            throws AdapterException {
        UpstreamResponse response = ctx.http().get(uri, headers);
        if (response.truncated()) {
            throw new UpstreamFormatException("eBird response exceeded maxResponseBytes", null);
        }
        for (NormalizedRecord record : EbirdParser.parse(response.body(), zones).records()) {
            out.putIfAbsent(record.externalId(), record);
        }
    }

    /** The notable request. */
    public URI notableUri(double[] point, int distKm, int backDays) {
        return URI.create(baseUrl + NOTABLE_PATH + query(point, distKm, backDays));
    }

    /** The recent request (latest observation per species). */
    public URI recentUri(double[] point, int distKm, int backDays) {
        return URI.create(baseUrl + RECENT_PATH + query(point, distKm, backDays));
    }

    private static String query(double[] point, int distKm, int backDays) {
        return "?lat=" + String.format(Locale.ROOT, "%.2f", point[1])
                + "&lng=" + String.format(Locale.ROOT, "%.2f", point[0])
                + "&dist=" + distKm + "&back=" + backDays + "&detail=simple";
    }

    /** The point from {@code lat}/{@code lng} or a {@code place} id, or null. */
    double[] point(ValidatedParams params) {
        Double lat = params.number("lat");
        Double lon = params.number("lng");
        if (lat != null && lon != null && lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180) {
            return new double[] {lon, lat};
        }
        String place = params.string("place");
        if (place != null && !place.isBlank()) {
            PlacePoints lookup = places.get();
            if (lookup != null) {
                return lookup.point(place).orElse(null);
            }
        }
        return null;
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("eBird notable sightings")
                .description("Notable bird sightings reported to eBird near a point, from the eBird API 2.0."
                        + " Disabled by default and needs a per-account key. Sensitive species are withheld"
                        + " upstream; private locations are rounded to 0.05 degrees and their place name is"
                        + " never stored. Non-commercial with attribution; served locally only for 109/110.")
                .kind(KIND)
                .homepage("https://ebird.org")
                .attribution("eBird.org (Cornell Lab of Ornithology)")
                .licence("eBird API Terms of Use (non-commercial, attribution)")
                .termsUrl("https://www.birds.cornell.edu/home/ebird-api-terms-of-use/")
                .egress(Egress.LOCAL_ONLY)
                .mode(Mode.POLLING)
                .credential(CredentialField.required(API_KEY,
                        "Your eBird API key (free from https://ebird.org/api/keygen), sent as the"
                                + " x-ebirdapitoken header; one key per eBird account"))
                .param(ParamSpec.optional("lat", ParamType.NUMBER, "Latitude of the search centre", null))
                .param(ParamSpec.optional("lng", ParamType.NUMBER, "Longitude of the search centre", null))
                .param(ParamSpec.optional("place", ParamType.STRING,
                        "A 036 place id used as the search centre, instead of lat and lng", null))
                .param(ParamSpec.optional("distKm", ParamType.INTEGER, "Search radius in km (1 to 50)", DEFAULT_DIST_KM))
                .param(ParamSpec.optional("backDays", ParamType.INTEGER, "How many days back to look (1 to 30)",
                        DEFAULT_BACK_DAYS))
                .param(ParamSpec.optional("includeRecent", ParamType.BOOLEAN,
                        "Also read the recent feed (the latest observation per species)", false))
                .interval(Duration.ofMinutes(30), Duration.ofHours(1))
                .defaultBudget(Budget.of(2, 100))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.RECEIVED)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .titleField("comName")
                .searchFields("comName", "sciName", "speciesCode")
                .tileProperties("speciesCode")
                .zeroResultsNormal(true)
                .kindDisplay(KIND, new KindDisplay("{comName}", null, "#4263EB", null));
        b.fieldSpec("speciesCode", new FieldSpec(FieldType.STRING, null, "Species code",
                "eBird six-letter species code", null, Set.of()));
        b.fieldSpec("comName", new FieldSpec(FieldType.STRING, null, "Common name", "eBird common name", null,
                Set.of()));
        b.fieldSpec("sciName", new FieldSpec(FieldType.STRING, null, "Scientific name", "eBird scientific name",
                null, Set.of()));
        b.fieldSpec("howMany", new FieldSpec(FieldType.INTEGER, "1", "Count",
                "Number seen, absent when eBird did not report one", null, Set.of()));
        b.fieldSpec("obsValid", new FieldSpec(FieldType.BOOLEAN, null, "Valid", "Whether eBird considers the"
                + " observation valid", null, Set.of()));
        b.fieldSpec("obsReviewed", new FieldSpec(FieldType.BOOLEAN, null, "Reviewed",
                "Whether a reviewer has looked at the observation", null, Set.of()));
        b.fieldSpec("locId", new FieldSpec(FieldType.STRING, null, "Location id", "eBird location id", null,
                Set.of()));
        b.fieldSpec("locName", new FieldSpec(FieldType.STRING, null, "Location name",
                "Location name, omitted when the location is private", null, Set.of()));
        b.fieldSpec("obsDtLocal", new FieldSpec(FieldType.STRING, null, "Observed (local)",
                "Local wall-clock observation time as eBird sent it (no zone)", null, Set.of()));
        return b.build();
    }
}
