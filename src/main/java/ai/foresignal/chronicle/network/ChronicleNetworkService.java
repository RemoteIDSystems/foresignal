package ai.foresignal.chronicle.network;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.chronicle.ChronicleEntity;
import ai.foresignal.chronicle.ChronicleEventEntity;
import ai.foresignal.chronicle.ChronicleEventRepository;
import ai.foresignal.chronicle.ChronicleLifecycleListener;
import ai.foresignal.chronicle.ChronicleRepository;
import ai.foresignal.chronicle.ChronicleService;
import ai.foresignal.collab.AccessAction;
import ai.foresignal.collab.AclRole;
import ai.foresignal.collab.CollabInputs;
import ai.foresignal.collab.MeEvents;
import ai.foresignal.collab.PersonalNotifier;
import ai.foresignal.collab.TlpChangeListener;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.security.Tlp;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

/**
 * The chronicle network's API side (prompt 134): related chronicles at the caller's disclosure level, the owner's
 * discoverability setting, dismissing links, clusters and their combined timeline, and network salts. It also keeps
 * network state in step with the rest of the system: a stricter TLP label lowers discoverability (logged, owner
 * notified), a stopped chronicle's sketch is refreshed, and a deleted chronicle's keys, sketches and links are
 * forgotten (and remembered for the churn rule).
 */
@Service
public class ChronicleNetworkService implements TlpChangeListener, ChronicleLifecycleListener {

    private static final Logger log = LoggerFactory.getLogger(ChronicleNetworkService.class);

    private static final Set<String> DISCOVERABILITY_FIELDS = Set.of("discoverability", "revealTitle", "revealOwner",
            "maxAllowed", "tlp", "updatedAt");

    /** The most evidence keys and thread items a timeline reads per request. */
    static final int TIMELINE_READ = 5_000;

    private final ChronicleService chronicleService;
    private final ChronicleRepository chronicles;
    private final ChronicleLinkRepository links;
    private final ChronicleClusterRepository clusters;
    private final ChronicleEventRepository events;
    private final ChronicleChurnRepository churn;
    private final ChronicleLinkViews views;
    private final ChronicleKeyStore store;
    private final NetworkSalts salts;
    private final ChronicleSketcher sketcher;
    private final FederatedSketches federated;
    private final ObjectProvider<PersonalNotifier> notifier;
    private final Clock clock;

    /** Creates the service. */
    public ChronicleNetworkService(ChronicleService chronicleService, ChronicleRepository chronicles,
            ChronicleLinkRepository links, ChronicleClusterRepository clusters, ChronicleEventRepository events,
            ChronicleChurnRepository churn, ChronicleLinkViews views, ChronicleKeyStore store, NetworkSalts salts,
            ChronicleSketcher sketcher, FederatedSketches federated, ObjectProvider<PersonalNotifier> notifier,
            Clock clock) {
        this.chronicleService = chronicleService;
        this.chronicles = chronicles;
        this.links = links;
        this.clusters = clusters;
        this.events = events;
        this.churn = churn;
        this.views = views;
        this.store = store;
        this.salts = salts;
        this.sketcher = sketcher;
        this.federated = federated;
        this.notifier = notifier;
        this.clock = clock;
    }

    // ------------------------------------------------------------------------- related

    /**
     * The links of a chronicle at the caller's disclosure level ({@code listRelatedChronicles}).
     *
     * @param chronicleId the chronicle (id or display id)
     * @param caller a viewer of it
     * @param minStrength the weakest strength to include, or null
     * @param includeDismissed true to include dismissed links
     * @return the {@code ChronicleRelatedPage}
     */
    public Map<String, Object> related(String chronicleId, ObjectCaller caller, String minStrength,
            boolean includeDismissed, String cursor, int limit) {
        ChronicleEntity c = chronicleService.require(chronicleId, caller, AccessAction.VIEW);
        int min = minStrength == null ? 0 : NetworkVocabulary.rank(minStrength);
        List<Map<String, Object>> all = new ArrayList<>();
        for (ChronicleLinkEntity l : links.findBySide(c.getId())) {
            if (!includeDismissed && !l.isActive()) {
                continue;
            }
            if (NetworkVocabulary.rank(l.getStrength()) < min) {
                continue;
            }
            Disclosure.Level level = views.level(l, c.getId(), caller);
            if (level == Disclosure.Level.FULL) {
                all.add(views.fullFrom(l, c.getId(), caller));
            } else {
                views.view(l, c.getId(), caller, true).ifPresent(all::add);
            }
        }
        return page(all, cursor, limit);
    }

    // ------------------------------------------------------------------------- discoverability

    /**
     * Sets a chronicle's discoverability and reveal flags ({@code setChronicleDiscoverability}, owner).
     *
     * @param chronicleId the chronicle
     * @param body the {@code Discoverability}
     * @param caller the owner
     * @return the effective {@code Discoverability}
     * @throws ForesignalException {@code 404 UNKNOWN_CHRONICLE}, {@code 403 FORBIDDEN} for a non-owner,
     *     {@code 409 DISCOVERABILITY_NOT_ALLOWED} with the TLP rule
     */
    public Map<String, Object> setDiscoverability(String chronicleId, JsonNode body, ObjectCaller caller) {
        ChronicleEntity c = chronicleService.require(chronicleId, caller, AccessAction.VIEW);
        if (views.role(caller, c.getId()) != AclRole.OWNER) {
            throw new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN, "Only the chronicle's owner sets"
                    + " its discoverability.");
        }
        CollabInputs.object(body, "Discoverability", DISCOVERABILITY_FIELDS);
        DiscoverabilityLevel level = DiscoverabilityLevel.parse("discoverability",
                CollabInputs.text(body, "discoverability", 1, 16));
        Tlp tlp = views.tlp(c.getId());
        DiscoverabilityLevel.check(level, tlp);
        DiscoverabilityLevel before = views.effective(c);
        c.setDiscoverability(level.wireValue());
        if (CollabInputs.has(body, "revealTitle")) {
            c.setRevealTitle(CollabInputs.bool(body, "revealTitle", false));
        }
        if (CollabInputs.has(body, "revealOwner")) {
            c.setRevealOwner(CollabInputs.bool(body, "revealOwner", false));
        }
        c.setDiscoverabilityUpdatedAt(clock.instant());
        ChronicleEntity saved = chronicles.save(c);
        if (before == DiscoverabilityLevel.NETWORK && level != DiscoverabilityLevel.NETWORK) {
            dropNetworkKeys(saved.getId());
        }
        return discoverability(saved);
    }

    /** The {@code Discoverability} view: the effective setting, the reveal flags, the TLP cap and the label. */
    public Map<String, Object> discoverability(ChronicleEntity c) {
        Tlp tlp = views.tlp(c.getId());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("discoverability", views.effective(c).wireValue());
        out.put("revealTitle", c.isRevealTitle());
        out.put("revealOwner", c.isRevealOwner());
        out.put("maxAllowed", DiscoverabilityLevel.maxAllowed(tlp).wireValue());
        out.put("tlp", tlp.wireValue());
        out.put("updatedAt", c.getDiscoverabilityUpdatedAt() == null ? null
                : c.getDiscoverabilityUpdatedAt().toString());
        return out;
    }

    private void dropNetworkKeys(String chronicleId) {
        for (NetworkSalts.Salt s : salts.networkSalts()) {
            store.dropChronicleSalt(chronicleId, s.id());
        }
    }

    // ------------------------------------------------------------------------- dismiss

    /**
     * Dismisses a link ({@code dismissChronicleLink}): the owner of either local side.
     *
     * @throws NotFoundException {@code 404 UNKNOWN_LINK} when the caller owns neither side
     */
    public void dismiss(String linkId, ObjectCaller caller) {
        ChronicleLinkEntity l = links.findById(linkId == null ? "" : linkId)
                .orElseThrow(() -> ChronicleAccessService.unknownLink(linkId));
        boolean owner = false;
        for (String side : List.of(l.getA(), l.getB())) {
            if (!NetworkVocabulary.isRemote(side) && views.role(caller, side) == AclRole.OWNER) {
                owner = true;
            }
        }
        if (!owner) {
            throw ChronicleAccessService.unknownLink(linkId);
        }
        if (l.isActive()) {
            l.dismiss(caller.principal(), clock.instant());
            links.save(l);
        }
    }

    // ------------------------------------------------------------------------- clusters

    /**
     * The clusters holding a chronicle the caller can see ({@code listChronicleClusters}).
     *
     * @return the {@code ChronicleClusterPage}
     */
    public Map<String, Object> clusters(ObjectCaller caller, String cursor, int limit) {
        List<Map<String, Object>> all = new ArrayList<>();
        for (ChronicleClusterEntity c : clusters.findAllByOrderByUpdatedAtDescIdAsc()) {
            clusterView(c, caller).ifPresent(all::add);
        }
        return page(all, cursor, limit);
    }

    /**
     * One cluster at the caller's disclosure level ({@code getChronicleCluster}).
     *
     * @throws NotFoundException {@code 404 UNKNOWN_CLUSTER}
     */
    public Map<String, Object> cluster(String id, ObjectCaller caller) {
        ChronicleClusterEntity c = clusters.findById(id == null ? "" : id).orElseThrow(() -> unknownCluster(id));
        return clusterView(c, caller).orElseThrow(() -> unknownCluster(id));
    }

    /** {@code 404 UNKNOWN_CLUSTER}. */
    static NotFoundException unknownCluster(String id) {
        return new NotFoundException(ErrorCodes.UNKNOWN_CLUSTER, "No chronicle cluster '" + id + "' holds a chronicle"
                + " you can see.");
    }

    /**
     * A {@code ChronicleCluster} as far as the caller's disclosure allows: chronicles it can see in full; others only
     * when they are directly linked to one it can see and discoverable to it (opaque, within the daily cap); the rest
     * left out and not counted. Empty when the caller sees no member.
     */
    Optional<Map<String, Object>> clusterView(ChronicleClusterEntity cluster, ObjectCaller caller) {
        List<String> visible = visibleMembers(cluster, caller);
        if (visible.isEmpty()) {
            return Optional.empty();
        }
        Set<String> memberSet = new HashSet<>(cluster.getMembers());
        List<ChronicleLinkEntity> clusterLinks = links.findBySides(visible).stream()
                .filter(l -> l.isActive() && memberSet.contains(l.getA()) && memberSet.contains(l.getB())).toList();
        List<Map<String, Object>> members = new ArrayList<>();
        int opaque = 0;
        for (String m : cluster.getMembers()) {
            if (visible.contains(m)) {
                Map<String, Object> v = new LinkedHashMap<>();
                v.put("disclosure", NetworkVocabulary.FULL);
                v.put("chronicleId", m);
                v.put("envelope", views.envelope("chronicle:" + m, caller));
                v.put("linkId", null);
                members.add(v);
                continue;
            }
            for (ChronicleLinkEntity l : clusterLinks) {
                String other = l.other(m);
                if (other == null || !visible.contains(other)) {
                    continue;
                }
                if (views.level(l, other, caller) == Disclosure.Level.OPAQUE
                        && views.allowOpaque(caller, l.getId(), true)) {
                    Map<String, Object> v = new LinkedHashMap<>();
                    v.put("disclosure", NetworkVocabulary.OPAQUE);
                    v.put("chronicleId", null);
                    v.put("envelope", null);
                    v.put("linkId", l.getId());
                    v.put("strength", l.getStrength());
                    members.add(v);
                    opaque++;
                    break;
                }
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", cluster.getId());
        out.put("members", members);
        out.put("visibleMembers", visible.size());
        out.put("opaqueMembers", opaque);
        out.put("updatedAt", cluster.getUpdatedAt().toString());
        return Optional.of(out);
    }

    private List<String> visibleMembers(ChronicleClusterEntity cluster, ObjectCaller caller) {
        List<String> out = new ArrayList<>();
        for (String m : cluster.getMembers()) {
            if (!NetworkVocabulary.isRemote(m) && chronicles.existsById(m) && views.sees(caller, m)) {
                out.add(m);
            }
        }
        return out;
    }

    // ------------------------------------------------------------------------- timeline

    /**
     * The combined timeline of a cluster ({@code getChronicleClusterTimeline}): the visible members' captured evidence
     * and thread items, deduplicated by global evidence key (thread items about the same object count once), each
     * listing which visible chronicles hold it. Opaque members contribute nothing.
     *
     * @param clusterId the cluster
     * @param caller the viewer
     * @param from only items at or after this, or null
     * @param to only items before this, or null
     * @return the {@code ClusterTimelinePage}
     */
    public Map<String, Object> timeline(String clusterId, ObjectCaller caller, Instant from, Instant to, String cursor,
            int limit) {
        ChronicleClusterEntity cluster = clusters.findById(clusterId == null ? "" : clusterId)
                .orElseThrow(() -> unknownCluster(clusterId));
        List<String> visible = visibleMembers(cluster, caller);
        if (visible.isEmpty()) {
            throw unknownCluster(clusterId);
        }
        Map<String, Item> merged = new LinkedHashMap<>();
        String salt = salts.instanceSalt().id();
        for (ChronicleKeyStore.HeldKey k : store.heldEvidence(visible, salt, from, to, TIMELINE_READ)) {
            String key = k.keyText() == null ? "hkey:" + Long.toHexString(k.hkey()) : k.keyText();
            merged.put(key, new Item(key, k.at(), "record", title(k.keyText()), k.ref(), new ArrayList<>(k.heldBy())));
        }
        for (String chronicleId : visible) {
            int read = 0;
            for (ChronicleEventEntity e : events.findByChronicleIdOrderByIdAsc(chronicleId)) {
                if (read++ >= TIMELINE_READ) {
                    break;
                }
                if (e.isPrivateItem() && !caller.seesPrivate()) {
                    continue;
                }
                if ((from != null && e.getAt().isBefore(from)) || (to != null && !e.getAt().isBefore(to))) {
                    continue;
                }
                String key = e.getRef() == null ? "item:" + chronicleId + ":" + e.getId()
                        : "item:" + e.getType() + ":" + e.getRef();
                Item existing = merged.get(key);
                if (existing == null) {
                    List<String> held = new ArrayList<>();
                    held.add(chronicleId);
                    merged.put(key, new Item(key, e.getAt(), e.getType(), e.getTitle(), e.getRef(), held));
                } else if (!existing.heldBy().contains(chronicleId)) {
                    existing.heldBy().add(chronicleId);
                    if (e.getAt().isBefore(existing.at())) {
                        merged.put(key, new Item(key, e.getAt(), existing.type(), existing.title(), existing.ref(),
                                existing.heldBy()));
                    }
                }
            }
        }
        List<Item> sorted = new ArrayList<>(merged.values());
        sorted.sort(Comparator.comparing(Item::at).thenComparing(Item::key));
        int offset = CollabInputs.offset(cursor);
        List<Map<String, Object>> items = new ArrayList<>();
        for (Item i : sorted.subList(Math.min(offset, sorted.size()), Math.min(offset + limit, sorted.size()))) {
            Map<String, Object> v = new LinkedHashMap<>();
            v.put("evidenceKey", i.key());
            v.put("at", i.at().toString());
            v.put("type", i.type());
            v.put("title", i.title());
            v.put("ref", i.ref());
            v.put("envelope", i.ref() == null ? null : views.envelope(i.ref(), caller));
            List<String> held = new ArrayList<>(i.heldBy());
            held.sort(null);
            v.put("heldBy", held);
            items.add(v);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", offset + limit < sorted.size() ? CollabInputs.cursor(offset + limit) : null);
        return out;
    }

    /** One merged timeline item. */
    private record Item(String key, Instant at, String type, String title, String ref, List<String> heldBy) {}

    /** The title of an evidence item: its family and place or upstream id, from the key text. */
    static String title(String keyText) {
        if (keyText == null) {
            return "Captured evidence";
        }
        String[] parts = keyText.split(":", 4);
        if (keyText.startsWith("ev:") && parts.length == 4) {
            return parts[1] + " event " + parts[3] + " (" + parts[2] + ")";
        }
        if (keyText.startsWith("cell:") && parts.length >= 3) {
            return parts[1] + " event near H3 " + parts[2];
        }
        return keyText;
    }

    // ------------------------------------------------------------------------- salts

    /**
     * Installs a network salt ({@code setChronicleNetworkSalt}, admin): the network's previous salt and the sketches
     * peers sent under it are forgotten, so linkage restarts under the new one with new sketch ids.
     *
     * @return the {@code NetworkSaltStatus}
     */
    public Map<String, Object> installSalt(JsonNode body) {
        NetworkSalts.Installed installed = salts.install(body);
        for (String old : installed.replaced()) {
            store.dropSalt(old);
        }
        federated.forgetNetwork(String.valueOf(installed.status().get("networkId")));
        return installed.status();
    }

    // ------------------------------------------------------------------------- listeners

    @Override
    public void tlpChanged(String ref, Tlp from, Tlp to, String actor) {
        if (ref == null || !ref.startsWith("chronicle:") || to == null) {
            return;
        }
        chronicles.findById(ref.substring("chronicle:".length())).ifPresent(c -> {
            DiscoverabilityLevel setting = DiscoverabilityLevel.fromWire(c.getDiscoverability());
            DiscoverabilityLevel max = DiscoverabilityLevel.maxAllowed(to);
            if (setting.ordinal() <= max.ordinal()) {
                return;
            }
            c.setDiscoverability(max.wireValue());
            c.setDiscoverabilityUpdatedAt(clock.instant());
            chronicles.save(c);
            if (setting == DiscoverabilityLevel.NETWORK) {
                dropNetworkKeys(c.getId());
            }
            log.info("Chronicle {} discoverability lowered from {} to {} by TLP:{} ({})", c.getId(),
                    setting.wireValue(), max.wireValue(), to.wireValue(), actor);
            String owner = views.owner(c.getId());
            PersonalNotifier n = notifier.getIfAvailable();
            if (owner != null && n != null) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("chronicleId", c.getId());
                payload.put("from", setting.wireValue());
                payload.put("to", max.wireValue());
                payload.put("tlp", to.wireValue());
                n.send(new PersonalNotifier.Notice(owner, MeEvents.CHRONICLE_DISCOVERABILITY_LOWERED,
                        "Discoverability of " + c.getDisplayId() + " lowered to " + max.wireValue(),
                        DiscoverabilityLevel.rule(to) + ".", ref, actor, to.wireValue(), payload));
            }
        });
    }

    @Override
    public void stopped(ChronicleEntity chronicle) {
        try {
            sketcher.refresh(chronicles.findById(chronicle.getId()).orElse(chronicle));
        } catch (RuntimeException e) {
            log.warn("Sketching stopped chronicle {} failed: {}", chronicle.getId(), e.toString());
        }
    }

    @Override
    public void deleted(ChronicleEntity chronicle, String owner) {
        String id = chronicle.getId();
        List<String> gone = links.findBySide(id).stream().map(ChronicleLinkEntity::getId).toList();
        links.deleteBySide(id);
        store.forgetDisclosures(gone);
        store.forget(id);
        Instant now = clock.instant();
        for (ChronicleClusterEntity c : clusters.findAll()) {
            if (c.getMembers().contains(id)) {
                List<String> members = new ArrayList<>(c.getMembers());
                members.remove(id);
                if (members.size() < 2) {
                    clusters.delete(c);
                } else {
                    c.setMembers(members, now);
                    clusters.save(c);
                }
            }
        }
        if (owner != null) {
            churn.save(new ChronicleChurnEntity(owner, chronicle.getCreatedAt(), now));
        }
        churn.deleteByDeletedAtBefore(now.minus(NetworkVocabulary.CHURN_WINDOW.multipliedBy(2)));
    }

    // ------------------------------------------------------------------------- helpers

    private static Map<String, Object> page(List<Map<String, Object>> all, String cursor, int limit) {
        int offset = CollabInputs.offset(cursor);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", new ArrayList<>(all.subList(Math.min(offset, all.size()), Math.min(offset + limit,
                all.size()))));
        out.put("nextCursor", offset + limit < all.size() ? CollabInputs.cursor(offset + limit) : null);
        return out;
    }

    /** Validates a {@code minStrength} query value. */
    public static String minStrength(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (!NetworkVocabulary.STRENGTHS.contains(value)) {
            throw ValidationException.forField("minStrength", "must be low, medium or high");
        }
        return value;
    }
}
