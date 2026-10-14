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
        harness.reset(ID, OurAirportsAdapter.AIRPORTS_STATE, OurAirportsAdapter.NAVAIDS_STATE);
        adapter = new OurAirportsAdapter(baseUrl());
        collected.clear();
    }

    private void serve(String url, String fixture) {
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(java.net.URI.create(url).getPath()))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/csv")
                        .withBody(AdapterFixtures.bytes(ID, fixture))));
    }

    @Test
    @DisplayName("airports only: the default types parse and the run is complete")
    void airports() throws Exception {
        serve(adapter.airportsUri().toString(), "airports.csv");
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of(), null, null, collected::addAll));
        assertThat(result.complete()).isTrue();
        assertThat(collected).extracting(NormalizedRecord::externalId).containsExactlyInAnyOrder("KJFK", "EGLL",
                "LFPG", "ZZZZ");
    }

    @Test
    @DisplayName("navaids=true adds the navaids file as kind navaid")
    void withNavaids() throws Exception {
        serve(adapter.airportsUri().toString(), "airports.csv");
        serve(adapter.navaidsUri().toString(), "navaids.csv");
        FetchResult result = adapter.fetch(
                harness.context(adapter, Map.of("navaids", true), null, null, collected::addAll));
        assertThat(result.complete()).isTrue();
        assertThat(collected).extracting(NormalizedRecord::kind).contains(OurAirportsParser.NAVAID_KIND);
        assertThat(collected).extracting(NormalizedRecord::externalId).contains("navaid:1", "navaid:2");
    }
}
