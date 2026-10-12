package ai.foresignal.inbound.device;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.store.purge.EntityPurgeService;
import ai.foresignal.store.purge.PurgeReport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * {@code listIngestDevices}, {@code createIngestDevice}, {@code deleteIngestDevice},
 * {@code rotateIngestDeviceKey} and {@code purgeDevice} (admin, prompt 039).
 *
 * <p>Keys are shown once, with the tracker app setup data ({@link IngestDeviceSetup}), in the create
 * and rotate responses; listings never carry a key. All setup URLs are based on
 * {@code server.publicBaseUrl}, or the request's own origin when it is unset.
 */
@RestController
@Tag(name = "Ingest")
public class IngestDevicesController {

    private final IngestDeviceService devices;
    private final EntityPurgeService purges;
    private final AppSettingService settings;

    /** Creates the controller. */
    public IngestDevicesController(IngestDeviceService devices, EntityPurgeService purges,
            AppSettingService settings) {
        this.devices = devices;
        this.purges = purges;
        this.settings = settings;
    }

    /** {@code listIngestDevices}. */
    @GetMapping("/v1/custom-sources/{sourceId}/devices")
    @Operation(operationId = "listIngestDevices", tags = "Ingest")
    public List<Map<String, Object>> listIngestDevices(@PathVariable String sourceId) {
        return devices.list(sourceId).stream().map(IngestDevicesController::view).toList();
    }

    /** {@code createIngestDevice}: 201 with the key, shown once, and the setup data. */
    @PostMapping("/v1/custom-sources/{sourceId}/devices")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createIngestDevice", tags = "Ingest")
    public Map<String, Object> createIngestDevice(@PathVariable String sourceId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        return created(devices.create(sourceId, body), request);
    }

    /** {@code deleteIngestDevice}: 204; the key stops working at once, the records stay. */
    @DeleteMapping("/v1/custom-sources/{sourceId}/devices/{deviceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteIngestDevice", tags = "Ingest")
    public void deleteIngestDevice(@PathVariable String sourceId, @PathVariable String deviceId) {
        devices.revoke(sourceId, uuid(deviceId));
    }

    /** {@code rotateIngestDeviceKey}: the new key and setup data, once; the old key stops working. */
    @PostMapping("/v1/custom-sources/{sourceId}/devices/{deviceId}/key/rotate")
    @Operation(operationId = "rotateIngestDeviceKey", tags = "Ingest")
    public Map<String, Object> rotateIngestDeviceKey(@PathVariable String sourceId, @PathVariable String deviceId,
            HttpServletRequest request) {
        return created(devices.rotate(sourceId, uuid(deviceId)), request);
    }

    /**
     * {@code purgeDevice}: purges the device's bound entity ({@code EntityPurge}) and revokes its key.
     * The key is revoked first, so nothing can re-create the entity between the purge and the
     * revocation.
     */
    @PostMapping("/v1/custom-sources/{sourceId}/devices/{deviceId}/purge")
    @Operation(operationId = "purgeDevice", tags = "Ingest")
    public PurgeReport purgeDevice(@PathVariable String sourceId, @PathVariable String deviceId,
            @RequestBody(required = false) JsonNode body) {
        boolean purgeAlerts = purgeAlerts(body);
        IngestDeviceEntity device = devices.revoke(sourceId, uuid(deviceId));
        return purges.purge(device.getSourceId(), device.getExternalId(), purgeAlerts);
    }

    /** The {@code IngestDevice} JSON of {@code device}; never the key. */
    static Map<String, Object> view(IngestDeviceEntity device) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", device.getId());
        out.put("sourceId", device.getSourceId());
        out.put("externalId", device.getExternalId());
        out.put("name", device.getName());
        out.put("keyPrefix", device.getKeyPrefix());
        out.put("createdAt", device.getCreatedAt());
        out.put("lastSeenAt", device.getLastSeenAt());
        out.put("revokedAt", device.getRevokedAt());
        return out;
    }

    private Map<String, Object> created(IngestDeviceService.Created created, HttpServletRequest request) {
        Map<String, Object> out = view(created.device());
        out.put("key", created.key());
        out.put("setup", IngestDeviceSetup.of(baseUrl(request), created.device(), created.key()));
        return out;
    }

    /** {@code server.publicBaseUrl} when set, else the request's own origin. */
    private String baseUrl(HttpServletRequest request) {
        ServerSettings server = settings.getOrDefault(SettingKeys.SERVER, ServerSettings.class, ServerSettings::defaults);
        String configured = server.normalizedPublicBaseUrl();
        if (configured != null) {
            return configured;
        }
        return requestBase(request.getScheme(), request.getServerName(), request.getServerPort());
    }

    /** {@code scheme://host[:port]}, bracketing an IPv6 literal and omitting 80/443 for http/https. */
    static String requestBase(String scheme, String host, int port) {
        String h = host.contains(":") && !host.startsWith("[") ? "[" + host + "]" : host;
        boolean defaultPort = port <= 0
                || ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);
        return scheme + "://" + h + (defaultPort ? "" : ":" + port);
    }

    /**
     * The {@code purgeAlerts} member of an optional {@code PurgeRequest} body (also used by the Postman
     * contract test).
     *
     * @throws ValidationException when the body is not a {@code PurgeRequest}
     */
    public static boolean purgeAlerts(JsonNode body) {
        if (body == null || body.isNull() || body.isMissingNode()) {
            return false;
        }
        if (!body.isObject()) {
            throw new ValidationException("The body must be a PurgeRequest object.");
        }
        for (String member : body.propertyNames()) {
            if (!"purgeAlerts".equals(member)) {
                throw ValidationException.forField(member, "is not a PurgeRequest member");
            }
        }
        JsonNode value = body.get("purgeAlerts");
        if (value == null || value.isNull()) {
            return false;
        }
        if (!value.isBoolean()) {
            throw ValidationException.forField("purgeAlerts", "must be a boolean");
        }
        return value.booleanValue();
    }

    private static UUID uuid(String deviceId) {
        try {
            return UUID.fromString(deviceId);
        } catch (IllegalArgumentException e) {
            throw NotFoundException.of("Ingest device", deviceId);
        }
    }
}
