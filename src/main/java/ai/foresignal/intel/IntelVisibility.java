package ai.foresignal.intel;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.visibility.SourceVisibility;
import java.util.Collections;
import java.util.Set;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Who may see a private intelligence event or investigation item (prompt 101, invariant EGR-1).
 *
 * <p>An event is {@code private} when any member comes from a private source (039). It follows the
 * private-source rule on every path: it is visible only to a caller that may see private sources (an
 * {@code admin} token, or a token, feed key or ticket with the private grant), whose restriction does
 * not exclude private sources, and from whom <em>no</em> private source is hidden. The last condition
 * is what makes the rule strict for callers whose excluded sources were widened on purpose, such as the
 * tool caller of an LLM destination that may not receive private records ({@code
 * EgressPolicy.llmExcludedSources}): such a caller never sees a private event.
 *
 * <p>An event the caller may not see is invisible, not forbidden: reads answer {@code 404
 * UNKNOWN_EVENT} and lists leave it out.
 */
@Component
public class IntelVisibility {

    private final SourceVisibility visibility;
    private final ObjectProvider<ReadCallerResolver> callers;

    /** Creates the component. */
    public IntelVisibility(@Lazy SourceVisibility visibility, ObjectProvider<ReadCallerResolver> callers) {
        this.visibility = visibility;
        this.callers = callers;
    }

    /** {@link #seesPrivate(Scope, boolean, Set, ReadRestriction)} for the caller of a REST request. */
    public boolean seesPrivate(HttpServletRequest request) {
        ReadCallerResolver resolver = callers == null ? null : callers.getIfAvailable();
        if (resolver == null) {
            return false;
        }
        return seesPrivate(resolver.resolve(request), resolver.excludedSources(request));
    }

    /**
     * True when a caller may see private events.
     *
     * @param scope the caller's scope, null when anonymous
     * @param privateAccess the caller's private grant
     * @param excludedSources the sources the caller may not read (private and restriction-hidden)
     * @param restriction the caller's read restriction, or null
     */
    public boolean seesPrivate(Scope scope, boolean privateAccess, Set<String> excludedSources,
            ReadRestriction restriction) {
        if (!SourceVisibility.canSeePrivate(scope, privateAccess)) {
            return false;
        }
        if (restriction != null && restriction.excludePrivate()) {
            return false;
        }
        Set<String> excluded = excludedSources == null ? Set.of() : excludedSources;
        if (excluded.isEmpty()) {
            return true;
        }
        try {
            return Collections.disjoint(excluded, visibility.privateSourceIds());
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** {@link #seesPrivate(Scope, boolean, Set, ReadRestriction)} for a REST read caller. */
    public boolean seesPrivate(ReadCaller caller, Set<String> excludedSources) {
        return seesPrivate(caller.scope(), caller.privateAccess(), excludedSources, caller.restriction());
    }

    /** {@link #seesPrivate(Scope, boolean, Set, ReadRestriction)} for an MCP (or tool-loop) caller. */
    public boolean seesPrivate(McpCaller caller) {
        return seesPrivate(caller.scope(), caller.readCaller().privateAccess(), caller.excludedSources(),
                caller.restriction());
    }

    /** True when {@code event} is visible to a caller with {@code privateAccess}. */
    public static boolean visible(IntelligenceEventEntity event, boolean privateAccess) {
        return event != null && (!event.isPrivateEvent() || privateAccess);
    }

    /** True when records of {@code sourceId} are private (descriptor or operator). */
    public boolean isPrivateSource(String sourceId) {
        try {
            return visibility.isPrivate(sourceId);
        } catch (RuntimeException e) {
            // Fail closed: an unreadable visibility state treats the source as private.
            return true;
        }
    }

    /** Every private source id. */
    public Set<String> privateSourceIds() {
        try {
            return visibility.privateSourceIds();
        } catch (RuntimeException e) {
            return Set.of();
        }
    }
}
