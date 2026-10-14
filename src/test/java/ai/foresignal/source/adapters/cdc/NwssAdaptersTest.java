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
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOON),
                Budget.of(100, 10_000));
        harness.reset(NwssWvalAdapter.ID, NwssWvalAdapter.CURSOR_KEY, NwssDetectionAdapter.ID,
                NwssDetectionAdapter.cursorKey("mtpu-urpp"), NwssDetectionAdapter.cursorKey("akvg-8vrb"),
                NwssDetectionAdapter.cursorKey("xpxn-rzgz"));
        harness.credentials.put(NwssWvalAdapter.APP_TOKEN, "app-token");
        wval = new NwssWvalAdapter(baseUrl(), CountyGeometries.none());
        detections = new NwssDetectionAdapter(baseUrl(), CountyGeometries.none());
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
        assertThat(event.getUrl()).contains("$where=").contains("date_updated%20%3E%20%272026-09-04");
        assertThat(event.getHeader("X-App-Token")).isEqualTo("app-token");
        assertThat(harness.client.stateStore(NwssWvalAdapter.ID).get(NwssWvalAdapter.CURSOR_KEY))
                .contains("2026-09-11T00:00:00.000");
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
