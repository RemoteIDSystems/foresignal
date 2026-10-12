package ai.foresignal.media;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.retention.RetentionResolver;
import ai.foresignal.source.spi.MediaInput;
import ai.foresignal.source.spi.MediaRef;
import ai.foresignal.source.spi.MediaRole;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.media.ImageFixtures;
import ai.foresignal.testsupport.media.MediaTestFiles;
import ai.foresignal.testsupport.raster.RasterTestSources;
import ai.foresignal.testsupport.raster.SyntheticRasterAdapter;
import io.micrometer.core.instrument.MeterRegistry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * {@link MediaRetentionStep} (prompt 043): {@code expires_at} deletion; record-following deletion by
 * the owning record's per-kind window; deletion after 24 h when the owning head is gone; thumbnails
 * going with their original; a shared file surviving while another row references it; and the orphan
 * sweep of files, rows and staging. The step is driven directly through {@link MediaRetentionStep#apply}.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class MediaRetentionStepIT {

    static final Instant T = Instant.parse("2026-10-12T12:00:00Z");
    static final String SOURCE = SyntheticRasterAdapter.SOURCE;

    @Autowired MediaRetentionStep step;
    @Autowired MediaStore store;
    @Autowired MediaQueries queries;
    @Autowired MediaPaths paths;
    @Autowired ThumbnailService thumbnails;
    @Autowired RetentionResolver resolver;
    @Autowired TestRecords records;
    @Autowired MeterRegistry meters;
    @Autowired JdbcTemplate jdbc;
    @Autowired MutableClock clock;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        MediaTestFiles.clear(paths);
    }

    private MediaRef image(int seed, MediaInput template) {
        return store.store(new MediaInput(null, ImageFixtures.png(20 + seed, 20), "image/png", template.role(),
                template.sourceId(), template.recordSourceId(), template.recordExternalId(), template.licence(),
                template.expiresAt(), false));
    }

    private static MediaInput owned(String externalId) {
        return MediaInput.of(new byte[0], "image/png", MediaRole.IMAGE).withSourceId(SOURCE).withRecord(SOURCE,
                externalId);
    }

    private void createdAt(String id, Instant at) {
        jdbc.update("UPDATE media SET created_at = ? WHERE id = ?", OffsetDateTime.ofInstant(at, ZoneOffset.UTC), id);
    }

    private boolean exists(String id) {
        return queries.find(id).isPresent();
    }

    private Path file(MediaRef ref) {
        return paths.resolve(MediaPaths.relative(ref.sha256(), "png"));
    }

    private void ingestHead(String externalId) {
        records.ingest(SOURCE, NormalizedRecord.builder(externalId, ProductSpec.RECORD_KIND)
                .observedAt(T.minus(Duration.ofHours(1)))
                .put("productId", externalId)
                .build());
    }

    @Test
    @DisplayName("rows past expires_at are deleted with their files and leave a tombstone")
    void expiresAt() {
        MediaRef soon = image(1, MediaInput.of(new byte[0], "image/png", MediaRole.MAP).withExpiresAt(T.plus(Duration.ofDays(7))));
        MediaRef later = image(2, MediaInput.of(new byte[0], "image/png", MediaRole.MAP).withExpiresAt(T.plus(Duration.ofDays(30))));
        MediaRetentionStep.Result result = step.apply(T.plus(Duration.ofDays(8)));
        assertThat(exists(soon.id())).isFalse();
        assertThat(file(soon)).doesNotExist();
        assertThat(queries.tombstoned(soon.id())).isTrue();
        assertThat(exists(later.id())).isTrue();
        assertThat(file(later)).exists();
        assertThat(result.rowsDeleted()).isEqualTo(1);
        assertThat(result.filesUnlinked()).isEqualTo(1);
    }

    @Test
    @DisplayName("record-owned media follows its record's (source, kind) window; thumbnails go with it")
    void recordFollowing() {
        ingestHead("owner-1");
        Duration window = resolver.window(SOURCE, ProductSpec.RECORD_KIND);
        MediaRef old = image(3, owned("owner-1"));
        MediaRef fresh = image(4, owned("owner-1"));
        MediaRef big = store.store(MediaInput.of(ImageFixtures.png(600, 300), "image/png", MediaRole.IMAGE)
                .withSourceId(SOURCE).withRecord(SOURCE, "owner-1"));
        MediaRow thumb = thumbnails.thumbnail(big.id(), ThumbnailService.SMALL);
        createdAt(old.id(), T.minus(window).minus(Duration.ofHours(1)));
        createdAt(big.id(), T.minus(window).minus(Duration.ofHours(1)));
        createdAt(fresh.id(), T.minus(window).plus(Duration.ofHours(1)));
        step.apply(T);
        assertThat(exists(old.id())).isFalse();
        assertThat(exists(big.id())).isFalse();
        assertThat(exists(thumb.id())).as("thumbnails go with their original").isFalse();
        assertThat(paths.resolve(thumb.path())).doesNotExist();
        assertThat(exists(fresh.id())).isTrue();
    }

    @Test
    @DisplayName("media whose owning head is gone is deleted once older than 24 h")
    void headMissing() {
        MediaRef stale = image(5, owned("never-ingested"));
        MediaRef recent = image(6, owned("never-ingested"));
        createdAt(stale.id(), T.minus(Duration.ofHours(25)));
        createdAt(recent.id(), T.minus(Duration.ofHours(23)));
        step.apply(T);
        assertThat(exists(stale.id())).isFalse();
        assertThat(exists(recent.id())).isTrue();
    }

    @Test
    @DisplayName("a shared file survives while another row references it, and goes with the last row")
    void sharedFile() {
        byte[] png = ImageFixtures.png(50, 50);
        MediaRef first = store.store(MediaInput.of(png, "image/png", MediaRole.MAP).withExpiresAt(T.plusSeconds(60)));
        MediaRef second = store.store(MediaInput.of(png, "image/png", MediaRole.MAP).withExpiresAt(T.plusSeconds(600)));
        Path shared = file(first);
        step.apply(T.plusSeconds(120));
        assertThat(exists(first.id())).isFalse();
        assertThat(exists(second.id())).isTrue();
        assertThat(shared).as("still referenced by the second row").exists();
        step.apply(T.plusSeconds(1200));
        assertThat(exists(second.id())).isFalse();
        assertThat(shared).doesNotExist();
    }

    @Test
    @DisplayName("media with neither a record nor an expiry follows its source's window")
    void unowned() {
        MediaRef old = image(7, MediaInput.of(new byte[0], "image/png", MediaRole.IMAGE).withSourceId(SOURCE));
        MediaRef young = image(8, MediaInput.of(new byte[0], "image/png", MediaRole.IMAGE).withSourceId(SOURCE));
        Duration window = resolver.window(SOURCE, null);
        createdAt(old.id(), T.minus(window).minus(Duration.ofHours(1)));
        createdAt(young.id(), T.minus(window).plus(Duration.ofHours(1)));
        step.apply(T);
        assertThat(exists(old.id())).isFalse();
        assertThat(exists(young.id())).isTrue();
    }

    @Test
    @DisplayName("the orphan sweep removes files with no row, rows with no file and stale staging, after the grace")
    void orphanSweep() throws Exception {
        double rowOrphansBefore = meters.counter(MediaRetentionStep.ORPHANS_METRIC, "type", "row").count();
        double fileOrphansBefore = meters.counter(MediaRetentionStep.ORPHANS_METRIC, "type", "file").count();
        String sha = "ab".repeat(32);
        Path orphan = paths.resolve(MediaPaths.relative(sha, "png"));
        Files.createDirectories(orphan.getParent());
        Files.write(orphan, ImageFixtures.png(5, 5));
        Files.setLastModifiedTime(orphan, FileTime.from(T.minus(Duration.ofHours(1))));
        String youngSha = "cd".repeat(32);
        Path young = paths.resolve(MediaPaths.relative(youngSha, "png"));
        Files.createDirectories(young.getParent());
        Files.write(young, ImageFixtures.png(6, 6));
        Files.setLastModifiedTime(young, FileTime.from(T));
        Path staged = paths.tmp().resolve("stale.part");
        Files.write(staged, new byte[] {1, 2, 3});
        Files.setLastModifiedTime(staged, FileTime.from(T.minus(Duration.ofHours(1))));

        MediaRef missing = image(9, MediaInput.of(new byte[0], "image/png", MediaRole.IMAGE)
                .withExpiresAt(T.plus(Duration.ofDays(1))));
        Files.delete(file(missing));
        createdAt(missing.id(), T.minus(Duration.ofHours(1)));
        MediaRef kept = image(10, MediaInput.of(new byte[0], "image/png", MediaRole.IMAGE)
                .withExpiresAt(T.plus(Duration.ofDays(1))));

        MediaRetentionStep.Result result = step.apply(T);
        assertThat(orphan).doesNotExist();
        assertThat(young).as("within the grace").exists();
        assertThat(staged).doesNotExist();
        assertThat(exists(missing.id())).isFalse();
        assertThat(queries.tombstoned(missing.id())).isTrue();
        assertThat(exists(kept.id())).isTrue();
        assertThat(file(kept)).exists();
        assertThat(result.orphanRows()).isEqualTo(1);
        assertThat(result.orphanFiles()).isEqualTo(2);
        assertThat(meters.counter(MediaRetentionStep.ORPHANS_METRIC, "type", "row").count() - rowOrphansBefore)
                .isEqualTo(1.0);
        assertThat(meters.counter(MediaRetentionStep.ORPHANS_METRIC, "type", "file").count() - fileOrphansBefore)
                .isEqualTo(2.0);

        // After a sweep no orphan of either kind remains.
        Files.setLastModifiedTime(young, FileTime.from(T.minus(Duration.ofHours(1))));
        step.apply(T);
        assertThat(young).doesNotExist();
        for (MediaRow row : queries.page(null, 100)) {
            assertThat(paths.resolve(row.path())).exists();
        }
    }

    @Test
    @DisplayName("tombstones are pruned after the tombstone retention")
    void tombstonePrune() {
        MediaRef gone = image(11, MediaInput.of(new byte[0], "image/png", MediaRole.MAP).withExpiresAt(T.plusSeconds(1)));
        step.apply(T.plusSeconds(10));
        assertThat(queries.tombstoned(gone.id())).isTrue();
        step.apply(T.plus(Duration.ofDays(31)));
        assertThat(queries.tombstoned(gone.id())).isFalse();
    }
}
