package ai.foresignal.intel;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.derive.DerivedGenerator;
import ai.foresignal.derive.DerivedInputs;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

/**
 * {@code foresignal-intel} (prompt 101): publishes every public intelligence event as a record of kind
 * {@code intelligence-event}, externalId = the event id, so SSE changes, tiles, search and ordinary
 * 017 rules (for example {@code kind=intelligence-event}, {@code severity == critical}, {@code
 * enter}) use them unchanged. Emit only on change; tombstone a dismissed or merged event.
 *
 * <p><strong>Private events are never published here</strong> (invariant EGR-1). {@code foresignal-intel}
 * is a public source, so a record of it reaches tiles, search, {@code query_records} and anonymous
 * readers; an event with a member from a private source is therefore left out, and a record published
 * before the event became private is tombstoned. Private events stay readable through the
 * private-aware {@code /v1/intel/events} paths and the {@code intel} SSE topic.
 *
 * <p><strong>Every event is covered.</strong> After {@link #reconcile} (startup, gaps) the next tick
 * pages through <em>all</em> events; later ticks page through the events updated since the previous
 * tick (with a {@value #OVERLAP_MINUTES}-minute overlap for late commits). The set of published ids is
 * rebuilt from the source's own heads, so nothing is tombstoned merely because it is old.
 */
@Component
public class ForesignalIntelGenerator implements DerivedGenerator {

    /** The source id. */
    public static final String ID = "foresignal-intel";

    /** The record kind. */
    public static final String KIND = "intelligence-event";

    /** Events read per page. */
    static final int PAGE = 500;

    /** The overlap of an incremental pass, minutes. */
    static final int OVERLAP_MINUTES = 5;

    private final SourceDescriptor descriptor = buildDescriptor();
    private final IntelligenceEventRepository events;
    private final IntelEventMemberRepository members;
    /** The ids with a live (published, not tombstoned) head. */
    private final Set<String> known = new LinkedHashSet<>();
    /** The start of the previous pass, or null when the next pass must be a full one. */
    private Instant watermark;

    /** The situation records (prompt 118), or null when the situation layer is absent. */
    private ai.foresignal.situation.SituationRecordPublisher situations;

    /** The hypothesis-set records (prompt 125), or null when the hypothesis layer is absent. */
    private ai.foresignal.hypothesis.HypothesisSetRecordPublisher hypothesisSets;

    /** Creates the generator. */
    public ForesignalIntelGenerator(IntelligenceEventRepository events, IntelEventMemberRepository members) {
        this.events = events;
        this.members = members;
    }

    /** Injects the situation record publisher (prompt 118: kind {@code situation} of this source). */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setSituations(ai.foresignal.situation.SituationRecordPublisher situations) {
        this.situations = situations;
    }

    /** Injects the hypothesis-set record publisher (prompt 125: kind {@code hypothesis-set} of this source). */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setHypothesisSets(ai.foresignal.hypothesis.HypothesisSetRecordPublisher hypothesisSets) {
        this.hypothesisSets = hypothesisSets;
    }

    /** The descriptor (analytic retention class {@code intel}: 1825 days, Conventions "Analytic retention"). */
    public static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("Intelligence events (derived)")
                .description("Typed, located, time-bounded intelligence findings and their members, situations"
                        + " (prompt 118) and hypothesis sets (prompt 125), as ordinary records so rules, SSE, tiles and"
                        + " search use them unchanged. Private events, situations and sets are not published as"
                        + " records.")
                .kinds(KIND, ai.foresignal.situation.SituationRecordPublisher.KIND,
                        ai.foresignal.hypothesis.HypothesisSetRecordPublisher.KIND)
                .embedFields("title", "summary")
                .attribution("Derived by Foresignal from stored versions")
                .licence("derived")
                .mode(Mode.DERIVED)
                .defaultBudget(Budget.of(1, 1))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(null)
                .zeroResultsNormal(true)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(Duration.ofDays(1825))
                .build();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public DerivedInputs inputs(ValidatedParams params) {
        return new DerivedInputs(Set.of(), Set.of(), Set.of(), Duration.ofSeconds(30), false);
    }

    /** Rebuilds the published set from the source's own live heads; the next tick is a full pass. */
    @Override
    public void reconcile(List<HeadSnapshot> ownHeads, DerivedContext ctx) {
        known.clear();
        for (HeadSnapshot head : ownHeads) {
            if (!head.tombstone() && KIND.equals(head.kind())) {
                known.add(head.externalId());
            }
        }
        watermark = null;
        if (situations != null) {
            situations.reconcile(ownHeads);
        }
        if (hypothesisSets != null) {
            hypothesisSets.reconcile(ownHeads);
        }
    }

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        if (situations != null) {
            situations.onTick(now, ctx);
        }
        if (hypothesisSets != null) {
            hypothesisSets.onTick(now, ctx);
        }
        boolean full = watermark == null;
        Set<String> seen = new HashSet<>();
        int page = 0;
        Slice<IntelligenceEventEntity> slice;
        do {
            PageRequest request = PageRequest.of(page++, PAGE);
            slice = full ? events.findAllByOrderByIdAsc(request)
                    : events.findByUpdatedAtGreaterThanEqualOrderByIdAsc(
                            watermark.minus(Duration.ofMinutes(OVERLAP_MINUTES)), request);
            for (IntelligenceEventEntity event : slice.getContent()) {
                seen.add(event.getId());
                apply(event, ctx);
            }
        } while (slice.hasNext());
        if (full) {
            // An id published earlier whose event no longer exists at all: its subject was deleted.
            for (String id : new LinkedHashSet<>(known)) {
                if (!seen.contains(id)) {
                    known.remove(id);
                    ctx.tombstone(KIND, id);
                }
            }
        }
        watermark = now;
    }

    /** Publishes a live public event, or tombstones a published one that was dismissed, merged or made private. */
    private void apply(IntelligenceEventEntity event, DerivedContext ctx) {
        boolean publish = event.getMergedInto() == null && !"dismissed".equals(event.getStatus())
                && !event.isPrivateEvent();
        if (publish) {
            ctx.emit(record(event));
            known.add(event.getId());
        } else if (known.remove(event.getId())) {
            ctx.tombstone(KIND, event.getId());
        }
    }

    /** True when {@code eventId} currently has a published head (tests). */
    boolean published(String eventId) {
        return known.contains(eventId);
    }

    private NormalizedRecord record(IntelligenceEventEntity event) {
        Set<String> families = new TreeSet<>();
        List<IntelEventMemberEntity> memberRows = members.findByEventIdOrderByIdAsc(event.getId());
        for (IntelEventMemberEntity member : memberRows) {
            if (member.getSignalFamily() != null) {
                families.add(member.getSignalFamily());
            }
        }
        java.util.Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("type", event.getType());
        payload.put("severity", event.getSeverity());
        payload.put("status", event.getStatus());
        payload.put("confidence", event.getConfidence());
        payload.put("confidenceLabel", event.getConfidenceLabel());
        payload.put("evidenceQuality", event.getEvidenceQuality());
        payload.put("memberCount", memberRows.size());
        payload.put("signalFamilies", new java.util.ArrayList<>(families));
        payload.put("title", event.getTitle());
        payload.put("private", false);
        payload.put("automated", true);
        NormalizedRecord.Builder builder = NormalizedRecord.builder(event.getId(), KIND)
                .observedAt(event.getUpdatedAt() == null ? Instant.EPOCH.plusSeconds(1) : event.getUpdatedAt())
                .putAll(payload);
        if (event.getGeom() != null) {
            builder.geometry(event.getGeom());
        }
        return builder.build();
    }
}
