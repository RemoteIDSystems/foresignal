package ai.foresignal.trigger;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Recurring local-time windows in which a rule is active (prompt 017), evaluated on a version's
 * {@code observedAt} in the rule's IANA timezone.
 *
 * <p>Evaluation goes through {@link ZonedDateTime}, so it is DST-safe: a window is a range of local
 * wall-clock times, and on the night a clock jumps the window simply covers the wall-clock times that
 * exist. A window whose {@code end} is before its {@code start} spans midnight: it is active from
 * {@code start} on a listed day until {@code end} on the next day.
 *
 * @param zone the timezone
 * @param windows the windows; the rule is active when any of them is
 */
public record ActiveWindow(ZoneId zone, List<Window> windows) {

    /** {@code HH:mm}. */
    static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    /** The three-letter day names of the API, in ISO order. */
    static final List<String> DAY_NAMES = List.of("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN");

    /** Copies the windows. */
    public ActiveWindow {
        windows = List.copyOf(windows);
    }

    /**
     * One window.
     *
     * @param days the days it starts on
     * @param start the local start time, inclusive
     * @param end the local end time, exclusive; before {@code start} when it spans midnight
     */
    public record Window(Set<DayOfWeek> days, LocalTime start, LocalTime end) {

        /** Copies the days. */
        public Window {
            days = days.isEmpty() ? Set.of() : Set.copyOf(EnumSet.copyOf(days));
        }

        /** True when the local {@code day} and {@code time} fall inside this window. */
        boolean contains(DayOfWeek day, LocalTime time) {
            if (start.isBefore(end)) {
                return days.contains(day) && !time.isBefore(start) && time.isBefore(end);
            }
            boolean lateToday = days.contains(day) && !time.isBefore(start);
            boolean earlyAfterYesterday = days.contains(day.minus(1)) && time.isBefore(end);
            return lateToday || earlyAfterYesterday;
        }
    }

    /** True when {@code instant} falls inside any window. */
    public boolean isActive(Instant instant) {
        ZonedDateTime local = instant.atZone(zone);
        DayOfWeek day = local.getDayOfWeek();
        LocalTime time = local.toLocalTime();
        for (Window window : windows) {
            if (window.contains(day, time)) {
                return true;
            }
        }
        return false;
    }

    /** The stored and API form ({@code ActiveWindow} schema). */
    public Map<String, Object> toMap() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Window window : windows) {
            Map<String, Object> w = new LinkedHashMap<>();
            List<String> days = new ArrayList<>();
            for (DayOfWeek day : DayOfWeek.values()) {
                if (window.days().contains(day)) {
                    days.add(DAY_NAMES.get(day.ordinal()));
                }
            }
            w.put("days", days);
            w.put("start", window.start().format(HH_MM));
            w.put("end", window.end().format(HH_MM));
            list.add(w);
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("timezone", zone.getId());
        map.put("windows", list);
        return map;
    }

    /** Reads the stored form; null stays null (always active). */
    @SuppressWarnings("unchecked")
    public static ActiveWindow fromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        List<Window> windows = new ArrayList<>();
        Object raw = map.get("windows");
        if (raw instanceof List<?> list) {
            for (Object item : list) {
                Map<String, Object> w = (Map<String, Object>) item;
                Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
                for (Object day : (List<Object>) w.get("days")) {
                    days.add(day(String.valueOf(day)));
                }
                windows.add(new Window(days, LocalTime.parse(String.valueOf(w.get("start")), HH_MM),
                        LocalTime.parse(String.valueOf(w.get("end")), HH_MM)));
            }
        }
        return new ActiveWindow(ZoneId.of(String.valueOf(map.get("timezone"))), windows);
    }

    /**
     * The day named {@code name} ({@code MON} … {@code SUN}).
     *
     * @throws IllegalArgumentException for anything else
     */
    static DayOfWeek day(String name) {
        int index = DAY_NAMES.indexOf(name);
        if (index < 0) {
            throw new IllegalArgumentException("days must be MON, TUE, WED, THU, FRI, SAT or SUN");
        }
        return DayOfWeek.of(index + 1);
    }
}
