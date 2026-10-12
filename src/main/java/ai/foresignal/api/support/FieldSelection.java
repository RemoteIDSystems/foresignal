package ai.foresignal.api.support;

import java.util.List;
import java.util.Set;

/**
 * A parsed {@code fields=} projection.
 *
 * <p>{@link #ALL} selects every default field. Otherwise only the listed top-level properties are
 * returned, and {@code payload} holds only the listed {@code payload.<dot.path>} entries (or the
 * whole payload when {@code payload} itself is listed).
 *
 * @param all true when no projection was requested
 * @param topLevel the selected top-level property names
 * @param payloadPaths the selected payload paths, dot-separated, in request order
 */
public record FieldSelection(boolean all, Set<String> topLevel, List<String> payloadPaths) {

    /** No projection: every field. */
    public static final FieldSelection ALL = new FieldSelection(true, Set.of(), List.of());

    /** Copies the collections. */
    public FieldSelection {
        topLevel = topLevel == null ? Set.of() : Set.copyOf(topLevel);
        payloadPaths = payloadPaths == null ? List.of() : List.copyOf(payloadPaths);
    }

    /** True when the top-level property {@code name} is returned. */
    public boolean includes(String name) {
        if (all) {
            return true;
        }
        if ("payload".equals(name)) {
            return topLevel.contains("payload") || !payloadPaths.isEmpty();
        }
        return topLevel.contains(name);
    }

    /** True when the payload is reduced to {@link #payloadPaths} (not returned whole). */
    public boolean projectsPayload() {
        return !all && !topLevel.contains("payload") && !payloadPaths.isEmpty();
    }

    /** True when the geometry is returned. */
    public boolean includesGeometry() {
        return includes("geometry");
    }

    /** A stable text form, for cursor fingerprints and entity tags. */
    public String canonical() {
        if (all) {
            return "*";
        }
        return String.join(",", new java.util.TreeSet<>(topLevel)) + "|" + String.join(",", payloadPaths);
    }
}
