package ai.foresignal.llm;

import ai.foresignal.ingest.IngestStateReset;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * The character budget of the {@code tts} role (prompt 141): per provider, the characters synthesized per UTC
 * day ({@code options.ttsCharactersPerDay}, default {@value #DEFAULT_CHARACTERS_PER_DAY}). Speech is priced and
 * limited by input characters, not tokens, so it does not draw on the provider's token budget; each request still
 * takes one of the provider's {@code requestsPerMinute} through {@link LlmBudgetGovernor}.
 *
 * <p>{@link #reserve} refuses a synthesis whose characters exceed what is left today with
 * {@link LlmBudgetExceededException} (Retry-After at 00:00 UTC); {@link #settle} gives back the characters of a
 * synthesis that failed. The count is kept in memory, so a restart starts the day's count again from zero.
 */
@Component
public class TtsBudget implements IngestStateReset {

    /** The daily characters when the provider sets none. */
    public static final long DEFAULT_CHARACTERS_PER_DAY = 100_000L;

    private final Clock clock;
    private final Map<String, Day> days = new ConcurrentHashMap<>();

    /** Creates the budget. */
    public TtsBudget(Clock clock) {
        this.clock = clock;
    }

    /**
     * Reserves {@code characters} of {@code providerId}'s daily allowance {@code perDay}.
     *
     * @throws LlmBudgetExceededException when not enough characters are left today
     */
    public void reserve(String providerId, long perDay, long characters) {
        LocalDate today = today();
        days.compute(providerId, (id, day) -> {
            Day current = day == null || !day.date.equals(today) ? new Day(today) : day;
            long left = Math.max(0, perDay - current.used);
            if (characters > left) {
                throw new LlmBudgetExceededException(providerId, "Speech of " + characters + " characters exceeds the "
                        + left + " of " + perDay + " tts characters per day left for provider '" + providerId + "'.",
                        untilMidnight());
            }
            current.used += characters;
            return current;
        });
    }

    /** Gives back {@code characters} reserved today (a synthesis that failed before any audio). */
    public void settle(String providerId, long characters) {
        LocalDate today = today();
        days.computeIfPresent(providerId, (id, day) -> {
            if (day.date.equals(today)) {
                day.used = Math.max(0, day.used - characters);
            }
            return day;
        });
    }

    /** The characters counted today for {@code providerId}. */
    public long usedToday(String providerId) {
        Day day = days.get(providerId);
        return day == null || !day.date.equals(today()) ? 0 : day.used;
    }

    @Override
    public void resetState() {
        days.clear();
    }

    private LocalDate today() {
        return clock.instant().atZone(ZoneOffset.UTC).toLocalDate();
    }

    private Duration untilMidnight() {
        Instant now = clock.instant();
        return Duration.between(now, today().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());
    }

    /** One provider's day; changed only inside the map's atomic compute calls. */
    private static final class Day {
        private final LocalDate date;
        private long used;

        Day(LocalDate date) {
            this.date = date;
        }
    }
}
