package ai.foresignal.raster;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.RasterDtype;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Random;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The frame store on a real file system (prompt 042): the §2.4 layout, atomic writes with no
 * partial file left behind, the decoded-content SHA-256 across codecs, and the byte-weighted frame
 * cache. No Spring context and no database.
 */
class FrameStoreIT {

    static final Instant T = Instant.parse("2026-10-10T14:32:40Z");

    @TempDir Path data;

    RasterPaths paths;
    FrameStore store;

    @BeforeEach
    void setUp() {
        paths = new RasterPaths(data);
        store = new FrameStore(paths, 1024 * 1024);
    }

    static FsgHeader header(int codec) {
        return new FsgHeader(RasterDtype.U16, codec, 16, 8, 1, false, 0.1, 0, 0);
    }

    static byte[] payload(int seed) {
        byte[] p = new byte[16 * 8 * 2];
        new Random(seed).nextBytes(p);
        return p;
    }

    private long stagedFiles() throws IOException {
        try (Stream<Path> files = Files.list(paths.tmp())) {
            return files.count();
        }
    }

    @Test
    @DisplayName("startup creates the §2.4 layout")
    void layout() {
        assertThat(data.resolve("rasters/tmp")).isDirectory();
        assertThat(data.resolve("rasters/frames")).isDirectory();
        assertThat(data.resolve("rasters/fields")).isDirectory();
        assertThat(data.resolve("rasters/glm")).isDirectory();
        assertThat(data.resolve("rasters/satdump")).isDirectory();
        assertThat(data.resolve("media")).isDirectory();
        assertThat(data.resolve("cache/netcdf")).isDirectory();
        assertThat(data.resolve("tmp")).isDirectory();
    }

    @Test
    @DisplayName("a frame lands at frames/{product}/{yyyy}/{mm}/{dd}/{frameId}.fsg and reads back")
    void writeAndRead() throws IOException {
        byte[] decoded = payload(1);
        FrameStore.StoredFrame stored = store.write("p-one", T, "20261010T143240Z", header(1), decoded);
        assertThat(stored.relativePath()).isEqualTo("frames/p-one/2026/10/10/20261010T143240Z.fsg");
        assertThat(paths.resolve(stored.relativePath())).isRegularFile();
        assertThat(Files.size(paths.resolve(stored.relativePath()))).isEqualTo(stored.bytes());
        assertThat(stagedFiles()).isZero();
        assertThat(store.read(stored.relativePath()).payload()).isEqualTo(decoded);
        assertThat(store.exists(stored.relativePath())).isTrue();
        assertThat(store.delete(stored.relativePath())).isTrue();
        assertThat(store.exists(stored.relativePath())).isFalse();
    }

    @Test
    @DisplayName("a failed rename leaves no partial file visible and nothing in staging")
    void atomicWrite() throws IOException {
        String relative = paths.relativeFramePath("p-two", T, "20261010T143240Z");
        Path target = paths.resolve(relative);
        // A non-empty directory where the file should go makes the atomic rename fail.
        Files.createDirectories(target.resolve("blocker"));
        assertThatThrownBy(() -> store.write("p-two", T, "20261010T143240Z", header(1), payload(2)))
                .isInstanceOf(UncheckedIOException.class);
        assertThat(target).isDirectory();
        assertThat(stagedFiles()).isZero();
    }

    @Test
    @DisplayName("the SHA-256 is of the decoded content, so it is the same for both codecs")
    void shaStableAcrossCodecs() {
        byte[] decoded = payload(3);
        FrameStore.StoredFrame plain = store.write("p-three", T, "a", header(FsgHeader.CODEC_NONE), decoded);
        FrameStore.StoredFrame deflated = store.write("p-three", T, "b", header(FsgHeader.CODEC_DEFLATE), decoded);
        assertThat(plain.sha256()).isEqualTo(deflated.sha256()).isEqualTo(FrameStore.sha256Hex(decoded));
        assertThat(plain.bytes()).isNotEqualTo(deflated.bytes());
        assertThat(FrameKeys.sha8(plain.sha256())).hasSize(8);
    }

    @Test
    @DisplayName("the cache weighs decoded bytes plus the header and stays under its bound")
    void cacheWeigher() {
        int frameBytes = 16 * 8 * 2;
        long bound = 2L * (frameBytes + ElgCodec.HEADER_BYTES) + 10;
        FrameStore small = new FrameStore(paths, bound);
        String a = small.write("p-four", T, "a", header(1), payload(4)).relativePath();
        String b = small.write("p-four", T, "b", header(1), payload(5)).relativePath();
        String c = small.write("p-four", T, "c", header(1), payload(6)).relativePath();
        small.read(a);
        small.cache().cleanUp();
        long one = small.cache().policy().eviction().orElseThrow().weightedSize().orElseThrow();
        assertThat(one).isEqualTo(frameBytes + ElgCodec.HEADER_BYTES);
        small.read(b);
        small.read(c);
        small.cache().cleanUp();
        long after = small.cache().policy().eviction().orElseThrow().weightedSize().orElseThrow();
        assertThat(after).isLessThanOrEqualTo(bound);
        assertThat(small.cache().stats().evictionCount()).isPositive();
        small.read(c);
        assertThat(small.cache().stats().hitCount()).isPositive();
    }

    @Test
    @DisplayName("concurrent readers of a frame being rewritten only ever see a whole file, old or new")
    void concurrentReadersSeeWholeFiles() throws Exception {
        // Checkpoint 057 (Weather-Imagery-Design §2.4): staging plus an atomic rename means a reader opening the
        // path sees either complete version; a partial file would fail the CRC or length checks of the decode.
        byte[] a = payload(11);
        byte[] b = payload(12);
        String relative = store.write("p-seven", T, "x", header(FsgHeader.CODEC_DEFLATE), a).relativePath();
        Path file = paths.resolve(relative);
        java.util.concurrent.atomic.AtomicBoolean stop = new java.util.concurrent.atomic.AtomicBoolean();
        java.util.concurrent.atomic.AtomicInteger reads = new java.util.concurrent.atomic.AtomicInteger();
        java.util.concurrent.ConcurrentLinkedQueue<String> problems = new java.util.concurrent.ConcurrentLinkedQueue<>();
        java.util.List<Thread> readers = new java.util.ArrayList<>();
        for (int r = 0; r < 3; r++) {
            readers.add(Thread.ofPlatform().start(() -> {
                while (!stop.get()) {
                    try {
                        byte[] payload = ElgCodec.decode(Files.readAllBytes(file)).payload();
                        if (!java.util.Arrays.equals(payload, a) && !java.util.Arrays.equals(payload, b)) {
                            problems.add("a payload that is neither version");
                        }
                        reads.incrementAndGet();
                    } catch (IOException | RuntimeException e) {
                        problems.add(e.toString());
                    }
                }
            }));
        }
        for (int i = 0; i < 200; i++) {
            store.write("p-seven", T, "x", header(FsgHeader.CODEC_DEFLATE), i % 2 == 0 ? b : a);
        }
        stop.set(true);
        for (Thread t : readers) {
            t.join(10_000);
        }
        assertThat(problems).isEmpty();
        assertThat(reads.get()).isPositive();
        assertThat(stagedFiles()).isZero();
    }

    @Test
    @DisplayName("partial to complete keeps the frame's path; the sha changes only when the content does")
    void partialToComplete() {
        byte[] decoded = payload(13);
        FsgHeader partial = new FsgHeader(RasterDtype.U16, FsgHeader.CODEC_DEFLATE, 16, 8, 1, true, 0.1, 0, 0);
        FrameStore.StoredFrame first = store.write("p-eight", T, "20261010T143240Z", partial, decoded);
        FrameStore.StoredFrame same = store.write("p-eight", T, "20261010T143240Z", header(FsgHeader.CODEC_DEFLATE),
                decoded);
        FrameStore.StoredFrame changed = store.write("p-eight", T, "20261010T143240Z", header(FsgHeader.CODEC_DEFLATE),
                payload(14));
        assertThat(same.relativePath()).isEqualTo(first.relativePath()).isEqualTo(changed.relativePath());
        assertThat(same.sha256()).as("the partial flag is not content").isEqualTo(first.sha256());
        assertThat(changed.sha256()).isNotEqualTo(first.sha256());
        assertThat(store.read(first.relativePath()).header().partial()).isFalse();
    }

    @Test
    @DisplayName("rewriting a frame replaces the file atomically and drops the stale cache entry")
    void rewriteInvalidates() {
        FrameStore.StoredFrame first = store.write("p-five", T, "x", header(1), payload(7));
        store.read(first.relativePath());
        byte[] second = payload(8);
        store.write("p-five", T, "x", header(1), second);
        assertThat(store.read(first.relativePath()).payload()).isEqualTo(second);
    }

    @Test
    @DisplayName("a stored path cannot escape the raster store")
    void pathEscape() {
        assertThatThrownBy(() -> paths.resolve("../secret.key")).isInstanceOf(IllegalArgumentException.class);
    }
}
