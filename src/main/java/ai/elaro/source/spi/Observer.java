package ai.elaro.source.spi;

/**
 * One receiver that contributed to an observation (Core-Data-Model §14).
 *
 * <p>Matters for crowd-sourced feeds: knowing that a position came from one distant receiver rather
 * than six nearby ones is the difference between a usable track and noise. At most 8 per record.
 *
 * @param receiverId the upstream's identifier for the receiver
 * @param frequencyHz the frequency it was heard on, where applicable
 * @param rssi received signal strength
 * @param snr signal-to-noise ratio
 * @param decodeQuality upstream-specific decode confidence
 */
public record Observer(
        String receiverId, Long frequencyHz, Double rssi, Double snr, Double decodeQuality) {

    public static Observer of(String receiverId) {
        return new Observer(receiverId, null, null, null, null);
    }
}
