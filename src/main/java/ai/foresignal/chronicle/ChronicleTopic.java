package ai.foresignal.chronicle;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collab.AccessResolver;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.security.ApiTokenRepository;
import ai.foresignal.security.Scope;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.stream.Subscription;
import ai.foresignal.stream.sse.EventTopic;
import ai.foresignal.stream.sse.FifoEvent;
import ai.foresignal.stream.sse.ReplayOutcome;
import ai.foresignal.stream.sse.ReplayRequest;
import ai.foresignal.stream.sse.ReplaySink;
import ai.foresignal.stream.sse.SseHub;
import ai.foresignal.stream.sse.StreamPrincipal;
import ai.foresignal.stream.sse.TopicParams;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * The {@code chronicle} topic on {@code /v1/events/stream} (prompt 131): {@code chronicle.item}
 * ({@code ChronicleThreadItem}) for every new thread item and {@code chronicle.status} ({@code ChronicleStatusEvent})
 * for every lifecycle change, on the FIFO lane with cursor key {@value #CURSOR_KEY} = the {@code chronicle_event} id,
 * replayed from {@code chronicle_event} after {@code Last-Event-ID}. A token is required. Filter
 * {@code chronicle.chronicleId} (comma-separated, display ids accepted) narrows the stream; a stream only ever
 * receives events of chronicles its principal can see (130 access lists), and private items only with private access.
 */
@Component
public class ChronicleTopic implements EventTopic<ChronicleFilter> {

    /** The topic name. */
    public static final String NAME = "chronicle";

    /** The composite cursor key. */
    public static final String CURSOR_KEY = "k";

    /** The thread item event. */
    public static final String ITEM_EVENT = "chronicle.item";

    /** The status event. */
    public static final String STATUS_EVENT = "chronicle.status";

    /** Most followed chronicles. */
    static final int MAX_IDS = 50;

    /** Items per replay page. */
    static final int REPLAY_PAGE = 500;

    private final ChronicleEventRepository events;
    private final ChronicleRepository chronicles;
    private final AccessResolver access;
    private final ApiTokenRepository tokens;
    private volatile SseHub hub;

    /** Creates the topic. */
    public ChronicleTopic(ChronicleEventRepository events, ChronicleRepository chronicles, AccessResolver access,
            ApiTokenRepository tokens) {
        this.events = events;
        this.chronicles = chronicles;
        this.access = access;
        this.tokens = tokens;
    }

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
        return 97;
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public Class<ChronicleFilter> filterType() {
        return ChronicleFilter.class;
    }

    @Override
    public ChronicleFilter parseQuery(TopicParams params, StreamPrincipal principal) {
        return filter(params.multi("chronicleId"), principal, params.field("chronicleId"));
    }

    @Override
    public ChronicleFilter parseJson(JsonNode block, StreamPrincipal principal) {
        List<String> ids = new ArrayList<>();
        if (block != null && !block.isNull()) {
            if (!block.isObject()) {
                throw ValidationException.forField(NAME, "must be an object");
            }
            for (String field : block.propertyNames()) {
                if (!"chronicleIds".equals(field)) {
                    throw ValidationException.forField(NAME + "." + field, "is not a chronicle filter field");
                }
            }
            JsonNode list = block.get("chronicleIds");
            if (list != null && !list.isNull()) {
                if (!list.isArray()) {
                    throw ValidationException.forField(NAME + ".chronicleIds", "must be an array of chronicle ids");
                }
                list.forEach(n -> ids.add(n.asString()));
            }
        }
        return filter(ids, principal, NAME + ".chronicleIds");
    }

    private ChronicleFilter filter(List<String> raw, StreamPrincipal principal, String field) {
        if (principal.anonymous()) {
            throw new ForesignalException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED,
                    "The chronicle topic needs a token.");
        }
        if (raw.size() > MAX_IDS) {
            throw ValidationException.forField(field, "names more than " + MAX_IDS + " chronicles");
        }
        Set<String> ids = new LinkedHashSet<>();
        for (String id : raw) {
            String canonical = canonical(id);
            if (canonical == null) {
                throw ValidationException.forField(field, "'" + id + "' is not a chronicle id or display id");
            }
            ids.add(canonical);
        }
        boolean seesPrivate = SourceVisibility.canSeePrivate(principal.scope(), principal.privateAccess())
                && !principal.restriction().excludePrivate();
        ObjectCaller caller = new ObjectCaller(principalOf(principal), principal.id(), principal.scope(), seesPrivate,
                Set.of());
        return new ChronicleFilter(caller, ids);
    }

    /** The canonical id of a chronicle id or display id; the id itself when unknown, null when malformed. */
    private String canonical(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        if (id.startsWith("CHR-")) {
            return chronicles.findByDisplayId(id).map(ChronicleEntity::getId).orElse(id);
        }
        return ai.foresignal.security.Ulids.isUlid(id) ? id : null;
    }

    /** The principal behind a stream: the token's principal, or {@code token:<id>} without one. */
    String principalOf(StreamPrincipal principal) {
        String id = principal.id();
        if (id.startsWith("token:")) {
            try {
                UUID tokenId = UUID.fromString(id.substring("token:".length()));
                return tokens.findById(tokenId).map(t -> t.getPrincipalId() != null ? t.getPrincipalId() : id)
                        .orElse(id);
            } catch (IllegalArgumentException e) {
                return id;
            }
        }
        return id;
    }

    @Override
    public long head() {
        return events.findTop1ByOrderByIdDesc().map(ChronicleEventEntity::getId).orElse(0L);
    }

    @Override
    public ReplayOutcome replay(ReplayRequest request, ChronicleFilter filter, ReplaySink sink) {
        long cursor = request.after();
        Map<String, Boolean> visible = new HashMap<>();
        while (true) {
            List<ChronicleEventEntity> page = events.findByIdGreaterThanOrderByIdAsc(cursor, Limit.of(REPLAY_PAGE));
            for (ChronicleEventEntity row : page) {
                if (passes(row, filter, visible) && !sink.emit(frame(row))) {
                    return new ReplayOutcome.CapReached();
                }
                cursor = row.getId();
            }
            if (page.size() < REPLAY_PAGE) {
                return new ReplayOutcome.Done(Math.max(cursor, request.after()));
            }
        }
    }

    /** True when the stream follows the item's chronicle, may see the chronicle, and may see the item. */
    boolean passes(ChronicleEventEntity row, ChronicleFilter filter, Map<String, Boolean> visible) {
        if (!filter.follows(row.getChronicleId())) {
            return false;
        }
        if (row.isPrivateItem() && !filter.caller().seesPrivate()) {
            return false;
        }
        return visible.computeIfAbsent(row.getChronicleId(),
                id -> access.role(filter.caller(), "chronicle:" + id) != null);
    }

    /** The SSE frame of a thread item: {@code chronicle.status} for status changes, else {@code chronicle.item}. */
    static FifoEvent frame(ChronicleEventEntity row) {
        if (ChronicleVocabulary.ITEM_STATUS_CHANGE.equals(row.getType()) && row.getSnapshot() != null
                && row.getSnapshot().get("status") != null) {
            Map<String, Object> data = new java.util.LinkedHashMap<>(row.getSnapshot());
            data.put("chronicleId", row.getChronicleId());
            data.putIfAbsent("at", row.getAt().toString());
            return new FifoEvent(NAME, row.getId(), STATUS_EVENT, data, null);
        }
        return new FifoEvent(NAME, row.getId(), ITEM_EVENT, ChronicleThreadService.item(row, null), null);
    }

    /** The payload of an item's SSE frame (the {@code ChronicleThreadItem} or the {@code ChronicleStatusEvent}). */
    public static Map<String, Object> frameData(ChronicleEventEntity row) {
        return frame(row).data();
    }

    @Override
    public Subscription subscribe(SseHub target) {
        this.hub = target;
        return new Subscription() {
            @Override
            public String name() {
                return "sse-" + NAME;
            }

            @Override
            public void close() {
                hub = null;
            }
        };
    }

    /** Publishes a committed thread item to the live streams that may see it. */
    public void publish(ChronicleEventEntity row) {
        SseHub target = hub;
        if (target == null || row.getId() == null) {
            return;
        }
        FifoEvent event = frame(row);
        target.publish(NAME, f -> f instanceof ChronicleFilter cf && passes(row, cf, new HashMap<>()) ? event : null);
    }
}
