package ai.foresignal.backup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.export.ExportManifests;
import ai.foresignal.export.ExportStore;
import ai.foresignal.notify.SystemEvent;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code runBackup} and {@code listBackups} (prompt R2-017): a backup writes checksummed files to the target and a
 * {@code backup_run} row; a missing {@code pg_dump} fails the backup with an alert and the server stays up;
 * {@code keepBackups} prunes older backups with their files; the settings gate and the one-at-a-time guard.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RetentionTestSources.class)
@RecordApplicationEvents
class BackupServiceIT extends BackupItSupport {

    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired ApplicationEvents events;
    @Autowired ExportStore exportStore;

    private String startedId(String body) throws Exception {
        String answer = send(post("/v1/backups/run"), body)
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("pending"))
                .andReturn().getResponse().getContentAsString();
        return JSON.readTree(answer).get("id").asString();
    }

    @Test
    @DisplayName("a full backup writes a checksummed dump and config bundle to the target and records them")
    void writesChecksummedFiles() throws Exception {
        enable(7);
        BackupRun run = awaitSettled(startedId("{'label':'nightly'}"));

        assertThat(run.status()).isEqualTo(BackupRun.COMPLETE);
        assertThat(run.type()).isEqualTo(BackupRun.FULL);
        assertThat(run.trigger()).isEqualTo(BackupRun.MANUAL);
        assertThat(run.label()).isEqualTo("nightly");
        assertThat(run.target()).isEqualTo(BackupSettings.EXTERNAL);
        assertThat(run.files()).extracting(BackupRun.BackupFile::name)
                .containsExactlyInAnyOrder(BackupRun.DATABASE_FILE, BackupRun.CONFIG_FILE);
        long total = 0;
        for (BackupRun.BackupFile file : run.files()) {
            Path stored = root.resolve("backups").resolve(run.id()).resolve(file.name());
            assertThat(stored).exists();
            assertThat(ExportManifests.sha256(stored)).isEqualTo(file.sha256());
            assertThat(Files.size(stored)).isEqualTo(file.bytes());
            total += file.bytes();
        }
        assertThat(run.sizeBytes()).isEqualTo(total);
        assertThat(run.checksum()).isEqualTo(BackupService.checksum(run.files()));
        assertThat(run.location()).isEqualTo(root.resolve("backups").resolve(run.id()).toString());
        assertThat(run.startedAt()).isNotNull();
        assertThat(run.finishedAt()).isNotNull();

        // pg_dump got the custom format and the connection, and the password only through its environment.
        String dump = Files.readString(root.resolve("backups").resolve(run.id()).resolve(BackupRun.DATABASE_FILE));
        assertThat(dump).contains("--format=custom").contains("--dbname").contains("password-in-env")
                .doesNotContain("--password");
        // The staging copy is gone.
        assertThat(Files.exists(exportStore.resolve("backup-staging/" + run.id()))).isFalse();

        mvc.perform(get("/v1/backups").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(run.id()))
                .andExpect(jsonPath("$.items[0].status").value("complete"))
                .andExpect(jsonPath("$.items[0].type").value("full"))
                .andExpect(jsonPath("$.items[0].checksum").value(run.checksum()));
        mvc.perform(get("/v1/backups?type=config").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
        mvc.perform(get("/v1/settings/backup").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastRunAt").isNotEmpty());
    }

    @Test
    @DisplayName("without pg_dump a database backup fails with the reason and an alert, and the server stays up")
    void missingPgDump() throws Exception {
        enable(7);
        backups.commands(bin.resolve("no-such-pg_dump").toString(), "pg_restore");

        BackupRun run = awaitSettled(startedId("{'type':'database'}"));

        assertThat(run.status()).isEqualTo(BackupRun.FAILED);
        assertThat(run.error()).contains("was not found on the PATH");
        assertThat(run.files()).isEmpty();
        assertThat(events.stream(SystemEvent.class).filter(e -> e.subtype().equals("system.backup_failed")))
                .hasSize(1);
        mvc.perform(get("/v1/backups").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].status").value("failed"))
                .andExpect(jsonPath("$.items[0].error").isNotEmpty());

        // A config backup still works, and recovers the alert.
        BackupRun config = awaitSettled(startedId("{'type':'config'}"));
        assertThat(config.status()).isEqualTo(BackupRun.COMPLETE);
        assertThat(events.stream(SystemEvent.class).filter(e -> e.subtype().equals("system.backup_recovered")))
                .hasSize(1);
    }

    @Test
    @DisplayName("keepBackups keeps the newest complete backups of a type and deletes the rest with their files")
    void keepPrunes() throws Exception {
        enable(2);
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            clock.setInstant(T0.plusSeconds(60L * i));
            ids.add(awaitSettled(startedId("{'type':'database'}")).id());
        }

        assertThat(runs.ofType(BackupRun.DATABASE)).extracting(BackupRun::id)
                .containsExactly(ids.get(2), ids.get(1));
        assertThat(root.resolve("backups").resolve(ids.get(0))).doesNotExist();
        assertThat(root.resolve("backups").resolve(ids.get(1)).resolve(BackupRun.DATABASE_FILE)).exists();
    }

    @Test
    @DisplayName("backups off is 409 BACKUP_NOT_CONFIGURED; a bad request is one 400; one backup at a time")
    void gates() throws Exception {
        send(post("/v1/backups/run"), "{}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BACKUP_NOT_CONFIGURED"));
        enable(7);
        send(post("/v1/backups/run"), "{'type':'tape','label':7,'extra':true}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(3));
        mvc.perform(get("/v1/backups?type=tape").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/v1/backups?cursor=garbage").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isBadRequest());

        Path gate = bin.resolve("gate");
        script("slow_pg_dump", "#!/bin/sh\nwhile [ ! -f '" + gate + "' ]; do sleep 0.1; done\n"
                + "prev=''; for a in \"$@\"; do if [ \"$prev\" = '--file' ]; then echo PGDMP > \"$a\"; fi; prev=\"$a\"; done\n");
        backups.commands(bin.resolve("slow_pg_dump").toString(), "pg_restore");
        String id = startedId("{'type':'database'}");
        send(post("/v1/backups/run"), "{'type':'config'}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ALREADY_RUNNING"));
        Files.createFile(gate);
        assertThat(awaitSettled(id).status()).isEqualTo(BackupRun.COMPLETE);
    }

    @Test
    @DisplayName("the datasource URL is parsed into libpq options, never with the password")
    void connectionArgs() {
        BackupService.Connection db = BackupService.Connection.parse(
                "jdbc:postgresql://db.internal:6543/foresignal?sslmode=require", "fs", "s3cret");
        assertThat(db.args()).containsExactly("--host", "db.internal", "--port", "6543", "--dbname", "foresignal",
                "--username", "fs").doesNotContain("s3cret");
        assertThat(BackupService.Connection.parse("jdbc:postgresql://localhost/x", null, null).port()).isEqualTo(5432);
    }
}
