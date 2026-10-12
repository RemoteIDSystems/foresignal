package ai.foresignal.api;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.notify.ChannelService;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.CorsSettingsService;
import ai.foresignal.settings.WatchdogSettingsService;
import ai.foresignal.source.SourceService;
import ai.foresignal.store.NotificationChannelRepository;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.retention.RetentionItSupport;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import ai.foresignal.trigger.TriggerRuleService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/** {@code exportSettings} and {@code importSettings} (prompt 019). */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RetentionTestSources.class)
class SettingsExportImportIT extends RetentionItSupport {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final String SECRET = "https://hooks.example.invalid/services/T/B/SECRETVALUE";
    static final String RULE = "{'name':'Big','sources':['" + STANDARD + "'],'conditions':[{'path':'v','op':'>=','value':6}]}";

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired SourceService sources;
    @Autowired TriggerRuleService triggers;
    @Autowired TriggerRuleRepository triggerRows;
    @Autowired ChannelService channels;
    @Autowired NotificationChannelRepository channelRows;
    @Autowired CorsSettingsService cors;
    @Autowired WatchdogSettingsService watchdog;

    String token;

    @BeforeEach
    void rotate() {
        token = adminTokens.rotate();
    }

    ResultActions send(MockHttpServletRequestBuilder request, String body) throws Exception {
        return mvc.perform(request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body.replace('\'', '"')));
    }

    JsonNode export() throws Exception {
        String body = mvc.perform(get("/v1/settings/export").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JSON.readTree(body);
    }

    UUID slackChannel() throws Exception {
        String body = send(post("/v1/channels"),
                "{'name':'Slack #ops','type':'slack','secrets':{'webhookUrl':'" + SECRET + "'}}")
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(JSON.readTree(body).get("id").asString());
    }

    @Test
    @DisplayName("export carries every section, never a secret or a credential")
    void exportShape() throws Exception {
        sources.setCredentials(CollectorTestSources.KEYED, java.util.Map.of("apiKey", "SUPER-SECRET-KEY"));
        send(post("/v1/triggers"), RULE).andExpect(status().isCreated());
        slackChannel();

        String raw = mvc.perform(get("/v1/settings/export").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.foresignalVersion").isString())
                .andExpect(jsonPath("$.exportedAt").isString())
                .andExpect(jsonPath("$.sources." + STANDARD + ".enabled").isBoolean())
                .andExpect(jsonPath("$.retention.default").value("7d"))
                .andExpect(jsonPath("$.retention.kinds").isMap())
                .andExpect(jsonPath("$.cors.readOrigins").isArray())
                .andExpect(jsonPath("$.notifications.pauseBypassSeverity").value("critical"))
                .andExpect(jsonPath("$.watchdog.interval").value("1m"))
                .andExpect(jsonPath("$.triggers[0].id").isString())
                .andExpect(jsonPath("$.triggers[0].name").value("Big"))
                .andExpect(jsonPath("$.channels[0].id").isString())
                .andExpect(jsonPath("$.channels[0].type").value("slack"))
                .andReturn().getResponse().getContentAsString();

        assertThat(raw).doesNotContain("SECRETVALUE").doesNotContain("SUPER-SECRET-KEY").doesNotContain("\"secrets\"");
    }

    @Test
    @DisplayName("an export imports back unchanged, upserting triggers and channels by id and keeping channel secrets")
    void roundTrip() throws Exception {
        send(put("/v1/settings/retention"), "{'default':'10d','sources':{'" + STANDARD + "':'45d'}}").andExpect(status().isOk());
        send(put("/v1/settings/cors"), "{'readOrigins':['*'],'writeOrigins':['http://localhost:5173']}").andExpect(status().isOk());
        send(post("/v1/triggers"), RULE).andExpect(status().isCreated());
        UUID channel = slackChannel();
        JsonNode bundle = export();
        String ruleId = bundle.get("triggers").get(0).get("id").asString();

        // Change things, then restore them from the bundle.
        send(put("/v1/settings/retention"), "{'default':'20d'}").andExpect(status().isOk());
        triggerRows.deleteAll();

        send(put("/v1/settings/import"), bundle.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourcesApplied", hasItem(STANDARD)))
                .andExpect(jsonPath("$.sourcesUnknown").isEmpty())
                .andExpect(jsonPath("$.triggersUpserted").value(1))
                .andExpect(jsonPath("$.channelsUpserted").value(1));

        assertThat(triggerRows.findById(UUID.fromString(ruleId))).isPresent();
        assertThat(channels.secrets(channelRows.findById(channel).orElseThrow())).containsEntry("webhookUrl", SECRET);
        JsonNode again = export();
        assertThat(again.get("retention")).isEqualTo(bundle.get("retention"));
        assertThat(again.get("cors")).isEqualTo(bundle.get("cors"));
        assertThat(again.get("sources").get(STANDARD).get("retention").asString()).isEqualTo("45d");
    }

    @Test
    @DisplayName("unknown sources are reported, not fatal")
    void unknownSources() throws Exception {
        send(put("/v1/settings/import"), "{'sources':{'no-such-source':{'enabled':true}," + "'" + STANDARD + "':{'enabled':false}}}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourcesUnknown[0]").value("no-such-source"))
                .andExpect(jsonPath("$.sourcesApplied[0]").value(STANDARD));
    }

    @Test
    @DisplayName("enabling a source without its required credentials imports it disabled, with a warning")
    void credentiallessEnableDowngraded() throws Exception {
        send(put("/v1/settings/import"), "{'sources':{'" + CollectorTestSources.KEYED + "':{'enabled':true}}}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourcesApplied[0]").value(CollectorTestSources.KEYED))
                .andExpect(jsonPath("$.warnings[0]").value(org.hamcrest.Matchers.containsString("credentials")));

        assertThat(sources.view(CollectorTestSources.KEYED).enabled()).isFalse();
    }

    @Test
    @DisplayName("any validation error is a 400 with every error and nothing applied")
    void atomicFailure() throws Exception {
        String before = cors.get().writeOrigins().toString();
        ObjectNode bundle = (ObjectNode) JSON.readTree(("{'sources':{'" + STANDARD + "':{'enabled':true}},"
                + "'cors':{'readOrigins':['*'],'writeOrigins':['http://localhost:5173']},"
                + "'retention':{'default':'10m','sources':{'" + TRACK + "':'30d'}},"
                + "'watchdog':{'interval':'5s'},"
                + "'triggers':[{'name':'no id'}],"
                + "'channels':[{'id':'" + UUID.randomUUID() + "','name':'x','type':'slack'}]}").replace('\'', '"'));

        send(put("/v1/settings/import"), bundle.toString())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RETENTION_TIER_FIXED"))
                .andExpect(jsonPath("$.errors[*].field", hasItem("retention.default")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("retention.sources." + TRACK)))
                .andExpect(jsonPath("$.errors[*].field", hasItem("watchdog.interval")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("triggers[0].id")));

        assertThat(cors.get().writeOrigins().toString()).isEqualTo(before);
        assertThat(sources.view(STANDARD).enabled()).isFalse();
        assertThat(watchdog.get().interval()).isEqualTo(java.time.Duration.ofSeconds(60));
        assertThat(channelRows.count()).isZero();
    }
}
