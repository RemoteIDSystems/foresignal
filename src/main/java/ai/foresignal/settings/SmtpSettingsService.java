package ai.foresignal.settings;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.Encrypted;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

/**
 * Reads, validates and stores {@link SmtpSettings} ({@code GET/PUT /v1/settings/smtp}, prompt R2-007), the same way
 * the archive settings keep their S3 keys: the password is sealed with {@link CredentialCipher} and never returned;
 * the view lists {@code secretsSet: ["password"]} while one is stored.
 *
 * <p>A PUT replaces the non-secret members (a missing member falls back to its default); for {@code password}, an
 * omitted member keeps the stored one, and {@code null} or an empty string clears it. Every problem is reported in one
 * {@code 400 VALIDATION_FAILED}. {@code enabled} with no {@code host} is refused.
 */
@Service
public class SmtpSettingsService {

    /** The members of {@code SmtpSettingsInput}. */
    static final Set<String> FIELDS = Set.of("enabled", "host", "port", "security", "username", "password",
            "fromAddress", "fromName", "secretsSet");

    /** The accepted {@code security} values. */
    static final List<String> SECURITY = List.of("none", "starttls", "tls");

    private final AppSettingService settings;
    private final CredentialCipher cipher;

    /**
     * Creates the service.
     *
     * @param settings the settings store
     * @param cipher the credential cipher
     */
    public SmtpSettingsService(AppSettingService settings, CredentialCipher cipher) {
        this.settings = settings;
        this.cipher = cipher;
    }

    /**
     * The stored settings, or the defaults (off, port 587, STARTTLS, from {@value SmtpSettings#DEFAULT_FROM_ADDRESS}).
     *
     * @return the settings
     */
    public SmtpSettings get() {
        return settings.getOrDefault(SettingKeys.SMTP, SmtpSettings.class, SmtpSettings::defaults);
    }

    /**
     * Validates and stores an {@code SmtpSettingsInput} body.
     *
     * @param body the body
     * @return the stored settings
     * @throws ValidationException listing every problem
     */
    public SmtpSettings update(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be an SmtpSettingsInput object.");
        }
        SmtpSettings current = get();
        List<FieldError> errors = new ArrayList<>();
        for (String field : body.propertyNames()) {
            if (!FIELDS.contains(field)) {
                errors.add(new FieldError(field, "is not an SmtpSettingsInput member"));
            }
        }
        Boolean enabled = null;
        JsonNode enabledNode = body.get("enabled");
        if (enabledNode == null || !enabledNode.isBoolean()) {
            errors.add(new FieldError("enabled", "is required and must be a boolean"));
        } else {
            enabled = enabledNode.asBoolean();
        }
        String host = text(body, "host", 253, errors);
        Integer port = null;
        JsonNode portNode = body.get("port");
        if (portNode != null && !portNode.isNull()) {
            if (!portNode.isIntegralNumber() || portNode.asInt() < 1 || portNode.asInt() > 65_535) {
                errors.add(new FieldError("port", "must be a port number from 1 to 65535"));
            } else {
                port = portNode.asInt();
            }
        }
        String security = text(body, "security", 16, errors);
        if (security != null) {
            security = security.toLowerCase(Locale.ROOT);
            if (!SECURITY.contains(security)) {
                errors.add(new FieldError("security", "must be none, starttls or tls"));
            }
        }
        String username = text(body, "username", 320, errors);
        String fromAddress = text(body, "fromAddress", 320, errors);
        if (fromAddress != null && !isEmailAddress(fromAddress)) {
            errors.add(new FieldError("fromAddress", "is not an email address"));
        }
        String fromName = text(body, "fromName", 200, errors);
        if (Boolean.TRUE.equals(enabled) && host == null) {
            errors.add(new FieldError("host", "is required while enabled"));
        }
        String passwordCipher = current.passwordCipher();
        String passwordNonce = current.passwordNonce();
        if (body.has("password")) {
            JsonNode password = body.get("password");
            if (password.isNull() || (password.isString() && password.asString().isEmpty())) {
                passwordCipher = null;
                passwordNonce = null;
            } else if (!password.isString()) {
                errors.add(new FieldError("password", "must be a string"));
            } else {
                Encrypted sealed = cipher.encrypt(password.asString());
                passwordCipher = Base64.getEncoder().encodeToString(sealed.ciphertext());
                passwordNonce = Base64.getEncoder().encodeToString(sealed.nonce());
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The SMTP settings are not valid.", errors);
        }
        SmtpSettings updated = new SmtpSettings(enabled, host, port == null ? SmtpSettings.DEFAULT_PORT : port,
                security == null ? SmtpSettings.DEFAULT_SECURITY : security, username, passwordCipher, passwordNonce,
                fromAddress == null ? SmtpSettings.DEFAULT_FROM_ADDRESS : fromAddress,
                fromName == null ? SmtpSettings.DEFAULT_FROM_NAME : fromName);
        settings.put(SettingKeys.SMTP, updated);
        return updated;
    }

    /**
     * The {@code SmtpSettings} view: everything but the password, with {@code secretsSet}.
     *
     * @param value the settings
     * @return the view
     */
    public Map<String, Object> view(SmtpSettings value) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("enabled", value.enabled());
        out.put("host", value.host());
        out.put("port", value.effectivePort());
        out.put("security", value.effectiveSecurity());
        out.put("username", value.username());
        out.put("fromAddress", value.effectiveFromAddress());
        out.put("fromName", value.effectiveFromName());
        out.put("secretsSet", value.hasPassword() ? List.of("password") : List.of());
        return out;
    }

    /**
     * The decrypted password, for the sender only; never logged or returned.
     *
     * @param value the settings
     * @return the password, or null when none is stored
     */
    public String password(SmtpSettings value) {
        if (!value.hasPassword()) {
            return null;
        }
        return cipher.decryptToString(new Encrypted(Base64.getDecoder().decode(value.passwordCipher()),
                Base64.getDecoder().decode(value.passwordNonce())));
    }

    /**
     * True when {@code value} parses as one bare email address.
     *
     * @param value the text
     * @return true for an address
     */
    public static boolean isEmailAddress(String value) {
        try {
            InternetAddress parsed = new InternetAddress(value, true);
            return value.equals(parsed.getAddress()) && value.indexOf('@') > 0;
        } catch (AddressException e) {
            return false;
        }
    }

    private static String text(JsonNode body, String field, int max, List<FieldError> errors) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isString()) {
            errors.add(new FieldError(field, "must be a string"));
            return null;
        }
        String value = node.asString().strip();
        if (value.isEmpty()) {
            return null;
        }
        if (value.length() > max) {
            errors.add(new FieldError(field, "is longer than " + max + " characters"));
            return null;
        }
        return value;
    }
}
