package ai.foresignal.backup;

import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.export.ExportStore;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.system.OperationalAlertService;
import ai.foresignal.testsupport.retention.RetentionItSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Duration;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * The shared fixture of prompt R2-017's backup ITs, in the shared 019 retention context: an admin token, an external
 * backup root per test, and stand-in {@code pg_dump} and {@code pg_restore} scripts, so no PostgreSQL client is needed
 * on the machine running the tests. The stand-in dump writes its arguments, and whether {@code PGPASSWORD} was set, into
 * the dump file; the stand-in restore writes its arguments to {@link #restoreLog}.
 */
abstract class BackupItSupport extends RetentionItSupport {

    /** The statuses a backup or restore settles in. */
    static final Set<String> SETTLED = Set.of(BackupRun.COMPLETE, BackupRun.FAILED, BackupRun.RESTORED);

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired BackupService backups;
    @Autowired BackupRunStore runs;
    @Autowired BackupSettingsService backupSettings;
    @Autowired OperationalAlertService alerts;

    /** {@code Bearer …} of an admin token. */
    String admin;

    /** The external backup root of this test. */
    Path root;

    /** The directory of the stand-in programs. */
    Path bin;

    /** Where the stand-in {@code pg_restore} writes its arguments. */
    Path restoreLog;

    /** A fresh root, stand-in programs and no raised alert. */
    @BeforeEach
    void backupFixture() throws Exception {
        admin = "Bearer " + adminTokens.rotate();
        root = Files.createTempDirectory("foresignal-backup-it");
        bin = Files.createTempDirectory("foresignal-backup-bin");
        restoreLog = bin.resolve("restore.log");
        Path dump = script("pg_dump", """
                #!/bin/sh
                out=""
                prev=""
                for a in "$@"; do
                  if [ "$prev" = "--file" ]; then out="$a"; fi
                  prev="$a"
                done
                {
                  echo "PGDMP stand-in"
                  echo "$@"
                  if [ -n "$PGPASSWORD" ]; then echo "password-in-env"; fi
                } > "$out"
                """);
        Path restore = script("pg_restore", "#!/bin/sh\necho \"$@\" > '" + restoreLog + "'\n");
        backups.commands(dump.toString(), restore.toString());
        alerts.reset();
    }

    /** Waits for the worker, then drops the settings, the history's files and the stand-ins. */
    @AfterEach
    void cleanUpBackups() {
        await().atMost(Duration.ofSeconds(60)).until(() -> !backups.running());
        backups.commands("pg_dump", "pg_restore");
        settings.remove(SettingKeys.BACKUP);
        settings.invalidateCache();
        ExportStore.deleteTree(root);
        ExportStore.deleteTree(bin);
    }

    /** Writes an executable script. */
    Path script(String name, String body) throws Exception {
        Path file = bin.resolve(name);
        Files.writeString(file, body);
        Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rwx------"));
        return file;
    }

    /** Sends {@code body} (single quotes for double) as admin. */
    ResultActions send(MockHttpServletRequestBuilder request, String body) throws Exception {
        return mvc.perform(request.header(HttpHeaders.AUTHORIZATION, admin).contentType(MediaType.APPLICATION_JSON)
                .content(body.replace('\'', '"')));
    }

    /** Turns backups on to the external disk at {@link #root}, keeping {@code keep} of each type. */
    void enable(int keep) throws Exception {
        send(put("/v1/settings/backup"), "{'enabled':true,'target':'external','root':'" + root + "','keepBackups':"
                + keep + ",'includeConfig':true}").andExpect(status().isOk());
    }

    /** Waits until backup {@code id} settles, and returns it. */
    BackupRun awaitSettled(String id) {
        await().atMost(Duration.ofSeconds(60)).until(() -> !backups.running()
                && runs.find(id).map(r -> SETTLED.contains(r.status())).orElse(false));
        return runs.find(id).orElseThrow();
    }
}
