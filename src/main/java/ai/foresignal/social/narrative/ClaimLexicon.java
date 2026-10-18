package ai.foresignal.social.narrative;

import ai.foresignal.social.AhoCorasick;
import ai.foresignal.social.TextFolding;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.json.JsonMapper;

/**
 * The claim lexicon (prompt 136, {@code src/main/resources/intel/social/claim-lexicon.json}): event types with their
 * 119 signal families and multilingual terms, negation, denial, question and hedge markers, actor names and place
 * aliases in other scripts. Terms are folded ({@link TextFolding#fold}) and matched in one pass with an
 * {@link AhoCorasick} automaton on word boundaries ({@link TextFolding#onBoundaries}, which also accepts Arabic
 * proclitics and Hebrew prefix letters); a term ending in {@code *} matches any word starting with it. Immutable.
 */
public final class ClaimLexicon {

    /** The bundled resource. */
    public static final String RESOURCE = "intel/social/claim-lexicon.json";

    /** What a lexicon term marks. */
    public enum Kind {
        /** An event term of a claim type. */
        TYPE,
        /** A negation word (within three words before an event term). */
        NEGATION,
        /** A denial marker (anywhere in the post). */
        DENIAL,
        /** A question marker. */
        QUESTION,
        /** A hedge marker. */
        HEDGE,
        /** An actor name. */
        ACTOR
    }

    /**
     * One claim type.
     *
     * @param id the lexicon id
     * @param signalFamily the 119 signal family
     */
    public record Type(String id, String signalFamily) {}

    /**
     * What one folded term stands for.
     *
     * @param kind the kind
     * @param id the type id or actor name (null for markers)
     * @param language the language, or null
     * @param prefix true for a {@code *} term
     */
    public record Term(Kind kind, String id, String language, boolean prefix) {}

    /**
     * A match in a folded text.
     *
     * @param kind the kind
     * @param id the type id or actor name, or null
     * @param start the start index in the folded text
     * @param end the end index (exclusive; for a prefix term, the end of the word)
     */
    public record Hit(Kind kind, String id, int start, int end) {}

    private static volatile ClaimLexicon bundled;

    private final String version;
    private final Map<String, Type> types;
    private final Map<String, String> placeAliases;
    private final AhoCorasick<List<Term>> automaton;

    private ClaimLexicon(String version, Map<String, Type> types, Map<String, String> placeAliases,
            Map<String, List<Term>> terms) {
        this.version = version;
        this.types = Map.copyOf(types);
        this.placeAliases = Map.copyOf(placeAliases);
        this.automaton = new AhoCorasick<>(terms);
    }

    /** The bundled lexicon (loaded once). */
    public static ClaimLexicon bundled() {
        ClaimLexicon current = bundled;
        if (current == null) {
            try (InputStream in = new ClassPathResource(RESOURCE).getInputStream()) {
                current = parse(JsonMapper.builder().build().readValue(in, Map.class));
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot read " + RESOURCE, e);
            }
            bundled = current;
        }
        return current;
    }

    /** Parses a lexicon document. */
    public static ClaimLexicon parse(Map<?, ?> doc) {
        Map<String, Type> types = new LinkedHashMap<>();
        Map<String, List<Term>> terms = new LinkedHashMap<>();
        for (Object item : list(doc.get("types"))) {
            if (!(item instanceof Map<?, ?> t) || t.get("id") == null) {
                continue;
            }
            String id = t.get("id").toString();
            types.put(id, new Type(id, t.get("signalFamily") == null ? null : t.get("signalFamily").toString()));
            addLanguages(terms, t.get("terms"), Kind.TYPE, id);
        }
        addLanguages(terms, doc.get("negations"), Kind.NEGATION, null);
        addLanguages(terms, doc.get("denials"), Kind.DENIAL, null);
        addLanguages(terms, doc.get("questions"), Kind.QUESTION, null);
        addLanguages(terms, doc.get("hedges"), Kind.HEDGE, null);
        for (Object item : list(doc.get("actors"))) {
            if (item instanceof Map<?, ?> a && a.get("name") != null) {
                for (Object term : list(a.get("terms"))) {
                    add(terms, term == null ? null : term.toString(), Kind.ACTOR, a.get("name").toString(), null);
                }
            }
        }
        Map<String, String> aliases = new LinkedHashMap<>();
        if (doc.get("placeAliases") instanceof Map<?, ?> m) {
            for (Map.Entry<?, ?> e : m.entrySet()) {
                String folded = TextFolding.fold(String.valueOf(e.getKey()));
                if (!folded.isEmpty() && e.getValue() != null) {
                    aliases.put(folded, e.getValue().toString());
                }
            }
        }
        return new ClaimLexicon(doc.get("version") == null ? "0" : doc.get("version").toString(), types, aliases,
                terms);
    }

    private static void addLanguages(Map<String, List<Term>> terms, Object byLanguage, Kind kind, String id) {
        if (!(byLanguage instanceof Map<?, ?> m)) {
            return;
        }
        for (Map.Entry<?, ?> e : m.entrySet()) {
            for (Object term : list(e.getValue())) {
                add(terms, term == null ? null : term.toString(), kind, id, String.valueOf(e.getKey()));
            }
        }
    }

    private static void add(Map<String, List<Term>> terms, String raw, Kind kind, String id, String language) {
        if (raw == null || raw.isBlank()) {
            return;
        }
        boolean prefix = raw.strip().endsWith("*");
        String folded = TextFolding.fold(prefix ? raw.strip().substring(0, raw.strip().length() - 1) : raw);
        if (folded.isEmpty()) {
            return;
        }
        Term term = new Term(kind, id, language, prefix);
        List<Term> list = terms.computeIfAbsent(folded, k -> new ArrayList<>());
        if (!list.contains(term)) {
            list.add(term);
        }
    }

    private static List<?> list(Object value) {
        return value instanceof Collection<?> c ? new ArrayList<>(c) : List.of();
    }

    /** The lexicon version. */
    public String version() {
        return version;
    }

    /** The claim types by id. */
    public Map<String, Type> types() {
        return types;
    }

    /** The type with an id, or null. */
    public Type type(String id) {
        return id == null ? null : types.get(id);
    }

    /** Folded alias to the gazetteer name it stands for. */
    public Map<String, String> placeAliases() {
        return placeAliases;
    }

    /**
     * Every lexicon match in a folded text, by start; overlapping matches of one kind keep the longest.
     *
     * @param folded the folded text
     */
    public List<Hit> scan(String folded) {
        List<Hit> raw = new ArrayList<>();
        for (AhoCorasick.Hit<List<Term>> h : automaton.search(folded)) {
            for (Term t : h.value()) {
                int end = h.end();
                if (t.prefix()) {
                    while (end < folded.length() && folded.charAt(end) != ' ') {
                        end++;
                    }
                    if (!TextFolding.onBoundaries(folded, folded.substring(h.start(), end), h.start(), end)) {
                        continue;
                    }
                } else if (!TextFolding.onBoundaries(folded, h.pattern(), h.start(), h.end())) {
                    continue;
                }
                raw.add(new Hit(t.kind(), t.id(), h.start(), end));
            }
        }
        raw.sort((a, b) -> a.start() != b.start() ? Integer.compare(a.start(), b.start())
                : Integer.compare(b.end() - b.start(), a.end() - a.start()));
        List<Hit> out = new ArrayList<>();
        for (Hit h : raw) {
            boolean covered = false;
            for (Hit kept : out) {
                if (kept.kind() == h.kind() && kept.start() <= h.start() && kept.end() >= h.end()) {
                    covered = true;
                    break;
                }
            }
            if (!covered) {
                out.add(h);
            }
        }
        return out;
    }
}
