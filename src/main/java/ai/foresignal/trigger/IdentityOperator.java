package ai.foresignal.trigger;

import java.util.Collection;
import java.util.Map;

/**
 * An {@link Operator} whose condition may omit its path and read, per source, the payload field
 * holding an identity (prompt 036: {@code inWatchlist} and {@code notInWatchlist}). The watchlist
 * module provides the beans; rules and views referencing one are sensitive.
 */
public interface IdentityOperator extends Operator {

    /**
     * The payload path of the identity {@code value} names (a watchlist id) for each of
     * {@code sources}; a source that does not declare the identity is absent and never matches.
     *
     * @param value the normalized condition value
     * @param sources the sources the rule or view watches; empty for every registered source
     */
    Map<String, String> identityPaths(Object value, Collection<String> sources);

    /**
     * The sources among {@code sources} (every registered source when empty) that do not declare the
     * identity {@code value} names, for validation warnings.
     */
    java.util.List<String> sourcesWithoutIdentity(Object value, Collection<String> sources);

    /** True when the identity of {@code value} needs an explicit path ({@code custom} lists, unknown lists). */
    boolean requiresPath(Object value);

    /** True when {@code value} names an existing watchlist. */
    boolean known(Object value);
}
