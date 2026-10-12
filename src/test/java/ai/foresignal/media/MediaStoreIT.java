package ai.foresignal.media;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.collect.FetchContextFactory;
import ai.foresignal.collect.RequestRecorder;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.ImageRejectedException;
import ai.foresignal.source.spi.MediaInput;
import ai.foresignal.source.spi.MediaRef;
import ai.foresignal.source.spi.MediaRole;
import ai.foresignal.source.spi.MediaSink;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.media.ImageFixtures;
import ai.foresignal.testsupport.media.MediaTestFiles;
import ai.foresignal.testsupport.raster.RasterTestSources;
import ai.foresignal.testsupport.raster.SyntheticRasterAdapter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * {@link MediaStore} against PostgreSQL and the data directory (prompt 043): content addressing (one
 * file, two rows), the atomic rename out of staging, {@code ctx.media()} filling the source id, a
 * guard refusal or failed rename leaving nothing behind, {@code 413 MEDIA_TOO_LARGE}, and thumbnail
 * rows (rendered and linked). Shares the 042 raster context ({@code RasterTestSources}).
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class MediaStoreIT {

    static final Instant T = Instant.parse("2026-10-12T12:00:00Z");
    static final String SOURCE = SyntheticRasterAdapter.SOURCE;

    @Autowired MediaStore store;
    @Autowired MediaQueries queries;
    @Autowired MediaPaths paths;
    @Autowired MediaLocks locks;
    @Autowired MediaSinks sinks;
    @Autowired ThumbnailService thumbnails;
    @Autowired FetchContextFactory contexts;
    @Autowired SourceRegistry registry;
    @Autowired JdbcTemplate jdbc;
    @Autowired MutableClock clock;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        MediaTestFiles.clear(paths);
    }

    private long rows() {
        return jdbc.queryForObject("SELECT count(*) FROM media", Long.class);
    }

    @Test
    @DisplayName("identical content is stored once on disk with two rows")
    void dedupe() throws Exception {
        byte[] png = ImageFixtures.png(64, 32);
        MediaRef first = store.store(MediaInput.of(png, "image/png", MediaRole.IMAGE).withSourceId(SOURCE));
        MediaRef second = store.store(MediaInput.of(png, "image/png", MediaRole.MAP).withSourceId(SOURCE)
                .withExpiresAt(T.plusSeconds(3600)));
        assertThat(first.id()).isNotEqualTo(second.id());
        assertThat(first.sha256()).isEqualTo(second.sha256()).hasSize(64);
        assertThat(rows()).isEqualTo(2);
        Path file = paths.root().resolve(first.sha256().substring(0, 2)).resolve(first.sha256() + ".png");
        assertThat(file).exists();
        assertThat(Files.size(file)).isEqualTo(png.length).isEqualTo(first.bytes());
        assertThat(MediaTestFiles.contentFiles(paths)).containsExactly(file);
        assertThat(queries.totalBytes()).as("a shared file is counted once").isEqualTo(png.length);
        MediaRow row = queries.find(first.id()).orElseThrow();
        assertThat(row.path()).isEqualTo(first.sha256().substring(0, 2) + "/" + first.sha256() + ".png");
        assertThat(row.width()).isEqualTo(64);
        assertThat(row.height()).isEqualTo(32);
        assertThat(row.createdAt()).isEqualTo(T);
        assertThat(row.role()).isEqualTo("image");
        assertThat(queries.find(second.id()).orElseThrow().expiresAt()).isEqualTo(T.plusSeconds(3600));
    }

    @Test
    @DisplayName("a stored file is renamed out of staging; nothing stays in media/tmp")
    void atomicRename() throws Exception {
        Path source = Files.createTempFile("media-in", ".jpg");
        Files.write(source, ImageFixtures.jpeg(40, 40));
        MediaRef ref = store.store(MediaInput.of(source, "image/jpeg", MediaRole.IMAGE));
        assertThat(source).as("a path input is copied, never moved").exists();
        assertThat(MediaTestFiles.staged(paths)).isEmpty();
        assertThat(store.file(queries.find(ref.id()).orElseThrow())).exists()
                .hasFileName(ref.sha256() + ".jpg");
        Files.delete(source);
    }

    @Test
    @DisplayName("a failed rename leaves no staged file, no content file and no row")
    void failedRenameLeavesNothing() throws Exception {
        byte[] png = ImageFixtures.png(33, 33);
        String sha = java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(png));
        Path dir = paths.root().resolve(sha.substring(0, 2));
        Files.createDirectories(dir);
        try {
            Files.setPosixFilePermissions(dir, PosixFilePermissions.fromString("r-xr-xr-x"));
        } catch (UnsupportedOperationException e) {
            assumeTrue(false, "POSIX permissions are needed");
        }
        try {
            assumeTrue(!Files.isWritable(dir), "running as root ignores directory permissions");
            assertThatThrownBy(() -> store.store(MediaInput.of(png, "image/png", MediaRole.IMAGE)))
                    .isInstanceOf(UncheckedIOException.class);
            assertThat(rows()).isZero();
            assertThat(MediaTestFiles.staged(paths)).isEmpty();
            assertThat(MediaTestFiles.contentFiles(paths)).isEmpty();
        } finally {
            Files.setPosixFilePermissions(dir, PosixFilePermissions.fromString("rwxr-xr-x"));
        }
    }

    @Test
    @DisplayName("ctx.media() fills in the context's source id; a probe writes nothing")
    void contextFillsSourceId() {
        MediaSink sink = contexts.forFetch(registry.descriptor(SOURCE), null).media();
        MediaRef ref = sink.store(MediaInput.of(ImageFixtures.png(20, 20), "image/png", MediaRole.IMAGE)
                .withSourceId("someone-else").withRecord(SOURCE, "test-synth-obs").withLicence("US-PD"));
        MediaRow row = queries.find(ref.id()).orElseThrow();
        assertThat(row.sourceId()).isEqualTo(SOURCE);
        assertThat(row.recordSourceId()).isEqualTo(SOURCE);
        assertThat(row.recordExternalId()).isEqualTo("test-synth-obs");
        assertThat(row.licence()).isEqualTo("US-PD");
        assertThat(sink.probing()).isFalse();

        MediaSink probe = contexts.forFetch(registry.descriptor(SOURCE), new RequestRecorder()).media();
        assertThat(probe.probing()).isTrue();
        MediaRef preview = probe.store(MediaInput.of(ImageFixtures.png(21, 21), "image/png", MediaRole.IMAGE));
        assertThat(preview.id()).isNull();
        assertThat(preview.width()).isEqualTo(21);
        assertThat(probe.thumbnails(preview)).isEmpty();
        assertThat(rows()).isEqualTo(1);
    }

    @Test
    @DisplayName("a guard refusal leaves no file behind and inserts no row")
    void guardFailureLeavesNothing() {
        assertThatThrownBy(() -> store.store(MediaInput.of(ImageFixtures.png(10, 10), "image/jpeg", MediaRole.IMAGE)))
                .isInstanceOfSatisfying(ImageRejectedException.class,
                        e -> assertThat(e.tag()).isEqualTo(ImageRejectedException.TYPE_MISMATCH));
        assertThatThrownBy(() -> store.store(MediaInput.of(ImageFixtures.craftedPngHeader(20_000, 10), "image/png",
                MediaRole.IMAGE))).isInstanceOf(ImageRejectedException.class);
        assertThat(rows()).isZero();
        assertThat(MediaTestFiles.staged(paths)).isEmpty();
        assertThat(MediaTestFiles.contentFiles(paths)).isEmpty();
    }

    @Test
    @DisplayName("content over max-file-bytes is 413 MEDIA_TOO_LARGE and leaves nothing behind")
    void tooLarge() {
        MediaStore small = new MediaStore(paths, queries, new ImageGuard(1_000, null), locks, clock);
        byte[] noisy = ImageFixtures.noisyPng(64, 64);
        assertThatThrownBy(() -> small.store(MediaInput.of(noisy, "image/png", MediaRole.IMAGE)))
                .isInstanceOfSatisfying(ForesignalException.class, e -> {
                    assertThat(e.getCode()).isEqualTo(ErrorCodes.MEDIA_TOO_LARGE);
                    assertThat(e.getStatus().value()).isEqualTo(413);
                });
        assertThat(rows()).isZero();
        assertThat(MediaTestFiles.staged(paths)).isEmpty();
    }

    @Test
    @DisplayName("thumbnails: a larger original is rendered, a smaller one is linked, and repeats reuse the rows")
    void thumbnails() {
        MediaRef original = store.store(MediaInput.of(ImageFixtures.png(600, 400), "image/png", MediaRole.IMAGE)
                .withSourceId(SOURCE).withRecord(SOURCE, "test-synth-obs").withLicence("US-PD"));
        List<MediaRef> refs = thumbnails.thumbnails(original);
        assertThat(refs).hasSize(2);
        MediaRow small = queries.find(refs.get(0).id()).orElseThrow();
        assertThat(small.role()).isEqualTo("thumbnail");
        assertThat(small.variantOf()).isEqualTo(original.id());
        assertThat(small.variantWidth()).isEqualTo(256);
        assertThat(small.contentType()).isEqualTo("image/jpeg");
        assertThat(small.width()).isEqualTo(256);
        assertThat(small.height()).isEqualTo(171);
        assertThat(small.recordExternalId()).isEqualTo("test-synth-obs");
        assertThat(small.licence()).isEqualTo("US-PD");
        MediaRow large = queries.find(refs.get(1).id()).orElseThrow();
        assertThat(large.variantWidth()).isEqualTo(1024);
        assertThat(large.sha256()).as("600 px is not upscaled: the original's file is linked")
                .isEqualTo(original.sha256());
        assertThat(large.width()).isEqualTo(600);
        assertThat(thumbnails.thumbnails(original)).extracting(MediaRef::id)
                .containsExactly(small.id(), large.id());
        assertThat(rows()).isEqualTo(3);
        assertThat(queries.variants(original.id())).hasSize(2);
    }

    @Test
    @DisplayName("a TIFF original gets browser-format thumbnails even when small")
    void tiffThumbnails() {
        MediaRef original = store.store(MediaInput.of(ImageFixtures.tiff(100, 50), "image/tiff", MediaRole.IMAGE));
        MediaRow small = thumbnails.thumbnail(original.id(), 256);
        assertThat(small.contentType()).isEqualTo("image/jpeg");
        assertThat(small.width()).isEqualTo(100);
        assertThat(small.sha256()).isNotEqualTo(original.sha256());
    }
}
