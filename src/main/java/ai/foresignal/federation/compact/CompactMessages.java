package ai.foresignal.federation.compact;

import ai.foresignal.federation.RecordRef;
import ai.foresignal.notify.mesh.MeshShortCode;
import ai.foresignal.notify.mesh.cbor.MeshCborMessage;
import ai.foresignal.notify.mesh.cbor.MeshCborProfile;
import ai.foresignal.security.restrict.RestrictionClamp;
import ai.foresignal.trigger.AlertEvent;
import ai.foresignal.trigger.AlertView;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

/**
 * Builds compact prioritized messages (prompt 113) from events only: fired and resolved alerts (017) and
 * intelligence events (101), and the resolution of either. There is deliberately no way to build one from a
 * record, so raw tracks, live updates and bulk records never become compact messages.
 *
 * <p>Each message is an 084 {@link MeshCborMessage} with the 113 keys: 10 this instance's 4-byte short id, 11 the
 * message id (the first 8 bytes of the SHA-256 of the origin ref: {@code urn:foresignal:{instance}:alert:{id}} for
 * an alert, {@code urn:foresignal:{instance}:foresignal-intel:{id}} for an intelligence event) and 12 the expiry in
 * minutes after key 7 ({@value #DEFAULT_EXPIRY_MINUTES} by default).
 */
public final class CompactMessages {

    /** The pseudo source id of an alert's origin ref. */
    public static final String ALERT_SOURCE = "alert";

    /** The source id of an intelligence event's origin ref (the {@code foresignal-intel} records, 101). */
    public static final String INTEL_SOURCE = "foresignal-intel";

    /** The default expiry: one day. */
    public static final int DEFAULT_EXPIRY_MINUTES = 1_440;

    private static final HexFormat HEX = HexFormat.of();

    private CompactMessages() {}

    /** The origin ref of an alert on {@code instanceId}. */
    public static String alertRef(String instanceId, long alertId) {
        return RecordRef.format(instanceId, ALERT_SOURCE, Long.toString(alertId));
    }

    /** The origin ref of an intelligence event on {@code instanceId}. */
    public static String intelRef(String instanceId, String eventId) {
        return RecordRef.format(instanceId, INTEL_SOURCE, eventId);
    }

    /**
     * The message of an alert event: type 1 when fired, type 2 when resolved; null for any other event type
     * (acknowledgements, notes and enrichment are not sent).
     *
     * @param originShortId this instance's short id (key 10)
     * @param instanceId this instance's id (for the message id)
     */
    public static MeshCborMessage alert(AlertEvent event, byte[] originShortId, String instanceId) {
        AlertView alert = event.alert();
        int type;
        Instant at;
        if (AlertEvent.FIRED.equals(event.type())) {
            type = MeshCborProfile.TYPE_ALERT;
            at = alert.firedAt();
        } else if (AlertEvent.RESOLVED.equals(event.type())) {
            type = MeshCborProfile.TYPE_RESOLVED;
            at = alert.resolvedAt() == null ? alert.firedAt() : alert.resolvedAt();
        } else {
            return null;
        }
        Point point = centroid(RestrictionClamp.snapshotGeometry(alert.recordSnapshot()));
        String title = alert.ruleName() != null ? alert.ruleName() : alert.message();
        return new MeshCborMessage(MeshCborProfile.VERSION, type, MeshShortCode.of(alert.id()),
                MeshCborMessage.severity(alert.severity()), alert.kind(),
                point == null ? null : MeshCborMessage.e5(point.getY()),
                point == null ? null : MeshCborMessage.e5(point.getX()),
                at == null ? null : at.getEpochSecond(), title, alert.sourceId(), originShortId,
                RecordRef.messageId(alertRef(instanceId, alert.id())), DEFAULT_EXPIRY_MINUTES);
    }

    /**
     * The message of an intelligence event: type {@value MeshCborProfile#TYPE_INTEL_EVENT} while open, type 2
     * (resolved) once resolved or merged.
     *
     * @param change the 101 change ({@code created}, {@code updated}, {@code resolved}, {@code merged})
     * @param summary the event summary ({@code id}, {@code type}, {@code title}, {@code severity}, …)
     * @param centroid the event's centroid, or null
     */
    public static MeshCborMessage intel(String change, Map<String, Object> summary, Geometry centroid,
            byte[] originShortId, String instanceId, Instant now) {
        String id = summary.get("id") == null ? null : String.valueOf(summary.get("id"));
        if (id == null) {
            return null;
        }
        boolean closed = "resolved".equals(change) || "merged".equals(change);
        Point point = centroid(centroid);
        Instant at = instant(summary.get(closed ? "updatedAt" : "startTime"), now);
        String severity = summary.get("severity") == null ? null : String.valueOf(summary.get("severity"));
        return new MeshCborMessage(MeshCborProfile.VERSION, closed ? MeshCborProfile.TYPE_RESOLVED
                : MeshCborProfile.TYPE_INTEL_EVENT, null, MeshCborMessage.severity(severity),
                summary.get("type") == null ? null : String.valueOf(summary.get("type")),
                point == null ? null : MeshCborMessage.e5(point.getY()),
                point == null ? null : MeshCborMessage.e5(point.getX()), at.getEpochSecond(),
                summary.get("title") == null ? null : String.valueOf(summary.get("title")), INTEL_SOURCE,
                originShortId, RecordRef.messageId(intelRef(instanceId, id)), DEFAULT_EXPIRY_MINUTES);
    }

    /** When {@code message} expires (key 7 plus key 12), or null when it never does. */
    public static Instant expiresAt(MeshCborMessage message) {
        if (message.epochSeconds() == null || message.expiryMinutes() == null) {
            return null;
        }
        return Instant.ofEpochSecond(message.epochSeconds()).plus(Duration.ofMinutes(message.expiryMinutes()));
    }

    /** The message id as lowercase hex, or null. */
    public static String idHex(MeshCborMessage message) {
        return message.messageId() == null ? null : HEX.formatHex(message.messageId());
    }

    /** The origin short id as lowercase hex, or null. */
    public static String originHex(MeshCborMessage message) {
        return message.originShortId() == null ? null : HEX.formatHex(message.originShortId());
    }

    /** The severity wire value of a severity number. */
    public static String severityWire(Integer severity) {
        if (severity == null) {
            return "info";
        }
        return switch (severity) {
            case 2 -> "critical";
            case 1 -> "warning";
            default -> "info";
        };
    }

    /**
     * The short text form of a message, at most {@code maxChars} characters: {@code CRIT|WARN|INFO}, {@code RESOLVED}
     * for type 2, the title and the short code.
     */
    public static String text(MeshCborMessage message, int maxChars) {
        String head = message.type() == MeshCborProfile.TYPE_RESOLVED ? "RESOLVED"
                : switch (message.severity() == null ? 0 : message.severity()) {
                    case 2 -> "CRIT";
                    case 1 -> "WARN";
                    default -> "INFO";
                };
        StringBuilder out = new StringBuilder(head);
        if (message.title() != null) {
            out.append(' ').append(message.title().replaceAll("[\\p{Cntrl}]+", " ").strip());
        }
        if (message.shortCode() != null) {
            out.append(" #").append(message.shortCode());
        }
        String text = out.toString();
        if (text.length() <= maxChars) {
            return text;
        }
        int end = maxChars;
        if (Character.isHighSurrogate(text.charAt(end - 1))) {
            end--;
        }
        return text.substring(0, end);
    }

    private static Point centroid(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }
        return geometry instanceof Point p ? p : geometry.getCentroid();
    }

    private static Instant instant(Object value, Instant fallback) {
        if (value instanceof Instant i) {
            return i;
        }
        if (value instanceof String s) {
            try {
                return Instant.parse(s);
            } catch (RuntimeException e) {
                return fallback;
            }
        }
        if (value instanceof java.time.OffsetDateTime o) {
            return o.toInstant();
        }
        return fallback;
    }
}
