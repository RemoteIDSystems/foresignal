package ai.foresignal.source.adapters.celestrak;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.UpstreamHaltException;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/** {@code celestrak-socrates} against a stubbed CelesTrak (prompt 064): conditional GET, filters and the halt rule. */
@IntegrationTest
class CelestrakSocratesAdapterTest extends WireMockSupport {

    private static final String ID = CelestrakSocratesAdapter.ID;
    private static final String PATH = "/SOCRATES/sort-minRange.csv";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;

    private AdapterHttpHarness harness;
    private CelestrakSocratesAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install,
                new MutableClock(Instant.parse("2026-10-13T13:00:00Z")), Budget.of(100, 1000));
        harness.reset(ID, "etag:socrates", "lastmod:socrates");
        adapter = new CelestrakSocratesAdapter(baseUrl());
    }

    @Test
    @DisplayName("the default filters keep close or probable conjunctions; the ETag makes the next fetch a 304")
    void conditional() throws Exception {
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(PATH)).withHeader("If-None-Match", equalTo("\"v1\""))
                .atPriority(1).willReturn(aResponse().withStatus(304)));
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(PATH)).atPriority(5).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/octet-stream").withHeader("ETag", "\"v1\"")
                .withBody(AdapterFixtures.bytes(ID, "sort-minRange-head.csv"))));

        FetchResult first = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(first.records()).isNotEmpty().allSatisfy(r -> assertThat(
                (Double) r.payload().get("maxProb") >= 1e-5 || (Double) r.payload().get("tcaRangeKm") <= 1.0).isTrue());
        FetchResult second = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(second.notModified()).isTrue();
        WIREMOCK.verify(2, getRequestedFor(urlEqualTo(PATH)));
    }

    @Test
    @DisplayName("a 404 or an HTML body halts the source")
    void halts() {
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(PATH)).willReturn(aResponse().withStatus(404)));
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, Map.of())))
                .isInstanceOf(UpstreamHaltException.class);
        WIREMOCK.resetAll();
        harness.reset(ID);
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(PATH)).willReturn(aResponse().withStatus(200).withBody("<html/>")));
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, Map.of())))
                .isInstanceOf(UpstreamHaltException.class);
    }

    @Test
    @DisplayName("parameter checks and the descriptor")
    void descriptor() {
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("minProb", 2.0)))).containsKey("minProb");
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("maxRangeKm", -1.0)))).containsKey("maxRangeKm");
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("watchNorad", List.of("abc"))))).containsKey("watchNorad");
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("watchNorad", List.of("25544"))))).isEmpty();
        SocratesParser.Filter f = CelestrakSocratesAdapter.filter(ValidatedParams.of(Map.of("watchNorad", List.of("25544"))));
        assertThat(f.minProb()).isEqualTo(1e-5);
        assertThat(f.maxRangeKm()).isEqualTo(1.0);
        assertThat(f.watch()).containsExactly(25544L);
        var d = adapter.descriptor();
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(1, 4));
        assertThat(d.maxResponseBytes()).isEqualTo(64 * 1024 * 1024);
        assertThat(d.observedAtMode()).isEqualTo(ai.foresignal.source.spi.ObservedAtMode.RECEIVED);
        assertThat(ai.foresignal.source.DescriptorValidator.problems(d, adapter)).isEmpty();
    }
}
