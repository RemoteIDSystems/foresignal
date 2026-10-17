package ai.foresignal.notify.mesh;

import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.ChannelType;
import ai.foresignal.notify.PreparedRequest;

/**
 * A mesh channel type (prompt 084): {@code meshtastic} or {@code meshcore}. Beyond the 018 SPI it renders a plain
 * reply line (a field-report answer) on the channel's private channel, which goes through the same
 * {@link MeshTransmitter} and airtime governor as every delivery.
 */
public interface MeshChannelType extends ChannelType {

    /** The network: {@code meshtastic} or {@code meshcore}. */
    String network();

    /**
     * The transmission of a plain reply line on the channel's private channel (never a direct message).
     *
     * @param text the reply, already within the network's text cap
     */
    PreparedRequest renderReply(String text, ChannelSettings settings);

    /**
     * The transmission of a compact federation message (prompt 113) through the channel's {@code profile}, exactly
     * as 084 sends an alert: the {@code cbor} profile sends the CBOR map ({@code ~E1 } + base64url as Meshtastic text,
     * a channel datagram on MeshCore), the {@code text} profile the short text line on the private channel. The
     * default sends the text line as a reply would.
     *
     * @param cbor the encoded message (at most 160 bytes)
     * @param text the short text form
     * @param severity {@code info}, {@code warning} or {@code critical} (critical jumps the airtime queue)
     */
    default PreparedRequest renderCompact(byte[] cbor, String text, String severity, ChannelSettings settings) {
        return renderReply(text, settings);
    }

    /** The channel's shared settings, parsed (the config validated). */
    MeshChannels.Common common(java.util.Map<String, Object> config);
}
