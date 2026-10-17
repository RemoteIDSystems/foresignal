package ai.foresignal.observability;

/**
 * Whether an absence may be read as "nothing happened" (prompt 116, OBS-1).
 *
 * @param ok true when every involved source was healthy over the window and {@code p ≥ absenceMinDetect}
 * @param p the combined detection probability (null when not computed because coverage failed)
 * @param reason when not ok: {@value #LOW_DETECTABILITY} or the coverage reason
 */
public record AbsenceCheck(boolean ok, Double p, String reason) {

    /** The reason of an absence that detectability cannot support. */
    public static final String LOW_DETECTABILITY = "LOW_DETECTABILITY";
}
