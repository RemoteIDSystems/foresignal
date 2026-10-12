package ai.foresignal.api;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.notify.push.device.NativePushTester;
import ai.foresignal.notify.push.device.PushDeviceService;
import ai.foresignal.notify.push.provider.PushProviderService;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.CurrentToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The native push operations of the {@code Push} tag (prompt 032).
 *
 * <ul>
 *   <li>Providers (admin): {@code listPushProviders}, {@code putPushProvider},
 *       {@code deletePushProvider}, {@code testPushProvider}. Secrets are never returned.
 *   <li>Devices (read scope, always authenticated): {@code registerPushDevice},
 *       {@code listPushDevices}, {@code updatePushDevice}, {@code deletePushDevice},
 *       {@code testPushDevice}. Device tokens are never returned, only their last 6 characters.
 * </ul>
 */
@RestController
@Tag(name = "Push")
@RequestMapping("/v1/push")
public class NativePushController {

    private final PushProviderService providers;
    private final PushDeviceService devices;
    private final NativePushTester tester;

    /** Creates the controller. */
    public NativePushController(PushProviderService providers, PushDeviceService devices, NativePushTester tester) {
        this.providers = providers;
        this.devices = devices;
        this.tester = tester;
    }

    /** {@code listPushProviders}: by id, without secrets. */
    @GetMapping("/providers")
    @Operation(operationId = "listPushProviders", tags = "Push")
    public List<Map<String, Object>> listPushProviders() {
        return providers.list().stream().map(PushProviderService::view).toList();
    }

    /** {@code putPushProvider}: 201 when created, 200 when replaced. */
    @PutMapping("/providers/{providerId}")
    @Operation(operationId = "putPushProvider", tags = "Push")
    public ResponseEntity<Map<String, Object>> putPushProvider(@PathVariable String providerId,
            @RequestBody JsonNode body) {
        PushProviderService.Put put = providers.put(providerId, body);
        Map<String, Object> view = PushProviderService.view(put.provider());
        return put.created()
                ? ResponseEntity.created(URI.create("/v1/push/providers/" + providerId)).body(view)
                : ResponseEntity.ok(view);
    }

    /** {@code deletePushProvider}. */
    @DeleteMapping("/providers/{providerId}")
    @Operation(operationId = "deletePushProvider", tags = "Push")
    public ResponseEntity<Void> deletePushProvider(@PathVariable String providerId) {
        providers.delete(providerId);
        return ResponseEntity.noContent().build();
    }

    /** {@code testPushProvider}: mints a credential and optionally sends a test alert. */
    @PostMapping("/providers/{providerId}/test")
    @Operation(operationId = "testPushProvider", tags = "Push")
    public Map<String, Object> testPushProvider(@PathVariable String providerId,
            @RequestBody(required = false) JsonNode body) {
        return tester.testProvider(providerId, body);
    }

    /** {@code listPushDevices}: every device for admin, the caller's own otherwise. */
    @GetMapping("/devices")
    @Operation(operationId = "listPushDevices", tags = "Push")
    public List<Map<String, Object>> listPushDevices(HttpServletRequest request) {
        return devices.list(caller(request)).stream().map(PushDeviceService::view).toList();
    }

    /** {@code registerPushDevice}: 201 for a new token, 200 when re-registered. */
    @PostMapping("/devices")
    @Operation(operationId = "registerPushDevice", tags = "Push")
    public ResponseEntity<Map<String, Object>> registerPushDevice(@RequestBody JsonNode body,
            HttpServletRequest request) {
        PushDeviceService.Registered registered = devices.register(body, caller(request));
        Map<String, Object> view = PushDeviceService.view(registered.device());
        return registered.created()
                ? ResponseEntity.created(URI.create("/v1/push/devices/" + registered.device().getId())).body(view)
                : ResponseEntity.ok(view);
    }

    /** {@code updatePushDevice}: owner or admin. */
    @PutMapping("/devices/{deviceId}")
    @Operation(operationId = "updatePushDevice", tags = "Push")
    public Map<String, Object> updatePushDevice(@PathVariable String deviceId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        return PushDeviceService.view(devices.update(id(deviceId), body, caller(request)));
    }

    /** {@code deletePushDevice}: owner or admin. */
    @DeleteMapping("/devices/{deviceId}")
    @Operation(operationId = "deletePushDevice", tags = "Push")
    public ResponseEntity<Void> deletePushDevice(@PathVariable String deviceId, HttpServletRequest request) {
        devices.delete(id(deviceId), caller(request));
        return ResponseEntity.noContent().build();
    }

    /** {@code testPushDevice}: owner only, 5 per hour. */
    @PostMapping("/devices/{deviceId}/test")
    @Operation(operationId = "testPushDevice", tags = "Push")
    public Map<String, Object> testPushDevice(@PathVariable String deviceId, HttpServletRequest request) {
        return tester.testDevice(id(deviceId), caller(request));
    }

    private static AuthenticatedToken caller(HttpServletRequest request) {
        return CurrentToken.of(request).map(CurrentToken::token).orElse(null);
    }

    private static UUID id(String text) {
        try {
            return UUID.fromString(text);
        } catch (IllegalArgumentException e) {
            throw NotFoundException.of("Push device", text);
        }
    }
}
