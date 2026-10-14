package ai.foresignal.notify;

import java.io.IOException;

/**
 * Transmits a {@link PreparedRequest.Mesh} body over the operator's LoRa mesh (prompt 084). Implemented by
 * {@code MeshTransmitter}; {@link ChannelSender} calls it so mesh deliveries keep the 018 pipeline (rows, retries,
 * quiet hours, folding). Every transmission passes the channel's airtime governor first.
 */
public interface MeshDeliveryGateway {

    /**
     * Admits {@code request} through its channel's airtime governor and transmits it.
     *
     * @return status 200 with body {@code PUBACK} (Meshtastic), {@code OK} (MeshCore channel), {@code ACKED} or
     *     {@code SENT} (MeshCore direct message), or {@code UNACKED} when a required acknowledgement never came
     * @throws SendDeferredException when the governor has no room now; the delivery waits until the time it names
     * @throws IOException when the radio path is unavailable, refuses the message, or the channel slot is not private
     */
    ChannelResponse transmit(PreparedRequest.Mesh request) throws IOException;
}
