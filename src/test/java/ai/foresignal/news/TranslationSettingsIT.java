package ai.foresignal.news;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.llm.client.ChatMessage;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskItSupport;
import ai.foresignal.testsupport.ask.AskTestSources;
import ai.foresignal.testsupport.llm.FakeLlmClient;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;

/**
 * {@code getTranslationSettings} / {@code updateTranslationSettings} and translation at ingest end to end (prompt
 * NP-002): the settings round-trip with defaults, canonical tags and one 400 for every problem; they are admin-only;
 * an enabled target language makes {@link NewsTranslationJob} translate a foreign article with the scripted provider,
 * and {@code listNewsArticles} then carries the translation beside the untouched original; disabling translation
 * leaves the originals only.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(AskTestSources.class)
class TranslationSettingsIT extends AskItSupport {

    @Autowired MockMvc mvc;
    @Autowired NewsStoryStore stories;
    @Autowired NewsTranslationJob job;

    @BeforeEach
    void clean() {
        jdbc.update("DELETE FROM news_translation");
        jdbc.update("DELETE FROM news_article");
    }

    MvcResult call(MockHttpServletRequestBuilder request, String bearer) throws Exception {
        if (bearer != null) {
            request.header(HttpHeaders.AUTHORIZATION, bearer);
        }
        return mvc.perform(request).andReturn();
    }

    JsonNode body(MvcResult result) throws Exception {
        return JSON.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    MvcResult putSettings(String body, String bearer) throws Exception {
        return call(put("/v1/settings/translation").contentType(MediaType.APPLICATION_JSON)
                .content(body.replace('\'', '"')), bearer);
    }

    @Test
    @DisplayName("GET before any PUT: off, no targets, title and excerpt, llm, a day window with no cap")
    void defaults() throws Exception {
        MvcResult r = call(get("/v1/settings/translation"), admin);
        assertThat(r.getResponse().getStatus()).isEqualTo(200);
        JsonNode s = body(r);
        assertThat(s.get("enabled").asBoolean()).isFalse();
        assertThat(s.get("targetLanguages")).isEmpty();
        assertThat(s.get("fields").toString()).isEqualTo("[\"title\",\"excerpt\"]");
        assertThat(s.get("engine").asString()).isEqualTo("llm");
        assertThat(s.at("/budget/window").asString()).isEqualTo("day");
        assertThat(s.at("/budget/capUsd").isNull()).isTrue();
        assertThat(s.at("/spend/window").asString()).isEqualTo("day");
        assertThat(s.at("/spend/exhausted").asBoolean()).isFalse();
    }

    @Test
    @DisplayName("PUT round-trips, canonicalizing and deduplicating the tags; the read-only spend is ignored")
    void roundTrip() throws Exception {
        MvcResult r = putSettings("{'enabled':true,'targetLanguages':['en','pt-br','EN'],'fields':['title'],"
                + "'budget':{'window':'month','capUsd':5},'minSourceLanguageConfidence':0.5,'spend':{}}", admin);
        assertThat(r.getResponse().getStatus()).as(r.getResponse().getContentAsString()).isEqualTo(200);

        JsonNode s = body(call(get("/v1/settings/translation"), admin));
        assertThat(s.get("enabled").asBoolean()).isTrue();
        assertThat(s.get("targetLanguages").toString()).isEqualTo("[\"en\",\"pt-BR\"]");
        assertThat(s.get("fields").toString()).isEqualTo("[\"title\"]");
        assertThat(s.at("/budget/window").asString()).isEqualTo("month");
        assertThat(s.at("/budget/capUsd").decimalValue()).isEqualByComparingTo("5");
        assertThat(s.get("minSourceLanguageConfidence").asDouble()).isEqualTo(0.5);
        assertThat(s.at("/spend/capUsd").decimalValue()).isEqualByComparingTo("5");
    }

    @Test
    @DisplayName("every problem in one 400: missing enabled, bad tag, unknown field, bad engine, window, cap, confidence")
    void validation() throws Exception {
        MvcResult r = putSettings("{'targetLanguages':['not a tag'],'fields':['body'],'engine':'deepl',"
                + "'budget':{'window':'week','capUsd':-1},'minSourceLanguageConfidence':2,'extra':1}", admin);
        assertThat(r.getResponse().getStatus()).isEqualTo(400);
        String text = r.getResponse().getContentAsString(StandardCharsets.UTF_8);
        for (String field : List.of("enabled", "targetLanguages[0]", "fields", "engine", "budget.window",
                "budget.capUsd", "minSourceLanguageConfidence", "extra")) {
            assertThat(text).contains("\"" + field + "\"");
        }
    }

    @Test
    @DisplayName("admin only: a read token gets 403, no token 401")
    void adminOnly() throws Exception {
        assertThat(call(get("/v1/settings/translation"), reader).getResponse().getStatus()).isEqualTo(403);
        assertThat(putSettings("{'enabled':false}", reader).getResponse().getStatus()).isEqualTo(403);
        assertThat(call(get("/v1/settings/translation"), null).getResponse().getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("enabled: a German article is translated at ingest and served beside the original; disabled: originals only")
    void endToEnd() throws Exception {
        stories.upsertArticle(new NewsStoryStore.Article("gdelt-articles/de-1", "gdelt-articles", "de-1", 1, null,
                null, null, null, null, "example.de", "Erdbeben in Bayern", List.of("erdbeben", "bayern"),
                "https://example.de/1", "de", "DE", clock.instant(), clock.instant(), List.of(), List.of(),
                "Starkes Beben bei München", null, false, null, null, null, clock.instant()));
        FakeLlmClient chat = chat();
        chat.otherwise(r -> FakeLlmClient.text(((ChatMessage.User) r.messages().getFirst()).text().contains("Erdbeben")
                ? "Earthquake in Bavaria" : "Strong quake near Munich"));
        assertThat(putSettings("{'enabled':true,'targetLanguages':['en']}", admin).getResponse().getStatus())
                .isEqualTo(200);

        assertThat(job.run()).isEqualTo(1);
        assertThat(job.run()).as("unchanged content is not translated again").isZero();

        JsonNode item = body(call(get("/v1/news/articles"), admin)).get("items").get(0);
        assertThat(item.get("title").asString()).isEqualTo("Erdbeben in Bayern");
        assertThat(item.get("summary").asString()).isEqualTo("Starkes Beben bei München");
        assertThat(item.get("targetLanguage").asString()).isEqualTo("en");
        assertThat(item.get("translatedTitle").asString()).isEqualTo("Earthquake in Bavaria");
        assertThat(item.get("translatedExcerpt").asString()).isEqualTo("Strong quake near Munich");
        assertThat(item.at("/translationProvenance/engine").asString()).isEqualTo("llm");

        assertThat(putSettings("{'enabled':false,'targetLanguages':['en']}", admin).getResponse().getStatus())
                .isEqualTo(200);
        JsonNode original = body(call(get("/v1/news/articles"), admin)).get("items").get(0);
        assertThat(original.get("title").asString()).isEqualTo("Erdbeben in Bayern");
        assertThat(original.has("translatedTitle")).isFalse();
        assertThat(original.has("targetLanguage")).isFalse();
    }
}
