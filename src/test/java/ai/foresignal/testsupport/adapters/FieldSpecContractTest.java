package ai.foresignal.testsupport.adapters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link FieldSpecContract} itself: what it accepts, and that it lists every violation. */
class FieldSpecContractTest {

    private static final Instant T = Instant.parse("2026-10-11T00:00:00Z");

    private static SourceDescriptor descriptor() {
        return SourceDescriptor.builder("contract-test")
                .kinds("a", "b")
                .fieldSpec("name", FieldSpec.of(FieldType.STRING, null))
                .fieldSpec("count", FieldSpec.of(FieldType.INTEGER, "1"))
                .fieldSpec("value", FieldSpec.of(FieldType.NUMBER, "m"))
                .fieldSpec("flag", FieldSpec.of(FieldType.BOOLEAN, null))
                .fieldSpec("at", FieldSpec.of(FieldType.TIMESTAMP, "ms"))
                .fieldSpec("obj", FieldSpec.of(FieldType.OBJECT, null))
                .fieldSpec("list", FieldSpec.of(FieldType.ARRAY, null))
                .fieldSpec("onlyB", new FieldSpec(FieldType.STRING, null, null, null, null, Set.of("b")))
                .build();
    }

    private static NormalizedRecord record(String id, String kind, Map<String, Object> payload) {
        return NormalizedRecord.builder(id, kind).observedAt(T).putAll(payload).build();
    }

    @Test
    @DisplayName("passes when every key is declared with a matching type")
    void passes() {
        NormalizedRecord ok = record("r1", "a", Map.of(
                "name", "x", "count", 3L, "value", 1.5, "flag", true, "at", 1791750000000L,
                "obj", Map.of("k", 1), "list", List.of(1, 2)));
        NormalizedRecord iso = record("r2", "b", Map.of("at", "2026-10-11T00:00:00Z", "onlyB", "y", "value", 2L));
        assertThatCode(() -> FieldSpecContract.assertPayloadKeysDeclared(descriptor(), List.of(ok, iso)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("lists every undeclared key, including one declared only for another kind")
    void undeclared() {
        NormalizedRecord bad = record("r1", "a", Map.of("mystery", 1L, "onlyB", "y"));
        assertThatThrownBy(() -> FieldSpecContract.assertPayloadKeysDeclared(descriptor(), List.of(bad)))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("a.mystery")
                .hasMessageContaining("a.onlyB");
    }

    @Test
    @DisplayName("lists every value that contradicts its declared type")
    void mistyped() {
        NormalizedRecord bad = record("r1", "a", Map.of("count", 1.5, "name", 7L, "at", "yesterday"));
        assertThatThrownBy(() -> FieldSpecContract.assertPayloadKeysDeclared(descriptor(), List.of(bad)))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("r1.count is declared INTEGER")
                .hasMessageContaining("r1.name is declared STRING")
                .hasMessageContaining("r1.at is declared TIMESTAMP");
    }

    @Test
    @DisplayName("tombstones are skipped")
    void tombstones() {
        assertThatCode(() -> FieldSpecContract.assertPayloadKeysDeclared(
                        descriptor(), List.of(NormalizedRecord.tombstone("t", "a", T))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("type matching rules")
    void matching() {
        assertThat(FieldSpecContract.matches(FieldType.NUMBER, Double.NaN)).isFalse();
        assertThat(FieldSpecContract.matches(FieldType.NUMBER, new BigDecimal("1.5"))).isTrue();
        assertThat(FieldSpecContract.matches(FieldType.NUMBER, 1.5f)).isTrue();
        assertThat(FieldSpecContract.matches(FieldType.INTEGER, BigInteger.TEN)).isTrue();
        assertThat(FieldSpecContract.matches(FieldType.INTEGER, 2)).isTrue();
        assertThat(FieldSpecContract.matches(FieldType.BOOLEAN, "true")).isFalse();
        assertThat(FieldSpecContract.matches(FieldType.OBJECT, List.of())).isFalse();
        assertThat(FieldSpecContract.matches(FieldType.ARRAY, Map.of())).isFalse();
        assertThat(FieldSpecContract.matches(FieldType.STRING, null)).isFalse();
    }
}
