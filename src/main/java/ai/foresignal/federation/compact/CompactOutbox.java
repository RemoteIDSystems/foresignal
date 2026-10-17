package ai.foresignal.federation.compact;

import ai.foresignal.notify.mesh.cbor.MeshCborMessage;
import ai.foresignal.notify.mesh.cbor.MeshCborProfile;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The compact-message outbox (prompt 113): a priority queue of encoded {@link MeshCborMessage}s for delay-tolerant
 * transports (the 084 mesh channels and the store-and-forward bundle).
 *
 * <ul>
 *   <li><strong>Priority:</strong> critical, then warning, then info; newest first within a class.
 *   <li><strong>TTL:</strong> a message is dropped once key 7 plus key 12 has passed.
 *   <li><strong>Dedup</strong> by message id (key 11): a message whose id is queued is ignored unless its severity
 *       changed or it is a resolution (type 2), which replaces it and is sent again.
 *   <li><strong>Byte budget:</strong> {@link #take} hands a transport only what fits its byte budget for a sliding
 *       window (the 084 duty-cycle settings: {@code maxMessagesPerHour × 160} bytes per hour per mesh channel), and
 *       each message version at most once per transport.
 * </ul>
 *
 * <p>Thread-safe; pure (the caller passes the time). Held in memory: a restart loses queued messages, which the
 * next events re-queue.
 */
public final class CompactOutbox {

    /** The default capacity. */
    public static final int DEFAULT_CAPACITY = 1_000;

    /** Priority order: severity descending, then newest first. */
    public static final Comparator<Entry> PRIORITY = Comparator
            .comparingInt((Entry e) -> e.message().severity() == null ? 0 : e.message().severity()).reversed()
            .thenComparing(Entry::queuedAt, Comparator.reverseOrder())
            .thenComparing(Entry::id);

    /**
     * One queued message.
     *
     * @param id the message id (key 11) as hex
     * @param message the message
     * @param encoded its CBOR encoding (at most 160 bytes)
     * @param queuedAt when it was queued
     * @param expiresAt when it expires, or null
     * @param version incremented when a resend replaces it
     */
    public record Entry(String id, MeshCborMessage message, byte[] encoded, Instant queuedAt, Instant expiresAt,
            long version) {

        /** True when expired at {@code now}. */
        public boolean expired(Instant now) {
            return expiresAt != null && !now.isBefore(expiresAt);
        }
    }

    private final int capacity;
    private final Map<String, Entry> entries = new LinkedHashMap<>();
    private final Map<String, Map<String, Long>> sent = new HashMap<>();
    private final Map<String, Deque<long[]>> spent = new HashMap<>();
    private long versions;

    /** An outbox of {@value #DEFAULT_CAPACITY} messages. */
    public CompactOutbox() {
        this(DEFAULT_CAPACITY);
    }

    /** An outbox of {@code capacity} messages. */
    public CompactOutbox(int capacity) {
        this.capacity = Math.max(1, capacity);
    }

    /**
     * Queues {@code message} (which must carry key 11).
     *
     * @return true when it was queued (new, a severity change or a resolution); false when it was a duplicate,
     *     already expired or carried no message id
     */
    public synchronized boolean offer(MeshCborMessage message, Instant now) {
        Objects.requireNonNull(message, "message");
        String id = CompactMessages.idHex(message);
        if (id == null) {
            return false;
        }
        Instant expires = CompactMessages.expiresAt(message);
        if (expires != null && !now.isBefore(expires)) {
            return false;
        }
        Entry existing = entries.get(id);
        if (existing != null) {
            boolean resolution = message.type() == MeshCborProfile.TYPE_RESOLVED
                    && existing.message().type() != MeshCborProfile.TYPE_RESOLVED;
            boolean severityChanged = !Objects.equals(existing.message().severity(), message.severity());
            if (!resolution && !severityChanged) {
                return false;
            }
        }
        byte[] encoded = MeshCborProfile.encode(message);
        entries.remove(id);
        entries.put(id, new Entry(id, message, encoded, now, expires, ++versions));
        evict(now);
        return true;
    }

    /** The queued messages in priority order, expired ones dropped. */
    public synchronized List<Entry> pending(Instant now) {
        purge(now);
        List<Entry> out = new ArrayList<>(entries.values());
        out.sort(PRIORITY);
        return out;
    }

    /** The queued messages queued at or after {@code since} (all when null), in priority order. */
    public synchronized List<Entry> since(Instant since, Instant now) {
        List<Entry> out = new ArrayList<>();
        for (Entry entry : pending(now)) {
            if (since == null || !entry.queuedAt().isBefore(since)) {
                out.add(entry);
            }
        }
        return out;
    }

    /**
     * The messages {@code transport} has not sent yet (at their current version), in priority order, as many as
     * fit the bytes left of {@code budgetBytes} in the sliding {@code window}; they are marked sent.
     */
    public synchronized List<Entry> take(String transport, int budgetBytes, Duration window, Instant now) {
        purge(now);
        Deque<long[]> log = spent.computeIfAbsent(transport, k -> new ArrayDeque<>());
        long cutoff = now.minus(window).toEpochMilli();
        while (!log.isEmpty() && log.peekFirst()[0] < cutoff) {
            log.pollFirst();
        }
        long used = 0;
        for (long[] entry : log) {
            used += entry[1];
        }
        Map<String, Long> done = sent.computeIfAbsent(transport, k -> new HashMap<>());
        List<Entry> ordered = new ArrayList<>(entries.values());
        ordered.sort(PRIORITY);
        List<Entry> out = new ArrayList<>();
        for (Entry entry : ordered) {
            Long version = done.get(entry.id());
            if (version != null && version >= entry.version()) {
                continue;
            }
            if (used + entry.encoded().length > budgetBytes) {
                continue;
            }
            used += entry.encoded().length;
            log.addLast(new long[] {now.toEpochMilli(), entry.encoded().length});
            done.put(entry.id(), entry.version());
            out.add(entry);
        }
        return out;
    }

    /** Undoes {@link #take} for one entry that the transport could not send (deferred). */
    public synchronized void unsend(String transport, Entry entry) {
        Map<String, Long> done = sent.get(transport);
        if (done != null && Objects.equals(done.get(entry.id()), entry.version())) {
            done.remove(entry.id());
        }
        Deque<long[]> log = spent.get(transport);
        if (log != null && !log.isEmpty()) {
            Iterator<long[]> it = log.descendingIterator();
            while (it.hasNext()) {
                if (it.next()[1] == entry.encoded().length) {
                    it.remove();
                    break;
                }
            }
        }
    }

    /** The number of queued messages. */
    public synchronized int size() {
        return entries.size();
    }

    /** Empties the outbox (tests and the test database cleaner). */
    public synchronized void clear() {
        entries.clear();
        sent.clear();
        spent.clear();
    }

    private void purge(Instant now) {
        entries.values().removeIf(entry -> entry.expired(now));
        for (Map<String, Long> done : sent.values()) {
            done.keySet().retainAll(entries.keySet());
        }
    }

    private void evict(Instant now) {
        purge(now);
        while (entries.size() > capacity) {
            List<Entry> ordered = new ArrayList<>(entries.values());
            ordered.sort(PRIORITY);
            entries.remove(ordered.getLast().id());
        }
    }
}
