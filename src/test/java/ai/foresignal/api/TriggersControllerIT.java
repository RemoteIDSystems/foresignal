package ai.foresignal.api;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static ai.foresignal.testsupport.trigger.TriggerTestSources.VOLATILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.TriggerEngine;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Instant;
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

/** The {@code Triggers} operations over HTTP (prompt 017). */
@IntegrationTest
@AutoConfigureMockMvc
@Import(TriggerTestSources.class)
class TriggersControllerIT {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();
    static final String BIG = "{\"name\":\"Big\",\"sources\":[\"" + STANDARD + "\"],\"tags\":[\"quakes\"],"
            + "\"conditions\":[{\"path\":\"mag\",\"op\":\">=\",\"value\":6}]}";

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

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        token = adminTokens.rotate();
        clock.setInstant(T);
        fixture = new TriggerFixture(rules, engine, changeBus, alerts, bus);
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

    String create(String body) throws Exception {
        String json = send(post("/v1/triggers"), body).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JSON.readTree(json).get("id").asString();
    }

    @Nested
    @DisplayName("CRUD")
    class Crud {

        @Test
        @DisplayName("createTrigger is 201 with Location and the whole TriggerRule")
        void createTrigger() throws Exception {
            send(post("/v1/triggers"), BIG)
                    .andExpect(status().isCreated())
                    .andExpect(header().string(HttpHeaders.LOCATION, startsWith("/v1/triggers/")))
                    .andExpect(jsonPath("$.name", is("Big")))
                    .andExpect(jsonPath("$.sources[0]", is(STANDARD)))
                    .andExpect(jsonPath("$.mode", is("match")))
                    .andExpect(jsonPath("$.severity", is("warning")))
                    .andExpect(jsonPath("$.cooldown", is("0s")))
                    .andExpect(jsonPath("$.conditions[0].op", is(">=")))
                    .andExpect(jsonPath("$.messageTemplate", is("{{name}}: {{source}}/{{externalId}}")))
                    .andExpect(jsonPath("$.alertCount", is(0)))
                    .andExpect(jsonPath("$.openAlertCount", is(0)))
                    .andExpect(jsonPath("$.suppressedCount", is(0)))
                    .andExpect(jsonPath("$.feedbackCounts.useful", is(0)))
                    .andExpect(jsonPath("$.snoozedUntil", nullValue()))
                    .andExpect(jsonPath("$.createdAt").exists());
        }

        @Test
        @DisplayName("a route geofence comes back with its corridor, its original line and the bufferMeters")
        void route() throws Exception {
            send(post("/v1/triggers"), "{\"name\":\"Route\",\"kinds\":[\"aircraft\"],\"conditions\":[{\"path\":\"callsign\","
                    + "\"op\":\"in\",\"value\":[\"N1\",\"N2\"]}],\"geofence\":{\"type\":\"LineString\",\"coordinates\":"
                    + "[[-97.75,30.25],[-97.0,32.9]]},\"bufferMeters\":5000,\"mode\":\"enter\"}")
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.geofence.type", is("MultiPolygon")))
                    .andExpect(jsonPath("$.geofenceRoute.type", is("LineString")))
                    .andExpect(jsonPath("$.bufferMeters", is(5000.0)))
                    .andExpect(jsonPath("$.reappearAsEnter", is(true)))
                    .andExpect(jsonPath("$.autoResolve.onInverseEdge", is(true)));
        }

        @Test
        @DisplayName("get, list with tag and enabled filters, full replace and delete keeping alerts")
        void lifecycle() throws Exception {
            String id = create(BIG);
            create("{\"name\":\"Other\",\"enabled\":false,\"sources\":[\"" + STANDARD + "\"]}");
            mvc.perform(get("/v1/triggers/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.id", is(id)));
            mvc.perform(get("/v1/triggers")).andExpect(jsonPath("$", hasSize(2)));
            mvc.perform(get("/v1/triggers?tag=quakes")).andExpect(jsonPath("$", hasSize(1)));
            mvc.perform(get("/v1/triggers?enabled=false")).andExpect(jsonPath("$[0].name", is("Other")));

            send(put("/v1/triggers/" + id), "{\"name\":\"Bigger\",\"sources\":[\"" + STANDARD + "\"],"
                    + "\"conditions\":[{\"path\":\"mag\",\"op\":\">=\",\"value\":7}]}")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Bigger")))
                    .andExpect(jsonPath("$.tags", hasSize(0)));

            records.ingest(STANDARD, NormalizedRecord.builder("q", STANDARD_KIND).observedAt(T).put("mag", 7.5).build());
            assertThat(fixture.alerts(UUID.fromString(id))).hasSize(1);
            mvc.perform(get("/v1/triggers/" + id)).andExpect(jsonPath("$.alertCount", is(1)))
                    .andExpect(jsonPath("$.lastFiredAt").exists());

            mvc.perform(auth(delete("/v1/triggers/" + id))).andExpect(status().isNoContent());
            mvc.perform(get("/v1/triggers/" + id)).andExpect(status().isNotFound());
            assertThat(jdbc.queryForObject("SELECT count(*) FROM alert", Integer.class)).as("no cascade").isEqualTo(1);
            mvc.perform(auth(delete("/v1/triggers/" + id))).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("an unknown or malformed rule id is 404")
        void notFound() throws Exception {
            mvc.perform(get("/v1/triggers/" + UUID.randomUUID())).andExpect(status().isNotFound());
            mvc.perform(get("/v1/triggers/not-a-uuid")).andExpect(status().isNotFound());
            send(put("/v1/triggers/" + UUID.randomUUID()), BIG).andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("auth and validation")
    class Errors {

        @Test
        @DisplayName("every write is 401 without a token; reads are open")
        void unauthorized() throws Exception {
            String id = create(BIG);
            mvc.perform(post("/v1/triggers").contentType(MediaType.APPLICATION_JSON).content(BIG)).andExpect(status().isUnauthorized());
            mvc.perform(put("/v1/triggers/" + id).contentType(MediaType.APPLICATION_JSON).content(BIG)).andExpect(status().isUnauthorized());
            mvc.perform(delete("/v1/triggers/" + id)).andExpect(status().isUnauthorized());
            mvc.perform(post("/v1/triggers/" + id + "/test")).andExpect(status().isUnauthorized());
            mvc.perform(post("/v1/triggers/" + id + "/snooze").contentType(MediaType.APPLICATION_JSON).content("{\"for\":\"1h\"}"))
                    .andExpect(status().isUnauthorized());
            mvc.perform(delete("/v1/triggers/" + id + "/snooze")).andExpect(status().isUnauthorized());
            mvc.perform(get("/v1/triggers")).andExpect(status().isOk());
        }

        @Test
        @DisplayName("400s carry their codes and field errors")
        void badRequests() throws Exception {
            send(post("/v1/triggers"), "{\"name\":\"\"}").andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")))
                    .andExpect(jsonPath("$.errors[0].field").exists());
            send(post("/v1/triggers"), "{\"name\":\"v\",\"sources\":[\"" + VOLATILE + "\"],\"conditions\":"
                    + "[{\"path\":\"fetchedAt\",\"op\":\"exists\",\"value\":true}]}")
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code", is("TRIGGER_PATH_VOLATILE")));
            send(post("/v1/triggers"), "{\"name\":\"p\",\"near\":{\"placeId\":\"home\",\"radiusKm\":5}}")
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code", is("PLACES_UNAVAILABLE")));
            send(post("/v1/triggers"), "{\"name\":\"s\",\"source\":\"" + STANDARD + "\",\"sources\":[\"" + STANDARD + "\"]}")
                    .andExpect(status().isBadRequest());
            send(post("/v1/triggers"), "{\"name\":\"g\",\"geofence\":{\"type\":\"Polygon\",\"coordinates\":"
                    + "[[[0,0],[0.001,0],[0.001,0.001],[0,0.001],[0,0]]]},\"mode\":\"enter\",\"hysteresisMeters\":500}")
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code", is("GEOFENCE_TOO_SMALL_FOR_HYSTERESIS")));
        }
    }

    @Nested
    @DisplayName("snooze and backtest")
    class Operations {

        @Test
        @DisplayName("snoozeTrigger sets snoozedUntil (at most 7 days); unsnoozeTrigger clears it idempotently")
        void snooze() throws Exception {
            String id = create(BIG);
            send(post("/v1/triggers/" + id + "/snooze"), "{\"for\":\"2h\"}")
                    .andExpect(status().isOk()).andExpect(jsonPath("$.snoozedUntil", is("2026-05-01T14:00:00Z")));
            send(post("/v1/triggers/" + id + "/snooze"), "{\"for\":\"8d\"}").andExpect(status().isBadRequest());
            send(post("/v1/triggers/" + id + "/snooze"), "{}").andExpect(status().isBadRequest());
            mvc.perform(auth(delete("/v1/triggers/" + id + "/snooze"))).andExpect(status().isOk())
                    .andExpect(jsonPath("$.snoozedUntil", nullValue()));
            mvc.perform(auth(delete("/v1/triggers/" + id + "/snooze"))).andExpect(status().isOk());
        }

        @Test
        @DisplayName("testTrigger by seq range, raw and simulated, creates nothing")
        void backtest() throws Exception {
            String id = create("{\"name\":\"R\",\"sources\":[\"" + STANDARD + "\"],\"cooldown\":\"1h\","
                    + "\"conditions\":[{\"path\":\"mag\",\"op\":\">=\",\"value\":6}]}");
            fixture.settle();
            jdbc.update("DELETE FROM alert");
            for (int i = 0; i < 3; i++) {
                clock.setInstant(T.plusSeconds(60L * i));
                records.ingest(STANDARD, NormalizedRecord.builder("b", STANDARD_KIND).observedAt(T.plusSeconds(60L * i))
                        .put("mag", 6.0 + i).build());
            }
            records.ingest(STANDARD, NormalizedRecord.builder("c", STANDARD_KIND).observedAt(T.plusSeconds(120)).put("mag", 1).build());
            fixture.settle();
            int before = jdbc.queryForObject("SELECT count(*) FROM alert", Integer.class);
            long to = jdbc.queryForObject("SELECT max(seq) FROM record_version", Long.class);

            String raw = mvc.perform(auth(post("/v1/triggers/" + id + "/test?fromSeq=0&toSeq=" + to + "&limit=2")))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            JsonNode result = JSON.readTree(raw);
            assertThat(result.get("matchCount").asLong()).isEqualTo(3);
            assertThat(result.get("matches")).hasSize(2);
            assertThat(result.get("truncated").asBoolean()).isTrue();
            assertThat(result.get("fromSeq").asLong()).isZero();
            assertThat(result.get("toSeq").asLong()).isEqualTo(to);
            assertThat(result.get("simulated").asBoolean()).isFalse();
            assertThat(result.get("wouldFire").isNull()).isTrue();
            assertThat(result.get("matches").get(0).get("source").asString()).isEqualTo(STANDARD);

            mvc.perform(auth(post("/v1/triggers/" + id + "/test?fromSeq=0&toSeq=" + to + "&simulate=true")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.simulated", is(true)))
                    .andExpect(jsonPath("$.wouldFire", is(1)))
                    .andExpect(jsonPath("$.wouldSuppress", is(2)));
            mvc.perform(auth(post("/v1/triggers/" + id + "/test?since=2026-05-01T00:00:00Z&until=2026-05-01T13:00:00Z")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.evaluatedFrom", is("2026-05-01T00:00:00Z")));
            assertThat(jdbc.queryForObject("SELECT count(*) FROM alert", Integer.class)).isEqualTo(before);

            mvc.perform(auth(post("/v1/triggers/" + id + "/test?fromSeq=0&since=2026-05-01T00:00:00Z")))
                    .andExpect(status().isBadRequest());
            mvc.perform(auth(post("/v1/triggers/" + id + "/test?fromSeq=9&toSeq=2"))).andExpect(status().isBadRequest());
            mvc.perform(auth(post("/v1/triggers/" + id + "/test?limit=1001"))).andExpect(status().isBadRequest());
            mvc.perform(auth(post("/v1/triggers/" + UUID.randomUUID() + "/test"))).andExpect(status().isNotFound());
        }
    }
}
