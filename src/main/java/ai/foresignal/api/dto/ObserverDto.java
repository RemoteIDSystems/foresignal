package ai.foresignal.api.dto;

/**
 * The {@code Observer} schema: a receiver that observed a record.
 *
 * @param receiverId the receiver
 * @param frequencyHz the receive frequency
 * @param rssi received signal strength
 * @param snr signal-to-noise ratio
 * @param decodeQuality decoder quality figure
 */
public record ObserverDto(
        String receiverId, Long frequencyHz, Double rssi, Double snr, Double decodeQuality) {}
