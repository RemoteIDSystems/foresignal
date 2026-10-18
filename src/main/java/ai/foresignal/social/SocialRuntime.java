package ai.foresignal.social;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * What a social platform adapter needs beyond its {@code FetchContext} (prompt 135), in one bean so adapters depend on
 * one thing: the collector, the operator's platform settings, the money and quota ledgers, the stream ledger, the
 * volume store (for counts a platform reports itself) and the stored posts (for deletion sync). Every member except
 * the collector may be null in a hand-built test runtime; adapters treat a null member as "not available".
 */
@Component
public class SocialRuntime {

    private final SocialCollector collector;
    private final SocialPlatformService platforms;
    private final XSpend spend;
    private final QuotaLedger quotas;
    private final StreamLedger ledger;
    private final SocialVolumeStore volume;
    private final StoredPosts stored;

    /** Creates the runtime. */
    @Autowired
    public SocialRuntime(SocialCollector collector, SocialPlatformService platforms, XSpend spend,
            QuotaLedger quotas, StreamLedger ledger, SocialVolumeStore volume, StoredPosts stored) {
        this.collector = collector;
        this.platforms = platforms;
        this.spend = spend;
        this.quotas = quotas;
        this.ledger = ledger;
        this.volume = volume;
        this.stored = stored;
    }

    /** The collector. */
    public SocialCollector collector() {
        return collector;
    }

    /** The current term index. */
    public TermIndex index() {
        return collector.index();
    }

    /** The settings of an adapter; the catalog defaults when the service is absent or fails. */
    public SocialPlatformSettings settings(String platformId) {
        if (platforms != null) {
            try {
                return platforms.settings(platformId);
            } catch (RuntimeException e) {
                // fall through to the defaults
            }
        }
        return SocialPlatformCatalog.get(platformId).map(SocialPlatformSettings::defaults)
                .orElseGet(() -> new SocialPlatformSettings(platformId, false, null, null, false,
                        SocialVocabulary.DEFAULT_POST_RETENTION, null, null, null, null));
    }

    /** The money ledger, or null. */
    public XSpend spend() {
        return spend;
    }

    /** The quota ledger, or null. */
    public QuotaLedger quotas() {
        return quotas;
    }

    /** The stream ledger, or null. */
    public StreamLedger ledger() {
        return ledger;
    }

    /** The volume store, or null. */
    public SocialVolumeStore volume() {
        return volume;
    }

    /** The stored posts, or null. */
    public StoredPosts stored() {
        return stored;
    }

    private CueRequests cues;

    /** Installs the 137 cue hand-off (absent in hand-built test runtimes unless set). */
    @Autowired(required = false)
    public void setCues(CueRequests cues) {
        this.cues = cues;
    }

    /** The 137 cue hand-off, or null. */
    public CueRequests cues() {
        return cues;
    }

    /**
     * Drains an adapter's cue requests (prompt 137).
     *
     * @param sourceId the adapter
     * @return the requests, empty without the hand-off
     */
    public java.util.List<CueRequests.Request> drainCues(String sourceId) {
        return cues == null ? java.util.List.of() : cues.drain(sourceId);
    }

    /** Reports a cue request's result (no-op without the hand-off). */
    public void reportCue(CueRequests.Result result) {
        if (cues != null) {
            cues.report(result);
        }
    }

    /** Records that deletion sync ran (no-op without the service). */
    public void deletionRan(String platformId, Instant at) {
        if (platforms != null) {
            try {
                platforms.deletionRan(platformId, at);
            } catch (RuntimeException e) {
                // the view shows the previous run
            }
        }
    }
}
