package ai.foresignal.notify.escalation;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.store.EscalationPolicyEntity;
import ai.foresignal.store.EscalationPolicyRepository;
import ai.foresignal.store.NotificationChannelRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/**
 * Escalation policy management (prompt 033): list, get, create, replace and delete. Every channel a
 * step names must exist; a policy still referenced by trigger rules cannot be deleted
 * ({@code 409 POLICY_IN_USE}). A replace takes effect for running escalations from their next step.
 */
@Service
public class EscalationPolicyService {

    private final EscalationPolicyRepository policies;
    private final NotificationChannelRepository channels;
    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;

    /** Creates the service. */
    public EscalationPolicyService(EscalationPolicyRepository policies, NotificationChannelRepository channels,
            NamedParameterJdbcTemplate jdbc, Clock clock) {
        this.policies = policies;
        this.channels = channels;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /** Every policy, by name, as {@code EscalationPolicy}. */
    public List<Map<String, Object>> list() {
        List<EscalationPolicyEntity> all = new ArrayList<>(policies.findAll());
        all.sort(Comparator.comparing(EscalationPolicyEntity::getName).thenComparing(p -> p.getId().toString()));
        return all.stream().map(this::view).toList();
    }

    /**
     * One policy.
     *
     * @throws NotFoundException when there is none
     */
    public Map<String, Object> get(UUID id) {
        return view(load(id));
    }

    /** The stored policy, or 404. */
    public EscalationPolicyEntity load(UUID id) {
        return policies.findById(id).orElseThrow(() -> NotFoundException.of("Escalation policy", id));
    }

    /** Creates a policy from an {@code EscalationPolicyInput}. */
    @Transactional
    public Map<String, Object> create(JsonNode body) {
        EscalationPolicyInput input = validated(body);
        Instant now = clock.instant();
        EscalationPolicyEntity entity = new EscalationPolicyEntity(UUID.randomUUID());
        entity.setCreatedAt(now);
        apply(entity, input, now);
        return view(policies.saveAndFlush(entity));
    }

    /** Replaces a policy whole. */
    @Transactional
    public Map<String, Object> update(UUID id, JsonNode body) {
        EscalationPolicyEntity entity = load(id);
        EscalationPolicyInput input = validated(body);
        apply(entity, input, clock.instant());
        return view(policies.saveAndFlush(entity));
    }

    /**
     * Deletes a policy.
     *
     * @throws ConflictException {@code 409 POLICY_IN_USE} while rules reference it
     */
    @Transactional
    public void delete(UUID id) {
        EscalationPolicyEntity entity = load(id);
        long rules = ruleCount(id);
        if (rules > 0) {
            throw new ConflictException(ErrorCodes.POLICY_IN_USE, "The escalation policy is used by " + rules
                    + " trigger rule(s); remove it from them first.");
        }
        policies.delete(entity);
    }

    private EscalationPolicyInput validated(JsonNode body) {
        EscalationPolicyInput input = EscalationPolicyInput.parse(body);
        List<FieldError> errors = new ArrayList<>();
        for (int i = 0; i < input.steps().size(); i++) {
            for (UUID channel : input.steps().get(i).channelIds()) {
                if (!channels.existsById(channel)) {
                    errors.add(new FieldError("steps[" + i + "].channelIds", "names no channel: " + channel));
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The escalation policy is not valid.", errors);
        }
        return input;
    }

    private static void apply(EscalationPolicyEntity entity, EscalationPolicyInput input, Instant now) {
        entity.setName(input.name());
        List<Map<String, Object>> steps = new ArrayList<>();
        input.steps().forEach(step -> steps.add(step.toMap()));
        entity.setSteps(steps);
        entity.setRepeat(input.repeat() == null ? null : input.repeat().toMap());
        Map<String, Object> stopOn = new LinkedHashMap<>();
        stopOn.put("ack", true);
        stopOn.put("resolve", input.stopOnResolve());
        stopOn.put("snooze", input.stopOnSnooze());
        entity.setStopOn(stopOn);
        entity.setUpdatedAt(now);
    }

    /** How many rules reference policy {@code id}. */
    public long ruleCount(UUID id) {
        Long count = jdbc.queryForObject("select count(*) from trigger_rule where escalation_policy_id = :id",
                new MapSqlParameterSource("id", id), Long.class);
        return count == null ? 0 : count;
    }

    /** The {@code EscalationPolicy} JSON. */
    Map<String, Object> view(EscalationPolicyEntity e) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", e.getId().toString());
        out.put("name", e.getName());
        out.put("steps", e.getSteps());
        out.put("repeat", e.getRepeat());
        Map<String, Object> stopOn = new LinkedHashMap<>();
        stopOn.put("resolve", !Boolean.FALSE.equals(e.getStopOn().get("resolve")));
        stopOn.put("snooze", !Boolean.FALSE.equals(e.getStopOn().get("snooze")));
        out.put("stopOn", stopOn);
        out.put("createdAt", e.getCreatedAt().toString());
        out.put("updatedAt", e.getUpdatedAt().toString());
        out.put("ruleCount", ruleCount(e.getId()));
        return out;
    }
}
