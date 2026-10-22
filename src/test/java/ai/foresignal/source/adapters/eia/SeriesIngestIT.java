package ai.foresignal.source.adapters.eia;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.util.List;
import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * End-to-end energy-series ingest (prompt 093): a revised EIA value is version 2 and updates {@code series-latest};
 * an unchanged re-poll adds nothing; {@code /v1/series} lists the catalog and one series' points come back in
 * period order and as CSV; a private source's series are hidden from callers without the grant and from a restricted
 * token.
 */
class SeriesIngestIT extends WeatherIngestSupport {

    static final String SPOT = "/v2/petroleum/pri/spt/data/";
    static final String STOCKS = "/v2/petroleum/stoc/wstk/data/";

    @Autowired MockMvc mvc;
    @Autowired SourceVisibility visibility;
    @Autowired TokenService tokens;

    private static Map<String, Object> params() {
        return Map.of(EiaPetroleumAdapter.SPOT_PARAM, List.of("RWTC"), EiaPetroleumAdapter.STOCK_PARAM,
                List.of("WCESTUS1"));
    }

    private static void serveSpot(String value) {
        String body = "{\"response\":{\"total\":\"2\",\"frequency\":\"daily\",\"data\":["
                + "{\"period\":\"2026-10-09\",\"series\":\"RWTC\",\"value\":\"" + value + "\",\"units\":\"$ per barrel\"},"
                + "{\"period\":\"2026-10-08\",\"series\":\"RWTC\",\"value\":\"60.98\",\"units\":\"$ per barrel\"}],"
                + "\"warnings\":[]}}";
        UPSTREAM.stubFor(get(urlPathEqualTo(SPOT)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(body)));
    }

    private static String stockBody() {
        return "{\"response\":{\"total\":\"1\",\"frequency\":\"weekly\",\"data\":[{\"period\":\"2026-10-02\","
                + "\"series\":\"WCESTUS1\",\"value\":\"415234\",\"units\":\"thousand barrels\"}],\"warnings\":[]}}";
    }

    @Test
    @DisplayName("a revised value is version 2 and updates series-latest; an unchanged re-poll adds none")
    void revision() throws Exception {
        sources.setCredentials(EiaPetroleumAdapter.ID, Map.of(EiaApi.API_KEY, "eia-it-key"));
        configure(EiaPetroleumAdapter.ID, true, params());
        UPSTREAM.stubFor(get(urlPathEqualTo(STOCKS)).willReturn(aResponse().withStatus(200).withBody(stockBody())));
        serveSpot("61.23");
        fetch(EiaPetroleumAdapter.ID);
        assertThat(versions(EiaPetroleumAdapter.ID, "eia:RWTC:2026-10-09")).isEqualTo(1);

        // A revision of the newest point becomes version 2 and moves the latest record.
        UPSTREAM.resetRequests();
        serveSpot("62.00");
        fetch(EiaPetroleumAdapter.ID);
        assertThat(versions(EiaPetroleumAdapter.ID, "eia:RWTC:2026-10-09")).isEqualTo(2);
        assertThat(versionValue("eia-petroleum", "eia:RWTC:latest")).isEqualTo(62.00);

        // The same body again adds no version.
        int before = versions(EiaPetroleumAdapter.ID, (ai.foresignal.ingest.PersistReason) null);
        fetch(EiaPetroleumAdapter.ID);
        assertThat(versions(EiaPetroleumAdapter.ID, (ai.foresignal.ingest.PersistReason) null)).isEqualTo(before);

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/v1/series"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"series\":\"eia:RWTC\"")));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/v1/series/eia-petroleum/eia:RWTC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].period").value("2026-10-08"))
                .andExpect(jsonPath("$.items[1].period").value("2026-10-09"))
                .andExpect(jsonPath("$.items[1].value").value(62.00));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/v1/series/eia-petroleum/eia:RWTC").param("format", "csv"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("period,periodStart,periodEnd")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("2026-10-09")));

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/v1/series/eia-petroleum/no-such-series"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("a private source's series are hidden from anonymous callers and a restricted token, shown with the grant")
    void privateAndRestricted() throws Exception {
        sources.setCredentials(EiaPetroleumAdapter.ID, Map.of(EiaApi.API_KEY, "eia-it-key"));
        configure(EiaPetroleumAdapter.ID, true, params());
        UPSTREAM.stubFor(get(urlPathEqualTo(STOCKS)).willReturn(aResponse().withStatus(200).withBody(stockBody())));
        serveSpot("61.23");
        fetch(EiaPetroleumAdapter.ID);
        String points = "/v1/series/eia-petroleum/eia:RWTC";

        visibility.setOperatorPrivate(EiaPetroleumAdapter.ID, true);
        try {
            // Anonymous (openReads): absent from the catalog; naming the source is a 404 like an unknown one.
            mvc.perform(MockMvcRequestBuilders.get("/v1/series"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(Matchers.not(Matchers.containsString("eia:RWTC"))));
            mvc.perform(MockMvcRequestBuilders.get("/v1/series").param("source", EiaPetroleumAdapter.ID))
                    .andExpect(status().isNotFound());
            mvc.perform(MockMvcRequestBuilders.get(points)).andExpect(status().isNotFound());

            // A token with the private grant sees it.
            String granted = TestTokens.bearer(TestTokens.create(tokens, "series-private", Scope.READ, true));
            mvc.perform(MockMvcRequestBuilders.get(points).header("Authorization", granted))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items[0].period").value("2026-10-08"));
        } finally {
            visibility.setOperatorPrivate(EiaPetroleumAdapter.ID, false);
        }

        // A token restricted to another source sees no EIA series, in the catalog or by id.
        String restricted = TestTokens.bearer(TestTokens.create(tokens, "series-usgs-only", Scope.READ,
                new TokenRestrictions(List.of("usgs-earthquakes"), null, null, null), null));
        mvc.perform(MockMvcRequestBuilders.get("/v1/series").header("Authorization", restricted))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.not(Matchers.containsString("eia:RWTC"))));
        mvc.perform(MockMvcRequestBuilders.get(points).header("Authorization", restricted))
                .andExpect(status().isNotFound());
        // A spatial restriction hides series too: they carry no geometry (fail closed).
        String boxed = TestTokens.bearer(TestTokens.create(tokens, "series-boxed", Scope.READ,
                new TokenRestrictions(null, null, List.of(-1.0, -1.0, 1.0, 1.0), null), null));
        mvc.perform(MockMvcRequestBuilders.get(points).header("Authorization", boxed))
                .andExpect(status().isNotFound());
        // Unrestricted, it is there.
        mvc.perform(MockMvcRequestBuilders.get(points)).andExpect(status().isOk());
    }

    /** The {@code value} in the latest head payload of one series. */
    private Double versionValue(String sourceId, String externalId) {
        String json = jdbc.queryForObject("select payload::text from record_head where source_id = ? and external_id = ?",
                String.class, sourceId, externalId);
        assertThat(json).isNotNull();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"value\"\\s*:\\s*([0-9.]+)").matcher(json);
        assertThat(m.find()).isTrue();
        return Double.parseDouble(m.group(1));
    }
}
