package ai.foresignal.notify.mesh;

import java.util.Locale;

/**
 * LoRa time on air (prompt 084), with the Semtech formula (SX1276 datasheet §4.1.1.7, application note AN1200.13):
 *
 * <ul>
 *   <li>{@code Tsym = 2^SF / BW};
 *   <li>preamble {@code (nPreamble + 4.25) · Tsym};
 *   <li>payload symbols {@code 8 + max(ceil((8·PL − 4·SF + 28 + 16·CRC − 20·IH) / (4·(SF − 2·DE))) · (CR + 4), 0)},
 *       with {@code CR} 1–4 for coding rates 4/5–4/8 and low-data-rate optimization {@code DE = 1} when
 *       {@code Tsym > 16 ms};
 *   <li>time on air = preamble + payload symbols · {@code Tsym}.
 * </ul>
 *
 * <p>{@code PL} is the message bytes plus a per-network overhead: {@value #MESHTASTIC_OVERHEAD_BYTES} bytes for
 * Meshtastic (its 16-byte header plus the encrypted protobuf envelope) and {@value #MESHCORE_OVERHEAD_BYTES} for
 * MeshCore (header, path, channel hash, MAC and the text frame's timestamp). Both are conservative estimates, so the
 * governor errs toward less airtime, never more. The explicit header and the CRC are on, as both firmwares send them.
 *
 * <p>The operator confirms that the preset matches the node: Foresignal never reads radio settings from it.
 *
 * @param spreadingFactor SF, 5 to 12
 * @param bandwidthHz the bandwidth in Hz
 * @param codingRate the coding-rate denominator, 5 to 8 (4/5 to 4/8)
 * @param preambleSymbols the preamble length in symbols
 */
public record LoRaAirtime(int spreadingFactor, double bandwidthHz, int codingRate, int preambleSymbols) {

    /** The Meshtastic per-packet overhead added to the message bytes (conservative estimate). */
    public static final int MESHTASTIC_OVERHEAD_BYTES = 32;

    /** The MeshCore per-packet overhead added to the message bytes (conservative estimate). */
    public static final int MESHCORE_OVERHEAD_BYTES = 24;

    /** The {@code airtimePreset} of Meshtastic LongFast. */
    public static final String LONGFAST = "meshtastic-longfast";

    /** The {@code airtimePreset} of Meshtastic MediumFast. */
    public static final String MEDIUMFAST = "meshtastic-mediumfast";

    /** The {@code airtimePreset} of operator-entered radio settings ({@code airtimeCustom}). */
    public static final String CUSTOM = "custom";

    /** Meshtastic LongFast: SF11, 250 kHz, 4/5, 16-symbol preamble. */
    public static final LoRaAirtime MESHTASTIC_LONGFAST = new LoRaAirtime(11, 250_000, 5, 16);

    /** Meshtastic MediumFast: SF9, 250 kHz, 4/5, 16-symbol preamble. */
    public static final LoRaAirtime MESHTASTIC_MEDIUMFAST = new LoRaAirtime(9, 250_000, 5, 16);

    /** Checks the ranges. */
    public LoRaAirtime {
        if (spreadingFactor < 5 || spreadingFactor > 12) {
            throw new IllegalArgumentException("the spreading factor must be 5 to 12, but was " + spreadingFactor);
        }
        if (!(bandwidthHz > 0) || bandwidthHz > 1_625_000) {
            throw new IllegalArgumentException("the bandwidth must be above 0 and at most 1625 kHz, but was "
                    + bandwidthHz + " Hz");
        }
        if (codingRate < 5 || codingRate > 8) {
            throw new IllegalArgumentException("the coding rate denominator must be 5 to 8 (4/5 to 4/8), but was "
                    + codingRate);
        }
        if (preambleSymbols < 1 || preambleSymbols > 65_535) {
            throw new IllegalArgumentException("the preamble must be 1 to 65535 symbols, but was " + preambleSymbols);
        }
    }

    /** The symbol time in seconds. */
    public double symbolSeconds() {
        return Math.pow(2, spreadingFactor) / bandwidthHz;
    }

    /** True when low-data-rate optimization is on ({@code Tsym > 16 ms}). */
    public boolean lowDataRateOptimize() {
        return symbolSeconds() > 0.016;
    }

    /** The number of payload symbols for {@code payloadBytes} on the air (explicit header, CRC on). */
    public int payloadSymbols(int payloadBytes) {
        int de = lowDataRateOptimize() ? 1 : 0;
        int crc = 1;
        int ih = 0;
        double numerator = 8.0 * payloadBytes - 4.0 * spreadingFactor + 28 + 16.0 * crc - 20.0 * ih;
        double denominator = 4.0 * (spreadingFactor - 2 * de);
        int blocks = (int) Math.ceil(numerator / denominator);
        return 8 + Math.max(blocks * codingRate, 0);
    }

    /** The time on air in seconds of {@code payloadBytes} on the air (the overhead already included). */
    public double timeOnAirSeconds(int payloadBytes) {
        if (payloadBytes < 0) {
            throw new IllegalArgumentException("the payload cannot be negative");
        }
        double tsym = symbolSeconds();
        return (preambleSymbols + 4.25) * tsym + payloadSymbols(payloadBytes) * tsym;
    }

    /**
     * The time on air in seconds of a message of {@code messageBytes} on {@code network}, with the network's
     * overhead.
     *
     * @param network {@code meshtastic} or {@code meshcore}
     */
    public double messageSeconds(String network, int messageBytes) {
        return timeOnAirSeconds(messageBytes + overheadBytes(network));
    }

    /** The per-packet overhead of {@code network}. */
    public static int overheadBytes(String network) {
        return "meshcore".equals(network) ? MESHCORE_OVERHEAD_BYTES : MESHTASTIC_OVERHEAD_BYTES;
    }

    /**
     * The radio settings of a preset.
     *
     * @param preset {@value #LONGFAST}, {@value #MEDIUMFAST} or {@value #CUSTOM}
     * @param custom the operator's settings for {@value #CUSTOM}: {@code spreadingFactor}, {@code bandwidthKhz},
     *     {@code codingRate} (denominator) and {@code preambleSymbols}; ignored otherwise
     * @throws IllegalArgumentException for an unknown preset or missing or invalid custom settings
     */
    public static LoRaAirtime preset(String preset, java.util.Map<?, ?> custom) {
        String name = preset == null ? "" : preset.strip().toLowerCase(Locale.ROOT);
        return switch (name) {
            case LONGFAST -> MESHTASTIC_LONGFAST;
            case MEDIUMFAST -> MESHTASTIC_MEDIUMFAST;
            case CUSTOM -> custom(custom);
            default -> throw new IllegalArgumentException("must be one of " + LONGFAST + ", " + MEDIUMFAST + ", "
                    + CUSTOM);
        };
    }

    private static LoRaAirtime custom(java.util.Map<?, ?> custom) {
        if (custom == null) {
            throw new IllegalArgumentException("airtimeCustom is required when airtimePreset is custom");
        }
        Object sf = custom.get("spreadingFactor");
        Object bw = custom.get("bandwidthKhz");
        Object cr = custom.get("codingRate");
        Object preamble = custom.get("preambleSymbols");
        if (!(sf instanceof Number s) || !(bw instanceof Number b) || !(cr instanceof Number c)
                || !(preamble instanceof Number p)) {
            throw new IllegalArgumentException("airtimeCustom needs spreadingFactor, bandwidthKhz, codingRate and"
                    + " preambleSymbols as numbers");
        }
        if (s.doubleValue() != Math.rint(s.doubleValue()) || c.doubleValue() != Math.rint(c.doubleValue())
                || p.doubleValue() != Math.rint(p.doubleValue())) {
            throw new IllegalArgumentException("spreadingFactor, codingRate and preambleSymbols must be integers");
        }
        return new LoRaAirtime(s.intValue(), b.doubleValue() * 1000.0, c.intValue(), p.intValue());
    }
}
