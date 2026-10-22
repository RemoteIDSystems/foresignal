package ai.foresignal.source.adapters.adsblol;

import ai.foresignal.source.formats.readsb.AircraftDescriptors;
import ai.foresignal.source.formats.readsb.AircraftFields;
import ai.foresignal.source.formats.readsb.ReadsbAircraftMapper;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Coverage;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.locationtech.jts.geom.Envelope;

/**
 * One {@code adsblol-area-N} slot (prompt 062): every aircraft within {@code radiusNm} (at most 250 nm) of a
 * point the operator chooses, from {@code /v2/point/{lat}/{lon}/{radiusNm}}.
 *
 * <p>The parameters are validated numbers, never URLs, so the request can only reach the fixed adsb.lol
 * path. Interval 60 s (30 s minimum), budget 2 per minute and 1,500 per day per slot. An empty answer is
 * ordinary for a small area at night ({@code zeroResultsNormal}). Each result carries
 * {@code Coverage(region = the circle's envelope, actual = aircraft returned)}.
 */
public class AdsbLolAreaAdapter implements PollingAdapter {

    /** The id prefix of the slots. */
    public static final String ID_PREFIX = "adsblol-area-";

    /** The center latitude parameter. */
    public static final String LAT_PARAM = "lat";

    /** The center longitude parameter. */
    public static final String LON_PARAM = "lon";

    /** The radius parameter, in nautical miles. */
    public static final String RADIUS_PARAM = "radiusNm";

    /** The largest radius the adsb.lol API accepts. */
    public static final int MAX_RADIUS_NM = 250;

    /** Nautical miles per degree of latitude. */
    static final double NM_PER_DEGREE = 60.0;

    private final SourceDescriptor descriptor;
    private final String baseUrl;

    /**
     * Creates one slot.
     *
     * @param slot the slot number, 1 to 3
     * @param baseUrl the upstream base URL without a trailing slash
     */
    AdsbLolAreaAdapter(int slot, String baseUrl) {
        this.descriptor = buildDescriptor(slot);
        this.baseUrl = baseUrl;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** Checks the ranges of the center and radius (the {@code ParamValidator} cross-check). */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        Double lat = params.number(LAT_PARAM);
        Double lon = params.number(LON_PARAM);
        Integer radius = params.integer(RADIUS_PARAM);
        if (lat != null && (lat < -90.0 || lat > 90.0)) {
            problems.put(LAT_PARAM, "must be between -90 and 90, but was " + lat);
        }
        if (lon != null && (lon < -180.0 || lon > 180.0)) {
            problems.put(LON_PARAM, "must be between -180 and 180, but was " + lon);
        }
        if (radius != null && (radius < 1 || radius > MAX_RADIUS_NM)) {
            problems.put(RADIUS_PARAM, "must be between 1 and " + MAX_RADIUS_NM + " nm, but was " + radius);
        }
        return problems;
    }

    /** The request URL for a center and radius. */
    public URI uri(double lat, double lon, int radiusNm) {
        return URI.create(baseUrl + "/v2/point/" + plain(lat) + "/" + plain(lon) + "/" + radiusNm);
    }

    /**
     * Fetches the circle once.
     *
     * @throws AdapterException when the upstream fails or refuses this client
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        ValidatedParams params = ctx.params();
        double lat = params.number(LAT_PARAM);
        double lon = params.number(LON_PARAM);
        int radius = params.integer(RADIUS_PARAM, MAX_RADIUS_NM);
        UpstreamResponse response = ctx.http().get(uri(lat, lon, radius));
        ReadsbAircraftMapper.MapResult mapped = AdsbLol.parse(ctx, response);
        int returned = mapped.records().size() + mapped.skipped();
        return FetchResult.complete(mapped.records())
                .withCoverage(new Coverage(null, null, returned, envelope(lat, lon, radius)));
    }

    /**
     * The bounding envelope of a circle of {@code radiusNm} around a point: latitude clamped to the poles,
     * the whole longitude range when the circle reaches a pole or crosses the antimeridian.
     */
    static Envelope envelope(double lat, double lon, int radiusNm) {
        double dLat = radiusNm / NM_PER_DEGREE;
        double minLat = Math.max(-90.0, lat - dLat);
        double maxLat = Math.min(90.0, lat + dLat);
        double cos = Math.cos(Math.toRadians(lat));
        if (minLat <= -90.0 || maxLat >= 90.0 || cos < 1e-6) {
            return new Envelope(-180.0, 180.0, minLat, maxLat);
        }
        double dLon = dLat / cos;
        double minLon = lon - dLon;
        double maxLon = lon + dLon;
        if (minLon < -180.0 || maxLon > 180.0) {
            return new Envelope(-180.0, 180.0, minLat, maxLat);
        }
        return new Envelope(minLon, maxLon, minLat, maxLat);
    }

    /** A decimal without exponent or trailing zeros. */
    static String plain(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private static SourceDescriptor buildDescriptor(int slot) {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID_PREFIX + slot)
                .name("adsb.lol area " + slot)
                .description("Every aircraft within radiusNm (at most 250 nm) of an operator-chosen point, from"
                        + " adsb.lol community receivers (api.adsb.lol /v2/point). Disabled until lat and lon are set."
                        + " TRACK with a 6-minute TTL and a trail point every 60 s.");
        AdsbLol.upstream(builder)
                .interval(Duration.ofSeconds(30), Duration.ofSeconds(60))
                .defaultBudget(2, 1_500)
                .zeroResultsNormal(true)
                .param(ParamSpec.required(LAT_PARAM, ParamType.NUMBER, "Center latitude, -90 to 90"))
                .param(ParamSpec.required(LON_PARAM, ParamType.NUMBER, "Center longitude, -180 to 180"))
                .param(ParamSpec.optional(RADIUS_PARAM, ParamType.INTEGER,
                        "Radius in nautical miles, 1 to " + MAX_RADIUS_NM, MAX_RADIUS_NM));
        AircraftDescriptors.global(builder);
        builder.kindDisplay(AircraftFields.KIND, AircraftDescriptors.civilDisplay());
        return builder.build();
    }
}
