package ai.foresignal.place;

import static ai.foresignal.testsupport.place.PlaceTestSources.EVENTS;
import static ai.foresignal.testsupport.place.PlaceTestSources.EVENT_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.place.PlaceTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.trigger.TriggerFixture;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.TriggerEngine;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

/**
 * The {@code Places} operations over HTTP (prompt 036): CRUD, always-authenticated reads (anonymous
 * is 401 even with {@code openReads}), {@code 409 PLACE_IN_USE} and {@code force}, and the
 * re-materialization of referencing rules on an edit, which never fires edges for a stationary
 * entity.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(PlaceTestSources.class)
class PlacesIT {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();

    static final String HOME = "{\"id\":\"home\",\"name\":\"Home\",\"shape\":\"point\","
            + "\"geometry\":{\"type\":\"Point\",\"coordinates\":[0,0]},\"radiusKm\":5,\"kind\":\"user\",\"tags\":[\"family\"]}";

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired TokenService tokens;
    @Autowired AppSettingService settings;
    @Autowired MutableClock clock;
    @Autowired TestRecords records;
    @Autowired TriggerRuleService rules;
    @Autowired TriggerRuleRepository ruleRepository;
    @Autowired TriggerEngine engine;
    @Autowired ChangeBus changeBus;
    @Autowired AlertStore alerts;
    @Autowired AlertBus bus;
    @Autowired JdbcTemplate jdbc;

    String admin;
    String reader;
    TriggerFixture fixture;
    Instant now = T;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        admin = "Bearer " + adminTokens.rotate();
        reader = TestTokens.bearer(TestTokens.create(tokens, "reader", Scope.READ));
        clock.setInstant(T);
        fixture = new TriggerFixture(rules, engine, changeBus, alerts, bus);
    }

    @AfterEach
    void tearDown() {
        fixture.close();
    }

    ResultActions send(MockHttpServletRequestBuilder request, String token, String body) throws Exception {
        if (token != null) {
            request = request.header(HttpHeaders.AUTHORIZATION, token);
        }
        if (body != null) {
            request = request.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        return mvc.perform(request);
    }

    JsonNode json(ResultActions result) throws Exception {
        return JSON.readTree(result.andReturn().getResponse().getContentAsString());
    }

    void observe(String id, double lon, double lat) {
        now = now.plusSeconds(10);
        clock.setInstant(now);
        records.ingest(EVENTS, NormalizedRecord.builder(id, EVENT_KIND).observedAt(now).point(lon, lat)
                .put("name", id).build());
    }

    long versions(String id) {
        return jdbc.queryForObject("SELECT count(*) FROM record_version WHERE external_id = ?", Long.class, id);
    }

    @Test
    @DisplayName("CRUD: create 201 with derived area and version 1, duplicate 409, get, list filters, replace bumps the version, delete")
    void crud() throws Exception {
        JsonNode created = json(send(post("/v1/places"), admin, HOME).andExpect(status().isCreated()));
        assertThat(created.get("id").asString()).isEqualTo("home");
        assertThat(created.get("version").asInt()).isEqualTo(1);
        assertThat(created.get("area").get("type").asString()).isEqualTo("MultiPolygon");
        assertThat(created.get("timezone").asString()).isNotBlank();
        assertThat(created.get("sensitive").asBoolean()).isTrue();

        send(post("/v1/places"), admin, HOME).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PLACE_EXISTS"));
        send(post("/v1/places"), admin, "{\"id\":\"Bad Id\",\"name\":\"x\",\"shape\":\"point\","
                + "\"geometry\":{\"type\":\"Point\",\"coordinates\":[0,0]}}").andExpect(status().isBadRequest());
        send(post("/v1/places"), admin, "{\"id\":\"aoi\",\"name\":\"x\",\"shape\":\"point\","
                + "\"geometry\":{\"type\":\"Point\",\"coordinates\":[0,0]}}").andExpect(status().isBadRequest());
        send(post("/v1/places"), admin, "{\"id\":\"road\",\"name\":\"Road\",\"shape\":\"route\","
                + "\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[0,0],[1,1]]}}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors[0].field").value("bufferM"));
        send(post("/v1/places"), admin, "{\"id\":\"airport\",\"name\":\"Airport\",\"shape\":\"polygon\",\"kind\":\"airport\","
                + "\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[1,1],[2,1],[2,2],[1,2],[1,1]]]}}")
                .andExpect(status().isCreated());

        send(get("/v1/places/home"), reader, null).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Home"));
        send(get("/v1/places/nowhere"), reader, null).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("UNKNOWN_PLACE"));
        JsonNode list = json(send(get("/v1/places?kind=airport"), reader, null).andExpect(status().isOk()));
        assertThat(list.get("items")).hasSize(1);
        assertThat(list.get("items").get(0).get("id").asString()).isEqualTo("airport");
        assertThat(json(send(get("/v1/places?tag=family"), reader, null)).get("items")).hasSize(1);
        JsonNode page = json(send(get("/v1/places?limit=1"), reader, null));
        assertThat(page.get("nextCursor").isNull()).isFalse();
        JsonNode second = json(send(get("/v1/places?limit=1&cursor=" + page.get("nextCursor").asString()), reader, null));
        assertThat(second.get("items").get(0).get("id").asString()).isEqualTo("home");

        JsonNode replaced = json(send(put("/v1/places/home"), admin, HOME.replace("\"radiusKm\":5", "\"radiusKm\":8"))
                .andExpect(status().isOk()));
        assertThat(replaced.get("version").asInt()).isEqualTo(2);
        assertThat(replaced.get("radiusKm").asDouble()).isEqualTo(8.0);
        send(put("/v1/places/home"), admin, HOME.replace("\"id\":\"home\"", "\"id\":\"other\""))
                .andExpect(status().isBadRequest());
        send(put("/v1/places/nowhere"), admin, HOME).andExpect(status().isNotFound());

        send(get("/v1/places/aoi"), reader, null).andExpect(status().isOk())
                .andExpect(jsonPath("$.isDefault").value(false)).andExpect(jsonPath("$.placeCount").value(2));

        send(delete("/v1/places/airport"), admin, null).andExpect(status().isNoContent());
        send(delete("/v1/places/airport"), admin, null).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("reads are always authenticated: anonymous is 401 even with openReads; a read token cannot write")
    void alwaysAuthenticated() throws Exception {
        send(post("/v1/places"), admin, HOME).andExpect(status().isCreated());
        send(get("/v1/places"), null, null).andExpect(status().isUnauthorized());
        send(get("/v1/places/home"), null, null).andExpect(status().isUnauthorized());
        send(get("/v1/places/aoi"), null, null).andExpect(status().isUnauthorized());
        send(get("/v1/records?place=home"), null, null).andExpect(status().isUnauthorized());
        send(get("/v1/places"), reader, null).andExpect(status().isOk());
        send(post("/v1/places"), reader, HOME.replace("home", "work")).andExpect(status().isForbidden());
        send(delete("/v1/places/home"), reader, null).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("place= on /v1/records returns only records inside the place's area (with a token)")
    void placeFilter() throws Exception {
        send(post("/v1/places"), admin, HOME).andExpect(status().isCreated());
        observe("inside", 0.01, 0.01);
        observe("outside", 1, 1);
        JsonNode page = json(send(get("/v1/records?source=" + EVENTS + "&place=home"), reader, null)
                .andExpect(status().isOk()));
        assertThat(page.get("items")).hasSize(1);
        assertThat(page.get("items").get(0).get("externalId").asString()).isEqualTo("inside");
        JsonNode buffered = json(send(get("/v1/records?source=" + EVENTS + "&place=home&radiusKm=200"), reader, null));
        assertThat(buffered.get("items")).hasSize(2);
        send(get("/v1/records?place=nowhere"), reader, null).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("UNKNOWN_PLACE"));
    }

    @Test
    @DisplayName("delete: 409 PLACE_IN_USE lists the rules and views; force deletes and disables the rules")
    void deleteInUse() throws Exception {
        send(post("/v1/places"), admin, HOME).andExpect(status().isCreated());
        UUID rule = fixture.rule("{'name':'Near home','sources':['" + EVENTS + "'],'mode':'enter','near':{'placeIds':['home']}}");
        String view = json(send(post("/v1/views"), admin, "{\"slug\":\"home-events\",\"name\":\"Home events\","
                + "\"placeIds\":[\"home\"]}").andExpect(status().isCreated())).get("id").asString();

        JsonNode conflict = json(send(delete("/v1/places/home"), admin, null).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PLACE_IN_USE")));
        assertThat(conflict.get("references").get("rules").get(0).asString()).isEqualTo(rule.toString());
        assertThat(conflict.get("references").get("views").get(0).asString()).isEqualTo(view);

        send(delete("/v1/places/home?force=true"), admin, null).andExpect(status().isNoContent());
        TriggerRuleEntity disabled = ruleRepository.findById(rule).orElseThrow();
        assertThat(disabled.isEnabled()).isFalse();
        assertThat(disabled.getLastError()).contains("home");
        send(get("/v1/places/home"), reader, null).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("editing a place re-materializes its rules without firing edges for a stationary entity")
    void rematerialization() throws Exception {
        send(post("/v1/places"), admin, HOME).andExpect(status().isCreated());
        UUID rule = fixture.rule("{'name':'Enter home','sources':['" + EVENTS + "'],'mode':'enter','near':{'placeIds':['home']}}");
        TriggerRuleEntity before = ruleRepository.findById(rule).orElseThrow();
        assertThat(before.getGeofenceOrigin()).isEqualTo("near");

        observe("parked", 0.01, 0.01);
        assertThat(fixture.alerts(rule)).hasSize(1);
        assertThat(versions("parked")).isEqualTo(1);

        // Move the place away: the stationary entity is now outside, and nothing fires.
        send(put("/v1/places/home"), admin, HOME.replace("[0,0]", "[2,2]")).andExpect(status().isOk());
        TriggerRuleEntity moved = ruleRepository.findById(rule).orElseThrow();
        assertThat(moved.getGeofence().getEnvelopeInternal().centre().x).isCloseTo(2.0, org.assertj.core.data.Offset.offset(0.01));
        assertThat(moved.getNear()).containsEntry("placeVersions", java.util.List.of(2));
        observe("parked", 0.01, 0.01);
        assertThat(versions("parked")).as("no edge under the new fence for an unchanged entity").isEqualTo(1);

        // Move it back: still nothing, because the entity did not move.
        send(put("/v1/places/home"), admin, HOME).andExpect(status().isOk());
        observe("parked", 0.01, 0.01);
        assertThat(versions("parked")).isEqualTo(1);
        assertThat(fixture.alerts(rule)).hasSize(1);

        // An entity that moves after the edit converges on its next observation.
        send(put("/v1/places/home"), admin, HOME.replace("[0,0]", "[2,2]")).andExpect(status().isOk());
        observe("parked", 2.0, 2.0);
        assertThat(fixture.alerts(rule)).hasSize(2);
        assertThat(fixture.alerts(rule).getLast().context()).contains("\"placeIds\"").contains("home")
                .doesNotContain("coordinates");
    }
}
