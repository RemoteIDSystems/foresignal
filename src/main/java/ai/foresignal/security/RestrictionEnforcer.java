package ai.foresignal.security;

import java.util.Optional;
import org.springframework.http.HttpMethod;

/**
 * Decides whether a token's <em>data</em> restrictions (sources, kinds, bbox) allow a request
 * (prompt 023 defines the SPI; prompt 035 enforces the restrictions on every read path and replaces
 * the default bean).
 *
 * <p>Until prompt 035 the default refused every data read of a token with data restrictions
 * ({@code 403 RESTRICTION_NOT_ENFORCED}). From 035 the bean is {@link ClampedRestrictionEnforcer}:
 * restrictions narrow the results through {@code RestrictionClamp} instead of refusing the request.
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
