package ai.foresignal.source;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FieldThreshold;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.MotionSpec;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.testsupport.adapters.FakeFamily;
import ai.foresignal.testsupport.adapters.FakePollingAdapter;
import ai.foresignal.testsupport.adapters.FakePushAdapter;
import ai.foresignal.testsupport.adapters.FakeStreamingAdapter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

class SourceRegistryTest {

    private final List<Object> published = new ArrayList<>();

    private final ApplicationEventPublisher events =
            new ApplicationEventPublisher() {
                @Override
                public void publishEvent(ApplicationEvent event) {
                    published.add(event);
                }

                @Override
                public void publishEvent(Object event) {
                    published.add(event);
                }
            };

    private SourceRegistry registry(List<SourceAdapter> adapters) {
        return new SourceRegistry(adapters, List.of(), events);
    }

    private static SourceDescriptor.Builder valid(String id) {
        return SourceDescriptor.builder(id)
                .kind("thing")
                .interval(Duration.ofSeconds(30), Duration.ofMinutes(1))
                .defaultBudget(60, 10_000);
    }

    /** An adapter whose descriptor is whatever the test supplies, so invalid shapes are reachable. */
    private record Fixed(SourceDescriptor descriptor) implements ai.foresignal.source.spi.PollingAdapter {
        @Override
        public ai.foresignal.source.spi.FetchResult fetch(ai.foresignal.source.spi.FetchContext ctx) {
            return ai.foresignal.source.spi.FetchResult.complete(List.of());
        }
    }

    private void assertRejected(SourceDescriptor descriptor, String expectedFragment) {
        assertThatThrownBy(() -> registry(List.of(new Fixed(descriptor))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Foresignal cannot start")
                .hasMessageContaining(expectedFragment);
    }

    // ---------- discovery ----------

    @Nested
    @DisplayName("discovery")
    class Discovery {

        @Test
        void findsAdapterBeans() {
            SourceRegistry registry =
                    registry(List.of(FakePollingAdapter.withId("alpha"), FakePollingAdapter.withId("bravo")));

            assertThat(registry.ids()).containsExactly("alpha", "bravo");
            assertThat(registry.all()).hasSize(2);
            assertThat(registry.descriptors()).extracting(SourceDescriptor::id).containsExactly("alpha", "bravo");
        }

        @Test
        @DisplayName("every feed of a family becomes its own source")
        void expandsFamilies() {
            SourceRegistry registry =
                    new SourceRegistry(
                            List.of(FakePollingAdapter.withId("standalone")),
                            List.of(new FakeFamily()),
                            events);

            assertThat(registry.ids())
                    .containsExactlyInAnyOrderElementsOf(
                            concat(List.of("standalone"), FakeFamily.FEED_IDS));
            assertThat(registry.get("fake-family-alpha").dynamic()).isFalse();
        }

        @Test
        void collectsKindsAcrossSources() {
            SourceRegistry registry =
                    registry(
                            List.of(
                                    new Fixed(valid("quakes").kind("earthquake").build()),
                                    new Fixed(valid("fires").kind("fire").build())));

            assertThat(registry.kinds()).contains("earthquake", "fire", "thing");
        }

        @Test
        void findAndGetAgree() {
            SourceRegistry registry = registry(List.of(FakePollingAdapter.withId("alpha")));

            assertThat(registry.find("alpha")).isPresent();
            assertThat(registry.get("alpha").id()).isEqualTo("alpha");
            assertThat(registry.contains("alpha")).isTrue();
            assertThat(registry.find("nope")).isEmpty();
            assertThat(registry.find(null)).isEmpty();
            assertThat(registry.contains(null)).isFalse();
        }

        @Test
        @DisplayName("get on an unknown id is a 404, not a null")
        void getThrowsNotFound() {
            SourceRegistry registry = registry(List.of());

            assertThatThrownBy(() -> registry.get("nope"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("nope");
        }

        @Test
        @DisplayName("a streaming adapter carries its adapter; a dynamic source does not")
        void registrationCarriesTheAdapter() {
            SourceRegistry registry = registry(List.of(FakeStreamingAdapter.withId("stream")));

            assertThat(registry.get("stream").adapterIfPresent()).isPresent();
        }
    }

    // ---------- validation ----------

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("every problem across every adapter is reported in one message")
        void reportsEveryProblemAtOnce() {
            var badId = valid("Bad_Id").build();
            var noKinds = SourceDescriptor.builder("no-kinds").defaultBudget(1, 1).build();

            assertThatThrownBy(() -> registry(List.of(new Fixed(badId), new Fixed(noKinds))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Bad_Id")
                    .hasMessageContaining("declares no kinds")
                    .hasMessageContaining("problem(s)");
        }

        @Test
        void rejectsNonKebabIds() {
            assertRejected(valid("Bad_Id").build(), "must be kebab-case");
            assertRejected(valid("trailing-").build(), "must be kebab-case");
            assertRejected(valid("double--hyphen").build(), "must be kebab-case");
            assertRejected(valid("UPPER").build(), "must be kebab-case");
        }

        @Test
        void rejectsOverlongIds() {
            assertRejected(valid("a".repeat(65)).build(), "the maximum is 64");
        }

        @Test
        void rejectsDuplicateIds() {
            assertThatThrownBy(
                            () ->
                                    registry(
                                            List.of(
                                                    FakePollingAdapter.withId("same"),
                                                    FakePollingAdapter.withId("same"))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Duplicate source id 'same'");
        }

        @Test
        void rejectsEmptyOrBadKinds() {
            assertRejected(SourceDescriptor.builder("x").defaultBudget(1, 1).build(), "declares no kinds");
            assertRejected(valid("x").kind("Bad Kind").build(), "must be kebab-case, like the source id");
        }

        @Test
        @DisplayName("minInterval above defaultInterval is rejected")
        void rejectsIntervalInversion() {
            assertRejected(
                    SourceDescriptor.builder("x")
                            .kind("thing")
                            .interval(Duration.ofMinutes(5), Duration.ofMinutes(1))
                            .defaultBudget(1, 1)
                            .build(),
                    "must not exceed defaultInterval");
        }

        @Test
        void rejectsMissingIntervalsForPollingSources() {
            assertRejected(
                    SourceDescriptor.builder("x").kind("thing").defaultBudget(1, 1).build(),
                    "declares no defaultInterval");
        }

        @Test
        void rejectsNonPositiveBudgets() {
            assertRejected(valid("x").defaultBudget(0, 10).build(), "perMinute must be greater than zero");
            assertRejected(valid("x").defaultBudget(10, 0).build(), "perDay must be greater than zero");
            assertRejected(valid("x").defaultBudget(-1, 10).build(), "perMinute must be greater than zero");
        }

        @Test
        void rejectsDuplicateParamNames() {
            assertRejected(
                    valid("x")
                            .param(ParamSpec.optional("feed", ParamType.STRING, "", "a"))
                            .param(ParamSpec.optional("feed", ParamType.STRING, "", "b"))
                            .build(),
                    "duplicate param 'feed'");
        }

        @Test
        @DisplayName("a default that is not among the allowed values is rejected")
        void rejectsInvalidDefault() {
            assertRejected(
                    valid("x").param(ParamSpec.enumerated("feed", "", "zzz", List.of("a", "b"))).build(),
                    "which is not one of");
        }

        @Test
        @DisplayName("required and defaulted cannot both apply")
        void rejectsRequiredWithDefault() {
            assertRejected(
                    valid("x")
                            .param(new ParamSpec("feed", ParamType.STRING, "", "a", true, List.of()))
                            .build(),
                    "required and also has a default");
        }

        @Test
        @DisplayName("the mode must match the implemented interface")
        void rejectsModeMismatch() {
            // A PollingAdapter claiming to stream.
            assertRejected(
                    valid("x").mode(Mode.STREAMING).build(), "does not implement StreamingAdapter");

            // A PushAdapter claiming to poll.
            SourceDescriptor pollingDescriptor = valid("pusher").mode(Mode.POLLING).build();
            assertThatThrownBy(
                            () -> registry(List.of(new FakePushAdapter(pollingDescriptor))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("does not implement PollingAdapter");
        }

        @Test
        @DisplayName("a PUSH descriptor needs no fetch implementation")
        void pushNeedsNoFetch() {
            SourceRegistry registry = registry(List.of(FakePushAdapter.withId("pusher")));

            assertThat(registry.get("pusher").descriptor().mode()).isEqualTo(Mode.PUSH);
        }

        @Test
        @DisplayName("a DERIVED source must have no TTL")
        void rejectsDerivedWithTtl() {
            assertRejected(
                    valid("derived")
                            .mode(Mode.DERIVED)
                            .recordTtl(Duration.ofMinutes(5))
                            .build(),
                    "is DERIVED, so recordTtl must be null");
        }

        @Test
        @DisplayName("a DERIVED source with no TTL is accepted and needs no fetch implementation")
        void acceptsDerived() {
            SourceDescriptor descriptor = valid("derived").mode(Mode.DERIVED).build();

            SourceRegistry registry = registry(List.of(new Fixed(descriptor)));

            assertThat(registry.get("derived").descriptor().mode()).isEqualTo(Mode.DERIVED);
        }
    }

    // ---------- v1.2 tier and field rules ----------

    @Nested
    @DisplayName("history tier")
    class Tier {

        @Test
        @DisplayName("TRACK without a TTL is rejected")
        void trackNeedsTtl() {
            assertRejected(
                    valid("track").historyTier(HistoryTier.TRACK).significantFields("flight").build(),
                    "is TRACK, which requires a recordTtl");
        }

        @Test
        @DisplayName("TRACK must anchor its TTL on observedAt")
        void trackNeedsObservedAnchor() {
            assertRejected(
                    valid("track")
                            .historyTier(HistoryTier.TRACK)
                            .recordTtl(Duration.ofMinutes(5))
                            .ttlAnchor(TtlAnchor.RECEIVED)
                            .build(),
                    "ttlAnchor must be OBSERVED");
        }

        @Test
        @DisplayName("TRACK must not set defaultRetention: the tier fixes it")
        void trackRejectsDefaultRetention() {
            assertRejected(
                    valid("track")
                            .historyTier(HistoryTier.TRACK)
                            .recordTtl(Duration.ofMinutes(5))
                            .defaultRetention(Duration.ofDays(7))
                            .build(),
                    "must not set defaultRetention");
        }

        @Test
        void trackRejectsKindRetention() {
            assertRejected(
                    valid("track")
                            .historyTier(HistoryTier.TRACK)
                            .recordTtl(Duration.ofMinutes(5))
                            .kindRetention("aircraft", Duration.ofDays(1))
                            .build(),
                    "must not set kindRetention");
        }

        @Test
        @DisplayName("a well-formed TRACK source is accepted")
        void acceptsValidTrack() {
            SourceDescriptor descriptor =
                    valid("adsblol-mil")
                            .kind("aircraft")
                            .historyTier(HistoryTier.TRACK)
                            .recordTtl(Duration.ofMinutes(5))
                            .significantFields("flight", "squawk")
                            .defaultIngestPolicy(
                                    IngestPolicy.sampled(
                                            Duration.ofSeconds(60), Duration.ofSeconds(180), 1000.0, true))
                            .build();

            assertThat(registry(List.of(new Fixed(descriptor))).get("adsblol-mil")).isNotNull();
        }
    }

    @Nested
    @DisplayName("field sets")
    class FieldSets {

        @Test
        @DisplayName("SAMPLED without significantFields is rejected")
        void samplingNeedsSignificantFields() {
            assertRejected(
                    valid("sampled")
                            .historyTier(HistoryTier.TRACK)
                            .recordTtl(Duration.ofMinutes(5))
                            .defaultIngestPolicy(
                                    IngestPolicy.sampled(
                                            Duration.ofSeconds(60), Duration.ofSeconds(180), 1000.0, true))
                            .build(),
                    "requires the descriptor to declare significantFields");
        }

        @Test
        void latestNeedsSignificantFields() {
            assertRejected(
                    valid("latest")
                            .defaultIngestPolicy(IngestPolicy.latest(Duration.ofSeconds(180)))
                            .build(),
                    "requires the descriptor to declare significantFields");
        }

        @Test
        @DisplayName("a threshold field must not also be significant")
        void thresholdFieldsMustNotBeSignificant() {
            assertRejected(
                    valid("x")
                            .significantFields("altitude")
                            .threshold(FieldThreshold.of("altitude", 1000))
                            .build(),
                    "must not also be significant");
        }

        @Test
        @DisplayName("a motion field must not be significant")
        void motionFieldsMustNotBeSignificant() {
            assertRejected(
                    valid("x")
                            .significantFields("sog", "name")
                            .motion(new MotionSpec("sog", MotionSpec.SpeedUnit.KNOTS, "cog"))
                            .build(),
                    "motion field 'sog' must not be significant");
        }

        @Test
        @DisplayName("a field cannot be both volatile and significant")
        void volatileAndSignificantConflict() {
            assertRejected(
                    valid("x").volatileFields("counter").significantFields("counter").build(),
                    "both volatile and significant");
        }

        @Test
        @DisplayName("field references must be top-level payload keys, not paths")
        void rejectsNestedFieldPaths() {
            assertRejected(
                    valid("x").significantFields("properties.mag").build(),
                    "must be a top-level payload key");
            assertRejected(
                    valid("x").volatileFields("a[0]").build(), "must be a top-level payload key");
            assertRejected(valid("x").rankField("properties.mag").build(), "must be a top-level payload key");
            assertRejected(
                    valid("x").tileProperties("a/b").build(), "must be a top-level payload key");
        }

        @Test
        @DisplayName("a non-significant search field is a warning, not a failure")
        void nonSignificantSearchFieldOnlyWarns() {
            SourceDescriptor descriptor =
                    valid("x").significantFields("callsign").searchFields("name").build();

            // Registers successfully; DescriptorValidator reports it as a warning.
            assertThat(registry(List.of(new Fixed(descriptor))).contains("x")).isTrue();
            assertThat(DescriptorValidator.warnings(descriptor))
                    .anySatisfy(w -> assertThat(w).contains("searchField 'name' is not significant"));
        }

        @Test
        void nonSignificantTilePropertyOnlyWarns() {
            SourceDescriptor descriptor =
                    valid("x").significantFields("callsign").tileProperties("altitude").build();

            assertThat(DescriptorValidator.warnings(descriptor))
                    .anySatisfy(w -> assertThat(w).contains("tileProperty 'altitude' is not significant"));
        }

        @Test
        @DisplayName("no warnings when significantFields is empty, since everything is significant")
        void noWarningsWithoutSignificantFields() {
            // Checkpoint 124: the fixture names a licence, since 109 warns about a source without one.
            assertThat(DescriptorValidator.warnings(valid("x").licence("CC-BY-4.0").searchFields("anything").build()))
                    .isEmpty();
        }
    }

    // ---------- dynamic registration ----------

    @Nested
    @DisplayName("dynamic registration")
    class Dynamic {

        private SourceDescriptor custom(String id) {
            return SourceDescriptor.builder(id)
                    .kind("custom-thing")
                    .mode(Mode.PUSH)
                    .defaultBudget(Budget.of(60, 10_000))
                    .build();
        }

        @Test
        @DisplayName("a PUSH source can be registered at runtime and publishes an event")
        void registersPushSource() {
            SourceRegistry registry = registry(List.of());

            registry.registerDynamic(custom("custom-sdr"));

            assertThat(registry.contains("custom-sdr")).isTrue();
            assertThat(registry.get("custom-sdr").dynamic()).isTrue();
            assertThat(registry.get("custom-sdr").adapterIfPresent()).isEmpty();
            assertThat(published)
                    .containsExactly(SourceRegistryChangedEvent.registered("custom-sdr"));
        }

        @Test
        void unregisterRemovesAndPublishes() {
            SourceRegistry registry = registry(List.of());
            registry.registerDynamic(custom("custom-sdr"));
            published.clear();

            registry.unregisterDynamic("custom-sdr");

            assertThat(registry.contains("custom-sdr")).isFalse();
            assertThat(published)
                    .containsExactly(SourceRegistryChangedEvent.unregistered("custom-sdr"));
        }

        @Test
        @DisplayName("redefining a dynamic source replaces its descriptor and publishes REDEFINED (024)")
        void redefinesDynamicSource() {
            SourceRegistry registry = registry(List.of());
            registry.registerDynamic(custom("custom-sdr"));
            published.clear();
            SourceDescriptor renamed = SourceDescriptor.builder("custom-sdr").name("Renamed").kind("custom-thing")
                    .mode(Mode.PUSH).defaultBudget(Budget.of(60, 10_000)).build();

            registry.redefineDynamic(renamed, null);

            assertThat(registry.descriptor("custom-sdr").name()).isEqualTo("Renamed");
            assertThat(published).containsExactly(SourceRegistryChangedEvent.redefined("custom-sdr"));
            assertThatThrownBy(() -> registry.redefineDynamic(custom("custom-missing"), null))
                    .isInstanceOf(ai.foresignal.api.error.NotFoundException.class);
        }

        @Test
        @DisplayName("a POLLING source registers dynamically only with a polling adapter (an EDP pull provider, 024)")
        void dynamicPollingNeedsAnAdapter() {
            SourceRegistry registry = registry(List.of());
            SourceDescriptor pulled = SourceDescriptor.builder("ext-pulled").kind("thing").mode(Mode.POLLING)
                    .interval(java.time.Duration.ofSeconds(60), java.time.Duration.ofSeconds(300))
                    .defaultBudget(Budget.of(60, 10_000)).build();

            assertThatThrownBy(() -> registry.registerDynamic(pulled))
                    .isInstanceOf(ai.foresignal.api.error.ValidationException.class);
            registry.registerDynamic(pulled, new ai.foresignal.source.spi.PollingAdapter() {
                @Override
                public SourceDescriptor descriptor() {
                    return pulled;
                }

                @Override
                public ai.foresignal.source.spi.FetchResult fetch(ai.foresignal.source.spi.FetchContext ctx) {
                    return ai.foresignal.source.spi.FetchResult.of(List.of());
                }
            });
            assertThat(registry.get("ext-pulled").adapterIfPresent()).isPresent();
        }

        @Test
        @DisplayName("a duplicate id is a conflict")
        void rejectsDuplicate() {
            SourceRegistry registry = registry(List.of());
            registry.registerDynamic(custom("custom-sdr"));

            assertThatThrownBy(() -> registry.registerDynamic(custom("custom-sdr")))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("a dynamic id cannot shadow a built-in source")
        void rejectsShadowingBuiltIn() {
            SourceRegistry registry = registry(List.of(FakePollingAdapter.withId("built-in")));

            assertThatThrownBy(
                            () ->
                                    registry.registerDynamic(
                                            SourceDescriptor.builder("built-in")
                                                    .kind("custom-thing")
                                                    .mode(Mode.PUSH)
                                                    .defaultBudget(1, 1)
                                                    .build()))
                    .isInstanceOf(ConflictException.class);
        }

        @Test
        @DisplayName("the same validation applies as to a compile-time adapter")
        void appliesTheSameValidation() {
            SourceRegistry registry = registry(List.of());

            assertThatThrownBy(() -> registry.registerDynamic(custom("Bad_Id")))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("kebab-case");
            assertThat(published).isEmpty();
        }

        @Test
        @DisplayName("only PUSH and DERIVED can be dynamic, since nothing else has an implementation")
        void rejectsPollingDynamicSource() {
            SourceRegistry registry = registry(List.of());

            assertThatThrownBy(
                            () ->
                                    registry.registerDynamic(
                                            SourceDescriptor.builder("custom-poll")
                                                    .kind("custom-thing")
                                                    .interval(Duration.ofSeconds(30), Duration.ofMinutes(1))
                                                    .defaultBudget(1, 1)
                                                    .build()))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("only PUSH and DERIVED");
        }

        @Test
        void rejectsNullDescriptor() {
            assertThatThrownBy(() -> registry(List.of()).registerDynamic(null))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("a built-in source cannot be unregistered")
        void cannotUnregisterBuiltIn() {
            SourceRegistry registry = registry(List.of(FakePollingAdapter.withId("built-in")));

            assertThatThrownBy(() -> registry.unregisterDynamic("built-in"))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("built-in source");
        }

        @Test
        void unregisterUnknownIsNotFound() {
            assertThatThrownBy(() -> registry(List.of()).unregisterDynamic("nope"))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Test
    @DisplayName("idsNotIn reports registered sources missing from a given set")
    void reportsIdsNotIn() {
        SourceRegistry registry =
                registry(List.of(FakePollingAdapter.withId("alpha"), FakePollingAdapter.withId("bravo")));

        assertThat(registry.idsNotIn(java.util.Set.of("alpha"))).containsExactly("bravo");
        assertThat(registry.idsNotIn(java.util.Set.of("alpha", "bravo"))).isEmpty();
    }

    @Test
    @DisplayName("a family returning null fails startup rather than registering nothing")
    void rejectsNullFamilyList() {
        assertThatThrownBy(() -> new SourceRegistry(List.of(), List.of(() -> null), events))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("returned null");
    }

    private static List<String> concat(List<String> a, List<String> b) {
        List<String> result = new ArrayList<>(a);
        result.addAll(b);
        return result;
    }
}
