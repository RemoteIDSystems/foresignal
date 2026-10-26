package ai.foresignal.export;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Set;

/**
 * The cold-archive settings ({@code ArchiveSettings}, prompt 111, extended by prompt R2-016), stored under
 * {@code app_setting['archive']}. The S3 secrets are held as ciphertext (base64) in the record and are never returned
 * by the API; {@link ArchiveSettingsService#view} exposes only their names.
 *
 * @param enabled whether the archive runs
 * @param sources the source allowlist; empty means every non-private STANDARD and TRACK source
 * @param root the local root (a mounted external disk for the {@code local} tier); empty means
 *     {@code ${FORESIGNAL_DATA_DIR}/archive}
 * @param target {@code local} (the external-disk tier: files under {@code root}), {@code s3} or {@code glacier}
 * @param s3 the object-storage target, when {@code target} is {@code s3} or {@code glacier}
 * @param accessKeyCipher base64 ciphertext of the S3 access key (write-only)
 * @param accessKeyNonce base64 nonce of the S3 access key
 * @param secretKeyCipher base64 ciphertext of the S3 secret key (write-only)
 * @param secretKeyNonce base64 nonce of the S3 secret key
 * @param keepLocal keep the local copy after an upload
 * @param cutoff the age after which an archive run (prompt R2-016) moves STANDARD history to the cold tier, as a
 *     duration ({@code 365d}); empty means an archive run takes only what retention would remove anyway
 * @param schedule the cron expression (six fields, Spring's {@code CronExpression}, UTC) of scheduled archive runs;
 *     empty means archive runs only on request
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ArchiveSettings(boolean enabled, List<String> sources, String root, String target, S3 s3,
        String accessKeyCipher, String accessKeyNonce, String secretKeyCipher, String secretKeyNonce,
        boolean keepLocal, String cutoff, String schedule) {

    /** The external-disk tier: files under {@link #root}. */
    public static final String LOCAL = "local";

    /** S3 or an S3-compatible store. */
    public static final String S3_TARGET = "s3";

    /** S3 with an archival storage class, whose objects must be thawed before they can be read. */
    public static final String GLACIER = "glacier";

    /** The targets. */
    public static final Set<String> TARGETS = Set.of(LOCAL, S3_TARGET, GLACIER);

    /**
     * The S3 target.
     *
     * @param endpoint the endpoint URL
     * @param bucket the bucket
     * @param prefix the key prefix
     * @param region the signing region
     * @param pathStyle path-style rather than virtual-hosted addressing
     * @param storageClass the {@code x-amz-storage-class} of uploads (prompt R2-016), or null for the bucket default;
     *     {@code GLACIER} when the target is {@code glacier} and none is set
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record S3(String endpoint, String bucket, String prefix, String region, boolean pathStyle,
            String storageClass) {

        /** Fills defaults. */
        public S3 {
            prefix = prefix == null ? "" : prefix;
            region = region == null || region.isBlank() ? "us-east-1" : region;
            storageClass = storageClass == null || storageClass.isBlank() ? null : storageClass;
        }

        /** The prompt 111 shape, with the bucket's default storage class. */
        public S3(String endpoint, String bucket, String prefix, String region, boolean pathStyle) {
            this(endpoint, bucket, prefix, region, pathStyle, null);
        }
    }

    /** Fills defaults. */
    public ArchiveSettings {
        sources = sources == null ? List.of() : List.copyOf(sources);
        root = root == null ? "" : root;
        target = target == null || target.isBlank() ? LOCAL : target.toLowerCase(java.util.Locale.ROOT);
        cutoff = cutoff == null || cutoff.isBlank() ? null : cutoff;
        schedule = schedule == null || schedule.isBlank() ? null : schedule;
    }

    /** The prompt 111 shape: no archive-run cutoff and no schedule. */
    public ArchiveSettings(boolean enabled, List<String> sources, String root, String target, S3 s3,
            String accessKeyCipher, String accessKeyNonce, String secretKeyCipher, String secretKeyNonce,
            boolean keepLocal) {
        this(enabled, sources, root, target, s3, accessKeyCipher, accessKeyNonce, secretKeyCipher, secretKeyNonce,
                keepLocal, null, null);
    }

    /** The shipped defaults: off, local, keep local. */
    public static ArchiveSettings defaults() {
        return new ArchiveSettings(false, List.of(), "", LOCAL, null, null, null, null, null, true);
    }

    /** True when S3 secrets are set. */
    public boolean hasSecrets() {
        return accessKeyCipher != null && secretKeyCipher != null;
    }

    /** True when the target is object storage ({@code s3} or {@code glacier}). */
    public boolean objectStorage() {
        return S3_TARGET.equals(target) || GLACIER.equals(target);
    }
}
