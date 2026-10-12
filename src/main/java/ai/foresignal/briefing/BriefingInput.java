package ai.foresignal.briefing;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * A validated {@code BriefingInput} (prompt 029).
 *
 * @param name 1–200 characters
 * @param enabled whether the scheduler runs it
 * @param schedule the 5-field cron as written
 * @param timezone the IANA timezone (default {@code UTC})
 * @param scope the scope
 * @param lookback the lookback as written (for example {@code 24h}), or null for "since the last run"
 * @param lookbackDuration the parsed lookback (1 h–7 d), or null
 * @param mode {@code digest} or {@code llm}
 * @param instructions the LLM instructions (≤ 4000 characters), or null
 * @param channelIds the channels; empty for every channel subscribed to {@code briefing}
 * @param maxItemsPerKind notable records per kind (1–50, default 5)
 */
public record BriefingInput(String name, boolean enabled, String schedule, String timezone, BriefingScope scope,
        String lookback, Duration lookbackDuration, String mode, String instructions, List<UUID> channelIds,
        int maxItemsPerKind) {

    /** {@code mode} without an LLM. */
    public static final String DIGEST = "digest";

    /** {@code mode} with the chat provider. */
    public static final String LLM = "llm";

    /** Requires the name, schedule and scope; copies the channel list. */
    public BriefingInput {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(schedule, "schedule");
        Objects.requireNonNull(scope, "scope");
        timezone = timezone == null ? "UTC" : timezone;
        mode = mode == null ? DIGEST : mode;
        channelIds = channelIds == null ? List.of() : List.copyOf(channelIds);
    }

    /** The input stored in {@code entity}. */
    public static BriefingInput of(BriefingEntity entity) {
        Duration lookback = null;
        if (entity.getLookback() != null) {
            try {
                lookback = BriefingValidator.lookback(entity.getLookback());
            } catch (RuntimeException e) {
                lookback = null;
            }
        }
        List<UUID> channels = entity.getChannelIds().stream().map(id -> {
            try {
                return UUID.fromString(id);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }).filter(Objects::nonNull).toList();
        return new BriefingInput(entity.getName(), entity.isEnabled(), entity.getSchedule(), entity.getTimezone(),
                BriefingScope.fromMap(entity.getScope()), entity.getLookback(), lookback, entity.getMode(),
                entity.getInstructions(), channels, entity.getMaxItemsPerKind());
    }

    /** Writes this input onto {@code entity} (schedule fields only; times and ownership are the caller's). */
    public void applyTo(BriefingEntity entity) {
        entity.setName(name);
        entity.setEnabled(enabled);
        entity.setSchedule(schedule);
        entity.setTimezone(timezone);
        entity.setScope(new java.util.LinkedHashMap<>(scope.toMap()));
        entity.setLookback(lookback);
        entity.setMode(mode);
        entity.setInstructions(instructions);
        entity.setChannelIds(new java.util.ArrayList<>(channelIds.stream().map(UUID::toString).toList()));
        entity.setMaxItemsPerKind(maxItemsPerKind);
    }
}
