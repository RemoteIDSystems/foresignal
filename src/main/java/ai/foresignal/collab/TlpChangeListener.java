package ai.foresignal.collab;

import ai.foresignal.security.Tlp;

/**
 * Told when a work product's TLP label changes (prompt 134), after the change is saved: prompt 134's chronicle network
 * lowers a chronicle's discoverability when a stricter label no longer allows it. Implementations must not throw for
 * types they do not handle.
 */
public interface TlpChangeListener {

    /**
     * A label changed.
     *
     * @param ref the work product
     * @param from the previous label
     * @param to the new label
     * @param actor the principal that changed it, or null
     */
    void tlpChanged(String ref, Tlp from, Tlp to, String actor);
}
