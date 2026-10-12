package ai.foresignal.llm;

import static ai.foresignal.testsupport.llm.FakeLlmClient.call;
import static ai.foresignal.testsupport.llm.FakeLlmClient.text;
import static ai.foresignal.testsupport.llm.FakeLlmClient.toolUse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import ai.foresignal.testsupport.enrichment.EnrichmentItSupport;
import ai.foresignal.testsupport.enrichment.EnrichmentTestSources;
import ai.foresignal.trigger.EnrichmentService;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * LLM egress of private records (prompt 039, EGR-1) with the scripted {@code FakeLlmClient}: no private
 * text ever reaches a remote provider in Ask or enrichment; a local provider (Ollama on a loopback
 * address) receives it only when {@code llm.allowPrivateLocal} is true, and in Ask only for a caller that
 * may see private sources. Briefings are covered in the briefing context ({@code LlmBriefingIT}), which
 * cannot share this one (both declare a primary {@code LlmClientFactory} variant).
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(EnrichmentTestSources.class)
class LlmEgressPrivateIT extends EnrichmentItSupport {

    static final String MARKER = "Hidden Family Home";

    @Autowired MockMvc mvc;

    @AfterEach
    void privateLocalOff() {
        allowPrivateLocal(false);
    }

    private void ask(String bearer) throws Exception {
        MvcResult result = mvc.perform(post("/v1/ask").contentType(MediaType.APPLICATION_JSON)
                .content("{\"question\":\"Everything you know?\"}").header(HttpHeaders.AUTHORIZATION, bearer))
                .andReturn();
        assertThat(result.getResponse().getStatus()).as(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .isEqualTo(200);
    }

    private void scriptReadAll(ai.foresignal.testsupport.llm.FakeLlmClient client) {
        client.then(toolUse(call("c1", "query_records", "{}"),
                        call("c2", "get_record", "{\"source\":\"" + AskTestSources.SECRET + "\",\"externalId\":\"s1\"}"),
                        call("c3", "search", "{\"q\":\"family\"}")))
                .then(text("Done."));
    }

    @Test
    @DisplayName("Ask: remote never; local only with allowPrivateLocal and a caller that sees private sources")
    void ask() throws Exception {
        secret("s1", MARKER, 10.0, 50.0);
        quake("q1", "Tokyo Bay", 5.5, 139.8, 35.6);

        allowPrivateLocal(true);
        scriptReadAll(chat());
        ask(privateAgent);
        assertThat(everythingSent(chat().requests())).as("remote provider").doesNotContain(MARKER);

        provider(LOCAL, "ollama", 1_000_000L);
        assign(LOCAL, false);
        scriptReadAll(fakes.client(LOCAL));
        ask(privateAgent);
        assertThat(everythingSent(fakes.client(LOCAL).requests())).as("local, setting off").doesNotContain(MARKER);

        assign(LOCAL, true);
        int before = fakes.client(LOCAL).requests().size();
        scriptReadAll(fakes.client(LOCAL));
        ask(privateAgent);
        assertThat(everythingSent(fakes.client(LOCAL).requests().subList(before, fakes.client(LOCAL).requests().size())))
                .as("local, setting on, private grant").contains(MARKER);
    }

    @Test
    @DisplayName("enrichment: a private record near the alert never reaches a remote provider")
    void enrichmentRemote() {
        enrichmentSettings(CHAT, true);
        allowPrivateLocal(true);
        UUID rule = enrichRule("Egress remote", 50, "1h");
        records.ingestAt(AskTestSources.SECRET, minutesAgo(3), NormalizedRecord.builder("s-near", AskTestSources.QUAKE_KIND)
                .observedAt(minutesAgo(3)).put("place", MARKER).put("mag", 1.0).point(LON + 0.01, LAT).build());
        quakeAt("q-egress", "Tokyo Bay", 5.5, T, LON, LAT);
        AlertView alert = alertFor(rule, "q-egress");
        chat().then(request -> text("A quake near Tokyo Bay [alert:" + alert.id() + "]."));

        EnrichmentService.Outcome outcome = enrichment.enrich(alert);

        assertThat(outcome.status()).isEqualTo(EnrichmentService.Outcome.SUCCEEDED);
        assertThat(everythingSent(chat().requests())).doesNotContain(MARKER).doesNotContain(AskTestSources.SECRET);
    }

    private String enrichLocally(boolean allowPrivate, String suffix) {
        provider(LOCAL, "ollama", 1_000_000L);
        enrichmentSettings(LOCAL, true);
        allowPrivateLocal(allowPrivate);
        UUID rule = enrichRule("Egress local " + suffix, 50, "1h");
        records.ingestAt(AskTestSources.SECRET, minutesAgo(3), NormalizedRecord.builder("s-" + suffix,
                AskTestSources.QUAKE_KIND).observedAt(minutesAgo(3)).put("place", MARKER).put("mag", 1.0)
                .point(LON + 0.01, LAT).build());
        quakeAt("q-" + suffix, "Tokyo Bay", 5.5, T, LON, LAT);
        AlertView alert = alertFor(rule, "q-" + suffix);
        fakes.client(LOCAL).otherwise(request -> text("Noted [alert:" + alert.id() + "]."));
        enrichment.enrich(alert);
        return everythingSent(fakes.client(LOCAL).requests());
    }

    @Test
    @DisplayName("enrichment: a local provider does not get the private record while allowPrivateLocal is off")
    void enrichmentLocalOff() {
        assertThat(enrichLocally(false, "off")).doesNotContain(MARKER);
    }

    @Test
    @DisplayName("enrichment: a local provider gets the private record with allowPrivateLocal")
    void enrichmentLocalOn() {
        assertThat(enrichLocally(true, "on")).contains(MARKER);
    }
}
