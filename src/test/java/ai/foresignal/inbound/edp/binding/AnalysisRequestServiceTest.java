package ai.foresignal.inbound.edp.binding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.forecast.ForecastEntity;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastRepository;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecasterEntity;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.inbound.edp.EdpHttpClient;
import ai.foresignal.inbound.edp.EdpManifestModel;
import ai.foresignal.inbound.edp.EdpManifestService;
import ai.foresignal.inbound.edp.ExternalSourceEntity;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.ServerSettingsService;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import tools.jackson.databind.json.JsonMapper;

/**
 * The on-demand analysis-request round-trip (prompt 155): a request persists a correlation and posts to the
 * provider, a synchronous answer records at once, and a callback records the forecast against the right question
 * via the correlation id.
 */
class AnalysisRequestServiceTest {

    private static final String FORECASTER = "edp:ext-timesfm/transits";
    private static final String QUESTION = "01HQWERTYQUESTION000000001";

    private final Clock clock = Clock.fixed(Instant.parse("2026-10-22T12:00:00Z"), ZoneOffset.UTC);
    private final EdpManifestService manifests = mock(EdpManifestService.class);
    private final EdpHttpClient http = mock(EdpHttpClient.class);
    private final AnalysisCorrelationRepository correlations = mock(AnalysisCorrelationRepository.class);
    private final ForecastService ledger = mock(ForecastService.class);
    private final ForecasterRegistry forecasters = mock(ForecasterRegistry.class);
    private final ForecastRepository forecasts = mock(ForecastRepository.class);
    private final ExternalQuestionLinkRepository links = mock(ExternalQuestionLinkRepository.class);
    private final JsonMapper json = JsonMapper.builder().build();

    @SuppressWarnings("unchecked")
    private final ObjectProvider<ServerSettingsService> server = mock(ObjectProvider.class);

    private final AnalysisRequestService service = new AnalysisRequestService(manifests, http, correlations, ledger,
            forecasters, forecasts, links, json, clock, server);

    private ForecastQuestionEntity binaryQuestion() {
        ForecastQuestionEntity q = mock(ForecastQuestionEntity.class);
        when(q.getId()).thenReturn(QUESTION);
        when(q.getType()).thenReturn(Ledger.BINARY);
        when(q.acceptsForecasts(any())).thenReturn(true);
        return q;
    }

    private EdpHttpClient.EdpHttpResponse responseOf(String body) {
        return new EdpHttpClient.EdpHttpResponse(200, URI.create("https://timesfm.example.org/analyze"), Map.of(),
                body.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("a request persists the correlation and records a synchronous provider's forecast at once")
    void requestRecordsSynchronousAnswer() throws Exception {
        ExternalSourceEntity ext = mock(ExternalSourceEntity.class);
        when(ext.isAllowPrivateNetwork()).thenReturn(false);
        when(manifests.require("ext-timesfm")).thenReturn(ext);
        EdpManifestModel model = mock(EdpManifestModel.class);
        when(model.analysisRequest())
                .thenReturn(new EdpManifestModel.AnalysisRequest("https://timesfm.example.org/analyze", "none", null));
        when(manifests.model(ext)).thenReturn(model);

        ForecastQuestionEntity q = binaryQuestion();
        when(ledger.find(QUESTION)).thenReturn(Optional.of(q));

        AtomicReference<AnalysisCorrelationEntity> saved = new AtomicReference<>();
        when(correlations.save(any())).thenAnswer(inv -> {
            saved.set(inv.getArgument(0));
            return inv.getArgument(0);
        });
        when(correlations.findById(anyString())).thenAnswer(inv -> Optional.ofNullable(saved.get()));
        when(http.post(any(), any(), any(), anyBoolean(), anyLong())).thenReturn(responseOf("{\"probability\":0.72}"));
        ServerSettingsService settings = mock(ServerSettingsService.class);
        ServerSettings serverSettings = mock(ServerSettings.class);
        when(serverSettings.normalizedPublicBaseUrl()).thenReturn("https://foresignal.example");
        when(settings.get()).thenReturn(serverSettings);
        when(server.getIfAvailable()).thenReturn(settings);
        when(forecasters.ensure(anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(mock(ForecasterEntity.class));
        when(ledger.record(any(), any(), any(), any(), any(), any(), any(), any(), anyBoolean()))
                .thenReturn(mock(ForecastEntity.class));

        AnalysisRequestService.Requested requested = service.request(FORECASTER, QUESTION, "same", "P7D", "operator");

        assertThat(requested.recorded()).isTrue();
        assertThat(saved.get()).isNotNull();
        assertThat(saved.get().getQuestionId()).isEqualTo(QUESTION);
        assertThat(saved.get().getForecasterId()).isEqualTo(FORECASTER);
        ArgumentCaptor<ForecastQuestionEntity> recorded = ArgumentCaptor.forClass(ForecastQuestionEntity.class);
        verify(ledger).record(recorded.capture(), any(), any(), any(), any(), any(), any(), any(), anyBoolean());
        assertThat(recorded.getValue().getId()).isEqualTo(QUESTION);
    }

    @Test
    @DisplayName("a callback records the forecast against the question the correlation names")
    void callbackRecordsAgainstCorrelatedQuestion() {
        AnalysisCorrelationEntity correlation = new AnalysisCorrelationEntity("01HQCORRELATION00000000001",
                "ext-timesfm", "transits", QUESTION, BindingVocabulary.SAME, "P7D", "operator", clock.instant());
        when(correlations.findById("01HQCORRELATION00000000001")).thenReturn(Optional.of(correlation));
        when(correlations.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ForecastQuestionEntity q = binaryQuestion();
        when(ledger.find(QUESTION)).thenReturn(Optional.of(q));
        when(forecasters.ensure(anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(mock(ForecasterEntity.class));
        ForecastEntity forecast = mock(ForecastEntity.class);
        when(forecast.getId()).thenReturn("forecast-1");
        when(ledger.record(any(), any(), any(), any(), any(), any(), any(), any(), anyBoolean())).thenReturn(forecast);

        Optional<ForecastEntity> recorded = service.fulfill("01HQCORRELATION00000000001", 0.61, null);

        assertThat(recorded).containsSame(forecast);
        assertThat(correlation.getStatus()).isEqualTo(AnalysisCorrelationEntity.FULFILLED);
        ArgumentCaptor<ForecastQuestionEntity> captor = ArgumentCaptor.forClass(ForecastQuestionEntity.class);
        verify(ledger).record(captor.capture(), any(), any(), any(), any(), any(), any(), any(), anyBoolean());
        assertThat(captor.getValue().getId()).isEqualTo(QUESTION);
    }

    @Test
    @DisplayName("an on-demand callback is deduped against the standing forecast like a pushed one (159)")
    void duplicateCallbackIsDeduped() {
        AnalysisCorrelationEntity correlation = new AnalysisCorrelationEntity("01HQCORRELATION00000000002",
                "ext-timesfm", "transits", QUESTION, BindingVocabulary.SAME, "P7D", "operator", clock.instant());
        when(correlations.findById("01HQCORRELATION00000000002")).thenReturn(Optional.of(correlation));
        when(correlations.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ForecastQuestionEntity q = binaryQuestion();
        when(ledger.find(QUESTION)).thenReturn(Optional.of(q));
        when(forecasters.ensure(anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(mock(ForecasterEntity.class));

        // A standing forecast made moments ago at the same probability: no material change within a day.
        ForecastEntity standing = mock(ForecastEntity.class);
        when(standing.getProbability()).thenReturn(0.61);
        when(standing.getQuantiles()).thenReturn(null);
        when(standing.getMadeAt()).thenReturn(clock.instant());
        when(forecasts.findTop1ByQuestionIdAndForecasterIdOrderByMadeAtDescIdDesc(anyString(), anyString()))
                .thenReturn(Optional.of(standing));

        Optional<ForecastEntity> recorded = service.fulfill("01HQCORRELATION00000000002", 0.61, null);

        assertThat(recorded).isEmpty();
        verify(ledger, org.mockito.Mockito.never())
                .record(any(), any(), any(), any(), any(), any(), any(), any(), anyBoolean());
        assertThat(correlation.getStatus()).isEqualTo(AnalysisCorrelationEntity.FULFILLED);
    }

    @Test
    @DisplayName("a recorded callback stamps lastForecastAt on the question link (159)")
    void recordedCallbackStampsTheQuestionLink() {
        AnalysisCorrelationEntity correlation = new AnalysisCorrelationEntity("01HQCORRELATION00000000003",
                "ext-timesfm", "transits", QUESTION, BindingVocabulary.SAME, "P7D", "operator", clock.instant());
        when(correlations.findById("01HQCORRELATION00000000003")).thenReturn(Optional.of(correlation));
        when(correlations.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ForecastQuestionEntity q = binaryQuestion();
        when(ledger.find(QUESTION)).thenReturn(Optional.of(q));
        when(forecasters.ensure(anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(mock(ForecasterEntity.class));
        ForecastEntity forecast = mock(ForecastEntity.class);
        Instant madeAt = clock.instant();
        when(forecast.getMadeAt()).thenReturn(madeAt);
        when(ledger.record(any(), any(), any(), any(), any(), any(), any(), any(), anyBoolean())).thenReturn(forecast);
        ExternalQuestionLinkEntity link = mock(ExternalQuestionLinkEntity.class);
        when(links.findBySourceIdAndStreamIdAndQuestionId("ext-timesfm", "transits", QUESTION))
                .thenReturn(java.util.List.of(link));

        service.fulfill("01HQCORRELATION00000000003", 0.61, null);

        verify(link).setLastForecastAt(madeAt);
        verify(links).save(link);
    }

    @Test
    @DisplayName("a callback for an unknown correlation is ignored")
    void callbackForUnknownCorrelationIsIgnored() {
        when(correlations.findById("missing")).thenReturn(Optional.empty());

        Optional<ForecastEntity> recorded = service.fulfill("missing", 0.5, null);

        assertThat(recorded).isEmpty();
    }

    @Test
    @DisplayName("edp forecaster ids parse to their source and stream, and others are rejected")
    void parsesForecasterId() {
        assertThat(AnalysisRequestService.parse(FORECASTER)).containsExactly("ext-timesfm", "transits");
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> AnalysisRequestService.parse("base-rate"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
