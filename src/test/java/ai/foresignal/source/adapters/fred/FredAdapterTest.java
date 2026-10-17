package ai.foresignal.source.adapters.fred;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.PersistentAdapterStateStore;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialsRejectedException;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code fred-series} against a stub (prompt 117): copyright detection skips {@code VIXCLS} unless allowed, a
 * {@code "."} value is skipped, the revision window turns a revised value into a new point value, the source is
 * {@code LOCAL_ONLY}, and every latest record carries the FRED notice and the series' source line.
 */
@IntegrationTest
class FredAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-16T12:00:00Z");
    static final String KEY = "abcdefghijklmnopqrstuvwxyz123456";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    FredSeriesAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(1000, 100_000));
        adapter = new FredSeriesAdapter(baseUrl());
        harness.reset(adapter.id());
        harness.credentials.clear();
        harness.credentials.put(FredSeriesAdapter.API_KEY, KEY);
        jdbc.update("update source_health set adapter_state = '{}'::jsonb where source_id = ?", adapter.id());
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(adapter.id());
        harness.credentials.clear();
    }

    private static void serve(String path, String seriesId, String fixture) {
        WIREMOCK.stubFor(get(urlPathEqualTo(path)).withQueryParam("series_id", equalTo(seriesId))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes("fred-series", fixture))));
    }

    private static void serveMetadata(String seriesId, String fixture) {
        serve("/fred/series", seriesId, fixture);
        serve("/fred/series/release", seriesId, "release-DGS10.json");
        WIREMOCK.stubFor(get(urlPathEqualTo("/fred/release/sources")).willReturn(aResponse().withStatus(200)
                .withBody(AdapterFixtures.bytes("fred-series", "release-sources-18.json"))));
    }

    private Map<String, Object> params(List<String> series, List<String> allowed) {
        return Map.of(FredSeriesAdapter.SERIES_PARAM, series, FredSeriesAdapter.ALLOW_COPYRIGHTED_PARAM, allowed);
    }

    private AdapterStateStore state() {
        return new PersistentAdapterStateStore(adapter.id(), jdbc, jsonMapper);
    }

    @Test
    @DisplayName("a copyrighted series (VIXCLS) is skipped with a health warning unless allowed")
    void copyright() throws Exception {
        serveMetadata("DGS10", "series-DGS10.json");
        serveMetadata("VIXCLS", "series-VIXCLS.json");
        serve("/fred/series/observations", "DGS10", "observations-DGS10.json");
        serve("/fred/series/observations", "VIXCLS", "observations-VIXCLS.json");

        FetchResult skipped = adapter.fetch(harness.context(adapter, params(List.of("DGS10", "VIXCLS"), List.of())));
        assertThat(skipped.records()).extracting(r -> String.valueOf(r.payload().get("series")))
                .doesNotContain("fred.VIXCLS");
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/fred/series/observations"))
                .withQueryParam("series_id", equalTo("VIXCLS")));
        assertThat(state().get(AdapterStateStore.DEGRADED_REASON_KEY)).hasValueSatisfying(
                reason -> assertThat(reason).contains("VIXCLS").contains("allowCopyrighted"));

        FetchResult allowed = adapter.fetch(harness.context(adapter,
                params(List.of("DGS10", "VIXCLS"), List.of("vixcls"))));
        assertThat(allowed.records()).extracting(NormalizedRecord::externalId).contains("fred.VIXCLS:2026-10-14");
        assertThat(state().get(AdapterStateStore.DEGRADED_REASON_KEY)).isEmpty();
    }

    @Test
    @DisplayName("the missing value \".\" makes no record; points are rates in percent with a 1-day lag")
    void missingValue() throws Exception {
        serveMetadata("DGS10", "series-DGS10.json");
        serve("/fred/series/observations", "DGS10", "observations-DGS10.json");
        FetchResult result = adapter.fetch(harness.context(adapter, params(List.of("DGS10"), List.of())));
        assertThat(result.records()).extracting(NormalizedRecord::externalId)
                .containsExactlyInAnyOrder("fred.DGS10:2026-10-09", "fred.DGS10:2026-10-13", "fred.DGS10:2026-10-14",
                        "fred.DGS10:latest")
                .doesNotContain("fred.DGS10:2026-10-12");
        NormalizedRecord latest = result.records().stream().filter(r -> r.kind().equals("series-latest"))
                .findFirst().orElseThrow();
        assertThat(latest.payload()).containsEntry("family", "rate").containsEntry("transform", "difference")
                .containsEntry("releaseLagDays", 1.0).containsEntry("egress", "LOCAL_ONLY")
                .containsEntry("unit", "%").containsEntry("regionCode", "US");
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/fred/series/observations"))
                .withQueryParam("observation_start", equalTo("2025-10-16"))
                .withQueryParam("file_type", equalTo("json"))
                .withQueryParam("api_key", equalTo(KEY)));
        FieldSpecContract.assertPayloadKeysDeclared(adapter.descriptor(), result.records());
    }

    @Test
    @DisplayName("the revision window re-reads 14 days before the last period, so a revised value is a new value")
    void revision() throws Exception {
        serveMetadata("DGS10", "series-DGS10.json");
        serve("/fred/series/observations", "DGS10", "observations-DGS10.json");
        FetchResult first = adapter.fetch(harness.context(adapter, params(List.of("DGS10"), List.of())));
        NormalizedRecord before = first.records().stream()
                .filter(r -> r.externalId().equals("fred.DGS10:2026-10-13")).findFirst().orElseThrow();
        assertThat(before.payload()).containsEntry("value", 4.08);

        serve("/fred/series/observations", "DGS10", "observations-DGS10-revised.json");
        FetchResult second = adapter.fetch(harness.context(adapter, params(List.of("DGS10"), List.of())));
        NormalizedRecord after = second.records().stream()
                .filter(r -> r.externalId().equals("fred.DGS10:2026-10-13")).findFirst().orElseThrow();
        assertThat(after.payload()).containsEntry("value", 4.09);
        assertThat(after.externalId()).isEqualTo(before.externalId());
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/fred/series/observations"))
                .withQueryParam("observation_start", equalTo("2026-09-30")));
        // The metadata is cached for a week: one /fred/series request across both runs.
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/fred/series")));
    }

    @Test
    @DisplayName("the source is LOCAL_ONLY and its attribution carries the FRED notice and the series' source")
    void localOnlyAndAttribution() throws Exception {
        assertThat(adapter.descriptor().egress()).isEqualTo(Egress.LOCAL_ONLY);
        assertThat(adapter.descriptor().attribution()).isEqualTo(FredParser.NOTICE);
        assertThat(FredParser.NOTICE).isEqualTo("This product uses the FRED® API but is not endorsed or certified"
                + " by the Federal Reserve Bank of St. Louis.");
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
        serveMetadata("DGS10", "series-DGS10.json");
        serve("/fred/series/observations", "DGS10", "observations-DGS10.json");
        FetchResult result = adapter.fetch(harness.context(adapter, params(List.of("DGS10"), List.of())));
        NormalizedRecord latest = result.records().stream().filter(r -> r.kind().equals("series-latest"))
                .findFirst().orElseThrow();
        assertThat(String.valueOf(latest.payload().get("attribution"))).startsWith(FredParser.NOTICE)
                .contains("Board of Governors of the Federal Reserve System (US)")
                .contains("H.15 Selected Interest Rates");
    }

    @Test
    @DisplayName("family from units: percent is a rate, dollars per barrel a price, an index an index")
    void families() {
        assertThat(FredParser.family("Percent")).isEqualTo("rate");
        assertThat(FredParser.family("Dollars per Barrel")).isEqualTo("price");
        assertThat(FredParser.family("Dollars per Million BTU")).isEqualTo("price");
        assertThat(FredParser.family("Index Jan 2006=100")).isEqualTo("index");
        assertThat(FredParser.meta("Dollars per Barrel").transform()).isEqualTo("log-return");
        assertThat(FredParser.frequency("Q")).isEqualTo("P3M");
        assertThat(FredParser.frequency("A")).isEqualTo("P1Y");
        assertThat(FredParser.frequency("W")).isEqualTo("P1W");
        assertThat(FredParser.isCopyrighted("Copyright, 2026, Cboe")).isTrue();
        assertThat(FredParser.isCopyrighted("Reprinted with permission of the owner")).isTrue();
        assertThat(FredParser.isCopyrighted("H.15 release notes")).isFalse();
        assertThat(FredParser.ucum("Dollars per Barrel")).isEqualTo("[USD]/[bbl_us]");
    }

    @Test
    @DisplayName("a missing key fails before any request; a rejected key is a rejected credential")
    void keys() {
        harness.credentials.clear();
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, params(List.of("DGS10"), List.of()))))
                .hasMessageContaining("apiKey");
        harness.credentials.put(FredSeriesAdapter.API_KEY, KEY);
        WIREMOCK.stubFor(get(urlPathEqualTo("/fred/series")).willReturn(aResponse().withStatus(400)
                .withBody(AdapterFixtures.bytes("fred-series", "error-api-key.json"))));
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, params(List.of("DGS10"), List.of()))))
                .isInstanceOf(CredentialsRejectedException.class);
        assertThat(Optional.ofNullable(adapter.checkParams(ai.foresignal.source.spi.ValidatedParams.of(
                Map.of(FredSeriesAdapter.SERIES_PARAM, List.of("DGS10&api_key=x")))).get("series"))).isPresent();
    }
}
