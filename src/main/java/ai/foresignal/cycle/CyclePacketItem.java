package ai.foresignal.cycle;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One packet entry before ranking (prompt 128): an object, what happened to it, how much it matters, and the
 * attributes the scope filters on.
 *
 * @param ref the object reference ({@code situation:…}, {@code hypothesis-set:…}, {@code finding:…})
 * @param title the title
 * @param change what happened (new, activated, escalated, revised, observed, due, resolved …)
 * @param severity the severity, or null
 * @param at when it happened, or null
 * @param score the ranking score (higher first)
 * @param regionCodes the regions (empty: not located)
 * @param theme the situation theme, or null
 * @param situationId the linked situation, or null
 * @param setId the linked hypothesis set, or null
 * @param setCadence the linked set's cadence, or null
 * @param sourceIds the sources (empty: none)
 * @param category the question category, or null
 * @param privateItem true when built from a private source
 * @param detail the section-specific detail
 */
public record CyclePacketItem(String ref, String title, String change, String severity, Instant at, double score,
        List<String> regionCodes, String theme, String situationId, String setId, String setCadence,
        List<String> sourceIds, String category, boolean privateItem, Map<String, Object> detail) {

    /** Copies the members. */
    public CyclePacketItem {
        regionCodes = regionCodes == null ? List.of() : List.copyOf(regionCodes);
        sourceIds = sourceIds == null ? List.of() : List.copyOf(sourceIds);
        detail = detail == null ? new LinkedHashMap<>() : new LinkedHashMap<>(detail);
        title = title == null ? ref : title;
    }

    /** True when the item passes {@code scope} for a cycle of {@code cadence}. */
    public boolean admittedBy(CycleScope scope, String cadence) {
        return scope.admits(regionCodes, theme, situationId, setId, setCadence, sourceIds, category, cadence);
    }

    /** The wire form ({@code CyclePacketItem}) at 1-based {@code rank}. */
    public Map<String, Object> toMap(int rank) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ref", ref);
        out.put("title", title);
        out.put("change", change);
        out.put("severity", severity);
        out.put("at", at == null ? null : at.toString());
        out.put("detail", detail.isEmpty() ? null : detail);
        out.put("rank", rank);
        return out;
    }

    /** The id part of the reference. */
    public String id() {
        int colon = ref.indexOf(':');
        return colon < 0 ? ref : ref.substring(colon + 1);
    }
}
