package ai.foresignal.security;

import java.util.Optional;
import org.springframework.http.HttpMethod;

/**
 * Decides whether a token's <em>data</em> restrictions (sources, kinds, bbox) allow a request
 * (prompt 023 defines the SPI; prompt 035 enforces the restrictions on every read path and replaces
 * the default bean).
 *
 * <p>The default, {@link FailClosedRestrictionEnforcer}, refuses every data read of a token that
 * carries data restrictions with {@code 403 RESTRICTION_NOT_ENFORCED}: until 035 can narrow the
 * results, serving them unfiltered would fail open.
 */
public interface RestrictionEnforcer {

    /**
     * A refusal.
     *
     * @param code the problem code
     * @param detail the problem detail
     */
    record Refusal(String code, String detail) {}

    /**
     * Checks {@code method} on {@code path} (whose policy requirement is {@code requirement}) for
     * {@code token}.
     *
     * @return a refusal, or empty to let the request through
     */
    Optional<Refusal> check(HttpMethod method, String path, AccessRequirement requirement, AuthenticatedToken token);
}
