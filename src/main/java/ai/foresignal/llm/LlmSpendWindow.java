package ai.foresignal.llm;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

/**
 * The windows a USD spend cap covers, on UTC boundaries: the clock hour, the day (from 00:00, as the
 * daily token budget) and the calendar month (from the 1st).
 */
public enum LlmSpendWindow {
    /** The UTC clock hour. */
    HOUR("hour"),
    /** The UTC day. */
    DAY("day"),
    /** The UTC calendar month. */
    MONTH("month");

    private final String wireValue;

    LlmSpendWindow(String wireValue) {
        this.wireValue = wireValue;
    }

    /** The wire value: {@code hour}, {@code day} or {@code month}. */
    public String wireValue() {
        return wireValue;
    }

    /** The start of the window that holds {@code now}. */
    public Instant start(Instant now) {
        return switch (this) {
            case HOUR -> now.truncatedTo(ChronoUnit.HOURS);
            case DAY -> now.truncatedTo(ChronoUnit.DAYS);
            case MONTH -> {
                LocalDate day = now.atZone(ZoneOffset.UTC).toLocalDate();
                yield day.withDayOfMonth(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            }
        };
    }

    /** The end (exclusive) of the window that starts at {@code start}: when it resets. */
    public Instant end(Instant start) {
        return switch (this) {
            case HOUR -> start.plus(1, ChronoUnit.HOURS);
            case DAY -> start.plus(1, ChronoUnit.DAYS);
            case MONTH -> start.atZone(ZoneOffset.UTC).plusMonths(1).toInstant();
        };
    }
}
