package ai.foresignal.forecast.ensemble;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.forecast.ForecastEntity;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastQuestionRepository;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecasterEntity;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.forecast.ForecastScoreRepository;
import ai.foresignal.forecast.ForecastCalibrationRepository;
import ai.foresignal.forecast.score.ScoringService;
import ai.foresignal.settings.AppSettingService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * External (EDP) forecasters join the ensemble only from 30 resolved linked questions (prompt 138, EXT-3): the count
 * comes from {@link ExternalForecasterCounts}, not from every scored question.
 */
class ExternalEnsembleAdmissionTest {

    private static final Instant NOW = Instant.parse("2026-10-18T06:00:00Z");
    private static final String EDP = "edp:ext-markets/kalshi";

    /** The members of one question with one external forecast, given the linked count. */
    @SuppressWarnings("unchecked")
    private static List<EnsembleService.Member> members(int resolvedLinked) {
        ForecasterRegistry registry = mock(ForecasterRegistry.class);
        ForecasterEntity external = new ForecasterEntity(EDP, "external", "Kalshi", "1.0.0", NOW);
        when(registry.find(EDP)).thenReturn(Optional.of(external));
        ForecastScoreRepository scores = mock(ForecastScoreRepository.class);
        EnsembleService service = new EnsembleService(mock(ForecastService.class), registry,
                mock(ForecastQuestionRepository.class), scores, mock(ForecastCalibrationRepository.class),
                mock(ScoringService.class), mock(AppSettingService.class), Clock.fixed(NOW, ZoneOffset.UTC));
        ExternalForecasterCounts counts = new ExternalForecasterCounts() {
            @Override
            public boolean handles(String forecasterId) {
                return forecasterId.startsWith("edp:");
            }

            @Override
            public int resolvedLinked(String forecasterId) {
                return resolvedLinked;
            }
        };
        ObjectProvider<ExternalForecasterCounts> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(counts);
        ReflectionTestUtils.setField(service, "externalCounts", provider);
        ForecastQuestionEntity q = new ForecastQuestionEntity("01JA8Q3V7S2K9W4T6Y1B5N0M3Q", NOW.minusSeconds(86_400));
        q.setType(Ledger.BINARY);
        q.setOpensAt(NOW.minusSeconds(86_400));
        ForecastEntity f = new ForecastEntity("01JA8Q3V7S2K9W4T6Y1B5N0M3F", q.getId(), EDP, "external",
                NOW.minusSeconds(60), 10L);
        f.setProbability(0.4);
        ForecasterEntity ensemble = new ForecasterEntity(ForecasterRegistry.ENSEMBLE, "ensemble", "Ensemble",
                "1.0.0", NOW);
        ensemble.setConfig(Map.of("externalMinResolved", 30));
        List<EnsembleService.Member> out = service.members(q, Map.of(EDP, f), ensemble, NOW);
        verify(scores, never()).findAll();
        return out;
    }

    @Test
    @DisplayName("29 resolved linked questions keep an edp forecaster out; 30 let it in")
    void threshold() {
        assertThat(members(29)).isEmpty();
        assertThat(members(30)).hasSize(1);
    }
}
