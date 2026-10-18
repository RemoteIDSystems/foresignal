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
 * @param audio the audio option (prompt 141), or null for no audio
 */
public record BriefingInput(String name, boolean enabled, String schedule, String timezone, BriefingScope scope,
        String lookback, Duration lookbackDuration, String mode, String instructions, List<UUID> channelIds,
        int maxItemsPerKind, Audio audio) {

    /** An input without audio (the shape before prompt 141). */
    public BriefingInput(String name, boolean enabled, String schedule, String timezone, BriefingScope scope,
            String lookback, Duration lookbackDuration, String mode, String instructions, List<UUID> channelIds,
            int maxItemsPerKind) {
        this(name, enabled, schedule, timezone, scope, lookback, lookbackDuration, mode, instructions, channelIds,
                maxItemsPerKind, null);
    }

    /**
     * The audio option of a briefing (prompt 141, OpenAPI {@code BriefingInput.audio}): the plain-text rendition
     * is synthesized with the {@code tts} role after each run.
     *
     * @param voice the provider's voice name, for example {@code alloy} or {@code af_bella}
     * @param format {@value #MP3} (default) or {@value #OPUS}
     */
    public record Audio(String voice, String format) {

        /** MP3: long text is synthesized in chunks and concatenated. */
        public static final String MP3 = "mp3";

        /** Opus in Ogg: long text is truncated with a note. */
        public static final String OPUS = "opus";

        /** Requires the voice; the format defaults to {@value #MP3}. */
        public Audio {
            Objects.requireNonNull(voice, "voice");
            format = format == null ? MP3 : format;
        }

        /** The stored and wire form, {@code {voice, format}}. */
        public java.util.Map<String, Object> toMap() {
            java.util.Map<String, Object> out = new java.util.LinkedHashMap<>();
            out.put("voice", voice);
            out.put("format", format);
            return out;
        }

        /** The option stored as {@code map}, or null when absent or unreadable. */
        public static Audio fromMap(java.util.Map<String, Object> map) {
            if (map == null || !(map.get("voice") instanceof String voice) || voice.isBlank()) {
                return null;
            }
            return new Audio(voice, map.get("format") instanceof String f ? f : MP3);
        }
    }

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
                entity.getInstructions(), channels, entity.getMaxItemsPerKind(), Audio.fromMap(entity.getAudio()));
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
        entity.setAudio(audio == null ? null : new java.util.LinkedHashMap<>(audio.toMap()));
    }
}
