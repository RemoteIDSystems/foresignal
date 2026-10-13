package ai.foresignal.source.adapters.nvd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

/**
 * The NVD cursor (prompt 076): windows of at most 120 days, the first-run backfill clamp, and a cursor that advances
 * only after every page of a window succeeded, with a partly read window resumed one page early.
 */
class NvdCursorTest {

    static final Instant NOW = Instant.parse("2026-10-13T20:00:00Z");

    /** An in-memory adapter state. */
    static final class MapState implements AdapterStateStore {
        final Map<String, String> values = new HashMap<>();

        @Override
        public Optional<String> get(String key) {
            return Optional.ofNullable(values.get(key));
        }

        @Override
        public void put(String key, String value) {
            values.put(key, value);
        }

        @Override
        public void remove(String key) {
            values.remove(key);
        }
    }

    @Test
    @DisplayName("split: a 300-day span is three windows (120, 120, 60 days); none when the cursor is now")
    void split() {
        Instant from = NOW.minus(Duration.ofDays(300));
        List<NvdWindows.Window> windows = NvdWindows.split(from, NOW);
        assertThat(windows).hasSize(3);
        assertThat(windows.get(0)).isEqualTo(new NvdWindows.Window(from, from.plus(Duration.ofDays(120))));
        assertThat(windows.get(1).start()).isEqualTo(windows.get(0).end());
        assertThat(windows.get(2).end()).isEqualTo(NOW);
        assertThat(Duration.between(windows.get(2).start(), windows.get(2).end())).isEqualTo(Duration.ofDays(60));
        assertThat(windows).allSatisfy(w -> assertThat(Duration.between(w.start(), w.end()))
                .isLessThanOrEqualTo(NvdWindows.MAX_WINDOW));
        assertThat(NvdWindows.split(NOW.minus(Duration.ofDays(120)), NOW)).hasSize(1);
        assertThat(NvdWindows.split(NOW, NOW)).isEmpty();
        assertThat(NvdWindows.split(NOW.plusSeconds(1), NOW)).isEmpty();
        assertThat(windows.get(2).endParam()).isEqualTo("2026-10-13T20:00:00.000Z");
    }

    @Test
    @DisplayName("backfill: default 7 days, clamped to 1-120")
    void backfillClamp() {
        assertThat(NvdWindows.initialCursor(NOW, null)).isEqualTo(NOW.minus(Duration.ofDays(7)));
        assertThat(NvdWindows.initialCursor(NOW, 0)).isEqualTo(NOW.minus(Duration.ofDays(1)));
        assertThat(NvdWindows.initialCursor(NOW, 500)).isEqualTo(NOW.minus(Duration.ofDays(120)));
        assertThat(NvdWindows.clampBackfill(30)).isEqualTo(30);
        assertThat(new NvdCvesAdapter("", Duration.ZERO, d -> { }).checkParams(ValidatedParams.of(Map.of(
                "backfillDays", 121, "minCvss", 11.0, "epssRefreshDays", 0)))).containsOnlyKeys("backfillDays",
                "minCvss", "epssRefreshDays");
    }

    /** A context over {@code http} and {@code state} at {@link #NOW}, with {@code params}. */
    static FetchContext context(SourceHttp http, MapState state, Map<String, Object> params) {
        FetchContext ctx = mock(FetchContext.class);
        when(ctx.sourceId()).thenReturn(NvdCvesAdapter.ID);
        when(ctx.http()).thenReturn(http);
        when(ctx.state()).thenReturn(state);
        when(ctx.params()).thenReturn(ValidatedParams.of(params));
        when(ctx.credentials()).thenReturn(Map.of());
        when(ctx.clock()).thenReturn(Clock.fixed(NOW, ZoneOffset.UTC));
        when(ctx.now()).thenReturn(NOW);
        when(ctx.log()).thenReturn(LoggerFactory.getLogger("test.nvd"));
        return ctx;
    }

    static UpstreamResponse ok(byte[] body) {
        return new UpstreamResponse(200, Map.of(), body, "application/json", false, false);
    }

    /** A page of {@code total} results whose CVEs are the fixture's first {@code n}. */
    static byte[] page(int startIndex, int total, int n) {
        String fixture = AdapterFixtures.text("nvd-cves", "cves-page.json");
        var json = tools.jackson.databind.json.JsonMapper.builder().build();
        var root = (tools.jackson.databind.node.ObjectNode) json.readTree(fixture);
        var vulns = (tools.jackson.databind.node.ArrayNode) root.get("vulnerabilities");
        while (vulns.size() > n) {
            vulns.remove(vulns.size() - 1);
        }
        root.put("startIndex", startIndex).put("totalResults", total).put("resultsPerPage", n);
        return json.writeValueAsBytes(root);
    }

    @Test
    @DisplayName("a 200-day span: the cursor moves to the first window's end only after both its pages; a failure"
            + " in the second window keeps its position and the records")
    void cursorAdvancesPerWindow() throws Exception {
        SourceHttp http = mock(SourceHttp.class);
        List<URI> asked = new ArrayList<>();
        when(http.get(any(URI.class), anyMap())).thenAnswer(i -> {
            URI uri = i.getArgument(0);
            asked.add(uri);
            String q = uri.getRawQuery();
            if (q.contains("api.first.org") || uri.getPath().contains("epss")) {
                return ok("{\"status\": \"OK\", \"data\": []}".getBytes(StandardCharsets.UTF_8));
            }
            boolean firstWindow = q.contains("lastModStartDate=2026-03-27T20:00:00.000Z");
            if (firstWindow) {
                return q.contains("startIndex=0") ? ok(page(0, 4, 2)) : ok(page(2, 4, 2));
            }
            throw new AdapterException("budget refused");
        });
        MapState state = new MapState();
        state.put(NvdCvesAdapter.CURSOR_KEY, NOW.minus(Duration.ofDays(200)).toString());
        NvdCvesAdapter adapter = new NvdCvesAdapter("", Duration.ZERO, d -> { });
        FetchResult result = adapter.fetch(context(http, state, Map.of()));

        assertThat(result.records()).extracting(NormalizedRecord::externalId).as("one record per entity")
                .containsOnlyOnce("CVE-2025-8679");
        assertThat(state.values.get(NvdCvesAdapter.CURSOR_KEY)).as("the first window's end")
                .isEqualTo("2026-07-25T20:00:00Z");
        assertThat(state.values.get(NvdCvesAdapter.WINDOW_KEY)).isEqualTo(
                "2026-07-25T20:00:00Z|2026-10-13T20:00:00Z|0");
        assertThat(asked).filteredOn(u -> u.getRawQuery().contains("lastModStartDate")).hasSize(3);
    }

    @Test
    @DisplayName("a failure on the very first request fails the run and moves nothing")
    void firstFailureThrows() throws Exception {
        SourceHttp http = mock(SourceHttp.class);
        when(http.get(any(URI.class), anyMap())).thenThrow(new AdapterException("HTTP 503"));
        MapState state = new MapState();
        NvdCvesAdapter adapter = new NvdCvesAdapter("", Duration.ZERO, d -> { });
        assertThatThrownBy(() -> adapter.fetch(context(http, state, Map.of()))).isInstanceOf(AdapterException.class);
        assertThat(state.values).doesNotContainKeys(NvdCvesAdapter.CURSOR_KEY, NvdCvesAdapter.WINDOW_KEY);
    }

    @Test
    @DisplayName("a stored partial window resumes one page early, then the cursor moves to its end")
    void resume() throws Exception {
        MapState state = new MapState();
        state.put(NvdCvesAdapter.CURSOR_KEY, "2026-10-10T00:00:00Z");
        state.put(NvdCvesAdapter.WINDOW_KEY, "2026-10-10T00:00:00Z|2026-10-12T00:00:00Z|4000");
        state.put(NvdCvesAdapter.REFRESHED_ON_KEY, "2026-10-13");
        SourceHttp http = mock(SourceHttp.class);
        List<String> queries = new ArrayList<>();
        when(http.get(any(URI.class), anyMap())).thenAnswer(i -> {
            URI uri = i.getArgument(0);
            queries.add(uri.getRawQuery());
            if (uri.getPath().contains("epss")) {
                return ok("{\"status\": \"OK\", \"data\": []}".getBytes(StandardCharsets.UTF_8));
            }
            String q = uri.getRawQuery();
            return q.contains("startIndex=2000") ? ok(page(2000, 2001, 1)) : ok(page(0, 0, 0));
        });
        new NvdCvesAdapter("", Duration.ZERO, d -> { }).fetch(context(http, state, Map.of()));
        assertThat(queries.getFirst()).contains("lastModStartDate=2026-10-10T00:00:00.000Z")
                .contains("lastModEndDate=2026-10-12T00:00:00.000Z").contains("startIndex=2000");
        assertThat(queries.get(1)).contains("lastModStartDate=2026-10-12T00:00:00.000Z")
                .contains("lastModEndDate=2026-10-13T20:00:00.000Z").contains("startIndex=0");
        assertThat(state.values.get(NvdCvesAdapter.CURSOR_KEY)).isEqualTo(NOW.toString());
        assertThat(state.values).doesNotContainKey(NvdCvesAdapter.WINDOW_KEY);
    }
}
