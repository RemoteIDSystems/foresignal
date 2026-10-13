package ai.foresignal.raster;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.raster.model.FieldStore;
import ai.foresignal.raster.model.ModelRunIngest;
import ai.foresignal.source.adapters.gfs.GfsAdapter;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.raster.ModelFixtures;
import ai.foresignal.testsupport.raster.ProductAdapterHarness;
import ai.foresignal.testsupport.raster.RasterApiSupport;
import ai.foresignal.testsupport.raster.RasterTestSources;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * A forecast run end to end (prompt 051): the real {@code gfs} adapter and {@link ModelRunIngest}
 * decode the captured GFS 1° messages (served by an in-memory {@code SourceHttp}) and commit through
 * the real raster pipeline. Steps arrive over several fetches; each step upserts
 * {@code {productId}:partial}; a product's run makes exactly one {@code raster-frame} record (its
 * version) when its last step lands, with the {@code :partial} record tombstoned; {@code gfs} frames
 * get their God's Eye View field files; retention keeps the current and previous complete runs.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class ForecastRunIT extends RasterApiSupport {

    static final Instant RUN = Instant.parse("2026-10-12T12:00:00Z");

    @Autowired GfsAdapter gfs;
    @Autowired RasterRetentionStep retention;
    @Autowired JdbcTemplate jdbc;
    @Autowired GridStore grids;

    ModelFixtures.GfsStepHttp http;
    ProductAdapterHarness.MapState state;

    @BeforeEach
    void setUp() {
        ModelFixtures.clean(jdbc, paths);
        http = new ModelFixtures.GfsStepHttp();
        state = new ProductAdapterHarness.MapState();
        clock.setInstant(Instant.parse("2026-10-12T16:00:00Z"));
    }

    @AfterEach
    void tearDown() {
        ModelFixtures.clean(jdbc, paths);
    }

    private FetchResult fetch() throws Exception {
        ProductAdapterHarness.ParamContext ctx = new ProductAdapterHarness.ParamContext(gfs.descriptor(), http, clock,
                state, pipeline.forSource(gfs.descriptor(), false), ValidatedParams.of(Map.of(
                        ModelRunIngest.PATH_TEMPLATE, ModelFixtures.TEMPLATE_1P00, ModelRunIngest.STEPS,
                        List.of("3", "6"))));
        return gfs.fetch(ctx);
    }

    private static String key(Instant cycle, int step) {
        return ai.foresignal.raster.model.ModelCycleSelector.objectKey(ModelFixtures.TEMPLATE_1P00, cycle, step);
    }

    private static List<NormalizedRecord> live(FetchResult result, String externalId) {
        return result.records().stream().filter(r -> r.externalId().equals(externalId) && !r.tombstone()).toList();
    }

    @Test
    @DisplayName("steps arrive, :partial upserts per step, one run record only at completion, fields written")
    void runLifecycle() throws Exception {
        http.publish(key(RUN, 3));
        FetchResult first = fetch();
        assertThat(live(first, "gfs")).as("no run version before the run completes").isEmpty();
        assertThat(live(first, "gfs:partial")).hasSize(1);
        assertThat(live(first, "gfs:partial").get(0).payload()).containsEntry("steps", List.of(3));
        assertThat(first.records()).extracting(NormalizedRecord::externalId).containsExactly("gfs:partial",
                "gfs-gust:partial", "gfs-t2m:partial", "gfs-apcp:partial");
        assertThat(frames.runFrames("gfs", RUN, false)).extracting(RasterFrameRow::frameId)
                .containsExactly("2026101212Z-f003");
        assertThat(frames.runs("gfs").get(0).complete()).isFalse();

        http.publish(key(RUN, 6));
        FetchResult second = fetch();
        List<NormalizedRecord> run = live(second, "gfs");
        assertThat(run).as("exactly one run record").hasSize(1);
        assertThat(run.get(0).observedAt()).isEqualTo(RUN);
        assertThat(run.get(0).payload()).containsEntry("run", "2026-10-12T12:00:00Z").containsEntry("steps", List.of(3, 6))
                .containsEntry("validTimes", List.of("2026-10-12T15:00:00Z", "2026-10-12T18:00:00Z"));
        assertThat(second.records()).filteredOn(r -> r.externalId().equals("gfs:partial") && r.tombstone()).hasSize(1);
        for (String product : List.of("gfs-gust", "gfs-t2m", "gfs-apcp")) {
            assertThat(live(second, product)).as(product).hasSize(1);
        }
        assertThat(frames.runs("gfs").get(0).complete()).isTrue();

        RasterFrameRow f006 = frames.find("gfs", "2026101212Z-f006").orElseThrow();
        assertThat(f006.referenceTime()).isEqualTo(RUN);
        assertThat(f006.forecastStep()).isEqualTo(6);
        assertThat(f006.dtype()).isEqualTo("i16");
        Map<String, FieldStore.StoredField> files = FieldStore.fromStats(f006.stats());
        assertThat(files).containsOnlyKeys("1", "0.25");
        assertThat(files.get("1").byteLength()).isEqualTo(521_280);
        assertThat(files.get("0.25").byteLength()).isEqualTo(8_305_920);
        assertThat(files.get("1").path()).isEqualTo("fields/gfs/2026101212Z/f006.1p00.f32.gz");
        assertThat(paths.resolve(files.get("1").path())).isRegularFile();
        assertThat(grids.find(f006.gridId()).orElseThrow().params()).containsEntry("dx", 1.0);
        assertThat(frames.find("gfs-gust", "2026101212Z-f006").orElseThrow().stats())
                .as("scalars get no field files").doesNotContainKey(FieldStore.STATS_KEY);

        List<String> before = http.requests();
        FetchResult third = fetch();
        assertThat(third.records()).as("a complete run is never read again").isEmpty();
        assertThat(http.requests()).as("both candidate cycles are complete or older: no request").hasSize(before.size());
    }

    @Test
    @DisplayName("the ladder keeps the current and previous complete runs; an older run's frames go")
    void ladderKeepsTwoRuns() throws Exception {
        Instant[] runs = {RUN, Instant.parse("2026-10-12T18:00:00Z"), Instant.parse("2026-10-13T00:00:00Z")};
        for (Instant r : runs) {
            clock.setInstant(r.plusSeconds(4 * 3600));
            http.publish(key(r, 3)).publish(key(r, 6));
            fetch();
            assertThat(frames.runs("gfs").get(0).run()).isEqualTo(r);
            assertThat(frames.runs("gfs").get(0).complete()).isTrue();
        }
        retention.thin(clock.instant());
        assertThat(frames.runs("gfs")).extracting(RasterFrameQueries.RunSummary::run).containsExactly(runs[2], runs[1]);
        assertThat(frames.runs("gfs-apcp")).hasSize(2);
        assertThat(state.asMap()).as("state of cycles outside the window is dropped")
                .doesNotContainKey("done:2026101212Z").containsEntry("complete:gfs", "2026101300Z");
    }
}
