package ai.foresignal.source.adapters.wri;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/** {@code wri-power-plants} against a stubbed GitHub raw host (prompt 094). */
@IntegrationTest
class WriPowerPlantsAdapterTest extends WireMockSupport {

    private static final String ID = WriPowerPlantsAdapter.ID;
    private static final Instant NOW = Instant.parse("2026-10-14T10:00:00Z");
    private static final String ETAG = "\"gppd-1.3.0\"";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;

    private AdapterHttpHarness harness;
    private WriPowerPlantsAdapter adapter;
    private final List<NormalizedRecord> collected = new ArrayList<>();

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000));
        harness.reset(ID, WriPowerPlantsAdapter.STATE_KEY);
        adapter = new WriPowerPlantsAdapter(baseUrl());
        collected.clear();
    }

    private String path() {
        return adapter.csvUri().getPath();
    }

    private void serveCsv() {
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(path())).atPriority(5)
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/csv")
                        .withHeader("ETag", ETAG).withBody(AdapterFixtures.bytes(ID, "gppd.csv"))));
    }

    @Test
    @DisplayName("a 200 parses the CSV into chunks and reports the release")
    void fetch() throws Exception {
        serveCsv();
        FetchResult result = adapter.fetch(harness.context(adapter, java.util.Map.of(), null, null, collected::addAll));

        assertThat(result.complete()).isTrue();
        assertThat(result.release()).isEqualTo(ETAG);
        assertThat(collected).hasSize(3);
        assertThat(result.records()).isEmpty();
    }

    @Test
    @DisplayName("a 304 is unchanged and ingests nothing")
    void notModified() throws Exception {
        serveCsv();
        adapter.fetch(harness.context(adapter, java.util.Map.of(), null, null, collected::addAll));
        collected.clear();
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(path())).atPriority(1)
                .withHeader("If-None-Match", equalTo(ETAG))
                .willReturn(aResponse().withStatus(304).withHeader("ETag", ETAG)));

        FetchResult result = adapter.fetch(harness.context(adapter, java.util.Map.of(), null, null, collected::addAll));
        assertThat(result.notModified()).isTrue();
        assertThat(collected).isEmpty();
    }

    @Test
    @DisplayName("a non-200 is a format error and forgets the validators")
    void badStatus() {
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(path())).willReturn(aResponse().withStatus(500)));
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, java.util.Map.of())))
                .isInstanceOf(UpstreamHttpException.class);
    }

    @Test
    @DisplayName("the descriptor validates")
    void descriptor() {
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
    }
}
