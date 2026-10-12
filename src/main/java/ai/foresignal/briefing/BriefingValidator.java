package ai.foresignal.briefing;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxParser;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.settings.Durations;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import ai.foresignal.store.TriggerRuleRepository;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * Validates a {@code BriefingInput} body (prompt 029), reporting every problem in one {@code 400}.
 *
 * <ul>
 *   <li>{@code schedule}: a standard 5-field cron, converted to Spring's 6-field
 *       {@link CronExpression} by prefixing {@code 0} seconds; it must fire, and never more often
 *       than every 15 minutes ({@link #MIN_INTERVAL}).
 *   <li>{@code timezone}: a valid {@link ZoneId} (default {@code UTC}).
 *   <li>{@code scope}: sources and kinds that exist (a source the caller may not see looks unknown),
 *       rule ids that exist, a valid bbox. Members of later prompts are refused when non-empty.
 *   <li>{@code channelIds}: UUIDs; a channel that does not exist, is disabled or is not subscribed to
 *       {@code briefing} is a warning in the response, not a failure.
 *   <li>{@code lookback}: 1 h–7 d, or null for "since the last run, or 24 h".
 *   <li>{@code mode=llm} is accepted without a chat provider; that is checked when it runs.
 * </ul>
 */
@Component
public class BriefingValidator {

    /** The most frequent schedule allowed. */
    public static final Duration MIN_INTERVAL = Duration.ofMinutes(15);

    /** The shortest lookback. */
    public static final Duration MIN_LOOKBACK = Duration.ofHours(1);

    /** The longest lookback (and the cap on "since the last run"). */
    public static final Duration MAX_LOOKBACK = Duration.ofDays(7);

    /** The longest name. */
    public static final int MAX_NAME = 200;

    /** The longest instructions. */
    public static final int MAX_INSTRUCTIONS = 4000;

    /** The most notable records per kind. */
    public static final int MAX_ITEMS_PER_KIND = 50;

    /** The default notable records per kind. */
    public static final int DEFAULT_ITEMS_PER_KIND = 5;

    /** Input members. */
    static final Set<String> MEMBERS = Set.of("name", "enabled", "schedule", "timezone", "scope", "lookback", "mode",
            "instructions", "channelIds", "maxItemsPerKind", "audio");

    /** Output-only {@code Briefing} members, accepted and ignored so a read briefing can be sent back. */
    static final Set<String> OUTPUT_ONLY = Set.of("id", "createdAt", "updatedAt", "nextRunAt", "lastRunAt",
            "lastRunStatus", "warnings");

    /** Scope members this prompt implements. */
    static final Set<String> SCOPE_MEMBERS = Set.of("sources", "kinds", "bbox", "ruleIds", "includeHealth", "placeIds",
            "viewId", "country", "admin1", "county");

    /** Scope members of later prompts: accepted only when empty. */
    static final Map<String, String> LATER_SCOPE_MEMBERS = Map.of("places", "056", "weatherProducts", "056");

    /** The window over which the frequency floor is checked. */
    private static final Duration FLOOR_WINDOW = Duration.ofDays(400);

    /** The most fire times inspected for the frequency floor. */
    private static final int FLOOR_FIRES = 2000;

    private final SourceRegistry registry;
    private final TriggerRuleRepository rules;
    private final NotificationChannelRepository channels;
    private final java.util.function.Predicate<String> placeExists;
    private final java.util.function.Predicate<String> viewExists;

    /** Validates the scope's region codes (prompt 040); null only in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.reference.RegionCodes regionCodes;

    /** Creates the validator with the place and saved-view checks of prompt 036. */
    @org.springframework.beans.factory.annotation.Autowired
    public BriefingValidator(SourceRegistry registry, TriggerRuleRepository rules,
            NotificationChannelRepository channels,
            org.springframework.beans.factory.ObjectProvider<ai.foresignal.trigger.PlaceLookup> places,
            org.springframework.beans.factory.ObjectProvider<ai.foresignal.view.SavedViewService> views) {
        this.registry = registry;
        this.rules = rules;
        this.channels = channels;
        this.placeExists = id -> {
            ai.foresignal.trigger.PlaceLookup lookup = places == null ? null : places.getIfAvailable();
            return lookup != null && lookup.geometry(id).isPresent();
        };
        this.viewExists = id -> {
            ai.foresignal.view.SavedViewService service = views == null ? null : views.getIfAvailable();
            return service != null && service.find(id).isPresent();
        };
    }

    /** Creates the validator without places or views (every place and view id is then unknown). */
    public BriefingValidator(SourceRegistry registry, TriggerRuleRepository rules,
            NotificationChannelRepository channels) {
        this(registry, rules, channels, null, null);
    }

    /**
     * A valid input and its warnings.
     *
     * @param input the input
     * @param warnings non-fatal problems (channels)
     */
    public record Validated(BriefingInput input, List<String> warnings) {}

    /**
     * Validates {@code body}.
     *
     * @param body the request body
     * @param hiddenSources sources the caller may not see; naming one is reported as unknown
     * @throws ValidationException listing every problem
     */
    public Validated validate(JsonNode body, Set<String> hiddenSources) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a BriefingInput object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String member : body.propertyNames()) {
            if (!MEMBERS.contains(member) && !OUTPUT_ONLY.contains(member)) {
                errors.add(new FieldError(member, "is not a BriefingInput member"));
            }
        }
        String name = text(body, "name", errors);
        if (name == null || name.isBlank()) {
            errors.add(new FieldError("name", "is required"));
        } else if (name.length() > MAX_NAME) {
            errors.add(new FieldError("name", "must be at most " + MAX_NAME + " characters"));
        }
        boolean enabled = bool(body, "enabled", true, errors);

        String timezone = text(body, "timezone", errors);
        timezone = timezone == null || timezone.isBlank() ? "UTC" : timezone.strip();
        try {
            zone(timezone);
        } catch (IllegalArgumentException e) {
            errors.add(new FieldError("timezone", e.getMessage()));
        }

        String schedule = text(body, "schedule", errors);
        if (schedule == null || schedule.isBlank()) {
            errors.add(new FieldError("schedule", "is required (a 5-field cron such as 0 7 * * *)"));
        } else {
            schedule = normalizeSchedule(schedule);
            try {
                checkFrequency(cron(schedule));
            } catch (IllegalArgumentException e) {
                errors.add(new FieldError("schedule", e.getMessage()));
            }
        }

        BriefingScope scope = scope(body.get("scope"), hiddenSources == null ? Set.of() : hiddenSources, errors);

        String lookbackText = null;
        Duration lookback = null;
        JsonNode lookbackNode = body.get("lookback");
        if (lookbackNode != null && !lookbackNode.isNull()) {
            if (!lookbackNode.isString()) {
                errors.add(new FieldError("lookback", "must be a duration such as 24h or P1D"));
            } else {
                try {
                    lookbackText = lookbackNode.asString().strip();
                    lookback = lookback(lookbackText);
                } catch (IllegalArgumentException e) {
                    errors.add(new FieldError("lookback", e.getMessage()));
                }
            }
        }

        String mode = text(body, "mode", errors);
        mode = mode == null ? BriefingInput.DIGEST : mode;
        if (!BriefingInput.DIGEST.equals(mode) && !BriefingInput.LLM.equals(mode)) {
            errors.add(new FieldError("mode", "must be digest or llm"));
        }
        String instructions = text(body, "instructions", errors);
        if (instructions != null && instructions.length() > MAX_INSTRUCTIONS) {
            errors.add(new FieldError("instructions", "must be at most " + MAX_INSTRUCTIONS + " characters"));
        }
        if (instructions != null && instructions.isBlank()) {
            instructions = null;
        }

        List<UUID> channelIds = new ArrayList<>();
        JsonNode channelsNode = body.get("channelIds");
        if (channelsNode != null && !channelsNode.isNull()) {
            if (!channelsNode.isArray()) {
                errors.add(new FieldError("channelIds", "must be an array of channel ids"));
            } else {
                Set<UUID> seen = new LinkedHashSet<>();
                for (int i = 0; i < channelsNode.size(); i++) {
                    JsonNode id = channelsNode.get(i);
                    UUID parsed = uuid(id);
                    if (parsed == null) {
                        errors.add(new FieldError("channelIds[" + i + "]", "must be a channel id (UUID)"));
                    } else {
                        seen.add(parsed);
                    }
                }
                channelIds.addAll(seen);
            }
        }

        int maxItems = DEFAULT_ITEMS_PER_KIND;
        JsonNode maxNode = body.get("maxItemsPerKind");
        if (maxNode != null && !maxNode.isNull()) {
            if (!maxNode.isIntegralNumber() || maxNode.asLong() < 1 || maxNode.asLong() > MAX_ITEMS_PER_KIND) {
                errors.add(new FieldError("maxItemsPerKind", "must be an integer from 1 to " + MAX_ITEMS_PER_KIND));
            } else {
                maxItems = maxNode.asInt();
            }
        }

        JsonNode audio = body.get("audio");
        if (audio != null && !audio.isNull()) {
            errors.add(new FieldError("audio", "audio briefings are not available until prompt 141; use null"));
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("The briefing is not valid.", errors);
        }
        BriefingInput input = new BriefingInput(name.strip(), enabled, schedule, timezone, scope, lookbackText, lookback,
                mode, instructions, channelIds, maxItems);
        return new Validated(input, channelWarnings(channelIds));
    }

    /**
     * The warnings for {@code channelIds}: a channel that does not exist, is disabled, or is not
     * subscribed to {@code briefing}; with none listed, a warning when no channel takes briefings.
     */
    public List<String> channelWarnings(List<UUID> channelIds) {
        List<String> warnings = new ArrayList<>();
        if (channelIds.isEmpty()) {
            boolean any = channels.findByEnabledTrue().stream()
                    .anyMatch(c -> c.getEvents().contains(NotificationMessage.BRIEFING));
            if (!any) {
                warnings.add("no enabled channel is subscribed to briefing; runs are stored but not delivered");
            }
            return warnings;
        }
        for (UUID id : channelIds) {
            Optional<NotificationChannelEntity> channel = channels.findById(id);
            if (channel.isEmpty()) {
                warnings.add("channel " + id + " does not exist; runs will not be delivered to it");
            } else if (!channel.get().getEvents().contains(NotificationMessage.BRIEFING)) {
                warnings.add("channel " + id + " (" + channel.get().getName()
                        + ") is not subscribed to briefing; runs will not be delivered to it");
            } else if (!channel.get().isEnabled()) {
                warnings.add("channel " + id + " (" + channel.get().getName() + ") is disabled");
            }
        }
        return warnings;
    }

    private BriefingScope scope(JsonNode node, Set<String> hidden, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return BriefingScope.all();
        }
        if (!node.isObject()) {
            errors.add(new FieldError("scope", "must be a BriefingScope object"));
            return BriefingScope.all();
        }
        for (String member : node.propertyNames()) {
            if (SCOPE_MEMBERS.contains(member)) {
                continue;
            }
            String prompt = LATER_SCOPE_MEMBERS.get(member);
            if (prompt == null) {
                errors.add(new FieldError("scope." + member, "is not a BriefingScope member"));
            } else if (!empty(node.get(member))) {
                errors.add(new FieldError("scope." + member, "is not supported until prompt " + prompt));
            }
        }
        List<String> sources = strings(node, "sources", errors);
        for (String source : sources) {
            if (!registry.contains(source) || hidden.contains(source)) {
                errors.add(new FieldError("scope.sources", "unknown source: " + source));
            }
        }
        List<String> kinds = strings(node, "kinds", errors);
        Set<String> knownKinds = registry.kinds();
        for (String kind : kinds) {
            if (!knownKinds.contains(kind)) {
                errors.add(new FieldError("scope.kinds", "unknown kind: " + kind));
            }
        }
        String bbox = null;
        JsonNode bboxNode = node.get("bbox");
        if (bboxNode != null && !bboxNode.isNull()) {
            if (!bboxNode.isString()) {
                errors.add(new FieldError("scope.bbox", "must be minLon,minLat,maxLon,maxLat"));
            } else {
                try {
                    BboxParser.parse(bboxNode.asString());
                    bbox = bboxNode.asString().isBlank() ? null : bboxNode.asString().strip();
                } catch (ValidationException e) {
                    errors.add(new FieldError("scope.bbox", e.getErrors().isEmpty() ? e.getMessage()
                            : e.getErrors().getFirst().message()));
                }
            }
        }
        List<UUID> ruleIds = new ArrayList<>();
        int i = 0;
        for (String raw : strings(node, "ruleIds", errors)) {
            UUID id = null;
            try {
                id = UUID.fromString(raw);
            } catch (IllegalArgumentException e) {
                errors.add(new FieldError("scope.ruleIds[" + i + "]", "must be a rule id (UUID)"));
            }
            if (id != null) {
                if (!rules.existsById(id)) {
                    errors.add(new FieldError("scope.ruleIds[" + i + "]", "unknown rule: " + id));
                } else if (!ruleIds.contains(id)) {
                    ruleIds.add(id);
                }
            }
            i++;
        }
        boolean includeHealth = bool(node, "includeHealth", true, errors, "scope.");
        List<String> placeIds = strings(node, "placeIds", errors);
        for (String placeId : placeIds) {
            if (!placeExists.test(placeId)) {
                errors.add(new FieldError("scope.placeIds", "unknown place: " + placeId));
            }
        }
        String viewId = null;
        JsonNode viewNode = node.get("viewId");
        if (viewNode != null && !viewNode.isNull()) {
            if (!viewNode.isString() || viewNode.asString().isBlank()) {
                errors.add(new FieldError("scope.viewId", "must be a saved view id or slug"));
            } else if (!viewExists.test(viewNode.asString().strip())) {
                errors.add(new FieldError("scope.viewId", "unknown view: " + viewNode.asString().strip()));
            } else {
                viewId = viewNode.asString().strip();
            }
        }
        // Region filters (prompt 040): unknown codes are UNKNOWN_REGION.
        List<String> country = upper(strings(node, "country", errors));
        List<String> admin1 = upper(strings(node, "admin1", errors));
        List<String> county = strings(node, "county", errors);
        if (regionCodes != null && errors.isEmpty()) {
            regionCodes.validate(dedupe(country), dedupe(admin1), dedupe(county), "scope.");
        }
        return new BriefingScope(dedupe(sources), dedupe(kinds), bbox, ruleIds, includeHealth, dedupe(placeIds), viewId,
                dedupe(country), dedupe(admin1), dedupe(county));
    }

    private static List<String> upper(List<String> values) {
        return values.stream().map(v -> v.toUpperCase(java.util.Locale.ROOT)).toList();
    }

    // ------------------------------------------------------------------------- schedule helpers

    /** {@code schedule} with runs of whitespace collapsed to one space. */
    public static String normalizeSchedule(String schedule) {
        return String.join(" ", schedule.strip().split("\\s+"));
    }

    /**
     * The Spring expression of a standard 5-field cron ({@code minute hour day-of-month month
     * day-of-week}): {@code 0} seconds prefixed.
     *
     * @throws IllegalArgumentException when it is not a valid 5-field cron
     */
    public static CronExpression cron(String schedule) {
        if (schedule == null || schedule.isBlank()) {
            throw new IllegalArgumentException("must be a 5-field cron (minute hour day-of-month month day-of-week)");
        }
        String[] fields = schedule.strip().split("\\s+");
        if (fields.length != 5) {
            throw new IllegalArgumentException("must be a 5-field cron (minute hour day-of-month month day-of-week), got "
                    + fields.length + " fields");
        }
        for (String field : fields) {
            if (field.startsWith("@")) {
                throw new IllegalArgumentException("macros such as @daily are not supported; write the 5 fields");
            }
        }
        try {
            return CronExpression.parse("0 " + String.join(" ", fields));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("is not a valid cron: " + e.getMessage());
        }
    }

    /**
     * The shortest interval between consecutive fire times of {@code cron} (in UTC, so a DST shift does
     * not count), looked at over 400 days or 2000 fire times; null when it fires at most once.
     *
     * @throws IllegalArgumentException when it never fires
     */
    public static Duration minimumInterval(CronExpression cron) {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0).minusSeconds(1);
        LocalDateTime end = start.plus(FLOOR_WINDOW);
        LocalDateTime previous = cron.next(start);
        if (previous == null) {
            throw new IllegalArgumentException("never fires");
        }
        Duration min = null;
        for (int i = 0; i < FLOOR_FIRES; i++) {
            LocalDateTime next = cron.next(previous);
            if (next == null || next.isAfter(end)) {
                break;
            }
            Duration gap = Duration.between(previous, next);
            if (min == null || gap.compareTo(min) < 0) {
                min = gap;
            }
            previous = next;
        }
        return min;
    }

    /**
     * Refuses a schedule more frequent than every 15 minutes.
     *
     * @throws IllegalArgumentException when it is, or never fires
     */
    public static void checkFrequency(CronExpression cron) {
        Duration min = minimumInterval(cron);
        if (min != null && min.compareTo(MIN_INTERVAL) < 0) {
            long seconds = min.toSeconds();
            String every = seconds % 60 == 0 ? (seconds / 60) + " minutes" : seconds + " seconds";
            throw new IllegalArgumentException("fires as often as every " + every
                    + "; briefings may run at most every 15 minutes");
        }
    }

    /**
     * The first fire time of {@code schedule} in {@code timezone} strictly after {@code after}, or null
     * when it never fires again.
     */
    public static Instant nextRun(String schedule, String timezone, Instant after) {
        ZonedDateTime next = cron(schedule).next(ZonedDateTime.ofInstant(after, zone(timezone)));
        return next == null ? null : next.toInstant();
    }

    /**
     * The zone of {@code timezone}.
     *
     * @throws IllegalArgumentException when it is not a valid zone id
     */
    public static ZoneId zone(String timezone) {
        try {
            return ZoneId.of(timezone == null || timezone.isBlank() ? "UTC" : timezone.strip());
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("must be an IANA timezone such as America/Chicago");
        }
    }

    /**
     * A lookback: an ISO-8601 duration or shorthand ({@code 24h}, {@code 7d}) from 1 h to 7 d.
     *
     * @throws IllegalArgumentException when it is not one
     */
    public static Duration lookback(String text) {
        Duration value;
        try {
            value = Durations.parse(text);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("must be a duration such as 24h or P1D");
        }
        if (value.compareTo(MIN_LOOKBACK) < 0 || value.compareTo(MAX_LOOKBACK) > 0) {
            throw new IllegalArgumentException("must be from 1h to 7d");
        }
        return value;
    }

    // ------------------------------------------------------------------------- JSON helpers

    private static String text(JsonNode body, String field, List<FieldError> errors) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isString()) {
            errors.add(new FieldError(field, "must be a string"));
            return null;
        }
        return node.asString();
    }

    private static boolean bool(JsonNode body, String field, boolean defaultValue, List<FieldError> errors) {
        return bool(body, field, defaultValue, errors, "");
    }

    private static boolean bool(JsonNode body, String field, boolean defaultValue, List<FieldError> errors,
            String prefix) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        if (!node.isBoolean()) {
            errors.add(new FieldError(prefix + field, "must be true or false"));
            return defaultValue;
        }
        return node.asBoolean();
    }

    private static List<String> strings(JsonNode body, String field, List<FieldError> errors) {
        JsonNode node = body.get(field);
        List<String> out = new ArrayList<>();
        if (node == null || node.isNull()) {
            return out;
        }
        if (!node.isArray()) {
            errors.add(new FieldError("scope." + field, "must be an array of strings"));
            return out;
        }
        for (JsonNode item : node) {
            if (!item.isString() || item.asString().isBlank()) {
                errors.add(new FieldError("scope." + field, "must be an array of non-empty strings"));
            } else {
                out.add(item.asString().strip());
            }
        }
        return out;
    }

    private static boolean empty(JsonNode node) {
        return node == null || node.isNull() || (node.isArray() && node.isEmpty())
                || (node.isString() && node.asString().isBlank());
    }

    private static UUID uuid(JsonNode node) {
        if (node == null || !node.isString()) {
            return null;
        }
        try {
            return UUID.fromString(node.asString());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static List<String> dedupe(List<String> values) {
        return List.copyOf(new LinkedHashSet<>(values));
    }
}
