package ai.foresignal.weather;

import static ai.foresignal.testsupport.llm.FakeLlmClient.call;
import static ai.foresignal.testsupport.llm.FakeLlmClient.text;
import static ai.foresignal.testsupport.llm.FakeLlmClient.toolUse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.ask.AskCitation;
import ai.foresignal.ask.CitationSet;
import ai.foresignal.ask.CitationVerifier;
import ai.foresignal.llm.client.ChatRequest;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.sample.SampleService;
import ai.foresignal.raster.sample.SampleStatus;
import ai.foresignal.raster.sample.SampleValue;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskItSupport;
import ai.foresignal.testsupport.ask.AskTestSources;
import ai.foresignal.testsupport.raster.SampleFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;

/**
 * Ask with the weather tools and {@code FakeLlmClient} (prompt 056): the weather tools are in the shared catalog;
 * a weather value's {@code wx:} citation is verified (an invented one dropped); an answer that claims clear skies
 * while the radar value is {@code no-coverage} gets the standard caveat and a warning, one that states the status does
 * not; and the private gate reaches the weather tools (a non-local provider's excluded sources are passed to them).
 * The sampled values come from a spy on {@link SampleService}, so no frames are needed.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(AskTestSources.class)
class AskWeatherToolsIT extends AskItSupport {

    static final String PRODUCT = "mrms-reflectivity";
    static final String FRAME_KEY = "20261012T115800Z~0123abcd";
    static final String CITE = "[wx:" + PRODUCT + "@" + FRAME_KEY + "]";
    static final String SAMPLE = "{\"lon\":-97.75,\"lat\":30.27,\"products\":[\"" + PRODUCT + "\"]}";

    @Autowired MockMvc mvc;
    @MockitoSpyBean SampleService samples;

    @BeforeEach
    void weatherValues() {
        ProductRegistry.Registered product = new ProductRegistry.Registered(SampleFixtures.reflectivity(PRODUCT), "mrms",
                false);
        doReturn(List.of(product)).when(samples).resolveProducts(anyList(), anySet());
        doReturn(Instant.parse("2026-10-12T12:00:00Z")).when(samples).now();
    }

    void sampled(SampleStatus status) {
        Instant valid = Instant.parse("2026-10-12T11:58:00Z");
        SampleValue value = new SampleValue(PRODUCT, status.ok() ? 52.5 : null, null, null, null, "dBZ",
                status.ok() ? "52.5 dBZ" : null, FRAME_KEY, valid, "2026-10-12T06:58:00-05:00", 2.0, null, null,
                "nearest", "CONUS", status, status.ok() ? null : WeatherSections.NO_COVERAGE_TEXT, Map.of());
        doReturn(value).when(samples).value(any(), any(), any(), any(), anyBoolean(), any());
    }

    JsonNode ask(String bearer, String question) throws Exception {
        MvcResult result = mvc.perform(post("/v1/ask").contentType(MediaType.APPLICATION_JSON)
                .content("{\"question\":\"" + question + "\"}").header(HttpHeaders.AUTHORIZATION, bearer)).andReturn();
        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(result.getResponse().getStatus()).as(content).isEqualTo(200);
        return JSON.readTree(content);
    }

    @Test
    @DisplayName("the weather tools are offered; a wx citation is verified with its frame, an invented one dropped")
    void weatherCitationVerified() throws Exception {
        sampled(SampleStatus.OK);
        chat().then(toolUse(call("c1", "sample_weather", SAMPLE)))
                .then(text("Radar shows 52.5 dBZ, valid 11:58 UTC, 2 minutes old " + CITE + ". Also [wx:"
                        + PRODUCT + "@20990101T000000Z~deadbeef]."));

        JsonNode response = ask(reader, "What does the radar show at home?");

        ChatRequest first = chat().requests().getFirst();
        assertThat(first.tools()).extracting(t -> t.name())
                .contains("sample_weather", "weather_forecast", "area_weather_stats", "get_map_image");
        assertThat(first.system()).contains("[wx:...]").contains("unknown, never clear");
        JsonNode citation = response.get("citations").get(0);
        assertThat(citation.get("id").asString()).isEqualTo("wx:" + PRODUCT + "@" + FRAME_KEY);
        assertThat(citation.get("type").asString()).isEqualTo(AskCitation.WEATHER);
        assertThat(citation.get("productId").asString()).isEqualTo(PRODUCT);
        assertThat(citation.get("frameKey").asString()).isEqualTo(FRAME_KEY);
        assertThat(citation.get("validTime").asString()).isEqualTo("2026-10-12T11:58:00Z");
        assertThat(citation.get("ageMinutes").asDouble()).isEqualTo(2.0);
        assertThat(response.get("citations")).hasSize(1);
        assertThat(response.get("warnings").toString()).contains("dropped citation [wx:" + PRODUCT + "@20990101T000000Z")
                .doesNotContain(WeatherAnswerGuard.WARNING_PREFIX);
    }

    @Test
    @DisplayName("claiming clear skies while radar is no-coverage appends the caveat and a warning")
    void clearSkiesCaveat() throws Exception {
        sampled(SampleStatus.NO_COVERAGE);
        chat().then(toolUse(call("c1", "sample_weather", SAMPLE)))
                .then(text("Skies are clear at home " + CITE + "."));

        JsonNode response = ask(reader, "Is it raining at home?");

        String answer = response.get("answer").asString();
        assertThat(answer).startsWith("Skies are clear at home").contains(WeatherAnswerGuard.CAVEAT_PREFIX)
                .contains(PRODUCT + " (no-coverage)").endsWith("not official warnings.");
        assertThat(response.get("warnings").toString()).contains(WeatherAnswerGuard.WARNING_PREFIX)
                .contains(PRODUCT + " (no-coverage)");
    }

    @Test
    @DisplayName("an answer that states the no-coverage status gets no caveat")
    void statedStatusNoCaveat() throws Exception {
        sampled(SampleStatus.NO_COVERAGE);
        chat().then(toolUse(call("c1", "sample_weather", SAMPLE)))
                .then(text("Radar has no coverage at home, so rain there is unknown " + CITE + "."));

        JsonNode response = ask(reader, "Is it raining at home?");

        assertThat(response.get("answer").asString()).doesNotContain(WeatherAnswerGuard.CAVEAT_PREFIX);
        assertThat(response.get("warnings").toString()).doesNotContain(WeatherAnswerGuard.WARNING_PREFIX);
    }

    @Test
    @DisplayName("the private gate reaches the weather tools: a non-local provider's excluded sources are passed on")
    void privateGate() throws Exception {
        sampled(SampleStatus.OK);
        chat().then(toolUse(call("c1", "sample_weather", SAMPLE))).then(text("Radar 52.5 dBZ " + CITE + "."));

        ask(privateAgent, "What does the radar show?");

        verify(samples, atLeastOnce()).resolveProducts(anyList(),
                argThat(excluded -> excluded != null && excluded.contains(AskTestSources.SECRET)));
    }

    @Test
    @DisplayName("the guard and the verifier on a hand-built citation set")
    void guardUnit() {
        CitationSet set = new CitationSet();
        new WeatherCitationExtractor().extract("sample_weather", JSON.readTree("{\"values\":[{\"productId\":\"" + PRODUCT
                + "\",\"status\":\"stale\",\"statusText\":\"the frame is 20 min old\",\"frameKey\":\"" + FRAME_KEY
                + "\",\"citationId\":\"wx:" + PRODUCT + "@" + FRAME_KEY + "\",\"validTime\":\"2026-10-12T11:40:00Z\"}]}"),
                set);
        assertThat(set.hasWeather("wx:" + PRODUCT + "@" + FRAME_KEY)).isTrue();
        assertThat(WeatherAnswerGuard.unaddressed("It is calm.", set.weatherIssues())).hasSize(1);
        assertThat(WeatherAnswerGuard.unaddressed("The radar frame is stale.", set.weatherIssues())).isEmpty();
        assertThat(WeatherAnswerGuard.unaddressed("Note: the frame is 20 min old.", set.weatherIssues())).isEmpty();
        CitationVerifier.Verification verification = new CitationVerifier((id, seq) -> java.util.Optional.empty())
                .verify("Stale radar " + CITE + ".", set);
        assertThat(verification.citations()).singleElement()
                .satisfies(c -> assertThat(c.type()).isEqualTo(AskCitation.WEATHER));
    }
}
