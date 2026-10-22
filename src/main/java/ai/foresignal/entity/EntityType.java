package ai.foresignal.entity;

import java.util.Locale;

/**
 * What a canonical entity is (prompt 098): a physical vehicle or object, never a person or an
 * organization.
 */
public enum EntityType {
    AIRCRAFT("aircraft"),
    VESSEL("vessel"),
    SATELLITE("satellite"),
    STATION("station");

    private final String wire;

    EntityType(String wire) {
        this.wire = wire;
    }

    /** The wire value stored in {@code entity.type} and served by the API. */
    public String wire() {
        return wire;
    }

    /** The type of a wire value, or null when it is unknown. */
    public static EntityType fromWire(String value) {
        if (value == null) {
            return null;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        for (EntityType type : values()) {
            if (type.wire.equals(lower)) {
                return type;
            }
        }
        return null;
    }
}
