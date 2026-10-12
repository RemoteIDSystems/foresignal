package ai.foresignal.notify.action;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.ChannelRateLimiter;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.notify.DeliveryWorker;
import ai.foresignal.notify.NotificationRouter;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.store.NotificationDeliveryRepository;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.notify.ActionFixture;
import ai.foresignal.testsupport.notify.NotifyFixture;
import ai.foresignal.testsupport.notify.NotifyTestConfig;
import ai.foresignal.trigger.AlertStore;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Matrix reaction acknowledgements against a WireMock homeserver (prompt 033): the first sync only
 * records where to start, a ✅ from an allowed sender on Foresignal's event acknowledges, other
 * senders, other keys and other events do not, and {@code since} is persisted.
 */
@IntegrationTest
@Import(NotifyTestConfig.class)
class MatrixReactionIT extends WireMockSupport {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final String ROOM = "!r:x";

    @Autowired MatrixReactionPoller poller;
    @Autowired NotificationRouter router;
    @Autowired DeliveryWorker worker;
    @Autowired AlertStore alerts;
    @Autowired ChannelService channels;
    @Autowired ChannelRateLimiter limiter;
    @Autowired NotificationDeliveryRepository deliveries;
    @Autowired TriggerRuleRepository rules;
    @Autowired AppSettingService settings;
    @Autowired JdbcTemplate jdbc;
    @Autowired MutableClock clock;

    ActionFixture fixture;
    UUID channel;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        settings.invalidateCache();
        fixture = new ActionFixture(jdbc, rules);
        channel = new NotifyFixture(channels, deliveries, limiter).channel("{'name':'m','type':'matrix','config':{"
                + "'homeserverUrl':'" + baseUrl() + "','roomId':'" + ROOM + "','actions':{'reactions':true},"
                + "'allowedSenders':['@op:x']},'secrets':{'accessToken':'syt'}}");
        WIREMOCK.stubFor(put(urlMatching("/_matrix/client/v3/rooms/.*/send/m.room.message/\\d+"))
                .willReturn(okJson("{\"event_id\":\"$e1\"}")));
    }

    static String reaction(String sender, String target, String key) {
        return "{\"type\":\"m.reaction\",\"sender\":\"" + sender + "\",\"event_id\":\"$r" + sender.hashCode()
                + "\",\"content\":{\"m.relates_to\":{\"rel_type\":\"m.annotation\",\"event_id\":\"" + target
                + "\",\"key\":\"" + key + "\"}}}";
    }

    static String sync(String next, String... events) {
        return "{\"next_batch\":\"" + next + "\",\"rooms\":{\"join\":{\"" + ROOM + "\":{\"timeline\":{\"events\":["
                + String.join(",", events) + "]}}}}}";
    }

    @Test
    @DisplayName("first sync records since only; then a ✅ from an allowed sender on our event acknowledges")
    void acknowledges() throws Exception {
        long alert = fixture.alert(fixture.rule("Quakes", T), "warning", T);
        router.fired(alerts.find(alert).orElseThrow());
        worker.runOnce();
        WIREMOCK.stubFor(get(urlPathEqualTo("/_matrix/client/v3/sync")).withQueryParam("since", absent())
                .willReturn(okJson(sync("s1", reaction("@op:x", "$e1", "✅")))));
        assertThat(poller.pollOnce(channel, 0)).isZero();
        assertThat(fixture.state(alert)).isEqualTo("firing");
        assertThat(settings.get(MatrixReactionPoller.sinceKey(channel), String.class)).contains("s1");

        WIREMOCK.stubFor(get(urlPathEqualTo("/_matrix/client/v3/sync")).withQueryParam("since", equalTo("s1"))
                .willReturn(okJson(sync("s2",
                        reaction("@intruder:x", "$e1", "✅"),
                        reaction("@op:x", "$e1", "👍"),
                        reaction("@op:x", "$other", "✅")))));
        assertThat(poller.pollOnce(channel, 0)).isZero();
        assertThat(fixture.state(alert)).isEqualTo("firing");

        WIREMOCK.stubFor(get(urlPathEqualTo("/_matrix/client/v3/sync")).withQueryParam("since", equalTo("s2"))
                .willReturn(okJson(sync("s3", reaction("@op:x", "$e1", "✅")))));
        assertThat(poller.pollOnce(channel, 0)).isEqualTo(1);
        assertThat(fixture.state(alert)).isEqualTo("acknowledged");
        assertThat(fixture.acknowledgedBy(alert)).isEqualTo("action:matrix:m");
        assertThat(settings.get(MatrixReactionPoller.sinceKey(channel), String.class)).contains("s3");
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/_matrix/client/v3/sync"))
                .withHeader("Authorization", equalTo("Bearer syt"))
                .withQueryParam("filter", com.github.tomakehurst.wiremock.client.WireMock.containing("m.reaction")));
    }

    @Test
    @DisplayName("a channel without reactions enabled is not polled")
    void notWanted() throws Exception {
        UUID plain = new NotifyFixture(channels, deliveries, limiter).channel("{'name':'p','type':'matrix','config':{"
                + "'homeserverUrl':'" + baseUrl() + "','roomId':'" + ROOM + "'},'secrets':{'accessToken':'syt'}}");
        assertThat(poller.pollOnce(plain, 0)).isZero();
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/_matrix/client/v3/sync")));
    }
}
