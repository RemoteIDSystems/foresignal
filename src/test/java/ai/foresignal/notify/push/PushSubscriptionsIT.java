package ai.foresignal.notify.push;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.PushSettings;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.store.PushSubscriptionEntity;
import ai.foresignal.store.PushSubscriptionRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.push.MutableResolver;
import ai.foresignal.testsupport.push.PushTestConfig;
import ai.foresignal.testsupport.push.TestSubscriber;
import ai.foresignal.testsupport.security.TestTokens;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
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
 * The push subscription operations over HTTP (prompt 031): validation, the address policy, caps,
 * registration verification, expiry, ownership, the admin list and the revoke cascade. The push
 * service is a WireMock stand-in over https on localhost, reached as {@code push.test} through the
 * DNS stand-in and the {@code push.allowedPrivateHosts} allowlist; nothing real is contacted.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(PushTestConfig.class)
class PushSubscriptionsIT {

    @RegisterExtension
    static final WireMockExtension PUSH = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().dynamicHttpsPort().gzipDisabled(true))
            .failOnUnmatchedRequests(true)
            .resetOnEachTest(true)
            .build();

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired TokenService tokens;
    @Autowired AppSettingService settings;
    @Autowired PushSubscriptionRepository subscriptions;
    @Autowired PushSubscriptionService service;
    @Autowired PushHostLimiter limiter;
    @Autowired PushHttp http;
    @Autowired VapidTokens vapidTokens;
    @Autowired MutableResolver dns;
    @Autowired MutableClock clock;

    String admin;
    TokenService.CreatedToken reader;
    TokenService.CreatedToken otherReader;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        dns.clear();
        dns.set("push.test", "127.0.0.1");
        settings.put(SettingKeys.SERVER, new ServerSettings(null, null, null,
                new PushSettings(null, List.of("push.test"), null)));
        settings.invalidateCache();
        limiter.reset();
        http.reset();
        vapidTokens.clear();
        admin = "Bearer " + adminTokens.rotate();
        reader = TestTokens.create(tokens, "phone", Scope.READ);
        otherReader = TestTokens.create(tokens, "other", Scope.READ);
        PUSH.stubFor(WireMock.post(urlPathMatching("/push/.*")).willReturn(aResponse().withStatus(201)));
    }

    static String endpoint(String id) {
        return "https://push.test:" + PUSH.getRuntimeInfo().getHttpsPort() + "/push/" + id;
    }

    ResultActions send(MockHttpServletRequestBuilder request, String bearer, String body) throws Exception {
        MockHttpServletRequestBuilder r = bearer == null ? request : request.header(HttpHeaders.AUTHORIZATION, bearer);
        if (body != null) {
            r = r.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        return mvc.perform(r);
    }

    JsonNode json(ResultActions result) throws Exception {
        return JSON.readTree(result.andReturn().getResponse().getContentAsString());
    }

    /** Creates a subscription for {@code token} and returns it with the code its verify push carried. */
    record Subscribed(String id, String code, JsonNode created) {}

    Subscribed subscribe(TestSubscriber subscriber, TokenService.CreatedToken token, String extra) throws Exception {
        JsonNode created = json(send(post("/v1/push/subscriptions"), TestTokens.bearer(token), subscriber.body(extra))
                .andExpect(status().isCreated()));
        String path = java.net.URI.create(subscriber.endpoint()).getPath();
        List<LoggedRequest> pushes = PUSH.findAll(postRequestedFor(urlPathEqualTo(path)));
        JsonNode verify = subscriber.decrypt(pushes.getLast().getBody());
        return new Subscribed(created.get("id").asString(), verify.path("code").asString(), created);
    }

    @Test
    @DisplayName("GET /v1/push/vapid-public-key is open and returns an 87-character P-256 point")
    void vapidKey() throws Exception {
        JsonNode key = json(send(get("/v1/push/vapid-public-key"), null, null).andExpect(status().isOk()));
        String value = key.get("publicKey").asString();
        assertThat(value).hasSize(87);
        assertThat(EcKeys.decodePoint(EcKeys.unb64(value))).isNotNull();
    }

    @Test
    @DisplayName("validation: http, user info, long endpoints, bad key lengths, off-curve keys and bad members are 400")
    void validation() throws Exception {
        TestSubscriber s = new TestSubscriber(endpoint("v"));
        String good = s.p256dh();
        String[] bad = {
            "{\"subscription\":{\"endpoint\":\"http://push.test/p\",\"keys\":{\"p256dh\":\"" + good + "\",\"auth\":\"" + s.auth() + "\"}}}",
            "{\"subscription\":{\"endpoint\":\"https://u:p@push.test/p\",\"keys\":{\"p256dh\":\"" + good + "\",\"auth\":\"" + s.auth() + "\"}}}",
            "{\"subscription\":{\"endpoint\":\"https://push.test/" + "x".repeat(1000) + "\",\"keys\":{\"p256dh\":\"" + good + "\",\"auth\":\"" + s.auth() + "\"}}}",
            "{\"subscription\":{\"endpoint\":\"" + s.endpoint() + "\",\"keys\":{\"p256dh\":\"" + good.substring(0, 80) + "\",\"auth\":\"" + s.auth() + "\"}}}",
            "{\"subscription\":{\"endpoint\":\"" + s.endpoint() + "\",\"keys\":{\"p256dh\":\"" + good + "\",\"auth\":\"AAAA\"}}}",
            "{\"subscription\":{\"endpoint\":\"" + s.endpoint() + "\",\"keys\":{\"p256dh\":\"B" + "A".repeat(86) + "\",\"auth\":\"" + s.auth() + "\"}}}",
            "{\"subscription\":{\"endpoint\":\"" + s.endpoint() + "\"}}",
            "{}",
            s.body("\"transportLabel\":\"carrier-pigeon\""),
            s.body("\"payloadFormat\":\"xml\""),
            s.body("\"filters\":{\"events\":[\"weather\"]}"),
            s.body("\"filters\":{\"minSeverity\":\"loud\"}"),
            s.body("\"lang\":\"english please\""),
            s.body("\"colour\":1")};
        for (String body : bad) {
            JsonNode problem = json(send(post("/v1/push/subscriptions"), TestTokens.bearer(reader), body)
                    .andExpect(status().isBadRequest()));
            assertThat(problem.get("code").asString()).as(body).isEqualTo("VALIDATION_FAILED");
        }
        assertThat(subscriptions.count()).isZero();
        PUSH.verify(0, postRequestedFor(urlPathMatching("/.*")));
    }

    @Test
    @DisplayName("a private host outside the allowlist is 400 URL_NOT_ALLOWED and nothing is sent")
    void privateHost() throws Exception {
        dns.set("lan.test", "192.168.1.5");
        TestSubscriber s = new TestSubscriber("https://lan.test:" + PUSH.getRuntimeInfo().getHttpsPort() + "/push/x");
        JsonNode problem = json(send(post("/v1/push/subscriptions"), TestTokens.bearer(reader), s.body(null))
                .andExpect(status().isBadRequest()));
        assertThat(problem.get("code").asString()).isEqualTo("URL_NOT_ALLOWED");
        dns.set("mixed.test", "8.8.8.8", "10.0.0.1");
        TestSubscriber mixed = new TestSubscriber("https://mixed.test/push/y");
        assertThat(json(send(post("/v1/push/subscriptions"), TestTokens.bearer(reader), mixed.body(null))
                .andExpect(status().isBadRequest())).get("code").asString()).isEqualTo("URL_NOT_ALLOWED");
        assertThat(subscriptions.count()).isZero();
        PUSH.verify(0, postRequestedFor(urlPathMatching("/.*")));
    }

    @Test
    @DisplayName("without a token the subscription operations are 401")
    void needsToken() throws Exception {
        TestSubscriber s = new TestSubscriber(endpoint("anon"));
        send(post("/v1/push/subscriptions"), null, s.body(null)).andExpect(status().isUnauthorized());
        send(get("/v1/push/subscriptions"), TestTokens.bearer(reader), null).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("pending → verification push (TTL 600, Urgency high) → echoed code → active")
    void verifyFlow() throws Exception {
        TestSubscriber s = new TestSubscriber(endpoint("a1"));
        Subscribed sub = subscribe(s, reader, "\"transportLabel\":\"unifiedpush\",\"lang\":\"de\","
                + "\"filters\":{\"events\":[\"alert\"],\"minSeverity\":\"warning\",\"tags\":[\"quake\"]}");
        assertThat(sub.created().get("status").asString()).isEqualTo("pending");
        assertThat(sub.created().get("endpointHost").asString()).isEqualTo("push.test");
        assertThat(sub.created().has("endpoint")).isFalse();
        assertThat(sub.created().get("transportLabel").asString()).isEqualTo("unifiedpush");
        assertThat(sub.created().get("filters").get("tags").get(0).asString()).isEqualTo("quake");
        PUSH.verify(1, postRequestedFor(urlPathEqualTo("/push/a1"))
                .withHeader("TTL", equalTo("600"))
                .withHeader("Urgency", equalTo("high"))
                .withHeader("Content-Encoding", equalTo("aes128gcm")));
        assertThat(sub.code()).hasSize(22);
        PushSubscriptionEntity row = subscriptions.findById(UUID.fromString(sub.id())).orElseThrow();
        assertThat(row.getVerifyCodeHash()).isNotEqualTo(sub.code()).hasSize(64);
        assertThat(row.getTokenId()).isEqualTo(reader.entity().getId());

        JsonNode active = json(send(post("/v1/push/subscriptions/" + sub.id() + "/verify"), TestTokens.bearer(reader),
                "{\"code\":\"" + sub.code() + "\"}").andExpect(status().isOk()));
        assertThat(active.get("status").asString()).isEqualTo("active");
        // Idempotent once active.
        send(post("/v1/push/subscriptions/" + sub.id() + "/verify"), TestTokens.bearer(reader), "{\"code\":\"x\"}")
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("a wrong code is 400 VERIFY_FAILED; the fifth deletes; another token gets 404")
    void wrongCode() throws Exception {
        Subscribed sub = subscribe(new TestSubscriber(endpoint("w1")), reader, null);
        send(post("/v1/push/subscriptions/" + sub.id() + "/verify"), TestTokens.bearer(otherReader),
                "{\"code\":\"" + sub.code() + "\"}").andExpect(status().isNotFound());
        for (int i = 1; i <= 4; i++) {
            JsonNode problem = json(send(post("/v1/push/subscriptions/" + sub.id() + "/verify"), TestTokens.bearer(reader),
                    "{\"code\":\"wrong\"}").andExpect(status().isBadRequest()));
            assertThat(problem.get("code").asString()).isEqualTo("VERIFY_FAILED");
            assertThat(subscriptions.findById(UUID.fromString(sub.id()))).isPresent();
        }
        send(post("/v1/push/subscriptions/" + sub.id() + "/verify"), TestTokens.bearer(reader), "{\"code\":\"wrong\"}")
                .andExpect(status().isBadRequest());
        assertThat(subscriptions.findById(UUID.fromString(sub.id()))).isEmpty();
        send(post("/v1/push/subscriptions/" + sub.id() + "/verify"), TestTokens.bearer(reader),
                "{\"code\":\"" + sub.code() + "\"}").andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("pending rows expire: verify after 10 minutes is 404 and the sweeper deletes them; active rows stay")
    void expirySweep() throws Exception {
        Subscribed pending = subscribe(new TestSubscriber(endpoint("e1")), reader, null);
        Subscribed verified = subscribe(new TestSubscriber(endpoint("e2")), reader, null);
        send(post("/v1/push/subscriptions/" + verified.id() + "/verify"), TestTokens.bearer(reader),
                "{\"code\":\"" + verified.code() + "\"}").andExpect(status().isOk());
        clock.advance(Duration.ofMinutes(10).plusSeconds(1));
        send(post("/v1/push/subscriptions/" + pending.id() + "/verify"), TestTokens.bearer(reader),
                "{\"code\":\"" + pending.code() + "\"}").andExpect(status().isNotFound());
        assertThat(service.sweep()).isEqualTo(1);
        assertThat(subscriptions.findById(UUID.fromString(pending.id()))).isEmpty();
        assertThat(subscriptions.findById(UUID.fromString(verified.id()))).isPresent();
    }

    @Test
    @DisplayName("at most 5 pending per token and 200 in total: 409 PUSH_LIMIT_REACHED")
    void limits() throws Exception {
        for (int i = 0; i < 5; i++) {
            subscribe(new TestSubscriber(endpoint("l" + i)), reader, null);
        }
        JsonNode problem = json(send(post("/v1/push/subscriptions"), TestTokens.bearer(reader),
                new TestSubscriber(endpoint("l5")).body(null)).andExpect(status().isConflict()));
        assertThat(problem.get("code").asString()).isEqualTo("PUSH_LIMIT_REACHED");
        // Another token still may.
        subscribe(new TestSubscriber(endpoint("o1")), otherReader, null);

        TestSubscriber filler = new TestSubscriber(endpoint("filler"));
        for (long i = subscriptions.count(); i < PushSubscriptionService.MAX_TOTAL; i++) {
            PushSubscriptionEntity e = new PushSubscriptionEntity(UUID.randomUUID(), endpoint("f" + i), filler.p256dh(),
                    filler.auth(), T);
            e.setStatus("active");
            e.setEndpointHost("push.test");
            e.setTransportLabel("browser");
            e.setPayloadFormat("foresignal");
            subscriptions.save(e);
        }
        TokenService.CreatedToken third = TestTokens.create(tokens, "third", Scope.READ);
        assertThat(json(send(post("/v1/push/subscriptions"), TestTokens.bearer(third),
                new TestSubscriber(endpoint("over")).body(null)).andExpect(status().isConflict())).get("code").asString())
                .isEqualTo("PUSH_LIMIT_REACHED");
    }

    @Test
    @DisplayName("subscribing an endpoint again replaces the earlier subscription")
    void sameEndpointReplaces() throws Exception {
        TestSubscriber s = new TestSubscriber(endpoint("same"));
        Subscribed first = subscribe(s, reader, null);
        Subscribed second = subscribe(s, reader, null);
        assertThat(second.id()).isNotEqualTo(first.id());
        assertThat(subscriptions.findAll()).singleElement().satisfies(e -> assertThat(e.getId().toString()).isEqualTo(second.id()));
    }

    @Test
    @DisplayName("delete: another read token gets 404, the creator 204, an admin may delete any")
    void deleteOwnership() throws Exception {
        Subscribed mine = subscribe(new TestSubscriber(endpoint("d1")), reader, null);
        Subscribed theirs = subscribe(new TestSubscriber(endpoint("d2")), otherReader, null);
        send(delete("/v1/push/subscriptions/" + mine.id()), TestTokens.bearer(otherReader), null).andExpect(status().isNotFound());
        send(delete("/v1/push/subscriptions/" + mine.id()), TestTokens.bearer(reader), null).andExpect(status().isNoContent());
        send(delete("/v1/push/subscriptions/" + mine.id()), TestTokens.bearer(reader), null).andExpect(status().isNotFound());
        send(delete("/v1/push/subscriptions/" + theirs.id()), admin, null).andExpect(status().isNoContent());
        send(delete("/v1/push/subscriptions/not-a-uuid"), admin, null).andExpect(status().isNotFound());
        assertThat(subscriptions.count()).isZero();
    }

    @Test
    @DisplayName("the admin list shows endpointHost and status, never the endpoint")
    void adminListHidesEndpoints() throws Exception {
        subscribe(new TestSubscriber(endpoint("secret-capability-path")), reader, null);
        String body = send(get("/v1/push/subscriptions"), admin, null).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString();
        JsonNode list = JSON.readTree(body);
        assertThat(list).hasSize(1);
        assertThat(list.get(0).get("endpointHost").asString()).isEqualTo("push.test");
        assertThat(list.get(0).get("status").asString()).isEqualTo("pending");
        assertThat(list.get(0).get("failureCount").asInt()).isZero();
        assertThat(list.get(0).has("endpoint")).isFalse();
        assertThat(body).doesNotContain("secret-capability-path");
    }

    @Test
    @DisplayName("revoking a token deletes its subscriptions; the sweeper removes those of expired tokens")
    void revokeCascade() throws Exception {
        Subscribed mine = subscribe(new TestSubscriber(endpoint("r1")), reader, null);
        Subscribed theirs = subscribe(new TestSubscriber(endpoint("r2")), otherReader, null);
        tokens.revoke(reader.entity().getId());
        assertThat(subscriptions.findById(UUID.fromString(mine.id()))).isEmpty();
        assertThat(subscriptions.findById(UUID.fromString(theirs.id()))).isPresent();

        TokenService.CreatedToken shortLived = TestTokens.create(tokens, "short", Scope.READ,
                ai.foresignal.security.TokenRestrictions.NONE, T.plus(Duration.ofHours(1)));
        Subscribed expiring = subscribe(new TestSubscriber(endpoint("r3")), shortLived, null);
        send(post("/v1/push/subscriptions/" + expiring.id() + "/verify"), TestTokens.bearer(shortLived),
                "{\"code\":\"" + expiring.code() + "\"}").andExpect(status().isOk());
        clock.advance(Duration.ofHours(2));
        service.sweep();
        assertThat(subscriptions.findById(UUID.fromString(expiring.id()))).isEmpty();
    }

    @Test
    @DisplayName("PUT /v1/settings/server stores push settings and validates the allowlist")
    void serverSettings() throws Exception {
        String body = "{\"publicBaseUrl\":null,\"push\":{\"contact\":\"mailto:ops@fs.example\","
                + "\"allowedPrivateHosts\":[\"distributor.lan\",\"192.168.0.0/16\"],\"defaultLang\":\"fr\"}}";
        JsonNode view = json(send(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/v1/settings/server"),
                admin, body).andExpect(status().isOk()));
        assertThat(view.get("push").get("contact").asString()).isEqualTo("mailto:ops@fs.example");
        assertThat(view.get("push").get("allowedPrivateHosts")).hasSize(2);
        send(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/v1/settings/server"), admin,
                "{\"publicBaseUrl\":null,\"push\":{\"allowedPrivateHosts\":[\"*.lan\"]}}").andExpect(status().isBadRequest());
    }
}
