package ai.foresignal.inbound.edp.binding;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.forecast.ForecastEntity;
import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastRepository;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecasterEntity;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.inbound.edp.EdpFetchException;
import ai.foresignal.inbound.edp.EdpHttpClient;
import ai.foresignal.inbound.edp.EdpManifestModel;
import ai.foresignal.inbound.edp.EdpManifestService;
import ai.foresignal.inbound.edp.ExternalSourceEntity;
import ai.foresignal.security.Ulids;
import ai.foresignal.settings.ServerSettingsService;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The on-demand analysis-request round-trip (prompt 155): Foresignal asks an external forecaster to forecast a
 * ledger question now, over the manifest's {@code transport.analysisRequest} endpoint, and ingests the correlated
 * result. A provider that answers synchronously returns the forecast in the HTTP response (used by the
 * retrospective backtest runner); one that answers asynchronously posts it to the callback endpoint, which calls
 * {@link #fulfill}. Either way the returned forecast is recorded as the external forecaster
 * {@code edp:{sourceId}/{streamId}}, exactly like a pushed or pulled forecast item, decile output mapped onto the
 * ledger's required quantiles.
 *
 * <p>TimesFM and every other forecaster stay external services; this is only the transport and the correlation.
 */
@Service
public class AnalysisRequestService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisRequestService.class);

    /** The response-body cap of an analysis request. */
    static final long MAX_BYTES = 1_048_576;

    /** The path the provider posts an asynchronous result to. */
    public static final String CALLBACK_PATH = "/v1/external/analysis/callback";

    private final EdpManifestService manifests;
    private final EdpHttpClient http;
    private final AnalysisCorrelationRepository correlations;
    private final ForecastService ledger;
    private final ForecasterRegistry forecasters;
    private final ForecastRepository forecasts;
    private final ExternalQuestionLinkRepository links;
    private final JsonMapper json;
    private final Clock clock;
    private final ObjectProvider<ServerSettingsService> server;

    /** Creates the service. */
    public AnalysisRequestService(EdpManifestService manifests, EdpHttpClient http,
            AnalysisCorrelationRepository correlations, ForecastService ledger, ForecasterRegistry forecasters,
            ForecastRepository forecasts, ExternalQuestionLinkRepository links, JsonMapper json, Clock clock,
            ObjectProvider<ServerSettingsService> server) {
        this.manifests = manifests;
        this.http = http;
        this.correlations = correlations;
        this.ledger = ledger;
        this.forecasters = forecasters;
        this.forecasts = forecasts;
        this.links = links;
        this.json = json;
        this.clock = clock;
        this.server = server;
    }

    /**
     * The outcome of an on-demand request.
     *
     * @param correlationId the id sent to the provider and echoed in its callback
     * @param recorded true when the provider answered synchronously and the forecast was recorded at once
     */
    public record Requested(String correlationId, boolean recorded) {}

    /**
     * Requests an on-demand forecast of {@code questionId} from the external forecaster {@code forecasterId}
     * ({@code edp:{sourceId}/{streamId}}). Persists the correlation, posts the request, and — if the provider
     * answers synchronously — records the forecast at once.
     *
     * @throws IllegalArgumentException when the forecaster id, the provider or its analysis-request transport is
     *     unknown, or the question is missing or not open
     */
    @Transactional
    public Requested request(String forecasterId, String questionId, String orientation, String horizon,
            String actor) {
        String[] parts = parse(forecasterId);
        String sourceId = parts[0];
        String streamId = parts[1];
        ExternalSourceEntity ext;
        try {
            ext = manifests.require(sourceId);
        } catch (ForesignalException e) {
            // requestAnalysis answers an unknown provider like any other unusable forecaster id: 400.
            throw new IllegalArgumentException("There is no external source '" + sourceId + "'");
        }
        EdpManifestModel model = manifests.model(ext);
        EdpManifestModel.AnalysisRequest transport = model.analysisRequest();
        if (transport == null) {
            throw new IllegalArgumentException("Provider '" + sourceId + "' declares no transport.analysisRequest");
        }
        ForecastQuestionEntity q = ledger.find(questionId)
                .orElseThrow(() -> new IllegalArgumentException("No question '" + questionId + "'"));
        Instant now = clock.instant();
        if (!q.acceptsForecasts(now)) {
            throw new IllegalArgumentException("Question '" + questionId + "' is " + q.getStatus());
        }
        String correlationId = Ulids.next(now);
        correlations.save(new AnalysisCorrelationEntity(correlationId, sourceId, streamId, questionId, orientation,
                horizon, actor == null ? "operator" : actor, now));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("questionId", questionId);
        body.put("horizon", horizon);
        body.put("correlationId", correlationId);
        body.put("callbackUrl", callbackUrl());
        JsonNode response = post(transport, ext.isAllowPrivateNetwork(), body);
        boolean recorded = false;
        if (response != null && (response.hasNonNull("probability") || response.has("quantiles"))) {
            // A synchronous provider returned the forecast in the response body.
            recorded = fulfillInternal(correlationId, probability(response), quantiles(response), now) != null;
        }
        return new Requested(correlationId, recorded);
    }

    /**
     * Records the forecast a provider returned for a correlation (the callback path). Resolves the correlation to
     * its question and external forecaster and records the forecast, decile output mapped to required quantiles.
     *
     * @return the recorded forecast, or empty when the correlation is unknown, already answered, or the forecast
     *     is unusable for the question
     */
    @Transactional
    public Optional<ForecastEntity> fulfill(String correlationId, Double probability, Map<String, Double> quantiles) {
        return Optional.ofNullable(fulfillInternal(correlationId, probability, quantiles, clock.instant()));
    }

    private ForecastEntity fulfillInternal(String correlationId, Double probability, Map<String, Double> quantiles,
            Instant now) {
        AnalysisCorrelationEntity correlation = correlations.findById(correlationId).orElse(null);
        if (correlation == null || !AnalysisCorrelationEntity.REQUESTED.equals(correlation.getStatus())) {
            return null;
        }
        ForecastQuestionEntity q = ledger.find(correlation.getQuestionId()).orElse(null);
        if (q == null || !q.acceptsForecasts(now)) {
            correlation.setStatus(AnalysisCorrelationEntity.FAILED);
            correlation.setFulfilledAt(now);
            correlations.save(correlation);
            return null;
        }
        Map<String, Double> mapped = DecileQuantiles.toLedger(quantiles);
        ForecastInputs.ForecastValue value = ExternalForecastPolicy.value(q.getType(), probability, mapped,
                correlation.getOrientation());
        if (value == null) {
            correlation.setStatus(AnalysisCorrelationEntity.FAILED);
            correlation.setFulfilledAt(now);
            correlations.save(correlation);
            return null;
        }
        ForecasterEntity forecaster = forecasters.ensure(correlation.getForecasterId(), "external",
                correlation.getForecasterId(), "1.0.0",
                Map.of("sourceId", correlation.getSourceId(), "streamId", correlation.getStreamId()));
        Map<String, Object> inputs = new LinkedHashMap<>();
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("correlationId", correlationId);
        request.put("horizon", correlation.getHorizon());
        request.put("sourceId", correlation.getSourceId());
        request.put("streamId", correlation.getStreamId());
        inputs.put("analysisRequest", request);
        // Prompt 159: dedup and stamp lastForecastAt exactly like the push path (ForecastBindingTarget), so on-demand
        // and pushed forecasts are tracked identically; the forecaster identity and ledger.record terminal are the
        // same, so scoring is unchanged. A deduped answer still fulfills the correlation (it was answered).
        if (!ForecastBindingTarget.shouldRecordStanding(forecasts, q.getId(), forecaster.getId(), value, now)) {
            correlation.setStatus(AnalysisCorrelationEntity.FULFILLED);
            correlation.setFulfilledAt(now);
            correlations.save(correlation);
            return null;
        }
        ForecastEntity recorded = ledger.record(q, forecaster, value, null, List.of(), List.of(), inputs, null, false);
        for (ExternalQuestionLinkEntity link : links.findBySourceIdAndStreamIdAndQuestionId(correlation.getSourceId(),
                correlation.getStreamId(), correlation.getQuestionId())) {
            link.setLastForecastAt(recorded.getMadeAt());
            links.save(link);
        }
        correlation.setStatus(AnalysisCorrelationEntity.FULFILLED);
        correlation.setFulfilledAt(now);
        correlations.save(correlation);
        return recorded;
    }

    /**
     * Requests a forecast of {@code q} as of {@code asOf} synchronously, for the retrospective backtest runner.
     * Returns the parsed forecast value, or null when the provider, its transport or the response is unusable.
     */
    public ForecastInputs.ForecastValue forecastSyncAsOf(String sourceId, String streamId, ForecastQuestionEntity q,
            Instant asOf) {
        ExternalSourceEntity ext;
        EdpManifestModel.AnalysisRequest transport;
        try {
            ext = manifests.require(sourceId);
            transport = manifests.model(ext).analysisRequest();
        } catch (RuntimeException e) {
            return null;
        }
        if (transport == null) {
            return null;
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("questionId", q.getId());
        body.put("asOf", asOf.toString());
        JsonNode response = post(transport, ext.isAllowPrivateNetwork(), body);
        if (response == null || !(response.hasNonNull("probability") || response.has("quantiles"))) {
            return null;
        }
        return ExternalForecastPolicy.value(q.getType(), probability(response),
                DecileQuantiles.toLedger(quantiles(response)), BindingVocabulary.SAME);
    }

    private JsonNode post(EdpManifestModel.AnalysisRequest transport, boolean allowPrivateNetwork,
            Map<String, Object> body) {
        try {
            EdpHttpClient.EdpHttpResponse response = http.post(URI.create(transport.url()),
                    Map.of("Accept", "application/json"), json.writeValueAsString(body), allowPrivateNetwork,
                    MAX_BYTES);
            String text = response.bodyText();
            return text == null || text.isBlank() ? null : json.readTree(text);
        } catch (EdpFetchException e) {
            log.info("Analysis request to {} failed: {}", transport.url(), e.getMessage());
            return null;
        } catch (RuntimeException e) {
            log.info("Analysis request to {} could not be parsed: {}", transport.url(), e.toString());
            return null;
        }
    }

    private String callbackUrl() {
        ServerSettingsService settings = server.getIfAvailable();
        String base = settings == null ? null : settings.get().normalizedPublicBaseUrl();
        return base == null ? null : base + CALLBACK_PATH;
    }

    /** {@code edp:{sourceId}/{streamId}} → {@code [sourceId, streamId]}. */
    static String[] parse(String forecasterId) {
        String prefix = BindingVocabulary.FORECASTER_PREFIX;
        if (forecasterId == null || !forecasterId.startsWith(prefix) || !forecasterId.contains("/")) {
            throw new IllegalArgumentException("'" + forecasterId + "' is not an external forecaster id");
        }
        String rest = forecasterId.substring(prefix.length());
        int slash = rest.indexOf('/');
        return new String[] {rest.substring(0, slash), rest.substring(slash + 1)};
    }

    private static Double probability(JsonNode node) {
        JsonNode p = node.get("probability");
        return p != null && p.isNumber() ? p.doubleValue() : null;
    }

    private static Map<String, Double> quantiles(JsonNode node) {
        JsonNode q = node.get("quantiles");
        if (q == null || !q.isObject()) {
            return null;
        }
        Map<String, Double> out = new LinkedHashMap<>();
        q.properties().forEach(e -> {
            if (e.getValue().isNumber()) {
                out.put(e.getKey(), e.getValue().doubleValue());
            }
        });
        return out.isEmpty() ? null : out;
    }
}
