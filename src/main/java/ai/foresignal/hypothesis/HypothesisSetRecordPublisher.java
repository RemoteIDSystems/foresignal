package ai.foresignal.hypothesis;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

/**
 * Publishes hypothesis sets as records of the DERIVED source {@code foresignal-intel} (prompt 125 §1), kind
 * {@value #KIND}, externalId = the set id, so ordinary 017 rules, SSE changes and search see them. The
 * {@code ForesignalIntelGenerator} drives it on its ticks.
 *
 * <p>Payload: {@code question}, {@code kind}, {@code hypotheses} (id, label, probability, ICD term, confidence),
 * {@code confidence} (set level), {@code status}, {@code lastRevisionAt}, {@code private: false} and
 * {@code automated: true}. A record is emitted only when its payload changed; it is tombstoned when the set turns
 * private (EGR-1: {@code foresignal-intel} is a public source) or is deleted.
 */
@Component
public class HypothesisSetRecordPublisher {

    /** The DERIVED source. */
    public static final String SOURCE_ID = "foresignal-intel";

    /** The record kind. */
    public static final String KIND = "hypothesis-set";

    /** Sets read per page. */
    static final int PAGE = 500;

    /** The overlap of an incremental pass, minutes. */
    static final int OVERLAP_MINUTES = 5;

    private final HypothesisSetRepository sets;
    private final HypothesisRepository hypotheses;
    /** The ids with a live head, and the payload hash last emitted for each (null when unknown since a reconcile). */
    private final Map<String, Integer> published = new HashMap<>();
    private Instant watermark;

    /** Creates the publisher. */
    public HypothesisSetRecordPublisher(HypothesisSetRepository sets, HypothesisRepository hypotheses) {
        this.sets = sets;
        this.hypotheses = hypotheses;
    }

    /** Rebuilds the published set from the source's own live heads; the next tick is a full pass. */
    public synchronized void reconcile(List<HeadSnapshot> ownHeads) {
        published.clear();
        for (HeadSnapshot head : ownHeads) {
            if (!head.tombstone() && KIND.equals(head.kind())) {
                published.put(head.externalId(), null);
            }
        }
        watermark = null;
    }

    /** Publishes the changed sets (all of them after a reconcile) and tombstones the ones that left. */
    public synchronized void onTick(Instant now, DerivedContext ctx) {
        boolean full = watermark == null;
        Set<String> seen = new HashSet<>();
        int page = 0;
        Slice<HypothesisSetEntity> slice;
        do {
            PageRequest request = PageRequest.of(page++, PAGE);
            slice = full ? sets.findAllByOrderByIdAsc(request)
                    : sets.findByUpdatedAtGreaterThanEqualOrderByIdAsc(
                            watermark.minus(Duration.ofMinutes(OVERLAP_MINUTES)), request);
            for (HypothesisSetEntity set : slice.getContent()) {
                seen.add(set.getId());
                apply(set, ctx);
            }
        } while (slice.hasNext());
        if (full) {
            for (String id : new LinkedHashSet<>(published.keySet())) {
                if (!seen.contains(id)) {
                    published.remove(id);
                    ctx.tombstone(KIND, id);
                }
            }
        }
        watermark = now;
    }

    private void apply(HypothesisSetEntity set, DerivedContext ctx) {
        if (!set.isPrivateSet()) {
            Map<String, Object> payload = payload(set, hypotheses.findBySetIdOrderByPositionAscIdAsc(set.getId()));
            int hash = payload.hashCode();
            Integer last = published.get(set.getId());
            if (last == null || last != hash) {
                ctx.emit(record(set, payload));
                published.put(set.getId(), hash);
            }
        } else if (published.containsKey(set.getId())) {
            published.remove(set.getId());
            ctx.tombstone(KIND, set.getId());
        }
    }

    /** True when the set currently has a published record (tests). */
    synchronized boolean isPublished(String id) {
        return published.containsKey(id);
    }

    /** The record payload of a set. */
    static Map<String, Object> payload(HypothesisSetEntity set, List<HypothesisEntity> hyps) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("question", set.getQuestion());
        payload.put("kind", set.getKind());
        List<Map<String, Object>> items = new ArrayList<>();
        for (HypothesisEntity h : hyps) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", h.getId());
            item.put("label", h.getLabel());
            item.put("type", h.getType());
            item.put("probability", h.getProbability());
            item.put("estimativeTerm", h.getEstimativeTerm());
            item.put("confidence", h.getConfidence());
            item.put("status", h.getStatus());
            items.add(item);
        }
        payload.put("hypotheses", items);
        payload.put("confidence", set.getConfidence());
        payload.put("status", set.getStatus());
        payload.put("regionCodes", set.getRegionCodes());
        payload.put("lastRevisionAt", set.getLastRevisionAt() == null ? null : set.getLastRevisionAt().toString());
        payload.put("private", false);
        payload.put("automated", true);
        return payload;
    }

    /** The record of a set. */
    static NormalizedRecord record(HypothesisSetEntity set, Map<String, Object> payload) {
        return NormalizedRecord.builder(set.getId(), KIND)
                .observedAt(set.getUpdatedAt() == null ? Instant.EPOCH.plusSeconds(1) : set.getUpdatedAt())
                .putAll(payload)
                .build();
    }
}
