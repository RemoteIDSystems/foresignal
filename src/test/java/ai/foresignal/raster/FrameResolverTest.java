package ai.foresignal.raster;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.testsupport.raster.SyntheticRasterAdapter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Frame tokens, aliases and run selectors (prompt 042, Weather-Imagery-Design §6.1), over an in-memory lookup. */
class FrameResolverTest {

    static final Instant NOW = Instant.parse("2026-10-10T15:00:00Z");
    static final ProductSpec OBS = SyntheticRasterAdapter.observationSpec();
    static final ProductSpec FCST = SyntheticRasterAdapter.forecastSpec();
    static final String SHA = "1a2b3c4d" + "0".repeat(56);

    final List<RasterFrameRow> rows = new ArrayList<>();
    final FrameResolver resolver = new FrameResolver(new InMemory(rows));

    static RasterFrameRow obs(Instant t, String status) {
        return new RasterFrameRow(OBS.id(), FrameKeys.observationFrameId(t), t, null, null, null, null, "g", "p", 10, SHA,
                "u8", 1, Map.of(), null, status, false, false, t, null);
    }

    static RasterFrameRow fcst(Instant run, int step, boolean runComplete) {
        Instant valid = run.plusSeconds(step * 3600L);
        return new RasterFrameRow(FCST.id(), FrameKeys.forecastFrameId(run, step), valid, run, step, null, null, "g", "p",
                10, SHA, "i16", 1, Map.of(), null, RasterFrameRow.COMPLETE, false, runComplete, run, null);
    }

    static ForesignalException problem(Runnable call) {
        try {
            call.run();
        } catch (ForesignalException e) {
            return e;
        }
        throw new AssertionError("expected a problem");
    }

    FrameResolver.Found found(ProductSpec spec, String token, String run, boolean partial) {
        return (FrameResolver.Found) resolver.resolve(spec, token, run, partial, NOW, key -> "/current/" + key);
    }

    @Test
    @DisplayName("latest is the newest complete observed frame; partial frames only with includePartial")
    void latest() {
        rows.add(obs(NOW.minusSeconds(240), RasterFrameRow.COMPLETE));
        rows.add(obs(NOW.minusSeconds(120), RasterFrameRow.PARTIAL));
        assertThat(found(OBS, "latest", null, false).frame().validTime()).isEqualTo(NOW.minusSeconds(240));
        assertThat(found(OBS, "latest", null, true).frame().validTime()).isEqualTo(NOW.minusSeconds(120));
        assertThat(found(OBS, "latest", null, false).contentAddressed()).isFalse();
    }

    @Test
    @DisplayName("now resolves within maxGap and is UNKNOWN_FRAME beyond it")
    void now() {
        rows.add(obs(NOW.minusSeconds(300), RasterFrameRow.COMPLETE));
        rows.add(obs(NOW.plusSeconds(60), RasterFrameRow.COMPLETE));
        assertThat(found(OBS, "now", null, false).frame().validTime()).isEqualTo(NOW.minusSeconds(300));
        rows.clear();
        rows.add(obs(NOW.minusSeconds(420), RasterFrameRow.COMPLETE));
        ForesignalException e = problem(() -> found(OBS, "now", null, false));
        assertThat(e.getCode()).isEqualTo("UNKNOWN_FRAME");
        assertThat(e.getStatus().value()).isEqualTo(404);
    }

    @Test
    @DisplayName("a bare time key and a current frame key resolve; only the frame key with a complete frame is immutable")
    void timeAndFrameKeys() {
        Instant t = NOW.minusSeconds(600);
        rows.add(obs(t, RasterFrameRow.COMPLETE));
        String timeKey = FrameKeys.timeKey(t);
        assertThat(found(OBS, timeKey, null, false).contentAddressed()).isFalse();
        FrameResolver.Found key = found(OBS, timeKey + "~1a2b3c4d", null, false);
        assertThat(key.frame().validTime()).isEqualTo(t);
        assertThat(key.contentAddressed()).isTrue();
    }

    @Test
    @DisplayName("a stale sha8 is 404 UNKNOWN_FRAME with a Link header to the current key")
    void staleSha8() {
        Instant t = NOW.minusSeconds(600);
        rows.add(obs(t, RasterFrameRow.COMPLETE));
        String timeKey = FrameKeys.timeKey(t);
        ForesignalException e = problem(() -> found(OBS, timeKey + "~deadbeef", null, false));
        assertThat(e.getCode()).isEqualTo("UNKNOWN_FRAME");
        assertThat(e.getProperties()).containsEntry("currentFrameKey", timeKey + "~1a2b3c4d");
        assertThat(e.getHeaders().get("Link")).isEqualTo("</current/" + timeKey + "~1a2b3c4d>; rel=\"current\"");
    }

    @Test
    @DisplayName("an ISO-8601 extended time is a redirect to the canonical time key")
    void isoRedirect() {
        FrameResolver.Resolution r = resolver.resolve(OBS, "2026-10-10T14:32:40Z", null, false, NOW, null);
        assertThat(r).isEqualTo(new FrameResolver.Redirect("20261010T143240Z"));
        FrameResolver.Resolution offset = resolver.resolve(OBS, "2026-10-10T09:32:40-05:00", null, false, NOW, null);
        assertThat(offset).isEqualTo(new FrameResolver.Redirect("20261010T143240Z"));
        assertThat(problem(() -> resolver.resolve(OBS, "garbage", null, false, NOW, null)).getCode())
                .isEqualTo("VALIDATION_FAILED");
    }

    @Test
    @DisplayName("partial frames are excluded from concrete times unless includePartial")
    void partialExclusion() {
        Instant t = NOW.minusSeconds(60);
        rows.add(obs(t, RasterFrameRow.PARTIAL));
        assertThat(problem(() -> found(OBS, FrameKeys.timeKey(t), null, false)).getCode()).isEqualTo("UNKNOWN_FRAME");
        assertThat(found(OBS, FrameKeys.timeKey(t), null, true).frame().complete()).isFalse();
        assertThat(found(OBS, FrameKeys.timeKey(t) + "~1a2b3c4d", null, true).contentAddressed())
                .as("a partial frame is never immutable").isFalse();
    }

    @Test
    @DisplayName("a time older than the oldest retained frame, or in the thinned zone, is 410 FRAME_EXPIRED")
    void expired() {
        rows.add(obs(NOW.minusSeconds(8 * 3600), RasterFrameRow.COMPLETE));
        rows.add(obs(NOW.minusSeconds(120), RasterFrameRow.COMPLETE));
        ForesignalException old = problem(() -> found(OBS, FrameKeys.timeKey(NOW.minusSeconds(9 * 3600)), null, false));
        assertThat(old.getCode()).isEqualTo("FRAME_EXPIRED");
        assertThat(old.getStatus().value()).isEqualTo(410);
        ForesignalException thinned = problem(() -> found(OBS, FrameKeys.timeKey(NOW.minusSeconds(7 * 3600)), null, false));
        assertThat(thinned.getCode()).isEqualTo("FRAME_EXPIRED");
        ForesignalException recent = problem(() -> found(OBS, FrameKeys.timeKey(NOW.minusSeconds(30 * 60)), null, false));
        assertThat(recent.getCode()).isEqualTo("UNKNOWN_FRAME");
    }

    @Test
    @DisplayName("a forecast concrete time without run is 400 RUN_REQUIRED")
    void runRequired() {
        Instant run = NOW.minusSeconds(3 * 3600);
        rows.add(fcst(run, 3, true));
        ForesignalException e = problem(() -> found(FCST, FrameKeys.timeKey(run.plusSeconds(3 * 3600)), null, false));
        assertThat(e.getCode()).isEqualTo("RUN_REQUIRED");
        assertThat(e.getStatus().value()).isEqualTo(400);
    }

    @Test
    @DisplayName("run=latest, run=previous and a pinned run select complete runs; only the pinned run is immutable")
    void runs() {
        Instant previous = NOW.minusSeconds(12 * 3600);
        Instant current = NOW.minusSeconds(6 * 3600);
        Instant inProgress = NOW.minusSeconds(3600);
        for (int step = 0; step <= 12; step += 3) {
            rows.add(fcst(previous, step, true));
            rows.add(fcst(current, step, true));
        }
        rows.add(fcst(inProgress, 0, false));
        assertThat(found(FCST, "latest", null, false).frame().referenceTime()).isEqualTo(current);
        assertThat(found(FCST, "latest", null, false).frame().validTime()).isEqualTo(NOW);
        assertThat(found(FCST, "latest", "previous", false).frame().referenceTime()).isEqualTo(previous);
        assertThat(found(FCST, "latest", null, true).frame().referenceTime()).isEqualTo(inProgress);
        String key = FrameKeys.timeKey(current.plusSeconds(3 * 3600)) + "~1a2b3c4d";
        assertThat(found(FCST, key, "latest", false).contentAddressed()).isFalse();
        FrameResolver.Found pinned = found(FCST, key, FrameKeys.runIso(current), false);
        assertThat(pinned.contentAddressed()).isTrue();
        assertThat(pinned.frame().forecastStep()).isEqualTo(3);
        assertThat(resolver.selectRun(FCST, "previous", false)).isEqualTo(previous);
        assertThat(problem(() -> resolver.selectRun(FCST, FrameKeys.runIso(NOW), false)).getCode())
                .isEqualTo("UNKNOWN_FRAME");
        assertThat(problem(() -> resolver.selectRun(FCST, "tuesday", false)).getCode()).isEqualTo("VALIDATION_FAILED");
    }

    @Test
    @DisplayName("run=previous with a single complete run, and any alias of an empty product, are UNKNOWN_FRAME")
    void noFrames() {
        assertThat(problem(() -> found(OBS, "latest", null, false)).getCode()).isEqualTo("UNKNOWN_FRAME");
        rows.add(fcst(NOW.minusSeconds(3600), 0, true));
        assertThat(problem(() -> resolver.selectRun(FCST, "previous", false)).getCode()).isEqualTo("UNKNOWN_FRAME");
    }

    @Test
    @DisplayName("frame ids and keys have the documented shapes")
    void keyShapes() {
        Instant t = Instant.parse("2026-10-10T14:32:40.700Z");
        assertThat(FrameKeys.timeKey(t)).isEqualTo("20261010T143240Z");
        assertThat(FrameKeys.forecastFrameId(Instant.parse("2026-10-10T12:00:00Z"), 6)).isEqualTo("2026101012Z-f006");
        assertThat(FrameKeys.parseFrameKey("20261010T143240Z~1A2B3C4D").sha8()).isEqualTo("1a2b3c4d");
        assertThat(FrameKeys.parseFrameKey("20261310T143240Z")).isNull();
        assertThat(FrameKeys.parseFrameKey("20261010T143240Z~xyz")).isNull();
        assertThat(FrameKeys.parseRun("2026-10-10T12:00:00Z")).isEqualTo(Instant.parse("2026-10-10T12:00:00Z"));
        assertThat(FrameKeys.parseRun("20261010T120000Z")).isEqualTo(Instant.parse("2026-10-10T12:00:00Z"));
        assertThat(FrameKeys.parseRun("latest")).isNull();
    }

    /** The lookup over a list, with the same ordering rules as the SQL. */
    record InMemory(List<RasterFrameRow> rows) implements FrameResolver.FrameLookup {

        private List<RasterFrameRow> of(String productId, boolean includePartial) {
            return rows.stream().filter(r -> r.productId().equals(productId))
                    .filter(r -> includePartial || r.complete()).toList();
        }

        @Override
        public Optional<RasterFrameRow> newest(String productId, boolean includePartial) {
            return of(productId, includePartial).stream().max(Comparator.comparing(RasterFrameRow::validTime));
        }

        @Override
        public Optional<RasterFrameRow> newestAtOrBefore(String productId, Instant at, boolean includePartial) {
            return of(productId, includePartial).stream().filter(r -> !r.validTime().isAfter(at))
                    .max(Comparator.comparing(RasterFrameRow::validTime));
        }

        @Override
        public List<RasterFrameRow> atValidTime(String productId, Instant validTime) {
            return of(productId, true).stream().filter(r -> r.validTime().equals(validTime)).toList();
        }

        @Override
        public Optional<Instant> earliest(String productId) {
            return of(productId, true).stream().map(RasterFrameRow::validTime).min(Comparator.naturalOrder());
        }

        @Override
        public List<RasterFrameQueries.RunSummary> runs(String productId) {
            Map<Instant, List<RasterFrameRow>> byRun = new TreeMap<>(Comparator.reverseOrder());
            for (RasterFrameRow r : of(productId, true)) {
                if (r.referenceTime() != null) {
                    byRun.computeIfAbsent(r.referenceTime(), k -> new ArrayList<>()).add(r);
                }
            }
            List<RasterFrameQueries.RunSummary> out = new ArrayList<>();
            Map<Instant, List<RasterFrameRow>> ordered = new LinkedHashMap<>(byRun);
            ordered.forEach((run, frames) -> out.add(new RasterFrameQueries.RunSummary(run,
                    frames.stream().anyMatch(RasterFrameRow::runComplete),
                    frames.stream().map(RasterFrameRow::forecastStep).sorted().toList(),
                    frames.stream().map(RasterFrameRow::validTime).min(Comparator.naturalOrder()).orElseThrow(),
                    frames.stream().map(RasterFrameRow::validTime).max(Comparator.naturalOrder()).orElseThrow(),
                    (int) frames.stream().filter(f -> !f.complete()).count())));
            return out;
        }

        @Override
        public List<RasterFrameRow> runFrames(String productId, Instant run, boolean includePartial) {
            return of(productId, includePartial).stream().filter(r -> run.equals(r.referenceTime()))
                    .sorted(Comparator.comparing(RasterFrameRow::forecastStep)).toList();
        }
    }
}
