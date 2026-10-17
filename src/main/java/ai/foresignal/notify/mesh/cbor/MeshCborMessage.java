package ai.foresignal.notify.mesh.cbor;

/**
 * One machine-readable mesh message (prompt 084), as {@link MeshCborProfile} encodes it. Every member but the version
 * and type is optional (null when absent).
 *
 * @param version the profile version (key 0); {@value MeshCborProfile#VERSION} for this codec
 * @param type the message type (key 1): {@value MeshCborProfile#TYPE_ALERT} alert, {@value MeshCborProfile#TYPE_RESOLVED}
 *     resolved, {@value MeshCborProfile#TYPE_REPORT_ACK} report acknowledgement, {@value MeshCborProfile#TYPE_STATUS}
 *     status, {@value MeshCborProfile#TYPE_INTEL_EVENT} intelligence event (113)
 * @param shortCode the alert's short code (key 2)
 * @param severity 0 info, 1 warning, 2 critical (key 3)
 * @param kind the record kind, at most 16 characters (key 4)
 * @param latitudeE5 the latitude × 1e5, rounded (key 5)
 * @param longitudeE5 the longitude × 1e5, rounded (key 6)
 * @param epochSeconds the event time in Unix seconds (key 7)
 * @param title the title, at most 60 characters (key 8; dropped first when the message is too long)
 * @param sourceId the source id, at most 24 characters (key 9; dropped second)
 * @param originShortId the origin instance's 4-byte short id (key 10, prompt 113), or null
 * @param messageId the message id: the first 8 bytes of the SHA-256 of the origin ref (key 11, prompt 113), or null
 * @param expiryMinutes minutes after key 7 at which the message expires (key 12, prompt 113), or null
 */
public record MeshCborMessage(int version, int type, String shortCode, Integer severity, String kind,
        Integer latitudeE5, Integer longitudeE5, Long epochSeconds, String title, String sourceId,
        byte[] originShortId, byte[] messageId, Integer expiryMinutes) {

    /** Copies the byte members. */
    public MeshCborMessage {
        originShortId = originShortId == null ? null : originShortId.clone();
        messageId = messageId == null ? null : messageId.clone();
    }

    /** A message without the prompt 113 extension keys (the 084 shape). */
    public MeshCborMessage(int version, int type, String shortCode, Integer severity, String kind,
            Integer latitudeE5, Integer longitudeE5, Long epochSeconds, String title, String sourceId) {
        this(version, type, shortCode, severity, kind, latitudeE5, longitudeE5, epochSeconds, title, sourceId, null,
                null, null);
    }

    /** True when any of the prompt 113 extension keys (10, 11, 12) is set. */
    public boolean extended() {
        return originShortId != null || messageId != null || expiryMinutes != null;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof MeshCborMessage m && version == m.version && type == m.type
                && java.util.Objects.equals(shortCode, m.shortCode) && java.util.Objects.equals(severity, m.severity)
                && java.util.Objects.equals(kind, m.kind) && java.util.Objects.equals(latitudeE5, m.latitudeE5)
                && java.util.Objects.equals(longitudeE5, m.longitudeE5)
                && java.util.Objects.equals(epochSeconds, m.epochSeconds) && java.util.Objects.equals(title, m.title)
                && java.util.Objects.equals(sourceId, m.sourceId)
                && java.util.Arrays.equals(originShortId, m.originShortId)
                && java.util.Arrays.equals(messageId, m.messageId)
                && java.util.Objects.equals(expiryMinutes, m.expiryMinutes);
    }

    @Override
    public int hashCode() {
        int hash = java.util.Objects.hash(version, type, shortCode, severity, kind, latitudeE5, longitudeE5,
                epochSeconds, title, sourceId, expiryMinutes);
        return 31 * (31 * hash + java.util.Arrays.hashCode(originShortId)) + java.util.Arrays.hashCode(messageId);
    }

    @Override
    public String toString() {
        java.util.HexFormat hex = java.util.HexFormat.of();
        return "MeshCborMessage[version=" + version + ", type=" + type + ", shortCode=" + shortCode + ", severity="
                + severity + ", kind=" + kind + ", latitudeE5=" + latitudeE5 + ", longitudeE5=" + longitudeE5
                + ", epochSeconds=" + epochSeconds + ", title=" + title + ", sourceId=" + sourceId + ", originShortId="
                + (originShortId == null ? null : hex.formatHex(originShortId)) + ", messageId="
                + (messageId == null ? null : hex.formatHex(messageId)) + ", expiryMinutes=" + expiryMinutes + "]";
    }

    /** A copy without the title. */
    public MeshCborMessage withoutTitle() {
        return new MeshCborMessage(version, type, shortCode, severity, kind, latitudeE5, longitudeE5, epochSeconds,
                null, sourceId, originShortId, messageId, expiryMinutes);
    }

    /** A copy without the source id. */
    public MeshCborMessage withoutSourceId() {
        return new MeshCborMessage(version, type, shortCode, severity, kind, latitudeE5, longitudeE5, epochSeconds,
                title, null, originShortId, messageId, expiryMinutes);
    }

    /** A copy with the prompt 113 extension keys set. */
    public MeshCborMessage withExtensions(byte[] origin, byte[] id, Integer expiry) {
        return new MeshCborMessage(version, type, shortCode, severity, kind, latitudeE5, longitudeE5, epochSeconds,
                title, sourceId, origin, id, expiry);
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
