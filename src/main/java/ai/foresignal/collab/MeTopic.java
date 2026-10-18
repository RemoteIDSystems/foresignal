package ai.foresignal.collab;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.ApiTokenRepository;
import ai.foresignal.security.Scope;
import ai.foresignal.stream.Subscription;
import ai.foresignal.stream.sse.EventTopic;
import ai.foresignal.stream.sse.FifoEvent;
import ai.foresignal.stream.sse.ReplayOutcome;
import ai.foresignal.stream.sse.ReplayRequest;
import ai.foresignal.stream.sse.ReplaySink;
import ai.foresignal.stream.sse.SseHub;
import ai.foresignal.stream.sse.StreamPrincipal;
import ai.foresignal.stream.sse.TopicParams;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * The caller's personal {@code me} topic on {@code /v1/events/stream} (prompt 130): {@code share.received},
 * {@code mention}, {@code comment.reply}, {@code saved-search.matches} and (prompt 132) {@code report.received},
 * {@code package.received} and {@code report.retracted} addressed to the stream's principal, with
 * the personal inbox item id as the cursor (key {@value #CURSOR_KEY}), replayed from {@code principal_inbox} after
 * {@code Last-Event-ID}. A token is required; the topic has no filter fields.
 */
@Component
public class MeTopic implements EventTopic<MeFilter> {

    /** The topic name. */
    public static final String NAME = "me";

    /** The composite cursor key. */
    public static final String CURSOR_KEY = "p";

    /** Items per replay page. */
    static final int REPLAY_PAGE = 500;

    private final InboxItemRepository items;
    private final ApiTokenRepository tokens;
    private volatile SseHub hub;

    /** Creates the topic. */
    public MeTopic(InboxItemRepository items, ApiTokenRepository tokens) {
        this.items = items;
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
        return 95;
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public Class<MeFilter> filterType() {
        return MeFilter.class;
    }

    @Override
    public MeFilter parseQuery(TopicParams params, StreamPrincipal principal) {
        return filter(principal);
    }

    @Override
    public MeFilter parseJson(JsonNode block, StreamPrincipal principal) {
        if (block != null && block.isObject() && !block.isEmpty()) {
            throw ValidationException.forField(NAME, "the me topic has no filter fields");
        }
        return filter(principal);
    }

    private MeFilter filter(StreamPrincipal principal) {
        if (principal.anonymous()) {
            throw new ForesignalException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED,
                    "The me topic needs a token.");
        }
        return new MeFilter(principalOf(principal));
    }

    /** The principal behind a stream: the token's principal. */
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
        return items.findFirstByOrderByIdDesc().map(InboxItemEntity::getId).orElse(0L);
    }

    @Override
    public ReplayOutcome replay(ReplayRequest request, MeFilter filter, ReplaySink sink) {
        long cursor = request.after();
        while (true) {
            List<InboxItemEntity> page = items.findByPrincipalIdAndIdGreaterThanOrderByIdAsc(filter.principalId(),
                    cursor, Limit.of(REPLAY_PAGE));
            for (InboxItemEntity item : page) {
                FifoEvent event = event(item);
                if (event != null && !sink.emit(event)) {
                    return new ReplayOutcome.CapReached();
                }
                cursor = item.getId();
            }
            if (page.size() < REPLAY_PAGE) {
                return new ReplayOutcome.Done(Math.max(cursor, request.after()));
            }
        }
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

    /** Sends a just-persisted inbox item to the streams of its principal. */
    public void publish(InboxItemEntity item) {
        SseHub target = hub;
        FifoEvent event = event(item);
        if (target == null || event == null) {
            return;
        }
        target.publish(NAME, f -> f instanceof MeFilter mf && mf.principalId().equals(item.getPrincipalId())
                ? event : null);
    }

    /** The SSE event of an item: its stored payload under its event name; null for events not on this topic. */
    static FifoEvent event(InboxItemEntity item) {
        if (item.getId() == null || !MeEvents.ALL.contains(item.getEvent()) || item.getPayload() == null) {
            return null;
        }
        Map<String, Object> data = new LinkedHashMap<>(item.getPayload());
        return new FifoEvent(NAME, item.getId(), item.getEvent(), data, null);
    }
}
