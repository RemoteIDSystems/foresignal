package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.CoverageSample;
import ai.foresignal.testsupport.adapters.FakePollingAdapter;
import ai.foresignal.testsupport.adapters.FakeStreamingAdapter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * The status table of Architecture §5.4, as amended by Core-Data-Model §14, with no database.
 *
 * <p>The table is applied in order and the first matching rule decides, so the interesting cases
 * are the precedences — a source both in backoff and with a silent break is failing, not degraded —
 * and the boundaries: exactly three failures, exactly 20% refusals, exactly two minutes down.
 */
class HealthEvaluatorTest {

    private static final Instant NOW = Instant.parse("2026-03-01T12:00:00Z");
    private static final SourceDescriptor POLLING = FakePollingAdapter.defaultDescriptor("eval").build();
    private static final SourceDescriptor ZERO_NORMAL =
            FakePollingAdapter.defaultDescriptor("eval-zero").zeroResultsNormal(true).build();
    private static final SourceDescriptor STREAMING = FakeStreamingAdapter.defaultDescriptor("eval-stream").build();

    private final HealthEvaluator evaluator = new HealthEvaluator();

    // ---------- builders ----------

    /** A healthy, attempted, successful source; each case changes only what it is about. */
    private static HealthSnapshot attempted() {
        return new HealthSnapshot(
                "eval",
                NOW.minusSeconds(60),
                NOW.minusSeconds(60),
                null,
                null,
                null,
                0,
                10,
                2,
                List.of(10, 10, 10, 10, 10),
                null,
                null,
                List.of(),
                NOW.minusSeconds(60));
    }

    private static HealthSnapshot withFailures(int failures, String error) {
        HealthSnapshot h = attempted();
        return new HealthSnapshot(
                h.sourceId(), h.lastAttemptAt(), h.lastSuccessAt(), NOW.minusSeconds(5), error, 503,
                failures, h.lastRecordsSeen(), h.lastRecordsChanged(), h.recentRecordCounts(),
                h.backoffUntil(), h.lastCoverage(), h.recentCoverage(), h.lastDataAt());
    }

    private static HealthSnapshot withCounts(Integer... counts) {
        HealthSnapshot h = attempted();
        return new HealthSnapshot(
                h.sourceId(), h.lastAttemptAt(), h.lastSuccessAt(), null, null, null, 0,
                counts.length == 0 ? null : counts[counts.length - 1], 0, List.of(counts),
                null, h.lastCoverage(), h.recentCoverage(), h.lastDataAt());
    }

    private static HealthSnapshot withCoverage(HealthSnapshot base, List<CoverageSample> ring) {
        return new HealthSnapshot(
                base.sourceId(), base.lastAttemptAt(), base.lastSuccessAt(), base.lastErrorAt(),
                base.lastError(), base.lastHttpStatus(), base.consecutiveFailures(), base.lastRecordsSeen(),
                base.lastRecordsChanged(), base.recentRecordCounts(), base.backoffUntil(),
                ring.isEmpty() ? null : ring.get(ring.size() - 1), ring, base.lastDataAt());
    }

    private static HealthSnapshot neverAttempted() {
        return HealthSnapshot.empty("eval");
    }

    private static UsageTotalsWindow usage(long requests, long refused) {
        return new UsageTotalsWindow(requests, 0, 0, refused, IngestCounts.ZERO);
    }

    private static HealthInputs inputs(SourceDescriptor d, boolean enabled, HealthSnapshot h) {
        return new HealthInputs(d, enabled, h, null, UsageTotalsWindow.ZERO, null, null, NOW);
    }

    private static CoverageSample receivers(int n) {
        return new CoverageSample(n, null, null, null);
    }

    private static CoverageSample expected(int n) {
        return new CoverageSample(null, n, null, null);
    }

    private static List<CoverageSample> ring(CoverageSample... samples) {
        return List.of(samples);
    }

    // ---------- the table ----------

    record Case(String name, HealthInputs in, HealthStatus status, String reasonPrefix) {
        @Override
        public String toString() {
            return name;
        }
    }

    static Stream<Case> statusTable() {
        HealthSnapshot ok = attempted();
        return Stream.of(
                // 1. disabled
                new Case("disabled wins over everything", inputs(POLLING, false, withFailures(9, "boom")),
                        HealthStatus.DISABLED, "source disabled"),
                new Case("disabled with no row", inputs(POLLING, false, null), HealthStatus.DISABLED, "source disabled"),
                // 2. idle
                new Case("enabled, never attempted", inputs(POLLING, true, neverAttempted()), HealthStatus.IDLE,
                        "no attempt yet"),
                // 3. failing
                new Case("in backoff",
                        new HealthInputs(POLLING, true, ok, NOW.plusSeconds(300), UsageTotalsWindow.ZERO, null, null, NOW),
                        HealthStatus.FAILING, "in backoff until 2026-03-01T12:05:00Z"),
                new Case("backoff deadline already passed is ignored",
                        new HealthInputs(POLLING, true, ok, NOW, UsageTotalsWindow.ZERO, null, null, NOW),
                        HealthStatus.HEALTHY, null),
                new Case("exactly 3 consecutive failures", inputs(POLLING, true, withFailures(3, "IOException: reset")),
                        HealthStatus.FAILING, "3 consecutive failures: IOException: reset"),
                new Case("more than 3 consecutive failures", inputs(POLLING, true, withFailures(7, "x")),
                        HealthStatus.FAILING, "7 consecutive failures: x"),
                new Case("backoff beats failure count",
                        new HealthInputs(POLLING, true, withFailures(5, "x"), NOW.plusSeconds(30),
                                UsageTotalsWindow.ZERO, null, null, NOW),
                        HealthStatus.FAILING, "in backoff until"),
                // 4. last run failed
                new Case("1 failure", inputs(POLLING, true, withFailures(1, "HTTP 500")), HealthStatus.DEGRADED,
                        "last run failed: HTTP 500"),
                new Case("2 failures", inputs(POLLING, true, withFailures(2, null)), HealthStatus.DEGRADED,
                        "last run failed: unknown error"),
                // 5/6. silent break
                new Case("zero after non-empty history", inputs(POLLING, true, withCounts(10, 12, 9, 11, 0)),
                        HealthStatus.DEGRADED, HealthEvaluator.REASON_FORMAT_CHANGE),
                new Case("zero after exactly 3 non-empty", inputs(POLLING, true, withCounts(0, 5, 5, 5, 0)),
                        HealthStatus.DEGRADED, HealthEvaluator.REASON_FORMAT_CHANGE),
                new Case("zero after only 2 non-empty", inputs(POLLING, true, withCounts(0, 0, 5, 5, 0)),
                        HealthStatus.HEALTHY, null),
                new Case("zero with too little history", inputs(POLLING, true, withCounts(5, 5, 0)),
                        HealthStatus.HEALTHY, null),
                new Case("zero is normal for this source", inputs(ZERO_NORMAL, true, withCounts(10, 12, 9, 11, 0)),
                        HealthStatus.HEALTHY, null),
                new Case("non-zero latest is fine", inputs(POLLING, true, withCounts(10, 0, 0, 0, 3)),
                        HealthStatus.HEALTHY, null),
                new Case("failure beats silent break", inputs(POLLING, true, withFailures(1, "x")),
                        HealthStatus.DEGRADED, "last run failed"),
                // 7. refusals
                new Case("refusals above 20%",
                        new HealthInputs(POLLING, true, ok, null, usage(3, 1), null, null, NOW),
                        HealthStatus.DEGRADED, "budget refusals 25% in last hour"),
                new Case("refusals exactly 20% is not degraded",
                        new HealthInputs(POLLING, true, ok, null, usage(4, 1), null, null, NOW),
                        HealthStatus.HEALTHY, null),
                new Case("silent break beats refusals",
                        new HealthInputs(POLLING, true, withCounts(5, 5, 5, 5, 0), null, usage(1, 9), null, null, NOW),
                        HealthStatus.DEGRADED, HealthEvaluator.REASON_FORMAT_CHANGE),
                // 8. streams
                new Case("stream down for more than 2 minutes",
                        new HealthInputs(STREAMING, true, ok, null, UsageTotalsWindow.ZERO, null,
                                StreamState.disconnectedAt(NOW.minusSeconds(121)), NOW),
                        HealthStatus.DEGRADED, "stream not connected for 121s"),
                new Case("stream down for exactly 2 minutes",
                        new HealthInputs(STREAMING, true, ok, null, UsageTotalsWindow.ZERO, null,
                                StreamState.disconnectedAt(NOW.minusSeconds(120)), NOW),
                        HealthStatus.HEALTHY, null),
                new Case("stream connected",
                        new HealthInputs(STREAMING, true, ok, null, UsageTotalsWindow.ZERO, null,
                                StreamState.connectedAt(NOW.minusSeconds(3600)), NOW),
                        HealthStatus.HEALTHY, null),
                new Case("a polling source ignores stream state",
                        new HealthInputs(POLLING, true, ok, null, UsageTotalsWindow.ZERO, null,
                                StreamState.disconnectedAt(NOW.minusSeconds(3600)), NOW),
                        HealthStatus.HEALTHY, null),
                // 9. healthy
                new Case("healthy", inputs(POLLING, true, ok), HealthStatus.HEALTHY, null));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("statusTable")
    @DisplayName("the status table, in order")
    void table(Case c) {
        HealthAssessment result = evaluator.evaluate(c.in());

        assertThat(result.status()).isEqualTo(c.status());
        if (c.reasonPrefix() == null) {
            assertThat(result.statusReason()).isNull();
        } else {
            assertThat(result.statusReason()).startsWith(c.reasonPrefix());
        }
    }

    @Test
    @DisplayName("a 304 never enters the ring, so a run of 304s after a zero cannot change the verdict")
    void notModifiedDoesNotPollute() {
        // The tracker leaves the ring untouched on a 304; the evaluator sees exactly the counted runs.
        HealthSnapshot afterNotModified = withCounts(10, 10, 10, 10);
        assertThat(evaluator.evaluate(inputs(POLLING, true, afterNotModified)).status())
                .isEqualTo(HealthStatus.HEALTHY);
    }

    @Test
    @DisplayName("acceptance: a source that starts returning nothing after non-empty results is degraded with the format-change reason")
    void acceptance() {
        List<Integer> counts = new ArrayList<>();
        HealthAssessment result = null;
        for (int seen : new int[] {40, 38, 41, 39, 0}) {
            counts.add(seen);
            result = evaluator.evaluate(inputs(POLLING, true, withCounts(counts.toArray(Integer[]::new))));
        }
        assertThat(result.status()).isEqualTo(HealthStatus.DEGRADED);
        assertThat(result.statusReason()).isEqualTo(HealthEvaluator.REASON_FORMAT_CHANGE);
    }

    // ---------- coverage dropped vs possible format change ----------

    static Stream<Arguments> coverageTable() {
        return Stream.of(
                Arguments.of("zero records, receivers halved", withCounts(5, 5, 5, 5, 0),
                        ring(receivers(10), receivers(12), receivers(10), receivers(5)),
                        HealthStatus.DEGRADED, "coverage dropped: receivers 5 against a median of 10"),
                Arguments.of("zero records, coverage steady", withCounts(5, 5, 5, 5, 0),
                        ring(receivers(10), receivers(12), receivers(10), receivers(9)),
                        HealthStatus.DEGRADED, HealthEvaluator.REASON_FORMAT_CHANGE),
                Arguments.of("zero records, no coverage reported", withCounts(5, 5, 5, 5, 0),
                        ring(), HealthStatus.DEGRADED, HealthEvaluator.REASON_FORMAT_CHANGE),
                Arguments.of("records flowing, receivers dropped", withCounts(5, 5, 5, 5, 5),
                        ring(receivers(10), receivers(10), receivers(10), receivers(4)),
                        HealthStatus.DEGRADED, "coverage dropped: receivers 4"),
                Arguments.of("receivers fell by just under 50%", withCounts(5, 5, 5, 5, 5),
                        ring(receivers(10), receivers(10), receivers(10), receivers(6)),
                        HealthStatus.HEALTHY, null),
                Arguments.of("expected fell by 50%", withCounts(5, 5, 5, 5, 5),
                        ring(expected(100), expected(110), expected(90), expected(50)),
                        HealthStatus.DEGRADED, "coverage dropped: expected 50 against a median of 100"),
                Arguments.of("too little coverage history to judge", withCounts(5, 5, 5, 5, 5),
                        ring(receivers(10), receivers(10), receivers(1)),
                        HealthStatus.HEALTHY, null),
                Arguments.of("median of an even history", withCounts(5, 5, 5, 5, 5),
                        ring(receivers(8), receivers(12), receivers(10), receivers(20), receivers(5)),
                        HealthStatus.DEGRADED, "coverage dropped: receivers 5 against a median of 11"),
                Arguments.of("zero median is never a drop", withCounts(5, 5, 5, 5, 5),
                        ring(receivers(0), receivers(0), receivers(0), receivers(0)),
                        HealthStatus.HEALTHY, null),
                Arguments.of("zeroResultsNormal does not hide a coverage drop", withCounts(5, 5, 5, 5, 0),
                        ring(receivers(10), receivers(10), receivers(10), receivers(2)),
                        HealthStatus.DEGRADED, "coverage dropped"));
    }


    @Nested
    @DisplayName("coverage dropped vs possible format change")
    class CoverageRules {

        @ParameterizedTest(name = "{0}")
        @MethodSource("ai.foresignal.collect.HealthEvaluatorTest#coverageTable")
        void coverage(String name, HealthSnapshot counts, List<CoverageSample> ring, HealthStatus status, String reason) {
            SourceDescriptor d = name.startsWith("zeroResultsNormal") ? ZERO_NORMAL : POLLING;
            HealthAssessment result = evaluator.evaluate(inputs(d, true, withCoverage(counts, ring)));

            assertThat(result.status()).isEqualTo(status);
            if (reason == null) {
                assertThat(result.statusReason()).isNull();
            } else {
                assertThat(result.statusReason()).startsWith(reason);
            }
        }

        @Test
        @DisplayName("median of odd and even lists")
        void median() {
            assertThat(HealthEvaluator.median(List.of(3, 1, 2))).isEqualTo(2.0);
            assertThat(HealthEvaluator.median(List.of(4, 1, 2, 3))).isEqualTo(2.5);
        }
    }

    // ---------- derived figures ----------

    @Nested
    @DisplayName("budget hours remaining")
    class BudgetHours {

        private BudgetStatus status(long remaining) {
            return new BudgetStatus("eval", 60, 1000, 1000 - remaining, remaining, 10, null, null);
        }

        @Test
        @DisplayName("remaining divided by the last hour's request rate")
        void computed() {
            assertThat(HealthEvaluator.budgetHoursRemaining(status(500), usage(100, 0))).isEqualTo(5.0);
            assertThat(HealthEvaluator.budgetHoursRemaining(status(50), usage(100, 0))).isEqualTo(0.5);
        }

        @Test
        @DisplayName("refusals do not count toward the rate, because they spend nothing")
        void refusalsIgnored() {
            assertThat(HealthEvaluator.budgetHoursRemaining(status(500), usage(100, 400))).isEqualTo(5.0);
        }

        @Test
        @DisplayName("null when the rate is zero or there is no budget")
        void nullCases() {
            assertThat(HealthEvaluator.budgetHoursRemaining(status(500), usage(0, 3))).isNull();
            assertThat(HealthEvaluator.budgetHoursRemaining(null, usage(10, 0))).isNull();
        }

        @Test
        @DisplayName("an exhausted budget gives zero hours")
        void exhausted() {
            assertThat(HealthEvaluator.budgetHoursRemaining(status(0), usage(10, 0))).isZero();
        }

        @Test
        @DisplayName("error rate is errors over requests, 0 without requests")
        void errorRate() {
            assertThat(HealthEvaluator.errorRate(new UsageTotalsWindow(10, 0, 3, 0, null))).isEqualTo(0.3);
            assertThat(HealthEvaluator.errorRate(UsageTotalsWindow.ZERO)).isZero();
            assertThat(HealthEvaluator.errorRate(null)).isZero();
        }
    }

    // ---------- contributed rules ----------

    @Nested
    @DisplayName("contributed health rules")
    class ContributedRules {

        private final Instant frame = NOW.minus(Duration.ofHours(2));

        private HealthEvaluator with(HealthRule... rules) {
            return new HealthEvaluator(List.of(rules));
        }

        @Test
        @DisplayName("a rule can degrade an otherwise healthy source, and its frame time is reported")
        void degrades() {
            HealthRule stale = ctx -> FrameAgeRule.verdict(frame, Duration.ofMinutes(10), null, ctx.now());

            HealthAssessment result = with(stale).evaluate(inputs(POLLING, true, attempted()));

            assertThat(result.status()).isEqualTo(HealthStatus.DEGRADED);
            assertThat(result.statusReason()).isEqualTo("newest frame is 7200s old (limit 1800s)");
            assertThat(result.newestFrameAt()).isEqualTo(frame);
        }

        @Test
        @DisplayName("a healthy verdict keeps the built-in status but still reports the frame time")
        void healthyVerdict() {
            HealthRule fresh = ctx -> Optional.of(HealthVerdict.healthy(frame));

            HealthAssessment result = with(fresh).evaluate(inputs(POLLING, true, withFailures(1, "x")));

            assertThat(result.status()).isEqualTo(HealthStatus.DEGRADED);
            assertThat(result.statusReason()).startsWith("last run failed");
            assertThat(result.newestFrameAt()).isEqualTo(frame);
        }

        @Test
        @DisplayName("a failing verdict beats a built-in degraded")
        void worseWins() {
            HealthRule failing = ctx -> Optional.of(new HealthVerdict(HealthStatus.FAILING, "decoder broken", null));

            HealthAssessment result = with(failing).evaluate(inputs(POLLING, true, withFailures(1, "x")));

            assertThat(result.status()).isEqualTo(HealthStatus.FAILING);
            assertThat(result.statusReason()).isEqualTo("decoder broken");
        }

        @Test
        @DisplayName("an equally severe verdict keeps the built-in reason")
        void equalKeepsBuiltIn() {
            HealthRule degraded = ctx -> Optional.of(HealthVerdict.degraded("rule says so", null));

            HealthAssessment result = with(degraded).evaluate(inputs(POLLING, true, withFailures(1, "x")));

            assertThat(result.statusReason()).startsWith("last run failed");
        }

        @Test
        @DisplayName("rules do not run for disabled or idle sources, and cannot displace a failing reason")
        void notRunWhenNotRunning() {
            HealthRule failing = ctx -> Optional.of(new HealthVerdict(HealthStatus.FAILING, "rule", null));
            HealthEvaluator e = with(failing);

            assertThat(e.evaluate(inputs(POLLING, false, attempted())).status()).isEqualTo(HealthStatus.DISABLED);
            assertThat(e.evaluate(inputs(POLLING, true, neverAttempted())).status()).isEqualTo(HealthStatus.IDLE);
            assertThat(e.evaluate(inputs(POLLING, true, withFailures(3, "x"))).statusReason())
                    .startsWith("3 consecutive failures");
        }

        @Test
        @DisplayName("a failing source still reports the newest frame time a rule found")
        void failingKeepsFrameTime() {
            Instant frame = NOW.minusSeconds(30);
            HealthEvaluator e = with(ctx -> Optional.of(HealthVerdict.healthy(frame)));

            HealthAssessment result = e.evaluate(inputs(POLLING, true, withFailures(4, "x")));

            assertThat(result.status()).isEqualTo(HealthStatus.FAILING);
            assertThat(result.newestFrameAt()).isEqualTo(frame);
        }

        @Test
        @DisplayName("a rule that throws is ignored; an empty verdict changes nothing")
        void faultyRule() {
            HealthRule broken = ctx -> {
                throw new IllegalStateException("bug");
            };
            HealthRule silent = ctx -> Optional.empty();

            HealthAssessment result = with(broken, silent).evaluate(inputs(POLLING, true, attempted()));

            assertThat(result.status()).isEqualTo(HealthStatus.HEALTHY);
        }

        @Test
        @DisplayName("the rule context carries the source and the evaluation instant")
        void context() {
            List<HealthRuleContext> seen = new ArrayList<>();
            with(ctx -> {
                        seen.add(ctx);
                        return Optional.empty();
                    })
                    .evaluate(inputs(POLLING, true, attempted()));

            assertThat(seen).singleElement().satisfies(ctx -> {
                assertThat(ctx.sourceId()).isEqualTo("eval");
                assertThat(ctx.now()).isEqualTo(NOW);
            });
        }

        @Test
        @DisplayName("a verdict cannot be disabled or idle")
        void verdictStatuses() {
            assertThatThrownBy(() -> new HealthVerdict(HealthStatus.IDLE, "x", null))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new HealthVerdict(null, "x", null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("frame-age rule")
    class FrameAge {

        @Test
        @DisplayName("maxGap overrides three cadences")
        void maxGap() {
            Instant frame = NOW.minus(Duration.ofMinutes(20));
            assertThat(FrameAgeRule.verdict(frame, Duration.ofMinutes(10), Duration.ofMinutes(15), NOW))
                    .get().extracting(HealthVerdict::status).isEqualTo(HealthStatus.DEGRADED);
            assertThat(FrameAgeRule.verdict(frame, Duration.ofMinutes(10), null, NOW))
                    .get().extracting(HealthVerdict::status).isEqualTo(HealthStatus.HEALTHY);
        }

        @Test
        @DisplayName("exactly at the limit is still healthy")
        void boundary() {
            Instant frame = NOW.minus(Duration.ofMinutes(30));
            assertThat(FrameAgeRule.verdict(frame, Duration.ofMinutes(10), null, NOW))
                    .get().extracting(HealthVerdict::status).isEqualTo(HealthStatus.HEALTHY);
        }

        @Test
        @DisplayName("no frame or no threshold gives no verdict")
        void empty() {
            assertThat(FrameAgeRule.verdict(null, Duration.ofMinutes(10), null, NOW)).isEmpty();
            assertThat(FrameAgeRule.verdict(NOW, null, null, NOW)).isEmpty();
            assertThat(FrameAgeRule.verdict(NOW, Duration.ZERO, Duration.ZERO, NOW)).isEmpty();
        }
    }

    @Nested
    @DisplayName("health status")
    class Statuses {

        @Test
        @DisplayName("wire values are lowercase and parse back")
        void wire() {
            for (HealthStatus s : HealthStatus.values()) {
                assertThat(HealthStatus.fromValue(s.value())).isEqualTo(s);
            }
            assertThat(HealthStatus.DEGRADED.value()).isEqualTo("degraded");
        }

        @Test
        @DisplayName("worse compares only running statuses")
        void worse() {
            assertThat(HealthStatus.HEALTHY.worse(HealthStatus.DEGRADED)).isEqualTo(HealthStatus.DEGRADED);
            assertThat(HealthStatus.FAILING.worse(HealthStatus.DEGRADED)).isEqualTo(HealthStatus.FAILING);
            assertThat(HealthStatus.IDLE.worse(HealthStatus.FAILING)).isEqualTo(HealthStatus.IDLE);
            assertThat(HealthStatus.HEALTHY.worse(HealthStatus.DISABLED)).isEqualTo(HealthStatus.HEALTHY);
            assertThat(HealthStatus.HEALTHY.worse(null)).isEqualTo(HealthStatus.HEALTHY);
        }
    }

    @Nested
    @DisplayName("storage math")
    class Storage {

        private UsageTotalsWindow ingest(long seen, long versions, long bytes, long late, long clamped, long edges) {
            return new UsageTotalsWindow(0, 0, 0, 0,
                    new IngestCounts(seen, versions, 0, 0, late, 0, 0, bytes, clamped, edges));
        }

        @Test
        @DisplayName("rates per minute, ratios and the projection")
        void figures() {
            SourceStorage s = StorageMath.compute(
                    ingest(600, 60, 0, 4, 2, 15),
                    ingest(0, 0, 7_000, 0, 0, 0),
                    Duration.ofDays(7),
                    Duration.ofDays(30));

            assertThat(s.observationsPerMinute()).isEqualTo(10.0);
            assertThat(s.versionsPerMinute()).isEqualTo(1.0);
            assertThat(s.persistRatio()).isEqualTo(0.1);
            assertThat(s.triggerEdgeShare()).isEqualTo(0.25);
            assertThat(s.bytesPerDay()).isEqualTo(1_000);
            assertThat(s.projectedBytes()).isEqualTo(30_000);
            assertThat(s.lateLastHour()).isEqualTo(4);
            assertThat(s.clampedLastHour()).isEqualTo(2);
        }

        @Test
        @DisplayName("a source running for two days is averaged over two days, and the span is clamped")
        void span() {
            UsageTotalsWindow week = ingest(0, 0, 2_000, 0, 0, 0);
            assertThat(StorageMath.compute(null, week, Duration.ofDays(2), null).bytesPerDay()).isEqualTo(1_000);
            assertThat(StorageMath.compute(null, week, Duration.ofDays(30), null).bytesPerDay())
                    .isEqualTo(Math.round(2_000 / 7.0));
            assertThat(StorageMath.compute(null, week, Duration.ofMinutes(1), null).bytesPerDay())
                    .isEqualTo(48_000);
        }

        @Test
        @DisplayName("no usage is all zeros, and ratios never exceed 1")
        void zeros() {
            assertThat(StorageMath.compute(null, null, null, Duration.ofDays(7))).isEqualTo(SourceStorage.ZERO);
            assertThat(StorageMath.ratio(5, 2)).isEqualTo(1.0);
            assertThat(StorageMath.ratio(1, 0)).isZero();
        }
    }

    @Test
    @DisplayName("inputs default a missing row and usage")
    void inputDefaults() {
        HealthInputs in = new HealthInputs(POLLING, true, null, null, null, null, null, NOW);
        assertThat(in.health()).isEqualTo(HealthSnapshot.empty("eval"));
        assertThat(in.lastHour()).isEqualTo(UsageTotalsWindow.ZERO);
        assertThat(in.zeroResultsNormal()).isFalse();
    }
}
