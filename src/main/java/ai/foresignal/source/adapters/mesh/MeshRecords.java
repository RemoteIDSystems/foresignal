package ai.foresignal.source.adapters.mesh;

import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Observer;
import ai.foresignal.source.spi.Quality;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

/**
 * The shared mesh model of prompt 083: the kinds {@value #NODE_KIND} and {@value #MESSAGE_KIND}, their payload keys,
 * the descriptor defaults all five mesh sources share, and the record builders. Not an adapter.
 *
 * <ul>
 *   <li><strong>{@value #NODE_KIND}:</strong> externalId {@code !} + 8 hex (Meshtastic node number, unsigned) or
 *       {@code mc:} + 12 hex (MeshCore key prefix). The payload is allowlisted and has no nulls; telemetry and the
 *       last-heard and advert times are volatile, so their jitter only touches the live row; {@code lowBattery}
 *       ({@code batteryLevel < 15}) is the computed significant field (adapter rule 12). The geometry is the last
 *       known position, null when the node never shared one; {@code Quality.accuracyMeters} comes from the position
 *       precision bits.
 *   <li><strong>{@value #MESSAGE_KIND}:</strong> a text message, persisted only when a source's {@code includeText} is
 *       true; never embedded and never sent to an LLM (no {@code embedFields}).
 *   <li><strong>Observer:</strong> the gateway or own node that heard it, with RSSI and SNR.
 * </ul>
 */
public final class MeshRecords {

    /** The node kind. */
    public static final String NODE_KIND = "mesh-node";

    /** The message kind. */
    public static final String MESSAGE_KIND = "mesh-message";

    /** The Meshtastic network. */
    public static final String MESHTASTIC = "meshtastic";

    /** The MeshCore network. */
    public static final String MESHCORE = "meshcore";

    /** The battery level below which {@code lowBattery} is true. */
    public static final int LOW_BATTERY_PERCENT = 15;

    /** The record TTL of every mesh source except the daily map (prompt 083). */
    public static final Duration TTL = Duration.ofHours(6);

    /** The keyframe of the streaming mesh sources: under {@code ttl - 2 s - 30 s}. */
    public static final Duration STREAM_KEYFRAME = Duration.ofHours(5);

    /** Raw retention: node state is the {@code state} class (365 d). */
    public static final Duration RETENTION = Duration.ofDays(365);

    /** The significant payload fields. */
    public static final List<String> SIGNIFICANT = List.of("longName", "shortName", "role", "hwModel", "nodeType",
            "network", "lowBattery", "advertStatus");

    /** The volatile payload fields. */
    public static final List<String> VOLATILE = List.of("batteryLevel", "voltage", "channelUtil", "airUtilTx", "uptimeS",
            "lastHeardAt", "lastAdvertAt", "advertTimestamp");

    /** The parameter that persists text messages as {@value #MESSAGE_KIND} records. */
    public static final String INCLUDE_TEXT = "includeText";

    private static final Set<String> NODE_KINDS = Set.of(NODE_KIND);

    private MeshRecords() {}

    /**
     * Applies the shared mesh descriptor defaults (prompt 083): STANDARD, ALL with {@code keyframe}, the TTL from the
     * observed time, upstream observed times, the significant, volatile, tile and search fields, the {@code state}
     * retention and the field specs. The caller sets mode, budget, interval and {@code observedAtMode} where it differs.
     */
    public static SourceDescriptor.Builder defaults(SourceDescriptor.Builder b, Duration ttl, Duration keyframe) {
        b.historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(new IngestPolicy.All(keyframe))
                .recordTtl(ttl)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .significantFields(SIGNIFICANT.toArray(String[]::new))
                .volatileFields(VOLATILE.toArray(String[]::new))
                .tileProperties("shortName", "role", "nodeType", "batteryLevel")
                .searchFields("longName", "shortName")
                .titleField("longName")
                .defaultRetention(RETENTION);
        field(b, "network", FieldType.STRING, null, "Network", "meshtastic or meshcore");
        field(b, "longName", FieldType.STRING, null, "Name", "The node's long name as its owner set it");
        field(b, "shortName", FieldType.STRING, null, "Short name", "The node's short name");
        field(b, "role", FieldType.STRING, null, "Role", "The Meshtastic device role (CLIENT, ROUTER, ...)");
        field(b, "hwModel", FieldType.STRING, null, "Hardware", "The hardware model (name, or number when only that"
                + " is known)");
        field(b, "firmware", FieldType.STRING, null, "Firmware", "The firmware version");
        field(b, "nodeType", FieldType.STRING, null, "Node type", "MeshCore advert type: chat, repeater, room or"
                + " sensor");
        field(b, "positionPrecisionBits", FieldType.INTEGER, null, "Position precision", "Bits of the 32-bit coordinate"
                + " the node shares (32 is exact; the public broker allows 10 to 16)");
        field(b, "batteryLevel", FieldType.INTEGER, "%", "Battery", "Battery level; above 100 means external power");
        field(b, "voltage", FieldType.NUMBER, "V", "Voltage", "Battery voltage");
        field(b, "channelUtil", FieldType.NUMBER, "%", "Channel utilization", "Share of airtime the node hears in use");
        field(b, "airUtilTx", FieldType.NUMBER, "%", "Transmit airtime", "Share of airtime the node transmits");
        field(b, "uptimeS", FieldType.INTEGER, "s", "Uptime", "Seconds since the node started");
        field(b, "lastHeardAt", FieldType.TIMESTAMP, null, "Last heard", "When the node was last heard");
        field(b, "lowBattery", FieldType.BOOLEAN, null, "Low battery", "batteryLevel below 15");
        field(b, "advertStatus", FieldType.STRING, null, "Advert health", "MeshCore repeaters: ok, late or silent, from"
                + " the age of the last advert");
        field(b, "lastAdvertAt", FieldType.TIMESTAMP, null, "Last advert", "When the last MeshCore advert was received");
        field(b, "advertTimestamp", FieldType.TIMESTAMP, null, "Advert clock", "The timestamp in the advert itself (the"
                + " node's clock, which may drift)");
        return b;
    }

    private static void field(SourceDescriptor.Builder b, String key, FieldType type, String unit, String label,
            String description) {
        b.fieldSpec(key, new FieldSpec(type, unit, label, description, null, NODE_KINDS));
    }

    /**
     * The horizontal accuracy of a Meshtastic position with {@code bits} of precision: half the cell a truncated
     * coordinate can lie in, {@code 2^(32 - bits) × 1e-7°}, in meters; null for unknown, 0 or 32 and more bits.
     */
    public static Double accuracyMeters(Integer bits) {
        if (bits == null || bits <= 0 || bits >= 32) {
            return null;
        }
        double cellDegrees = Math.pow(2, 32 - bits) * 1e-7;
        return Math.round(cellDegrees * 111_320.0 / 2 * 10) / 10.0;
    }

    /**
     * The {@value #NODE_KIND} record of {@code state}.
     *
     * @param observedAt the record's observed time, or null under {@code RECEIVED}
     * @param observer the receiver that heard this message, or null
     */
    public static NormalizedRecord node(MeshNodeState state, Instant observedAt, Observer observer) {
        NormalizedRecord.Builder b = NormalizedRecord.builder(state.externalId(), NODE_KIND)
                .observedAt(observedAt)
                .put("network", state.network())
                .put("longName", state.longName)
                .put("shortName", state.shortName)
                .put("role", state.role)
                .put("hwModel", state.hwModel)
                .put("firmware", state.firmware)
                .put("nodeType", state.nodeType)
                .put("positionPrecisionBits", state.hasPosition() ? state.positionPrecisionBits : null)
                .put("batteryLevel", state.batteryLevel)
                .put("voltage", state.voltage)
                .put("channelUtil", state.channelUtil)
                .put("airUtilTx", state.airUtilTx)
                .put("uptimeS", state.uptimeS)
                .put("lastHeardAt", state.lastHeardAt == null ? null : state.lastHeardAt.toString())
                .put("lowBattery", state.batteryLevel == null ? null : state.batteryLevel < LOW_BATTERY_PERCENT)
                .put("advertStatus", state.advertStatus)
                .put("lastAdvertAt", state.lastAdvertAt == null ? null : state.lastAdvertAt.toString())
                .put("advertTimestamp", state.advertTimestamp == null ? null : state.advertTimestamp.toString());
        if (state.hasPosition()) {
            b.point(state.longitude, state.latitude);
            Double accuracy = accuracyMeters(state.positionPrecisionBits);
            if (accuracy != null) {
                b.quality(new Quality(accuracy, null, null, java.util.Map.of()));
            }
        }
        if (observer != null) {
            b.observers(List.of(observer));
        }
        return b.build();
    }

    /**
     * The {@value #MESSAGE_KIND} record of {@code message}.
     *
     * @param externalId the message id, unique within the source
     * @param observedAt the sender's time, or null under {@code RECEIVED}
     */
    public static NormalizedRecord message(MeshTextMessage message, String externalId, Instant observedAt) {
        NormalizedRecord.Builder b = NormalizedRecord.builder(externalId, MESSAGE_KIND)
                .observedAt(observedAt)
                .eventTime(observedAt != null ? observedAt : message.receivedAt())
                .put("network", message.network())
                .put("channelIndex", message.channelIndex())
                .put("channelName", message.channelName())
                .put("fromId", message.fromId())
                .put("toId", message.toId())
                .put("text", message.text());
        if (message.rssi() != null || message.snr() != null) {
            b.observers(List.of(new Observer(message.sourceId(), null, message.rssi(), message.snr(), null)));
        }
        return b.build();
    }

    /** A short stable hash of {@code parts} (16 hex characters), for message ids without an upstream id. */
    public static String hash(String... parts) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            for (String part : parts) {
                sha.update((part == null ? "" : part).getBytes(StandardCharsets.UTF_8));
                sha.update((byte) 0);
            }
            return HexFormat.of().formatHex(sha.digest(), 0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** The Meshtastic device role names by number (the firmware's {@code Config.DeviceConfig.Role} order). */
    static final List<String> MESHTASTIC_ROLES = List.of("CLIENT", "CLIENT_MUTE", "ROUTER", "ROUTER_CLIENT", "REPEATER",
            "TRACKER", "SENSOR", "TAK", "CLIENT_HIDDEN", "LOST_AND_FOUND", "TAK_TRACKER", "ROUTER_LATE", "CLIENT_BASE");

    /** The role name of a Meshtastic role number, or the number as text when it is unknown; null for null. */
    public static String meshtasticRole(Integer role) {
        if (role == null) {
            return null;
        }
        return role >= 0 && role < MESHTASTIC_ROLES.size() ? MESHTASTIC_ROLES.get(role) : Integer.toString(role);
    }

    /** {@code !} and the 8 hex digits of a node number read as unsigned 32-bit (firmware below 2.2.0 sent signed). */
    public static String meshtasticNodeId(long number) {
        return String.format("!%08x", number & 0xFFFF_FFFFL);
    }
}
