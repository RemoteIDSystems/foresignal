package ai.foresignal.sink;

import static ai.foresignal.testsupport.sink.SinkTestConfig.FEED_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.TokenService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.sink.SinkTestConfig;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The sinks API (prompt 110): CRUD, secrets write-only (never returned) and merged on a partial update, admin
 * only, the test endpoint, and the status block (cursor, lag against the published watermark, denied counts).
 */
@IntegrationTest
@Import(SinkTestConfig.class)
class SinkApiIT {

    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired TokenService tokens;
    @Autowired SinkService sinks;
    @Autowired SinkSupervisor supervisor;

    String admin;
    String reader;

    @BeforeEach
    void setUp() {
        admin = "Bearer " + adminTokens.rotate();
        reader = "Bearer " + tokens.create(JSON.readTree("{\"name\":\"reader\",\"scope\":\"read\"}")).plaintext();
        SinkTestConfig.FEED.reset();
    }

    @AfterEach
    void tearDown() {
        supervisor.stopAll();
    }

    JsonNode call(MockHttpServletRequestBuilder request, String auth, int expected) throws Exception {
        String body = mvc.perform(request.header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().is(expected)).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return body.isBlank() ? null : JSON.readTree(body);
    }

    static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    static final String MQTT = "{\"id\":\"home-mqtt\",\"type\":\"mqtt-state\",\"name\":\"Home MQTT\","
            + "\"config\":{\"brokerUrl\":\"mqtt://192.168.1.10:1883\",\"prefix\":\"foresignal\"},"
            + "\"secrets\":{\"username\":\"fs\",\"password\":\"s3cret\"},\"filter\":{\"sources\":[\"x\"]}}";

    @Test
    @DisplayName("CRUD: created disabled, secrets never returned, partial secrets update keeps the others")
    void crud() throws Exception {
        JsonNode created = call(json(post("/v1/sinks"), MQTT), admin, 201);
        assertThat(created.get("enabled").asBoolean()).isFalse();
        assertThat(created.has("secrets")).isFalse();
        assertThat(created.toString()).doesNotContain("s3cret");
        assertThat(created.get("secretsSet").toString()).contains("username", "password");

        JsonNode updated = call(json(put("/v1/sinks/home-mqtt"), "{\"secrets\":{\"password\":\"n3w\"}}"), admin,
                200);
        assertThat(updated.get("secretsSet").toString()).as("username kept").contains("username", "password");
        assertThat(sinks.secrets(sinks.get("home-mqtt").sink())).containsEntry("username", "fs")
                .containsEntry("password", "n3w");

        JsonNode removed = call(json(put("/v1/sinks/home-mqtt"), "{\"secrets\":{\"username\":null}}"), admin, 200);
        assertThat(removed.get("secretsSet").toString()).doesNotContain("username").contains("password");

        JsonNode list = call(get("/v1/sinks"), admin, 200);
        assertThat(list.get("items").toString()).contains("home-mqtt").doesNotContain("n3w");
        assertThat(call(get("/v1/sinks/home-mqtt"), admin, 200).get("name").asString()).isEqualTo("Home MQTT");
        call(delete("/v1/sinks/home-mqtt"), admin, 204);
        call(get("/v1/sinks/home-mqtt"), admin, 404);
    }

    @Test
    @DisplayName("every sink operation is admin only")
    void adminOnly() throws Exception {
        call(get("/v1/sinks"), reader, 403);
        call(get("/v1/sinks/types"), reader, 403);
        call(json(post("/v1/sinks"), MQTT), reader, 403);
        call(get("/v1/sinks/types"), admin, 200);
    }

    @Test
    @DisplayName("the test endpoint sends one synthetic record; status reports the cursor and lag")
    void testAndStatus() throws Exception {
        call(json(post("/v1/sinks"), "{\"id\":\"rec\",\"type\":\"" + FEED_TYPE + "\",\"name\":\"Rec\"}"), admin,
                201);
        JsonNode test = call(post("/v1/sinks/rec/test"), admin, 200);
        assertThat(test.get("ok").asBoolean()).isTrue();

        JsonNode status = call(get("/v1/sinks/rec/status"), admin, 200);
        assertThat(status.get("state").asString()).isEqualTo("STOPPED");
        assertThat(status.get("lastPublishedSeq").asLong()).isEqualTo(supervisor.lastPublishedSeq());
        assertThat(status.has("denied")).isTrue();

        call(post("/v1/sinks/rec/resync"), admin, 202);
    }

    @Test
    @DisplayName("SERVE sinks refuse a public bind and a missing port")
    void serveBinding() throws Exception {
        call(json(post("/v1/sinks"), "{\"id\":\"sbs-pub\",\"type\":\"sbs\",\"name\":\"SBS\","
                + "\"config\":{\"bind\":\"8.8.8.8\",\"port\":30003}}"), admin, 400);
        call(json(post("/v1/sinks"), "{\"id\":\"sbs-noport\",\"type\":\"sbs\",\"name\":\"SBS\",\"config\":{}}"),
                admin, 400);
        call(json(post("/v1/sinks"), "{\"id\":\"cot-global\",\"type\":\"cot\",\"name\":\"CoT\","
                + "\"config\":{\"group\":\"230.1.2.3\"}}"), admin, 400);
    }
}
