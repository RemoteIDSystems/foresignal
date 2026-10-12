package ai.foresignal.raster;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.raster.RasterFixtures;
import ai.foresignal.testsupport.raster.RasterTestSources;
import ai.foresignal.testsupport.raster.SyntheticRasterAdapter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Frame retention (prompt 042): ladder thinning keeps the frames nearest each boundary and marks
 * them; maxAge and forecast runs; the row goes before the file; quota eviction; and the orphan sweep
 * removes both orphan kinds and re-emits a missing {@code raster-frame} record. The step's parts are
 * driven directly; the retention job only sequences them.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class RasterRetentionStepIT {

    /** On a 10-minute boundary, so the ladder's boundaries fall on whole frames. */
    static final Instant NOW = Instant.parse("2026-10-10T14:00:00Z");
    static final String OBS = SyntheticRasterAdapter.OBS;
    static final String FCST = SyntheticRasterAdapter.FCST;
    static final String SOURCE = SyntheticRasterAdapter.SOURCE;

    @Autowired RasterRetentionStep step;
    @Autowired RasterPipeline pipeline;
    @Autowired RasterPaths paths;
    @Autowired FrameStore store;
    @Autowired JdbcTemplate jdbc;
    @Autowired MutableClock clock;

    @BeforeEach
    void setUp() {
        clock.setInstant(NOW);
        RasterFixtures.deleteTree(paths.productFrames(OBS));
        RasterFixtures.deleteTree(paths.productFrames(FCST));
    }

    private void commit(Instant t) throws AdapterException {
        pipeline.commit(SOURCE, RasterFixtures.observation(t, (int) (t.getEpochSecond() / 60 % 50)));
    }

    private List<Map<String, Object>> frames(String product) {
        return jdbc.queryForList("SELECT frame_id, path, thinned, expires_at FROM raster_frame WHERE product_id = ?"
                + " ORDER BY valid_time", product);
    }

    private long rows() {
        return jdbc.queryForObject("SELECT count(*) FROM raster_frame", Long.class);
    }

    @Test
    @DisplayName("past the first rung only the frame nearest each 10-minute boundary survives, thinned with a 48 h expiry")
    void ladderThinning() throws Exception {
        Instant base = NOW.minus(Duration.ofHours(7));
        for (int minute = 0; minute <= 20; minute += 2) {
            commit(base.plus(Duration.ofMinutes(minute)));
        }
        commit(NOW.minus(Duration.ofHours(1)));
        List<String> before = frames(OBS).stream().map(r -> (String) r.get("path")).toList();

        long deleted = step.thin(NOW);

        assertThat(deleted).isEqualTo(8);
        List<Map<String, Object>> after = frames(OBS);
        assertThat(after).extracting(r -> r.get("frame_id"))
                .containsExactly("20261010T070000Z", "20261010T071000Z", "20261010T072000Z", "20261010T130000Z");
        assertThat(after.subList(0, 3)).allMatch(r -> Boolean.TRUE.equals(r.get("thinned")));
        assertThat(after.get(3).get("thinned")).isEqualTo(false);
        java.time.OffsetDateTime expiry = (java.time.OffsetDateTime) jdbc.queryForObject(
                "SELECT expires_at FROM raster_frame WHERE product_id = ? AND frame_id = '20261010T071000Z'",
                java.time.OffsetDateTime.class, OBS);
        assertThat(expiry.toInstant()).isEqualTo(base.plus(Duration.ofMinutes(10)).plus(Duration.ofHours(48)));
        List<String> kept = after.stream().map(r -> (String) r.get("path")).toList();
        for (String path : before) {
            assertThat(store.exists(path)).as(path).isEqualTo(kept.contains(path));
        }
        assertThat(step.thin(NOW)).as("a second pass changes nothing").isZero();
    }

    @Test
    @DisplayName("frames older than maxAge go, and forecasts keep only the current and previous complete runs")
    void maxAgeAndRuns() throws Exception {
        commit(NOW.minus(Duration.ofHours(49)));
        commit(NOW.minus(Duration.ofMinutes(4)));
        for (int run = 3; run >= 1; run--) {
            Instant ref = NOW.minus(Duration.ofHours(6L * run));
            pipeline.commit(SOURCE, RasterFixtures.forecast(ref, 0));
            pipeline.commit(SOURCE, RasterFixtures.forecast(ref, 1));
            pipeline.completeRun(SOURCE, FCST, ref);
        }
        pipeline.commit(SOURCE, RasterFixtures.forecast(NOW, 0));

        step.thin(NOW);

        assertThat(frames(OBS)).extracting(r -> r.get("frame_id")).containsExactly("20261010T135600Z");
        assertThat(jdbc.queryForList("SELECT DISTINCT reference_time FROM raster_frame WHERE product_id = ?"
                + " ORDER BY reference_time", java.time.OffsetDateTime.class, FCST))
                .extracting(java.time.OffsetDateTime::toInstant)
                .containsExactly(NOW.minus(Duration.ofHours(12)), NOW.minus(Duration.ofHours(6)), NOW);
    }

    @Test
    @DisplayName("the row is deleted before the file: when the unlink fails the row is already gone")
    void rowBeforeFile() throws Exception {
        assumeTrue(paths.root().getFileSystem().supportedFileAttributeViews().contains("posix"));
        Instant old = NOW.minus(Duration.ofHours(49));
        commit(old);
        String path = (String) frames(OBS).get(0).get("path");
        Path dir = paths.resolve(path).getParent();
        Files.setPosixFilePermissions(dir, PosixFilePermissions.fromString("r-xr-xr-x"));
        try {
            assumeTrue(!Files.isWritable(dir), "running with permissions that ignore the read-only directory");
            step.thin(NOW);
            assertThat(frames(OBS)).isEmpty();
            assertThat(store.exists(path)).as("the file outlives its row until the sweep").isTrue();
        } finally {
            Files.setPosixFilePermissions(dir, PosixFilePermissions.fromString("rwxr-xr-x"));
        }
        Files.setLastModifiedTime(paths.resolve(path), FileTime.from(NOW.minus(Duration.ofDays(1))));
        step.sweep(NOW);
        assertThat(store.exists(path)).isFalse();
    }

    @Test
    @DisplayName("above the quota the oldest frames are evicted, rows and files")
    void quotaEviction() throws Exception {
        commit(NOW.minus(Duration.ofMinutes(6)));
        commit(NOW.minus(Duration.ofMinutes(4)));
        commit(NOW.minus(Duration.ofMinutes(2)));
        List<Map<String, Object>> all = frames(OBS);
        long one = jdbc.queryForObject("SELECT max(bytes) FROM raster_frame", Long.class);

        long deleted = step.enforceQuota(one + 1);

        assertThat(deleted).isEqualTo(2);
        assertThat(frames(OBS)).extracting(r -> r.get("frame_id")).containsExactly("20261010T135800Z");
        assertThat(store.exists((String) all.get(0).get("path"))).isFalse();
        assertThat(store.exists((String) all.get(1).get("path"))).isFalse();
        assertThat(store.exists((String) all.get(2).get("path"))).isTrue();
        assertThat(step.enforceQuota(Long.MAX_VALUE)).isZero();
    }

    @Test
    @DisplayName("the sweep removes old files with no row and old rows with no file, and leaves young ones")
    void orphanSweep() throws Exception {
        commit(NOW.minus(Duration.ofMinutes(4)));
        commit(NOW.minus(Duration.ofMinutes(2)));
        List<Map<String, Object>> all = frames(OBS);
        String rowless = "frames/" + OBS + "/2026/10/09/20261009T000000Z.fsg";
        Files.createDirectories(paths.resolve(rowless).getParent());
        Files.write(paths.resolve(rowless), new byte[] {1, 2, 3});
        Files.setLastModifiedTime(paths.resolve(rowless), FileTime.from(NOW.minus(Duration.ofDays(1))));
        String young = "frames/" + OBS + "/2026/10/09/young.fsg";
        Files.write(paths.resolve(young), new byte[] {4});
        Files.setLastModifiedTime(paths.resolve(young), FileTime.from(NOW.plus(Duration.ofMinutes(55))));
        Files.delete(paths.resolve((String) all.get(0).get("path")));

        Instant later = NOW.plus(Duration.ofHours(1));
        clock.setInstant(later);
        RasterRetentionStep.SweepResult result = step.sweep(later);

        assertThat(Files.exists(paths.resolve(rowless))).isFalse();
        assertThat(Files.exists(paths.resolve(young))).as("younger than the grace").isTrue();
        assertThat(frames(OBS)).extracting(r -> r.get("frame_id")).containsExactly("20261010T135800Z");
        assertThat(result.filesDeleted()).as("the row-less file (and any stale staging file)").isPositive();
        assertThat(result.rowsDeleted()).isEqualTo(1);
        Files.delete(paths.resolve(young));
    }

    @Test
    @DisplayName("a complete frame newer than its record head is re-emitted as a MANUAL batch by system:raster-sweeper")
    void reemitsMissingRecord() throws Exception {
        commit(NOW.minus(Duration.ofMinutes(2)));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM record_version WHERE source_id = ? AND external_id = ?",
                Long.class, SOURCE, OBS)).as("committed but never enqueued, as after a crash").isZero();

        Instant later = NOW.plus(Duration.ofHours(1));
        clock.setInstant(later);
        RasterRetentionStep.SweepResult result = step.sweep(later);

        assertThat(result.reemitted()).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM record_version WHERE source_id = ? AND external_id = ?",
                Long.class, SOURCE, OBS)).isEqualTo(1);
        Map<String, Object> log = jdbc.queryForMap("SELECT trigger, principal FROM fetch_log WHERE source_id = ?"
                + " ORDER BY id DESC LIMIT 1", SOURCE);
        assertThat(log.get("trigger")).isEqualTo("MANUAL");
        assertThat(log.get("principal")).isEqualTo(RasterRetentionStep.SWEEPER_PRINCIPAL);
        assertThat(step.sweep(later).reemitted()).as("nothing newer than the head any more").isZero();
        assertThat(rows()).isEqualTo(1);
    }

    @Test
    @DisplayName("the step reports raster_frame deletions under its name, after the record steps")
    void stepShape() {
        assertThat(step.name()).isEqualTo("rasters");
        assertThat(step.order()).isGreaterThan(700);
    }
}
