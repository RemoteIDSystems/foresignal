package ai.foresignal.stream.sse;

import ai.foresignal.analyst.AnalystNoteEntity;
import ai.foresignal.analyst.AnalystNoteRepository;
import ai.foresignal.analyst.AnalystProposalEntity;
import ai.foresignal.analyst.AnalystProposalRepository;
import ai.foresignal.analyst.AnalystViews;
import ai.foresignal.notify.NotificationBus;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotificationStore;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.store.NotificationEntity;
import ai.foresignal.store.NotificationRepository;
import ai.foresignal.stream.Subscription;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;

/**
 * The {@code analyst} topic (prompt 102): analyst notes and proposals as SSE events
 * {@code analyst.note} and {@code analyst.proposal}, cursor = notification id (key {@code b}). Live
 * events come from the {@link NotificationBus}; a replay pages the {@code notification} table and
 * keeps only the analyst events.
 *
 * <p>Payloads follow the spec (checkpoint 124): {@code analyst.note} is {@code {note}}, the stored note
 * ({@code AnalystNoteEvent}); {@code analyst.proposal} is {@code {change, proposal}}
 * ({@code AnalystProposalEvent}). An event whose note or proposal no longer exists is skipped.
 */
@Component
public class AnalystTopic extends AbstractNotificationTopic {

    /** The topic name. */
    public static final String NAME = "analyst";

    /** The composite cursor key. */
    public static final String CURSOR_KEY = "b";

    /** The note event. */
    public static final String NOTE = "analyst.note";

    /** The proposal event. */
    public static final String PROPOSAL = "analyst.proposal";

    private final NotificationRepository repository;
    private final NotificationBus bus;
    private final ObjectProvider<AnalystNoteRepository> notes;
    private final ObjectProvider<AnalystProposalRepository> proposals;

    /** Creates the topic. */
    public AnalystTopic(NotificationBus bus, SourceRegistry registry, NotificationRepository repository,
            ObjectProvider<AnalystNoteRepository> notes, ObjectProvider<AnalystProposalRepository> proposals) {
        super(bus, registry);
        this.bus = bus;
        this.repository = repository;
        this.notes = notes;
        this.proposals = proposals;
    }

    /**
     * The spec payload of an analyst notification: {@code {note}} or {@code {change, proposal}}, or null when the
     * note or proposal it names is gone.
     */
    Map<String, Object> payload(NotificationMessage message) {
        Map<String, Object> data = message.data() == null ? Map.of() : message.data();
        if (PROPOSAL.equals(message.event())) {
            AnalystProposalRepository repo = proposals.getIfAvailable();
            Object id = data.get("proposalId");
            AnalystProposalEntity proposal = repo == null || id == null ? null
                    : repo.findById(String.valueOf(id)).orElse(null);
            if (proposal == null) {
                return null;
            }
            Map<String, Object> out = new LinkedHashMap<>();
            Object change = data.get("change");
            out.put("change", change == null ? proposal.getStatus() : String.valueOf(change));
            out.put("proposal", AnalystViews.proposal(proposal));
            return out;
        }
        AnalystNoteRepository repo = notes.getIfAvailable();
        Object id = data.get("noteId");
        AnalystNoteEntity note = repo == null || id == null ? null : repo.findById(String.valueOf(id)).orElse(null);
        if (note == null) {
            return null;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("note", AnalystViews.note(note));
        return out;
    }

    /** The frame of an analyst notification, or null when it has no payload any more. */
    private FifoEvent frame(NotificationMessage message) {
        Map<String, Object> payload = payload(message);
        return payload == null ? null : new FifoEvent(NAME, message.id(), message.event(), payload, null);
    }

    @Override
    public ReplayOutcome replay(ReplayRequest request, NotificationFilter filter, ReplaySink sink) {
        long cursor = request.after();
        while (true) {
            List<NotificationEntity> page = page(cursor, REPLAY_PAGE);
            for (NotificationEntity entity : page) {
                NotificationMessage message = NotificationStore.toMessage(entity);
                if (carries(message) && filter.matches(message)) {
                    FifoEvent frame = frame(message);
                    if (frame != null && !sink.emit(frame)) {
                        return new ReplayOutcome.CapReached();
                    }
                }
                cursor = entity.getId();
            }
            if (page.size() < REPLAY_PAGE) {
                return new ReplayOutcome.Done(Math.max(cursor, request.after()));
            }
        }
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
        return 50;
    }

    @Override
    String eventName() {
        return NOTE;
    }

    @Override
    boolean carries(NotificationMessage message) {
        return NOTE.equals(message.event()) || PROPOSAL.equals(message.event());
    }

    @Override
    List<NotificationEntity> page(long cursor, int limit) {
        return repository.findByIdGreaterThanOrderByIdAsc(cursor, Limit.of(limit));
    }

    @Override
    public long head() {
        return repository.findFirstByOrderByIdDesc().map(NotificationEntity::getId).orElse(0L);
    }

    @Override
    public Subscription subscribe(SseHub hub) {
        AutoCloseable handle = bus.subscribe(message -> {
            if (message.id() == null || !carries(message)) {
                return;
            }
            FifoEvent frame = frame(message);
            if (frame == null) {
                return;
            }
            hub.publish(NAME, filter -> ((NotificationFilter) filter).matches(message) ? frame : null);
        });
        return new Subscription() {
            @Override
            public String name() {
                return "sse-analyst";
            }

            @Override
            public void close() {
                try {
                    handle.close();
                } catch (Exception e) {
                    // removing a listener from a list does not fail
                }
            }
        };
    }
}
