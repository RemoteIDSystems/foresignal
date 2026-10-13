package ai.foresignal.source.adapters.env.airnow;

import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import ai.foresignal.source.support.MappedRecord;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The AirNow API "Observations by Monitoring Site" answer ({@code /aq/data/?…&format=application/json&verbose=1
 * &dataType=B}) as {@code air-quality} records (prompt 078). Pure.
 *
 * <p>The query documentation is behind the AirNow API login, so the field list below is unverified against a live
 * answer (the fixture is synthetic, to be recaptured with a key): a JSON array of {@code {Latitude, Longitude, UTC,
 * Parameter, Unit, Value, AQI, Category, SiteName, AgencyName, FullAQSCode, IntlAQSCode}} with {@code -999} for a
 * missing number.
 *
 * <ul>
 *   <li>{@code externalId = FullAQSCode + ":" + Parameter}, a point from {@code Latitude}/{@code Longitude};
 *   <li>payload {@code parameter, value, unit} (as a UCUM code: {@code ug/m3}, {@code [ppb]}, {@code [ppm]}),
 *       {@code aqi}, {@code aqiCategory} (the EPA category number 1–6), {@code siteName}, {@code agency};
 *   <li>{@code observedAt = eventTime = UTC} (the start of the hour the value averages).
 * </ul>
 */
public final class AirNowParser {

    /** The record kind. */
    public static final String KIND = "air-quality";

    /** AirNow's missing-number marker. */
    static final double MISSING = -999;

    /** The default {@code maxAge}. */
    public static final Duration DEFAULT_MAX_AGE = Duration.ofHours(6);

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private AirNowParser() {}

    /**
     * Parses one answer.
     *
     * @param now the fetch time
     * @param maxAge the oldest value kept
     * @throws UpstreamFormatException when the body is not a JSON array (AirNow answers errors as an object or
     *     XML)
     */
    public static EnvSources.Parsed parse(byte[] body, Instant now, Duration maxAge) {
        JsonNode root;
        try {
            root = body == null || body.length == 0 ? null : JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("AirNow answer is not JSON: " + e.getOriginalMessage(), "", e);
        }
        if (root == null || !root.isArray()) {
            throw new UpstreamFormatException("AirNow answer is not a JSON array", root == null ? ""
                    : root.toString().substring(0, Math.min(200, root.toString().length())));
        }
        EnvSources.Collector collector = new EnvSources.Collector(now, maxAge);
        for (JsonNode row : root) {
            collector.add(map(row));
        }
        return collector.result();
    }

    static MappedRecord map(JsonNode r) {
        String site = JsonProps.text(r, "FullAQSCode");
        if (site == null) {
            site = JsonProps.text(r, "IntlAQSCode");
        }
        String parameter = JsonProps.text(r, "Parameter");
        if (site == null || parameter == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        Instant at = hour(JsonProps.text(r, "UTC"));
        if (at == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_OBSERVED_AT);
        }
        Double lat = present(JsonProps.number(r, "Latitude"));
        Double lon = present(JsonProps.number(r, "Longitude"));
        if (!EnvSources.validPoint(lat, lon)) {
            return MappedRecord.skipped(EnvSources.MISSING_LOCATION);
        }
        Double value = present(JsonProps.number(r, "Value"));
        Double aqi = present(JsonProps.number(r, "AQI"));
        if (value == null && aqi == null) {
            return MappedRecord.skipped(EnvSources.NO_VALUES);
        }
        Double category = present(JsonProps.number(r, "Category"));
        return MappedRecord.of(NormalizedRecord.builder(site + ":" + parameter, KIND)
                .observedAt(at)
                .eventTime(at)
                .point(lon, lat)
                .put("parameter", parameter)
                .put("value", value)
                .put("unit", EnvSources.ucum(JsonProps.text(r, "Unit")))
                .put("aqi", aqi == null ? null : Math.round(aqi))
                .put("aqiCategory", category == null ? null : Math.round(category))
                .put("siteName", JsonProps.text(r, "SiteName"))
                .put("agency", JsonProps.text(r, "AgencyName"))
                .build());
    }

    /** {@code 2026-10-13T20:00} (or with seconds or a zone) as a UTC instant, or null. */
    static Instant hour(String text) {
        if (text == null) {
            return null;
        }
        Instant iso = JsonProps.iso(JSON.createObjectNode().put("t", text), "t");
        if (iso != null) {
            return iso;
        }
        try {
            return LocalDateTime.parse(text.strip()).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static Double present(Double v) {
        return v == null || v == MISSING ? null : v;
    }
}
