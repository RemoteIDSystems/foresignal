package ai.foresignal.chronicle.network;

import ai.foresignal.chronicle.ChronicleEntity;
import ai.foresignal.chronicle.ChronicleRepository;
import ai.foresignal.chronicle.ChronicleVocabulary;
import ai.foresignal.security.Tlp;
import ai.foresignal.security.Ulids;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Discovers related chronicles from their fingerprints (prompt 134), nightly and whenever a chronicle's sketch changed
 * by more than 10%.
 *
 * <ol>
 *   <li><b>Candidates:</b> LSH banding over the MinHash signatures ({@value MinHashSketch#BANDS} bands of
 *       {@value MinHashSketch#ROWS} rows, per key type): two sketches that agree on every row of some band are a
 *       candidate pair. Locally, every eligible chronicle (at least 50 keys, not held back by churn) is compared with
 *       every other, private ones included (disclosure decides who learns of a link); across instances, each
 *       chronicle's network-salt fingerprints are compared with the unexpired sketches peers sent under the same
 *       salt.</li>
 *   <li><b>Scores:</b> per key type, the Jaccard and the containment of the smaller set. Local pairs are scored
 *       exactly from both key sets; remote pairs are estimated from the signatures and log2 size bands.</li>
 *   <li><b>Links:</b> a pair is linked when one key type reaches the minimum shared keys and a containment of 0.05
 *       ({@link LinkScores}); links that no longer pass are removed (dismissed ones are kept, so a dismissal holds).
 *       Each new link notifies the owners at their own disclosure level.</li>
 *   <li><b>Clusters:</b> connected components of medium-or-stronger active links.</li>
 * </ol>
 */
@Component
public class ChronicleDiscoveryJob {

    private static final Logger log = LoggerFactory.getLogger(ChronicleDiscoveryJob.class);

    /** LSH buckets holding more sketches than this are skipped (keys shared by nearly everyone say little). */
    static final int MAX_BUCKET = 500;

    private final ChronicleRepository chronicles;
    private final ChronicleKeyStore store;
    private final NetworkSalts salts;
    private final ChronicleRemoteSketchRepository remotes;
    private final ChronicleLinkRepository links;
    private final ChronicleClusterRepository clusters;
    private final ChronicleChurnRepository churn;
    private final ChronicleLinkViews views;
    private final ChronicleNetworkNotices notices;
    private final Clock clock;

    /** Creates the job. */
    public ChronicleDiscoveryJob(ChronicleRepository chronicles, ChronicleKeyStore store, NetworkSalts salts,
            ChronicleRemoteSketchRepository remotes, ChronicleLinkRepository links,
            ChronicleClusterRepository clusters, ChronicleChurnRepository churn, ChronicleLinkViews views,
            ChronicleNetworkNotices notices, Clock clock) {
        this.chronicles = chronicles;
        this.store = store;
        this.salts = salts;
        this.remotes = remotes;
        this.links = links;
        this.clusters = clusters;
        this.churn = churn;
        this.views = views;
        this.notices = notices;
        this.clock = clock;
    }

    /**
     * What a run did.
     *
     * @param eligible the chronicles that took part
     * @param candidates the candidate pairs scored
     * @param linksFound the new links
     * @param linksRemoved the links that no longer pass
     * @param clusters the clusters after the run
     */
    public record Result(int eligible, int candidates, int linksFound, int linksRemoved, int clusters) {}

    /** Runs only when some chronicle's sketch changed by more than 10% since its last discovery. */
    public Result runIfDue() {
        return store.anyDiscoveryDue() ? run() : new Result(0, 0, 0, 0, 0);
    }

    /** Runs discovery over every eligible chronicle and every unexpired peer sketch. */
    public synchronized Result run() {
        Instant now = clock.instant();
        int minShared = salts.minSharedKeys();
        NetworkSalts.Salt local = salts.instanceSalt();
        Map<String, ChronicleEntity> byId = new LinkedHashMap<>();
        for (ChronicleEntity c : chronicles.findByStatusInOrderByCreatedAtAscIdAsc(List.of(ChronicleVocabulary.ACTIVE,
                ChronicleVocabulary.PAUSED, ChronicleVocabulary.STOPPED, ChronicleVocabulary.ARCHIVED))) {
            byId.put(c.getId(), c);
        }
        Map<String, Long> totals = new HashMap<>();
        for (ChronicleKeyStore.State s : store.states(local.id())) {
            totals.put(s.chronicleId(), s.totalKeys());
        }
        Set<String> eligible = new TreeSet<>();
        Map<String, List<AntiProbing.Churn>> churnByOwner = new HashMap<>();
        for (ChronicleEntity c : byId.values()) {
            String owner = views.owner(c.getId());
            List<AntiProbing.Churn> ownerChurn = owner == null ? List.of() : churnByOwner.computeIfAbsent(owner,
                    o -> churn.findByOwnerAndDeletedAtAfter(o, now.minus(NetworkVocabulary.CHURN_WINDOW)).stream()
                            .map(r -> new AntiProbing.Churn(r.getCreatedAt(), r.getDeletedAt())).toList());
            if (AntiProbing.eligible(totals.getOrDefault(c.getId(), 0L), c.getCreatedAt(), ownerChurn, now)) {
                eligible.add(c.getId());
            }
        }
        Set<String> confirmed = new HashSet<>();
        List<ChronicleLinkEntity> fresh = new ArrayList<>();
        int candidates = 0;

        Map<String, Map<String, ChronicleKeyStore.Fingerprint>> localPrints = store.fingerprints(local.id());
        localPrints.keySet().retainAll(eligible);
        Set<List<String>> localPairs = candidates(localPrints, Map.of());
        for (List<String> pair : localPairs) {
            candidates++;
            String a = pair.get(0);
            String b = pair.get(1);
            Map<String, Long> shared = store.shared(a, b, local.id());
            Map<String, LinkScores.Estimate> perType = new LinkedHashMap<>();
            for (String type : NetworkVocabulary.KEY_TYPES) {
                ChronicleKeyStore.Fingerprint fa = localPrints.get(a).get(type);
                ChronicleKeyStore.Fingerprint fb = localPrints.get(b).get(type);
                if (fa == null || fb == null) {
                    continue;
                }
                perType.put(type, LinkScores.exact(shared.getOrDefault(type, 0L), fa.size(), fb.size()));
            }
            LinkScores.Decision d = LinkScores.decide(perType, minShared);
            if (!d.linked()) {
                continue;
            }
            ChronicleEntity ca = byId.get(a);
            ChronicleEntity cb = byId.get(b);
            long[] first = store.firstSeen(a, b, local.id());
            String temporal = first[0] > first[1] * 2 && first[0] > 0 ? NetworkVocabulary.A_PRECEDES_B
                    : first[1] > first[0] * 2 && first[1] > 0 ? NetworkVocabulary.B_PRECEDES_A
                            : NetworkVocabulary.CONCURRENT;
            List<String> situations = new ArrayList<>(ca.getSituationIds());
            situations.retainAll(cb.getSituationIds());
            ChronicleLinkEntity l = upsert(a, b, local.id(), d, temporal, Disclosure.timeOverlap(ca.getStartedAt(),
                    ca.getStoppedAt(), cb.getStartedAt(), cb.getStoppedAt(), now), situations, d.sharedKeys(), now,
                    fresh);
            confirmed.add(l.getId());
        }

        Set<String> evaluatedRemote = new HashSet<>();
        for (NetworkSalts.Salt network : salts.networkSalts()) {
            Map<String, Map<String, ChronicleKeyStore.Fingerprint>> prints = store.fingerprints(network.id());
            prints.keySet().retainAll(eligible);
            prints.keySet().removeIf(id -> views.effective(byId.get(id)) != DiscoverabilityLevel.NETWORK);
            Map<String, ChronicleRemoteSketchEntity> sketches = new LinkedHashMap<>();
            Map<String, Map<String, ChronicleKeyStore.Fingerprint>> remotePrints = new LinkedHashMap<>();
            for (ChronicleRemoteSketchEntity s : remotes.findBySaltIdAndExpiresAtAfterOrderByIdAsc(network.id(), now)) {
                Tlp tlp = Tlp.fromWire(s.getTlp());
                if (DiscoverabilityLevel.maxAllowed(tlp) != DiscoverabilityLevel.NETWORK) {
                    continue;
                }
                sketches.put(s.node(), s);
                evaluatedRemote.add(s.node());
                remotePrints.put(s.node(), remoteFingerprints(s));
            }
            for (List<String> pair : candidates(prints, remotePrints)) {
                candidates++;
                String a = pair.get(0);
                String b = pair.get(1);
                Map<String, LinkScores.Estimate> perType = new LinkedHashMap<>();
                for (String type : NetworkVocabulary.KEY_TYPES) {
                    ChronicleKeyStore.Fingerprint fa = prints.get(a).get(type);
                    ChronicleKeyStore.Fingerprint fb = remotePrints.get(b).get(type);
                    if (fa == null || fb == null) {
                        continue;
                    }
                    perType.put(type, LinkScores.estimate(fa.sketch(), fa.size(), fb.sketch(), fb.size()));
                }
                LinkScores.Decision d = LinkScores.decide(perType, minShared);
                if (!d.linked()) {
                    continue;
                }
                ChronicleEntity ca = byId.get(a);
                ChronicleRemoteSketchEntity s = sketches.get(b);
                Instant bFrom = IsoWeeks.start(s.getCaptureFrom());
                Instant bTo = IsoWeeks.end(s.getCaptureTo());
                String aWeek = IsoWeeks.of(ca.getStartedAt());
                String temporal = aWeek == null || aWeek.equals(s.getCaptureFrom()) ? NetworkVocabulary.CONCURRENT
                        : aWeek.compareTo(s.getCaptureFrom()) < 0 ? NetworkVocabulary.A_PRECEDES_B
                                : NetworkVocabulary.B_PRECEDES_A;
                ChronicleLinkEntity l = upsert(a, b, network.id(), d, temporal, Disclosure.timeOverlap(
                        ca.getStartedAt(), ca.getStoppedAt(), bFrom, bTo, now), List.of(), d.sharedKeys(), now, fresh);
                confirmed.add(l.getId());
            }
        }

        int removed = 0;
        List<String> gone = new ArrayList<>();
        for (ChronicleLinkEntity l : links.findByStatusOrderByIdAsc(NetworkVocabulary.ACTIVE)) {
            if (confirmed.contains(l.getId())) {
                continue;
            }
            boolean aEvaluated = eligible.contains(l.getA());
            boolean bEvaluated = l.isRemote() ? evaluatedRemote.contains(l.getB()) : eligible.contains(l.getB());
            boolean sideGone = !byId.containsKey(l.getA()) || (!l.isRemote() && !byId.containsKey(l.getB()))
                    || (l.isRemote() && views.remote(l.getB()).isEmpty());
            if ((aEvaluated && bEvaluated) || sideGone) {
                gone.add(l.getId());
                links.delete(l);
                removed++;
            }
        }
        store.forgetDisclosures(gone);
        for (String id : eligible) {
            store.discovered(id, local.id(), totals.getOrDefault(id, 0L));
        }
        int clusterCount = recomputeClusters(now);
        for (ChronicleLinkEntity l : fresh) {
            notices.linkFound(l);
        }
        log.info("Chronicle discovery: {} eligible, {} candidate pairs, {} new links, {} removed, {} clusters",
                eligible.size(), candidates, fresh.size(), removed, clusterCount);
        return new Result(eligible.size(), candidates, fresh.size(), removed, clusterCount);
    }

    /**
     * Candidate pairs by LSH banding. With {@code right} empty, pairs within {@code left} ({@code a < b}); otherwise
     * pairs of a left node and a right node ({@code [left, right]}).
     */
    static Set<List<String>> candidates(Map<String, Map<String, ChronicleKeyStore.Fingerprint>> left,
            Map<String, Map<String, ChronicleKeyStore.Fingerprint>> right) {
        Set<List<String>> out = new LinkedHashSet<>();
        boolean cross = !right.isEmpty();
        for (String type : NetworkVocabulary.KEY_TYPES) {
            Map<Long, List<String>> leftBuckets = buckets(left, type);
            Map<Long, List<String>> rightBuckets = cross ? buckets(right, type) : leftBuckets;
            for (Map.Entry<Long, List<String>> e : leftBuckets.entrySet()) {
                List<String> l = e.getValue();
                List<String> r = rightBuckets.get(e.getKey());
                if (r == null || l.size() > MAX_BUCKET || r.size() > MAX_BUCKET) {
                    continue;
                }
                for (String x : l) {
                    for (String y : r) {
                        if (cross) {
                            out.add(List.of(x, y));
                        } else if (x.compareTo(y) < 0) {
                            out.add(List.of(x, y));
                        }
                    }
                }
            }
        }
        return out;
    }

    private static Map<Long, List<String>> buckets(Map<String, Map<String, ChronicleKeyStore.Fingerprint>> prints,
            String type) {
        Map<Long, List<String>> out = new HashMap<>();
        for (Map.Entry<String, Map<String, ChronicleKeyStore.Fingerprint>> e : prints.entrySet()) {
            ChronicleKeyStore.Fingerprint f = e.getValue().get(type);
            if (f == null) {
                continue;
            }
            for (long band : f.sketch().bandKeys()) {
                List<String> bucket = out.computeIfAbsent(band, k -> new ArrayList<>());
                if (!bucket.contains(e.getKey())) {
                    bucket.add(e.getKey());
                }
            }
        }
        return out;
    }

    /** The fingerprints of a received sketch: its signatures with the sizes its log2 bands stand for. */
    static Map<String, ChronicleKeyStore.Fingerprint> remoteFingerprints(ChronicleRemoteSketchEntity s) {
        Map<String, ChronicleKeyStore.Fingerprint> out = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : s.getMinhash().entrySet()) {
            try {
                MinHashSketch sketch = MinHashSketch.fromBase64(e.getValue());
                Integer band = s.getSizeBand().get(e.getKey());
                long size = Math.round(MinHashSketch.sizeOfBand(band == null ? 0 : band));
                out.put(e.getKey(), new ChronicleKeyStore.Fingerprint(s.node(), e.getKey(), sketch, size));
            } catch (IllegalArgumentException ex) {
                // a malformed signature of one key type is left out
            }
        }
        return out;
    }

    private ChronicleLinkEntity upsert(String a, String b, String saltId, LinkScores.Decision d, String temporal,
            String overlap, List<String> situations, long shared, Instant now, List<ChronicleLinkEntity> fresh) {
        ChronicleLinkEntity l = links.findPair(a, b).orElse(null);
        boolean created = l == null;
        if (created) {
            l = new ChronicleLinkEntity(Ulids.next(now), a, b, saltId, d.strength(), now);
        }
        boolean changed = created || !d.strength().equals(l.getStrength()) || !d.dimensions().equals(l.getDimensions())
                || !d.scores().equals(l.getScores()) || l.getSharedKeyCount() != shared;
        l.setScores(d.scores());
        l.setDimensions(d.dimensions());
        l.setStrength(d.strength());
        l.setTemporal(temporal);
        l.setTimeOverlap(overlap);
        l.setSharedSituations(situations);
        l.setSharedKeyCount(shared);
        l.setSaltId(saltId);
        if (changed) {
            l.setLastUpdated(now);
        }
        l = links.save(l);
        if (created) {
            fresh.add(l);
        }
        return l;
    }

    /** Recomputes the clusters: connected components of medium-or-stronger active links, keeping ids stable. */
    int recomputeClusters(Instant now) {
        Map<String, String> parent = new HashMap<>();
        for (ChronicleLinkEntity l : links.findByStatusOrderByIdAsc(NetworkVocabulary.ACTIVE)) {
            if (NetworkVocabulary.rank(l.getStrength()) < NetworkVocabulary.rank(NetworkVocabulary.MEDIUM)) {
                continue;
            }
            union(parent, l.getA(), l.getB());
        }
        Map<String, TreeSet<String>> components = new LinkedHashMap<>();
        for (String node : new ArrayList<>(parent.keySet())) {
            components.computeIfAbsent(find(parent, node), k -> new TreeSet<>()).add(node);
        }
        List<ChronicleClusterEntity> old = clusters.findAll();
        Set<String> claimed = new HashSet<>();
        List<String> keep = new ArrayList<>();
        for (TreeSet<String> members : components.values()) {
            if (members.size() < 2) {
                continue;
            }
            ChronicleClusterEntity best = null;
            int bestOverlap = 0;
            for (ChronicleClusterEntity c : old) {
                if (claimed.contains(c.getId())) {
                    continue;
                }
                int overlap = 0;
                for (String m : c.getMembers()) {
                    if (members.contains(m)) {
                        overlap++;
                    }
                }
                if (overlap > bestOverlap) {
                    best = c;
                    bestOverlap = overlap;
                }
            }
            List<String> list = new ArrayList<>(members);
            if (best == null) {
                best = new ChronicleClusterEntity(Ulids.next(now), list, now);
            } else if (!new TreeSet<>(best.getMembers()).equals(members)) {
                best.setMembers(list, now);
            }
            claimed.add(best.getId());
            keep.add(clusters.save(best).getId());
        }
        for (ChronicleClusterEntity c : old) {
            if (!claimed.contains(c.getId())) {
                clusters.delete(c);
            }
        }
        return keep.size();
    }

    private static void union(Map<String, String> parent, String x, String y) {
        String rx = find(parent, x);
        String ry = find(parent, y);
        if (!rx.equals(ry)) {
            parent.put(rx, ry);
        }
    }

    private static String find(Map<String, String> parent, String x) {
        parent.putIfAbsent(x, x);
        String root = x;
        while (!parent.get(root).equals(root)) {
            root = parent.get(root);
        }
        String cur = x;
        while (!parent.get(cur).equals(root)) {
            String next = parent.get(cur);
            parent.put(cur, root);
            cur = next;
        }
        return root;
    }
}
