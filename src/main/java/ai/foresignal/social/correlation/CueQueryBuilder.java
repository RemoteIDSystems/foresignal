package ai.foresignal.social.correlation;

import ai.foresignal.social.narrative.ClaimLexicon;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.json.JsonMapper;

/**
 * Builds the exact queries of a cue (prompt 137): the place (036/040 names, aliases in other scripts from the claim
 * lexicon, nearby facilities from 098/104, entity names), the event (claim-lexicon terms of the cue's signal families
 * in the region's languages, plus the caller's extra terms), one query per language and search-capable platform in
 * that platform's syntax. Pure apart from reading the bundled lexicon and the region-language table.
 */
public final class CueQueryBuilder {

    /** The region-language table. */
    public static final String LANGUAGES_RESOURCE = "intel/social/region-languages.json";

    /** The most names per query. */
    static final int MAX_NAMES = 6;

    /** The most event terms per query. */
    static final int MAX_TERMS = 8;

    /** The most languages per cue. */
    static final int MAX_LANGUAGES = 4;

    /** The longest query. */
    static final int MAX_QUERY = 1024;

    private static volatile Map<String, List<String>> regionLanguages;
    private static volatile Map<String, Map<String, List<String>>> typeTerms;
    private static volatile Map<String, String> typeFamilies;

    private CueQueryBuilder() {}

    /**
     * What a cue searches for.
     *
     * @param placeNames place and region names and aliases (most specific first)
     * @param facilityNames nearby port, airport or facility names
     * @param entityNames entity names and keys
     * @param families the signal families whose lexicon terms are added
     * @param extraTerms the caller's extra terms
     * @param languages the languages to search in (English is always added)
     */
    public record Spec(List<String> placeNames, List<String> facilityNames, List<String> entityNames,
            List<String> families, List<String> extraTerms, List<String> languages) {

        /** Copies the lists. */
        public Spec {
            placeNames = placeNames == null ? List.of() : List.copyOf(placeNames);
            facilityNames = facilityNames == null ? List.of() : List.copyOf(facilityNames);
            entityNames = entityNames == null ? List.of() : List.copyOf(entityNames);
            families = families == null ? List.of() : List.copyOf(families);
            extraTerms = extraTerms == null ? List.of() : List.copyOf(extraTerms);
            languages = languages == null ? List.of() : List.copyOf(languages);
        }
    }

    /**
     * One exact query.
     *
     * @param platform the adapter
     * @param query the query string
     * @param language the language, or null for a language-neutral query
     */
    public record Query(String platform, String query, String language) {

        /** The {@code SocialCueQuery} view (without cost). */
        public Map<String, Object> toMap(Double estimatedCostUsd) {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("platform", platform);
            out.put("query", query);
            out.put("language", language);
            out.put("estimatedCostUsd", estimatedCostUsd);
            return out;
        }
    }

    /** The languages of a region (country of an admin-1 code), English first. */
    public static List<String> languages(String regionCode) {
        Set<String> out = new LinkedHashSet<>();
        out.add("en");
        if (regionCode != null && regionCode.length() >= 2) {
            out.addAll(regionLanguages().getOrDefault(regionCode.substring(0, 2), List.of()));
        }
        return new ArrayList<>(out);
    }

    /** The lexicon terms of the families' claim types per language ({@code *} prefixes cut to their stems). */
    public static Map<String, List<String>> familyTerms(List<String> families, List<String> languages) {
        Map<String, List<String>> out = new LinkedHashMap<>();
        loadLexicon();
        for (Map.Entry<String, Map<String, List<String>>> type : typeTerms.entrySet()) {
            if (!families.contains(typeFamilies.get(type.getKey()))) {
                continue;
            }
            for (String language : languages) {
                for (String term : type.getValue().getOrDefault(language, List.of())) {
                    String t = term.endsWith("*") ? term.substring(0, term.length() - 1) : term;
                    if (t.length() >= 3) {
                        List<String> list = out.computeIfAbsent(language, k -> new ArrayList<>());
                        if (!list.contains(t)) {
                            list.add(t);
                        }
                    }
                }
            }
        }
        return out;
    }

    /** The other-script aliases of a gazetteer name (the claim lexicon's {@code placeAliases}, reversed). */
    public static List<String> aliases(String name) {
        List<String> out = new ArrayList<>();
        if (name == null) {
            return out;
        }
        for (Map.Entry<String, String> e : ClaimLexicon.bundled().placeAliases().entrySet()) {
            if (e.getValue().equalsIgnoreCase(name.strip())) {
                out.add(e.getKey());
            }
        }
        return out;
    }

    /**
     * The queries of a cue on the given platforms.
     *
     * @param spec what to search for
     * @param platforms the adapters
     * @return the queries, one per language and platform (none when the spec names no place, facility or entity)
     */
    public static List<Query> build(Spec spec, List<String> platforms) {
        List<String> names = new ArrayList<>(dedupe(spec.placeNames(), spec.facilityNames(), spec.entityNames()));
        if (names.isEmpty()) {
            return List.of();
        }
        List<String> langs = new ArrayList<>(new LinkedHashSet<>(spec.languages().isEmpty() ? List.of("en")
                : spec.languages()));
        if (!langs.contains("en")) {
            langs.add(0, "en");
        }
        langs = langs.subList(0, Math.min(MAX_LANGUAGES, langs.size()));
        Map<String, List<String>> terms = familyTerms(spec.families(), langs);
        List<Query> out = new ArrayList<>();
        for (String language : langs) {
            List<String> eventTerms = new ArrayList<>(dedupe(spec.extraTerms(), terms.getOrDefault(language,
                    List.of())));
            if (eventTerms.isEmpty() && !"en".equals(language)) {
                continue;
            }
            List<String> n = names.subList(0, Math.min(MAX_NAMES, names.size()));
            List<String> t = eventTerms.subList(0, Math.min(MAX_TERMS, eventTerms.size()));
            for (String platform : platforms) {
                String q = query(platform, n, t, language);
                if (q != null && !q.isBlank()) {
                    out.add(new Query(platform, q.length() > MAX_QUERY ? q.substring(0, MAX_QUERY) : q, language));
                }
            }
        }
        return out;
    }

    /** One query in a platform's syntax. */
    static String query(String platform, List<String> names, List<String> terms, String language) {
        return switch (platform) {
            case "threads" -> names.get(0) + (terms.isEmpty() ? "" : " " + terms.get(0));
            case "x" -> group(names) + (terms.isEmpty() ? "" : " " + group(terms)) + " -is:retweet"
                    + (language == null ? "" : " lang:" + language);
            default -> group(names) + (terms.isEmpty() ? "" : " AND " + group(terms));
        };
    }

    /** {@code ("a" OR "b")}, or {@code "a"} for one item. */
    static String group(List<String> items) {
        List<String> quoted = new ArrayList<>();
        for (String i : items) {
            quoted.add("\"" + i.replace("\"", "") + "\"");
        }
        return quoted.size() == 1 ? quoted.get(0) : "(" + String.join(" OR ", quoted) + ")";
    }

    @SafeVarargs
    private static Set<String> dedupe(List<String>... lists) {
        Set<String> out = new LinkedHashSet<>();
        Set<String> lower = new LinkedHashSet<>();
        for (List<String> list : lists) {
            for (String s : list) {
                if (s != null && !s.isBlank() && lower.add(s.strip().toLowerCase(java.util.Locale.ROOT))) {
                    out.add(s.strip());
                }
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, List<String>> regionLanguages() {
        Map<String, List<String>> current = regionLanguages;
        if (current == null) {
            try (InputStream in = new ClassPathResource(LANGUAGES_RESOURCE).getInputStream()) {
                Map<String, Object> doc = JsonMapper.builder().build().readValue(in, Map.class);
                Map<String, List<String>> m = new LinkedHashMap<>();
                ((Map<String, Object>) doc.getOrDefault("regions", Map.of())).forEach((k, v) -> m.put(k,
                        CompareRules.strings(v)));
                current = Map.copyOf(m);
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot read " + LANGUAGES_RESOURCE, e);
            }
            regionLanguages = current;
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    private static synchronized void loadLexicon() {
        if (typeTerms != null) {
            return;
        }
        try (InputStream in = new ClassPathResource(ClaimLexicon.RESOURCE).getInputStream()) {
            Map<String, Object> doc = JsonMapper.builder().build().readValue(in, Map.class);
            Map<String, Map<String, List<String>>> terms = new LinkedHashMap<>();
            Map<String, String> families = new LinkedHashMap<>();
            for (Object o : (List<Object>) doc.getOrDefault("types", List.of())) {
                if (!(o instanceof Map<?, ?> t) || t.get("id") == null) {
                    continue;
                }
                String id = t.get("id").toString();
                families.put(id, t.get("signalFamily") == null ? null : t.get("signalFamily").toString());
                Map<String, List<String>> byLanguage = new LinkedHashMap<>();
                if (t.get("terms") instanceof Map<?, ?> m) {
                    m.forEach((k, v) -> byLanguage.put(String.valueOf(k), CompareRules.strings(v)));
                }
                terms.put(id, byLanguage);
            }
            typeFamilies = families;
            typeTerms = terms;
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + ClaimLexicon.RESOURCE, e);
        }
    }
}
