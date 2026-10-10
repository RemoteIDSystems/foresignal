package ai.elaro.settings;

import ai.elaro.api.error.InvalidInputException;
import java.time.Duration;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

/**
 * Teaches Jackson to read and write {@link Duration} in Elaro's shorthand.
 *
 * <p>Without this, Jackson emits ISO-8601 ({@code PT168H}) or, with the JSR-310 defaults, a
 * floating-point number of seconds. The API contract shows {@code "7d"}, so durations are written
 * with {@link Durations#format(Duration)} and read with {@link Durations#parse(String)}, which also
 * accepts ISO-8601 for callers that prefer it.
 *
 * <p>Registered on Boot's {@code JsonMapper} by {@link JacksonDurationConfig}, so it applies to API
 * DTOs and to the settings values stored as {@code jsonb}.
 */
public final class DurationShorthandModule extends SimpleModule {

    public DurationShorthandModule() {
        super("elaro-duration-shorthand");
        addSerializer(Duration.class, new ShorthandSerializer());
        addDeserializer(Duration.class, new ShorthandDeserializer());
    }

    /** Writes a duration as shorthand, for example {@code 7d}. */
    static final class ShorthandSerializer extends ValueSerializer<Duration> {

        @Override
        public void serialize(Duration value, JsonGenerator gen, SerializationContext ctx) {
            gen.writeString(Durations.format(value));
        }
    }

    /** Reads shorthand or ISO-8601, and a bare number as seconds. */
    static final class ShorthandDeserializer extends ValueDeserializer<Duration> {

        @Override
        public Duration deserialize(JsonParser parser, DeserializationContext ctx) {
            JsonToken token = parser.currentToken();
            if (token == JsonToken.VALUE_NUMBER_INT) {
                long seconds = parser.getLongValue();
                if (seconds < 0) {
                    throw new InvalidInputException(
                            "Duration must not be negative, but was " + seconds + " seconds");
                }
                return Duration.ofSeconds(seconds);
            }
            return Durations.parse(parser.getString());
        }
    }
}
