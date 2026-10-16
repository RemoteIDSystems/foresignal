package ai.foresignal.export;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * The cold-archive settings ({@code ArchiveSettings}, prompt 111), stored under
 * {@code app_setting['archive']}. The S3 secrets are held as ciphertext (base64) in the record and
 * are never returned by the API; {@link ArchiveSettingsService#view} exposes only their names.
 *
 * @param enabled whether the archive runs
 * @param sources the source allowlist; empty means every non-private STANDARD and TRACK source
 * @param root the local root; empty means {@code ${FORESIGNAL_DATA_DIR}/archive}
 * @param target {@code local} or {@code s3}
 * @param s3 the S3 target, when {@code target=s3}
 * @param accessKeyCipher base64 ciphertext of the S3 access key (write-only)
 * @param accessKeyNonce base64 nonce of the S3 access key
 * @param secretKeyCipher base64 ciphertext of the S3 secret key (write-only)
 * @param secretKeyNonce base64 nonce of the S3 secret key
 * @param keepLocal keep the local copy after an upload
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ArchiveSettings(boolean enabled, List<String> sources, String root, String target, S3 s3,
        String accessKeyCipher, String accessKeyNonce, String secretKeyCipher, String secretKeyNonce,
        boolean keepLocal) {

    /** The S3 target. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record S3(String endpoint, String bucket, String prefix, String region, boolean pathStyle) {

        /** Fills defaults. */
        public S3 {
            prefix = prefix == null ? "" : prefix;
            region = region == null || region.isBlank() ? "us-east-1" : region;
        }
    }

    /** Fills defaults. */
    public ArchiveSettings {
        sources = sources == null ? List.of() : List.copyOf(sources);
        root = root == null ? "" : root;
        target = target == null || target.isBlank() ? "local" : target.toLowerCase(java.util.Locale.ROOT);
    }

    /** The shipped defaults: off, local, keep local. */
    public static ArchiveSettings defaults() {
        return new ArchiveSettings(false, List.of(), "", "local", null, null, null, null, null, true);
    }

    /** True when S3 secrets are set. */
    public boolean hasSecrets() {
        return accessKeyCipher != null && secretKeyCipher != null;
    }
}
