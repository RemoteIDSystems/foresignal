package ai.foresignal.inbound.edp.binding;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.forecast.ForecastEntity;
import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastRepository;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecasterEntity;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.object.ObjectTypes;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

/**
 * Target {@code forecast} (prompt 138, Intelligence-Design §8.6): forecast items become forecasts of the external
 * forecaster {@code edp:{sourceId}/{streamId}} ({@code type=external}, registered on first use) on ledger questions,
 * scored like every forecaster. The question is the item's {@code questionRef}, else the question its
 * {@code question.externalId} is linked to ({@code external_question_link}, with its orientation). A forecast is
 * recorded on the first item, on a material move and at least daily ({@link ExternalForecastPolicy}). The ledger
 * stamps {@code made_at} when the router records it, which is never before Foresignal received the item (EXT-3); the
 * provider's {@code madeAt}, the effective {@code madeAt} and the receipt time are kept in the forecast's
 * {@code inputs.edp}. External forecasters join the 122 ensemble only after 30 resolved linked questions
 * ({@link ExternalForecastCounter}).
 */
@Component
public class ForecastBindingTarget implements BindingTargetHandler {

    private final ObjectProvider<ForecastService> ledger;
    private final ObjectProvider<ForecasterRegistry> forecasters;
    private final ForecastRepository forecasts;
    private final ExternalQuestionLinkRepository links;
    private final Clock clock;

    /** Creates the target. */
    public ForecastBindingTarget(ObjectProvider<ForecastService> ledger, ObjectProvider<ForecasterRegistry> forecasters,
            ForecastRepository forecasts, ExternalQuestionLinkRepository links, Clock clock) {
        this.ledger = ledger;
        this.forecasters = forecasters;
        this.forecasts = forecasts;
        this.links = links;
        this.clock = clock;
    }

    @Override
    public String target() {
        return BindingVocabulary.FORECAST;
    }

    @Override
    public List<FieldError> validate(Map<String, Object> config, BoundStream stream) {
        return new ArrayList<>();
    }

    /**
     * Where an item goes: the question and orientation.
     *
     * @param questionId the ledger question
     * @param orientation {@code same} or {@code inverse}
     * @param link the question link used, or null for a {@code questionRef} item
     */
    record Destination(String questionId, String orientation, ExternalQuestionLinkEntity link) {}

    /** The destination of an item, or empty when it names no linked or referenced question. */
    Optional<Destination> destination(BoundStream stream, BoundItem item) {
        String questionRef = item.text("questionRef");
        if (questionRef != null) {
            return Optional.of(new Destination(questionRef, BindingVocabulary.SAME, null));
        }
        String external = null;
        if (item.payload().get("question") instanceof Map<?, ?> q && q.get("externalId") != null) {
            external = String.valueOf(q.get("externalId"));
        }
        if (external == null) {
            return Optional.empty();
        }
        return links.findBySourceIdAndStreamIdAndExternalQuestionId(item.sourceId(), stream.streamId(), external)
                .map(l -> new Destination(l.getQuestionId(), l.getOrientation(), l));
    }

    @Override
    public BindingEffect plan(ExternalBindingEntity binding, BoundStream stream, BoundItem item) {
        Optional<Destination> destination = destination(stream, item);
        if (destination.isEmpty()) {
            return BindingEffect.skip(item, "question-not-linked");
        }
        return BindingEffect.of(item, "forecast", ObjectTypes.QUESTION + ":" + destination.get().questionId());
    }

    @Override
    public BindingEffect apply(ExternalBindingEntity binding, BoundStream stream, BoundItem item) {
        Optional<Destination> destination = destination(stream, item);
        if (destination.isEmpty()) {
            return BindingEffect.skip(item, "question-not-linked");
        }
        ForecastService service = ledger.getIfAvailable();
        ForecasterRegistry registry = forecasters.getIfAvailable();
        if (service == null || registry == null) {
            return BindingEffect.error(item, "the forecast ledger is not available");
        }
        Destination d = destination.get();
        Optional<ForecastQuestionEntity> question = service.find(d.questionId());
        if (question.isEmpty()) {
            return BindingEffect.error(item, "no question '" + d.questionId() + "'");
        }
        ForecastQuestionEntity q = question.get();
        Instant now = clock.instant();
        if (!q.acceptsForecasts(now)) {
            return BindingEffect.skip(item, "question-" + q.getStatus());
        }
        Double probability = item.payload().get("probability") instanceof Number n ? n.doubleValue() : null;
        // Prompt 155: a decile provider (TimesFM emits p10–p90) is mapped onto the ledger's required quantiles.
        Map<String, Double> quantiles = DecileQuantiles.toLedger(quantiles(item.payload().get("quantiles")));
        ForecastInputs.ForecastValue value = ExternalForecastPolicy.value(q.getType(), probability, quantiles,
                d.orientation());
        if (value == null) {
            return BindingEffect.error(item, ExternalForecastPolicy.unsupported(q.getType(), probability, quantiles,
                    d.orientation()));
        }
        ForecasterEntity forecaster = forecaster(registry, item.sourceId(), stream);
        if (!shouldRecordStanding(forecasts, q.getId(), forecaster.getId(), value, now)) {
            return BindingEffect.skip(item, "no-material-change");
        }
        ForecastEntity recorded = service.record(q, forecaster, value, null, List.of(), List.of(),
                inputs(item, stream, d), null, false);
        if (d.link() != null) {
            d.link().setLastForecastAt(recorded.getMadeAt());
            links.save(d.link());
        }
        return BindingEffect.of(item, "forecast-recorded", ObjectTypes.QUESTION + ":" + q.getId());
    }

    /**
     * The push path's material-change dedup, shared with the on-demand / callback path (prompt 159): true when a
     * new {@code value} for the external forecaster {@code forecasterId} on {@code questionId} should be recorded,
     * false when {@link ExternalForecastPolicy#shouldRecord} finds the forecaster's standing forecast still covers
     * it (same within a day, no material move). Keeps push and on-demand forecasts deduped identically.
     */
    static boolean shouldRecordStanding(ForecastRepository forecasts, String questionId, String forecasterId,
            ForecastInputs.ForecastValue value, Instant now) {
        Optional<ForecastEntity> previous = forecasts.findTop1ByQuestionIdAndForecasterIdOrderByMadeAtDescIdDesc(
                questionId, forecasterId);
        ForecastInputs.ForecastValue standing = previous.map(f -> new ForecastInputs.ForecastValue(
                f.getProbability(), null, f.getQuantiles())).orElse(null);
        return ExternalForecastPolicy.shouldRecord(standing, previous.map(ForecastEntity::getMadeAt).orElse(null),
                value, now);
    }

    /** The external forecaster of a stream, registered on first use. */
    ForecasterEntity forecaster(ForecasterRegistry registry, String sourceId, BoundStream stream) {
        String id = BindingVocabulary.forecasterId(sourceId, stream.streamId());
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("sourceId", sourceId);
        config.put("streamId", stream.streamId());
        String name = stream.stream().title() != null ? stream.stream().title()
                : stream.manifest().name() + " / " + stream.streamId();
        try {
            return registry.ensure(id, "external", name.length() <= 200 ? name : name.substring(0, 200), "1.0.0",
                    config);
        } catch (DataIntegrityViolationException e) {
            return registry.get(id);
        }
    }

    /** The forecast's {@code inputs}: the item's provenance and times. Pure. */
    static Map<String, Object> inputs(BoundItem item, BoundStream stream, Destination d) {
        Map<String, Object> edp = new LinkedHashMap<>();
        edp.put("sourceId", item.sourceId());
        edp.put("streamId", stream.streamId());
        edp.put("itemId", item.itemId());
        edp.put("ref", item.ref());
        edp.put("via", d.link() == null ? "questionRef" : "link");
        if (d.link() != null) {
            edp.put("linkId", d.link().getId());
            edp.put("externalQuestionId", d.link().getExternalQuestionId());
        }
        edp.put("orientation", d.orientation());
        edp.put("providerMadeAt", item.text("madeAt"));
        Instant effective = item.effective("effectiveMadeAt");
        edp.put("effectiveMadeAt", effective == null ? null : effective.toString());
        edp.put("receivedAt", item.receivedAt() == null ? null : item.receivedAt().toString());
        edp.put("originator", item.originator());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("edp", edp);
        return out;
    }

    private static Map<String, Double> quantiles(Object value) {
        if (!(value instanceof Map<?, ?> m)) {
            return null;
        }
        Map<String, Double> out = new LinkedHashMap<>();
        m.forEach((k, v) -> {
            if (v instanceof Number n) {
                out.put(String.valueOf(k), n.doubleValue());
            }
        });
        return out.isEmpty() ? null : out;
    }
}
