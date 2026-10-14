package ai.foresignal.notify.mesh;

import ai.foresignal.notify.DigestBuilder;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.mesh.cbor.MeshCborMessage;
import ai.foresignal.notify.mesh.cbor.MeshCborProfile;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * What both mesh channel types derive from one notification (prompt 084): its short code, the number of alerts a
 * digest folded, its CBOR profile message, and the Meshtastic text form of a CBOR message.
 */
public final class MeshRenderings {

    /** The prefix of a CBOR message sent as Meshtastic text (the JSON downlink carries only text). */
    public static final String CBOR_TEXT_PREFIX = "~E1 ";

    /**
     * The largest CBOR message that fits one Meshtastic text: {@code ~E1 } plus unpadded base64url of 147 bytes is
     * 200 bytes.
     */
    public static final int MESHTASTIC_CBOR_MAX = 147;

    private MeshRenderings() {}

    /** The short code the text of {@code m} ends with, or null when it has none. */
    public static String shortCode(NotificationMessage m) {
        if (DigestBuilder.SUBTYPE.equals(m.subtype())) {
            Object top = m.data() == null ? null : m.data().get("topAlertId");
            return top instanceof Number n ? MeshShortCode.of(n.longValue()) : null;
        }
        return NotificationMessage.ALERT.equals(m.event()) && m.alertId() != null ? MeshShortCode.of(m.alertId())
                : null;
    }

    /** How many alerts beyond the first a digest stands for; 0 for any other message. */
    public static int folded(NotificationMessage m) {
        if (!DigestBuilder.SUBTYPE.equals(m.subtype()) || m.data() == null) {
            return 0;
        }
        return m.data().get("count") instanceof Number n ? Math.max(0, n.intValue() - 1) : 0;
    }

    /** The CBOR profile message of {@code m}. */
    public static MeshCborMessage cbor(NotificationMessage m) {
        Map<String, Object> data = m.data() == null ? Map.of() : m.data();
        int type;
        if ("alert.resolved".equals(m.subtype())) {
            type = MeshCborProfile.TYPE_RESOLVED;
        } else if (NotificationMessage.ALERT.equals(m.event())) {
            type = MeshCborProfile.TYPE_ALERT;
        } else {
            type = MeshCborProfile.TYPE_STATUS;
        }
        double[] point = MeshShortTextFormatter.point(data);
        String title = DigestBuilder.SUBTYPE.equals(m.subtype())
                ? data.get("count") + " alerts, top: " + data.get("topTitle")
                : "alert.resolved".equals(m.subtype()) ? MeshShortTextFormatter.ruleName(m) : m.shortTitle();
        return new MeshCborMessage(MeshCborProfile.VERSION, type, shortCode(m), MeshCborMessage.severity(m.severity()),
                data.get("kind") == null ? null : String.valueOf(data.get("kind")),
                point == null ? null : MeshCborMessage.e5(point[1]), point == null ? null : MeshCborMessage.e5(point[0]),
                MeshShortTextFormatter.time(m).getEpochSecond(), MeshShortTextFormatter.plain(title),
                data.get("sourceId") == null ? null : String.valueOf(data.get("sourceId")));
    }

    /** {@code cbor} as one Meshtastic text: {@code ~E1 } and unpadded base64url. */
    public static String cborText(byte[] cbor) {
        return CBOR_TEXT_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(cbor);
    }

    /** The UTF-8 size of {@code text}. */
    public static int bytes(String text) {
        return text.getBytes(StandardCharsets.UTF_8).length;
    }
}
