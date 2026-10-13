package ai.foresignal.mapping;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * A time value (OpenAPI {@code MappingTimeSpec}, prompt 059): read from a path in a format, or the receive time.
 */
public sealed interface TimeSpec {

    /** The time Foresignal received the request: {@code {received: true}}. */
    record Received() implements TimeSpec {}

    /**
     * A value read and parsed.
     *
     * @param value where the value is read
     * @param format {@code iso}, {@code epochS}, {@code epochMs} or {@code pattern}
     * @param pattern the formatter of a {@code pattern:} format, else null
     * @param zone the zone of zone-less values (default UTC)
     */
    record FromValue(ValueSpec value, String format, DateTimeFormatter pattern, ZoneId zone) implements TimeSpec {}
}
