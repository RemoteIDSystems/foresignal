package ai.foresignal.export;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.Encrypted;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

/**
 * Reads and writes the {@code archive} settings (prompt 111) through the settings store. The S3
 * secrets are encrypted with {@link CredentialCipher} (SEC-3) and never returned: the API view lists
 * only the names in {@code secretsSet}. An omitted member keeps its stored value; a secret given as an
 * empty string is cleared. An S3 endpoint must pass the outbound URL policy's {@code LAN_ALLOWED}
 * profile ({@code 400 URL_NOT_ALLOWED}), the profile {@link S3Uploader} sends under.
 */
@Service
public class ArchiveSettingsService {

    private final AppSettingService settings;
    private final CredentialCipher cipher;
    private final OutboundUrlPolicy outbound;

    /** Creates the service. */
    public ArchiveSettingsService(AppSettingService settings, CredentialCipher cipher, OutboundUrlPolicy outbound) {
        this.settings = settings;
        this.cipher = cipher;
        this.outbound = outbound;
    }

    /** The settings, or the shipped defaults. */
    public ArchiveSettings get() {
        return settings.getOrDefault(SettingKeys.ARCHIVE, ArchiveSettings.class, ArchiveSettings::defaults);
    }

    /**
     * Applies an {@code ArchiveSettings} body.
     *
     * @throws ValidationException {@code 400} for a bad member, {@code URL_NOT_ALLOWED} for a refused endpoint
     */
    public ArchiveSettings update(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be an ArchiveSettings object.");
        }
        ArchiveSettings current = get();
        List<FieldError> errors = new ArrayList<>();
        boolean enabled = bool(body, "enabled", current.enabled(), errors);
        List<String> sources = body.has("sources") ? strings(body.get("sources"), errors) : current.sources();
        String root = body.has("root") ? (body.get("root").isNull() ? "" : body.get("root").asString()) : current.root();
        String target = body.hasNonNull("target") ? body.get("target").asString() : current.target();
        if (!"local".equals(target) && !"s3".equals(target)) {
            errors.add(new FieldError("target", "must be local or s3"));
        }
        ArchiveSettings.S3 s3 = current.s3();
        if (body.has("s3")) {
            JsonNode node = body.get("s3");
            if (node.isNull()) {
                s3 = null;
            } else if (!node.isObject()) {
                errors.add(new FieldError("s3", "must be an object"));
            } else {
                s3 = new ArchiveSettings.S3(
                        node.hasNonNull("endpoint") ? node.get("endpoint").asString() : s3 == null ? null : s3.endpoint(),
                        node.hasNonNull("bucket") ? node.get("bucket").asString() : s3 == null ? null : s3.bucket(),
                        node.hasNonNull("prefix") ? node.get("prefix").asString() : s3 == null ? "" : s3.prefix(),
                        node.hasNonNull("region") ? node.get("region").asString() : s3 == null ? null : s3.region(),
                        node.has("pathStyle") ? node.get("pathStyle").asBoolean() : s3 != null && s3.pathStyle());
            }
        }
        boolean keepLocal = bool(body, "keepLocal", current.keepLocal(), errors);
        String accessCipher = current.accessKeyCipher();
        String accessNonce = current.accessKeyNonce();
        String secretCipher = current.secretKeyCipher();
        String secretNonce = current.secretKeyNonce();
        JsonNode secrets = body.get("secrets");
        if (secrets != null && secrets.isObject()) {
            if (secrets.has("accessKey")) {
                String[] sealed = seal(secrets.get("accessKey"));
                accessCipher = sealed[0];
                accessNonce = sealed[1];
            }
            if (secrets.has("secretKey")) {
                String[] sealed = seal(secrets.get("secretKey"));
                secretCipher = sealed[0];
                secretNonce = sealed[1];
            }
        }
        if ("s3".equals(target)) {
            if (s3 == null || s3.endpoint() == null || s3.endpoint().isBlank()) {
                errors.add(new FieldError("s3.endpoint", "is required when target is s3"));
            }
            if (s3 == null || s3.bucket() == null || s3.bucket().isBlank()) {
                errors.add(new FieldError("s3.bucket", "is required when target is s3"));
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The archive settings are not valid.", errors);
        }
        if (s3 != null && s3.endpoint() != null && !s3.endpoint().isBlank()) {
            outbound.requireSavable("s3.endpoint", s3.endpoint(), OutboundUrlPolicy.Profile.LAN_ALLOWED, true);
        }
        ArchiveSettings updated = new ArchiveSettings(enabled, sources, root, target, s3, accessCipher, accessNonce,
                secretCipher, secretNonce, keepLocal);
        settings.put(SettingKeys.ARCHIVE, updated);
        return updated;
    }

    private String[] seal(JsonNode value) {
        if (value == null || value.isNull() || value.asString().isEmpty()) {
            return new String[] {null, null};
        }
        Encrypted encrypted = cipher.encrypt(value.asString());
        return new String[] {Base64.getEncoder().encodeToString(encrypted.ciphertext()),
            Base64.getEncoder().encodeToString(encrypted.nonce())};
    }

    /** The API view ({@code ArchiveSettings}) without any secret. */
    public Map<String, Object> view(ArchiveSettings value) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("enabled", value.enabled());
        out.put("sources", value.sources());
        out.put("root", value.root());
        out.put("target", value.target());
        if (value.s3() != null) {
            Map<String, Object> s3 = new LinkedHashMap<>();
            s3.put("endpoint", value.s3().endpoint());
            s3.put("bucket", value.s3().bucket());
            s3.put("prefix", value.s3().prefix());
            s3.put("region", value.s3().region());
            s3.put("pathStyle", value.s3().pathStyle());
            out.put("s3", s3);
        } else {
            out.put("s3", null);
        }
        List<String> set = new ArrayList<>();
        if (value.accessKeyCipher() != null) {
            set.add("accessKey");
        }
        if (value.secretKeyCipher() != null) {
            set.add("secretKey");
        }
        out.put("secretsSet", set);
        out.put("keepLocal", value.keepLocal());
        return out;
    }

    /** The decrypted S3 secrets ({@code accessKey}, {@code secretKey}), or null when not both are set. */
    public Map<String, String> secrets(ArchiveSettings value) {
        if (!value.hasSecrets()) {
            return null;
        }
        Map<String, String> out = new LinkedHashMap<>();
        out.put("accessKey", cipher.decryptToString(new Encrypted(Base64.getDecoder().decode(value.accessKeyCipher()),
                Base64.getDecoder().decode(value.accessKeyNonce()))));
        out.put("secretKey", cipher.decryptToString(new Encrypted(Base64.getDecoder().decode(value.secretKeyCipher()),
                Base64.getDecoder().decode(value.secretKeyNonce()))));
        return out;
    }

    private static boolean bool(JsonNode body, String field, boolean fallback, List<FieldError> errors) {
        if (!body.has(field) || body.get(field).isNull()) {
            return fallback;
        }
        if (!body.get(field).isBoolean()) {
            errors.add(new FieldError(field, "must be a boolean"));
            return fallback;
        }
        return body.get(field).asBoolean();
    }

    private static List<String> strings(JsonNode node, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return List.of();
        }
        if (!node.isArray()) {
            errors.add(new FieldError("sources", "must be an array of source ids"));
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (JsonNode item : node) {
            out.add(item.asString());
        }
        return out;
    }
}
