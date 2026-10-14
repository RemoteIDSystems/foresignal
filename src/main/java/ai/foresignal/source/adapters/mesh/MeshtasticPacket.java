package ai.foresignal.source.adapters.mesh;

import java.time.Instant;

/**
 * One Meshtastic JSON message from {@code msh/<root>/2/json/<channel>/<gatewayId>} (prompt 083), as
 * {@link MeshtasticJsonParser} reads it. Only the firmware's JSON output: nothing in Foresignal decodes Meshtastic
 * protobufs (GPL-3.0).
 *
 * @param topic the MQTT topic
 * @param channelName the channel segment of the topic ({@code LongFast}, ...)
 * @param gatewayId the topic's last segment: the node that uplinked the message
 * @param packetId the packet id, or null
 * @param channel the channel index, or null
 * @param from the sender's node id ({@code !xxxxxxxx})
 * @param to the addressee's node id ({@code !ffffffff} for broadcast), or null
 * @param type the JSON {@code type}
 * @param timestamp the JSON {@code timestamp} (the gateway's clock), or null
 * @param rssi the RSSI at the gateway, or null
 * @param snr the SNR at the gateway, or null
 * @param hopsAway hops between sender and gateway, or null
 * @param payload the typed payload
 */
public record MeshtasticPacket(String topic, String channelName, String gatewayId, Long packetId, Integer channel,
        String from, String to, String type, Instant timestamp, Double rssi, Double snr, Integer hopsAway,
        Payload payload) {

    /** The broadcast address. */
    public static final String BROADCAST = "!ffffffff";

    /** A typed JSON payload. */
    public sealed interface Payload permits NodeInfo, Position, Telemetry, Text {}

    /**
     * {@code nodeinfo}.
     *
     * @param longName {@code longname}
     * @param shortName {@code shortname}
     * @param hardware {@code hardware}, the hardware model number
     * @param role {@code role}, the role number
     */
    public record NodeInfo(String longName, String shortName, Integer hardware, Integer role) implements Payload {}

    /**
     * {@code position}.
     *
     * @param latitude {@code latitude_i} × 1e-7
     * @param longitude {@code longitude_i} × 1e-7
     * @param altitude {@code altitude} in meters, or null
     * @param precisionBits {@code precision_bits}, or null
     */
    public record Position(Double latitude, Double longitude, Integer altitude, Integer precisionBits) implements Payload {}

    /**
     * {@code telemetry} device metrics; all null for another telemetry variant (environment, power).
     *
     * @param batteryLevel {@code battery_level}
     * @param voltage {@code voltage}
     * @param channelUtil {@code channel_utilization}
     * @param airUtilTx {@code air_util_tx}
     * @param uptimeS {@code uptime_seconds}
     */
    public record Telemetry(Integer batteryLevel, Double voltage, Double channelUtil, Double airUtilTx, Long uptimeS)
            implements Payload {

        /** True when no device metric is present. */
        public boolean empty() {
            return batteryLevel == null && voltage == null && channelUtil == null && airUtilTx == null && uptimeS == null;
        }
    }

    /**
     * {@code text}.
     *
     * @param text the message text
     */
    public record Text(String text) implements Payload {}
}
