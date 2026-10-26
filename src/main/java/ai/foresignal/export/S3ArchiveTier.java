package ai.foresignal.export;

import org.springframework.stereotype.Component;

/**
 * The S3 archive tier (prompt R2-016, target {@code s3}): AWS S3 or an S3-compatible store such as MinIO, with the
 * bucket's storage class unless {@code s3.storageClass} names one that can be read at once.
 */
@Component
public class S3ArchiveTier extends ObjectStorageArchiveTier {

    /** Creates the tier. */
    public S3ArchiveTier(S3Uploader s3, ArchiveSettingsService settingsService) {
        super(s3, settingsService);
    }

    @Override
    public String target() {
        return ArchiveSettings.S3_TARGET;
    }
}
