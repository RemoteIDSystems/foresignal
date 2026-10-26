package ai.foresignal.backup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.retention.RetentionJob;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import java.nio.file.Files;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;

/**
 * {@code restoreBackup} (prompt R2-017): a database restore needs {@code confirm: true}, runs {@code pg_restore} on the
 * chosen dump and marks the backup {@code restored}; a failed restore keeps the backup restorable with the reason; a
 * config restore answers with its {@code ImportReport}; unknown, failed and busy backups are refused.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RetentionTestSources.class)
class BackupRestoreIT extends BackupItSupport {

    @Autowired RetentionJob job;

    private BackupRun backup(String type) throws Exception {
        enable(7);
        BackupRun run = awaitSettled(backups.start(type, null, BackupRun.MANUAL).id());
        assertThat(run.status()).isEqualTo(BackupRun.COMPLETE);
        return run;
    }

    @Test
    @DisplayName("a database restore refuses without confirm: true and runs nothing")
    void refusesWithoutConfirm() throws Exception {
        BackupRun run = backup(BackupRun.DATABASE);

        send(post("/v1/backups/" + run.id() + "/restore"), "{'confirm':false}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("confirm"));
        send(post("/v1/backups/" + run.id() + "/restore"), "{}")
                .andExpect(status().isBadRequest());
        mvc.perform(post("/v1/backups/" + run.id() + "/restore").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isBadRequest());
        send(post("/v1/backups/" + run.id() + "/restore"), "{'confirm':true,'force':true}")
                .andExpect(status().isBadRequest());

        assertThat(restoreLog).doesNotExist();
        assertThat(runs.find(run.id()).orElseThrow().status()).isEqualTo(BackupRun.COMPLETE);
    }

    @Test
    @DisplayName("a confirmed database restore runs pg_restore on the dump and marks the backup restored")
    void restoresDatabase() throws Exception {
        BackupRun run = backup(BackupRun.DATABASE);

        send(post("/v1/backups/" + run.id() + "/restore"), "{'confirm':true}")
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("restoring"));
        BackupRun restored = awaitSettled(run.id());

        assertThat(restored.status()).isEqualTo(BackupRun.RESTORED);
        assertThat(restored.restoredAt()).isNotNull();
        assertThat(restored.error()).isNull();
        String args = Files.readString(restoreLog);
        assertThat(args).contains("--clean").contains("--if-exists").contains("--single-transaction")
                .contains("--dbname").contains(BackupRun.DATABASE_FILE).doesNotContain("--password");
        // The fetched copy was cleaned up; the backup itself is still on the target.
        assertThat(root.resolve("backups").resolve(run.id()).resolve(BackupRun.DATABASE_FILE)).exists();
    }

    @Test
    @DisplayName("a failed pg_restore keeps the backup restorable, records why and raises the alert")
    void failedRestore() throws Exception {
        BackupRun run = backup(BackupRun.DATABASE);
        script("failing_pg_restore", "#!/bin/sh\necho 'could not connect to the server' >&2\nexit 1\n");
        backups.commands("pg_dump", bin.resolve("failing_pg_restore").toString());

        backups.restore(run.id());
        BackupRun after = awaitSettled(run.id());

        assertThat(after.status()).isEqualTo(BackupRun.COMPLETE);
        assertThat(after.error()).startsWith("The restore failed: pg_restore exited with 1")
                .contains("could not connect to the server");
        assertThat(after.restoredAt()).isNull();
    }

    @Test
    @DisplayName("a config restore answers with the ImportReport")
    void configRestore() throws Exception {
        BackupRun run = backup(BackupRun.CONFIG);

        send(post("/v1/backups/" + run.id() + "/restore"), "{'confirm':true}")
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("restored"))
                .andExpect(jsonPath("$.importReport.sourcesApplied").isArray())
                .andExpect(jsonPath("$.importReport.triggersUpserted").isNumber());
        assertThat(restoreLog).doesNotExist();
    }

    @Test
    @DisplayName("unknown is 404 UNKNOWN_BACKUP, failed is 409 BACKUP_NOT_RESTORABLE, busy is 409 ALREADY_RUNNING")
    void guards() throws Exception {
        send(post("/v1/backups/01JUNKNOWNUNKNOWNUNKNOWN00/restore"), "{'confirm':true}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("UNKNOWN_BACKUP"));

        enable(7);
        backups.commands(bin.resolve("no-such-pg_dump").toString(), "pg_restore");
        BackupRun failed = awaitSettled(backups.start(BackupRun.DATABASE, null, BackupRun.MANUAL).id());
        send(post("/v1/backups/" + failed.id() + "/restore"), "{'confirm':true}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BACKUP_NOT_RESTORABLE"));

        backups.commands(bin.resolve("pg_dump").toString(), bin.resolve("pg_restore").toString());
        BackupRun run = awaitSettled(backups.start(BackupRun.DATABASE, null, BackupRun.MANUAL).id());
        try (RetentionJob.Lease lease = job.lease()) {
            send(post("/v1/backups/" + run.id() + "/restore"), "{'confirm':true}")
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("ALREADY_RUNNING"));
        }
        assertThat(backups.running()).isFalse();
        assertThat(runs.find(run.id()).orElseThrow().status()).isEqualTo(BackupRun.COMPLETE);
    }
}
