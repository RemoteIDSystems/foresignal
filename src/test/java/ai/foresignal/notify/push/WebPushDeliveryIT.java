package ai.foresignal.notify.push;

import static ai.foresignal.testsupport.notify.NotifyFixture.alert;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.ChannelService;
import ai.foresignal.notify.DeliveryWorker;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotificationRouter;
import ai.foresignal.notify.NotificationService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.PushSettings;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.store.DeliveryStatus;
import ai.foresignal.store.NotificationDeliveryEntity;
import ai.foresignal.store.NotificationDeliveryRepository;
import ai.foresignal.store.PushDispatchEntity;
import ai.foresignal.store.PushDispatchRepository;
import ai.foresignal.store.PushSubscriptionEntity;
import ai.foresignal.store.PushSubscriptionRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.push.MutableResolver;
import ai.foresignal.testsupport.push.PushTestConfig;
import ai.foresignal.testsupport.push.TestSubscriber;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
 * Web Push delivery end to end against a WireMock push service over https (prompt 031): the
 * built-in Push channel fans out, each target is encrypted, signed and sent with the RFC 8030
 * headers, outcomes are kept per target, and the endpoint is re-checked before every send. No real
 * push is ever sent.
 */
@IntegrationTest
@Import(PushTestConfig.class)
class WebPushDeliveryIT {

    @RegisterExtension
    static final WireMockExtension PUSH = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().dynamicHttpsPort().gzipDisabled(true))
            .failOnUnmatchedRequests(true)
            .resetOnEachTest(true)
            .build();

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final UUID RULE = UUID.fromString("00000000-0000-0000-0000-0000000000c1");
    static final UUID OTHER_RULE = UUID.fromString("00000000-0000-0000-0000-0000000000c2");
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired NotificationRouter router;
    @Autowired NotificationService notifications;
    @Autowired DeliveryWorker worker;
    @Autowired PushDispatchWorker pushWorker;
    @Autowired PushBootstrap bootstrap;
    @Autowired ChannelService channels;
    @Autowired NotificationDeliveryRepository deliveries;
    @Autowired PushDispatchRepository dispatches;
    @Autowired PushSubscriptionRepository subscriptions;
    @Autowired VapidKeyService vapid;
    @Autowired VapidTokens vapidTokens;
    @Autowired PushHostLimiter limiter;
    @Autowired PushHttp http;
    @Autowired AppSettingService settings;
    @Autowired MutableResolver dns;
    @Autowired MutableClock clock;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        dns.clear();
        dns.set("push.test", "127.0.0.1");
        allow(List.of("push.test", "127.0.0.0/8"));
        limiter.reset();
        http.reset();
        vapidTokens.clear();
        assertThat(bootstrap.ensureChannel()).isTrue();
        PUSH.stubFor(post(urlPathMatching("/push/.*")).willReturn(aResponse().withStatus(201)));
    }

    void allow(List<String> hosts) {
        settings.put(SettingKeys.SERVER, new ServerSettings("https://fs.example", null, null,
                new PushSettings("mailto:ops@fs.example", hosts, "en")));
        settings.invalidateCache();
    }

    static String endpoint(String host, String id) {
        return "https://" + host + ":" + PUSH.getRuntimeInfo().getHttpsPort() + "/push/" + id;
    }

    /** An active subscription (as if verified) with {@code filters} and {@code format}. */
    PushSubscriptionEntity active(TestSubscriber s, Map<String, Object> filters, String format) {
        PushSubscriptionEntity e = new PushSubscriptionEntity(UUID.randomUUID(), s.endpoint(), s.p256dh(), s.auth(), T);
        e.setStatus(PushSubscriptionService.ACTIVE);
        e.setEndpointHost(URI.create(s.endpoint()).getHost());
        e.setTransportLabel("browser");
        e.setPayloadFormat(format);
        e.setFilters(filters == null ? new LinkedHashMap<>(PushFilters.all().toMap()) : new LinkedHashMap<>(filters));
        return subscriptions.saveAndFlush(e);
    }

    PushSubscriptionEntity active(TestSubscriber s) {
        return active(s, null, "foresignal");
    }

    List<LoggedRequest> pushes(String id) {
        return PUSH.findAll(postRequestedFor(urlPathEqualTo("/push/" + id)));
    }

    NotificationDeliveryEntity pushDelivery() {
        return deliveries.findAll().stream().filter(d -> d.getChannelId().equals(PushChannelType.BUILT_IN_ID))
                .reduce((a, b) -> b).orElseThrow();
    }

    void deliverAll() {
        worker.runOnce();
        pushWorker.runOnce();
    }

    @Test
    @DisplayName("headers (Content-Encoding, TTL, Urgency, Topic, VAPID) and a body that decrypts with the subscription key")
    void headersAndBody() {
        TestSubscriber s = new TestSubscriber(endpoint("push.test", "h1"));
        PushSubscriptionEntity row = active(s);
        router.fired(alert(1, RULE, "Quakes", "critical", T));
        deliverAll();

        List<LoggedRequest> sent = pushes("h1");
        assertThat(sent).hasSize(1);
        LoggedRequest request = sent.getFirst();
        assertThat(request.getHeader("Content-Encoding")).isEqualTo("aes128gcm");
        assertThat(request.getHeader("Content-Type")).startsWith("application/octet-stream");
        assertThat(request.getHeader("TTL")).isEqualTo("86400");
        assertThat(request.getHeader("Urgency")).isEqualTo("high");
        assertThat(request.getHeader("Topic")).isEqualTo(PushTopic.alert(RULE.toString(), "usgs-earthquakes", "us1"))
                .hasSizeLessThanOrEqualTo(32);
        String authorization = request.getHeader("Authorization");
        assertThat(authorization).startsWith("vapid t=").endsWith(", k=" + vapid.publicKey());
        String jwt = authorization.substring("vapid t=".length(), authorization.indexOf(", k="));
        assertThat(Vapid.verify(jwt, vapid.keys().publicKey())).isTrue();
        JsonNode claims = JSON.readTree(new String(EcKeys.unb64(jwt.split("\\.")[1]), java.nio.charset.StandardCharsets.UTF_8));
        assertThat(claims.get("aud").asString()).isEqualTo("https://push.test:" + PUSH.getRuntimeInfo().getHttpsPort());
        assertThat(claims.get("sub").asString()).isEqualTo("mailto:ops@fs.example");
        assertThat(claims.get("exp").asLong()).isEqualTo(T.plus(Duration.ofHours(12)).getEpochSecond());
        assertThat(request.getBody().length).isLessThanOrEqualTo(4096);

        JsonNode payload = s.decrypt(request.getBody());
        assertThat(payload.get("v").asInt()).isEqualTo(1);
        assertThat(payload.get("event").asString()).isEqualTo("alert");
        assertThat(payload.get("alertId").asLong()).isEqualTo(1);
        assertThat(payload.get("ruleId").asString()).isEqualTo(RULE.toString());
        assertThat(payload.get("severity").asString()).isEqualTo("critical");
        assertThat(payload.get("shortTitle").asString()).isEqualTo("Quakes");
        assertThat(payload.get("tag").asString()).isEqualTo("foresignal-alert-1");
        assertThat(payload.get("lang").asString()).isEqualTo("en");
        assertThat(payload.get("links").get("api").asString()).isEqualTo("https://fs.example/v1/alerts/1");
        assertThat(payload.get("links").get("app").asString()).isEqualTo("foresignal://alerts/1");
        assertThat(payload.get("url").asString()).isEqualTo("https://fs.example/v1/alerts/1");
        assertThat(payload.get("tags").get(0).asString()).isEqualTo("quake");
        assertThat(payload.get("silent").asBoolean()).isFalse();

        assertThat(dispatches.findAll()).singleElement().satisfies(d -> {
            assertThat(d.getStatus()).isEqualTo("sent");
            assertThat(d.getLastStatus()).isEqualTo(201);
            assertThat(d.getTargetId()).isEqualTo(row.getId().toString());
        });
        assertThat(pushDelivery().getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(subscriptions.findById(row.getId()).orElseThrow().getLastSuccessAt()).isEqualTo(T);
    }

    @Test
    @DisplayName("Urgency follows severity: warning normal, info low")
    void urgencyBySeverity() {
        TestSubscriber s = new TestSubscriber(endpoint("push.test", "u1"));
        active(s);
        router.fired(alert(2, RULE, "Quakes", "warning", T));
        router.fired(alert(3, RULE, "Quakes", "info", T));
        deliverAll();
        List<String> urgencies = pushes("u1").stream().map(r -> r.getHeader("Urgency")).sorted().toList();
        assertThat(urgencies).containsExactly("low", "normal");
    }

    @Test
    @DisplayName("quiet hours: downgrade sends very-low and silent; defer waits; drop settles without sending; critical bypasses")
    void quietHours() throws Exception {
        TestSubscriber s = new TestSubscriber(endpoint("push.test", "q1"));
        active(s);
        String quiet = "'quietHours':{'start':'00:00','end':'23:59','bypassSeverity':'critical'}";
        channels.replace(PushChannelType.BUILT_IN_ID, JSON.readTree(("{'name':'Push','type':'webpush',"
                + "'events':['alert','briefing','system'],'config':{'quietHoursMode':'downgrade'}," + quiet + "}")
                .replace('\'', '"')));
        router.fired(alert(4, RULE, "Quakes", "warning", T));
        deliverAll();
        assertThat(pushes("q1")).singleElement().satisfies(r -> {
            assertThat(r.getHeader("Urgency")).isEqualTo("very-low");
            assertThat(s.decrypt(r.getBody()).get("silent").asBoolean()).isTrue();
        });

        router.fired(alert(5, RULE, "Quakes", "critical", T));
        deliverAll();
        assertThat(pushes("q1")).hasSize(2);
        assertThat(pushes("q1").stream().map(r -> r.getHeader("Urgency"))).contains("high");

        channels.replace(PushChannelType.BUILT_IN_ID, JSON.readTree(("{'name':'Push','type':'webpush',"
                + "'events':['alert'],'config':{'quietHoursMode':'defer'}," + quiet + "}").replace('\'', '"')));
        router.fired(alert(6, RULE, "Quakes", "warning", T));
        deliverAll();
        assertThat(pushes("q1")).hasSize(2);
        assertThat(pushDelivery().getStatus()).isEqualTo(DeliveryStatus.PENDING);
        assertThat(pushDelivery().getDeferredReason()).isEqualTo("quiet_hours");

        channels.replace(PushChannelType.BUILT_IN_ID, JSON.readTree(("{'name':'Push','type':'webpush',"
                + "'events':['alert'],'config':{'quietHoursMode':'drop'}," + quiet + "}").replace('\'', '"')));
        router.fired(alert(7, RULE, "Quakes", "warning", T));
        deliverAll();
        assertThat(pushes("q1")).hasSize(2);
        assertThat(deliveries.findAll()).anySatisfy(d -> {
            assertThat(d.getAlertId()).isEqualTo(7L);
            assertThat(d.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
            assertThat(d.getLastError()).contains("quiet hours");
        });
    }

    @Test
    @DisplayName("filters: events, ruleIds, minSeverity and any-of tags decide which subscriptions receive")
    void filters() {
        active(new TestSubscriber(endpoint("push.test", "all")));
        active(new TestSubscriber(endpoint("push.test", "briefings")), filters(List.of("briefing"), null, null, null), "foresignal");
        active(new TestSubscriber(endpoint("push.test", "rule2")), filters(null, List.of(OTHER_RULE.toString()), null, null), "foresignal");
        active(new TestSubscriber(endpoint("push.test", "rule1")), filters(null, List.of(RULE.toString()), null, null), "foresignal");
        active(new TestSubscriber(endpoint("push.test", "critical")), filters(null, null, "critical", null), "foresignal");
        active(new TestSubscriber(endpoint("push.test", "fire")), filters(null, null, null, List.of("fire")), "foresignal");
        active(new TestSubscriber(endpoint("push.test", "quake")), filters(null, null, null, List.of("fire", "quake")), "foresignal");
        router.fired(alert(8, RULE, "Quakes", "warning", T));
        deliverAll();
        assertThat(pushes("all")).hasSize(1);
        assertThat(pushes("rule1")).hasSize(1);
        assertThat(pushes("quake")).hasSize(1);
        assertThat(pushes("briefings")).isEmpty();
        assertThat(pushes("rule2")).isEmpty();
        assertThat(pushes("critical")).isEmpty();
        assertThat(pushes("fire")).isEmpty();
        assertThat(dispatches.findAll()).hasSize(3);
    }

    static Map<String, Object> filters(List<String> events, List<String> rules, String min, List<String> tags) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("events", events == null ? List.of("alert", "briefing", "system") : events);
        out.put("ruleIds", rules);
        out.put("minSeverity", min == null ? "info" : min);
        out.put("tags", tags == null ? List.of() : tags);
        return out;
    }

    @Test
    @DisplayName("only active subscriptions receive; with no targets the Push delivery is delivered at once")
    void onlyActive() {
        PushSubscriptionEntity pending = active(new TestSubscriber(endpoint("push.test", "p1")));
        pending.setStatus(PushSubscriptionService.PENDING);
        subscriptions.saveAndFlush(pending);
        PushSubscriptionEntity disabled = active(new TestSubscriber(endpoint("push.test", "p2")));
        disabled.setStatus(PushSubscriptionService.DISABLED);
        subscriptions.saveAndFlush(disabled);
        router.fired(alert(9, RULE, "Quakes", "warning", T));
        deliverAll();
        PUSH.verify(0, postRequestedFor(urlPathMatching("/push/.*")));
        assertThat(dispatches.findAll()).isEmpty();
        assertThat(pushDelivery().getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(pushDelivery().getNote()).contains("no push targets");
    }

    @Test
    @DisplayName("410 marks the dispatch gone and deletes the subscription")
    void goneDeletes() {
        PUSH.stubFor(post(urlPathEqualTo("/push/g1")).willReturn(aResponse().withStatus(410)));
        PushSubscriptionEntity gone = active(new TestSubscriber(endpoint("push.test", "g1")));
        active(new TestSubscriber(endpoint("push.test", "g2")));
        router.fired(alert(10, RULE, "Quakes", "warning", T));
        deliverAll();
        assertThat(subscriptions.findById(gone.getId())).isEmpty();
        assertThat(dispatches.findAll()).extracting(PushDispatchEntity::getStatus).containsExactlyInAnyOrder("gone", "sent");
        assertThat(pushDelivery().getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
    }

    @Test
    @DisplayName("429 honors Retry-After and is retried later")
    void retryAfter() {
        PUSH.stubFor(post(urlPathEqualTo("/push/r1")).willReturn(aResponse().withStatus(429).withHeader("Retry-After", "30")));
        active(new TestSubscriber(endpoint("push.test", "r1")));
        router.fired(alert(11, RULE, "Quakes", "warning", T));
        deliverAll();
        PushDispatchEntity d = dispatches.findAll().getFirst();
        assertThat(d.getStatus()).isEqualTo("pending");
        assertThat(d.getAttempts()).isEqualTo(1);
        assertThat(d.getLastStatus()).isEqualTo(429);
        assertThat(d.getNextAttemptAt()).isEqualTo(T.plusSeconds(30));
        assertThat(pushDelivery().getStatus()).isEqualTo(DeliveryStatus.PENDING);

        pushWorker.runOnce();
        assertThat(pushes("r1")).hasSize(1);
        PUSH.stubFor(post(urlPathEqualTo("/push/r1")).willReturn(aResponse().withStatus(201)));
        clock.advance(Duration.ofSeconds(30));
        pushWorker.runOnce();
        assertThat(pushes("r1")).hasSize(2);
        assertThat(dispatches.findAll().getFirst().getStatus()).isEqualTo("sent");
        assertThat(pushDelivery().getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
    }

    @Test
    @DisplayName("a retry of one failed target does not re-send to the others; all failing fails the delivery")
    void retryIsPerTarget() {
        PUSH.stubFor(post(urlPathEqualTo("/push/b")).willReturn(aResponse().withStatus(500)));
        active(new TestSubscriber(endpoint("push.test", "a")));
        active(new TestSubscriber(endpoint("push.test", "b")));
        router.fired(alert(12, RULE, "Quakes", "warning", T));
        deliverAll();
        assertThat(pushes("a")).hasSize(1);
        assertThat(pushes("b")).hasSize(1);
        assertThat(pushDelivery().getStatus()).isEqualTo(DeliveryStatus.PENDING);

        clock.advance(Duration.ofSeconds(10));
        PUSH.stubFor(post(urlPathEqualTo("/push/b")).willReturn(aResponse().withStatus(201)));
        pushWorker.runOnce();
        assertThat(pushes("a")).hasSize(1);
        assertThat(pushes("b")).hasSize(2);
        assertThat(pushDelivery().getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(pushDelivery().getNote()).isEqualTo("push: 2 sent, 0 gone, 0 failed");
    }

    @Test
    @DisplayName("a target that fails every attempt is failed after the ladder; with no success the delivery fails")
    void allFail() {
        PUSH.stubFor(post(urlPathEqualTo("/push/f1")).willReturn(aResponse().withStatus(500).withBody("down")));
        active(new TestSubscriber(endpoint("push.test", "f1")));
        router.fired(alert(13, RULE, "Quakes", "warning", T));
        deliverAll();
        for (Duration wait : DeliveryWorker.LADDER) {
            clock.advance(wait);
            pushWorker.runOnce();
        }
        assertThat(pushes("f1")).hasSize(DeliveryWorker.MAX_ATTEMPTS);
        PushDispatchEntity d = dispatches.findAll().getFirst();
        assertThat(d.getStatus()).isEqualTo("failed");
        assertThat(d.getLastError()).contains("HTTP 500").doesNotContain("/push/f1");
        assertThat(pushDelivery().getStatus()).isEqualTo(DeliveryStatus.FAILED);
    }

    @Test
    @DisplayName("declarative format: {web_push: 8030, notification {title, body, navigate, lang, tag, silent, data}}")
    void declarative() {
        TestSubscriber s = new TestSubscriber(endpoint("push.test", "safari"));
        PushSubscriptionEntity row = active(s, null, "declarative");
        row.setLang("fr");
        subscriptions.saveAndFlush(row);
        router.fired(alert(14, RULE, "Quakes", "warning", T));
        deliverAll();
        JsonNode payload = s.decrypt(pushes("safari").getFirst().getBody());
        assertThat(payload.get("web_push").asInt()).isEqualTo(8030);
        JsonNode n = payload.get("notification");
        assertThat(n.get("title").asString()).isNotBlank();
        assertThat(n.get("navigate").asString()).isEqualTo("https://fs.example/v1/alerts/14");
        assertThat(n.get("lang").asString()).isEqualTo("fr");
        assertThat(n.get("tag").asString()).isEqualTo("foresignal-alert-14");
        assertThat(n.has("silent")).isTrue();
        assertThat(n.has("app_badge")).isFalse();
        assertThat(n.get("data").get("alertId").asLong()).isEqualTo(14);
        assertThat(n.get("data").has("title")).isFalse();
    }

    @Test
    @DisplayName("DNS rebinding between subscribe and send is blocked; three failed rechecks disable the subscription")
    void dnsRebinding() {
        dns.set("rebind.test", "127.0.0.1");
        PushSubscriptionEntity row = active(new TestSubscriber(endpoint("rebind.test", "rb")));
        dns.set("rebind.test", "10.0.0.5");
        for (int i = 1; i <= 3; i++) {
            router.fired(alert(20 + i, RULE, "Quakes", "warning", T));
            deliverAll();
            assertThat(subscriptions.findById(row.getId()).orElseThrow().getRecheckFailures()).isEqualTo(i);
        }
        PUSH.verify(0, postRequestedFor(urlPathMatching("/push/.*")));
        assertThat(dispatches.findAll()).allSatisfy(d -> {
            assertThat(d.getStatus()).isEqualTo("failed");
            assertThat(d.getLastError()).startsWith("URL_NOT_ALLOWED").contains("10.0.0.5").doesNotContain("/push/rb");
        });
        assertThat(subscriptions.findById(row.getId()).orElseThrow().getStatus()).isEqualTo(PushSubscriptionService.DISABLED);
        router.fired(alert(30, RULE, "Quakes", "warning", T));
        deliverAll();
        assertThat(dispatches.findAll()).hasSize(3);
    }

    @Test
    @DisplayName("system events carry title and code only, TTL 3600 and the system topic; briefings an excerpt and TTL 43200")
    void systemAndBriefing() {
        TestSubscriber s = new TestSubscriber(endpoint("push.test", "sys"));
        active(s);
        notifications.publish(NotificationMessage.builder(NotificationMessage.SYSTEM, "system.rule_storm",
                "Rule Quakes hit its storm cap", "warning").body("A long **markdown** body that is not sent.").build(), null);
        notifications.publish(NotificationMessage.builder(NotificationMessage.BRIEFING, "briefing.run",
                "Morning brief", "info").body("x".repeat(500)).data(Map.of("briefingId", "b-1", "id", "r-1")).build(), null);
        deliverAll();
        List<LoggedRequest> sent = pushes("sys");
        assertThat(sent).hasSize(2);
        for (LoggedRequest r : sent) {
            JsonNode p = s.decrypt(r.getBody());
            if ("system".equals(p.get("event").asString())) {
                assertThat(r.getHeader("TTL")).isEqualTo("3600");
                assertThat(r.getHeader("Topic")).isEqualTo(PushTopic.system("system.rule_storm"));
                assertThat(p.get("body").asString()).isEqualTo("system.rule_storm");
                assertThat(p.get("title").asString()).isEqualTo("Rule Quakes hit its storm cap");
            } else {
                assertThat(r.getHeader("TTL")).isEqualTo("43200");
                assertThat(r.getHeader("Topic")).isEqualTo(PushTopic.briefing("b-1"));
                assertThat(p.get("body").asString()).hasSizeLessThanOrEqualTo(200);
            }
        }
    }

    @Test
    @DisplayName("the Push channel is created once with the fixed id and cannot be created through the API")
    void builtInChannel() {
        assertThat(bootstrap.ensureChannel()).isFalse();
        var view = channels.get(PushChannelType.BUILT_IN_ID);
        assertThat(view.channel().type()).isEqualTo("webpush");
        assertThat(view.channel().events()).containsExactly("alert", "briefing", "system");
        assertThat(view.channel().enabled()).isTrue();
        assertThat(channels.test(PushChannelType.BUILT_IN_ID).success()).isFalse();
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> channels.create(JSON.readTree(
                "{\"name\":\"Second push\",\"type\":\"webpush\"}")))
                .isInstanceOf(ai.foresignal.api.error.ValidationException.class);
    }
}
