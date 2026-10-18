package ai.foresignal.social;

import ai.foresignal.attention.AttentionTopicEntity;
import ai.foresignal.attention.AttentionTopicRepository;
import ai.foresignal.attention.AttentionTopicsChangedEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Holds the current {@link TermIndex} (prompt 135) and rebuilds it from the enabled attention topics that carry a
 * {@code social} block: at startup, after every topic change ({@link AttentionTopicsChangedEvent}) and on demand.
 * Readers never block; a rebuild swaps the index atomically.
 */
@Service
public class TermIndexService {

    private static final Logger log = LoggerFactory.getLogger(TermIndexService.class);

    private final ObjectProvider<AttentionTopicRepository> topics;
    private final TermExpansion expansion;
    private volatile TermIndex index = TermIndex.EMPTY;

    /** Creates the service. */
    public TermIndexService(ObjectProvider<AttentionTopicRepository> topics, TermExpansion expansion) {
        this.topics = topics;
        this.expansion = expansion;
    }

    /** The current index. */
    public TermIndex current() {
        return index;
    }

    /** Rebuilds at startup. */
    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        try {
            rebuild();
        } catch (RuntimeException e) {
            log.warn("Could not build the social term index: {}", e.toString());
        }
    }

    /** Rebuilds after a topic change commits. */
    @TransactionalEventListener(fallbackExecution = true)
    public void onTopicsChanged(AttentionTopicsChangedEvent event) {
        try {
            rebuild();
        } catch (RuntimeException e) {
            log.warn("Could not rebuild the social term index: {}", e.toString());
        }
    }

    /**
     * Rebuilds the index from the enabled topics.
     *
     * @return the new index
     */
    public TermIndex rebuild() {
        AttentionTopicRepository repository = topics.getIfAvailable();
        if (repository == null) {
            return index;
        }
        List<TermIndex.TopicEntry> entries = new ArrayList<>();
        for (AttentionTopicEntity topic : repository.findByEnabledOrderByIdAsc(true)) {
            SocialTopicBlock block = SocialTopicBlock.parse(topic.getSocial(), null);
            if (block == null) {
                continue;
            }
            entries.add(entry(topic.getId(), block));
        }
        TermIndex built = new TermIndex(entries);
        index = built;
        log.debug("Social term index rebuilt: {} topic(s), {} term(s)", entries.size(), built.termCount());
        return built;
    }

    /** The indexed form of one topic, with its places, regions and entities expanded. */
    TermIndex.TopicEntry entry(String topicId, SocialTopicBlock block) {
        Map<String, List<String>> places = new LinkedHashMap<>();
        for (String placeId : block.placeIds()) {
            places.put(placeId, expansion.place(placeId));
        }
        Map<String, List<String>> regions = new LinkedHashMap<>();
        for (String code : block.regionCodes()) {
            regions.put(code, expansion.region(code));
        }
        Map<String, List<String>> entities = new LinkedHashMap<>();
        for (String entityId : block.entityIds()) {
            entities.put(entityId, expansion.entity(entityId));
        }
        return new TermIndex.TopicEntry(topicId, block, places, regions, entities);
    }

    /** Replaces the index (tests and replays). */
    public void install(TermIndex replacement) {
        index = replacement == null ? TermIndex.EMPTY : replacement;
    }
}
