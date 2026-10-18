package ai.foresignal.chronicle.network;

import ai.foresignal.chronicle.ChronicleRepository;
import ai.foresignal.collab.MeEvents;
import ai.foresignal.collab.PersonalNotifier;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.security.Scope;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Personal notifications of the chronicle network (prompt 134), on the owner's inbox, {@code me} SSE topic and
 * personal channels: {@code chronicle.link.found} to the owner of each local chronicle of a new link, at that owner's
 * disclosure level (a new opaque link counts against the daily cap; beyond it the owner is not told);
 * {@code chronicle.access.requested} to the owner of the requested chronicle, with only the requester's handle and
 * message; {@code chronicle.access.granted} or {@code chronicle.access.declined} to the requester, with only the
 * status.
 */
@Component
public class ChronicleNetworkNotices {

    private static final Logger log = LoggerFactory.getLogger(ChronicleNetworkNotices.class);

    private final ObjectProvider<PersonalNotifier> notifier;
    private final ChronicleLinkViews views;
    private final ChronicleRepository chronicles;
    private final Clock clock;

    /** Creates the component. */
    public ChronicleNetworkNotices(ObjectProvider<PersonalNotifier> notifier, ChronicleLinkViews views,
            ChronicleRepository chronicles, Clock clock) {
        this.notifier = notifier;
        this.views = views;
        this.chronicles = chronicles;
        this.clock = clock;
    }

    /** A caller standing for a principal (read scope, no admin override, no private sources). */
    static ObjectCaller as(String principalId) {
        return new ObjectCaller(principalId, principalId, Scope.READ, false, Set.of());
    }

    /** Tells the owners of a new link's local chronicles, each at its own disclosure level. */
    public void linkFound(ChronicleLinkEntity link) {
        for (String side : new String[] {link.getA(), link.getB()}) {
            if (NetworkVocabulary.isRemote(side)) {
                continue;
            }
            String owner = views.owner(side);
            if (owner == null) {
                continue;
            }
            try {
                ObjectCaller caller = as(owner);
                Disclosure.Level level = views.level(link, side, caller);
                Optional<Map<String, Object>> view = level == Disclosure.Level.FULL
                        ? Optional.of(views.fullFrom(link, side, caller)) : views.view(link, side, caller, true);
                if (view.isEmpty()) {
                    continue;
                }
                String name = chronicles.findById(side).map(c -> c.getDisplayId() + " " + c.getName()).orElse(side);
                String title = "A related chronicle was found for " + name;
                String body = level == Disclosure.Level.FULL ? "Both chronicles are visible to you; open the link"
                        + " for the shared canonical events and entities."
                        : "A related thread exists (" + view.get().get("strength") + " overlap). You can ask its owner"
                                + " for access with POST /v1/chronicle-network/links/" + link.getId() + "/request-access.";
                send(owner, MeEvents.CHRONICLE_LINK_FOUND, title, body, "chronicle:" + side, views.tlp(side)
                        .wireValue(), MeEvents.chronicleLinkFound(side, view.get(), clock.instant()));
            } catch (RuntimeException e) {
                log.warn("Could not notify the owner of {} about link {}: {}", side, link.getId(), e.toString());
            }
        }
    }

    /** Tells a chronicle's owner about an access request: the requester's handle and message only. */
    public void requested(ChronicleAccessRequestEntity r) {
        if (r.getOwnerPrincipal() == null || r.getTargetChronicleId() == null) {
            return;
        }
        String who = r.getRequesterHandle() + (r.getRequesterInstance() == null ? "" : "@" + r.getRequesterInstance());
        String name = chronicles.findById(r.getTargetChronicleId()).map(c -> c.getDisplayId() + " " + c.getName())
                .orElse(r.getTargetChronicleId());
        Instant now = clock.instant();
        send(r.getOwnerPrincipal(), MeEvents.CHRONICLE_ACCESS_REQUESTED, who + " asks for access to " + name,
                (r.getMessage() == null ? "" : r.getMessage() + "\n\n") + "Grant with POST /v1/chronicle-network/"
                        + "requests/" + r.getId() + "/grant or decline with /decline.",
                "chronicle:" + r.getTargetChronicleId(), views.tlp(r.getTargetChronicleId()).wireValue(),
                MeEvents.chronicleAccess(r.getId(), r.getLinkId(), r.getStatus(), who, r.getMessage(),
                        r.getTargetChronicleId(), now));
    }

    /**
     * Tells a local requester the owner's decision (the status only).
     *
     * @param r the request
     * @param grantedChronicle the chronicle now shared with the requester (a local grant), or null
     */
    public void decided(ChronicleAccessRequestEntity r, String grantedChronicle) {
        if (r.getRequesterPrincipal() == null) {
            return;
        }
        boolean granted = NetworkVocabulary.GRANTED.equals(r.getStatus());
        String event = granted ? MeEvents.CHRONICLE_ACCESS_GRANTED : MeEvents.CHRONICLE_ACCESS_DECLINED;
        String title = granted ? "Your access request was granted" : "Your access request was declined";
        send(r.getRequesterPrincipal(), event, title, granted && grantedChronicle == null
                ? "The owner sent the material as a signed package; find it under /v1/exchange/received." : null,
                grantedChronicle == null ? null : "chronicle:" + grantedChronicle, null,
                MeEvents.chronicleAccess(r.getId(), r.getLinkId(), r.getStatus(), null, null, grantedChronicle,
                        clock.instant()));
    }

    private void send(String principal, String event, String title, String body, String ref, String tlp,
            Map<String, Object> payload) {
        PersonalNotifier n = notifier.getIfAvailable();
        if (n == null) {
            return;
        }
        n.send(new PersonalNotifier.Notice(principal, event, title, body, ref, null, tlp, payload));
    }
}
