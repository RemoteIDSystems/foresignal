package ai.foresignal.security;

import ai.foresignal.api.error.ErrorCodes;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

/**
 * The default {@link RestrictionEnforcer} until prompt 035: a token with source, kind or bbox
 * restrictions gets {@code 403 RESTRICTION_NOT_ENFORCED} on every data-read path; everything else
 * passes (endpoint restrictions are the token filter's).
 *
 * <p>A data-read path is a {@code GET} or {@code HEAD} whose requirement is the {@code read} scope
 * (records, changes, streams, tiles, kinds, history, sources, alerts, triggers, briefings,
 * search), plus Ask, which reads data into an LLM context.
 */
@Component
public class FailClosedRestrictionEnforcer implements RestrictionEnforcer {

    /** Non-GET operations that read data on the caller's behalf. */
    static final Set<String> DATA_READ_POSTS = Set.of("/v1/ask", "/v1/ask/stream");

    private final AntPathMatcher matcher = new AntPathMatcher();

    @Override
    public Optional<Refusal> check(HttpMethod method, String path, AccessRequirement requirement,
            AuthenticatedToken token) {
        if (!token.restrictions().hasDataRestrictions() || !isDataRead(method, path, requirement)) {
            return Optional.empty();
        }
        return Optional.of(new Refusal(ErrorCodes.RESTRICTION_NOT_ENFORCED,
                "This token has source, kind or bbox restrictions, which this version cannot apply to data reads yet"
                        + " (they are enforced from prompt 035). Use a token without data restrictions."));
    }

    /** True for a data-read path. */
    boolean isDataRead(HttpMethod method, String path, AccessRequirement requirement) {
        String normalized = AccessPolicy.normalize(path);
        if (HttpMethod.POST.equals(method)) {
            return DATA_READ_POSTS.stream().anyMatch(p -> matcher.match(p, normalized));
        }
        boolean read = HttpMethod.GET.equals(method) || HttpMethod.HEAD.equals(method);
        return read && requirement instanceof AccessRequirement.RequiresScope required
                && required.scope() == Scope.READ;
    }
}
