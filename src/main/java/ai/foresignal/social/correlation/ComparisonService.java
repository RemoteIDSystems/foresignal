package ai.foresignal.social.correlation;

import ai.foresignal.manifest.ManifestRequest;
import ai.foresignal.manifest.ManifestService;
import ai.foresignal.news.NewsStoryStore;
import ai.foresignal.observability.AbsenceCheck;
import ai.foresignal.observability.ObservabilityService;
import ai.foresignal.observability.Place;
import ai.foresignal.security.Ulids;
import ai.foresignal.social.SocialEvents;
import ai.foresignal.social.SocialVocabulary;
import ai.foresignal.social.narrative.NarrativeService;
import ai.foresignal.social.narrative.NarrativeStore;
import ai.foresignal.social.narrative.NarrativeViews;
import ai.foresignal.social.narrative.NarrativeVocabulary;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Runs the social–physical comparison (prompt 137) over significant narratives and keeps each comparison's revisions.
 *
 * <p>A narrative is significant when it has at least {@code social.compare.minGroups} (3) evidence groups or a
 * {@code social-velocity} finding over its scopes. Each of its asserted claims whose place is {@code locality} or finer
 * ({@code admin1} for large-area families) is compared: physical evidence from {@link PhysicalEvidenceSource},
 * detectability from 116 {@code canSeeAbsence}, the claim's media reuse, then {@link SocialPhysicalComparator}.
 * Comparisons re-evaluate as late evidence arrives until the window closes plus the family's publication lag; every
 * status change (and every change of the matching evidence) is a revision.
 *
 * <p>Each evaluation sets the claim's status (its credibility follows) and the narrative's status, emits
 * {@code comparison.updated} (and {@code narrative.updated} on a narrative status change), opens and resolves the 122
 * ledger question, updates 125 indicators, writes chronicle thread items, and sends {@code social.divergence} for a
 * fast narrative that becomes {@code unobserved} or {@code contradicted}.
 */
@Component
public class ComparisonService {

    private static final Logger log = LoggerFactory.getLogger(ComparisonService.class);

    /** The narratives re-examined per pass. */
    static final int NARRATIVE_BATCH = 500;

    /** The open comparisons re-evaluated per pass. */
    static final int OPEN_BATCH = 1_000;

    /** How far back a narrative's activity makes it a candidate. */
    static final Duration ACTIVITY = Duration.ofDays(3);

    private final NarrativeStore narratives;
    private final ComparisonStore store;
    private final PhysicalEvidenceSource evidence;
    private final ObjectProvider<ObservabilityService> observability;
    private final SocialSettingsService settings;
    private final ComparisonQuestions questions;
    private final ObjectProvider<NewsStoryStore> stories;
    private final ObjectProvider<ManifestService> manifests;
    private final SocialEvents events;
    private final SocialHypothesisBridge hypotheses;
    private final ChronicleSocialWriter chronicles;
    private final SocialNotifications notifications;
    private final Clock clock;

    /** Creates the service. */
    public ComparisonService(NarrativeStore narratives, ComparisonStore store, PhysicalEvidenceSource evidence,
            ObjectProvider<ObservabilityService> observability, SocialSettingsService settings,
            ComparisonQuestions questions, ObjectProvider<NewsStoryStore> stories,
            ObjectProvider<ManifestService> manifests, SocialEvents events, SocialHypothesisBridge hypotheses,
            ChronicleSocialWriter chronicles, SocialNotifications notifications, Clock clock) {
        this.narratives = narratives;
        this.store = store;
        this.evidence = evidence;
        this.observability = observability;
        this.settings = settings;
        this.questions = questions;
        this.stories = stories;
        this.manifests = manifests;
        this.events = events;
        this.hypotheses = hypotheses;
        this.chronicles = chronicles;
        this.notifications = notifications;
        this.clock = clock;
    }

    // ------------------------------------------------------------------------- eligibility

    /** The precision of a claim place, or null. */
    static String precision(NarrativeStore.Claim c) {
        return c.place() == null || c.place().get("precision") == null ? null : c.place().get("precision").toString();
    }

    /**
     * True when a claim is compared: asserted, with a signal family other than attention, and a place of
     * {@code point} or {@code locality} precision ({@code admin1} too for a large-area family).
     */
    public static boolean comparable(NarrativeStore.Claim c, CompareRules.FamilyRule rule) {
        if (!"asserted".equals(c.polarity()) || c.signalFamily() == null || "attention".equals(c.signalFamily())) {
            return false;
        }
        String p = precision(c);
        if ("point".equals(p) || "locality".equals(p)) {
            return true;
        }
        return "admin1".equals(p) && rule.largeArea();
    }

    /** True when a narrative is significant enough to compare. */
    boolean significant(NarrativeStore.Narrative n) {
        if (n.evidenceGroupCount() >= settings.compare().minGroups()) {
            return true;
        }
        return velocityFinding(n);
    }

    /** True when a {@code social-velocity} finding names one of the narrative's topics or places in its span. */
    boolean velocityFinding(NarrativeStore.Narrative n) {
        List<String> ids = new ArrayList<>(n.topicIds());
        for (String p : n.placeRefs()) {
            ids.add(p.startsWith("place:") ? p.substring(6) : p);
        }
        if (ids.isEmpty() || n.firstObservedAt() == null) {
            return false;
        }
        Integer count = store.jdbc().queryForObject("SELECT count(*) FROM analytic_finding WHERE finding_type = ? AND"
                + " status = 'open' AND detected_at BETWEEN ? AND ? AND stats -> 'scope' ->> 'id' = ANY(?)",
                Integer.class, SocialVocabulary.VELOCITY_FINDING,
                Timestamp.from(n.firstObservedAt().minus(Duration.ofHours(6))),
                Timestamp.from((n.lastActivityAt() == null ? n.firstObservedAt() : n.lastActivityAt())
                        .plus(Duration.ofHours(6))), ids.toArray(String[]::new));
        return count != null && count > 0;
    }

    /** The claim time: the claim's own event time when it states one, else its earliest supporting post. */
    static Instant claimTime(NarrativeStore.Claim c) {
        if (c.eventTime() != null && c.eventTime().get("from") != null) {
            try {
                return Instant.parse(c.eventTime().get("from").toString());
            } catch (RuntimeException e) {
                // fall through to the first observation
            }
        }
        return c.firstObservedAt();
    }

    // ------------------------------------------------------------------------- passes

    /**
     * Compares the significant narratives active in the last three days and re-evaluates the open comparisons.
     *
     * @return the comparisons created or revised
     */
    public int run() {
        Instant now = clock.instant();
        int changed = 0;
        for (NarrativeStore.Narrative n : narratives.activeSince(now.minus(ACTIVITY), NARRATIVE_BATCH)) {
            try {
                changed += compareNarrative(n.id());
            } catch (RuntimeException e) {
                log.warn("Comparison of narrative {} failed: {}", n.id(), e.toString());
            }
        }
        Set<String> seen = new LinkedHashSet<>();
        for (ComparisonStore.Comparison c : store.open(now, OPEN_BATCH)) {
            if (!seen.add(c.narrativeId())) {
                continue;
            }
            try {
                changed += compareNarrative(c.narrativeId());
            } catch (RuntimeException e) {
                log.warn("Re-evaluation of narrative {} failed: {}", c.narrativeId(), e.toString());
            }
        }
        return changed;
    }

    /**
     * Compares one narrative's claims (creating comparisons for newly eligible claims, re-evaluating open ones) and
     * sets its status.
     *
     * @return the comparisons created or revised
     */
    @Transactional
    public int compareNarrative(String narrativeId) {
        NarrativeStore.Narrative n = narratives.narrative(narrativeId).orElse(null);
        if (n == null || n.mergedInto() != null) {
            return 0;
        }
        Instant now = clock.instant();
        boolean significant = significant(n);
        int changed = 0;
        for (NarrativeStore.Claim claim : narratives.claims(n.id())) {
            ComparisonStore.Comparison existing = store.byClaim(claim.id()).orElse(null);
            if (existing == null && !significant) {
                continue;
            }
            CompareRules.FamilyRule rule = settings.rule(claim.signalFamily());
            if (existing == null && !comparable(claim, rule)) {
                continue;
            }
            if (existing != null && existing.isFinal(now)) {
                continue;
            }
            if (evaluate(n, claim, rule, existing, now)) {
                changed++;
            }
        }
        refreshNarrativeStatus(n, now);
        return changed;
    }

    /**
     * Evaluates one claim and stores the result.
     *
     * @return true when the comparison was created or revised
     */
    boolean evaluate(NarrativeStore.Narrative n, NarrativeStore.Claim claim, CompareRules.FamilyRule rule,
            ComparisonStore.Comparison existing, Instant now) {
        Instant t = claimTime(claim);
        Instant windowStart = existing != null ? existing.windowStart() : t.minus(rule.before());
        Instant windowEnd = existing != null ? existing.windowEnd() : t.plus(rule.after());
        Instant finalAt = windowEnd.plus(rule.publicationLag());
        Instant to = windowEnd.isAfter(now) ? now : windowEnd;
        List<PhysicalEvidenceSource.Evidence> found = to.isAfter(windowStart) ? evidence.find(
                new PhysicalEvidenceSource.Query(rule, claim.lon(), claim.lat(), claim.regionCode(), windowStart, to))
                : List.of();
        SocialPhysicalComparator.Detectability detect = detectability(claim, windowStart, windowEnd,
                !now.isBefore(windowEnd));
        String cueId = existing != null ? existing.cueId() : store.cueOfNarrative(n.id());
        String triggerRef = existing != null ? existing.triggerRef()
                : cueId == null ? null : store.cue(cueId).map(ComparisonStore.Cue::triggerRef).orElse(null);
        SocialPhysicalComparator.Decision d = SocialPhysicalComparator.decide(new SocialPhysicalComparator.Inputs(
                windowStart, windowEnd, finalAt, now, found, detect, mediaReuse(n.id(), claim.claimKey()),
                claim.firstObservedAt(), newsFirst(n), triggerRef));
        List<String> refs = d.evidence().stream().map(PhysicalEvidenceSource.Evidence::ref).toList();
        boolean changed = existing == null || !Objects.equals(existing.status(), d.status())
                || !refs.equals(refsOf(existing));
        String manifestId = existing == null ? null : existing.manifestId();
        if (changed) {
            manifestId = manifest(claim, rule, refs, windowStart, windowEnd);
        }
        List<String> platforms = new ArrayList<>();
        for (Object p : n.platforms()) {
            if (p instanceof Map<?, ?> m && m.get("platform") != null) {
                platforms.add(m.get("platform").toString());
            }
        }
        String id = existing != null ? existing.id() : Ulids.next(now);
        int revision = existing == null ? 1 : changed ? existing.revision() + 1 : existing.revision();
        ComparisonStore.Comparison c = new ComparisonStore.Comparison(id, claim.id(), n.id(), claim.type(),
                claim.signalFamily(), claim.place() == null ? Map.of("name", "", "precision", "country", "confidence",
                0) : claim.place(), claim.lon(), claim.lat(), claim.regionCode(), windowStart, windowEnd, d.status(),
                SocialPhysicalComparator.evidenceView(d.evidence()), detect.toMap(), d.leadTimes(), d.contradiction(),
                new ArrayList<>(d.caveats()), cueId == null ? CorrelationVocabulary.ORGANIC : CorrelationVocabulary.CUE,
                cueId, triggerRef, platforms, claim.evidenceGroupCount(), existing == null ? null
                : existing.questionId(), revision, finalAt, manifestId, now, existing == null ? now
                : existing.createdAt());
        if (existing == null) {
            store.insert(c);
            String questionId = questions.open(c, now);
            if (questionId != null) {
                store.setQuestion(c.id(), questionId);
                c = withQuestion(c, questionId);
            }
        } else {
            store.update(c);
        }
        String previous = existing == null ? null : existing.status();
        if (changed) {
            store.insertRevision(Ulids.next(now), new ComparisonStore.Revision(c.id(), revision, now, c.status(),
                    previous, d.reason(), refs, manifestId));
            narratives.updateClaimStatus(claim.id(), c.status(), c.id(), now);
            events.emit(CorrelationVocabulary.EVENT_COMPARISON, comparisonEvent(c, previous), false);
            hypotheses.apply(c, n.situationIds(), d.evidence(), detect);
            chronicles.comparisonChanged(n, c, previous, now);
            if (!Objects.equals(previous, c.status()) && (CorrelationVocabulary.UNOBSERVED.equals(c.status())
                    || CorrelationVocabulary.CONTRADICTED.equals(c.status())) && fast(n)) {
                notifications.sendDivergence(c, n.title(), n.velocity());
            }
        } else if (claim.comparisonId() == null) {
            narratives.updateClaimStatus(claim.id(), c.status(), c.id(), now);
        }
        if (c.isFinal(now)) {
            questions.resolve(c, refs.stream().filter(r -> !r.startsWith("record:")).toList());
        }
        return changed;
    }

    /** True when the narrative is above the velocity threshold ({@code social.velocity.minRatio}). */
    boolean fast(NarrativeStore.Narrative n) {
        Object ratio = settings.get().get("velocity") instanceof Map<?, ?> v ? v.get("minRatio") : null;
        double min = CompareRules.number(ratio, NarrativeVocabulary.EMERGING_VELOCITY);
        return n.velocity() != null && n.velocity() >= min;
    }

    private static ComparisonStore.Comparison withQuestion(ComparisonStore.Comparison c, String questionId) {
        return new ComparisonStore.Comparison(c.id(), c.claimId(), c.narrativeId(), c.claimType(), c.signalFamily(),
                c.place(), c.lon(), c.lat(), c.regionCode(), c.windowStart(), c.windowEnd(), c.status(),
                c.physicalEvidence(), c.detectability(), c.leadTimes(), c.contradiction(), c.caveats(), c.foundBy(),
                c.cueId(), c.triggerRef(), c.platforms(), c.evidenceGroupCount(), questionId, c.revision(), c.finalAt(),
                c.manifestId(), c.evaluatedAt(), c.createdAt());
    }

    static List<String> refsOf(ComparisonStore.Comparison c) {
        List<String> out = new ArrayList<>();
        for (Object o : c.physicalEvidence()) {
            if (o instanceof Map<?, ?> m && m.get("ref") != null) {
                out.add(m.get("ref").toString());
            }
        }
        return out;
    }

    /** Sets a narrative's status from its comparisons; logs and announces a change. */
    void refreshNarrativeStatus(NarrativeStore.Narrative n, Instant now) {
        List<String> statuses = new ArrayList<>();
        for (ComparisonStore.Comparison c : store.byNarrative(n.id())) {
            statuses.add(c.status());
        }
        String status = statuses.isEmpty() ? n.status() : SocialPhysicalComparator.narrativeStatus(statuses);
        if (Objects.equals(status, n.status())) {
            return;
        }
        narratives.updateStatus(n.id(), status, now);
        narratives.log(n.id(), "status-changed", "system", List.of(), n.status(), status, null, now);
        NarrativeStore.Narrative after = narratives.narrative(n.id()).orElse(n);
        events.emit(NarrativeVocabulary.EVENT_UPDATED, NarrativeService.narrativeBody(NarrativeVocabulary.EVENT_UPDATED,
                NarrativeViews.summary(after), List.of("status"), now), false);
        chronicles.narrativeChanged(after, n.status(), now);
    }

    /** Detectability of the claim's family at its place over the window ({@code canSeeAbsence} once it closed). */
    SocialPhysicalComparator.Detectability detectability(NarrativeStore.Claim claim, Instant from, Instant to,
            boolean closed) {
        ObservabilityService service = observability.getIfAvailable();
        if (service == null) {
            return SocialPhysicalComparator.Detectability.unknown("no observability service");
        }
        Place place = claim.lon() != null && claim.lat() != null
                ? new Place(claim.lon(), claim.lat(), null, claim.regionCode())
                : claim.regionCode() != null ? Place.region(claim.regionCode()) : Place.GLOBAL;
        try {
            ObservabilityService.Detectability d = service.pDetect(claim.signalFamily(), place, to, null);
            boolean ok = false;
            String reason = closed ? null : "the window is still open";
            if (closed) {
                AbsenceCheck check = service.canSeeAbsence(claim.signalFamily(), place, from, to, null);
                ok = check.ok();
                reason = check.reason();
            }
            return new SocialPhysicalComparator.Detectability(d.p(), d.lo(), d.hi(), d.methods(), ok, reason);
        } catch (RuntimeException e) {
            return SocialPhysicalComparator.Detectability.unknown("detectability unavailable: " + e.getMessage());
        }
    }

    /** The reuse finding of the media of the claim's supporting posts, or null. */
    String mediaReuse(String narrativeId, String claimKey) {
        Set<String> fingerprints = new LinkedHashSet<>();
        for (NarrativeStore.Member m : narratives.members(narrativeId)) {
            for (Object o : m.claims()) {
                if (o instanceof Map<?, ?> pc && Objects.equals(String.valueOf(pc.get("key")), claimKey)) {
                    fingerprints.addAll(m.fingerprints());
                    break;
                }
            }
        }
        for (NarrativeStore.Fingerprint fp : narratives.fingerprints(fingerprints).values()) {
            if (!fp.reuseFindingIds().isEmpty()) {
                return "finding:" + fp.reuseFindingIds().get(0);
            }
        }
        return null;
    }

    /** The earliest article of the narrative's news stories, or null. */
    Instant newsFirst(NarrativeStore.Narrative n) {
        NewsStoryStore s = stories.getIfAvailable();
        if (s == null || n.newsStoryIds().isEmpty()) {
            return null;
        }
        Instant first = null;
        for (NewsStoryStore.Story story : s.stories(n.newsStoryIds()).values()) {
            if (story.firstSeenAt() != null && (first == null || story.firstSeenAt().isBefore(first))) {
                first = story.firstSeenAt();
            }
        }
        return first;
    }

    private String manifest(NarrativeStore.Claim claim, CompareRules.FamilyRule rule, List<String> refs,
            Instant from, Instant to) {
        ManifestService service = manifests.getIfAvailable();
        if (service == null) {
            return null;
        }
        try {
            Map<String, Object> config = new LinkedHashMap<>();
            config.put("rules", CompareRules.bundled().version());
            config.put("family", rule.family());
            config.put("window", Map.of("start", from.toString(), "end", to.toString()));
            config.put("toleranceKm", rule.toleranceKm());
            config.put("publicationLag", rule.publicationLag().toString());
            List<String> inputs = new ArrayList<>();
            inputs.add("claim:" + claim.id());
            inputs.addAll(refs);
            return service.record(ManifestRequest.of(CorrelationVocabulary.COMPARATOR,
                    CorrelationVocabulary.COMPARATOR_VERSION).config(config).inputs(inputs));
        } catch (RuntimeException e) {
            log.debug("Could not record the comparison manifest of claim {}: {}", claim.id(), e.toString());
            return null;
        }
    }

    /** The {@code SocialComparisonEvent} body. */
    public static Map<String, Object> comparisonEvent(ComparisonStore.Comparison c, String previous) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("event", CorrelationVocabulary.EVENT_COMPARISON);
        body.put("comparisonId", c.id());
        body.put("claimId", c.claimId());
        body.put("narrativeId", c.narrativeId());
        body.put("status", c.status());
        body.put("previousStatus", previous);
        body.put("revision", c.revision());
        body.put("evaluatedAt", c.evaluatedAt().toString());
        return body;
    }
}
