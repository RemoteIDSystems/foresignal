package ai.foresignal.source.adapters.env.airq;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldThreshold;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * One {@code airq-local-{n}} slot (prompt 078): the operator's own PurpleAir, airrohr or AirGradient monitor,
 * polled over the LAN ({@link AirqLocalParsers}).
 *
 * <ul>
 *   <li><strong>Params:</strong> {@value #DEVICE_TYPE_PARAM} ({@code purpleair|airrohr|airgradient}, required),
 *       {@value #URL_PARAM} ({@code LAN_URL}, the device's address; resolved once to a private address, pinned,
 *       no redirects), {@value #LAT_PARAM}/{@value #LON_PARAM} (required for airrohr and AirGradient, an override
 *       for PurpleAir), {@value #INDOOR_PARAM} (default from the device: PurpleAir {@code place}, AirGradient
 *       {@code I-}/{@code O-} model prefix, airrohr outdoor) and {@value #NAME_PARAM} (a label for log lines).
 *   <li><strong>Request:</strong> {@code /json?live=true}, {@code /data.json} or {@code /measures/current} under
 *       the device's address.
 *   <li><strong>Record:</strong> {@code externalId = deviceType + ":" + serial} (the slot id for airrohr, which
 *       names no serial), kind {@code indoor-air-quality} when indoor, else {@code air-quality}.
 *   <li><strong>Time base:</strong> RECEIVED (device clocks disagree; a LAN poll is near-instant); TTL 10 min
 *       anchored on {@code observedAt}.
 *   <li><strong>Policy</strong> (a deviation from Core-Data-Model §5.3, justified by 60-s sampling): SAMPLED,
 *       {@code minInterval} 60 s, {@code keyframe} 8 min (at most {@code ttl - poll - 30 s} = 510 s), significant
 *       {@code deviceModel, channelDisagreement}, thresholds {@code pm25} 5, {@code pm10} 10, {@code co2Ppm} 100,
 *       {@code tempC} 1, {@code humidityPct} 5.
 *   <li><strong>Privacy:</strong> {@code private} (the operator's home) and egress {@code LOCAL_ONLY}, overridable.
 * </ul>
 */
public class AirqLocalAdapter implements PollingAdapter {

    /** The id prefix of the slots. */
    public static final String ID_PREFIX = "airq-local-";

    /** The device type parameter. */
    public static final String DEVICE_TYPE_PARAM = "deviceType";

    /** The URL parameter. */
    public static final String URL_PARAM = "url";

    /** The latitude parameter. */
    public static final String LAT_PARAM = "lat";

    /** The longitude parameter. */
    public static final String LON_PARAM = "lon";

    /** The indoor parameter. */
    public static final String INDOOR_PARAM = "indoor";

    /** The name parameter. */
    public static final String NAME_PARAM = "name";

    /** PurpleAir. */
    public static final String PURPLEAIR = "purpleair";

    /** airrohr (Sensor.Community firmware). */
    public static final String AIRROHR = "airrohr";

    /** AirGradient. */
    public static final String AIRGRADIENT = "airgradient";

    /** The device types. */
    public static final List<String> DEVICE_TYPES = List.of(PURPLEAIR, AIRROHR, AIRGRADIENT);

    /** The record TTL. */
    static final Duration TTL = Duration.ofMinutes(10);

    /** Raw retention: sensor readings are the {@code state} class (365 d). */
    static final Duration RETENTION = Duration.ofDays(365);

    /** The default policy. */
    static final IngestPolicy POLICY = new IngestPolicy.Sampled(Duration.ofSeconds(60), Duration.ofMinutes(8), null,
            true, Duration.ofSeconds(30));

    private final SourceDescriptor descriptor;

    /**
     * Creates one slot.
     *
     * @param slot the slot number, from 1
     */
    public AirqLocalAdapter(int slot) {
        this.descriptor = buildDescriptor(slot);
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /**
     * The URL must be the device's address only; coordinates in range and given together; airrohr and AirGradient
     * need them.
     */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        URI url = params.lanUrl(URL_PARAM);
        if (url != null && (url.getRawUserInfo() != null || url.getRawQuery() != null
                || url.getRawFragment() != null)) {
            problems.put(URL_PARAM, "must be the device's address only, for example http://192.168.1.60");
        }
        Double lat = params.number(LAT_PARAM);
        Double lon = params.number(LON_PARAM);
        if (lat != null && (lat < -90 || lat > 90)) {
            problems.put(LAT_PARAM, "must be between -90 and 90, but was " + lat);
        }
        if (lon != null && (lon < -180 || lon > 180)) {
            problems.put(LON_PARAM, "must be between -180 and 180, but was " + lon);
        }
        if ((lat == null) != (lon == null)) {
            problems.put(lat == null ? LAT_PARAM : LON_PARAM, "lat and lon must be set together");
        }
        String type = params.optionalString(DEVICE_TYPE_PARAM).map(t -> t.toLowerCase(Locale.ROOT)).orElse(null);
        if ((AIRROHR.equals(type) || AIRGRADIENT.equals(type)) && (lat == null || lon == null)) {
            problems.putIfAbsent(lat == null ? LAT_PARAM : LON_PARAM, "is required for " + type
                    + ", which reports no location");
        }
        return problems;
    }

    /** The request path of {@code deviceType}. */
    static String path(String deviceType) {
        return switch (deviceType) {
            case PURPLEAIR -> "/json?live=true";
            case AIRROHR -> "/data.json";
            case AIRGRADIENT -> "/measures/current";
            default -> throw new IllegalArgumentException("Unknown device type '" + deviceType + "'");
        };
    }

    /**
     * Polls the device once.
     *
     * @throws AdapterException when the device type or URL is missing, or the device cannot be reached
     * @throws UpstreamFormatException when the answer is not the device's JSON or no location is known
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        ValidatedParams params = ctx.params();
        String type = params.optionalString(DEVICE_TYPE_PARAM).map(t -> t.toLowerCase(Locale.ROOT)).orElse(null);
        URI url = params.lanUrl(URL_PARAM);
        if (type == null || !DEVICE_TYPES.contains(type) || url == null) {
            throw new AdapterException(ctx.sourceId() + ": deviceType and url are required");
        }
        UpstreamResponse response = ctx.http().get(url.resolve(path(type)));
        if (response.status() != 200 || response.truncated()) {
            throw new UpstreamFormatException(ctx.sourceId() + ": the " + type + " device answered "
                    + (response.status() != 200 ? "HTTP " + response.status() : "more than the response cap"), "");
        }
        AirqLocalParsers.Reading reading = switch (type) {
            case PURPLEAIR -> AirqLocalParsers.purpleAir(response.body());
            case AIRROHR -> AirqLocalParsers.airrohr(response.body());
            default -> AirqLocalParsers.airGradient(response.body());
        };
        ctx.log().debug("{}: {} ({}) answered", ctx.sourceId(), params.optionalString(NAME_PARAM).orElse(type),
                reading.model());
        return FetchResult.complete(List.of(AirqLocalParsers.record(reading, type, ctx.sourceId(),
                params.has(INDOOR_PARAM) ? params.bool(INDOOR_PARAM) : null, params.number(LAT_PARAM),
                params.number(LON_PARAM))));
    }

    private static SourceDescriptor buildDescriptor(int slot) {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID_PREFIX + slot)
                .name("Local air-quality monitor " + slot)
                .description("The operator's own PurpleAir, airrohr (Sensor.Community firmware) or AirGradient"
                        + " monitor, polled on the LAN every 60 s: PM1, PM2.5, PM10, temperature, humidity,"
                        + " pressure and, where the device has them, CO2, VOC and NOx indexes. Indoor monitors are"
                        + " indoor-air-quality records. Private (the operator's home) and egress LOCAL_ONLY (the"
                        + " operator may set it to OPEN). The URL must resolve to a loopback, private or"
                        + " unique-local address; it is resolved once and pinned. Disabled by default.")
                .kinds(AirqLocalParsers.OUTDOOR_KIND, AirqLocalParsers.INDOOR_KIND)
                .homepage("https://sensor.community/en/sensors/airrohr/")
                .attribution("The operator's own monitor")
                .licence("Operator's own data (no upstream license)")
                .mode(Mode.POLLING)
                .param(new ParamSpec(DEVICE_TYPE_PARAM, ParamType.STRING, "The device type: purpleair, airrohr or"
                        + " airgradient", null, true, DEVICE_TYPES))
                .param(ParamSpec.lanUrl(URL_PARAM, "The device on the LAN, for example http://192.168.1.60 (its"
                        + " local API must be enabled)", true, "http", "https"))
                .param(new ParamSpec(LAT_PARAM, ParamType.NUMBER, "Latitude, -90 to 90; required for airrohr and"
                        + " AirGradient, overrides PurpleAir's own", null, false, List.of()))
                .param(new ParamSpec(LON_PARAM, ParamType.NUMBER, "Longitude, -180 to 180; required for airrohr and"
                        + " AirGradient, overrides PurpleAir's own", null, false, List.of()))
                .param(new ParamSpec(INDOOR_PARAM, ParamType.BOOLEAN, "True for an indoor monitor; default from the"
                        + " device (PurpleAir place, AirGradient model I-/O-, airrohr outdoor)", null, false,
                        List.of()))
                .param(new ParamSpec(NAME_PARAM, ParamType.STRING, "A label for log lines", null, false, List.of()))
                .interval(Duration.ofSeconds(30), Duration.ofSeconds(60))
                .defaultBudget(Budget.of(4, 3000))
                .fetchTimeout(Duration.ofSeconds(5))
                .maxResponseBytes(1024 * 1024)
                .followRedirects(false)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.RECEIVED)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(TTL)
                .defaultIngestPolicy(POLICY)
                .significantFields("deviceModel", "channelDisagreement")
                .threshold(FieldThreshold.of("pm25", 5))
                .threshold(FieldThreshold.of("pm10", 10))
                .threshold(FieldThreshold.of("co2Ppm", 100))
                .threshold(FieldThreshold.of("tempC", 1))
                .threshold(FieldThreshold.of("humidityPct", 5))
                .defaultRetention(RETENTION)
                .kindRetention(AirqLocalParsers.OUTDOOR_KIND, RETENTION)
                .kindRetention(AirqLocalParsers.INDOOR_KIND, RETENTION)
                .searchFields()
                .privateSource(true)
                .egress(Egress.LOCAL_ONLY)
                .egressOverridable(true);
        number(b, "pm1", "ug/m3", "PM1", "Particulate matter below 1 µm");
        number(b, "pm25", "ug/m3", "PM2.5", "Particulate matter below 2.5 µm (PurpleAir: mean of channels A and B)");
        number(b, "pm10", "ug/m3", "PM10", "Particulate matter below 10 µm");
        number(b, "tempC", "Cel", "Temperature", "Device temperature reading, uncorrected (PurpleAir reads warm)");
        number(b, "humidityPct", "%", "Humidity", "Relative humidity, uncorrected");
        number(b, "pressureHpa", "hPa", "Pressure", "Station pressure");
        number(b, "co2Ppm", "[ppm]", "CO2", "Carbon dioxide (AirGradient)");
        number(b, "tvocIndex", "1", "VOC index", "Sensirion VOC index (AirGradient)");
        number(b, "noxIndex", "1", "NOx index", "Sensirion NOx index (AirGradient)");
        b.fieldSpec("deviceModel", new FieldSpec(FieldType.STRING, null, "Device model",
                "The device model or firmware", null, Set.of()));
        b.fieldSpec("channelDisagreement", new FieldSpec(FieldType.BOOLEAN, null, "Channels disagree",
                "PurpleAir: PM2.5 channels A and B differ by more than 5 ug/m3 and more than 70%", null, Set.of()));
        b.kindDisplay(AirqLocalParsers.OUTDOOR_KIND, new KindDisplay("PM2.5 {pm25}", null, "#2F9E44", null));
        b.kindDisplay(AirqLocalParsers.INDOOR_KIND, new KindDisplay("PM2.5 {pm25}", "home", "#5F3DC4", null));
        return b.build();
    }

    private static void number(SourceDescriptor.Builder b, String key, String unit, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.NUMBER, unit, label, description, null, Set.of()));
    }
}
