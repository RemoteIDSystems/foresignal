package ai.foresignal.reference;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Adds region tags to record items (prompt 040): {@code region} on {@code Record} objects and
 * {@code properties.near} on GeoJSON features when a read asks for {@code include=region}, and
 * {@code near} on MCP record items.
 *
 * <p>Stored tags are read in one query per page. A record without a stored row is tagged on demand when
 * its source is {@code lazy} or {@code eager} (lazy tagging, at most {@value #MAX_LAZY_PER_PAGE} per
 * page; the rest come back without tags and are tagged by later reads); an {@code off} source has none.
 */
@Component
public class RegionIncludes {

    /** The most records one page tags on demand. */
    public static final int MAX_LAZY_PER_PAGE = 200;

    private final RegionTagStore store;
    private final RegionTagger tagger;

    /** Creates the helper. */
    public RegionIncludes(RegionTagStore store, RegionTagger tagger) {
        this.store = store;
        this.tagger = tagger;
    }

    /** The tags of each record key ({@code source}, {@code externalId}), by {@link RegionTagStore#key}. */
    public Map<String, RegionTags> tags(List<Map.Entry<String, String>> keys) {
        Map<String, RegionTags> out = new HashMap<>();
        if (keys.isEmpty()) {
            return out;
        }
        store.findAll(keys).forEach((key, stored) -> out.put(key, stored.tags()));
        int lazy = 0;
        for (Map.Entry<String, String> key : keys) {
            String k = RegionTagStore.key(key.getKey(), key.getValue());
            if (out.containsKey(k) || lazy >= MAX_LAZY_PER_PAGE) {
                continue;
            }
            lazy++;
            RegionTags tags = tagger.tagRecord(key.getKey(), key.getValue());
            if (tags != null) {
                out.put(k, tags);
            }
        }
        return out;
    }

    /** Adds {@code region} (a {@code RegionTags} object, or null) to each {@code Record} item. */
    public void addToRecords(List<Map<String, Object>> items) {
        Map<String, RegionTags> tags = tags(keys(items));
        for (Map<String, Object> item : items) {
            Map.Entry<String, String> key = key(item);
            RegionTags t = key == null ? null : tags.get(RegionTagStore.key(key.getKey(), key.getValue()));
            item.put("region", t == null ? null : t.toMap());
        }
    }

    /** Adds {@code properties.near} (the near text, or null) to each GeoJSON feature. */
    @SuppressWarnings("unchecked")
    public void addToFeatures(List<Map<String, Object>> features) {
        Map<String, RegionTags> tags = tags(keys(features));
        for (Map<String, Object> feature : features) {
            Map.Entry<String, String> key = key(feature);
            if (feature.get("properties") instanceof Map<?, ?> props) {
                Map<String, Object> properties = (Map<String, Object>) props;
                RegionTags t = key == null ? null : tags.get(RegionTagStore.key(key.getKey(), key.getValue()));
                properties.put("near", t == null ? null : t.nearText());
                if (t != null && t.near() != null) {
                    properties.put("nearAttribution", RegionAttribution.GEONAMES);
                }
            }
        }
    }

    private static List<Map.Entry<String, String>> keys(List<Map<String, Object>> items) {
        List<Map.Entry<String, String>> keys = new ArrayList<>(items.size());
        for (Map<String, Object> item : items) {
            Map.Entry<String, String> key = key(item);
            if (key != null) {
                keys.add(key);
            }
        }
        return keys;
    }

    /**
     * The record key of a {@code Record} item or GeoJSON feature, from its {@code id}
     * ({@code <source>:<externalId>}; source ids never contain a colon), else from its {@code source} and
     * {@code externalId}, or null.
     */
    static Map.Entry<String, String> key(Map<String, Object> item) {
        Object id = item.get("id");
        if (id == null) {
            // A projected item may lack its id; fall back to its (or its properties') source and externalId.
            Object holder = item.get("properties") instanceof Map<?, ?> props ? props : item;
            if (holder instanceof Map<?, ?> map && map.get("source") != null && map.get("externalId") != null) {
                return new AbstractMap.SimpleEntry<>(String.valueOf(map.get("source")),
                        String.valueOf(map.get("externalId")));
            }
            return null;
        }
        String text = String.valueOf(id);
        int colon = text.indexOf(':');
        return colon <= 0 ? null : new AbstractMap.SimpleEntry<>(text.substring(0, colon), text.substring(colon + 1));
    }
}
