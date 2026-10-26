package ai.foresignal.export;

import java.nio.file.Path;
import java.util.List;

/**
 * Where archive segments live once they leave the hot database (prompt R2-016). The external-disk tier
 * ({@link ExternalDiskArchiveTier}, target {@code local}) needs no cloud account; {@link S3ArchiveTier} and
 * {@link GlacierArchiveTier} put the same files in object storage through {@link S3Uploader}, with no AWS SDK.
 *
 * <p>Every method takes the settings in force, so a tier holds no configuration of its own.
 */
public interface ArchiveTier {

    /** The {@code target} this tier serves. */
    String target();

    /**
     * Stores {@code spec}'s file and checks the stored copy.
     *
     * @return the stored copy
     * @throws RuntimeException when the copy cannot be written or does not match
     */
    ArchiveRef offload(ArchiveSettings settings, ArchiveSpec spec);

    /**
     * Checks that the stored copy still matches its checksum (object storage: its ETag and size).
     *
     * @return true when it does
     */
    boolean verify(ArchiveSettings settings, ArchiveRef ref);

    /**
     * Brings the stored copy back to {@code into}, checking its SHA-256.
     *
     * @return ready with the file, or not ready while the tier is still thawing it ({@code glacier})
     * @throws RuntimeException when the copy cannot be read or does not match
     */
    Restored restore(ArchiveSettings settings, ArchiveRef ref, Path into);

    /**
     * The files stored under {@code keyPrefix}; their {@code sha256} is null (listing does not read them).
     *
     * @param keyPrefix a relative key prefix such as {@code segments/}
     */
    List<ArchiveRef> list(ArchiveSettings settings, String keyPrefix);

    /** Deletes the stored copy; deleting a missing one is not an error. */
    void delete(ArchiveSettings settings, ArchiveRef ref);

    /**
     * The outcome of {@link #restore}.
     *
     * @param ready true when {@code file} holds the verified copy
     * @param file the restored file, or null when not ready
     * @param detail why it is not ready, or null
     */
    record Restored(boolean ready, Path file, String detail) {

        /** A verified copy at {@code file}. */
        public static Restored ready(Path file) {
            return new Restored(true, file, null);
        }

        /** Not ready yet. */
        public static Restored pending(String detail) {
            return new Restored(false, null, detail);
        }
    }
}
