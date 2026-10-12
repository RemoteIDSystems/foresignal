package ai.foresignal.api;

import ai.foresignal.notify.NotificationSettingsService;
import ai.foresignal.settings.NotificationSettings;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * {@code getNotificationSettings} and {@code updateNotificationSettings} (prompt 018): the global
 * pause, {@code app_setting['notifications']}. Admin only ({@code /v1/settings/**}).
 *
 * <p>A PUT replaces the setting; parsing and applying are {@link NotificationSettingsService}'s,
 * which the settings import (prompt 019) shares.
 */
@RestController
public class NotificationSettingsController {

    private final NotificationSettingsService service;

    /** Creates the controller. */
    public NotificationSettingsController(NotificationSettingsService service) {
        this.service = service;
    }

    /** {@code getNotificationSettings}. */
    @GetMapping("/v1/settings/notifications")
    public Map<String, Object> getNotificationSettings() {
        return NotificationSettingsService.body(service.get());
    }

    /** {@code updateNotificationSettings}. */
    @PutMapping("/v1/settings/notifications")
    public Map<String, Object> updateNotificationSettings(@RequestBody JsonNode body) {
        NotificationSettings value = service.apply(NotificationSettingsService.parse(body));
        return NotificationSettingsService.body(value);
    }
}
