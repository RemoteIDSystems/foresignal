package ai.foresignal.backup;

import ai.foresignal.export.ArchiveSettings;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The backup settings ({@code BackupSettings}, prompt R2-017), stored under {@code app_setting['backup']}. The S3
 * secrets are held as ciphertext (base64) and never returned by the API; {@link BackupSettingsService#view} exposes
 * only their names.
 *
 * <p>Backups land through the cold-archive tiers of prompt R2-016 ({@link #tierSettings}): {@code local} and
 * {@code external} use the external-disk tier, {@code s3} the S3 tier, so backup and archive share one target
 * interface.
 *
 * @param enabled whether backups run at all: off, {@code runBackup} is {@code 409 BACKUP_NOT_CONFIGURED} and the
 *     schedule does not fire
 * @param schedule the cron expression (six fields, Spring's {@code CronExpression}, UTC) of scheduled backups, or null
 *     for backups only on request
 * @param target {@code local} (under {@link #root}, default the data directory), {@code external} (a mounted disk at
 *     the absolute {@link #root}) or {@code s3}
 * @param root the backup directory of {@code local} and {@code external}; empty means the data directory for
 *     {@code local}
 * @param s3 the bucket of {@code s3}
 * @param keepBackups how many complete backups of each type to keep; older ones are deleted with their files. Null
 *     keeps every backup
 * @param includeConfig whether a scheduled backup, and a {@code runBackup} that names no type, also writes the config
 *     bundle (type {@code full})
 * @param accessKeyCipher base64 ciphertext of the S3 access key (write-only)
 * @param accessKeyNonce base64 nonce of the S3 access key
 * @param secretKeyCipher base64 ciphertext of the S3 secret key (write-only)
 * @param secretKeyNonce base64 nonce of the S3 secret key
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BackupSettings(boolean enabled, String schedule, String target, String root, ArchiveSettings.S3 s3,
        Integer keepBackups, boolean includeConfig, String accessKeyCipher, String accessKeyNonce,
        String secretKeyCipher, String secretKeyNonce) {

    /** A directory under the data directory, or the configured root. */
    public static final String LOCAL = "local";

    /** A mounted external disk at the configured (absolute) root. */
    public static final String EXTERNAL = "external";

    /** S3 or an S3-compatible store. */
    public static final String S3_TARGET = "s3";

    /** The targets. */
    public static final List<String> TARGETS = List.of(LOCAL, EXTERNAL, S3_TARGET);

    /** The disk targets, served by the external-disk archive tier. */
    public static final Set<String> DISK_TARGETS = Set.of(LOCAL, EXTERNAL);

    /** The shipped {@code keepBackups}. */
    public static final int DEFAULT_KEEP = 7;

    /** Fills defaults. */
    public BackupSettings {
        schedule = schedule == null || schedule.isBlank() ? null : schedule.strip();
        target = target == null || target.isBlank() ? LOCAL : target.toLowerCase(Locale.ROOT);
        root = root == null ? "" : root.strip();
    }

    /** The shipped defaults: off, no schedule, local, keep 7, with the config bundle. */
    public static BackupSettings defaults() {
        return new BackupSettings(false, null, LOCAL, "", null, DEFAULT_KEEP, true, null, null, null, null);
    }

    /** True when both S3 secrets are set. */
    public boolean hasSecrets() {
        return accessKeyCipher != null && secretKeyCipher != null;
    }

    /** True when the target is a directory ({@code local} or {@code external}). */
    public boolean disk() {
        return DISK_TARGETS.contains(target);
    }

    /**
     * These settings as the {@link ArchiveSettings} an archive tier takes: {@code local} and {@code external} become
     * the external-disk tier at {@code diskRoot}, {@code s3} the S3 tier with this bucket and these secrets.
     *
     * @param diskRoot the absolute directory backups of a disk target live under
     */
    public ArchiveSettings tierSettings(String diskRoot) {
        String tier = disk() ? ArchiveSettings.LOCAL : ArchiveSettings.S3_TARGET;
        return new ArchiveSettings(true, List.of(), disk() ? diskRoot : "", tier, s3, accessKeyCipher, accessKeyNonce,
                secretKeyCipher, secretKeyNonce, true);
    }
}
