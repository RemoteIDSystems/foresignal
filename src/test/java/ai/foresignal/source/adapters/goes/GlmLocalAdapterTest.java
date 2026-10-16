package ai.foresignal.source.adapters.goes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.raster.RasterPaths;
import ai.foresignal.raster.decode.NetcdfDecoder;
import ai.foresignal.raster.lightning.FlashArchive;
import ai.foresignal.raster.lightning.FlashStore;
import ai.foresignal.source.InboxFiles;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.CurrentRecords;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.InboxAccess;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.RasterSink;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.weather.FlashTestSink;
import ai.foresignal.testsupport.weather.WeatherAdapterHarness;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * {@code glm-local-{station}} (prompt 107): the per-file settle check, a young file that does not parse
 * retried rather than skipped past, an old unreadable file skipped, and every local cluster ended when
 * the internet {@code goes-glm} feed recovers.
 */
class GlmLocalAdapterTest {

    static final Instant NOW = Instant.parse("2026-10-16T12:00:00Z");

    @TempDir Path temp;

    Path inbox;
    Path dir;
    MutableClock clock;
    WeatherAdapterHarness.MapState state;
    String goesGlmHealth = "FAILING";
    GlmLocalAdapter adapter;
    FetchContext ctx;

    @BeforeEach
    void setUp() throws Exception {
        inbox = Files.createDirectories(temp.resolve("inbox"));
        dir = Files.createDirectories(inbox.resolve("satdump/home/glm"));
        clock = new MutableClock(NOW);
        state = new WeatherAdapterHarness.MapState();
        RasterPaths paths = new RasterPaths(temp.resolve("data"));
        Files.createDirectories(paths.tmp());
        CurrentRecords records = new CurrentRecords() {
            @Override
            public List<Row> current(List<String> sources, List<String> kinds, int limit) {
                return List.of();
            }

            @Override
            public String health(String sourceId) {
                return goesGlmHealth;
            }
        };
        adapter = new GlmLocalAdapter("home", mock(NetcdfDecoder.class), paths, records);
        InboxFiles files = new InboxFiles(new ForesignalProperties(temp, inbox, false, null));
        FlashTestSink sink = new FlashTestSink(new FlashStore(new FlashArchive(temp.resolve("glm")), null,
                FlashStore.Scope.AOI, clock), false);
        Path real = dir.toRealPath();
        SourceDescriptor descriptor = adapter.descriptor();
        ctx = new FetchContext() {
            @Override
            public String sourceId() {
                return descriptor.id();
            }

            @Override
            public SourceDescriptor descriptor() {
                return descriptor;
            }

            @Override
            public SourceHttp http() {
                throw new UnsupportedOperationException();
            }

            @Override
            public ValidatedParams params() {
                return ValidatedParams.of(Map.of(GlmLocalAdapter.INBOX_PATH, real));
            }

            @Override
            public Map<String, String> credentials() {
                return Map.of();
            }

            @Override
            public Clock clock() {
                return clock;
            }

            @Override
            public Optional<Instant> lastSuccessAt() {
                return Optional.empty();
            }

            @Override
            public AdapterStateStore state() {
                return state;
            }

            @Override
            public RasterSink rasters() {
                return sink;
            }

            @Override
            public InboxAccess inbox() {
                return files;
            }
        };
    }

    Path file(String name, Instant modified) throws Exception {
        Path f = dir.resolve(name);
        Files.writeString(f, "not a NetCDF file");
        Files.setLastModifiedTime(f, FileTime.from(modified));
        return f;
    }

    @Test
    @DisplayName("settled after 10 s unmodified; a parse failure is retried while the file is younger than 2 min")
    void helpers() {
        InboxAccess.FileStat young = new InboxAccess.FileStat(dir, 1, NOW.minusSeconds(5));
        InboxAccess.FileStat settled = new InboxAccess.FileStat(dir, 1, NOW.minusSeconds(30));
        InboxAccess.FileStat old = new InboxAccess.FileStat(dir, 1, NOW.minusSeconds(600));
        assertThat(GlmLocalAdapter.settled(young, NOW)).isFalse();
        assertThat(GlmLocalAdapter.settled(settled, NOW)).isTrue();
        assertThat(GlmLocalAdapter.retryable(settled, NOW)).isTrue();
        assertThat(GlmLocalAdapter.retryable(old, NOW)).isFalse();
    }

    @Test
    @DisplayName("a file still being written is not read and the watermark does not pass it")
    void unsettledFileWaits() throws Exception {
        file("OR_GLM-L2-LCFA_G19_s1.nc", NOW.minusSeconds(2));
        adapter.fetch(ctx);
        assertThat(state.get(GlmLocalAdapter.LAST_MTIME)).isEmpty();
    }

    @Test
    @DisplayName("a young file that does not parse is retried next run; an old one is skipped")
    void youngUnreadableRetried() throws Exception {
        file("OR_GLM-L2-LCFA_G19_s1.nc", NOW.minusSeconds(30));
        adapter.fetch(ctx);
        assertThat(state.get(GlmLocalAdapter.LAST_MTIME)).isEmpty();

        clock.advance(java.time.Duration.ofMinutes(5));
        adapter.fetch(ctx);
        assertThat(state.get(GlmLocalAdapter.LAST_MTIME)).contains(NOW.minusSeconds(30).toString());
    }

    @Test
    @DisplayName("when goes-glm recovers, every open local cluster is ended with a tombstone")
    void clustersEndWhenInternetRecovers() throws Exception {
        long t = NOW.minusSeconds(120).toEpochMilli();
        state.put(GlmLocalAdapter.CLUSTERS, "lc-01ABC|30.3|-97.7|" + t + "|" + t + "|0|1\n"
                + "lc-01DEF|31.3|-97.7|" + t + "|" + t + "|0|0");
        goesGlmHealth = "HEALTHY";
        FetchResult result = adapter.fetch(ctx);
        assertThat(result.records()).extracting(NormalizedRecord::externalId).containsExactly("lc-01ABC");
        assertThat(result.records().get(0).tombstone()).isTrue();
        assertThat(state.get(GlmLocalAdapter.CLUSTERS).orElse("")).isEmpty();
    }

    @Test
    @DisplayName("while goes-glm is down, open local clusters are kept")
    void clustersKeptWhileInternetDown() throws Exception {
        long t = NOW.minusSeconds(60).toEpochMilli();
        state.put(GlmLocalAdapter.CLUSTERS, "lc-01ABC|30.3|-97.7|" + t + "|" + t + "|0|1");
        FetchResult result = adapter.fetch(ctx);
        assertThat(result.records()).isEmpty();
        assertThat(state.get(GlmLocalAdapter.CLUSTERS).orElse("")).contains("lc-01ABC");
    }
}
