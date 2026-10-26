package ai.foresignal.backup;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.export.ArchiveSettings;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.Encrypted;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

/**
 * {@code getBackupSettings} and {@code updateBackupSettings} (prompt R2-017) over the settings store, key
 * {@code backup}.
 *
 * <p>A PUT replaces the settings: an omitted member takes its shipped default, except the secrets, which an omitted
 * member keeps and an empty string or {@code null} clears. Every problem is reported in one 400. The S3 secrets are
 * encrypted with {@link CredentialCipher} and never returned; the view lists only their names in {@code secretsSet}.
 * An S3 endpoint must pass the outbound URL policy's {@code LAN_ALLOWED} profile ({@code 400 URL_NOT_ALLOWED}), the
 * profile the S3 client sends under.
 */
@Service
public class BackupSettingsService {

    /** The largest {@code keepBackups}. */
    static final int MAX_KEEP = 10_000;

    /** The members of {@code BackupSettingsInput}, plus the read-only ones a client may echo back from a GET. */
    static final Set<String> MEMBERS = Set.of("enabled", "schedule", "target", "root", "s3", "keepBackups",
            "includeConfig", "secrets", "secretsSet", "lastRunAt");

    private final AppSettingService settings;
    private final CredentialCipher cipher;
    private final OutboundUrlPolicy outbound;

    /** Creates the service. */
    public BackupSettingsService(AppSettingService settings, CredentialCipher cipher, OutboundUrlPolicy outbound) {
        this.settings = settings;
        this.cipher = cipher;
        this.outbound = outbound;
    }

    /** The settings, or the shipped defaults. */
    public BackupSettings get() {
        return settings.getOrDefault(SettingKeys.BACKUP, BackupSettings.class, BackupSettings::defaults);
    }

    /**
     * Replaces the settings with a {@code BackupSettingsInput} body.
     *
     * @return the stored settings
     * @throws ValidationException {@code 400} listing every bad member, or {@code URL_NOT_ALLOWED} for a refused
     *     endpoint; nothing is stored
     */
    public BackupSettings update(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a BackupSettingsInput object.");
        }
        BackupSettings current = get();
        List<FieldError> errors = new ArrayList<>();
        for (String name : body.propertyNames()) {
            if (!MEMBERS.contains(name)) {
                errors.add(new FieldError(name, "is not a member of BackupSettingsInput"));
            }
        }
        Boolean enabled = requiredBoolean(body, "enabled", errors);
        String target = null;
        JsonNode targetNode = body.get("target");
        if (targetNode == null || targetNode.isNull()) {
            errors.add(new FieldError("target", "is required"));
        } else if (!targetNode.isString() || !BackupSettings.TARGETS.contains(targetNode.asString())) {
            errors.add(new FieldError("target", "must be one of " + String.join(", ", BackupSettings.TARGETS)));
        } else {
            target = targetNode.asString();
        }
        String schedule = text(body, "schedule", errors);
        if (schedule != null && !CronExpression.isValidExpression(schedule)) {
            errors.add(new FieldError("schedule", "must be a six-field cron expression (second minute hour day month"
                    + " weekday), evaluated in UTC"));
        }
        String root = text(body, "root", errors);
        if (BackupSettings.EXTERNAL.equals(target)) {
            if (root == null) {
                errors.add(new FieldError("root", "is required when target is external: the mount point of the disk"));
            } else if (!absolute(root)) {
                errors.add(new FieldError("root", "must be an absolute path when target is external"));
            }
        } else if (root != null && !valid(root)) {
            errors.add(new FieldError("root", "must be a path"));
        }
        ArchiveSettings.S3 s3 = s3(body.get("s3"), errors);
        if (BackupSettings.S3_TARGET.equals(target)) {
            if (s3 == null || s3.endpoint() == null || s3.endpoint().isBlank()) {
                errors.add(new FieldError("s3.endpoint", "is required when target is s3"));
            }
            if (s3 == null || s3.bucket() == null || s3.bucket().isBlank()) {
                errors.add(new FieldError("s3.bucket", "is required when target is s3"));
            }
        }
        Integer keep = BackupSettings.DEFAULT_KEEP;
        if (body.has("keepBackups")) {
            JsonNode node = body.get("keepBackups");
            if (node.isNull()) {
                keep = null;
            } else if (!node.isIntegralNumber() || node.asLong() < 1 || node.asLong() > MAX_KEEP) {
                errors.add(new FieldError("keepBackups", "must be a whole number from 1 to " + MAX_KEEP
                        + ", or null to keep every backup"));
            } else {
                keep = node.asInt();
            }
        }
        boolean includeConfig = true;
        if (body.has("includeConfig") && !body.get("includeConfig").isNull()) {
            if (!body.get("includeConfig").isBoolean()) {
                errors.add(new FieldError("includeConfig", "must be a boolean"));
            } else {
                includeConfig = body.get("includeConfig").asBoolean();
            }
        }
        String[] access = {current.accessKeyCipher(), current.accessKeyNonce()};
        String[] secret = {current.secretKeyCipher(), current.secretKeyNonce()};
        JsonNode secrets = body.get("secrets");
        if (secrets != null && !secrets.isNull()) {
            if (!secrets.isObject()) {
                errors.add(new FieldError("secrets", "must be an object with accessKey and secretKey"));
            } else {
                for (String name : secrets.propertyNames()) {
                    if (!name.equals("accessKey") && !name.equals("secretKey")) {
                        errors.add(new FieldError("secrets." + name, "is not a backup secret"));
                    } else if (!secrets.get(name).isNull() && !secrets.get(name).isString()) {
                        errors.add(new FieldError("secrets." + name, "must be a string"));
                    }
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The backup settings are not valid.", errors);
        }
        if (s3 != null && s3.endpoint() != null && !s3.endpoint().isBlank()) {
            outbound.requireSavable("s3.endpoint", s3.endpoint(), OutboundUrlPolicy.Profile.LAN_ALLOWED, true);
        }
        if (secrets != null && secrets.isObject()) {
            if (secrets.has("accessKey")) {
                access = seal(secrets.get("accessKey"));
            }
            if (secrets.has("secretKey")) {
                secret = seal(secrets.get("secretKey"));
            }
        }
        BackupSettings updated = new BackupSettings(enabled, schedule, target, root, s3, keep, includeConfig,
                access[0], access[1], secret[0], secret[1]);
        settings.put(SettingKeys.BACKUP, updated);
        return updated;
    }

    /**
     * The API view ({@code BackupSettings}) without any secret.
     *
     * @param value the settings
     * @param lastRunAt when the newest backup was started, or null
     */
    public Map<String, Object> view(BackupSettings value, Instant lastRunAt) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("enabled", value.enabled());
        out.put("schedule", value.schedule());
        out.put("target", value.target());
        out.put("root", value.root().isEmpty() ? null : value.root());
        if (value.s3() == null) {
            out.put("s3", null);
        } else {
            Map<String, Object> s3 = new LinkedHashMap<>();
            s3.put("endpoint", value.s3().endpoint());
            s3.put("bucket", value.s3().bucket());
            s3.put("prefix", value.s3().prefix());
            s3.put("region", value.s3().region());
            s3.put("pathStyle", value.s3().pathStyle());
            out.put("s3", s3);
        }
        out.put("keepBackups", value.keepBackups());
        out.put("includeConfig", value.includeConfig());
        List<String> set = new ArrayList<>();
        if (value.accessKeyCipher() != null) {
            set.add("accessKey");
        }
        if (value.secretKeyCipher() != null) {
            set.add("secretKey");
        }
        out.put("secretsSet", set);
        out.put("lastRunAt", lastRunAt);
        return out;
    }

    private String[] seal(JsonNode value) {
        if (value == null || value.isNull() || value.asString().isEmpty()) {
            return new String[] {null, null};
        }
        Encrypted encrypted = cipher.encrypt(value.asString());
        return new String[] {Base64.getEncoder().encodeToString(encrypted.ciphertext()),
            Base64.getEncoder().encodeToString(encrypted.nonce())};
    }

    private static ArchiveSettings.S3 s3(JsonNode node, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isObject()) {
            errors.add(new FieldError("s3", "must be an S3Target object"));
            return null;
        }
        for (String name : List.of("endpoint", "bucket", "prefix", "region")) {
            if (node.has(name) && !node.get(name).isNull() && !node.get(name).isString()) {
                errors.add(new FieldError("s3." + name, "must be a string"));
            }
        }
        if (node.has("pathStyle") && !node.get("pathStyle").isNull() && !node.get("pathStyle").isBoolean()) {
            errors.add(new FieldError("s3.pathStyle", "must be a boolean"));
        }
        return new ArchiveSettings.S3(string(node, "endpoint"), string(node, "bucket"), string(node, "prefix"),
                string(node, "region"), node.has("pathStyle") && node.get("pathStyle").asBoolean(false));
    }

    private static String string(JsonNode node, String name) {
        JsonNode value = node.get(name);
        return value == null || value.isNull() || !value.isString() || value.asString().isBlank() ? null
                : value.asString().strip();
    }

    private static Boolean requiredBoolean(JsonNode body, String name, List<FieldError> errors) {
        JsonNode node = body.get(name);
        if (node == null || node.isNull()) {
            errors.add(new FieldError(name, "is required"));
            return false;
        }
        if (!node.isBoolean()) {
            errors.add(new FieldError(name, "must be a boolean"));
            return false;
        }
        return node.asBoolean();
    }

    private static String text(JsonNode body, String name, List<FieldError> errors) {
        JsonNode node = body.get(name);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isString()) {
            errors.add(new FieldError(name, "must be a string"));
            return null;
        }
        return node.asString().isBlank() ? null : node.asString().strip();
    }

    private static boolean valid(String path) {
        try {
            Path.of(path);
            return true;
        } catch (InvalidPathException e) {
            return false;
        }
    }

    private static boolean absolute(String path) {
        return valid(path) && Path.of(path).isAbsolute();
    }
}
