package ai.foresignal.security;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
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
 *
 * <p>Prompt 138 adds the external bindings ({@code admin}) and question links ({@code read} to list, {@code agent} to
 * link and unlink).
 *
 * <p>Prompt 033 adds {@code /v1/actions/*} as open (the capability is the credential, rate limited
 * in the controller), the inbox and presence paths (read, always authenticated; {@code GET
 * /v1/clients} admin) and the escalation policies (agent).
 *
 * <p>Prompt 036 adds the places (reads {@code read}, always authenticated; writes {@code agent}),
 * saved views ({@code read}/{@code agent}) and watchlists ({@code agent}).
 *
 * <p>Prompt 042 adds {@code GET /v1/products/**} and {@code GET /v1/styles/**} as {@code read}.
 *
 * <p>Prompt 052 adds sampling as {@code read}: {@code GET} and {@code POST /v1/sample} and
 * {@code POST /v1/products/{id}/stats} (the series is under {@code GET /v1/products/**}, the place weather under
 * the always-authenticated {@code GET /v1/places/**}).
 *
 * <p>Prompt 049 adds {@code GET /v1/ground-stations/**} as {@code read}. Prompt 043 adds
 * {@code GET /v1/media/**} as {@code read}; the news providers of prompt 133 under
 * {@code /v1/media/providers} stay {@code admin}, listed before it.
 *
 * <p>Prompt 059 adds {@code /v1/mappings/**} (preview, presets) as {@code admin}, with
 * {@code POST /v1/mappings/suggest} also always authenticated (it spends LLM budget). The samples and mapping
 * test under {@code /v1/custom-sources/**} are admin through the existing rule; {@code GET /v1/ingest/**}
 * (WU, GMC) is already {@code INGEST_KEY_OR_ADMIN}.
 *
 * <p>Prompt 058 adds {@code GET /v1/map/**} (MapLibre style, sprite, icons), {@code GET /v1/kml/**},
 * {@code GET /v1/alerts/feed.*}, {@code GET /v1/briefings/*}{@code /runs/feed.*} and
 * {@code GET /v1/triggers/*.kml} as {@code read}, ahead of the broader rules.
 *
 * <p>Prompt 055 adds {@code GET /v1/maps/**} ({@code getStaticMap}) as {@code read}; a map of a place
 * needs a token, which the controller enforces as the loop does.
 *
 * <p>Prompt 050 adds the tile-key operations ({@code GET}/{@code POST /v1/tile-keys},
 * {@code DELETE /v1/tile-keys/{id}}) as {@code agent}. Feed- and tile-key URLs ({@code /v1/k/**}) have no
 * rule: {@code PathKeyFilter} forwards them to their canonical path, which is what is evaluated.
 *
 * <p>Prompt 112 adds share links ({@code GET}/{@code POST /v1/share-links}, {@code DELETE /v1/share-links/{id}}
 * as {@code agent}), their key URLs ({@code GET /v1/k/*}, {@code …/geojson}, {@code …/tiles/*}{@code /*}{@code /*.mvt},
 * {@code …/download}) as {@code open} (the {@code fss_} key is the credential) and {@code GET /v1/cap/**} and
 * {@code GET /v1/calendars/**} as {@code read}; the OGC Features operations fall under {@code /v1/ogc/**}.
 *
 * <p>Prompt 113 adds federation: {@code GET /v1/federation/**} as {@code read} (the controller also requires the
 * token's {@code federation} capability), the instance-to-instance pushes ({@code POST /v1/federation/ingest/*},
 * {@code POST /v1/federation/bundle}, and 134's {@code POST /v1/federation/chronicle-sketches}) as
 * {@link AccessRequirement#PEER} (a peer ingest key, {@code fsp_}), and {@code /v1/peers/**} as {@code admin}.
 *
 * <p>Prompt 064 adds {@code GET /compat/celestrak/**} (the CelesTrak-compatible GP output) as {@code read}; other
 * methods on it are {@code admin}. It sits outside {@code /v1}, so without the rule it would be open.
 *
 * <p>Prompt 130 adds the collaboration operations: principals ({@code admin} writes, reads always authenticated),
 * groups, access lists and shares ({@code agent}), and the personal inbox, preferences, tags, comment replies,
 * attachments, evidence items and saved searches ({@code read}, always authenticated); the access lists then decide per
 * object.
 *
 * <p>Prompt 132 adds reports ({@code read}, always authenticated, with the report's access list deciding; sending and
 * share links {@code agent}), the report key URL {@code GET /v1/k/*}{@code /report} ({@code open}), and exchange:
 * packages and the sent list {@code agent}, the peer inbox and receipts {@link AccessRequirement#PEER}, imports
 * {@code admin}, received packages {@code read} (always authenticated, per recipient).
 *
 * <p>Prompt 134 adds the chronicle network: related links, clusters, cluster timelines, the access-request list,
 * dismissing links and requesting access ({@code read}, always authenticated; the service decides disclosure and
 * ownership), discoverability and grant or decline ({@code agent}), network salts ({@code admin}) and the peer sketch
 * inbox ({@link AccessRequirement#PEER}).
 *
 * <p>Prompt 140 adds the adapter doctor: {@code POST /v1/sources/{id}/doctor} ({@code admin}, always authenticated,
 * because a run can spend LLM budget) and the doctor reports ({@code agent}).
 *
 * <p>Prompt 141 adds the plugin operations ({@code GET}/{@code PUT /v1/plugins} and {@code /v1/plugins/{name}}) as
 * explicit {@code admin} rules.
 *
 * <p>Prompt 141 adds the exchange formats: {@code GET /fdsnws/**} (the FDSN event service) as {@code read},
 * {@code GET /taxii2} and {@code GET /taxii2/**} (TAXII 2.1) and {@code GET /v1/export/stix} as {@code agent}. The
 * FDSN and TAXII paths are outside {@code /v1}, so every other method on them is mapped to {@code admin} explicitly
 * rather than reaching the open fallback for non-{@code /v1} paths.
 *
 * <p>Prompt 025 adds {@code /mcp} and {@code /mcp/**} (the MCP server, also outside {@code /v1}):
 * {@code read}, with per-tool scope checks inside the server ({@code McpToolRegistry}).
 *
 * <p>Prompt 141 (MCP remote authorization) needs no new rule: the RFC 9728 metadata at
 * {@code /.well-known/oauth-protected-resource} and {@code /.well-known/oauth-protected-resource/mcp} is open under
 * the {@code /.well-known/**} entry of {@link #OPEN_PATHS}, and {@code /v1/settings/mcp-auth} is admin under
 * {@code /v1/settings/**}. Which credential {@code /mcp} accepts ({@code mcp.auth.mode}) is decided in
 * {@link TokenFilter} through {@link McpOAuthGate}, not here.
 *
 * <p>Prompt R2-004 adds the permission half of the table, {@link #permissionFor}: the operation's
 * {@code x-foresignal-permission}, transcribed into {@link AccessPermissions} by
 * {@code scripts/generate-access-permissions.py}. It is a parallel lookup rather than a field on
 * {@link AccessRequirement.RequiresScope}, so the scope requirement, and with it every machine token's access, stays
 * exactly what it was; {@link TokenFilter} applies the permission only to a signed-in user's session.
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
                    "/actuator/prometheus",
                // The instance JWKS is published anonymously (prompt 109).
                "/.well-known/**");

    private final AntPathMatcher matcher = new AntPathMatcher();
    private final List<Rule> rules = rules();
    private final Map<String, List<AccessPermissions.PermissionRule>> permissionRules =
            AccessPermissions.RULES.stream().collect(Collectors.groupingBy(AccessPermissions.PermissionRule::method));

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
     * The permission a signed-in user must hold for {@code method} on {@code path}: the operation's
     * {@code x-foresignal-permission} (R2-004). {@code HEAD} takes the {@code GET} row, as the scope rules do.
     *
     * @param method the request method
     * @param path the request path
     * @return the permission key, or empty when the operation carries none (a session is then gated on its scope)
     */
    public Optional<String> permissionFor(HttpMethod method, String path) {
        return permissionRule(method, path).map(AccessPermissions.PermissionRule::permission);
    }

    /**
     * Every permission that satisfies {@code method} on {@code path} for a signed-in user: the operation's
     * {@code x-foresignal-permission} first, then its {@code x-foresignal-permission-alternatives} (R2-009). Holding
     * any one is enough.
     *
     * @param method the request method
     * @param path the request path
     * @return the keys, empty when the operation carries no permission
     */
    public List<String> acceptedPermissions(HttpMethod method, String path) {
        return permissionRule(method, path).filter(rule -> rule.permission() != null).map(rule -> {
            List<String> keys = new java.util.ArrayList<>();
            keys.add(rule.permission());
            keys.addAll(rule.alternatives());
            return List.copyOf(keys);
        }).orElse(List.of());
    }

    private Optional<AccessPermissions.PermissionRule> permissionRule(HttpMethod method, String path) {
        String key = HttpMethod.HEAD.equals(method) ? HttpMethod.GET.name() : method.name();
        List<AccessPermissions.PermissionRule> candidates = permissionRules.get(key);
        if (candidates == null) {
            return Optional.empty();
        }
        String normalized = normalize(path);
        for (AccessPermissions.PermissionRule rule : candidates) {
            if (matcher.match(rule.pattern(), normalized)) {
                return rule.permission() == null ? Optional.empty() : Optional.of(rule);
            }
        }
        return Optional.empty();
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
                // ---- MCP server (Architecture §18, prompt 025), outside /v1 ----
                // Listing and calling need read (open while openReads is on); each tool checks its own
                // scope inside, so agent tools stay closed to read tokens and anonymous callers.
                rule(null, "/mcp", AccessRequirement.READ),
                rule(null, "/mcp/**", AccessRequirement.READ),

                // ---- Federation (prompt 113) ----
                // Instance-to-instance pushes authenticate with a peer ingest key (fsp_), never a token.
                rule(HttpMethod.POST, "/v1/federation/ingest/*", AccessRequirement.PEER),
                rule(HttpMethod.POST, "/v1/federation/bundle", AccessRequirement.PEER),
                rule(HttpMethod.POST, "/v1/federation/chronicle-sketches", AccessRequirement.PEER),
                // The feed is a read; the controller also requires the token's federation capability.
                rule(HttpMethod.GET, "/v1/federation/**", AccessRequirement.READ),
                // Peers are admin.
                rule(null, "/v1/peers", AccessRequirement.ADMIN),
                rule(null, "/v1/peers/**", AccessRequirement.ADMIN),

                // ---- Inbound ingest (Architecture §16, prompt 024) ----
                rule(HttpMethod.GET, "/v1/ingest/formats", AccessRequirement.ADMIN),
                rule(HttpMethod.POST, "/v1/ingest/**", AccessRequirement.INGEST_KEY_OR_ADMIN),
                // GET pushes (OsmAnd/Traccar Client, GPSLogger) and the key-in-path forms (039); device
                // keys (eid_) and path keys are resolved by the ingest-key authenticator.
                rule(HttpMethod.GET, "/v1/ingest/**", AccessRequirement.INGEST_KEY_OR_ADMIN),
                // The CloudEvents webhook validation handshake (ingestWebhookHandshake).
                rule(HttpMethod.OPTIONS, "/v1/ingest/**", AccessRequirement.INGEST_KEY_OR_ADMIN),
                // CelesTrak-compatible GP output (064), outside /v1: read, never open by accident.
                rule(HttpMethod.GET, "/compat/celestrak/**", AccessRequirement.READ),
                rule(HttpMethod.HEAD, "/compat/celestrak/**", AccessRequirement.READ),
                rule(null, "/compat/celestrak/**", AccessRequirement.ADMIN),
                // FDSN event service (141), outside /v1: read (open while openReads is on); any other method is
                // admin, so nothing under /fdsnws reaches the open fallback for paths outside /v1.
                rule(HttpMethod.GET, "/fdsnws/**", AccessRequirement.READ),
                rule(null, "/fdsnws/**", AccessRequirement.ADMIN),
                // TAXII 2.1 (141), outside /v1: read-only and agent; any other method is admin.
                rule(HttpMethod.GET, "/taxii2", AccessRequirement.AGENT),
                rule(HttpMethod.GET, "/taxii2/**", AccessRequirement.AGENT),
                rule(null, "/taxii2", AccessRequirement.ADMIN),
                rule(null, "/taxii2/**", AccessRequirement.ADMIN),
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
                // Prompt 138: bindings are admin; question links are read (list) and agent (link, unlink).
                rule(HttpMethod.GET, "/v1/external/sources/*/bindings", AccessRequirement.ADMIN),
                rule(HttpMethod.POST, "/v1/external/sources/*/bindings", AccessRequirement.ADMIN),
                rule(HttpMethod.POST, "/v1/external/sources/*/bindings/accept-suggested", AccessRequirement.ADMIN),
                rule(null, "/v1/external/bindings/*", AccessRequirement.ADMIN),
                rule(HttpMethod.POST, "/v1/external/bindings/*/preview", AccessRequirement.ADMIN),
                rule(HttpMethod.GET, "/v1/external/sources/*/question-links", AccessRequirement.READ),
                rule(HttpMethod.POST, "/v1/external/sources/*/question-links", AccessRequirement.AGENT),
                rule(HttpMethod.DELETE, "/v1/external/question-links/*", AccessRequirement.AGENT),
                // Prompt 155: an operator requests an on-demand forecast (agent); a provider returns the
                // correlated result to the callback, where the unguessable correlationId in the body is the
                // credential (checked in code), the same pattern as /v1/external/register.
                rule(HttpMethod.POST, "/v1/external/analysis/requests", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/external/analysis/callback", AccessRequirement.OPEN),

                // ---- Open informational endpoints ----
                rule(HttpMethod.GET, "/v1/system", AccessRequirement.OPEN),
                // The JWKS and attribution aggregate (109): open; key rotation is admin.
                // First-run status (R2-026): open, so a fresh instance's sign-in page can tell setup is pending.
                rule(HttpMethod.GET, "/v1/system/first-run", AccessRequirement.OPEN),
                rule(HttpMethod.GET, "/v1/system/jwks", AccessRequirement.OPEN),
                rule(HttpMethod.POST, "/v1/system/signing-key/rotate", AccessRequirement.ADMIN),
                rule(HttpMethod.GET, "/v1/push/vapid-public-key", AccessRequirement.OPEN),
                // The pairing code in the body is the credential (023).
                rule(HttpMethod.POST, "/v1/pairing/claim", AccessRequirement.OPEN),
                // Notification action capabilities: the capability in the path is the credential (033).
                rule(HttpMethod.GET, "/v1/actions/*", AccessRequirement.OPEN),
                rule(HttpMethod.POST, "/v1/actions/*", AccessRequirement.OPEN),
                // WebSub callbacks of feed-kit sources: the unguessable path token identifies the subscription (073).
                // Only GET (intent verification) and POST (content delivery) on exactly two segments are open.
                rule(HttpMethod.GET, "/v1/websub/*/*", AccessRequirement.OPEN),
                rule(HttpMethod.POST, "/v1/websub/*/*", AccessRequirement.OPEN),

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
                // Outbound sinks (110): admin.
                rule(null, "/v1/sinks", AccessRequirement.ADMIN),
                rule(null, "/v1/sinks/**", AccessRequirement.ADMIN),
                rule(null, "/v1/tokens/**", AccessRequirement.ADMIN),
                // Feed keys (035): admin; /v1/settings/ratelimits and /v1/settings/outbound fall under /v1/settings/**.
                rule(null, "/v1/feed-keys", AccessRequirement.ADMIN),
                rule(null, "/v1/feed-keys/*", AccessRequirement.ADMIN),
                // Tile keys (050): agent. /v1/k/** has no rule on purpose: only PathKeyFilter's forward
                // to the canonical path reaches a controller; anything else falls to the admin default.
                rule(HttpMethod.GET, "/v1/tile-keys", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/tile-keys", AccessRequirement.AGENT),
                rule(HttpMethod.DELETE, "/v1/tile-keys/*", AccessRequirement.AGENT),
                // Share links (112): agent to manage (private targets need admin, checked by the service); the
                // fss_ key URLs are open because the key in the path is the credential, checked by PathKeyFilter's
                // ShareKeyResolver, which passes them on unforwarded (a forwarded feed or tile key never reaches
                // these paths with a principal, and the share-link service checks the key again).
                rule(HttpMethod.GET, "/v1/share-links", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/share-links", AccessRequirement.AGENT),
                rule(HttpMethod.DELETE, "/v1/share-links/*", AccessRequirement.AGENT),
                rule(HttpMethod.GET, "/v1/k/*", AccessRequirement.OPEN),
                rule(HttpMethod.GET, "/v1/k/*/geojson", AccessRequirement.OPEN),
                rule(HttpMethod.GET, "/v1/k/*/tiles/*/*/*.mvt", AccessRequirement.OPEN),
                rule(HttpMethod.GET, "/v1/k/*/download", AccessRequirement.OPEN),
                // 132: a report share key serves the published HTML; the fss_ key in the path is the credential.
                rule(HttpMethod.GET, "/v1/k/*/report", AccessRequirement.OPEN),
                // CAP output and calendars (112): reads; feed keys reach them under their prefixes.
                rule(HttpMethod.GET, "/v1/cap/**", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/calendars/**", AccessRequirement.READ),
                // Testing a provider spends LLM tokens (always authenticated, 026).
                rule(HttpMethod.POST, "/v1/llm/providers/*/test", AccessRequirement.ADMIN_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/llm/probe", AccessRequirement.ADMIN_ALWAYS_AUTHENTICATED),
                rule(null, "/v1/llm/**", AccessRequirement.ADMIN),
                rule(null, "/v1/custom-sources/**", AccessRequirement.ADMIN),
                // ---- Plugins (prompt 141): out-of-tree WASM parsers; listing, switching and enabling are admin ----
                rule(null, "/v1/plugins", AccessRequirement.ADMIN),
                rule(null, "/v1/plugins/*", AccessRequirement.ADMIN),
                // ---- Mappings (prompt 059): the LLM draft spends budget, so it is always authenticated ----
                rule(HttpMethod.POST, "/v1/mappings/suggest", AccessRequirement.ADMIN_ALWAYS_AUTHENTICATED),
                rule(null, "/v1/mappings/**", AccessRequirement.ADMIN),

                // ---- Sources: reads open, configuration admin ----
                rule(HttpMethod.PUT, "/v1/sources/*/credentials", AccessRequirement.ADMIN),
                rule(HttpMethod.DELETE, "/v1/sources/*/credentials", AccessRequirement.ADMIN),
                rule(HttpMethod.POST, "/v1/sources/*/fetch", AccessRequirement.ADMIN),
                // The CFTC COT history backfill (prompt 117) spends the source's budget: admin.
                rule(HttpMethod.POST, "/v1/sources/cftc-cot/backfill", AccessRequirement.ADMIN),
                // Enabling every free source (prompt 145) is a configuration change: admin.
                rule(HttpMethod.POST, "/v1/sources/enable-free", AccessRequirement.ADMIN),
                rule(HttpMethod.POST, "/v1/sources/*/probe", AccessRequirement.ADMIN),
                // The adapter doctor (prompt 140): a run probes the upstream and may spend LLM budget, so it is admin
                // and always authenticated; its reports are agent reads (before the sources READ rule).
                rule(HttpMethod.POST, "/v1/sources/*/doctor", AccessRequirement.ADMIN_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/sources/*/doctor/reports", AccessRequirement.AGENT),
                rule(HttpMethod.GET, "/v1/doctor/reports/*", AccessRequirement.AGENT),
                rule(HttpMethod.PUT, "/v1/sources/*", AccessRequirement.ADMIN),
                // The provenance log exposes redacted URLs, ETags and body hashes: admin (013).
                rule(HttpMethod.GET, "/v1/sources/*/fetches", AccessRequirement.ADMIN),
                // The collection source set (R2-004, implemented by R2-007): admin, ahead of the sources READ rule.
                rule(HttpMethod.GET, "/v1/sources/collection", AccessRequirement.ADMIN),
                rule(HttpMethod.GET, "/v1/sources/**", AccessRequirement.READ),

                // ---- Records, changes, tiles, kinds, history: data reads ----
                // Per-entity export and purge (039) are admin and sit before the records READ rule.
                rule(HttpMethod.GET, "/v1/records/*/*/export", AccessRequirement.ADMIN),
                // The STIX 2.1 bundle (141) is agent; /v1/exports/** below does not match /v1/export/stix.
                rule(HttpMethod.GET, "/v1/export/stix", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/records/*/*/purge", AccessRequirement.ADMIN),
                // Attribution aggregates for a scope (109): read.
                rule(HttpMethod.GET, "/v1/attributions", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/records/**", AccessRequirement.READ),
                // Energy and markets series (093): the catalog and one series' points are read.
                rule(HttpMethod.GET, "/v1/series", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/series/**", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/changes/**", AccessRequirement.READ),
                // The multiplexed event stream and its filter swap; ownership is enforced in code (015).
                rule(HttpMethod.GET, "/v1/events/stream", AccessRequirement.READ),
                rule(HttpMethod.PATCH, "/v1/events/stream/*/filter", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/kinds", AccessRequirement.READ),
                // The /v1/kinds rule matches only the exact path (014).
                rule(HttpMethod.GET, "/v1/kinds/*/schema", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/history", AccessRequirement.READ),
                // History frames (037); /v1/records/** already covers the track.
                rule(HttpMethod.GET, "/v1/history/**", AccessRequirement.READ),
                // The event-density timeline (037).
                rule(HttpMethod.GET, "/v1/timeline", AccessRequirement.READ),
                // ---- Transit catalog search (070): admin, read-only; feeds are enabled by property, never by POST ----
                rule(HttpMethod.GET, "/v1/transit/catalog", AccessRequirement.ADMIN),
                // ---- Reference data (040): reads are read scope, loading is admin ----
                rule(HttpMethod.POST, "/v1/reference/load", AccessRequirement.ADMIN),
                rule(HttpMethod.GET, "/v1/reference", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/reference/**", AccessRequirement.READ),
                // ---- Stats, glance and metric projections (038) ----
                rule(HttpMethod.POST, "/v1/stats/rollup/rebuild", AccessRequirement.ADMIN),
                rule(HttpMethod.GET, "/v1/stats/**", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/glance", AccessRequirement.READ),
                // Prometheus sends Authorization: Bearer; this is not the open actuator endpoint.
                rule(HttpMethod.GET, "/v1/metrics", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/tiles/**", AccessRequirement.READ),
                // The TileJSON index; /v1/tiles/** does not match /v1/tiles.json (016).
                rule(HttpMethod.GET, "/v1/tiles.json", AccessRequirement.READ),

                // ---- Raster products and styles (042, Weather-Imagery-Design §6.11): reads ----
                rule(HttpMethod.GET, "/v1/products", AccessRequirement.READ),
                // Raster exports (108): submit is agent, but /v1/products/** GET stays read.
                rule(HttpMethod.POST, "/v1/products/*/exports", AccessRequirement.AGENT),
                rule(HttpMethod.GET, "/v1/products/**", AccessRequirement.READ),
                // WMTS and OGC API for raster products (108): read.
                rule(HttpMethod.GET, "/v1/wmts", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/wmts/**", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/ogc", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/ogc/**", AccessRequirement.READ),
                // Exports (108): downloading a file is read, polling the job is agent.
                rule(HttpMethod.GET, "/v1/exports/*/files/*", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/exports/**", AccessRequirement.AGENT),
                // Bulk exports, snapshots, holds and the cold archive (111). Listing, polling, downloads and
                // manifests are agent (the GET rule above, which also matches /v1/exports itself); a job is
                // visible to its creator and to admin. /v1/settings/archive falls under /v1/settings/** (admin).
                rule(HttpMethod.GET, "/v1/exports", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/exports", AccessRequirement.AGENT_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.DELETE, "/v1/exports/*", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/snapshots", AccessRequirement.ADMIN),
                rule(null, "/v1/holds", AccessRequirement.ADMIN),
                rule(null, "/v1/holds/**", AccessRequirement.ADMIN),
                rule(HttpMethod.GET, "/v1/archive/**", AccessRequirement.ADMIN),
                // ---- Sampling (052, Weather-Imagery-Design §6.5): reads, the POSTs included ----
                rule(HttpMethod.GET, "/v1/sample", AccessRequirement.READ),
                rule(HttpMethod.POST, "/v1/sample", AccessRequirement.READ),
                rule(HttpMethod.POST, "/v1/sample/track", AccessRequirement.READ),
                rule(HttpMethod.POST, "/v1/products/*/stats", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/styles", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/styles/**", AccessRequirement.READ),

                // ---- Media (043, Weather-Imagery-Design §6.11): reads; 133's providers are admin ----
                rule(HttpMethod.GET, "/v1/media/providers", AccessRequirement.ADMIN),
                rule(HttpMethod.GET, "/v1/media/providers/**", AccessRequirement.ADMIN),
                rule(HttpMethod.GET, "/v1/media/**", AccessRequirement.READ),

                // ---- Static maps (055, Weather-Imagery-Design §6.11): reads; a place needs a token ----
                rule(HttpMethod.GET, "/v1/maps/**", AccessRequirement.READ),

                // ---- Ground stations (049, Weather-Imagery-Design §6.11): reads ----
                rule(HttpMethod.GET, "/v1/ground-stations/**", AccessRequirement.READ),

                // ---- Places (036): a home location is sensitive, so reads are always authenticated ----
                rule(HttpMethod.GET, "/v1/places", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/places/**", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(null, "/v1/places", AccessRequirement.AGENT),
                rule(null, "/v1/places/**", AccessRequirement.AGENT),

                // ---- Saved views (036): definitions and renderings read, writes agent ----
                rule(HttpMethod.GET, "/v1/views", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/views/**", AccessRequirement.READ),
                rule(null, "/v1/views", AccessRequirement.AGENT),
                rule(null, "/v1/views/**", AccessRequirement.AGENT),

                // ---- Watchlists (036): agent for reads and writes ----
                rule(null, "/v1/watchlists", AccessRequirement.AGENT),
                rule(null, "/v1/watchlists/**", AccessRequirement.AGENT),

                // ---- Entities (098): reads open while openReads, operator writes admin ----
                rule(HttpMethod.GET, "/v1/entities", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/entities/**", AccessRequirement.READ),
                rule(null, "/v1/entities", AccessRequirement.ADMIN),
                rule(null, "/v1/entities/**", AccessRequirement.ADMIN),

                // ---- Search: read, except reindex ----
                rule(HttpMethod.POST, "/v1/search/reindex", AccessRequirement.ADMIN_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.DELETE, "/v1/search/reindex", AccessRequirement.ADMIN),
                rule(HttpMethod.GET, "/v1/search/status", AccessRequirement.READ),
                // Semantic search embeds the query: always authenticated (023, 027).
                rule(HttpMethod.GET, "/v1/search", AccessRequirement.READ_ALWAYS_AUTHENTICATED),

                // ---- Map styles, feeds and KML (058): reads, ordered before the broader alert, briefing and
                // trigger rules; feed keys (035) reach them under their prefixes, tile keys (050) /v1/map/ ----
                rule(HttpMethod.GET, "/v1/map/**", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/kml/**", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/alerts/feed.*", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/briefings/*/runs/feed.*", AccessRequirement.READ),
                // Prompt 141: the briefing podcast (RSS 2.0 with audio enclosures), a read like the run feeds.
                rule(HttpMethod.GET, "/v1/briefings/*/podcast.rss", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/triggers/*.kml", AccessRequirement.READ),

                // ---- Triggers: read to list, agent to change ----
                // ---- Exercise replays (prompt 115): agent, always authenticated (they deliver to channels) ----
                rule(null, "/v1/exercises", AccessRequirement.AGENT_ALWAYS_AUTHENTICATED),
                rule(null, "/v1/exercises/**", AccessRequirement.AGENT_ALWAYS_AUTHENTICATED),
                // Tuning (115): suggestions and the tuning log are reads; apply, dismiss and undo fall under the
                // agent rule for /v1/triggers/** below.
                rule(HttpMethod.GET, "/v1/triggers/*/tuning-log", AccessRequirement.READ),
                // The advisory source-relevance check spends LLM budget, so it always needs an agent token.
                rule(HttpMethod.POST, "/v1/triggers/analyze-sources", AccessRequirement.AGENT_ALWAYS_AUTHENTICATED),
                // ---- Pattern of life, trips and kinematics (prompt 114) ----
                // Pattern of life and trips are agent operations, reads included (private subjects need admin,
                // which the service checks); CPA and ETA are reads.
                rule(null, "/v1/pol/**", AccessRequirement.AGENT),
                rule(null, "/v1/trips", AccessRequirement.AGENT),
                rule(null, "/v1/trips/**", AccessRequirement.AGENT),
                rule(HttpMethod.GET, "/v1/kinematics/**", AccessRequirement.READ),
                rule(HttpMethod.POST, "/v1/kinematics/cpa/batch", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/triggers/**", AccessRequirement.READ),
                rule(null, "/v1/triggers/**", AccessRequirement.AGENT),

                // ---- Anomalies feed (prompt 100): a read ----
                rule(HttpMethod.GET, "/v1/anomalies/**", AccessRequirement.READ),

                // ---- Intelligence, investigations, exposure and packs (prompt 101) ----
                rule(HttpMethod.POST, "/v1/intel/recipes/*/test", AccessRequirement.AGENT),
                // Prompt 157: authoring an operator recipe (full definition) is admin, like updating one.
                rule(HttpMethod.POST, "/v1/intel/recipes", AccessRequirement.ADMIN),
                rule(HttpMethod.PUT, "/v1/intel/recipes/*", AccessRequirement.ADMIN),
                // The corroboration policy is admin (154), like the recipes it gates.
                rule(HttpMethod.PUT, "/v1/intel/corroboration", AccessRequirement.ADMIN),
                rule(HttpMethod.GET, "/v1/intel/**", AccessRequirement.READ),
                rule(null, "/v1/intel/**", AccessRequirement.AGENT),
                rule(HttpMethod.GET, "/v1/investigations/**", AccessRequirement.READ),
                rule(null, "/v1/investigations/**", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/exposure", AccessRequirement.READ),
                rule(HttpMethod.POST, "/v1/packs/population/import", AccessRequirement.ADMIN),
                rule(HttpMethod.POST, "/v1/packs", AccessRequirement.AGENT_ALWAYS_AUTHENTICATED),

                // ---- Attention topics (prompt 117): reads are read, changes agent ----
                rule(HttpMethod.GET, "/v1/attention/topics", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/attention/topics/*", AccessRequirement.READ),
                rule(HttpMethod.POST, "/v1/attention/topics", AccessRequirement.AGENT),
                rule(HttpMethod.PUT, "/v1/attention/topics/*", AccessRequirement.AGENT),
                rule(HttpMethod.DELETE, "/v1/attention/topics/*", AccessRequirement.AGENT),
                // Prompt 133: news stories and articles, the outlet registry (read); outlet edits, provider updates and
                // imports (admin). Reading the providers is admin by the 043 rows above.
                rule(HttpMethod.GET, "/v1/news/stories", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/news/stories/*", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/news/articles", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/media/outlets", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/media/outlets/*", AccessRequirement.READ),
                rule(HttpMethod.PATCH, "/v1/media/outlets/*", AccessRequirement.ADMIN),
                rule(HttpMethod.PUT, "/v1/media/providers/*", AccessRequirement.ADMIN),
                rule(HttpMethod.POST, "/v1/media/ratings/import", AccessRequirement.ADMIN),
                // Prompt 135: social platforms, coverage, volume and posts are reads; platform settings are admin.
                rule(HttpMethod.GET, "/v1/social/platforms", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/social/platforms/*", AccessRequirement.READ),
                rule(HttpMethod.PUT, "/v1/social/platforms/*", AccessRequirement.ADMIN),
                rule(HttpMethod.GET, "/v1/social/coverage", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/social/volume", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/social/posts", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/social/posts/*/*", AccessRequirement.READ),
                // Prompt 136: narratives, claims and fingerprints are reads; merge and split are agent; image matching
                // needs a token (an upload).
                rule(HttpMethod.GET, "/v1/social/narratives", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/social/narratives/*", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/social/narratives/*/posts", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/social/narratives/*/propagation", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/social/narratives/*/timeline", AccessRequirement.READ),
                rule(HttpMethod.POST, "/v1/social/narratives/*/merge", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/social/narratives/*/split", AccessRequirement.AGENT),
                rule(HttpMethod.GET, "/v1/social/claims", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/social/claims/*", AccessRequirement.READ),
                rule(HttpMethod.POST, "/v1/social/media/match", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/social/media/*", AccessRequirement.READ),
                // Prompt 137: comparisons, cues and lead times are reads; a manual cue is an agent operation
                // (it spends platform budgets); /v1/settings/social falls under /v1/settings/** (admin).
                rule(HttpMethod.GET, "/v1/social/comparisons", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/social/comparisons/*", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/social/cues", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/social/cues/*", AccessRequirement.READ),
                rule(HttpMethod.POST, "/v1/social/cues", AccessRequirement.AGENT_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/social/lead-times", AccessRequirement.READ),

                // ---- Evidence identity, observability and manifests (prompt 116) ----
                // Canonical events, the coverage layer and prompt templates are reads; manifests and their
                // replay are agent operations (a replay is a deterministic 103 job and spends no LLM budget).
                rule(HttpMethod.GET, "/v1/evidence/**", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/observability", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/observability/**", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/manifests/*", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/manifests/*/replay", AccessRequirement.AGENT),
                rule(HttpMethod.GET, "/v1/prompt-templates", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/prompt-templates/*", AccessRequirement.READ),

                // ---- Situations (prompt 118): reads are read; changes, the brief and the linker dry run agent. The
                // brief may spend LLM budget, so it is always authenticated. ----
                rule(HttpMethod.POST, "/v1/situations/*/brief", AccessRequirement.AGENT_ALWAYS_AUTHENTICATED),
                // An on-demand situation assessment (prompt 128) runs the cycle roles and may spend LLM budget.
                rule(HttpMethod.POST, "/v1/situations/*/assessment", AccessRequirement.AGENT_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/situations/linker/test", AccessRequirement.AGENT),
                rule(HttpMethod.GET, "/v1/situations", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/situations/**", AccessRequirement.READ),
                rule(null, "/v1/situations", AccessRequirement.AGENT),
                rule(null, "/v1/situations/**", AccessRequirement.AGENT),

                // ---- Discovery (prompt 119): series data, the registry and findings are reads; registry changes,
                // dismissals and batch event studies are agent operations (no LLM is used). ----
                rule(HttpMethod.GET, "/v1/analytics/series/data", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/analytics/series", AccessRequirement.READ),
                rule(HttpMethod.PUT, "/v1/analytics/series", AccessRequirement.AGENT),
                rule(HttpMethod.DELETE, "/v1/analytics/series", AccessRequirement.AGENT),
                rule(HttpMethod.GET, "/v1/analytics/findings", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/analytics/findings/*", AccessRequirement.READ),
                rule(HttpMethod.PATCH, "/v1/analytics/findings/*", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/analytics/event-studies", AccessRequirement.AGENT),

                // ---- Precursor library (prompt 120): the library is a read; batches, candidates, retire/pin, mining
                // and re-tests are agent operations (no LLM is used). ----
                rule(HttpMethod.GET, "/v1/precursors/batches", AccessRequirement.AGENT),
                rule(HttpMethod.GET, "/v1/precursors/batches/*", AccessRequirement.AGENT),
                rule(HttpMethod.GET, "/v1/precursors/candidates", AccessRequirement.AGENT),
                rule(HttpMethod.GET, "/v1/precursors", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/precursors/*", AccessRequirement.READ),
                rule(null, "/v1/precursors/**", AccessRequirement.AGENT),

                // ---- Base rates, reference imports and analogs (prompt 121): rates, ad-hoc queries and analogs are
                // reads; storing, deleting and refreshing rates are agent operations; reference imports are admin
                // (they read operator files and spend upstream budgets). No LLM is used. ----
                rule(HttpMethod.POST, "/v1/base-rates/query", AccessRequirement.READ),
                rule(HttpMethod.POST, "/v1/base-rates/refresh", AccessRequirement.AGENT),
                rule(HttpMethod.GET, "/v1/base-rates", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/base-rates/*", AccessRequirement.READ),
                rule(null, "/v1/base-rates", AccessRequirement.AGENT),
                rule(null, "/v1/base-rates/**", AccessRequirement.AGENT),
                rule(null, "/v1/reference-imports", AccessRequirement.ADMIN),
                rule(null, "/v1/reference-imports/**", AccessRequirement.ADMIN),
                rule(HttpMethod.POST, "/v1/analogs/query", AccessRequirement.READ),

                // ---- Forecast ledger (prompt 122): questions, forecasts, forecasters, scorecards, calibration and
                // comparisons are reads; a forecast is a read-scope write under the caller's operator forecaster, so it
                // always needs a token; opening, changing, annulling and resolving questions, evaluations and the
                // ensemble refit are agent operations; forecaster settings are admin. ----
                rule(HttpMethod.POST, "/v1/forecasts/questions/*/forecasts",
                        AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/forecasts/evaluations", AccessRequirement.AGENT_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/forecasts/evaluations/*", AccessRequirement.AGENT),
                rule(HttpMethod.PUT, "/v1/forecasts/forecasters/*", AccessRequirement.ADMIN),
                rule(HttpMethod.POST, "/v1/forecasts/forecasters", AccessRequirement.ADMIN),
                rule(HttpMethod.GET, "/v1/forecasts/questions", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/forecasts/questions/**", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/forecasts/forecasters", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/forecasts/scorecard", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/forecasts/calibration", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/forecasts/compare", AccessRequirement.READ),
                rule(null, "/v1/forecasts/**", AccessRequirement.AGENT),

                // ---- Rolling hypotheses (prompt 125): sets, revisions, the ACH matrix, information value,
                // indicators and collection gaps are reads; creating and changing sets, hypotheses, indicators,
                // evidence, assumptions and reviews are agent operations; an LLM draft always needs a token. ----
                rule(HttpMethod.POST, "/v1/hypotheses/sets/draft", AccessRequirement.AGENT_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/hypotheses/sets", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/hypotheses/sets/**", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/indicators", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/collection-gaps", AccessRequirement.READ),
                rule(null, "/v1/hypotheses/**", AccessRequirement.AGENT),
                rule(null, "/v1/indicators/**", AccessRequirement.AGENT),

                // ---- Series forecasts, regional outlooks and indices (prompt 126): forecasts, models, outlooks,
                // index definitions and values are reads; defining an index, fitting its weights and applying weights
                // are agent operations (no LLM is used). ----
                rule(HttpMethod.GET, "/v1/analytics/series/forecast", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/analytics/series/forecast/model", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/outlooks", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/outlooks/**", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/indices", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/indices/*", AccessRequirement.READ),
                rule(null, "/v1/indices/**", AccessRequirement.AGENT),

                // ---- Impact projection (prompt 127): projections and conditional rates are reads. ----
                rule(HttpMethod.GET, "/v1/analytics/impacts", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/analytics/impacts/*", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/analytics/impact-rates", AccessRequirement.READ),

                // ---- Analytic benchmark harness (prompt 123): runs, their results and the status per component are
                // reads; starting a run and the historical corpus are agent operations. ----
                rule(HttpMethod.GET, "/v1/benchmarks/runs", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/benchmarks/runs/*", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/benchmarks/status", AccessRequirement.READ),
                rule(null, "/v1/benchmarks/**", AccessRequirement.AGENT),

                // ---- Analysis cycles and assessments (prompt 128): cycle definitions and assessments are reads;
                // changing cycles and reading runs are agent operations; running a cycle and the monthly maintenance may
                // spend LLM budget or start jobs, so they are always authenticated; feedback is a read-scope write, like
                // the 102 note feedback. The intel board is a read under /v1/intel/**. ----
                rule(HttpMethod.POST, "/v1/analysis/cycles/*/run", AccessRequirement.AGENT_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/analysis/maintenance/run", AccessRequirement.AGENT_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/analysis/cycles", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/analysis/cycles/*/runs", AccessRequirement.AGENT),
                rule(HttpMethod.GET, "/v1/analysis/cycles/*", AccessRequirement.READ),
                rule(null, "/v1/analysis/**", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/assessments/*/feedback", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/assessments", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/assessments/**", AccessRequirement.READ),

                // ---- Auth (prompt R2-003): signing in, resetting and activating need no credential; the session
                // operations always need one (a signed-in session; AuthController refuses an API token). Specific
                // before general. ----
                rule(HttpMethod.POST, "/v1/auth/login", AccessRequirement.OPEN),
                rule(HttpMethod.POST, "/v1/auth/password/reset-request", AccessRequirement.OPEN),
                rule(HttpMethod.POST, "/v1/auth/password/reset", AccessRequirement.OPEN),
                rule(HttpMethod.POST, "/v1/auth/activate", AccessRequirement.OPEN),
                rule(HttpMethod.POST, "/v1/auth/logout", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.DELETE, "/v1/auth/sessions/*", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/auth/sessions", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.DELETE, "/v1/auth/sessions", AccessRequirement.READ_ALWAYS_AUTHENTICATED),

                // ---- Collaboration (prompt 130): every operation needs a token. Reads, tags, comments, attachments,
                // evidence items and saved searches are read-scope (the access lists decide per object); groups,
                // access-list changes and shares are agent; principals and their tokens are admin. ----
                rule(HttpMethod.GET, "/v1/me", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                // Self-service (R2-004, written ahead of the prompts that implement them): every /v1/me operation acts
                // on the caller, so a credential is always needed; the session's permission decides the rest.
                rule(HttpMethod.GET, "/v1/me/sources", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.PUT, "/v1/me/sources", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/me/llm/budget", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/me/preferences", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.PUT, "/v1/me/preferences", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/me/mfa/enroll", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/me/mfa/verify", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/me/mfa/disable", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/me/mfa/recovery-codes", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/me/tokens", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/me/tokens", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.DELETE, "/v1/me/tokens/*", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/me/quota", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/me/notification-preferences", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.PUT, "/v1/me/notification-preferences", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/inbox", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/principals", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/principals", AccessRequirement.ADMIN),
                rule(HttpMethod.POST, "/v1/principals/*/tokens", AccessRequirement.ADMIN),
                rule(HttpMethod.GET, "/v1/principals/*", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.PATCH, "/v1/principals/*", AccessRequirement.ADMIN),
                rule(HttpMethod.GET, "/v1/groups", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/groups", AccessRequirement.AGENT),
                rule(HttpMethod.GET, "/v1/groups/*", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.PATCH, "/v1/groups/*", AccessRequirement.AGENT),
                rule(HttpMethod.DELETE, "/v1/groups/*", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/groups/*/members", AccessRequirement.AGENT),
                rule(HttpMethod.DELETE, "/v1/groups/*/members", AccessRequirement.AGENT),
                rule(HttpMethod.GET, "/v1/objects/*/acl", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.PUT, "/v1/objects/*/acl", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/objects/*/share", AccessRequirement.AGENT),
                rule(HttpMethod.GET, "/v1/objects/*/tags", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/shared-with-me", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/my", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/tags", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/tags", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/tags/*", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.PATCH, "/v1/tags/*", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.DELETE, "/v1/tags/*", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/tags/*/assign", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/tags/*/unassign", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/tags/*/objects", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/annotations/*/replies", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/annotations/*/resolve", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/attachments", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/attachments/*", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.DELETE, "/v1/attachments/*", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/attachments/*/content", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/evidence-items", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/evidence-items", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/evidence-items/*", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.PATCH, "/v1/evidence-items/*", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.DELETE, "/v1/evidence-items/*", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/saved-searches", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/saved-searches", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/saved-searches/*", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.PATCH, "/v1/saved-searches/*", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.DELETE, "/v1/saved-searches/*", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/saved-searches/*/results", AccessRequirement.READ_ALWAYS_AUTHENTICATED),

                // ---- Chronicles (prompt 131): reads, the thread and its read cursor need a token at read scope
                // (the controller checks the viewer role); creating, changing, layers, the lifecycle, suggestions and
                // exports are agent operations (the controller checks editor or owner). Each 131 route has its own
                // rule; the 134 chronicle-network routes under /v1/chronicles/{id}/ have theirs below. ----
                rule(HttpMethod.GET, "/v1/chronicles", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/chronicles", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/chronicles/suggest", AccessRequirement.AGENT_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/chronicles/*", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.PATCH, "/v1/chronicles/*", AccessRequirement.AGENT),
                rule(HttpMethod.DELETE, "/v1/chronicles/*", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/chronicles/*/layers", AccessRequirement.AGENT),
                rule(HttpMethod.PATCH, "/v1/chronicles/*/layers/*", AccessRequirement.AGENT),
                rule(HttpMethod.DELETE, "/v1/chronicles/*/layers/*", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/chronicles/*/activate", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/chronicles/*/pause", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/chronicles/*/resume", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/chronicles/*/stop", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/chronicles/*/archive", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/chronicles/*/restore", AccessRequirement.AGENT),
                rule(HttpMethod.GET, "/v1/chronicles/*/estimate", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/chronicles/*/export", AccessRequirement.AGENT),
                rule(HttpMethod.GET, "/v1/chronicles/*/thread", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/chronicles/*/thread/read", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/chronicles/*/state", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/chronicles/*/tiles/**", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/chronicles/*/frames", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/chronicles/*/tracks/*/*", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/chronicles/*/series", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/chronicles/*/summary", AccessRequirement.READ_ALWAYS_AUTHENTICATED),

                // ---- Chronicle network (prompt 134): related links, clusters, timelines, the request list, dismissing
                // and requesting access are read-scope operations held to a token (the service decides disclosure and
                // ownership); discoverability and decisions on requests are agent operations for the owner; network
                // salts are admin; the peer sketch inbox is a PEER rule above. ----
                rule(HttpMethod.GET, "/v1/chronicles/*/related", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.PUT, "/v1/chronicles/*/discoverability", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/chronicle-network/links/*/dismiss",
                        AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/chronicle-network/links/*/request-access",
                        AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/chronicle-network/clusters", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/chronicle-network/clusters/*", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/chronicle-network/clusters/*/timeline",
                        AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/chronicle-network/requests", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/chronicle-network/requests/*/grant", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/chronicle-network/requests/*/decline", AccessRequirement.AGENT),
                rule(HttpMethod.PUT, "/v1/chronicle-network/salts", AccessRequirement.ADMIN),

                // ---- Reports and exchange (prompt 132): report reads and edits are read-scope operations held to a
                // token (the controller checks viewer, editor or owner on the report); sending, share links, building
                // and downloading packages and the sent list are agent; the peer inbox and receipts take a peer
                // ingest key; importing an uploaded package is admin; received packages are read per recipient. ----
                rule(HttpMethod.GET, "/v1/reports", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/reports", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/reports/draft", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/reports/*", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.PATCH, "/v1/reports/*", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.DELETE, "/v1/reports/*", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/reports/*/blocks", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.PATCH, "/v1/reports/*/blocks/*", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.DELETE, "/v1/reports/*/blocks/*", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/reports/*/publish", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/reports/*/retract", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/reports/*/revisions", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/reports/*/rendition", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/reports/*/send", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/reports/*/share-link", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/exchange/packages", AccessRequirement.AGENT),
                rule(HttpMethod.GET, "/v1/exchange/packages/*", AccessRequirement.AGENT),
                rule(HttpMethod.GET, "/v1/exchange/packages/*/download", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/exchange/inbox", AccessRequirement.PEER),
                rule(HttpMethod.POST, "/v1/exchange/receipts", AccessRequirement.PEER),
                rule(HttpMethod.POST, "/v1/exchange/import", AccessRequirement.ADMIN),
                rule(HttpMethod.GET, "/v1/exchange/received", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/exchange/received/*", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/exchange/received/*/rendition", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/exchange/received/*/acknowledge", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/exchange/sent", AccessRequirement.AGENT),

                // ---- Objects, relations, annotations and workspaces (prompt 129): object reads, graph queries, the
                // context summary, precision, sensor discovery and deep links are reads; annotations and workspaces are
                // read-scope writes that the controllers hold to a token (verdicts and workspace metadata changes check
                // agent scope there); operator relations and workspace share links are agent operations. The 130 routes
                // under /v1/objects/{ref}/ and /v1/annotations/{id}/ keep their own rules. ----
                rule(HttpMethod.GET, "/v1/contract", AccessRequirement.READ),
                rule(HttpMethod.POST, "/v1/objects/resolve", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/objects/*", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/objects/*/dossier", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/objects/*/related", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/objects/*/annotations", AccessRequirement.READ),
                rule(HttpMethod.POST, "/v1/objects/*/annotations", AccessRequirement.READ),
                rule(HttpMethod.PATCH, "/v1/annotations/*", AccessRequirement.READ),
                rule(HttpMethod.DELETE, "/v1/annotations/*", AccessRequirement.READ),
                rule(HttpMethod.POST, "/v1/context/summary", AccessRequirement.READ),
                rule(HttpMethod.POST, "/v1/context-links", AccessRequirement.READ),
                rule(HttpMethod.POST, "/v1/graph", AccessRequirement.READ),
                rule(HttpMethod.POST, "/v1/graph/common-dependencies", AccessRequirement.READ),
                rule(HttpMethod.POST, "/v1/graph/ownership-chain", AccessRequirement.READ),
                rule(HttpMethod.POST, "/v1/relations", AccessRequirement.AGENT),
                rule(HttpMethod.DELETE, "/v1/relations/*", AccessRequirement.AGENT),
                rule(HttpMethod.GET, "/v1/analytics/precision", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/sensors", AccessRequirement.READ),
                rule(HttpMethod.POST, "/v1/workspaces/*/share", AccessRequirement.AGENT),
                rule(HttpMethod.GET, "/v1/workspaces", AccessRequirement.READ),
                rule(HttpMethod.POST, "/v1/workspaces", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/workspaces/*", AccessRequirement.READ),
                rule(HttpMethod.PATCH, "/v1/workspaces/*", AccessRequirement.READ),
                rule(HttpMethod.DELETE, "/v1/workspaces/*", AccessRequirement.READ),
                rule(HttpMethod.PUT, "/v1/workspaces/*/layout", AccessRequirement.READ),
                rule(HttpMethod.PATCH, "/v1/workspaces/*/context", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/workspaces/*/summary", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/workspaces/*/context-link", AccessRequirement.READ),

                // ---- Autonomous analyst (prompt 102) ----
                rule(HttpMethod.POST, "/v1/analyst/profiles/*/run", AccessRequirement.AGENT_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/analyst/profiles/**", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/analyst/notes/**", AccessRequirement.READ),
                rule(HttpMethod.PATCH, "/v1/analyst/notes/*", AccessRequirement.READ),
                rule(HttpMethod.POST, "/v1/analyst/notes/*/feedback", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/analyst/runs/**", AccessRequirement.AGENT),
                rule(HttpMethod.GET, "/v1/analyst/proposals/**", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/analyst/proposals/*/confirm", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/analyst/proposals/*/reject", AccessRequirement.AGENT),
                rule(HttpMethod.POST, "/v1/analyst/proposals/*/feedback", AccessRequirement.AGENT),
                rule(HttpMethod.GET, "/v1/analyst/evaluation", AccessRequirement.AGENT),
                rule(null, "/v1/analyst/**", AccessRequirement.AGENT),

                // ---- Backtesting, jobs and precursor tests (prompt 103) ----
                rule(HttpMethod.GET, "/v1/triggers/*/quality", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/triggers/*/suggestions", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/precursor-tests/**", AccessRequirement.READ),
                rule(null, "/v1/precursor-tests/**", AccessRequirement.AGENT),
                rule(null, "/v1/backtests/**", AccessRequirement.AGENT),
                rule(null, "/v1/jobs/**", AccessRequirement.AGENT),

                // ---- Infrastructure graph (prompt 104) ----
                rule(HttpMethod.POST, "/v1/infra/rebuild", AccessRequirement.ADMIN),
                rule(HttpMethod.POST, "/v1/infra/affected", AccessRequirement.READ),
                rule(HttpMethod.GET, "/v1/infra/**", AccessRequirement.READ),
                rule(null, "/v1/settings/infra-rules", AccessRequirement.ADMIN),

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
                // The catch-up digest is a data read (029).
                rule(HttpMethod.GET, "/v1/digest", AccessRequirement.READ),

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
                rule(HttpMethod.DELETE, "/v1/push/subscriptions/*", AccessRequirement.READ_ALWAYS_AUTHENTICATED),

                // ---- Notification inbox and presence (033): per device, so a token is always needed ----
                rule(null, "/v1/notifications", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(null, "/v1/notifications/**", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.POST, "/v1/clients/presence", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(HttpMethod.GET, "/v1/clients", AccessRequirement.ADMIN),

                // ---- Escalation policies (033): agent ----
                rule(null, "/v1/escalation-policies", AccessRequirement.AGENT),
                rule(null, "/v1/escalation-policies/**", AccessRequirement.AGENT),

                // ---- Native push (032): providers are admin; devices are managed by their own token ----
                rule(null, "/v1/push/providers", AccessRequirement.ADMIN),
                rule(null, "/v1/push/providers/**", AccessRequirement.ADMIN),
                // Registering makes the server push to a device: always authenticated (023, 032).
                rule(null, "/v1/push/devices", AccessRequirement.READ_ALWAYS_AUTHENTICATED),
                rule(null, "/v1/push/devices/**", AccessRequirement.READ_ALWAYS_AUTHENTICATED));
    }

    private static Rule rule(HttpMethod method, String pattern, AccessRequirement requirement) {
        return new Rule(method, pattern, requirement);
    }

    /** One row of the policy table. A null {@code method} matches every method. */
    record Rule(HttpMethod method, String pattern, AccessRequirement requirement) {

        boolean matches(AntPathMatcher matcher, HttpMethod candidate, String path) {
            // HEAD is GET without the body (RFC 9110 §9.3.2), so a GET rule covers it. Checkpoint 057: before,
            // a HEAD matched no rule and fell through to the fail-closed admin requirement (403 for every other
            // caller), although key-in-path URLs (050) and Spring MVC both serve HEAD.
            boolean sameMethod = method == null || method.equals(candidate)
                    || (HttpMethod.GET.equals(method) && HttpMethod.HEAD.equals(candidate));
            if (!sameMethod) {
                return false;
            }
            return matcher.match(pattern, path);
        }
    }
}
