package ai.foresignal.api.support;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * A JSON text produced by PostgreSQL ({@code ST_AsGeoJSON}, {@code jsonb::text}) and written into a
 * response verbatim, without being parsed and re-serialized.
 *
 * @param json well-formed JSON text
 */
@JsonSerialize(using = RawJson.Serializer.class)
public record RawJson(String json) {

    /** Wraps {@code json}, or returns null for null. */
    public static RawJson of(String json) {
        return json == null ? null : new RawJson(json);
    }

    @Override
    public String toString() {
        return json;
    }

    /** Writes the text as a raw JSON value. */
    public static final class Serializer extends ValueSerializer<RawJson> {

        @Override
        public void serialize(RawJson value, JsonGenerator gen, SerializationContext ctx) {
            gen.writeRawValue(value.json());
        }
    }
}
