/**
 * The content-addressed media store (prompt 043, Weather-Imagery-Design §2.3, §2.4, §4.6, §6.6):
 * images Foresignal derives or receives, such as SatDump composites and their thumbnails, static maps
 * and notification attachments.
 *
 * <ul>
 *   <li>{@link ai.foresignal.media.ImageGuard} inspects every PNG, JPEG and TIFF from its header
 *       before any pixel is decoded; {@link ai.foresignal.media.GuardedImages} is the only decode
 *       entry point.
 *   <li>{@link ai.foresignal.media.MediaStore} stages, hashes, guards and atomically renames files to
 *       {@code media/{ab}/{sha256}.{ext}}, one row per store.
 *   <li>{@link ai.foresignal.media.ThumbnailService} renders the 256 px and 1024 px variants.
 *   <li>{@link ai.foresignal.media.MediaRetentionStep} makes media follow the lifetime of the record
 *       that owns it and sweeps orphans.
 * </ul>
 *
 * <p>Prompt 133 (news) also uses the {@code media} name in Architecture §3; see the 042 Completion
 * Report, issue 3.
 */
package ai.foresignal.media;
