package ai.foresignal.sink;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.Encrypted;
import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/** CRUD, validation and lifecycle for the outbound sinks (prompt 110). */
@Service
public class SinkService {

    /** The id pattern. */
    public static final Pattern ID = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    /** The default page size. */
    public static final int DEFAULT_LIMIT = 100;

    /** The largest page size. */
    public static final int MAX_LIMIT = 500;

    private final SinkRepository sinks;
    private final SinkCursorRepository cursors;
    private final SinkTypeRegistry types;
    private final SinkSupervisor supervisor;
    private final CredentialCipher cipher;
    private final Clock clock;

    /** Creates the service. */
    public SinkService(SinkRepository sinks, SinkCursorRepository cursors, SinkTypeRegistry types,
            SinkSupervisor supervisor, CredentialCipher cipher, Clock clock) {
        this.sinks = sinks;
        this.cursors = cursors;
        this.types = types;
        this.supervisor = supervisor;
        this.cipher = cipher;
        this.clock = clock;
    }

    /** A sink and its cursor. */
    public record View(SinkEntity sink, SinkCursorEntity cursor) {}

    /** Every sink, by id. */
    @Transactional(readOnly = true)
    public List<View> list() {
        List<View> out = new ArrayList<>();
        for (SinkEntity sink : sinks.findAllByOrderByIdAsc()) {
            out.add(new View(sink, cursors.findById(sink.getId()).orElse(null)));
        }
        return out;
    }

    /** One sink. */
    @Transactional(readOnly = true)
    public View get(String id) {
        SinkEntity sink = sinks.findById(id).orElseThrow(() -> NotFoundException.of("Sink", id));
        return new View(sink, cursors.findById(id).orElse(null));
    }

    /** Creates a sink (disabled unless {@code enabled=true}). */
    @Transactional
    public View create(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a SinkCreate object.");
        }
        String id = text(body.get("id"));
        if (id == null || !ID.matcher(id).matches() || id.length() > 64) {
            throw ValidationException.forField("id", "must match ^[a-z0-9]+(-[a-z0-9]+)*$ and be at most 64 chars");
        }
        if (sinks.existsById(id)) {
            throw new ConflictException(ErrorCodes.SINK_EXISTS, "A sink with id " + id + " exists.");
        }
        SinkEntity sink = new SinkEntity(id, type(body), name(body), clock.instant());
        apply(sink, body, Map.of());
        sinks.save(sink);
        if (sink.isEnabled()) {
            supervisor.start(sink.getId());
        }
        return get(id);
    }

    /** Replaces a sink; omitted secrets keep their stored values. */
    @Transactional
    public View update(String id, JsonNode body) {
        SinkEntity sink = sinks.findById(id).orElseThrow(() -> NotFoundException.of("Sink", id));
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a SinkUpdate object.");
        }
        apply(sink, body, secrets(sink));
        sinks.save(sink);
        supervisor.restart(id);
        return get(id);
    }

    /** Deletes a sink. */
    @Transactional
    public void delete(String id) {
        SinkEntity sink = sinks.findById(id).orElseThrow(() -> NotFoundException.of("Sink", id));
        supervisor.stop(id);
        cursors.deleteById(id);
        sinks.delete(sink);
    }

    /** Forces a resync. */
    @Transactional
    public View resync(String id) {
        get(id);
        supervisor.resync(id);
        return get(id);
    }

    /** Decrypted secrets of a sink. */
    @Transactional(readOnly = true)
    public Map<String, String> secrets(SinkEntity sink) {
        if (sink.getSecretCiphertext() == null || sink.getSecretNonce() == null) {
            return Map.of();
        }
        return new LinkedHashMap<>(
                cipher.decryptToMap(new Encrypted(sink.getSecretCiphertext(), sink.getSecretNonce())));
    }

    /** The config a type opens. */
    public SinkConfig config(SinkEntity sink, SinkMode mode) {
        return new SinkConfig(sink.getId(), sink.getType(), sink.getName(), mode,
                new LinkedHashMap<>(sink.getConfig()), secrets(sink), filter(sink));
    }

    /** The sink's filter. */
    public SinkFilter filter(SinkEntity sink) {
        return SinkFilter.parse(tools.jackson.databind.json.JsonMapper.builder().build()
                .valueToTree(sink.getFilter() == null ? Map.of() : sink.getFilter()));
    }

    private void apply(SinkEntity sink, JsonNode body, Map<String, String> stored) {
        List<FieldError> errors = new ArrayList<>();
        SinkType type = types.get(sink.getType());
        if (body.has("name")) {
            sink.setName(name(body));
        }
        if (body.has("enabled")) {
            sink.setEnabled(body.get("enabled").asBoolean(false));
        }
        Map<String, Object> config = body.has("config")
                ? map(body.get("config")) : new LinkedHashMap<>(sink.getConfig());
        Map<String, String> secrets = body.has("secrets") ? secretMap(body.get("secrets")) : stored;
        for (String field : secrets.keySet()) {
            if (!type.spec().secretFields().contains(field)) {
                errors.add(new FieldError("secrets." + field, "is not a secret field of " + type.id()));
            }
        }
        if (body.has("filter")) {
            SinkFilter parsed = SinkFilter.parse(body.get("filter"));
            sink.setFilter(new LinkedHashMap<>(parsed.toMap()));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The sink is not valid.", errors);
        }
        try {
            type.validate(config, secrets);
        } catch (ValidationException e) {
            errors.addAll(e.getErrors());
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The sink is not valid.", errors);
        }
        sink.setConfig(config);
        Map<String, String> toStore = secrets.isEmpty() ? Map.of() : secrets;
        if (toStore.isEmpty()) {
            sink.setSecretCiphertext(null);
            sink.setSecretNonce(null);
        } else {
            Encrypted encrypted = cipher.encrypt(new java.util.HashMap<>(toStore));
            sink.setSecretCiphertext(encrypted.ciphertext());
            sink.setSecretNonce(encrypted.nonce());
        }
        sink.setUpdatedAt(clock.instant());
    }

    private String type(JsonNode body) {
        String type = text(body.get("type"));
        if (type == null || types.find(type).isEmpty()) {
            throw ValidationException.forField("type", "is not a known sink type");
        }
        return type;
    }

    private static String name(JsonNode body) {
        String name = text(body.get("name"));
        if (name == null || name.isBlank() || name.length() > 200) {
            throw ValidationException.forField("name", "is required and at most 200 characters");
        }
        return name.strip();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(JsonNode node) {
        if (node == null || !node.isObject()) {
            throw ValidationException.forField("config", "must be an object");
        }
        return tools.jackson.databind.json.JsonMapper.builder().build().convertValue(node, Map.class);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> secretMap(JsonNode node) {
        if (node == null || node.isNull()) {
            return Map.of();
        }
        if (!node.isObject()) {
            throw ValidationException.forField("secrets", "must be an object");
        }
        Map<String, Object> raw = tools.jackson.databind.json.JsonMapper.builder().build().convertValue(node,
                Map.class);
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : raw.entrySet()) {
            if (e.getValue() != null) {
                out.put(e.getKey(), String.valueOf(e.getValue()));
            }
        }
        return out;
    }

    private static String text(JsonNode node) {
        return node == null || node.isNull() ? null : node.asString();
    }
}
