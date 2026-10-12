package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import ai.foresignal.source.SourceService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.adapters.FakePollingAdapter;
import ai.foresignal.testsupport.adapters.FakeStreamingAdapter;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.collect.ControllablePollingAdapter;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Invariant SRC-6: a client request never causes an upstream fetch.
 *
 * <p>With the scheduler running and every source disabled, client reads of every kind — the API,
 * the spec, actuator — and the source service's own reads leave every adapter untouched. The
 * read endpoints themselves arrive in later prompts; the test hits their paths now, so it keeps
 * holding as they are built.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
@TestPropertySource(properties = "foresignal.collector.scheduled=true")
class NoClientTriggeredFetchIT {

    private static final List<String> CLIENT_PATHS = List.of(
            "/v1/sources",
            "/v1/sources/" + CollectorTestSources.POLL,
            "/v1/sources/" + CollectorTestSources.POLL + "/health",
            "/v1/records",
            "/v1/changes",
            "/v1/openapi.json",
            // The raster product catalog and frames (prompt 042).
            "/v1/products",
            "/v1/products/test-synth-obs",
            "/v1/products/test-synth-obs/frames",
            "/v1/products/test-synth-obs/frames/latest",
            // Tiles, images, loops, styles and legends (prompt 045).
            "/v1/products/test-synth-obs/tiles/latest/4/3/6.png",
            "/v1/products/test-synth-obs/tiles/latest/4/3/6@2x.png",
            "/v1/products/test-synth-obs/tilejson",
            "/v1/products/test-synth-obs/image/latest.png?bbox=-100,28,-94,34",
            "/v1/products/loop?products=test-synth-obs&bbox=-100,28,-94,34",
            "/v1/products/gibs-goes-east-geocolor",
            "/v1/styles",
            "/v1/styles/nws-reflectivity@1",
            "/v1/products/test-synth-obs/legend",
            "/v1/products/test-synth-obs/legend.png",
            // The media store (prompt 043); an unknown id still runs the whole read path.
            "/v1/media/01JA8Q3V7S2K9W4T6Y1B5N0M3C",
            "/v1/media/01JA8Q3V7S2K9W4T6Y1B5N0M3C/thumbnail?w=256",
            "/v1/media/01JA8Q3V7S2K9W4T6Y1B5N0M3C/meta",
            "/actuator/health");

    @Autowired private MockMvc mvc;
    @Autowired private CollectorScheduler scheduler;
    @Autowired private SourceService sources;
    @Autowired private ControllablePollingAdapter collectPollAdapter;
    @Autowired private ControllablePollingAdapter collectKeyedAdapter;
    @Autowired private FakePollingAdapter fakePollingAdapter;
    @Autowired private FakeStreamingAdapter fakeStreamingAdapter;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        scheduler.reconcileNow();
        await().atMost(Duration.ofSeconds(10)).until(() -> scheduler.state().scheduledSources().isEmpty());
        collectPollAdapter.reset();
        collectKeyedAdapter.reset();
        fakePollingAdapter.reset();
        fakeStreamingAdapter.reset();
    }

    @Test
    @DisplayName("client reads never reach an adapter")
    void clientReadsDoNotFetch() throws Exception {
        for (int round = 0; round < 3; round++) {
            for (String path : CLIENT_PATHS) {
                mvc.perform(get(path));
            }
        }
        sources.list();
        sources.view(CollectorTestSources.POLL);

        await().during(Duration.ofMillis(2500)).atMost(Duration.ofSeconds(4)).until(() ->
                collectPollAdapter.fetchCount() == 0
                        && collectKeyedAdapter.fetchCount() == 0
                        && fakePollingAdapter.fetchCount() == 0
                        && fakeStreamingAdapter.startCount() == 0);
        assertThat(jdbc.queryForObject("select count(*) from fetch_log", Integer.class)).isZero();
        assertThat(scheduler.isRunning()).isTrue();
    }
}
