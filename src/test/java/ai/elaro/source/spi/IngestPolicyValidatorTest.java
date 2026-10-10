package ai.elaro.source.spi;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** The §5.1 validation matrix. */
class IngestPolicyValidatorTest {

    private static final Duration S2 = Duration.ofSeconds(2);
    private static final Duration S30 = Duration.ofSeconds(30);
    private static final Duration S60 = Duration.ofSeconds(60);
    private static final Duration S240 = Duration.ofSeconds(240);
    private static final Duration MIN5 = Duration.ofMinutes(5);

    /** A polling STANDARD source with no TTL. */
    private static SourceDescriptor.Builder standard() {
        return SourceDescriptor.builder("standard-source")
                .kind("thing")
                .interval(S30, S60)
                .defaultBudget(60, 10_000);
    }

    /**
     * A TRACK source shaped like the aircraft defaults in §5.3.
     *
     * <p>The 10-second poll interval is not incidental. §5.3 pairs a 240s keyframe with a 5-minute
     * TTL, and {@code keyframe <= ttl - pollInterval - 30s} then requires the poll interval to be
     * 30s or less. A 60-second interval would make the documented default invalid.
     */
    private static SourceDescriptor.Builder track() {
        return SourceDescriptor.builder("track-source")
                .kind("aircraft")
                .historyTier(HistoryTier.TRACK)
                .interval(Duration.ofSeconds(5), Duration.ofSeconds(10))
                .defaultBudget(60, 10_000)
                .recordTtl(MIN5)
                .significantFields("flight", "squawk");
    }

    private static java.util.List<String> problems(IngestPolicy policy, SourceDescriptor descriptor) {
        return IngestPolicyValidator.problems(policy, descriptor, "ingestPolicy");
    }

    @Nested
    @DisplayName("keyframe")
    class Keyframe {

        @Test
        @DisplayName("'none' is allowed only when the source has no TTL")
        void noneOnlyWithoutTtl() {
            assertThat(problems(IngestPolicy.all(), standard().build())).isEmpty();

            assertThat(problems(IngestPolicy.all(), standard().recordTtl(MIN5).build()))
                    .singleElement()
                    .asString()
                    .contains("keyframe is required")
                    .contains("only allowed when the source has no TTL");
        }

        @Test
        @DisplayName("must not exceed ttl - pollInterval - 30s")
        void respectsTheTtlCeiling() {
            // ttl 5m, poll 60s, margin 30s -> ceiling 3m30s.
            SourceDescriptor descriptor = standard().recordTtl(MIN5).build();

            assertThat(IngestPolicyValidator.keyframeCeiling(descriptor))
                    .isEqualTo(Duration.ofSeconds(210));
            assertThat(problems(IngestPolicy.all(Duration.ofSeconds(210)), descriptor)).isEmpty();
            assertThat(problems(IngestPolicy.all(Duration.ofSeconds(211)), descriptor))
                    .singleElement()
                    .asString()
                    .contains("must not exceed ttl - pollInterval - 30s");
        }

        @Test
        @DisplayName("a stream's ceiling uses a 2-second poll interval")
        void streamUsesTwoSecondPoll() {
            SourceDescriptor stream =
                    SourceDescriptor.builder("stream")
                            .kind("vessel")
                            .mode(Mode.STREAMING)
                            .defaultBudget(10, 100)
                            .recordTtl(MIN5)
                            .build();

            assertThat(IngestPolicyValidator.keyframeCeiling(stream))
                    .isEqualTo(MIN5.minus(S2).minus(Duration.ofSeconds(30)));
        }

        @Test
        @DisplayName("a TTL too short for the poll interval is rejected with the reason")
        void ttlTooShortForPollInterval() {
            // ttl 30s, poll 60s -> the ceiling is negative, so no keyframe can work.
            SourceDescriptor descriptor = standard().recordTtl(S30).build();

            assertThat(problems(IngestPolicy.all(Duration.ofSeconds(5)), descriptor))
                    .anySatisfy(
                            problem ->
                                    assertThat(problem)
                                            .contains("too short for its poll interval")
                                            .contains("no keyframe can satisfy"));
        }

        @Test
        void mustBePositive() {
            assertThat(problems(IngestPolicy.all(Duration.ZERO), standard().build()))
                    .anySatisfy(p -> assertThat(p).contains("keyframe must be greater than zero"));
            assertThat(problems(IngestPolicy.all(Duration.ofSeconds(-1)), standard().build()))
                    .anySatisfy(p -> assertThat(p).contains("keyframe must be greater than zero"));
        }
    }

    @Nested
    @DisplayName("minInterval")
    class MinInterval {

        @Test
        @DisplayName("must be at least 2 seconds")
        void hasAFloor() {
            SourceDescriptor descriptor = track().build();

            assertThat(problems(IngestPolicy.sampled(S2, S240, 1000.0, true), descriptor)).isEmpty();
            assertThat(problems(IngestPolicy.sampled(Duration.ofSeconds(1), S240, 1000.0, true), descriptor))
                    .anySatisfy(p -> assertThat(p).contains("minInterval must be at least 2s"));
        }

        @Test
        @DisplayName("must not exceed the keyframe")
        void mustNotExceedKeyframe() {
            SourceDescriptor descriptor = track().build();

            assertThat(problems(IngestPolicy.sampled(S240, S240, 1000.0, true), descriptor)).isEmpty();
            assertThat(problems(IngestPolicy.sampled(Duration.ofSeconds(241), S240, 1000.0, true), descriptor))
                    .anySatisfy(p -> assertThat(p).contains("must not exceed ingestPolicy.keyframe"));
        }
    }

    @Nested
    @DisplayName("edgeMinInterval")
    class EdgeMinInterval {

        @Test
        @DisplayName("defaults to half the minInterval")
        void defaultsToHalfMinInterval() {
            SourceDescriptor descriptor = track().build();

            IngestPolicy applied =
                    IngestPolicyValidator.applyDefaults(
                            IngestPolicy.sampled(S60, S240, 1000.0, true), descriptor);

            assertThat(applied.edgeMinInterval()).isEqualTo(S30);
        }

        @Test
        void mustNotExceedMinInterval() {
            SourceDescriptor descriptor = track().build();
            var policy = new IngestPolicy.Sampled(S60, S240, 1000.0, true, Duration.ofSeconds(61));

            assertThat(problems(policy, descriptor))
                    .anySatisfy(p -> assertThat(p).contains("edgeMinInterval"))
                    .anySatisfy(p -> assertThat(p).contains("must not exceed ingestPolicy.minInterval"));
        }

        @Test
        void mustNotBeNegative() {
            var policy = new IngestPolicy.Sampled(S60, S240, 1000.0, true, Duration.ofSeconds(-1));

            assertThat(problems(policy, track().build()))
                    .anySatisfy(p -> assertThat(p).contains("edgeMinInterval must not be negative"));
        }

        @Test
        @DisplayName("an explicit value is kept")
        void explicitValueIsKept() {
            var policy = new IngestPolicy.Sampled(S60, S240, 1000.0, true, Duration.ofSeconds(5));

            assertThat(IngestPolicyValidator.applyDefaults(policy, track().build()).edgeMinInterval())
                    .isEqualTo(Duration.ofSeconds(5));
        }
    }

    @Nested
    @DisplayName("significantFields")
    class SignificantFields {

        @Test
        @DisplayName("SAMPLED and LATEST require them; ALL does not")
        void requiredBySamplingModes() {
            SourceDescriptor without =
                    SourceDescriptor.builder("no-significant")
                            .kind("aircraft")
                            .historyTier(HistoryTier.TRACK)
                            .interval(Duration.ofSeconds(5), Duration.ofSeconds(10))
                            .defaultBudget(60, 10_000)
                            .recordTtl(MIN5)
                            .build();

            assertThat(problems(IngestPolicy.sampled(S60, S240, 1000.0, true), without))
                    .anySatisfy(p -> assertThat(p).contains("requires the descriptor to declare significantFields"));
            assertThat(problems(IngestPolicy.latest(S240), without))
                    .anySatisfy(p -> assertThat(p).contains("requires the descriptor to declare significantFields"));
            assertThat(problems(IngestPolicy.all(Duration.ofSeconds(200)), without)).isEmpty();
        }

        @Test
        void satisfiedWhenDeclared() {
            assertThat(problems(IngestPolicy.sampled(S60, S240, 1000.0, true), track().build())).isEmpty();
            assertThat(problems(IngestPolicy.latest(S240), track().build())).isEmpty();
        }
    }

    @Nested
    @DisplayName("defaults")
    class Defaults {

        @Test
        @DisplayName("an ALL policy with no keyframe gets ttl - poll - 30s when there is a TTL")
        void allGetsDerivedKeyframe() {
            SourceDescriptor descriptor = standard().recordTtl(MIN5).build();

            IngestPolicy applied = IngestPolicyValidator.applyDefaults(IngestPolicy.all(), descriptor);

            assertThat(applied.keyframe()).isEqualTo(Duration.ofSeconds(210));
            assertThat(problems(applied, descriptor)).isEmpty();
        }

        @Test
        @DisplayName("an ALL policy stays keyframe-less when there is no TTL")
        void allStaysNoneWithoutTtl() {
            IngestPolicy applied =
                    IngestPolicyValidator.applyDefaults(IngestPolicy.all(), standard().build());

            assertThat(applied.keyframe()).isNull();
            assertThat(IngestPolicyValidator.defaultAllKeyframe(standard().build())).isNull();
        }

        @Test
        @DisplayName("no default keyframe is invented when the TTL is too short to allow one")
        void noKeyframeWhenCeilingIsNotPositive() {
            assertThat(IngestPolicyValidator.defaultAllKeyframe(standard().recordTtl(S30).build()))
                    .isNull();
        }

        @Test
        void anExplicitKeyframeIsKept() {
            IngestPolicy applied =
                    IngestPolicyValidator.applyDefaults(
                            IngestPolicy.all(Duration.ofSeconds(100)), standard().recordTtl(MIN5).build());

            assertThat(applied.keyframe()).isEqualTo(Duration.ofSeconds(100));
        }

        @Test
        void nullPolicyStaysNull() {
            assertThat(IngestPolicyValidator.applyDefaults(null, standard().build())).isNull();
            assertThat(problems(null, standard().build())).isEmpty();
        }
    }

    @Nested
    @DisplayName("deviation")
    class Deviation {

        @Test
        void mustBePositiveWhenSet() {
            var zero = new IngestPolicy.Sampled(S60, S240, 0.0, true, S30);
            var negative = new IngestPolicy.Sampled(S60, S240, -5.0, true, S30);

            assertThat(problems(zero, track().build()))
                    .anySatisfy(p -> assertThat(p).contains("minDeviationMeters must be greater than zero"));
            assertThat(problems(negative, track().build()))
                    .anySatisfy(p -> assertThat(p).contains("minDeviationMeters must be greater than zero"));
        }

        @Test
        @DisplayName("omitting the deviation is allowed: the interval alone then decides")
        void mayBeAbsent() {
            var policy = new IngestPolicy.Sampled(S60, S240, null, true, S30);

            assertThat(problems(policy, track().build())).isEmpty();
        }
    }

    @Nested
    @DisplayName("mode shape")
    class ModeShape {

        @Test
        @DisplayName("LATEST is SAMPLED without an interval")
        void latestHasNoInterval() {
            IngestPolicy latest = IngestPolicy.latest(S240);

            assertThat(latest.minInterval()).isNull();
            assertThat(latest.mode()).isEqualTo(IngestPolicy.PolicyMode.LATEST);
            assertThat(latest.requiresSignificantFields()).isTrue();
        }

        @Test
        @DisplayName("ALL has neither an interval nor an edge interval")
        void allHasNeitherInterval() {
            IngestPolicy all = IngestPolicy.all(S240);

            assertThat(all.minInterval()).isNull();
            assertThat(all.edgeMinInterval()).isNull();
            assertThat(all.requiresSignificantFields()).isFalse();
        }

        @Test
        @DisplayName("the §5.3 aircraft default validates")
        void aircraftDefaultIsValid() {
            // SAMPLED, minInterval 60s, keyframe 240s, TTL 5m, deviation 1000m.
            SourceDescriptor aircraft = track().build();
            IngestPolicy policy =
                    IngestPolicyValidator.applyDefaults(
                            IngestPolicy.sampled(S60, S240, 1000.0, true), aircraft);

            assertThat(problems(policy, aircraft)).isEmpty();
            assertThat(policy.edgeMinInterval()).isEqualTo(S30);
        }
    }

    @Test
    @DisplayName("every problem is reported, not just the first")
    void reportsEveryProblem() {
        SourceDescriptor descriptor =
                SourceDescriptor.builder("messy")
                        .kind("aircraft")
                        .historyTier(HistoryTier.TRACK)
                        .interval(Duration.ofSeconds(5), Duration.ofSeconds(10))
                        .defaultBudget(60, 10_000)
                        .recordTtl(MIN5)
                        .build();
        // No significantFields, minInterval below the floor, and a keyframe over the ceiling.
        var policy =
                new IngestPolicy.Sampled(
                        Duration.ofSeconds(1), Duration.ofSeconds(600), 1000.0, true, Duration.ofSeconds(1));

        assertThat(problems(policy, descriptor)).hasSizeGreaterThanOrEqualTo(3);
    }
}
