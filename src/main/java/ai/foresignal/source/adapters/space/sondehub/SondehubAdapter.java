package ai.foresignal.source.adapters.space.sondehub;

import ai.foresignal.source.adapters.space.SpaceSources;
import ai.foresignal.source.formats.sonde.RadiosondeRecords;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PlacePoints;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.Duration;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code sondehub} (prompt 077): radiosondes near a point from the SondeHub v2 API, as {@code radiosonde} records
 * ({@link SondehubParser}, {@link RadiosondeRecords}).
 *
 * <ul>
 *   <li><strong>Request:</strong> {@value #SONDES_URL}{@code ?lat={lat}&lon={lon}&distance={radiusKm × 1000}&last=
 *       {windowS}}: the latest telemetry of each sonde heard within the radius in the window. The SondeHub docs ask
 *       clients not to poll the full telemetry endpoint regularly, so only {@code /sondes} is used.
 *   <li><strong>Params:</strong> {@code center} ({@code lat,lon}; default the first place by id, else required),
 *       {@code radiusKm} (default 500, at most 1500), {@code windowS} (default 600).
 *   <li><strong>Privacy:</strong> uploaders' positions and antennas are never stored; the uploader callsign is the
 *       record's observer.
 * </ul>
 */
@Component
public class SondehubAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "sondehub";

    /** The latest-telemetry endpoint. */
    public static final String SONDES_URL = "https://api.v2.sondehub.org/sondes";

    /** The {@code center} parameter. */
    public static final String CENTER_PARAM = "center";

    /** The {@code radiusKm} parameter. */
    public static final String RADIUS_PARAM = "radiusKm";

    /** The {@code windowS} parameter. */
    public static final String WINDOW_PARAM = "windowS";

    /** The default radius. */
    public static final int DEFAULT_RADIUS_KM = 500;

    /** The largest radius. */
    public static final int MAX_RADIUS_KM = 1500;

    /** The default window. */
    public static final int DEFAULT_WINDOW_S = 600;

    /** The record TTL, from {@code observedAt}. */
    static final Duration TTL = Duration.ofMinutes(30);

    /** The keyframe: under {@code ttl - poll - 30 s}. */
    static final Duration KEYFRAME = Duration.ofMinutes(20);

    /** Raw retention: high-volume balloon telemetry, the {@code bulk-event} class (90 d). */
    static final Duration RETENTION = Duration.ofDays(90);

    private final String sondesUrl;
    private final Supplier<PlacePoints> places;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter for Spring.
     *
     * @param places the places, when the place module is present (the default center)
     * @param baseUrlOverride the value of {@value SpaceSources#BASE_URL_PROPERTY}; blank in production
     */
    @Autowired
    public SondehubAdapter(ObjectProvider<PlacePoints> places,
            @Value("${" + SpaceSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this(() -> places == null ? null : places.getIfAvailable(), baseUrlOverride);
    }

    /**
     * Creates the adapter.
     *
     * @param places supplies the places (null or a null result means none)
     * @param baseUrlOverride a base URL, or blank for the real upstream
     */
    public SondehubAdapter(Supplier<PlacePoints> places, String baseUrlOverride) {
        this.places = places == null ? () -> null : places;
        this.sondesUrl = SpaceSources.uri(SONDES_URL, baseUrlOverride).toString();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /**
     * {@code center} must be {@code lat,lon}, and is required when there is no place to default to;
     * {@code radiusKm} 1 to 1500; {@code windowS} 60 to 86400.
     */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        String text = params == null || !params.has(CENTER_PARAM) ? null : params.string(CENTER_PARAM);
        if (text != null && center(text) == null) {
            problems.put(CENTER_PARAM, "must be lat,lon in degrees, for example 52.2,13.6");
        } else if (text == null && !hasPlace()) {
            // Without this the source was seeded enabled and failed every run on an install with no places.
            problems.put(CENTER_PARAM, "is required when there is no place to default to");
        }
        if (params == null) {
            return problems;
        }
        if (params.has(RADIUS_PARAM) && params.integer(RADIUS_PARAM) != null) {
            int r = params.integer(RADIUS_PARAM);
            if (r < 1 || r > MAX_RADIUS_KM) {
                problems.put(RADIUS_PARAM, "must be between 1 and " + MAX_RADIUS_KM);
            }
        }
        if (params.has(WINDOW_PARAM) && params.integer(WINDOW_PARAM) != null) {
            int w = params.integer(WINDOW_PARAM);
            if (w < 60 || w > 86_400) {
                problems.put(WINDOW_PARAM, "must be between 60 and 86400 seconds");
            }
        }
        return problems;
    }

    private boolean hasPlace() {
        PlacePoints p = places.get();
        return p != null && !p.summaries().isEmpty();
    }

    /** {@code lat,lon} as {@code {lat, lon}}, or null when it is not a valid pair. */
    static double[] center(String text) {
        if (text == null) {
            return null;
        }
        String[] parts = text.split(",");
        if (parts.length != 2) {
            return null;
        }
        try {
            double lat = Double.parseDouble(parts[0].strip());
            double lon = Double.parseDouble(parts[1].strip());
            return RadiosondeRecords.validPosition(lat, lon) ? new double[] {lat, lon} : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** The request URL for a center, radius and window. */
    public URI uri(double lat, double lon, int radiusKm, int windowS) {
        return URI.create(sondesUrl + "?lat=" + coordinate(lat) + "&lon=" + coordinate(lon) + "&distance="
                + (radiusKm * 1000L) + "&last=" + windowS);
    }

    /**
     * Fetches the sondes around the center (see the class note).
     *
     * @throws AdapterException when the request fails, or there is no center (no {@code center} and no place)
     * @throws UpstreamFormatException when the answer cannot be used
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        double[] center = ctx.params().has(CENTER_PARAM) ? center(ctx.params().string(CENTER_PARAM)) : null;
        if (center == null) {
            PlacePoints p = places.get();
            PlacePoints.Summary first = p == null ? null
                    : p.summaries().stream().min(Comparator.comparing(PlacePoints.Summary::id)).orElse(null);
            if (first == null) {
                throw new AdapterException(ID + ": set the center parameter (lat,lon); there is no place to default"
                        + " to");
            }
            center = new double[] {first.lat(), first.lon()};
        }
        int radius = Math.max(1, Math.min(MAX_RADIUS_KM, ctx.params().integer(RADIUS_PARAM, DEFAULT_RADIUS_KM)));
        int window = Math.max(60, ctx.params().integer(WINDOW_PARAM, DEFAULT_WINDOW_S));
        URI uri = uri(center[0], center[1], radius, window);
        SondehubParser.Parsed parsed = SondehubParser.parse(SpaceSources.body(ctx, ctx.http().get(uri), null, uri));
        ctx.log().debug("{}: {} sonde(s), skipped {}", ID, parsed.records().size(), parsed.skipped());
        return FetchResult.of(parsed.records());
    }

    /** A coordinate to at most 4 decimals. */
    static String coordinate(double v) {
        String s = BigDecimal.valueOf(v).setScale(4, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
        return s.equals("-0") ? "0" : s;
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = RadiosondeRecords.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("SondeHub radiosondes")
                .description("Weather balloon radiosondes heard by the SondeHub amateur receiver network within"
                        + " radiusKm of a center: position, altitude, speeds, heading and the sonde's temperature,"
                        + " humidity and pressure. Sensor values are uncalibrated amateur telemetry. Receivers'"
                        + " positions and antennas are never stored; the uploader callsign is the observer.")
                .kind(kind)
                .homepage("https://sondehub.org")
                .attribution("SondeHub (CC BY-SA 2.0)")
                .licence("CC BY-SA 2.0 (\"Radiosonde and Amateur balloon telemetry data captured in the SondeHub"
                        + " database is licensed under Creative Commons BY-SA v2.0\")")
                .termsUrl("https://github.com/projecthorus/sondehub-infra/wiki/SondeHub-Data-Licence")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(2), Duration.ofMinutes(5))
                .defaultBudget(Budget.of(2, 400))
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(new IngestPolicy.All(KEYFRAME))
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(TTL)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .zeroResultsNormal(true)
                .defaultRetention(RETENTION)
                .kindRetention(kind, RETENTION)
                .titleField("type")
                .searchFields("type", "subtype", "manufacturer")
                .param(ParamSpec.optional(CENTER_PARAM, ParamType.STRING,
                        "Center as lat,lon; default the first place by id, required when there is no place", null))
                .param(ParamSpec.optional(RADIUS_PARAM, ParamType.INTEGER, "Radius in km, at most 1500",
                        DEFAULT_RADIUS_KM))
                .param(ParamSpec.optional(WINDOW_PARAM, ParamType.INTEGER,
                        "Only sondes heard in the last windowS seconds, 60 to 86400", DEFAULT_WINDOW_S));
        RadiosondeRecords.fieldSpecs(b);
        b.kindDisplay(kind, new KindDisplay("{type} {altitudeM} m", "marker", "#1C7ED6", null));
        return b.build();
    }
}
