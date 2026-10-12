package ai.foresignal.notify.escalation;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import tools.jackson.databind.JsonNode;

/**
 * A validated {@code EscalationPolicyInput} (prompt 033).
 *
 * @param name 1–200 characters
 * @param steps 1–5 steps with strictly increasing {@code afterMinutes} (1–1440)
 * @param repeat the optional repeat of the last step, or null
 * @param stopOnResolve stop when the alert resolves (default true)
 * @param stopOnSnooze stop when the alert is snoozed (default true)
 */
public record EscalationPolicyInput(String name, List<Step> steps, Repeat repeat, boolean stopOnResolve,
        boolean stopOnSnooze) {

    /** The most steps. */
    public static final int MAX_STEPS = 5;

    /** The largest {@code afterMinutes}. */
    public static final int MAX_AFTER = 1440;

    /** The most repeats. */
    public static final int MAX_REPEATS = 10;

    /**
     * One step.
     *
     * @param afterMinutes minutes after the alert fired
     * @param channelIds the channels to deliver to
     * @param includePush whether the built-in Push channel is added
     */
    public record Step(int afterMinutes, List<UUID> channelIds, boolean includePush) {

        /** Copies the ids. */
        public Step {
            channelIds = channelIds == null ? List.of() : List.copyOf(channelIds);
        }

        /** The stored form. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("afterMinutes", afterMinutes);
            out.put("channelIds", channelIds.stream().map(UUID::toString).toList());
            out.put("includePush", includePush);
            return out;
        }

        /** Reads the stored form. */
        public static Step fromMap(Map<String, Object> map) {
            List<UUID> ids = new ArrayList<>();
            if (map.get("channelIds") instanceof List<?> list) {
                for (Object id : list) {
                    try {
                        ids.add(UUID.fromString(String.valueOf(id)));
                    } catch (IllegalArgumentException e) {
                        // a malformed stored id names no channel
                    }
                }
            }
            Object after = map.get("afterMinutes");
            return new Step(after instanceof Number n ? n.intValue() : 1, ids, Boolean.TRUE.equals(map.get("includePush")));
        }
    }

    /**
     * The repeat of the last step.
     *
     * @param everyMinutes minutes between repeats (≥ 1)
     * @param maxTimes how many repeats (1–10)
     */
    public record Repeat(int everyMinutes, int maxTimes) {

        /** The stored form. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("everyMinutes", everyMinutes);
            out.put("maxTimes", maxTimes);
            return out;
        }

        /** Reads the stored form, or null. */
        public static Repeat fromMap(Map<String, Object> map) {
            if (map == null || !(map.get("everyMinutes") instanceof Number every)
                    || !(map.get("maxTimes") instanceof Number times)) {
                return null;
            }
            return new Repeat(every.intValue(), times.intValue());
        }
    }

    /** Copies the steps. */
    public EscalationPolicyInput {
        steps = List.copyOf(steps);
    }

    /**
     * Parses and validates {@code body}, reporting every problem in one {@code 400}. Channel existence
     * is checked by the service.
     *
     * @throws ValidationException when it is not valid
     */
    public static EscalationPolicyInput parse(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The body must be an EscalationPolicyInput object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String field : body.propertyNames()) {
            // Output-only members of a policy read back are ignored, so a GET can be PUT back.
            if (!Set.of("name", "steps", "repeat", "stopOn", "id", "createdAt", "updatedAt", "ruleCount").contains(field)) {
                errors.add(new FieldError(field, "is not an EscalationPolicyInput member"));
            }
        }
        JsonNode nameNode = body.get("name");
        String name = null;
        if (nameNode == null || !nameNode.isString() || nameNode.asString().isBlank()) {
            errors.add(new FieldError("name", "is required"));
        } else if (nameNode.asString().length() > 200) {
            errors.add(new FieldError("name", "must be at most 200 characters"));
        } else {
            name = nameNode.asString().strip();
        }
        List<Step> steps = new ArrayList<>();
        JsonNode stepsNode = body.get("steps");
        if (stepsNode == null || !stepsNode.isArray() || stepsNode.isEmpty() || stepsNode.size() > MAX_STEPS) {
            errors.add(new FieldError("steps", "must be an array of 1 to " + MAX_STEPS + " steps"));
        } else {
            int previous = 0;
            for (int i = 0; i < stepsNode.size(); i++) {
                JsonNode step = stepsNode.get(i);
                String at = "steps[" + i + "]";
                if (!step.isObject()) {
                    errors.add(new FieldError(at, "must be an object"));
                    continue;
                }
                JsonNode after = step.get("afterMinutes");
                int minutes = 0;
                if (after == null || !after.isIntegralNumber() || after.asLong() < 1 || after.asLong() > MAX_AFTER) {
                    errors.add(new FieldError(at + ".afterMinutes", "must be an integer from 1 to " + MAX_AFTER));
                } else {
                    minutes = after.asInt();
                    if (minutes <= previous) {
                        errors.add(new FieldError(at + ".afterMinutes", "must be greater than the previous step's"));
                    }
                    previous = Math.max(previous, minutes);
                }
                List<UUID> ids = new ArrayList<>();
                JsonNode channels = step.get("channelIds");
                if (channels != null && !channels.isNull()) {
                    if (!channels.isArray()) {
                        errors.add(new FieldError(at + ".channelIds", "must be an array of channel ids"));
                    } else {
                        for (JsonNode id : channels) {
                            try {
                                UUID channel = UUID.fromString(id.asString());
                                if (!ids.contains(channel)) {
                                    ids.add(channel);
                                }
                            } catch (RuntimeException e) {
                                errors.add(new FieldError(at + ".channelIds", "'" + id + "' is not a channel id"));
                            }
                        }
                    }
                }
                JsonNode push = step.get("includePush");
                if (push != null && !push.isNull() && !push.isBoolean()) {
                    errors.add(new FieldError(at + ".includePush", "must be true or false"));
                }
                boolean includePush = push != null && push.asBoolean(false);
                if (ids.isEmpty() && !includePush) {
                    errors.add(new FieldError(at, "needs at least one channel or includePush"));
                }
                for (String field : step.propertyNames()) {
                    if (!Set.of("afterMinutes", "channelIds", "includePush").contains(field)) {
                        errors.add(new FieldError(at + "." + field, "is not an EscalationStep member"));
                    }
                }
                steps.add(new Step(minutes, ids, includePush));
            }
        }
        Repeat repeat = null;
        JsonNode repeatNode = body.get("repeat");
        if (repeatNode != null && !repeatNode.isNull()) {
            JsonNode every = repeatNode.get("everyMinutes");
            JsonNode times = repeatNode.get("maxTimes");
            if (!repeatNode.isObject() || every == null || !every.isIntegralNumber() || every.asLong() < 1
                    || every.asLong() > MAX_AFTER) {
                errors.add(new FieldError("repeat.everyMinutes", "must be an integer from 1 to " + MAX_AFTER));
            } else if (times == null || !times.isIntegralNumber() || times.asLong() < 1 || times.asLong() > MAX_REPEATS) {
                errors.add(new FieldError("repeat.maxTimes", "must be an integer from 1 to " + MAX_REPEATS));
            } else {
                repeat = new Repeat(every.asInt(), times.asInt());
            }
        }
        boolean resolve = true;
        boolean snooze = true;
        JsonNode stopOn = body.get("stopOn");
        if (stopOn != null && !stopOn.isNull()) {
            if (!stopOn.isObject()) {
                errors.add(new FieldError("stopOn", "must be an object"));
            } else {
                for (String field : stopOn.propertyNames()) {
                    JsonNode value = stopOn.get(field);
                    if (!Set.of("resolve", "snooze", "ack").contains(field)) {
                        errors.add(new FieldError("stopOn." + field, "is not a stopOn member"));
                    } else if (!value.isBoolean()) {
                        errors.add(new FieldError("stopOn." + field, "must be true or false"));
                    } else if (field.equals("ack") && !value.asBoolean()) {
                        errors.add(new FieldError("stopOn.ack", "acknowledgement always stops an escalation"));
                    }
                }
                resolve = stopOn.path("resolve").asBoolean(true);
                snooze = stopOn.path("snooze").asBoolean(true);
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The escalation policy is not valid.", errors);
        }
        return new EscalationPolicyInput(name, steps, repeat, resolve, snooze);
    }
}
