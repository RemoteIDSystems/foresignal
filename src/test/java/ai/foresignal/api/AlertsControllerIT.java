package ai.foresignal.api;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static ai.foresignal.testsupport.records.RecordsTestSources.PRIVATE;
import static ai.foresignal.testsupport.records.RecordsTestSources.PRIVATE_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.trigger.TriggerFixture;
import ai.foresignal.testsupport.trigger.TriggerTestSources;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertEvent;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.TriggerEngine;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The {@code Alerts} operations over HTTP, except the stream (prompt 017). */
@IntegrationTest
@AutoConfigureMockMvc
@Import(TriggerTestSources.class)
class AlertsControllerIT {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired AppSettingService settings;
    @Autowired MutableClock clock;
    @Autowired TestRecords records;
    @Autowired TriggerRuleService rules;
    @Autowired TriggerEngine engine;
    @Autowired ChangeBus changeBus;
    @Autowired AlertStore alerts;
    @Autowired AlertBus bus;
    @Autowired JdbcTemplate jdbc;

    String token;
    TriggerFixture fixture;
    UUID warning;
    UUID critical;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        token = adminTokens.rotate();
        clock.setInstant(T);
        fixture = new TriggerFixture(rules, engine, changeBus, alerts, bus);
        warning = fixture.rule("{'name':'Warn','sources':['" + STANDARD + "'],'tags':['ops'],"
                + "'conditions':[{'path':'mag','op':'>=','value':5}]}");
        critical = fixture.rule("{'name':'Crit','sources':['" + STANDARD + "'],'severity':'critical',"
                + "'conditions':[{'path':'mag','op':'>=','value':7}]}");
    }

    @AfterEach
    void tearDown() {
        fixture.close();
    }

    MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder request) {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    ResultActions send(MockHttpServletRequestBuilder request, String body) throws Exception {
        return mvc.perform(auth(request).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    void quake(String id, double mag) {
        records.ingest(STANDARD, NormalizedRecord.builder(id, STANDARD_KIND).observedAt(T).put("mag", mag).build());
    }

    AlertView only(UUID rule) {
        List<AlertView> list = fixture.alerts(rule);
        assertThat(list).hasSize(1);
        return list.getFirst();
    }

    @Nested
    @DisplayName("listAlerts and getAlert")
    class Reads {

        @Test
        @DisplayName("newest first with filters, and keyset pagination")
        void list() throws Exception {
            quake("a", 5.5);
            quake("b", 7.5);
            fixture.settle();
            mvc.perform(get("/v1/alerts")).andExpect(status().isOk()).andExpect(jsonPath("$.items", hasSize(3)))
                    .andExpect(jsonPath("$.items[0].externalId", is("b")))
                    .andExpect(jsonPath("$.nextCursor", nullValue()));
            mvc.perform(get("/v1/alerts?ruleId=" + critical)).andExpect(jsonPath("$.items", hasSize(1)));
            mvc.perform(get("/v1/alerts?minSeverity=critical")).andExpect(jsonPath("$.items", hasSize(1)))
                    .andExpect(jsonPath("$.items[0].severity", is("critical")));
            mvc.perform(get("/v1/alerts?tag=ops")).andExpect(jsonPath("$.items", hasSize(2)));
            mvc.perform(get("/v1/alerts?source=" + STANDARD + "&kind=" + STANDARD_KIND)).andExpect(jsonPath("$.items", hasSize(3)));
            mvc.perform(get("/v1/alerts?source=other")).andExpect(jsonPath("$.items", hasSize(0)));
            mvc.perform(get("/v1/alerts?state=resolved")).andExpect(jsonPath("$.items", hasSize(0)));
            mvc.perform(get("/v1/alerts?acknowledged=false")).andExpect(jsonPath("$.items", hasSize(3)));
            mvc.perform(get("/v1/alerts?since=2026-05-01T13:00:00Z")).andExpect(jsonPath("$.items", hasSize(0)));

            String page1 = mvc.perform(get("/v1/alerts?limit=2")).andExpect(jsonPath("$.items", hasSize(2)))
                    .andExpect(jsonPath("$.nextCursor", notNullValue())).andReturn().getResponse().getContentAsString();
            String cursor = JSON.readTree(page1).get("nextCursor").asString();
            mvc.perform(get("/v1/alerts?limit=2&cursor=" + cursor)).andExpect(jsonPath("$.items", hasSize(1)))
                    .andExpect(jsonPath("$.nextCursor", nullValue()));
            mvc.perform(get("/v1/alerts?limit=3&cursor=" + cursor)).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("bad filters are 400")
        void badFilters() throws Exception {
            mvc.perform(get("/v1/alerts?state=open")).andExpect(status().isBadRequest());
            mvc.perform(get("/v1/alerts?feedback=meh")).andExpect(status().isBadRequest());
            mvc.perform(get("/v1/alerts?minSeverity=high")).andExpect(status().isBadRequest());
            mvc.perform(get("/v1/alerts?ruleId=nope")).andExpect(status().isBadRequest());
            mvc.perform(get("/v1/alerts?limit=5001")).andExpect(status().isBadRequest());
            mvc.perform(get("/v1/alerts?cursor=garbage")).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("getAlert loads the version by seq and falls back to the snapshot once it is gone")
        void get_() throws Exception {
            quake("g", 5.5);
            AlertView alert = only(warning);
            mvc.perform(get("/v1/alerts/" + alert.id())).andExpect(status().isOk())
                    .andExpect(jsonPath("$.record.seq", is((int) alert.seq())))
                    .andExpect(jsonPath("$.record.payload.mag", is(5.5)))
                    .andExpect(jsonPath("$.recordSnapshot.payload.mag", is(5.5)))
                    .andExpect(jsonPath("$.ruleName", is("Warn")))
                    .andExpect(jsonPath("$.state", is("firing")));
            // Simulates retention removing the version.
            jdbc.update("DELETE FROM record_version WHERE seq = ?", alert.seq());
            mvc.perform(get("/v1/alerts/" + alert.id())).andExpect(status().isOk())
                    .andExpect(jsonPath("$.record").doesNotExist())
                    .andExpect(jsonPath("$.recordSnapshot.kind", is(STANDARD_KIND)));
            mvc.perform(get("/v1/alerts/999999")).andExpect(status().isNotFound());
            mvc.perform(get("/v1/alerts/abc")).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("anonymous callers never see alerts of private sources")
        void privateSources() throws Exception {
            UUID rule = fixture.rule("{'name':'Mine','sources':['" + PRIVATE + "']}");
            records.ingest(PRIVATE, NormalizedRecord.builder("p", PRIVATE_KIND).observedAt(T).point(1, 1).build());
            AlertView alert = only(rule);
            mvc.perform(get("/v1/alerts?ruleId=" + rule)).andExpect(jsonPath("$.items", hasSize(0)));
            mvc.perform(get("/v1/alerts/" + alert.id())).andExpect(status().isNotFound());
            mvc.perform(auth(get("/v1/alerts?ruleId=" + rule))).andExpect(jsonPath("$.items", hasSize(1)));
            mvc.perform(auth(get("/v1/alerts/" + alert.id()))).andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("lifecycle")
    class Lifecycle {

        @Test
        @DisplayName("ack is idempotent and records note and feedback; resolve is terminal and idempotent")
        void ackAndResolve() throws Exception {
            quake("l", 5.5);
            AlertView alert = only(warning);
            send(post("/v1/alerts/" + alert.id() + "/ack"), "{\"note\":\"seen\",\"feedback\":\"useful\"}")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.state", is("acknowledged")))
                    .andExpect(jsonPath("$.acknowledgedBy", is("admin")))
                    .andExpect(jsonPath("$.note", is("seen")))
                    .andExpect(jsonPath("$.feedback", is("useful")));
            long rev = alerts.find(alert.id()).orElseThrow().rev();
            mvc.perform(auth(post("/v1/alerts/" + alert.id() + "/ack"))).andExpect(status().isOk());
            assertThat(alerts.find(alert.id()).orElseThrow().rev()).as("a bare repeat changes nothing").isEqualTo(rev);
            fixture.awaitEvent(AlertEvent.ACKNOWLEDGED, alert.id());

            mvc.perform(auth(post("/v1/alerts/" + alert.id() + "/resolve"))).andExpect(status().isOk())
                    .andExpect(jsonPath("$.state", is("resolved")))
                    .andExpect(jsonPath("$.resolvedBy", is("admin")))
                    .andExpect(jsonPath("$.resolvedAt", is("2026-05-01T12:00:00Z")));
            fixture.awaitEvent(AlertEvent.RESOLVED, alert.id());
            long resolvedRev = alerts.find(alert.id()).orElseThrow().rev();
            mvc.perform(auth(post("/v1/alerts/" + alert.id() + "/resolve"))).andExpect(status().isOk());
            mvc.perform(auth(post("/v1/alerts/" + alert.id() + "/ack"))).andExpect(status().isOk())
                    .andExpect(jsonPath("$.state", is("resolved")));
            assertThat(alerts.find(alert.id()).orElseThrow().rev()).isEqualTo(resolvedRev);

            mvc.perform(get("/v1/triggers/" + warning)).andExpect(jsonPath("$.feedbackCounts.useful", is(1)))
                    .andExpect(jsonPath("$.openAlertCount", is(0)));
        }

        @Test
        @DisplayName("snooze, unsnooze and feedback")
        void snoozeAndFeedback() throws Exception {
            quake("s", 5.5);
            AlertView alert = only(warning);
            send(post("/v1/alerts/" + alert.id() + "/snooze"), "{\"for\":\"1h\"}").andExpect(status().isOk())
                    .andExpect(jsonPath("$.snoozedUntil", is("2026-05-01T13:00:00Z")));
            fixture.awaitEvent(AlertEvent.SNOOZED, alert.id());
            send(post("/v1/alerts/" + alert.id() + "/snooze"), "{\"until\":\"2026-04-01T00:00:00Z\"}").andExpect(status().isBadRequest());
            mvc.perform(auth(delete("/v1/alerts/" + alert.id() + "/snooze"))).andExpect(status().isOk())
                    .andExpect(jsonPath("$.snoozedUntil", nullValue()));
            mvc.perform(auth(delete("/v1/alerts/" + alert.id() + "/snooze"))).andExpect(status().isOk());
            send(put("/v1/alerts/" + alert.id() + "/feedback"), "{\"feedback\":\"noise\",\"note\":\"routine\"}")
                    .andExpect(status().isOk()).andExpect(jsonPath("$.feedback", is("noise")))
                    .andExpect(jsonPath("$.note", is("routine"))).andExpect(jsonPath("$.feedbackAt").exists());
            send(put("/v1/alerts/" + alert.id() + "/feedback"), "{\"note\":\"x\"}").andExpect(status().isBadRequest());
            send(put("/v1/alerts/" + alert.id() + "/feedback"), "{\"feedback\":\"meh\"}").andExpect(status().isBadRequest());
            send(post("/v1/alerts/" + alert.id() + "/ack"), "{\"note\":\"" + "x".repeat(2001) + "\"}")
                    .andExpect(status().isBadRequest());
            JsonNode listed = JSON.readTree(mvc.perform(get("/v1/alerts?feedback=noise")).andReturn().getResponse().getContentAsString());
            assertThat(listed.get("items")).hasSize(1);
        }

        @Test
        @DisplayName("lifecycle writes are 401 without a token and 404 for an unknown alert")
        void errors() throws Exception {
            quake("e", 5.5);
            AlertView alert = only(warning);
            mvc.perform(post("/v1/alerts/" + alert.id() + "/ack")).andExpect(status().isUnauthorized());
            mvc.perform(post("/v1/alerts/" + alert.id() + "/resolve")).andExpect(status().isUnauthorized());
            mvc.perform(post("/v1/alerts/" + alert.id() + "/snooze").contentType(MediaType.APPLICATION_JSON).content("{\"for\":\"1h\"}"))
                    .andExpect(status().isUnauthorized());
            mvc.perform(delete("/v1/alerts/" + alert.id() + "/snooze")).andExpect(status().isUnauthorized());
            mvc.perform(put("/v1/alerts/" + alert.id() + "/feedback").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"feedback\":\"late\"}")).andExpect(status().isUnauthorized());
            mvc.perform(auth(post("/v1/alerts/424242/ack"))).andExpect(status().isNotFound());
            mvc.perform(auth(post("/v1/alerts/424242/resolve"))).andExpect(status().isNotFound());
            send(put("/v1/alerts/424242/feedback"), "{\"feedback\":\"late\"}").andExpect(status().isNotFound());
        }
    }
}
