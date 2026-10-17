package ai.foresignal.situation;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Duration;
import java.time.Instant;
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
 * Publishes situations as records of the DERIVED source {@code foresignal-intel} (prompt 118), kind
 * {@value #KIND}, externalId = the situation id, so ordinary 017 rules, SSE changes, tiles and search use them
 * unchanged. The {@code ForesignalIntelGenerator} drives it on its ticks.
 *
 * <p>Payload: {@code title}, {@code theme}, {@code status}, {@code severity}, {@code memberCount},
 * {@code signalFamilies}, {@code regionCodes}, {@code corroborationShare}, {@code summary} (the stem, without the
 * aging "Last activity" sentence) and {@code automated: true}. A record is emitted only when its payload changed; it
 * is tombstoned when the situation is merged, turns private (EGR-1: {@code foresignal-intel} is a public source),
 * or has been closed for longer than the {@code intel} retention class ({@value #CLOSED_RETENTION_DAYS} days).
 */
@Component
public class SituationRecordPublisher {

    /** The DERIVED source. */
    public static final String SOURCE_ID = "foresignal-intel";

    /** The record kind. */
    public static final String KIND = "situation";

    /** How long a closed situation stays published (the {@code intel} analytic retention class), days. */
    public static final int CLOSED_RETENTION_DAYS = 1825;

    /** Situations read per page. */
    static final int PAGE = 500;

    /** The overlap of an incremental pass, minutes. */
    static final int OVERLAP_MINUTES = 5;

    private final SituationRepository situations;
    /** The ids with a live head, and the payload last emitted for each (null when unknown since a reconcile). */
    private final Map<String, Integer> published = new HashMap<>();
    private Instant watermark;

    /** Creates the publisher. */
    public SituationRecordPublisher(SituationRepository situations) {
        this.situations = situations;
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

    /** Publishes the changed situations (all of them after a reconcile) and tombstones the ones that left. */
    public synchronized void onTick(Instant now, DerivedContext ctx) {
        boolean full = watermark == null;
        Set<String> seen = new HashSet<>();
        int page = 0;
        Slice<SituationEntity> slice;
        do {
            PageRequest request = PageRequest.of(page++, PAGE);
            slice = full ? situations.findAllByOrderByIdAsc(request)
                    : situations.findByUpdatedAtGreaterThanEqualOrderByIdAsc(
                            watermark.minus(Duration.ofMinutes(OVERLAP_MINUTES)), request);
            for (SituationEntity situation : slice.getContent()) {
                seen.add(situation.getId());
                apply(situation, now, ctx);
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

    /** True when the situation should have a live record at {@code now}. */
    static boolean publishable(SituationEntity situation, Instant now) {
        if (situation.getMergedInto() != null || situation.isPrivateSituation()) {
            return false;
        }
        return !(situation.isClosed() && situation.getClosedAt() != null
                && situation.getClosedAt().plus(Duration.ofDays(CLOSED_RETENTION_DAYS)).isBefore(now));
    }

    private void apply(SituationEntity situation, Instant now, DerivedContext ctx) {
        if (publishable(situation, now)) {
            NormalizedRecord record = record(situation);
            int hash = payload(situation).hashCode();
            Integer last = published.get(situation.getId());
            if (last == null || last != hash) {
                ctx.emit(record);
                published.put(situation.getId(), hash);
            }
        } else if (published.containsKey(situation.getId())) {
            published.remove(situation.getId());
            ctx.tombstone(KIND, situation.getId());
        }
    }

    /** True when the situation currently has a published record (tests). */
    synchronized boolean isPublished(String id) {
        return published.containsKey(id);
    }

    /** The record payload of a situation. */
    static Map<String, Object> payload(SituationEntity situation) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", situation.getTitle());
        payload.put("theme", situation.getTheme());
        payload.put("status", situation.getStatus());
        payload.put("severity", situation.getSeverity());
        payload.put("memberCount", situation.getMemberCount());
        payload.put("signalFamilies", situation.getSignalFamilies());
        payload.put("regionCodes", situation.getRegionCodes());
        payload.put("corroborationShare", situation.getCorroborationShare());
        payload.put("summary", SituationMapper.stem(situation));
        payload.put("private", false);
        payload.put("automated", true);
        return payload;
    }

    /** The record of a situation. */
    static NormalizedRecord record(SituationEntity situation) {
        NormalizedRecord.Builder builder = NormalizedRecord.builder(situation.getId(), KIND)
                .observedAt(situation.getUpdatedAt() == null ? Instant.EPOCH.plusSeconds(1) : situation.getUpdatedAt())
                .putAll(payload(situation));
        if (situation.getGeom() != null) {
            builder.geometry(situation.getGeom());
        }
        return builder.build();
    }
}
