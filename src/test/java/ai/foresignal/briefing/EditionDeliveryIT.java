package ai.foresignal.briefing;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.llm.LlmSettings;
import ai.foresignal.llm.TtsBudget;
import ai.foresignal.notify.DeliveryWorker;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.briefing.BriefingItSupport;
import ai.foresignal.testsupport.briefing.BriefingTestSources;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;

/**
 * Edition output targets end to end (prompt NP-004): an email target sends the HTML edition with its audio attached
 * and the site link over SMTP; a push target sends a headline, a summary and the link, never the body; an alert target
 * sends the alert shape to its channel; a target that cannot be delivered never blocks the others, and each outcome
 * is stored on the run. A briefing without targets delivers exactly as before.
 */
@IntegrationTest
@Import(BriefingTestSources.class)
class EditionDeliveryIT extends BriefingItSupport {

    /** An MP3 as a speech server returns it: an empty ID3v2 tag, then MPEG frame bytes. */
    static final byte[] MP3 = {'I', 'D', '3', 4, 0, 0, 0, 0, 0, 0, (byte) 0xFF, (byte) 0xFB, (byte) 0x90, 0x64, 1, 2, 3};

    @RegisterExtension
    static final WireMockExtension TTS = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().gzipDisabled(true)).resetOnEachTest(true).build();

    @RegisterExtension
    static final WireMockExtension HOOKS = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().gzipDisabled(true)).resetOnEachTest(true).build();

    @Autowired DeliveryWorker worker;
    @Autowired TtsBudget ttsBudget;

    GreenMail mail;
    UUID email;
    UUID push;
    UUID alert;

    @BeforeEach
    void setUpTargets() {
        ttsBudget.resetState();
        settings.put(SettingKeys.SERVER, new ServerSettings("https://fs.example"));
        providers.create(json("{\"id\":\"speech\",\"type\":\"openai-compatible\",\"baseUrl\":\"" + TTS.baseUrl()
                + "/v1\",\"apiKey\":\"sk-test-not-a-key\",\"requestsPerMinute\":100,"
                + "\"options\":{\"ttsModel\":\"kokoro\",\"ttsCharactersPerDay\":50000}}"));
        llmSettings.update(LlmSettings.DEFAULTS.withTtsProvider("speech"));
        TTS.stubFor(post(urlPathEqualTo("/v1/audio/speech")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "audio/mpeg").withBody(MP3)));
        HOOKS.stubFor(post(urlEqualTo("/push")).willReturn(aResponse().withStatus(204)));
        HOOKS.stubFor(post(urlEqualTo("/alert")).willReturn(aResponse().withStatus(204)));

        mail = new GreenMail(ServerSetupTest.SMTP.dynamicPort());
        mail.start();
        email = notify.channel("{'name':'edition-mail','type':'email','config':{'smtpHost':'127.0.0.1','smtpPort':"
                + mail.getSmtp().getPort() + ",'tls':'none','from':'fs@example.invalid',"
                + "'to':['reader@example.invalid']}}");
        push = notify.channel("{'name':'edition-push','type':'webhook','config':{'url':'" + HOOKS.baseUrl()
                + "/push'}}");
        alert = notify.channel("{'name':'edition-alert','type':'webhook','config':{'url':'" + HOOKS.baseUrl()
                + "/alert'}}");
    }

    @AfterEach
    void stopMail() {
        mail.stop();
    }

    private UUID edition(String targets) {
        return id(briefings.create(json(digestBody("Morning edition", "0 7 * * *",
                "\"audio\":{\"voice\":\"af_bella\",\"format\":\"mp3\"},\"deliveryTargets\":" + targets)),
                agentCaller()));
    }

    private static String target(UUID channel, String format, String options) {
        return "{\"kind\":\"channel\",\"channelId\":\"" + channel + "\",\"format\":\"" + format + "\""
                + (options == null ? "" : ",\"options\":" + options) + "}";
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> results(Map<String, Object> run) {
        return (List<Map<String, Object>>) run.get("deliveryResults");
    }

    @Test
    @DisplayName("email: the HTML edition (BriefingHtmlRenderer), the audio attached and the site link")
    void email() throws Exception {
        quake("q1", "Tokyo Bay", 5.5);
        UUID id = edition("[" + target(email, "email", null) + "]");

        Map<String, Object> run = briefings.runNow(id);
        assertThat(results(run)).singleElement().satisfies(r -> assertThat(r).containsEntry("status", "queued")
                .containsEntry("format", "email").containsEntry("channelId", email.toString()));
        assertThat(run.get("deliveries")).isEqualTo(1);

        worker.runOnce();
        assertThat(mail.waitForIncomingEmail(5_000, 1)).isTrue();
        MimeMessage received = mail.getReceivedMessages()[0];
        String whole = GreenMailUtil.getWholeMessage(received);
        assertThat(whole).contains("<!DOCTYPE html>").contains("<html lang=3D\"en\"").contains("Tokyo Bay")
                .contains("https://fs.example/v1/editions/" + id + "/site")
                .contains("filename=briefing.mp3").contains("audio/mpeg");
    }

    @Test
    @DisplayName("push: a headline, a summary and the site link; never the body, the audio or the citations")
    void push() throws Exception {
        quake("q1", "Tokyo Bay", 5.5);
        UUID id = edition("[" + target(push, "push", null) + "]");
        Map<String, Object> run = briefings.runNow(id);
        assertThat(results(run)).singleElement().satisfies(r -> assertThat(r).containsEntry("status", "queued"));

        worker.runOnce();
        String payload = HOOKS.findAll(postRequestedFor(urlEqualTo("/push"))).getFirst().getBodyAsString();
        JsonNode body = json(payload);
        assertThat(payload).contains("Morning edition").contains("https://fs.example/v1/editions/" + id + "/site")
                .doesNotContain("contentMarkdown").doesNotContain("mediaId").doesNotContain("citations")
                .doesNotContain("editionHtml");
        assertThat(payload).doesNotContain(((String) run.get("contentMarkdown")).strip());
        assertThat(EditionDelivery.summary((String) run.get("contentMarkdown")).length())
                .isLessThanOrEqualTo(EditionDelivery.SUMMARY_CHARS + 1);
        assertThat(body.isObject()).isTrue();
    }

    @Test
    @DisplayName("alert: the alert shape (event alert, subtype edition.alert, the configured severity) on the channel")
    void alert() throws Exception {
        quake("q1", "Tokyo Bay", 5.5);
        UUID id = edition("[" + target(alert, "alert", "{\"severity\":\"warning\"}") + "]");
        briefings.runNow(id);

        worker.runOnce();
        String payload = HOOKS.findAll(postRequestedFor(urlEqualTo("/alert"))).getFirst().getBodyAsString();
        assertThat(payload).contains("\"alert\"").contains(EditionDelivery.ALERT_SUBTYPE).contains("warning")
                .contains("https://fs.example/v1/editions/" + id + "/site").doesNotContain("contentMarkdown");
    }

    @Test
    @DisplayName("one target failing never blocks the others; every outcome is stored on the run, in order")
    void isolation() throws Exception {
        quake("q1", "Tokyo Bay", 5.5);
        UUID gone = notify.channel("{'name':'gone','type':'webhook','config':{'url':'" + HOOKS.baseUrl() + "/gone'}}");
        UUID id = edition("[" + target(gone, "push", null) + "," + target(push, "push", null) + ","
                + "{\"kind\":\"website\",\"format\":\"html\"},{\"kind\":\"podcast\",\"format\":\"audio\"},"
                + target(alert, "alert", null) + "]");
        channels.delete(gone);

        Map<String, Object> run = briefings.runNow(id);

        List<Map<String, Object>> results = results(run);
        assertThat(results).extracting(r -> r.get("status"))
                .containsExactly("skipped", "queued", "available", "available", "queued");
        assertThat(results.getFirst().get("error")).isEqualTo("the channel does not exist");
        assertThat(results.get(2)).containsEntry("url", "https://fs.example/v1/editions/" + id + "/site");
        assertThat(results.get(3)).containsEntry("url", "https://fs.example/v1/briefings/" + id + "/podcast.rss");
        assertThat(run.get("deliveries")).isEqualTo(2);
        assertThat(BriefingViews.run(runRepository.findById(UUID.fromString((String) run.get("id"))).orElseThrow())
                .get("deliveryResults")).isEqualTo(results);
    }

    @Test
    @DisplayName("without targets a briefing delivers exactly as before: every channel subscribed to briefing")
    void defaultUnchanged() {
        quake("q1", "Tokyo Bay", 5.5);
        UUID subscribed = notify.channel("{'name':'briefings','type':'webhook','events':['briefing'],'config':{'url':'"
                + HOOKS.baseUrl() + "/push'}}");
        UUID id = id(briefings.create(json(digestBody("Plain", "0 7 * * *", null)), agentCaller()));

        Map<String, Object> run = briefings.runNow(id);

        assertThat(run.get("deliveryResults")).isNull();
        assertThat(run.get("deliveries")).isEqualTo(1);
        assertThat(notify.deliveries(subscribed)).hasSize(1);
        assertThat(notify.deliveries(email)).isEmpty();
    }

    @Test
    @DisplayName("deliveryTargets are validated: kinds, formats, channel ids and the website/podcast formats")
    void validation() {
        Throwable thrown = org.assertj.core.api.Assertions.catchThrowable(() -> briefings.create(json(digestBody("Bad",
                "0 7 * * *", "\"deliveryTargets\":[{\"kind\":\"channel\",\"format\":\"html\"},"
                        + "{\"kind\":\"website\",\"format\":\"audio\"},{\"kind\":\"fax\",\"format\":\"push\"}]")),
                agentCaller()));
        assertThat(thrown).isInstanceOf(ai.foresignal.api.error.ValidationException.class);
        assertThat(((ai.foresignal.api.error.ValidationException) thrown).getErrors().stream()
                .map(ai.foresignal.api.error.FieldError::field).toList().toString())
                .contains("deliveryTargets[0].channelId").contains("deliveryTargets[0].format")
                .contains("deliveryTargets[1].format").contains("deliveryTargets[2].kind");
    }
}
