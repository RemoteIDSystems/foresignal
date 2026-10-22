package ai.foresignal.social.narrative;

import ai.foresignal.media.outlet.Consensus;
import ai.foresignal.media.outlet.MediaVocabulary;
import ai.foresignal.media.outlet.OutletRegistry;
import ai.foresignal.news.NewsReliability;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Folds outlet reputation into social-claim scoring (prompt 153). When a claim's source maps to a rated outlet — a
 * channel or account key that is a resolvable web domain — this consults the {@link OutletRegistry} consensus for the
 * outlet's factuality, partisan lean and state-affiliation, mirroring how {@link NewsReliability} grades news, and
 * reports both the reputation (for the claim view) and the reputation-adjusted reliability grade
 * ({@link SocialEvidenceGrades#withReputation}).
 *
 * <p>Social accounts without a resolvable domain (the common case) map to no outlet and leave the track-record grade
 * unchanged; the lever applies wherever a source does resolve to a rated outlet.
 */
@Component
public class ClaimEnrichment {

    private final ObjectProvider<OutletRegistry> outlets;
    private final ObjectProvider<NewsReliability> news;

    /** Creates the helper. */
    public ClaimEnrichment(ObjectProvider<OutletRegistry> outlets, ObjectProvider<NewsReliability> news) {
        this.outlets = outlets;
        this.news = news;
    }

    /**
     * An outlet's reputation as it bears on a claim.
     *
     * @param outletId the rated outlet
     * @param factualityGrade the factuality grade (A–F)
     * @param leanBucket left / center / right / unrated
     * @param stateAffiliated whether the outlet is state media
     */
    public record Reputation(String outletId, String factualityGrade, String leanBucket, boolean stateAffiliated) {

        /** The {@code reputation} member of the claim view, or null when no outlet resolved. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("outletId", outletId);
            out.put("factuality", factualityGrade);
            out.put("lean", leanBucket);
            out.put("stateAffiliated", stateAffiliated);
            return out;
        }
    }

    /** The reputation of a claim's source, or null when it maps to no rated outlet. */
    public Reputation reputation(String sourceKey) {
        OutletRegistry registry = outlets.getIfAvailable();
        if (registry == null) {
            return null;
        }
        String domain = domainOf(sourceKey);
        if (domain == null) {
            return null;
        }
        OutletRegistry.Outlet outlet = registry.byDomain(domain).orElse(null);
        if (outlet == null) {
            return null;
        }
        Map<String, Consensus.Result> consensus = registry.consensus(outlet.id());
        Consensus.Result lean = consensus.get(MediaVocabulary.LEAN);
        Consensus.Result state = consensus.get(MediaVocabulary.STATE_AFFILIATION);
        boolean stateAffiliated = MediaVocabulary.STATE_AFFILIATED.equals(outlet.type())
                || (state != null && state.median() != null && state.median() >= 0.5);
        NewsReliability reliability = news.getIfAvailable();
        String factualityGrade = reliability == null ? "F" : reliability.gradeForOutlet(outlet.id());
        return new Reputation(outlet.id(), factualityGrade, lean == null ? null : lean.bucket(), stateAffiliated);
    }

    /** The reliability grade after reputation, or the base grade unchanged when no outlet resolved. */
    public String adjustedReliability(String base, Reputation reputation) {
        if (reputation == null) {
            return base;
        }
        return SocialEvidenceGrades.withReputation(base, reputation.factualityGrade(), reputation.leanBucket(),
                reputation.stateAffiliated());
    }

    /** The domain-like tail of a source key ({@code account|channel:platform:tail}), or null when it is not a domain. */
    static String domainOf(String sourceKey) {
        if (sourceKey == null) {
            return null;
        }
        int lastColon = sourceKey.lastIndexOf(':');
        String tail = lastColon < 0 ? sourceKey : sourceKey.substring(lastColon + 1);
        tail = tail.strip().toLowerCase(java.util.Locale.ROOT);
        // A domain has a dot, no spaces, and a plausible TLD; anything else is an ordinary handle with no outlet.
        if (tail.contains(" ") || !tail.matches("^[a-z0-9.-]+\\.[a-z]{2,}$")) {
            return null;
        }
        return tail;
    }
}
