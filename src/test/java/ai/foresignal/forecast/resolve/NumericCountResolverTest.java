package ai.foresignal.forecast.resolve;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.Ledger;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The numeric mode of {@code count-threshold} (prompt 126): without {@code op} and {@code threshold} the count itself
 * answers a numeric question; a {@code referenceFamily} selector counts reference-history events once the source
 * covers the window.
 */
class NumericCountResolverTest {

    static final Instant START = Instant.parse("2026-11-02T00:00:00Z");
    static final Instant END = Instant.parse("2026-11-30T00:00:00Z");

    /** A data port with scripted reference counts. */
    static final class Data implements ResolutionData {
        ReferenceCount count = new ReferenceCount(null, false, null, List.of());
        List<Occurrence> occurrences = List.of();
        Absence absence = new Absence(true, null);

        @Override
        public List<Occurrence> occurrences(Map<String, Object> selector, Map<String, Object> area, Instant from,
                Instant to) {
            return occurrences;
        }

        @Override
        public Aggregate seriesAggregate(String ref, Instant from, Instant to, String aggregate, Instant asOf) {
            return new Aggregate(17.0, 0, List.of("series:" + ref));
        }

        @Override
        public SituationState situationAt(String situationId, Instant at) {
            return null;
        }

        @Override
        public Scope scope(Map<String, Object> resolver) {
            return new Scope("conflict", List.of(), List.of("conflict-event"));
        }

        @Override
        public double coverage(Scope scope, Instant from, Instant to) {
            return 1;
        }

        @Override
        public Absence canSeeAbsence(Scope scope, Map<String, Object> area, Instant from, Instant to) {
            return absence;
        }

        @Override
        public double releaseLagDays(Map<String, Object> resolver) {
            return 7;
        }

        @Override
        public ReferenceCount referenceCount(String family, List<String> regions, Instant from, Instant to) {
            return count;
        }
    }

    static Map<String, Object> resolver() {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("type", "count-threshold");
        r.put("selector", Map.of("referenceFamily", "conflict:battles"));
        r.put("area", Map.of("regionCodes", List.of("YE")));
        r.put("window", Map.of("start", START.toString(), "end", END.toString()));
        return r;
    }

    static ForecastQuestionEntity question(Map<String, Object> resolver) {
        ForecastQuestionEntity q = new ForecastQuestionEntity("01JA8Q3V7S2K9W4T6Y1B5N0M3C", START);
        q.setTitle("t");
        q.setType(Ledger.NUMERIC);
        q.setResolutionCriteria("c");
        q.setResolver(ResolverSpec.validate(resolver, Ledger.NUMERIC));
        q.setOpensAt(START.minus(Duration.ofDays(7)));
        q.setClosesAt(START);
        q.setResolveBy(END);
        q.setCategory("conflict");
        q.setOrigin("system");
        return q;
    }

    @Test
    @DisplayName("validation: numeric without op/threshold, binary with both, never one without the other")
    void validation() {
        assertThat(ResolverSpec.validate(resolver(), Ledger.NUMERIC)).doesNotContainKey("op");
        assertThatThrownBy(() -> ResolverSpec.validate(resolver(), Ledger.BINARY))
                .isInstanceOf(ValidationException.class);
        Map<String, Object> half = resolver();
        half.put("op", "gte");
        assertThatThrownBy(() -> ResolverSpec.validate(half, Ledger.BINARY)).isInstanceOf(ValidationException.class);
        half.put("threshold", 3);
        assertThat(ResolverSpec.validate(half, Ledger.BINARY)).containsEntry("op", "gte");
        assertThatThrownBy(() -> ResolverSpec.validate(half, Ledger.NUMERIC)).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("a covered reference count resolves with the count after the release lag")
    void resolves() {
        Data data = new Data();
        data.count = new ResolutionData.ReferenceCount(23.0, true, "ucdp", List.of("reference-history:x"));
        ForecastQuestionEntity q = question(resolver());
        assertThat(ResolverEngine.evaluate(q, END.plus(Duration.ofDays(3)), data).kind())
                .isEqualTo(ResolverEngine.Kind.WAIT);
        ResolverEngine.Verdict v = ResolverEngine.evaluate(q, END.plus(Duration.ofDays(8)), data);
        assertThat(v.kind()).isEqualTo(ResolverEngine.Kind.RESOLVE);
        assertThat(v.value()).isEqualTo(23.0);
        assertThat(v.outcome()).isNull();
        assertThat(v.evidence()).containsExactly("reference-history:x");
    }

    @Test
    @DisplayName("an uncovered window waits, then annuls COVERAGE_GAP after 14 days")
    void coverage() {
        Data data = new Data();
        data.count = new ResolutionData.ReferenceCount(5.0, false, "ucdp", List.of());
        ForecastQuestionEntity q = question(resolver());
        assertThat(ResolverEngine.evaluate(q, END.plus(Duration.ofDays(9)), data).kind())
                .isEqualTo(ResolverEngine.Kind.WAIT);
        ResolverEngine.Verdict v = ResolverEngine.evaluate(q, END.plus(Duration.ofDays(15)), data);
        assertThat(v.kind()).isEqualTo(ResolverEngine.Kind.ANNUL);
        assertThat(v.annulCode()).isEqualTo("COVERAGE_GAP");
    }

    @Test
    @DisplayName("other selectors count occurrences and need detectability; a rollup ref sums the series")
    void otherForms() {
        Map<String, Object> r = resolver();
        r.put("selector", Map.of("intelEventType", "maritime-disruption"));
        Data data = new Data();
        data.occurrences = List.of(new ResolutionData.Occurrence(START.plus(Duration.ofDays(1)), "intel-event:1"));
        ResolverEngine.Verdict v = ResolverEngine.evaluate(question(r), END.plus(Duration.ofDays(8)), data);
        assertThat(v.value()).isEqualTo(1.0);
        data.absence = new ResolutionData.Absence(false, "LOW_DETECTABILITY");
        assertThat(ResolverEngine.evaluate(question(r), END.plus(Duration.ofDays(8)), data).annulCode())
                .isEqualTo("LOW_DETECTABILITY");
        Map<String, Object> rollup = new LinkedHashMap<>();
        rollup.put("type", "count-threshold");
        rollup.put("ref", "rollup:day/*/conflict-event/YE");
        rollup.put("window", Map.of("start", START.toString(), "end", END.toString()));
        assertThat(ResolverEngine.evaluate(question(rollup), END.plus(Duration.ofDays(8)), new Data()).value())
                .isEqualTo(17.0);
    }
}
