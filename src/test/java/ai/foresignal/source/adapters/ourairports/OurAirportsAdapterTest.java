package ai.foresignal.source.adapters.ourairports;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/** {@code ourairports} against a stubbed OurAirports host (prompt 094). */
@IntegrationTest
class OurAirportsAdapterTest extends WireMockSupport {

    private static final String ID = OurAirportsAdapter.ID;
    private static final Instant NOW = Instant.parse("2026-10-14T10:00:00Z");

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;

    private AdapterHttpHarness harness;
    private OurAirportsAdapter adapter;
    private final List<NormalizedRecord> collected = new ArrayList<>();

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000));
        harness.reset(ID, OurAirportsAdapter.AIRPORTS_STATE, OurAirportsAdapter.NAVAIDS_STATE,
                OurAirportsAdapter.CHART_STATE);
        adapter = new OurAirportsAdapter(baseUrl());
        collected.clear();
    }

    private void serve(String url, String fixture) {
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(java.net.URI.create(url).getPath()))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/csv")
                        .withBody(AdapterFixtures.bytes(ID, fixture))));
    }

    /** Serves the runways and frequencies files the airports are drawn from. */
    private void serveChart() {
        serve(adapter.runwaysUri().toString(), "runways.csv");
        serve(adapter.frequenciesUri().toString(), "airport-frequencies.csv");
    }

    /** Answers 304 to a conditional request for {@code url} and the fixture, with an ETag, to any other. */
    private void serveWithEtag(String url, String fixture, String etag) {
        String path = java.net.URI.create(url).getPath();
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(path)).atPriority(2)
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/csv")
                        .withHeader("ETag", etag).withBody(AdapterFixtures.bytes(ID, fixture))));
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(path)).atPriority(1)
                .withHeader("If-None-Match", WireMock.equalTo(etag))
                .willReturn(aResponse().withStatus(304)));
    }

    @Test
    @DisplayName("airports only: the default types parse with their chart symbols and the run is complete")
    void airports() throws Exception {
        serve(adapter.airportsUri().toString(), "airports.csv");
        serveChart();
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of(), null, null, collected::addAll));
        assertThat(result.complete()).isTrue();
        assertThat(collected).extracting(NormalizedRecord::externalId).containsExactlyInAnyOrder("KJFK", "EGLL",
                "LFPG", "ZZZZ");
        assertThat(collected).filteredOn(r -> r.externalId().equals("KJFK")).singleElement()
                .satisfies(r -> assertThat(r.payload()).containsEntry("chartSymbol", "major")
                        .containsEntry("towered", true));
    }

    @Test
    @DisplayName("an unchanged airports.csv is one request; a changed one fetches runways and frequencies whole")
    void airportsFileDecides() throws Exception {
        serveWithEtag(adapter.airportsUri().toString(), "airports.csv", "\"a1\"");
        serveChart();
        assertThat(adapter.fetch(harness.context(adapter, Map.of(), null, null, collected::addAll)).complete())
                .isTrue();
        WIREMOCK.resetRequests();
        assertThat(adapter.fetch(harness.context(adapter, Map.of(), null, null, collected::addAll)).notModified())
                .as("airports.csv answered 304").isTrue();
        assertThat(WIREMOCK.findAll(WireMock.anyRequestedFor(WireMock.anyUrl()))).hasSize(1);

        collected.clear();
        WIREMOCK.resetRequests();
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(adapter.airportsUri().getPath())).atPriority(0)
                .withHeader("If-None-Match", WireMock.equalTo("\"a1\""))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/csv")
                        .withHeader("ETag", "\"a2\"").withBody(AdapterFixtures.bytes(ID, "airports.csv"))));
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of(), null, null, collected::addAll));
        assertThat(result.complete()).isTrue();
        assertThat(WIREMOCK.findAll(WireMock.anyRequestedFor(WireMock.anyUrl()))).hasSize(3);
        assertThat(collected).filteredOn(r -> r.externalId().equals("KJFK")).singleElement()
                .satisfies(r -> assertThat(r.payload()).containsEntry("chartSymbol", "major"));
    }

    @Test
    @DisplayName("an install whose airports predate the chart symbols fetches airports.csv whole once, then revalidates")
    void upgradeRebuildsOnce() throws Exception {
        serveWithEtag(adapter.airportsUri().toString(), "airports.csv", "\"a1\"");
        serveChart();
        assertThat(adapter.fetch(harness.context(adapter, Map.of(), null, null, collected::addAll)).complete())
                .isTrue();
        // An install upgraded from the build without chart symbols: the airports' validator, no chart marker.
        harness.client.stateStore(ID).remove(OurAirportsAdapter.CHART_STATE);
        collected.clear();
        FetchResult upgraded = adapter.fetch(harness.context(adapter, Map.of(), null, null, collected::addAll));
        assertThat(upgraded.complete()).as("fetched whole despite the stored ETag").isTrue();
        assertThat(collected).filteredOn(r -> r.externalId().equals("KJFK")).singleElement()
                .satisfies(r -> assertThat(r.payload()).containsEntry("chartSymbol", "major"));
        assertThat(adapter.fetch(harness.context(adapter, Map.of(), null, null, collected::addAll)).notModified())
                .as("then revalidated as before").isTrue();
    }

    @Test
    @DisplayName("the default budget covers a whole run with navaids, since one refused request refuses the run")
    void budgetCoversRun() {
        assertThat(adapter.descriptor().defaultBudget().perMinute()).isGreaterThanOrEqualTo(4);
    }

    @Test
    @DisplayName("a failed runways.csv fails the run rather than drawing every airport by its type")
    void runwaysMissing() {
        serve(adapter.airportsUri().toString(), "airports.csv");
        serve(adapter.frequenciesUri().toString(), "airport-frequencies.csv");
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(java.net.URI.create(adapter.runwaysUri().toString()).getPath()))
                .willReturn(aResponse().withStatus(500)));
        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> adapter.fetch(harness.context(adapter, Map.of(), null, null, collected::addAll)))
                .isInstanceOf(ai.foresignal.collect.UpstreamHttpException.class);
        assertThat(collected).isEmpty();
    }

    @Test
    @DisplayName("navaids=true adds the navaids file as kind navaid")
    void withNavaids() throws Exception {
        serve(adapter.airportsUri().toString(), "airports.csv");
        serveChart();
        serve(adapter.navaidsUri().toString(), "navaids.csv");
        FetchResult result = adapter.fetch(
                harness.context(adapter, Map.of("navaids", true), null, null, collected::addAll));
        assertThat(result.complete()).isTrue();
        assertThat(collected).extracting(NormalizedRecord::kind).contains(OurAirportsParser.NAVAID_KIND);
        assertThat(collected).extracting(NormalizedRecord::externalId).contains("navaid:1", "navaid:2");
        assertThat(result.reconcileKinds()).containsExactlyInAnyOrder(
                OurAirportsParser.AIRPORT_KIND, OurAirportsParser.NAVAID_KIND);
    }

    @Test
    @DisplayName("a 304 on airports.csv still reconciles the navaids, and leaves the airports alone")
    void airportsUnchanged() throws Exception {
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(adapter.airportsUri().getPath()))
                .willReturn(aResponse().withStatus(304)));
        serve(adapter.navaidsUri().toString(), "navaids.csv");
        FetchResult result = adapter.fetch(
                harness.context(adapter, Map.of("navaids", true), null, null, collected::addAll));
        assertThat(result.complete()).isTrue();
        assertThat(result.reconcileKinds()).containsExactly(OurAirportsParser.NAVAID_KIND);
        assertThat(collected).extracting(NormalizedRecord::kind).containsOnly(OurAirportsParser.NAVAID_KIND);
    }
}
