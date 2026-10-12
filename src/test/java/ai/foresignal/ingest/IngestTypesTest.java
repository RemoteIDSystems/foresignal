package ai.foresignal.ingest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.Coverage;
import ai.foresignal.store.FetchTrigger;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The value types the writer consumes. */
class IngestTypesTest {

    private static final Instant T0 = Instant.parse("2026-10-11T12:00:00Z");

    @Test
    @DisplayName("IngestOutcome always lists all nine persist reasons")
    void outcomeReasons() {
        IngestOutcome outcome =
                new IngestOutcome(3, 2, 0, 0, 0, 0, 0, 1, 0, 10, 11, Map.of(PersistReason.FIRST, 2), 240,
                        List.of(new Rejection(2, "x", RejectionReason.UNKNOWN_KIND, "detail")));

        assertThat(outcome.byReason()).hasSize(9).containsEntry(PersistReason.FIRST, 2).containsEntry(PersistReason.SAMPLE, 0);
        assertThat(outcome.persisted(PersistReason.FIRST)).isEqualTo(2);
        assertThat(outcome.rejections()).singleElement().extracting(Rejection::reason).isEqualTo(RejectionReason.UNKNOWN_KIND);
        assertThat(IngestOutcome.empty().byReason()).hasSize(9).allSatisfy((reason, count) -> assertThat(count).isZero());
        assertThat(IngestOutcome.of(5, 1).persisted()).isEqualTo(1);
    }

    @Test
    @DisplayName("commands are a sealed set carrying futures")
    void commands() {
        IngestCommand batch = new IngestBatch("s", FetchLogEntry.ok("s", FetchTrigger.SCHEDULED, T0, T0), null, null);
        IngestCommand purge = new HeadPurge(List.of(new PurgeSpec("s", 100, T0)), null);

        String kind = switch (batch) {
            case IngestBatch b -> "batch " + b.records().size();
            case HeadPurge p -> "purge";
            case EntityPurge e -> "entity purge";
        };
        assertThat(kind).isEqualTo("batch 0");
        assertThat(((IngestBatch) batch).future()).isNotNull();
        assertThat(((HeadPurge) purge).future()).isInstanceOf(CompletableFuture.class);
        assertThat(new PurgeOutcome(null).headsDeleted()).isEmpty();
        assertThat(((IngestBatch) batch).log().coverage()).isEqualTo(Coverage.none());
        EntityPurge entity = new EntityPurge("s", "e", null);
        assertThat(entity.future()).isNotNull();
        assertThat(EntityPurgeOutcome.notFound().found()).isFalse();
        assertThatThrownBy(() -> new EntityPurge(null, "e", null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("records need a record and a receive time")
    void ingestRecord() {
        assertThatThrownBy(() -> new IngestRecord(null, T0, 0)).isInstanceOf(NullPointerException.class);
        assertThat(QaFlags.has(QaFlags.FUTURE_CLAMPED | QaFlags.POSITION_STALE, QaFlags.POSITION_STALE)).isTrue();
        assertThat(QaFlags.has(QaFlags.NULL_ISLAND, QaFlags.TIME_IMPUTED)).isFalse();
        assertThat(List.of(QaFlags.FUTURE_CLAMPED, QaFlags.GEOMETRY_FIXED, QaFlags.NULL_ISLAND, QaFlags.TIME_IMPUTED,
                        QaFlags.POSITION_STALE, QaFlags.PURGED))
                .containsExactly(1, 2, 4, 8, 16, 32);
    }

    @Test
    @DisplayName("JSON values: paths, numbers and text")
    void jsonValues() {
        Map<String, Object> payload = Map.of("a", Map.of("b", 3L), "s", "x");
        assertThat(JsonValues.at(payload, "a.b")).isEqualTo(3L);
        assertThat(JsonValues.at(payload, "a.c")).isNull();
        assertThat(JsonValues.at(payload, "s.t")).isNull();
        assertThat(JsonValues.at(payload, "")).isNull();
        // Array steps index like PostgreSQL's #> (prompt 017).
        Map<String, Object> withList = Map.of("l", List.of(5L, Map.of("x", 6L)));
        assertThat(JsonValues.at(withList, "l.0")).isEqualTo(5L);
        assertThat(JsonValues.at(withList, "l.1.x")).isEqualTo(6L);
        assertThat(JsonValues.at(withList, "l.2")).isNull();
        assertThat(JsonValues.at(withList, "l.x")).isNull();
        assertThat(JsonValues.at(withList, "l.-1")).isNull();
        assertThat(JsonValues.finiteNumber(Double.NaN)).isNull();
        assertThat(JsonValues.finiteNumber("1")).isNull();
        assertThat(JsonValues.text(2.50)).isEqualTo("2.5");
        assertThat(JsonValues.text(List.of())).isNull();
        assertThatThrownBy(() -> JsonValues.normalize(Map.of("k", Map.of(1, 2))))
                .isInstanceOf(JsonValues.UnsupportedValueException.class);
    }
}
