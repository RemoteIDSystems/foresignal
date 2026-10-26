package ai.foresignal.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.notify.SystemEvent;
import ai.foresignal.retention.RetentionJob;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

/**
 * Archive runs end to end (prompt R2-016): select older than the cutoff → export → verify on the tier → manifest →
 * purge through the retention steps; a failed verification purges nothing and raises {@code system.archive_failed};
 * one run at a time; a dry run; the inventory.
 */
@IntegrationTest
@Import(RetentionTestSources.class)
@RecordApplicationEvents
class ArchiveRunnerIT extends ArchiveRunItSupport {

    @Autowired RetentionJob job;
    @Autowired ApplicationEvents events;

    private List<String> lines(Path gz) throws Exception {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(gz)),
                StandardCharsets.UTF_8))) {
            return in.lines().toList();
        }
    }

    @Test
    @DisplayName("a run archives what is older than the cutoff, verifies it, records it and only then purges it")
    void archivesThenPurges() throws Exception {
        seed();
        enable(root, STD);
        atNow();

        ArchiveSegment started = runner.start(new ArchiveRunner.Request(cutoff(), List.of(), false),
                ArchiveRunner.MANUAL);
        assertThat(started.status()).isEqualTo(ArchiveSegment.PENDING);
        ArchiveSegment segment = awaitSettled(started.id());

        assertThat(segment.status()).isEqualTo(ArchiveSegment.PURGED);
        assertThat(segment.purgedFromHot()).isTrue();
        assertThat(segment.trigger()).isEqualTo(ArchiveRunner.MANUAL);
        assertThat(segment.target()).isEqualTo(ArchiveSettings.LOCAL);
        assertThat(segment.cutoff()).isEqualTo(cutoff());
        assertThat(segment.sources()).containsExactly(STD);
        assertThat(segment.itemCount()).isEqualTo(2);
        assertThat(segment.files()).hasSize(1);
        ArchiveSegment.SegmentFile file = segment.files().getFirst();
        Path stored = root.resolve(file.key());
        assertThat(stored).exists();
        assertThat(ExportManifests.sha256(stored)).isEqualTo(file.sha256());
        assertThat(Files.size(stored)).isEqualTo(file.bytes()).isEqualTo(segment.sizeBytes());
        assertThat(segment.checksum()).isEqualTo(ArchiveRunner.checksum(segment.files()));
        assertThat(lines(stored)).hasSize(2).allSatisfy(line -> assertThat(line).contains("\"source_id\":\"" + STD));
        assertThat(lines(stored)).anySatisfy(line -> assertThat(line).contains("\"external_id\":\"d1\""));

        // Purged through the retention steps: the archived versions are gone, the newer marker and its head remain.
        assertThat(versions(STD, "d1")).isZero();
        assertThat(versions(STD, "d2")).isZero();
        assertThat(versions(STD, "marker")).isEqualTo(1);
        assertThat(head(STD, "marker")).isTrue();
        assertThat(live(STD, "marker")).isTrue();
        // The staging copy is gone; only the tier holds the segment.
        assertThat(Files.exists(store.resolve("archive-staging/" + segment.id()))).isFalse();
        assertThat(segment.location()).isEqualTo(root.resolve("segments").resolve(segment.id()).toString());
    }

    @Test
    @DisplayName("a failed verification purges nothing, marks the segment failed and raises system.archive_failed")
    void failedVerifyPurgesNothing() throws Exception {
        seed();
        Path notADirectory = Files.createFile(root.resolve("not-a-directory"));
        enable(notADirectory, STD);
        atNow();

        ArchiveSegment segment = awaitSettled(runner.start(new ArchiveRunner.Request(cutoff(), List.of(), false),
                ArchiveRunner.MANUAL).id());

        assertThat(segment.status()).isEqualTo(ArchiveSegment.FAILED);
        assertThat(segment.purgedFromHot()).isFalse();
        assertThat(segment.error()).startsWith("Verification failed");
        assertThat(versions(STD, "d1")).isEqualTo(1);
        assertThat(versions(STD, "d2")).isEqualTo(1);
        assertThat(events.stream(SystemEvent.class).filter(e -> e.subtype().equals("system.archive_failed")))
                .hasSize(1);
    }

    @Test
    @DisplayName("one run at a time: while retention or another run holds the guard, a run is 409 ALREADY_RUNNING")
    void oneAtATime() throws Exception {
        enable(root, STD);
        try (RetentionJob.Lease lease = job.lease()) {
            assertThatThrownBy(() -> runner.start(new ArchiveRunner.Request(null, List.of(), false),
                    ArchiveRunner.MANUAL))
                    .isInstanceOfSatisfying(ConflictException.class,
                            e -> assertThat(e.getCode()).isEqualTo(ConflictException.ALREADY_RUNNING));
            mvc.perform(post("/v1/archives/run").header(HttpHeaders.AUTHORIZATION, admin)
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("ALREADY_RUNNING"));
        }
    }

    @Test
    @DisplayName("with the archive off, a run is 409 ARCHIVE_NOT_CONFIGURED; a bad body is 400")
    void notConfiguredAndValidation() throws Exception {
        mvc.perform(post("/v1/archives/run").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ARCHIVE_NOT_CONFIGURED"));
        enable(root, STD);
        mvc.perform(post("/v1/archives/run").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cutoff\":\"yesterday\",\"dryRun\":\"no\",\"extra\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(3));
        mvc.perform(post("/v1/archives/run").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"sources\":[\"" + SECRET + "\"]}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/v1/archives/run").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"cutoff\":\"2999-01-01T00:00:00Z\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("a dry run counts what would be archived and stores, exports and purges nothing")
    void dryRun() throws Exception {
        seed();
        enable(root, STD);
        atNow();

        mvc.perform(post("/v1/archives/run").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cutoff\":\"" + cutoff() + "\",\"dryRun\":true}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("pending"))
                .andExpect(jsonPath("$.itemCount").value(2));

        assertThat(manifests.page(null, null, 100).items()).isEmpty();
        assertThat(versions(STD, "d1")).isEqualTo(1);
        assertThat(Files.exists(root.resolve("segments"))).isFalse();
    }

    @Test
    @DisplayName("listArchives pages the inventory newest first and filters by status")
    void inventory() throws Exception {
        seed();
        enable(root, STD);
        atNow();
        ArchiveSegment segment = awaitSettled(runner.start(new ArchiveRunner.Request(cutoff(), List.of(), false),
                ArchiveRunner.MANUAL).id());

        mvc.perform(get("/v1/archives").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(segment.id()))
                .andExpect(jsonPath("$.items[0].status").value("purged"))
                .andExpect(jsonPath("$.items[0].purgedFromHot").value(true))
                .andExpect(jsonPath("$.items[0].itemCount").value(2))
                .andExpect(jsonPath("$.items[0].files[0].sha256").value(segment.files().getFirst().sha256()));
        mvc.perform(get("/v1/archives?status=failed").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
        mvc.perform(get("/v1/archives?status=nope").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/v1/archives?cursor=garbage").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isBadRequest());
    }
}
