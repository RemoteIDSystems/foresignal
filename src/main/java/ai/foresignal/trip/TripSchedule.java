package ai.foresignal.trip;

import ai.foresignal.api.error.ValidationException;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import tools.jackson.databind.JsonNode;

/**
 * A trip's schedule (prompt 114, OpenAPI {@code TripSchedule}): a one-off {@code departAt}, or a recurring commute
 * ({@code days} of week, local {@code departTime}, IANA {@code timezone}).
 *
 * <p>Recurring occurrences are local times: across a DST change the departure stays at the same wall-clock time
 * (a time inside a spring-forward gap moves forward by the gap, as {@link ZonedDateTime#of} resolves it).
 *
 * @param type {@code once} or {@code recurring}
 * @param departAt the one-off departure, or null
 * @param days the recurring days, empty for a one-off trip
 * @param departTime the recurring local departure time, or null
 * @param zone the recurring timezone, or null
 */
public record TripSchedule(String type, Instant departAt, Set<DayOfWeek> days, LocalTime departTime, ZoneId zone) {

    static final List<String> DAY_CODES = List.of("MO", "TU", "WE", "TH", "FR", "SA", "SU");

    /** Copies the days. */
    public TripSchedule {
        days = days == null || days.isEmpty() ? Set.of() : Set.copyOf(EnumSet.copyOf(days));
    }

    /** True for a recurring commute. */
    public boolean recurring() {
        return "recurring".equals(type);
    }

    /**
     * Parses a {@code TripSchedule}.
     *
     * @throws ValidationException when it is not valid
     */
    public static TripSchedule parse(JsonNode node) {
        if (node == null || !node.isObject()) {
            throw ValidationException.forField("schedule", "is required: an object with type once or recurring");
        }
        String type = node.has("type") && node.get("type").isString() ? node.get("type").asString() : null;
        if ("once".equals(type)) {
            JsonNode at = node.get("departAt");
            if (at == null || !at.isString()) {
                throw ValidationException.forField("schedule.departAt", "is required for a one-off trip");
            }
            try {
                return new TripSchedule(type, OffsetDateTime.parse(at.asString()).toInstant(), Set.of(), null, null);
            } catch (DateTimeParseException e) {
                throw ValidationException.forField("schedule.departAt", "must be an RFC 3339 date-time");
            }
        }
        if (!"recurring".equals(type)) {
            throw ValidationException.forField("schedule.type", "must be once or recurring");
        }
        JsonNode daysNode = node.get("days");
        if (daysNode == null || !daysNode.isArray() || daysNode.isEmpty()) {
            throw ValidationException.forField("schedule.days", "is required for a recurring trip: MO … SU");
        }
        Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        for (JsonNode d : daysNode) {
            int index = d.isString() ? DAY_CODES.indexOf(d.asString()) : -1;
            if (index < 0) {
                throw ValidationException.forField("schedule.days", "must contain MO, TU, WE, TH, FR, SA or SU");
            }
            days.add(DayOfWeek.of(index + 1));
        }
        JsonNode time = node.get("departTime");
        LocalTime departTime;
        if (time == null || !time.isString() || !time.asString().matches("^([01][0-9]|2[0-3]):[0-5][0-9]$")) {
            throw ValidationException.forField("schedule.departTime", "is required for a recurring trip: HH:mm");
        }
        departTime = LocalTime.parse(time.asString());
        JsonNode tz = node.get("timezone");
        if (tz == null || !tz.isString()) {
            throw ValidationException.forField("schedule.timezone", "is required for a recurring trip (IANA zone)");
        }
        ZoneId zone;
        try {
            zone = ZoneId.of(tz.asString());
        } catch (DateTimeException e) {
            throw ValidationException.forField("schedule.timezone", "must be an IANA timezone");
        }
        return new TripSchedule(type, null, days, departTime, zone);
    }

    /** The stored and served form. */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("type", type);
        if (recurring()) {
            List<String> codes = new ArrayList<>();
            for (DayOfWeek d : EnumSet.copyOf(days)) {
                codes.add(DAY_CODES.get(d.getValue() - 1));
            }
            out.put("days", codes);
            out.put("departTime", departTime.toString());
            out.put("timezone", zone.getId());
        } else {
            out.put("departAt", departAt.toString());
        }
        return out;
    }

    /** The schedule of a stored map. */
    public static TripSchedule fromMap(Map<String, Object> map, tools.jackson.databind.json.JsonMapper json) {
        return parse(json.valueToTree(map));
    }

    /** The occurrences in {@code [from, to]}, in time order. */
    public List<Instant> occurrences(Instant from, Instant to) {
        List<Instant> out = new ArrayList<>();
        if (!recurring()) {
            if (!departAt.isBefore(from) && !departAt.isAfter(to)) {
                out.add(departAt);
            }
            return out;
        }
        LocalDate start = from.atZone(zone).toLocalDate().minusDays(1);
        LocalDate end = to.atZone(zone).toLocalDate().plusDays(1);
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            if (!days.contains(d.getDayOfWeek())) {
                continue;
            }
            Instant t = ZonedDateTime.of(d, departTime, zone).toInstant();
            if (!t.isBefore(from) && !t.isAfter(to)) {
                out.add(t);
            }
        }
        return out;
    }

    /** The first occurrence at or after {@code after}, or null (a past one-off trip). */
    public Instant next(Instant after) {
        if (!recurring()) {
            return departAt.isBefore(after) ? null : departAt;
        }
        List<Instant> week = occurrences(after, after.plus(java.time.Duration.ofDays(8)));
        return week.isEmpty() ? null : week.getFirst();
    }

    /** The timezone of the briefing's times: the schedule's, else {@code fallback}. */
    public ZoneId zoneOr(ZoneId fallback) {
        return zone == null ? fallback : zone;
    }
}
