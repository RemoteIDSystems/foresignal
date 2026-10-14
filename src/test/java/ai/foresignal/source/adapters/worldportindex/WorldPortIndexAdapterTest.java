package ai.foresignal.source.adapters.worldportindex;

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
import java.net.URI;
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

/** {@code world-port-index} against a stubbed NGA host (prompt 094). */
@IntegrationTest
class WorldPortIndexAdapterTest extends WireMockSupport {

    private static final String ID = WorldPortIndexAdapter.ID;
    private static final Instant NOW = Instant.parse("2026-10-14T10:00:00Z");
    private static final String LAST_MODIFIED = "Tue, 13 Oct 2026 12:00:00 GMT";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;

    private AdapterHttpHarness harness;
    private WorldPortIndexAdapter adapter;
    private final List<NormalizedRecord> collected = new ArrayList<>();

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000));
        harness.reset(ID, WorldPortIndexAdapter.STATE_KEY);
        adapter = new WorldPortIndexAdapter(baseUrl());
        collected.clear();
    }

    private String path() {
        return URI.create(adapter.wpiUri().toString()).getPath();
    }

    @Test
    @DisplayName("a 200 parses ports and areas and reports the Last-Modified release")
    void fetch() throws Exception {
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(path())).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withHeader("Last-Modified", LAST_MODIFIED)
                .withBody(AdapterFixtures.bytes(ID, "ports.json"))));
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of(), null, null, collected::addAll));
        assertThat(result.complete()).isTrue();
        assertThat(result.release()).isEqualTo(LAST_MODIFIED);
        assertThat(collected).hasSize(7);
        assertThat(collected).extracting(NormalizedRecord::kind)
                .contains(WpiParser.PORT_KIND, WpiParser.AREA_KIND);
    }
}
