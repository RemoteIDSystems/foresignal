package ai.foresignal.weather;

import ai.foresignal.ask.CitationSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The deterministic weather check of an Ask answer (prompt 056): when a weather tool result in the turn had a
 * status other than {@code ok} and the answer mentions neither that result's {@code statusText} nor an equivalent
 * status phrase, a standard caveat listing the affected products and statuses is appended, and a warning is added to
 * the response. No-coverage, stale and unavailable mean unknown, never clear.
 *
 * <p>Equivalent phrases, case-insensitive: {@code stale} for stale; {@code no coverage}, {@code no-coverage},
 * {@code outside coverage}, {@code outside product coverage} or {@code not covered} for no-coverage; {@code no data}
 * or {@code no-data} for no-data; {@code unavailable} or {@code not available} for unavailable (and any other
 * status: the status word itself).
 */
public final class WeatherAnswerGuard {

    /** The start of the caveat. */
    public static final String CAVEAT_PREFIX = "Caveat (automated check): some weather data was not current or not"
            + " available, so it is unknown, not clear:";

    /** The start of the warning added when the caveat is appended. */
    public static final String WARNING_PREFIX = "weather caveat appended: the answer did not state the status of";

    private WeatherAnswerGuard() {}

    /**
     * The issues the answer does not address.
     *
     * @param answer the model's answer
     * @param issues the non-ok weather statuses of the turn
     */
    public static List<CitationSet.WeatherIssue> unaddressed(String answer, List<CitationSet.WeatherIssue> issues) {
        String text = answer == null ? "" : answer.toLowerCase(Locale.ROOT);
        List<CitationSet.WeatherIssue> out = new ArrayList<>();
        for (CitationSet.WeatherIssue issue : issues) {
            if (!addressed(text, issue)) {
                out.add(issue);
            }
        }
        return out;
    }

    /** True when the lower-cased answer states the issue's status. */
    static boolean addressed(String lowerAnswer, CitationSet.WeatherIssue issue) {
        if (issue.statusText() != null && !issue.statusText().isBlank()
                && lowerAnswer.contains(issue.statusText().toLowerCase(Locale.ROOT).strip())) {
            return true;
        }
        for (String phrase : phrases(issue.status())) {
            if (lowerAnswer.contains(phrase)) {
                return true;
            }
        }
        return false;
    }

    /** The equivalent phrases of a status. */
    static List<String> phrases(String status) {
        String s = status == null ? "" : status.toLowerCase(Locale.ROOT);
        return switch (s) {
            case "stale" -> List.of("stale");
            case "no-coverage" -> List.of("no coverage", "no-coverage", "outside coverage", "outside product coverage",
                    "not covered");
            case "no-data" -> List.of("no data", "no-data");
            case "unavailable" -> List.of("unavailable", "not available");
            default -> s.isEmpty() ? List.of() : List.of(s);
        };
    }

    /** The caveat for {@code issues} (not empty). */
    public static String caveat(List<CitationSet.WeatherIssue> issues) {
        return CAVEAT_PREFIX + " " + list(issues) + ". Weather values are automated and are not official warnings.";
    }

    /** The warning for {@code issues} (not empty). */
    public static String warning(List<CitationSet.WeatherIssue> issues) {
        return WARNING_PREFIX + " " + list(issues);
    }

    private static String list(List<CitationSet.WeatherIssue> issues) {
        List<String> parts = new ArrayList<>();
        for (CitationSet.WeatherIssue issue : issues) {
            parts.add(issue.productId() + " (" + issue.status() + ")");
        }
        return String.join(", ", parts);
    }
}
