package ai.foresignal.chronicle.network;

import ai.foresignal.chronicle.ChronicleEntity;
import ai.foresignal.chronicle.ChronicleRepository;
import ai.foresignal.chronicle.ChronicleService;
import ai.foresignal.collab.AccessResolver;
import ai.foresignal.collab.AclRole;
import ai.foresignal.collab.ShareableObjectEntity;
import ai.foresignal.entity.EntityKeyRepository;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectService;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.Tlp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Renders chronicle links at the disclosure level a viewer is allowed (prompt 134): {@code ChronicleLink} (full) when
 * the viewer can see both chronicles, {@code OpaqueChronicleLink} when the other chronicle is discoverable to the
 * viewer, nothing otherwise ({@link Disclosure}). New opaque links count against the viewer's daily cap of
 * {@value NetworkVocabulary#OPAQUE_LINKS_PER_DAY}; a link shown once stays visible.
 */
@Component
public class ChronicleLinkViews {

    private final ChronicleRepository chronicles;
    private final ChronicleRemoteSketchRepository remotes;
    private final ChronicleAccessRequestRepository requests;
    private final ChronicleKeyStore store;
    private final NetworkSalts salts;
    private final AccessResolver access;
    private final ObjectService objects;
    private final PrincipalService principals;
    private final EntityKeyRepository entityKeys;
    private final Clock clock;

    /** Creates the component. */
    public ChronicleLinkViews(ChronicleRepository chronicles, ChronicleRemoteSketchRepository remotes,
            ChronicleAccessRequestRepository requests, ChronicleKeyStore store, NetworkSalts salts,
            AccessResolver access, ObjectService objects, PrincipalService principals, EntityKeyRepository entityKeys,
            Clock clock) {
        this.chronicles = chronicles;
        this.remotes = remotes;
        this.requests = requests;
        this.store = store;
        this.salts = salts;
        this.access = access;
        this.objects = objects;
        this.principals = principals;
        this.entityKeys = entityKeys;
        this.clock = clock;
    }

    /** True when the caller holds a role on a local chronicle. */
    public boolean sees(ObjectCaller caller, String chronicleId) {
        return access.role(caller, ChronicleService.ref(chronicleId)) != null;
    }

    /** The effective discoverability of a local chronicle (its setting capped by its TLP label). */
    public DiscoverabilityLevel effective(ChronicleEntity c) {
        return DiscoverabilityLevel.cap(DiscoverabilityLevel.fromWire(c.getDiscoverability()), tlp(c.getId()));
    }

    /** The TLP label of a local chronicle. */
    public Tlp tlp(String chronicleId) {
        return access.record(ChronicleService.ref(chronicleId)).map(r -> Tlp.fromWire(r.getTlp()))
                .orElse(access.defaultTlp());
    }

    /** The owner principal of a local chronicle, or null. */
    public String owner(String chronicleId) {
        return access.record(ChronicleService.ref(chronicleId)).map(ShareableObjectEntity::getOwner).orElse(null);
    }

    /**
     * The disclosure level of the other side of a link for a viewer who can see {@code ownSide}.
     *
     * @return the level ({@link Disclosure.Level#NONE} when the other side is gone or not discoverable)
     */
    public Disclosure.Level level(ChronicleLinkEntity link, String ownSide, ObjectCaller caller) {
        String other = link.other(ownSide);
        if (other == null) {
            return Disclosure.Level.NONE;
        }
        if (NetworkVocabulary.isRemote(other)) {
            Optional<ChronicleRemoteSketchEntity> sketch = remote(other);
            boolean usable = sketch.isPresent() && salts.federated()
                    && sketch.get().getExpiresAt().isAfter(clock.instant())
                    && salts.networkSalt(sketch.get().getNetworkId()).map(s -> s.id().equals(sketch.get().getSaltId()))
                            .orElse(false);
            return usable ? Disclosure.Level.OPAQUE : Disclosure.Level.NONE;
        }
        Optional<ChronicleEntity> c = chronicles.findById(other);
        if (c.isEmpty()) {
            return Disclosure.Level.NONE;
        }
        return Disclosure.level(sees(caller, other), false, effective(c.get()));
    }

    /**
     * The view of a link from one of the viewer's chronicles, or empty when nothing may be disclosed (including a new
     * opaque link beyond the viewer's daily cap).
     *
     * @param link the link
     * @param ownSide the viewer's chronicle in the link
     * @param caller the viewer
     * @param record true to count a newly shown opaque link against the daily cap
     * @return the {@code ChronicleLink} or {@code OpaqueChronicleLink}
     */
    public Optional<Map<String, Object>> view(ChronicleLinkEntity link, String ownSide, ObjectCaller caller,
            boolean record) {
        Disclosure.Level level = level(link, ownSide, caller);
        if (level == Disclosure.Level.FULL) {
            return Optional.of(full(link, caller));
        }
        if (level == Disclosure.Level.NONE || !allowOpaque(caller, link.getId(), record)) {
            return Optional.empty();
        }
        return Optional.of(opaque(link, ownSide, caller));
    }

    /**
     * True when an opaque link may be shown: already shown to this principal, or within its daily cap of new ones (and
     * then recorded when {@code record}). An admin or a caller without a principal is not capped.
     */
    public boolean allowOpaque(ObjectCaller caller, String linkId, boolean record) {
        String principal = caller.principal();
        if (principal == null || caller.admin()) {
            return true;
        }
        if (!store.disclosed(principal, List.of(linkId)).isEmpty()) {
            return true;
        }
        Instant now = clock.instant();
        if (!AntiProbing.mayDiscloseNew(store.disclosedSince(principal, now.minus(Duration.ofDays(1))))) {
            return false;
        }
        if (record) {
            store.disclose(principal, linkId, now);
        }
        return true;
    }

    /** A {@code ChronicleLink} (full disclosure; both sides local and visible). */
    public Map<String, Object> full(ChronicleLinkEntity link, ObjectCaller caller) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("linkId", link.getId());
        out.put("disclosure", NetworkVocabulary.FULL);
        out.put("a", link.getA());
        out.put("b", link.getB());
        out.put("other", envelope("chronicle:" + link.getB(), caller));
        out.put("strength", link.getStrength());
        out.put("dimensions", link.getDimensions());
        out.put("scores", link.getScores());
        out.put("temporal", link.getTemporal());
        out.put("timeOverlap", link.getTimeOverlap());
        out.put("sharedSituations", link.getSharedSituations());
        String salt = salts.instanceSalt().id();
        out.put("sharedCanonicalEvents", store.sharedRefs(link.getA(), link.getB(), salt, NetworkVocabulary.EVIDENCE,
                NetworkVocabulary.MAX_SHARED_LISTED));
        out.put("sharedEntities", entities(store.sharedRefs(link.getA(), link.getB(), salt,
                NetworkVocabulary.ENTITIES, NetworkVocabulary.MAX_SHARED_LISTED)));
        out.put("sharedKeyCount", link.getSharedKeyCount());
        out.put("status", link.getStatus());
        out.put("firstFound", link.getFirstFound().toString());
        out.put("lastUpdated", link.getLastUpdated().toString());
        return out;
    }

    /** A full view seen from {@code ownSide}: the envelope is of the other side. */
    public Map<String, Object> fullFrom(ChronicleLinkEntity link, String ownSide, ObjectCaller caller) {
        Map<String, Object> out = full(link, caller);
        String other = link.other(ownSide);
        out.put("other", other == null ? null : envelope("chronicle:" + other, caller));
        return out;
    }

    /** An {@code OpaqueChronicleLink} seen from {@code ownSide}. */
    public Map<String, Object> opaque(ChronicleLinkEntity link, String ownSide, ObjectCaller caller) {
        String other = link.other(ownSide);
        String title = null;
        String ownerHandle = null;
        boolean remote = NetworkVocabulary.isRemote(other);
        if (remote) {
            Optional<ChronicleRemoteSketchEntity> sketch = remote(other);
            title = sketch.map(ChronicleRemoteSketchEntity::getTitle).orElse(null);
            ownerHandle = sketch.map(ChronicleRemoteSketchEntity::getOwnerHandle).orElse(null);
        } else if (other != null) {
            ChronicleEntity c = chronicles.findById(other).orElse(null);
            if (c != null && c.isRevealTitle()) {
                title = c.getDisplayId() + " " + c.getName();
            }
            if (c != null && c.isRevealOwner()) {
                ownerHandle = principals.find(owner(c.getId())).map(PrincipalEntity::getHandle).orElse(null);
            }
        }
        return Disclosure.opaque(link.getId(), ownSide, link.getStrength(), link.getDimensions(),
                link.getTimeOverlap(), remote, ownerHandle, title, requestStatus(link.getId(), caller));
    }

    /** The viewer's latest access request status on a link, or null. */
    String requestStatus(String linkId, ObjectCaller caller) {
        if (caller.principal() == null) {
            return null;
        }
        List<ChronicleAccessRequestEntity> mine = requests.findByLinkIdAndRequesterPrincipalOrderByCreatedAtDescIdDesc(
                linkId, caller.principal());
        return mine.isEmpty() ? null : mine.get(0).getStatus();
    }

    /** The received sketch behind a remote node. */
    public Optional<ChronicleRemoteSketchEntity> remote(String node) {
        if (!NetworkVocabulary.isRemote(node)) {
            return Optional.empty();
        }
        String rest = node.substring(NetworkVocabulary.REMOTE_PREFIX.length());
        int slash = rest.indexOf("/chronicle:");
        if (slash <= 0) {
            return Optional.empty();
        }
        return remotes.findByInstanceIdAndSketchId(rest.substring(0, slash),
                rest.substring(slash + "/chronicle:".length()));
    }

    /** The envelope of a reference as the caller resolves it, or null. */
    public Object envelope(String ref, ObjectCaller caller) {
        try {
            return objects.find(ObjectRef.parse(ref), new ResolveContext(caller, null)).orElse(null);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** Entity references ({@code entity:{id}}) for entity keys ({@code {keyType}:{value}}) the registry holds open. */
    List<String> entities(List<String> keys) {
        List<String> out = new ArrayList<>();
        for (String k : keys) {
            int colon = k.indexOf(':');
            if (colon <= 0) {
                continue;
            }
            entityKeys.findOpen(k.substring(0, colon), k.substring(colon + 1)).ifPresent(row -> {
                String ref = "entity:" + row.getEntityId();
                if (!out.contains(ref)) {
                    out.add(ref);
                }
            });
        }
        return out;
    }

    /** The role a caller holds on a local chronicle, or null. */
    public AclRole role(ObjectCaller caller, String chronicleId) {
        return access.role(caller, ChronicleService.ref(chronicleId));
    }
}
