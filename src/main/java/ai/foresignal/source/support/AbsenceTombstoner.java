package ai.foresignal.source.support;

import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.NormalizedRecord;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Tombstones the entities of a catalog that disappear from <strong>two consecutive</strong> complete fetches
 * (prompt 072; reused by 074 and 075).
 *
 * <p>Catalogs (camera lists, reference tables) carry no deletion marker: an entity is gone when the upstream stops
 * listing it. One missing fetch is not enough, because catalogs flap (a camera briefly dropped while it is
 * serviced, a district file regenerated half-way). So an id is tombstoned only when it was listed, then missing
 * from one complete fetch, then still missing from the next one.
 *
 * <p><strong>Only complete fetches count.</strong> The caller passes the {@link Outcome} of the fetch: a 304, a
 * truncated body or a parse error ({@link Outcome#NOT_MODIFIED}, {@link Outcome#TRUNCATED},
 * {@link Outcome#PARSE_ERROR}) leaves the state untouched and tombstones nothing, so stale or partial data never reads
 * as "everything else was removed" (Adapter Guide §7).
 *
 * <p><strong>State</strong> (in the {@link AdapterStateStore}, so it survives restarts):
 * <ul>
 *   <li>{@value #IDS_KEY}: the ids of the last complete fetch;
 *   <li>{@value #MISSED_KEY}: the ids missing from the last complete fetch for the first time.
 * </ul>
 * Each value is the sorted ids joined by newlines; above {@value #COMPRESS_ABOVE_BYTES} bytes it is gzipped and
 * base64-encoded behind the prefix {@value #GZIP_PREFIX}. A scope string, when given, is appended to both keys
 * ({@code absence:ids:{scope}}) so one source can keep independent catalogs apart.
 */
public final class AbsenceTombstoner {

    /** The state key of the ids of the last complete fetch. */
    public static final String IDS_KEY = "absence:ids";

    /** The state key of the ids missing (once) from the last complete fetch. */
    public static final String MISSED_KEY = "absence:missed";

    /** A stored value longer than this many UTF-8 bytes is gzipped and base64-encoded. */
    public static final int COMPRESS_ABOVE_BYTES = 64 * 1024;

    /** The prefix that marks a gzipped, base64-encoded value. */
    public static final String GZIP_PREFIX = "gz:";

    /** How a fetch ended, as far as absence is concerned. */
    public enum Outcome {
        /** The whole catalog arrived and parsed: the only outcome that moves the state. */
        COMPLETE,
        /** The upstream answered 304: nothing is known about absence. */
        NOT_MODIFIED,
        /** The body was cut at the response cap. */
        TRUNCATED,
        /** The body could not be parsed. */
        PARSE_ERROR
    }

    private AbsenceTombstoner() {}

    /**
     * Records a fetch and returns the ids to tombstone, in the source's only scope.
     *
     * @see #update(AdapterStateStore, String, Outcome, Collection)
     */
    public static List<String> update(AdapterStateStore state, Outcome outcome, Collection<String> currentIds) {
        return update(state, null, outcome, currentIds);
    }

    /**
     * Records a fetch and returns the ids to tombstone.
     *
     * <p>For {@link Outcome#COMPLETE}: the ids missing now that were already missing from the previous complete fetch
     * are returned; the ids listed before and missing now for the first time are remembered; {@code currentIds}
     * becomes the new last complete set. An id that reappears is forgotten from the missing set. Every other outcome
     * returns nothing and changes nothing.
     *
     * @param state the adapter state
     * @param scope separates independent catalogs of one source; null or blank for the only one
     * @param outcome how the fetch ended
     * @param currentIds every id of the complete fetch (ignored unless {@code outcome} is COMPLETE)
     * @return the ids missing from two consecutive complete fetches, sorted; empty otherwise
     */
    public static List<String> update(AdapterStateStore state, String scope, Outcome outcome,
            Collection<String> currentIds) {
        if (outcome != Outcome.COMPLETE) {
            return List.of();
        }
        String idsKey = key(IDS_KEY, scope);
        String missedKey = key(MISSED_KEY, scope);
        Set<String> current = new TreeSet<>();
        if (currentIds != null) {
            for (String id : currentIds) {
                if (id != null && !id.isBlank()) {
                    current.add(id);
                }
            }
        }
        Set<String> previous = read(state, idsKey);
        Set<String> missedOnce = read(state, missedKey);

        Set<String> tombstone = new TreeSet<>(missedOnce);
        tombstone.removeAll(current);

        Set<String> newlyMissed = new TreeSet<>(previous);
        newlyMissed.removeAll(current);

        write(state, idsKey, current);
        write(state, missedKey, newlyMissed);
        return List.copyOf(tombstone);
    }

    /**
     * Tombstone records for {@code ids}.
     *
     * @param ids the ids returned by {@link #update}
     * @param kind the records' kind
     * @param at the tombstones' {@code observedAt}
     */
    public static List<NormalizedRecord> tombstones(Collection<String> ids, String kind, Instant at) {
        List<NormalizedRecord> out = new ArrayList<>(ids.size());
        for (String id : ids) {
            out.add(NormalizedRecord.tombstone(id, kind, at));
        }
        return out;
    }

    /**
     * The ids stored under {@code key}, decoding a compressed value.
     *
     * @return the ids, possibly empty
     */
    public static Set<String> read(AdapterStateStore state, String key) {
        String value = state.get(key).orElse("");
        Set<String> out = new TreeSet<>();
        if (value.isEmpty()) {
            return out;
        }
        String text = value.startsWith(GZIP_PREFIX) ? gunzip(value.substring(GZIP_PREFIX.length())) : value;
        Arrays.stream(text.split("\n")).filter(s -> !s.isBlank()).forEach(out::add);
        return out;
    }

    /** Encodes sorted ids as stored: newline-joined, compressed above {@value #COMPRESS_ABOVE_BYTES} bytes. */
    static String encode(Set<String> ids) {
        String text = String.join("\n", new TreeSet<>(ids));
        if (text.getBytes(StandardCharsets.UTF_8).length <= COMPRESS_ABOVE_BYTES) {
            return text;
        }
        return GZIP_PREFIX + gzip(text);
    }

    private static void write(AdapterStateStore state, String key, Set<String> ids) {
        if (ids.isEmpty()) {
            if (state.get(key).isPresent()) {
                state.remove(key);
            }
            return;
        }
        String value = encode(ids);
        if (!value.equals(state.get(key).orElse(null))) {
            state.put(key, value);
        }
    }

    private static String key(String base, String scope) {
        return scope == null || scope.isBlank() ? base : base + ":" + scope.strip();
    }

    private static String gzip(String text) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (GZIPOutputStream out = new GZIPOutputStream(bytes)) {
            out.write(text.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not compress the absence state", e);
        }
        return Base64.getEncoder().encodeToString(bytes.toByteArray());
    }

    private static String gunzip(String base64) {
        try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(base64)))) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException | IllegalArgumentException e) {
            // A corrupt value is treated as no state: the next complete fetch starts a fresh set.
            return "";
        }
    }
}
