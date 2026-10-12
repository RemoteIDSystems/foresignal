package ai.foresignal.api;

import ai.foresignal.notify.inbox.DeviceKeys;
import ai.foresignal.notify.inbox.PresenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Client presence of the {@code Notifications} tag (prompt 033): {@code updatePresence} (read,
 * always authenticated; the calling device's heartbeat, {@code ttlSeconds} ≤ 300) and
 * {@code listClients} (admin).
 */
@RestController
@Tag(name = "Notifications")
@RequestMapping("/v1/clients")
public class ClientsController {

    private final PresenceService presence;
    private final DeviceKeys devices;

    /** Creates the controller. */
    public ClientsController(PresenceService presence, DeviceKeys devices) {
        this.presence = presence;
        this.devices = devices;
    }

    /** {@code updatePresence}. */
    @PostMapping("/presence")
    @Operation(operationId = "updatePresence", tags = "Notifications")
    public Map<String, Object> updatePresence(@RequestBody JsonNode body, HttpServletRequest request) {
        String device = devices.resolve(request);
        return presence.update(device, PresenceService.parse(body)).toMap();
    }

    /** {@code listClients}: every presence row. */
    @GetMapping
    @Operation(operationId = "listClients", tags = "Notifications")
    public List<Map<String, Object>> listClients() {
        return presence.list().stream().map(PresenceService.Presence::toMap).toList();
    }
}
