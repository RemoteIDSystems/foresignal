package ai.foresignal.notify;

import static ai.foresignal.testsupport.notify.NotifyFixture.alert;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.store.DeliveryKind;
import ai.foresignal.store.DeliveryStatus;
import ai.foresignal.store.NotificationDeliveryEntity;
import ai.foresignal.store.NotificationDeliveryRepository;
import ai.foresignal.store.NotificationRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.notify.NotifyFixture;
import ai.foresignal.testsupport.notify.NotifyTestConfig;
import ai.foresignal.trigger.AlertEvent;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * The delivery worker against WireMock receivers and an in-process GreenMail SMTP server (prompt
 * 018). Nothing here contacts a real service.
 */
@IntegrationTest
@Import(NotifyTestConfig.class)
class DeliveryWorkerIT extends WireMockSupport {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final UUID RULE = UUID.fromString("00000000-0000-0000-0000-0000000000c1");

    @Autowired NotificationRouter router;
    @Autowired DeliveryWorker worker;
    @Autowired ChannelService channels;
    @Autowired ChannelRateLimiter limiter;
    @Autowired NotificationDeliveryRepository deliveries;
    @Autowired NotificationRepository notifications;
    @Autowired AppSettingService settings;
    @Autowired MutableClock clock;

    NotifyFixture fixture;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        clock.setInstant(T);
        fixture = new NotifyFixture(channels, deliveries, limiter);
    }

    NotificationDeliveryEntity only(UUID channel) {
        return fixture.deliveries(channel).getFirst();
    }

    @Test
    @DisplayName("Discord: sent with ?wait=true, the message id stored, and a resolve edits it with PATCH")
    void discordEditInPlace() {
        WIREMOCK.stubFor(post(urlEqualTo("/discord/1/T?wait=true")).willReturn(okJson("{\"id\":\"555\"}")));
        WIREMOCK.stubFor(patch(urlEqualTo("/discord/1/T/messages/555")).willReturn(okJson("{\"id\":\"555\"}")));
        UUID c = fixture.channel("{'name':'d','type':'discord','secrets':{'webhookUrl':'" + url("/discord/1/T") + "'}}");
        var a = alert(1, RULE, "Quakes", "warning", T);
        router.fired(a);
        worker.runOnce();
        assertThat(only(c).getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(only(c).getProviderMessageRef()).isEqualTo("555");
        assertThat(only(c).getAttempts()).isEqualTo(1);

        router.lifecycle(AlertEvent.RESOLVED, NotifyFixture.resolved(a, T.plusSeconds(60), "admin"));
        worker.runOnce();
        WIREMOCK.verify(1, patchRequestedFor(urlEqualTo("/discord/1/T/messages/555"))
                .withRequestBody(matchingJsonPath("$.embeds[0].title", containing("Resolved"))));
        NotificationDeliveryEntity update = fixture.deliveries(c).getLast();
        assertThat(update.getKind()).isEqualTo(DeliveryKind.UPDATE);
        assertThat(update.isEdited()).isTrue();
        assertThat(update.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
    }

    @Test
    @DisplayName("Telegram and Matrix capture message_id and event_id; Matrix uses the delivery id as txnId")
    void telegramAndMatrixRefs() {
        WIREMOCK.stubFor(post(urlEqualTo("/botTOKEN1/sendMessage")).willReturn(okJson("{\"ok\":true,\"result\":{\"message_id\":77}}")));
        WIREMOCK.stubFor(put(urlMatching("/_matrix/client/v3/rooms/%21r%3Ax/send/m.room.message/\\d+"))
                .willReturn(okJson("{\"event_id\":\"$e1\"}")));
        UUID tg = fixture.channel("{'name':'t','type':'telegram','config':{'chatId':'5','apiBaseUrl':'" + baseUrl()
                + "'},'secrets':{'botToken':'TOKEN1'}}");
        UUID mx = fixture.channel("{'name':'m','type':'matrix','config':{'homeserverUrl':'" + baseUrl()
                + "','roomId':'!r:x'},'secrets':{'accessToken':'syt'}}");
        router.fired(alert(2, RULE, "Quakes", "warning", T));
        worker.runOnce();
        assertThat(only(tg).getProviderMessageRef()).isEqualTo("77");
        assertThat(only(mx).getProviderMessageRef()).isEqualTo("$e1");
        WIREMOCK.verify(putRequestedFor(urlEqualTo("/_matrix/client/v3/rooms/%21r%3Ax/send/m.room.message/" + only(mx).getId()))
                .withHeader("Authorization", equalTo("Bearer syt")));
    }

    @Test
    @DisplayName("failures follow the retry ladder 10 s, 1 m, 5 m, 30 m, 2 h, then fail and raise a system event")
    void retryLadder() {
        WIREMOCK.stubFor(post(urlEqualTo("/fail")).willReturn(aResponse().withStatus(500).withBody("boom")));
        UUID c = fixture.channel("{'name':'w','type':'webhook','config':{'url':'" + url("/fail") + "'}}");
        router.fired(alert(3, RULE, "Quakes", "warning", T));
        long[] waits = {10, 60, 300, 1800, 7200};
        for (int i = 0; i < waits.length; i++) {
            worker.runOnce();
            NotificationDeliveryEntity d = only(c);
            assertThat(d.getAttempts()).isEqualTo(i + 1);
            assertThat(d.getStatus()).isEqualTo(DeliveryStatus.PENDING);
            assertThat(d.getNextAttemptAt()).isEqualTo(clock.instant().plusSeconds(waits[i]));
            assertThat(d.getLastError()).isEqualTo("HTTP 500: boom");
            clock.advanceSeconds(waits[i]);
        }
        worker.runOnce();
        assertThat(only(c).getStatus()).isEqualTo(DeliveryStatus.FAILED);
        assertThat(only(c).getAttempts()).isEqualTo(6);
        assertThat(notifications.findAll()).anySatisfy(n -> assertThat(n.getSubtype()).isEqualTo("system.delivery_failed"));
        assertThat(fixture.deliveries(c)).hasSize(1);
    }

    @Test
    @DisplayName("429 honors Retry-After and counts as an attempt at most once per 10 minutes")
    void retryAfter() {
        WIREMOCK.stubFor(post(urlEqualTo("/limited")).willReturn(aResponse().withStatus(429).withHeader("Retry-After", "30")));
        UUID c = fixture.channel("{'name':'w','type':'webhook','config':{'url':'" + url("/limited") + "'}}");
        router.fired(alert(4, RULE, "Quakes", "warning", T));
        worker.runOnce();
        assertThat(only(c).getAttempts()).isEqualTo(1);
        assertThat(only(c).getNextAttemptAt()).isEqualTo(T.plusSeconds(30));
        clock.advanceSeconds(30);
        worker.runOnce();
        assertThat(only(c).getAttempts()).isEqualTo(1);
        assertThat(only(c).getNextAttemptAt()).isEqualTo(T.plusSeconds(60));
        clock.advanceSeconds(600);
        worker.runOnce();
        assertThat(only(c).getAttempts()).isEqualTo(2);
    }

    @Test
    @DisplayName("Apprise 424 is delivered with a warning and never retried")
    void apprisePartial() {
        WIREMOCK.stubFor(post(urlEqualTo("/notify/fs")).willReturn(aResponse().withStatus(424).withBody("partial")));
        UUID c = fixture.channel("{'name':'a','type':'apprise','config':{'serverUrl':'" + baseUrl() + "','configKey':'fs'}}");
        router.fired(alert(5, RULE, "Quakes", "warning", T));
        worker.runOnce();
        clock.advanceSeconds(3600);
        worker.runOnce();
        WIREMOCK.verify(1, postRequestedFor(urlEqualTo("/notify/fs")));
        assertThat(only(c).getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(only(c).getLastError()).startsWith("warning: HTTP 424");
    }

    @Test
    @DisplayName("Pushover: the emergency receipt is stored and canceled on resolve before the follow-up")
    void pushoverReceiptCancel() {
        WIREMOCK.stubFor(post(urlEqualTo("/1/messages.json"))
                .willReturn(okJson("{\"status\":1,\"request\":\"r1\",\"receipt\":\"rcpt9\"}")));
        WIREMOCK.stubFor(post(urlEqualTo("/1/receipts/rcpt9/cancel.json")).willReturn(okJson("{\"status\":1}")));
        UUID c = fixture.channel("{'name':'p','type':'pushover','config':{'apiBaseUrl':'" + baseUrl()
                + "'},'secrets':{'appToken':'app','userKey':'usr'}}");
        var a = alert(6, RULE, "Quakes", "critical", T);
        router.fired(a);
        worker.runOnce();
        assertThat(only(c).getProviderMessageRef()).isEqualTo("rcpt9");
        WIREMOCK.verify(postRequestedFor(urlEqualTo("/1/messages.json")).withRequestBody(containing("priority=2")));

        router.lifecycle(AlertEvent.RESOLVED, NotifyFixture.resolved(a, T, "admin"));
        worker.runOnce();
        WIREMOCK.verify(1, postRequestedFor(urlEqualTo("/1/receipts/rcpt9/cancel.json")).withRequestBody(containing("token=app")));
        WIREMOCK.verify(2, postRequestedFor(urlEqualTo("/1/messages.json")));
        assertThat(fixture.deliveries(c).getLast().getKind()).isEqualTo(DeliveryKind.FOLLOWUP);
    }

    @Test
    @DisplayName("the per-channel rate limit reschedules without counting an attempt")
    void rateLimit() {
        WIREMOCK.stubFor(post(urlPathEqualTo("/botTOK/sendMessage")).willReturn(okJson("{\"ok\":true,\"result\":{\"message_id\":1}}")));
        UUID c = fixture.channel("{'name':'t','type':'telegram','backlogCap':50,'config':{'chatId':'5','apiBaseUrl':'"
                + baseUrl() + "'},'secrets':{'botToken':'TOK'}}");
        for (long id = 1; id <= 21; id++) {
            router.fired(alert(100 + id, RULE, "Quakes", "info", T));
        }
        worker.runOnce();
        WIREMOCK.verify(20, postRequestedFor(urlPathEqualTo("/botTOK/sendMessage")));
        NotificationDeliveryEntity waiting = fixture.pending(c).getFirst();
        assertThat(waiting.getAttempts()).isZero();
        assertThat(waiting.getNextAttemptAt()).isAfter(T);
    }

    @Test
    @DisplayName("two workers never send the same delivery twice")
    void noDoubleSend() throws Exception {
        WIREMOCK.stubFor(post(urlEqualTo("/hook")).willReturn(aResponse().withStatus(200).withFixedDelay(50)));
        for (int i = 0; i < 4; i++) {
            fixture.channel("{'name':'w" + i + "','type':'webhook','config':{'url':'" + url("/hook") + "'}}");
        }
        for (long id = 1; id <= 3; id++) {
            router.fired(alert(id, RULE, "Quakes", "info", T));
        }
        CompletableFuture<Integer> a = CompletableFuture.supplyAsync(worker::runOnce);
        CompletableFuture<Integer> b = CompletableFuture.supplyAsync(worker::runOnce);
        assertThat(a.get() + b.get()).isEqualTo(12);
        worker.runOnce();
        WIREMOCK.verify(12, postRequestedFor(urlEqualTo("/hook")));
    }

    @Test
    @DisplayName("redirects are not followed and count as failures")
    void noRedirects() {
        WIREMOCK.stubFor(post(urlEqualTo("/moved")).willReturn(aResponse().withStatus(302).withHeader("Location", url("/elsewhere"))));
        UUID c = fixture.channel("{'name':'w','type':'webhook','config':{'url':'" + url("/moved") + "'}}");
        router.fired(alert(7, RULE, "Quakes", "info", T));
        worker.runOnce();
        WIREMOCK.verify(0, postRequestedFor(urlEqualTo("/elsewhere")));
        WIREMOCK.verify(0, com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor(urlEqualTo("/elsewhere")));
        assertThat(only(c).getAttempts()).isEqualTo(1);
        assertThat(only(c).getLastError()).contains("redirect not followed");
    }

    @Test
    @DisplayName("secrets embedded in URLs are redacted from last_error")
    void redaction() {
        String secretPath = "/services/T0/B0/SUPERSECRETTOKEN";
        WIREMOCK.stubFor(post(urlEqualTo(secretPath)).willReturn(aResponse().withStatus(400)
                .withBody("invalid webhook " + url(secretPath) + " token SUPERSECRETTOKEN")));
        UUID c = fixture.channel("{'name':'s','type':'slack','secrets':{'webhookUrl':'" + url(secretPath) + "'}}");
        router.fired(alert(8, RULE, "Quakes", "info", T));
        worker.runOnce();
        assertThat(only(c).getLastError()).doesNotContain("SUPERSECRETTOKEN").contains("***");
    }

    @Test
    @DisplayName("email is sent over SMTP as multipart text and HTML to an in-process server")
    void email() throws Exception {
        GreenMail mail = new GreenMail(ServerSetupTest.SMTP.dynamicPort());
        mail.start();
        try {
            UUID c = fixture.channel("{'name':'e','type':'email','config':{'smtpHost':'127.0.0.1','smtpPort':"
                    + mail.getSmtp().getPort() + ",'tls':'none','from':'fs@example.invalid','to':['ops@example.invalid']}}");
            router.fired(alert(9, RULE, "Quakes", "warning", T));
            worker.runOnce();
            assertThat(mail.waitForIncomingEmail(5_000, 1)).isTrue();
            MimeMessage received = mail.getReceivedMessages()[0];
            assertThat(received.getSubject()).isEqualTo("[warning] Quakes");
            assertThat(received.getHeader("X-Foresignal-Origin")).isNotEmpty();
            assertThat(received.getContent()).isInstanceOf(MimeMultipart.class);
            assertThat(only(c).getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        } finally {
            mail.stop();
        }
    }

    @Test
    @DisplayName("a deleted channel's pending deliveries fail with 'channel deleted'")
    void deletedChannel() {
        UUID c = fixture.channel("{'name':'w','type':'webhook','config':{'url':'" + url("/never") + "'}}");
        router.fired(alert(10, RULE, "Quakes", "info", T));
        channels.delete(c);
        worker.runOnce();
        assertThat(only(c).getStatus()).isEqualTo(DeliveryStatus.FAILED);
        assertThat(only(c).getLastError()).isEqualTo("channel deleted");
        WIREMOCK.verify(0, postRequestedFor(urlEqualTo("/never")));
    }
}
