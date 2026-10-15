package ai.foresignal.analyst;

import ai.foresignal.briefing.BriefingValidator;
import ai.foresignal.settings.Durations;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * An analyst profile's schedule (prompt 102): either a 5-field cron with an IANA timezone, or
 * {@code every} a duration of at least ten minutes. The default is every 30 minutes.
 */
public record AnalystSchedule(String cron, String timezone, Duration every) {

    /** The floor for an {@code every} schedule. */
    public static final Duration MIN_EVERY = Duration.ofMinutes(10);

    /** The default schedule. */
    public static final Duration DEFAULT_EVERY = Duration.ofMinutes(30);

    /** Parses a schedule map ({@code cron}, {@code timezone}, {@code every}). */
    public static AnalystSchedule of(Map<String, Object> map) {
        if (map == null) {
            return new AnalystSchedule(null, "UTC", DEFAULT_EVERY);
        }
        String cron = map.get("cron") == null ? null : String.valueOf(map.get("cron"));
        String timezone = map.get("timezone") == null ? "UTC" : String.valueOf(map.get("timezone"));
        Duration every = null;
        if (map.get("every") != null) {
            every = Durations.parse(String.valueOf(map.get("every")));
            if (every.compareTo(MIN_EVERY) < 0) {
                throw new IllegalArgumentException("every must be at least 10 minutes");
            }
        }
        if (cron == null && every == null) {
            every = DEFAULT_EVERY;
        }
        if (cron != null) {
            BriefingValidator.cron(cron);
        }
        return new AnalystSchedule(cron, timezone, every);
    }

    /** The stored map form. */
    public Map<String, Object> toMap() {
        java.util.Map<String, Object> out = new java.util.LinkedHashMap<>();
        if (cron != null) {
            out.put("cron", cron);
            out.put("timezone", timezone);
        } else {
            out.put("every", every.toString());
        }
        return out;
    }

    /** The next fire time after {@code after}. */
    public Instant next(Instant after) {
        if (cron != null) {
            return BriefingValidator.nextRun(cron, timezone, after);
        }
        return after.plus(every);
    }
}
