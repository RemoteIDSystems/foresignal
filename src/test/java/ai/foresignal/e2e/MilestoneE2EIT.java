package ai.foresignal.e2e;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.notContaining;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.collect.BackoffRegistry;
import ai.foresignal.collect.CollectorScheduler;
import ai.foresignal.collect.FetchExecutor;
import ai.foresignal.notify.ChannelRateLimiter;
import ai.foresignal.notify.DeliveryWorker;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.adapters.usgs.UsgsEarthquakesAdapter;
import ai.foresignal.store.NotificationRepository;
import ai.foresignal.stream.sse.SseClient;
import ai.foresignal.stream.sse.SseHub;
import ai.foresignal.stream.sse.StreamLimits;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.stream.SseTestClient;
import ai.foresignal.testsupport.tiles.MvtDecoder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The core milestone (prompt 021), end to end over real HTTP on a random port: enable USGS through
 * the API, ingest the recorded quakes, re-poll identical content as a touch only, persist a revision
 * as version 2, fire a {@code mag >= 6} rule, deliver the alert on SSE and to three channels, read
 * version 1 through {@code asOf}, then resolve the alert and see the Discord message edited in place.
 *
 * <h2>Harness</h2>
 *
 * <ul>
 *   <li><b>Upstream:</b> {@link WireMockSupport}'s server serves the USGS fixtures through the
 *       adapter's base-URL override ({@value UsgsEarthquakesAdapter#BASE_URL_PROPERTY}, prompt 011).
 *       {@code all_day.geojson} is the real capture; {@code e2e-mag-6.4.geojson} and
 *       {@code e2e-mag-5.8.geojson} revise {@code us6000u1mj} (see the fixture README).
 *   <li><b>Receivers:</b> one WireMock server per channel (webhook, Slack, Discord), so each
 *       channel's traffic is counted alone.
 *   <li><b>Admin token:</b> the test-only hook {@link AdminTokenService#rotate()}, as every other
 *       controller IT does. It returns the new plaintext token, which is used as a bearer token.
 *   <li><b>Clock:</b> a {@link MutableClock} placed just after the capture, so the fixture's times are
 *       in the past. It is moved past {@code touchMin} (1 h) before the identical re-poll, because a
 *       re-poll inside {@code touchMin} is a throttled touch, which publishes nothing.
 *   <li><b>Scheduling:</b> the collector scheduler runs for real (this context sets
 *       {@code foresignal.collector.scheduled=true}), so the first fetch is the scheduler's reaction to
 *       the enable. The interval is set to 1 h and the budget raised first, so later fetches are the
 *       test's explicit {@code POST /fetch} calls and are never refused.
 *   <li><b>Delivery:</b> the notification worker is driven with {@link DeliveryWorker#runOnce()}.
 *       Scheduling it in this cached context would let it claim deliveries that other notification
 *       ITs drive by hand when contexts overlap in one JVM.
 *   <li><b>SSE:</b> each stream is drained on a virtual thread into a bounded {@link SseRecorder};
 *       heartbeats are set to 1 s so a "no event" assertion waits a fixed time after a heartbeat.
 * </ul>
 *
 * <p>Nothing is seeded with SQL: every record comes from the adapter through the collector and the
 * writer, and every rule and channel from the API.
 */
@WebIntegrationTest
@Import(MutableClock.TestConfig.class)
@TestPropertySource(properties = "foresignal.collector.scheduled=true")
class MilestoneE2EIT extends WireMockSupport {

    private static final String ID = UsgsEarthquakesAdapter.ID;
    private static final String FEED_PATH = "/earthquakes/feed/v1.0/summary/all_day.geojson";
    private static final String LAST_MODIFIED = "Sun, 11 Oct 2026 22:32:45 GMT";

    /** Just after the capture, so every fixture's {@code updated} is in the past. */
    private static final Instant NOW = Instant.parse("2026-10-11T22:33:00Z");

    /** Features in {@code all_day.geojson}. */
    private static final int FEATURES = 25;

    /** The feature the E2E fixtures revise: M 5.3, then 6.4, then 5.8. */
    private static final String REVISED = "us6000u1mj";

    /** The only feature of {@code all_day.geojson} at or above M 6 (M 6.2): it fires on its FIRST version. */
    private static final String ALREADY_BIG = "us6000u1kv";

    private static final String RENDERED = "M6.4 151 km W of Gorontalo, Indonesia";

    private static final String WEBHOOK_PATH = "/hook";
    private static final String SLACK_PATH = "/services/T000/B000/XXXX";
    private static final String DISCORD_PATH = "/api/webhooks/1/TOKEN";

    private static final Duration SSE_WAIT = Duration.ofSeconds(5);
    private static final Duration WAIT = Duration.ofSeconds(15);
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    @RegisterExtension
    static final WireMockExtension WEBHOOK = receiver();

    @RegisterExtension
    static final WireMockExtension SLACK = receiver();

    @RegisterExtension
    static final WireMockExtension DISCORD = receiver();

    @DynamicPropertySource
    static void upstream(DynamicPropertyRegistry registry) {
        registry.add(UsgsEarthquakesAdapter.BASE_URL_PROPERTY, WIREMOCK::baseUrl);
    }

    private static WireMockExtension receiver() {
        return WireMockExtension.newInstance()
                .options(wireMockConfig().dynamicPort().gzipDisabled(true))
                .failOnUnmatchedRequests(true)
                .resetOnEachTest(true)
                .build();
    }

    @LocalServerPort int port;
    @Autowired AdminTokenService adminTokens;
    @Autowired CollectorScheduler scheduler;
    @Autowired FetchExecutor executor;
    @Autowired BackoffRegistry backoff;
    @Autowired DeliveryWorker deliveryWorker;
    @Autowired ChannelRateLimiter limiter;
    @Autowired NotificationRepository notifications;
    @Autowired AppSettingService settings;
    @Autowired SseHub hub;
    @Autowired MutableClock clock;

    private final List<SseRecorder> streams = new ArrayList<>();
    private String token;

    @BeforeEach
    void setUp() {
        clock.setInstant(NOW);
        settings.invalidateCache();
        limiter.reset();
        backoff.onSuccess(ID);
        if (!scheduler.isRunning()) {
            scheduler.start();
        }
        // The cleaner truncated source_settings; let the scheduler notice before the test starts.
        scheduler.reconcileNow();
        await().atMost(Duration.ofSeconds(10))
                .until(() -> scheduler.state().scheduledSources().isEmpty() && executor.activeFetches() == 0);
        // Heartbeats every second, so "no event" can be asserted a fixed time after one.
        settings.put(SettingKeys.STREAM_LIMITS, new StreamLimits(0, 0, 0, 1, 0, 0, 0, 0));

        serve("all_day.geojson");
        WEBHOOK.stubFor(post(urlEqualTo(WEBHOOK_PATH)).willReturn(aResponse().withStatus(200)));
        SLACK.stubFor(post(urlEqualTo(SLACK_PATH)).willReturn(aResponse().withStatus(200).withBody("ok")));
        // The revised quake's message gets id 111, which the resolve must edit; any other message 110.
        DISCORD.stubFor(post(urlPathEqualTo(DISCORD_PATH)).atPriority(1)
                .withQueryParam("wait", equalTo("true"))
                .withRequestBody(containing(REVISED))
                .willReturn(okJson("{\"id\":\"111\"}")));
        DISCORD.stubFor(post(urlPathEqualTo(DISCORD_PATH)).atPriority(5)
                .withQueryParam("wait", equalTo("true"))
                .willReturn(okJson("{\"id\":\"110\"}")));
        DISCORD.stubFor(patch(urlPathMatching(DISCORD_PATH + "/messages/11[01]"))
                .willReturn(okJson("{\"id\":\"111\"}")));
    }

    @AfterEach
    void tearDown() {
        streams.forEach(SseRecorder::close);
        hub.clients().forEach(SseClient::close);
        backoff.onSuccess(ID);
    }

    @Test
    @DisplayName("USGS end to end: enable, ingest, touch-only re-poll, revision, alert on SSE and three channels, asOf, resolve with an in-place edit")
    void milestone() throws Exception {
        // ---- 1. The admin token; a write without it is 401.
        token = adminTokens.rotate();
        assertThat(send("PUT", "/v1/sources/" + ID, "{\"enabled\":true}", null).status()).isEqualTo(401);
        assertThat(send("POST", "/v1/triggers", "{\"name\":\"x\"}", null).status()).isEqualTo(401);

        // ---- 2. The source is listed, disabled, STANDARD, with a licence.
        JsonNode source = find(get("/v1/sources").body(), "id", ID);
        assertThat(source).as("usgs-earthquakes in /v1/sources").isNotNull();
        assertThat(source.get("enabled").asBoolean()).isFalse();
        assertThat(source.get("status").asString()).isEqualTo("disabled");
        assertThat(source.get("historyTier").asString()).isEqualTo("STANDARD");
        assertThat(source.get("licence").asString()).isEqualTo("US-PD");

        // Interval and budget first, still disabled, so the enable below is exactly {"enabled":true}.
        Response settingsPut = apiPut("/v1/sources/" + ID,
                "{\"interval\":\"1h\",\"budget\":{\"perMinute\":600,\"perDay\":100000}}");
        assertThat(settingsPut.status()).isEqualTo(200);
        assertThat(settingsPut.body().get("enabled").asBoolean()).isFalse();

        // ---- 3. The rule.
        Response rule = apiPost("/v1/triggers", """
                {"name":"Big quake","sources":["usgs-earthquakes"],"mode":"enter","autoResolve":true,
                 "conditions":[{"path":"mag","op":">=","value":6}],
                 "messageTemplate":"M{{payload.mag}} {{payload.place}}"}""");
        assertThat(rule.status()).isEqualTo(201);
        String ruleId = rule.body().get("id").asString();
        assertThat(rule.body().get("mode").asString()).isEqualTo("enter");

        // ---- 4. Three channels, each at its own receiver.
        Response webhook = apiPost("/v1/channels", "{\"name\":\"E2E webhook\",\"type\":\"webhook\",\"events\":[\"alert\"],"
                + "\"config\":{\"url\":\"" + WEBHOOK.url(WEBHOOK_PATH) + "\"},\"secrets\":{\"hmacSecret\":\"s3cret\"}}");
        Response slack = apiPost("/v1/channels", "{\"name\":\"E2E Slack\",\"type\":\"slack\","
                + "\"secrets\":{\"webhookUrl\":\"" + SLACK.url(SLACK_PATH) + "\"}}");
        Response discord = apiPost("/v1/channels", "{\"name\":\"E2E Discord\",\"type\":\"discord\","
                + "\"secrets\":{\"webhookUrl\":\"" + DISCORD.url(DISCORD_PATH) + "\"}}");
        assertThat(List.of(webhook.status(), slack.status(), discord.status())).containsOnly(201);

        // ---- 5. The change stream (with touches) and the alert stream.
        SseRecorder changes = open("/v1/changes/stream?source=" + ID + "&touches=true&payload=true");
        SseRecorder alerts = open("/v1/alerts/stream");

        // ---- 6. Enable: the scheduler fetches once and every quake is a FIRST change.
        Response enabled = apiPut("/v1/sources/" + ID, "{\"enabled\":true}");
        assertThat(enabled.status()).isEqualTo(200);
        assertThat(enabled.body().get("enabled").asBoolean()).isTrue();

        List<JsonNode> firstChanges = changes.awaitData("change", FEATURES, 0, SSE_WAIT);
        assertThat(firstChanges).hasSize(FEATURES)
                .allSatisfy(c -> assertThat(c.get("persistReason").asString()).isEqualTo("FIRST"));
        assertThat(firstChanges.stream().map(c -> c.get("externalId").asString()).distinct()).hasSize(FEATURES);
        await().atMost(WAIT).until(() -> executor.activeFetches() == 0);

        JsonNode collection = get("/v1/records?source=" + ID + "&format=geojson&limit=100").body();
        assertThat(collection.get("type").asString()).isEqualTo("FeatureCollection");
        assertThat(collection.get("features").size()).isEqualTo(FEATURES);
        assertThat(collection.get("watermarkSeq").isNumber()).isTrue();
        assertThat(collection.get("watermarkSeq").asLong()).isGreaterThanOrEqualTo(FEATURES);

        assertThat(get("/v1/sources/" + ID + "/health").body().get("status").asString()).isEqualTo("healthy");
        JsonNode usage = get("/v1/sources/" + ID + "/usage?resolution=minute&from=" + NOW.minusSeconds(3600)
                + "&to=" + NOW.plusSeconds(3600)).body();
        assertThat(usage.get("totals").get("requests").asLong()).isGreaterThanOrEqualTo(1);

        JsonNode fetches = get("/v1/sources/" + ID + "/fetches").body().get("items");
        assertThat(fetches.size()).isEqualTo(1);
        assertThat(fetches.get(0).get("status").asString()).isEqualTo("OK");
        assertThat(fetches.get(0).get("trigger").asString()).isEqualTo("SCHEDULED");
        assertThat(fetches.get(0).get("versionsWritten").asInt()).isEqualTo(FEATURES);

        // The real M 6.2 in the capture enters the rule on its FIRST version.
        JsonNode bigFired = alerts.awaitFirst("alert", d -> ALREADY_BIG.equals(d.get("alert").get("externalId").asString()), 0, WAIT);
        assertThat(bigFired.get("type").asString()).isEqualTo("fired");
        deliverUntil(() -> WEBHOOK.findAll(postRequestedFor(urlEqualTo(WEBHOOK_PATH))).size() >= 1
                && SLACK.findAll(postRequestedFor(urlEqualTo(SLACK_PATH))).size() >= 1
                && DISCORD.findAll(postRequestedFor(urlPathEqualTo(DISCORD_PATH))).size() >= 1);

        // ---- 7. An identical re-poll past touchMin: touches, no change, no version.
        clock.advance(Duration.ofMinutes(61));
        int changeMark = changes.size();
        Response repoll = apiPost("/v1/sources/" + ID + "/fetch", "");
        assertThat(repoll.status()).isEqualTo(200);
        assertThat(repoll.body().get("persisted").asInt()).isZero();
        changes.awaitData("touch", 1, changeMark, SSE_WAIT);
        int afterTouch = changes.size();
        changes.awaitData("heartbeat", 1, afterTouch, SSE_WAIT);
        Thread.sleep(1_500);
        assertThat(changes.dataSince("change", changeMark)).as("change events after an identical re-poll").isEmpty();

        JsonNode newest = get("/v1/sources/" + ID + "/fetches?limit=1").body().get("items").get(0);
        assertThat(newest.get("trigger").asString()).isEqualTo("MANUAL");
        assertThat(newest.get("versionsWritten").asInt()).isZero();
        assertThat(newest.get("touches").asInt()).isGreaterThanOrEqualTo(1);
        JsonNode current = get("/v1/records?source=" + ID + "&limit=100").body().get("items");
        assertThat(current.size()).isEqualTo(FEATURES);
        for (JsonNode item : current) {
            assertThat(item.get("version").asInt()).as("version of %s", item.get("externalId").asString()).isEqualTo(1);
        }

        // ---- 8. T1, then a revision to M 6.4.
        clock.advance(Duration.ofMinutes(1));
        Instant t1 = clock.instant();
        clock.advance(Duration.ofMinutes(1));
        int alertMark = alerts.size();
        changeMark = changes.size();
        serve("e2e-mag-6.4.geojson");
        Response revision = apiPost("/v1/sources/" + ID + "/fetch", "");
        assertThat(revision.status()).isEqualTo(200);
        assertThat(revision.body().get("persisted").asInt()).isEqualTo(1);

        JsonNode v2 = changes.awaitFirst("change", c -> REVISED.equals(c.get("externalId").asString()), changeMark, SSE_WAIT);
        assertThat(v2.get("version").asInt()).isEqualTo(2);
        assertThat(v2.get("persistReason").asString()).isEqualTo("SIGNIFICANT");
        assertThat(v2.get("payload").get("mag").asDouble()).isEqualTo(6.4);
        assertThat(changes.dataSince("change", changeMark)).hasSize(1);

        JsonNode versions = get("/v1/records/" + ID + "/" + REVISED + "/versions").body().get("items");
        assertThat(versions.size()).isEqualTo(2);

        JsonNode fired = alerts.awaitFirst("alert", d -> REVISED.equals(d.get("alert").get("externalId").asString()), alertMark, WAIT);
        assertThat(fired.get("type").asString()).isEqualTo("fired");
        assertThat(fired.get("alert").get("message").asString()).isEqualTo(RENDERED);
        assertThat(fired.get("alert").get("ruleId").asString()).isEqualTo(ruleId);
        long alertId = fired.get("alert").get("id").asLong();

        deliverUntil(() -> !WEBHOOK.findAll(postRequestedFor(urlEqualTo(WEBHOOK_PATH))
                        .withRequestBody(containing(REVISED))).isEmpty()
                && !SLACK.findAll(postRequestedFor(urlEqualTo(SLACK_PATH)).withRequestBody(containing(REVISED))).isEmpty()
                && !DISCORD.findAll(postRequestedFor(urlPathEqualTo(DISCORD_PATH)).withRequestBody(containing(REVISED))).isEmpty());

        LoggedRequest hook = WEBHOOK.findAll(postRequestedFor(urlEqualTo(WEBHOOK_PATH)).withRequestBody(containing(REVISED))).getFirst();
        String timestamp = hook.getHeader("X-Foresignal-Timestamp");
        assertThat(timestamp).isNotBlank();
        assertThat(hook.getHeader("X-Foresignal-Signature")).isEqualTo("sha256=" + hmac("s3cret", timestamp, hook.getBody()));
        assertThat(hook.getHeader("X-Foresignal-Origin")).isNotBlank();
        assertThat(JSON.readTree(hook.getBodyAsString()).get("subtype").asString()).isEqualTo("alert.fired");

        LoggedRequest slackPost = SLACK.findAll(postRequestedFor(urlEqualTo(SLACK_PATH)).withRequestBody(containing(REVISED))).getFirst();
        JsonNode blocks = JSON.readTree(slackPost.getBodyAsString()).get("blocks");
        assertThat(blocks.isArray()).isTrue();
        assertThat(blocks.toString()).contains("151 km W of Gorontalo");

        LoggedRequest discordPost = DISCORD.findAll(postRequestedFor(urlPathEqualTo(DISCORD_PATH))
                .withRequestBody(containing(REVISED))).getFirst();
        assertThat(discordPost.queryParameter("wait").firstValue()).isEqualTo("true");
        assertThat(notifications.count()).isGreaterThanOrEqualTo(2);

        // ---- 9. asOf T1 still shows version 1 with the old magnitude.
        JsonNode asOf = get("/v1/records?source=" + ID + "&externalId=" + REVISED + "&asOf=" + t1).body();
        assertThat(asOf.get("items").size()).isEqualTo(1);
        assertThat(asOf.get("items").get(0).get("version").asInt()).isEqualTo(1);
        assertThat(asOf.get("items").get(0).get("payload").get("mag").asDouble()).isEqualTo(5.3);
        assertThat(Instant.parse(asOf.get("asOf").asString())).isEqualTo(t1);
        assertThat(asOf.get("horizon").isNull()).isFalse();

        // ---- 10. The alert is listed and acknowledged.
        JsonNode listed = find(get("/v1/alerts?ruleId=" + ruleId).body().get("items"), "id", Long.toString(alertId));
        assertThat(listed).as("alert %s in /v1/alerts", alertId).isNotNull();
        Response ack = apiPost("/v1/alerts/" + alertId + "/ack", "");
        assertThat(ack.status()).isEqualTo(200);
        assertThat(ack.body().get("state").asString()).isEqualTo("acknowledged");

        // ---- 11. A revision to M 5.8 leaves the rule: version 3, resolved, Discord edited in place.
        clock.advance(Duration.ofMinutes(1));
        int resolvedMark = alerts.size();
        changeMark = changes.size();
        serve("e2e-mag-5.8.geojson");
        assertThat(apiPost("/v1/sources/" + ID + "/fetch", "").status()).isEqualTo(200);

        JsonNode v3 = changes.awaitFirst("change", c -> REVISED.equals(c.get("externalId").asString()), changeMark, SSE_WAIT);
        assertThat(v3.get("version").asInt()).isEqualTo(3);
        JsonNode resolved = alerts.awaitFirst("alert.resolved", d -> d.get("alert").get("id").asLong() == alertId, resolvedMark, WAIT);
        assertThat(resolved.get("alert").get("state").asString()).isEqualTo("resolved");

        deliverUntil(() -> !DISCORD.findAll(patchRequestedFor(urlEqualTo(DISCORD_PATH + "/messages/111"))
                        .withRequestBody(containing("Resolved"))).isEmpty()
                && !SLACK.findAll(postRequestedFor(urlEqualTo(SLACK_PATH)).withRequestBody(containing("Resolved"))).isEmpty());
        assertThat(DISCORD.findAll(patchRequestedFor(urlEqualTo(DISCORD_PATH + "/messages/110")))).isEmpty();
        assertThat(DISCORD.findAll(postRequestedFor(urlPathEqualTo(DISCORD_PATH))
                .withRequestBody(containing(REVISED)).withRequestBody(notContaining("Resolved")))).hasSize(1);

        // ---- 12. A tile with features.
        Response tile = sendBytes("/v1/tiles/earthquake/0/0/0.mvt", token);
        assertThat(tile.status()).isEqualTo(200);
        assertThat(MvtDecoder.features(tile.bytes())).isNotEmpty();

        // ---- 13. /v1/system: no history anonymously; two tiers and a warnings array with the token.
        Response anonymous = send("GET", "/v1/system", null, null);
        assertThat(anonymous.status()).isEqualTo(200);
        assertThat(anonymous.body().has("history")).isFalse();
        JsonNode system = get("/v1/system").body();
        assertThat(system.get("history").get("tiers").size()).isEqualTo(2);
        assertThat(system.get("warnings").isArray()).isTrue();

        // ---- 14. Disable: no further upstream requests.
        assertThat(apiPut("/v1/sources/" + ID, "{\"enabled\":false}").status()).isEqualTo(200);
        await().atMost(Duration.ofSeconds(10))
                .until(() -> scheduler.state().scheduledSources().isEmpty() && executor.activeFetches() == 0);
        int upstreamRequests = WIREMOCK.getAllServeEvents().size();
        await().during(Duration.ofSeconds(5)).atMost(Duration.ofSeconds(7))
                .until(() -> WIREMOCK.getAllServeEvents().size() == upstreamRequests);

        // ---- 15. A retention run deletes nothing current.
        Response retention = apiPost("/v1/settings/retention/run", "");
        assertThat(retention.status()).isEqualTo(200);
        assertThat(retention.body().get("versionsDeleted").isNumber()).isTrue();
        assertThat(retention.body().get("headsPurged").asLong()).isZero();
        assertThat(retention.body().get("versionsDeleted").asLong()).isZero();
        assertThat(get("/v1/records?source=" + ID + "&limit=100").body().get("items").size()).isEqualTo(FEATURES);
        assertThat(get("/v1/records/" + ID + "/" + REVISED + "/versions").body().get("items").size()).isEqualTo(3);
    }

    // ------------------------------------------------------------------ harness

    /** Serves {@code fixture} for every feed request, with the capture's {@code Last-Modified}. */
    private static void serve(String fixture) {
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(FEED_PATH))
                .atPriority(5)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withHeader("Last-Modified", LAST_MODIFIED)
                        .withBody(AdapterFixtures.bytes(ID, fixture))));
    }

    /**
     * Runs the delivery worker until {@code done} holds. The clock moves a second per round so the
     * channels' rate buckets refill and retry delays fall due.
     */
    private void deliverUntil(Callable<Boolean> done) {
        await().atMost(WAIT).pollInterval(Duration.ofMillis(200)).until(() -> {
            clock.advance(Duration.ofSeconds(1));
            deliveryWorker.runOnce();
            return done.call();
        });
    }

    private SseRecorder open(String path) {
        SseRecorder recorder = new SseRecorder(
                SseTestClient.connect("http://localhost:" + port, path, Map.of("Authorization", "Bearer " + token)));
        streams.add(recorder);
        assertThat(recorder.status()).as("status of %s", path).isEqualTo(200);
        return recorder;
    }

    /** The element of {@code array} whose {@code field} renders as {@code value}, or null. */
    private static JsonNode find(JsonNode array, String field, String value) {
        for (JsonNode node : array) {
            if (node.has(field) && value.equals(node.get(field).asString())) {
                return node;
            }
        }
        return null;
    }

    /** {@code hex(HMAC-SHA256(secret, timestamp + "." + body))}, computed independently of production code. */
    private static String hmac(String secret, String timestamp, byte[] body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        mac.update((timestamp + ".").getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(mac.doFinal(body));
    }

    // ------------------------------------------------------------------ HTTP

    /** One response: status, body bytes, and the body as JSON when it parses. */
    private record Response(int status, byte[] bytes) {

        JsonNode body() {
            return bytes.length == 0 ? JSON.nullNode() : JSON.readTree(bytes);
        }
    }

    private Response get(String path) {
        Response response = send("GET", path, null, token);
        assertThat(response.status()).as("GET %s", path).isEqualTo(200);
        return response;
    }

    private Response apiPost(String path, String body) {
        return send("POST", path, body, token);
    }

    private Response apiPut(String path, String body) {
        return send("PUT", path, body, token);
    }

    private Response sendBytes(String path, String bearer) {
        return send("GET", path, null, bearer);
    }

    private Response send(String method, String path, String body, String bearer) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(30));
        if (bearer != null) {
            request.header("Authorization", "Bearer " + bearer);
        }
        if (body == null) {
            request.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            request.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        }
        try {
            HttpResponse<byte[]> response = HTTP.send(request.build(), HttpResponse.BodyHandlers.ofByteArray());
            return new Response(response.statusCode(), response.body());
        } catch (IOException e) {
            throw new IllegalStateException(method + " " + path + " failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(method + " " + path + " interrupted", e);
        }
    }

    // ------------------------------------------------------------------ SSE

    /**
     * Drains one {@link SseTestClient} on a virtual thread into a bounded, ordered list, so the test
     * can wait for events by position without losing the ones it skipped. Overflowing the bound fails
     * the next wait instead of growing without limit.
     */
    static final class SseRecorder implements AutoCloseable {

        /** Events kept per stream; the test produces a few dozen. */
        static final int CAPACITY = 5_000;

        private final SseTestClient client;
        private final List<SseTestClient.Event> events = new ArrayList<>();
        private final AtomicBoolean overflow = new AtomicBoolean();
        private volatile boolean closed;

        SseRecorder(SseTestClient client) {
            this.client = client;
            if (client.status() == 200) {
                Thread.ofVirtual().name("e2e-sse-recorder").start(this::drain);
            }
        }

        private void drain() {
            while (!closed && !client.ended()) {
                SseTestClient.Event event = client.next(Duration.ofMillis(200));
                if (event == null) {
                    continue;
                }
                synchronized (events) {
                    if (events.size() >= CAPACITY) {
                        overflow.set(true);
                    } else {
                        events.add(event);
                    }
                }
            }
        }

        int status() {
            return client.status();
        }

        /** Events recorded so far. */
        int size() {
            synchronized (events) {
                return events.size();
            }
        }

        /** The parsed data of every event named {@code name} at index {@code from} or later. */
        List<JsonNode> dataSince(String name, int from) {
            assertThat(overflow.get()).as("SSE recorder overflow").isFalse();
            List<SseTestClient.Event> copy;
            synchronized (events) {
                copy = new ArrayList<>(events.subList(Math.min(from, events.size()), events.size()));
            }
            List<JsonNode> out = new ArrayList<>();
            for (SseTestClient.Event event : copy) {
                if (event.event().equals(name)) {
                    out.add(event.data().isEmpty() ? JSON.nullNode() : JSON.readTree(event.data()));
                }
            }
            return out;
        }

        /** Waits until at least {@code count} events named {@code name} arrived at or after {@code from}. */
        List<JsonNode> awaitData(String name, int count, int from, Duration timeout) {
            await().atMost(timeout).pollInterval(Duration.ofMillis(50))
                    .until(() -> dataSince(name, from).size() >= count);
            return dataSince(name, from);
        }

        /** Waits for the first event named {@code name} at or after {@code from} whose data matches. */
        JsonNode awaitFirst(String name, Predicate<JsonNode> match, int from, Duration timeout) {
            await().atMost(timeout).pollInterval(Duration.ofMillis(50))
                    .until(() -> dataSince(name, from).stream().anyMatch(match));
            return dataSince(name, from).stream().filter(match).findFirst().orElseThrow();
        }

        @Override
        public void close() {
            closed = true;
            client.close();
        }
    }
}
