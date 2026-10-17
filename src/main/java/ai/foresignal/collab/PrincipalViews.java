package ai.foresignal.collab;

import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.PrincipalRepository;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Renders principals (prompt 130): the public {@code PrincipalSummary} (handle, display name, kind, avatar) used
 * wherever another principal appears, and the full {@code Principal} for admins and the principal itself.
 */
@Component
public class PrincipalViews {

    private final PrincipalRepository principals;

    /** Creates the component. */
    public PrincipalViews(PrincipalRepository principals) {
        this.principals = principals;
    }

    /** The {@code PrincipalSummary} of a principal. */
    public static Map<String, Object> summary(PrincipalEntity p) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", p.getId());
        out.put("handle", p.getHandle());
        out.put("displayName", p.getDisplayName());
        out.put("kind", p.getKind());
        out.put("avatarMediaId", p.getAvatarMediaId());
        return out;
    }

    /** The full {@code Principal} (email and organization included). */
    public static Map<String, Object> full(PrincipalEntity p, Integer tokenCount) {
        Map<String, Object> out = summary(p);
        out.put("email", p.getEmail());
        out.put("timezone", p.getTimezone());
        out.put("organization", p.getOrganization());
        out.put("disabled", p.isDisabled());
        if (tokenCount != null) {
            out.put("tokenCount", tokenCount);
        }
        out.put("createdAt", p.getCreatedAt());
        out.put("updatedAt", p.getUpdatedAt());
        return out;
    }

    /**
     * The summary of principal {@code id}, or null for null. An id that names no principal (a system actor) renders
     * as a {@code service} principal with that id as its handle.
     */
    public Map<String, Object> summary(String id) {
        if (id == null) {
            return null;
        }
        return principals.findById(id).map(PrincipalViews::summary).orElseGet(() -> unknown(id));
    }

    /** A per-call cache of summaries, for listings that name the same principals many times. */
    public Map<String, Object> summary(String id, Map<String, Map<String, Object>> cache) {
        if (id == null) {
            return null;
        }
        return cache.computeIfAbsent(id, this::summary);
    }

    /** A fresh cache for {@link #summary(String, Map)}. */
    public static Map<String, Map<String, Object>> cache() {
        return new HashMap<>();
    }

    private static Map<String, Object> unknown(String id) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", id);
        out.put("handle", id.length() > 32 ? id.substring(0, 32) : id);
        out.put("displayName", id);
        out.put("kind", "service");
        out.put("avatarMediaId", null);
        return out;
    }
}
