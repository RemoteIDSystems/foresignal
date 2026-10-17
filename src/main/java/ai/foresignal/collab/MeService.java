package ai.foresignal.collab;

import ai.foresignal.object.ObjectCaller;
import ai.foresignal.security.ApiTokenEntity;
import ai.foresignal.security.ApiTokenRepository;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.Scope;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@code GET /v1/me} and MCP {@code whoami} (prompt 130): the calling principal (in full), the token used, the scopes
 * it may call, its groups, its notification preferences and the instance's default TLP label.
 */
@Service
public class MeService {

    private final PrincipalAdminService principals;
    private final ApiTokenRepository tokens;
    private final GroupService groups;
    private final NotificationPreferenceService preferences;
    private final AccessResolver access;

    /** Creates the service. */
    public MeService(PrincipalAdminService principals, ApiTokenRepository tokens, GroupService groups,
            NotificationPreferenceService preferences, AccessResolver access) {
        this.principals = principals;
        this.tokens = tokens;
        this.groups = groups;
        this.preferences = preferences;
        this.access = access;
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
        PrincipalEntity p = principals.get(caller.principal());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("principal", PrincipalViews.full(p, principals.tokenCount(p.getId())));
        Map<String, Object> t = new LinkedHashMap<>();
        t.put("id", token.id().toString());
        t.put("name", token.name());
        t.put("scope", token.scope().wireValue());
        ApiTokenEntity row = tokens.findById(token.id()).orElse(null);
        t.put("tokenPrefix", row == null ? null : row.getTokenPrefix());
        t.put("expiresAt", row == null ? null : row.getExpiresAt());
        out.put("token", t);
        out.put("scopes", scopes(token.scope()));
        out.put("groups", groups.membershipsOf(p.getId()));
        out.put("notificationPreferences", preferences.list(p.getId()));
        out.put("defaultTlp", access.defaultTlp().wireValue());
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
