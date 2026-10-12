package ai.foresignal.notify.inbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotificationRouter;
import ai.foresignal.notify.NotificationService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.stream.sse.SseClient;
import ai.foresignal.stream.sse.SseFrame;
import ai.foresignal.stream.sse.SseHub;
import ai.foresignal.stream.sse.SseSender;
import ai.foresignal.stream.sse.StreamFilter;
import ai.foresignal.stream.sse.StreamPrincipal;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.notify.ActionFixture;
import ai.foresignal.testsupport.notify.NotifyTestConfig;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.trigger.AlertStore;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The notification inbox (prompt 033): listing and filters, cursor and sparse reads, badge math,
 * reading never acknowledging, per-device isolation, fresh capabilities on a single read, the SSE
 * replay and {@code badge} event, and retention.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(NotifyTestConfig.class)
class NotificationInboxIT {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();
    static final String DEVICE_A = "0b9f4a3e-7f1c-4c39-9c51-2b1d0f1f0a01";
    static final String DEVICE_B = "0b9f4a3e-7f1c-4c39-9c51-2b1d0f1f0a02";

    @Autowired MockMvc mvc;
    @Autowired ai.foresignal.security.AdminTokenService adminTokens;
    @Autowired TokenService tokens;
    @Autowired NotificationRouter router;
    @Autowired NotificationService notifications;
    @Autowired AlertStore alerts;
    @Autowired InboxService inbox;
    @Autowired SseHub hub;
    @Autowired TriggerRuleRepository rules;
    @Autowired JdbcTemplate jdbc;
    @Autowired MutableClock clock;

    ActionFixture fixture;
    TokenService.CreatedToken reader;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        fixture = new ActionFixture(jdbc, rules);
        reader = TestTokens.create(tokens, "console", Scope.READ);
    }

    ResultActions call(MockHttpServletRequestBuilder request, String device, String body) throws Exception {
        MockHttpServletRequestBuilder r = request.header(HttpHeaders.AUTHORIZATION, TestTokens.bearer(reader));
        if (device != null) {
            r = r.header(DeviceKeys.HEADER, device);
        }
        if (body != null) {
            r = r.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        return mvc.perform(r);
    }

    static JsonNode json(ResultActions result) throws Exception {
        return JSON.readTree(result.andReturn().getResponse().getContentAsString());
    }

    long fire(String severity) {
        long alert = fixture.alert(fixture.rule("Rule " + severity + System.nanoTime(), T), severity, T);
        return router.fired(alerts.find(alert).orElseThrow()).id();
    }

    long system(String severity) {
        return notifications.publish(NotificationMessage.builder(NotificationMessage.SYSTEM, "system.source_failing",
                "Source failing", severity).build(), c -> false).id();
    }

    @Test
    @DisplayName("listing: newest first with live alert state, filters, keyset paging and an opaque cursor")
    void listing() throws Exception {
        long a1 = fire("warning");
        long s1 = system("warning");
        long a2 = fire("critical");
        JsonNode page = json(call(get("/v1/notifications"), DEVICE_A, null).andExpect(status().isOk()));
        assertThat(page.get("items")).extracting(n -> n.get("id").asLong()).containsExactly(a2, s1, a1);
        assertThat(page.at("/items/0/alertState").asString()).isEqualTo("firing");
        assertThat(page.at("/items/0/read").asBoolean()).isFalse();
        assertThat(page.at("/items/1/alertState").isNull()).isTrue();

        assertThat(json(call(get("/v1/notifications?events=system"), DEVICE_A, null)).get("items")).hasSize(1);
        assertThat(json(call(get("/v1/notifications?minSeverity=critical"), DEVICE_A, null)).get("items")).hasSize(1);
        assertThat(json(call(get("/v1/notifications?after=" + s1), DEVICE_A, null)).get("items"))
                .extracting(n -> n.get("id").asLong()).containsExactly(a2);
        JsonNode first = json(call(get("/v1/notifications?limit=2"), DEVICE_A, null));
        assertThat(first.get("items")).hasSize(2);
        String cursor = first.get("nextCursor").asString();
        JsonNode second = json(call(get("/v1/notifications?limit=2&cursor=" + cursor), DEVICE_A, null));
        assertThat(second.get("items")).extracting(n -> n.get("id").asLong()).containsExactly(a1);
        assertThat(second.get("nextCursor").isNull()).isTrue();
        call(get("/v1/notifications?limit=0"), DEVICE_A, null).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("no device: 400 DEVICE_ID_REQUIRED; a bad header too; no token: 401 even under openReads")
    void deviceRequired() throws Exception {
        assertThat(json(call(get("/v1/notifications"), null, null).andExpect(status().isBadRequest()))
                .get("code").asString()).isEqualTo("DEVICE_ID_REQUIRED");
        call(get("/v1/notifications/badge"), "not-a-uuid", null).andExpect(status().isBadRequest());
        mvc.perform(get("/v1/notifications").header(DeviceKeys.HEADER, DEVICE_A)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("cursor and sparse reads per device; reading never acknowledges; devices are isolated")
    void readState() throws Exception {
        long a1 = fire("warning");
        long a2 = fire("warning");
        long a3 = fire("critical");
        call(post("/v1/notifications/read"), DEVICE_A, "{\"ids\":[" + a3 + "]}").andExpect(status().isOk());
        JsonNode list = json(call(get("/v1/notifications"), DEVICE_A, null));
        assertThat(list.get("items")).extracting(n -> n.get("read").asBoolean()).containsExactly(true, false, false);
        JsonNode badge = json(call(post("/v1/notifications/read"), DEVICE_A, "{\"upToId\":" + a1 + "}"));
        assertThat(badge.get("unread").asInt()).isEqualTo(1);
        assertThat(badge.get("unreadCritical").asInt()).isZero();
        assertThat(badge.get("latestId").asLong()).isEqualTo(a3);
        assertThat(json(call(get("/v1/notifications?unread=true"), DEVICE_A, null)).get("items"))
                .extracting(n -> n.get("id").asLong()).containsExactly(a2);
        // Device B saw nothing.
        assertThat(json(call(get("/v1/notifications/badge"), DEVICE_B, null)).get("unread").asInt()).isEqualTo(3);
        assertThat(json(call(get("/v1/notifications/badge"), DEVICE_B, null)).get("unreadCritical").asInt()).isEqualTo(1);
        // Reading never acknowledges.
        assertThat(jdbc.queryForObject("select count(*) from alert where state <> 'firing'", Long.class)).isZero();
        // Moving the cursor past a sparse read deletes it.
        call(post("/v1/notifications/read"), DEVICE_A, "{\"upToId\":" + a3 + "}").andExpect(status().isOk());
        assertThat(jdbc.queryForObject("select count(*) from notification_read", Long.class)).isZero();
        // The cursor never moves back.
        call(post("/v1/notifications/read"), DEVICE_A, "{\"upToId\":" + a1 + "}").andExpect(status().isOk());
        assertThat(json(call(get("/v1/notifications/badge"), DEVICE_A, null)).get("unread").asInt()).isZero();
        call(post("/v1/notifications/read"), DEVICE_A, "{\"upToId\":1,\"ids\":[1]}").andExpect(status().isBadRequest());
        call(post("/v1/notifications/read"), DEVICE_A, "{\"ids\":[]}").andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("badge: unread system events plus unread firing alerts; acknowledged alerts drop out")
    void badgeMath() throws Exception {
        long fired = fire("critical");
        system("warning");
        long alertId = jdbc.queryForObject("select alert_id from notification where id = ?", Long.class, fired);
        assertThat(inbox.badge("k").unread()).isEqualTo(2);
        assertThat(inbox.badge("k").unreadCritical()).isEqualTo(1);
        jdbc.update("update alert set state = 'acknowledged' where id = ?", alertId);
        assertThat(inbox.badge("k").unread()).isEqualTo(1);
        assertThat(inbox.badge("k").unreadCritical()).isZero();
    }

    @Test
    @DisplayName("sparse reads are capped at 500 per device by dropping the oldest, which become unread again")
    void sparseCap() {
        List<Long> ids = new ArrayList<>();
        for (long i = 1; i <= 501; i++) {
            ids.add(i);
        }
        inbox.markRead("cap", new InboxService.ReadRequest(null, ids.subList(0, 500)));
        inbox.markRead("cap", new InboxService.ReadRequest(null, List.of(501L)));
        assertThat(jdbc.queryForObject("select count(*) from notification_read where device_key = 'cap'", Long.class))
                .isEqualTo(500L);
        assertThat(jdbc.queryForObject("select min(notification_id) from notification_read where device_key = 'cap'",
                Long.class)).isEqualTo(2L);
    }

    @Test
    @DisplayName("a token with device metadata is its own device; getNotification mints capabilities for it")
    void deviceTokenAndDetail() throws Exception {
        tokens.updateDevice(reader.entity().getId(), JSON.readTree(
                "{\"deviceName\":\"iPhone\",\"platform\":\"ios\",\"appVersion\":\"1.0\"}"));
        long fired = fire("warning");
        JsonNode detail = json(call(get("/v1/notifications/" + fired), null, null).andExpect(status().isOk()));
        assertThat(detail.get("actions")).hasSize(3);
        assertThat(detail.at("/actions/0/method").asString()).isEqualTo("POST");
        assertThat(detail.at("/actions/0/url").asString()).contains("/v1/actions/eac_");
        assertThat(detail.get("links").isArray()).isTrue();
        assertThat(jdbc.queryForObject("select device_id from action_capability limit 1", String.class))
                .isEqualTo(reader.entity().getId().toString());
        call(get("/v1/notifications/999999"), null, null).andExpect(status().isNotFound());
        call(get("/v1/notifications/abc"), null, null).andExpect(status().isNotFound());
        long sys = system("info");
        assertThat(json(call(get("/v1/notifications/" + sys), null, null)).get("actions")).isEmpty();
    }

    @Test
    @DisplayName("SSE: replay after Last-Event-ID with read state, live notifications, and badge to the same device only")
    void stream() throws Exception {
        long a1 = fire("warning");
        long a2 = fire("warning");
        inbox.markRead("dev-1", new InboxService.ReadRequest(a1, List.of()));
        RecordingSender mine = new RecordingSender();
        RecordingSender other = new RecordingSender();
        SseClient client = open("dev-1", 0L, mine);
        SseClient otherClient = open("dev-2", a2, other);
        try {
            SseFrame first = mine.next();
            assertThat(first.event()).isEqualTo("notification");
            assertThat(first.id()).isEqualTo(Long.toString(a1));
            assertThat(JSON.readTree(first.data()).get("read").asBoolean()).isTrue();
            SseFrame second = mine.next();
            assertThat(second.id()).isEqualTo(Long.toString(a2));
            assertThat(JSON.readTree(second.data()).get("read").asBoolean()).isFalse();

            long a3 = fire("critical");
            assertThat(mine.next().id()).isEqualTo(Long.toString(a3));
            assertThat(other.next().id()).isEqualTo(Long.toString(a3));

            inbox.markRead("dev-1", new InboxService.ReadRequest(a3, List.of()));
            SseFrame badge = mine.next();
            assertThat(badge.event()).isEqualTo("badge");
            assertThat(badge.id()).isNull();
            assertThat(JSON.readTree(badge.data()).get("unread").asInt()).isZero();
            assertThat(other.poll(500)).isNull();
        } finally {
            client.close();
            otherClient.close();
        }
    }

    @Test
    @DisplayName("retention: notifications older than the inbox window without deliveries go, with their reads; stale cursors go")
    void retention() {
        long old = system("info");
        long recent = system("info");
        jdbc.update("update notification set created_at = ? where id = ?",
                java.sql.Timestamp.from(T.minus(Duration.ofDays(31))), old);
        inbox.markRead("r", new InboxService.ReadRequest(null, List.of(old, recent)));
        inbox.markRead("stale", new InboxService.ReadRequest(1L, List.of()));
        jdbc.update("update notification_read_cursor set updated_at = ? where device_key = 'stale'",
                java.sql.Timestamp.from(T.minus(Duration.ofDays(91))));
        Map<String, Integer> deleted = inbox.purge(T);
        assertThat(deleted.get("notification")).isEqualTo(1);
        assertThat(deleted.get("notification_read")).isEqualTo(1);
        assertThat(deleted.get("notification_read_cursor")).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from notification where id = ?", Long.class, recent)).isEqualTo(1);
        assertThat(inbox.list("r", new InboxService.Query(null, null, List.of(), null, false, 10), Set.of()))
                .extracting(InboxService.Item::id).containsExactly(recent);
    }

    @Test
    @DisplayName("presence endpoints: a heartbeat per device (read), the listing for admin only, mute and unmute a rule")
    void presenceAndMuteEndpoints() throws Exception {
        JsonNode beat = json(call(post("/v1/clients/presence"), DEVICE_A, "{\"kind\":\"desktop\",\"state\":\"active\","
                + "\"ttlSeconds\":60}").andExpect(status().isOk()));
        assertThat(beat.get("deviceKey").asString()).isEqualTo(reader.entity().getId() + ":" + DEVICE_A);
        assertThat(beat.get("expiresAt").asString()).isEqualTo(T.plusSeconds(60).toString());
        call(post("/v1/clients/presence"), DEVICE_A, "{\"kind\":\"desktop\",\"state\":\"active\",\"ttlSeconds\":301}")
                .andExpect(status().isBadRequest());
        call(get("/v1/clients"), null, null).andExpect(status().isForbidden());
        String admin = "Bearer " + adminTokens.rotate();
        JsonNode clients = JSON.readTree(mvc.perform(get("/v1/clients").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(clients).hasSize(1);
        UUID rule = fixture.rule("Muted", T);
        JsonNode muted = JSON.readTree(mvc.perform(post("/v1/triggers/" + rule + "/mute")
                        .header(HttpHeaders.AUTHORIZATION, admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"for\":\"4h\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(muted.get("mutedUntil").asString()).isEqualTo(T.plus(Duration.ofHours(4)).toString());
        mvc.perform(post("/v1/triggers/" + rule + "/mute").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"for\":\"8d\"}")).andExpect(status().isBadRequest());
        mvc.perform(post("/v1/triggers/" + rule + "/mute").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"for\":\"30m\"}")).andExpect(status().isBadRequest());
        call(post("/v1/triggers/" + rule + "/mute"), null, "{\"for\":\"4h\"}").andExpect(status().isForbidden());
        JsonNode unmuted = JSON.readTree(mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/v1/triggers/" + rule + "/mute").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(unmuted.get("mutedUntil").isNull()).isTrue();
    }

    SseClient open(String device, Long resume, RecordingSender sender) {
        StreamFilter filter = new StreamFilter(List.of(InboxTopic.NAME),
                Map.of(InboxTopic.NAME, new InboxFilter(device, Set.of())), 1);
        return hub.open(new SseHub.OpenRequest(StreamPrincipal.forToken(UUID.randomUUID(), Scope.READ, "127.0.0.1"),
                SseClient.CursorMode.SINGLE, filter, Map.of(InboxTopic.NAME, resume), false), sender);
    }

    /** Records frames other than heartbeats; every wait has a timeout. */
    static final class RecordingSender implements SseSender {
        private final BlockingQueue<SseFrame> frames = new LinkedBlockingQueue<>();

        @Override
        public void send(SseFrame frame) {
            if (!"heartbeat".equals(frame.event())) {
                frames.add(frame);
            }
        }

        @Override
        public void complete() {}

        SseFrame next() throws InterruptedException {
            SseFrame frame = frames.poll(10, TimeUnit.SECONDS);
            assertThat(frame).as("an SSE frame within 10 s").isNotNull();
            return frame;
        }

        SseFrame poll(long millis) throws InterruptedException {
            return frames.poll(millis, TimeUnit.MILLISECONDS);
        }
    }
}
