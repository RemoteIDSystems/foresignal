package ai.foresignal.source.adapters.spc;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherAdapterHarness;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code spc-storm-reports} against a stubbed {@code www.spc.noaa.gov} (prompt 053): ETag conditional GETs,
 * {@code yesterday.csv} hourly, the convective date across the 12 UTC rollover (no duplicate reports when today's
 * file is seen again as yesterday's), and per-file failures.
 */
@IntegrationTest
class SpcStormReportsAdapterWireMockTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-13T04:45:00Z");
    static final String TODAY = "/climo/reports/today.csv";
    static final String YESTERDAY = "/climo/reports/yesterday.csv";
    static final String TODAY_MODIFIED = "Tue, 13 Oct 2026 04:38:06 GMT";
    static final String YESTERDAY_MODIFIED = "Mon, 12 Oct 2026 23:51:04 GMT";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    MutableClock clock;
    WeatherAdapterHarness.Upstream upstream;
    SpcStormReportsAdapter adapter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(NOW);
        upstream = WeatherAdapterHarness.upstream(jdbc, jsonMapper, properties, install, clock, Budget.of(100, 400), null);
        adapter = new SpcStormReportsAdapter(baseUrl());
    }

    @AfterEach
    void tearDown() {
        upstream.backoff().onSuccess(SpcStormReportsAdapter.ID);
    }

    private WeatherAdapterHarness.Context context() {
        SourceDescriptor d = adapter.descriptor();
        return new WeatherAdapterHarness.Context(d, upstream.client().forSource(SpcStormReportsAdapter.ID, d),
                ValidatedParams.empty(), state(), clock);
    }

    private AdapterStateStore state() {
        return upstream.client().stateStore(SpcStormReportsAdapter.ID);
    }

    private static void serve(String path, String fixture, String etag, String lastModified) {
        WIREMOCK.stubFor(get(urlPathEqualTo(path)).withHeader("If-None-Match", absent())
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/csv").withHeader("ETag", etag)
                        .withHeader("Last-Modified", lastModified).withHeader("Cache-Control", "max-age=300")
                        .withBody(AdapterFixtures.bytes("spc-storm-reports", fixture))));
        WIREMOCK.stubFor(get(urlPathEqualTo(path)).withHeader("If-None-Match", equalTo(etag))
                .willReturn(aResponse().withStatus(304).withHeader("ETag", etag)));
    }

    private static Set<String> ids(FetchResult result) {
        return result.records().stream().map(NormalizedRecord::externalId).collect(Collectors.toSet());
    }

    @Test
    @DisplayName("the descriptor is valid: storm-report, 30 d, 2/min and 400/day, 10 minutes, no TTL, zero rows normal")
    void descriptor() {
        SourceDescriptor d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.kinds()).containsExactly("storm-report");
        assertThat(d.kindRetention()).containsEntry("storm-report", Duration.ofDays(30));
        assertThat(d.defaultBudget().perMinute()).isEqualTo(2);
        assertThat(d.defaultBudget().perDay()).isEqualTo(400);
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofMinutes(10));
        assertThat(d.recordTtl()).isNull();
        assertThat(d.zeroResultsNormal()).isTrue();
        assertThat(d.embedFields()).containsExactly("comments", "location");
        assertThat(d.fieldSpecs()).containsKeys("type", "magnitude", "magnitudeUnits", "location", "county", "state", "wfo",
                "comments", "title");
    }

    @Test
    @DisplayName("first run reads both files; the next run 10 minutes later is a 304 on today and skips yesterday")
    void conditionalGets() throws Exception {
        serve(TODAY, "today-20261012.csv", "\"t1\"", TODAY_MODIFIED);
        serve(YESTERDAY, "yesterday-20261011.csv", "\"y1\"", YESTERDAY_MODIFIED);

        FetchResult first = adapter.fetch(context());

        assertThat(first.records()).hasSize(1 + 15 + 43);
        assertThat(first.records()).allSatisfy(r -> assertThat(r.tombstone()).isFalse());

        clock.setInstant(NOW.plus(Duration.ofMinutes(10)));
        FetchResult second = adapter.fetch(context());

        assertThat(second.notModified()).isTrue();
        assertThat(second.records()).isEmpty();
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(TODAY)).withHeader("If-None-Match", equalTo("\"t1\"")));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(YESTERDAY)));

        clock.setInstant(NOW.plus(Duration.ofMinutes(61)));
        adapter.fetch(context());
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(YESTERDAY)).withHeader("If-None-Match", equalTo("\"y1\"")));
    }

    @Test
    @DisplayName("across the 12 UTC rollover today's reports come back from yesterday.csv with the same ids: no duplicates")
    void rolloverWithoutDuplicates() throws Exception {
        clock.setInstant(Instant.parse("2026-10-13T11:50:00Z"));
        serve(TODAY, "today-20261012.csv", "\"t1\"", TODAY_MODIFIED);
        serve(YESTERDAY, "yesterday-20261011.csv", "\"y1\"", YESTERDAY_MODIFIED);
        FetchResult before = adapter.fetch(context());
        Set<String> todayIds = before.records().stream().filter(r -> "2 SSW Muleshoe".equals(r.payload().get("location")))
                .map(NormalizedRecord::externalId).collect(Collectors.toSet());
        assertThat(todayIds).containsExactly("3177780650fc");

        // 12:10 UTC: SPC moved Oct 12's file to yesterday.csv (rewritten after its day began) and has not
        // rewritten today.csv yet (its Last-Modified is before 12 UTC Oct 13, so it still holds Oct 12).
        clock.setInstant(Instant.parse("2026-10-13T12:10:00Z"));
        WIREMOCK.resetAll();
        serve(TODAY, "today-20261012.csv", "\"t2\"", TODAY_MODIFIED);
        serve(YESTERDAY, "today-20261012.csv", "\"y2\"", "Tue, 13 Oct 2026 12:01:00 GMT");
        state().remove(SpcStormReportsAdapter.YESTERDAY_AT);
        ai.foresignal.source.spi.SourceHttp.forgetValidators(state(), "today");
        ai.foresignal.source.spi.SourceHttp.forgetValidators(state(), "yesterday");
        FetchResult after = adapter.fetch(context());

        assertThat(ids(after)).as("the same report from both files keeps one id").containsExactly("3177780650fc");
        assertThat(after.records()).hasSize(2);
        assertThat(after.records()).allSatisfy(r ->
                assertThat(r.observedAt()).isEqualTo(Instant.parse("2026-10-13T03:53:00Z")));
    }

    @Test
    @DisplayName("convective date: the clock minus 12 h, one day back for a file not rewritten since its day began")
    void convectiveDate() {
        assertThat(SpcStormReportsAdapter.convectiveDate(Instant.parse("2026-10-13T11:59:00Z")))
                .isEqualTo(LocalDate.parse("2026-10-12"));
        assertThat(SpcStormReportsAdapter.convectiveDate(Instant.parse("2026-10-13T12:00:00Z")))
                .isEqualTo(LocalDate.parse("2026-10-13"));
        LocalDate d = LocalDate.parse("2026-10-13");
        assertThat(SpcStormReportsAdapter.fileDate(d, "Tue, 13 Oct 2026 04:38:06 GMT")).isEqualTo(d.minusDays(1));
        assertThat(SpcStormReportsAdapter.fileDate(d, "Tue, 13 Oct 2026 12:00:30 GMT")).isEqualTo(d);
        assertThat(SpcStormReportsAdapter.fileDate(d, null)).isEqualTo(d);
        assertThat(SpcStormReportsAdapter.fileDate(d, "yesterday-ish")).isEqualTo(d);
    }

    @Test
    @DisplayName("a header-only today and a failing yesterday: zero rows is normal, the failure degrades; both failing throws")
    void failures() throws Exception {
        serve(TODAY, "header-only.csv", "\"t1\"", TODAY_MODIFIED);
        WIREMOCK.stubFor(get(urlPathEqualTo(YESTERDAY)).willReturn(aResponse().withStatus(500)));

        FetchResult result = adapter.fetch(context());

        assertThat(result.records()).isEmpty();
        assertThat(result.notModified()).isFalse();
        assertThat(state().get(AdapterStateStore.DEGRADED_REASON_KEY)).hasValueSatisfying(
                reason -> assertThat(reason).contains("SPC yesterday.csv failed"));

        WIREMOCK.resetAll();
        WIREMOCK.stubFor(get(urlPathEqualTo(TODAY)).willReturn(aResponse().withStatus(500)));
        WIREMOCK.stubFor(get(urlPathEqualTo(YESTERDAY)).willReturn(aResponse().withStatus(500)));
        state().remove(SpcStormReportsAdapter.YESTERDAY_AT);
        assertThatThrownBy(() -> adapter.fetch(context())).isInstanceOf(AdapterException.class);
    }

    @Test
    @DisplayName("a truncated file forgets its validators, so the next run asks again in full")
    void truncatedForgetsValidators() throws Exception {
        serve(TODAY, "today-20261012.csv", "\"t1\"", TODAY_MODIFIED);
        serve(YESTERDAY, "yesterday-20261011.csv", "\"y1\"", YESTERDAY_MODIFIED);
        WeatherAdapterHarness.Context ctx = context();
        WeatherAdapterHarness.Context truncating = new WeatherAdapterHarness.Context(ctx.descriptor(),
                WeatherAdapterHarness.truncating(ctx.http()), ctx.params(), ctx.state(), ctx.clock());

        assertThatThrownBy(() -> adapter.fetch(truncating)).isInstanceOf(AdapterException.class);
        assertThat(state().get("etag:today")).isEmpty();
        assertThat(state().get("etag:yesterday")).isEmpty();
    }
}
