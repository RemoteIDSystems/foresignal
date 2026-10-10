package ai.elaro.store;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Stores {@link TriggerMode} as the lowercase wire value from Architecture §7.1
 * ({@code match}/{@code enter}/{@code exit}).
 *
 * <p>{@code @Enumerated(STRING)} would write the enum name in uppercase, which would not match the
 * documented column values or the API representation.
 */
@Converter(autoApply = true)
public class TriggerModeConverter implements AttributeConverter<TriggerMode, String> {

    @Override
    public String convertToDatabaseColumn(TriggerMode mode) {
        return mode == null ? TriggerMode.MATCH.wireValue() : mode.wireValue();
    }

    @Override
    public TriggerMode convertToEntityAttribute(String value) {
        if (value == null || value.isBlank()) {
            return TriggerMode.MATCH;
        }
        return TriggerMode.valueOf(value.trim().toUpperCase());
    }
}
