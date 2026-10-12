package ai.foresignal.stats;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Top-N by source, kind, cell or entity (OpenAPI {@code StatsTop}, prompt 038).
 *
 * @param by {@code source}, {@code kind}, {@code cell} or {@code entity}
 * @param measure the measure
 * @param from the start
 * @param to the end
 * @param backend {@code rollup}, {@code raw} or {@code mixed}
 * @param items ranked items, largest first
 */
public record StatsTop(String by, String measure, Instant from, Instant to, String backend, List<Item> items) {

    /** Copies the items. */
    public StatsTop {
        items = items == null ? List.of() : List.copyOf(items);
    }

    /**
     * One ranked item.
     *
     * @param key the group key ({@code source:externalId} for entities)
     * @param sourceId the source, or null
     * @param kind the kind, or null
     * @param cell the H3 address, or null
     * @param externalId the entity, or null
     * @param title the entity's title, or null
     * @param count the measure
     */
    public record Item(String key, String sourceId, String kind, String cell, String externalId, String title,
            long count) {}

    /** The {@code StatsTop} JSON. */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("by", by);
        out.put("measure", measure);
        out.put("from", from.toString());
        out.put("to", to.toString());
        out.put("backend", backend);
        out.put("items", items.stream().map(i -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("key", i.key());
            m.put("sourceId", i.sourceId());
            m.put("kind", i.kind());
            m.put("cell", i.cell());
            m.put("externalId", i.externalId());
            m.put("title", i.title());
            m.put("count", i.count());
            return m;
        }).toList());
        return out;
    }
}
