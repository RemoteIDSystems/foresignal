package ai.foresignal.entity;

import java.util.Locale;

/**
 * What a canonical entity is (prompt 098): a physical vehicle or object (aircraft, vessel, satellite,
 * station), and — since prompt 158 — a person or an organization, resolved on deterministic corporate and
 * person keys for relationship intelligence (ownership and affiliation).
 */
public enum EntityType {
    AIRCRAFT("aircraft"),
    VESSEL("vessel"),
    SATELLITE("satellite"),
    STATION("station"),
    PERSON("person"),
    ORGANIZATION("organization");

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
