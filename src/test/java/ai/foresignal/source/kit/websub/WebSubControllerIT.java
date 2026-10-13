package ai.foresignal.source.kit.websub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.kit.feed.FeedDocument;
import ai.foresignal.source.kit.feed.FeedKitFamily;
import ai.foresignal.source.kit.feed.FeedSourceSpec;
import ai.foresignal.source.kit.feed.KitFeedAdapter;
import ai.foresignal.source.kit.feed.WebSubCoordinator;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * WebSub with {@code foresignal.kits.websub.enabled=true} (prompt 073): the subscribe request through the source's
 * client, intent verification (echo, topic mismatch, wrong token, unsubscribe), signed delivery (sha256 and sha1
 * accepted, a bad or missing signature 403, a non-feed 415), records reaching the store through the push pipeline,
 * the stretched poll while a subscription is active, renewal at 80 % of the lease and the lost-lease fallback.
 *
 * <p>Its own cached context (WebSub enabled). The hub is a public IP literal so the outbound URL policy passes
 * without DNS; the stub context records the hub request instead of sending it. The disabled default is covered by
 * {@link WebSubDisabledIT}.
 */
@IntegrationTest
@Import(MutableClock.TestConfig.class)
@TestPropertySource(properties = WebSubService.ENABLED_PROPERTY + "=true")
class WebSubControllerIT {

    static final String ID = "feed-nhc-atlantic";
    static final String HUB = "https://8.8.8.8/hub";
    static final String TOPIC = "https://www.nhc.noaa.gov/index-at.xml";
    static final Instant T0 = Instant.parse("2026-10-13T12:00:00Z");

    @Autowired MockMvc mvc;
    @Autowired WebSubService websub;
    @Autowired FeedKitFamily feeds;
    @Autowired AppSettingService settings;
    @Autowired SourceService sources;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;

    FeedSourceSpec spec;
    StubHubContext ctx;

    @BeforeEach
    void setUp() {
        clock.setInstant(T0);
        settings.put(SettingKeys.SERVER, new ServerSettings("https://fs.example", null, null, null, null));
        sources.update(ID, new SourceSettingsUpdate(true, null, Map.of(), Budget.of(600, 100_000, 10_000), null,
                false, null, false));
        websub.resetRates();
        FeedSourceSpec s = feeds.spec(ID);
        spec = new FeedSourceSpec(s.id(), s.name(), s.description(), s.url(), s.kind(), s.language(), s.categories(),
                s.maxItems(), s.recordTtl(), s.ttlAnchor(), s.observedAt(), s.interval(), s.minInterval(), s.budget(),
                s.license(), s.termsUrl(), s.attribution(), s.homepage(), s.itemMapper(), Boolean.TRUE, s.retention(),
                s.maxResponseBytes());
        ctx = new StubHubContext(ID, null, clock);
    }

    static FeedDocument document(String hub, String self) {
        return new FeedDocument(FeedDocument.RSS2, "NHC Atlantic", "https://www.nhc.noaa.gov/", self,
                hub == null ? List.of() : List.of(hub), "en-us", List.of(), List.of());
    }

    /** Subscribes and returns the hub request's form. */
    Map<String, String> subscribe() {
        websub.afterPoll(ctx, spec, document(HUB, TOPIC));
        assertThat(ctx.requests).hasSize(1);
        return ctx.lastForm();
    }

    static String token(Map<String, String> form) {
        String callback = form.get("hub.callback");
        return callback.substring(callback.lastIndexOf('/') + 1);
    }

    void verify(Map<String, String> form) throws Exception {
        mvc.perform(get("/v1/websub/" + ID + "/" + token(form)).param("hub.mode", "subscribe")
                        .param("hub.topic", TOPIC).param("hub.challenge", "c-123").param("hub.lease_seconds", "86400"))
                .andExpect(status().isOk())
                .andExpect(content().string("c-123"));
    }

    static String sign(String algorithm, String prefix, String secret, byte[] body) throws Exception {
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithm));
        return prefix + "=" + HexFormat.of().formatHex(mac.doFinal(body));
    }

    @Test
    @DisplayName("subscribe: POST hub with mode, topic, the https callback, a secret and a 86400 s lease")
    void subscribeRequest() {
        Map<String, String> form = subscribe();
        assertThat(ctx.requests.get(0).uri().toString()).isEqualTo(HUB);
        assertThat(form).containsEntry("hub.mode", "subscribe").containsEntry("hub.topic", TOPIC)
                .containsEntry("hub.lease_seconds", "86400");
        assertThat(form.get("hub.callback")).startsWith("https://fs.example/v1/websub/" + ID + "/");
        assertThat(token(form)).matches("[A-Za-z0-9_-]{43}");
        assertThat(form.get("hub.secret")).hasSize(43);
        WebSubSubscriptionEntity row = websub.subscription(ID).orElseThrow();
        assertThat(row.getState()).isEqualTo(WebSubSubscriptionEntity.PENDING);
        assertThat(new String(row.getSecretEnc(), StandardCharsets.ISO_8859_1)).doesNotContain(form.get("hub.secret"));
        assertThat(websub.active(ID, T0)).isFalse();

        // A pending request is not repeated within the hour.
        websub.afterPoll(ctx, spec, document(HUB, TOPIC));
        assertThat(ctx.requests).hasSize(1);
    }

    @Test
    @DisplayName("no subscription without an https publicBaseUrl, without a hub or self link, or without websub")
    void preconditions() {
        settings.put(SettingKeys.SERVER, new ServerSettings("http://fs.local", null, null, null, null));
        websub.afterPoll(ctx, spec, document(HUB, TOPIC));
        settings.put(SettingKeys.SERVER, new ServerSettings("https://fs.example", null, null, null, null));
        websub.afterPoll(ctx, spec, document(null, TOPIC));
        websub.afterPoll(ctx, spec, document(HUB, null));
        websub.afterPoll(ctx, feeds.spec(ID), document(HUB, TOPIC));
        websub.afterPoll(ctx, spec, document("https://127.0.0.1/hub", TOPIC));
        assertThat(ctx.requests).isEmpty();
        assertThat(websub.subscription(ID)).isEmpty();
    }

    @Test
    @DisplayName("verification echoes the challenge only for the right mode, topic and token")
    void verification() throws Exception {
        Map<String, String> form = subscribe();
        String token = token(form);
        mvc.perform(get("/v1/websub/" + ID + "/" + token).param("hub.mode", "subscribe")
                .param("hub.topic", "https://other.example/feed").param("hub.challenge", "x"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/v1/websub/" + ID + "/" + "A".repeat(43)).param("hub.mode", "subscribe")
                .param("hub.topic", TOPIC).param("hub.challenge", "x"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/v1/websub/" + ID + "/" + token).param("hub.mode", "unsubscribe")
                .param("hub.topic", TOPIC).param("hub.challenge", "x"))
                .andExpect(status().isNotFound());
        verify(form);
        WebSubSubscriptionEntity row = websub.subscription(ID).orElseThrow();
        assertThat(row.getState()).isEqualTo(WebSubSubscriptionEntity.ACTIVE);
        assertThat(row.getLastVerifiedAt()).isEqualTo(T0);
        assertThat(row.getLeaseExpiresAt()).isEqualTo(T0.plusSeconds(86_400));
        assertThat(websub.active(ID, T0.plusSeconds(60))).isTrue();
    }

    @Test
    @DisplayName("signed delivery: 202 and the items reach the store; sha1 accepted; bad or missing signature 403")
    void delivery() throws Exception {
        Map<String, String> form = subscribe();
        verify(form);
        String path = "/v1/websub/" + ID + "/" + token(form);
        byte[] body = AdapterFixtures.bytes("kit-feeds", "nhc-index-at.xml");
        String secret = form.get("hub.secret");

        mvc.perform(post(path).contentType("application/rss+xml").content(body)
                        .header("X-Hub-Signature", sign("HmacSHA256", "sha256", secret, body)))
                .andExpect(status().isAccepted());
        assertThat(jdbc.queryForObject("select count(*) from record_head where source_id = ?", Integer.class, ID))
                .isEqualTo(2);
        assertThat(jdbc.queryForObject("select count(*) from record_head where source_id = ? and external_id = ?",
                Integer.class, ID, "https://www.nhc.noaa.gov/gtwo.php?basin=atlc&202610131717")).isEqualTo(1);
        assertThat(websub.subscription(ID).orElseThrow().getLastPushAt()).isEqualTo(T0);

        mvc.perform(post(path).contentType("application/rss+xml").content(body)
                        .header("X-Hub-Signature", sign("HmacSHA1", "sha1", secret, body)))
                .andExpect(status().isAccepted());
        mvc.perform(post(path).contentType("application/rss+xml").content(body)
                        .header("X-Hub-Signature", sign("HmacSHA256", "sha256", "wrong-secret", body)))
                .andExpect(status().isForbidden());
        mvc.perform(post(path).contentType("application/rss+xml").content(body))
                .andExpect(status().isForbidden());
        mvc.perform(post(path).contentType("application/rss+xml").content(body)
                        .header("X-Hub-Signature", "md5=00"))
                .andExpect(status().isForbidden());
        byte[] html = "<html><body>not a feed</body></html>".getBytes(StandardCharsets.UTF_8);
        mvc.perform(post(path).contentType("text/html").content(html)
                        .header("X-Hub-Signature", sign("HmacSHA256", "sha256", secret, html)))
                .andExpect(status().isUnsupportedMediaType());
        mvc.perform(post("/v1/websub/" + ID + "/" + "B".repeat(43)).contentType("application/rss+xml").content(body)
                        .header("X-Hub-Signature", sign("HmacSHA256", "sha256", secret, body)))
                .andExpect(status().isNotFound());
        byte[] huge = new byte[WebSubService.MAX_BODY_BYTES + 1];
        mvc.perform(post(path).contentType("application/rss+xml").content(huge)
                        .header("X-Hub-Signature", sign("HmacSHA256", "sha256", secret, huge)))
                .andExpect(status().isPayloadTooLarge());
    }

    @Test
    @DisplayName("delivery before verification, or to a disabled source, is 404; more than 60 a minute is 429")
    void deliveryLimits() throws Exception {
        Map<String, String> form = subscribe();
        String path = "/v1/websub/" + ID + "/" + token(form);
        byte[] body = AdapterFixtures.bytes("kit-feeds", "nhc-index-at.xml");
        String signature = sign("HmacSHA256", "sha256", form.get("hub.secret"), body);
        mvc.perform(post(path).contentType("application/rss+xml").content(body).header("X-Hub-Signature", signature))
                .andExpect(status().isNotFound());
        verify(form);
        for (int i = 0; i < WebSubService.PUSHES_PER_MINUTE; i++) {
            mvc.perform(post(path).contentType("application/rss+xml").content(body)
                    .header("X-Hub-Signature", signature)).andExpect(status().isAccepted());
        }
        mvc.perform(post(path).contentType("application/rss+xml").content(body).header("X-Hub-Signature", signature))
                .andExpect(status().isTooManyRequests());
        websub.resetRates();
        sources.update(ID, new SourceSettingsUpdate(false, null, Map.of(), Budget.of(600, 100_000, 10_000),
                null, false, null, false));
        mvc.perform(post(path).contentType("application/rss+xml").content(body).header("X-Hub-Signature", signature))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("while a subscription is active the poll stretches to 6 h: runs in between make no request")
    void stretchedPoll() throws Exception {
        verify(subscribe());
        KitFeedAdapter adapter = new KitFeedAdapter(spec, () -> websub, null);
        ctx.state.put(KitFeedAdapter.LAST_POLL_KEY, T0.minus(Duration.ofHours(1)).toString());
        FetchResult result = adapter.fetch(ctx);
        assertThat(result.notModified()).isTrue();
        assertThat(ctx.requests).hasSize(1);
        assertThat(KitFeedAdapter.WEBSUB_SAFETY_NET).isEqualTo(Duration.ofHours(6));
    }

    @Test
    @DisplayName("renewal at 80 % of the lease keeps the token and secret; a failed renewal falls back to polling")
    void renewal() throws Exception {
        Map<String, String> form = subscribe();
        verify(form);
        clock.setInstant(T0.plus(Duration.ofHours(19)));
        websub.maintain(ctx, spec);
        assertThat(ctx.requests).as("not yet at 80 %").hasSize(1);
        WebSubSubscriptionEntity row = websub.subscription(ID).orElseThrow();
        assertThat(WebSubService.renewalDue(row, T0.plus(Duration.ofMinutes(1152)))).isTrue();
        assertThat(WebSubService.renewalDue(row, T0.plus(Duration.ofMinutes(1151)))).isFalse();

        clock.setInstant(T0.plus(Duration.ofMinutes(1160)));
        websub.maintain(ctx, spec);
        assertThat(ctx.requests).hasSize(2);
        assertThat(ctx.lastForm()).containsEntry("hub.mode", "subscribe")
                .containsEntry("hub.callback", form.get("hub.callback"))
                .containsEntry("hub.secret", form.get("hub.secret"));
        assertThat(websub.subscription(ID).orElseThrow().getState()).isEqualTo(WebSubSubscriptionEntity.ACTIVE);
        websub.maintain(ctx, spec);
        assertThat(ctx.requests).as("not repeated while the renewal is pending").hasSize(2);

        clock.setInstant(T0.plus(Duration.ofMinutes(1230)));
        ctx.status = 500;
        websub.maintain(ctx, spec);
        assertThat(ctx.requests).hasSize(3);
        assertThat(websub.subscription(ID).orElseThrow().getState()).isEqualTo(WebSubSubscriptionEntity.LOST);
        assertThat(ctx.state.get(AdapterStateStore.DEGRADED_REASON_KEY)).isEqualTo(WebSubCoordinator.LEASE_LOST);
        assertThat(websub.active(ID, clock.instant())).isFalse();
    }

    @Test
    @DisplayName("an expired lease returns the source to polling with the health reason; a denial marks it lost")
    void expiredAndDenied() throws Exception {
        Map<String, String> form = subscribe();
        verify(form);
        clock.setInstant(T0.plus(Duration.ofHours(25)));
        websub.maintain(ctx, spec);
        assertThat(websub.subscription(ID).orElseThrow().getState()).isEqualTo(WebSubSubscriptionEntity.LOST);
        assertThat(ctx.state.get(AdapterStateStore.DEGRADED_REASON_KEY)).isEqualTo(WebSubCoordinator.LEASE_LOST);

        jdbc.update("delete from websub_subscription");
        ctx.requests.clear();
        Map<String, String> again = subscribe();
        mvc.perform(get("/v1/websub/" + ID + "/" + token(again)).param("hub.mode", "denied")
                .param("hub.topic", TOPIC).param("hub.reason", "not allowed"))
                .andExpect(status().isOk());
        assertThat(websub.subscription(ID).orElseThrow().getState()).isEqualTo(WebSubSubscriptionEntity.LOST);
    }

    @Test
    @DisplayName("signature checks: hex case, unknown methods and malformed values")
    void signatures() throws Exception {
        byte[] body = "x".getBytes(StandardCharsets.UTF_8);
        String good = sign("HmacSHA256", "sha256", "s", body);
        assertThat(WebSubService.signatureValid(good, body, "s")).isTrue();
        assertThat(WebSubService.signatureValid(good.toUpperCase(java.util.Locale.ROOT).replace("SHA256", "sha256"),
                body, "s")).isTrue();
        assertThat(WebSubService.signatureValid("sha256=zz", body, "s")).isFalse();
        assertThat(WebSubService.signatureValid("sha512=00", body, "s")).isFalse();
        assertThat(WebSubService.signatureValid("nonsense", body, "s")).isFalse();
        assertThat(WebSubService.signatureValid(null, body, "s")).isFalse();
    }
}
