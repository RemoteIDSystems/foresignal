package ai.foresignal.source.adapters.satdump;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Normalizes satellite names to the {@code satKey} used in SatDump pass and product ids
 * (Weather-Imagery-Design §4.4): {@code METEOR-M2 3}, {@code Meteor-M2-3} and NORAD 57166 all become
 * {@code meteor-m2-3}.
 *
 * <p>The table lives in {@code src/main/resources/satdump/sat-names.json}. A name is compared after
 * {@link #slug}: lower case, every run of other characters replaced by {@code -}. A name the table does
 * not know becomes its own slug; a bare number is looked up as a NORAD catalog number. The operator's
 * {@code satNames} parameter (a JSON object of alias to key) is applied on top.
 */
public final class SatNames {

    /** The bundled table. */
    public static final String RESOURCE = "satdump/sat-names.json";

    /**
     * One satellite of the table.
     *
     * @param key the satKey
     * @param name the display name
     * @param norad the NORAD catalog number, or null
     */
    public record Entry(String key, String name, Integer norad) {}

    private final Map<String, String> aliases;
    private final Map<Integer, String> norads;
    private final Map<String, Entry> entries;

    private SatNames(Map<String, String> aliases, Map<Integer, String> norads, Map<String, Entry> entries) {
        this.aliases = Map.copyOf(aliases);
        this.norads = Map.copyOf(norads);
        this.entries = Map.copyOf(entries);
    }

    /** The bundled table. */
    public static SatNames bundled() {
        try (InputStream in = SatNames.class.getClassLoader().getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException(RESOURCE + " is missing from the classpath");
            }
            return parse(JsonMapper.builder().build().readTree(in));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** A table from its JSON form ({@code {"satellites": [{key, name, norad, aliases}]}}). */
    static SatNames parse(JsonNode root) {
        Map<String, String> aliases = new HashMap<>();
        Map<Integer, String> norads = new HashMap<>();
        Map<String, Entry> entries = new LinkedHashMap<>();
        for (JsonNode node : root.path("satellites")) {
            String key = slug(node.path("key").asString(""));
            if (key.isEmpty()) {
                continue;
            }
            Integer norad = node.path("norad").isNumber() ? node.path("norad").asInt() : null;
            entries.put(key, new Entry(key, node.path("name").asString(key), norad));
            aliases.put(key, key);
            aliases.put(slug(node.path("name").asString("")), key);
            for (JsonNode alias : node.path("aliases")) {
                aliases.put(slug(alias.asString("")), key);
            }
            if (norad != null) {
                norads.put(norad, key);
            }
        }
        aliases.remove("");
        return new SatNames(aliases, norads, entries);
    }

    /**
     * A copy with the operator's overrides applied: each alias (a name or a NORAD number) maps to a key.
     *
     * @param overrides alias to key; null or empty for none
     */
    public SatNames withOverrides(Map<String, String> overrides) {
        if (overrides == null || overrides.isEmpty()) {
            return this;
        }
        Map<String, String> a = new HashMap<>(aliases);
        Map<Integer, String> n = new HashMap<>(norads);
        overrides.forEach((alias, key) -> {
            String target = slug(key);
            if (target.isEmpty() || alias == null) {
                return;
            }
            String trimmed = alias.trim();
            if (trimmed.matches("\\d{1,9}")) {
                n.put(Integer.parseInt(trimmed), target);
            } else if (!slug(trimmed).isEmpty()) {
                a.put(slug(trimmed), target);
            }
        });
        return new SatNames(a, n, entries);
    }

    /**
     * The satKey of a name or NORAD number.
     *
     * @return the key, or {@code unknown} for a blank name
     */
    public String key(String nameOrNorad) {
        if (nameOrNorad == null || nameOrNorad.isBlank()) {
            return "unknown";
        }
        String trimmed = nameOrNorad.trim();
        if (trimmed.matches("\\d{1,9}")) {
            String byNorad = norads.get(Integer.parseInt(trimmed));
            if (byNorad != null) {
                return byNorad;
            }
        }
        String slug = slug(trimmed);
        if (slug.isEmpty()) {
            return "unknown";
        }
        return aliases.getOrDefault(slug, slug);
    }

    /** The table entry of {@code key}, if any. */
    public Optional<Entry> entry(String key) {
        return Optional.ofNullable(entries.get(key));
    }

    /** The display name of {@code key}, or {@code fallback} (or the key) when the table has none. */
    public String displayName(String key, String fallback) {
        Entry entry = entries.get(key);
        if (entry != null) {
            return entry.name();
        }
        return fallback == null || fallback.isBlank() ? key : fallback.trim();
    }

    /** The NORAD number of {@code key}, or null. */
    public Integer norad(String key) {
        Entry entry = entries.get(key);
        return entry == null ? null : entry.norad();
    }

    /** Every entry of the table, in file order. */
    public List<Entry> entries() {
        return new ArrayList<>(entries.values());
    }

    /** Lower case, every run of characters other than {@code a-z0-9} replaced by one {@code -}, trimmed. */
    public static String slug(String text) {
        if (text == null) {
            return "";
        }
        String lower = text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
        int start = 0;
        int end = lower.length();
        while (start < end && lower.charAt(start) == '-') {
            start++;
        }
        while (end > start && lower.charAt(end - 1) == '-') {
            end--;
        }
        return lower.substring(start, end);
    }
}
