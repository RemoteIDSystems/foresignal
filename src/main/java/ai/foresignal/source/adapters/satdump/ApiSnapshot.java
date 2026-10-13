package ai.foresignal.source.adapters.satdump;

import java.time.Instant;
import java.util.List;

/**
 * One reading of SatDump's {@code /api}, reduced to what the station loop uses (prompt 049).
 *
 * <p>Module statistics differ by decoder, so the per-module values are folded into one figure each:
 * the best SNR and peak SNR, the lowest Viterbi BER, the mean Reed-Solomon figure, the synced deframer
 * state when any module is synced, and {@code locked} when any module reports a locked deframer or
 * Viterbi decoder.
 *
 * @param mode {@code live} or {@code autotrack}
 * @param frequencyHz the source frequency (autotrack), or null
 * @param target the tracker's object name, or null when it tracks nothing ({@code None})
 * @param az the target's azimuth in degrees, or null
 * @param el the target's elevation in degrees, or null
 * @param nextAos the next AOS, or null
 * @param nextLos the next LOS, or null
 * @param rotatorEngaged whether the rotator is engaged, or null
 * @param modules the ids of the modules reporting statistics, sorted
 * @param snr the best SNR in dB, or null
 * @param peakSnr the best peak SNR in dB, or null
 * @param ber the lowest Viterbi BER, or null
 * @param deframerState {@code SYNCED} when any module is synced, else the first state seen, or null
 * @param locked true when any module reports a locked deframer or Viterbi decoder
 * @param rsAvg the mean Reed-Solomon figure, or null
 * @param driftReason why the response did not match the pinned shape, or null when it did
 */
public record ApiSnapshot(
        String mode,
        Long frequencyHz,
        String target,
        Double az,
        Double el,
        Instant nextAos,
        Instant nextLos,
        Boolean rotatorEngaged,
        List<String> modules,
        Double snr,
        Double peakSnr,
        Double ber,
        String deframerState,
        boolean locked,
        Double rsAvg,
        String driftReason) {

    /** Live mode: {@code satdump legacy live} serves the module statistics at top level. */
    public static final String LIVE = "live";

    /** Autotrack mode: {@code satdump legacy autotrack} wraps them with the object tracker. */
    public static final String AUTOTRACK = "autotrack";

    /** Copies the module list. */
    public ApiSnapshot {
        modules = modules == null ? List.of() : List.copyOf(modules);
    }

    /** True when a pipeline is running (any module reports statistics). */
    public boolean pipelineRunning() {
        return !modules.isEmpty();
    }

    /** True in autotrack mode with a target above the horizon. */
    public boolean trackingAboveHorizon() {
        return AUTOTRACK.equals(mode) && target != null && el != null && el > 0;
    }

    /** True in autotrack mode. */
    public boolean autotrack() {
        return AUTOTRACK.equals(mode);
    }
}
