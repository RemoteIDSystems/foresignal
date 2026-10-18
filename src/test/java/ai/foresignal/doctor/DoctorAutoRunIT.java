package ai.foresignal.doctor;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.collect.BackoffRegistry;
import ai.foresignal.collect.BudgetGovernor;
import ai.foresignal.collect.FetchExecutor;
import ai.foresignal.collect.HealthEvaluator;
import ai.foresignal.collect.HealthService;
import ai.foresignal.collect.SourceHealthChangedEvent;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.adapters.usgs.UsgsEarthquakesAdapter;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.store.FetchTrigger;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Automatic doctor runs (prompt 140, task 6; Charter success criterion 5): a simulated upstream format change marks the
 * source degraded, which triggers a deterministic doctor report ({@code trigger=AUTO}, {@code llm=off}) naming the
 * change, and a {@code system} notification for the {@code BROKEN} report; the cool-off; {@code doctor.autoLlm};
 * {@code lastDoctorReport} on health.
 *
 * <p>Its own context: the hook is on and synchronous here ({@code foresignal.doctor.auto-enabled=true},
 * {@code auto-async=false}), and the USGS adapter points at WireMock.
 */
@IntegrationTest
@TestPropertySource(properties = {"foresignal.doctor.auto-enabled=true", "foresignal.doctor.auto-async=false"})
@Import(MutableClock.TestConfig.class)
class DoctorAutoRunIT extends WireMockSupport {

    static final String ID = UsgsEarthquakesAdapter.ID;
    static final Instant NOW = Instant.parse("2026-10-11T22:33:00Z");

    @DynamicPropertySource
    static void upstream(DynamicPropertyRegistry registry) {
        registry.add(UsgsEarthquakesAdapter.BASE_URL_PROPERTY, WIREMOCK::baseUrl);
    }

    @Autowired DoctorAutoRunner runner;
    @Autowired DoctorService doctor;
    @Autowired ApplicationEventPublisher events;
    @Autowired FetchExecutor executor;
    @Autowired HealthService health;
    @Autowired SourceService sources;
    @Autowired BudgetGovernor budgets;
    @Autowired BackoffRegistry backoff;
    @Autowired AppSettingService settings;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc mvc;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        clock.setInstant(NOW);
        backoff.onSuccess(ID);
        backoff.clearHalt(ID);
        // Enabled, so health is evaluated (a disabled source is never degraded); the scheduler is off in tests.
        sources.update(ID, new SourceSettingsUpdate(true, null, null, Budget.of(600, 100_000), null, false, null,
                false));
        budgets.invalidate(ID);
    }

    @AfterEach
    void tearDown() {
        backoff.onSuccess(ID);
        backoff.clearHalt(ID);
    }

    static void serve(String body) {
        WIREMOCK.stubFor(WireMock.get(urlPathMatching("/earthquakes/feed/v1.0/summary/.*"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(body)));
    }

    static String fixture(String name) {
        return new String(AdapterFixtures.bytes(ID, name), StandardCharsets.UTF_8);
    }

    int reports(String trigger) {
        Integer n = jdbc.queryForObject("select count(*) from doctor_report where source_id = ? and trigger = ?",
                Integer.class, ID, trigger);
        return n == null ? 0 : n;
    }

    int brokenNotifications() {
        Integer n = jdbc.queryForObject("select count(*) from notification where subtype = ?", Integer.class,
                DoctorAutoRunner.SUBTYPE);
        return n == null ? 0 : n;
    }

    SourceHealthChangedEvent degraded(String reason) {
        return new SourceHealthChangedEvent(ID, HealthStatus.HEALTHY, HealthStatus.DEGRADED, reason, clock.instant());
    }

    @Test
    @DisplayName("a simulated format change degrades the source, runs the doctor automatically (llm off) and notifies")
    void formatChangeTriggersAutoRun() throws Exception {
        serve(fixture("all_day.geojson"));
        executor.execute(ID, FetchTrigger.MANUAL);
        assertThat(health.health(ID).status()).isEqualTo(HealthStatus.HEALTHY);

        serve(fixture("not-a-feature-collection.json"));
        executor.execute(ID, FetchTrigger.MANUAL);
        assertThat(health.health(ID).status()).isEqualTo(HealthStatus.DEGRADED);

        assertThat(reports("AUTO")).isEqualTo(1);
        DoctorReportView report = doctor.page(ID, null, 10).items().get(0);
        assertThat(report.trigger()).isEqualTo(DoctorTrigger.AUTO);
        assertThat(report.status()).isEqualTo(DoctorStatus.BROKEN);
        assertThat(report.llm()).containsEntry("status", DoctorLlm.OFF);
        assertThat(report.findings()).extracting(DoctorFinding::code).contains(FindingCode.PARSER_REJECTS);
        assertThat(report.diff().get("removedPaths").toString()).contains("features");
        assertThat(brokenNotifications()).isEqualTo(1);
        String title = jdbc.queryForObject("select title from notification where subtype = ?", String.class,
                DoctorAutoRunner.SUBTYPE);
        assertThat(title).startsWith(ID + ": ").contains(" — ");

        mvc.perform(get("/v1/sources/" + ID + "/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastDoctorReport.id", is(report.id())))
                .andExpect(jsonPath("$.lastDoctorReport.status", is("BROKEN")));
    }

    @Test
    @DisplayName("an event with a qualifying reason runs once; the 6 h cool-off holds; after it the doctor runs again")
    void cooldown() {
        serve(fixture("all_day.geojson").replace("\"mag\":", "\"magnitude\":"));

        Optional<DoctorReportView> first = runner.consider(degraded(HealthEvaluator.REASON_FORMAT_CHANGE));
        assertThat(first).isPresent();
        assertThat(first.get().findings()).extracting(DoctorFinding::code).contains(FindingCode.FIELD_RENAMED);

        clock.setInstant(NOW.plus(Duration.ofHours(1)));
        events.publishEvent(degraded("coverage dropped: receivers 10 against a median of 100"));
        assertThat(reports("AUTO")).isEqualTo(1);

        clock.setInstant(NOW.plus(Duration.ofHours(7)));
        assertThat(runner.consider(degraded(HealthEvaluator.REASON_FORMAT_CHANGE))).isPresent();
        assertThat(reports("AUTO")).isEqualTo(2);
    }

    @Test
    @DisplayName("doctor.autoCooldown shortens the cool-off; non-qualifying transitions never run")
    void settingsAndQualification() {
        serve(fixture("all_day.geojson"));
        settings.put(SettingKeys.DOCTOR_AUTO_COOLDOWN, "PT30M");
        assertThat(runner.cooldown()).isEqualTo(Duration.ofMinutes(30));

        assertThat(runner.consider(degraded("budget refusals 40% in last hour"))).isEmpty();
        assertThat(runner.consider(new SourceHealthChangedEvent(ID, HealthStatus.DEGRADED, HealthStatus.HEALTHY,
                null, NOW))).isEmpty();
        assertThat(reports("AUTO")).isZero();

        assertThat(runner.consider(degraded("last run failed: UpstreamFormatException: no features"))).isPresent();
        clock.setInstant(NOW.plus(Duration.ofMinutes(31)));
        assertThat(runner.consider(degraded(HealthEvaluator.REASON_FORMAT_CHANGE))).isPresent();
        assertThat(reports("AUTO")).isEqualTo(2);
    }

    @Test
    @DisplayName("without doctor.autoLlm the LLM is off; with it, auto runs ask the chat role (none here: no-provider)")
    void autoLlmOptIn() {
        serve(fixture("all_day.geojson"));
        assertThat(runner.autoLlm()).isFalse();
        assertThat(runner.consider(degraded(HealthEvaluator.REASON_FORMAT_CHANGE)).orElseThrow().llm())
                .containsEntry("status", DoctorLlm.OFF);

        settings.put(SettingKeys.DOCTOR_AUTO_LLM, true);
        clock.setInstant(NOW.plus(Duration.ofHours(7)));
        assertThat(runner.consider(degraded(HealthEvaluator.REASON_FORMAT_CHANGE)).orElseThrow().llm())
                .containsEntry("status", DoctorLlm.NO_PROVIDER);
    }

    @Test
    @DisplayName("an OK automatic report publishes no notification")
    void okReportDoesNotNotify() {
        serve(fixture("all_day.geojson"));

        DoctorReportView report = runner.consider(degraded(HealthEvaluator.REASON_FORMAT_CHANGE)).orElseThrow();

        assertThat(report.status()).isEqualTo(DoctorStatus.OK);
        assertThat(brokenNotifications()).isZero();
    }
}
