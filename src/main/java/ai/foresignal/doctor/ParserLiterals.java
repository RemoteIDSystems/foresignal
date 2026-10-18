package ai.foresignal.doctor;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * "Paths the parser reads" (prompt 140, task 3): a static scan of the adapter source for string literals, matched
 * against the names in structure paths. A cheap heuristic, and every finding that relies on it says so: a parser can
 * read a field through a constant built at run time, and a literal can name something that is not a field.
 *
 * <p>Literals are split on {@code .}, {@code /} and {@code @} as well, so a JSON pointer ({@code "/properties/mag"})
 * or a dotted path ({@code "properties.mag"}) counts for each of its names.
 */
public final class ParserLiterals implements Predicate<String> {

    /** The evidence line attached to every finding that relies on this scan. */
    public static final String HEURISTIC_NOTE =
            "heuristic: the adapter source contains this name as a string literal (static scan, not proof the parser"
                    + " reads it)";

    private static final Pattern LITERAL = Pattern.compile("\"((?:[^\"\\\\\\n]|\\\\.){1,200})\"");

    /** No literals: nothing counts as read. */
    public static final ParserLiterals NONE = new ParserLiterals(Set.of());

    private final Set<String> names;

    private ParserLiterals(Set<String> names) {
        this.names = Set.copyOf(names);
    }

    /** Scans the given Java sources. */
    public static ParserLiterals scan(Collection<String> sources) {
        Set<String> out = new TreeSet<>();
        if (sources != null) {
            for (String source : sources) {
                if (source == null) {
                    continue;
                }
                Matcher m = LITERAL.matcher(source);
                while (m.find()) {
                    String literal = m.group(1);
                    out.add(literal);
                    for (String part : literal.split("[./@\\[\\]]")) {
                        if (!part.isBlank()) {
                            out.add(part);
                        }
                    }
                }
            }
        }
        return new ParserLiterals(out);
    }

    /** The literals and their parts. */
    public Set<String> names() {
        return names;
    }

    /** True when there were no sources to scan, so no path can be judged. */
    public boolean isEmpty() {
        return names.isEmpty();
    }

    /** True when the last name of {@code path} appears as a literal. */
    @Override
    public boolean test(String path) {
        if (path == null || names.isEmpty()) {
            return false;
        }
        String leaf = StructureDiff.leafName(path);
        return !leaf.isEmpty() && names.contains(leaf);
    }
}
