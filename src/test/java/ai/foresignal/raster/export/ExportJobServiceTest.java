package ai.foresignal.raster.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.raster.FrameResolver;
import ai.foresignal.raster.ProductRegistry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Raster export job lifecycle: submit after commit, expiry sweep, stuck jobs, pack reuse (prompt 108, post-review fixes). */
class ExportJobServiceTest {

    private static final Instant T = Instant.parse("2026-10-16T12:00:00Z");

    @AfterEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private static ExportJobService service(ExportJobRepository jobs, ExportPaths paths, Instant now) {
        return new ExportJobService(jobs, mock(RasterExportService.class), mock(PlacePackService.class),
                mock(FrameResolver.class), mock(ProductRegistry.class), paths, Clock.fixed(now, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("inside a transaction the job is handed to the pool only after commit")
    void afterCommit() {
        TransactionSynchronizationManager.initSynchronization();
        AtomicBoolean ran = new AtomicBoolean();
        ExportJobService.afterCommit(() -> ran.set(true));
        assertThat(ran).isFalse();
        List<TransactionSynchronization> registered = TransactionSynchronizationManager.getSynchronizations();
        assertThat(registered).hasSize(1);
        registered.get(0).afterCommit();
        assertThat(ran).isTrue();
    }

    @Test
    @DisplayName("outside a transaction the job is handed over at once")
    void noTransaction() {
        AtomicBoolean ran = new AtomicBoolean();
        ExportJobService.afterCommit(() -> ran.set(true));
        assertThat(ran).isTrue();
    }

    @Test
    @DisplayName("the sweep removes jobs past expires_at even when they never finished, with their files")
    void sweepExpired(@TempDir Path dir) throws Exception {
        ExportJobRepository jobs = mock(ExportJobRepository.class);
        ExportPaths paths = mock(ExportPaths.class);
        ExportJobEntity neverFinished = new ExportJobEntity("01J0000000000000000000000A", "EXPORT", "cog",
                T.minusSeconds(3 * 86_400));
        neverFinished.setExpiresAt(T.minusSeconds(86_400));
        Path jobDir = Files.createDirectories(dir.resolve(neverFinished.getId()));
        Files.writeString(jobDir.resolve("x.tif"), "x");
        when(paths.jobDir(neverFinished.getId())).thenReturn(jobDir);
        when(jobs.findByExpiresAtBefore(T)).thenReturn(List.of(neverFinished));
        when(jobs.findByFinishedAtBefore(any())).thenReturn(List.of());
        when(jobs.findByStatusInAndCreatedAtBefore(any(), any())).thenReturn(List.of());
        int removed = service(jobs, paths, T).sweep();
        assertThat(removed).isEqualTo(1);
        verify(jobs).delete(neverFinished);
        assertThat(jobDir).doesNotExist();
    }

    @Test
    @DisplayName("jobs queued or running when the process started, or stuck past six hours, are failed")
    void stuckJobsFail() {
        ExportJobRepository jobs = mock(ExportJobRepository.class);
        ExportJobEntity stuck = new ExportJobEntity("01J0000000000000000000000B", "PLACE_PACK", "zip",
                T.minusSeconds(7 * 3600));
        stuck.setStatus("RUNNING");
        when(jobs.findByStatusInAndCreatedAtBefore(eq(ExportJobService.UNFINISHED), eq(T.minus(ExportJobService.STUCK_AFTER))))
                .thenReturn(List.of(stuck));
        when(jobs.findByStatusInAndCreatedAtBefore(eq(ExportJobService.UNFINISHED), eq(T))).thenReturn(List.of());
        when(jobs.findByExpiresAtBefore(any())).thenReturn(List.of());
        when(jobs.findByFinishedAtBefore(any())).thenReturn(List.of());
        service(jobs, mock(ExportPaths.class), T).sweep();
        assertThat(stuck.getStatus()).isEqualTo("FAILED");
        assertThat(stuck.getFinishedAt()).isEqualTo(T);
        assertThat(stuck.getError()).contains("6 hours");
        verify(jobs).save(stuck);
    }

    @Test
    @DisplayName("the same pack input from the same token within 10 minutes answers the existing job")
    void packReuse() {
        ExportJobRepository jobs = mock(ExportJobRepository.class);
        PlacePackService.Request request = new PlacePackService.Request("home", List.of(), 12, 150, 8,
                List.of("private-b", "private-a"));
        ExportJobEntity existing = new ExportJobEntity("01J0000000000000000000000C", "PLACE_PACK", "zip",
                T.minusSeconds(60));
        String hash = ExportJobService.inputHash(request, "tok");
        when(jobs.findFirstByKindAndInputHashAndCreatedAtAfterAndStatusInOrderByCreatedAtDesc(eq("PLACE_PACK"),
                eq(hash), eq(T.minus(ExportJobService.PACK_REUSE)), any())).thenReturn(Optional.of(existing));
        ExportJobEntity job = service(jobs, mock(ExportPaths.class), T).submitPlacePack("home", request, "tok");
        assertThat(job).isSameAs(existing);
        verify(jobs, never()).save(any());
        // a different token or a different visibility is a different input
        assertThat(ExportJobService.inputHash(request, "other")).isNotEqualTo(hash);
        assertThat(ExportJobService.inputHash(new PlacePackService.Request("home", List.of(), 12, 150, 8,
                List.of("private-a")), "tok")).isNotEqualTo(hash);
        // the exclusions are sorted, so their order does not change the hash
        assertThat(ExportJobService.inputHash(new PlacePackService.Request("home", List.of(), 12, 150, 8,
                List.of("private-a", "private-b")), "tok")).isEqualTo(hash);
    }

    @Test
    @DisplayName("a new pack stores its input hash, and its spec omits products when none were named")
    void newPack() {
        ExportJobRepository jobs = mock(ExportJobRepository.class);
        when(jobs.findFirstByKindAndInputHashAndCreatedAtAfterAndStatusInOrderByCreatedAtDesc(anyString(),
                anyString(), any(), any())).thenReturn(Optional.empty());
        PlacePackService.Request request = new PlacePackService.Request("home", List.of(), 6, 100, 5, List.of());
        TransactionSynchronizationManager.initSynchronization(); // keep the worker from running in the test
        ExportJobEntity job = service(jobs, mock(ExportPaths.class), T).submitPlacePack("home", request, "tok");
        assertThat(job.getInputHash()).isEqualTo(ExportJobService.inputHash(request, "tok"));
        assertThat(job.getSpec()).doesNotContainKey("products");
        verify(jobs).save(job);
    }

    @Test
    @DisplayName("the client view never shows the excluded (private) source ids")
    void publicSpec() {
        Map<String, Object> spec = Map.of("placeId", "home", "excludedSources", List.of("secret-feed"));
        assertThat(ExportJobService.publicSpec(spec)).containsEntry("placeId", "home")
                .doesNotContainKey("excludedSources");
    }
}
