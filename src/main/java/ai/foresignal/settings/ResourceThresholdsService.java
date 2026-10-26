package ai.foresignal.settings;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

/**
 * Reads, validates and stores {@link ResourceThresholds} ({@code GET/PUT /v1/settings/thresholds}, prompt R2-014).
 * A PUT replaces the whole value and every problem is reported in one 400.
 *
 * <p>In a PUT body a missing member takes its default ({@link ResourceThresholds#defaults()}): disk at 15 % / 5 %
 * free, memory off, a storm at 5 failing sources, a backlog of 10,000 seq for 5 minutes. An explicit {@code null}
 * turns that member off. Inside a threshold, a missing or null level is off.
 */
@Service
public class ResourceThresholdsService {

    static final Set<String> FIELDS = Set.of("disk", "memory", "alertChannels", "sourceStorm", "collectorBacklog");
    static final Set<String> THRESHOLD_FIELDS =
            Set.of("warnFreePercent", "warnFreeBytes", "criticalFreePercent", "criticalFreeBytes");
    static final Set<String> STORM_FIELDS = Set.of("failedSources");
    static final Set<String> BACKLOG_FIELDS = Set.of("triggerLagSeq", "waitingPulls", "sustainedSeconds");

    /** The shortest sustained window a backlog may have. */
    static final int MIN_SUSTAINED_SECONDS = 30;

    /** The longest sustained window a backlog may have (one day). */
    static final int MAX_SUSTAINED_SECONDS = 86_400;

    private final AppSettingService settings;
    private final NotificationChannelRepository channels;

    /**
     * Creates the service.
     *
     * @param settings the settings store
     * @param channels the channels, to check {@code alertChannels}
     */
    public ResourceThresholdsService(AppSettingService settings, NotificationChannelRepository channels) {
        this.settings = settings;
        this.channels = channels;
    }

    /** The stored thresholds, or the defaults. */
    public ResourceThresholds get() {
        return settings.getOrDefault(SettingKeys.THRESHOLDS, ResourceThresholds.class, ResourceThresholds::defaults);
    }

    /**
     * Stores {@code value} (already validated by {@link #parse}) after checking that every alert channel exists.
     *
     * @param value the thresholds
     * @return the stored thresholds
     * @throws ValidationException one 400 naming every unknown channel id
     */
    public ResourceThresholds put(ResourceThresholds value) {
        List<UUID> ids = value.alertChannels().stream().map(UUID::fromString).toList();
        Set<UUID> known = new HashSet<>();
        for (NotificationChannelEntity channel : channels.findAllById(ids)) {
            known.add(channel.getId());
        }
        List<String> unknown = ids.stream().filter(id -> !known.contains(id)).map(UUID::toString).toList();
        if (!unknown.isEmpty()) {
            throw new ValidationException("The thresholds name channels that do not exist.",
                    List.of(new FieldError("alertChannels", "unknown channel ids: " + String.join(", ", unknown))));
        }
        settings.put(SettingKeys.THRESHOLDS, value);
        return value;
    }

    /**
     * Parses a {@code ResourceThresholds} body.
     *
     * @param body the request body
     * @return the thresholds
     * @throws ValidationException listing every problem
     */
    public static ResourceThresholds parse(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a ResourceThresholds object.");
        }
        List<FieldError> errors = new ArrayList<>();
        unknown(body, FIELDS, "", errors);
        ResourceThresholds defaults = ResourceThresholds.defaults();
        ResourceThresholds.Threshold disk = threshold(body, "disk", defaults.disk(), errors);
        ResourceThresholds.Threshold memory = threshold(body, "memory", defaults.memory(), errors);
        List<String> alertChannels = alertChannels(body.get("alertChannels"), errors);
        ResourceThresholds.SourceStorm storm = defaults.sourceStorm();
        if (body.has("sourceStorm")) {
            JsonNode node = body.get("sourceStorm");
            if (node.isNull()) {
                storm = new ResourceThresholds.SourceStorm(null);
            } else if (!node.isObject()) {
                errors.add(new FieldError("sourceStorm", "must be an object or null"));
            } else {
                unknown(node, STORM_FIELDS, "sourceStorm.", errors);
                Long failed = whole(node.get("failedSources"), "sourceStorm.failedSources", 1, errors);
                storm = new ResourceThresholds.SourceStorm(failed == null ? null : Math.toIntExact(
                        Math.min(failed, Integer.MAX_VALUE)));
            }
        }
        ResourceThresholds.CollectorBacklog backlog = defaults.collectorBacklog();
        if (body.has("collectorBacklog")) {
            JsonNode node = body.get("collectorBacklog");
            if (node.isNull()) {
                backlog = new ResourceThresholds.CollectorBacklog(null, null, null);
            } else if (!node.isObject()) {
                errors.add(new FieldError("collectorBacklog", "must be an object or null"));
            } else {
                unknown(node, BACKLOG_FIELDS, "collectorBacklog.", errors);
                Long lag = whole(node.get("triggerLagSeq"), "collectorBacklog.triggerLagSeq", 1, errors);
                Long waiting = whole(node.get("waitingPulls"), "collectorBacklog.waitingPulls", 1, errors);
                Long sustained = whole(node.get("sustainedSeconds"), "collectorBacklog.sustainedSeconds",
                        MIN_SUSTAINED_SECONDS, errors);
                if (sustained != null && sustained > MAX_SUSTAINED_SECONDS) {
                    errors.add(new FieldError("collectorBacklog.sustainedSeconds", "must be at most 86400"));
                }
                backlog = new ResourceThresholds.CollectorBacklog(lag,
                        waiting == null ? null : Math.toIntExact(Math.min(waiting, Integer.MAX_VALUE)),
                        sustained == null ? null : Math.toIntExact(Math.min(sustained, MAX_SUSTAINED_SECONDS)));
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The resource thresholds are not valid.", errors);
        }
        return new ResourceThresholds(disk, memory, alertChannels, storm, backlog);
    }

    private static ResourceThresholds.Threshold threshold(JsonNode body, String field,
            ResourceThresholds.Threshold fallback, List<FieldError> errors) {
        if (!body.has(field)) {
            return fallback;
        }
        JsonNode node = body.get(field);
        if (node.isNull()) {
            return ResourceThresholds.Threshold.OFF;
        }
        if (!node.isObject()) {
            errors.add(new FieldError(field, "must be an object or null"));
            return fallback;
        }
        unknown(node, THRESHOLD_FIELDS, field + ".", errors);
        Double warnPercent = percent(node.get("warnFreePercent"), field + ".warnFreePercent", errors);
        Double criticalPercent = percent(node.get("criticalFreePercent"), field + ".criticalFreePercent", errors);
        Long warnBytes = whole(node.get("warnFreeBytes"), field + ".warnFreeBytes", 0, errors);
        Long criticalBytes = whole(node.get("criticalFreeBytes"), field + ".criticalFreeBytes", 0, errors);
        if (warnPercent != null && criticalPercent != null && criticalPercent >= warnPercent) {
            errors.add(new FieldError(field + ".criticalFreePercent",
                    "must be below warnFreePercent (critical is the more severe level)"));
        }
        if (warnBytes != null && criticalBytes != null && criticalBytes >= warnBytes) {
            errors.add(new FieldError(field + ".criticalFreeBytes",
                    "must be below warnFreeBytes (critical is the more severe level)"));
        }
        return new ResourceThresholds.Threshold(warnPercent, warnBytes, criticalPercent, criticalBytes);
    }

    private static List<String> alertChannels(JsonNode node, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return List.of();
        }
        if (!node.isArray()) {
            errors.add(new FieldError("alertChannels", "must be an array of channel ids"));
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (JsonNode item : node) {
            String value = item.isString() ? item.asString().strip() : null;
            try {
                String id = UUID.fromString(value == null ? "" : value).toString();
                if (!out.contains(id)) {
                    out.add(id);
                }
            } catch (IllegalArgumentException e) {
                errors.add(new FieldError("alertChannels", "must hold channel ids (UUIDs); got " + item));
            }
        }
        return out;
    }

    private static Double percent(JsonNode node, String field, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isNumber() || node.asDouble() < 0 || node.asDouble() > 100 || Double.isNaN(node.asDouble())) {
            errors.add(new FieldError(field, "must be a number from 0 to 100 or null"));
            return null;
        }
        return node.asDouble();
    }

    private static Long whole(JsonNode node, String field, long min, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isIntegralNumber() || !node.canConvertToLong() || node.asLong() < min) {
            errors.add(new FieldError(field, "must be a whole number of at least " + min + " or null"));
            return null;
        }
        return node.asLong();
    }

    private static void unknown(JsonNode node, Set<String> allowed, String prefix, List<FieldError> errors) {
        for (String member : node.propertyNames()) {
            if (!allowed.contains(member)) {
                errors.add(new FieldError(prefix + member, "is not a member of this object"));
            }
        }
    }
}
