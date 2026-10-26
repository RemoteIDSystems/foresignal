package ai.foresignal.collab;

import ai.foresignal.object.ObjectCaller;
import ai.foresignal.security.ApiTokenEntity;
import ai.foresignal.security.ApiTokenRepository;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.Scope;
import ai.foresignal.security.user.AuthenticatedSession;
import ai.foresignal.security.user.UserViews;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@code GET /v1/me} and MCP {@code whoami} (prompt 130): the calling principal (in full), the token used, the scopes
 * it may call, its groups, its notification preferences and the instance's default TLP label.
 *
 * <p>For a signed-in session (R2-003) the view also carries {@code user} (the {@code User} shape) and
 * {@code permissions} (the effective permission keys), and {@code token} describes the session (its id, prefix and
 * absolute expiry). A request with an API token gets exactly the earlier shape.
 */
@Service
public class MeService {

    private final PrincipalAdminService principals;
    private final ApiTokenRepository tokens;
    private final GroupService groups;
    private final NotificationPreferenceService preferences;
    private final AccessResolver access;
    private final UserViews users;

    /** Creates the service. */
    public MeService(PrincipalAdminService principals, ApiTokenRepository tokens, GroupService groups,
            NotificationPreferenceService preferences, AccessResolver access, UserViews users) {
        this.principals = principals;
        this.tokens = tokens;
        this.groups = groups;
        this.preferences = preferences;
        this.access = access;
        this.users = users;
    }

    /**
     * The {@code Me} view.
     *
     * @param token the token behind the request
     * @param caller the caller it makes
     * @return the view
     */
    @Transactional(readOnly = true)
    public Map<String, Object> me(AuthenticatedToken token, ObjectCaller caller) {
        return me(token, caller, null);
    }

    /**
     * The {@code Me} view, with the user and its permissions when the caller is a signed-in session.
     *
     * @param token the token behind the request (a session's token view for a session)
     * @param caller the caller it makes
     * @param session the session, or null for an API token
     * @return the view
     */
    @Transactional(readOnly = true)
    public Map<String, Object> me(AuthenticatedToken token, ObjectCaller caller, AuthenticatedSession session) {
        PrincipalEntity p = principals.get(caller.principal());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("principal", PrincipalViews.full(p, principals.tokenCount(p.getId())));
        Map<String, Object> t = new LinkedHashMap<>();
        t.put("id", token.id().toString());
        t.put("name", token.name());
        t.put("scope", token.scope().wireValue());
        if (session != null) {
            t.put("tokenPrefix", session.tokenPrefix());
            t.put("expiresAt", session.expiresAt());
        } else {
            ApiTokenEntity row = tokens.findById(token.id()).orElse(null);
            t.put("tokenPrefix", row == null ? null : row.getTokenPrefix());
            t.put("expiresAt", row == null ? null : row.getExpiresAt());
        }
        out.put("token", t);
        out.put("scopes", scopes(token.scope()));
        out.put("groups", groups.membershipsOf(p.getId()));
        out.put("notificationPreferences", preferences.list(p.getId()));
        out.put("defaultTlp", access.defaultTlp().wireValue());
        if (session != null) {
            out.put("user", users.user(session.userId()));
            out.put("permissions", session.permissions().asList());
        }
        return out;
    }

    /** The scopes a token of {@code scope} may call (the hierarchy expanded). */
    static List<String> scopes(Scope scope) {
        List<String> out = new ArrayList<>();
        for (Scope s : List.of(Scope.READ, Scope.AGENT, Scope.ADMIN)) {
            if (scope.includes(s)) {
                out.add(s.wireValue());
            }
        }
        return out;
    }
}
