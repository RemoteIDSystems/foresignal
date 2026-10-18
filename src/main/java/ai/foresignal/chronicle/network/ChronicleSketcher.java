package ai.foresignal.chronicle.network;

import ai.foresignal.chronicle.ChronicleEntity;
import ai.foresignal.chronicle.ChronicleRepository;
import ai.foresignal.chronicle.ChronicleService;
import ai.foresignal.chronicle.ChronicleVocabulary;
import ai.foresignal.collab.AccessResolver;
import ai.foresignal.security.Tlp;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Keeps each chronicle's keyed key sets and MinHash fingerprints up to date (prompt 134): under the instance salt for
 * every chronicle that captured something, and under each current network salt for chronicles whose effective
 * discoverability is {@code network}. It reads only what was captured since the last pass (a per-salt seq position),
 * so a pass is incremental, and runs at most hourly plus once when capture stops.
 *
 * <p>A pass marks discovery due when the key total moved by more than 10% since the last discovery, or a fingerprint's
 * slots changed by more than 10%.
 */
@Component
public class ChronicleSketcher {

    private static final Logger log = LoggerFactory.getLogger(ChronicleSketcher.class);

    /** Captured rows read per chunk. */
    static final int CHUNK = 5_000;

    /** The most chunks per chronicle and salt in one pass; the rest waits for the next pass. */
    static final int MAX_CHUNKS = 40;

    private final ChronicleRepository chronicles;
    private final ChronicleKeyExtractor extractor;
    private final ChronicleKeyStore store;
    private final NetworkSalts salts;
    private final AccessResolver access;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    /** Creates the sketcher. */
    public ChronicleSketcher(ChronicleRepository chronicles, ChronicleKeyExtractor extractor, ChronicleKeyStore store,
            NetworkSalts salts, AccessResolver access, Clock clock) {
        this.chronicles = chronicles;
        this.extractor = extractor;
        this.store = store;
        this.salts = salts;
        this.access = access;
        this.clock = clock;
    }

    /** The effective discoverability of a chronicle (its setting capped by its TLP label). */
    public DiscoverabilityLevel effective(ChronicleEntity c) {
        Tlp tlp = access.record(ChronicleService.ref(c.getId())).map(r -> Tlp.fromWire(r.getTlp()))
                .orElse(access.defaultTlp());
        return DiscoverabilityLevel.cap(DiscoverabilityLevel.fromWire(c.getDiscoverability()), tlp);
    }

    /**
     * Refreshes every chronicle that has captured or may capture (every status but proposed), and drops the state of
     * rotated or stale salts first.
     *
     * @return how many chronicle passes changed a fingerprint
     */
    public int refreshAll() {
        dropStaleSalts();
        int changed = 0;
        for (ChronicleEntity c : chronicles.findByStatusInOrderByCreatedAtAscIdAsc(List.of(ChronicleVocabulary.ACTIVE,
                ChronicleVocabulary.PAUSED, ChronicleVocabulary.STOPPED, ChronicleVocabulary.ARCHIVED))) {
            try {
                changed += refresh(c);
            } catch (RuntimeException e) {
                log.warn("Sketching chronicle {} failed: {}", c.getId(), e.toString());
            }
        }
        return changed;
    }

    /** Drops the key sets and fingerprints of retired instance salts and stale network salts. */
    public void dropStaleSalts() {
        for (String id : salts.retiredInstanceSalts()) {
            store.dropSalt(id);
        }
        for (String id : salts.staleNetworkSalts()) {
            store.dropSalt(id);
        }
    }

    /**
     * Refreshes one chronicle under the instance salt and, when it is network-discoverable, under every current
     * network salt; a chronicle no longer network-discoverable loses its network key sets (its sketch id stays, so
     * the next batch withdraws it).
     *
     * @return how many salts' fingerprints changed
     */
    public int refresh(ChronicleEntity c) {
        if (ChronicleVocabulary.PROPOSED.equals(c.getStatus())) {
            return 0;
        }
        int changed = 0;
        NetworkSalts.Salt local = salts.instanceSalt();
        if (refreshUnder(c, local)) {
            changed++;
        }
        boolean network = effective(c) == DiscoverabilityLevel.NETWORK;
        for (NetworkSalts.Salt s : salts.networkSalts()) {
            if (network) {
                if (refreshUnder(c, s)) {
                    changed++;
                }
            } else {
                store.dropChronicleSalt(c.getId(), s.id());
            }
        }
        return changed;
    }

    /**
     * Turns what a chronicle captured since the last pass under one salt into keyed keys and updates its fingerprints.
     *
     * @return true when a fingerprint changed
     */
    boolean refreshUnder(ChronicleEntity c, NetworkSalts.Salt salt) {
        String sketchId = salt.network() ? HexFormat.of().formatHex(randomBytes(16)) : null;
        ChronicleKeyStore.State state = store.state(c.getId(), salt.id(), sketchId);
        KeyedHasher hasher = salt.hasher();
        Map<String, Map<Long, ChronicleKeyStore.KeyRow>> byType = new LinkedHashMap<>();
        add(byType, extractor.linkedTopics(c), hasher, !salt.network());
        long seq = state.keySeq();
        for (int i = 0; i < MAX_CHUNKS; i++) {
            ChronicleKeyExtractor.Batch batch = extractor.read(c, seq, CHUNK);
            if (batch.rows() == 0) {
                break;
            }
            add(byType, batch.keys(), hasher, !salt.network());
            seq = batch.maxSeq();
            if (batch.rows() < CHUNK) {
                break;
            }
        }
        Instant now = clock.instant();
        boolean changed = false;
        double maxChange = 0;
        for (Map.Entry<String, Map<Long, ChronicleKeyStore.KeyRow>> e : byType.entrySet()) {
            if (e.getValue().isEmpty()) {
                continue;
            }
            store.insertKeys(c.getId(), salt.id(), e.getValue().values());
        }
        Map<String, Long> sizes = store.sizes(c.getId(), salt.id());
        long total = 0;
        for (String type : NetworkVocabulary.KEY_TYPES) {
            long size = sizes.getOrDefault(type, 0L);
            total += size;
            Map<Long, ChronicleKeyStore.KeyRow> fresh = byType.getOrDefault(type, Map.of());
            ChronicleKeyStore.Fingerprint old = store.fingerprint(c.getId(), type, salt.id()).orElse(null);
            if (fresh.isEmpty() && old != null && old.size() == size) {
                continue;
            }
            if (fresh.isEmpty() && old == null) {
                continue;
            }
            MinHashSketch before = old == null ? MinHashSketch.empty() : old.sketch();
            MinHashSketch after = before.with(fresh.keySet());
            if (!after.equals(before) || old == null || old.size() != size) {
                store.upsertFingerprint(c.getId(), type, salt.id(), after, size, now);
                changed = true;
                maxChange = Math.max(maxChange, after.changedShare(before));
            }
        }
        boolean due = discoveryDue(state.discoveredKeys(), total, maxChange);
        store.sketched(c.getId(), salt.id(), seq, total, due, now);
        return changed;
    }

    /**
     * True when a sketch moved enough to run discovery before the nightly run: the key total changed by more than 10%
     * since the last discovery (or reached the discovery minimum for the first time), or a fingerprint's slots changed
     * by more than 10%.
     */
    static boolean discoveryDue(long discoveredKeys, long total, double maxSlotChange) {
        if (total < NetworkVocabulary.MIN_KEYS_FOR_DISCOVERY) {
            return false;
        }
        if (discoveredKeys <= 0) {
            return true;
        }
        double moved = Math.abs(total - discoveredKeys) / (double) discoveredKeys;
        return moved > NetworkVocabulary.CHANGE_TRIGGER || maxSlotChange > NetworkVocabulary.CHANGE_TRIGGER;
    }

    private static void add(Map<String, Map<Long, ChronicleKeyStore.KeyRow>> byType,
            List<ChronicleKeyExtractor.RawKey> keys, KeyedHasher hasher, boolean local) {
        Map<String, Long> hashes = new HashMap<>();
        for (ChronicleKeyExtractor.RawKey k : keys) {
            if (k.at() == null) {
                continue;
            }
            long h = hashes.computeIfAbsent(k.text(), hasher::hash);
            Map<Long, ChronicleKeyStore.KeyRow> rows = byType.computeIfAbsent(k.keyType(), t -> new LinkedHashMap<>());
            ChronicleKeyStore.KeyRow existing = rows.get(h);
            if (existing == null || k.at().isBefore(existing.firstSeen())) {
                rows.put(h, new ChronicleKeyStore.KeyRow(k.keyType(), h, k.at(), local ? k.text() : null,
                        local ? k.ref() : null));
            }
        }
    }

    private byte[] randomBytes(int n) {
        byte[] b = new byte[n];
        random.nextBytes(b);
        return b;
    }
}
