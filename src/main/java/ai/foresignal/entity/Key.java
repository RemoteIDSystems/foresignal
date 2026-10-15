package ai.foresignal.entity;

import java.util.Objects;

/**
 * One normalized, validated entity key with the metadata its role implies (prompt 098).
 *
 * @param keyType the key type ({@code icao24}, {@code mmsi}, {@code cospar} …)
 * @param keyValue the normalized value
 * @param role ANCHOR or WEAK
 * @param confidence the default confidence of this key type and value
 */
public record Key(String keyType, String keyValue, EntityKeyRole role, float confidence) {

    public Key {
        Objects.requireNonNull(keyType, "keyType");
        Objects.requireNonNull(keyValue, "keyValue");
        Objects.requireNonNull(role, "role");
    }

    /** True when this key identifies an entity on its own. */
    public boolean anchor() {
        return role == EntityKeyRole.ANCHOR;
    }

    /** The entity type implied by the key type, or null when it is ambiguous. */
    public EntityType entityType() {
        return EntityKeys.typeOf(keyType);
    }
}
