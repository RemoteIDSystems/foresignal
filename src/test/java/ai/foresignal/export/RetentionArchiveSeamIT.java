package ai.foresignal.export;

import static ai.foresignal.testsupport.retention.RetentionTestSources.KINDS;
import static ai.foresignal.testsupport.retention.RetentionTestSources.LONG_KIND;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.retention.RetentionRunResult;
import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

/**
 * Prompt R2-015's size-cap seam filled by prompt R2-016: with an archive tier configured, the cap's overwrite-oldest
 * step archives the oldest history, verifies it, records a {@code cap} segment, and only then purges it.
 */
@IntegrationTest
@Import(RetentionTestSources.class)
class RetentionArchiveSeamIT extends ArchiveRunItSupport {

    /** About 1 KB: below any real database, so the cap always steps. */
    static final double TINY_GB = 0.000001;

    /** The defaults with a 64-day cap and the tiny size cap, overwrite-oldest on. */
    static RetentionSettings capped() {
        RetentionSettings d = RetentionSettings.defaults();
        return new RetentionSettings(d.defaultRetention(), d.alerts(), d.usage(), d.sources(), d.trackWindow(),
                d.provenance(), d.notifications(), d.briefings(), d.ask(), d.kinds(), d.audit(), d.inbox(), d.stats(),
                d.trackRawWindow(), d.trackDownsampleInterval(), d.entityLinks(), true, 64, TINY_GB, true);
    }

    private List<ArchiveSegment> capSegments() {
        return manifests.page(null, null, 100).items().stream()
                .filter(s -> ArchiveRunner.CAP.equals(s.trigger())).toList();
    }

    @Test
    @DisplayName("with a tier configured, the size cap archives, verifies and records before it purges")
    void capArchivesThenPurges() throws Exception {
        ingestAt(KINDS, T0, record("seam-old", LONG_KIND, T0, 1));
        Instant marker = T0.plus(Duration.ofMinutes(1));
        ingestAt(KINDS, marker, record("seam-marker", LONG_KIND, marker, 1));
        Instant fresh = T0.plus(Duration.ofDays(39));
        ingestAt(KINDS, fresh, record("seam-fresh", LONG_KIND, fresh, 1));
        enable(root, KINDS);
        settings.put(SettingKeys.RETENTION, capped());
        settings.invalidateCache();

        // 40 days on: the 64-day cap removes nothing; the size cap steps to 32 days and prunes through the archive.
        RetentionRunResult result = runAt(T0.plus(Duration.ofDays(40)));

        assertThat(result.dbCap()).isNotNull();
        assertThat(result.dbCap().steps()).isPositive();
        List<ArchiveSegment> segments = capSegments();
        assertThat(segments).isNotEmpty();
        ArchiveSegment first = segments.getLast();
        assertThat(first.status()).isEqualTo(ArchiveSegment.PURGED);
        assertThat(first.purgedFromHot()).isTrue();
        assertThat(first.sources()).contains(KINDS);
        assertThat(first.files()).allSatisfy(file -> {
            assertThat(root.resolve(file.key())).exists();
            assertThat(ExportManifests.sha256(root.resolve(file.key()))).isEqualTo(file.sha256());
        });
        assertThat(versions(KINDS, "seam-old")).isZero();
        assertThat(versions(KINDS, "seam-fresh")).isEqualTo(1);
    }

    @Test
    @DisplayName("with the archive off, the size cap prunes without a segment, as in prompt R2-015")
    void archiveOffKeepsRerun() {
        ingestAt(KINDS, T0, record("seam-off", LONG_KIND, T0, 1));
        Instant marker = T0.plus(Duration.ofMinutes(1));
        ingestAt(KINDS, marker, record("seam-off-marker", LONG_KIND, marker, 1));
        settings.put(SettingKeys.RETENTION, capped());
        settings.invalidateCache();

        RetentionRunResult result = runAt(T0.plus(Duration.ofDays(40)));

        assertThat(result.dbCap().steps()).isPositive();
        assertThat(capSegments()).isEmpty();
        assertThat(versions(KINDS, "seam-off")).isZero();
        assertThat(Files.exists(root.resolve("segments"))).isFalse();
    }
}
