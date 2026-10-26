package ai.foresignal.export;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The shared half of the object-storage archive tiers (prompt R2-016): offload is an {@link S3Uploader} upload whose
 * ETag must match the file's MD5; verify is a {@code HeadObject} that must report the recorded ETag and size; restore
 * downloads the object and checks its SHA-256. The S3 credentials are decrypted from the settings
 * ({@link ArchiveSettingsService#secrets}) for each call and never kept.
 */
abstract class ObjectStorageArchiveTier implements ArchiveTier {

    /** The uploader. */
    protected final S3Uploader s3;

    /** The settings, for the secrets. */
    protected final ArchiveSettingsService settingsService;

    /** Creates the tier. */
    protected ObjectStorageArchiveTier(S3Uploader s3, ArchiveSettingsService settingsService) {
        this.s3 = s3;
        this.settingsService = settingsService;
    }

    /** The bucket settings of an upload; a tier may set the storage class. */
    protected ArchiveSettings.S3 bucket(ArchiveSettings settings) {
        if (settings.s3() == null) {
            throw new IllegalStateException("The archive target " + target() + " has no s3 settings");
        }
        return settings.s3();
    }

    /** The decrypted S3 secrets. */
    protected Map<String, String> secrets(ArchiveSettings settings) {
        Map<String, String> secrets = settingsService.secrets(settings);
        if (secrets == null) {
            throw new IllegalStateException("The S3 access key and secret key are not set");
        }
        return secrets;
    }

    @Override
    public ArchiveRef offload(ArchiveSettings settings, ArchiveSpec spec) {
        ArchiveSettings.S3 bucket = bucket(settings);
        S3Uploader.Result result = s3.upload(bucket, secrets(settings), spec.key(), spec.file());
        if (!result.verified()) {
            throw new IllegalStateException("The upload of " + spec.key() + " returned ETag " + result.etag()
                    + ", which does not match the file");
        }
        ArchiveRef ref = new ArchiveRef(target(), spec.key(), "s3://" + bucket.bucket() + "/" + result.key(),
                spec.sha256(), spec.bytes(), result.etag());
        if (!verify(settings, ref)) {
            throw new IllegalStateException("The stored object " + result.key() + " does not match the upload");
        }
        return ref;
    }

    @Override
    public boolean verify(ArchiveSettings settings, ArchiveRef ref) {
        S3Uploader.Head head = s3.head(bucket(settings), secrets(settings), ref.key());
        return head.found() && head.length() == ref.bytes() && ref.etag() != null
                && ref.etag().equalsIgnoreCase(head.etag());
    }

    @Override
    public Restored restore(ArchiveSettings settings, ArchiveRef ref, Path into) {
        s3.download(bucket(settings), secrets(settings), ref.key(), into);
        return checked(ref, into);
    }

    /** {@code into} when its SHA-256 matches {@code ref}; else it is deleted and this throws. */
    protected static Restored checked(ArchiveRef ref, Path into) {
        try {
            if (ref.sha256() == null || !ref.sha256().equalsIgnoreCase(ExportManifests.sha256(into))) {
                Files.deleteIfExists(into);
                throw new IllegalStateException("The downloaded copy of " + ref.key() + " does not match its SHA-256");
            }
            return Restored.ready(into);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not check the downloaded copy of " + ref.key(), e);
        }
    }

    @Override
    public List<ArchiveRef> list(ArchiveSettings settings, String keyPrefix) {
        ArchiveSettings.S3 bucket = bucket(settings);
        String prefix = bucket.prefix() == null || bucket.prefix().isBlank() ? ""
                : bucket.prefix().replaceAll("^/+|/+$", "") + "/";
        List<ArchiveRef> out = new ArrayList<>();
        for (S3Uploader.Listed listed : s3.list(bucket, secrets(settings), keyPrefix)) {
            String key = listed.key().startsWith(prefix) ? listed.key().substring(prefix.length()) : listed.key();
            out.add(new ArchiveRef(target(), key, "s3://" + bucket.bucket() + "/" + listed.key(), null, listed.size(),
                    listed.etag()));
        }
        return out;
    }

    @Override
    public void delete(ArchiveSettings settings, ArchiveRef ref) {
        s3.delete(bucket(settings), secrets(settings), ref.key());
    }
}
