package ai.foresignal.raster;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The native-grid frame store (Weather-Imagery-Design §2.3, §2.4).
 *
 * <p>A frame is written to {@code rasters/tmp/}, forced to disk, then renamed atomically into
 * {@code rasters/frames/{productId}/{yyyy}/{mm}/{dd}/{frameId}.fsg}, so a reader never sees a
 * half-written file and a failure leaves no partial file behind. Its SHA-256 is computed over the
 * <em>decoded</em> content, so it is the same whatever the codec; its first 8 hex characters are the
 * {@code sha8} of the frame key.
 *
 * <p>Reads go through a Caffeine {@code frameCache} weighted by decoded bytes and bounded by the
 * profile's {@code frameCacheMb}; rewriting or deleting a frame invalidates its entry.
 */
@Component
public class FrameStore {

    /** The name of the frame cache in {@code /v1/system}. */
    public static final String CACHE_NAME = "frame";

    private static final HexFormat HEX = HexFormat.of();

    private final RasterPaths paths;
    private final Cache<String, ElgCodec.Decoded> frameCache;
    private final long maxBytes;

    /** Creates the store with the configured cache bound and registers the cache's metrics. */
    @Autowired
    public FrameStore(RasterPaths paths, RasterSettings settings, RasterCacheRegistry caches) {
        this(paths, settings.frameCacheBytes());
        caches.register(CACHE_NAME, frameCache, maxBytes);
    }

    /** Creates a store with a cache of {@code maxCacheBytes} (tests). */
    public FrameStore(RasterPaths paths, long maxCacheBytes) {
        this.paths = paths;
        this.maxBytes = maxCacheBytes;
        this.frameCache = Caffeine.newBuilder()
                .maximumWeight(maxCacheBytes)
                .weigher((String key, ElgCodec.Decoded value) -> weight(value))
                .recordStats()
                .build();
    }

    /** The weight of a cached frame: its decoded payload plus the header. */
    static int weight(ElgCodec.Decoded value) {
        long w = (long) value.payload().length + ElgCodec.HEADER_BYTES;
        return (int) Math.min(Integer.MAX_VALUE, w);
    }

    /**
     * A frame as stored.
     *
     * @param relativePath the path relative to {@code rasters/}
     * @param bytes the file's size on disk
     * @param sha256 hex SHA-256 of the decoded content
     */
    public record StoredFrame(String relativePath, long bytes, String sha256) {}

    /**
     * Writes a frame atomically.
     *
     * @param productId the product
     * @param validTime the valid time, which dates the directory
     * @param frameId the frame id, which names the file
     * @param header the header (its codec decides the compression)
     * @param decoded the decoded payload
     * @return where and how big
     * @throws UncheckedIOException when the write or the rename fails; no file is left behind
     */
    public StoredFrame write(String productId, Instant validTime, String frameId, FsgHeader header, byte[] decoded) {
        byte[] file = ElgCodec.encode(header, decoded);
        String relative = paths.relativeFramePath(productId, validTime, frameId);
        Path target = paths.resolve(relative);
        Path staged = null;
        try {
            Files.createDirectories(paths.tmp());
            staged = Files.createTempFile(paths.tmp(), productId + "-", ".fsg.part");
            try (FileChannel channel = FileChannel.open(staged, StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                ByteBuffer buffer = ByteBuffer.wrap(file);
                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }
                channel.force(true);
            }
            Files.createDirectories(target.getParent());
            Files.move(staged, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            staged = null;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not store frame " + productId + "/" + frameId, e);
        } finally {
            if (staged != null) {
                try {
                    Files.deleteIfExists(staged);
                } catch (IOException ignored) {
                    // The orphan sweep removes leftovers in rasters/tmp.
                }
            }
        }
        frameCache.invalidate(relative);
        return new StoredFrame(relative, file.length, sha256Hex(decoded));
    }

    /**
     * Reads a frame through the cache.
     *
     * @param relativePath the stored relative path
     * @throws UncheckedIOException when the file cannot be read
     * @throws RasterFormatException when the file is not a valid frame
     */
    public ElgCodec.Decoded read(String relativePath) {
        return frameCache.get(relativePath, key -> {
            try {
                return ElgCodec.decode(Files.readAllBytes(paths.resolve(key)));
            } catch (IOException e) {
                throw new UncheckedIOException("Could not read frame " + key, e);
            }
        });
    }

    /** True when the frame's file exists. */
    public boolean exists(String relativePath) {
        return Files.isRegularFile(paths.resolve(relativePath));
    }

    /**
     * Deletes a frame's file and its cache entry.
     *
     * @return true when a file was deleted
     */
    public boolean delete(String relativePath) {
        frameCache.invalidate(relativePath);
        try {
            return Files.deleteIfExists(paths.resolve(relativePath));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not delete frame " + relativePath, e);
        }
    }

    /** The layout. */
    public RasterPaths paths() {
        return paths;
    }

    /** The frame cache (tests and metrics). */
    Cache<String, ElgCodec.Decoded> cache() {
        return frameCache;
    }

    /** The cache bound, bytes. */
    public long maxCacheBytes() {
        return maxBytes;
    }

    /** Hex SHA-256 of {@code data}. */
    public static String sha256Hex(byte[] data) {
        try {
            return HEX.formatHex(MessageDigest.getInstance("SHA-256").digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
