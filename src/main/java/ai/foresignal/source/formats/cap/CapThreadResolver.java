package ai.foresignal.source.formats.cap;

import ai.foresignal.source.spi.AdapterStateStore;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Gives one Foresignal record per CAP alert thread (prompt 067): an {@code Alert}, its {@code Update}s and
 * its {@code Cancel} share one {@code externalId}, so a rule sees the thread change state instead of a
 * new record per message.
 *
 * <p>The thread key is, in order:
 * <ol>
 *   <li>the source's custom key when it supplies one ({@code tsunami-warnings} uses
 *       {@code {center}:{incidents}});
 *   <li>otherwise, when the message has {@code references}: the thread of the earliest referenced message
 *       this resolver remembers, or, when it remembers none, the earliest reference's own
 *       {@code {sender}:{identifier}};
 *   <li>otherwise the message's own {@code {sender}:{identifier}}.
 * </ol>
 *
 * <p>The {@code identifier → threadKey} map is bounded: entries expire {@link #TTL} (14 days) after they
 * were last used and at most {@link #MAX_ENTRIES} (20,000) are kept, the least recently used going first.
 * It lives in the adapter state under {@value #STATE_KEY}, one {@code identifier TAB threadKey TAB
 * epochSecond} line per entry. Not thread-safe; one instance serves one fetch.
 */
public final class CapThreadResolver {

    /** The adapter-state key holding the map. */
    public static final String STATE_KEY = "cap.threads";

    /** How long an unused entry is remembered. */
    public static final Duration TTL = Duration.ofDays(14);

    /** The most entries kept. */
    public static final int MAX_ENTRIES = 20_000;

    private final Duration ttl;
    private final int maxEntries;
    private final LinkedHashMap<String, Entry> entries = new LinkedHashMap<>();
    private boolean dirty;

    /** An empty resolver with the prompt's bounds. */
    public CapThreadResolver() {
        this(TTL, MAX_ENTRIES);
    }

    /**
     * An empty resolver with other bounds (tests).
     *
     * @param ttl how long an unused entry is remembered
     * @param maxEntries the most entries kept
     */
    public CapThreadResolver(Duration ttl, int maxEntries) {
        if (ttl == null || ttl.isNegative() || ttl.isZero() || maxEntries <= 0) {
            throw new IllegalArgumentException("ttl and maxEntries must be positive");
        }
        this.ttl = ttl;
        this.maxEntries = maxEntries;
    }

    /**
     * The resolver stored in {@code state}, without the entries that expired before {@code now}; an
     * unreadable line is dropped.
     */
    public static CapThreadResolver load(AdapterStateStore state, Instant now) {
        return load(state, now, TTL, MAX_ENTRIES);
    }

    /** {@link #load(AdapterStateStore, Instant)} with other bounds (tests). */
    public static CapThreadResolver load(AdapterStateStore state, Instant now, Duration ttl, int maxEntries) {
        CapThreadResolver resolver = new CapThreadResolver(ttl, maxEntries);
        String stored = state.get(STATE_KEY).orElse("");
        List<String[]> rows = new ArrayList<>();
        for (String line : stored.split("\n")) {
            String[] parts = line.split("\t");
            if (parts.length == 3) {
                rows.add(parts);
            }
        }
        rows.sort(Comparator.comparingLong(r -> parseLong(r[2])));
        for (String[] row : rows) {
            long seconds = parseLong(row[2]);
            if (seconds > 0) {
                resolver.entries.put(row[0], new Entry(row[1], Instant.ofEpochSecond(seconds)));
            } else {
                resolver.dirty = true;
            }
        }
        resolver.evict(now);
        return resolver;
    }

    /**
     * The thread key of {@code alert}, remembering it for the alert's identifier and for every
     * identifier it references.
     *
     * @param alert the message
     * @param customKey the source's own key, or null or blank for none
     * @param now the time used for expiry
     * @return the thread key, never blank
     */
    public String resolve(CapAlert alert, String customKey, Instant now) {
        String key;
        if (customKey != null && !customKey.isBlank()) {
            key = customKey;
        } else if (!alert.references().isEmpty()) {
            List<CapAlert.Reference> ordered = new ArrayList<>(alert.references());
            ordered.sort(Comparator.comparing(CapAlert.Reference::sent,
                    Comparator.nullsLast(Comparator.comparing(OffsetDateTime::toInstant))));
            key = null;
            for (CapAlert.Reference reference : ordered) {
                Entry known = entries.get(reference.identifier());
                if (known != null) {
                    key = known.threadKey();
                    break;
                }
            }
            if (key == null) {
                key = ordered.get(0).key();
            }
        } else {
            key = alert.ownKey();
        }
        remember(alert.identifier(), key, now);
        for (CapAlert.Reference reference : alert.references()) {
            if (!entries.containsKey(reference.identifier())) {
                remember(reference.identifier(), key, now);
            }
        }
        evict(now);
        return key;
    }

    /** The remembered thread of {@code identifier}. */
    public Optional<String> lookup(String identifier) {
        Entry entry = entries.get(identifier);
        return entry == null ? Optional.empty() : Optional.of(entry.threadKey());
    }

    /** How many identifiers are remembered. */
    public int size() {
        return entries.size();
    }

    /** Writes the map to {@code state} when it changed. */
    public void save(AdapterStateStore state) {
        if (!dirty) {
            return;
        }
        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, Entry> e : entries.entrySet()) {
            if (!out.isEmpty()) {
                out.append('\n');
            }
            out.append(e.getKey()).append('\t').append(e.getValue().threadKey()).append('\t')
                    .append(e.getValue().seenAt().getEpochSecond());
        }
        if (out.isEmpty()) {
            state.remove(STATE_KEY);
        } else {
            state.put(STATE_KEY, out.toString());
        }
        dirty = false;
    }

    private void remember(String identifier, String threadKey, Instant now) {
        if (identifier == null || identifier.isBlank() || !storable(identifier) || !storable(threadKey)) {
            return;
        }
        entries.remove(identifier);
        entries.put(identifier, new Entry(threadKey, now));
        dirty = true;
    }

    /** Drops expired entries, then the least recently used beyond the cap. */
    void evict(Instant now) {
        Instant cutoff = now.minus(ttl);
        Iterator<Map.Entry<String, Entry>> it = entries.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Entry> e = it.next();
            if (e.getValue().seenAt().isBefore(cutoff) || entries.size() > maxEntries) {
                it.remove();
                dirty = true;
            } else {
                break;
            }
        }
        while (entries.size() > maxEntries) {
            Iterator<String> oldest = entries.keySet().iterator();
            oldest.next();
            oldest.remove();
            dirty = true;
        }
    }

    private static boolean storable(String value) {
        return value != null && value.indexOf('\t') < 0 && value.indexOf('\n') < 0 && value.indexOf('\r') < 0;
    }

    private static long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private record Entry(String threadKey, Instant seenAt) {}
}
