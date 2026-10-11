package ai.foresignal.source.spi;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Client display hints for one kind (descriptor {@code kindDisplay}, OpenAPI schema
 * {@code KindDisplay}).
 *
 * <p>{@code titleTemplate} is a client hint only; the server-side title comes from
 * {@code ai.foresignal.ingest.TitleResolver} and never from this template.
 *
 * <p>The color is serialized under the JSON key {@code colour}, which is the published wire name in
 * the OpenAPI document; the Java accessor is {@link #color()}.
 *
 * @param titleTemplate a client title template with {@code {field}} placeholders
 * @param icon an icon name
 * @param color {@code #RRGGBB}
 * @param sidc an optional MIL-STD-2525 symbol identification code of 15 or 20 characters
 */
public record KindDisplay(
        String titleTemplate, String icon, @JsonProperty("colour") String color, String sidc) {}
