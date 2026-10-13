package ai.foresignal.source.adapters.adsbfi;

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
import ai.foresignal.source.spi.ValidatedParams;
import java.math.BigDecimal;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.locationtech.jts.geom.Envelope;

/**
 * One {@code adsbfi-region-N} slot (prompt 080): every aircraft within {@code distNm} (at most 250 nm, the
 * documented maximum) of a point the operator chooses, from {@code /api/v3/lat/{lat}/lon/{lon}/dist/{distNm}}.
 *
 * <p>The parameters are validated numbers, never URLs, so the request can only reach the fixed adsb.fi path.
 * Records carry {@code externalId = hex} like every aircraft source, so an aircraft in two overlapping slots is
 * the same entity id in two sources; fusing them is the derived layers' work (prompt 099), not this adapter's.
 * An empty answer is ordinary for a small area at night ({@code zeroResultsNormal}). Each result carries
 * {@code Coverage(region = the circle's envelope, actual = aircraft returned)}.
 */
public class AdsbFiRegionAdapter implements PollingAdapter {

    /** The id prefix of the slots. */
    public static final String ID_PREFIX = "adsbfi-region-";

    /** The center latitude parameter. */
    public static final String LAT_PARAM = "lat";

    /** The center longitude parameter. */
    public static final String LON_PARAM = "lon";

    /** The radius parameter, in nautical miles. */
    public static final String DIST_PARAM = "distNm";

    /** The operator's name for the slot, used in log lines only. */
    public static final String LABEL_PARAM = "label";

    /** The default radius. */
    public static final int DEFAULT_DIST_NM = 150;

    /** The largest radius adsb.fi documents for {@code /v3/lat/lon/dist}. */
    public static final int MAX_DIST_NM = 250;

    /** Nautical miles per degree of latitude. */
    static final double NM_PER_DEGREE = 60.0;

    private final SourceDescriptor descriptor;
    private final String origin;

    /**
     * Creates one slot.
     *
     * @param slot the slot number, 1 to {@link AdsbFiRegionFamily#SLOTS}
     * @param origin the upstream origin without a trailing slash
     */
    AdsbFiRegionAdapter(int slot, String origin) {
        this.descriptor = buildDescriptor(slot);
        this.origin = origin;
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
        Integer dist = params.integer(DIST_PARAM);
        if (lat != null && (lat < -90.0 || lat > 90.0)) {
            problems.put(LAT_PARAM, "must be between -90 and 90, but was " + lat);
        }
        if (lon != null && (lon < -180.0 || lon > 180.0)) {
            problems.put(LON_PARAM, "must be between -180 and 180, but was " + lon);
        }
        if (dist != null && (dist < 1 || dist > MAX_DIST_NM)) {
            problems.put(DIST_PARAM, "must be between 1 and " + MAX_DIST_NM + " nm, but was " + dist);
        }
        return problems;
    }

    /** The request URL for a center and radius. */
    public URI uri(double lat, double lon, int distNm) {
        return URI.create(origin + "/api/v3/lat/" + plain(lat) + "/lon/" + plain(lon) + "/dist/" + distNm);
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
        int dist = params.integer(DIST_PARAM, DEFAULT_DIST_NM);
        ReadsbAircraftMapper.MapResult mapped = AdsbFi.parse(ctx, ctx.http().get(uri(lat, lon, dist)));
        int returned = mapped.records().size() + mapped.skipped();
        params.optionalString(LABEL_PARAM).ifPresent(label ->
                ctx.log().debug("{} ({}): {} aircraft", ctx.sourceId(), label, returned));
        return FetchResult.complete(mapped.records())
                .withCoverage(new Coverage(null, null, returned, envelope(lat, lon, dist)));
    }

    /**
     * The bounding envelope of a circle of {@code distNm} around a point: latitude clamped to the poles, the whole
     * longitude range when the circle reaches a pole or crosses the antimeridian.
     */
    static Envelope envelope(double lat, double lon, int distNm) {
        double dLat = distNm / NM_PER_DEGREE;
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
                .name("adsb.fi region " + slot)
                .description("Every aircraft within distNm (default 150, at most 250 nm) of an operator-chosen point,"
                        + " from adsb.fi community receivers (opendata.adsb.fi /api/v3/lat/lon/dist). Disabled until"
                        + " lat and lon are set. Personal, non-commercial use only; cite adsb.fi. Requests to"
                        + " opendata.adsb.fi are serial across every adsbfi-* source, at least 1.1 s apart. TRACK"
                        + " with a 6-minute TTL and the 240 s keyframe (holds up to a 90 s interval).");
        AdsbFi.upstream(builder)
                .zeroResultsNormal(true)
                .param(ParamSpec.required(LAT_PARAM, ParamType.NUMBER, "Center latitude, -90 to 90"))
                .param(ParamSpec.required(LON_PARAM, ParamType.NUMBER, "Center longitude, -180 to 180"))
                .param(ParamSpec.optional(DIST_PARAM, ParamType.INTEGER,
                        "Radius in nautical miles, 1 to " + MAX_DIST_NM, DEFAULT_DIST_NM))
                .param(ParamSpec.optional(LABEL_PARAM, ParamType.STRING,
                        "A name for this slot, shown in log lines (for example the airport it watches)", null));
        AircraftDescriptors.global(builder);
        builder.kindDisplay(AircraftFields.KIND, AircraftDescriptors.civilDisplay());
        return builder.build();
    }
}
