package ai.foresignal.source.spi;

import java.util.List;

/**
 * The media store as an adapter sees it: {@code ctx.media()} (Weather-Imagery-Design §2.1, prompt
 * 043).
 *
 * <p>Adapters never write image files themselves. They hand each image to {@link #store}, which
 * streams it to staging, hashes it, runs {@code ImageGuard} before any pixel is decoded, and moves it
 * to its content-addressed place. The sink fills in the source id.
 *
 * <p>During a probe the sink is {@linkplain #probing() probing}: it inspects the image and returns a
 * preview reference with a null id, and writes nothing.
 */
public interface MediaSink {

    /**
     * Stores one image.
     *
     * @param input the image; its {@code sourceId} is replaced with the context's source
     * @return the stored reference
     * @throws ImageRejectedException when {@code ImageGuard} refuses the image; nothing is stored
     * @throws RuntimeException {@code 413 MEDIA_TOO_LARGE} when the content exceeds
     *     {@code foresignal.media.max-file-bytes}; {@link java.io.UncheckedIOException} on an I/O error
     */
    MediaRef store(MediaInput input);

    /**
     * The 256 px and 1024 px renditions of {@code original} (Weather-Imagery-Design §4.6), created
     * when missing. A probing sink returns an empty list.
     */
    List<MediaRef> thumbnails(MediaRef original);

    /** True when this sink writes nothing (a probe). */
    default boolean probing() {
        return false;
    }
}
