package ai.foresignal.source.adapters.local;

import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The record model the local weather stations share (prompt 069): Davis WeatherLink Live and Ecowitt
 * gateways emit {@code weather-observation} records in SI units with the payload keys of the 059
 * {@code ecowitt} and {@code wu-pws} presets.
 *
 * <ul>
 *   <li><strong>Keys:</strong> {@code tempC, rh, dewPointC, windSpeedMs, windDirDeg, windGustMs,
 *       rainRateMmH, rainDailyMm, pressureHpa}, plus {@code pressureTrendHpa} and {@code batteryLow}
 *       (Davis), {@code solarWm2} and {@code uvIndex} (Ecowitt), and
 *       {@code indoorTempC, indoorRh} only with {@code includeIndoor}.
 *   <li><strong>Geometry:</strong> {@code lat} and {@code lon} params rounded to 2 decimals (about 1 km),
 *       coarse on purpose: the station is at the operator's home.
 *   <li><strong>Policy:</strong> STANDARD, ALL, TTL 10 min anchored on {@code observedAt}, retention 365 d
 *       ({@code state}: sensor status).
 * </ul>
 */
final class WeatherStations {

    /** The record kind. */
    static final String KIND = "weather-observation";

    /** The latitude parameter. */
    static final String LAT_PARAM = "lat";

    /** The longitude parameter. */
    static final String LON_PARAM = "lon";

    /** The indoor parameter. */
    static final String INDOOR_PARAM = "includeIndoor";

    /** The URL parameter. */
    static final String URL_PARAM = "url";

    /** The record TTL. */
    static final Duration TTL = Duration.ofMinutes(10);

    /** The {@code state} class retention. */
    static final Duration RETENTION = Duration.ofDays(365);

    /** Coordinate decimals kept: about 1 km. */
    static final int COORDINATE_DECIMALS = 2;

    static final double MPH_TO_MPS = 0.44704;
    static final double KMH_TO_MPS = 1.0 / 3.6;
    static final double KNOT_TO_MPS = 0.514444;
    static final double FPS_TO_MPS = 0.3048;
    static final double INHG_TO_HPA = 33.8639;
    static final double MMHG_TO_HPA = 1.33322;
    static final double INCH_TO_MM = 25.4;

    private WeatherStations() {}

    /** Degrees Fahrenheit to Celsius, to 0.1. */
    static double fahrenheitToCelsius(double f) {
        return LocalReceivers.round((f - 32.0) * 5.0 / 9.0, 1);
    }

    /** Miles per hour to m/s, to 0.1. */
    static double mphToMps(double mph) {
        return LocalReceivers.round(mph * MPH_TO_MPS, 1);
    }

    /** Inches of mercury to hPa, to 0.1. */
    static double inHgToHpa(double inHg) {
        return LocalReceivers.round(inHg * INHG_TO_HPA, 1);
    }

    /** A coordinate rounded to {@link #COORDINATE_DECIMALS}. */
    static double coarse(double degrees) {
        return LocalReceivers.round(degrees, COORDINATE_DECIMALS);
    }

    /** The coordinate ranges, for {@code checkParams}. */
    static Map<String, String> checkCoordinates(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        Double lat = params.number(LAT_PARAM);
        Double lon = params.number(LON_PARAM);
        if (lat != null && (lat < -90 || lat > 90)) {
            problems.put(LAT_PARAM, "must be between -90 and 90, but was " + lat);
        }
        if (lon != null && (lon < -180 || lon > 180)) {
            problems.put(LON_PARAM, "must be between -180 and 180, but was " + lon);
        }
        return problems;
    }

    /**
     * Applies the shared descriptor: kind, polling, the params, the policy and the field specs.
     *
     * @param minInterval the shortest poll interval
     */
    static SourceDescriptor.Builder apply(SourceDescriptor.Builder b, Duration minInterval, String urlDescription) {
        b.kind(KIND)
                .mode(Mode.POLLING)
                .interval(minInterval, Duration.ofSeconds(60))
                .defaultBudget(6, 10_000)
                .fetchTimeout(Duration.ofSeconds(5))
                .maxResponseBytes(1024 * 1024)
                .followRedirects(false)
                .historyTier(HistoryTier.STANDARD)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(TTL)
                .defaultRetention(RETENTION)
                .defaultIngestPolicy(IngestPolicy.all())
                .tileProperties("tempC", "rh", "windSpeedMs", "windDirDeg", "rainRateMmH")
                .searchFields()
                .param(ParamSpec.lanUrl(URL_PARAM, urlDescription, true, "http"))
                .param(ParamSpec.required(LAT_PARAM, ParamType.NUMBER,
                        "Station latitude, -90 to 90; stored rounded to 2 decimals (about 1 km)"))
                .param(ParamSpec.required(LON_PARAM, ParamType.NUMBER,
                        "Station longitude, -180 to 180; stored rounded to 2 decimals (about 1 km)"))
                .param(ParamSpec.optional(INDOOR_PARAM, ParamType.BOOLEAN,
                        "Also store the console's indoor temperature and humidity", false));
        LocalReceivers.local(b);
        number(b, "tempC", "Cel", "Temperature", "Outdoor air temperature");
        number(b, "rh", "%", "Relative humidity", "Outdoor relative humidity");
        number(b, "dewPointC", "Cel", "Dew point", "Outdoor dew point");
        number(b, "windSpeedMs", "m/s", "Wind speed", "Most recent wind speed");
        number(b, "windDirDeg", "deg", "Wind direction", "Direction the wind blows from, degrees from true north");
        number(b, "windGustMs", "m/s", "Wind gust", "Highest wind speed (Davis: last 10 minutes)");
        number(b, "rainRateMmH", "mm/h", "Rain rate", "Most recent rain rate");
        number(b, "rainDailyMm", "mm", "Rain today", "Rain since local midnight");
        number(b, "pressureHpa", "hPa", "Pressure", "Sea-level (relative) pressure");
        number(b, "pressureTrendHpa", "hPa", "Pressure trend", "Change over the last 3 hours (Davis)");
        number(b, "solarWm2", "W/m2", "Solar radiation", "Solar radiation");
        number(b, "uvIndex", "1", "UV index", "UV index");
        number(b, "indoorTempC", "Cel", "Indoor temperature", "Console temperature (includeIndoor)");
        number(b, "indoorRh", "%", "Indoor humidity", "Console relative humidity (includeIndoor)");
        b.fieldSpec("batteryLow", new FieldSpec(FieldType.BOOLEAN, null, "Battery low",
                "True when the outdoor transmitter reports a low battery (Davis)", null, Set.of()));
        b.kindDisplay(KIND, new KindDisplay("{tempC} °C", "forecast", "#1971C2", null));
        return b;
    }

    private static void number(SourceDescriptor.Builder b, String key, String unit, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.NUMBER, unit, label, description, null, Set.of()));
    }
}
