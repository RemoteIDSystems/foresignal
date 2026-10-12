package ai.foresignal.notify.push.device;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.notify.push.provider.PushProviderRepository;
import ai.foresignal.notify.push.provider.PushProviderService;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.TokenService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.push.NativeTestKeys;
import ai.foresignal.testsupport.push.PushTestConfig;
import ai.foresignal.testsupport.security.TestTokens;
import java.time.Duration;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Native device registration over HTTP (prompt 032): validation, the provider requirement and the
 * APNs environment, upsert and ownership moves, per-token visibility, the cap, device metadata and
 * the revoke cascade. Keys and device tokens are generated or made up at test time; nothing is
 * sent.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(PushTestConfig.class)
class PushDevicesIT {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();
    static final String APP = "ai.foresignal.app";

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired TokenService tokens;
    @Autowired PushProviderService providers;
    @Autowired PushProviderRepository providerRows;
    @Autowired PushDeviceRepository devices;
    @Autowired PushDeviceService service;
    @Autowired MutableClock clock;

    String admin;
    TokenService.CreatedToken reader;
    TokenService.CreatedToken other;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        devices.deleteAll();
        providerRows.deleteAll();
        admin = "Bearer " + adminTokens.rotate();
        reader = TestTokens.create(tokens, "phone", Scope.READ);
        other = TestTokens.create(tokens, "tablet", Scope.READ);
    }

    void apnsProvider(String keyEnvironment, boolean enabled) {
        providers.put("ios-apns", JSON.readTree("""
                {"type":"apns","appIds":["%s"],"enabled":%s,
                 "config":{"teamId":"TEAM123456","keyId":"KEY1234567","keyEnvironment":"%s"},
                 "secrets":{"p8":%s}}
                """.formatted(APP, enabled, keyEnvironment, JSON.writeValueAsString(NativeTestKeys.pem(NativeTestKeys.ec())))));
    }

    void fcmProvider() {
        String account = NativeTestKeys.serviceAccountJson(NativeTestKeys.rsa(), "my-project",
                "sender@my-project.iam.gserviceaccount.com", "https://oauth2.googleapis.com/token");
        providers.put("android-fcm", JSON.readTree("{\"type\":\"fcm\",\"appIds\":[\"" + APP + "\"],\"config\":{},"
                + "\"secrets\":{\"serviceAccountJson\":" + JSON.writeValueAsString(account) + "}}"));
    }

    static String apnsToken(int n) {
        return String.format("%064x", n);
    }

    static String apnsBody(String token, String environment) {
        return "{\"transport\":\"apns\",\"platform\":\"ios\",\"appId\":\"" + APP + "\",\"environment\":\"" + environment
                + "\",\"token\":\"" + token + "\",\"name\":\"iPhone\"}";
    }

    ResultActions send(MockHttpServletRequestBuilder request, String bearer, String body) throws Exception {
        MockHttpServletRequestBuilder r = bearer == null ? request : request.header(HttpHeaders.AUTHORIZATION, bearer);
        if (body != null) {
            r = r.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        return mvc.perform(r);
    }

    static JsonNode json(ResultActions result) throws Exception {
        return JSON.readTree(result.andReturn().getResponse().getContentAsString());
    }

    @Test
    @DisplayName("validation: token formats, the APNs environment and platform, enums; nothing is stored")
    void validation() throws Exception {
        apnsProvider("both", true);
        String bearer = TestTokens.bearer(reader);
        send(post("/v1/push/devices"), bearer, apnsBody("xyz", "production")).andExpect(status().isBadRequest());
        send(post("/v1/push/devices"), bearer, apnsBody("ab".repeat(20), "production")).andExpect(status().isBadRequest());
        send(post("/v1/push/devices"), bearer, apnsBody("a".repeat(201), "production")).andExpect(status().isBadRequest());
        send(post("/v1/push/devices"), bearer, apnsBody(apnsToken(1), "staging")).andExpect(status().isBadRequest());
        send(post("/v1/push/devices"), bearer, "{\"transport\":\"apns\",\"platform\":\"ios\",\"appId\":\"" + APP
                + "\",\"token\":\"" + apnsToken(1) + "\"}").andExpect(status().isBadRequest());
        send(post("/v1/push/devices"), bearer, apnsBody(apnsToken(1), "production").replace("\"ios\"", "\"android\""))
                .andExpect(status().isBadRequest());
        send(post("/v1/push/devices"), bearer, "{\"transport\":\"fcm\",\"platform\":\"android\",\"appId\":\"" + APP
                + "\",\"token\":\"" + "t".repeat(4097) + "\"}").andExpect(status().isBadRequest());
        send(post("/v1/push/devices"), bearer, "{\"transport\":\"mqtt\",\"platform\":\"web\",\"appId\":\"" + APP
                + "\",\"token\":\"t\",\"contentLevel\":\"full\",\"extra\":1}").andExpect(status().isBadRequest());
        send(post("/v1/push/devices"), null, apnsBody(apnsToken(1), "production")).andExpect(status().isUnauthorized());
        assertThat(devices.count()).isZero();
    }

    @Test
    @DisplayName("409 PUSH_PROVIDER_NOT_CONFIGURED: no provider, a disabled provider, or an APNs key of another environment")
    void providerRequired() throws Exception {
        String bearer = TestTokens.bearer(reader);
        JsonNode none = json(send(post("/v1/push/devices"), bearer, apnsBody(apnsToken(1), "production"))
                .andExpect(status().isConflict()));
        assertThat(none.get("code").asString()).isEqualTo("PUSH_PROVIDER_NOT_CONFIGURED");
        apnsProvider("production", false);
        send(post("/v1/push/devices"), bearer, apnsBody(apnsToken(1), "production")).andExpect(status().isConflict());
        apnsProvider("production", true);
        JsonNode env = json(send(post("/v1/push/devices"), bearer, apnsBody(apnsToken(1), "sandbox"))
                .andExpect(status().isConflict()));
        assertThat(env.get("code").asString()).isEqualTo("PUSH_PROVIDER_NOT_CONFIGURED");
        assertThat(env.get("detail").asString()).contains("sandbox");
        send(post("/v1/push/devices"), bearer, apnsBody(apnsToken(1), "production")).andExpect(status().isCreated());
        send(post("/v1/push/devices"), bearer, "{\"transport\":\"fcm\",\"platform\":\"android\",\"appId\":\"" + APP
                + "\",\"token\":\"fcm-1\"}").andExpect(status().isConflict());
    }

    @Test
    @DisplayName("register 201, never returning the token; re-registering the token moves the device to the caller (200)")
    void upsertMovesOwnership() throws Exception {
        apnsProvider("both", true);
        tokens.updateDevice(reader.entity().getId(), JSON.readTree(
                "{\"deviceName\":\"Adam's iPhone\",\"platform\":\"ios\",\"appVersion\":\"1.2.3\"}"));
        String token = apnsToken(0xabcdef);
        JsonNode created = json(send(post("/v1/push/devices"), TestTokens.bearer(reader), apnsBody(token, "sandbox"))
                .andExpect(status().isCreated()));
        assertThat(created.get("tokenSuffix").asString()).isEqualTo(token.substring(58));
        assertThat(created.get("tokenId").asString()).isEqualTo(reader.entity().getId().toString());
        assertThat(created.get("environment").asString()).isEqualTo("sandbox");
        assertThat(created.get("contentLevel").asString()).isEqualTo("minimal");
        assertThat(created.get("status").asString()).isEqualTo("active");
        assertThat(created.get("deviceName").asString()).isEqualTo("Adam's iPhone");
        assertThat(created.get("appVersion").asString()).isEqualTo("1.2.3");
        assertThat(created.toString()).doesNotContain(token);
        assertThat(created.has("token")).isFalse();

        JsonNode moved = json(send(post("/v1/push/devices"), TestTokens.bearer(other),
                apnsBody(token.toUpperCase(), "production")).andExpect(status().isOk()));
        assertThat(moved.get("id").asString()).isEqualTo(created.get("id").asString());
        assertThat(moved.get("tokenId").asString()).isEqualTo(other.entity().getId().toString());
        assertThat(moved.get("environment").asString()).isEqualTo("production");
        assertThat(devices.count()).isEqualTo(1);
        assertThat(new String(devices.findAll().getFirst().getTokenCiphertext(), java.nio.charset.StandardCharsets.ISO_8859_1))
                .doesNotContain(token);
    }

    @Test
    @DisplayName("read tokens see and manage only their own devices; admin sees all; others get 404")
    void visibility() throws Exception {
        apnsProvider("both", true);
        fcmProvider();
        String mine = json(send(post("/v1/push/devices"), TestTokens.bearer(reader), apnsBody(apnsToken(1), "production"))
                .andExpect(status().isCreated())).get("id").asString();
        json(send(post("/v1/push/devices"), TestTokens.bearer(other), "{\"transport\":\"fcm\",\"platform\":\"android\","
                + "\"appId\":\"" + APP + "\",\"token\":\"fcm-token-2\"}").andExpect(status().isCreated()));

        assertThat(json(send(get("/v1/push/devices"), TestTokens.bearer(reader), null).andExpect(status().isOk())))
                .hasSize(1);
        assertThat(json(send(get("/v1/push/devices"), admin, null).andExpect(status().isOk()))).hasSize(2);

        send(put("/v1/push/devices/" + mine), TestTokens.bearer(other), "{\"name\":\"x\"}").andExpect(status().isNotFound());
        send(delete("/v1/push/devices/" + mine), TestTokens.bearer(other), null).andExpect(status().isNotFound());
        send(post("/v1/push/devices/" + mine + "/test"), admin, null).andExpect(status().isNotFound());

        JsonNode updated = json(send(put("/v1/push/devices/" + mine), TestTokens.bearer(reader),
                "{\"name\":\"Work phone\",\"contentLevel\":\"short\",\"lang\":\"pt-BR\","
                        + "\"filters\":{\"events\":[\"alert\"],\"minSeverity\":\"critical\"}}").andExpect(status().isOk()));
        assertThat(updated.get("name").asString()).isEqualTo("Work phone");
        assertThat(updated.get("contentLevel").asString()).isEqualTo("short");
        assertThat(updated.get("lang").asString()).isEqualTo("pt-BR");
        assertThat(updated.get("filters").get("minSeverity").asString()).isEqualTo("critical");
        send(put("/v1/push/devices/" + mine), TestTokens.bearer(reader), "{\"contentLevel\":\"full\"}")
                .andExpect(status().isBadRequest());

        send(delete("/v1/push/devices/" + mine), admin, null).andExpect(status().isNoContent());
        send(delete("/v1/push/devices/" + mine), TestTokens.bearer(reader), null).andExpect(status().isNotFound());
        send(get("/v1/push/devices"), null, null).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("a token may register 20 devices (409 PUSH_LIMIT_REACHED); re-registering one of them is still allowed")
    void cap() throws Exception {
        apnsProvider("both", true);
        String bearer = TestTokens.bearer(reader);
        for (int i = 0; i < PushDeviceService.MAX_PER_TOKEN; i++) {
            send(post("/v1/push/devices"), bearer, apnsBody(apnsToken(i + 1), "production")).andExpect(status().isCreated());
        }
        JsonNode problem = json(send(post("/v1/push/devices"), bearer, apnsBody(apnsToken(99), "production"))
                .andExpect(status().isConflict()));
        assertThat(problem.get("code").asString()).isEqualTo("PUSH_LIMIT_REACHED");
        send(post("/v1/push/devices"), bearer, apnsBody(apnsToken(1), "production")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("revoking a token deletes its devices; the sweep removes devices of expired tokens")
    void revokeCascade() throws Exception {
        apnsProvider("both", true);
        String mine = json(send(post("/v1/push/devices"), TestTokens.bearer(reader), apnsBody(apnsToken(1), "production"))
                .andExpect(status().isCreated())).get("id").asString();
        String theirs = json(send(post("/v1/push/devices"), TestTokens.bearer(other), apnsBody(apnsToken(2), "production"))
                .andExpect(status().isCreated())).get("id").asString();
        tokens.revoke(reader.entity().getId());
        assertThat(devices.findById(UUID.fromString(mine))).isEmpty();
        assertThat(devices.findById(UUID.fromString(theirs))).isPresent();

        TokenService.CreatedToken shortLived = TestTokens.create(tokens, "short", Scope.READ, TokenRestrictions.NONE,
                T.plus(Duration.ofHours(1)));
        String expiring = json(send(post("/v1/push/devices"), TestTokens.bearer(shortLived),
                apnsBody(apnsToken(3), "production")).andExpect(status().isCreated())).get("id").asString();
        clock.advance(Duration.ofHours(2));
        assertThat(service.sweep()).isEqualTo(1);
        assertThat(devices.findById(UUID.fromString(expiring))).isEmpty();
        assertThat(devices.findById(UUID.fromString(theirs))).isPresent();
    }
}
