package ai.foresignal.inbound;

import static ai.foresignal.testsupport.inbound.InboundIt.body;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.inbound.InboundIt;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;

/**
 * Custom push sources (prompt 024): creation, the dynamic descriptor, update rules, the lazy
 * hash-recipe migration, key rotation, deletion and startup re-registration.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
class CustomSourceServiceIT {

    static final Instant T = Instant.parse("2026-10-12T08:00:00Z");
    static final String OBSERVED = "2026-10-12T07:59:00Z";

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired AppSettingService settings;
    @Autowired MutableClock clock;
    @Autowired CustomSourceService service;
    @Autowired CustomSourceRegistrar registrar;
    @Autowired SourceRegistry registry;
    @Autowired SourceService sources;
    @Autowired JdbcTemplate jdbc;

    String admin;
    InboundIt it;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        settings.invalidateCache();
        admin = "Bearer " + adminTokens.rotate();
        it = new InboundIt(mvc, admin);
    }

    @Test
    @DisplayName("create returns the key once and registers a PUSH, STANDARD, custom descriptor with its settings enabled")
    void create() throws Exception {
        String id = InboundIt.newId("create");
        InboundIt.Created created = it.create(id, "{'name':'Home','kinds':['sensor-reading','sensor-alarm'],"
                + "'recordTtl':'1h','observedAtMode':'RECEIVED','ttlAnchor':'OBSERVED','volatileFields':['rssi'],"
                + "'significantFields':['state'],'titleField':'name','private':true,'licence':'CC0-1.0',"
                + "'rateLimitPerMinute':30,'recordsPerMinute':100,'tileProperties':['state'],"
                + "'fieldSpecs':{'temp':{'type':'number','unit':'Cel','label':'Temperature'}}}");

        assertThat(created.key()).startsWith("eik_");
        assertThat(created.body().get("ingestKeyPrefix").asString()).isEqualTo(created.key().substring(0, 8));
        SourceDescriptor d = registry.descriptor(id);
        assertThat(d.mode()).isEqualTo(Mode.PUSH);
        assertThat(d.custom()).isTrue();
        assertThat(d.historyTier()).isEqualTo(HistoryTier.STANDARD);
        assertThat(d.observedAtMode()).isEqualTo(ObservedAtMode.RECEIVED);
        assertThat(d.ttlAnchor()).isEqualTo(TtlAnchor.OBSERVED);
        assertThat(d.recordTtl()).isEqualTo(Duration.ofHours(1));
        assertThat(d.volatileFields()).containsExactly("rssi");
        assertThat(d.significantFields()).containsExactly("state");
        assertThat(d.titleField()).isEqualTo("name");
        assertThat(d.privateSource()).isTrue();
        assertThat(d.licence()).isEqualTo("CC0-1.0");
        assertThat(d.zeroResultsNormal()).isTrue();
        assertThat(d.defaultBudget().perMinute()).isEqualTo(30);
        assertThat(d.defaultBudget().perDay()).isEqualTo(30 * 1440);
        assertThat(d.fieldSpecs().get("temp").unit()).isEqualTo("Cel");
        assertThat(sources.view(id).enabled()).isTrue();

        MockHttpServletResponse listed = it.perform(get("/v1/custom-sources").header(HttpHeaders.AUTHORIZATION, admin));
        assertThat(listed.getContentAsString()).contains(id).doesNotContain(created.key());
        JsonNode summary = body(it.perform(get("/v1/sources/" + id).header(HttpHeaders.AUTHORIZATION, admin)));
        assertThat(summary.toString()).contains("\"custom\":true");
    }

    @Test
    @DisplayName("an id taken by any registered source, or an invalid input, is refused")
    void conflictsAndValidation() throws Exception {
        String taken = InboundIt.newId("taken");
        registry.registerDynamic(SourceDescriptor.builder(taken).kind("x").mode(Mode.PUSH).defaultBudget(1, 1).build());
        try {
            MockHttpServletResponse conflict = it.perform(post("/v1/custom-sources").header(HttpHeaders.AUTHORIZATION, admin)
                    .contentType(MediaType.APPLICATION_JSON).content("{\"id\":\"" + taken + "\",\"name\":\"x\",\"kinds\":[\"x\"]}"));
            assertThat(conflict.getStatus()).isEqualTo(409);
            assertThat(body(conflict).get("code").asString()).isEqualTo("SOURCE_EXISTS");
        } finally {
            registry.unregisterDynamic(taken);
        }
        MockHttpServletResponse invalid = it.perform(post("/v1/custom-sources").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"id\":\"usgs-thing\",\"name\":\"\",\"kinds\":[\"Bad Kind\"],"
                        + "\"volatileFields\":[\"a.b\"],\"profile\":\"unknown-profile\",\"formats\":{\"nope\":{}}}"));
        assertThat(invalid.getStatus()).isEqualTo(400);
        assertThat(body(invalid).get("errors").findValuesAsString("field"))
                .contains("id", "name", "kinds[0]", "volatileFields[0]", "profile", "formats.nope");
    }

    @Test
    @DisplayName("update: removing a kind with current heads is KIND_IN_USE; changing observedAtMode with heads is locked")
    void updateRules() throws Exception {
        String id = InboundIt.newId("update");
        InboundIt.Created created = it.create(id, "{'name':'Home','kinds':['sensor-reading','sensor-alarm']}");
        assertThat(it.push("/v1/ingest/" + id, created.key(), MediaType.APPLICATION_JSON,
                InboundIt.feature("a", "sensor-alarm", OBSERVED, 1)).getStatus()).isEqualTo(200);

        MockHttpServletResponse inUse = it.perform(put("/v1/custom-sources/" + id).header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"id\":\"" + id + "\",\"name\":\"Home\",\"kinds\":[\"sensor-reading\"]}"));
        assertThat(inUse.getStatus()).isEqualTo(409);
        assertThat(body(inUse).get("code").asString()).isEqualTo("KIND_IN_USE");

        MockHttpServletResponse locked = it.perform(put("/v1/custom-sources/" + id).header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"id\":\"" + id + "\",\"name\":\"Home\","
                        + "\"kinds\":[\"sensor-reading\",\"sensor-alarm\"],\"observedAtMode\":\"RECEIVED\"}"));
        assertThat(locked.getStatus()).isEqualTo(409);
        assertThat(body(locked).get("code").asString()).isEqualTo("OBSERVED_AT_MODE_LOCKED");

        MockHttpServletResponse renamed = it.perform(put("/v1/custom-sources/" + id).header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"id\":\"" + id + "\",\"name\":\"Renamed\","
                        + "\"kinds\":[\"sensor-reading\",\"sensor-alarm\",\"sensor-extra\"]}"));
        assertThat(renamed.getStatus()).as(renamed.getContentAsString()).isEqualTo(200);
        assertThat(registry.descriptor(id).name()).isEqualTo("Renamed");
        assertThat(registry.descriptor(id).kinds()).contains("sensor-extra");
        assertThatThrownBy(() -> service.update(id, InboundIt.JSON.readTree(
                "{\"id\":\"custom-other-name\",\"name\":\"x\",\"kinds\":[\"sensor-reading\"]}")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("a SAMPLED policy override left without significant fields is 400 INGEST_POLICY_INVALID")
    void sampledNeedsSignificantFields() throws Exception {
        String id = InboundIt.newId("sampled");
        it.create(id, "{'name':'Gauges','kinds':['gauge'],'recordTtl':'1h','significantFields':['state']}");
        MockHttpServletResponse policy = it.perform(put("/v1/sources/" + id).header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ingestPolicy\":{\"mode\":\"SAMPLED\",\"minInterval\":\"60s\",\"keyframe\":\"10m\"}}"));
        assertThat(policy.getStatus()).as(policy.getContentAsString()).isEqualTo(200);

        MockHttpServletResponse emptied = it.perform(put("/v1/custom-sources/" + id).header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"id\":\"" + id + "\",\"name\":\"Gauges\",\"kinds\":[\"gauge\"],"
                        + "\"recordTtl\":\"1h\",\"significantFields\":[]}"));
        assertThat(emptied.getStatus()).isEqualTo(400);
        assertThat(body(emptied).get("code").asString()).isEqualTo("INGEST_POLICY_INVALID");
    }

    @Test
    @DisplayName("editing volatileFields changes the hash recipe; the writer migrates and an identical re-push writes no version")
    void recipeMigration() throws Exception {
        String id = InboundIt.newId("recipe");
        InboundIt.Created created = it.create(id, "{'name':'Radio','kinds':['station']}");
        String first = "{\"records\":[{\"externalId\":\"s1\",\"kind\":\"station\",\"observedAt\":\"" + OBSERVED
                + "\",\"payload\":{\"state\":\"up\",\"rssi\":-50}}]}";
        assertThat(body(it.push("/v1/ingest/" + id, created.key(), MediaType.APPLICATION_JSON, first))
                .get("persisted").asInt()).isEqualTo(1);
        Integer before = jdbc.queryForObject("select hash_recipe from record_head where source_id = ?", Integer.class, id);

        assertThat(it.perform(put("/v1/custom-sources/" + id).header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"id\":\"" + id + "\",\"name\":\"Radio\","
                        + "\"kinds\":[\"station\"],\"volatileFields\":[\"rssi\"]}")).getStatus()).isEqualTo(200);
        clock.setInstant(T.plusSeconds(3_700));
        String jittered = first.replace("-50", "-61").replace(OBSERVED, "2026-10-12T09:00:00Z");
        JsonNode outcome = body(it.push("/v1/ingest/" + id, created.key(), MediaType.APPLICATION_JSON, jittered));
        assertThat(outcome.get("persisted").asInt()).isZero();
        Integer after = jdbc.queryForObject("select hash_recipe from record_head where source_id = ?", Integer.class, id);
        assertThat(after).isNotEqualTo(before);
    }

    @Test
    @DisplayName("delete unregisters the source and removes its settings; the key stops working")
    void delete_() throws Exception {
        String id = InboundIt.newId("delete");
        InboundIt.Created created = it.create(id, "{'name':'Gone','kinds':['thing']}");
        assertThat(it.perform(delete("/v1/custom-sources/" + id).header(HttpHeaders.AUTHORIZATION, admin)).getStatus())
                .isEqualTo(204);
        assertThat(registry.contains(id)).isFalse();
        assertThat(jdbc.queryForList("select 1 from source_settings where source_id = ?", Integer.class, id)).isEmpty();
        assertThat(it.push("/v1/ingest/" + id, created.key(), MediaType.APPLICATION_JSON, "{}").getStatus()).isEqualTo(401);
        assertThat(it.perform(get("/v1/custom-sources/" + id).header(HttpHeaders.AUTHORIZATION, admin)).getStatus())
                .isEqualTo(404);
    }

    @Test
    @DisplayName("startup registration rebuilds every persisted custom source")
    void startupReRegistration() throws Exception {
        String id = InboundIt.newId("startup");
        it.create(id, "{'name':'Persisted','kinds':['thing'],'recordTtl':'2h'}");
        registry.unregisterDynamic(id);
        assertThat(registry.contains(id)).isFalse();

        registrar.reconcile();

        assertThat(registry.descriptor(id).recordTtl()).isEqualTo(Duration.ofHours(2));
        assertThat(registry.get(id).dynamic()).isTrue();
    }

    @Test
    @DisplayName("createExternal refuses an id that is already taken")
    void externalConflict() throws Exception {
        String id = InboundIt.newId("ext");
        it.create(id, "{'name':'Taken','kinds':['thing']}");
        CustomSourceEntity row = new CustomSourceEntity(id);
        row.setName("x");
        row.setKinds(java.util.List.of("thing"));
        assertThatThrownBy(() -> service.createExternal(row, registry.descriptor(id), null, true))
                .isInstanceOf(ConflictException.class);
    }
}
