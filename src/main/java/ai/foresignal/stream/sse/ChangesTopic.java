package ai.foresignal.stream.sse;

import ai.foresignal.api.support.ReadCallerResolver;

import ai.foresignal.security.SourceVisibilityGate;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.api.support.ApiProperties;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.BboxParser;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.api.support.NearFilter;
import ai.foresignal.api.support.NearParser;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.retention.RetentionState;
import ai.foresignal.security.Scope;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.ChangesQuery;
import ai.foresignal.store.query.GeometryEncoding;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.store.query.Slices;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.ChangeListener;
import ai.foresignal.stream.ChangeSummary;
import ai.foresignal.stream.LiveBatch;
import ai.foresignal.stream.LiveBus;
import ai.foresignal.stream.LiveListener;
import ai.foresignal.stream.LiveUpdate;
import ai.foresignal.stream.Subscription;
import ai.foresignal.stream.Touch;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * The {@code changes} topic (prompt 015): persisted changes on the FIFO lane ({@code id:} = seq)
 * and live updates and touches on the conflation lane, from one {@link ChangeBus} subscription and
 * one {@link LiveBus} subscription shared by every client.
 *
 * <p><b>Live changes:</b> for each committed batch the versions some client accepts are loaded once
 * ({@code WHERE seq = ANY(?)}, geometry but no payload) and handed to the hub; each client renders
 * its own copy with its {@code leftFilter}. Payloads ({@code payload=true}, and always for kinds in
 * {@code inlinePayloadKinds}) are loaded later on the client's sender thread, in small batches.
 *
 * <p><b>Replay:</b> {@link RecordQueryService#changes} in pages, with the previous persisted box
 * joined by seq so an entity that left the area is replayed with {@code leftFilter: true}. Before
 * replaying a resume point it checks the floor: {@code after < max(lowestRetainedSeq, max purgeSeq)}
 * over the filtered sources means the client must re-snapshot ({@code reset {reason:
 * resync_required}}).
 *
 * <p><b>Gaps:</b> a {@code ChangeBus} overflow ({@code onGap}) makes every client replay from its
 * last delivered seq, and clients with {@code live=true} also get {@code resync {reason: gap}}; a
 * {@code LiveBus} overflow sends {@code resync {reason: live_dropped}} to clients with
 * {@code live=true}.
 */
@Component
public class ChangesTopic implements EventTopic<ChangeFilter>, ChangeListener, LiveListener {

    /** The topic name. */
    public static final String NAME = "changes";

    /** The composite cursor key. */
    public static final String CURSOR_KEY = "c";

    /** The SSE event name of a persisted change. */
    public static final String EVENT = "change";

    /** Changes per replay page. */
    static final int REPLAY_PAGE = 500;

    /** Batches the hub's bus queues hold before they are lagging (ChangeBus) or dropped (LiveBus). */
    static final int BUS_QUEUE = 1_024;

    /** The PATCH block fields. */
    static final Set<String> JSON_FIELDS =
            Set.of("source", "kind", "externalId", "bbox", "near", "radiusKm", "live", "touches", "payload");

    private final RecordQueryService queries;
    private final RecordMapper mapper;
    private final ChangeBus changeBus;
    private final LiveBus liveBus;
    private final RetentionState retention;
    private final SourceRegistry registry;
    private final ApiProperties properties;
    private volatile SseHub hub;

    /** Creates the topic. */
    public ChangesTopic(
            RecordQueryService queries,
            RecordMapper mapper,
            ChangeBus changeBus,
            LiveBus liveBus,
            RetentionState retention,
            SourceRegistry registry,
            ApiProperties properties) {
        this.queries = queries;
        this.mapper = mapper;
        this.changeBus = changeBus;
        this.liveBus = liveBus;
        this.retention = retention;
        this.registry = registry;
        this.properties = properties;
    }

    // ------------------------------------------------------------------------- identity

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String cursorKey() {
        return CURSOR_KEY;
    }

    @Override
    public int cursorOrder() {
        return 0;
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public Class<ChangeFilter> filterType() {
        return ChangeFilter.class;
    }

    // ------------------------------------------------------------------------- filters

    @Override
    public ChangeFilter parseQuery(TopicParams params, StreamPrincipal principal) {
        List<String> sources = params.multi("source");
        checkSources(sources);
        BboxFilter bbox = prefixed(params.prefix(), () -> BboxParser.parse(params.single("bbox")));
        NearFilter near = prefixed(params.prefix(), () -> NearParser.parse(params.single("near"), params.single("radiusKm")));
        return new ChangeFilter(
                sources,
                params.multi("kind"),
                params.multi("externalId"),
                bbox,
                near,
                QueryParams.bool(params.field("payload"), params.single("payload"), false),
                QueryParams.bool(params.field("live"), params.single("live"), true),
                QueryParams.bool(params.field("touches"), params.single("touches"), false),
                excludedSources(principal),
                principal.restriction());
    }

    @Override
    public ChangeFilter parseJson(JsonNode block, StreamPrincipal principal) {
        String prefix = NAME + ".";
        if (block == null || block.isNull()) {
            return ChangeFilter.all(excludedSources(principal), principal.restriction());
        }
        if (!block.isObject()) {
            throw ValidationException.forField(NAME, "must be an object");
        }
        for (String field : block.propertyNames()) {
            if (!JSON_FIELDS.contains(field)) {
                throw ValidationException.forField(prefix + field, "is not a changes filter field");
            }
        }
        List<String> sources = strings(block, "source", prefix);
        checkSources(sources);
        String bboxText = text(block, "bbox", prefix);
        String nearText = text(block, "near", prefix);
        String radius = number(block, "radiusKm", prefix);
        BboxFilter bbox = prefixed(prefix, () -> BboxParser.parse(bboxText));
        NearFilter near = prefixed(prefix, () -> NearParser.parse(nearText, radius));
        return new ChangeFilter(
                sources,
                strings(block, "kind", prefix),
                strings(block, "externalId", prefix),
                bbox,
                near,
                bool(block, "payload", prefix, false),
                bool(block, "live", prefix, true),
                bool(block, "touches", prefix, false),
                excludedSources(principal),
                principal.restriction());
    }

    private void checkSources(List<String> sources) {
        for (String id : sources) {
            registry.descriptor(id);
        }
    }

    /** Private sources the caller may not see: all of them unless it holds an agent or admin token. */
    Set<String> excludedSources(StreamPrincipal principal) {
        // Private sources: agent and admin tokens only (SourceVisibilityGate, prompt 023).
        return ReadCallerResolver.withRestriction(registry,
                SourceVisibilityGate.excludedSources(registry, principal.scope(), TokenRestrictions.NONE),
                principal.restriction());
    }

    // ------------------------------------------------------------------------- resume

    @Override
    public long head() {
        return changeBus.lastPublishedSeq();
    }

    /** The resume floor over {@code sources} (every source when empty). */
    public long floor(List<String> sources) {
        long lowest = retention.lowestRetainedSeq(Set.copyOf(sources));
        return Math.max(lowest, queries.purgeHorizon(sources));
    }

    @Override
    public ReplayOutcome replay(ReplayRequest request, ChangeFilter filter, ReplaySink sink) {
        if (request.checkFloor() && request.after() < floor(filter.sources())) {
            return new ReplayOutcome.ResyncRequired(changeBus.lastPublishedSeq());
        }
        long cursor = request.after();
        while (true) {
            ChangesQuery query = new ChangesQuery(cursor, filter.sources(), filter.kinds(), filter.externalIds(),
                    filter.bbox(), filter.payload(), REPLAY_PAGE, FieldSelection.ALL, GeometryOptions.DEFAULT,
                    GeometryEncoding.GEOJSON, filter.excludedSources(), filter.near(), filter.spatial())
                    .withRestriction(filter.restriction());
            Slices.ChangeSlice slice;
            try {
                slice = queries.changes(query);
            } catch (ForesignalException e) {
                if (ErrorCodes.RESYNC_REQUIRED.equals(e.getCode())) {
                    return new ReplayOutcome.ResyncRequired(changeBus.lastPublishedSeq());
                }
                throw e;
            }
            for (RecordRow row : slice.rows()) {
                FilterMatch match = filter.matchBoxes(RecordMapper.bbox(row.bbox()), RecordMapper.bbox(row.prevBbox()), null);
                if (!match.delivered()) {
                    continue;
                }
                Map<String, Object> data =
                        mapper.streamChange(row, RecordMapper.bbox(row.prevBbox()), null, match == FilterMatch.LEFT);
                if (!sink.emit(new FifoEvent(NAME, row.seq(), EVENT, data, null))) {
                    return new ReplayOutcome.CapReached();
                }
            }
            if (!slice.hasMore()) {
                return new ReplayOutcome.Done(slice.lastSeq());
            }
            cursor = slice.lastSeq();
        }
    }

    @Override
    public Map<Long, String> loadPayloads(Collection<Long> seqs) {
        return queries.payloadsBySeq(seqs);
    }

    // ------------------------------------------------------------------------- live

    @Override
    public Subscription subscribe(SseHub target) {
        this.hub = target;
        Subscription changes = changeBus.subscribe("sse-hub", this, BUS_QUEUE);
        Subscription live = liveBus.subscribe("sse-hub", this, BUS_QUEUE);
        return new Subscription() {
            @Override
            public String name() {
                return "sse-hub";
            }

            @Override
            public void close() {
                changes.close();
                live.close();
                hub = null;
            }
        };
    }

    @Override
    public void onBatch(ChangeBatch batch) {
        SseHub target = hub;
        if (target == null) {
            return;
        }
        List<ChangeSummary> wanted = new ArrayList<>();
        for (ChangeSummary summary : batch.changes()) {
            if (target.anyChangesClient(f -> f.matchChange(summary).delivered())) {
                wanted.add(summary);
            }
        }
        if (wanted.isEmpty()) {
            return;
        }
        Map<Long, RecordRow> rows = queries.changesBySeq(wanted.stream().map(ChangeSummary::seq).toList());
        Set<String> inline = Set.copyOf(properties.changes().inlinePayloadKinds());
        for (ChangeSummary summary : wanted) {
            RecordRow row = rows.get(summary.seq());
            target.publish(NAME, topicFilter -> render((ChangeFilter) topicFilter, summary, row, inline));
        }
    }

    /** One client's {@code change} event for {@code summary}, or null when its filter rejects it. */
    FifoEvent render(ChangeFilter filter, ChangeSummary summary, RecordRow row, Set<String> inline) {
        FilterMatch match = filter.matchChange(summary);
        if (!match.delivered()) {
            return null;
        }
        boolean left = match == FilterMatch.LEFT;
        Map<String, Object> data = row != null
                ? mapper.streamChange(row, summary.prevPersistedBbox(), summary.prevLiveBbox(), left)
                : mapper.streamChange(summary, left);
        boolean payload = row != null && (filter.payload() || inline.contains(summary.kind()));
        return new FifoEvent(NAME, summary.seq(), EVENT, data, payload ? summary.seq() : null);
    }

    @Override
    public void onGap(long lastDeliveredSeq) {
        SseHub target = hub;
        if (target != null) {
            target.gap(NAME);
        }
    }

    @Override
    public void onBatch(LiveBatch batch) {
        SseHub target = hub;
        if (target == null) {
            return;
        }
        for (LiveUpdate update : batch.updates()) {
            target.publishLive(update);
        }
        if (!batch.touches().isEmpty()) {
            Collection<String> kinds = registry.find(batch.sourceId())
                    .map(r -> (Collection<String>) r.descriptor().kinds())
                    .orElse(List.of());
            for (Touch touch : batch.touches()) {
                target.publishTouch(touch, kinds);
            }
        }
    }

    @Override
    public void onLiveDropped() {
        SseHub target = hub;
        if (target != null) {
            target.liveDropped();
        }
    }

    // ------------------------------------------------------------------------- helpers

    /** Runs {@code parse}, prefixing the fields of any validation error with {@code prefix}. */
    static <T> T prefixed(String prefix, Supplier<T> parse) {
        try {
            return parse.get();
        } catch (ValidationException e) {
            if (prefix == null || prefix.isEmpty()) {
                throw e;
            }
            List<FieldError> errors = e.getErrors().stream()
                    .map(err -> new FieldError(prefix + err.field(), err.message()))
                    .toList();
            throw new ValidationException(e.getCode(), e.getMessage(), errors);
        }
    }

    private static List<String> strings(JsonNode block, String field, String prefix) {
        JsonNode node = block.get(field);
        if (node == null || node.isNull()) {
            return List.of();
        }
        if (!node.isArray()) {
            throw ValidationException.forField(prefix + field, "must be an array of strings");
        }
        List<String> values = new ArrayList<>();
        for (JsonNode item : node) {
            if (!item.isString() || item.asString().isBlank()) {
                throw ValidationException.forField(prefix + field, "must be an array of non-empty strings");
            }
            values.add(item.asString().strip());
        }
        return QueryParams.multi(prefix + field, values);
    }

    private static String text(JsonNode block, String field, String prefix) {
        JsonNode node = block.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isString()) {
            throw ValidationException.forField(prefix + field, "must be a string");
        }
        return node.asString();
    }

    private static String number(JsonNode block, String field, String prefix) {
        JsonNode node = block.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isNumber()) {
            throw ValidationException.forField(prefix + field, "must be a number");
        }
        return node.asString();
    }

    private static boolean bool(JsonNode block, String field, String prefix, boolean defaultValue) {
        JsonNode node = block.get(field);
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        if (!node.isBoolean()) {
            throw ValidationException.forField(prefix + field, "must be true or false");
        }
        return node.asBoolean();
    }
}
