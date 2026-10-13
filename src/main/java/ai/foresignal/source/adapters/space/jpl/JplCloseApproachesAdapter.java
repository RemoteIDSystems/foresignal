package ai.foresignal.source.adapters.space.jpl;

import ai.foresignal.source.adapters.space.SpaceSources;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
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
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code jpl-close-approaches} (prompt 077): asteroid and comet close approaches to Earth in the next 60 days, from
 * the JPL SBDB Close Approach Data API, as {@code close-approach} records without geometry
 * ({@link JplParsers#closeApproaches}).
 *
 * <p><strong>Each run:</strong> one request for {@value #API}{@code ?date-min=now&date-max=%2B60&dist-max=
 * {distMaxAu}&fullname=true} ({@code distMaxAu} default 0.05 au, about 19.5 lunar distances). Rows are revised
 * with each orbit solution and carry no revision stamp, so {@code observedAtMode} is RECEIVED; an approach that has
 * passed drops out of the window and expires 7 days after it was last received.
 */
@Component
public class JplCloseApproachesAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "jpl-close-approaches";

    /** The API. */
    public static final String API = "https://ssd-api.jpl.nasa.gov/cad.api";

    /** The {@code distMaxAu} parameter. */
    public static final String DIST_MAX_PARAM = "distMaxAu";

    /** The default {@code distMaxAu}. */
    public static final double DEFAULT_DIST_MAX_AU = 0.05;

    /** The record TTL, on receipt. */
    static final Duration TTL = Duration.ofDays(7);

    /** The keyframe: under {@code ttl - poll - 30 s}. */
    static final Duration KEYFRAME = Duration.ofDays(6);

    /** Raw retention: close approaches are the {@code event} class (1825 d). */
    static final Duration RETENTION = Duration.ofDays(1825);

    private final String api;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value SpaceSources#BASE_URL_PROPERTY}; blank in production
     */
    @Autowired
    public JplCloseApproachesAdapter(@Value("${" + SpaceSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.api = SpaceSources.uri(API, baseUrlOverride).toString();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** {@code distMaxAu} must be greater than 0 and at most 0.5 au. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        if (params != null && params.has(DIST_MAX_PARAM) && params.number(DIST_MAX_PARAM) != null) {
            double d = params.number(DIST_MAX_PARAM);
            if (!(d > 0) || d > 0.5) {
                problems.put(DIST_MAX_PARAM, "must be greater than 0 and at most 0.5 (au)");
            }
        }
        return problems;
    }

    /** The request URL for a maximum distance in au. */
    public URI uri(double distMaxAu) {
        return URI.create(api + "?date-min=now&date-max=%2B60&dist-max="
                + BigDecimal.valueOf(distMaxAu).stripTrailingZeros().toPlainString() + "&fullname=true");
    }

    /**
     * Fetches the next 60 days (see the class note).
     *
     * @throws AdapterException when the request fails
     * @throws UpstreamFormatException when the answer is not the API's table
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        Double dist = ctx.params().has(DIST_MAX_PARAM) ? ctx.params().number(DIST_MAX_PARAM) : null;
        URI uri = uri(dist == null ? DEFAULT_DIST_MAX_AU : dist);
        JplParsers.Parsed parsed = JplParsers.closeApproaches(SpaceSources.body(ctx, ctx.http().get(uri), null, uri));
        ctx.log().debug("{}: API {} with {} approach(es), skipped {}", ID, parsed.version(), parsed.records().size(),
                parsed.skipped());
        return FetchResult.of(parsed.records());
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = JplParsers.CLOSE_APPROACH_KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("JPL asteroid close approaches")
                .description("Asteroid and comet close approaches to Earth in the next 60 days within distMaxAu"
                        + " (default 0.05 au), from the NASA/JPL SBDB Close Approach Data API: designation, nominal,"
                        + " minimum and maximum distance, relative velocity and absolute magnitude. No geometry.")
                .kind(kind)
                .homepage("https://cneos.jpl.nasa.gov/ca/")
                .attribution("NASA/JPL Solar System Dynamics, SBDB Close Approach Data API")
                .licence("US-PD")
                .termsUrl("https://ssd-api.jpl.nasa.gov/doc/cad.html")
                .mode(Mode.POLLING)
                .interval(Duration.ofHours(1), Duration.ofHours(12))
                .defaultBudget(Budget.of(1, 10))
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(new IngestPolicy.All(KEYFRAME))
                .observedAtMode(ObservedAtMode.RECEIVED)
                .recordTtl(TTL)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .zeroResultsNormal(true)
                .defaultRetention(RETENTION)
                .kindRetention(kind, RETENTION)
                .titleField("fullName")
                .searchFields("designation", "fullName")
                .param(ParamSpec.optional(DIST_MAX_PARAM, ParamType.NUMBER,
                        "Largest approach distance in au, at most 0.5 (1 au is 389.17 lunar distances)",
                        DEFAULT_DIST_MAX_AU));
        b.fieldSpec("designation", new FieldSpec(FieldType.STRING, null, "Designation", "The object's primary"
                + " designation", null, Set.of()));
        b.fieldSpec("fullName", new FieldSpec(FieldType.STRING, null, "Name", "The object's full name", null,
                Set.of()));
        b.fieldSpec("distAu", new FieldSpec(FieldType.NUMBER, "[AU]", "Distance", "Nominal approach distance in au",
                null, Set.of()));
        b.fieldSpec("distLunar", new FieldSpec(FieldType.NUMBER, null, "Distance (LD)",
                "Nominal approach distance in lunar distances (au x 389.17)", null, Set.of()));
        b.fieldSpec("distMinAu", new FieldSpec(FieldType.NUMBER, "[AU]", "Minimum distance",
                "Minimum (3-sigma) approach distance in au", null, Set.of()));
        b.fieldSpec("distMaxAu", new FieldSpec(FieldType.NUMBER, "[AU]", "Maximum distance",
                "Maximum (3-sigma) approach distance in au", null, Set.of()));
        b.fieldSpec("vRelKms", new FieldSpec(FieldType.NUMBER, "km/s", "Relative velocity",
                "Velocity relative to Earth at close approach", null, Set.of()));
        b.fieldSpec("absMagnitude", new FieldSpec(FieldType.NUMBER, "1", "H", "Absolute magnitude H (absent when"
                + " unknown)", null, Set.of()));
        b.fieldSpec("orbitId", new FieldSpec(FieldType.STRING, null, "Orbit", "The orbit solution id", null,
                Set.of()));
        b.fieldSpec("timeSigma", new FieldSpec(FieldType.STRING, null, "Time uncertainty",
                "3-sigma uncertainty of the approach time (d_hh:mm, or < 00:01). The approach time (eventTime) is"
                        + " TDB, about 69 s ahead of UTC", null, Set.of()));
        return b.build();
    }
}
