package ai.foresignal.doctor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * The structural difference between the recorded fixture and a probe body (OpenAPI {@code StructureDiff}): paths and
 * types, never bodies beyond the capped examples of the paths that changed.
 *
 * @param format the probe body's format
 * @param fixtureFile the fixture compared against, or null when there is none
 * @param addedPaths paths in the probe but not in the fixture
 * @param removedPaths paths in the fixture but not in the probe
 * @param typeChanged paths whose non-null types no longer overlap
 * @param presenceDropped paths whose presence ratio fell by at least 50 points
 * @param arrayEmpty array paths that held elements in the fixture and are empty in the probe
 * @param columnsReordered CSV/TSV: the same header columns in another order
 * @param delimiterChanged CSV/TSV: the delimiter changed, or null
 * @param contentTypeChanged the media type changed, or null
 * @param sizeRatio probe bytes / fixture bytes, or null
 * @param renameCandidates removed/added pairs under the same parent that look like a rename, best first
 * @param htmlTitle the {@code <title>} when HTML arrived instead of data, or null
 * @param examples up to 3 capped example values of each added, removed or retyped path
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record StructureDiff(
        String format,
        String fixtureFile,
        List<String> addedPaths,
        List<String> removedPaths,
        List<TypeChange> typeChanged,
        List<PresenceDrop> presenceDropped,
        List<String> arrayEmpty,
        boolean columnsReordered,
        Change delimiterChanged,
        Change contentTypeChanged,
        Double sizeRatio,
        List<RenameCandidate> renameCandidates,
        String htmlTitle,
        Map<String, List<String>> examples) {

    /** The presence drop, in ratio points, that counts as {@code presenceDropped}. */
    public static final double PRESENCE_DROP = 0.5;

    /** The lowest score a rename candidate needs to be listed. */
    public static final double MIN_CANDIDATE_SCORE = 0.5;

    /** The normalized Levenshtein similarity at which two names count as similar. */
    public static final double NAME_SIMILARITY = 0.6;

    /** The most paths listed per category. */
    public static final int MAX_LISTED = 200;

    /** Copies the collections. */
    public StructureDiff {
        addedPaths = addedPaths == null ? List.of() : List.copyOf(addedPaths);
        removedPaths = removedPaths == null ? List.of() : List.copyOf(removedPaths);
        typeChanged = typeChanged == null ? List.of() : List.copyOf(typeChanged);
        presenceDropped = presenceDropped == null ? List.of() : List.copyOf(presenceDropped);
        arrayEmpty = arrayEmpty == null ? List.of() : List.copyOf(arrayEmpty);
        renameCandidates = renameCandidates == null ? List.of() : List.copyOf(renameCandidates);
        examples = examples == null ? Map.of() : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(examples));
    }

    /**
     * A type change.
     *
     * @param path the path
     * @param from the fixture's types, joined with {@code |}
     * @param to the probe's types, joined with {@code |}
     */
    public record TypeChange(String path, String from, String to) {}

    /**
     * A presence drop.
     *
     * @param path the path
     * @param from the fixture's presence ratio
     * @param to the probe's presence ratio
     */
    public record PresenceDrop(String path, double from, double to) {}

    /**
     * A changed scalar (delimiter, content type).
     *
     * @param from the fixture's value
     * @param to the probe's value
     */
    public record Change(String from, String to) {}

    /**
     * A removed path paired with an added path under the same parent.
     *
     * @param from the removed path
     * @param to the added path
     * @param score 0..1, higher is likelier
     * @param reason {@code name-similarity}, {@code snake-camel} or {@code value-distribution}
     * @param readByParser true when the adapter source contains the removed path's name as a string literal
     *     (a heuristic)
     */
    public record RenameCandidate(String from, String to, double score, String reason, boolean readByParser) {}

    /** True when nothing structural changed (the size ratio alone is not a change). */
    @JsonIgnore
    public boolean isUnchanged() {
        return addedPaths.isEmpty() && removedPaths.isEmpty() && typeChanged.isEmpty() && presenceDropped.isEmpty()
                && arrayEmpty.isEmpty() && !columnsReordered && delimiterChanged == null && contentTypeChanged == null
                && htmlTitle == null;
    }

    /** The first path that diverges (removed, retyped, added, dropped presence, emptied array), or null. */
    public String firstDivergentPath() {
        if (!removedPaths.isEmpty()) {
            return removedPaths.get(0);
        }
        if (!typeChanged.isEmpty()) {
            return typeChanged.get(0).path();
        }
        if (!addedPaths.isEmpty()) {
            return addedPaths.get(0);
        }
        if (!presenceDropped.isEmpty()) {
            return presenceDropped.get(0).path();
        }
        return arrayEmpty.isEmpty() ? null : arrayEmpty.get(0);
    }

    /**
     * Compares a fixture's structure with a probe's.
     *
     * @param fixture the fixture's structure, or null when there is no fixture (the diff is then only the probe's
     *     format and HTML title)
     * @param probe the probe's structure
     * @param fixtureFile the fixture's file name, or null
     * @param readByParser decides whether the parser reads a path (see {@link ParserLiterals}); null for never
     */
    public static StructureDiff compare(Structure fixture, Structure probe, String fixtureFile,
            Predicate<String> readByParser) {
        Objects.requireNonNull(probe, "probe");
        Predicate<String> reads = readByParser == null ? p -> false : readByParser;
        boolean htmlInstead = probe.format() == StructureFormat.HTML
                && (fixture == null || fixture.format() != StructureFormat.HTML);
        String htmlTitle = htmlInstead ? (probe.htmlTitle() == null ? "" : probe.htmlTitle()) : null;
        if (fixture == null) {
            return new StructureDiff(probe.format().wireValue(), null, List.of(), List.of(), List.of(), List.of(),
                    List.of(), false, null, null, null, List.of(), htmlTitle, Map.of());
        }
        Map<String, Structure.PathStats> before = fixture.paths();
        Map<String, Structure.PathStats> after = probe.paths();
        List<String> added = new ArrayList<>();
        List<String> removed = new ArrayList<>();
        List<TypeChange> typeChanged = new ArrayList<>();
        List<PresenceDrop> presence = new ArrayList<>();
        if (!htmlInstead) {
            for (String path : after.keySet()) {
                if (!before.containsKey(path)) {
                    added.add(path);
                }
            }
            for (Map.Entry<String, Structure.PathStats> e : before.entrySet()) {
                Structure.PathStats now = after.get(e.getKey());
                if (now == null) {
                    removed.add(e.getKey());
                    continue;
                }
                Set<String> from = e.getValue().nonNullTypes();
                Set<String> to = now.nonNullTypes();
                if (!from.isEmpty() && !to.isEmpty() && disjoint(from, to)) {
                    typeChanged.add(new TypeChange(e.getKey(), String.join("|", from), String.join("|", to)));
                }
                if (e.getValue().presence() - now.presence() >= PRESENCE_DROP) {
                    presence.add(new PresenceDrop(e.getKey(), round(e.getValue().presence()), round(now.presence())));
                }
            }
        }
        List<String> arrayEmpty = new ArrayList<>();
        fixture.arrays().forEach((path, stats) -> {
            Structure.ArrayStats now = probe.arrays().get(path);
            if (stats.maxLength() > 0 && now != null && now.maxLength() == 0) {
                arrayEmpty.add(path);
            }
        });
        // The element paths of an emptied array are not removed fields: there is simply nothing in the array.
        removed.removeIf(path -> arrayEmpty.stream().anyMatch(empty -> path.startsWith(empty + "[]")));

        // A removed parent implies its children; keep the list readable by dropping descendants of a removed path.
        List<String> removedTop = topLevel(removed);
        List<String> addedTop = topLevel(added);

        boolean reordered = !fixture.columns().isEmpty() && fixture.header() && probe.header()
                && new HashSet<>(fixture.columns()).equals(new HashSet<>(probe.columns()))
                && !fixture.columns().equals(probe.columns());
        Change delimiter = null;
        if (fixture.delimiter() != null && probe.delimiter() != null
                && !fixture.delimiter().equals(probe.delimiter())) {
            delimiter = new Change(fixture.delimiter(), probe.delimiter());
        }
        Change contentType = null;
        String mediaBefore = fixture.mediaType();
        String mediaAfter = probe.mediaType();
        if (mediaBefore != null && mediaAfter != null && !mediaBefore.equals(mediaAfter)) {
            contentType = new Change(mediaBefore, mediaAfter);
        }
        Double sizeRatio = fixture.bytes() > 0 && probe.bytes() > 0 ? round(probe.bytes() / (double) fixture.bytes())
                : null;

        List<RenameCandidate> candidates = htmlInstead ? List.of() : renameCandidates(removed, added, before, after,
                reads);
        // A path explained by a rename is listed as removed/added still, so the diff stays a plain set difference.
        Map<String, List<String>> examples = new LinkedHashMap<>();
        for (String path : removedTop) {
            putExamples(examples, path, before.get(path));
        }
        for (String path : addedTop) {
            putExamples(examples, path, after.get(path));
        }
        for (TypeChange t : typeChanged) {
            putExamples(examples, t.path(), after.get(t.path()));
        }
        return new StructureDiff(probe.format().wireValue(), fixtureFile, cap(addedTop), cap(removedTop),
                typeChanged.subList(0, Math.min(MAX_LISTED, typeChanged.size())),
                presence.subList(0, Math.min(MAX_LISTED, presence.size())), cap(arrayEmpty), reordered, delimiter,
                contentType, sizeRatio, candidates, htmlTitle, examples);
    }

    private static void putExamples(Map<String, List<String>> examples, String path, Structure.PathStats stats) {
        if (stats != null && !stats.examples().isEmpty() && examples.size() < MAX_LISTED) {
            examples.put(path, stats.examples());
        }
    }

    private static List<String> cap(List<String> paths) {
        return paths.subList(0, Math.min(MAX_LISTED, paths.size()));
    }

    /** The paths not below another path of the list. */
    static List<String> topLevel(List<String> paths) {
        Set<String> all = new LinkedHashSet<>(paths);
        List<String> out = new ArrayList<>();
        for (String path : paths) {
            boolean covered = false;
            String parent = parentOf(path);
            while (parent != null && !parent.isEmpty()) {
                if (all.contains(parent) || all.contains(stripArray(parent))) {
                    covered = true;
                    break;
                }
                parent = parentOf(parent);
            }
            if (!covered) {
                out.add(path);
            }
        }
        return out;
    }

    private static String stripArray(String path) {
        return path.endsWith("[]") ? path.substring(0, path.length() - 2) : path;
    }

    /**
     * The parent of a path: {@code a.b[].c} → {@code a.b[]}, {@code a.b[]} → {@code a.b}, {@code e@x} → {@code e},
     * a top-level name → the empty string.
     */
    static String parentOf(String path) {
        if (path.endsWith("[]")) {
            return path.substring(0, path.length() - 2);
        }
        int at = path.lastIndexOf('@');
        int dot = path.lastIndexOf('.');
        int cut = Math.max(at, dot);
        return cut < 0 ? "" : path.substring(0, cut);
    }

    /** The last name of a path, without {@code []} or {@code @}: {@code features[].properties.mag} → {@code mag}. */
    static String leafName(String path) {
        String p = stripArray(path);
        while (p.endsWith("[]")) {
            p = stripArray(p);
        }
        int cut = Math.max(p.lastIndexOf('.'), p.lastIndexOf('@'));
        return cut < 0 ? p : p.substring(cut + 1);
    }

    private static boolean disjoint(Set<String> a, Set<String> b) {
        Set<String> na = numericAware(a);
        Set<String> nb = numericAware(b);
        for (String x : na) {
            if (nb.contains(x)) {
                return false;
            }
        }
        return true;
    }

    /** Integer and number count as one type (a value gaining decimals is not a format change). */
    private static Set<String> numericAware(Set<String> types) {
        Set<String> out = new HashSet<>();
        for (String t : types) {
            out.add(t.equals("integer") ? "number" : t);
        }
        return out;
    }

    private static double round(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }

    // ---------- rename candidates ----------

    static List<RenameCandidate> renameCandidates(List<String> removed, List<String> added,
            Map<String, Structure.PathStats> before, Map<String, Structure.PathStats> after, Predicate<String> reads) {
        List<RenameCandidate> all = new ArrayList<>();
        for (String r : removed) {
            for (String a : added) {
                if (!parentOf(r).equals(parentOf(a))) {
                    continue;
                }
                Structure.PathStats rs = before.get(r);
                Structure.PathStats as = after.get(a);
                if (rs == null || as == null || !compatible(rs, as)) {
                    continue;
                }
                RenameCandidate c = score(r, a, rs, as, reads.test(r));
                if (c != null && c.score() >= MIN_CANDIDATE_SCORE) {
                    all.add(c);
                }
            }
        }
        all.sort(Comparator.comparingDouble(RenameCandidate::score).reversed()
                .thenComparing(RenameCandidate::from).thenComparing(RenameCandidate::to));
        // Each path takes part in at most one candidate: the best one.
        Set<String> usedFrom = new HashSet<>();
        Set<String> usedTo = new HashSet<>();
        List<RenameCandidate> out = new ArrayList<>();
        for (RenameCandidate c : all) {
            if (usedFrom.contains(c.from()) || usedTo.contains(c.to())) {
                continue;
            }
            usedFrom.add(c.from());
            usedTo.add(c.to());
            out.add(c);
            if (out.size() >= 50) {
                break;
            }
        }
        return out;
    }

    /** True when the two paths' non-null types overlap (or one side was only ever null). */
    static boolean compatible(Structure.PathStats a, Structure.PathStats b) {
        Set<String> ta = a.nonNullTypes();
        Set<String> tb = b.nonNullTypes();
        return ta.isEmpty() || tb.isEmpty() || !disjoint(ta, tb);
    }

    /** Scores one pair, or null when no rule applies. */
    static RenameCandidate score(String from, String to, Structure.PathStats fs, Structure.PathStats ts,
            boolean readByParser) {
        String a = leafName(from);
        String b = leafName(to);
        double overlap = valueOverlap(fs, ts);
        if (!a.equals(b) && normalize(a).equals(normalize(b))) {
            return new RenameCandidate(from, to, 0.95, "snake-camel", readByParser);
        }
        double similarity = similarity(a.toLowerCase(Locale.ROOT), b.toLowerCase(Locale.ROOT));
        double nameScore = similarity >= NAME_SIMILARITY ? Math.min(1.0, similarity + (overlap > 0 ? 0.1 : 0)) : 0;
        double valueScore = 0;
        if (overlap > 0 && Math.abs(fs.presence() - ts.presence()) <= 0.3) {
            boolean contained = a.length() >= 2 && b.length() >= 2
                    && (b.toLowerCase(Locale.ROOT).contains(a.toLowerCase(Locale.ROOT))
                            || a.toLowerCase(Locale.ROOT).contains(b.toLowerCase(Locale.ROOT)));
            valueScore = Math.min(1.0, 0.55 + 0.35 * overlap + (contained ? 0.1 : 0));
        }
        if (nameScore == 0 && valueScore == 0) {
            return null;
        }
        if (nameScore >= valueScore) {
            return new RenameCandidate(from, to, round(nameScore), "name-similarity", readByParser);
        }
        return new RenameCandidate(from, to, round(valueScore), "value-distribution", readByParser);
    }

    /** A name without case, underscores or hyphens: {@code event_time}, {@code eventTime} → {@code eventtime}. */
    static String normalize(String name) {
        return name.replace("_", "").replace("-", "").toLowerCase(Locale.ROOT);
    }

    /** 1 − Levenshtein distance / longer length; 1 for two empty names. */
    static double similarity(String a, String b) {
        int max = Math.max(a.length(), b.length());
        return max == 0 ? 1.0 : 1.0 - levenshtein(a, b) / (double) max;
    }

    /** The Levenshtein edit distance. */
    static int levenshtein(String a, String b) {
        int[] previous = new int[b.length() + 1];
        int[] current = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            previous[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost);
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[b.length()];
    }

    /**
     * How much two value distributions overlap, 0..1: for numbers the intersection of the ranges over their union
     * (identical single values count as 1), otherwise the Jaccard index of the example sets.
     */
    static double valueOverlap(Structure.PathStats a, Structure.PathStats b) {
        if (a.min() != null && a.max() != null && b.min() != null && b.max() != null) {
            double lo = Math.max(a.min(), b.min());
            double hi = Math.min(a.max(), b.max());
            if (hi < lo) {
                return 0;
            }
            double union = Math.max(a.max(), b.max()) - Math.min(a.min(), b.min());
            return union == 0 ? 1.0 : (hi - lo) / union;
        }
        if (a.examples().isEmpty() || b.examples().isEmpty()) {
            return 0;
        }
        Set<String> inter = new HashSet<>(a.examples());
        inter.retainAll(new HashSet<>(b.examples()));
        Set<String> union = new HashSet<>(a.examples());
        union.addAll(b.examples());
        return union.isEmpty() ? 0 : inter.size() / (double) union.size();
    }
}
