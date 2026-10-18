package ai.foresignal.chronicle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.ingest.PersistReason;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import tools.jackson.databind.json.JsonMapper;

/**
 * Capture modes (prompt 131): parsing and validation, and what each mode writes for a stream of versions: full keeps
 * every version, downsampled one per entity per interval plus the event versions, events-only the event reasons,
 * latest-per-interval one replaced version per entity per bucket, rollup-only a count per region and hour, frames every
 * Nth raster frame; the size cap degrades full and downsampled to events-only.
 */
class CaptureModeTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final Instant T = Instant.parse("2026-10-12T12:00:00Z");

    private static CaptureMode parse(String text) {
        return CaptureMode.parse(JSON.readTree(text.replace('\'', '"')), "capture");
    }

    private static CapturedRow version(long seq, String entity, PersistReason reason, Instant observedAt) {
        return CapturedRow.of(seq, "src", entity, "aircraft", reason, observedAt);
    }

    private static List<CaptureDecider.Decision> run(CaptureMode mode, List<CapturedRow> rows) {
        CaptureDecider.State state = new CaptureDecider.State();
        List<CaptureDecider.Decision> out = new ArrayList<>();
        for (CapturedRow r : rows) {
            out.add(CaptureDecider.decide(mode, "L", r, state));
        }
        return out;
    }

    private static long inserts(List<CaptureDecider.Decision> decisions) {
        return decisions.stream().filter(d -> d.action() == CaptureDecider.Action.INSERT).count();
    }

    @Test
    @DisplayName("modes parse with their parameters and defaults; invalid ones are CHRONICLE_LAYER_INVALID")
    void parsing() {
        assertThat(parse("{'mode':'full'}")).isEqualTo(CaptureMode.FULL_MODE);
        assertThat(parse("{'mode':'downsampled'}").interval()).isEqualTo(Duration.ofSeconds(60));
        assertThat(parse("{'mode':'downsampled','interval':'PT5M'}").interval()).isEqualTo(Duration.ofMinutes(5));
        assertThat(parse("{'mode':'latest-per-interval','interval':'PT1H'}").interval()).isEqualTo(Duration.ofHours(1));
        assertThat(parse("{'mode':'frames','everyNthFrame':4}").everyNthFrame()).isEqualTo(4);
        assertThat(parse("{'mode':'frames'}").everyNthFrame()).isEqualTo(1);
        assertThat(parse("{'mode':'rollup-only'}").writesVersions()).isFalse();
        for (String bad : List.of("{}", "{'mode':'sometimes'}", "{'mode':'full','interval':'PT1M'}",
                "{'mode':'downsampled','interval':'soon'}", "{'mode':'downsampled','interval':'PT0.5S'}",
                "{'mode':'events-only','everyNthFrame':2}", "{'mode':'frames','everyNthFrame':0}",
                "{'mode':'full','extra':true}")) {
            assertThatThrownBy(() -> parse(bad)).as(bad).isInstanceOfSatisfying(ValidationException.class,
                    e -> assertThat(e.getCode()).isEqualTo(ErrorCodes.CHRONICLE_LAYER_INVALID));
        }
        CaptureMode mode = parse("{'mode':'downsampled','interval':'PT2M'}");
        assertThat(CaptureMode.fromMap(mode.toMap())).isEqualTo(mode);
        assertThat(CaptureMode.fromMap(Map.of())).isEqualTo(CaptureMode.FULL_MODE);
    }

    @Test
    @DisplayName("full keeps every version")
    void full() {
        List<CapturedRow> rows = List.of(version(1, "a", PersistReason.FIRST, T),
                version(2, "a", PersistReason.SAMPLE, T.plusSeconds(1)),
                version(3, "a", PersistReason.HEARTBEAT, T.plusSeconds(2)));
        assertThat(inserts(run(CaptureMode.FULL_MODE, rows))).isEqualTo(3);
    }

    @Test
    @DisplayName("downsampled keeps one version per entity per interval, plus every event version")
    void downsampled() {
        CaptureMode mode = CaptureMode.downsampled(Duration.ofSeconds(60));
        List<CapturedRow> rows = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            rows.add(version(1 + i, "a", PersistReason.SAMPLE, T.plusSeconds(i * 15L)));
            rows.add(version(100 + i, "b", PersistReason.SAMPLE, T.plusSeconds(i * 15L)));
        }
        List<CaptureDecider.Decision> decisions = run(mode, rows);
        // 0 s, 60 s, 120 s for each of the two entities.
        assertThat(inserts(decisions)).isEqualTo(6);

        List<CapturedRow> withEvent = List.of(version(1, "a", PersistReason.SAMPLE, T),
                version(2, "a", PersistReason.TOMBSTONE, T.plusSeconds(5)),
                version(3, "a", PersistReason.SAMPLE, T.plusSeconds(10)));
        assertThat(run(mode, withEvent)).extracting(CaptureDecider.Decision::action).containsExactly(
                CaptureDecider.Action.INSERT, CaptureDecider.Action.INSERT, CaptureDecider.Action.SKIP);
    }

    @Test
    @DisplayName("downsampled after a restart starts from the last capture the state was filled with")
    void downsampledAfterRestart() {
        CaptureMode mode = CaptureMode.downsampled(Duration.ofSeconds(60));
        CaptureDecider.State state = new CaptureDecider.State();
        CapturedRow row = version(5, "a", PersistReason.SAMPLE, T.plusSeconds(30));
        state.captured("L", row.entityKey(), T);
        assertThat(state.knows("L", row.entityKey())).isTrue();
        assertThat(CaptureDecider.decide(mode, "L", row, state).action()).isEqualTo(CaptureDecider.Action.SKIP);
        CapturedRow later = version(6, "a", PersistReason.SAMPLE, T.plusSeconds(61));
        assertThat(CaptureDecider.decide(mode, "L", later, state).action()).isEqualTo(CaptureDecider.Action.INSERT);
    }

    @ParameterizedTest
    @EnumSource(PersistReason.class)
    @DisplayName("events-only keeps FIRST, SIGNIFICANT, REAPPEAR, TOMBSTONE and TRIGGER_EDGE")
    void eventsOnly(PersistReason reason) {
        CaptureDecider.Decision d = CaptureDecider.decide(CaptureMode.EVENTS_ONLY_MODE, "L", version(1, "a", reason, T),
                new CaptureDecider.State());
        boolean event = List.of(PersistReason.FIRST, PersistReason.SIGNIFICANT, PersistReason.REAPPEAR,
                PersistReason.TOMBSTONE, PersistReason.TRIGGER_EDGE).contains(reason);
        assertThat(d.action()).isEqualTo(event ? CaptureDecider.Action.INSERT : CaptureDecider.Action.SKIP);
    }

    @Test
    @DisplayName("latest-per-interval replaces the entity's version in its epoch-aligned bucket")
    void latestPerInterval() {
        CaptureMode mode = new CaptureMode(CaptureMode.LATEST_PER_INTERVAL, Duration.ofMinutes(10), null);
        List<CaptureDecider.Decision> decisions = run(mode, List.of(version(1, "a", PersistReason.SAMPLE, T),
                version(2, "a", PersistReason.SAMPLE, T.plusSeconds(300)),
                version(3, "a", PersistReason.SAMPLE, T.plusSeconds(600))));
        assertThat(decisions).extracting(CaptureDecider.Decision::action).containsOnly(CaptureDecider.Action.REPLACE);
        assertThat(decisions.get(0).bucketStart()).isEqualTo(decisions.get(1).bucketStart());
        assertThat(decisions.get(2).bucketStart()).isEqualTo(T.plusSeconds(600));
        assertThat(CaptureDecider.bucket(Instant.parse("2026-10-12T12:07:30Z"), Duration.ofMinutes(10)))
                .isEqualTo(Instant.parse("2026-10-12T12:00:00Z"));
    }

    @Test
    @DisplayName("rollup-only counts per hour and writes no record")
    void rollupOnly() {
        CaptureMode mode = new CaptureMode(CaptureMode.ROLLUP_ONLY, null, null);
        CaptureDecider.Decision d = CaptureDecider.decide(mode, "L",
                version(1, "a", PersistReason.SAMPLE, Instant.parse("2026-10-12T12:42:10Z")), new CaptureDecider.State());
        assertThat(d.action()).isEqualTo(CaptureDecider.Action.ROLLUP);
        assertThat(d.bucketStart()).isEqualTo(Instant.parse("2026-10-12T12:00:00Z"));
    }

    @Test
    @DisplayName("frames keeps the first raster frame and every Nth after it, and ignores other kinds")
    void frames() {
        CaptureMode mode = CaptureMode.frames(4);
        CaptureDecider.State state = new CaptureDecider.State();
        List<CaptureDecider.Action> actions = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            CapturedRow frame = CapturedRow.of(i + 1, "goes", "goes-east-ir", ChronicleVocabulary.RASTER_FRAME_KIND,
                    PersistReason.SIGNIFICANT, T.plusSeconds(i * 600L));
            actions.add(CaptureDecider.decide(mode, "L", frame, state).action());
        }
        assertThat(actions).containsExactly(CaptureDecider.Action.INSERT, CaptureDecider.Action.SKIP,
                CaptureDecider.Action.SKIP, CaptureDecider.Action.SKIP, CaptureDecider.Action.INSERT,
                CaptureDecider.Action.SKIP, CaptureDecider.Action.SKIP, CaptureDecider.Action.SKIP,
                CaptureDecider.Action.INSERT);
        assertThat(state.frames("L")).isEqualTo(9);
        assertThat(CaptureDecider.decide(mode, "L", version(20, "a", PersistReason.FIRST, T), state).action())
                .isEqualTo(CaptureDecider.Action.SKIP);
    }

    @Test
    @DisplayName("over the cap, full and downsampled capture events only; the other modes keep their behavior")
    void capFallback() {
        assertThat(CaptureMode.FULL_MODE.effective(true)).isEqualTo(CaptureMode.EVENTS_ONLY_MODE);
        assertThat(CaptureMode.downsampled(Duration.ofMinutes(1)).effective(true)).isEqualTo(CaptureMode.EVENTS_ONLY_MODE);
        assertThat(CaptureMode.FULL_MODE.effective(false)).isEqualTo(CaptureMode.FULL_MODE);
        CaptureMode latest = new CaptureMode(CaptureMode.LATEST_PER_INTERVAL, Duration.ofMinutes(5), null);
        assertThat(latest.effective(true)).isEqualTo(latest);
        assertThat(CaptureMode.frames(3).effective(true)).isEqualTo(CaptureMode.frames(3));
        assertThat(ChronicleCaptureService.capState(89, 100)).isEqualTo(ChronicleVocabulary.CAP_OK);
        assertThat(ChronicleCaptureService.capState(90, 100)).isEqualTo(ChronicleVocabulary.CAP_WARNING);
        assertThat(ChronicleCaptureService.capState(100, 100)).isEqualTo(ChronicleVocabulary.CAP_EXCEEDED);
    }

    @Test
    @DisplayName("the projection of each mode is the mode's share of the day's volume")
    void projections() {
        ChronicleEstimator.Volume v = new ChronicleEstimator.Volume(10_000, 100, 10, 3, 500);
        assertThat(ChronicleEstimator.apply(v, CaptureMode.FULL_MODE, "b").rowsPerDay()).isEqualTo(10_000);
        assertThat(ChronicleEstimator.apply(v, CaptureMode.EVENTS_ONLY_MODE, "b").rowsPerDay()).isEqualTo(100);
        assertThat(ChronicleEstimator.apply(v, CaptureMode.downsampled(Duration.ofMinutes(10)), "b").rowsPerDay())
                .isEqualTo(10 * 144 + 100);
        assertThat(ChronicleEstimator.apply(v, new CaptureMode(CaptureMode.ROLLUP_ONLY, null, null), "b")
                .bytesPerDay()).isEqualTo(3 * 24 * ChronicleSizes.ROLLUP_ROW);
        assertThat(ChronicleEstimator.apply(v, CaptureMode.frames(4), "b").rowsPerDay()).isEqualTo(2_500);
        assertThat(ChronicleEstimator.apply(v, CaptureMode.FULL_MODE, "b").bytesPerDay()).isEqualTo(5_000_000);
    }
}
