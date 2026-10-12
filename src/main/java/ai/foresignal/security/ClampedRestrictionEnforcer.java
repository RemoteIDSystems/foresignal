package ai.foresignal.security;

import java.util.Optional;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

/**
 * The {@link RestrictionEnforcer} from prompt 035, replacing 023's fail-closed default: data
 * restrictions (sources, kinds, bbox, area) are no longer refused at the filter, because every read
 * path that returns records or alerts applies them through
 * {@link ai.foresignal.security.restrict.RestrictionClamp} and carries
 * {@link ai.foresignal.security.restrict.RestrictedRead} ({@code RestrictedReadCoverageTest}).
 * Endpoint restrictions stay with {@link TokenFilter}.
 */
@Component
public class ClampedRestrictionEnforcer implements RestrictionEnforcer {

    @Override
    public Optional<Refusal> check(HttpMethod method, String path, AccessRequirement requirement,
            AuthenticatedToken token) {
        return Optional.empty();
    }
}
