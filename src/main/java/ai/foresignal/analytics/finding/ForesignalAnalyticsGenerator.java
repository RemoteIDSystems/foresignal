package ai.foresignal.analytics.finding;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.derive.DerivedGenerator;
import ai.foresignal.derive.DerivedInputs;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
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
 * {@code foresignal-analytics} (prompt 119): the DERIVED source that publishes every open, public finding of severity
 * notable or higher (also {@code info} with {@code analysis.findings.publishInfo=true}) as a record of kind
 * {@value #KIND}, {@code externalId} = the finding id, so ordinary 017 rules can alert on findings, SSE, tiles and
 * search carry them, and the 118 situation linker reads them from the change bus.
 *
 * <p>Payload: {@code findingType}, {@code severity}, {@code status}, {@code title}, {@code regionCodes},
 * {@code seriesRefs}, {@code signalFamily}, {@code stats} (raw values of {@code LOCAL_ONLY} series never: the record is
 * {@code OPEN} and may leave on sinks and federation, so they are always removed), {@code baseRate}, {@code caveats}
 * (codes), {@code evidenceRefs} (record members), {@code private: false} and {@code automated: true}. Emitted on create
 * and on change only; tombstoned on dismissal or supersession, or when the finding turns private or is deleted.
 * Analytic retention class {@code intel} (1825 days).
 */
@Component
public class ForesignalAnalyticsGenerator implements DerivedGenerator {

    /** The source id. */
    public static final String ID = "foresignal-analytics";

    /** The record kind. */
    public static final String KIND = "analytic-finding";

    /** Findings read per page. */
    static final int PAGE = 500;

    /** The overlap of an incremental pass, minutes. */
    static final int OVERLAP_MINUTES = 5;

    private final SourceDescriptor descriptor = buildDescriptor();
    private final AnalyticFindingRepository findings;
    private final AppSettingService settings;
    /** The ids with a live head, and the payload hash last emitted (null when unknown since a reconcile). */
    private final Map<String, Integer> published = new HashMap<>();
    private Instant watermark;

    /** Creates the generator. */
    public ForesignalAnalyticsGenerator(AnalyticFindingRepository findings, AppSettingService settings) {
        this.findings = findings;
        this.settings = settings;
    }

    /** The descriptor (analytic retention class {@code intel}: 1825 days). */
    public static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("Analytic findings (derived)")
                .description("Change points, pre-event signals, unexplained moves and co-occurrences found by"
                        + " Foresignal's discovery detectors (prompt 119), each with its base rate and caveats, as"
                        + " ordinary records so rules, SSE, tiles and search use them unchanged. Private findings are"
                        + " not published.")
                .kinds(KIND)
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
    public synchronized void reconcile(List<HeadSnapshot> ownHeads, DerivedContext ctx) {
        published.clear();
        for (HeadSnapshot head : ownHeads) {
            if (!head.tombstone() && KIND.equals(head.kind())) {
                published.put(head.externalId(), null);
            }
        }
        watermark = null;
    }

    @Override
    public synchronized void onTick(Instant now, DerivedContext ctx) {
        boolean full = watermark == null;
        boolean publishInfo = publishInfo();
        Set<String> seen = new HashSet<>();
        int page = 0;
        Slice<AnalyticFindingEntity> slice;
        do {
            PageRequest request = PageRequest.of(page++, PAGE);
            slice = full ? findings.findAllByOrderByIdAsc(request)
                    : findings.findByUpdatedAtGreaterThanEqualOrderByIdAsc(
                            watermark.minus(Duration.ofMinutes(OVERLAP_MINUTES)), request);
            for (AnalyticFindingEntity f : slice.getContent()) {
                seen.add(f.getId());
                apply(f, publishInfo, ctx);
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

    /** True when the finding should have a live record. */
    static boolean publishable(AnalyticFindingEntity f, boolean publishInfo) {
        return "open".equals(f.getStatus()) && !f.isPrivateFinding()
                && (publishInfo || !FindingSeverities.INFO.equals(f.getSeverity()));
    }

    private void apply(AnalyticFindingEntity f, boolean publishInfo, DerivedContext ctx) {
        if (publishable(f, publishInfo)) {
            Map<String, Object> payload = payload(f);
            int hash = payload.hashCode() * 31 + (f.getGeom() == null ? 0 : f.getGeom().toText().hashCode());
            Integer last = published.get(f.getId());
            if (last == null || last != hash) {
                ctx.emit(record(f, payload));
                published.put(f.getId(), hash);
            }
        } else if (published.containsKey(f.getId())) {
            published.remove(f.getId());
            ctx.tombstone(KIND, f.getId());
        }
    }

    /** True when the finding currently has a published record (tests). */
    synchronized boolean isPublished(String id) {
        return published.containsKey(id);
    }

    private boolean publishInfo() {
        try {
            return settings.get(SettingKeys.ANALYSIS_FINDINGS_PUBLISH_INFO, Boolean.class).orElse(false);
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** The record payload of a finding; raw values of {@code LOCAL_ONLY} series are always left out. */
    static Map<String, Object> payload(AnalyticFindingEntity f) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("findingType", f.getFindingType());
        payload.put("severity", f.getSeverity());
        payload.put("status", f.getStatus());
        payload.put("title", f.getTitle());
        payload.put("regionCodes", new ArrayList<>(f.getRegionCodes()));
        payload.put("seriesRefs", new ArrayList<>(f.getSeriesRefs()));
        payload.put("signalFamily", f.getSignalFamily());
        payload.put("stats", FindingViews.stats(f, !f.getLocalOnlySources().isEmpty()));
        payload.put("baseRate", f.getBaseRate());
        List<String> codes = new ArrayList<>();
        for (Map<String, Object> caveat : f.getCaveats()) {
            if (caveat.get("code") != null) {
                codes.add(caveat.get("code").toString());
            }
        }
        payload.put("caveats", codes);
        List<String> evidence = new ArrayList<>();
        for (String member : f.getMembers()) {
            if (member.startsWith("record:")) {
                evidence.add(member);
            }
        }
        payload.put("evidenceRefs", evidence);
        payload.put("detectedAt", f.getDetectedAt().toString());
        payload.put("private", false);
        payload.put("automated", true);
        return payload;
    }

    /** The record of a finding. */
    static NormalizedRecord record(AnalyticFindingEntity f, Map<String, Object> payload) {
        Instant observed = f.getUpdatedAt() == null ? Instant.EPOCH.plusSeconds(1) : f.getUpdatedAt();
        NormalizedRecord.Builder builder = NormalizedRecord.builder(f.getId(), KIND).observedAt(observed)
                .putAll(payload);
        if (f.getGeom() != null) {
            builder.geometry(f.getGeom());
        }
        return builder.build();
    }
}
