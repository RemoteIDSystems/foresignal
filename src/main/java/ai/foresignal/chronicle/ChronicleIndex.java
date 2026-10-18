package ai.foresignal.chronicle;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The in-memory index of the active chronicles' compiled layers (prompt 131): rows are pre-filtered by source (layers
 * naming sources), with the source-agnostic layers checked for every row, before the full test with its JTS prepared
 * geometries. Immutable; {@link ChronicleCaptureService} rebuilds it when a chronicle or layer changes and every
 * minute (entity links and situation members move).
 */
public final class ChronicleIndex {

    /**
     * One active chronicle in the index.
     *
     * @param chronicle a detached snapshot of the chronicle
     * @param layers its compiled, enabled, live layers
     */
    public record Entry(ChronicleEntity chronicle, List<CompiledLayer> layers) {

        /** Copies the list. */
        public Entry {
            layers = List.copyOf(layers);
        }

        /** The chronicle id. */
        public String id() {
            return chronicle.getId();
        }

        /** The sources its record-backed layers read, or null when one of them reads any source. */
        public Set<String> sources() {
            Set<String> out = new HashSet<>();
            for (CompiledLayer l : layers) {
                if (!l.recordBacked()) {
                    continue;
                }
                if (l.sources().isEmpty()) {
                    return null;
                }
                out.addAll(l.sources());
            }
            return out;
        }
    }

    /** No active chronicle. */
    public static final ChronicleIndex EMPTY = new ChronicleIndex(List.of(), Instant.EPOCH);

    private final Map<String, Entry> entries;
    private final Map<String, List<CompiledLayer>> bySource;
    private final List<CompiledLayer> anySource;
    private final Instant builtAt;

    /**
     * Builds the index.
     *
     * @param entries the active chronicles with their layers
     * @param builtAt when it was built
     */
    public ChronicleIndex(Collection<Entry> entries, Instant builtAt) {
        Map<String, Entry> byId = new LinkedHashMap<>();
        Map<String, List<CompiledLayer>> sources = new HashMap<>();
        List<CompiledLayer> any = new ArrayList<>();
        for (Entry e : entries) {
            byId.put(e.id(), e);
            for (CompiledLayer l : e.layers()) {
                if (!l.recordBacked()) {
                    continue;
                }
                if (l.sources().isEmpty()) {
                    any.add(l);
                } else {
                    for (String s : l.sources()) {
                        sources.computeIfAbsent(s, k -> new ArrayList<>()).add(l);
                    }
                }
            }
        }
        this.entries = byId;
        this.bySource = sources;
        this.anySource = List.copyOf(any);
        this.builtAt = builtAt;
    }

    /** The active chronicles. */
    public Collection<Entry> entries() {
        return entries.values();
    }

    /** One active chronicle, or null. */
    public Entry entry(String chronicleId) {
        return entries.get(chronicleId);
    }

    /** True when no chronicle is active. */
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    /** When it was built. */
    public Instant builtAt() {
        return builtAt;
    }

    /**
     * The sources the capture read may be narrowed to, or null when some layer reads any source (or when there is no
     * record-backed layer at all, an empty set).
     */
    public Set<String> sourceFilter() {
        if (!anySource.isEmpty()) {
            return null;
        }
        return Set.copyOf(bySource.keySet());
    }

    /** The record-backed layers a row of {@code sourceId} may match. */
    public List<CompiledLayer> candidates(String sourceId) {
        List<CompiledLayer> named = bySource.get(sourceId);
        if (named == null) {
            return anySource;
        }
        if (anySource.isEmpty()) {
            return named;
        }
        List<CompiledLayer> out = new ArrayList<>(named.size() + anySource.size());
        out.addAll(named);
        out.addAll(anySource);
        return out;
    }
}
