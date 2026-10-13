package ai.foresignal.raster.model;

import java.util.Objects;

/**
 * Which {@code .idx} line holds a field (prompt 051): matched by variable and level and, for
 * accumulations, by the run-total time range; never by message number.
 *
 * <p>A run-total accumulation at step {@code N} is {@code 0-N hour acc}. NCEP writes whole days as
 * days: GFS lists {@code APCP:surface:0-1 day acc fcst} at f024 and {@code 0-5 day acc fcst} at f120
 * (verified on the 2026-10-12 12z run), so {@code 0-(N/24) day acc} matches too when {@code N} is a
 * multiple of 24. The interval accumulation listed beside it ({@code 18-24 hour acc fcst}) never
 * matches.
 *
 * @param variable the variable, for example {@code UGRD}
 * @param level the level, for example {@code 10 m above ground}
 * @param accumulationStep the step whose run-total accumulation is wanted, or null for any time range
 */
public record FieldSelector(String variable, String level, Integer accumulationStep) {

    /** Checks the parts. */
    public FieldSelector {
        Objects.requireNonNull(variable, "variable");
        Objects.requireNonNull(level, "level");
        if (accumulationStep != null && accumulationStep < 0) {
            throw new IllegalArgumentException("accumulationStep must not be negative");
        }
    }

    /** A field at any time range. */
    public static FieldSelector of(String variable, String level) {
        return new FieldSelector(variable, level, null);
    }

    /** The run-total accumulation of {@code variable} at {@code level} for {@code step}. */
    public static FieldSelector runTotal(String variable, String level, int step) {
        return new FieldSelector(variable, level, step);
    }

    /** True when {@code message} is this field. */
    public boolean matches(GribIdx.Message message) {
        if (!variable.equals(message.variable()) || !level.equals(message.level())) {
            return false;
        }
        if (accumulationStep == null) {
            return true;
        }
        String range = message.timeRange();
        if (startsWithWord(range, "0-" + accumulationStep + " hour acc")) {
            return true;
        }
        return accumulationStep > 0 && accumulationStep % 24 == 0
                && startsWithWord(range, "0-" + accumulationStep / 24 + " day acc");
    }

    /** {@code VAR:LEVEL} (plus the time range of an accumulation), as health reasons name it. */
    public String label() {
        return variable + ":" + level + (accumulationStep == null ? "" : ":0-" + accumulationStep + " hour acc");
    }

    private static boolean startsWithWord(String text, String prefix) {
        return text.startsWith(prefix) && (text.length() == prefix.length() || text.charAt(prefix.length()) == ' ');
    }
}
