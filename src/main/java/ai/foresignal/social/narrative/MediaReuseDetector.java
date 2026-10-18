package ai.foresignal.social.narrative;

import ai.foresignal.analytics.finding.FindingDraft;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.social.SocialEvents;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Reused media (prompt 136): a {@code media-reuse} finding (119) when a fingerprint attached to a narrative was first
 * seen at least seven days before the narrative began ({@code earlier}), or was attached to an earlier narrative in a
 * different place ({@code elsewhere}: both have regions and they do not overlap). Each finding names both narratives,
 * dates and places, and states its limit: Foresignal only knows media it has fingerprinted, so it carries the
 * {@code REFERENCE_WINDOW} caveat with the fingerprint store's start date. One finding per fingerprint and narrative;
 * each emits {@code media.reuse} on the {@code social} topic.
 */
@Component
public class MediaReuseDetector {

    private static final Logger log = LoggerFactory.getLogger(MediaReuseDetector.class);

    private final NarrativeStore store;
    private final ObjectProvider<FindingService> findings;
    private final SocialEvents events;
    private final Clock clock;

    /** Creates the detector. */
    public MediaReuseDetector(NarrativeStore store, ObjectProvider<FindingService> findings, SocialEvents events,
            Clock clock) {
        this.store = store;
        this.findings = findings;
        this.events = events;
        this.clock = clock;
    }

    /** The {@code REFERENCE_WINDOW} caveat with the store's start. */
    public static Map<String, Object> referenceWindowCaveat(Instant storeStart) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("code", NarrativeVocabulary.REFERENCE_WINDOW);
        out.put("text", "Foresignal only knows media it has fingerprinted" + (storeStart == null ? " (none yet)"
                : " since " + storeStart) + "; an image first seen here may have circulated before, and absence of a"
                + " match does not mean an image is new.");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("referenceWindowStart", storeStart == null ? null : storeStart.toString());
        out.put("data", data);
        return out;
    }

    /**
     * Why a fingerprint attached to a narrative is a reuse.
     *
     * @param reason {@code earlier} or {@code elsewhere}
     * @param earlier the earlier narrative, or null
     */
    public record Reuse(String reason, NarrativeStore.Narrative earlier) {}

    /**
     * The reuse of a fingerprint in a narrative, or null (pure apart from reading the earlier narratives).
     *
     * @param fp the fingerprint
     * @param n the narrative
     */
    public Reuse judge(NarrativeStore.Fingerprint fp, NarrativeStore.Narrative n) {
        NarrativeStore.Narrative earlier = null;
        for (NarrativeStore.Narrative other : store.narratives(fp.narrativeIds()).values()) {
            if (other.id().equals(n.id()) || !other.firstObservedAt().isBefore(n.firstObservedAt())) {
                continue;
            }
            if (earlier == null || other.firstObservedAt().isBefore(earlier.firstObservedAt())) {
                earlier = other;
            }
        }
        if (!fp.firstSeenAt().isAfter(n.firstObservedAt().minus(NarrativeVocabulary.REUSE_AGE))) {
            return new Reuse("earlier", earlier);
        }
        if (earlier != null && !earlier.regionCodes().isEmpty() && !n.regionCodes().isEmpty()) {
            Set<String> overlap = new HashSet<>(earlier.regionCodes());
            overlap.retainAll(n.regionCodes());
            if (overlap.isEmpty()) {
                return new Reuse("elsewhere", earlier);
            }
        }
        return null;
    }

    /**
     * Checks a fingerprint attached to a narrative and records a finding for a reuse.
     *
     * @return the finding id, or null
     */
    public String check(NarrativeStore.Fingerprint fp, String narrativeId) {
        FindingService service = findings.getIfAvailable();
        NarrativeStore.Narrative n = store.narrative(narrativeId).orElse(null);
        if (service == null || n == null) {
            return null;
        }
        Reuse reuse = judge(fp, n);
        if (reuse == null) {
            return null;
        }
        Instant now = clock.instant();
        try {
            FindingService.Outcome outcome = service.record(draft(fp, n, reuse, store.storeStart(), now));
            String findingId = outcome.finding().getId();
            store.addReuseFinding(fp.id(), findingId, now);
            if (outcome.change() != FindingService.Change.UNCHANGED) {
                events.emit(NarrativeVocabulary.EVENT_MEDIA_REUSE, body(findingId, fp, n, reuse, now), false);
            }
            return findingId;
        } catch (RuntimeException e) {
            log.warn("Could not record a media-reuse finding for {} in {}: {}", fp.id(), narrativeId, e.toString());
            return null;
        }
    }

    /** The finding of a reuse. */
    static FindingDraft draft(NarrativeStore.Fingerprint fp, NarrativeStore.Narrative n, Reuse reuse,
            Instant storeStart, Instant now) {
        NarrativeStore.Narrative e = reuse.earlier();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("fingerprintId", fp.id());
        stats.put("pdq", fp.pdq());
        stats.put("quality", fp.quality());
        stats.put("reason", reuse.reason());
        stats.put("firstSeenAt", fp.firstSeenAt().toString());
        stats.put("firstPostRef", fp.firstPostRef());
        stats.put("narrativeId", n.id());
        stats.put("narrativeFirstObservedAt", n.firstObservedAt().toString());
        stats.put("narrativeRegions", n.regionCodes());
        stats.put("earlierNarrativeId", e == null ? null : e.id());
        stats.put("earlierFirstObservedAt", e == null ? null : e.firstObservedAt().toString());
        stats.put("earlierRegions", e == null ? List.of() : e.regionCodes());
        stats.put("daysEarlier", Duration.between(fp.firstSeenAt(), n.firstObservedAt()).toHours() / 24.0);
        List<String> members = new ArrayList<>();
        members.add("narrative:" + n.id());
        if (e != null) {
            members.add("narrative:" + e.id());
        }
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("reuseDays", NarrativeVocabulary.REUSE_AGE.toDays());
        params.put("maxDistance", NarrativeVocabulary.PDQ_MATCH_DISTANCE);
        params.put("minQuality", NarrativeVocabulary.PDQ_MIN_QUALITY);
        String title = "earlier".equals(reuse.reason())
                ? String.format(Locale.ROOT, "Media reuse: an image in \"%s\" was first seen %s",
                        NarrativeStore.clip(n.title(), 80), fp.firstSeenAt().toString().substring(0, 10))
                : String.format(Locale.ROOT, "Media reuse: an image in \"%s\" appeared earlier elsewhere",
                        NarrativeStore.clip(n.title(), 80));
        String explanation = "earlier".equals(reuse.reason())
                ? String.format(Locale.ROOT, "A post image of narrative %s (first observed %s, regions %s) matches a"
                        + " fingerprint Foresignal first saw on %s in %s, at least %d days before the narrative began.",
                        n.id(), n.firstObservedAt(), n.regionCodes(), fp.firstSeenAt(), fp.firstPostRef(),
                        NarrativeVocabulary.REUSE_AGE.toDays())
                : String.format(Locale.ROOT, "A post image of narrative %s (regions %s) matches a fingerprint already"
                        + " attached to the earlier narrative %s (first observed %s, regions %s) in a different place.",
                        n.id(), n.regionCodes(), e.id(), e.firstObservedAt(), e.regionCodes());
        return new FindingDraft(NarrativeVocabulary.MEDIA_REUSE_FINDING, NarrativeStore.clip(title, 200), "notable",
                null, n.regionCodes(), fp.firstSeenAt(), now, now, List.of(), members, 0, stats, Map.of(),
                List.of(referenceWindowCaveat(storeStart)), NarrativeVocabulary.FINGERPRINTER,
                NarrativeVocabulary.FINGERPRINTER_VERSION, params, explanation, false, "attention", List.of(),
                List.of(), fp.id() + "/" + n.id());
    }

    /** The {@code MediaReuseEvent} body. */
    public static Map<String, Object> body(String findingId, NarrativeStore.Fingerprint fp, NarrativeStore.Narrative n,
            Reuse reuse, Instant at) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("event", NarrativeVocabulary.EVENT_MEDIA_REUSE);
        body.put("findingId", findingId);
        body.put("fingerprintId", fp.id());
        body.put("narrativeId", n.id());
        body.put("earlierNarrativeId", reuse.earlier() == null ? null : reuse.earlier().id());
        body.put("firstSeenAt", fp.firstSeenAt().toString());
        body.put("reason", reuse.reason());
        body.put("at", at.toString());
        return body;
    }
}
