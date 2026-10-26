package ai.foresignal.export;

import static org.awaitility.Awaitility.await;

import ai.foresignal.settings.SettingKeys;
import ai.foresignal.system.OperationalAlertService;
import ai.foresignal.testsupport.export.ExportItSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The shared fixture of prompt R2-016's archive-run ITs: an external-disk archive root per test, the archive switched
 * on for {@value ExportItSupport#STD}, and helpers that seed history and wait for a segment to settle. Runs in the
 * shared 019 retention context.
 */
abstract class ArchiveRunItSupport extends ExportItSupport {

    /** The statuses a run or restore settles in. */
    static final Set<String> SETTLED = Set.of(ArchiveSegment.COMPLETE, ArchiveSegment.PURGED, ArchiveSegment.FAILED,
            ArchiveSegment.RESTORED);

    @Autowired ArchiveRunner runner;
    @Autowired ArchiveManifestStore manifests;
    @Autowired OperationalAlertService alerts;
    @Autowired ExportStore store;

    Path root;

    /** A fresh archive root and no raised alert. */
    @BeforeEach
    void archiveRoot() throws Exception {
        root = Files.createTempDirectory("foresignal-cold-archive-it");
        alerts.reset();
    }

    /** Drops the settings and the root. */
    @AfterEach
    void cleanUpArchive() {
        settings.remove(SettingKeys.ARCHIVE);
        settings.remove(SettingKeys.RETENTION);
        settings.remove(SettingKeys.RETENTION_DB_CAP);
        settings.invalidateCache();
        ExportStore.deleteTree(root);
    }

    /** Switches the archive on, external disk at {@code archiveRoot}, for {@code sources}. */
    void enable(Path archiveRoot, String... sources) {
        settings.put(SettingKeys.ARCHIVE, new ArchiveSettings(true, List.of(sources), archiveRoot.toString(),
                ArchiveSettings.LOCAL, null, null, null, null, null, true));
        settings.invalidateCache();
    }

    /**
     * {@code d1} at T0 and {@code d2} a day later, then a marker a minute after that: a run at T0 + 3 days with a
     * cutoff of T0 + 2 days archives the first two and keeps the marker; the 7-day default window removes nothing.
     */
    void seed() {
        ingestAt(STD, T0, point("d1", STD_KIND, T0, 1, 1, 1));
        Instant nextDay = T0.plus(Duration.ofDays(1));
        ingestAt(STD, nextDay, point("d2", STD_KIND, nextDay, 2, 2, 2));
        Instant marker = nextDay.plus(Duration.ofMinutes(1));
        ingestAt(STD, marker, point("marker", STD_KIND, marker, 3, 3, 3));
    }

    /** The run time of {@link #seed}'s scenario. */
    static Instant now() {
        return T0.plus(Duration.ofDays(3));
    }

    /** The cutoff of {@link #seed}'s scenario. */
    static Instant cutoff() {
        return T0.plus(Duration.ofDays(2));
    }

    /** Catches the cursors up and moves the clock to {@link #now()}, as a retention run would. */
    void atNow() {
        awaitTriggerCursor();
        clock.setInstant(now());
    }

    /** Waits until segment {@code id} settles, and returns it. */
    ArchiveSegment awaitSettled(String id) {
        await().atMost(Duration.ofSeconds(60)).until(() -> manifests.find(id)
                .map(s -> SETTLED.contains(s.status())).orElse(false));
        return manifests.find(id).orElseThrow();
    }
}
