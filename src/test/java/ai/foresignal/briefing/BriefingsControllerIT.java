package ai.foresignal.briefing;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.briefing.BriefingHttp;
import ai.foresignal.testsupport.briefing.BriefingItSupport;
import ai.foresignal.testsupport.briefing.BriefingTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;

/**
 * The {@code Briefings} operations over HTTP (prompt 029): CRUD with validation and warnings, scopes
 * (agent creates, read lists, run and preview always authenticated), run-now with
 * {@code BRIEFING_RUNNING}, previews that store and schedule nothing, and run pagination.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(BriefingTestSources.class)
class BriefingsControllerIT extends BriefingItSupport {

    @Autowired MockMvc mvc;
    @Autowired ai.foresignal.settings.SettingsTransferService transfer;
    @Autowired tools.jackson.databind.json.JsonMapper appJson;

    BriefingHttp http;

    @BeforeEach
    void setUpHttp() {
        http = new BriefingHttp(mvc);
    }

    JsonNode create(String body) throws Exception {
        return http.call("POST", "/v1/briefings", agent, body, 201);
    }

    @Test
    @DisplayName("create, get, list, replace and delete; next_run_at persisted; warnings returned")
    void crud() throws Exception {
        JsonNode created = create(digestBody("Morning", "0 7 * * *", null));
        String id = created.get("id").asString();
        assertThat(created.get("name").asString()).isEqualTo("Morning");
        assertThat(created.get("nextRunAt").asString()).isEqualTo("2026-10-13T07:00:00Z");
        assertThat(created.get("warnings").toString()).contains("no enabled channel");
        assertThat(created.at("/scope/kinds/0").asString()).isEqualTo(BriefingTestSources.QUAKE_KIND);
        assertThat(created.get("mode").asString()).isEqualTo("digest");

        assertThat(http.call("GET", "/v1/briefings/" + id, reader, null, 200).get("schedule").asString())
                .isEqualTo("0 7 * * *");
        assertThat(http.call("GET", "/v1/briefings", reader, null, 200)).hasSize(1);
        assertThat(http.call("GET", "/v1/briefings", null, null, 200)).as("open reads").hasSize(1);

        JsonNode replaced = http.call("PUT", "/v1/briefings/" + id, agent,
                "{\"name\":\"Evening\",\"schedule\":\"30 18 * * *\",\"timezone\":\"Europe/Berlin\",\"enabled\":false}", 200);
        assertThat(replaced.get("name").asString()).isEqualTo("Evening");
        assertThat(replaced.get("nextRunAt").isNull()).as("disabled: no next run").isTrue();
        assertThat(briefingRepository.findById(UUID.fromString(id)).orElseThrow().getTimezone()).isEqualTo("Europe/Berlin");

        http.call("DELETE", "/v1/briefings/" + id, agent, null, 204);
        http.call("GET", "/v1/briefings/" + id, reader, null, 404);
        http.call("GET", "/v1/briefings/not-a-uuid", reader, null, 404);
        http.call("DELETE", "/v1/briefings/" + id, agent, null, 404);
    }

    @Test
    @DisplayName("validation reports every problem at once; a channel not subscribed to briefing is a warning")
    void validation() throws Exception {
        JsonNode problem = http.call("POST", "/v1/briefings", agent, "{\"name\":\"x\",\"schedule\":\"*/5 * * * *\","
                + "\"timezone\":\"Nowhere\",\"scope\":{\"sources\":[\"nope\"]},\"lookback\":\"9d\"}", 400);
        assertThat(problem.get("code").asString()).isEqualTo("VALIDATION_FAILED");
        assertThat(problem.get("errors").toString()).contains("schedule").contains("timezone").contains("scope.sources")
                .contains("lookback");

        UUID alertsOnly = notify.channel("{'name':'alerts','type':'slack','events':['alert'],"
                + "'secrets':{'webhookUrl':'http://127.0.0.1:9/hook'}}");
        JsonNode created = create(digestBody("With channel", "0 7 * * *", "\"channelIds\":[\"" + alertsOnly + "\"]"));
        assertThat(created.get("warnings").toString()).contains("not subscribed to briefing");
    }

    @Test
    @DisplayName("scopes: a read token may not create or run; run and preview need a token even with open reads")
    void scopes() throws Exception {
        http.call("POST", "/v1/briefings", reader, digestBody("x", "0 7 * * *", null), 403);
        String id = create(digestBody("x", "0 7 * * *", null)).get("id").asString();
        http.call("POST", "/v1/briefings/" + id + "/run", reader, null, 403);
        http.call("POST", "/v1/briefings/" + id + "/run", null, null, 401);
        http.call("POST", "/v1/briefings/preview", null, digestBody("x", "0 7 * * *", null), 401);
        http.call("DELETE", "/v1/briefings/" + id, reader, null, 403);

        settings.put(SettingKeys.SECURITY, new SecuritySettings(false));
        http.call("GET", "/v1/briefings", null, null, 401);
        http.call("GET", "/v1/briefings", reader, null, 200);
    }

    @Test
    @DisplayName("run now stores a succeeded digest run, updates the briefing, and a concurrent run is 409")
    void runNow() throws Exception {
        quake("q1", "Tokyo Bay", 5.5);
        String id = create(digestBody("Now", "0 7 * * *", null)).get("id").asString();

        JsonNode run = http.call("POST", "/v1/briefings/" + id + "/run", agent, null, 200);
        assertThat(run.get("status").asString()).isEqualTo("succeeded");
        assertThat(run.get("mode").asString()).isEqualTo("digest");
        assertThat(run.get("contentMarkdown").asString()).contains("# Now").contains("Tokyo Bay");
        assertThat(run.at("/stats/counts/0/new").asLong()).isEqualTo(1);
        assertThat(run.get("citations").toString()).contains(BriefingTestSources.QUAKES + ":q1");
        assertThat(run.get("periodFrom").asString()).isEqualTo(clock.instant().minus(Duration.ofHours(24)).toString());

        BriefingEntity stored = briefingRepository.findById(UUID.fromString(id)).orElseThrow();
        assertThat(stored.getLastRunStatus()).isEqualTo("succeeded");
        assertThat(stored.getRunningSince()).isNull();
        assertThat(stored.getNextRunAt()).isEqualTo(Instant.parse("2026-10-13T07:00:00Z"));

        jdbc.update("UPDATE briefing SET running_since = ? WHERE id = ?", java.sql.Timestamp.from(clock.instant()),
                UUID.fromString(id));
        JsonNode busy = http.call("POST", "/v1/briefings/" + id + "/run", agent, null, 409);
        assertThat(busy.get("code").asString()).isEqualTo("BRIEFING_RUNNING");
        http.call("POST", "/v1/briefings/" + UUID.randomUUID() + "/run", agent, null, 404);
    }

    @Test
    @DisplayName("preview renders but stores, delivers and schedules nothing")
    void previewStoresNothing() throws Exception {
        quake("q1", "Tokyo Bay", 5.5);
        String id = create(digestBody("Kept", "0 7 * * *", null)).get("id").asString();
        Instant next = briefingRepository.findById(UUID.fromString(id)).orElseThrow().getNextRunAt();

        JsonNode preview = http.call("POST", "/v1/briefings/preview", agent, digestBody("Preview", "0 7 * * *", null), 200);
        assertThat(preview.get("id").isNull()).isTrue();
        assertThat(preview.get("briefingId").isNull()).isTrue();
        assertThat(preview.get("status").asString()).isEqualTo("succeeded");
        assertThat(preview.get("contentMarkdown").asString()).contains("# Preview").contains("Tokyo Bay");
        assertThat(runRepository.count()).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM notification", Long.class)).isZero();
        assertThat(briefingRepository.findById(UUID.fromString(id)).orElseThrow().getNextRunAt()).isEqualTo(next);
        assertThat(briefingRepository.count()).isEqualTo(1);

        http.call("POST", "/v1/briefings/preview", agent, "{\"name\":\"bad\"}", 400);
    }

    @Test
    @DisplayName("runs are listed newest first with a cursor; a deleted briefing's runs stay listed")
    void pagination() throws Exception {
        String id = create(digestBody("Paged", "0 7 * * *", null)).get("id").asString();
        List<String> runIds = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            clock.advance(Duration.ofMinutes(1));
            runIds.add(http.call("POST", "/v1/briefings/" + id + "/run", agent, null, 200).get("id").asString());
        }
        JsonNode first = http.call("GET", "/v1/briefings/" + id + "/runs?limit=2", reader, null, 200);
        assertThat(first.get("items")).hasSize(2);
        assertThat(first.at("/items/0/id").asString()).isEqualTo(runIds.get(2));
        assertThat(first.at("/items/1/id").asString()).isEqualTo(runIds.get(1));
        String cursor = first.get("nextCursor").asString();
        JsonNode second = http.call("GET", "/v1/briefings/" + id + "/runs?limit=2&cursor=" + cursor, reader, null, 200);
        assertThat(second.get("items")).hasSize(1);
        assertThat(second.at("/items/0/id").asString()).isEqualTo(runIds.get(0));
        assertThat(second.get("nextCursor").isNull()).isTrue();

        http.call("GET", "/v1/briefings/" + id + "/runs?cursor=garbage", reader, null, 400);
        http.call("GET", "/v1/briefings/" + id + "/runs?limit=0", reader, null, 400);

        http.call("DELETE", "/v1/briefings/" + id, agent, null, 204);
        assertThat(http.call("GET", "/v1/briefings/" + id + "/runs", reader, null, 200).get("items")).hasSize(3);
        http.call("GET", "/v1/briefings/" + UUID.randomUUID() + "/runs", reader, null, 404);
    }

    @Test
    @DisplayName("the second run's period starts where the previous run ended")
    void sinceLastRun() throws Exception {
        String id = create("{\"name\":\"Since\",\"schedule\":\"0 7 * * *\"}").get("id").asString();
        JsonNode first = http.call("POST", "/v1/briefings/" + id + "/run", agent, null, 200);
        assertThat(first.get("periodFrom").asString()).isEqualTo(clock.instant().minus(Duration.ofHours(24)).toString());
        Instant firstEnd = Instant.parse(first.get("periodTo").asString());
        clock.advance(Duration.ofHours(2));
        JsonNode second = http.call("POST", "/v1/briefings/" + id + "/run", agent, null, 200);
        assertThat(Instant.parse(second.get("periodFrom").asString())).isEqualTo(firstEnd);
    }

    @Test
    @DisplayName("settings export carries briefings with their ids, and import upserts them with an admin owner")
    void exportImport() throws Exception {
        String id = create(digestBody("Exported", "0 7 * * *", null)).get("id").asString();
        java.util.Map<String, Object> bundle = transfer.export();
        assertThat(bundle.get("briefings").toString()).contains(id).contains("Exported");

        tools.jackson.databind.node.ObjectNode tree =
                (tools.jackson.databind.node.ObjectNode) appJson.readTree(appJson.writeValueAsString(bundle));
        tools.jackson.databind.node.ObjectNode copy = ((tools.jackson.databind.node.ObjectNode) tree.get("briefings").get(0))
                .deepCopy();
        String newId = UUID.randomUUID().toString();
        copy.put("id", newId);
        copy.put("name", "Imported");
        ((tools.jackson.databind.node.ArrayNode) tree.get("briefings")).add(copy);

        var report = transfer.importBundle(tree);
        assertThat(report.briefingsUpserted()).isEqualTo(2);
        assertThat(report.warnings()).noneMatch(w -> w.contains("briefings"));
        BriefingEntity imported = briefingRepository.findById(UUID.fromString(newId)).orElseThrow();
        assertThat(imported.getName()).isEqualTo("Imported");
        assertThat(imported.getOwnerScope()).isEqualTo("admin");
        assertThat(imported.getNextRunAt()).isNotNull();
    }
}
