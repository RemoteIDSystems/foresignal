package ai.foresignal.cycle;

import ai.foresignal.analyst.AnalystCitations;
import ai.foresignal.ask.CitableExtractor;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;

/**
 * What one cycle run may cite (prompt 128, built on the 102 {@link AnalystCitations}): the records, alerts and ids a
 * tool returned in the run, plus the objects of the deterministic packet. Typed object references
 * ({@code finding:ID}, {@code question:ID}, {@code hypothesis-set:ID}, {@code situation:ID} …) are valid when their id
 * was returned or is in the packet; anything else is dropped by the validator. Safe for parallel tool calls.
 */
public final class CycleCitations {

    private static final Pattern TYPED = Pattern.compile("^([a-z][a-z-]*):(.+)$");

    private final AnalystCitations returned;
    private final Set<String> packetRefs = ConcurrentHashMap.newKeySet();
    private final Set<String> packetIds = ConcurrentHashMap.newKeySet();

    /** A citation set fed by {@code extractors} and seeded with the packet's references. */
    public CycleCitations(List<CitableExtractor> extractors, Collection<String> packetRefs) {
        this.returned = new AnalystCitations(extractors);
        for (String ref : packetRefs) {
            seed(ref);
        }
    }

    /** Adds a reference the role was given (the packet, a question, a set). */
    public void seed(String ref) {
        if (ref == null) {
            return;
        }
        packetRefs.add(ref);
        Matcher m = TYPED.matcher(ref);
        if (m.matches()) {
            packetIds.add(m.group(2));
        }
    }

    /** Records what tool {@code tool} returned (after egress filtering). */
    public void accept(String tool, JsonNode shown) {
        returned.accept(tool, shown);
    }

    /** True when {@code citation} names an object returned in this run or given in the packet. */
    public boolean valid(String citation) {
        if (citation == null || citation.isBlank()) {
            return false;
        }
        String id = citation.strip();
        if (id.startsWith("[") && id.endsWith("]")) {
            id = id.substring(1, id.length() - 1).strip();
        }
        if (packetRefs.contains(id) || returned.valid(id)) {
            return true;
        }
        Matcher m = TYPED.matcher(id);
        if (m.matches()) {
            String bare = m.group(2);
            return packetIds.contains(bare) || returned.valid(bare);
        }
        return false;
    }
}
