package ai.foresignal.inbound.edp.binding;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.inbound.edp.EdpManifestModel;
import ai.foresignal.inbound.edp.EdpManifestService;
import ai.foresignal.inbound.edp.ExternalEvent;
import ai.foresignal.inbound.edp.ExternalEventBus;
import ai.foresignal.inbound.edp.ExternalSourceChangedEvent;
import ai.foresignal.inbound.edp.ExternalSourceEntity;
import ai.foresignal.isolation.WorkRole;
import ai.foresignal.isolation.WorkThreads;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotificationService;
import ai.foresignal.security.Ulids;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * External data bindings (prompt 138, docs/External-Data-Protocol.md §5): creating, replacing, previewing and deleting
 * bindings, accepting a manifest's suggested bindings, question links for forecast streams, and disabling the bindings
 * of streams a breaking manifest update changed (with an {@code external.binding.disabled} event and a
 * {@code system} notification).
 *
 * <p>A binding is checked against its stream: the target's item types ({@code 400 BINDING_TARGET_INVALID} names the
 * stream's item type and the target's requirement), the target's configuration and the 017 filter. New bindings
 * backfill from stored records within retention unless {@code backfill} is false.
 */
@Service
public class BindingService {

    private static final Logger log = LoggerFactory.getLogger(BindingService.class);

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};
    private static final TypeReference<List<Object>> LIST = new TypeReference<>() {};

    private final EdpManifestService manifests;
    private final ExternalBindingRepository bindings;
    private final ExternalQuestionLinkRepository links;
    private final BindingStreams streams;
    private final BindingFilter filters;
    private final BindingApplier applier;
    private final BindingRouter router;
    private final BindingRecords records;
    private final ExternalCoverageRegistry coverage;
    private final ExternalEventBus events;
    private final ObjectProvider<NotificationService> notifications;
    private final ObjectProvider<ForecastService> ledger;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final TransactionTemplate tx;
    private final Clock clock;

    /** Creates the service. */
    public BindingService(EdpManifestService manifests, ExternalBindingRepository bindings,
            ExternalQuestionLinkRepository links, BindingStreams streams, BindingFilter filters, BindingApplier applier,
            BindingRouter router, BindingRecords records, ExternalCoverageRegistry coverage, ExternalEventBus events,
            ObjectProvider<NotificationService> notifications, ObjectProvider<ForecastService> ledger,
            JdbcTemplate jdbc, JsonMapper json, PlatformTransactionManager transactions, Clock clock) {
        this.manifests = manifests;
        this.bindings = bindings;
        this.links = links;
        this.streams = streams;
        this.filters = filters;
        this.applier = applier;
        this.router = router;
        this.records = records;
        this.coverage = coverage;
        this.events = events;
        this.notifications = notifications;
        this.ledger = ledger;
        this.jdbc = jdbc;
        this.json = json;
        this.tx = new TransactionTemplate(transactions);
        this.tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.clock = clock;
    }

    // ------------------------------------------------------------------------- input

    /**
     * A parsed {@code ExternalBindingInput}.
     *
     * @param streamId the stream
     * @param target the target
     * @param config the target configuration
     * @param filter the 017 conditions, or null
     * @param enabled whether it is on
     * @param backfill whether to backfill
     * @param backfillGiven whether {@code backfill} was given
     */
    record Input(String streamId, String target, Map<String, Object> config, List<Object> filter, boolean enabled,
            boolean backfill, boolean backfillGiven) {}

    /** Parses an {@code ExternalBindingInput} body. */
    Input input(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw invalid("The body must be an ExternalBindingInput object.", List.of());
        }
        List<FieldError> errors = new ArrayList<>();
        for (String member : body.propertyNames()) {
            if (!List.of("streamId", "target", "config", "filter", "enabled", "backfill").contains(member)) {
                errors.add(new FieldError(member, "is not an ExternalBindingInput member"));
            }
        }
        JsonNode stream = body.get("streamId");
        if (stream == null || !stream.isString() || stream.asString().isBlank()) {
            errors.add(new FieldError("streamId", "is required"));
        }
        JsonNode target = body.get("target");
        if (target == null || !target.isString() || !BindingVocabulary.TARGETS.contains(target.asString())) {
            errors.add(new FieldError("target", "must be one of " + BindingVocabulary.TARGETS));
        }
        JsonNode config = body.get("config");
        if (config != null && !config.isNull() && !config.isObject()) {
            errors.add(new FieldError("config", "must be an object"));
        }
        JsonNode filter = body.get("filter");
        if (filter != null && !filter.isNull() && !filter.isArray()) {
            errors.add(new FieldError("filter", "must be an array of {path, op, value} or null"));
        }
        for (String flag : List.of("enabled", "backfill")) {
            JsonNode v = body.get(flag);
            if (v != null && !v.isNull() && !v.isBoolean()) {
                errors.add(new FieldError(flag, "must be a boolean"));
            }
        }
        if (!errors.isEmpty()) {
            throw invalid("The binding is not valid.", errors);
        }
        Map<String, Object> cfg = config == null || config.isNull() ? new LinkedHashMap<>()
                : json.convertValue(config, MAP);
        List<Object> conditions = filter == null || filter.isNull() ? null : json.convertValue(filter, LIST);
        JsonNode backfill = body.get("backfill");
        return new Input(stream.asString().strip(), target.asString(), cfg, conditions,
                body.path("enabled").asBoolean(true), backfill == null || backfill.isNull() || backfill.asBoolean(),
                backfill != null && !backfill.isNull());
    }

    /**
     * Checks an input against its stream; returns the stream.
     *
     * @throws ValidationException {@code 400 BINDING_TARGET_INVALID}
     */
    BoundStream check(String sourceId, Input input) {
        BoundStream stream = streams.stream(sourceId, input.streamId()).orElseThrow(() -> invalid(
                "Provider '" + sourceId + "' has no stream '" + input.streamId() + "'.",
                List.of(new FieldError("streamId", "is not a stream of the manifest"))));
        List<FieldError> errors = new ArrayList<>();
        if (!BindingVocabulary.ITEM_TYPES.getOrDefault(input.target(), java.util.Set.of())
                .contains(stream.itemType())) {
            throw invalid("Stream '" + stream.streamId() + "' carries " + stream.itemType() + " items; target '"
                    + input.target() + "' needs " + BindingVocabulary.requirement(input.target()) + ".",
                    List.of(new FieldError("target", "stream itemType " + stream.itemType() + "; target needs "
                            + BindingVocabulary.requirement(input.target()))));
        }
        String documentType = BindingVocabulary.documentType(input.target());
        if (documentType != null && stream.documentType() != null && !documentType.equals(stream.documentType())) {
            throw invalid("Stream '" + stream.streamId() + "' carries " + stream.documentType() + " documents; target '"
                    + input.target() + "' needs " + BindingVocabulary.requirement(input.target()) + ".",
                    List.of(new FieldError("target", "stream documentType " + stream.documentType()
                            + "; target needs " + documentType)));
        }
        errors.addAll(applier.handler(input.target()).validate(input.config(), stream));
        errors.addAll(filters.validate(input.filter(), "filter"));
        if (!errors.isEmpty()) {
            throw invalid("The binding does not fit target '" + input.target() + "' on stream '" + stream.streamId()
                    + "' (" + stream.itemType() + " items).", errors);
        }
        return stream;
    }

    static ValidationException invalid(String detail, List<FieldError> errors) {
        return new ValidationException(ErrorCodes.BINDING_TARGET_INVALID, detail, errors);
    }

    // ------------------------------------------------------------------------- bindings

    /** The bindings of a provider ({@code 404 UNKNOWN_EXTERNAL_SOURCE}). */
    @Transactional(readOnly = true)
    public List<ExternalBindingEntity> list(String sourceId) {
        manifests.require(sourceId);
        return bindings.findBySourceIdOrderByCreatedAtAscIdAsc(sourceId);
    }

    /** One binding ({@code 404 UNKNOWN_BINDING}). */
    @Transactional(readOnly = true)
    public ExternalBindingEntity get(String bindingId) {
        return bindings.findById(bindingId == null ? "" : bindingId).orElseThrow(() -> new NotFoundException(
                ErrorCodes.UNKNOWN_BINDING, "There is no external binding '" + bindingId + "'."));
    }

    /** Creates a binding from an {@code ExternalBindingInput}; backfills after commit unless asked not to. */
    @Transactional
    public ExternalBindingEntity create(String sourceId, JsonNode body, String actor) {
        ExternalSourceEntity ext = manifests.require(sourceId);
        Input input = input(body);
        BoundStream stream = check(sourceId, input);
        return save(ext, input, stream, actor);
    }

    private ExternalBindingEntity save(ExternalSourceEntity ext, Input input, BoundStream stream, String actor) {
        Instant now = clock.instant();
        ExternalBindingEntity b = new ExternalBindingEntity(Ulids.next(now), ext.getSourceId(), input.streamId(),
                input.target(), actor == null ? "admin" : actor, now);
        b.setConfig(input.config());
        b.setFilter(BindingFilter.normalize(input.filter()));
        b.setEnabled(input.enabled());
        b.setBackfill(input.backfill());
        b.setManifestSha256(ext.getManifestSha256());
        b.setState(!input.enabled() ? BindingVocabulary.DISABLED
                : input.backfill() ? BindingVocabulary.BACKFILLING : BindingVocabulary.ACTIVE);
        bindings.save(b);
        b.setTargetRef(applier.handler(input.target()).setUp(b, stream));
        bindings.save(b);
        coverage.invalidate();
        if (input.enabled() && input.backfill()) {
            afterCommit(() -> startBackfill(b.getId()));
        }
        return b;
    }

    /** Replaces a binding's stream, target, configuration, filter and enabled state. */
    @Transactional
    public ExternalBindingEntity update(String bindingId, JsonNode body) {
        ExternalBindingEntity b = get(bindingId);
        ExternalSourceEntity ext = manifests.require(b.getSourceId());
        Input input = input(body);
        BoundStream stream = check(b.getSourceId(), input);
        boolean changed = !Objects.equals(b.getStreamId(), input.streamId())
                || !Objects.equals(b.getTarget(), input.target()) || !Objects.equals(b.getConfig(), input.config())
                || !Objects.equals(b.getFilter(), BindingFilter.normalize(input.filter()));
        boolean reenabled = input.enabled() && !b.routable();
        boolean setUpAgain = !Objects.equals(b.getTarget(), input.target())
                || !Objects.equals(b.getStreamId(), input.streamId())
                || (BindingVocabulary.CHRONICLE.equals(input.target()) && !Objects.equals(b.getConfig(), input.config()));
        b.setStreamId(input.streamId());
        b.setTarget(input.target());
        b.setConfig(input.config());
        b.setFilter(BindingFilter.normalize(input.filter()));
        b.setEnabled(input.enabled());
        b.setManifestSha256(ext.getManifestSha256());
        boolean backfill = input.enabled() && (input.backfillGiven() ? input.backfill() : changed || reenabled);
        b.setBackfill(input.backfill());
        b.setState(!input.enabled() ? BindingVocabulary.DISABLED
                : backfill ? BindingVocabulary.BACKFILLING : BindingVocabulary.ACTIVE);
        if (input.enabled()) {
            b.setLastError(null);
        }
        if (setUpAgain) {
            b.setTargetRef(applier.handler(input.target()).setUp(b, stream));
        }
        b.setUpdatedAt(clock.instant());
        bindings.save(b);
        coverage.invalidate();
        if (backfill) {
            afterCommit(() -> startBackfill(b.getId()));
        }
        return b;
    }

    /** Deletes a binding; what it created stays. */
    @Transactional
    public void delete(String bindingId) {
        ExternalBindingEntity b = get(bindingId);
        bindings.delete(b);
        jdbc.update("DELETE FROM external_binding_applied WHERE binding_id = ?", b.getId());
        coverage.invalidate();
    }

    /**
     * Creates the bindings the manifest suggests ({@code suggestedBindings} of each stream), skipping those that
     * already exist. A suggestion that does not validate (a target that needs configuration the provider cannot know,
     * such as a hypothesis set) is created disabled in state {@code error} with the problems in {@code lastError}, so
     * the operator can complete it.
     */
    @Transactional
    public List<ExternalBindingEntity> acceptSuggested(String sourceId, String actor) {
        ExternalSourceEntity ext = manifests.require(sourceId);
        EdpManifestModel manifest = manifests.model(ext);
        List<ExternalBindingEntity> existing = bindings.findBySourceIdOrderByCreatedAtAscIdAsc(sourceId);
        List<ExternalBindingEntity> created = new ArrayList<>();
        for (EdpManifestModel.Stream s : manifest.streams()) {
            BoundStream stream = new BoundStream(sourceId, ext.getManifestSha256(), manifest, s);
            for (Map<String, Object> suggestion : stream.suggestedBindings()) {
                String target = String.valueOf(suggestion.get("target"));
                Map<String, Object> config = suggestion.get("config") instanceof JsonNode node && node.isObject()
                        ? json.convertValue(node, MAP) : new LinkedHashMap<>();
                boolean duplicate = existing.stream().anyMatch(b -> b.getStreamId().equals(s.id())
                        && b.getTarget().equals(target) && Objects.equals(b.getConfig(), config));
                if (duplicate || !BindingVocabulary.TARGETS.contains(target)) {
                    continue;
                }
                Input input = new Input(s.id(), target, config, null, true, true, false);
                try {
                    check(sourceId, input);
                    ExternalBindingEntity b = save(ext, input, stream, actor);
                    created.add(b);
                    existing.add(b);
                } catch (ValidationException e) {
                    Instant now = clock.instant();
                    ExternalBindingEntity b = new ExternalBindingEntity(Ulids.next(now), sourceId, s.id(), target,
                            actor == null ? "admin" : actor, now);
                    b.setConfig(config);
                    b.setEnabled(false);
                    b.setBackfill(true);
                    b.setManifestSha256(ext.getManifestSha256());
                    b.setState(BindingVocabulary.ERROR);
                    b.setLastError(e.getMessage() + " " + e.getErrors().stream()
                            .map(f -> f.field() + ": " + f.message()).toList());
                    bindings.save(b);
                    created.add(b);
                    existing.add(b);
                }
            }
        }
        return created;
    }

    /**
     * Evaluates a binding over the stream's last {@value BindingVocabulary#PREVIEW_ITEMS} items without side effects:
     * what would be created or changed for each item its filter matches.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> preview(String bindingId) {
        ExternalBindingEntity b = get(bindingId);
        Map<String, Object> out = new LinkedHashMap<>();
        List<Map<String, Object>> effects = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Optional<BoundStream> stream = streams.stream(b);
        if (stream.isEmpty()) {
            out.put("evaluated", 0);
            out.put("matched", 0);
            out.put("effects", effects);
            out.put("errors", List.of("the stream '" + b.getStreamId() + "' is no longer in the manifest"));
            return out;
        }
        BindingTargetHandler handler = applier.handler(b.getTarget());
        var predicate = filters.compile(b.getFilter(), b.getSourceId());
        int evaluated = 0;
        int matched = 0;
        for (BoundItem item : records.recent(b.getSourceId(), stream.get().kind(), b.getStreamId(),
                BindingVocabulary.PREVIEW_ITEMS, Long.MAX_VALUE)) {
            if (!item.edpItem()) {
                continue;
            }
            evaluated++;
            if (!BindingFilter.matches(predicate, item)) {
                continue;
            }
            matched++;
            BindingEffect effect;
            try {
                effect = handler.plan(b, stream.get(), item);
            } catch (RuntimeException e) {
                effect = BindingEffect.error(item, e.getMessage() == null ? e.toString() : e.getMessage());
            }
            effects.add(effect.view());
            if (effect.failed()) {
                errors.add(item.ref() + ": " + effect.error());
            }
        }
        out.put("evaluated", evaluated);
        out.put("matched", matched);
        out.put("effects", effects);
        out.put("errors", errors);
        return out;
    }

    // ------------------------------------------------------------------------- breaking manifest updates

    /**
     * Disables the bindings of streams a breaking manifest update added, removed or changed: each gets state
     * {@code disabled} with the reason, an {@code external.binding.disabled} event and a {@code system} notification;
     * the operator re-enables it with {@code PUT /v1/external/bindings/{bindingId}} after checking it still fits.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onSourceChanged(ExternalSourceChangedEvent change) {
        if (!change.breaking() || change.changedStreams().isEmpty()) {
            return;
        }
        List<ExternalBindingEntity> disabled = new ArrayList<>();
        String reason = "breaking manifest update: " + String.join("; ", change.breakingChanges());
        tx.executeWithoutResult(status -> {
            for (ExternalBindingEntity b : bindings.findBySourceIdOrderByCreatedAtAscIdAsc(change.sourceId())) {
                if (!b.isEnabled() || !change.changedStreams().contains(b.getStreamId())) {
                    continue;
                }
                b.setEnabled(false);
                b.setState(BindingVocabulary.DISABLED);
                b.setLastError(reason);
                b.setUpdatedAt(change.at());
                bindings.save(b);
                disabled.add(b);
            }
        });
        coverage.invalidate();
        for (ExternalBindingEntity b : disabled) {
            events.publish(ExternalEvent.bindingDisabled(b.getId(), b.getSourceId(), reason, change.at()));
            notify(b, reason);
        }
    }

    /** The {@code system} notification of a disabled binding. Pure. */
    static NotificationMessage disabledMessage(ExternalBindingEntity b, String reason) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("bindingId", b.getId());
        data.put("sourceId", b.getSourceId());
        data.put("streamId", b.getStreamId());
        data.put("target", b.getTarget());
        data.put("reason", reason);
        return NotificationMessage.builder(NotificationMessage.SYSTEM, "system.binding_disabled",
                        "External binding disabled: " + b.getSourceId() + "/" + b.getStreamId() + " → " + b.getTarget(),
                        "warning")
                .body("The provider changed stream '" + b.getStreamId() + "' in a breaking way (" + reason + "). The"
                        + " binding stopped; review the stream and re-enable the binding if it still fits.")
                .url("/v1/external/bindings/" + b.getId())
                .data(data)
                .tags(List.of("external"))
                .build();
    }

    private void notify(ExternalBindingEntity b, String reason) {
        NotificationService service = notifications.getIfAvailable();
        if (service == null) {
            return;
        }
        try {
            service.publish(disabledMessage(b, reason), null);
        } catch (RuntimeException e) {
            log.warn("The notification for disabled binding {} failed: {}", b.getId(), e.toString());
        }
    }

    // ------------------------------------------------------------------------- question links

    /** The question links of a provider ({@code 404 UNKNOWN_EXTERNAL_SOURCE}). */
    @Transactional(readOnly = true)
    public List<ExternalQuestionLinkEntity> links(String sourceId) {
        manifests.require(sourceId);
        return links.findBySourceIdOrderByCreatedAtAscIdAsc(sourceId);
    }

    /**
     * Links a provider question to a ledger question ({@code ExternalQuestionLinkInput}).
     *
     * @throws ValidationException {@code 400 BINDING_TARGET_INVALID} for a stream that is not a forecast stream or an
     *     inverse link on a question that is not binary
     * @throws NotFoundException {@code 404 UNKNOWN_EXTERNAL_SOURCE} or {@code UNKNOWN_QUESTION}
     * @throws ConflictException {@code 409 QUESTION_ALREADY_LINKED}
     */
    @Transactional
    public ExternalQuestionLinkEntity link(String sourceId, JsonNode body, String actor, boolean seesPrivate) {
        manifests.require(sourceId);
        if (body == null || !body.isObject()) {
            throw invalid("The body must be an ExternalQuestionLinkInput object.", List.of());
        }
        List<FieldError> errors = new ArrayList<>();
        String streamId = text(body, "streamId", errors, true);
        String external = text(body, "externalQuestionId", errors, true);
        String questionId = text(body, "questionId", errors, true);
        String orientation = text(body, "orientation", errors, false);
        if (orientation == null) {
            orientation = BindingVocabulary.SAME;
        } else if (!BindingVocabulary.ORIENTATIONS.contains(orientation)) {
            errors.add(new FieldError("orientation", "must be same or inverse"));
        }
        if (external != null && external.length() > 256) {
            errors.add(new FieldError("externalQuestionId", "must be at most 256 characters"));
        }
        if (!errors.isEmpty()) {
            throw invalid("The question link is not valid.", errors);
        }
        BoundStream stream = streams.stream(sourceId, streamId).orElseThrow(() -> invalid("Provider '" + sourceId
                + "' has no stream '" + streamId + "'.", List.of(new FieldError("streamId",
                "is not a stream of the manifest"))));
        if (!"forecast".equals(stream.itemType())) {
            throw invalid("Stream '" + streamId + "' carries " + stream.itemType() + " items; question links need "
                    + BindingVocabulary.requirement(BindingVocabulary.FORECAST) + ".",
                    List.of(new FieldError("streamId", "stream itemType " + stream.itemType()
                            + "; question links need forecast")));
        }
        ForecastService service = ledger.getIfAvailable();
        if (service == null) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_QUESTION, "No forecast question '" + questionId + "'.");
        }
        ForecastQuestionEntity question = service.get(questionId, seesPrivate);
        if (BindingVocabulary.INVERSE.equals(orientation) && !Ledger.BINARY.equals(question.getType())) {
            throw invalid("Only binary questions can be linked with orientation inverse.",
                    List.of(new FieldError("orientation", "inverse needs a binary question")));
        }
        if (links.findBySourceIdAndStreamIdAndExternalQuestionId(sourceId, streamId, external).isPresent()) {
            throw new ConflictException(ErrorCodes.QUESTION_ALREADY_LINKED, "Provider question '" + external
                    + "' of " + sourceId + "/" + streamId + " is already linked.");
        }
        ExternalQuestionLinkEntity link = new ExternalQuestionLinkEntity(Ulids.next(clock.instant()), sourceId,
                streamId, external, question.getId(), orientation, actor == null ? "agent" : actor, clock.instant());
        return links.save(link);
    }

    /** Removes a question link ({@code 404 UNKNOWN_QUESTION_LINK}); recorded forecasts stay. */
    @Transactional
    public void unlink(String linkId) {
        ExternalQuestionLinkEntity link = links.findById(linkId == null ? "" : linkId).orElseThrow(() ->
                new NotFoundException(ErrorCodes.UNKNOWN_QUESTION_LINK, "There is no question link '" + linkId + "'."));
        links.delete(link);
    }

    // ------------------------------------------------------------------------- views

    /** The {@code ExternalBinding} view. */
    public Map<String, Object> view(ExternalBindingEntity b) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", b.getId());
        out.put("sourceId", b.getSourceId());
        out.put("streamId", b.getStreamId());
        out.put("target", b.getTarget());
        out.put("config", b.getConfig());
        out.put("filter", b.getFilter());
        out.put("enabled", b.isEnabled());
        out.put("backfill", b.isBackfill());
        out.put("manifestSha256", b.getManifestSha256());
        out.put("state", b.getState());
        out.put("applied", b.getApplied());
        out.put("lastError", b.getLastError());
        out.put("targetRef", b.getTargetRef());
        out.put("createdBy", b.getCreatedBy());
        out.put("createdAt", b.getCreatedAt());
        out.put("updatedAt", b.getUpdatedAt());
        out.put("lastAppliedAt", b.getLastAppliedAt());
        return out;
    }

    /** The {@code ExternalQuestionLink} view. */
    public static Map<String, Object> view(ExternalQuestionLinkEntity l) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", l.getId());
        out.put("sourceId", l.getSourceId());
        out.put("streamId", l.getStreamId());
        out.put("externalQuestionId", l.getExternalQuestionId());
        out.put("questionId", l.getQuestionId());
        out.put("orientation", l.getOrientation());
        out.put("forecasterId", l.getForecasterId());
        out.put("createdAt", l.getCreatedAt());
        out.put("lastForecastAt", l.getLastForecastAt());
        return out;
    }

    // ------------------------------------------------------------------------- helpers

    /** Runs a backfill in the background when the router runs, else at once (tests drive the router themselves). */
    void startBackfill(String bindingId) {
        Runnable task = () -> {
            try {
                int n = router.backfill(bindingId);
                log.info("Binding {} backfilled {} stored item(s)", bindingId, n);
            } catch (RuntimeException e) {
                log.warn("Binding {} backfill failed: {}", bindingId, e.toString());
                bindings.findById(bindingId).ifPresent(b -> {
                    b.setState(BindingVocabulary.ACTIVE);
                    b.setLastError("backfill failed: " + e.getMessage());
                    bindings.save(b);
                });
            }
        };
        if (router.autoStart()) {
            WorkThreads.start(WorkRole.COLLECT, "external-binding-backfill-" + bindingId, task);
        } else {
            task.run();
        }
    }

    private static void afterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }

    private static String text(JsonNode body, String name, List<FieldError> errors, boolean required) {
        JsonNode v = body.get(name);
        if (v == null || v.isNull()) {
            if (required) {
                errors.add(new FieldError(name, "is required"));
            }
            return null;
        }
        if (!v.isString() || v.asString().isBlank()) {
            errors.add(new FieldError(name, "must be a non-empty string"));
            return null;
        }
        return v.asString().strip();
    }
}
