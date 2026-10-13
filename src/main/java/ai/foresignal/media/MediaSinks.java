package ai.foresignal.media;

import ai.foresignal.collect.MediaSinkFactory;
import ai.foresignal.source.spi.MediaInput;
import ai.foresignal.source.spi.MediaRef;
import ai.foresignal.source.spi.MediaSink;
import ai.foresignal.source.spi.SourceDescriptor;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * The sinks behind {@code ctx.media()} (prompt 043): each fills in the context's source id and
 * delegates to {@link MediaStore} and {@link ThumbnailService}. A probe gets a sink that runs the
 * guard and hashes the content but writes nothing.
 */
@Component
public class MediaSinks implements MediaSinkFactory {

    private final MediaStore store;
    private final ThumbnailService thumbnails;
    private final ImageGuard guard;
    private final GuardedImages images;

    /** Creates the factory. */
    public MediaSinks(MediaStore store, ThumbnailService thumbnails, ImageGuard guard, GuardedImages images) {
        this.store = store;
        this.thumbnails = thumbnails;
        this.guard = guard;
        this.images = images;
    }

    @Override
    public MediaSink forSource(SourceDescriptor descriptor, boolean probing) {
        String sourceId = descriptor.id();
        return probing ? new ProbingSink(guard, images) : new StoringSink(sourceId, store, thumbnails, images);
    }

    /** Stores through the media store with the source id filled in. */
    private record StoringSink(String sourceId, MediaStore store, ThumbnailService thumbnails, GuardedImages images)
            implements MediaSink {

        @Override
        public BufferedImage decode(byte[] bytes, String contentType) {
            return images.decode(bytes, contentType);
        }

        @Override
        public MediaRef store(MediaInput input) {
            return store.store(input.withSourceId(sourceId));
        }

        @Override
        public List<MediaRef> thumbnails(MediaRef original) {
            return thumbnails.thumbnails(original);
        }
    }

    /** Inspects and hashes, writes nothing, and returns a reference with a null id. */
    private record ProbingSink(ImageGuard guard, GuardedImages images) implements MediaSink {

        @Override
        public BufferedImage decode(byte[] bytes, String contentType) {
            return images.decode(bytes, contentType);
        }

        @Override
        public MediaRef store(MediaInput input) {
            ImageInfo info = input.path() != null
                    ? guard.inspect(input.path(), input.contentType(), input.allowMultiImage())
                    : guard.inspect(input.bytes(), input.contentType(), input.allowMultiImage());
            try (InputStream in = input.open()) {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] buffer = new byte[64 * 1024];
                long total = 0;
                int n;
                while ((n = in.read(buffer)) > 0) {
                    digest.update(buffer, 0, n);
                    total += n;
                }
                return new MediaRef(null, HexFormat.of().formatHex(digest.digest()), info.format().contentType(),
                        info.width(), info.height(), total);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("SHA-256 is unavailable", e);
            }
        }

        @Override
        public List<MediaRef> thumbnails(MediaRef original) {
            return List.of();
        }

        @Override
        public boolean probing() {
            return true;
        }
    }
}
