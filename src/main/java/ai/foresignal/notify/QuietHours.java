package ai.foresignal.notify;

import ai.foresignal.trigger.Severity;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A channel's quiet hours (OpenAPI {@code QuietHours}, prompt 018): {@code {start: "22:00", end:
 * "07:00", days: ["MON", …], bypassSeverity: "critical"}} in the channel timezone.
 *
 * <p>A window starts at {@code start} on each listed day (every day when {@code days} is empty) and
 * ends at the next {@code end}: the same day when {@code end} is later than {@code start}, otherwise
 * the next day. Messages at or above {@code bypassSeverity} are sent anyway; a null bypass means
 * nothing bypasses. Times that do not exist on a DST transition day resolve as {@link ZonedDateTime}
 * resolves them (shifted forward by the gap).
 *
 * @param start the window start, local time
 * @param end the window end, local time
 * @param days the days a window starts on; empty means every day
 * @param bypassSeverity the bypass severity wire value, or null
 */
public record QuietHours(LocalTime start, LocalTime end, Set<DayOfWeek> days, String bypassSeverity) {

    /** Copies the days. */
    public QuietHours {
        days = days == null || days.isEmpty() ? EnumSet.noneOf(DayOfWeek.class) : EnumSet.copyOf(days);
    }

    /** The end of the quiet window containing {@code now}, or empty when {@code now} is outside one. */
    public Optional<Instant> windowEnd(Instant now, ZoneId zone) {
        LocalDate today = now.atZone(zone).toLocalDate();
        // A window that started yesterday may still be running; one starting today may have begun.
        for (LocalDate day : List.of(today.minusDays(1), today)) {
            if (!days.isEmpty() && !days.contains(day.getDayOfWeek())) {
                continue;
            }
            ZonedDateTime from = ZonedDateTime.of(day, start, zone);
            ZonedDateTime to = ZonedDateTime.of(end.isAfter(start) ? day : day.plusDays(1), end, zone);
            Instant a = from.toInstant();
            Instant b = to.toInstant();
            if (!now.isBefore(a) && now.isBefore(b)) {
                return Optional.of(b);
            }
        }
        return Optional.empty();
    }

    /** True when {@code severity} is at or above the bypass severity. */
    public boolean bypasses(String severity) {
        if (bypassSeverity == null || severity == null) {
            return false;
        }
        return Severity.fromWire(severity).compareTo(Severity.fromWire(bypassSeverity)) >= 0;
    }

    /** The stored and API form. */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("start", start.toString());
        out.put("end", end.toString());
        List<String> names = new ArrayList<>();
        for (DayOfWeek day : days) {
            names.add(code(day));
        }
        out.put("days", names);
        out.put("bypassSeverity", bypassSeverity);
        return out;
    }

    /** Reads the stored form; null for null. */
    public static QuietHours fromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        if (map.get("days") instanceof List<?> list) {
            for (Object item : list) {
                day(String.valueOf(item)).ifPresent(days::add);
            }
        }
        Object bypass = map.get("bypassSeverity");
        return new QuietHours(LocalTime.parse(String.valueOf(map.get("start"))),
                LocalTime.parse(String.valueOf(map.get("end"))), days, bypass == null ? null : String.valueOf(bypass));
    }

    /** {@code MON} … {@code SUN}. */
    public static String code(DayOfWeek day) {
        return day.getDisplayName(TextStyle.SHORT, Locale.US).toUpperCase(Locale.ROOT);
    }

    /** Parses {@code MON} … {@code SUN}. */
    public static Optional<DayOfWeek> day(String code) {
        for (DayOfWeek day : DayOfWeek.values()) {
            if (code(day).equals(code)) {
                return Optional.of(day);
            }
        }
        return Optional.empty();
    }
}
