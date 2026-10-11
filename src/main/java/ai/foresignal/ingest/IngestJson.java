package ai.foresignal.ingest;

import ai.foresignal.source.spi.Geo;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The writer's encodings for {@code jsonb} and geometry columns.
 *
 * <p>Payloads hold only JSON types after {@link JsonValues#normalize}, so Jackson writes them
 * exactly as they were hashed. Values read back are converted to the same types, so a payload
 * loaded from the database hashes like the one that was stored. {@code kinematic} keeps its JSON
 * nulls, which mask persisted keys the latest observation dropped. Geometry travels as
 * two-dimensional WKB with SRID 4326 applied on both sides ({@code ST_GeomFromWKB(?, 4326)},
 * {@link Geo#factory()}).
 */
public final class IngestJson {

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final JsonMapper mapper;

    /** A codec using {@code mapper}. */
    public IngestJson(JsonMapper mapper) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    /** {@code value} as JSON text; null for null. */
    public String write(Object value) {
        return value == null ? null : mapper.writeValueAsString(value);
    }

    /** The UTF-8 length of {@code json}, 0 for null. */
    public static int utf8Length(String json) {
        return json == null ? 0 : json.getBytes(StandardCharsets.UTF_8).length;
    }

    /**
     * A JSON object read as JSON-typed values with nulls dropped (a payload); empty for null text.
     */
    public Map<String, Object> readPayload(String json) {
        if (json == null) {
            return Map.of();
        }
        return JsonValues.normalize(mapper.readValue(json, MAP));
    }

    /**
     * A JSON object read with its top-level JSON nulls kept (a {@code kinematic} overlay); empty
     * for null text.
     */
    public Map<String, Object> readKeepingNulls(String json) {
        if (json == null) {
            return Map.of();
        }
        Map<String, Object> raw = mapper.readValue(json, MAP);
        Map<String, Object> result = new LinkedHashMap<>();
        raw.forEach((key, value) -> result.put(key, value == null ? null : value(value)));
        return Collections.unmodifiableMap(result);
    }

    /** Any JSON value read as a JSON-typed Java value; null for null text or JSON null. */
    public Object readValue(String json) {
        if (json == null) {
            return null;
        }
        Object raw = mapper.readValue(json, Object.class);
        return raw == null ? null : value(raw);
    }

    /** A JSON object read as a plain map, or null for null text. */
    public Map<String, Object> readMap(String json) {
        return json == null ? null : mapper.readValue(json, MAP);
    }

    private static Object value(Object raw) {
        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put("v", raw);
        return JsonValues.normalize(wrapper).get("v");
    }

    /** {@code geometry} as 2D WKB, or null. */
    public static byte[] wkb(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }
        return new WKBWriter(2).write(geometry);
    }

    /** WKB read with SRID 4326, or null for null bytes. */
    public static Geometry geometry(byte[] wkb) {
        if (wkb == null) {
            return null;
        }
        try {
            Geometry geometry = new WKBReader(Geo.factory()).read(wkb);
            geometry.setSRID(Geo.SRID);
            return geometry;
        } catch (ParseException e) {
            throw new IllegalStateException("Stored geometry is not valid WKB", e);
        }
    }
}
