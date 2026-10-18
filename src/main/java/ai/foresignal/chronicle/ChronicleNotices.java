package ai.foresignal.chronicle;

import ai.foresignal.collab.AccessResolver;
import ai.foresignal.collab.MeEvents;
import ai.foresignal.collab.PersonalNotifier;
import ai.foresignal.collab.ShareableObjectEntity;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * The chronicle notifications (prompt 131, 018 event types delivered personally through 130's
 * {@link PersonalNotifier}): {@code chronicle.proposed} when the analysis cycles propose a chronicle,
 * {@code chronicle.cap_warning} at 90% of the cap and again when the cap is reached, and {@code chronicle.gap} when
 * capture fell behind global retention. Each goes to the chronicle's owner: an inbox item (category
 * {@code chronicle}) and the owner's personal channels whose preferences carry {@code chronicle}, labeled with the
 * chronicle's TLP.
 */
@Component
public class ChronicleNotices {

    private static final Logger log = LoggerFactory.getLogger(ChronicleNotices.class);

    private final ObjectProvider<PersonalNotifier> notifier;
    private final AccessResolver access;

    /** Creates the component. */
    public ChronicleNotices(ObjectProvider<PersonalNotifier> notifier, AccessResolver access) {
        this.notifier = notifier;
        this.access = access;
    }

    /** {@code chronicle.proposed} to the owner. */
    public void proposed(ChronicleEntity c, String rationale) {
        send(c, MeEvents.CHRONICLE_PROPOSED, "Chronicle proposed: " + c.getName(),
                (rationale == null ? "" : rationale + "\n\n")
                        + "Accept it with POST /v1/chronicles/" + c.getId() + "/activate, edit it, or delete it.");
    }

    /** {@code chronicle.cap_warning} to the owner. */
    public void capWarning(ChronicleEntity c, long bytesUsed, long cap, boolean exceeded) {
        String title = (exceeded ? "Chronicle at its size cap: " : "Chronicle at 90% of its size cap: ") + c.getName();
        String body = "It holds " + bytesUsed + " of " + cap + " bytes."
                + (exceeded ? " Full and downsampled layers now capture events only; raise the cap to restore them."
                        : " At the cap, full and downsampled layers switch to events-only.");
        send(c, MeEvents.CHRONICLE_CAP_WARNING, title, body);
    }

    /** {@code chronicle.gap} to the owner. */
    public void gap(ChronicleEntity c, String from, String to) {
        send(c, MeEvents.CHRONICLE_GAP, "Chronicle capture gap: " + c.getName(),
                "Global retention removed versions before capture reached them, between " + from + " and " + to
                        + ". The thread records the gap.");
    }

    private void send(ChronicleEntity c, String event, String title, String body) {
        PersonalNotifier n = notifier.getIfAvailable();
        if (n == null) {
            return;
        }
        try {
            Optional<ShareableObjectEntity> rec = access.record("chronicle:" + c.getId());
            if (rec.isEmpty()) {
                return;
            }
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("chronicleId", c.getId());
            payload.put("displayId", c.getDisplayId());
            payload.put("status", c.getStatus());
            n.send(new PersonalNotifier.Notice(rec.get().getOwner(), event, title, body, "chronicle:" + c.getId(), null,
                    rec.get().getTlp(), payload));
        } catch (RuntimeException e) {
            log.warn("Could not send {} for chronicle {}: {}", event, c.getId(), e.toString());
        }
    }
}
