package ai.foresignal.source.adapters.usgs;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * USGS earthquakes from the Earthquake Hazards Program's GeoJSON summary feeds: the reference
 * polling adapter that {@code docs/Adapter-Guide.md} walks through.
 *
 * <p>One conditional GET per fetch on {@code /earthquakes/feed/v1.0/summary/{feed}.geojson}. The
 * feeds carry no {@code ETag}, only {@code Last-Modified} (the generation time) and
 * {@code cache-control: public, max-age=60}; {@code If-Modified-Since} returns 304, which becomes
 * {@link FetchResult#unchanged()} (verified 2026-10).
 *
 * <p>USGS revises events after the fact: magnitude, status and location change, and
 * {@code updated} moves. A revision changes the static hash and is persisted as a SIGNIFICANT
 * version; a re-fetch where only {@code updated} moved hashes equal because {@code updated} is
 * volatile, and an unchanged re-poll is a touch.
 *
 * <p>Events are never auto-deleted when they age out of a feed's rolling window: the result is
 * complete, but absent records are never tombstoned. Only an explicit {@code status: deleted}
 * becomes a tombstone.
 *
 * <p>The upstream base URL is {@value #DEFAULT_BASE_URL}. The test property
 * {@value #BASE_URL_PROPERTY} replaces it, so integration tests can point the adapter at WireMock;
 * there is no operator setting for it, because the feed list is code-reviewed (Architecture §4.2).
 */
@Component
public class UsgsEarthquakesAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "usgs-earthquakes";

    /** The one record kind. */
    public static final String KIND = UsgsGeoJsonParser.KIND;

    /** The production upstream. */
    public static final String DEFAULT_BASE_URL = "https://earthquake.usgs.gov";

    /** Test-only override of {@link #DEFAULT_BASE_URL}. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.usgs.base-url";

    /** The {@code feed} parameter's name. */
    public static final String FEED_PARAM = "feed";

    /** The feed fetched when the operator chooses none. */
    public static final String DEFAULT_FEED = "all_day";

    /** Every summary feed USGS publishes, by magnitude floor and window. */
    public static final List<String> FEEDS =
            List.of(
                    "all_hour", "all_day", "all_week", "all_month",
                    "1.0_hour", "1.0_day", "1.0_week", "1.0_month",
                    "2.5_hour", "2.5_day", "2.5_week", "2.5_month",
                    "4.5_hour", "4.5_day", "4.5_week", "4.5_month",
                    "significant_hour", "significant_day", "significant_week", "significant_month");

    /** Path of a summary feed, before the feed name. */
    static final String FEED_PATH = "/earthquakes/feed/v1.0/summary/";

    /** Prefix of the adapter-state key holding a feed's {@code Last-Modified}. */
    static final String STATE_KEY_PREFIX = "feed:";

    /**
     * Default raw retention: earthquakes are the {@code event} analytic class, which
     * {@code docs/Conventions.md} ("Analytic retention") keeps for 1825 days.
     */
    static final Duration DEFAULT_RETENTION = Duration.ofDays(1825);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means
     *     {@value #DEFAULT_BASE_URL}
     */
    public UsgsEarthquakesAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        String chosen = baseUrlOverride == null || baseUrlOverride.isBlank() ? DEFAULT_BASE_URL : baseUrlOverride.trim();
        this.baseUrl = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The base URL requests go to, without a trailing slash. */
    public String baseUrl() {
        return baseUrl;
    }

    /** The URL of one summary feed. */
    public URI feedUri(String feed) {
        return URI.create(baseUrl + FEED_PATH + feed + ".geojson");
    }

    /**
     * Fetches the configured feed once with a conditional GET.
     *
     * @return {@link FetchResult#unchanged()} on a 304, otherwise every parsed record as a complete
     *     result
     * @throws AdapterException when the upstream fails; {@link UpstreamFormatException} when the
     *     body is truncated or not a feature collection
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        String feed = ctx.params().optionalString(FEED_PARAM).orElse(DEFAULT_FEED);
        UpstreamResponse response = ctx.http().getConditional(feedUri(feed), STATE_KEY_PREFIX + feed);
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        if (response.truncated()) {
            throw new UpstreamFormatException(
                    "USGS feed " + feed + " exceeded maxResponseBytes and was truncated", null);
        }
        UsgsGeoJsonParser.ParseResult parsed = UsgsGeoJsonParser.parseWithDiagnostics(response.body());
        UsgsGeoJsonParser.ParseDiagnostics diagnostics = parsed.diagnostics();
        if (diagnostics.skipped() > 0) {
            ctx.log().warn("USGS feed {}: skipped {} of {} feature(s): {}", feed, diagnostics.skipped(),
                    diagnostics.features(), diagnostics);
        } else {
            ctx.log().debug("USGS feed {}: {}", feed, diagnostics);
        }
        return FetchResult.complete(parsed.records());
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder =
                SourceDescriptor.builder(ID)
                        .name("USGS Earthquakes")
                        .description(
                                "Earthquakes worldwide from the USGS Earthquake Hazards Program summary feeds,"
                                        + " with revisions to magnitude, location and review status.")
                        .kind(KIND)
                        .homepage("https://earthquake.usgs.gov/earthquakes/feed/")
                        .attribution("U.S. Geological Survey, Earthquake Hazards Program")
                        .licence("US-PD")
                        .termsUrl("https://www.usgs.gov/information-policies-and-instructions/copyrights-and-credits")
                        .mode(Mode.POLLING)
                        .param(
                                ParamSpec.enumerated(
                                        FEED_PARAM,
                                        "Which summary feed to poll: magnitude floor and time window",
                                        DEFAULT_FEED,
                                        FEEDS))
                        // The feeds regenerate about once a minute.
                        .interval(Duration.ofSeconds(60), Duration.ofSeconds(60))
                        // USGS publishes no numeric limit; this stays well inside one request per feed-minute.
                        .defaultBudget(4, 2_000)
                        .historyTier(HistoryTier.STANDARD)
                        .observedAtMode(ObservedAtMode.UPSTREAM)
                        .ttlAnchor(TtlAnchor.RECEIVED)
                        .recordTtl(null)
                        .defaultIngestPolicy(IngestPolicy.all())
                        .defaultRetention(DEFAULT_RETENTION)
                        .volatileFields("updated")
                        .titleField("title")
                        .zeroResultsNormal(true)
                        .tileProperties("mag", "place", "depth", "tsunami", "alert", "sig")
                        .searchFields("place", "title")
                        .rankField("mag")
                        .kindDisplay(KIND, new KindDisplay("M {mag} - {place}", "earthquake", "#D9480F", null));

        field(builder, "mag", FieldType.NUMBER, "1", "Magnitude", "Event magnitude, on the scale named by magType");
        field(builder, "magType", FieldType.STRING, null, "Magnitude type", "Method or algorithm, for example ml, mb or mww");
        field(builder, "place", FieldType.STRING, null, "Place", "Named geographic region near the event");
        field(builder, "time", FieldType.TIMESTAMP, "ms", "Origin time", "Origin time, milliseconds since the Unix epoch");
        field(builder, "updated", FieldType.TIMESTAMP, "ms", "Last updated", "When USGS last revised the event, milliseconds since the Unix epoch");
        field(builder, "url", FieldType.STRING, null, "Event page", "USGS event page");
        field(builder, "detail", FieldType.STRING, null, "Detail feed", "GeoJSON detail feed for the event");
        field(builder, "felt", FieldType.INTEGER, "{report}", "Felt reports", "Did You Feel It? responses");
        field(builder, "cdi", FieldType.NUMBER, "1", "Max reported intensity", "Maximum community decimal intensity (Did You Feel It?)");
        field(builder, "mmi", FieldType.NUMBER, "1", "Max estimated intensity", "Maximum Modified Mercalli intensity (ShakeMap)");
        field(builder, "alert", FieldType.STRING, null, "PAGER alert", "PAGER alert level: green, yellow, orange or red");
        field(builder, "status", FieldType.STRING, null, "Review status", "automatic, reviewed or deleted");
        field(builder, "tsunami", FieldType.INTEGER, "1", "Tsunami flag", "1 for large events in oceanic regions, otherwise 0; not a tsunami warning");
        field(builder, "sig", FieldType.INTEGER, "1", "Significance", "USGS significance, 0 and up, from magnitude, intensity, felt reports and impact");
        field(builder, "net", FieldType.STRING, null, "Network", "Preferred contributing network");
        field(builder, "code", FieldType.STRING, null, "Network code", "Event code assigned by the preferred network");
        field(builder, "ids", FieldType.STRING, null, "Event ids", "Comma-delimited event ids associated with the event");
        field(builder, "sources", FieldType.STRING, null, "Sources", "Comma-delimited contributing networks");
        field(builder, "types", FieldType.STRING, null, "Product types", "Comma-delimited product types available");
        field(builder, "nst", FieldType.INTEGER, "{station}", "Stations", "Seismic stations used to locate the event");
        field(builder, "dmin", FieldType.NUMBER, "deg", "Nearest station", "Horizontal distance to the nearest station");
        field(builder, "rms", FieldType.NUMBER, "s", "RMS residual", "Root-mean-square travel time residual");
        field(builder, "gap", FieldType.NUMBER, "deg", "Azimuthal gap", "Largest azimuthal gap between adjacent stations");
        field(builder, "type", FieldType.STRING, null, "Event type", "earthquake, quarry blast, explosion and others");
        field(builder, "title", FieldType.STRING, null, "Title", "USGS event title");
        builder.fieldSpec(
                UsgsGeoJsonParser.DEPTH,
                new FieldSpec(
                        FieldType.NUMBER,
                        "km",
                        "Depth",
                        "Hypocenter depth from coordinates[2], positive down; negative values are above the reference",
                        "network-dependent reference, positive down",
                        Set.of()));
        return builder.build();
    }

    private static void field(
            SourceDescriptor.Builder builder, String key, FieldType type, String unit, String label, String description) {
        builder.fieldSpec(key, new FieldSpec(type, unit, label, description, null, Set.of()));
    }
}
