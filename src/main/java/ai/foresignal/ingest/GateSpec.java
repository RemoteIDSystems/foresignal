package ai.foresignal.ingest;

import java.util.Set;

/**
 * What a source's trigger rules read (Core-Data-Model §11).
 *
 * @param paths dot-separated payload paths the rules' conditions reference
 * @param needsGeometry true when a rule has a geofence, so the persisted geometry must be loaded
 */
public record GateSpec(Set<String> paths, boolean needsGeometry) {

    /** No rules. */
    public static final GateSpec NONE = new GateSpec(Set.of(), false);

    public GateSpec {
        paths = paths == null ? Set.of() : Set.copyOf(paths);
    }

    /** True when no rule applies. */
    public boolean isEmpty() {
        return paths.isEmpty() && !needsGeometry;
    }
}
