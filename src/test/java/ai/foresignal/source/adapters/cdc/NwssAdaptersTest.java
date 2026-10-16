package ai.foresignal.source.adapters.cdc;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CountyGeometries;
import ai.foresignal.source.spi.CurrentRecords;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/** The CDC NWSS adapters against a stubbed Socrata (prompt 095). */
@IntegrationTest
class NwssAdaptersTest extends WireMockSupport {

    private static final Instant NOON = Instant.parse("2026-10-13T12:00:00Z");

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;

    private AdapterHttpHarness harness;
    private NwssWvalAdapter wval;
    private NwssDetectionAdapter detections;

    @BeforeEach
    void setUp() {
        wval = new NwssWvalAdapter(baseUrl(), CountyGeometries.none());
        detections = new NwssDetectionAdapter(baseUrl(), CountyGeometries.none());
        // The descriptors' own budget (4/min, 100/day): a run must fit it, not a raised test budget.
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOON),
                wval.descriptor().defaultBudget());
        harness.reset(NwssWvalAdapter.ID, NwssWvalAdapter.CURSOR_KEY, NwssWvalAdapter.CURSOR_KEY + ":skip");
        harness.reset(NwssDetectionAdapter.ID, NwssDetectionAdapter.NEXT_DATASET_KEY);
        for (String dataset : NwssDetectionAdapter.DATASETS.values()) {
            harness.reset(NwssDetectionAdapter.ID, NwssDetectionAdapter.cursorKey(dataset),
                    NwssDetectionAdapter.cursorKey(dataset) + ":skip");
        }
        harness.credentials.put(NwssWvalAdapter.APP_TOKEN, "app-token");
    }

    @Test
    @DisplayName("both descriptors validate")
    void descriptorsValidate() {
        assertThat(DescriptorValidator.problems(wval.descriptor(), wval)).isEmpty();
        assertThat(DescriptorValidator.problems(detections.descriptor(), detections)).isEmpty();
    }

    @Test
    @DisplayName("the stored date_updated cursor becomes a $where and the app token is sent")
    void cursorAndToken() throws Exception {
        harness.client.stateStore(NwssWvalAdapter.ID).put(NwssWvalAdapter.CURSOR_KEY, "2026-09-04T00:00:00.000");
        serve(NwssWvalParser.DATASET, 200, AdapterFixtures.text(NwssWvalAdapter.ID, "wval.json"));

        wval.fetch(harness.context(wval, Map.of()));

        var event = WIREMOCK.getAllServeEvents().get(0).getRequest();
        assertThat(event.getUrl()).contains("$where=").contains("date_updated%20%3E%3D%20%272026-09-04")
                .contains("%3Aid%20ASC");
        assertThat(event.getHeader("X-App-Token")).isEqualTo("app-token");
        assertThat(harness.client.stateStore(NwssWvalAdapter.ID).get(NwssWvalAdapter.CURSOR_KEY))
                .contains("2026-09-11T00:00:00.000");
    }

    @Test
    @DisplayName("a backlog larger than the budget is read over several runs without skipping same-timestamp rows")
    void resumesWithinOneTimestamp() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo("/resource/" + NwssWvalParser.DATASET + ".json"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(fullPage("2026-09-11T00:00:00.000"))));

        wval.fetch(harness.context(wval, Map.of()));

        assertThat(WIREMOCK.getAllServeEvents()).hasSize(4);
        var state = harness.client.stateStore(NwssWvalAdapter.ID);
        assertThat(state.get(NwssWvalAdapter.CURSOR_KEY)).contains("2026-09-11T00:00:00.000");
        assertThat(state.get(NwssWvalAdapter.CURSOR_KEY + ":skip")).contains(Integer.toString(4 * SodaClient.LIMIT));

        WIREMOCK.resetRequests();
        harness.clock.advance(java.time.Duration.ofMinutes(5));
        wval.fetch(harness.context(wval, Map.of()));

        String first = WIREMOCK.getAllServeEvents().stream().map(e -> e.getRequest().getUrl())
                .filter(u -> u.contains("$offset=" + 4 * SodaClient.LIMIT)).findFirst().orElseThrow();
        assertThat(first).contains("date_updated%20%3E%3D%20%272026-09-11T00%3A00%3A00.000%27");
    }

    @Test
    @DisplayName("more datasets than requests: each run reads within the budget and the next starts where it stopped")
    void detectionsRoundRobin() throws Exception {
        for (String dataset : NwssDetectionAdapter.DATASETS.values()) {
            serve(dataset, 200, "[]");
        }
        Map<String, Object> all = Map.of(NwssDetectionAdapter.PATHOGENS_PARAM, NwssDetectionAdapter.ALL_PATHOGENS);

        detections.fetch(harness.context(detections, all));
        assertThat(WIREMOCK.getAllServeEvents()).hasSize(4);
        assertThat(harness.client.stateStore(NwssDetectionAdapter.ID).get(NwssDetectionAdapter.NEXT_DATASET_KEY))
                .contains("4");

        WIREMOCK.resetRequests();
        harness.clock.advance(java.time.Duration.ofMinutes(5));
        detections.fetch(harness.context(detections, all));
        assertThat(WIREMOCK.getAllServeEvents()).extracting(e -> e.getRequest().getUrl())
                .anySatisfy(u -> assertThat(u).contains("/resource/45cq-cw4i.json"))
                .anySatisfy(u -> assertThat(u).contains("/resource/j9g8-acpt.json"));
    }

    @Test
    @DisplayName("a re-dated older week does not replace the stored week")
    void rejectsOlderWeek() throws Exception {
        java.util.List<CurrentRecords.Row> stored = java.util.List.of(new CurrentRecords.Row(NwssWvalAdapter.ID,
                "CA-1:SARS-CoV-2", NwssWvalAdapter.KIND, NOON, Map.of("weekEnd", "2026-09-12T00:00:00Z")));
        CurrentRecords view = new CurrentRecords() {
            @Override
            public java.util.List<Row> current(java.util.List<String> sources, java.util.List<String> kinds,
                    int limit) {
                return stored;
            }

            @Override
            public String health(String sourceId) {
                return "HEALTHY";
            }
        };
        NwssWvalAdapter withHeads = new NwssWvalAdapter(new SodaClient(baseUrl()), CountyGeometries.none(),
                () -> view);
        serve(NwssWvalParser.DATASET, 200, AdapterFixtures.text(NwssWvalAdapter.ID, "wval.json"));

        var result = withHeads.fetch(harness.context(withHeads, Map.of()));

        assertThat(result.records()).extracting(r -> r.externalId())
                .doesNotContain("CA-1:SARS-CoV-2")
                .contains("TX-1:Influenza A");
    }

    /** A page of exactly {@value SodaClient#LIMIT} rows sharing one {@code date_updated}. */
    private static String fullPage(String updated) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < SodaClient.LIMIT; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append("{\"site\":\"S").append(i).append("\",\"pathogen_target\":\"RSV\",\"wval\":\"1.0\",")
                    .append("\"week_end\":\"2026-09-05\",\"date_updated\":\"").append(updated)
                    .append("\",\"source\":\"CDC\",\"state\":\"CA\"}");
        }
        return sb.append(']').toString();
    }

    @Test
    @DisplayName("each configured pathogen dataset is read once")
    void detectionDatasets() throws Exception {
        serve("mtpu-urpp", 200, "[]");
        serve("akvg-8vrb", 200, AdapterFixtures.text(NwssDetectionAdapter.ID, "detections.json"));
        serve("xpxn-rzgz", 200, "[]");

        detections.fetch(harness.context(detections, Map.of()));

        assertThat(WIREMOCK.getAllServeEvents()).hasSize(3);
        assertThat(WIREMOCK.getAllServeEvents()).extracting(e -> e.getRequest().getUrl())
                .anySatisfy(u -> assertThat(u).contains("/resource/mtpu-urpp.json"))
                .anySatisfy(u -> assertThat(u).contains("/resource/akvg-8vrb.json"))
                .anySatisfy(u -> assertThat(u).contains("/resource/xpxn-rzgz.json"));
    }

    private static void serve(String dataset, int status, String body) {
        WIREMOCK.stubFor(get(urlPathEqualTo("/resource/" + dataset + ".json")).willReturn(aResponse().withStatus(status)
                .withHeader("Content-Type", "application/json").withBody(body)));
    }
}
