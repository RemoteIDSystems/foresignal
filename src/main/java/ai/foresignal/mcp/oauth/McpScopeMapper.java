package ai.foresignal.mcp.oauth;

import ai.foresignal.security.Scope;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Maps identity-provider scopes to the Foresignal scope the MCP tool checks use (prompt 141), and back for the
 * {@code scope} parameter of a challenge.
 *
 * <p>{@code admin} is <strong>never</strong> mappable: an entry whose value is {@code admin} (refused when the
 * settings are saved, but possible in an imported or hand-edited row) is ignored here, so no identity-provider
 * token can ever act as admin.
 */
public final class McpScopeMapper {

    private McpScopeMapper() {}

    /**
     * The scopes a token carries in {@code claim}: a space-separated string ({@code scope}, RFC 9068 and
     * RFC 8693) or an array of strings ({@code scp} as Okta, Auth0 and Entra ID send it); either shape is read
     * from either claim.
     *
     * @param value the claim's value, or null
     * @return the scopes in order, without duplicates or blanks
     */
    public static Set<String> scopes(Object value) {
        Set<String> out = new LinkedHashSet<>();
        if (value instanceof String text) {
            for (String part : text.trim().split("\\s+")) {
                if (!part.isBlank()) {
                    out.add(part);
                }
            }
        } else if (value instanceof Collection<?> list) {
            for (Object item : list) {
                if (item instanceof String text) {
                    out.addAll(scopes(text));
                }
            }
        }
        return out;
    }

    /**
     * The Foresignal scope of a token: the most privileged mapped value, never {@code admin}.
     *
     * @param idpScopes the scopes the token carries
     * @param scopeMap identity-provider scope to Foresignal scope wire value
     * @return {@code read} or {@code agent}, or empty when nothing maps
     */
    public static Optional<Scope> map(Set<String> idpScopes, Map<String, String> scopeMap) {
        Scope best = null;
        for (String idpScope : idpScopes) {
            Optional<Scope> mapped = mappable(scopeMap.get(idpScope));
            if (mapped.isPresent() && (best == null || mapped.get().includes(best))) {
                best = mapped.get();
            }
        }
        return Optional.ofNullable(best);
    }

    /**
     * The Foresignal scope of one map value: {@code read} or {@code agent}; empty for {@code admin}, unknown values
     * and null.
     */
    public static Optional<Scope> mappable(String value) {
        if (value == null) {
            return Optional.empty();
        }
        try {
            Scope scope = Scope.fromWireValue(value);
            return scope == Scope.ADMIN ? Optional.empty() : Optional.of(scope);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * The identity-provider scopes to ask for to reach {@code required}: the keys that map exactly to it, else the
     * keys whose mapped scope includes it, sorted; {@code foresignal:<scope>} when the map names none.
     */
    public static List<String> scopesFor(Scope required, Map<String, String> scopeMap) {
        Set<String> exact = new TreeSet<>();
        Set<String> wider = new TreeSet<>();
        for (Map.Entry<String, String> entry : scopeMap.entrySet()) {
            Optional<Scope> mapped = mappable(entry.getValue());
            if (mapped.isEmpty()) {
                continue;
            }
            if (mapped.get() == required) {
                exact.add(entry.getKey());
            } else if (mapped.get().includes(required)) {
                wider.add(entry.getKey());
            }
        }
        if (!exact.isEmpty()) {
            return new ArrayList<>(exact);
        }
        if (!wider.isEmpty()) {
            return new ArrayList<>(wider);
        }
        return List.of("foresignal:" + required.wireValue());
    }

    /** Every identity-provider scope that maps to {@code read} or {@code agent}, sorted ({@code scopes_supported}). */
    public static List<String> supported(Map<String, String> scopeMap) {
        Set<String> out = new TreeSet<>();
        for (Map.Entry<String, String> entry : scopeMap.entrySet()) {
            if (mappable(entry.getValue()).isPresent()) {
                out.add(entry.getKey());
            }
        }
        return new ArrayList<>(out);
    }
}
