package ai.foresignal.source.adapters.mesh;

import java.time.Instant;
import java.util.Objects;

/**
 * One decoded text message from the operator's own mesh node (prompt 083), as {@link MeshMessageBus} publishes it.
 * In-process only: never persisted by the bus, never embedded, never sent to an LLM. Prompt 084 reads field reports
 * from it.
 *
 * @param sourceId the source that heard it ({@code meshtastic-local} or {@code meshcore-local})
 * @param network {@code meshtastic} or {@code meshcore}
 * @param channelIndex the channel index, or null for a direct message
 * @param channelName the channel's name when known, else null
 * @param fromId the sender: a Meshtastic {@code !xxxxxxxx} node id or a MeshCore {@code mc:} key prefix; null when the
 *     protocol does not say (MeshCore channel messages)
 * @param toId the addressee node id, or null for a channel broadcast
 * @param text the text
 * @param receivedAt when Foresignal received it
 * @param rssi the received signal strength, or null
 * @param snr the signal-to-noise ratio in dB, or null
 */
public record MeshTextMessage(String sourceId, String network, Integer channelIndex, String channelName, String fromId,
        String toId, String text, Instant receivedAt, Double rssi, Double snr) {

    /** Requires the source, the network, the text and the receive time. */
    public MeshTextMessage {
        Objects.requireNonNull(sourceId, "sourceId");
        Objects.requireNonNull(network, "network");
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(receivedAt, "receivedAt");
    }

    /** True for a direct message (no channel). */
    public boolean direct() {
        return channelIndex == null;
    }
}
