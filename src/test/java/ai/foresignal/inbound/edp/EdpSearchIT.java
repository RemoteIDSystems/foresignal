package ai.foresignal.inbound.edp;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.collect.BackoffRegistry;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.inbound.EdpConformance;
import ai.foresignal.testsupport.inbound.InboundIt;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.node.ObjectNode;

/**
 * A provider's {@code transport.search} against a WireMock endpoint (OpenAPI webhook {@code edpProviderSearch},
 * prompt 137): the query parameters, the header credential, the records stored as cue results, and the request
 * budget. The provider is on loopback, so it is registered with {@code allowPrivateNetwork}.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
class EdpSearchIT extends WireMockSupport {

    static final Instant T = Instant.parse("2026-10-11T18:00:00Z");
    static final String ID = "ext-river-gauges";
    static final String CUE = "01JA8Q3V7S2K9W4T6Y1B5N0M3P";

    @Autowired org.springframework.test.web.servlet.MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired AppSettingService settings;
    @Autowired MutableClock clock;
    @Autowired BackoffRegistry backoff;
    @Autowired EdpSearchService search;
    @Autowired JdbcTemplate jdbc;

    String admin;
    InboundIt it;

    @BeforeEach
    void setUp() throws Exception {
        clock.setInstant(T);
        settings.invalidateCache();
        admin = "Bearer " + adminTokens.rotate();
        it = new InboundIt(mvc, admin);
        ObjectNode manifest = (ObjectNode) InboundIt.JSON.readTree(EdpConformance.manifest("pull-atomic.json"));
        ObjectNode transport = (ObjectNode) manifest.get("transport");
        ((ObjectNode) transport.get("pull")).put("itemsUrl", url("/edp/items"));
        ObjectNode searchNode = transport.putObject("search");
        searchNode.put("url", url("/edp/search"));
        searchNode.put("auth", transport.get("pull").path("auth").asString("none"));
        if (transport.get("pull").hasNonNull("header")) {
            searchNode.put("header", transport.get("pull").get("header").asString());
        }
        MockHttpServletResponse registered = it.perform(post("/v1/external/sources")
                .header(HttpHeaders.AUTHORIZATION, admin).contentType(MediaType.APPLICATION_JSON)
                .content("{\"manifest\":" + manifest + ",\"approve\":true,\"allowPrivateNetwork\":true}"));
        assertThat(registered.getStatus()).as(registered.getContentAsString()).isEqualTo(201);
        assertThat(it.perform(put("/v1/sources/" + ID + "/credentials").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"values\":{\"value\":\"river-secret\"}}"))
                .getStatus()).isIn(200, 204);
        backoff.onSuccess(ID);
    }

    static EdpSearchService.Search cue(String... texts) {
        List<EdpSearchService.Query> queries = java.util.Arrays.stream(texts)
                .map(t -> new EdpSearchService.Query(t, "en")).toList();
        return new EdpSearchService.Search(CUE, queries, T.minusSeconds(3600), T, List.of(-96.0, 29.0, -95.0, 30.0),
                25);
    }

    @Test
    @DisplayName("a cue search sends the window, area, language and limit with the credential; items are cue results")
    void cueResults() {
        WIREMOCK.stubFor(get(urlPathEqualTo("/edp/search")).willReturn(okJson(EdpPullIT.page(null, null, "s1", "s2"))));

        List<EdpSearchService.Result> results = search.search(cue("flood gauge"));

        assertThat(results).containsExactly(new EdpSearchService.Result(ID, 1, 2, null));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/edp/search"))
                .withQueryParam("q", equalTo("flood gauge"))
                .withQueryParam("since", equalTo("2026-10-11T17:00:00Z"))
                .withQueryParam("until", equalTo("2026-10-11T18:00:00Z"))
                .withQueryParam("bbox", equalTo("-96.0,29.0,-95.0,30.0"))
                .withQueryParam("lang", equalTo("en"))
                .withQueryParam("limit", equalTo("25")));
        assertThat(WIREMOCK.findAll(getRequestedFor(urlPathEqualTo("/edp/search"))).getFirst().getHeaders().all()
                .stream().anyMatch(h -> h.values().contains("river-secret") || h.values().contains("Bearer river-secret")))
                .isTrue();
        assertThat(jdbc.queryForList("select payload->>'collection' || '/' || (payload->>'cueId') from record_head "
                + "where source_id = ? order by external_id", String.class, ID))
                .containsExactly("cue/" + CUE, "cue/" + CUE);
    }

    @Test
    @DisplayName("the request budget stops a provider's queries for the cue with BUDGET_EXCEEDED")
    void budget() throws Exception {
        assertThat(it.perform(put("/v1/sources/" + ID).header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"budget\":{\"perMinute\":1,\"perDay\":10}}"))
                .getStatus()).isEqualTo(200);
        WIREMOCK.stubFor(get(urlPathEqualTo("/edp/search")).willReturn(okJson(EdpPullIT.page(null, null, "s1"))));

        List<EdpSearchService.Result> results = search.search(cue("one", "two", "three"));

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().queries()).isEqualTo(1);
        assertThat(results.getFirst().error()).startsWith("BUDGET_EXCEEDED");
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/edp/search")));
    }

    @Test
    @DisplayName("a suspended provider is not searched")
    void suspendedIsNotSearched() throws Exception {
        it.perform(post("/v1/external/sources/" + ID + "/suspend").header(HttpHeaders.AUTHORIZATION, admin));
        WIREMOCK.stubFor(get(urlPathEqualTo("/edp/search")).willReturn(okJson(EdpPullIT.page(null, null, "s1"))));

        assertThat(search.search(cue("flood"))).isEmpty();
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/edp/search")));
    }
}
