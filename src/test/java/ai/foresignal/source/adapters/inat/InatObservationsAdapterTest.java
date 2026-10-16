package ai.foresignal.source.adapters.inat;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.DefaultFetchContext;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.PlaceTimeZones;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/** {@code inat-observations} against a stubbed iNaturalist (prompt 095). */
@IntegrationTest
class InatObservationsAdapterTest extends WireMockSupport {

    private static final Instant NOON = Instant.parse("2026-10-13T12:00:00Z");

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;

    private AdapterHttpHarness harness;
    private InatObservationsAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new InatObservationsAdapter(baseUrl(), PlaceTimeZones.none(), () -> null);
        // The descriptor's own budget (2/min, 500/day): a run must fit it, not a raised test budget.
        harness = harness(adapter.descriptor().defaultBudget());
    }

    private AdapterHttpHarness harness(Budget budget) {
        AdapterHttpHarness h = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOON),
                budget);
        h.reset(InatObservationsAdapter.ID, InatObservationsAdapter.MAX_ID_KEY,
                InatObservationsAdapter.LAST_RUN_KEY, InatObservationsAdapter.FRESH_UNTIL_KEY);
        return h;
    }

    @Test
    @DisplayName("the default budget is the documented 2/min and 500/day")
    void defaultBudget() {
        assertThat(adapter.descriptor().defaultBudget()).isEqualTo(Budget.of(2, 500));
        assertThat(InatObservationsAdapter.requestsPerRun(adapter.descriptor())).isEqualTo(2);
    }

    @Test
    @DisplayName("the descriptor validates")
    void descriptorValid() {
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
    }

    @Test
    @DisplayName("a stored cursor becomes id_above and a stored run time becomes updated_since")
    void cursorAndRevisions() throws Exception {
        harness.client.stateStore(InatObservationsAdapter.ID).put(InatObservationsAdapter.MAX_ID_KEY, "999");
        harness.client.stateStore(InatObservationsAdapter.ID).put(InatObservationsAdapter.LAST_RUN_KEY,
                "2026-10-01T00:00:00Z");
        serveFixture();

        adapter.fetch(context());

        List<String> urls = WIREMOCK.getAllServeEvents().stream()
                .map(e -> e.getRequest().getUrl()).toList();
        assertThat(urls).hasSize(2);
        assertThat(urls).anySatisfy(u -> assertThat(u).contains("id_above=999").contains("order=asc"));
        // The adapter URL-encodes the ISO instant (colons as %3A); decode before matching the value.
        assertThat(urls).anySatisfy(u -> assertThat(java.net.URLDecoder.decode(u,
                        java.nio.charset.StandardCharsets.UTF_8))
                .contains("updated_since=2026-10-01T00:00:00Z").contains("order_by=updated_at"));
        assertThat(harness.client.stateStore(InatObservationsAdapter.ID)
                .get(InatObservationsAdapter.MAX_ID_KEY)).contains("1007");
    }

    @Test
    @DisplayName("a first run seeds the cursor newest-first and sends no more requests than the budget allows")
    void runFitsTheBudget() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo("/v1/observations")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(fullPage())));

        var result = adapter.fetch(context());

        assertThat(WIREMOCK.getAllServeEvents()).hasSize(2);
        assertThat(result.records()).hasSize(InatObservationsAdapter.PER_PAGE);
        assertThat(harness.client.stateStore(InatObservationsAdapter.ID).get(InatObservationsAdapter.MAX_ID_KEY))
                .contains("200");
        assertThat(harness.client.stateStore(InatObservationsAdapter.ID).get(InatObservationsAdapter.LAST_RUN_KEY))
                .contains(NOON.toString());
    }

    @Test
    @DisplayName("a page the budget refuses part-way ends the run with what was read; the cursor resumes it")
    void refusalPartWayKeepsProgress() throws Exception {
        harness = harness(Budget.of(1, 500));
        WIREMOCK.stubFor(get(urlPathEqualTo("/v1/observations")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(fullPage())));

        var result = adapter.fetch(context());

        assertThat(WIREMOCK.getAllServeEvents()).hasSize(1);
        assertThat(result.records()).hasSize(InatObservationsAdapter.PER_PAGE);
        assertThat(harness.client.stateStore(InatObservationsAdapter.ID).get(InatObservationsAdapter.MAX_ID_KEY))
                .contains("200");
    }

    @Test
    @DisplayName("Cache-Control max-age is honored: a run inside it sends nothing")
    void honorsMaxAge() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo("/v1/observations")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withHeader("Cache-Control", "public, max-age=300")
                .withBody(AdapterFixtures.text(InatObservationsAdapter.ID, "observations.json"))));
        adapter.fetch(context());
        int sent = WIREMOCK.getAllServeEvents().size();

        harness.clock.advance(java.time.Duration.ofSeconds(120));
        var inside = adapter.fetch(context());
        assertThat(inside.notModified()).isTrue();
        assertThat(WIREMOCK.getAllServeEvents()).hasSize(sent);

        harness.clock.advance(java.time.Duration.ofSeconds(240));
        adapter.fetch(context());
        assertThat(WIREMOCK.getAllServeEvents().size()).isGreaterThan(sent);
    }

    private DefaultFetchContext context() {
        return harness.context(adapter, Map.of(InatObservationsAdapter.AOI_PARAM, "-98,30,-97,31"), null);
    }

    private static void serveFixture() {
        WIREMOCK.stubFor(get(urlPathEqualTo("/v1/observations")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(AdapterFixtures.text(InatObservationsAdapter.ID, "observations.json"))));
    }

    /** A page of exactly {@value InatObservationsAdapter#PER_PAGE} observations. */
    private static String fullPage() {
        StringBuilder sb = new StringBuilder("{\"total_results\":200,\"page\":1,\"per_page\":200,\"results\":[");
        for (int i = 1; i <= InatObservationsAdapter.PER_PAGE; i++) {
            if (i > 1) {
                sb.append(',');
            }
            sb.append("{\"id\":").append(i)
                    .append(",\"updated_at\":\"2026-09-02T00:00:00Z\",\"quality_grade\":\"research\",")
                    .append("\"license_code\":\"cc0\",\"geojson\":{\"type\":\"Point\",\"coordinates\":[-97.7,30.2]},")
                    .append("\"taxon\":{\"id\":1,\"name\":\"x\",\"iconic_taxon_name\":\"Plantae\"}}");
        }
        return sb.append("]}").toString();
    }
}
