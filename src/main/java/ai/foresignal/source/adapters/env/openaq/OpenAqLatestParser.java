package ai.foresignal.source.adapters.env.openaq;

import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import ai.foresignal.source.support.MappedRecord;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * One page of OpenAQ v3 {@code GET /v3/parameters/{id}/latest} as records (prompt 078). Pure.
 *
 * <p>The v3 OpenAPI document (verified 2026-10) gives each result as {@code {datetime: {utc, local}, value,
 * coordinates: {latitude, longitude}, sensorsId, locationsId}}; the parameter name and unit are not repeated per
 * result, so they come from the resolved {@link OpenAqParameterResolver.Parameter}.
 *
 * <ul>
 *   <li>Kind {@value #KIND}, {@code externalId = locationsId + ":" + parameter}, a point from
 *       {@code coordinates};
 *   <li>payload {@code parameter, value, unit} (as a UCUM code: {@code ug/m3}, {@code [ppm]}), {@code sensorId,
 *       locationId};
 *   <li>{@code observedAt = eventTime = datetime.utc}.
 * </ul>
 */
public final class OpenAqLatestParser {

    /** The record kind. */
    public static final String KIND = "air-quality";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /**
     * One page.
     *
     * @param entries one mapped entry per result
     * @param resultCount how many results the page held (a page shorter than the limit is the last)
     */
    public record Page(List<MappedRecord> entries, int resultCount) {

        /** Copies the entries. */
        public Page {
            entries = List.copyOf(entries);
        }
    }

    private OpenAqLatestParser() {}

    /**
     * Parses one page for {@code parameter}.
     *
     * @throws UpstreamFormatException when the body is not a {@code LatestResponse}
     */
    public static Page parse(byte[] body, OpenAqParameterResolver.Parameter parameter) {
        JsonNode root;
        try {
            root = body == null || body.length == 0 ? null : JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("OpenAQ latest answer is not JSON: " + e.getOriginalMessage(), "", e);
        }
        JsonNode results = root == null ? null : root.get("results");
        if (results == null || !results.isArray()) {
            throw new UpstreamFormatException("OpenAQ latest answer has no results array", "");
        }
        List<MappedRecord> out = new ArrayList<>();
        for (JsonNode r : results) {
            out.add(map(r, parameter));
        }
        return new Page(out, results.size());
    }

    static MappedRecord map(JsonNode r, OpenAqParameterResolver.Parameter parameter) {
        Long locationId = JsonProps.integer(r, "locationsId");
        if (locationId == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        Instant at = JsonProps.iso(r.get("datetime"), "utc");
        if (at == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_OBSERVED_AT);
        }
        JsonNode coordinates = r.get("coordinates");
        Double lat = JsonProps.number(coordinates, "latitude");
        Double lon = JsonProps.number(coordinates, "longitude");
        if (!EnvSources.validPoint(lat, lon)) {
            return MappedRecord.skipped(EnvSources.MISSING_LOCATION);
        }
        Double value = JsonProps.number(r, "value");
        if (value == null) {
            return MappedRecord.skipped(EnvSources.NO_VALUES);
        }
        return MappedRecord.of(NormalizedRecord.builder(locationId + ":" + parameter.name(), KIND)
                .observedAt(at)
                .eventTime(at)
                .point(lon, lat)
                .put("parameter", parameter.name())
                .put("value", value)
                .put("unit", EnvSources.ucum(parameter.units()))
                .put("sensorId", JsonProps.integer(r, "sensorsId"))
                .put("locationId", locationId)
                .build());
    }
}
