package ai.foresignal.media;

import ai.foresignal.source.spi.MediaInput;
import ai.foresignal.source.spi.MediaRef;
import ai.foresignal.source.spi.MediaRole;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/**
 * Thumbnails (prompt 043, Weather-Imagery-Design §4.6): a {@value #SMALL} px and a {@value #LARGE} px
 * long-edge rendition of an original, stored as {@code role=thumbnail} rows with {@code variant_of}
 * and {@code variant_width}.
 *
 * <ul>
 *   <li>Rendering follows {@link ThumbnailRenderer}: progressive bilinear halving, one bicubic step,
 *       JPEG at quality 0.85 or PNG when the source has alpha.
 *   <li>An original already at most the target in a browser format (PNG or JPEG) is not upscaled: the
 *       variant row links the original's file instead. A small TIFF is re-encoded at its own size,
 *       since browsers cannot show TIFF.
 *   <li>{@link #thumbnail} is single-flight: concurrent requests for one variant render it once.
 * </ul>
 */
@Component
public class ThumbnailService {

    /** The small thumbnail's long edge. */
    public static final int SMALL = 256;

    /** The large thumbnail's long edge. */
    public static final int LARGE = 1024;

    /** The widths offered. */
    public static final List<Integer> WIDTHS = List.of(SMALL, LARGE);

    private final MediaStore store;
    private final MediaQueries queries;
    private final GuardedImages images;
    private final ConcurrentMap<String, CompletableFuture<MediaRow>> flights = new ConcurrentHashMap<>();
    private final AtomicLong renders = new AtomicLong();

    /** Creates the service. */
    public ThumbnailService(MediaStore store, MediaQueries queries, GuardedImages images) {
        this.store = store;
        this.queries = queries;
        this.images = images;
    }

    /** Both thumbnails of {@code original}, created when missing; smallest first. */
    public List<MediaRef> thumbnails(MediaRef original) {
        List<MediaRef> out = new ArrayList<>();
        for (int width : WIDTHS) {
            out.add(thumbnail(original.id(), width).toRef());
        }
        return out;
    }

    /**
     * The variant of {@code originalId} at {@code width}: the stored one, else rendered (once, however
     * many callers ask at the same time) and stored.
     *
     * @throws IllegalArgumentException for a width other than {@value #SMALL} or {@value #LARGE}
     * @throws ai.foresignal.api.error.ForesignalException {@code 404 UNKNOWN_MEDIA} or
     *     {@code 410 MEDIA_EXPIRED} when the original is gone
     */
    public MediaRow thumbnail(String originalId, int width) {
        if (!WIDTHS.contains(width)) {
            throw new IllegalArgumentException("Thumbnail width must be one of " + WIDTHS + ", not " + width);
        }
        Optional<MediaRow> existing = queries.findVariant(originalId, width);
        if (existing.isPresent()) {
            return existing.get();
        }
        String key = originalId + "@" + width;
        CompletableFuture<MediaRow> mine = new CompletableFuture<>();
        CompletableFuture<MediaRow> running = flights.putIfAbsent(key, mine);
        if (running != null) {
            return await(running);
        }
        try {
            MediaRow row = queries.findVariant(originalId, width).orElseGet(() -> create(originalId, width));
            mine.complete(row);
            return row;
        } catch (RuntimeException e) {
            mine.completeExceptionally(e);
            throw e;
        } finally {
            flights.remove(key, mine);
        }
    }

    /** How many variants this service has created (rendered or linked) since startup; for tests. */
    public long renders() {
        return renders.get();
    }

    private MediaRow create(String originalId, int width) {
        MediaRow original = queries.find(originalId).orElseThrow(() -> queries.tombstoned(originalId)
                ? MediaErrors.expired(originalId) : MediaErrors.unknown(originalId));
        renders.incrementAndGet();
        ImageFormat format = ImageFormat.ofContentType(original.contentType()).orElse(ImageFormat.TIFF);
        if (!needsRendering(format, original.width(), original.height(), width)) {
            return store.link(original, width);
        }
        BufferedImage decoded = images.decode(store.file(original), original.contentType());
        ThumbnailRenderer.Rendition rendition = ThumbnailRenderer.render(decoded, width);
        MediaInput input = MediaInput.of(rendition.bytes(), rendition.contentType(), MediaRole.THUMBNAIL)
                .withSourceId(original.sourceId())
                .withLicence(original.licence())
                .withExpiresAt(original.expiresAt());
        if (original.recordSourceId() != null) {
            input = input.withRecord(original.recordSourceId(), original.recordExternalId());
        }
        return store.store(input, original.id(), width);
    }

    /**
     * True when a variant at {@code target} must be rendered; false when the original's file can be
     * linked: it is in a browser format and its long edge is at most the target (no upscaling).
     */
    static boolean needsRendering(ImageFormat format, int width, int height, int target) {
        return !format.browserSafe() || Math.max(width, height) > target;
    }

    private static MediaRow await(CompletableFuture<MediaRow> flight) {
        try {
            return flight.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for a thumbnail", e);
        } catch (ExecutionException | CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new IllegalStateException("Thumbnail rendering failed", cause);
        }
    }
}
