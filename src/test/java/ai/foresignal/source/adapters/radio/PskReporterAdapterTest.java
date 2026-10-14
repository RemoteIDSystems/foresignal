package ai.foresignal.source.adapters.radio;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code pskreporter-ft8} through the real upstream client against WireMock (prompt 082): the query URL with
 * {@code appcontact}, one request per watched call sign in turn, the record cap, coverage, errors, the User-Agent and
 * the descriptor.
 */
@IntegrationTest
class PskReporterAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-14T00:45:00Z");
    static final String QUERY = "/retrieve.pskreporter.info/query";
    static final String CONTACT = "operator@example.org";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    PskReporterAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000));
        adapter = RadioTestAdapters.psk(baseUrl());
        harness.reset(adapter.id());
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(adapter.id());
    }

    static void serve(byte[] body) {
        WIREMOCK.stubFor(get(urlPathEqualTo(QUERY)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/xml; charset=utf-8").withBody(body)));
    }

    @Test
    @DisplayName("the descriptor validates: 10 min (min 5 min), 2/min and 300/day, TTL 30 min OBSERVED, bulk-event")
    void descriptor() {
        SourceDescriptor d = adapter.descriptor();

        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofMinutes(10));
        assertThat(d.minInterval()).isEqualTo(Duration.ofMinutes(5));
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(2, 300));
        assertThat(d.historyTier()).isEqualTo(HistoryTier.STANDARD);
        assertThat(d.recordTtl()).isEqualTo(Duration.ofMinutes(30));
        assertThat(d.ttlAnchor()).isEqualTo(TtlAnchor.OBSERVED);
        assertThat(d.observedAtMode()).isEqualTo(ObservedAtMode.UPSTREAM);
        assertThat(d.zeroResultsNormal()).isTrue();
        assertThat(d.tileProperties()).containsExactly("sender", "mode", "band", "snr");
        assertThat(d.searchFields()).containsExactly("sender");
        assertThat(d.licence()).isEqualTo("PSK Reporter terms (non-commercial, attribution)");
        assertThat(d.param(PskReporterAdapter.APP_CONTACT).required()).isTrue();
        assertThat(d.defaultRetention()).isEqualTo(Duration.ofDays(90));
    }

    @Test
    @DisplayName("without call signs: one request with mode, window, rronly, noactive, rptlimit and appcontact")
    void unfiltered() throws Exception {
        serve(AdapterFixtures.bytes("pskreporter-ft8", "callsign-N6AD.xml"));

        FetchResult result = adapter.fetch(harness.context(adapter, Map.of(PskReporterAdapter.APP_CONTACT, CONTACT)));

        assertThat(result.records()).hasSize(50);
        assertThat(result.coverage().actual()).isEqualTo(50);
        assertThat(result.coverage().receivers()).isNull();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(QUERY)).withQueryParam("mode", equalTo("FT8"))
                .withQueryParam("flowStartSeconds", equalTo("-900")).withQueryParam("rronly", equalTo("1"))
                .withQueryParam("noactive", equalTo("1")).withQueryParam("rptlimit", equalTo("2000"))
                .withQueryParam("appcontact", equalTo(CONTACT))
                .withHeader("User-Agent", matching("Foresignal/.*")));
        assertThat(WIREMOCK.findAll(getRequestedFor(urlPathEqualTo(QUERY))).getFirst().getQueryParams())
                .doesNotContainKey("callsign");
    }

    @Test
    @DisplayName("watched call signs: one request each, in order; spots are merged and capped at rptlimit per request")
    void perCallsign() throws Exception {
        serve(AdapterFixtures.bytes("pskreporter-ft8", "callsign-N6AD.xml"));

        FetchResult result = adapter.fetch(harness.context(adapter, Map.of(PskReporterAdapter.APP_CONTACT, CONTACT,
                PskReporterAdapter.CALLSIGNS, List.of("n6ad", "K1JT"), PskReporterAdapter.RPTLIMIT, 20,
                PskReporterAdapter.WINDOW, 300, PskReporterAdapter.MODE, "FT4")));

        var requests = WIREMOCK.findAll(getRequestedFor(urlPathEqualTo(QUERY)));
        assertThat(requests).hasSize(2);
        assertThat(requests.get(0).queryParameter("callsign").firstValue()).isEqualTo("N6AD");
        assertThat(requests.get(1).queryParameter("callsign").firstValue()).isEqualTo("K1JT");
        assertThat(requests.get(0).queryParameter("rptlimit").firstValue()).isEqualTo("20");
        assertThat(requests.get(0).queryParameter("flowStartSeconds").firstValue()).isEqualTo("-300");
        assertThat(requests.get(0).queryParameter("mode").firstValue()).isEqualTo("FT4");
        assertThat(result.records()).as("the same 20 spots from both requests, merged").hasSize(20);
    }

    @Test
    @DisplayName("activeReceiver elements fill coverage.receivers")
    void coverage() throws Exception {
        serve(AdapterFixtures.bytes("pskreporter-ft8", "active-receivers.xml"));

        FetchResult result = adapter.fetch(harness.context(adapter, Map.of(PskReporterAdapter.APP_CONTACT, CONTACT)));

        assertThat(result.coverage().receivers()).isEqualTo(25);
        assertThat(result.coverage().actual()).isEqualTo(49);
    }

    @Test
    @DisplayName("an error status is UpstreamHttpException; a non-XML 200 is a format error")
    void errors() {
        WIREMOCK.stubFor(get(urlPathEqualTo(QUERY)).willReturn(aResponse().withStatus(500)));
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, Map.of(PskReporterAdapter.APP_CONTACT,
                CONTACT)))).isInstanceOf(UpstreamHttpException.class);

        harness.reset(adapter.id());
        serve("<html>busy</html>".getBytes());
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, Map.of(PskReporterAdapter.APP_CONTACT,
                CONTACT)))).isInstanceOf(ai.foresignal.source.spi.UpstreamFormatException.class);
    }

    @Test
    @DisplayName("checkParams: window and limit ranges, an email contact, call signs")
    void params() {
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(PskReporterAdapter.APP_CONTACT, CONTACT)))).isEmpty();
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(PskReporterAdapter.APP_CONTACT, "nobody",
                PskReporterAdapter.WINDOW, 60, PskReporterAdapter.RPTLIMIT, 9000,
                PskReporterAdapter.CALLSIGNS, List.of("not a call")))))
                .containsOnlyKeys(PskReporterAdapter.APP_CONTACT, PskReporterAdapter.WINDOW, PskReporterAdapter.RPTLIMIT,
                        PskReporterAdapter.CALLSIGNS);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(PskReporterAdapter.APP_CONTACT, CONTACT,
                PskReporterAdapter.CALLSIGNS, List.of("F5OYA/P", "VE2FVV"))))).isEmpty();
    }
}
