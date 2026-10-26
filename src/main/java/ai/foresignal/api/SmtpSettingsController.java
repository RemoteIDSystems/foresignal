package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.user.TransactionalEmailService;
import ai.foresignal.settings.SmtpSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The SMTP settings of the transactional account emails (prompt R2-007): {@code getSmtpSettings},
 * {@code updateSmtpSettings} and {@code testSmtpSettings}. Admin-scoped with
 * {@code x-foresignal-permission: system.settings} (R2-004). The password is write-only.
 */
@RestController
@Tag(name = "Settings")
public class SmtpSettingsController {

    private final SmtpSettingsService settings;
    private final TransactionalEmailService email;

    /**
     * Creates the controller.
     *
     * @param settings the SMTP settings
     * @param email the transactional email sender, for the test message
     */
    public SmtpSettingsController(SmtpSettingsService settings, TransactionalEmailService email) {
        this.settings = settings;
        this.email = email;
    }

    /** {@code getSmtpSettings}: everything but the password, with {@code secretsSet}. */
    @GetMapping("/v1/settings/smtp")
    @Operation(operationId = "getSmtpSettings", tags = "Settings")
    public Map<String, Object> getSmtpSettings() {
        return settings.view(settings.get());
    }

    /** {@code updateSmtpSettings}: validates and stores; an omitted password keeps the stored one. */
    @PutMapping("/v1/settings/smtp")
    @Operation(operationId = "updateSmtpSettings", tags = "Settings")
    public Map<String, Object> updateSmtpSettings(@RequestBody JsonNode body) {
        return settings.view(settings.update(body));
    }

    /** {@code testSmtpSettings}: sends a test message with the stored settings; a failure is {@code ok: false}. */
    @PostMapping("/v1/settings/smtp/test")
    @Operation(operationId = "testSmtpSettings", tags = "Settings")
    public Map<String, Object> testSmtpSettings(@RequestBody JsonNode body) {
        JsonNode to = body == null ? null : body.get("to");
        if (to == null || !to.isString() || to.asString().isBlank()) {
            throw ValidationException.forField("to", "is required");
        }
        String address = to.asString().strip();
        if (!SmtpSettingsService.isEmailAddress(address)) {
            throw ValidationException.forField("to", "is not an email address");
        }
        return email.test(address);
    }
}
