package ai.foresignal.markets;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collect.BackoffRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.adapters.cftc.CftcCotAdapter;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * The CFTC COT history backfill end to end (prompt 117): {@link CotBackfillService} drives manual fetches of
 * {@code cftc-cot} through the real fetch executor and writer against WireMock; the backfill pages backwards to
 * {@code from}, resumes after an interrupted run from the last committed bound, and is idempotent.
 */
@IntegrationTest
@Import(MutableClock.TestConfig.class)
class CotBackfillIT extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-16T12:00:00Z");
    static final String DATASET = "/resource/72hh-3qpy.json";
    static final String ID = CftcCotAdapter.ID;

    @DynamicPropertySource
    static void upstreams(DynamicPropertyRegistry registry) {
        registry.add(CftcCotAdapter.BASE_URL_PROPERTY, WIREMOCK::baseUrl);
    }

    @Autowired CotBackfillService backfills;
    @Autowired BackfillJobRepository jobs;
    @Autowired SourceService sources;
    @Autowired BackoffRegistry backoff;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        clock.setInstant(NOW);
        backoff.onSuccess(ID);
        jobs.deleteAll();
        jdbc.update("update source_health set adapter_state = '{}'::jsonb where source_id = ?", ID);
        jdbc.update("delete from fetch_log where source_id = ?", ID);
    }

    @AfterEach
    void tearDown() {
        sources.update(ID, SourceSettingsUpdate.enabled(false));
        backoff.onSuccess(ID);
    }

    private void enable(int perMinute) {
        sources.update(ID, new SourceSettingsUpdate(true, null, Map.of(
                CftcCotAdapter.REPORTS_PARAM, List.of("disaggregated"),
                CftcCotAdapter.CONTRACTS_PARAM, List.of("067651")), Budget.of(perMinute, 10_000), null, false, null,
                false));
    }

    /** One row of the WTI contract on {@code date}. */
    private static String row(LocalDate date, long oi) {
        return "{\"id\":\"" + date + "067651F\",\"report_date_as_yyyy_mm_dd\":\"" + date
                + "T00:00:00.000\",\"cftc_contract_market_code\":\"067651\",\"market_and_exchange_names\":"
                + "\"WTI-PHYSICAL - NEW YORK MERCANTILE EXCHANGE\",\"open_interest_all\":\"" + oi
                + "\",\"m_money_positions_long_all\":\"200\",\"m_money_positions_short_all\":\"100\"}";
    }

    /** {@code count} weekly rows ending at {@code newest}, newest first (the backfill order). */
    private static String weeks(LocalDate newest, int count) {
        StringBuilder out = new StringBuilder("[");
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                out.append(',');
            }
            out.append(row(newest.minusWeeks(i), 1000 + i));
        }
        return out.append(']').toString();
    }

    private void serveIncremental() {
        WIREMOCK.stubFor(get(urlPathEqualTo(DATASET)).withQueryParam("$where", containing("> '"))
                .willReturn(aResponse().withStatus(200).withBody(weeks(LocalDate.parse("2026-10-13"), 2))));
    }

    private long heads() {
        Long count = jdbc.queryForObject("select count(*) from record_head where source_id = ? and kind ="
                + " 'series-point'", Long.class, ID);
        return count == null ? 0 : count;
    }

    private long versions() {
        Long count = jdbc.queryForObject("select count(*) from record_version where source_id = ?", Long.class, ID);
        return count == null ? 0 : count;
    }

    @Test
    @DisplayName("pages backwards from the oldest stored report to from, then the job is done")
    void pagesBackwards() {
        enable(100);
        serveIncremental();
        // The backfill asks for reports older than the oldest stored (2026-10-06) and not older than from.
        WIREMOCK.stubFor(get(urlPathEqualTo(DATASET))
                .withQueryParam("$where", containing("report_date_as_yyyy_mm_dd < '2026-10-06'"))
                .willReturn(aResponse().withStatus(200).withBody(weeks(LocalDate.parse("2026-09-29"), 4))));
        BackfillJobEntity job = backfills.start("2026-09-01", null, false);
        assertThat(job.getStatus()).isEqualTo("running");

        backfills.advance(job.getId());

        BackfillJobEntity done = backfills.get(job.getId());
        assertThat(done.getStatus()).isEqualTo("done");
        assertThat(done.getCursor()).isEqualTo(LocalDate.parse("2026-09-01"));
        assertThat(done.getPagesFetched()).isEqualTo(1);
        assertThat(done.getVersionsWritten()).isGreaterThan(0);
        assertThat(done.view()).containsEntry("sourceId", ID).containsEntry("from", "2026-09-01");
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(DATASET))
                .withQueryParam("$where", containing("report_date_as_yyyy_mm_dd >= '2026-09-01'"))
                .withQueryParam("$order", containing("DESC")));
        Long september = jdbc.queryForObject("select count(*) from record_head where source_id = ? and kind ="
                + " 'series-point' and external_id like '%:2026-09-08'", Long.class, ID);
        assertThat(september).isPositive();
    }

    @Test
    @DisplayName("resumes from the last committed bound after a budget pause and a failed run")
    void resumes() {
        enable(2);
        serveIncremental();
        // A full page (1,000 rows, 1,000 weekly reports) from 2026-09-29 back; its oldest report is re-read next.
        LocalDate pageNewest = LocalDate.parse("2026-09-29");
        LocalDate pageOldest = pageNewest.minusWeeks(999);
        WIREMOCK.stubFor(get(urlPathEqualTo(DATASET))
                .withQueryParam("$where", containing("report_date_as_yyyy_mm_dd < '2026-10-06'"))
                .willReturn(aResponse().withStatus(200).withBody(weeks(pageNewest, 1000))));
        String nextBound = "report_date_as_yyyy_mm_dd < '" + pageOldest.plusDays(1) + "'";
        WIREMOCK.stubFor(get(urlPathEqualTo(DATASET)).withQueryParam("$where", containing(nextBound))
                .inScenario("resume").whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(500).withBody("upstream error"))
                .willSetStateTo("recovered"));
        WIREMOCK.stubFor(get(urlPathEqualTo(DATASET)).withQueryParam("$where", containing(nextBound))
                .inScenario("resume").whenScenarioStateIs("recovered")
                .willReturn(aResponse().withStatus(200).withBody(weeks(pageOldest, 3))));
        BackfillJobEntity job = backfills.start("1986-01-01", null, false);

        // Run 1: the incremental read and one backfill page fit the 2-a-minute budget; the next page is refused.
        backfills.advance(job.getId());
        BackfillJobEntity afterFirst = backfills.get(job.getId());
        assertThat(afterFirst.getStatus()).isEqualTo("running");
        assertThat(afterFirst.getPagesFetched()).isEqualTo(1);
        assertThat(afterFirst.getCursor()).isEqualTo(pageOldest.plusDays(1));

        // Run 2 (a minute later): the upstream fails; nothing from this run is committed.
        clock.setInstant(NOW.plusSeconds(61));
        backoff.onSuccess(ID);
        backfills.advance(job.getId());
        assertThat(backfills.get(job.getId()).getStatus()).isEqualTo("running");
        assertThat(backfills.get(job.getId()).getCursor()).isEqualTo(pageOldest.plusDays(1));

        // Run 3: resumes from the committed bound, not from the oldest stored report.
        clock.setInstant(NOW.plusSeconds(122));
        backoff.onSuccess(ID);
        backfills.advance(job.getId());
        BackfillJobEntity done = backfills.get(job.getId());
        assertThat(done.getStatus()).isEqualTo("done");
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(DATASET))
                .withQueryParam("$where", containing("report_date_as_yyyy_mm_dd < '2026-10-06'")));
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(DATASET)).withQueryParam("$where", containing(nextBound)));
    }

    @Test
    @DisplayName("idempotent: a second backfill to the same date writes no version and finishes at once")
    void idempotent() {
        enable(100);
        serveIncremental();
        WIREMOCK.stubFor(get(urlPathEqualTo(DATASET))
                .withQueryParam("$where", containing("report_date_as_yyyy_mm_dd < '"))
                .willReturn(aResponse().withStatus(200).withBody(weeks(LocalDate.parse("2026-09-29"), 4))));
        BackfillJobEntity first = backfills.start("2026-09-01", null, false);
        backfills.advance(first.getId());
        assertThat(backfills.get(first.getId()).getStatus()).isEqualTo("done");
        long versions = versions();
        long heads = heads();

        clock.setInstant(NOW.plusSeconds(3600 * 2));
        BackfillJobEntity second = backfills.start("2026-09-01", null, false);
        backfills.advance(second.getId());

        assertThat(backfills.get(second.getId()).getStatus()).isEqualTo("done");
        assertThat(versions()).isEqualTo(versions);
        assertThat(heads()).isEqualTo(heads);
        assertThat(backfills.get(second.getId()).getVersionsWritten()).isZero();
    }

    @Test
    @DisplayName("refused while disabled or already running; from is validated")
    void refusals() {
        sources.update(ID, SourceSettingsUpdate.enabled(false));
        assertThatThrownBy(() -> backfills.start(null, null, false)).isInstanceOf(ConflictException.class)
                .hasMessageContaining("disabled");
        enable(100);
        assertThatThrownBy(() -> backfills.start("2026-13-01", null, false))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> backfills.start("1950-01-01", null, false))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> backfills.start("2030-01-01", null, false))
                .isInstanceOf(ValidationException.class);
        BackfillJobEntity job = backfills.start(null, null, false);
        assertThat(job.getFrom()).isEqualTo(CotBackfillService.DEFAULT_FROM);
        assertThatThrownBy(() -> backfills.start(null, null, false)).isInstanceOf(ConflictException.class)
                .hasMessageContaining("already running");
    }
}
