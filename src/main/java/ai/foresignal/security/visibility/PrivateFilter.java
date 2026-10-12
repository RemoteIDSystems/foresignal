package ai.foresignal.security.visibility;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * The private-source exclusion of one caller (prompt 039): the private sources it may not see. Every
 * query builder applies it, either as SQL ({@link #sql(String)}) or as the excluded-source set the
 * builders already take ({@link #hidden()}, bound as a parameter); in memory {@link #allows(String)}
 * tests one source.
 *
 * <p>Built by {@link SourceVisibility#filterFor}. {@link #NONE} hides nothing (admin, or a token or feed
 * key with the private grant).
 *
 * @param hidden the private source ids the caller may not see
 */
public record PrivateFilter(Set<String> hidden) {

    /** Hides nothing. */
    public static final PrivateFilter NONE = new PrivateFilter(Set.of());

    /** Source ids are kebab-case; anything else is never inlined into SQL. */
    private static final Pattern SAFE_ID = Pattern.compile("^[A-Za-z0-9._:-]+$");

    /** Copies the set, sorted. */
    public PrivateFilter {
        hidden = hidden == null ? Set.of() : java.util.Collections.unmodifiableSet(new TreeSet<>(hidden));
    }

    /** True when records of {@code sourceId} may be shown to the caller. */
    public boolean allows(String sourceId) {
        return sourceId != null && !hidden.contains(sourceId);
    }

    /** True when nothing is hidden. */
    public boolean isEmpty() {
        return hidden.isEmpty();
    }

    /**
     * A SQL predicate keeping hidden sources out of rows aliased {@code alias} (for example
     * {@code h} gives {@code h.source_id NOT IN ('a','b')}); {@code TRUE} when nothing is hidden.
     * Only safe ids are inlined; an id with any other character fails closed ({@code FALSE}).
     *
     * @param alias the table alias owning {@code source_id}, or null/blank for an unqualified column
     */
    public String sql(String alias) {
        if (hidden.isEmpty()) {
            return "TRUE";
        }
        StringBuilder out = new StringBuilder();
        out.append(alias == null || alias.isBlank() ? "" : alias + ".").append("source_id NOT IN (");
        boolean first = true;
        for (String id : hidden) {
            if (!SAFE_ID.matcher(id).matches()) {
                return "FALSE";
            }
            if (!first) {
                out.append(',');
            }
            out.append('\'').append(id).append('\'');
            first = false;
        }
        return out.append(')').toString();
    }

    /** This filter plus {@code more} hidden sources. */
    public PrivateFilter plus(Set<String> more) {
        Objects.requireNonNull(more, "more");
        if (more.isEmpty()) {
            return this;
        }
        Set<String> next = new TreeSet<>(hidden);
        next.addAll(more);
        return new PrivateFilter(next);
    }
}
