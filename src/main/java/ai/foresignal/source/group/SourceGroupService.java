package ai.foresignal.source.group;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.inbound.CustomSourceEntity;
import ai.foresignal.inbound.CustomSourceRepository;
import ai.foresignal.settings.Durations;
import ai.foresignal.source.SourceRegistry;
import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/**
 * Create, replace, get, list and delete source groups (prompt SG-002), all keyed on the owning user.
 *
 * <p>Each member source must be a known <strong>catalog</strong> source ({@link SourceRegistry#contains}) or a
 * <strong>custom</strong> source the caller owns (SG-001's store); any other id — unknown, or a custom source owned by
 * someone else or by the operator — is {@code 400}, mirroring the trigger validator's unknown-source rule. The group
 * id is server-generated; {@code ownerUserId} is forced to the caller; a group the caller does not own (or a bad id)
 * is {@code 404}.
 */
@Service
public class SourceGroupService {

    /** The default page size for {@code listMySourceGroups}. */
    public static final int DEFAULT_LIMIT = 50;

    /** The largest page size. */
    public static final int MAX_LIMIT = 200;

    private static final List<String> CORRELATION_LEVELS = List.of("conservative", "balanced", "aggressive");
    private static final List<String> ANALYSIS_STYLES = List.of("objective", "analytical", "in-depth");
    private static final List<String> STORY_LENGTHS = List.of("short", "medium", "long");
    private static final List<String> SEVERITIES = List.of("info", "warning", "critical");

    private final SourceGroupRepository repository;
    private final CustomSourceRepository customSources;
    private final SourceRegistry registry;
    private final Clock clock;

    /** Creates the service. */
    public SourceGroupService(SourceGroupRepository repository, CustomSourceRepository customSources,
            SourceRegistry registry, Clock clock) {
        this.repository = repository;
        this.customSources = customSources;
        this.registry = registry;
        this.clock = clock;
    }

    /** A page of a user's groups. */
    public record Page(List<SourceGroupEntity> items, String nextCursor) {}

    // ---------- reads ----------

    /**
     * A page of {@code owner}'s groups, oldest first, after {@code cursor} (a group id), at most {@code limit}.
     */
    @Transactional(readOnly = true)
    public Page list(UUID owner, String cursor, int limit) {
        List<SourceGroupEntity> all = repository.findByOwnerUserIdOrderByCreatedAtAscIdAsc(owner);
        int from = 0;
        if (cursor != null && !cursor.isBlank()) {
            for (int i = 0; i < all.size(); i++) {
                if (all.get(i).getId().equals(cursor)) {
                    from = i + 1;
                    break;
                }
            }
        }
        List<SourceGroupEntity> page = new ArrayList<>();
        String next = null;
        for (int i = from; i < all.size() && page.size() < limit; i++) {
            page.add(all.get(i));
            if (page.size() == limit && i + 1 < all.size()) {
                next = all.get(i).getId();
            }
        }
        return new Page(page, next);
    }

    /**
     * One group the caller owns.
     *
     * @throws NotFoundException when {@code owner} does not own a group with this id
     */
    @Transactional(readOnly = true)
    public SourceGroupEntity get(String id, UUID owner) {
        return repository.findByIdAndOwnerUserId(id, owner)
                .orElseThrow(() -> NotFoundException.of("Source group", id));
    }

    // ---------- writes ----------

    /**
     * Creates a group owned by {@code owner} from a {@code SourceGroupInput} body.
     *
     * @throws ValidationException on invalid input or an unknown/foreign member source
     */
    @Transactional
    public SourceGroupEntity create(JsonNode body, UUID owner) {
        SourceGroupEntity group = new SourceGroupEntity("sg-" + UUID.randomUUID(), owner);
        apply(group, body, owner);
        group.setCreatedAt(clock.instant());
        group.setUpdatedAt(group.getCreatedAt());
        return repository.saveAndFlush(group);
    }

    /**
     * Replaces a group the caller owns whole; keeps its id, owner, {@code editionId} and {@code createdAt}.
     *
     * @throws NotFoundException when {@code owner} does not own a group with this id
     * @throws ValidationException on invalid input or an unknown/foreign member source
     */
    @Transactional
    public SourceGroupEntity update(String id, JsonNode body, UUID owner) {
        SourceGroupEntity group = get(id, owner);
        apply(group, body, owner);
        group.setUpdatedAt(clock.instant());
        return repository.saveAndFlush(group);
    }

    /**
     * Deletes a group the caller owns.
     *
     * @throws NotFoundException when {@code owner} does not own a group with this id
     */
    @Transactional
    public void delete(String id, UUID owner) {
        repository.delete(get(id, owner));
    }

    // ---------- validation / mapping ----------

    private void apply(SourceGroupEntity group, JsonNode body, UUID owner) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The body must be a SourceGroupInput object.");
        }
        List<FieldError> errors = new ArrayList<>();
        group.setName(requiredString(body, "name", errors));
        JsonNode description = body.get("description");
        group.setDescription(description == null || description.isNull() ? null : description.asString());
        group.setSourceIds(sourceIds(body, owner, errors));
        group.setKinds(strings(body, "kinds"));
        group.setCorrelation(correlation(body.get("correlation"), errors));
        group.setNotificationThreshold(threshold(body.get("notificationThreshold"), errors));
        if (!errors.isEmpty()) {
            throw new ValidationException("The source group is not valid.", errors);
        }
    }

    private List<String> sourceIds(JsonNode body, UUID owner, List<FieldError> errors) {
        JsonNode node = body.get("sourceIds");
        if (node == null || !node.isArray() || node.isEmpty()) {
            errors.add(new FieldError("sourceIds", "must be a non-empty array of source ids"));
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (int i = 0; i < node.size(); i++) {
            JsonNode item = node.get(i);
            if (!item.isString() || item.asString().isBlank()) {
                errors.add(new FieldError("sourceIds[" + i + "]", "must be a non-empty string"));
                continue;
            }
            String id = item.asString().strip();
            if (!known(id, owner)) {
                errors.add(new FieldError("sourceIds[" + i + "]", "unknown source '" + id + "'"));
            } else if (!out.contains(id)) {
                out.add(id);
            }
        }
        return out;
    }

    /** A member source is valid if it is the caller's own custom source, or a catalog source in the registry. */
    private boolean known(String id, UUID owner) {
        Optional<CustomSourceEntity> custom = customSources.findById(id);
        if (custom.isPresent()) {
            return owner.equals(custom.get().getOwnerUserId());
        }
        return registry.contains(id);
    }

    private Map<String, Object> correlation(JsonNode node, List<FieldError> errors) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("correlationLevel", enumValue(node, "correlation.correlationLevel", "correlationLevel",
                CORRELATION_LEVELS, "balanced", errors));
        out.put("analysisStyle", enumValue(node, "correlation.analysisStyle", "analysisStyle", ANALYSIS_STYLES,
                "analytical", errors));
        out.put("storyLength", enumValue(node, "correlation.storyLength", "storyLength", STORY_LENGTHS, "medium",
                errors));
        return out;
    }

    private Map<String, Object> threshold(JsonNode node, List<FieldError> errors) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("minSeverity", enumValue(node, "notificationThreshold.minSeverity", "minSeverity", SEVERITIES,
                "warning", errors));
        JsonNode mute = node == null ? null : node.get("mute");
        out.put("mute", mute != null && mute.isBoolean() && mute.booleanValue());
        String cooldown = null;
        JsonNode cooldownNode = node == null ? null : node.get("cooldown");
        if (cooldownNode != null && !cooldownNode.isNull()) {
            if (!cooldownNode.isString()) {
                errors.add(new FieldError("notificationThreshold.cooldown", "must be a duration such as P7D, 15m or 12h"));
            } else {
                try {
                    Durations.parsePositive(cooldownNode.asString());
                    cooldown = cooldownNode.asString();
                } catch (InvalidInputException e) {
                    errors.add(new FieldError("notificationThreshold.cooldown", e.getMessage()));
                }
            }
        }
        out.put("cooldown", cooldown);
        return out;
    }

    private static String enumValue(JsonNode parent, String field, String member, List<String> allowed,
            String defaultValue, List<FieldError> errors) {
        JsonNode node = parent == null ? null : parent.get(member);
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        if (!node.isString() || !allowed.contains(node.asString())) {
            errors.add(new FieldError(field, "must be one of " + String.join(", ", allowed)));
            return defaultValue;
        }
        return node.asString();
    }

    private static String requiredString(JsonNode body, String field, List<FieldError> errors) {
        JsonNode node = body.get(field);
        if (node == null || !node.isString() || node.asString().isBlank()) {
            errors.add(new FieldError(field, "must be a non-empty string"));
            return "";
        }
        return node.asString().strip();
    }

    private static List<String> strings(JsonNode body, String field) {
        JsonNode node = body.get(field);
        List<String> out = new ArrayList<>();
        if (node != null && node.isArray()) {
            node.forEach(n -> {
                if (n.isString() && !n.asString().isBlank() && !out.contains(n.asString().strip())) {
                    out.add(n.asString().strip());
                }
            });
        }
        return out;
    }
}
