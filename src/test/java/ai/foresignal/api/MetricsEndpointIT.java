package ai.foresignal.api;

import static ai.foresignal.testsupport.stats.StatsTestSources.POINTS;
import static ai.foresignal.testsupport.stats.StatsTestSources.POINT_KIND;
import static ai.foresignal.testsupport.stats.StatsTestSources.SECRET;
import static ai.foresignal.testsupport.stats.StatsTestSources.SECRET_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.stats.StatsItSupport;
import ai.foresignal.testsupport.stats.StatsTestSources;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
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
 * {@code GET /v1/metrics} and {@code /v1/settings/metrics} over HTTP (prompt 038): the exposition
 * parses as the Prometheus text format (and OpenMetrics on request), projected series never appear on
 * {@code /actuator/prometheus}, private sources follow the 039 gate (admin or the private grant), and the endpoint needs the
 * {@code read} scope (401 without a token once {@code openReads} is off).
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(StatsTestSources.class)
class MetricsEndpointIT extends StatsItSupport {

    /** One sample line of the text format: name, optional labels, value, optional timestamp. */
    static final Pattern SAMPLE = Pattern.compile(
            "^[a-zA-Z_:][a-zA-Z0-9_:]*(\\{.*\\})? ([-+0-9.eE]+|NaN|[+-]Inf)( [0-9]+)?$");

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;

    String admin;
    String reader;
    String agent;
    String privateAgent;

    @BeforeEach
    void setUp() throws Exception {
        admin = TestTokens.bearer(TestTokens.create(tokens, "metrics-admin", Scope.ADMIN));
        reader = TestTokens.bearer(TestTokens.create(tokens, "metrics-reader", Scope.READ));
        agent = TestTokens.bearer(TestTokens.create(tokens, "metrics-agent", Scope.AGENT));
        privateAgent = TestTokens.bearer(TestTokens.create(tokens, "metrics-private-agent", Scope.AGENT, true));
        seed(POINTS, T0, point("q1", POINT_KIND, 10, 50, T0, 4.5));
        seed(SECRET, T0, point("s1", SECRET_KIND, 10, 50, T0, 9));
        mvc.perform(put("/v1/settings/metrics").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"projections\": [{\"name\": \"quake_value\", \"source\": \"" + POINTS
                                + "\", \"path\": \"value\", \"unit\": \"magnitude\", \"help\": \"Quake value\"},"
                                + " {\"name\": \"secret_value\", \"source\": \"" + SECRET
                                + "\", \"path\": \"value\", \"allowPrivate\": true}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projections[0].maxSeries").value(500));
    }

    @AfterEach
    void restore() {
        settings.put(SettingKeys.SECURITY, SecuritySettings.defaults());
    }

    private String scrape(String authorization, String accept) throws Exception {
        var request = get("/v1/metrics");
        if (authorization != null) {
            request = request.header(HttpHeaders.AUTHORIZATION, authorization);
        }
        if (accept != null) {
            request = request.header(HttpHeaders.ACCEPT, accept);
        }
        return mvc.perform(request).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("the text exposition parses: HELP and TYPE lines, then samples with observed timestamps")
    void textFormat() throws Exception {
        String body = mvc.perform(get("/v1/metrics").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentType())
                        .startsWith("text/plain").contains("version=0.0.4"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(body).contains("# HELP foresignal_data_quake_value Quake value (magnitude)")
                .contains("# TYPE foresignal_data_quake_value gauge")
                .contains("foresignal_data_quake_value{external_id=\"q1\",source=\"" + POINTS + "\"} 4.5")
                .contains("foresignal_data_quake_value_observed_timestamp_seconds{external_id=\"q1\",source=\""
                        + POINTS + "\"} ");
        for (String line : body.split("\n")) {
            if (!line.isBlank() && !line.startsWith("#")) {
                assertThat(line).as("sample line").matches(SAMPLE);
            }
        }
        assertThat(body).as("private series need the private gate").doesNotContain("secret_value");
        // Prompt 039: the private grant (or admin) is needed; a plain agent token no longer passes.
        assertThat(scrape(agent, null)).doesNotContain("secret_value");
        assertThat(scrape(privateAgent, null)).contains("foresignal_data_secret_value");
        assertThat(scrape(admin, null)).contains("foresignal_data_secret_value");
    }

    @Test
    @DisplayName("OpenMetrics when Accept asks for it")
    void openMetrics() throws Exception {
        String body = scrape(reader, "application/openmetrics-text; version=1.0.0");
        assertThat(body).contains("foresignal_data_quake_value").endsWith("# EOF\n");
    }

    @Test
    @DisplayName("projected series never appear on /actuator/prometheus")
    void notOnActuator() throws Exception {
        String actuator = mvc.perform(get("/actuator/prometheus").header(HttpHeaders.AUTHORIZATION, admin))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(actuator).doesNotContain("foresignal_data_");
    }

    @Test
    @DisplayName("read scope: anonymous under openReads; 401 without a token when openReads is off")
    void scopes() throws Exception {
        assertThat(scrape(null, null)).contains("foresignal_data_quake_value").doesNotContain("secret_value");
        settings.put(SettingKeys.SECURITY, new SecuritySettings(false, null, null, null));
        mvc.perform(get("/v1/metrics")).andExpect(status().isUnauthorized());
        assertThat(scrape(reader, null)).contains("foresignal_data_quake_value");
    }

    @Test
    @DisplayName("the settings are admin: a read token is refused; GET returns what PUT stored; bad bodies are 400")
    void settingsEndpoints() throws Exception {
        mvc.perform(get("/v1/settings/metrics").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isForbidden());
        mvc.perform(get("/v1/settings/metrics").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projections.length()").value(2))
                .andExpect(jsonPath("$.projections[1].allowPrivate").value(true));
        mvc.perform(put("/v1/settings/metrics").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"projections\": [{\"name\": \"BAD\"}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        assertThat(MetricsController.visible(new ai.foresignal.stats.metrics.MetricProjection("n", POINTS, null, "v",
                null, null, null, null, null), java.util.Set.of(), new ai.foresignal.security.restrict.ReadRestriction(
                null, java.util.List.of("other"), null, null, null, false, null))).as("kind-restricted token")
                .isFalse();
    }
}
