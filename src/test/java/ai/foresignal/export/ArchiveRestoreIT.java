package ai.foresignal.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * {@code restoreArchive} (prompt R2-016): a purged segment is pulled back from the external disk, checked, and its
 * rows loaded unchanged into the restore area; the segment is marked {@code restored}.
 */
@IntegrationTest
@Import(RetentionTestSources.class)
class ArchiveRestoreIT extends ArchiveRunItSupport {

    private ArchiveSegment archived() {
        seed();
        enable(root, STD);
        atNow();
        return awaitSettled(runner.start(new ArchiveRunner.Request(cutoff(), List.of(), false),
                ArchiveRunner.MANUAL).id());
    }

    @Test
    @DisplayName("a restore reloads the archived rows unchanged and marks the segment restored")
    void restoreReloads() throws Exception {
        Map<String, Object> before = jdbc.queryForMap("SELECT seq, kind, observed_at, payload::text AS payload,"
                + " ST_AsText(geom) AS geom FROM record_version WHERE source_id = ? AND external_id = 'd1'", STD);
        ArchiveSegment segment = archived();
        assertThat(segment.status()).isEqualTo(ArchiveSegment.PURGED);

        mvc.perform(post("/v1/archives/" + segment.id() + "/restore").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"confirm\":true}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("restoring"));
        ArchiveSegment restored = awaitSettled(segment.id());

        assertThat(restored.status()).isEqualTo(ArchiveSegment.RESTORED);
        assertThat(restored.restoredAt()).isNotNull();
        assertThat(restored.purgedFromHot()).isTrue();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM record_version_restored WHERE archive_id = ?",
                Long.class, segment.id())).isEqualTo(segment.itemCount());
        Map<String, Object> after = jdbc.queryForMap("SELECT seq, kind, observed_at, payload::text AS payload,"
                + " ST_AsText(geom) AS geom FROM record_version_restored WHERE archive_id = ? AND external_id = 'd1'",
                segment.id());
        assertThat(after).isEqualTo(before);
        // Nothing went back into record_version, and the restore copy was cleaned up.
        assertThat(versions(STD, "d1")).isZero();
        assertThat(Files.exists(store.resolve("archive-restore/" + segment.id()))).isFalse();
    }

    @Test
    @DisplayName("confirm is required, a second restore is 409 ALREADY_RESTORED, an unknown id is 404")
    void guards() throws Exception {
        ArchiveSegment segment = archived();
        mvc.perform(post("/v1/archives/" + segment.id() + "/restore").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"confirm\":false}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/v1/archives/" + segment.id() + "/restore").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isBadRequest());

        runner.restore(segment.id());
        awaitSettled(segment.id());
        mvc.perform(post("/v1/archives/" + segment.id() + "/restore").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"confirm\":true}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ALREADY_RESTORED"));
        mvc.perform(post("/v1/archives/01JUNKNOWNUNKNOWNUNKNOWN00/restore").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"confirm\":true}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("UNKNOWN_ARCHIVE"));
    }

    @Test
    @DisplayName("a corrupted archive copy fails the restore and leaves the segment as it was")
    void corruptedCopy() throws Exception {
        ArchiveSegment segment = archived();
        Files.writeString(root.resolve(segment.files().getFirst().key()), "corrupted");

        runner.restore(segment.id());
        ArchiveSegment after = awaitSettled(segment.id());

        assertThat(after.status()).isEqualTo(ArchiveSegment.PURGED);
        assertThat(after.error()).startsWith("The restore failed");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM record_version_restored WHERE archive_id = ?",
                Long.class, segment.id())).isZero();
    }
}
