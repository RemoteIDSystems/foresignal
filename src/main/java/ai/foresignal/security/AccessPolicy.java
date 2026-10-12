package ai.foresignal.security;

import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

/**
 * The single table mapping (method, path) to what a caller must present (Architecture §15).
 *
 * <p>Every rule below is transcribed from the {@code x-foresignal-scope} extension on the corresponding
 * operation in {@code docs/OpenAPI.json}, which is the authored contract. Keeping the table in one
 * place and in contract order means it can be unit tested exhaustively, and that prompt 019 only
 * has to change how tokens are looked up, not what the rules are.
 *
 * <p>Many of these paths belong to endpoints later prompts add. They are listed now on purpose: an
 * endpoint that appears before its rule does would otherwise be open by accident.
 *
 * <p><strong>Order matters.</strong> The first matching rule wins, so specific patterns precede
 * general ones ({@code /v1/sources/{id}/credentials} before {@code /v1/sources/**}).
 *
 * <p>Prompt 023 adds the token, ticket, pairing and audit paths and the
 * {@code alwaysAuthenticated} flag ({@link AccessRequirement.RequiresScope#alwaysAuthenticated()}),
 * which mirrors the spec's {@code x-foresignal-always-authenticated}. {@link #alwaysAuthenticated()}
 * lists those rules so prompt 025 can mark LLM-backed MCP tools the same way.
 *
 * <p>Prompt 024 adds the ingest, ntfy/Gotify compatibility and External Data Protocol rules. The
 * {@code /compat/**} paths are outside {@code /v1}, so without their explicit rules they would be
 * open.
 */
@Component
public class AccessPolicy {

    /**
     * Anything under {@code /v1} that no rule matches.
     *
     * <p>Deny by default: a new endpoint added without a rule requires an admin token rather than
     * being silently public.
     */
    static final AccessRequirement DEFAULT_V1_REQUIREMENT = AccessRequirement.ADMIN;

    /** Paths outside {@code /v1} that Foresignal serves openly. */
    static final List<String> OPEN_PATHS =
            List.of(
                    "/v1/openapi.json",
                    "/v1/openapi-generated.json",
                    // springdoc's swagger-config, fetched by Swagger UI at /docs (020).
                    "/v1/openapi-generated.json/**",
                    "/docs",
                    "/docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/webjars/**",
                    "/actuator/health",
                    "/actuator/health/**",
                    "/actuator/info",
                    // LAN-only by assumption; see the class note in TokenFilter.
                    "/actuator/prometheus");

    private final AntPathMatcher matcher = new AntPathMatcher();
    private final List<Rule> rules = rules();

    /** What {@code method} on {@code path} requires. */
    public AccessRequirement requirementFor(HttpMethod method, String path) {
        String normalized = normalize(path);

        for (String open : OPEN_PATHS) {
            if (matcher.match(open, normalized)) {
                return AccessRequirement.OPEN;
            }
        }
        for (Rule rule : rules) {
            if (rule.matches(matcher, method, normalized)) {
                return rule.requirement();
            }
        }
        if (matcher.match("/v1/**", normalized)) {
            return DEFAULT_V1_REQUIREMENT;
        }
        // Static resources, the error page, anything not under /v1.
        return AccessRequirement.OPEN;
    }

    /**
     * The rules marked always authenticated: operations that need a token even while
     * {@code security.openReads} is on (cost-bearing paths, invariant SEC-5). Prompt 025 reads this
     * to mark LLM-backed MCP tools the same way.
     *
     * @return each such rule's method (null for any) and Ant pattern, in table order
     */
    public List<EndpointRule> alwaysAuthenticated() {
        return rules.stream()
                .filter(rule -> rule.requirement().alwaysAuthenticated())
                .map(rule -> new EndpointRule(rule.method(), rule.pattern()))
                .toList();
    }

    /**
     * One (method, Ant pattern) pair of the table.
     *
     * @param method the HTTP method, or null for every method
     * @param pattern the Ant-style path pattern
     */
    public record EndpointRule(HttpMethod method, String pattern) {

        /** Requires a pattern. */
        public EndpointRule {
            Objects.requireNonNull(pattern, "pattern");
        }
    }

    /** Strips a trailing slash and any query string, so rules need not account for either. */
    static String normalize(String path) {
        String result = path == null || path.isEmpty() ? "/" : path;
        int query = result.indexOf('?');
        if (query >= 0) {
            result = result.substring(0, query);
        }
        if (result.length() > 1 && result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    /**
     * The rules, in contract order. {@code null} methods match any method.
     *
     * <p>Transcribed from the {@code x-foresignal-scope} and
     * {@code x-foresignal-always-authenticated} annotations in {@code docs/OpenAPI.json}.
     */
    private static List<Rule> rules() {
        return List.of(
                // ---- Inbound ingest (Architecture §16, prompt 024) ----
                rule(HttpMethod.GET, "/v1/ingest/formats", AccessRequirement.ADMIN),
                rule(HttpMethod.POST, "/v1/ingest/**", AccessRequirement.INGEST_KEY_OR_ADMIN),
                // The CloudEvents webhook validation handshake (ingestWebhookHandshake).
                rule(HttpMethod.OPTIONS, "/v1/ingest/**", AccessRequirement.INGEST_KEY_OR_ADMIN),
                // ntfy- and Gotify-compatible publish, outside /v1 (the key resolves the source).
                rule(null, "/compat/ntfy", AccessRequirement.INGEST_KEY_OR_ADMIN),
                rule(null, "/compat/ntfy/**", AccessRequirement.INGEST_KEY_OR_ADMIN),
                rule(null, "/compat/gotify/**", AccessRequirement.INGEST_KEY_OR_ADMIN),

                // ---- External Data Protocol (prompt 024) ----
                rule(HttpMethod.GET, "/v1/external/protocol", AccessRequirement.OPEN),
                rule(HttpMethod.GET, "/v1/external/protocol/**", AccessRequirement.OPEN),
                // The registration token in the body's bearer is the credential, checked in code.
                rule(HttpMethod.POST, "/v1/external/register", AccessRequirement.OPEN),
                // A provider pushes and updates its manifest with its own ingest key.
                rule(HttpMethod.POST, "/v1/external/sources/*/items", AccessRequirement.INGEST_KEY_OR_ADMIN),
                rule(HttpMethod.PUT, "/v1/external/sources/*/manifest", AccessRequirement.INGEST_KEY_OR_ADMIN),
                rule(HttpMethod.POST, "/v1/external/validate", AccessRequirement.AGENT),
                rule(null, "/v1/external/sources", AccessRequirement.ADMIN),
                rule(null, "/v1/external/sources/*", AccessRequirement.ADMIN),
                rule(HttpMethod.POST, "/v1/external/sources/*/approve", AccessRequirement.ADMIN),
                rule(HttpMethod.POST, "/v1/external/sources/*/suspend", AccessRequirement.ADMIN),
                rule(HttpMethod.POST, "/v1/external/sources/*/snapshots/*/allow", AccessRequirement.ADMIN),
                rule(null, "/v1/external/registration-tokens", AccessRequirement.ADMIN),
                rule(null, "/v1/external/registration-tokens/*", AccessRequirement.ADMIN),

                // ---- Open informational endpoints ----
                rule(HttpMethod.GET, "/v1/system", AccessRequirement.OPEN),
                rule(HttpMethod.GET, "/v1/push/vapid-public-key", AccessRequirement.OPEN),
                // The pairing code in the body is the credential (023).
                rule(HttpMethod.POST, "/v1/pairing/claim", AccessRequirement.OPEN),

                // ---- Tokens, tickets, pairing and audit (023) ----
                // The calling token reports its own device: read, before the admin /v1/tokens/** rule.
                rule(HttpMethod.PUT, "/v1/tokens/current/device", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/stream-tickets", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(null, "/v1/pairing-codes/**", AccessRequirement.ADMIN),
                rule(HttpMethod.GET, "/v1/audit", AccessRequirement.ADMIN),

                // ---- Admin configuration areas (reads included) ----
                rule(null, "/v1/settings/**", AccessRequirement.ADMIN),
                rule(null, "/v1/system/admin-token/**", AccessRequirement.ADMIN),
                rule(null, "/v1/channels/**", AccessRequirement.ADMIN),
                rule(null, "/v1/tokens/**", AccessRequirement.ADMIN),
                // Testing a provider spends LLM tokens (always authenticated, 026).
                rule(HttpMethod.POST, "/v1/llm/providers/*/test", AccessRequirement.ADMIN_ALWAYS_AUTHENTICATED),
                rule(null, "/v1/llm/**", AccessRequirement.ADMIN),
                rule(null, "/v1/custom-sources/**", AccessRequirement.ADMIN),

                // ---- Sources: reads open, configuration admin ----
                rule(HttpMethod.PUT, "/v1/sources/*/credentials", AccessRequirement.ADMIN),
                rule(HttpMethod.DELETE, "/v1/sources/*/credentials", AccessRequirement.ADMIN),
                rule(HttpMethod.POST, "/v1/sources/*/fetch", AccessRequirement.ADMIN),
                rule(HttpMethod.POST, "/v1/sources/*/probe", AccessRequirement.ADMIN),
                rule(HttpMethod.PUT, "/v1/sources/*", AccessRequirement.ADMIN),
                // The provenance log exposes redacted URLs, ETags and body hashes: admin (013).
                rule(HttpMethod.GET, "/v1/sources/*/fetches", AccessRequirement.ADMIN),
                rule(HttpMethod.GET, "/v1/sources/**", AccessRequirement.READ),

                // ---- Records, changes, tiles, kinds, history: data reads ----
                rule(HttpMethod.GET, "/v1/records/**", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/changes/**", AccessRequirement.READ),
                // The multiplexed event stream and its filter swap; ownership is enforced in code (015).
                rule(HttpMethod.GET, "/v1/events/stream", AccessRequirement.READ),
                rule(HttpMethod.PATCH, "/v1/events/stream/*/filter", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/kinds", AccessRequirement.READ),
                // The /v1/kinds rule matches only the exact path (014).
                rule(HttpMethod.GET, "/v1/kinds/*/schema", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/history", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/tiles/**", AccessRequirement.READ),
                // The TileJSON index; /v1/tiles/** does not match /v1/tiles.json (016).
                rule(HttpMethod.GET, "/v1/tiles.json", AccessRequirement.READ),

                // ---- Search: read, except reindex ----
                rule(HttpMethod.POST, "/v1/search/reindex", AccessRequirement.ADMIN_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/search/status", AccessRequirement.READ),
                // Semantic search embeds the query: always authenticated (023, 027).
                rule(HttpMethod.GET, "/v1/search", AccessRequirement.READ_ALWAYS_AUTHENTICATED),

                // ---- Triggers: read to list, agent to change ----
                rule(HttpMethod.GET, "/v1/triggers/**", AccessRequirement.READ),
                rule(null, "/v1/triggers/**", AccessRequirement.AGENT),

                // ---- Alerts: read to list, agent for the lifecycle (017) ----
                rule(HttpMethod.POST, "/v1/alerts/*/ack", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/alerts/*/resolve", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/alerts/*/snooze", AccessRequirement.AGENT),
                rule(HttpMethod.DELETE, "/v1/alerts/*/snooze", AccessRequirement.AGENT),
                rule(HttpMethod.PUT, "/v1/alerts/*/feedback", AccessRequirement.AGENT),
                rule(HttpMethod.GET, "/v1/alerts/**", AccessRequirement.READ),

                // ---- Briefings: read to list, agent to change or run ----
                rule(HttpMethod.GET, "/v1/briefings/**", AccessRequirement.READ),
                // Previewing and running a briefing call the LLM (029).
                rule(HttpMethod.POST, "/v1/briefings/preview", AccessRequirement.AGENT_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/briefings/*/run", AccessRequirement.AGENT_ALWAYS_AUTHENTICATED),
                rule(null, "/v1/briefings/**", AccessRequirement.AGENT),

                // ---- Ask: asking is a read; confirming an action is not ----
                rule(HttpMethod.POST, "/v1/ask/actions/**", AccessRequirement.AGENT),
                rule(HttpMethod.DELETE, "/v1/ask/threads/*", AccessRequirement.AGENT),
                // Asking spends LLM tokens: always authenticated (023, 028).
                rule(HttpMethod.GET, "/v1/ask/threads/**", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/ask/stream", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/ask", AccessRequirement.READ_ALWAYS_AUTHENTICATED),

                // ---- Web Push: listing subscriptions is admin, managing your own is read ----
                // Subscribing makes the server send pushes: always authenticated (023, 031).
                rule(HttpMethod.GET, "/v1/push/subscriptions", AccessRequirement.ADMIN),
                rule(HttpMethod.POST, "/v1/push/subscriptions", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/push/subscriptions/*/verify", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.DELETE, "/v1/push/subscriptions/*", AccessRequirement.READ_ALWAYS_AUTHENTICATED));
    }

    private static Rule rule(HttpMethod method, String pattern, AccessRequirement requirement) {
        return new Rule(method, pattern, requirement);
    }

    /** One row of the policy table. A null {@code method} matches every method. */
    record Rule(HttpMethod method, String pattern, AccessRequirement requirement) {

        boolean matches(AntPathMatcher matcher, HttpMethod candidate, String path) {
            if (method != null && !method.equals(candidate)) {
                return false;
            }
            return matcher.match(pattern, path);
        }
    }
}
