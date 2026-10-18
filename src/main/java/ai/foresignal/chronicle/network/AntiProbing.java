package ai.foresignal.chronicle.network;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;

/**
 * The anti-probing rules of the chronicle network (prompt 134). Pure.
 *
 * <ul>
 *   <li>Discovery runs only for chronicles that have captured at least {@value NetworkVocabulary#MIN_KEYS_FOR_DISCOVERY}
 *       global evidence keys, so nobody can probe with a handful of chosen keys.</li>
 *   <li>A principal is shown at most {@value NetworkVocabulary#OPAQUE_LINKS_PER_DAY} new opaque links per day; links
 *       already shown stay visible.</li>
 *   <li>Churn does not buy fresh discovery: when an owner deleted a chronicle less than 24 h after creating it, within
 *       the last 24 h, that owner's chronicles younger than 24 h wait until they are 24 h old.</li>
 * </ul>
 */
public final class AntiProbing {

    private AntiProbing() {}

    /**
     * One deleted chronicle of an owner.
     *
     * @param createdAt when the deleted chronicle was created
     * @param deletedAt when it was deleted
     */
    public record Churn(Instant createdAt, Instant deletedAt) {}

    /** True when an owner's churn holds back discovery for its young chronicles. */
    public static boolean churnHold(Collection<Churn> churn, Instant now) {
        if (churn == null) {
            return false;
        }
        for (Churn c : churn) {
            if (c.deletedAt() == null || c.createdAt() == null) {
                continue;
            }
            boolean recent = Duration.between(c.deletedAt(), now).compareTo(NetworkVocabulary.CHURN_WINDOW) < 0;
            boolean quick = Duration.between(c.createdAt(), c.deletedAt()).compareTo(NetworkVocabulary.CHURN_WINDOW) < 0;
            if (recent && quick) {
                return true;
            }
        }
        return false;
    }

    /**
     * True when a chronicle takes part in discovery.
     *
     * @param totalKeys the global evidence keys it captured, all key types together
     * @param createdAt when it was created
     * @param ownerChurn the owner's recent deletions
     * @param now the current time
     */
    public static boolean eligible(long totalKeys, Instant createdAt, Collection<Churn> ownerChurn, Instant now) {
        if (totalKeys < NetworkVocabulary.MIN_KEYS_FOR_DISCOVERY) {
            return false;
        }
        boolean young = createdAt != null
                && Duration.between(createdAt, now).compareTo(NetworkVocabulary.CHURN_WINDOW) < 0;
        return !(young && churnHold(ownerChurn, now));
    }

    /** True when a principal shown {@code disclosedLastDay} new opaque links in the last 24 h may be shown another. */
    public static boolean mayDiscloseNew(long disclosedLastDay) {
        return disclosedLastDay < NetworkVocabulary.OPAQUE_LINKS_PER_DAY;
    }

    /** True when a principal that sent {@code sentLastDay} access requests in the last 24 h may send another. */
    public static boolean mayRequest(long sentLastDay) {
        return sentLastDay < NetworkVocabulary.REQUESTS_PER_DAY;
    }
}
