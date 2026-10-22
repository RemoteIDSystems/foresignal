package ai.foresignal.retention;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.CredentialCipher;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.source.IngestPolicyCodec;
import ai.foresignal.source.ParamValidator;
import ai.foresignal.source.SourceHealthLookup;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.SourceView;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.store.SourceCredentialRepository;
import ai.foresignal.store.SourceSettingsEntity;
import ai.foresignal.store.SourceSettingsRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Validation of the downsampling settings (prompt 044): {@code trackRawWindow} against
 * {@code trackWindow}, the bounds of {@code trackDownsampleInterval}, {@code null} disabling, and the
 * per-source {@code downsampleInterval} override (TRACK only, at least 30 s, explicit null clears).
 */
class RetentionSettingsValidationTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    static final SourceDescriptor STANDARD = SourceDescriptor.builder("std").name("std").kind("event")
            .mode(Mode.PUSH).defaultBudget(Budget.of(60, 1000)).build();
    static final SourceDescriptor TRACK = SourceDescriptor.builder("trk").name("trk").kind("aircraft")
            .mode(Mode.PUSH).defaultBudget(Budget.of(60, 1000))
            .historyTier(HistoryTier.TRACK)
            .recordTtl(Duration.ofMinutes(6))
            .significantFields("callsign")
            .defaultIngestPolicy(new IngestPolicy.Sampled(Duration.ofSeconds(60), Duration.ofSeconds(240), 1000.0,
                    true, null))
            .build();

    SourceRegistry registry = mock(SourceRegistry.class);
    RetentionResolver resolver = mock(RetentionResolver.class);

    @BeforeEach
    void setUp() {
        when(registry.descriptors()).thenReturn(List.of(STANDARD, TRACK));
        when(registry.descriptor("std")).thenReturn(STANDARD);
        when(registry.descriptor("trk")).thenReturn(TRACK);
        when(resolver.settings()).thenReturn(RetentionSettings.defaults());
    }

    static JsonNode body(String json) {
        return JSON.readTree(json.replace('\'', '"'));
    }

    @Nested
    @DisplayName("RetentionSettings: trackRawWindow and trackDownsampleInterval")
    class Settings {

        RetentionSettingsService service;

        @BeforeEach
        void create() {
            service = new RetentionSettingsService(mock(AppSettingService.class), resolver, registry,
                    mock(SourceService.class));
        }

        List<String> fields(ValidationException e) {
            return e.getErrors().stream().map(FieldError::field).toList();
        }

        @Test
        @DisplayName("defaults: 24h raw window and 5m interval when both are omitted")
        void defaults() {
            RetentionSettings parsed = service.parse(body("{'default':'7d'}"));
            assertThat(parsed.trackRawWindow()).isEqualTo(Duration.ofHours(24));
            assertThat(parsed.trackDownsampleInterval()).isEqualTo(Duration.ofMinutes(5));
            assertThat(parsed.downsamplingEnabled()).isTrue();
        }

        @Test
        @DisplayName("explicit values are kept")
        void explicit() {
            RetentionSettings parsed = service.parse(
                    body("{'default':'7d','trackWindow':'3d','trackRawWindow':'12h','trackDownsampleInterval':'1m'}"));
            assertThat(parsed.trackRawWindow()).isEqualTo(Duration.ofHours(12));
            assertThat(parsed.trackDownsampleInterval()).isEqualTo(Duration.ofMinutes(1));
        }

        @Test
        @DisplayName("null disables downsampling")
        void nullDisables() {
            RetentionSettings parsed = service.parse(body("{'default':'7d','trackRawWindow':null}"));
            assertThat(parsed.downsamplingEnabled()).isFalse();
            assertThat(parsed.trackRawWindow()).isEqualTo(RetentionSettings.DOWNSAMPLING_DISABLED);
        }

        @Test
        @DisplayName("a raw window at or beyond the track window is rejected with its field path")
        void rawWindowNotShorterThanTrackWindow() {
            assertThatThrownBy(() -> service.parse(body("{'default':'7d','trackWindow':'2d','trackRawWindow':'2d'}")))
                    .isInstanceOfSatisfying(ValidationException.class, e -> {
                        assertThat(e.getCode()).isEqualTo(ErrorCodes.VALIDATION_FAILED);
                        assertThat(fields(e)).containsExactly("trackRawWindow");
                    });
            assertThatThrownBy(() -> service.parse(body("{'default':'7d','trackWindow':'2d','trackRawWindow':'3d'}")))
                    .isInstanceOfSatisfying(ValidationException.class,
                            e -> assertThat(fields(e)).containsExactly("trackRawWindow"));
        }

        @Test
        @DisplayName("the default raw window is checked against a short track window")
        void defaultRawWindowAgainstShortTrackWindow() {
            assertThatThrownBy(() -> service.parse(body("{'default':'7d','trackWindow':'12h'}")))
                    .isInstanceOfSatisfying(ValidationException.class,
                            e -> assertThat(fields(e)).containsExactly("trackRawWindow"));
            assertThat(service.parse(body("{'default':'7d','trackWindow':'12h','trackRawWindow':null}"))
                    .downsamplingEnabled()).isFalse();
        }

        @Test
        @DisplayName("the interval must be at least 30s and at most the raw window")
        void intervalBounds() {
            assertThatThrownBy(() -> service.parse(body("{'default':'7d','trackDownsampleInterval':'29s'}")))
                    .isInstanceOfSatisfying(ValidationException.class,
                            e -> assertThat(fields(e)).containsExactly("trackDownsampleInterval"));
            assertThat(service.parse(body("{'default':'7d','trackDownsampleInterval':'30s'}")).trackDownsampleInterval())
                    .isEqualTo(Duration.ofSeconds(30));
            assertThatThrownBy(() -> service.parse(
                    body("{'default':'7d','trackRawWindow':'1h','trackDownsampleInterval':'61m'}")))
                    .isInstanceOfSatisfying(ValidationException.class,
                            e -> assertThat(fields(e)).containsExactly("trackDownsampleInterval"));
            assertThat(service.parse(body("{'default':'7d','trackRawWindow':'1h','trackDownsampleInterval':'1h'}"))
                    .trackDownsampleInterval()).isEqualTo(Duration.ofHours(1));
            assertThat(service.parse(body("{'default':'7d','trackRawWindow':null,'trackDownsampleInterval':'2d'}"))
                    .trackDownsampleInterval()).as("no upper bound while disabled").isEqualTo(Duration.ofDays(2));
        }

        @Test
        @DisplayName("malformed and zero durations are field errors, listed with the others")
        void malformed() {
            assertThatThrownBy(() -> service.parse(
                    body("{'default':'7d','trackRawWindow':'soon','trackDownsampleInterval':0,'alerts':'5m'}")))
                    .isInstanceOfSatisfying(ValidationException.class, e -> assertThat(fields(e))
                            .contains("trackRawWindow", "trackDownsampleInterval", "alerts"));
            assertThatThrownBy(() -> service.parse(body("{'default':'7d','trackRawWindow':'0s'}")))
                    .isInstanceOfSatisfying(ValidationException.class,
                            e -> assertThat(fields(e)).containsExactly("trackRawWindow"));
        }
    }

    @Nested
    @DisplayName("SourceSettings.downsampleInterval")
    class PerSource {

        SourceSettingsRepository rows = mock(SourceSettingsRepository.class);
        ParamValidator params = mock(ParamValidator.class);
        SourceService service;

        @BeforeEach
        @SuppressWarnings("unchecked")
        void create() {
            when(rows.findById(any())).thenReturn(Optional.empty());
            when(rows.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(params.validate(any(), any())).thenReturn(ValidatedParams.of(Map.of()));
            ObjectProvider<SourceHealthLookup> health = mock(ObjectProvider.class);
            service = new SourceService(registry, rows, mock(SourceCredentialRepository.class), params,
                    new IngestPolicyCodec(JSON), mock(CredentialCipher.class), mock(ApplicationEventPublisher.class),
                    Clock.fixed(Instant.parse("2026-06-01T00:00:00Z"), ZoneOffset.UTC), health, resolver, true);
        }

        @Test
        @DisplayName("a STANDARD source is refused with 400 VALIDATION_FAILED on downsampleInterval")
        void standardRefused() {
            assertThatThrownBy(() -> service.update("std", SourceSettingsUpdate.downsampleInterval(Duration.ofMinutes(1))))
                    .isInstanceOfSatisfying(ValidationException.class, e -> {
                        assertThat(e.getCode()).isEqualTo(ErrorCodes.VALIDATION_FAILED);
                        assertThat(e.getErrors()).extracting(FieldError::field).containsExactly("downsampleInterval");
                    });
            verify(rows, never()).save(any());
        }

        @Test
        @DisplayName("a TRACK source stores it; under 30s or over the raw window is refused")
        void trackStored() {
            service.update("trk", SourceSettingsUpdate.downsampleInterval(Duration.ofMinutes(1)));
            ArgumentCaptor<SourceSettingsEntity> saved = ArgumentCaptor.forClass(SourceSettingsEntity.class);
            verify(rows).save(saved.capture());
            assertThat(saved.getValue().getDownsampleInterval()).isEqualTo(Duration.ofMinutes(1));

            assertThatThrownBy(() -> service.update("trk", SourceSettingsUpdate.downsampleInterval(Duration.ofSeconds(10))))
                    .isInstanceOfSatisfying(ValidationException.class, e -> assertThat(e.getErrors())
                            .extracting(FieldError::field).containsExactly("downsampleInterval"));
            assertThatThrownBy(() -> service.update("trk", SourceSettingsUpdate.downsampleInterval(Duration.ofHours(25))))
                    .isInstanceOfSatisfying(ValidationException.class, e -> assertThat(e.getErrors())
                            .extracting(FieldError::field).containsExactly("downsampleInterval"));
        }

        @Test
        @DisplayName("explicit null clears it, for any source")
        void clearing() {
            SourceSettingsEntity row = new SourceSettingsEntity("trk");
            row.setDownsampleInterval(Duration.ofMinutes(2));
            when(rows.findById("trk")).thenReturn(Optional.of(row));
            assertThat(service.downsampleIntervalOverride("trk")).contains(Duration.ofMinutes(2));
            assertThat(service.effectiveDownsampleInterval("trk")).isEqualTo(Duration.ofMinutes(2));

            SourceView view = service.update("trk", SourceSettingsUpdate.clearingDownsampleInterval());
            assertThat(view.id()).isEqualTo("trk");
            assertThat(row.getDownsampleInterval()).isNull();
            assertThat(service.effectiveDownsampleInterval("trk")).isEqualTo(Duration.ofMinutes(5));

            service.update("std", SourceSettingsUpdate.clearingDownsampleInterval());
            assertThat(service.downsampleIntervalOverride("std")).isEmpty();
        }
    }
}
