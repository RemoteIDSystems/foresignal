package ai.foresignal.notify;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.notify.types.EmailChannel;
import ai.foresignal.trigger.MessageTemplate;
import ai.foresignal.trigger.Severity;
import java.net.URI;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Validates a {@code ChannelInput} body (prompt 018) and collects every problem into one
 * {@code 400 VALIDATION_FAILED}.
 *
 * <p>Generic checks come from the type's {@link ChannelTypeSpec}: required config and secret fields
 * (secrets may be satisfied by the stored ones on a PUT that omits {@code secrets}), field types and
 * enum values, and unknown secret names. Config keys the type does not declare are kept, because the
 * {@code config} object is open (later prompts add {@code actions}, map attachments and so on). The
 * type's own {@link ChannelType#validate} runs last. {@code deferWhileActiveSeconds} (0–600) and
 * {@code config.actions} ({@code ChannelActionsConfig}) are validated and stored (prompt 033). Members
 * owned by later prompts ({@code acceptExercise}, {@code language}) are accepted and not stored yet.
 *
 * <p>Prompt 035: every {@code url} field Foresignal contacts (config or secret) and the email
 * {@code smtpHost} pass {@link OutboundUrlPolicy} profile {@code LAN_ALLOWED}; a refusal is reported
 * on its field and the whole 400 carries code {@code URL_NOT_ALLOWED}. A host that does not resolve
 * at save time is accepted; {@code ChannelSender} checks again on every send. Prompt 060: the {@code mqtt}
 * {@code brokerUrl} host gets the same check. Prompt 084: a type may name a more specific code for its own
 * problems ({@link ChannelType#validationCode}); the mesh types answer {@code MESH_PUBLIC_CHANNEL}.
 */
@Component
public class ChannelInputParser {

    /** Types the spec lists that later prompts implement (none since prompt 084 added meshtastic and meshcore). */
    static final Map<String, String> LATER_TYPES = Map.of();

    /**
     * Built-in types with exactly one channel, created at startup (031: the "Push" channel); they can
     * be updated but not created.
     */
    static final Map<String, String> BUILT_IN_TYPES = Map.of("webpush",
            "is the built-in Push channel, created at startup; update it instead (subscriptions are managed under /v1/push)");

    /** Top-level members of the body. */
    static final Set<String> FIELDS = Set.of("name", "type", "config", "secrets", "events", "ruleIds", "minSeverity",
            "titleTemplate", "bodyTemplate", "waitForEnrichmentSeconds", "enabled", "timezone", "quietHours",
            "groupWindowSeconds", "backlogCap", "editInPlace", "deferWhileActiveSeconds", "acceptExercise", "language");

    /** The longest grouping window, in seconds. */
    static final int MAX_GROUP_WINDOW = 3_600;

    /** The largest backlog cap. */
    static final int MAX_BACKLOG_CAP = 500;

    /** {@code url} fields that are passed on to the receiver rather than contacted by Foresignal. */
    static final Set<String> NOT_CONTACTED = Set.of("iconUrl");

    private static final Pattern HH_MM = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");

    private final ChannelTypeRegistry types;
    private final JsonMapper json;
    private final OutboundUrlPolicy policy;

    /** Creates the parser. */
    public ChannelInputParser(ChannelTypeRegistry types, JsonMapper json, OutboundUrlPolicy policy) {
        this.types = types;
        this.json = json;
        this.policy = policy;
    }

    /**
     * Parses and validates {@code body}.
     *
     * @param body the request body
     * @param storedSecrets the channel's current secrets (PUT), or null on create
     * @throws ValidationException listing every problem
     */
    @SuppressWarnings("unchecked")
    public ChannelInput parse(JsonNode body, Map<String, String> storedSecrets) {
        List<FieldError> errors = new ArrayList<>();
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a ChannelInput object.");
        }
        for (String field : body.propertyNames()) {
            if (!FIELDS.contains(field)) {
                errors.add(new FieldError(field, "is not a ChannelInput member"));
            }
        }
        String name = text(body, "name", errors);
        if (name == null || name.isBlank()) {
            errors.add(new FieldError("name", "is required"));
        } else if (name.length() > 200) {
            errors.add(new FieldError("name", "must be at most 200 characters"));
        }
        String typeId = text(body, "type", errors);
        ChannelType type = null;
        if (typeId == null) {
            errors.add(new FieldError("type", "is required"));
        } else if (BUILT_IN_TYPES.containsKey(typeId) && storedSecrets == null) {
            errors.add(new FieldError("type", "'" + typeId + "' " + BUILT_IN_TYPES.get(typeId)));
        } else if (LATER_TYPES.containsKey(typeId)) {
            errors.add(new FieldError("type", "'" + typeId + "' is not available yet (prompt " + LATER_TYPES.get(typeId) + ")"));
        } else {
            type = types.find(typeId).orElse(null);
            if (type == null) {
                errors.add(new FieldError("type", "unknown channel type '" + typeId + "'"));
            }
        }
        Map<String, Object> config = new LinkedHashMap<>();
        JsonNode configNode = body.get("config");
        if (configNode != null && !configNode.isNull()) {
            if (!configNode.isObject()) {
                errors.add(new FieldError("config", "must be an object"));
            } else {
                config = json.convertValue(configNode, LinkedHashMap.class);
            }
        }
        Map<String, String> secrets = null;
        JsonNode secretsNode = body.get("secrets");
        if (secretsNode != null && !secretsNode.isNull()) {
            secrets = new LinkedHashMap<>();
            if (!secretsNode.isObject()) {
                errors.add(new FieldError("secrets", "must be an object of strings"));
            } else {
                for (String key : secretsNode.propertyNames()) {
                    JsonNode value = secretsNode.get(key);
                    if (value.isNull()) {
                        continue;
                    }
                    if (!value.isString() || value.asString().isBlank()) {
                        errors.add(new FieldError("secrets." + key, "must be a non-empty string"));
                    } else {
                        secrets.put(key, value.asString());
                    }
                }
            }
        }
        boolean urlRefused = false;
        String typeCode = null;
        if (type != null) {
            checkFields(type.spec(), config, secrets == null ? storedSecrets : secrets, errors);
            if (errors.isEmpty()) {
                Map<String, String> effective = secrets == null
                        ? (storedSecrets == null ? Map.of() : storedSecrets) : secrets;
                List<FieldError> typeErrors = type.validate(config, effective);
                if (!typeErrors.isEmpty()) {
                    // Prompt 084: a public mesh channel is refused with MESH_PUBLIC_CHANNEL.
                    typeCode = type.validationCode(config, effective);
                }
                errors.addAll(typeErrors);
            }
            if (errors.isEmpty()) {
                List<FieldError> refused = outboundProblems(type, config, secrets == null ? storedSecrets : secrets);
                urlRefused = !refused.isEmpty();
                errors.addAll(refused);
            }
        }
        List<String> events = new ArrayList<>(List.of(NotificationMessage.ALERT));
        JsonNode eventsNode = body.get("events");
        if (eventsNode != null && !eventsNode.isNull()) {
            events = new ArrayList<>();
            if (!eventsNode.isArray()) {
                errors.add(new FieldError("events", "must be an array"));
            } else {
                for (JsonNode item : eventsNode) {
                    String event = item.isString() ? item.asString() : null;
                    if (event == null || !ChannelTypeSpec.ALL_EVENTS.contains(event)) {
                        errors.add(new FieldError("events", "supported events are alert, briefing and system"));
                    } else if (!events.contains(event)) {
                        events.add(event);
                    }
                }
            }
        }
        List<UUID> ruleIds = null;
        JsonNode rulesNode = body.get("ruleIds");
        if (rulesNode != null && !rulesNode.isNull()) {
            ruleIds = new ArrayList<>();
            if (!rulesNode.isArray()) {
                errors.add(new FieldError("ruleIds", "must be an array of rule ids or null"));
            } else {
                for (JsonNode item : rulesNode) {
                    try {
                        ruleIds.add(UUID.fromString(item.asString()));
                    } catch (RuntimeException e) {
                        errors.add(new FieldError("ruleIds", "'" + item + "' is not a rule id"));
                    }
                }
            }
        }
        String minSeverity = text(body, "minSeverity", errors);
        if (minSeverity == null) {
            minSeverity = Severity.INFO.wire();
        } else if (!Severity.WIRE_VALUES.contains(minSeverity)) {
            errors.add(new FieldError("minSeverity", "must be one of info, warning, critical"));
        }
        String titleTemplate = template(body, "titleTemplate", errors);
        String bodyTemplate = template(body, "bodyTemplate", errors);
        int wait = integer(body, "waitForEnrichmentSeconds", 0, 0, 60, errors);
        boolean enabled = bool(body, "enabled", true, errors);
        String timezone = text(body, "timezone", errors);
        if (timezone != null) {
            try {
                ZoneId.of(timezone);
            } catch (DateTimeException e) {
                errors.add(new FieldError("timezone", "must be an IANA timezone, e.g. America/Chicago"));
            }
        }
        QuietHours quietHours = quietHours(body.get("quietHours"), errors);
        int group = integer(body, "groupWindowSeconds", 0, 0, MAX_GROUP_WINDOW, errors);
        int cap = integer(body, "backlogCap", 20, 1, MAX_BACKLOG_CAP, errors);
        boolean edit = bool(body, "editInPlace", true, errors);
        int deferWhileActive = integer(body, "deferWhileActiveSeconds", 0, 0, 600, errors);
        actionsConfig(config, errors, typeId);
        if (!errors.isEmpty()) {
            if (urlRefused) {
                throw OutboundUrlPolicy.notAllowed(errors);
            }
            if (typeCode != null) {
                throw new ValidationException(typeCode, "The channel would transmit on a public or default mesh"
                        + " channel.", errors);
            }
            throw new ValidationException("The channel is not valid.", errors);
        }
        return new ChannelInput(name.strip(), typeId, config, secrets, events, ruleIds, minSeverity, titleTemplate,
                bodyTemplate, wait, enabled, timezone, quietHours, group, cap, edit, deferWhileActive);
    }

    /**
     * Checks {@code config.actions} ({@code ChannelActionsConfig}, prompt 033): an object with the
     * booleans {@code enabled}, {@code links} and {@code reactions} and a {@code list} of
     * {@code ack}, {@code snooze} and {@code mute}; plus {@code allowedUserIds} (integers) and
     * {@code allowedSenders} (strings) when present.
     */
    static void actionsConfig(Map<String, Object> config, List<FieldError> errors) {
        actionsConfig(config, errors, null);
    }

    /**
     * {@link #actionsConfig(Map, List)} for a channel of {@code typeId}: a {@code meshtastic} or {@code meshcore}
     * channel's {@code allowedSenders} is an object of node id to role (prompt 084), checked by the type itself.
     */
    static void actionsConfig(Map<String, Object> config, List<FieldError> errors, String typeId) {
        // The channel's units (prompt 040): metric or imperial, for {{near.text}}.
        ChannelUnits.validate(config, errors);
        // The map attachment keys (prompt 055): attachMap, mapProducts, mapRadiusKm, maxAttachmentBytes.
        ChannelMapConfig.validate(config, errors);
        Object privatePayload = config.get(PrivatePayloads.CONFIG_KEY);
        if (privatePayload != null && !(privatePayload instanceof Boolean)) {
            // The admin opt-in to private-source payloads (prompt 039).
            errors.add(new FieldError("config." + PrivatePayloads.CONFIG_KEY, "must be true or false"));
        }
        Object actions = config.get("actions");
        if (actions != null) {
            if (!(actions instanceof Map<?, ?> map)) {
                errors.add(new FieldError("config.actions", "must be an object"));
            } else {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    String key = String.valueOf(entry.getKey());
                    Object value = entry.getValue();
                    switch (key) {
                        case "enabled", "links", "reactions" -> {
                            if (value != null && !(value instanceof Boolean)) {
                                errors.add(new FieldError("config.actions." + key, "must be true or false"));
                            }
                        }
                        case "list" -> {
                            if (!(value instanceof List<?> list) || !list.stream()
                                    .allMatch(v -> v instanceof String t && Set.of("ack", "snooze", "mute").contains(t))) {
                                errors.add(new FieldError("config.actions.list", "must be an array of ack, snooze, mute"));
                            }
                        }
                        default -> errors.add(new FieldError("config.actions." + key, "is not a ChannelActionsConfig member"));
                    }
                }
            }
        }
        Object users = config.get("allowedUserIds");
        if (users != null && !(users instanceof List<?> list && list.stream()
                .allMatch(v -> v instanceof Number n && n.doubleValue() == Math.rint(n.doubleValue())))) {
            errors.add(new FieldError("config.allowedUserIds", "must be an array of integers"));
        }
        Object senders = config.get("allowedSenders");
        if (senders != null && !ai.foresignal.notify.mesh.MeshChannels.TYPES.contains(typeId) && !(senders instanceof List<?> list && list.stream().allMatch(String.class::isInstance))) {
            errors.add(new FieldError("config.allowedSenders", "must be an array of strings"));
        }
    }

    /**
     * The outbound URL policy (prompt 035, {@code LAN_ALLOWED}) applied to every contacted
     * {@code url} field and to the email {@code smtpHost}; empty when all pass.
     */
    List<FieldError> outboundProblems(ChannelType type, Map<String, Object> config, Map<String, String> secrets) {
        List<FieldError> out = new ArrayList<>();
        ChannelTypeSpec spec = type.spec();
        for (ChannelFieldSpec field : spec.configFields()) {
            if (ChannelFieldSpec.URL.equals(field.type()) && !NOT_CONTACTED.contains(field.name())
                    && config.get(field.name()) instanceof String url) {
                String problem = policy.saveProblem(url, ChannelSender.PROFILE, false);
                if (problem != null) {
                    out.add(new FieldError("config." + field.name(), problem));
                }
            }
        }
        Map<String, String> s = secrets == null ? Map.of() : secrets;
        for (ChannelFieldSpec field : spec.secretFields()) {
            String url = s.get(field.name());
            if (ChannelFieldSpec.URL.equals(field.type()) && url != null) {
                String problem = policy.saveProblem(url, ChannelSender.PROFILE, false);
                if (problem != null) {
                    out.add(new FieldError("secrets." + field.name(), problem));
                }
            }
        }
        if (EmailChannel.ID.equals(type.id()) && config.get("smtpHost") instanceof String host && !host.isBlank()) {
            String problem = policy.saveHostProblem(host, ChannelSender.PROFILE);
            if (problem != null) {
                out.add(new FieldError("config.smtpHost", problem));
            }
        }
        if (ai.foresignal.notify.types.MqttChannel.ID.equals(type.id())
                && config.get("brokerUrl") instanceof String broker) {
            // Prompt 060: the broker host passes the same LAN_ALLOWED policy (private brokers are fine).
            try {
                String host = ai.foresignal.mqtt.MqttEndpoint.parse(broker).host();
                String problem = policy.saveHostProblem(host, ChannelSender.PROFILE);
                if (problem != null) {
                    out.add(new FieldError("config.brokerUrl", problem));
                }
            } catch (IllegalArgumentException e) {
                out.add(new FieldError("config.brokerUrl", e.getMessage()));
            }
        }
        return out;
    }

    /** The generic checks of the spec's fields. */
    static void checkFields(ChannelTypeSpec spec, Map<String, Object> config, Map<String, String> secrets,
            List<FieldError> errors) {
        for (ChannelFieldSpec field : spec.configFields()) {
            Object value = config.get(field.name());
            if (value == null) {
                if (field.required()) {
                    errors.add(new FieldError("config." + field.name(), "is required"));
                }
                continue;
            }
            String problem = typeProblem(field, value);
            if (problem != null) {
                errors.add(new FieldError("config." + field.name(), problem));
            }
        }
        Map<String, String> s = secrets == null ? Map.of() : secrets;
        for (String key : s.keySet()) {
            if (secretSpec(spec, key) == null) {
                errors.add(new FieldError("secrets." + key, "is not a secret field of type " + spec.id()));
            }
        }
        for (ChannelFieldSpec field : spec.secretFields()) {
            if (field.name().endsWith("<Name>")) {
                continue;
            }
            String value = s.get(field.name());
            if (value == null) {
                if (field.required()) {
                    errors.add(new FieldError("secrets." + field.name(), "is required"));
                }
                continue;
            }
            String problem = typeProblem(field, value);
            if (problem != null) {
                errors.add(new FieldError("secrets." + field.name(), problem));
            }
        }
    }

    /** The secret field spec matching {@code key}, including {@code prefix.<Name>} wildcards. */
    static ChannelFieldSpec secretSpec(ChannelTypeSpec spec, String key) {
        for (ChannelFieldSpec field : spec.secretFields()) {
            if (field.name().equals(key)) {
                return field;
            }
            if (field.name().endsWith("<Name>")) {
                String prefix = field.name().substring(0, field.name().length() - "<Name>".length());
                if (key.startsWith(prefix) && key.length() > prefix.length()) {
                    return field;
                }
            }
        }
        return null;
    }

    private static String typeProblem(ChannelFieldSpec field, Object value) {
        return switch (field.type()) {
            case ChannelFieldSpec.STRING -> value instanceof String ? null : "must be a string";
            case ChannelFieldSpec.URL -> value instanceof String text && isHttpUrl(text) ? null
                    : "must be an absolute http or https URL";
            case ChannelFieldSpec.INTEGER -> value instanceof Number n && n.doubleValue() == Math.rint(n.doubleValue())
                    ? null : "must be an integer";
            case ChannelFieldSpec.NUMBER -> value instanceof Number ? null : "must be a number";
            case ChannelFieldSpec.BOOLEAN -> value instanceof Boolean ? null : "must be true or false";
            case ChannelFieldSpec.STRING_LIST -> value instanceof List<?> list && list.stream().allMatch(String.class::isInstance)
                    ? null : "must be an array of strings";
            case ChannelFieldSpec.MAP -> value instanceof Map<?, ?> map && map.values().stream().allMatch(String.class::isInstance)
                    ? null : "must be an object of strings";
            case ChannelFieldSpec.ENUM -> field.allowed().contains(String.valueOf(value)) ? null
                    : "must be one of " + String.join(", ", field.allowed());
            default -> null;
        };
    }

    /** True for an absolute http(s) URL with a host. */
    static boolean isHttpUrl(String text) {
        try {
            URI uri = URI.create(text.strip());
            String scheme = uri.getScheme();
            return ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) && uri.getHost() != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static QuietHours quietHours(JsonNode node, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isObject()) {
            errors.add(new FieldError("quietHours", "must be an object or null"));
            return null;
        }
        int before = errors.size();
        LocalTime start = time(node, "start", errors);
        LocalTime end = time(node, "end", errors);
        Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        JsonNode daysNode = node.get("days");
        if (daysNode != null && !daysNode.isNull()) {
            if (!daysNode.isArray()) {
                errors.add(new FieldError("quietHours.days", "must be an array of MON … SUN"));
            } else {
                for (JsonNode item : daysNode) {
                    QuietHours.day(item.isString() ? item.asString() : "").ifPresentOrElse(days::add,
                            () -> errors.add(new FieldError("quietHours.days", "must be an array of MON … SUN")));
                }
            }
        }
        String bypass = Severity.CRITICAL.wire();
        if (node.has("bypassSeverity")) {
            JsonNode b = node.get("bypassSeverity");
            if (b.isNull()) {
                bypass = null;
            } else if (!b.isString() || !Severity.WIRE_VALUES.contains(b.asString())) {
                errors.add(new FieldError("quietHours.bypassSeverity", "must be info, warning, critical or null"));
            } else {
                bypass = b.asString();
            }
        }
        for (String field : node.propertyNames()) {
            if (!Set.of("start", "end", "days", "bypassSeverity").contains(field)) {
                errors.add(new FieldError("quietHours." + field, "is not a QuietHours member"));
            }
        }
        if (start != null && start.equals(end)) {
            errors.add(new FieldError("quietHours.end", "must differ from start"));
        }
        return errors.size() > before ? null : new QuietHours(start, end, days, bypass);
    }

    private static LocalTime time(JsonNode node, String field, List<FieldError> errors) {
        JsonNode value = node.get(field);
        if (value == null || !value.isString() || !HH_MM.matcher(value.asString()).matches()) {
            errors.add(new FieldError("quietHours." + field, "is required as HH:mm"));
            return null;
        }
        return LocalTime.parse(value.asString());
    }

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

    private static String template(JsonNode body, String field, List<FieldError> errors) {
        String value = text(body, field, errors);
        if (value != null && value.length() > MessageTemplate.MAX_TEMPLATE) {
            errors.add(new FieldError(field, "must be at most " + MessageTemplate.MAX_TEMPLATE + " characters"));
        }
        return value == null || value.isBlank() ? null : value;
    }

    private static int integer(JsonNode body, String field, int def, int min, int max, List<FieldError> errors) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            return def;
        }
        if (!node.isIntegralNumber() || node.asLong() < min || node.asLong() > max) {
            errors.add(new FieldError(field, "must be an integer from " + min + " to " + max));
            return def;
        }
        return node.asInt();
    }

    private static boolean bool(JsonNode body, String field, boolean def, List<FieldError> errors) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            return def;
        }
        if (!node.isBoolean()) {
            errors.add(new FieldError(field, "must be true or false"));
            return def;
        }
        return node.asBoolean();
    }
}
