package ai.foresignal.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.api.dto.FetchOutcomeDto;
import ai.foresignal.api.dto.NormalizedRecordPreviewDto;
import ai.foresignal.api.dto.ProbeResultDto;
import ai.foresignal.api.dto.QualityDto;
import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.collect.NormalizedRecordPreview;
import ai.foresignal.collect.ProbeResult;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.source.spi.Observer;
import ai.foresignal.source.spi.PositionSource;
import ai.foresignal.source.spi.Quality;
import ai.foresignal.store.FetchStatus;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FetchOutcomeMapperTest {

    private static final Instant T0 = Instant.parse("2026-04-01T12:00:00Z");

    private static FetchOutcome outcome(Map<PersistReason, Integer> byReason) {
        return new FetchOutcome(
                "src", T0, 42, true, 10, 3, 3, 4, 2, 1, 1, 0, 1, byReason, 100L, 102L, null, 7L,
                FetchStatus.OK, null);
    }

    @Test
    @DisplayName("byReason lists all nine reasons in declaration order, zeros included")
    void allNineReasons() {
        Map<PersistReason, Integer> counts = new EnumMap<>(PersistReason.class);
        counts.put(PersistReason.FIRST, 2);
        counts.put(PersistReason.SIGNIFICANT, 1);

        FetchOutcomeDto dto = FetchOutcomeMapper.outcome(outcome(counts));

        assertThat(dto.byReason()).hasSize(9);
        assertThat(dto.byReason().keySet())
                .containsExactly(
                        "FIRST", "REAPPEAR", "SIGNIFICANT", "TOMBSTONE", "CHANGE", "SAMPLE", "KEYFRAME",
                        "HEARTBEAT", "TRIGGER_EDGE");
        assertThat(dto.byReason()).containsEntry("FIRST", 2).containsEntry("SIGNIFICANT", 1)
                .containsEntry("TRIGGER_EDGE", 0).containsEntry("HEARTBEAT", 0);
    }

    @Test
    @DisplayName("a null map still yields nine zero counts")
    void nullMap() {
        assertThat(FetchOutcomeMapper.byReason(null)).hasSize(9).allSatisfy((k, v) -> assertThat(v).isZero());
    }

    @Test
    @DisplayName("recordsChanged equals persisted, and every count is copied")
    void counts() {
        FetchOutcomeDto dto = FetchOutcomeMapper.outcome(outcome(null));

        assertThat(dto.recordsChanged()).isEqualTo(dto.persisted()).isEqualTo(3);
        assertThat(dto.sourceId()).isEqualTo("src");
        assertThat(dto.startedAt()).isEqualTo(T0);
        assertThat(dto.durationMs()).isEqualTo(42);
        assertThat(dto.success()).isTrue();
        assertThat(dto.recordsSeen()).isEqualTo(10);
        assertThat(dto.live()).isEqualTo(4);
        assertThat(dto.touched()).isEqualTo(2);
        assertThat(dto.touchesThrottled()).isEqualTo(1);
        assertThat(dto.late()).isEqualTo(1);
        assertThat(dto.skipped()).isZero();
        assertThat(dto.rejected()).isEqualTo(1);
        assertThat(dto.fromSeq()).isEqualTo(100L);
        assertThat(dto.toSeq()).isEqualTo(102L);
        assertThat(dto.fetchId()).isEqualTo(7L);
        assertThat(dto.error()).isNull();
    }

    @Test
    @DisplayName("a failure keeps its error and has zero persisted")
    void failure() {
        FetchOutcome failed =
                FetchOutcome.withoutIngest("src", T0, 5, false, 0, "upstream broke", 9L, FetchStatus.FAILED);

        FetchOutcomeDto dto = FetchOutcomeMapper.outcome(failed);

        assertThat(dto.success()).isFalse();
        assertThat(dto.error()).isEqualTo("upstream broke");
        assertThat(dto.recordsChanged()).isZero();
        assertThat(dto.byReason()).hasSize(9);
    }

    @Test
    @DisplayName("probe previews carry eventTime, validFrom, quality and observers")
    void probe() {
        Quality quality = new Quality(5.0, 0.9, PositionSource.REPORTED, Map.of("adsb.seen_pos", 1.5));
        NormalizedRecordPreview preview =
                new NormalizedRecordPreview(
                        "x1", "kind", T0, Map.of("type", "Point"), null, false, Map.of("a", 1),
                        T0.minusSeconds(60), T0.minusSeconds(30), quality,
                        List.of(new Observer("rx-1", 1_090_000_000L, -70.0, 12.5, 0.8)));
        ProbeResult result =
                new ProbeResult(
                        "src",
                        List.of(new ProbeResult.Request("GET", "https://x/?key=REDACTED", 200, 3, "application/json", 10, false, "{}")),
                        List.of(preview),
                        null);

        ProbeResultDto dto = FetchOutcomeMapper.probe(result);

        assertThat(dto.requests()).hasSize(1);
        NormalizedRecordPreviewDto p = dto.records().get(0);
        assertThat(p.externalId()).isEqualTo("x1");
        assertThat(p.eventTime()).isEqualTo(T0.minusSeconds(60));
        assertThat(p.validFrom()).isEqualTo(T0.minusSeconds(30));
        assertThat(p.quality().positionSource()).isEqualTo("reported");
        assertThat(p.quality().extra()).containsEntry("adsb.seen_pos", 1.5);
        assertThat(p.observers()).singleElement().satisfies(o -> {
            assertThat(o.receiverId()).isEqualTo("rx-1");
            assertThat(o.frequencyHz()).isEqualTo(1_090_000_000L);
            assertThat(o.snr()).isEqualTo(12.5);
        });
        assertThat(dto.parseError()).isNull();
    }

    @Test
    @DisplayName("empty quality maps to null and missing observers to an empty list")
    void emptyQuality() {
        assertThat(FetchOutcomeMapper.quality(Quality.none())).isNull();
        assertThat(FetchOutcomeMapper.quality(null)).isNull();
        QualityDto q = FetchOutcomeMapper.quality(new Quality(null, 0.5, null, null));
        assertThat(q.confidence()).isEqualTo(0.5);
        assertThat(q.positionSource()).isNull();

        NormalizedRecordPreview bare =
                new NormalizedRecordPreview("x", "k", T0, null, null, true, Map.of(), null, null, null, null);
        assertThat(FetchOutcomeMapper.preview(bare).observers()).isEmpty();
        assertThat(FetchOutcomeMapper.preview(bare).tombstone()).isTrue();
    }
}
