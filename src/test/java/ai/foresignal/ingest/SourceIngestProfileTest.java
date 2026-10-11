package ai.foresignal.ingest;

import static ai.foresignal.ingest.IngestFixtures.aircraft;
import static ai.foresignal.ingest.IngestFixtures.aircraftDescriptor;
import static ai.foresignal.ingest.IngestFixtures.catalog;
import static ai.foresignal.ingest.IngestFixtures.catalogDescriptor;
import static ai.foresignal.ingest.IngestFixtures.events;
import static ai.foresignal.ingest.IngestFixtures.eventsDescriptor;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Profile derivation: keyframe, effective keyframe, touchMin, window per tier, load paths. */
class SourceIngestProfileTest {

    @Nested
    @DisplayName("keyframe")
    class Keyframe {

        @Test
        @DisplayName("the policy's keyframe wins")
        void policyKeyframe() {
            assertThat(aircraft().keyframe()).isEqualTo(Duration.ofSeconds(240));
        }

        @Test
        @DisplayName("ALL defaults to ttl − pollInterval − 30 s")
        void allDefault() {
            // 10 min − 60 s − 30 s.
            assertThat(events().keyframe()).isEqualTo(Duration.ofSeconds(510));
        }

        @Test
        @DisplayName("streams use a 2 s poll interval for the default")
        void streamDefault() {
            SourceDescriptor stream =
                    eventsDescriptor().mode(Mode.STREAMING).interval(null, null).build();
            assertThat(SourceIngestProfile.forDescriptor(stream).keyframe()).isEqualTo(Duration.ofSeconds(568));
            assertThat(SourceIngestProfile.forDescriptor(stream).pollInterval()).isEqualTo(Duration.ofSeconds(2));
        }

        @Test
        @DisplayName("none without a TTL")
        void noneWithoutTtl() {
            assertThat(catalog().keyframe()).isNull();
        }

        @Test
        @DisplayName("an explicit effective policy overrides the descriptor default")
        void effectivePolicy() {
            SourceIngestProfile profile =
                    SourceIngestProfile.of(eventsDescriptor().build(), IngestPolicy.all(Duration.ofMinutes(3)), Duration.ofDays(7));
            assertThat(profile.keyframe()).isEqualTo(Duration.ofMinutes(3));
            assertThat(profile.policy()).isEqualTo(IngestPolicy.all(Duration.ofMinutes(3)));
        }

        @Test
        @DisplayName("SAMPLED edgeMinInterval defaults to minInterval / 2")
        void edgeDefault() {
            SourceIngestProfile profile =
                    SourceIngestProfile.of(
                            aircraftDescriptor().build(),
                            IngestPolicy.sampled(Duration.ofSeconds(40), Duration.ofSeconds(240), 500.0, false),
                            Duration.ofDays(3));
            assertThat(profile.policy().edgeMinInterval()).isEqualTo(Duration.ofSeconds(20));
        }
    }

    @Nested
    @DisplayName("effectiveKeyframe = min(keyframe, window / 4)")
    class EffectiveKeyframe {

        @Test
        @DisplayName("the keyframe when it is shorter")
        void keyframeShorter() {
            assertThat(aircraft().retentionKeyframe()).isEqualTo(Duration.ofHours(18));
            assertThat(aircraft().effectiveKeyframe()).isEqualTo(Duration.ofSeconds(240));
        }

        @Test
        @DisplayName("window / 4 when the window is short")
        void windowShorter() {
            SourceIngestProfile profile = SourceIngestProfile.of(eventsDescriptor().build(), null, Duration.ofMinutes(20));
            assertThat(profile.effectiveKeyframe()).isEqualTo(Duration.ofMinutes(5));
        }

        @Test
        @DisplayName("window / 4 when the keyframe is none")
        void keyframeNone() {
            assertThat(catalog().effectiveKeyframe()).isEqualTo(Duration.ofHours(42));
        }
    }

    @ParameterizedTest(name = "ttl {0} → touchMin {1}")
    @CsvSource({"PT1M, PT30S", "PT5M, PT30S", "PT10M, PT1M", "PT2H, PT12M", "PT10H, PT1H", "P2D, PT1H"})
    @DisplayName("touchMin = clamp(ttl / 10, 30 s, 1 h)")
    void touchMin(Duration ttl, Duration expected) {
        assertThat(SourceIngestProfile.touchMin(ttl)).isEqualTo(expected);
    }

    @Test
    @DisplayName("touchMin is 1 h without a TTL")
    void touchMinWithoutTtl() {
        assertThat(SourceIngestProfile.touchMin(null)).isEqualTo(Duration.ofHours(1));
        assertThat(catalog().touchMin()).isEqualTo(Duration.ofHours(1));
    }

    @Nested
    @DisplayName("window per tier")
    class Window {

        private final RetentionSettings retention =
                new RetentionSettings(Duration.ofDays(10), null, null, Map.of(), Duration.ofDays(2));

        @Test
        @DisplayName("TRACK uses retention.trackWindow, ignoring any source retention")
        void track() {
            assertThat(SourceIngestProfile.window(aircraftDescriptor().build(), Optional.of(Duration.ofDays(99)), retention))
                    .isEqualTo(Duration.ofDays(2));
            assertThat(aircraft().window()).isEqualTo(RetentionSettings.DEFAULT_TRACK_WINDOW);
        }

        @Test
        @DisplayName("STANDARD uses the effective retention, else retention.default")
        void standard() {
            assertThat(SourceIngestProfile.window(eventsDescriptor().build(), Optional.of(Duration.ofDays(30)), retention))
                    .isEqualTo(Duration.ofDays(30));
            assertThat(SourceIngestProfile.window(eventsDescriptor().build(), Optional.empty(), retention))
                    .isEqualTo(Duration.ofDays(10));
            assertThat(SourceIngestProfile.window(eventsDescriptor().build(), Optional.empty(), null))
                    .isEqualTo(Duration.ofDays(7));
        }

        @Test
        @DisplayName("forDescriptor uses the descriptor's defaultRetention on STANDARD")
        void descriptorDefault() {
            SourceDescriptor descriptor = catalogDescriptor().defaultRetention(Duration.ofDays(1825)).build();
            assertThat(SourceIngestProfile.forDescriptor(descriptor).window()).isEqualTo(Duration.ofDays(1825));
        }
    }

    @Test
    @DisplayName("the profile copies the descriptor's ingest-relevant fields")
    void copiesDescriptor() {
        SourceDescriptor descriptor =
                aircraftDescriptor().volatileFields("seen").titleField("flight").entityKey("icao24", "hex")
                        .privateSource(true).privatePrecisionDecimals(5).build();
        SourceIngestProfile profile = SourceIngestProfile.forDescriptor(descriptor);

        assertThat(profile.sourceId()).isEqualTo("test-aircraft");
        assertThat(profile.isTrack()).isTrue();
        assertThat(profile.ttlAnchor()).isEqualTo(TtlAnchor.OBSERVED);
        assertThat(profile.volatileFields()).containsExactly("seen");
        assertThat(profile.significantFields()).containsExactlyInAnyOrder("flight", "squawk");
        assertThat(profile.titleField()).isEqualTo("flight");
        assertThat(profile.entityKeys()).containsEntry("icao24", "hex");
        assertThat(profile.privateSource()).isTrue();
        assertThat(profile.privatePrecisionDecimals()).isEqualTo(5);
        assertThat(profile.recipe()).isEqualTo(ContentHasher.recipe(descriptor.volatileFields(), descriptor.significantFields()));
    }

    @Test
    @DisplayName("anchor follows ttlAnchor")
    void anchor() {
        Instant observed = Instant.parse("2026-10-11T10:00:00Z");
        Instant received = Instant.parse("2026-10-11T10:00:05Z");
        assertThat(aircraft().anchor(observed, received)).isEqualTo(observed);
        assertThat(events().anchor(observed, received)).isEqualTo(received);
    }

    @Test
    @DisplayName("load paths are motion and threshold fields plus the gate's paths")
    void loadPaths() {
        GateSnapshot gate =
                new GateSnapshot() {
                    @Override
                    public GateSpec spec(String sourceId) {
                        return new GateSpec(Set.of("squawk", "nav.mode"), true);
                    }

                    @Override
                    public boolean flips(String sourceId, String kind, GateState persisted, GateState candidate) {
                        return false;
                    }
                };

        assertThat(aircraft().basePaths()).containsExactlyInAnyOrder("gs", "track", "alt");
        assertThat(aircraft().loadPaths(gate)).containsExactlyInAnyOrder("gs", "track", "alt", "squawk", "nav.mode");
        assertThat(aircraft().loadPaths(GateSnapshot.EMPTY)).containsExactlyInAnyOrder("gs", "track", "alt");
        assertThat(aircraft().loadPaths(null)).containsExactlyInAnyOrder("gs", "track", "alt");
        assertThat(aircraft().needsPersistedGeometry(gate)).isTrue();
        assertThat(aircraft().needsPersistedGeometry(GateSnapshot.EMPTY)).isFalse();
        assertThat(events().basePaths()).isEmpty();
    }
}
