package ai.foresignal.notify.push;

import static ai.foresignal.testsupport.notify.NotifyFixture.alert;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.DeliveryWorker;
import ai.foresignal.notify.NotificationRouter;
import ai.foresignal.notify.push.apns.ApnsJwtCache;
import ai.foresignal.notify.push.apns.ApnsPayloads;
import ai.foresignal.notify.push.device.NativePushTester;
import ai.foresignal.notify.push.device.PushDeviceEntity;
import ai.foresignal.notify.push.device.PushDeviceRepository;
import ai.foresignal.notify.push.device.PushDeviceService;
import ai.foresignal.notify.push.fcm.FcmAccessTokenCache;
import ai.foresignal.notify.push.provider.PushProviderRepository;
import ai.foresignal.notify.push.provider.PushProviderService;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.store.NotificationRepository;
import ai.foresignal.store.PushDispatchEntity;
import ai.foresignal.store.PushDispatchRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.push.MutableNativeEndpoints;
import ai.foresignal.testsupport.push.NativeTestKeys;
import ai.foresignal.testsupport.push.PushTestConfig;
import ai.foresignal.testsupport.security.TestTokens;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Native push delivery end to end (prompt 032): the built-in Push channel fans out to APNs and FCM
 * devices, each sent to a WireMock stand-in (APNs over https, where the JDK client asks for HTTP/2;
 * FCM and the OAuth token endpoint over http). Keys are generated at test time; no real push is
 * ever sent.
 */
@IntegrationTest
@Import(PushTestConfig.class)
class NativePushDeliveryIT {

    @RegisterExtension
    static final WireMockExtension STUB = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().dynamicHttpsPort().gzipDisabled(true))
            .resetOnEachTest(true)
            .build();

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final UUID RULE = UUID.fromString("00000000-0000-0000-0000-0000000000d1");
    static final JsonMapper JSON = JsonMapper.builder().build();
    static final String APP = "ai.foresignal.app";
    static final String APNS_TOKEN = String.format("%064x", 0xfeed);
    static final String FCM_TOKEN = "fcm-registration-token-1";

    @Autowired NotificationRouter router;
    @Autowired DeliveryWorker worker;
    @Autowired PushDispatchWorker pushWorker;
    @Autowired PushBootstrap bootstrap;
    @Autowired PushProviderService providers;
    @Autowired PushProviderRepository providerRows;
    @Autowired PushDeviceService deviceService;
    @Autowired PushDeviceRepository devices;
    @Autowired PushDispatchRepository dispatches;
    @Autowired NotificationRepository notifications;
    @Autowired NativePushTester tester;
    @Autowired ApnsJwtCache jwts;
    @Autowired FcmAccessTokenCache oauth;
    @Autowired NativePushHttp http;
    @Autowired MutableNativeEndpoints endpoints;
    @Autowired TokenService tokens;
    @Autowired MutableClock clock;

    final KeyPair ec = NativeTestKeys.ec();
    AuthenticatedToken owner;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        devices.deleteAll();
        providerRows.deleteAll();
        jwts.clear();
        oauth.clear();
        http.reset();
        tester.reset();
        assertThat(bootstrap.ensureChannel()).isTrue();
        String https = "https://localhost:" + STUB.getRuntimeInfo().getHttpsPort();
        endpoints.set(https, https + "/sandbox", STUB.baseUrl());
        owner = AuthenticatedToken.of(TestTokens.create(tokens, "phone", Scope.READ).entity());
        providers.put("ios-apns", JSON.readTree("""
                {"type":"apns","appIds":["%s"],
                 "config":{"teamId":"TEAM123456","keyId":"KEY1234567","keyEnvironment":"both","timeSensitive":true},
                 "secrets":{"p8":%s}}
                """.formatted(APP, JSON.writeValueAsString(NativeTestKeys.pem(ec)))));
        String account = NativeTestKeys.serviceAccountJson(NativeTestKeys.rsa(), "my-project",
                "sender@my-project.iam.gserviceaccount.com", STUB.baseUrl() + "/token");
        providers.put("android-fcm", JSON.readTree("{\"type\":\"fcm\",\"appIds\":[\"" + APP + "\"],\"config\":{},"
                + "\"secrets\":{\"serviceAccountJson\":" + JSON.writeValueAsString(account) + "}}"));
        STUB.stubFor(post(urlPathEqualTo("/token")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody("{\"access_token\":\"ya29.test\",\"expires_in\":3600}")));
        STUB.stubFor(post(urlPathMatching("/3/device/.*")).willReturn(aResponse().withStatus(200)));
        STUB.stubFor(post(urlPathEqualTo("/v1/projects/my-project/messages:send")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody("{\"name\":\"projects/my-project/messages/1\"}")));
    }

    PushDeviceEntity iphone(String filters) {
        return deviceService.register(JSON.readTree("{\"transport\":\"apns\",\"platform\":\"ios\",\"appId\":\"" + APP
                + "\",\"environment\":\"production\",\"token\":\"" + APNS_TOKEN + "\""
                + (filters == null ? "" : ",\"filters\":" + filters) + "}"), owner).device();
    }

    PushDeviceEntity android() {
        return deviceService.register(JSON.readTree("{\"transport\":\"fcm\",\"platform\":\"android\",\"appId\":\"" + APP
                + "\",\"token\":\"" + FCM_TOKEN + "\"}"), owner).device();
    }

    List<LoggedRequest> apnsRequests() {
        return STUB.findAll(postRequestedFor(urlPathEqualTo("/3/device/" + APNS_TOKEN)));
    }

    List<LoggedRequest> fcmRequests() {
        return STUB.findAll(postRequestedFor(urlPathEqualTo("/v1/projects/my-project/messages:send")));
    }

    void deliverAll() {
        worker.runOnce();
        pushWorker.runOnce();
    }

    PushDispatchEntity dispatchOf(PushDeviceEntity device) {
        return dispatches.findAll().stream().filter(d -> d.getTargetId().equals(device.getId().toString()))
                .reduce((a, b) -> b).orElseThrow();
    }

    @Test
    @DisplayName("APNs: path, provider JWT, topic, push type, priority, expiration, collapse id, apns-id and a minimal body")
    void apnsRequest() {
        PushDeviceEntity device = iphone(null);
        router.fired(alert(1, RULE, "Quakes", "critical", T));
        deliverAll();

        assertThat(apnsRequests()).singleElement().satisfies(r -> {
            String authorization = r.getHeader("authorization");
            assertThat(authorization).startsWith("bearer ");
            assertThat(Vapid.verify(authorization.substring(7), NativeTestKeys.ecPublic(ec))).isTrue();
            assertThat(r.getHeader("apns-topic")).isEqualTo(APP);
            assertThat(r.getHeader("apns-push-type")).isEqualTo("alert");
            assertThat(r.getHeader("apns-priority")).isEqualTo("10");
            assertThat(r.getHeader("apns-expiration")).isEqualTo(Long.toString(T.plus(Duration.ofHours(24)).getEpochSecond()));
            assertThat(r.getHeader("apns-collapse-id")).startsWith("a-")
                    .isEqualTo("a-" + PushTopic.alert(RULE.toString(), "usgs-earthquakes", "us1"));
            assertThat(r.getHeader("apns-id")).isEqualTo(ApnsPayloads.apnsId(dispatchOf(device).getId()).toString());
            assertThat(r.getBody().length).isLessThanOrEqualTo(4096);
            JsonNode body = JSON.readTree(r.getBodyAsString());
            assertThat(body.get("aps").get("alert").get("title").asString()).isEqualTo("Foresignal");
            assertThat(body.get("aps").get("alert").get("body").asString()).isEqualTo("Critical alert");
            assertThat(body.get("aps").get("interruption-level").asString()).isEqualTo("time-sensitive");
            assertThat(body.get("aps").get("mutable-content").asInt()).isEqualTo(1);
            assertThat(body.get("aps").has("content-available")).isFalse();
            assertThat(body.get("e").get("a").asLong()).isEqualTo(1);
            assertThat(body.get("e").get("id").asLong()).isPositive();
            assertThat(body.get("e").get("s").asString()).isEqualTo("critical");
            assertThat(body.get("e").get("l").asString()).isEqualTo("foresignal://alerts/1");
            assertThat(r.getBodyAsString()).doesNotContain("Austin").doesNotContain("Quakes");
        });
        assertThat(dispatchOf(device).getStatus()).isEqualTo("sent");
        PushDeviceEntity after = devices.findById(device.getId()).orElseThrow();
        assertThat(after.getLastSuccessAt()).isEqualTo(T);
        assertThat(after.getLastStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("FCM: OAuth bearer, projects/<id>/messages:send, a HIGH data message with ids and minimal text")
    void fcmRequest() {
        PushDeviceEntity device = android();
        router.fired(alert(2, RULE, "Quakes", "warning", T));
        deliverAll();

        assertThat(fcmRequests()).singleElement().satisfies(r -> {
            assertThat(r.getHeader("Authorization")).isEqualTo("Bearer ya29.test");
            JsonNode message = JSON.readTree(r.getBodyAsString()).get("message");
            assertThat(message.get("token").asString()).isEqualTo(FCM_TOKEN);
            assertThat(message.get("data").get("a").asString()).isEqualTo("2");
            assertThat(message.get("data").get("s").asString()).isEqualTo("warning");
            assertThat(message.get("data").get("ev").asString()).isEqualTo("alert");
            assertThat(message.get("data").get("t").asString()).isEqualTo("Warning alert");
            assertThat(message.get("android").get("priority").asString()).isEqualTo("HIGH");
            assertThat(message.get("android").get("ttl").asString()).isEqualTo("86400s");
            assertThat(message.has("notification")).isFalse();
            assertThat(r.getBodyAsString()).doesNotContain("Austin");
        });
        assertThat(STUB.findAll(postRequestedFor(urlPathEqualTo("/token")))).hasSize(1);
        assertThat(dispatchOf(device).getStatus()).isEqualTo("sent");
    }

    @Test
    @DisplayName("APNs 410 Unregistered and FCM 404 UNREGISTERED delete the device")
    void goneDeletesDevices() {
        PushDeviceEntity ios = iphone(null);
        PushDeviceEntity droid = android();
        STUB.stubFor(post(urlPathMatching("/3/device/.*")).willReturn(aResponse().withStatus(410)
                .withHeader("Content-Type", "application/json").withBody("{\"reason\":\"Unregistered\",\"timestamp\":1}")));
        STUB.stubFor(post(urlPathEqualTo("/v1/projects/my-project/messages:send")).willReturn(aResponse().withStatus(404)
                .withHeader("Content-Type", "application/json").withBody("""
                        {"error":{"code":404,"message":"Requested entity was not found.","status":"NOT_FOUND",
                          "details":[{"@type":"type.googleapis.com/google.firebase.fcm.v1.FcmError","errorCode":"UNREGISTERED"}]}}
                        """)));
        router.fired(alert(3, RULE, "Quakes", "warning", T));
        deliverAll();
        assertThat(devices.findById(ios.getId())).isEmpty();
        assertThat(devices.findById(droid.getId())).isEmpty();
        assertThat(dispatchOf(ios).getStatus()).isEqualTo("gone");
        assertThat(dispatchOf(droid).getStatus()).isEqualTo("gone");
    }

    @Test
    @DisplayName("APNs 400 BadDeviceToken is gone; FCM INVALID_ARGUMENT naming the token is gone")
    void badTokens() {
        PushDeviceEntity ios = iphone(null);
        PushDeviceEntity droid = android();
        STUB.stubFor(post(urlPathMatching("/3/device/.*")).willReturn(aResponse().withStatus(400)
                .withBody("{\"reason\":\"BadDeviceToken\"}")));
        STUB.stubFor(post(urlPathEqualTo("/v1/projects/my-project/messages:send")).willReturn(aResponse().withStatus(400)
                .withBody("""
                        {"error":{"code":400,"status":"INVALID_ARGUMENT","details":[
                          {"@type":"type.googleapis.com/google.rpc.BadRequest","fieldViolations":[{"field":"message.token",
                           "description":"Invalid registration token"}]}]}}
                        """)));
        router.fired(alert(4, RULE, "Quakes", "warning", T));
        deliverAll();
        assertThat(devices.findById(ios.getId())).isEmpty();
        assertThat(devices.findById(droid.getId())).isEmpty();
    }

    @Test
    @DisplayName("APNs 429 TooManyRequests is retried after a wait; the device counts a failure and stays")
    void tooManyRequests() {
        PushDeviceEntity device = iphone(null);
        STUB.stubFor(post(urlPathMatching("/3/device/.*")).willReturn(aResponse().withStatus(429)
                .withBody("{\"reason\":\"TooManyRequests\"}")));
        router.fired(alert(5, RULE, "Quakes", "warning", T));
        deliverAll();
        PushDispatchEntity dispatch = dispatchOf(device);
        assertThat(dispatch.getStatus()).isEqualTo("pending");
        assertThat(dispatch.getNextAttemptAt()).isEqualTo(T.plus(Duration.ofMinutes(1)));
        assertThat(dispatch.getLastStatus()).isEqualTo(429);
        PushDeviceEntity after = devices.findById(device.getId()).orElseThrow();
        assertThat(after.getFailureCount()).isEqualTo(1);

        STUB.stubFor(post(urlPathMatching("/3/device/.*")).willReturn(aResponse().withStatus(200)));
        clock.advance(Duration.ofMinutes(1));
        pushWorker.runOnce();
        assertThat(dispatchOf(device).getStatus()).isEqualTo("sent");
        assertThat(apnsRequests()).hasSize(2);
    }

    @Test
    @DisplayName("ExpiredProviderToken: the JWT is regenerated once and the push retried")
    void expiredProviderToken() {
        iphone(null);
        STUB.stubFor(post(urlPathMatching("/3/device/.*")).inScenario("expired").whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(403).withBody("{\"reason\":\"ExpiredProviderToken\"}"))
                .willSetStateTo("refreshed"));
        STUB.stubFor(post(urlPathMatching("/3/device/.*")).inScenario("expired").whenScenarioStateIs("refreshed")
                .willReturn(aResponse().withStatus(200)));
        router.fired(alert(6, RULE, "Quakes", "warning", T));
        deliverAll();
        List<LoggedRequest> sent = apnsRequests();
        assertThat(sent).hasSize(2);
        assertThat(sent.get(0).getHeader("authorization")).isNotEqualTo(sent.get(1).getHeader("authorization"));
        assertThat(dispatches.findAll()).allSatisfy(d -> assertThat(d.getStatus()).isEqualTo("sent"));
        assertThat(providerRows.findById("ios-apns").orElseThrow().getLastError()).isNull();
    }

    @Test
    @DisplayName("a provider credential error records last_error and raises exactly one system notification")
    void providerErrorRaisesOneSystemEvent() {
        iphone(null);
        STUB.stubFor(post(urlPathMatching("/3/device/.*")).willReturn(aResponse().withStatus(403)
                .withBody("{\"reason\":\"InvalidProviderToken\"}")));
        router.fired(alert(7, RULE, "Quakes", "warning", T));
        router.fired(alert(8, RULE, "Quakes", "warning", T));
        deliverAll();
        assertThat(providerRows.findById("ios-apns").orElseThrow().getLastError())
                .isEqualTo("APNs 403 InvalidProviderToken");
        long raised = notifications.findAll().stream()
                .filter(n -> "system.push_provider_failed".equals(n.getSubtype())).count();
        assertThat(raised).isEqualTo(1);
        assertThat(dispatches.findAll()).allSatisfy(d -> {
            assertThat(d.getStatus()).isEqualTo("pending");
            assertThat(d.getLastError()).doesNotContain(APNS_TOKEN);
        });
    }

    @Test
    @DisplayName("filters are honored: a critical-only device receives no warning")
    void filters() {
        iphone("{\"minSeverity\":\"critical\"}");
        android();
        router.fired(alert(9, RULE, "Quakes", "warning", T));
        deliverAll();
        assertThat(apnsRequests()).isEmpty();
        assertThat(fcmRequests()).hasSize(1);
    }

    @Test
    @DisplayName("a failing APNs target does not block the FCM target of the same delivery")
    void independentTargets() {
        PushDeviceEntity ios = iphone(null);
        PushDeviceEntity droid = android();
        STUB.stubFor(post(urlPathMatching("/3/device/.*")).willReturn(aResponse().withStatus(500)
                .withBody("{\"reason\":\"InternalServerError\"}")));
        router.fired(alert(10, RULE, "Quakes", "critical", T));
        deliverAll();
        assertThat(dispatchOf(droid).getStatus()).isEqualTo("sent");
        PushDispatchEntity failed = dispatchOf(ios);
        assertThat(failed.getStatus()).isEqualTo("pending");
        assertThat(failed.getAttempts()).isEqualTo(1);
        assertThat(failed.getLastStatus()).isEqualTo(500);
        assertThat(fcmRequests()).hasSize(1);
    }

    @Test
    @DisplayName("a disabled provider's devices receive nothing")
    void disabledProvider() {
        iphone(null);
        providers.put("ios-apns", JSON.readTree("""
                {"type":"apns","appIds":["%s"],"enabled":false,
                 "config":{"teamId":"TEAM123456","keyId":"KEY1234567","keyEnvironment":"both"}}
                """.formatted(APP)));
        router.fired(alert(11, RULE, "Quakes", "critical", T));
        deliverAll();
        assertThat(apnsRequests()).isEmpty();
    }

    @Test
    @DisplayName("device test: owner only, a minimal system notice, 5 per hour")
    void deviceTest() {
        PushDeviceEntity device = iphone(null);
        for (int i = 0; i < 5; i++) {
            assertThat(tester.testDevice(device.getId(), owner)).containsEntry("success", true).containsEntry("sent", true);
        }
        JsonNode body = JSON.readTree(apnsRequests().getFirst().getBodyAsString());
        assertThat(body.get("aps").get("alert").get("body").asString()).isEqualTo("System notice");
        assertThat(body.get("e").get("id").asLong()).isZero();
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> tester.testDevice(device.getId(), owner))
                .isInstanceOf(ai.foresignal.api.error.ForesignalException.class)
                .hasMessageContaining("5 test pushes");
        clock.advance(Duration.ofHours(1));
        assertThat(tester.testDevice(device.getId(), owner)).containsEntry("success", true);
        assertThat(new String(apnsRequests().getFirst().getBody(), StandardCharsets.UTF_8)).doesNotContain(APNS_TOKEN);
    }

    @Test
    @DisplayName("provider test with a device sends a test alert through that provider")
    void providerTestWithDevice() {
        PushDeviceEntity droid = android();
        java.util.Map<String, Object> result = tester.testProvider("android-fcm",
                JSON.readTree("{\"deviceId\":\"" + droid.getId() + "\"}"));
        assertThat(result).containsEntry("success", true).containsEntry("credentialMinted", true)
                .containsEntry("sent", true).containsEntry("status", 200);
        assertThat(fcmRequests()).hasSize(1);
    }
}
