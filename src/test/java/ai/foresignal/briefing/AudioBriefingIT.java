package ai.foresignal.briefing;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.llm.LlmSettings;
import ai.foresignal.llm.TtsBudget;
import ai.foresignal.media.MediaRow;
import ai.foresignal.media.MediaStore;
import ai.foresignal.notify.DeliveryWorker;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.briefing.BriefingHttp;
import ai.foresignal.testsupport.briefing.BriefingItSupport;
import ai.foresignal.testsupport.briefing.BriefingTestSources;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import tools.jackson.databind.JsonNode;

/**
 * Audio briefings (prompt 141) end to end against WireMock stand-ins of an OpenAI-compatible speech server and the
 * Telegram Bot API: the run's plain text is synthesized through the {@code tts} role, stored as {@code role=audio}
 * media linked from the run and signed, attached to the Telegram delivery with {@code sendAudio}, and listed in the
 * podcast RSS with an {@code <enclosure>}; a speech failure is recorded on the run and the text is still delivered.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(BriefingTestSources.class)
class AudioBriefingIT extends BriefingItSupport {

    /** An MP3 as a speech server returns it: an empty ID3v2 tag, then MPEG frame bytes. */
    static final byte[] MP3 = {'I', 'D', '3', 4, 0, 0, 0, 0, 0, 0, (byte) 0xFF, (byte) 0xFB, (byte) 0x90, 0x64, 1, 2, 3};

    static final String TOKEN = "123456:TESTTOKEN";

    @RegisterExtension
    static final WireMockExtension TTS = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().gzipDisabled(true))
            .resetOnEachTest(true)
            .build();

    @RegisterExtension
    static final WireMockExtension TELEGRAM = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().gzipDisabled(true))
            .resetOnEachTest(true)
            .build();

    @Autowired DeliveryWorker worker;
    @Autowired MediaStore media;
    @Autowired TtsBudget ttsBudget;
    @Autowired MockMvc mvc;

    BriefingHttp http;
    UUID telegram;

    @BeforeEach
    void setUpAudio() {
        http = new BriefingHttp(mvc);
        ttsBudget.resetState();
        settings.put(SettingKeys.SERVER, new ServerSettings("https://fs.example"));
        providers.create(json("{\"id\":\"speech\",\"type\":\"openai-compatible\",\"baseUrl\":\"" + TTS.baseUrl()
                + "/v1\",\"apiKey\":\"sk-test-not-a-key\",\"requestsPerMinute\":100,"
                + "\"options\":{\"ttsModel\":\"kokoro\",\"ttsCharactersPerDay\":50000}}"));
        llmSettings.update(LlmSettings.DEFAULTS.withTtsProvider("speech"));
        telegram = notify.channel("{'name':'tg','type':'telegram','events':['briefing'],'config':{'chatId':'42',"
                + "'apiBaseUrl':'" + TELEGRAM.baseUrl() + "'},'secrets':{'botToken':'" + TOKEN + "'}}");
        TELEGRAM.stubFor(post(urlEqualTo("/bot" + TOKEN + "/sendAudio")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"ok\":true,\"result\":{\"message_id\":5,\"audio\":{\"file_id\":\"a\"}}}")));
        TELEGRAM.stubFor(post(urlEqualTo("/bot" + TOKEN + "/sendMessage")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"ok\":true,\"result\":{\"message_id\":6}}")));
    }

    void speechOk() {
        TTS.stubFor(post(urlPathEqualTo("/v1/audio/speech")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "audio/mpeg").withBody(MP3)));
    }

    UUID audioBriefing(String format) {
        return id(briefings.create(json(digestBody("Morning audio", "0 7 * * *", "\"channelIds\":[\"" + telegram
                + "\"],\"audio\":{\"voice\":\"af_bella\",\"format\":\"" + format + "\"}")), agentCaller()));
    }

    @Test
    @DisplayName("the run's text is synthesized, stored as role=audio media linked from the run, and signed")
    @SuppressWarnings("unchecked")
    void synthesizedAndStored() throws Exception {
        speechOk();
        quake("q1", "Tokyo Bay", 5.5);
        UUID id = audioBriefing("mp3");
        assertThat(briefingRepository.findById(id).orElseThrow().getAudio())
                .containsEntry("voice", "af_bella").containsEntry("format", "mp3");

        Map<String, Object> run = briefings.runNow(id);

        Map<String, Object> audio = (Map<String, Object>) run.get("audio");
        assertThat(audio).containsEntry("status", "ok").containsEntry("format", "mp3").containsEntry("voice", "af_bella")
                .containsEntry("truncated", false).containsEntry("contentType", "audio/mpeg");
        assertThat(audio.get("error")).isNull();
        String mediaId = (String) audio.get("mediaId");
        MediaRow row = media.find(mediaId).orElseThrow();
        assertThat(row.role()).isEqualTo("audio");
        assertThat(row.contentType()).isEqualTo("audio/mpeg");
        assertThat(row.width()).isZero();
        assertThat(row.expiresAt()).as("expires with the briefing-run retention").isAfter(clock.instant());
        assertThat(Files.readAllBytes(media.file(row))).isEqualTo(MP3);
        assertThat((String) audio.get("signature")).matches("^[A-Za-z0-9_-]+\\.\\.[A-Za-z0-9_-]+$");
        assertThat(((Number) audio.get("characters")).longValue()).isPositive();
        assertThat(ttsBudget.usedToday("speech")).isEqualTo(((Number) audio.get("characters")).longValue());

        LoggedRequest speech = TTS.findAll(postRequestedFor(urlPathEqualTo("/v1/audio/speech"))).getFirst();
        assertThat(speech.getHeader("Authorization")).isEqualTo("Bearer sk-test-not-a-key");
        JsonNode body = json(speech.getBodyAsString());
        assertThat(body.get("model").asString()).isEqualTo("kokoro");
        assertThat(body.get("voice").asString()).isEqualTo("af_bella");
        assertThat(body.get("response_format").asString()).isEqualTo("mp3");
        assertThat(body.get("input").asString()).startsWith("Morning audio").contains("Tokyo Bay")
                .doesNotContain("https://").doesNotContain("](");
        assertThat(body.get("input").asString().length()).isLessThanOrEqualTo(4000);

        Map<String, Object> stored = BriefingViews.run(runRepository.findById(UUID.fromString((String) run.get("id")))
                .orElseThrow());
        assertThat((Map<String, Object>) stored.get("audio")).containsEntry("mediaId", mediaId);
    }

    @Test
    @DisplayName("Telegram receives the audio with sendAudio (multipart, captioned) instead of a text message")
    void telegramAttachment() throws Exception {
        speechOk();
        quake("q1", "Tokyo Bay", 5.5);
        Map<String, Object> run = briefings.runNow(audioBriefing("mp3"));
        assertThat(run.get("deliveries")).isEqualTo(1);

        worker.runOnce();

        List<LoggedRequest> sent = TELEGRAM.findAll(postRequestedFor(urlEqualTo("/bot" + TOKEN + "/sendAudio")));
        assertThat(sent).hasSize(1);
        String multipart = new String(sent.getFirst().getBody(), StandardCharsets.ISO_8859_1);
        assertThat(multipart).contains("name=\"audio\"").contains("filename=\"briefing.mp3\"")
                .contains("Content-Type: audio/mpeg").contains("name=\"caption\"").contains("Morning audio")
                .contains("name=\"chat_id\"");
        assertThat(notify.deliveries(telegram)).singleElement()
                .satisfies(d -> assertThat(d.getStatus().name()).isEqualTo("DELIVERED"));
    }

    @Test
    @DisplayName("the podcast RSS lists the run with an enclosure to its media (url, length, type)")
    @SuppressWarnings("unchecked")
    void podcastEnclosure() throws Exception {
        speechOk();
        quake("q1", "Tokyo Bay", 5.5);
        UUID id = audioBriefing("mp3");
        Map<String, Object> run = briefings.runNow(id);
        String mediaId = (String) ((Map<String, Object>) run.get("audio")).get("mediaId");

        MvcResult result = http.perform(BriefingHttp.request("GET", "/v1/briefings/" + id + "/podcast.rss", reader,
                null), 200);

        assertThat(result.getResponse().getContentType()).startsWith("application/rss+xml");
        String etag = result.getResponse().getHeader(HttpHeaders.ETAG);
        assertThat(etag).isNotBlank();
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(
                result.getResponse().getContentAsByteArray()));
        assertThat(doc.getDocumentElement().getTagName()).isEqualTo("rss");
        assertThat(doc.getDocumentElement().getAttribute("version")).isEqualTo("2.0");
        assertThat(doc.getElementsByTagName("item").getLength()).isEqualTo(1);
        Element enclosure = (Element) doc.getElementsByTagName("enclosure").item(0);
        assertThat(enclosure.getAttribute("url")).isEqualTo("https://fs.example/v1/media/" + mediaId);
        assertThat(enclosure.getAttribute("length")).isEqualTo(Integer.toString(MP3.length));
        assertThat(enclosure.getAttribute("type")).isEqualTo("audio/mpeg");
        assertThat(doc.getElementsByTagName("guid").item(0).getTextContent()).endsWith("/briefing-run/" + run.get("id"));

        http.perform(BriefingHttp.request("GET", "/v1/briefings/" + id + "/podcast.rss", reader, null)
                .header(HttpHeaders.IF_NONE_MATCH, etag), 304);
        http.perform(BriefingHttp.request("GET", "/v1/briefings/" + UUID.randomUUID() + "/podcast.rss", reader, null),
                404);
    }

    @Test
    @DisplayName("a speech failure is recorded on the run and the text is still delivered (sendMessage, no audio)")
    @SuppressWarnings("unchecked")
    void failureKeepsTextDelivery() throws Exception {
        TTS.stubFor(post(urlPathEqualTo("/v1/audio/speech")).willReturn(aResponse().withStatus(500)
                .withHeader("Content-Type", "application/json").withBody("{\"error\":{\"message\":\"voice crashed\"}}")));
        quake("q1", "Tokyo Bay", 5.5);
        UUID id = audioBriefing("mp3");

        Map<String, Object> run = briefings.runNow(id);

        assertThat(run.get("status")).isEqualTo("succeeded");
        Map<String, Object> audio = (Map<String, Object>) run.get("audio");
        assertThat(audio).containsEntry("status", "failed").containsEntry("mediaId", null);
        assertThat((String) audio.get("error")).contains("HTTP 500");
        assertThat(run.get("deliveries")).isEqualTo(1);
        assertThat(ttsBudget.usedToday("speech")).as("characters given back").isZero();

        worker.runOnce();

        TELEGRAM.verify(1, postRequestedFor(urlEqualTo("/bot" + TOKEN + "/sendMessage"))
                .withRequestBody(containing("Tokyo Bay")));
        TELEGRAM.verify(0, postRequestedFor(urlEqualTo("/bot" + TOKEN + "/sendAudio")));
        MvcResult rss = http.perform(BriefingHttp.request("GET", "/v1/briefings/" + id + "/podcast.rss", reader, null),
                200);
        assertThat(rss.getResponse().getContentAsString(StandardCharsets.UTF_8)).doesNotContain("<item>");
    }

    @Test
    @DisplayName("without a tts provider the run records audio skipped and is delivered as text")
    @SuppressWarnings("unchecked")
    void skippedWithoutProvider() {
        llmSettings.update(LlmSettings.DEFAULTS);
        quake("q1", "Tokyo Bay", 5.5);

        Map<String, Object> run = briefings.runNow(audioBriefing("opus"));

        Map<String, Object> audio = (Map<String, Object>) run.get("audio");
        assertThat(audio).containsEntry("status", "skipped").containsEntry("format", "opus");
        assertThat((String) audio.get("error")).contains("ttsProvider");
        assertThat(run.get("deliveries")).isEqualTo(1);
        TTS.verify(0, postRequestedFor(urlPathEqualTo("/v1/audio/speech")));
    }

    @Test
    @DisplayName("a briefing without the audio option has audio null and calls no speech server")
    void noOption() {
        speechOk();
        quake("q1", "Tokyo Bay", 5.5);
        UUID id = id(briefings.create(json(digestBody("Plain", "0 7 * * *", null)), agentCaller()));

        Map<String, Object> run = briefings.runNow(id);

        assertThat(run.get("audio")).isNull();
        TTS.verify(0, postRequestedFor(urlPathEqualTo("/v1/audio/speech")));
    }

    @Test
    @DisplayName("the tts role needs an openai-compatible provider; roles list tts")
    void roleValidation() {
        providers.create(json("{\"id\":\"local-tts\",\"type\":\"ollama\",\"baseUrl\":\"http://127.0.0.1:11434\","
                + "\"chatModel\":\"m\"}"));
        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> llmSettings.update(LlmSettings.DEFAULTS.withTtsProvider("local-tts")))
                .isInstanceOf(ai.foresignal.api.error.ValidationException.class);
        assertThat(llmSettings.roles("speech")).containsExactly("tts");
    }
}
