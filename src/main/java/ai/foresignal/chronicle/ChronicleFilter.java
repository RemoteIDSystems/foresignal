package ai.foresignal.chronicle;

import ai.foresignal.object.ObjectCaller;
import java.util.Set;

/**
 * The {@code chronicle} SSE topic filter of one stream (prompt 131): the caller (access lists and TLP decide which
 * chronicles it sees, 130) and the chronicles it follows (empty: every chronicle it can see).
 *
 * @param caller the stream's caller
 * @param chronicleIds the followed chronicles (canonical ids); empty for all visible
 */
public record ChronicleFilter(ObjectCaller caller, Set<String> chronicleIds) {

    /** Copies the set. */
    public ChronicleFilter {
        chronicleIds = chronicleIds == null ? Set.of() : Set.copyOf(chronicleIds);
    }

    /** True when the stream follows {@code chronicleId} (before the access check). */
    public boolean follows(String chronicleId) {
        return chronicleIds.isEmpty() || chronicleIds.contains(chronicleId);
    }
}
