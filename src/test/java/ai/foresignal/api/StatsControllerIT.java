package ai.foresignal.api;

import static ai.foresignal.testsupport.stats.StatsTestSources.POINTS;
import static ai.foresignal.testsupport.stats.StatsTestSources.POINT_KIND;
import static ai.foresignal.testsupport.stats.StatsTestSources.SECRET;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.stats.StatsItSupport;
import ai.foresignal.testsupport.stats.StatsTestSources;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The stats endpoints over HTTP (prompt 038): the {@code StatsSeries}, {@code StatsGrid} and
 * {@code StatsTop} documents, the refused region filters, hidden sources answered 404, and the admin
 * rebuild answered 202.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(StatsTestSources.class)
class StatsControllerIT extends StatsItSupport {

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;

    String admin;
    String reader;

    @BeforeEach
    void setUp() {
        admin = TestTokens.bearer(TestTokens.create(tokens, "stats-admin", Scope.ADMIN));
        reader = TestTokens.bearer(TestTokens.create(tokens, "stats-reader", Scope.READ));
        seed(POINTS, T0.plusSeconds(600), point("a", POINT_KIND, 10, 50, T0.plusSeconds(600), 1));
        rollUpAt(T0.plus(Duration.ofMinutes(75)));
    }

    @Test
    @DisplayName("timeseries, grid and top answer their documents")
    void documents() throws Exception {
        mvc.perform(get("/v1/stats/timeseries").param("source", POINTS).param("from", T0.toString())
                        .param("to", H1.toString()).param("bucket", "1h"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.backend").value("rollup"))
                .andExpect(jsonPath("$.measure").value("versions"))
                .andExpect(jsonPath("$.series[0].key").doesNotExist())
                .andExpect(jsonPath("$.series[0].points[0].value").value(1))
                .andExpect(jsonPath("$.timeField").value("ingested"));
        mvc.perform(get("/v1/stats/grid").param("source", POINTS).param("bbox", "5,45,15,55")
                        .param("from", T0.toString()).param("to", H1.toString()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, startsWith("application/geo+json")))
                .andExpect(jsonPath("$.type").value("FeatureCollection"))
                .andExpect(jsonPath("$.cellSystem").value("h3"))
                .andExpect(jsonPath("$.features[0].properties.value").value(1));
        mvc.perform(get("/v1/stats/top").param("by", "kind").param("from", T0.toString()).param("to", H1.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.by").value("kind"))
                .andExpect(jsonPath("$.items[0].key").value(POINT_KIND));
    }

    @Test
    @DisplayName("region filters are refused until 040/119; hidden sources are 404; bad parameters are 400")
    void refusals() throws Exception {
        mvc.perform(get("/v1/stats/timeseries").param("country", "US").param("from", T0.toString())
                .param("to", H1.toString())).andExpect(status().isBadRequest());
        mvc.perform(get("/v1/stats/timeseries").param("groupBy", "region").param("from", T0.toString())
                .param("to", H1.toString())).andExpect(status().isBadRequest());
        mvc.perform(get("/v1/stats/top").param("by", "region").param("from", T0.toString())
                .param("to", H1.toString())).andExpect(status().isBadRequest());
        mvc.perform(get("/v1/stats/timeseries").param("source", SECRET).param("from", T0.toString())
                .param("to", H1.toString())).andExpect(status().isNotFound());
        mvc.perform(get("/v1/stats/timeseries").param("measure", "bogus").param("from", T0.toString())
                .param("to", H1.toString())).andExpect(status().isBadRequest());
        mvc.perform(get("/v1/stats/timeseries").param("to", H1.toString())).andExpect(status().isBadRequest());
        mvc.perform(get("/v1/stats/grid").param("from", T0.toString()).param("to", H1.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("the rebuild is admin and answers 202 with its plan")
    void rebuild() throws Exception {
        String body = "{\"from\": \"" + T0 + "\", \"to\": \"" + H1 + "\"}";
        mvc.perform(post("/v1/stats/rollup/rebuild").header(HttpHeaders.AUTHORIZATION, reader)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
        mvc.perform(post("/v1/stats/rollup/rebuild").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("queued"))
                .andExpect(jsonPath("$.hours").value(1))
                .andExpect(jsonPath("$.days").value(0));
        mvc.perform(post("/v1/stats/rollup/rebuild").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"from\": \"" + H1 + "\", \"to\": \"" + T0
                                + "\"}"))
                .andExpect(status().isBadRequest());
    }
}
