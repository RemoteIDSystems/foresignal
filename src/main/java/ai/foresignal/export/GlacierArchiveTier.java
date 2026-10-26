package ai.foresignal.export;

import java.nio.file.Path;
import org.springframework.stereotype.Component;

/**
 * The Glacier archive tier (prompt R2-016, target {@code glacier}): the S3 API with an archival storage class,
 * {@code GLACIER} (S3 Glacier Flexible Retrieval) unless {@code s3.storageClass} is {@code DEEP_ARCHIVE}.
 *
 * <p>An archived object cannot be read until S3 thaws it. {@link #restore} therefore asks S3 to thaw it
 * ({@code RestoreObject}, {@value #THAW_DAYS} days at the {@value #THAW_TIER} retrieval tier, which S3 documents as
 * typically 3–5 hours for Flexible Retrieval and 12 hours for Deep Archive) and reports it not ready; the archive
 * runner asks again on its tick and downloads the copy once {@code HeadObject} reports it thawed.
 */
@Component
public class GlacierArchiveTier extends ObjectStorageArchiveTier {

    /** How long S3 keeps a thawed copy. */
    public static final int THAW_DAYS = 7;

    /** The retrieval tier of a thaw. */
    public static final String THAW_TIER = "Standard";

    /** The storage class when none is configured. */
    public static final String DEFAULT_STORAGE_CLASS = "GLACIER";

    /** Creates the tier. */
    public GlacierArchiveTier(S3Uploader s3, ArchiveSettingsService settingsService) {
        super(s3, settingsService);
    }

    @Override
    public String target() {
        return ArchiveSettings.GLACIER;
    }

    @Override
    protected ArchiveSettings.S3 bucket(ArchiveSettings settings) {
        ArchiveSettings.S3 bucket = super.bucket(settings);
        if (bucket.storageClass() != null) {
            return bucket;
        }
        return new ArchiveSettings.S3(bucket.endpoint(), bucket.bucket(), bucket.prefix(), bucket.region(),
                bucket.pathStyle(), DEFAULT_STORAGE_CLASS);
    }

    @Override
    public Restored restore(ArchiveSettings settings, ArchiveRef ref, Path into) {
        ArchiveSettings.S3 bucket = bucket(settings);
        S3Uploader.Head head = s3.head(bucket, secrets(settings), ref.key());
        if (!head.found()) {
            throw new IllegalStateException("The archived object " + ref.key() + " is missing");
        }
        if (head.thawing()) {
            return Restored.pending("S3 is thawing " + ref.key());
        }
        if (head.frozen()) {
            s3.restore(bucket, secrets(settings), ref.key(), THAW_DAYS, THAW_TIER);
            return Restored.pending("Asked S3 to thaw " + ref.key());
        }
        s3.download(bucket, secrets(settings), ref.key(), into);
        return checked(ref, into);
    }
}
