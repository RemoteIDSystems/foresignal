package ai.foresignal.notify.push.provider;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.notify.push.NativePushHttp;
import ai.foresignal.notify.push.apns.ApnsJwtCache;
import ai.foresignal.notify.push.fcm.FcmAccessTokenCache;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.push.NativeTestKeys;
import ai.foresignal.testsupport.push.PushTestConfig;
import ai.foresignal.testsupport.security.TestTokens;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.security.KeyPair;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
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
 * The native push provider operations over HTTP (prompt 032): create and replace, secrets never
 * returned, key validation, delete, tests, and the settings bundle. Every key is generated at test
 * time; the OAuth token endpoint is a WireMock stand-in, and nothing real is contacted.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(PushTestConfig.class)
class PushProvidersIT {

    @RegisterExtension
    static final WireMockExtension OAUTH = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort()).resetOnEachTest(true).build();

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired TokenService tokens;
    @Autowired PushProviderRepository repository;
    @Autowired ApnsJwtCache jwts;
    @Autowired FcmAccessTokenCache oauth;
    @Autowired NativePushHttp http;
    @Autowired MutableClock clock;

    String admin;
    TokenService.CreatedToken reader;
    final KeyPair ec = NativeTestKeys.ec();
    final KeyPair rsa = NativeTestKeys.rsa();

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        repository.deleteAll();
        jwts.clear();
        oauth.clear();
        http.reset();
        admin = "Bearer " + adminTokens.rotate();
        reader = TestTokens.create(tokens, "phone", Scope.READ);
        OAUTH.stubFor(WireMock.post(urlPathEqualTo("/token")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"access_token\":\"ya29.test\",\"expires_in\":3600}")));
    }

    ResultActions send(MockHttpServletRequestBuilder request, String bearer, Object body) throws Exception {
        MockHttpServletRequestBuilder r = bearer == null ? request : request.header(HttpHeaders.AUTHORIZATION, bearer);
        if (body != null) {
            r = r.contentType(MediaType.APPLICATION_JSON)
                    .content(body instanceof String s ? s : JSON.writeValueAsString(body));
        }
        return mvc.perform(r);
    }

    static JsonNode json(ResultActions result) throws Exception {
        return JSON.readTree(result.andReturn().getResponse().getContentAsString());
    }

    Map<String, Object> apns(String p8) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "apns");
        body.put("appIds", List.of("ai.foresignal.app"));
        body.put("config", Map.of("teamId", "TEAM123456", "keyId", "KEY1234567", "keyEnvironment", "production"));
        if (p8 != null) {
            body.put("secrets", Map.of("p8", p8));
        }
        return body;
    }

    Map<String, Object> fcm(String serviceAccount) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "fcm");
        body.put("appIds", List.of("ai.foresignal.android"));
        body.put("config", Map.of());
        if (serviceAccount != null) {
            body.put("secrets", Map.of("serviceAccountJson", serviceAccount));
        }
        return body;
    }

    String serviceAccount() {
        return NativeTestKeys.serviceAccountJson(rsa, "my-project", "sender@my-project.iam.gserviceaccount.com",
                OAUTH.baseUrl() + "/token");
    }

    @Test
    @DisplayName("PUT creates (201) and replaces (200); omitted secrets keep the key; responses never carry it")
    void crud() throws Exception {
        String p8 = NativeTestKeys.pem(ec);
        JsonNode created = json(send(put("/v1/push/providers/ios-apns"), admin, apns(p8)).andExpect(status().isCreated()));
        assertThat(created.get("id").asString()).isEqualTo("ios-apns");
        assertThat(created.get("type").asString()).isEqualTo("apns");
        assertThat(created.get("secretFields").get(0).asString()).isEqualTo("p8");
        assertThat(created.get("config").get("keyEnvironment").asString()).isEqualTo("production");
        assertThat(created.get("config").get("timeSensitive").asBoolean()).isFalse();
        assertThat(created.get("enabled").asBoolean()).isTrue();
        assertThat(created.toString()).doesNotContain("PRIVATE KEY").doesNotContain("secrets");

        Map<String, Object> replace = apns(null);
        replace.put("config", Map.of("teamId", "TEAM123456", "keyId", "KEY1234567", "keyEnvironment", "both",
                "timeSensitive", true));
        replace.put("enabled", false);
        JsonNode replaced = json(send(put("/v1/push/providers/ios-apns"), admin, replace).andExpect(status().isOk()));
        assertThat(replaced.get("secretFields").get(0).asString()).isEqualTo("p8");
        assertThat(replaced.get("enabled").asBoolean()).isFalse();
        assertThat(replaced.get("config").get("timeSensitive").asBoolean()).isTrue();
        assertThat(repository.findById("ios-apns").orElseThrow().hasSecret()).isTrue();

        String list = send(get("/v1/push/providers"), admin, null).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString();
        assertThat(list).contains("ios-apns").doesNotContain("PRIVATE KEY").doesNotContain(p8.substring(40, 80));

        send(delete("/v1/push/providers/ios-apns"), admin, null).andExpect(status().isNoContent());
        send(delete("/v1/push/providers/ios-apns"), admin, null).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("FCM: the service-account JSON fills projectId and clientEmail; only the key is stored, encrypted")
    void fcmServiceAccount() throws Exception {
        JsonNode created = json(send(put("/v1/push/providers/android-fcm"), admin, fcm(serviceAccount()))
                .andExpect(status().isCreated()));
        assertThat(created.get("config").get("projectId").asString()).isEqualTo("my-project");
        assertThat(created.get("config").get("clientEmail").asString())
                .isEqualTo("sender@my-project.iam.gserviceaccount.com");
        assertThat(created.get("secretFields").get(0).asString()).isEqualTo("serviceAccountJson");
        assertThat(created.toString()).doesNotContain("PRIVATE KEY").doesNotContain("private_key");
        PushProviderEntity row = repository.findById("android-fcm").orElseThrow();
        assertThat(new String(row.getSecretCiphertext(), java.nio.charset.StandardCharsets.ISO_8859_1))
                .doesNotContain("PRIVATE KEY");
    }

    @Test
    @DisplayName("invalid keys are rejected: an RSA key as .p8, garbage, JSON without private_key, not JSON")
    void invalidSecrets() throws Exception {
        for (String bad : List.of(NativeTestKeys.pem(rsa), "not a key", "-----BEGIN PRIVATE KEY-----\n!!!\n-----END PRIVATE KEY-----")) {
            JsonNode problem = json(send(put("/v1/push/providers/bad"), admin, apns(bad)).andExpect(status().isBadRequest()));
            assertThat(problem.toString()).contains("secrets.p8").doesNotContain("BEGIN PRIVATE KEY");
        }
        send(put("/v1/push/providers/bad"), admin, fcm("{\"type\":\"service_account\",\"project_id\":\"p\","
                + "\"client_email\":\"a@b.c\"}")).andExpect(status().isBadRequest());
        send(put("/v1/push/providers/bad"), admin, fcm("not json")).andExpect(status().isBadRequest());
        send(put("/v1/push/providers/bad"), admin, fcm(NativeTestKeys.serviceAccountJson(ec, "p", "a@b.c",
                "https://oauth2.googleapis.com/token"))).andExpect(status().isBadRequest());
        send(put("/v1/push/providers/bad"), admin, apns(null)).andExpect(status().isBadRequest());
        Map<String, Object> badConfig = apns(NativeTestKeys.pem(ec));
        badConfig.put("config", Map.of("teamId", "short", "keyId", "KEY1234567", "keyEnvironment", "staging"));
        JsonNode problem = json(send(put("/v1/push/providers/bad"), admin, badConfig).andExpect(status().isBadRequest()));
        assertThat(problem.toString()).contains("config.teamId").contains("config.keyEnvironment");
        send(put("/v1/push/providers/Bad_Id"), admin, apns(NativeTestKeys.pem(ec))).andExpect(status().isBadRequest());
        assertThat(repository.count()).isZero();
    }

    @Test
    @DisplayName("providers are admin only")
    void adminOnly() throws Exception {
        send(get("/v1/push/providers"), TestTokens.bearer(reader), null).andExpect(status().isForbidden());
        send(put("/v1/push/providers/x"), TestTokens.bearer(reader), apns(NativeTestKeys.pem(ec)))
                .andExpect(status().isForbidden());
        send(get("/v1/push/providers"), null, null).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("test mints an APNs JWT locally and an FCM OAuth token from the token endpoint; lastTestAt is set")
    void testMints() throws Exception {
        send(put("/v1/push/providers/ios-apns"), admin, apns(NativeTestKeys.pem(ec))).andExpect(status().isCreated());
        JsonNode apns = json(send(post("/v1/push/providers/ios-apns/test"), admin, null).andExpect(status().isOk()));
        assertThat(apns.get("success").asBoolean()).isTrue();
        assertThat(apns.get("credentialMinted").asBoolean()).isTrue();
        assertThat(apns.get("sent").isNull()).isTrue();
        assertThat(repository.findById("ios-apns").orElseThrow().getLastTestAt()).isEqualTo(T);

        send(put("/v1/push/providers/android-fcm"), admin, fcm(serviceAccount())).andExpect(status().isCreated());
        JsonNode fcm = json(send(post("/v1/push/providers/android-fcm/test"), admin, "{}").andExpect(status().isOk()));
        assertThat(fcm.get("credentialMinted").asBoolean()).isTrue();
        assertThat(OAUTH.findAll(WireMock.postRequestedFor(urlPathEqualTo("/token")))).hasSize(1);

        OAUTH.stubFor(WireMock.post(urlPathEqualTo("/token")).willReturn(aResponse().withStatus(401)
                .withHeader("Content-Type", "application/json").withBody("{\"error\":\"invalid_client\"}")));
        JsonNode failed = json(send(post("/v1/push/providers/android-fcm/test"), admin, null).andExpect(status().isOk()));
        assertThat(failed.get("success").asBoolean()).isFalse();
        assertThat(failed.get("error").asString()).contains("401").doesNotContain("PRIVATE");
        assertThat(repository.findById("android-fcm").orElseThrow().getLastError()).contains("401");

        send(post("/v1/push/providers/nope/test"), admin, null).andExpect(status().isNotFound());
        send(post("/v1/push/providers/ios-apns/test"), admin,
                "{\"deviceId\":\"00000000-0000-0000-0000-000000000099\"}").andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("settings export carries providers without secrets; import keeps an existing key and stores a new one disabled")
    void settingsBundle() throws Exception {
        send(put("/v1/push/providers/ios-apns"), admin, apns(NativeTestKeys.pem(ec))).andExpect(status().isCreated());
        JsonNode bundle = json(send(get("/v1/settings/export"), admin, null).andExpect(status().isOk()));
        JsonNode exported = bundle.get("pushProviders");
        assertThat(exported).hasSize(1);
        assertThat(exported.get(0).get("id").asString()).isEqualTo("ios-apns");
        assertThat(exported.toString()).doesNotContain("PRIVATE KEY").doesNotContain("\"secrets\"");

        tools.jackson.databind.node.ObjectNode copy = (tools.jackson.databind.node.ObjectNode) exported.get(0).deepCopy();
        copy.put("id", "ios-apns-copy");
        String importBody = "{\"sources\":{},\"pushProviders\":[" + exported.get(0) + "," + copy + "]}";
        JsonNode report = json(send(put("/v1/settings/import"), admin, importBody).andExpect(status().isOk()));
        assertThat(report.toString()).contains("push provider");
        assertThat(repository.findById("ios-apns").orElseThrow().hasSecret()).isTrue();
        assertThat(repository.findById("ios-apns").orElseThrow().isEnabled()).isTrue();
        PushProviderEntity imported = repository.findById("ios-apns-copy").orElseThrow();
        assertThat(imported.hasSecret()).isFalse();
        assertThat(imported.isEnabled()).isFalse();
    }
}
