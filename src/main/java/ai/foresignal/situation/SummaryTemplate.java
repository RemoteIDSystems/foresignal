package ai.foresignal.situation;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * The deterministic situation summary (prompt 118 §4), template version {@value #VERSION}:
 *
 * <pre>
 * "{n} signals from {k} families across {regions} since {started}: {top 3 member titles}.
 *  {corroboration sentence}. Last activity {ago}."
 * </pre>
 *
 * <p>The stored summary is rendered when the situation changes; responses render it again at read time so
 * "Last activity" is relative to now. {@link #stem} is the same text without the last sentence (the part that does
 * not age), which the {@code foresignal-intel} record carries and the embedding similarity compares against.
 */
public final class SummaryTemplate {

    /** The template version. */
    public static final String VERSION = "1.0.0";

    /** The most region codes named before "+N more". */
    static final int MAX_REGIONS = 5;

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'")
            .withZone(ZoneOffset.UTC);

    private SummaryTemplate() {}

    /**
     * The inputs of the template.
     *
     * @param signals the member count
     * @param families the distinct signal families of the members
     * @param regionCodes the region codes
     * @param startedAt the start
     * @param topTitles the titles of the most recent core members (up to 3 are used)
     * @param corroborated corroborated members
     * @param uncorroborated uncorroborated members
     * @param lastActivityAt the last activity
     */
    public record Facts(int signals, int families, List<String> regionCodes, Instant startedAt, List<String> topTitles,
            int corroborated, int uncorroborated, Instant lastActivityAt) {}

    /** The full summary at {@code now}. */
    public static String render(Facts facts, Instant now) {
        return stem(facts) + " Last activity " + ago(facts.lastActivityAt(), now) + ".";
    }

    /** The summary without the "Last activity" sentence. */
    public static String stem(Facts facts) {
        StringBuilder out = new StringBuilder();
        out.append(facts.signals()).append(facts.signals() == 1 ? " signal from " : " signals from ")
                .append(facts.families()).append(facts.families() == 1 ? " family" : " families")
                .append(" across ").append(regions(facts.regionCodes()))
                .append(" since ").append(facts.startedAt() == null ? "an unknown time" : DAY.format(facts.startedAt()));
        List<String> titles = facts.topTitles() == null ? List.of()
                : facts.topTitles().stream().filter(t -> t != null && !t.isBlank()).limit(3).toList();
        out.append(titles.isEmpty() ? "." : ": " + String.join("; ", titles) + ".");
        out.append(' ').append(corroboration(facts.corroborated(), facts.uncorroborated())).append('.');
        return out.toString();
    }

    /** The corroboration sentence. */
    static String corroboration(int corroborated, int uncorroborated) {
        int total = corroborated + uncorroborated;
        if (total == 0) {
            return "No member has a canonical event to corroborate";
        }
        if (corroborated == 0) {
            return "Built only on uncorroborated reports (" + uncorroborated + ")";
        }
        long percent = Math.round(100.0 * corroborated / total);
        return corroborated + " of " + total + " members corroborated by independent evidence groups (" + percent + "%)";
    }

    /** The region list: up to five codes, then "+N more"; "an unlocated area" when none. */
    static String regions(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return "an unlocated area";
        }
        if (codes.size() <= MAX_REGIONS) {
            return String.join(", ", codes);
        }
        return String.join(", ", codes.subList(0, MAX_REGIONS)) + " +" + (codes.size() - MAX_REGIONS) + " more";
    }

    /** "just now", "{m} min ago", "{h} h ago" or "{d} d ago". */
    static String ago(Instant then, Instant now) {
        if (then == null || now == null) {
            return "at an unknown time";
        }
        Duration age = Duration.between(then, now);
        if (age.isNegative() || age.toMinutes() < 1) {
            return "just now";
        }
        if (age.toHours() < 1) {
            return age.toMinutes() + " min ago";
        }
        if (age.toDays() < 2) {
            return age.toHours() + " h ago";
        }
        return age.toDays() + " d ago";
    }
}
