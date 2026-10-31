package ai.foresignal.notify.threshold;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.inbound.CustomSourceEntity;
import ai.foresignal.inbound.CustomSourceRepository;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.settings.Durations;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.group.SourceGroupEntity;
import ai.foresignal.source.group.SourceGroupRepository;
import ai.foresignal.trigger.Severity;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
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
 * Per-source notification thresholds (prompt SG-003): get and replace a user's whole set, and decide at alert time
 * whether a source's alert notifies.
 *
 * <p>The decision is a <strong>notification-suppression</strong> one only: an alert below the threshold, from a muted
 * source, or within cooldown is still fired, persisted and shown in the inbox — it just does not notify. The effective
 * threshold for a source is, in order of precedence: the user's per-source entry; otherwise a source group of the
 * user whose members include the source (its {@code GroupThreshold}); otherwise the default ({@code warning},
 * un-muted, no cooldown). Keyed on {@code owner_principal_id} to match the alert enforcement path.
 */
@Service
public class SourceThresholdService {

    private final SourceThresholdRepository thresholds;
    private final SourceNotifyStateRepository state;
    private final SourceRegistry registry;
    private final CustomSourceRepository customSources;
    private final SourceGroupRepository groups;
    private final UserRepository users;
    private final Clock clock;

    /** Creates the service. */
    public SourceThresholdService(SourceThresholdRepository thresholds, SourceNotifyStateRepository state,
            SourceRegistry registry, CustomSourceRepository customSources, SourceGroupRepository groups,
            UserRepository users, Clock clock) {
        this.thresholds = thresholds;
        this.state = state;
        this.registry = registry;
        this.customSources = customSources;
        this.groups = groups;
        this.users = users;
        this.clock = clock;
    }

    /** An effective threshold: the minimum notify severity, mute, and cooldown (null for none). */
    public record Effective(Severity minSeverity, boolean mute, Duration cooldown) {}

    // ---------- API ----------

    /** The user's {@code SourceThresholds} ({@code entries}): one entry per source they have set. */
    @Transactional(readOnly = true)
    public Map<String, Object> get(String principal) {
        List<Map<String, Object>> entries = new ArrayList<>();
        for (SourceThresholdEntity row : thresholds.findByOwnerPrincipalId(principal)) {
            entries.add(entry(row));
        }
        return Map.of("entries", entries);
    }

    /**
     * Replaces the user's whole set from a {@code SourceThresholds} body (mirrors {@code updateMySources}).
     *
     * @throws ValidationException on an unknown source, a bad severity, or a bad cooldown
     */
    @Transactional
    public Map<String, Object> replace(String principal, JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The body must be a SourceThresholds object.");
        }
        JsonNode entries = body.get("entries");
        if (entries == null || entries.isNull()) {
            throw ValidationException.forField("entries", "is required (an array; empty clears the set)");
        }
        if (!entries.isArray()) {
            throw ValidationException.forField("entries", "must be an array");
        }
        UUID owner = users.findByPrincipalId(principal).map(u -> u.getId()).orElse(null);
        List<FieldError> errors = new ArrayList<>();
        List<SourceThresholdEntity> parsed = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            SourceThresholdEntity row = entry(entries.get(i), i, principal, owner, errors);
            if (row != null) {
                parsed.add(row);
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The source thresholds are not valid.", errors);
        }
        thresholds.deleteByOwnerPrincipalId(principal);
        thresholds.flush();
        thresholds.saveAllAndFlush(parsed);
        return get(principal);
    }

    // ---------- enforcement ----------

    /**
     * Whether an alert from {@code sourceId} owned by {@code principal} should notify at {@code now} (prompt SG-003):
     * false when its severity is below the effective minimum, the source is muted, or it is within the effective
     * cooldown of the last notification for this {@code (owner, source)}.
     */
    @Transactional(readOnly = true)
    public boolean shouldNotify(String principal, String sourceId, String severityWire, Instant now) {
        if (principal == null || sourceId == null) {
            return true;
        }
        Effective effective = effective(principal, sourceId);
        if (effective.mute()) {
            return false;
        }
        Severity severity;
        try {
            severity = Severity.fromWire(severityWire);
        } catch (IllegalArgumentException e) {
            severity = Severity.WARNING;
        }
        if (severity.ordinal() < effective.minSeverity().ordinal()) {
            return false;
        }
        if (effective.cooldown() != null) {
            Optional<SourceNotifyStateEntity> last = state.findByOwnerPrincipalIdAndSourceId(principal, sourceId);
            if (last.isPresent() && last.get().getLastNotifiedAt() != null
                    && !last.get().getLastNotifiedAt().plus(effective.cooldown()).isBefore(now)) {
                return false;
            }
        }
        return true;
    }

    /** Stamps that a notification for {@code (principal, sourceId)} went out at {@code now} (prompt SG-003). */
    @Transactional
    public void recordNotified(String principal, String sourceId, Instant now) {
        if (principal == null || sourceId == null) {
            return;
        }
        SourceNotifyStateEntity row = state.findByOwnerPrincipalIdAndSourceId(principal, sourceId)
                .orElseGet(() -> new SourceNotifyStateEntity(principal, sourceId));
        row.setLastNotifiedAt(now);
        state.saveAndFlush(row);
    }

    /** The effective threshold for a source: per-source entry &gt; covering group threshold &gt; default. */
    @Transactional(readOnly = true)
    public Effective effective(String principal, String sourceId) {
        Optional<SourceThresholdEntity> own = thresholds.findById(new SourceThresholdId(principal, sourceId));
        if (own.isPresent()) {
            SourceThresholdEntity row = own.get();
            return new Effective(Severity.fromWire(row.getMinSeverity()), row.isMute(),
                    row.getCooldownSeconds() == null ? null : Duration.ofSeconds(row.getCooldownSeconds()));
        }
        Optional<Effective> group = groupThreshold(principal, sourceId);
        return group.orElseGet(() -> new Effective(Severity.WARNING, false, null));
    }

    private Optional<Effective> groupThreshold(String principal, String sourceId) {
        UUID owner = users.findByPrincipalId(principal).map(u -> u.getId()).orElse(null);
        if (owner == null) {
            return Optional.empty();
        }
        for (SourceGroupEntity group : groups.findByOwnerUserIdOrderByCreatedAtAscIdAsc(owner)) {
            if (group.getSourceIds().contains(sourceId) && group.getNotificationThreshold() != null) {
                return Optional.of(fromGroup(group.getNotificationThreshold()));
            }
        }
        return Optional.empty();
    }

    private static Effective fromGroup(Map<String, Object> threshold) {
        Object severity = threshold.get("minSeverity");
        Object mute = threshold.get("mute");
        Object cooldown = threshold.get("cooldown");
        Severity min;
        try {
            min = severity instanceof String s ? Severity.fromWire(s) : Severity.WARNING;
        } catch (IllegalArgumentException e) {
            min = Severity.WARNING;
        }
        Duration cool = null;
        if (cooldown instanceof String s && !s.isBlank()) {
            try {
                cool = Durations.parsePositive(s);
            } catch (InvalidInputException e) {
                cool = null;
            }
        }
        return new Effective(min, Boolean.TRUE.equals(mute), cool);
    }

    // ---------- mapping ----------

    private static Map<String, Object> entry(SourceThresholdEntity row) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sourceId", row.getSourceId());
        out.put("minSeverity", row.getMinSeverity());
        out.put("mute", row.isMute());
        out.put("cooldown", row.getCooldownSeconds() == null ? null
                : Duration.ofSeconds(row.getCooldownSeconds()).toString());
        return out;
    }

    private SourceThresholdEntity entry(JsonNode node, int index, String principal, UUID owner,
            List<FieldError> errors) {
        String prefix = "entries[" + index + "]";
        if (node == null || !node.isObject()) {
            errors.add(new FieldError(prefix, "must be a SourceThresholdEntry object"));
            return null;
        }
        JsonNode sourceNode = node.get("sourceId");
        if (sourceNode == null || !sourceNode.isString() || sourceNode.asString().isBlank()) {
            errors.add(new FieldError(prefix + ".sourceId", "must be a non-empty string"));
            return null;
        }
        String sourceId = sourceNode.asString().strip();
        if (!known(sourceId, owner)) {
            errors.add(new FieldError(prefix + ".sourceId", "unknown source '" + sourceId + "'"));
        }
        SourceThresholdEntity row = new SourceThresholdEntity(principal, sourceId);
        String minSeverity = "warning";
        JsonNode severityNode = node.get("minSeverity");
        if (severityNode != null && !severityNode.isNull()) {
            try {
                minSeverity = Severity.fromWire(severityNode.isString() ? severityNode.asString() : "").wire();
            } catch (IllegalArgumentException e) {
                errors.add(new FieldError(prefix + ".minSeverity", e.getMessage()));
            }
        }
        row.setMinSeverity(minSeverity);
        JsonNode muteNode = node.get("mute");
        row.setMute(muteNode != null && muteNode.isBoolean() && muteNode.booleanValue());
        JsonNode cooldownNode = node.get("cooldown");
        if (cooldownNode != null && !cooldownNode.isNull()) {
            if (!cooldownNode.isString()) {
                errors.add(new FieldError(prefix + ".cooldown", "must be a duration such as P7D, 15m or 12h"));
            } else {
                try {
                    row.setCooldownSeconds(Durations.parsePositive(cooldownNode.asString()).toSeconds());
                } catch (InvalidInputException e) {
                    errors.add(new FieldError(prefix + ".cooldown", e.getMessage()));
                }
            }
        }
        return row;
    }

    /** A source is known if it is the caller's own custom source, or a catalog source in the registry. */
    private boolean known(String sourceId, UUID owner) {
        Optional<CustomSourceEntity> custom = customSources.findById(sourceId);
        if (custom.isPresent()) {
            return owner != null && owner.equals(custom.get().getOwnerUserId());
        }
        return registry.contains(sourceId);
    }
}
