package ai.foresignal.social.narrative;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Multi-index hashing over PDQ hashes (prompt 136): each 256-bit hash is split into its sixteen 16-bit words, and each
 * word position has a table from word value to the hashes holding it. Two hashes within Hamming distance {@code d}
 * agree to within {@code floor(d / 16)} bits in at least one word (pigeonhole), so a query probes, in every position,
 * each word value within that radius (for the default 31: the word itself and its 16 one-bit neighbors), then checks
 * the full distance of every candidate. Exact (no false negatives) and thread-safe.
 */
public final class PdqIndex {

    /**
     * A match.
     *
     * @param id the fingerprint id
     * @param hash the stored hash
     * @param distance the Hamming distance to the query
     */
    public record Match(String id, Pdq.Hash hash, int distance) {}

    private final List<Map<Integer, List<Integer>>> tables = new ArrayList<>();
    private final List<String> ids = new ArrayList<>();
    private final List<Pdq.Hash> hashes = new ArrayList<>();
    private final Map<String, Integer> positionOf = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /** Creates an empty index. */
    public PdqIndex() {
        for (int i = 0; i < 16; i++) {
            tables.add(new HashMap<>());
        }
    }

    /** The number of indexed hashes. */
    public int size() {
        lock.readLock().lock();
        try {
            return positionOf.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /** Adds (or keeps) a hash under an id. */
    public void add(String id, Pdq.Hash hash) {
        lock.writeLock().lock();
        try {
            if (positionOf.containsKey(id)) {
                return;
            }
            int position = ids.size();
            ids.add(id);
            hashes.add(hash);
            positionOf.put(id, position);
            for (int w = 0; w < 16; w++) {
                tables.get(w).computeIfAbsent(hash.word(w), k -> new ArrayList<>()).add(position);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /** Forgets every hash. */
    public void clear() {
        lock.writeLock().lock();
        try {
            for (Map<Integer, List<Integer>> t : tables) {
                t.clear();
            }
            ids.clear();
            hashes.clear();
            positionOf.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * The hashes within {@code maxDistance} of {@code query}, nearest first (ties by id).
     *
     * @param query the query hash
     * @param maxDistance the largest Hamming distance, 0–63
     * @param limit the most matches returned
     */
    public List<Match> search(Pdq.Hash query, int maxDistance, int limit) {
        int radius = Math.max(0, Math.min(3, maxDistance / 16));
        List<Integer> probes = new ArrayList<>();
        lock.readLock().lock();
        try {
            Set<Integer> candidates = new LinkedHashSet<>();
            for (int w = 0; w < 16; w++) {
                probes.clear();
                neighbors(query.word(w), radius, probes);
                Map<Integer, List<Integer>> table = tables.get(w);
                for (int probe : probes) {
                    List<Integer> hits = table.get(probe);
                    if (hits != null) {
                        candidates.addAll(hits);
                    }
                }
            }
            List<Match> out = new ArrayList<>();
            for (int position : candidates) {
                Pdq.Hash h = hashes.get(position);
                int d = query.distance(h);
                if (d <= maxDistance) {
                    out.add(new Match(ids.get(position), h, d));
                }
            }
            out.sort(Comparator.comparingInt(Match::distance).thenComparing(Match::id));
            return out.size() > limit ? List.copyOf(out.subList(0, limit)) : out;
        } finally {
            lock.readLock().unlock();
        }
    }

    /** Every 16-bit value within {@code radius} bits of {@code word}. */
    static void neighbors(int word, int radius, List<Integer> out) {
        out.add(word);
        if (radius >= 1) {
            for (int a = 0; a < 16; a++) {
                int w1 = word ^ (1 << a);
                out.add(w1);
                if (radius >= 2) {
                    for (int b = a + 1; b < 16; b++) {
                        int w2 = w1 ^ (1 << b);
                        out.add(w2);
                        if (radius >= 3) {
                            for (int c = b + 1; c < 16; c++) {
                                out.add(w2 ^ (1 << c));
                            }
                        }
                    }
                }
            }
        }
    }
}
