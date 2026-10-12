package ai.foresignal.inbound.edp;

import static ai.foresignal.testsupport.inbound.InboundIt.body;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.collect.BackoffRegistry;
import ai.foresignal.collect.FetchExecutor;
import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.store.FetchTrigger;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.inbound.EdpConformance;
import ai.foresignal.testsupport.inbound.InboundIt;
import java.time.Instant;
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
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.node.ObjectNode;

/**
 * A pull provider against a WireMock items endpoint (prompt 024, EDP §4.2): cursor paging, the stored
 * cursor, the request budget, the header auth style with the operator's credential, and provider
 * errors as health. The provider is on loopback, so it is registered with
 * {@code allowPrivateNetwork}.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
class EdpPullIT extends WireMockSupport {

    static final Instant T = Instant.parse("2026-10-11T18:00:00Z");
    static final String ID = "ext-river-gauges";

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired AppSettingService settings;
    @Autowired MutableClock clock;
    @Autowired FetchExecutor executor;
    @Autowired BackoffRegistry backoff;
    @Autowired SourceRegistry registry;
    @Autowired SourceService sources;
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
        ((ObjectNode) manifest.get("transport").get("pull")).put("itemsUrl", url("/edp/items"));
        MockHttpServletResponse registered = it.perform(post("/v1/external/sources").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"manifest\":" + manifest + ",\"approve\":true,\"allowPrivateNetwork\":true}"));
        assertThat(registered.getStatus()).as(registered.getContentAsString()).isEqualTo(201);
        assertThat(it.perform(put("/v1/sources/" + ID + "/credentials").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"values\":{\"value\":\"river-secret\"}}")).getStatus())
                .isIn(200, 204);
        backoff.onSuccess(ID);
    }

    static String page(String cursor, String next, String... ids) {
        StringBuilder items = new StringBuilder();
        for (String id : ids) {
            if (items.length() > 0) {
                items.append(',');
            }
            items.append("{\"stream\":\"levels\",\"type\":\"observation\",\"id\":\"").append(id)
                    .append("\",\"observedAt\":\"2026-10-11T17:00:00Z\",\"seriesKey\":\"").append(id)
                    .append("\",\"t\":\"2026-10-11\",\"value\":1.5}");
        }
        return "{\"protocol\":\"foresignal-edp/1.0\",\"sourceId\":\"river-gauges\",\"items\":[" + items + "],"
                + "\"nextCursor\":" + (next == null ? "null" : "\"" + next + "\"") + "}";
    }

    @Test
    @DisplayName("a pull follows nextCursor with the header credential, stores the cursor, and ingests every page")
    void cursorPaging() {
        assertThat(registry.descriptor(ID).mode()).isEqualTo(Mode.POLLING);
        WIREMOCK.stubFor(get(urlPathEqualTo("/edp/items")).withQueryParam("cursor", absent())
                .willReturn(okJson(page(null, "c2", "g1", "g2"))));
        WIREMOCK.stubFor(get(urlPathEqualTo("/edp/items")).withQueryParam("cursor", equalTo("c2"))
                .willReturn(okJson(page("c2", null, "g3"))));

        FetchOutcome outcome = executor.execute(ID, FetchTrigger.MANUAL);

        assertThat(outcome.success()).as(String.valueOf(outcome.error())).isTrue();
        assertThat(outcome.recordsSeen()).isEqualTo(3);
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo("/edp/items")).withHeader("X-Api-Key", equalTo("river-secret"))
                .withQueryParam("limit", equalTo("500")));
        assertThat(jdbc.queryForObject("select pull_cursor from external_source where source_id = ?", String.class, ID))
                .isEqualTo("c2");
        assertThat(jdbc.queryForList("select external_id from record_head where source_id = ? order by external_id",
                String.class, ID)).containsExactly("levels:g1", "levels:g2", "levels:g3");

        // The next run resumes from the stored cursor.
        WIREMOCK.resetRequests();
        executor.execute(ID, FetchTrigger.MANUAL);
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/edp/items")).withQueryParam("cursor", equalTo("c2")));
    }

    @Test
    @DisplayName("the request budget ends a pull early; the remaining pages wait for the next run")
    void budget() throws Exception {
        assertThat(it.perform(put("/v1/sources/" + ID).header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"budget\":{\"perMinute\":1,\"perDay\":10}}"))
                .getStatus()).isEqualTo(200);
        WIREMOCK.stubFor(get(urlPathEqualTo("/edp/items")).withQueryParam("cursor", absent())
                .willReturn(okJson(page(null, "c2", "g1"))));
        WIREMOCK.stubFor(get(urlPathEqualTo("/edp/items")).withQueryParam("cursor", equalTo("c2"))
                .willReturn(okJson(page("c2", null, "g2"))));

        executor.execute(ID, FetchTrigger.MANUAL);

        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/edp/items")));
        assertThat(jdbc.queryForObject("select pull_cursor from external_source where source_id = ?", String.class, ID))
                .isEqualTo("c2");
    }

    @Test
    @DisplayName("a provider error fails the run: health turns degraded and the error is kept on the provider")
    void providerErrors() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo("/edp/items")).willReturn(aResponse().withStatus(503).withBody("maintenance")));

        FetchOutcome outcome = executor.execute(ID, FetchTrigger.MANUAL);

        assertThat(outcome.success()).isFalse();
        assertThat(sources.view(ID).health().status()).isIn(HealthStatus.DEGRADED, HealthStatus.FAILING);
        assertThat(jdbc.queryForObject("select last_pull_error from external_source where source_id = ?", String.class, ID))
                .contains("503");
        assertThat(body(it.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/v1/external/sources/" + ID).header(HttpHeaders.AUTHORIZATION, admin))).get("pull").get("lastError")
                .asString()).contains("503");
    }

    @Test
    @DisplayName("a suspended provider is not pulled")
    void suspendedIsNotPulled() throws Exception {
        it.perform(post("/v1/external/sources/" + ID + "/suspend").header(HttpHeaders.AUTHORIZATION, admin));
        WIREMOCK.stubFor(get(urlPathEqualTo("/edp/items")).willReturn(okJson(page(null, null, "g1"))));

        executor.execute(ID, FetchTrigger.MANUAL);

        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/edp/items")));
    }
}
