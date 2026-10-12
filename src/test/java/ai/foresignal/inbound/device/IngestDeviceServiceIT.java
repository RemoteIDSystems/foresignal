package ai.foresignal.inbound.device;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.inbound.InboundIt;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;

/**
 * Device-scoped ingest keys (prompt 039): the key is shown once, listings never carry it, rotation
 * invalidates the old key, revocation stops it, and the setup bundle's URLs follow
 * {@code publicBaseUrl}.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
class IngestDeviceServiceIT {

    static final Instant T = Instant.parse("2026-10-12T08:00:00Z");

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired AppSettingService settings;
    @Autowired MutableClock clock;
    @Autowired IngestDeviceService devices;

    String admin;
    InboundIt it;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        settings.invalidateCache();
        admin = "Bearer " + adminTokens.rotate();
        it = new InboundIt(mvc, admin);
    }

    InboundIt.Created source() throws Exception {
        return it.create(InboundIt.newId("dev"), "{'name':'Family','profile':'personal-location'}");
    }

    JsonNode createDevice(String sourceId, String externalId) throws Exception {
        MockHttpServletResponse response = it.perform(post("/v1/custom-sources/" + sourceId + "/devices")
                .header(HttpHeaders.AUTHORIZATION, admin).contentType(MediaType.APPLICATION_JSON)
                .content("{\"externalId\":\"" + externalId + "\",\"name\":\"Ana phone\"}"));
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(201);
        return InboundIt.body(response);
    }

    @Test
    @DisplayName("the key is returned once at creation; the listing has the prefix but never the key")
    void keyShownOnce() throws Exception {
        InboundIt.Created source = source();
        JsonNode created = createDevice(source.id(), "ana-phone");
        String key = created.get("key").asString();
        assertThat(key).startsWith("eid_");
        assertThat(created.get("keyPrefix").asString()).isEqualTo(key.substring(0, 8));
        assertThat(created.get("externalId").asString()).isEqualTo("ana-phone");
        assertThat(created.get("revokedAt").isNull()).isTrue();

        MockHttpServletResponse list = it.perform(get("/v1/custom-sources/" + source.id() + "/devices")
                .header(HttpHeaders.AUTHORIZATION, admin));
        assertThat(list.getStatus()).isEqualTo(200);
        assertThat(list.getContentAsString()).doesNotContain(key).contains(created.get("keyPrefix").asString());
        assertThat(InboundIt.body(list).get(0).has("key")).isFalse();
        assertThat(InboundIt.body(list).get(0).has("setup")).isFalse();

        assertThat(devices.authenticate(key)).isPresent();
        assertThat(devices.authenticate(key).get().getLastSeenAt()).isEqualTo(T);
    }

    @Test
    @DisplayName("rotation returns a new key once and the old key stops working; revocation stops the new one")
    void rotateAndRevoke() throws Exception {
        InboundIt.Created source = source();
        JsonNode created = createDevice(source.id(), "ben-phone");
        String oldKey = created.get("key").asString();
        String id = created.get("id").asString();

        MockHttpServletResponse rotated = it.perform(post("/v1/custom-sources/" + source.id() + "/devices/" + id
                + "/key/rotate").header(HttpHeaders.AUTHORIZATION, admin));
        assertThat(rotated.getStatus()).as(rotated.getContentAsString()).isEqualTo(200);
        String newKey = InboundIt.body(rotated).get("key").asString();
        assertThat(newKey).startsWith("eid_").isNotEqualTo(oldKey);
        assertThat(devices.authenticate(oldKey)).isEmpty();
        assertThat(devices.authenticate(newKey)).isPresent();

        MockHttpServletResponse revoked = it.perform(delete("/v1/custom-sources/" + source.id() + "/devices/" + id)
                .header(HttpHeaders.AUTHORIZATION, admin));
        assertThat(revoked.getStatus()).isEqualTo(204);
        assertThat(devices.authenticate(newKey)).isEmpty();
        JsonNode listed = InboundIt.body(it.perform(get("/v1/custom-sources/" + source.id() + "/devices")
                .header(HttpHeaders.AUTHORIZATION, admin)));
        assertThat(listed.get(0).get("revokedAt").isNull()).isFalse();
    }

    @Test
    @DisplayName("the setup bundle builds OwnTracks, Overland, Traccar and GPSLogger settings on publicBaseUrl")
    void setupBundle() throws Exception {
        InboundIt.Created source = source();
        putServer("{\"publicBaseUrl\":\"https://fs.example\"}");
        try {
            JsonNode created = createDevice(source.id(), "cleo-phone");
            String key = created.get("key").asString();
            JsonNode setup = created.get("setup");
            String base = "https://fs.example/v1/ingest/" + source.id();

            JsonNode otrc = setup.get("owntracks");
            assertThat(otrc.get("_type").asString()).isEqualTo("configuration");
            assertThat(otrc.get("mode").asInt()).isEqualTo(3);
            assertThat(otrc.get("url").asString()).isEqualTo(base + "/owntracks");
            assertThat(otrc.get("auth").asBoolean()).isTrue();
            assertThat(otrc.get("password").asString()).isEqualTo(key);
            assertThat(otrc.get("username").asString()).isNotBlank();
            assertThat(otrc.get("deviceId").asString()).isEqualTo("cleo-phone");
            assertThat(otrc.get("tid").asString()).hasSize(2);

            assertThat(setup.get("overland").get("endpoint").asString()).isEqualTo(base + "/overland");
            assertThat(setup.get("overland").get("token").asString()).isEqualTo(key);
            assertThat(setup.get("traccar").get("serverUrl").asString()).isEqualTo(base + "/k/" + key + "/osmand");
            assertThat(setup.get("gpslogger").get("urlTemplate").asString())
                    .isEqualTo(base + "/k/" + key + "/gpslogger?" + IngestDeviceSetup.GPSLOGGER_QUERY);
            assertThat(setup.get("gpslogger").get("method").asString()).isEqualTo("GET");
        } finally {
            putServer("{\"publicBaseUrl\":null}");
        }
        JsonNode local = createDevice(source.id(), "dan-phone");
        assertThat(local.get("setup").get("overland").get("endpoint").asString())
                .isEqualTo("http://localhost/v1/ingest/" + source.id() + "/overland");
    }

    @Test
    @DisplayName("validation: missing members 400, unknown source or device 404, read tokens cannot manage devices")
    void validationAndAccess() throws Exception {
        InboundIt.Created source = source();
        MockHttpServletResponse missing = it.perform(post("/v1/custom-sources/" + source.id() + "/devices")
                .header(HttpHeaders.AUTHORIZATION, admin).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"x\"}"));
        assertThat(missing.getStatus()).isEqualTo(400);
        MockHttpServletResponse extra = it.perform(post("/v1/custom-sources/" + source.id() + "/devices")
                .header(HttpHeaders.AUTHORIZATION, admin).contentType(MediaType.APPLICATION_JSON)
                .content("{\"externalId\":\"a\",\"name\":\"x\",\"scope\":\"admin\"}"));
        assertThat(extra.getStatus()).isEqualTo(400);
        assertThat(it.perform(get("/v1/custom-sources/custom-nope-" + UUID.randomUUID().toString().substring(0, 8)
                + "/devices").header(HttpHeaders.AUTHORIZATION, admin)).getStatus()).isEqualTo(404);
        assertThat(it.perform(delete("/v1/custom-sources/" + source.id() + "/devices/" + UUID.randomUUID())
                .header(HttpHeaders.AUTHORIZATION, admin)).getStatus()).isEqualTo(404);
        assertThat(it.perform(delete("/v1/custom-sources/" + source.id() + "/devices/not-a-uuid")
                .header(HttpHeaders.AUTHORIZATION, admin)).getStatus()).isEqualTo(404);
        assertThat(it.perform(get("/v1/custom-sources/" + source.id() + "/devices")).getStatus()).isEqualTo(401);
    }

    private void putServer(String body) throws Exception {
        MockHttpServletResponse response = it.perform(put("/v1/settings/server").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content(body));
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(200);
        settings.invalidateCache();
    }
}
