package ai.foresignal.forecast.resolve;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.Ledger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * The machine resolvers (prompt 122 §3) over a scripted {@link ResolutionData}: early resolution, the release-lag
 * wait, coverage and detectability annulment, numeric aggregation, situation states, and every validator.
 */
class ResolverTest {

    static final Instant START = Instant.parse("2026-05-01T00:00:00Z");
    static final Instant RESOLVE_BY = Instant.parse("2026-05-29T00:00:00Z");

    /** A scripted data port. */
    static final class Fake implements ResolutionData {
        List<Occurrence> occurrences = new ArrayList<>();
        Aggregate aggregate = new Aggregate(null, 1.0, List.of());
        SituationState situation;
        Scope scope = new Scope("conflict", List.of(), List.of("conflict-event"));
        double coverage = 1.0;
        Absence absence = new Absence(true, null);
        double lag;
        Instant lastAsOf;

        @Override
        public List<Occurrence> occurrences(Map<String, Object> selector, Map<String, Object> area, Instant from,
                Instant to) {
            return occurrences.stream().filter(o -> !o.at().isBefore(from) && !o.at().isAfter(to)).toList();
        }

        @Override
        public Aggregate seriesAggregate(String ref, Instant from, Instant to, String aggregate, Instant asOf) {
            lastAsOf = asOf;
            return this.aggregate;
        }

        @Override
        public SituationState situationAt(String situationId, Instant at) {
            return situation;
        }

        @Override
        public Scope scope(Map<String, Object> resolver) {
            return scope;
        }

        @Override
        public double coverage(Scope scope, Instant from, Instant to) {
            return coverage;
        }

        @Override
        public Absence canSeeAbsence(Scope scope, Map<String, Object> area, Instant from, Instant to) {
            return absence;
        }

        @Override
        public double releaseLagDays(Map<String, Object> resolver) {
            return lag;
        }
    }

    static ForecastQuestionEntity question(String type, Map<String, Object> resolver) {
        ForecastQuestionEntity q = new ForecastQuestionEntity("01JA8Q3V7S2K9W4T6Y1B5N0M3C", START);
        q.setTitle("t");
        q.setType(type);
        q.setResolutionCriteria("c");
        q.setResolver(ResolverSpec.validate(resolver, type));
        q.setOpensAt(START.minus(Duration.ofDays(1)));
        q.setClosesAt(START);
        q.setResolveBy(RESOLVE_BY);
        q.setCategory("conflict");
        q.setOrigin("agent");
        return q;
    }

    static Map<String, Object> window() {
        return Map.of("start", START.toString(), "end", RESOLVE_BY.toString());
    }

    static Map<String, Object> eventExists() {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("type", "event-exists");
        r.put("selector", Map.of("referenceFamily", "conflict:battles"));
        r.put("area", Map.of("regionCodes", List.of("YE")));
        r.put("window", window());
        return r;
    }

    @Nested
    @DisplayName("event-exists and alert-fired")
    class Events {

        @Test
        @DisplayName("an event before resolve_by resolves yes early, with its evidence")
        void early() {
            Fake data = new Fake();
            data.occurrences.add(new ResolutionData.Occurrence(START.plus(Duration.ofDays(3)), "alert:7"));
            ResolverEngine.Verdict v = ResolverEngine.evaluate(question(Ledger.BINARY, eventExists()),
                    START.plus(Duration.ofDays(4)), data);
            assertThat(v.kind()).isEqualTo(ResolverEngine.Kind.RESOLVE);
            assertThat(v.outcome()).isEqualTo("yes");
            assertThat(v.early()).isTrue();
            assertThat(v.evidence()).containsExactly("alert:7");
        }

        @Test
        @DisplayName("no event yet waits; an event later in the window is not seen before it happens")
        void waiting() {
            Fake data = new Fake();
            data.occurrences.add(new ResolutionData.Occurrence(START.plus(Duration.ofDays(10)), "alert:8"));
            assertThat(ResolverEngine.evaluate(question(Ledger.BINARY, eventExists()), START.plus(Duration.ofDays(4)),
                    data).kind()).isEqualTo(ResolverEngine.Kind.WAIT);
        }

        @Test
        @DisplayName("the release lag is waited for before a negative answer, then resolved as of resolve_by + lag")
        void releaseLag() {
            Fake data = new Fake();
            data.lag = 7;
            ForecastQuestionEntity q = question(Ledger.BINARY, eventExists());
            assertThat(ResolverEngine.evaluate(q, RESOLVE_BY.plus(Duration.ofDays(3)), data).kind())
                    .isEqualTo(ResolverEngine.Kind.WAIT);
            ResolverEngine.Verdict v = ResolverEngine.evaluate(q, RESOLVE_BY.plus(Duration.ofDays(7)), data);
            assertThat(v.kind()).isEqualTo(ResolverEngine.Kind.RESOLVE);
            assertThat(v.outcome()).isEqualTo("no");
            assertThat(v.early()).isFalse();
            assertThat(v.asOf()).isEqualTo(RESOLVE_BY.plus(Duration.ofDays(7)));
            assertThat(v.evidence()).containsExactly("observability:conflict/YE");
        }

        @Test
        @DisplayName("a lag over 14 days is cut at 14 days and the resolution carries LATE_DATA")
        void lateData() {
            Fake data = new Fake();
            data.lag = 30;
            ResolverEngine.Verdict v = ResolverEngine.evaluate(question(Ledger.BINARY, eventExists()),
                    RESOLVE_BY.plus(Duration.ofDays(14)), data);
            assertThat(v.kind()).isEqualTo(ResolverEngine.Kind.RESOLVE);
            assertThat(v.caveats()).extracting(c -> c.get("code")).containsExactly("LATE_DATA");
        }

        @Test
        @DisplayName("sources failing for more than 20% of the window annul a negative answer with COVERAGE_GAP")
        void coverageGap() {
            Fake data = new Fake();
            data.coverage = 0.7;
            ResolverEngine.Verdict v = ResolverEngine.evaluate(question(Ledger.BINARY, eventExists()),
                    RESOLVE_BY.plus(Duration.ofHours(1)), data);
            assertThat(v.kind()).isEqualTo(ResolverEngine.Kind.ANNUL);
            assertThat(v.annulCode()).isEqualTo("COVERAGE_GAP");
            // A positive answer stands despite a gap.
            data.occurrences.add(new ResolutionData.Occurrence(START.plus(Duration.ofDays(1)), "alert:9"));
            assertThat(ResolverEngine.evaluate(question(Ledger.BINARY, eventExists()),
                    RESOLVE_BY.plus(Duration.ofHours(1)), data).outcome()).isEqualTo("yes");
        }

        @Test
        @DisplayName("a negative answer without detectability is annulled LOW_DETECTABILITY")
        void lowDetectability() {
            Fake data = new Fake();
            data.absence = new ResolutionData.Absence(false, "LOW_DETECTABILITY");
            ResolverEngine.Verdict v = ResolverEngine.evaluate(question(Ledger.BINARY, eventExists()),
                    RESOLVE_BY.plus(Duration.ofHours(1)), data);
            assertThat(v.kind()).isEqualTo(ResolverEngine.Kind.ANNUL);
            assertThat(v.annulCode()).isEqualTo("LOW_DETECTABILITY");
        }

        @Test
        @DisplayName("alert-fired resolves early on the rule's alert")
        void alertFired() {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("type", "alert-fired");
            r.put("ruleId", "3f2b7c1e-8a4d-4f6b-9c2e-1d5a7b9e0f13");
            r.put("window", window());
            Fake data = new Fake();
            data.occurrences.add(new ResolutionData.Occurrence(START.plus(Duration.ofDays(2)), "alert:11"));
            ResolverEngine.Verdict v = ResolverEngine.evaluate(question(Ledger.BINARY, r),
                    START.plus(Duration.ofDays(2)), data);
            assertThat(v.outcome()).isEqualTo("yes");
            assertThat(v.early()).isTrue();
        }
    }

    @Nested
    @DisplayName("count-threshold")
    class Count {

        Map<String, Object> resolver(String op, double threshold) {
            Map<String, Object> r = eventExists();
            r.put("type", "count-threshold");
            r.put("op", op);
            r.put("threshold", threshold);
            return r;
        }

        @Test
        @DisplayName("never early; counts occurrences after resolve_by and compares")
        void counts() {
            Fake data = new Fake();
            for (int i = 0; i < 3; i++) {
                data.occurrences.add(new ResolutionData.Occurrence(START.plus(Duration.ofDays(i + 1)), "alert:" + i));
            }
            ForecastQuestionEntity q = question(Ledger.BINARY, resolver("gte", 3));
            assertThat(ResolverEngine.evaluate(q, START.plus(Duration.ofDays(5)), data).kind())
                    .isEqualTo(ResolverEngine.Kind.WAIT);
            ResolverEngine.Verdict v = ResolverEngine.evaluate(q, RESOLVE_BY, data);
            assertThat(v.outcome()).isEqualTo("yes");
            assertThat(v.reason()).contains("count 3 gte 3");
        }

        @Test
        @DisplayName("a count below the threshold needs detectability")
        void lowCount() {
            Fake data = new Fake();
            data.absence = new ResolutionData.Absence(false, "no source");
            assertThat(ResolverEngine.evaluate(question(Ledger.BINARY, resolver("gte", 1)), RESOLVE_BY, data)
                    .annulCode()).isEqualTo("LOW_DETECTABILITY");
            data.absence = new ResolutionData.Absence(true, null);
            assertThat(ResolverEngine.evaluate(question(Ledger.BINARY, resolver("gte", 1)), RESOLVE_BY, data)
                    .outcome()).isEqualTo("no");
        }

        @Test
        @DisplayName("a rollup sum with too many gaps is annulled COVERAGE_GAP")
        void rollupGaps() {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("type", "count-threshold");
            r.put("ref", "rollup:day/*/earthquake/JP");
            r.put("window", window());
            r.put("op", "gt");
            r.put("threshold", 10);
            Fake data = new Fake();
            data.aggregate = new ResolutionData.Aggregate(40.0, 0.3, List.of("rollup:day/*/earthquake/JP"));
            assertThat(ResolverEngine.evaluate(question(Ledger.BINARY, r), RESOLVE_BY, data).annulCode())
                    .isEqualTo("COVERAGE_GAP");
            data.aggregate = new ResolutionData.Aggregate(40.0, 0.1, List.of("rollup:day/*/earthquake/JP"));
            assertThat(ResolverEngine.evaluate(question(Ledger.BINARY, r), RESOLVE_BY, data).outcome())
                    .isEqualTo("yes");
        }
    }

    @Nested
    @DisplayName("series-value")
    class Series {

        @Test
        @DisplayName("numeric: the aggregate of the period, read as of the period end plus the release lag")
        void numeric() {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("type", "series-value");
            r.put("ref", "series:cftc-cot/067651.disagg.mm_net");
            r.put("period", "2026-05");
            r.put("aggregate", "mean");
            Fake data = new Fake();
            data.lag = 3;
            data.aggregate = new ResolutionData.Aggregate(1234.5, 0.0, List.of("series:cftc-cot/067651.disagg.mm_net"));
            ForecastQuestionEntity q = question(Ledger.NUMERIC, r);
            Instant periodEnd = Instant.parse("2026-06-01T00:00:00Z");
            assertThat(ResolverEngine.evaluate(q, periodEnd, data).kind()).isEqualTo(ResolverEngine.Kind.WAIT);
            ResolverEngine.Verdict v = ResolverEngine.evaluate(q, periodEnd.plus(Duration.ofDays(3)), data);
            assertThat(v.kind()).isEqualTo(ResolverEngine.Kind.RESOLVE);
            assertThat(v.value()).isEqualTo(1234.5);
            assertThat(v.outcome()).isNull();
            assertThat(data.lastAsOf).isEqualTo(periodEnd.plus(Duration.ofDays(3)));
        }

        @Test
        @DisplayName("with a threshold: binary; missing data annuls")
        void threshold() {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("type", "series-value");
            r.put("ref", "series:fred-series/DCOILBRENTEU");
            r.put("window", Map.of("start", START.toString(), "end", RESOLVE_BY.toString()));
            r.put("aggregate", "max");
            r.put("op", "gt");
            r.put("threshold", 100);
            Fake data = new Fake();
            data.aggregate = new ResolutionData.Aggregate(104.0, 0.05, List.of());
            assertThat(ResolverEngine.evaluate(question(Ledger.BINARY, r), RESOLVE_BY, data).outcome())
                    .isEqualTo("yes");
            data.aggregate = new ResolutionData.Aggregate(null, 1.0, List.of());
            assertThat(ResolverEngine.evaluate(question(Ledger.BINARY, r), RESOLVE_BY, data).annulCode())
                    .isEqualTo("COVERAGE_GAP");
        }
    }

    @Test
    @DisplayName("situation-status reads the state at resolve_by")
    void situationStatus() {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("type", "situation-status");
        r.put("situationId", "01JA8Q3V7S2K9W4T6Y1B5N0M3C");
        r.put("minSeverity", "warning");
        Fake data = new Fake();
        data.situation = new ResolutionData.SituationState("active", "critical");
        ForecastQuestionEntity q = question(Ledger.BINARY, r);
        assertThat(ResolverEngine.evaluate(q, START, data).kind()).isEqualTo(ResolverEngine.Kind.WAIT);
        assertThat(ResolverEngine.evaluate(q, RESOLVE_BY, data).outcome()).isEqualTo("yes");
        data.situation = new ResolutionData.SituationState("dormant", "info");
        assertThat(ResolverEngine.evaluate(q, RESOLVE_BY, data).outcome()).isEqualTo("no");
        data.situation = null;
        assertThat(ResolverEngine.evaluate(q, RESOLVE_BY, data).kind()).isEqualTo(ResolverEngine.Kind.ANNUL);
    }

    @Test
    @DisplayName("manual and hypothesis-outcome questions are never machine-resolved")
    void manual() {
        assertThat(ResolverEngine.evaluate(question(Ledger.MULTIPLE, Map.of("type", "manual")), RESOLVE_BY.plus(
                Duration.ofDays(30)), new Fake()).kind()).isEqualTo(ResolverEngine.Kind.WAIT);
    }

    @Nested
    @DisplayName("validators")
    class Validators {

        void invalid(Map<String, Object> resolver, String type, String field) {
            assertThatThrownBy(() -> ResolverSpec.validate(resolver, type))
                    .isInstanceOfSatisfying(ValidationException.class, e -> {
                        assertThat(e.getCode()).isEqualTo(ErrorCodes.RESOLVER_INVALID);
                        assertThat(e.getErrors().get(0).field()).isEqualTo(field);
                    });
        }

        @Test
        @DisplayName("versions are server-set; unknown types, members and question types are refused")
        void basics() {
            assertThat(ResolverSpec.validate(eventExists(), Ledger.BINARY)).containsEntry("version",
                    ResolverSpec.VERSION);
            invalid(Map.of("type", "telepathy"), Ledger.BINARY, "resolver.type");
            Map<String, Object> extra = eventExists();
            extra.put("color", "red");
            invalid(extra, Ledger.BINARY, "resolver.color");
            invalid(eventExists(), Ledger.NUMERIC, "resolver.type");
        }

        @Test
        @DisplayName("selectors: exactly one resolvable form, no inline rule; geometric areas need located forms")
        void selectors() {
            Map<String, Object> r = eventExists();
            r.put("selector", Map.of("rule", Map.of()));
            invalid(r, Ledger.BINARY, "resolver.selector.rule");
            r.put("selector", Map.of("referenceFamily", "conflict:battles", "intelEventType", "x"));
            invalid(r, Ledger.BINARY, "resolver.selector");
            r.put("selector", Map.of("referenceFamily", "conflict:battles"));
            r.put("area", Map.of("bbox", List.of(40, 10, 50, 20)));
            invalid(r, Ledger.BINARY, "resolver.area");
            r.put("selector", Map.of("intelEventType", "maritime-disruption"));
            assertThat(ResolverSpec.validate(r, Ledger.BINARY)).containsKey("area");
            r.put("area", Map.of("bbox", List.of(50, 10, 40, 20)));
            invalid(r, Ledger.BINARY, "resolver.area.bbox");
            r.put("area", Map.of("regionCodes", List.of("yemen")));
            invalid(r, Ledger.BINARY, "resolver.area.regionCodes");
        }

        @Test
        @DisplayName("windows, operators, thresholds, periods and refs")
        void members() {
            Map<String, Object> r = eventExists();
            r.put("window", Map.of("start", RESOLVE_BY.toString(), "end", START.toString()));
            invalid(r, Ledger.BINARY, "resolver.window.end");
            Map<String, Object> c = eventExists();
            c.put("type", "count-threshold");
            c.put("op", "about");
            c.put("threshold", 1);
            invalid(c, Ledger.BINARY, "resolver.op");
            c.put("op", "gte");
            c.put("threshold", -1);
            invalid(c, Ledger.BINARY, "resolver.threshold");
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("type", "series-value");
            s.put("ref", "series:cftc-cot/x");
            s.put("period", "2026-13");
            invalid(s, Ledger.NUMERIC, "resolver.period");
            s.put("period", "2026-W48");
            assertThat(ResolverSpec.validate(s, Ledger.NUMERIC)).containsEntry("aggregate", "last");
            s.put("op", "gt");
            invalid(s, Ledger.NUMERIC, "resolver.threshold");
            s.put("threshold", 5);
            invalid(s, Ledger.NUMERIC, "resolver.type");
            assertThat(ResolverSpec.validate(s, Ledger.BINARY)).containsEntry("op", "gt");
            s.put("ref", "nonsense");
            invalid(s, Ledger.BINARY, "resolver.ref");
            invalid(Map.of("type", "situation-status", "situationId", "01JA8Q3V7S2K9W4T6Y1B5N0M3C"), Ledger.BINARY,
                    "resolver.status");
            invalid(Map.of("type", "hypothesis-outcome", "setId", "x"), Ledger.BINARY, "resolver.setId");
            invalid(Map.of("type", "alert-fired", "ruleId", "not-a-uuid", "window", window()), Ledger.BINARY,
                    "resolver.ruleId");
        }

        @Test
        @DisplayName("periods: year, month, ISO week and day")
        void periods() {
            assertThat(ResolverSpec.periodRange("2026")[0]).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
            assertThat(ResolverSpec.periodRange("2026-02")[1]).isEqualTo(Instant.parse("2026-03-01T00:00:00Z"));
            assertThat(ResolverSpec.periodRange("2026-W01")[0]).isEqualTo(Instant.parse("2025-12-29T00:00:00Z"));
            assertThat(ResolverSpec.periodRange("2026-05-03")[1]).isEqualTo(Instant.parse("2026-05-04T00:00:00Z"));
        }
    }
}
