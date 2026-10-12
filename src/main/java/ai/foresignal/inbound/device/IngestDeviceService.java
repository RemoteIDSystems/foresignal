package ai.foresignal.inbound.device;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.inbound.CustomSourceEntity;
import ai.foresignal.inbound.CustomSourceRepository;
import ai.foresignal.inbound.IngestKeys;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/**
 * Device-scoped ingest keys (prompt 039): create, list, rotate, revoke and authenticate.
 *
 * <p>A key is {@code eid_} plus 32 random bytes in base64url; only its SHA-256 and first 8
 * characters are stored. Revoking a key stops ingest at once; the device's records stay until
 * retention removes them or the operator purges them ({@code purgeDevice}).
 */
@Service
public class IngestDeviceService {

    /** Every device key starts with this. */
    public static final String PREFIX = "eid_";

    /** The longest bound externalId. */
    public static final int MAX_EXTERNAL_ID = 256;

    /** The longest device name. */
    public static final int MAX_NAME = 200;

    /** {@code last_seen_at} is written at most this often per device. */
    static final Duration LAST_SEEN_GRANULARITY = Duration.ofMinutes(1);

    private final IngestDeviceRepository devices;
    private final CustomSourceRepository sources;
    private final IngestKeys keys;
    private final Clock clock;

    /** Creates the service. */
    public IngestDeviceService(IngestDeviceRepository devices, CustomSourceRepository sources, IngestKeys keys,
            Clock clock) {
        this.devices = devices;
        this.sources = sources;
        this.keys = keys;
        this.clock = clock;
    }

    /**
     * A device and its plaintext key, shown once.
     *
     * @param device the stored row
     * @param key the key
     */
    public record Created(IngestDeviceEntity device, String key) {}

    /**
     * A validated {@code IngestDeviceInput}.
     *
     * @param externalId the entity to bind
     * @param name the display name
     */
    public record Input(String externalId, String name) {}

    /**
     * Reads and validates an {@code IngestDeviceInput} body (also used by the Postman contract test).
     *
     * @throws ValidationException listing every problem
     */
    public static Input readInput(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The body must be an IngestDeviceInput object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String member : body.propertyNames()) {
            if (!"externalId".equals(member) && !"name".equals(member)) {
                errors.add(new FieldError(member, "is not an IngestDeviceInput member"));
            }
        }
        String externalId = text(body, "externalId", MAX_EXTERNAL_ID, errors);
        String name = text(body, "name", MAX_NAME, errors);
        if (!errors.isEmpty()) {
            throw new ValidationException("The device input is not valid.", errors);
        }
        return new Input(externalId, name);
    }

    /** The devices of custom source {@code sourceId}, oldest first. */
    @Transactional(readOnly = true)
    public List<IngestDeviceEntity> list(String sourceId) {
        requireSource(sourceId);
        return devices.findBySourceIdOrderByCreatedAt(sourceId);
    }

    /**
     * Creates a device key from an {@code IngestDeviceInput} body.
     *
     * @throws NotFoundException when {@code sourceId} is not a custom source
     * @throws ValidationException listing every problem with the body
     */
    @Transactional
    public Created create(String sourceId, JsonNode body) {
        requireSource(sourceId);
        Input input = readInput(body);
        IngestDeviceEntity device = new IngestDeviceEntity(UUID.randomUUID(), sourceId, input.externalId(),
                input.name(), clock.instant());
        String key = keys.generate(PREFIX);
        device.setKey(keys.hash(key), IngestKeys.prefixOf(key));
        devices.saveAndFlush(device);
        return new Created(device, key);
    }

    /**
     * Replaces a device's key; the old key stops working at once. A revoked device gets a working key
     * again (rotation un-revokes it).
     *
     * @throws NotFoundException when there is no such device on {@code sourceId}
     */
    @Transactional
    public Created rotate(String sourceId, UUID deviceId) {
        IngestDeviceEntity device = get(sourceId, deviceId);
        String key = keys.generate(PREFIX);
        device.setKey(keys.hash(key), IngestKeys.prefixOf(key));
        device.setRevokedAt(null);
        devices.saveAndFlush(device);
        return new Created(device, key);
    }

    /**
     * Revokes a device's key; ingest with it stops at once. Revoking twice keeps the first time.
     *
     * @throws NotFoundException when there is no such device on {@code sourceId}
     */
    @Transactional
    public IngestDeviceEntity revoke(String sourceId, UUID deviceId) {
        IngestDeviceEntity device = get(sourceId, deviceId);
        if (device.getRevokedAt() == null) {
            device.setRevokedAt(clock.instant());
            devices.saveAndFlush(device);
        }
        return device;
    }

    /**
     * The device {@code deviceId} of {@code sourceId}.
     *
     * @throws NotFoundException when there is none
     */
    @Transactional(readOnly = true)
    public IngestDeviceEntity get(String sourceId, UUID deviceId) {
        requireSource(sourceId);
        return devices.findById(deviceId).filter(d -> d.getSourceId().equals(sourceId))
                .orElseThrow(() -> NotFoundException.of("Ingest device", deviceId));
    }

    /**
     * The active device whose key is {@code key} (constant-time hash comparison), recording its use;
     * empty for an unknown or revoked key.
     */
    @Transactional
    public Optional<IngestDeviceEntity> authenticate(String key) {
        if (key == null || !key.startsWith(PREFIX)) {
            return Optional.empty();
        }
        Optional<IngestDeviceEntity> found = devices.findByKeyHash(keys.hash(key));
        if (found.isEmpty() || !keys.matches(key, found.get().getKeyHash()) || !found.get().isActive()) {
            return Optional.empty();
        }
        IngestDeviceEntity device = found.get();
        Instant now = clock.instant();
        if (device.getLastSeenAt() == null || !now.isBefore(device.getLastSeenAt().plus(LAST_SEEN_GRANULARITY))) {
            device.setLastSeenAt(now);
            devices.save(device);
        }
        return Optional.of(device);
    }

    private CustomSourceEntity requireSource(String sourceId) {
        return sources.findById(sourceId).filter(row -> !row.isExternal())
                .orElseThrow(() -> NotFoundException.of("Custom source", sourceId));
    }

    private static String text(JsonNode body, String name, int max, List<FieldError> errors) {
        JsonNode value = body.get(name);
        if (value == null || value.isNull()) {
            errors.add(new FieldError(name, "is required"));
            return null;
        }
        if (!value.isString() || value.asString().isBlank()) {
            errors.add(new FieldError(name, "must be a non-empty string"));
            return null;
        }
        String text = value.asString().strip();
        if (text.length() > max) {
            errors.add(new FieldError(name, "must be at most " + max + " characters"));
            return null;
        }
        return text;
    }
}
