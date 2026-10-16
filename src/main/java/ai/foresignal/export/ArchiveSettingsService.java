package ai.foresignal.export;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
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
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/**
 * Reads and writes the {@code archive} settings (prompt 111). The S3 secrets are encrypted with
 * {@link CredentialCipher} and stored as ciphertext; the API view lists only {@code secretsSet}.
 */
@Service
public class ArchiveSettingsService {

    private final AppSettingService settings;
    private final CredentialCipher cipher;

    /** Creates the service. */
    public ArchiveSettingsService(AppSettingService settings, CredentialCipher cipher) {
        this.settings = settings;
        this.cipher = cipher;
    }

    /** The settings, or the defaults. */
    public ArchiveSettings get() {
        return settings.getOrDefault(SettingKeys.ARCHIVE, ArchiveSettings.class, ArchiveSettings::defaults);
    }

    /** Updates the settings from a body; absent members keep their value. */
    @Transactional
    public ArchiveSettings update(JsonNode body) {
        ArchiveSettings current = get();
        boolean enabled = body.has("enabled") ? body.get("enabled").asBoolean() : current.enabled();
        List<String> sources = body.has("sources") ? strings(body.get("sources")) : current.sources();
        String root = body.hasNonNull("root") ? body.get("root").asString() : current.root();
        String target = body.hasNonNull("target") ? body.get("target").asString() : current.target();
        if (!"local".equals(target) && !"s3".equals(target)) {
            throw ValidationException.forField("target", "must be local or s3");
        }
        ArchiveSettings.S3 s3 = current.s3();
        if (body.has("s3") && body.get("s3").isObject()) {
            JsonNode node = body.get("s3");
            s3 = new ArchiveSettings.S3(
                    node.hasNonNull("endpoint") ? node.get("endpoint").asString() : s3 == null ? null : s3.endpoint(),
                    node.hasNonNull("bucket") ? node.get("bucket").asString() : s3 == null ? null : s3.bucket(),
                    node.hasNonNull("prefix") ? node.get("prefix").asString() : s3 == null ? "" : s3.prefix(),
                    node.hasNonNull("region") ? node.get("region").asString() : s3 == null ? null : s3.region(),
                    node.has("pathStyle") ? node.get("pathStyle").asBoolean() : s3 != null && s3.pathStyle());
        }
        boolean keepLocal = body.has("keepLocal") ? body.get("keepLocal").asBoolean() : current.keepLocal();
        String accessCipher = current.accessKeyCipher();
        String accessNonce = current.accessKeyNonce();
        String secretCipher = current.secretKeyCipher();
        String secretNonce = current.secretKeyNonce();
        JsonNode secrets = body.get("secrets");
        if (secrets != null && secrets.isObject()) {
            if (secrets.hasNonNull("accessKey")) {
                Encrypted encrypted = cipher.encrypt(secrets.get("accessKey").asString());
                accessCipher = Base64.getEncoder().encodeToString(encrypted.ciphertext());
                accessNonce = Base64.getEncoder().encodeToString(encrypted.nonce());
            }
            if (secrets.hasNonNull("secretKey")) {
                Encrypted encrypted = cipher.encrypt(secrets.get("secretKey").asString());
                secretCipher = Base64.getEncoder().encodeToString(encrypted.ciphertext());
                secretNonce = Base64.getEncoder().encodeToString(encrypted.nonce());
            }
        }
        if ("s3".equals(target) && (s3 == null || s3.endpoint() == null || s3.bucket() == null)) {
            throw ValidationException.forField("s3", "endpoint and bucket are required when target=s3");
        }
        ArchiveSettings updated = new ArchiveSettings(enabled, sources, root, target, s3, accessCipher, accessNonce,
                secretCipher, secretNonce, keepLocal);
        settings.put(SettingKeys.ARCHIVE, updated);
        return updated;
    }

    /** The API view, without the secret ciphertext. */
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

    /** The decrypted S3 secrets, or null when unset. */
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

    /** {@code 400} when the archive target is misconfigured; used by the step. */
    public void requireValid(ArchiveSettings value) {
        if (!value.enabled()) {
            return;
        }
        if ("s3".equals(value.target()) && (value.s3() == null || value.s3().endpoint() == null
                || value.s3().bucket() == null)) {
            throw new ValidationException(ErrorCodes.EXPORT_SPEC_INVALID,
                    "The archive target is s3 but endpoint or bucket is unset.", List.of());
        }
    }

    private static List<String> strings(JsonNode node) {
        if (node == null || node.isNull() || !node.isArray()) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (JsonNode item : node) {
            out.add(item.asString());
        }
        return out;
    }
}
