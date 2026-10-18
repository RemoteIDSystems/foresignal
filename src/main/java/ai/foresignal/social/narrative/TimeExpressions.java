package ai.foresignal.social.narrative;

import ai.foresignal.social.TextFolding;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Time expressions of a claim, resolved relative to the post's {@code createdAt} (prompt 136). Deterministic and
 * multilingual (English, Arabic, Hebrew, Persian, Russian and Ukrainian), matched on folded text:
 *
 * <ul>
 *   <li>"just now", "moments ago": the 15 minutes before the post (precision {@code minute});
 *   <li>"N minutes ago" and "N hours ago": that instant (precision {@code minute} or {@code hour});
 *   <li>"last night", "overnight": 18:00 UTC the day before to 06:00 UTC of the post's day, or of the next day when
 *       posted in the evening (precision {@code hour});
 *   <li>"this morning": 00:00 to 12:00 UTC of the post's day (precision {@code hour});
 *   <li>"today": the post's UTC day up to the post (precision {@code day});
 *   <li>"yesterday": the UTC day before (precision {@code day}).
 * </ul>
 * The poster's own timezone is unknown, so day boundaries are UTC; the widths say how precise the expression is, not
 * that the event happened. The first expression found wins. Pure.
 */
public final class TimeExpressions {

    /**
     * A resolved time.
     *
     * @param from the start
     * @param to the end, or null for an instant
     * @param precision {@code minute}, {@code hour} or {@code day}
     */
    public record EventTime(Instant from, Instant to, String precision) {

        /** The {@code eventTime} member of a claim. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("from", from.toString());
            out.put("to", to == null ? null : to.toString());
            out.put("precision", precision);
            return out;
        }
    }

    private static final Map<String, List<String>> PHRASES = new LinkedHashMap<>();

    static {
        PHRASES.put("now", List.of("just now", "moments ago", "right now", "الان", "قبل قليل", "עכשיו", "הרגע",
                "همین الان", "только что", "щойно"));
        PHRASES.put("last-night", List.of("last night", "overnight", "الليلة الماضية", "ليلة امس", "אמש", "הלילה",
                "دیشب", "прошлой ночью", "этой ночью", "минулої ночі", "цієї ночі", "вночі", "ночью"));
        PHRASES.put("morning", List.of("this morning", "earlier this morning", "صباح اليوم", "هذا الصباح", "הבוקר",
                "امروز صبح", "сегодня утром", "сьогодні вранці"));
        PHRASES.put("today", List.of("today", "earlier today", "اليوم", "היום", "امروز", "сегодня", "сьогодні"));
        PHRASES.put("yesterday", List.of("yesterday", "امس", "البارحة", "אתמול", "دیروز", "вчера", "вчора"));
    }

    private static final Pattern MINUTES = Pattern.compile(
            "(?:^| )(\\d{1,3}) (?:min|mins|minute|minutes) ago(?: |$)|(?:^| )(\\d{1,3}) (?:минут|минуты|минуту|хвилин)"
                    + " назад(?: |$)|(?:^| )منذ (\\d{1,3}) دقيقه|(?:^| )(\\d{1,3}) دقیقه پیش");
    private static final Pattern HOURS = Pattern.compile(
            "(?:^| )(\\d{1,2}|an|one) (?:hour|hours|hr|hrs) ago(?: |$)|(?:^| )(\\d{1,2}) (?:час|часа|часов|години|годин)"
                    + " (?:назад|тому)(?: |$)|(?:^| )منذ (\\d{1,2}) ساع(?:ات|ه)|(?:^| )(\\d{1,2}) ساعت پیش");

    private TimeExpressions() {}

    /**
     * The first time expression of a text, resolved against the post time.
     *
     * @param text the post text (raw or folded)
     * @param createdAt the post's creation time
     * @return the time, or null when the text states none
     */
    public static EventTime resolve(String text, Instant createdAt) {
        if (text == null || createdAt == null) {
            return null;
        }
        String folded = TextFolding.fold(text);
        Matcher m = MINUTES.matcher(folded);
        if (m.find()) {
            int n = Integer.parseInt(first(m));
            return new EventTime(createdAt.minus(Duration.ofMinutes(n)).truncatedTo(ChronoUnit.MINUTES), null,
                    "minute");
        }
        m = HOURS.matcher(folded);
        if (m.find()) {
            String g = first(m);
            int n = g.equals("an") || g.equals("one") ? 1 : Integer.parseInt(g);
            return new EventTime(createdAt.minus(Duration.ofHours(n)).truncatedTo(ChronoUnit.HOURS), null, "hour");
        }
        Instant day = createdAt.truncatedTo(ChronoUnit.DAYS);
        for (Map.Entry<String, List<String>> e : PHRASES.entrySet()) {
            for (String phrase : e.getValue()) {
                String p = TextFolding.fold(phrase);
                int at = folded.indexOf(p);
                while (at >= 0) {
                    if (TextFolding.onBoundaries(folded, p, at, at + p.length())) {
                        return of(e.getKey(), createdAt, day);
                    }
                    at = folded.indexOf(p, at + 1);
                }
            }
        }
        return null;
    }

    private static EventTime of(String id, Instant createdAt, Instant day) {
        return switch (id) {
            case "now" -> new EventTime(createdAt.minus(Duration.ofMinutes(15)), createdAt, "minute");
            case "last-night" -> {
                boolean evening = createdAt.isAfter(day.plus(Duration.ofHours(18)));
                Instant start = evening ? day.plus(Duration.ofHours(18)) : day.minus(Duration.ofHours(6));
                Instant end = evening ? createdAt : day.plus(Duration.ofHours(6));
                yield new EventTime(start, end.isAfter(createdAt) ? createdAt : end, "hour");
            }
            case "morning" -> {
                Instant end = day.plus(Duration.ofHours(12));
                yield new EventTime(day, end.isAfter(createdAt) ? createdAt : end, "hour");
            }
            case "today" -> new EventTime(day, createdAt, "day");
            case "yesterday" -> new EventTime(day.minus(Duration.ofDays(1)), day, "day");
            default -> null;
        };
    }

    private static String first(Matcher m) {
        for (int i = 1; i <= m.groupCount(); i++) {
            if (m.group(i) != null) {
                return m.group(i);
            }
        }
        return "1";
    }

    /** The earliest {@code from} of several times, or null. */
    public static EventTime earliest(List<EventTime> times) {
        EventTime best = null;
        for (EventTime t : new ArrayList<>(times)) {
            if (t != null && (best == null || t.from().isBefore(best.from()))) {
                best = t;
            }
        }
        return best;
    }
}
