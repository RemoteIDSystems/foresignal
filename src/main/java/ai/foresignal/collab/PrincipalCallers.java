package ai.foresignal.collab;

import ai.foresignal.object.ObjectCaller;
import ai.foresignal.security.ApiTokenEntity;
import ai.foresignal.security.ApiTokenRepository;
import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.PrincipalRepository;
import ai.foresignal.security.Scope;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * The {@link ObjectCaller} a principal would be when it is not the one calling (prompt 130): who a mention, a share
 * notification or a saved-search run is checked as. Its scope is the strongest scope of the principal's active
 * tokens; it sees private objects when one of them may (an admin token or the 039 private grant). A disabled
 * principal, or one without an active token, is no caller.
 */
@Component
public class PrincipalCallers {

    private final PrincipalRepository principals;
    private final ApiTokenRepository tokens;
    private final Clock clock;

    /** Creates the component. */
    public PrincipalCallers(PrincipalRepository principals, ApiTokenRepository tokens, Clock clock) {
        this.principals = principals;
        this.tokens = tokens;
        this.clock = clock;
    }

    /** The caller principal {@code id} would be, if it can call at all. */
    public Optional<ObjectCaller> of(String id) {
        Optional<PrincipalEntity> p = id == null ? Optional.empty() : principals.findById(id);
        if (p.isEmpty() || p.get().isDisabled()) {
            return Optional.empty();
        }
        Instant now = clock.instant();
        Scope best = null;
        boolean seesPrivate = false;
        for (ApiTokenEntity t : tokens.findByPrincipalIdOrderByCreatedAtDesc(id)) {
            if (t.isPathKey() || !t.isActive(now)) {
                continue;
            }
            if (best == null || t.getScope().includes(best)) {
                best = t.getScope();
            }
            seesPrivate |= t.getScope() == Scope.ADMIN || t.isPrivateAccess();
        }
        if (best == null) {
            return Optional.empty();
        }
        return Optional.of(new ObjectCaller(id, "principal:" + p.get().getHandle(), best, seesPrivate, Set.of()));
    }
}
