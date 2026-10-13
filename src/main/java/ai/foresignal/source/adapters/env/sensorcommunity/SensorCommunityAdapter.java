package ai.foresignal.source.adapters.env.sensorcommunity;

import ai.foresignal.source.adapters.env.EnvSources;
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
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.source.support.SkipCounter;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code sensor-community} (prompt 078): Sensor.Community (formerly luftdaten.info) sensors in one operator-chosen
 * area, as {@code air-quality} (and, with {@code includeIndoor}, {@code indoor-air-quality}) point records
 * ({@link SensorCommunityParser}).
 *
 * <p><strong>Each run:</strong> one request for {@value #FILTER_URL}{@code {area}} (the last 5 minutes of
 * readings in the area, no validators). The global dumps {@code static/v2/data.json} and {@code data.1h.json} are
 * never read: on 2026-10-13 each was 8.5–8.7 MB with only {@code Last-Modified}, gigabytes a day at this cadence;
 * a Berlin box was 510 KB.
 */
@Component
public class SensorCommunityAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "sensor-community";

    /** The area filter API; the {@code area} parameter's filter follows. */
    public static final String FILTER_URL = "https://data.sensor.community/airrohr/v1/filter/";

    /** The area parameter. */
    public static final String AREA_PARAM = "area";

    /** The indoor parameter. */
    public static final String INDOOR_PARAM = "includeIndoor";

    /** The record TTL. */
    static final Duration TTL = Duration.ofHours(1);

    /** Raw retention: sensor readings are the {@code state} class (365 d). */
    static final Duration RETENTION = Duration.ofDays(365);

    private final String filterRoot;
    private final Supplier<MeterRegistry> meters;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value EnvSources#BASE_URL_PROPERTY}; blank in production
     * @param meters the registry of the skip counter, resolved lazily
     */
    @Autowired
    public SensorCommunityAdapter(@Value("${" + EnvSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<MeterRegistry> meters) {
        this(baseUrlOverride, meters == null ? () -> null : (Supplier<MeterRegistry>) meters::getIfAvailable);
    }

    /**
     * Creates the adapter with a plain supplier (tests).
     *
     * @param baseUrlOverride the test base URL, or blank
     * @param meters supplies the meter registry, or null
     */
    public SensorCommunityAdapter(String baseUrlOverride, Supplier<MeterRegistry> meters) {
        this.filterRoot = EnvSources.uri(FILTER_URL, baseUrlOverride).toString();
        this.meters = meters;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The area must be one valid filter ({@link SensorCommunityArea}). */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        String problem = SensorCommunityArea.check(params.has(AREA_PARAM) ? params.string(AREA_PARAM) : null);
        if (problem != null) {
            problems.put(AREA_PARAM, problem);
        }
        return problems;
    }

    /** The request URL of a valid {@code area} value. */
    public URI filterUri(String area) {
        return URI.create(filterRoot + SensorCommunityArea.path(area));
    }

    /**
     * Reads the area once.
     *
     * @throws AdapterException when {@code area} is missing or invalid, or the request fails
     * @throws UpstreamFormatException on a bad status, a truncated body or a body that is not a JSON array
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        ValidatedParams params = ctx.params();
        String area = params != null && params.has(AREA_PARAM) ? params.string(AREA_PARAM) : null;
        String problem = SensorCommunityArea.check(area);
        if (problem != null) {
            throw new AdapterException(ID + ": area " + problem);
        }
        URI uri = filterUri(area);
        EnvSources.Parsed parsed = SensorCommunityParser.parse(
                EnvSources.body(ctx, ctx.http().get(uri, Map.of("Accept", "application/json")), null, uri),
                params.bool(INDOOR_PARAM, false), ctx.now(),
                EnvSources.maxAge(params, SensorCommunityParser.DEFAULT_MAX_AGE));
        SkipCounter.count(meters, EnvSources.SKIPPED_METRIC, EnvSources.SKIPPED_DESCRIPTION, ctx.sourceId(),
                parsed.skipped());
        ctx.log().debug("{}: {} sensor(s), skipped {}", ID, parsed.records().size(), parsed.skipped());
        return FetchResult.of(parsed.records());
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("Sensor.Community")
                .description("Citizen air-quality sensors (Sensor.Community, formerly luftdaten.info) in one area:"
                        + " PM10, PM2.5, temperature, humidity and pressure per sensor, the newest reading of the last"
                        + " 5 minutes. area is required (box=lat1,lon1,lat2,lon2 up to 10 x 10 degrees,"
                        + " area=lat,lon,radiusKm or country=XX); indoor sensors are left out unless includeIndoor."
                        + " The global data dumps are never downloaded.")
                .kinds(SensorCommunityParser.OUTDOOR_KIND, SensorCommunityParser.INDOOR_KIND)
                .homepage("https://sensor.community/")
                .attribution("Sensor.Community")
                .licence("ODbL 1.0 (database) / DbCL 1.0 (contents)")
                .termsUrl("https://github.com/opendata-stuttgart/meta/wiki/EN-APIs")
                .mode(Mode.POLLING)
                .param(ParamSpec.required(AREA_PARAM, ParamType.STRING, "The area filter: box=lat1,lon1,lat2,lon2 (at"
                        + " most 10 degrees a side), area=lat,lon,radiusKm (at most 500 km) or country=XX"))
                .param(ParamSpec.optional(INDOOR_PARAM, ParamType.BOOLEAN, "Also keep indoor sensors, as"
                        + " indoor-air-quality records", false))
                .param(EnvSources.maxAgeParam("6h"))
                .interval(Duration.ofMinutes(5), Duration.ofMinutes(5))
                .defaultBudget(Budget.of(2, 400))
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(TTL)
                .defaultRetention(RETENTION)
                .kindRetention(SensorCommunityParser.OUTDOOR_KIND, RETENTION)
                .kindRetention(SensorCommunityParser.INDOOR_KIND, RETENTION)
                .zeroResultsNormal(true)
                .tileProperties("pm25", "pm10", "tempC")
                .searchFields("sensorType")
                .rankField("pm25");
        b.fieldSpec("sensorType", new FieldSpec(FieldType.STRING, null, "Sensor type", "Sensor model (SDS011, BME280,"
                + " DHT22, …)", null, Set.of()));
        number(b, "pm10", "ug/m3", "PM10", "Particulate matter below 10 µm (P1)");
        number(b, "pm25", "ug/m3", "PM2.5", "Particulate matter below 2.5 µm (P2)");
        number(b, "tempC", "Cel", "Temperature", "Air temperature");
        number(b, "humidityPct", "%", "Humidity", "Relative humidity");
        number(b, "pressureHpa", "hPa", "Pressure", "Station pressure");
        b.fieldSpec("exactLocation", new FieldSpec(FieldType.BOOLEAN, null, "Exact location",
                "False when Sensor.Community publishes the location rounded", null, Set.of()));
        b.kindDisplay(SensorCommunityParser.OUTDOOR_KIND, new KindDisplay("PM2.5 {pm25}", null, "#2F9E44", null));
        b.kindDisplay(SensorCommunityParser.INDOOR_KIND, new KindDisplay("PM2.5 {pm25}", "home", "#5F3DC4", null));
        return b.build();
    }

    private static void number(SourceDescriptor.Builder b, String key, String unit, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.NUMBER, unit, label, description, null, Set.of()));
    }
}
