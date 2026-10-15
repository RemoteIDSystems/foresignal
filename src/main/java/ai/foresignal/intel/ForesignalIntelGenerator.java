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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;

/**
 * {@code foresignal-intel} (prompt 101): publishes every intelligence event as a record of kind
 * {@code intelligence-event}, externalId = the event id, so SSE changes, tiles, search and ordinary
 * 017 rules (for example {@code kind=intelligence-event}, {@code severity == critical}, {@code
 * enter}) use them unchanged. Emit only on change; tombstone a dismissed or merged event.
 */
@Component
public class ForesignalIntelGenerator implements DerivedGenerator {

    /** The source id. */
    public static final String ID = "foresignal-intel";

    /** The record kind. */
    public static final String KIND = "intelligence-event";

    private final SourceDescriptor descriptor = buildDescriptor();
    private final IntelligenceEventRepository events;
    private final IntelEventMemberRepository members;
    private final Set<String> known = new LinkedHashSet<>();

    /** Creates the generator. */
    public ForesignalIntelGenerator(IntelligenceEventRepository events, IntelEventMemberRepository members) {
        this.events = events;
        this.members = members;
    }

    /** The descriptor. */
    public static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("Intelligence events (derived)")
                .description("Typed, located, time-bounded intelligence findings and their members, as ordinary"
                        + " records so rules, SSE, tiles and search use them unchanged.")
                .kind(KIND)
                .attribution("Derived by Foresignal from stored versions")
                .licence("derived")
                .mode(Mode.DERIVED)
                .defaultBudget(Budget.of(1, 1))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(null)
                .zeroResultsNormal(true)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(Duration.ofDays(730))
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

    @Override
    public void reconcile(List<HeadSnapshot> ownHeads, DerivedContext ctx) {
        known.clear();
    }

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        Set<String> current = new LinkedHashSet<>();
        for (IntelligenceEventEntity event : events.findAllByOrderByUpdatedAtDescIdDesc(Limit.of(5_000))) {
            boolean live = event.getMergedInto() == null && !"dismissed".equals(event.getStatus());
            if (!live) {
                if (known.contains(event.getId())) {
                    ctx.tombstone(KIND, event.getId());
                }
                continue;
            }
            current.add(event.getId());
            ctx.emit(record(event));
        }
        for (String gone : new LinkedHashSet<>(known)) {
            if (!current.contains(gone)) {
                ctx.tombstone(KIND, gone);
            }
        }
        known.clear();
        known.addAll(current);
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
        payload.put("private", event.isPrivateEvent());
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
