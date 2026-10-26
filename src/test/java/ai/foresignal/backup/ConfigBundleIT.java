package ai.foresignal.backup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.notify.ChannelService;
import ai.foresignal.settings.CorsSettingsService;
import ai.foresignal.store.NotificationChannelRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import java.nio.file.Files;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

/**
 * The config bundle of a backup (prompt R2-017): it is the {@code exportSettings} bundle, carries no plaintext
 * secret, and round-trips through {@code importSettings} when the backup is restored.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RetentionTestSources.class)
class ConfigBundleIT extends BackupItSupport {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final String SECRET = "https://hooks.example.invalid/services/T/B/BACKUPSECRETVALUE";
    static final String ORIGIN = "http://backup.example.invalid:5173";

    @Autowired ChannelService channels;
    @Autowired NotificationChannelRepository channelRows;
    @Autowired CorsSettingsService cors;

    @Test
    @DisplayName("a config backup holds the settings without secrets and restores them through importSettings")
    void roundTrip() throws Exception {
        String created = send(post("/v1/channels"),
                "{'name':'Slack #ops','type':'slack','secrets':{'webhookUrl':'" + SECRET + "'}}")
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID channel = UUID.fromString(JSON.readTree(created).get("id").asString());
        send(put("/v1/settings/cors"), "{'readOrigins':['*'],'writeOrigins':['" + ORIGIN + "']}")
                .andExpect(status().isOk());
        enable(7);

        String started = send(post("/v1/backups/run"), "{'type':'config'}")
                .andExpect(status().isAccepted())
                .andReturn().getResponse().getContentAsString();
        BackupRun run = awaitSettled(JSON.readTree(started).get("id").asString());
        assertThat(run.status()).isEqualTo(BackupRun.COMPLETE);
        assertThat(run.files()).extracting(BackupRun.BackupFile::name).containsExactly(BackupRun.CONFIG_FILE);

        String bundle = Files.readString(root.resolve("backups").resolve(run.id()).resolve(BackupRun.CONFIG_FILE));
        assertThat(bundle).contains(ORIGIN).contains(channel.toString())
                .doesNotContain("BACKUPSECRETVALUE").doesNotContain("\"secrets\"");
        assertThat(JSON.readTree(bundle).has("exportedAt")).isTrue();

        // Change the settings, then restore: the bundle puts them back, and the channel keeps its stored secret.
        send(put("/v1/settings/cors"), "{'readOrigins':['*'],'writeOrigins':[]}").andExpect(status().isOk());
        send(post("/v1/backups/" + run.id() + "/restore"), "{'confirm':true}")
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("restored"))
                .andExpect(jsonPath("$.restoredAt").isNotEmpty())
                .andExpect(jsonPath("$.importReport.channelsUpserted").value(1))
                .andExpect(jsonPath("$.importReport.warnings").isArray());

        assertThat(cors.get().writeOrigins()).containsExactly(ORIGIN);
        assertThat(channels.secrets(channelRows.findById(channel).orElseThrow())).containsEntry("webhookUrl", SECRET);
        assertThat(runs.find(run.id()).orElseThrow().status()).isEqualTo(BackupRun.RESTORED);
        assertThat(Files.exists(root.resolve("backups").resolve(run.id()).resolve(BackupRun.CONFIG_FILE))).isTrue();
    }

    @Test
    @DisplayName("a corrupted config bundle is 409 BACKUP_NOT_RESTORABLE and nothing is imported")
    void corruptedBundle() throws Exception {
        send(put("/v1/settings/cors"), "{'readOrigins':['*'],'writeOrigins':['" + ORIGIN + "']}")
                .andExpect(status().isOk());
        enable(7);
        BackupRun run = awaitSettled(backups.start(BackupRun.CONFIG, null, BackupRun.MANUAL).id());
        Files.writeString(root.resolve("backups").resolve(run.id()).resolve(BackupRun.CONFIG_FILE), "{}");
        send(put("/v1/settings/cors"), "{'readOrigins':['*'],'writeOrigins':[]}").andExpect(status().isOk());

        send(post("/v1/backups/" + run.id() + "/restore"), "{'confirm':true}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BACKUP_NOT_RESTORABLE"));

        assertThat(cors.get().writeOrigins()).isEmpty();
        BackupRun after = runs.find(run.id()).orElseThrow();
        assertThat(after.status()).isEqualTo(BackupRun.COMPLETE);
        assertThat(after.error()).startsWith("The restore failed");
        assertThat(backups.running()).isFalse();
    }
}
