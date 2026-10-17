package ai.foresignal.situation;

import java.util.List;
import java.util.Locale;

/**
 * Severity arithmetic for situations (prompt 118): the three levels {@code info < warning < critical}, and the
 * mapping of the severities other objects report onto them.
 */
public final class Severities {

    /** The levels, lowest first. */
    public static final List<String> LEVELS = List.of("info", "warning", "critical");

    private Severities() {}

    /** The rank of a level (0, 1, 2), or -1 for null or unknown. */
    public static int rank(String severity) {
        return severity == null ? -1 : LEVELS.indexOf(severity);
    }

    /** The higher of two levels (null when both are null or unknown). */
    public static String max(String a, String b) {
        return rank(a) >= rank(b) ? (rank(a) < 0 ? null : a) : b;
    }

    /** True when {@code severity} is one of {@link #LEVELS}. */
    public static boolean valid(String severity) {
        return rank(severity) >= 0;
    }

    /**
     * Maps a reported severity onto the three levels: {@code critical}, {@code severe}, {@code extreme} and
     * {@code high} are critical; {@code warning}, {@code significant}, {@code moderate} and {@code medium} are
     * warning; anything else (info, notable, minor, low, unknown) is info.
     */
    public static String normalize(String reported) {
        if (reported == null) {
            return "info";
        }
        return switch (reported.toLowerCase(Locale.ROOT)) {
            case "critical", "severe", "extreme", "high" -> "critical";
            case "warning", "significant", "moderate", "medium" -> "warning";
            default -> "info";
        };
    }
}
