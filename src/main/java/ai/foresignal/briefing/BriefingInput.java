package ai.foresignal.briefing;

import java.time.Duration;
import java.util.List;
import java.util.Map;
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
 * @param edition the Automated Newspaper options (NP-003 onward); {@link Edition#NONE} for none
 */
public record BriefingInput(String name, boolean enabled, String schedule, String timezone, BriefingScope scope,
        String lookback, Duration lookbackDuration, String mode, String instructions, List<UUID> channelIds,
        int maxItemsPerKind, Audio audio, Edition edition) {

    /** An input without edition options (the shape before NP-003). */
    public BriefingInput(String name, boolean enabled, String schedule, String timezone, BriefingScope scope,
            String lookback, Duration lookbackDuration, String mode, String instructions, List<UUID> channelIds,
            int maxItemsPerKind, Audio audio) {
        this(name, enabled, schedule, timezone, scope, lookback, lookbackDuration, mode, instructions, channelIds,
                maxItemsPerKind, audio, Edition.NONE);
    }

    /**
     * The Automated Newspaper options of a briefing (OpenAPI {@code BriefingInput}, authored by NP-001).
     *
     * @param outputLanguage the canonical BCP 47 language the edition is written and spoken in (NP-003), or null
     *     for the owner's {@code UserPreferences.locale}
     * @param deliveryTargets where each run goes (NP-004); empty to deliver as before ({@code channelIds}, else every
     *     channel subscribed to {@code briefing})
     * @param site the hosted site (NP-005); {@link Site#OFF} for none
     * @param breakingNews the breaking-news configuration (NP-006), or null for none
     * @param includeProvenance each run's citations carry why the item is in the edition (NP-007)
     * @param includeCorroboration each run's citations carry how well-supported the item is (NP-007)
     */
    public record Edition(String outputLanguage, List<DeliveryTarget> deliveryTargets, Site site,
            BreakingNews breakingNews, boolean includeProvenance, boolean includeCorroboration) {

        /** No edition options. */
        public static final Edition NONE = new Edition(null);

        /** Copies the targets; a null site is off. */
        public Edition {
            deliveryTargets = deliveryTargets == null ? List.of() : List.copyOf(deliveryTargets);
            site = site == null ? Site.OFF : site;
        }

        /** Options with a language and no targets (the shape of NP-003). */
        public Edition(String outputLanguage) {
            this(outputLanguage, List.of());
        }

        /** Options without a site (the shape of NP-004). */
        public Edition(String outputLanguage, List<DeliveryTarget> deliveryTargets) {
            this(outputLanguage, deliveryTargets, Site.OFF);
        }

        /** Options without breaking news (the shape of NP-005). */
        public Edition(String outputLanguage, List<DeliveryTarget> deliveryTargets, Site site) {
            this(outputLanguage, deliveryTargets, site, null);
        }

        /** Options without provenance or corroboration (the shape of NP-006). */
        public Edition(String outputLanguage, List<DeliveryTarget> deliveryTargets, Site site,
                BreakingNews breakingNews) {
            this(outputLanguage, deliveryTargets, site, breakingNews, false, false);
        }

        /** The targets stored as {@code maps}, the unreadable ones left out. */
        static List<DeliveryTarget> targets(List<Map<String, Object>> maps) {
            if (maps == null) {
                return List.of();
            }
            return maps.stream().map(DeliveryTarget::fromMap).filter(Objects::nonNull).toList();
        }
    }

    /**
     * The hosted edition site (NP-005, OpenAPI {@code BriefingInput.site}).
     *
     * @param enabled serve the latest successful run at {@code getEditionSite}
     * @param shareable keep a read-only, revocable share link to the site (effective only while enabled)
     */
    public record Site(boolean enabled, boolean shareable) {

        /** No site. */
        public static final Site OFF = new Site(false, false);

        /** The wire form. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new java.util.LinkedHashMap<>();
            out.put("enabled", enabled);
            out.put("shareable", shareable);
            return out;
        }
    }

    /**
     * Breaking-news special editions (NP-006, OpenAPI {@code BreakingNewsConfig}): an alert of one of {@code ruleIds}
     * (or, when empty, of the scope's {@code ruleIds}) at or above {@code minSeverity} runs the edition at once, at
     * most once per {@code cooldownMinutes}.
     *
     * @param enabled whether alerts fire breaking runs
     * @param ruleIds the trigger rules whose alerts fire one; empty for the scope's rules
     * @param minSeverity the least severity, {@code info}, {@code warning} or {@code critical}
     * @param cooldownMinutes the least time between two breaking runs (1–10080)
     */
    public record BreakingNews(boolean enabled, List<UUID> ruleIds, String minSeverity, int cooldownMinutes) {

        /** The default cooldown. */
        public static final int DEFAULT_COOLDOWN = 60;

        /** The longest cooldown (7 days). */
        public static final int MAX_COOLDOWN = 10_080;

        /** Copies the rules; the severity defaults to {@code info} (any alert). */
        public BreakingNews {
            ruleIds = ruleIds == null ? List.of() : List.copyOf(ruleIds);
            minSeverity = minSeverity == null ? "info" : minSeverity;
        }

        /** The stored and wire form. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new java.util.LinkedHashMap<>();
            out.put("enabled", enabled);
            out.put("ruleIds", new java.util.ArrayList<>(ruleIds.stream().map(UUID::toString).toList()));
            out.put("minSeverity", minSeverity);
            out.put("cooldownMinutes", cooldownMinutes);
            return out;
        }

        /** The configuration stored as {@code map}, or null when absent or unreadable. */
        public static BreakingNews fromMap(Map<String, Object> map) {
            if (map == null) {
                return null;
            }
            List<UUID> rules = new java.util.ArrayList<>();
            if (map.get("ruleIds") instanceof List<?> list) {
                for (Object o : list) {
                    try {
                        rules.add(UUID.fromString(String.valueOf(o)));
                    } catch (IllegalArgumentException e) {
                        // An unreadable id is left out.
                    }
                }
            }
            int cooldown = map.get("cooldownMinutes") instanceof Number n ? n.intValue() : DEFAULT_COOLDOWN;
            return new BreakingNews(Boolean.TRUE.equals(map.get("enabled")), rules,
                    map.get("minSeverity") instanceof String s ? s : null, cooldown);
        }
    }

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
        edition = edition == null ? Edition.NONE : edition;
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
                entity.getInstructions(), channels, entity.getMaxItemsPerKind(), Audio.fromMap(entity.getAudio()),
                new Edition(entity.getOutputLanguage(), Edition.targets(entity.getDeliveryTargets()),
                        new Site(entity.isSiteEnabled(), entity.isSiteShareable()),
                        BreakingNews.fromMap(entity.getBreakingNews()), entity.isIncludeProvenance(),
                        entity.isIncludeCorroboration()));
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
        entity.setOutputLanguage(edition.outputLanguage());
        entity.setBreakingNews(edition.breakingNews() == null ? null
                : new java.util.LinkedHashMap<>(edition.breakingNews().toMap()));
        entity.setIncludeProvenance(edition.includeProvenance());
        entity.setIncludeCorroboration(edition.includeCorroboration());
        entity.setSiteEnabled(edition.site().enabled());
        entity.setSiteShareable(edition.site().shareable());
        entity.setDeliveryTargets(edition.deliveryTargets().isEmpty() ? null
                : new java.util.ArrayList<>(edition.deliveryTargets().stream().map(DeliveryTarget::toMap).toList()));
    }
}
