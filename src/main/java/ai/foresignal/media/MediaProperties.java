package ai.foresignal.media;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

/**
 * The {@code foresignal.media.*} settings (prompt 043).
 *
 * @param maxFileBytes the largest image file accepted, default 25 MB
 *     ({@code FORESIGNAL_MEDIA_MAX_FILE_BYTES}); {@code ImageGuard} refuses larger files and
 *     {@code MediaStore} answers {@code 413 MEDIA_TOO_LARGE} when a stream exceeds it
 * @param orphanGrace how old an orphan file or row must be before the sweep removes it, so a store in
 *     flight is never touched; default 10 minutes
 * @param headMissingGrace how old a record-owned row must be before it is deleted because its record's
 *     head no longer exists; default 24 hours
 * @param tombstoneRetention how long a deleted id keeps answering {@code 410 MEDIA_EXPIRED} rather than
 *     {@code 404 UNKNOWN_MEDIA}; default 30 days
 */
@ConfigurationProperties("foresignal.media")
public record MediaProperties(
        DataSize maxFileBytes, Duration orphanGrace, Duration headMissingGrace, Duration tombstoneRetention) {

    /** The default file cap. */
    public static final DataSize DEFAULT_MAX_FILE_BYTES = DataSize.ofMegabytes(25);

    /** The default orphan grace. */
    public static final Duration DEFAULT_ORPHAN_GRACE = Duration.ofMinutes(10);

    /** The default grace before a row whose record head is gone is deleted. */
    public static final Duration DEFAULT_HEAD_MISSING_GRACE = Duration.ofHours(24);

    /** The default tombstone retention. */
    public static final Duration DEFAULT_TOMBSTONE_RETENTION = Duration.ofDays(30);

    /** Applies the defaults to missing or invalid values. */
    public MediaProperties {
        maxFileBytes = maxFileBytes == null || maxFileBytes.toBytes() <= 0 ? DEFAULT_MAX_FILE_BYTES : maxFileBytes;
        orphanGrace = orphanGrace == null || orphanGrace.isNegative() ? DEFAULT_ORPHAN_GRACE : orphanGrace;
        headMissingGrace = headMissingGrace == null || headMissingGrace.isNegative()
                ? DEFAULT_HEAD_MISSING_GRACE : headMissingGrace;
        tombstoneRetention = tombstoneRetention == null || tombstoneRetention.isNegative()
                ? DEFAULT_TOMBSTONE_RETENTION : tombstoneRetention;
    }

    /** Every value at its default. */
    public static MediaProperties defaults() {
        return new MediaProperties(null, null, null, null);
    }
}
