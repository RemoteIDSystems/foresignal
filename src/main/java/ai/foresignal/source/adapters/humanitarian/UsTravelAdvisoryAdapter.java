package ai.foresignal.source.adapters.humanitarian;

import ai.foresignal.reference.BundledCountryPolygons;
import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.reference.CountryRef;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code travel-advisory-us} (prompt 090): US State Department travel advisories.
 *
 * <p>{@code Category} is mostly a FIPS 10-4 country code but not consistently; it is resolved through
 * {@link TravelAdvisoryCodes} (a documented FIPS base with explicit ISO2 exceptions). The advisory level is
 * read from the title ({@code Level N: text}). Country geometry is 040's polygon, else the label point
 * (approximate).
 */
@Component
public final class UsTravelAdvisoryAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "travel-advisory-us";

    /** The record kind. */
    public static final String KIND = "travel-advisory";

    /** The default base URL. */
    public static final String DEFAULT_BASE_URL = "https://cadataapi.state.gov";

    /** Test-only base override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.travel-advisory-us.base-url";

    /** The adapter-state key for the index validators. */
    static final String STATE_KEY = "us-advisory:index";

    static final Duration RETENTION = Duration.ofDays(365);

    static final Pattern LEVEL = Pattern.compile("Level (\\d):\\s*(.+)$");

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final URI baseUrl;
    private final CountryCentroids countries;
    private final BundledCountryPolygons polygons;
    private final SourceDescriptor descriptor;

    /** Creates the adapter. */
    @Autowired
    public UsTravelAdvisoryAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            CountryCentroids countries, BundledCountryPolygons polygons) {
        this.baseUrl = URI.create(baseUrlOverride == null || baseUrlOverride.isBlank()
                ? DEFAULT_BASE_URL : baseUrlOverride.strip());
        this.countries = countries;
        this.polygons = polygons;
        this.descriptor = buildDescriptor();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse response = ctx.http().getConditional(
                URI.create(baseUrl + "/api/TravelAdvisories"), STATE_KEY);
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        if (response.status() != 200) {
            throw new UpstreamFormatException("The State Department advisories answered HTTP " + response.status(), "");
        }
        return FetchResult.of(parse(response.body()));
    }

    /** The records of a State Department advisories body. */
    public List<NormalizedRecord> parse(byte[] body) {
        JsonNode root = JSON.readTree(body);
        JsonNode array = root.isArray() ? root : root.path("value");
        List<NormalizedRecord> out = new ArrayList<>();
        for (JsonNode advisory : array) {
            NormalizedRecord record = record(advisory);
            if (record != null) {
                out.add(record);
            }
        }
        return out;
    }

    private NormalizedRecord record(JsonNode advisory) {
        Optional<CountryRef> country = TravelAdvisoryCodes.resolve(countries, fips(advisory.get("Category")));
        if (country.isEmpty()) {
            return null;
        }
        CountryRef ref = country.get();
        String iso2 = ref.iso2();
        String title = text(advisory, "Title");
        String[] level = level(title);
        NormalizedRecord.Builder b = NormalizedRecord.builder("us:" + iso2, KIND)
                .observedAt(time(text(advisory, "Updated")))
                .eventTime(time(text(advisory, "Published")));
        Geometry geometry = polygons.polygon(iso2).orElseGet(() -> Geo.point(ref.labelLon(), ref.labelLat()));
        b.geometry(geometry);
        b.put("geometryApproximate", true);
        b.put("issuer", "US");
        b.put("level", level[0] == null ? null : Integer.parseInt(level[0]));
        b.put("levelText", level[1]);
        b.put("title", title);
        b.put("summary", HtmlText.strip(text(advisory, "Summary"), 4 * 1024));
        b.put("url", text(advisory, "Link"));
        return b.build();
    }

    /** {@code {levelNumber, levelText}} from a title, or {@code {null, null}}. */
    static String[] level(String title) {
        if (title == null) {
            return new String[] {null, null};
        }
        Matcher matcher = LEVEL.matcher(title);
        return matcher.find() ? new String[] {matcher.group(1), matcher.group(2).strip()}
                : new String[] {null, null};
    }

    /** The first FIPS code of a {@code Category} member (a string or, as the API now sends, an array). */
    static String fips(JsonNode category) {
        if (category == null || category.isNull()) {
            return null;
        }
        if (category.isArray()) {
            return category.isEmpty() ? null : category.get(0).asString();
        }
        return category.asString();
    }

    private static String text(JsonNode node, String field) {
        if (node == null) {
            return null;
        }
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asString();
    }

    static Instant time(String value) {
        return Instants.parse(value);
    }

    private static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("US travel advisories")
                .description("US State Department travel advisories, one per country (FIPS 10-4 Category resolved "
                        + "through the gazetteer). The advisory level is read from the title. Country geometry is an "
                        + "approximate polygon or label point.")
                .kind(KIND)
                .mode(Mode.POLLING)
                .homepage("https://travel.state.gov/content/travel/en/traveladvisories/traveladvisories.html")
                .attribution("US Department of State")
                .licence("US Government work (public domain)")
                .termsUrl("https://travel.state.gov/content/travel/en/about/legal.html")
                .interval(Duration.ofHours(6), Duration.ofHours(6))
                .defaultBudget(2, 20)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .titleField("title")
                .searchFields("title", "summary")
                .significantFields("level")
                .embedFields("summary")
                .zeroResultsNormal(true)
                .kindDisplay(KIND, new KindDisplay("Level {level}: {title}", "information", "#1971C2", null))
                .build();
    }
}
