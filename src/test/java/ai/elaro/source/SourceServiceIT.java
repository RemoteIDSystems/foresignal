package ai.elaro.source;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.elaro.api.error.ConflictException;
import ai.elaro.api.error.ElaroException;
import ai.elaro.api.error.ValidationException;
import ai.elaro.source.spi.Budget;
import ai.elaro.source.spi.IngestPolicy;
import ai.elaro.store.SourceCredentialRepository;
import ai.elaro.store.SourceSettingsEntity;
import ai.elaro.store.SourceSettingsRepository;
import ai.elaro.test.IntegrationTest;
import ai.elaro.testsupport.adapters.FakeAdapters;
import ai.elaro.testsupport.adapters.FakeFamily;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;

@IntegrationTest
@Import({FakeAdapters.class, SourceServiceIT.EventRecorderConfig.class})
class SourceServiceIT {

    private static final String POLLING = FakeAdapters.POLLING_ID;
    private static final String STREAMING = FakeAdapters.STREAMING_ID;

    @Autowired private SourceService service;
    @Autowired private SourceRegistry registry;
    @Autowired private SourceSettingsRepository settings;
    @Autowired private SourceCredentialRepository credentials;
    @Autowired private CredentialsAccess credentialsAccess;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private EventRecorder recorder;

    @BeforeEach
    void seed() {
        // Tables are truncated before each test, so re-create the default rows.
        service.seedDefaults();
        recorder.clear();
    }

    // ---------- startup ----------

    @Nested
    @DisplayName("startup")
    class Startup {

        @Test
        @DisplayName("every registered source gets a disabled default row")
        void seedsDefaults() {
            assertThat(settings.count()).isEqualTo(registry.ids().size());

            SourceSettingsEntity row = settings.findById(POLLING).orElseThrow();
            assertThat(row.isEnabled()).isFalse();
            assertThat(row.getParams()).isEmpty();
            assertThat(row.getRetention()).isNull();
            assertThat(row.getIngestPolicy()).isNull();
            assertThat(row.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("defaults come from the descriptor")
        void defaultsMatchDescriptor() {
            var descriptor = registry.descriptor(POLLING);
            SourceSettingsEntity row = settings.findById(POLLING).orElseThrow();

            assertThat(row.getIntervalSeconds()).isEqualTo((int) descriptor.defaultInterval().toSeconds());
            assertThat(row.getBudgetPerMinute()).isEqualTo(descriptor.defaultBudget().perMinute());
            assertThat(row.getBudgetPerDay()).isEqualTo(descriptor.defaultBudget().perDay());
        }

        @Test
        @DisplayName("nothing is enabled by installing Elaro")
        void nothingIsEnabledByDefault() {
            assertThat(settings.findByEnabledTrue()).isEmpty();
        }

        @Test
        @DisplayName("seeding twice creates nothing new")
        void seedingIsIdempotent() {
            long before = settings.count();

            service.seedDefaults();

            assertThat(settings.count()).isEqualTo(before);
        }

        @Test
        @DisplayName("an orphaned row is kept, not deleted, so a removed adapter loses no config")
        void keepsOrphanedRows() {
            jdbc.update(
                    """
                    insert into source_settings
                        (source_id, enabled, interval_seconds, params, budget_per_minute,
                         budget_per_day, updated_at, lock_version)
                    values ('gone-away', false, 60, '{}'::jsonb, 10, 100, now(), 0)
                    """);

            service.seedDefaults();

            assertThat(settings.findById("gone-away")).isPresent();
            assertThat(registry.contains("gone-away")).isFalse();
        }

        @Test
        @DisplayName("every feed of a family gets its own row")
        void seedsFamilyFeeds() {
            assertThat(settings.findAllById(FakeFamily.FEED_IDS)).hasSize(FakeFamily.FEED_IDS.size());
        }
    }

    // ---------- views ----------

    @Nested
    @DisplayName("views")
    class Views {

        @Test
        void listCoversEveryRegisteredSource() {
            assertThat(service.list())
                    .extracting(SourceView::id)
                    .containsExactlyElementsOf(registry.ids());
        }

        @Test
        void viewCarriesDescriptorAndSettings() {
            SourceView view = service.view(POLLING);

            assertThat(view.descriptor().id()).isEqualTo(POLLING);
            assertThat(view.enabled()).isFalse();
            assertThat(view.credentialsSet()).isFalse();
            assertThat(view.interval()).isEqualTo(registry.descriptor(POLLING).defaultInterval());
            assertThat(view.ingestPolicy()).isNotNull();
        }

        @Test
        @DisplayName("an unknown source is a 404")
        void unknownSourceIsNotFound() {
            assertThatThrownBy(() -> service.view("nope"))
                    .isInstanceOf(ai.elaro.api.error.NotFoundException.class);
        }
    }

    // ---------- partial update ----------

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("omitted fields are left unchanged")
        void partialUpdateLeavesOtherFieldsAlone() {
            service.update(POLLING, SourceSettingsUpdate.interval(Duration.ofMinutes(5)));
            Budget budgetBefore = service.view(POLLING).budget();

            service.update(POLLING, SourceSettingsUpdate.enabled(true));

            SourceView after = service.view(POLLING);
            assertThat(after.enabled()).isTrue();
            assertThat(after.interval()).isEqualTo(Duration.ofMinutes(5));
            assertThat(after.budget()).isEqualTo(budgetBefore);
        }

        @Test
        @DisplayName("an empty update changes nothing")
        void emptyUpdateIsANoOp() {
            SourceView before = service.view(POLLING);

            SourceView after = service.update(POLLING, SourceSettingsUpdate.empty());

            assertThat(after.enabled()).isEqualTo(before.enabled());
            assertThat(after.interval()).isEqualTo(before.interval());
        }

        @Test
        @DisplayName("an interval below the descriptor's floor is rejected")
        void rejectsIntervalBelowMinimum() {
            // The fake polling source has minInterval 10s.
            assertThatThrownBy(
                            () -> service.update(POLLING, SourceSettingsUpdate.interval(Duration.ofSeconds(5))))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(
                            e ->
                                    assertThat(((ValidationException) e).getErrors())
                                            .anySatisfy(
                                                    error -> {
                                                        assertThat(error.field()).isEqualTo("interval");
                                                        assertThat(error.message()).contains("must be at least 10s");
                                                    }));
        }

        @Test
        void rejectsZeroInterval() {
            assertThatThrownBy(() -> service.update(POLLING, SourceSettingsUpdate.interval(Duration.ZERO)))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        void rejectsBudgetBelowOne() {
            var update =
                    new SourceSettingsUpdate(
                            null, null, null, new Budget(0, 0), null, false, null, false);

            assertThatThrownBy(() -> service.update(POLLING, update))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(
                            e ->
                                    assertThat(((ValidationException) e).getErrors())
                                            .extracting(ai.elaro.api.error.FieldError::field)
                                            .contains("budget.perMinute", "budget.perDay"));
        }

        @Test
        void acceptsValidBudget() {
            service.update(
                    POLLING,
                    new SourceSettingsUpdate(null, null, null, new Budget(5, 500), null, false, null, false));

            assertThat(service.view(POLLING).budget()).isEqualTo(new Budget(5, 500));
            assertThat(service.effectiveBudget(POLLING)).isEqualTo(new Budget(5, 500));
        }

        @Test
        @DisplayName("an unknown parameter is rejected with its field path")
        void rejectsUnknownParams() {
            assertThatThrownBy(
                            () -> service.update(POLLING, SourceSettingsUpdate.params(Map.of("nope", "x"))))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(
                            e ->
                                    assertThat(((ValidationException) e).getErrors())
                                            .extracting(ai.elaro.api.error.FieldError::field)
                                            .contains("params.nope"));
        }

        @Test
        @DisplayName("every problem in one update is reported together")
        void aggregatesErrors() {
            var update =
                    new SourceSettingsUpdate(
                            null,
                            Duration.ofSeconds(1),
                            Map.of("unknown", "x"),
                            new Budget(0, 5),
                            null,
                            false,
                            null,
                            false);

            assertThatThrownBy(() -> service.update(POLLING, update))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(
                            e ->
                                    assertThat(((ValidationException) e).getErrors())
                                            .hasSizeGreaterThanOrEqualTo(3));
        }

        @Test
        @DisplayName("a rejected update leaves nothing changed")
        void rejectedUpdateIsAtomic() {
            SourceView before = service.view(POLLING);

            assertThatThrownBy(
                            () ->
                                    service.update(
                                            POLLING,
                                            new SourceSettingsUpdate(
                                                    true,
                                                    Duration.ofSeconds(1),
                                                    null,
                                                    null,
                                                    null,
                                                    false,
                                                    null,
                                                    false)))
                    .isInstanceOf(ValidationException.class);

            assertThat(service.view(POLLING).enabled()).isEqualTo(before.enabled());
            assertThat(service.view(POLLING).interval()).isEqualTo(before.interval());
        }
    }

    // ---------- retention ----------

    @Nested
    @DisplayName("retention")
    class Retention {

        @Test
        void storesAndClearsRetention() {
            service.update(POLLING, SourceSettingsUpdate.empty().withRetention(Duration.ofDays(30)));
            assertThat(service.effectiveRetention(POLLING)).contains(Duration.ofDays(30));

            service.update(POLLING, SourceSettingsUpdate.clearingRetention());

            assertThat(service.effectiveRetention(POLLING)).isEmpty();
            assertThat(settings.findById(POLLING).orElseThrow().getRetention()).isNull();
        }

        @Test
        @DisplayName("retention is stored in shorthand")
        void storesShorthand() {
            service.update(POLLING, SourceSettingsUpdate.empty().withRetention(Duration.ofDays(30)));

            assertThat(settings.findById(POLLING).orElseThrow().getRetention()).isEqualTo("30d");
        }

        @Test
        void rejectsNonPositiveRetention() {
            assertThatThrownBy(
                            () -> service.update(POLLING, SourceSettingsUpdate.empty().withRetention(Duration.ZERO)))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("a TRACK source refuses a retention override with RETENTION_TIER_FIXED")
        void trackRetentionIsFixed() {
            // The fake streaming source is STANDARD; use the TRACK one registered below.
            assertThatThrownBy(
                            () ->
                                    service.update(
                                            TrackAdapterConfig.TRACK_ID,
                                            SourceSettingsUpdate.empty().withRetention(Duration.ofDays(7))))
                    .isInstanceOf(ElaroException.class)
                    .satisfies(
                            e -> {
                                ElaroException elaro = (ElaroException) e;
                                assertThat(elaro.getCode()).isEqualTo(SourceService.RETENTION_TIER_FIXED);
                                assertThat(elaro.getStatus().value()).isEqualTo(400);
                            })
                    .hasMessageContaining("fixed by its TRACK history tier");
        }

        @Test
        @DisplayName("a TRACK source never reports an effective retention")
        void trackHasNoEffectiveRetention() {
            assertThat(service.effectiveRetention(TrackAdapterConfig.TRACK_ID)).isEmpty();
            assertThat(service.view(TrackAdapterConfig.TRACK_ID).retention()).isNull();
        }
    }

    // ---------- ingest policy ----------

    @Nested
    @DisplayName("ingest policy")
    class Policies {

        @Test
        @DisplayName("the descriptor default applies until an override is set")
        void defaultsToDescriptorPolicy() {
            assertThat(service.effectiveIngestPolicy(POLLING))
                    .isInstanceOf(IngestPolicy.All.class);
        }

        @Test
        @DisplayName("an override round-trips through the jsonb column")
        void overrideRoundTrips() {
            service.update(
                    TrackAdapterConfig.TRACK_ID,
                    SourceSettingsUpdate.ingestPolicy(
                            IngestPolicy.sampled(
                                    Duration.ofSeconds(30), Duration.ofSeconds(120), 500.0, true)));

            IngestPolicy policy = service.effectiveIngestPolicy(TrackAdapterConfig.TRACK_ID);

            assertThat(policy).isInstanceOf(IngestPolicy.Sampled.class);
            assertThat(policy.minInterval()).isEqualTo(Duration.ofSeconds(30));
            assertThat(policy.keyframe()).isEqualTo(Duration.ofSeconds(120));
            assertThat(((IngestPolicy.Sampled) policy).minDeviationMeters()).isEqualTo(500.0);
            // edgeMinInterval defaults to half of minInterval.
            assertThat(policy.edgeMinInterval()).isEqualTo(Duration.ofSeconds(15));
        }

        @Test
        @DisplayName("the stored value is real jsonb with a mode discriminator")
        void storedAsJsonb() {
            service.update(
                    TrackAdapterConfig.TRACK_ID,
                    SourceSettingsUpdate.ingestPolicy(IngestPolicy.latest(Duration.ofSeconds(120))));

            assertThat(
                            jdbc.queryForObject(
                                    "select ingest_policy->>'mode' from source_settings where source_id = ?",
                                    String.class,
                                    TrackAdapterConfig.TRACK_ID))
                    .isEqualTo("LATEST");
        }

        @Test
        @DisplayName("an explicit clear reverts to the descriptor default")
        void clearRevertsToDefault() {
            service.update(
                    TrackAdapterConfig.TRACK_ID,
                    SourceSettingsUpdate.ingestPolicy(IngestPolicy.latest(Duration.ofSeconds(120))));

            service.update(TrackAdapterConfig.TRACK_ID, SourceSettingsUpdate.clearingIngestPolicy());

            assertThat(settings.findById(TrackAdapterConfig.TRACK_ID).orElseThrow().getIngestPolicy())
                    .isNull();
            assertThat(service.effectiveIngestPolicy(TrackAdapterConfig.TRACK_ID))
                    .isInstanceOf(IngestPolicy.Sampled.class);
        }

        @Test
        @DisplayName("an invalid policy is rejected under the ingestPolicy field")
        void rejectsInvalidPolicy() {
            // keyframe above ttl - pollInterval - 30s for the TRACK source.
            assertThatThrownBy(
                            () ->
                                    service.update(
                                            TrackAdapterConfig.TRACK_ID,
                                            SourceSettingsUpdate.ingestPolicy(
                                                    IngestPolicy.sampled(
                                                            Duration.ofSeconds(30),
                                                            Duration.ofHours(1),
                                                            500.0,
                                                            true))))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(
                            e ->
                                    assertThat(((ValidationException) e).getErrors())
                                            .anySatisfy(
                                                    error ->
                                                            assertThat(error.field()).isEqualTo("ingestPolicy")));
        }
    }

    // ---------- credentials ----------

    @Nested
    @DisplayName("credentials")
    class Credentials {

        private static final String NEEDS_CREDENTIALS = CredentialAdapterConfig.CREDENTIALLED_ID;

        @Test
        @DisplayName("enabling without the required credential is a 409 naming the field")
        void enablingWithoutCredentialsConflicts() {
            assertThatThrownBy(
                            () -> service.update(NEEDS_CREDENTIALS, SourceSettingsUpdate.enabled(true)))
                    .isInstanceOf(ConflictException.class)
                    .satisfies(
                            e ->
                                    assertThat(((ConflictException) e).getCode())
                                            .isEqualTo(ConflictException.CREDENTIALS_REQUIRED))
                    .hasMessageContaining("apiKey");
        }

        @Test
        @DisplayName("enabling succeeds once credentials are stored")
        void enablingSucceedsWithCredentials() {
            service.setCredentials(NEEDS_CREDENTIALS, Map.of("apiKey", "secret-value"));

            service.update(NEEDS_CREDENTIALS, SourceSettingsUpdate.enabled(true));

            assertThat(service.view(NEEDS_CREDENTIALS).enabled()).isTrue();
            assertThat(service.view(NEEDS_CREDENTIALS).credentialsSet()).isTrue();
        }

        @Test
        @DisplayName("a source needing no credentials enables freely")
        void noCredentialsNeeded() {
            service.update(POLLING, SourceSettingsUpdate.enabled(true));

            assertThat(service.view(POLLING).enabled()).isTrue();
        }

        @Test
        @DisplayName("the stored bytes do not contain the plaintext")
        void credentialsAreEncryptedAtRest() {
            service.setCredentials(NEEDS_CREDENTIALS, Map.of("apiKey", "recognisable-secret"));

            byte[] ciphertext =
                    jdbc.queryForObject(
                            "select ciphertext from source_credential where source_id = ?",
                            byte[].class,
                            NEEDS_CREDENTIALS);

            assertThat(new String(ciphertext, StandardCharsets.ISO_8859_1))
                    .doesNotContain("recognisable-secret")
                    .doesNotContain("apiKey");
        }

        @Test
        @DisplayName("credentials decrypt back through the internal accessor only")
        void credentialsDecryptThroughAccessor() {
            service.setCredentials(NEEDS_CREDENTIALS, Map.of("apiKey", "secret-value"));

            assertThat(credentialsAccess.forSource(NEEDS_CREDENTIALS))
                    .containsExactly(Map.entry("apiKey", "secret-value"));
            assertThat(credentialsAccess.isSet(NEEDS_CREDENTIALS)).isTrue();
        }

        @Test
        @DisplayName("no public view carries a credential value")
        void viewsNeverCarryValues() {
            service.setCredentials(NEEDS_CREDENTIALS, Map.of("apiKey", "secret-value"));

            SourceView view = service.view(NEEDS_CREDENTIALS);

            assertThat(view.credentialsSet()).isTrue();
            assertThat(view.toString()).doesNotContain("secret-value");
            assertThat(service.list().toString()).doesNotContain("secret-value");
        }

        @Test
        void rejectsUndeclaredKeys() {
            assertThatThrownBy(
                            () -> service.setCredentials(NEEDS_CREDENTIALS, Map.of("apiKey", "a", "nope", "b")))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(
                            e ->
                                    assertThat(((ValidationException) e).getErrors())
                                            .extracting(ai.elaro.api.error.FieldError::field)
                                            .contains("values.nope"));
        }

        @Test
        void rejectsMissingRequiredField() {
            assertThatThrownBy(() -> service.setCredentials(NEEDS_CREDENTIALS, Map.of()))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(
                            e ->
                                    assertThat(((ValidationException) e).getErrors())
                                            .anySatisfy(
                                                    error -> {
                                                        assertThat(error.field()).isEqualTo("values.apiKey");
                                                        assertThat(error.message()).isEqualTo("is required");
                                                    }));
        }

        @Test
        void rejectsBlankValues() {
            assertThatThrownBy(() -> service.setCredentials(NEEDS_CREDENTIALS, Map.of("apiKey", "   ")))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(
                            e ->
                                    assertThat(((ValidationException) e).getErrors())
                                            .anySatisfy(
                                                    error -> assertThat(error.message()).contains("must not be blank")));
        }

        @Test
        @DisplayName("a source that takes no credentials refuses them")
        void refusesCredentialsForSourceThatTakesNone() {
            assertThatThrownBy(() -> service.setCredentials(POLLING, Map.of("apiKey", "x")))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("does not take any credentials");
        }

        @Test
        @DisplayName("setting credentials again replaces them")
        void replacesCredentials() {
            service.setCredentials(NEEDS_CREDENTIALS, Map.of("apiKey", "first"));
            service.setCredentials(NEEDS_CREDENTIALS, Map.of("apiKey", "second"));

            assertThat(credentialsAccess.forSource(NEEDS_CREDENTIALS)).containsEntry("apiKey", "second");
            assertThat(credentials.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("deleting credentials disables a source that requires them")
        void deletingCredentialsDisablesTheSource() {
            service.setCredentials(NEEDS_CREDENTIALS, Map.of("apiKey", "secret"));
            service.update(NEEDS_CREDENTIALS, SourceSettingsUpdate.enabled(true));

            service.deleteCredentials(NEEDS_CREDENTIALS);

            assertThat(service.view(NEEDS_CREDENTIALS).enabled()).isFalse();
            assertThat(service.view(NEEDS_CREDENTIALS).credentialsSet()).isFalse();
        }

        @Test
        @DisplayName("deleting credentials does not disable a source that does not need them")
        void deletingOptionalCredentialsLeavesSourceEnabled() {
            service.update(POLLING, SourceSettingsUpdate.enabled(true));

            service.deleteCredentials(POLLING);

            assertThat(service.view(POLLING).enabled()).isTrue();
        }

        @Test
        void credentialsSetReportsPresence() {
            assertThat(service.credentialsSet(NEEDS_CREDENTIALS)).isFalse();
            service.setCredentials(NEEDS_CREDENTIALS, Map.of("apiKey", "x"));
            assertThat(service.credentialsSet(NEEDS_CREDENTIALS)).isTrue();
        }
    }

    // ---------- events ----------

    @Nested
    @DisplayName("events")
    class Events {

        @Test
        @DisplayName("a settings change publishes after commit, with before and after")
        void publishesSettingsChange() {
            service.update(POLLING, SourceSettingsUpdate.enabled(true));

            assertThat(recorder.settingsEvents()).hasSize(1);
            SourceSettingsChangedEvent event = recorder.settingsEvents().get(0);
            assertThat(event.sourceId()).isEqualTo(POLLING);
            assertThat(event.before().enabled()).isFalse();
            assertThat(event.after().enabled()).isTrue();
            assertThat(event.enabledChanged()).isTrue();
            assertThat(event.affectsScheduling()).isTrue();
        }

        @Test
        @DisplayName("a budget-only change does not affect scheduling")
        void budgetChangeDoesNotAffectScheduling() {
            service.update(
                    POLLING,
                    new SourceSettingsUpdate(null, null, null, new Budget(5, 50), null, false, null, false));

            assertThat(recorder.settingsEvents()).hasSize(1);
            assertThat(recorder.settingsEvents().get(0).affectsScheduling()).isFalse();
        }

        @Test
        @DisplayName("a failed update publishes nothing")
        void noEventOnFailure() {
            assertThatThrownBy(() -> service.update(POLLING, SourceSettingsUpdate.interval(Duration.ofSeconds(1))))
                    .isInstanceOf(ValidationException.class);

            assertThat(recorder.settingsEvents()).isEmpty();
        }

        @Test
        void publishesCredentialEvents() {
            service.setCredentials(CredentialAdapterConfig.CREDENTIALLED_ID, Map.of("apiKey", "x"));
            service.deleteCredentials(CredentialAdapterConfig.CREDENTIALLED_ID);

            assertThat(recorder.credentialEvents())
                    .extracting(SourceCredentialsChangedEvent::present)
                    .containsExactly(true, false);
        }

        @Test
        @DisplayName("a rejected credential write publishes nothing")
        void noCredentialEventOnFailure() {
            assertThatThrownBy(
                            () ->
                                    service.setCredentials(
                                            CredentialAdapterConfig.CREDENTIALLED_ID, Map.of()))
                    .isInstanceOf(ValidationException.class);

            assertThat(recorder.credentialEvents()).isEmpty();
        }
    }

    // ---------- effective settings ----------

    @Test
    @DisplayName("effectiveParams applies descriptor defaults")
    void effectiveParamsAppliesDefaults() {
        assertThat(service.effectiveParams(ParamAdapterConfig.PARAMETERISED_ID).string("feed"))
                .isEqualTo("all");

        service.update(
                ParamAdapterConfig.PARAMETERISED_ID, SourceSettingsUpdate.params(Map.of("feed", "mil")));

        assertThat(service.effectiveParams(ParamAdapterConfig.PARAMETERISED_ID).string("feed"))
                .isEqualTo("mil");
    }

    @Test
    @DisplayName("a streaming source has no poll interval to validate against")
    void streamingSourceHasNoMinInterval() {
        service.update(STREAMING, SourceSettingsUpdate.interval(Duration.ofSeconds(1)));

        assertThat(service.view(STREAMING).interval()).isEqualTo(Duration.ofSeconds(1));
    }

    // ---------- extra sources these tests need ----------

    /** A TRACK source, for the tier-fixed retention and sampling policy cases. */
    @TestConfiguration
    static class TrackAdapterConfig {

        static final String TRACK_ID = "fake-track";

        @Bean
        ai.elaro.testsupport.adapters.FakePollingAdapter fakeTrackAdapter() {
            return new ai.elaro.testsupport.adapters.FakePollingAdapter(
                    ai.elaro.testsupport.adapters.FakePollingAdapter.defaultDescriptor(TRACK_ID)
                            .kind("aircraft")
                            .historyTier(ai.elaro.source.spi.HistoryTier.TRACK)
                            .recordTtl(Duration.ofMinutes(5))
                            .significantFields("flight", "squawk")
                            .defaultIngestPolicy(
                                    IngestPolicy.sampled(
                                            Duration.ofSeconds(60), Duration.ofSeconds(180), 1000.0, true))
                            .build());
        }
    }

    /** A source with a required credential. */
    @TestConfiguration
    static class CredentialAdapterConfig {

        static final String CREDENTIALLED_ID = "fake-credentialled";

        @Bean
        ai.elaro.testsupport.adapters.FakePollingAdapter fakeCredentialledAdapter() {
            return new ai.elaro.testsupport.adapters.FakePollingAdapter(
                    ai.elaro.testsupport.adapters.FakePollingAdapter.defaultDescriptor(CREDENTIALLED_ID)
                            .credential(
                                    ai.elaro.source.spi.CredentialField.required("apiKey", "The upstream key"))
                            .build());
        }
    }

    /** A source with a defaulted parameter. */
    @TestConfiguration
    static class ParamAdapterConfig {

        static final String PARAMETERISED_ID = "fake-parameterised";

        @Bean
        ai.elaro.testsupport.adapters.FakePollingAdapter fakeParameterisedAdapter() {
            return new ai.elaro.testsupport.adapters.FakePollingAdapter(
                    ai.elaro.testsupport.adapters.FakePollingAdapter.defaultDescriptor(PARAMETERISED_ID)
                            .param(
                                    ai.elaro.source.spi.ParamSpec.optional(
                                            "feed", ai.elaro.source.spi.ParamType.STRING, "which feed", "all"))
                            .build());
        }
    }

    /** Captures the after-commit events. */
    static class EventRecorder {

        private final List<SourceSettingsChangedEvent> settingsEvents = new ArrayList<>();
        private final List<SourceCredentialsChangedEvent> credentialEvents = new ArrayList<>();

        @EventListener
        synchronized void onSettings(SourceSettingsChangedEvent event) {
            settingsEvents.add(event);
        }

        @EventListener
        synchronized void onCredentials(SourceCredentialsChangedEvent event) {
            credentialEvents.add(event);
        }

        synchronized List<SourceSettingsChangedEvent> settingsEvents() {
            return List.copyOf(settingsEvents);
        }

        synchronized List<SourceCredentialsChangedEvent> credentialEvents() {
            return List.copyOf(credentialEvents);
        }

        synchronized void clear() {
            settingsEvents.clear();
            credentialEvents.clear();
        }
    }

    @TestConfiguration
    @Import({TrackAdapterConfig.class, CredentialAdapterConfig.class, ParamAdapterConfig.class})
    static class EventRecorderConfig {

        @Bean
        EventRecorder eventRecorder() {
            return new EventRecorder();
        }
    }
}
