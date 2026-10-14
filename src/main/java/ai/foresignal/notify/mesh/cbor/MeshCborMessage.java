package ai.foresignal.notify.mesh.cbor;

/**
 * One machine-readable mesh message (prompt 084), as {@link MeshCborProfile} encodes it. Every member but the version
 * and type is optional (null when absent).
 *
 * @param version the profile version (key 0); {@value MeshCborProfile#VERSION} for this codec
 * @param type the message type (key 1): {@value MeshCborProfile#TYPE_ALERT} alert, {@value MeshCborProfile#TYPE_RESOLVED}
 *     resolved, {@value MeshCborProfile#TYPE_REPORT_ACK} report acknowledgement, {@value MeshCborProfile#TYPE_STATUS}
 *     status
 * @param shortCode the alert's short code (key 2)
 * @param severity 0 info, 1 warning, 2 critical (key 3)
 * @param kind the record kind, at most 16 characters (key 4)
 * @param latitudeE5 the latitude × 1e5, rounded (key 5)
 * @param longitudeE5 the longitude × 1e5, rounded (key 6)
 * @param epochSeconds the event time in Unix seconds (key 7)
 * @param title the title, at most 60 characters (key 8; dropped first when the message is too long)
 * @param sourceId the source id, at most 24 characters (key 9; dropped second)
 */
public record MeshCborMessage(int version, int type, String shortCode, Integer severity, String kind,
        Integer latitudeE5, Integer longitudeE5, Long epochSeconds, String title, String sourceId) {

    /** A copy without the title. */
    public MeshCborMessage withoutTitle() {
        return new MeshCborMessage(version, type, shortCode, severity, kind, latitudeE5, longitudeE5, epochSeconds,
                null, sourceId);
    }

    /** A copy without the source id. */
    public MeshCborMessage withoutSourceId() {
        return new MeshCborMessage(version, type, shortCode, severity, kind, latitudeE5, longitudeE5, epochSeconds,
                title, null);
    }

    /** The severity number of a wire value: 0 info, 1 warning, 2 critical. */
    public static int severity(String wire) {
        return switch (wire == null ? "" : wire) {
            case "critical" -> 2;
            case "warning" -> 1;
            default -> 0;
        };
    }

    /** A coordinate in degrees × 1e5, rounded. */
    public static int e5(double degrees) {
        return (int) Math.round(degrees * 1e5);
    }
}
