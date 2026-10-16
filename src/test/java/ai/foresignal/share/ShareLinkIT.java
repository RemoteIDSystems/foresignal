package ai.foresignal.share;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.export.ExportJobEntity;
import ai.foresignal.security.audit.AuditService;
import ai.foresignal.settings.ClientLinks;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.export.ExportItSupport;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Share links end to end (prompt 112): the key works only on its own paths and never as a bearer token, the
 * explicit allowlist and precision cap, a non-redistributable target is 409, a private target is refused, expiry,
 * maximum uses and revocation give 410, listings are per creator, the client redirect, an export download and the
 * audit rows.
 */
@IntegrationTest
@Import(RetentionTestSources.class)
class ShareLinkIT extends ExportItSupport {

    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired AuditService audit;
    @Autowired ShareLinkRepository links;

    @BeforeEach
    void seed() {
        ingestAt(STD, T0, point("open", STD_KIND, T0, 10.12345, 50.12345, 1));
        ingestAt(FORBIDDEN, T0, point("closed", STD_KIND, T0, 11, 51, 2));
        ingestAt(SECRET, T0, point("hidden", STD_KIND, T0, 12, 52, 3));
    }

    JsonNode create(String bearer, String body, int expectedStatus) throws Exception {
        MvcResult result = mvc.perform(post("/v1/share-links").header(HttpHeaders.AUTHORIZATION, bearer)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().is(expectedStatus)).andReturn();
        return JSON.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    JsonNode open(String key) throws Exception {
        MvcResult result = mvc.perform(get("/v1/k/" + key)).andExpect(status().isOk()).andReturn();
        return JSON.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    static String record(String externalId) {
        return "{\"targetType\":\"record\",\"targetRef\":\"" + STD + "/" + externalId + "\"}";
    }

    @Test
    @DisplayName("a record link: key shown once, JSON and GeoJSON with attribution, readable without a token")
    void recordLink() throws Exception {
        JsonNode created = create(agent, record("open"), 201);
        String key = created.get("key").asString();
        assertThat(key).startsWith("fss_").hasSize(47);
        assertThat(created.get("url").asString()).endsWith("/v1/k/" + key);
        assertThat(created.get("status").asString()).isEqualTo("active");
        assertThat(created.get("keyPrefix").asString()).isEqualTo(key.substring(0, 12));
        JsonNode shared = open(key);
        assertThat(shared.get("targetType").asString()).isEqualTo("record");
        assertThat(shared.get("masked").asBoolean()).isFalse();
        assertThat(shared.get("data").get("payload").get("v").asInt()).isEqualTo(1);
        assertThat(shared.get("attributions").get(0).get("sourceId").asString()).isEqualTo(STD);
        assertThat(shared.get("links").get("geojson").asString()).endsWith("/v1/k/" + key + "/geojson");
        MvcResult geo = mvc.perform(get("/v1/k/" + key).header(HttpHeaders.ACCEPT, "application/geo+json"))
                .andExpect(status().isOk()).andExpect(header().exists("Foresignal-Attribution")).andReturn();
        assertThat(JSON.readTree(geo.getResponse().getContentAsString()).get("type").asString())
                .isEqualTo("FeatureCollection");
        JsonNode features = JSON.readTree(mvc.perform(get("/v1/k/" + key + "/geojson")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        assertThat(features.get("features").get(0).get("geometry").get("type").asString()).isEqualTo("Point");
        assertThat(links.findById(created.get("id").asString()).orElseThrow().getUses()).isEqualTo(3);
        assertThat(links.findAll()).allSatisfy(l -> assertThat(l.getKeyHash()).doesNotContain(key));
    }

    @Test
    @DisplayName("the key works only on its own paths, never as a bearer token, and GET only")
    void pathBound() throws Exception {
        String key = create(agent, record("open"), 201).get("key").asString();
        mvc.perform(get("/v1/records/" + STD + "/open").header(HttpHeaders.AUTHORIZATION, "Bearer " + key))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/v1/share-links").header(HttpHeaders.AUTHORIZATION, "Bearer " + key))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/v1/k/" + key + "/records")).andExpect(status().isForbidden());
        mvc.perform(post("/v1/k/" + key)).andExpect(status().isMethodNotAllowed());
        mvc.perform(get("/v1/k/" + key + "/tiles/1/0/0.mvt")).andExpect(status().isNotFound());
        mvc.perform(get("/v1/k/" + key + "/download")).andExpect(status().isNotFound());
        mvc.perform(get("/v1/k/fss_" + "A".repeat(43))).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("an explicit allowlist and a precision cap shape the shared record")
    void allowlistAndPrecision() throws Exception {
        String key = create(agent, "{\"targetType\":\"record\",\"targetRef\":\"" + STD + "/open\",\"fieldMask\":[\"v\"],"
                + "\"precisionM\":1000}", 201).get("key").asString();
        JsonNode data = open(key).get("data");
        assertThat(data.get("payload").propertyNames()).containsExactly("v");
        double lon = data.get("geometry").get("coordinates").get(0).asDouble();
        double step = 1000 / 111_320.0;
        assertThat(Math.abs(lon / step - Math.round(lon / step))).isLessThan(1e-3);
        assertThat(lon).isNotEqualTo(10.12345);
    }

    @Test
    @DisplayName("a target whose license forbids redistribution is 409 SHARE_LICENCE_FORBIDDEN")
    void licenceForbidden() throws Exception {
        JsonNode problem = create(agent, "{\"targetType\":\"record\",\"targetRef\":\"" + FORBIDDEN + "/closed\"}", 409);
        assertThat(problem.get("code").asString()).isEqualTo("SHARE_LICENCE_FORBIDDEN");
        assertThat(links.count()).isZero();
    }

    @Test
    @DisplayName("a private target needs admin, and even admin is refused because private records never leave (EGR-1)")
    void privateTarget() throws Exception {
        String body = "{\"targetType\":\"record\",\"targetRef\":\"" + SECRET + "/hidden\"}";
        create(agent, body, 404);
        JsonNode refused = create(admin, body, 403);
        assertThat(refused.get("code").asString()).isEqualTo("RESTRICTED");
        assertThat(links.count()).isZero();
    }

    @Test
    @DisplayName("expiry, maximum uses and revocation answer 410 SHARE_EXPIRED")
    void gone() throws Exception {
        String expiring = create(agent, "{\"targetType\":\"record\",\"targetRef\":\"" + STD + "/open\",\"expiresAt\":\""
                + T0.plus(Duration.ofHours(1)) + "\"}", 201).get("key").asString();
        String once = create(agent, "{\"targetType\":\"record\",\"targetRef\":\"" + STD + "/open\",\"maxUses\":1}", 201)
                .get("key").asString();
        JsonNode revocable = create(agent, record("open"), 201);
        open(once);
        MvcResult exhausted = mvc.perform(get("/v1/k/" + once)).andExpect(status().isGone()).andReturn();
        assertThat(JSON.readTree(exhausted.getResponse().getContentAsString()).get("code").asString())
                .isEqualTo("SHARE_EXPIRED");
        mvc.perform(delete("/v1/share-links/" + revocable.get("id").asString()).header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isNoContent());
        mvc.perform(get("/v1/k/" + revocable.get("key").asString())).andExpect(status().isGone());
        open(expiring);
        clock.setInstant(T0.plus(Duration.ofHours(2)));
        mvc.perform(get("/v1/k/" + expiring)).andExpect(status().isGone());
        create(agent, "{\"targetType\":\"record\",\"targetRef\":\"" + STD + "/open\",\"expiresAt\":\""
                + T0.plus(Duration.ofDays(40)) + "\"}", 400);
        create(agent, "{\"targetType\":\"poster\",\"targetRef\":\"x\"}", 400);
    }

    @Test
    @DisplayName("an agent lists and revokes its own links; admin lists all")
    void listing() throws Exception {
        JsonNode mine = create(agent, record("open"), 201);
        create(otherAgent, record("open"), 201);
        JsonNode own = JSON.readTree(mvc.perform(get("/v1/share-links").header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(own.get("items")).hasSize(1);
        assertThat(own.get("items").get(0).has("key")).isFalse();
        JsonNode all = JSON.readTree(mvc.perform(get("/v1/share-links?limit=1").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(all.get("items")).hasSize(1);
        assertThat(all.get("nextCursor").isNull()).isFalse();
        mvc.perform(delete("/v1/share-links/" + mine.get("id").asString()).header(HttpHeaders.AUTHORIZATION, otherAgent))
                .andExpect(status().isNotFound());
        mvc.perform(get("/v1/share-links")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("open=1 redirects to clientLinks.share when it is configured")
    void redirect() throws Exception {
        String key = create(agent, record("open"), 201).get("key").asString();
        assertThat(open(key + "?open=1").get("targetType").asString()).isEqualTo("record");
        settings.put(SettingKeys.SERVER, new ServerSettings(null, null, new ClientLinks(null, null, null, null,
                "https://app.example/share/{key}"), null, null));
        settings.invalidateCache();
        mvc.perform(get("/v1/k/" + key + "?open=1")).andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, "https://app.example/share/" + key));
    }

    @Test
    @DisplayName("an export link downloads the export's file and nothing else")
    void exportDownload() throws Exception {
        MvcResult submitted = mvc.perform(post("/v1/exports").header(HttpHeaders.AUTHORIZATION, agent)
                .contentType(MediaType.APPLICATION_JSON).content("{\"sources\":[\"" + STD + "\"]}"))
                .andExpect(status().isAccepted()).andReturn();
        ExportJobEntity job = awaitTerminal(JSON.readTree(submitted.getResponse().getContentAsString()).get("id").asString());
        assertThat(job.getStatus()).isEqualTo(ExportJobEntity.DONE);
        String key = create(agent, "{\"targetType\":\"export\",\"targetRef\":\"" + job.getId() + "\"}", 201)
                .get("key").asString();
        mvc.perform(get("/v1/k/" + key + "/download")).andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCEPT_RANGES, "bytes"));
        assertThat(open(key).get("links").get("download").asString()).endsWith("/download");
        mvc.perform(get("/v1/k/" + key + "/tiles/1/0/0.mvt")).andExpect(status().isNotFound());
        create(otherAgent, "{\"targetType\":\"export\",\"targetRef\":\"" + job.getId() + "\"}", 404);
    }

    @Test
    @DisplayName("creation, use and revocation are audited")
    void auditRows() throws Exception {
        JsonNode created = create(agent, record("open"), 201);
        open(created.get("key").asString());
        mvc.perform(delete("/v1/share-links/" + created.get("id").asString()).header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isNoContent());
        audit.flush();
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM audit_event WHERE target_type = 'share-links' ORDER BY id");
        assertThat(rows).extracting(r -> r.get("operation_id"))
                .containsExactly("createShareLink", "openShareLink", "revokeShareLink");
        assertThat(rows).allSatisfy(r -> assertThat(r.get("target_id")).isEqualTo(created.get("id").asString()));
        assertThat(rows.toString()).doesNotContain(created.get("key").asString());
    }
}
