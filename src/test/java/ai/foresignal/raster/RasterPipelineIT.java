package ai.foresignal.raster;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.collect.ManualFetchService;
import ai.foresignal.collect.ProbeService;
import ai.foresignal.collect.ProbeResult;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SilentFormatChangeException;
import ai.foresignal.store.FetchStatus;
import ai.foresignal.stream.sse.SseClient;
import ai.foresignal.stream.sse.SseHub;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.raster.RasterFixtures;
import ai.foresignal.testsupport.raster.RasterTestSources;
import ai.foresignal.testsupport.raster.SyntheticRasterAdapter;
import ai.foresignal.testsupport.stream.SseTestClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The raster pipeline end to end (prompt 042) with the test-only {@link SyntheticRasterAdapter}: a
 * fetch commits frames through {@code ctx.rasters()}, which writes the {@code .fsg} files and
 * {@code raster_frame} rows and hands back the {@code raster-frame} records the fetch ingests; one
 * version per observation frame, one per completed forecast run, payloads under 1 KB, and SSE with
 * an {@code externalId} filter delivering the payload inline. No upstream is involved.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
@RecordApplicationEvents
class RasterPipelineIT {

    static final Instant T = Instant.parse("2026-10-10T14:00:00Z");
    static final Duration WAIT = Duration.ofSeconds(10);
    static final String OBS = SyntheticRasterAdapter.OBS;
    static final String FCST = SyntheticRasterAdapter.FCST;
    static final String SOURCE = SyntheticRasterAdapter.SOURCE;

    @LocalServerPort int port;
    @Autowired SyntheticRasterAdapter adapter;
    @Autowired ManualFetchService fetches;
    @Autowired ProbeService probes;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired RasterPaths paths;
    @Autowired RasterPipeline pipeline;
    @Autowired ProductStatusTracker status;
    @Autowired AppSettingService settings;
    @Autowired SseHub hub;
    @Autowired JsonMapper json;

    @Autowired ApplicationEvents events;

    final List<SseTestClient> streams = new ArrayList<>();

    @BeforeEach
    void setUp() {
        clock.setInstant(T.plus(Duration.ofMinutes(10)));
        adapter.clear();
        settings.invalidateCache();
        RasterFixtures.deleteTree(paths.productFrames(OBS));
        RasterFixtures.deleteTree(paths.productFrames(FCST));
    }

    @AfterEach
    void tearDown() {
        streams.forEach(SseTestClient::close);
        hub.clients().forEach(SseClient::close);
        adapter.clear();
    }

    private long versions(String externalId) {
        return jdbc.queryForObject("SELECT count(*) FROM record_version WHERE source_id = ? AND external_id = ?"
                + " AND NOT tombstone", Long.class, SOURCE, externalId);
    }

    private List<String> payloads(String externalId) {
        return jdbc.queryForList("SELECT payload::text FROM record_version WHERE source_id = ? AND external_id = ?"
                + " AND NOT tombstone ORDER BY seq", String.class, SOURCE, externalId);
    }

    @Test
    @DisplayName("each observation frame gets a row, a file and one record version with a payload under 1 KB")
    void observationFrames() {
        adapter.script(ctx -> FetchResult.of(List.of(
                ctx.rasters().commit(RasterFixtures.observation(T, 0)),
                ctx.rasters().commit(RasterFixtures.observation(T.plusSeconds(120), 1)))));
        FetchOutcome outcome = fetches.fetchNow(SOURCE);
        assertThat(outcome.status()).isEqualTo(FetchStatus.OK);

        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT frame_id, path, status, sha256, bytes, ST_AsText(footprint) AS fp, stats::text AS stats"
                        + " FROM raster_frame WHERE product_id = ? ORDER BY valid_time", OBS);
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).get("frame_id")).isEqualTo("20261010T140000Z");
        assertThat(rows.get(0).get("status")).isEqualTo("complete");
        assertThat(rows.get(0).get("path")).isEqualTo("frames/" + OBS + "/2026/10/10/20261010T140000Z.fsg");
        assertThat(paths.resolve((String) rows.get(0).get("path"))).isRegularFile();
        assertThat(rows.get(0).get("sha256")).isNotEqualTo(rows.get(1).get("sha256"));
        // The no-coverage top row is clipped from the footprint: north edge 34 − 2 = 32.
        assertThat((String) rows.get(0).get("fp")).startsWith("MULTIPOLYGON").contains("32");
        JsonNode stats = json.readTree((String) rows.get(0).get("stats"));
        assertThat(stats.get("coveragePct").asDouble()).isEqualTo(66.67);
        assertThat(stats.get("codeCounts").get("0").asLong()).isEqualTo(4);
        assertThat(stats.get("codeCounts").get("1").asLong()).isEqualTo(3);
        assertThat(stats.get("min").asDouble()).isEqualTo(-32.0);
        assertThat(stats.get("max").asDouble()).isEqualTo(30.0);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM raster_grid", Long.class)).isEqualTo(1);

        assertThat(versions(OBS)).isEqualTo(2);
        for (String payload : payloads(OBS)) {
            assertThat(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8).length).isLessThan(1024);
            JsonNode node = json.readTree(payload);
            assertThat(node.get("productId").asString()).isEqualTo(OBS);
            assertThat(node.get("frameKey").asString()).matches("^[0-9]{8}T[0-9]{6}Z~[0-9a-f]{8}$");
            assertThat(node.get("tiles").asString()).contains("/v1/products/" + OBS + "/tiles/")
                    .contains("{z}/{x}/{y}.png?style=test-reflectivity@1");
            assertThat(node.get("bounds").size()).isEqualTo(4);
            assertThat(node.get("expiresAt").asString()).isNotBlank();
        }
        String first = payloads(OBS).get(0);
        assertThat(json.readTree(first).get("timeKey").asString()).isEqualTo("20261010T140000Z");
    }

    @Test
    @DisplayName("a partial frame is stored but makes no record version")
    void partialFrame() {
        adapter.script(ctx -> {
            NormalizedRecord none = ctx.rasters().commit(RasterFixtures.partialObservation(T));
            assertThat(none).isNull();
            return FetchResult.of(List.of());
        });
        fetches.fetchNow(SOURCE);
        assertThat(jdbc.queryForObject("SELECT status FROM raster_frame WHERE product_id = ?", String.class, OBS))
                .isEqualTo("partial");
        assertThat(versions(OBS)).isZero();
    }

    @Test
    @DisplayName("a forecast run makes one version when it completes; the run-in-progress record is tombstoned")
    void forecastRun() {
        Instant run = T.minus(Duration.ofHours(2));
        adapter.script(ctx -> {
            List<NormalizedRecord> records = new ArrayList<>();
            for (int step = 0; step <= 3; step++) {
                records.add(ctx.rasters().commit(RasterFixtures.forecast(run, step)));
            }
            return FetchResult.of(records);
        });
        fetches.fetchNow(SOURCE);
        assertThat(versions(FCST)).as("no run version before the run completes").isZero();
        assertThat(versions(FCST + ":partial")).isPositive();

        adapter.script(ctx -> FetchResult.of(ctx.rasters().completeRun(FCST, run)));
        fetches.fetchNow(SOURCE);
        assertThat(versions(FCST)).isEqualTo(1);
        JsonNode payload = json.readTree(payloads(FCST).get(0));
        assertThat(payload.get("run").asString()).isEqualTo("2026-10-10T12:00:00Z");
        assertThat(payload.get("steps").toString()).isEqualTo("[0,1,2,3]");
        assertThat(payload.get("validTimes").size()).isEqualTo(4);
        assertThat(payload.get("tilesTemplate").asString()).contains("{timeKey}").contains("run=2026-10-10T12:00:00Z");
        assertThat(payloads(FCST).get(0).length()).isLessThan(1024);
        assertThat(jdbc.queryForObject("SELECT tombstone FROM record_head WHERE source_id = ? AND external_id = ?",
                Boolean.class, SOURCE, FCST + ":partial")).isTrue();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM raster_frame WHERE product_id = ? AND run_complete",
                Long.class, FCST)).isEqualTo(4);
        assertThat(jdbc.queryForObject("SELECT frame_id FROM raster_frame WHERE product_id = ? AND forecast_step = 2",
                String.class, FCST)).isEqualTo("2026101012Z-f002");
    }

    @Test
    @DisplayName("SSE with kind=raster-frame&externalId delivers the frame event with its payload inline, and only it")
    void sseExternalIdFilter() {
        SseTestClient stream = SseTestClient.connect("http://localhost:" + port,
                "/v1/changes/stream?kind=raster-frame&externalId=" + OBS, Map.of());
        streams.add(stream);
        assertThat(stream.status()).isEqualTo(200);
        Instant run = T.minus(Duration.ofHours(1));
        adapter.script(ctx -> {
            List<NormalizedRecord> records = new ArrayList<>();
            records.add(ctx.rasters().commit(RasterFixtures.forecast(run, 0)));
            records.addAll(ctx.rasters().completeRun(FCST, run));
            records.add(ctx.rasters().commit(RasterFixtures.observation(T, 0)));
            return FetchResult.of(records);
        });
        fetches.fetchNow(SOURCE);
        SseTestClient.Event event = stream.next("change", WAIT);
        assertThat(event).as("a change event").isNotNull();
        assertThat(event.data()).contains("\"payload\"").contains("\"frameKey\"").contains(OBS);
        List<SseTestClient.Event> more = stream.collect("change", Duration.ofSeconds(2));
        assertThat(more).noneMatch(e -> e.data() != null && e.data().contains(FCST));
    }

    @Test
    @DisplayName("every commit publishes RasterFrameCommittedEvent, and a reprocessed frame keeps its id with new content")
    void committedEventAndReprocessing() throws AdapterException {
        NormalizedRecord first = pipeline.commit(SOURCE, RasterFixtures.observation(T, 0));
        NormalizedRecord second = pipeline.commit(SOURCE, RasterFixtures.observation(T, 5));
        assertThat(first.payload().get("frameKey")).isNotEqualTo(second.payload().get("frameKey"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM raster_frame WHERE product_id = ?", Long.class, OBS))
                .isEqualTo(1);
        assertThat(events.stream(RasterFrameCommittedEvent.class))
                .contains(new RasterFrameCommittedEvent(OBS, "20261010T140000Z", "complete"));
    }

    @Test
    @DisplayName("a decoder's silent format change degrades health, records the reason and writes no frame")
    void silentFormatChange() throws Exception {
        Path temp = Files.createTempFile("synthetic", ".grib2");
        Files.write(temp, new byte[] {1, 2, 3});
        assertThatThrownBy(() -> pipeline.commit(SOURCE, ai.foresignal.source.spi.FrameInput.builder(OBS).validTime(T)
                .grid(RasterFixtures.GRID).file(temp, file -> {
                    throw new SilentFormatChangeException(OBS, "variable MergedReflectivityQCComposite not found");
                }).build())).isInstanceOf(SilentFormatChangeException.class);
        assertThat(status.formatChange(OBS)).isPresent();
        assertThat(status.formatChange(OBS).get().reason()).contains("MergedReflectivityQCComposite");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM raster_frame WHERE product_id = ?", Long.class, OBS))
                .isZero();
        assertThat(temp).doesNotExist();
        pipeline.commit(SOURCE, RasterFixtures.observation(T, 0));
        assertThat(status.formatChange(OBS)).as("a successful commit clears it").isEmpty();
    }

    @Test
    @DisplayName("a frame of another source's product, an unknown product or a misfit grid is refused")
    void refusals() {
        assertThatThrownBy(() -> pipeline.commit("collect-poll", RasterFixtures.observation(T, 0)))
                .isInstanceOf(AdapterException.class).hasMessageContaining("belongs to");
        assertThatThrownBy(() -> pipeline.commit(SOURCE, ai.foresignal.source.spi.FrameInput.builder("nope")
                .validTime(T).grid(RasterFixtures.GRID).bands(ai.foresignal.source.spi.DecodedBands.codes(new int[12]))
                .build())).isInstanceOf(AdapterException.class).hasMessageContaining("Unknown product");
        assertThatThrownBy(() -> pipeline.commit(SOURCE, ai.foresignal.source.spi.FrameInput.builder(OBS)
                .validTime(T).grid(RasterFixtures.GRID).bands(ai.foresignal.source.spi.DecodedBands.codes(new int[5]))
                .build())).isInstanceOf(AdapterException.class).hasMessageContaining("rejected");
        assertThatThrownBy(() -> pipeline.commit(SOURCE, ai.foresignal.source.spi.FrameInput.builder(FCST).validTime(T)
                .grid(RasterFixtures.GRID).bands(ai.foresignal.source.spi.DecodedBands.physical(new float[12])).build()))
                .isInstanceOf(AdapterException.class).hasMessageContaining("forecast");
    }

    @Test
    @DisplayName("a probe commits nothing and returns a grid summary preview")
    void probe() {
        adapter.script(ctx -> {
            assertThat(ctx.rasters().probing()).isTrue();
            return FetchResult.of(List.of(ctx.rasters().commit(RasterFixtures.observation(T, 0))));
        });
        ProbeResult result = probes.probe(SOURCE, null, null);
        assertThat(result.parseError()).isNull();
        assertThat(result.records()).hasSize(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM raster_frame", Long.class)).isZero();
        assertThat(versions(OBS)).isZero();
    }
}
