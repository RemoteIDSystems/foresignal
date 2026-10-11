package ai.foresignal.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * The {@code ParamSpec} schema: one adapter parameter.
 *
 * @param name the parameter key
 * @param type the wire type ({@code string}, {@code integer}, {@code duration}, ...)
 * @param description what it does
 * @param defaultValue the default, serialized as {@code default}
 * @param required whether a value must be supplied
 * @param allowed the allowed values, empty when unrestricted
 */
public record ParamSpecDto(
        String name,
        String type,
        String description,
        @JsonProperty("default") Object defaultValue,
        boolean required,
        List<String> allowed) {}
