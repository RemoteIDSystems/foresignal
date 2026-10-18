package ai.foresignal.doctor;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.collect.BackoffRegistry;
import ai.foresignal.collect.BudgetGovernor;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.adapters.usgs.UsgsEarthquakesAdapter;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.security.TestTokens;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The adapter doctor end to end against WireMock serving the 011 USGS adapter's upstream (prompt 140): a renamed
 * field ({@code mag} → {@code magnitude}) gives {@code FIELD_RENAMED} with a high score, an HTML maintenance page gives
 * {@code HTML_INSTEAD_OF_DATA}, 451 gives {@code UA_BLOCKED}, the probe is skipped when the budget is spent, the report
 * is persisted without raw bodies, a second run within 10 minutes is {@code DOCTOR_BUSY}, and the REST surface
 * (scopes, pages, {@code lastDoctorReport} on health).
 *
 * <p>The LLM step is off or has no provider here; {@link DoctorLlmIT} covers it.
 */
@IntegrationTest
@Import(MutableClock.TestConfig.class)
class DoctorServiceIT extends WireMockSupport {

    static final String ID = UsgsEarthquakesAdapter.ID;
    static final Instant NOW = Instant.parse("2026-10-11T22:33:00Z");
    static final String RAW_MARKER = "RAW-BODY-MARKER-7f3a9c";
    static final DoctorOptions NO_LLM = new DoctorOptions(true, DoctorOptions.LLM_OFF, DoctorOptions.COMPACT);

    @DynamicPropertySource
    static void upstream(DynamicPropertyRegistry registry) {
        registry.add(UsgsEarthquakesAdapter.BASE_URL_PROPERTY, WIREMOCK::baseUrl);
    }

    @Autowired DoctorService doctor;
    @Autowired SourceService sources;
    @Autowired BudgetGovernor budgets;
    @Autowired BackoffRegistry backoff;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired TokenService tokens;
    @Autowired AppSettingService settings;

    String admin;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        clock.setInstant(NOW);
        backoff.onSuccess(ID);
        backoff.clearHalt(ID);
        admin = adminTokens.rotate();
        budget(Budget.of(600, 100_000));
    }

    @AfterEach
    void tearDown() {
        backoff.onSuccess(ID);
        backoff.clearHalt(ID);
    }

    void budget(Budget budget) {
        sources.update(ID, new SourceSettingsUpdate(false, null, null, budget, null, false, null, false));
        budgets.invalidate(ID);
    }

    static void serve(int status, String contentType, String body) {
        WIREMOCK.stubFor(WireMock.get(urlPathMatching("/earthquakes/feed/v1.0/summary/.*"))
                .willReturn(aResponse().withStatus(status).withHeader("Content-Type", contentType).withBody(body)));
    }

    static String fixture() {
        return new String(AdapterFixtures.bytes(ID, "all_day.geojson"), StandardCharsets.UTF_8);
    }

    static List<FindingCode> codes(DoctorReportView report) {
        return report.findings().stream().map(DoctorFinding::code).toList();
    }

    @Test
    @DisplayName("a renamed field (mag → magnitude) yields FIELD_RENAMED with a high score and a BROKEN report")
    void renamedField() {
        serve(200, "application/json; charset=utf-8", fixture().replace("\"mag\":", "\"magnitude\":"));

        DoctorReportView report = doctor.run(ID, NO_LLM, DoctorTrigger.MANUAL);

        assertThat(report.status()).isEqualTo(DoctorStatus.BROKEN);
        DoctorFinding renamed = report.findings().stream().filter(f -> f.code() == FindingCode.FIELD_RENAMED)
                .findFirst().orElseThrow();
        assertThat(renamed.path()).isEqualTo("features[].properties.mag");
        assertThat(renamed.heuristic()).isTrue();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) report.diff().get("renameCandidates");
        Map<String, Object> best = candidates.get(0);
        assertThat(best.get("from")).isEqualTo("features[].properties.mag");
        assertThat(best.get("to")).isEqualTo("features[].properties.magnitude");
        assertThat(((Number) best.get("score")).doubleValue()).isGreaterThanOrEqualTo(0.9);
        assertThat(report.fixture()).containsEntry("file", "all_day.geojson").containsEntry("capturedAt",
                "2026-10-11");
        assertThat(report.probe()).containsEntry("skipped", false).containsEntry("httpStatus", 200);
        assertThat(report.llm()).containsEntry("status", DoctorLlm.OFF);
        assertThat(report.trigger()).isEqualTo(DoctorTrigger.MANUAL);
        assertThat(report.adapterVersion()).isNotBlank();
    }

    @Test
    @DisplayName("an HTML maintenance page yields HTML_INSTEAD_OF_DATA with its title")
    void htmlMaintenancePage() {
        serve(200, "text/html", "<!DOCTYPE html><html><head><title>Scheduled maintenance</title></head><body>The"
                + " service is temporarily unavailable.</body></html>");

        DoctorReportView report = doctor.run(ID, NO_LLM, DoctorTrigger.MANUAL);

        assertThat(codes(report)).contains(FindingCode.HTML_INSTEAD_OF_DATA);
        assertThat(report.diff()).containsEntry("htmlTitle", "Scheduled maintenance");
    }

    @Test
    @DisplayName("451 yields UA_BLOCKED, whose advice never suggests spoofing")
    void unavailableForLegalReasons() {
        serve(451, "text/plain", "Unavailable For Legal Reasons");

        DoctorReportView report = doctor.run(ID, NO_LLM, DoctorTrigger.MANUAL);

        DoctorFinding f = report.findings().stream().filter(x -> x.code() == FindingCode.UA_BLOCKED).findFirst()
                .orElseThrow();
        assertThat(f.suggestedAction()).contains("never spoof the User-Agent");
    }

    @Test
    @DisplayName("out of budget: the probe is skipped (PROBE_SKIPPED_BUDGET) and nothing is sent")
    void probeSkippedOutOfBudget() {
        serve(200, "application/json", fixture());
        budget(Budget.of(1, 1));
        assertThat(budgets.tryConsume(ID)).isTrue();

        DoctorReportView report = doctor.run(ID, NO_LLM, DoctorTrigger.MANUAL);

        assertThat(codes(report)).contains(FindingCode.PROBE_SKIPPED_BUDGET);
        assertThat(report.probe()).containsEntry("skipped", true).containsEntry("skipReason", "budget");
        WIREMOCK.verify(0, getRequestedFor(urlPathMatching("/earthquakes/.*")));
    }

    @Test
    @DisplayName("in backoff: the probe is skipped too")
    void probeSkippedInBackoff() {
        serve(200, "application/json", fixture());
        backoff.onThrottled(ID, java.util.Optional.of(Duration.ofMinutes(10)));

        DoctorReportView report = doctor.run(ID, NO_LLM, DoctorTrigger.MANUAL);

        assertThat(report.probe()).containsEntry("skipReason", "backoff");
        assertThat(codes(report)).contains(FindingCode.PROBE_SKIPPED_BUDGET, FindingCode.RATE_LIMITED);
    }

    @Test
    @DisplayName("the report is persisted without the raw body; only capped examples of changed paths are kept")
    void persistedWithoutRawBody() {
        String body = fixture().replace("\"mag\":", "\"magnitude\":").replaceFirst("\"title\":\"[^\"]*\"",
                "\"title\":\"" + RAW_MARKER + "\"");
        serve(200, "application/json", body);

        DoctorReportView report = doctor.run(ID, NO_LLM, DoctorTrigger.MANUAL);

        String stored = jdbc.queryForObject("select coalesce(findings::text, '') || coalesce(diff::text, '')"
                + " || coalesce(probe::text, '') || coalesce(llm::text, '') || coalesce(fixture::text, '')"
                + " from doctor_report where id = ?", String.class, report.id());
        assertThat(stored).doesNotContain(RAW_MARKER).doesNotContain("\"type\":\"FeatureCollection\"");
        assertThat(stored.length()).isLessThan(body.length());
        assertThat(doctor.report(report.id())).isPresent();
    }

    @Test
    @DisplayName("a second run within 10 minutes is DOCTOR_BUSY; after 10 minutes it runs again")
    void doctorBusy() {
        serve(200, "application/json", fixture());
        doctor.run(ID, NO_LLM, DoctorTrigger.MANUAL);

        assertThatThrownBy(() -> doctor.run(ID, NO_LLM, DoctorTrigger.MANUAL))
                .isInstanceOf(ConflictException.class)
                .extracting(e -> ((ConflictException) e).getCode()).isEqualTo(ErrorCodes.DOCTOR_BUSY);

        clock.setInstant(NOW.plus(Duration.ofMinutes(11)));
        assertThat(doctor.run(ID, NO_LLM, DoctorTrigger.MANUAL).findings()).isNotEmpty();
    }

    @Test
    @DisplayName("an unchanged upstream gives NO_CHANGE_DETECTED and an OK report")
    void noChange() {
        serve(200, "application/json", fixture());

        DoctorReportView report = doctor.run(ID, NO_LLM, DoctorTrigger.MANUAL);

        assertThat(codes(report)).containsExactly(FindingCode.NO_CHANGE_DETECTED);
        assertThat(report.status()).isEqualTo(DoctorStatus.OK);
    }

    @Test
    @DisplayName("REST: run (admin, always authenticated), 409 DOCTOR_BUSY, list and get (agent), health's lastDoctorReport")
    void rest() throws Exception {
        serve(200, "application/json", fixture().replace("\"mag\":", "\"magnitude\":"));
        String agent = TestTokens.bearer(TestTokens.create(tokens, "doctor-agent", Scope.AGENT));

        mvc.perform(post("/v1/sources/" + ID + "/doctor").contentType(MediaType.APPLICATION_JSON)
                .content("{\"llm\":\"off\"}")).andExpect(status().isUnauthorized());
        mvc.perform(post("/v1/sources/" + ID + "/doctor").header(HttpHeaders.AUTHORIZATION, agent)
                .contentType(MediaType.APPLICATION_JSON).content("{\"llm\":\"off\"}")).andExpect(status().isForbidden());

        String body = mvc.perform(post("/v1/sources/" + ID + "/doctor").header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + admin).contentType(MediaType.APPLICATION_JSON).content("{\"llm\":\"off\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceId", is(ID)))
                .andExpect(jsonPath("$.status", is("BROKEN")))
                .andExpect(jsonPath("$.trigger", is("MANUAL")))
                .andExpect(jsonPath("$.findings[0].severity", is("critical")))
                .andExpect(jsonPath("$.llm.status", is("off")))
                .andReturn().getResponse().getContentAsString();
        String reportId = com.jayway.jsonpath.JsonPath.read(body, "$.id");

        mvc.perform(post("/v1/sources/" + ID + "/doctor").header(HttpHeaders.AUTHORIZATION, "Bearer " + admin))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("DOCTOR_BUSY")));

        mvc.perform(get("/v1/sources/" + ID + "/doctor/reports").header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id", is(reportId)))
                .andExpect(jsonPath("$.nextCursor").value(org.hamcrest.Matchers.nullValue()));
        mvc.perform(get("/v1/doctor/reports/" + reportId).header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(reportId)));
        mvc.perform(get("/v1/doctor/reports/01J0000000000000000000000Z").header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("UNKNOWN_DOCTOR_REPORT")));
        mvc.perform(get("/v1/sources/no-such-source/doctor/reports").header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isNotFound());
        mvc.perform(get("/v1/sources/" + ID + "/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastDoctorReport.id", is(reportId)))
                .andExpect(jsonPath("$.lastDoctorReport.status", is("BROKEN")))
                .andExpect(jsonPath("$.lastDoctorReport.topFinding.code", notNullValue()))
                .andExpect(jsonPath("$.lastDoctorReport.createdAt", notNullValue()));
    }

    @Test
    @DisplayName("runSourceDoctor answers 202 with Location when the run outlasts the synchronous wait")
    void acceptedAfterWait(@Autowired DoctorController controller) throws Exception {
        WIREMOCK.stubFor(WireMock.get(urlPathMatching("/earthquakes/feed/v1.0/summary/.*"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(fixture()).withFixedDelay(1_500)));
        controller.syncMillis(100);
        try {
            mvc.perform(post("/v1/sources/" + ID + "/doctor").header(HttpHeaders.AUTHORIZATION, "Bearer " + admin)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"llm\":\"off\"}"))
                    .andExpect(status().isAccepted())
                    .andExpect(header().string(HttpHeaders.LOCATION, org.hamcrest.Matchers.startsWith(
                            "/v1/doctor/reports/")))
                    .andExpect(jsonPath("$.status", is("running")));
        } finally {
            controller.syncMillis(DoctorController.SYNC_SECONDS * 1000L);
        }
        org.awaitility.Awaitility.await().atMost(Duration.ofSeconds(15))
                .until(() -> doctor.latest(ID).isPresent());
    }
}
