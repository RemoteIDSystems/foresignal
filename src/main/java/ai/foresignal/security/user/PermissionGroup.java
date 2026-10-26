package ai.foresignal.security.user;

/** The grouping of a permission in the catalog (OpenAPI {@code Permission.group}). */
public enum PermissionGroup {
    /** Managing accounts and roles. */
    IDENTITY,
    /** Instance-wide settings: the collection, the collector, shared LLM, global channels, storage, network, audit. */
    PLATFORM,
    /** A user's own configuration layered over the shared collection. */
    SELF,
    /** Reading and exporting collected data, intelligence and forecasts. */
    DATA;

    /** The wire value, also the stored {@code permission.group}. */
    public String wireValue() {
        return name().toLowerCase();
    }

    /**
     * The group with the given wire value.
     *
     * @param value {@code identity}, {@code platform}, {@code self} or {@code data}
     * @return the group
     * @throws IllegalArgumentException when no group has that value
     */
    public static PermissionGroup fromWire(String value) {
        for (PermissionGroup group : values()) {
            if (group.wireValue().equals(value)) {
                return group;
            }
        }
        throw new IllegalArgumentException("unknown permission group: " + value);
    }
}
