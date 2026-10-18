package ai.foresignal.mcp.oauth;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

/**
 * Reads, validates and stores {@link McpAuthSettings} ({@code GET/PUT /v1/settings/mcp-auth}, prompt 141), and
 * derives the values the rest of MCP remote authorization needs: the effective audience and the URLs of the
 * protected resource metadata.
 *
 * <p>A PUT replaces the whole value; every problem is reported in one {@code 400} with its {@code field}
 * ({@code URL_NOT_ALLOWED} when one of them is an outbound URL refusal, else {@code VALIDATION_FAILED}). Rules:
 *
 * <ul>
 *   <li>{@code mode} is required: {@code token}, {@code oauth} or {@code both};
 *   <li>with {@code oauth} or {@code both}, {@code issuer} is required, and so is {@code audience} unless
 *       {@code server.publicBaseUrl} is set (the audience defaults to {@code {publicBaseUrl}/mcp});
 *   <li>{@code issuer} and {@code jwksUri} are absolute {@code http(s)} URLs without query or fragment that pass the
 *       outbound URL policy ({@code LAN_ALLOWED}; plain {@code http} only for local, private or tailnet hosts);
 *   <li>{@code scopeMap} values are {@code read} or {@code agent}; {@code admin} is never mappable;
 *   <li>{@code scopeClaim} is {@code scope} or {@code scp}; {@code allowedAlgs} a non-empty subset of
 *       {@code RS256}, {@code ES256}, {@code EdDSA}, {@code Ed25519}.
 * </ul>
 */
@Service
public class McpAuthSettingsService {

    /** The members of {@code McpAuthSettings}. */
    static final Set<String> FIELDS = Set.of("mode", "issuer", "audience", "jwksUri", "scopeMap", "scopeClaim",
            "allowedAlgs");

    /** The most {@code scopeMap} entries. */
    static final int MAX_SCOPES = 50;

    /** An identity-provider scope name: RFC 6749 §3.3 scope-token characters, at most 200. */
    private static final Pattern SCOPE_TOKEN = Pattern.compile("^[\\x21\\x23-\\x5B\\x5D-\\x7E]{1,200}$");

    /** The path of the MCP server, appended to the public base URL for the default audience. */
    public static final String MCP_PATH = "/mcp";

    /** The RFC 9728 metadata path of the {@code /mcp} resource. */
    public static final String METADATA_PATH = "/.well-known/oauth-protected-resource/mcp";

    private final AppSettingService settings;
    private final OutboundUrlPolicy policy;

    /** Creates the service. */
    public McpAuthSettingsService(AppSettingService settings, OutboundUrlPolicy policy) {
        this.settings = settings;
        this.policy = policy;
    }

    /** The stored settings, or the defaults ({@code mode: token}). */
    public McpAuthSettings get() {
        return settings.getOrDefault(SettingKeys.MCP_AUTH, McpAuthSettings.class, McpAuthSettings::defaults);
    }

    /**
     * Validates and stores {@code body}, replacing the settings.
     *
     * @throws ValidationException listing every problem
     */
    public McpAuthSettings update(JsonNode body) {
        McpAuthSettings value = parse(body);
        settings.put(SettingKeys.MCP_AUTH, value);
        return value;
    }

    /**
     * The audience tokens must name: {@code audience}, else {@code {server.publicBaseUrl}/mcp}, else null (then no
     * token is accepted: a resource URI taken from the request's {@code Host} could be chosen by the caller).
     */
    public String effectiveAudience(McpAuthSettings value) {
        if (value.audience() != null && !value.audience().isBlank()) {
            return value.audience().strip();
        }
        String base = publicBaseUrl();
        return base == null ? null : base + MCP_PATH;
    }

    /** {@code server.publicBaseUrl} without a trailing slash, or null. */
    public String publicBaseUrl() {
        return settings.getOrDefault(SettingKeys.SERVER, ServerSettings.class, ServerSettings::defaults)
                .normalizedPublicBaseUrl();
    }

    /** {@code server.publicBaseUrl}, else the request's own scheme, host and port. */
    public String baseUrl(HttpServletRequest request) {
        String configured = publicBaseUrl();
        if (configured != null) {
            return configured;
        }
        String host = request.getServerName();
        String h = host.contains(":") && !host.startsWith("[") ? "[" + host + "]" : host;
        int port = request.getServerPort();
        String scheme = request.getScheme();
        boolean defaultPort = port <= 0 || ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);
        return scheme + "://" + h + (defaultPort ? "" : ":" + port);
    }

    /** The URL of the {@code /mcp} protected resource metadata, as {@code resource_metadata} names it. */
    public String metadataUrl(HttpServletRequest request) {
        return baseUrl(request) + METADATA_PATH;
    }

    /**
     * The canonical resource URI shown in the metadata: the effective audience, else (no public base URL known)
     * the request's base plus {@code /mcp}.
     */
    public String resource(McpAuthSettings value, HttpServletRequest request) {
        String audience = effectiveAudience(value);
        return audience != null ? audience : baseUrl(request) + MCP_PATH;
    }

    /**
     * Parses and validates an {@code McpAuthSettings} body.
     *
     * @throws ValidationException listing every problem
     */
    McpAuthSettings parse(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be an McpAuthSettings object.");
        }
        List<FieldError> errors = new ArrayList<>();
        boolean urlRefused = false;
        for (String field : body.propertyNames()) {
            if (!FIELDS.contains(field)) {
                errors.add(new FieldError(field, "is not an McpAuthSettings member"));
            }
        }
        McpAuthMode mode = null;
        JsonNode modeNode = body.get("mode");
        if (modeNode == null || modeNode.isNull()) {
            errors.add(new FieldError("mode", "is required: token, oauth or both"));
        } else if (!modeNode.isString() || McpAuthMode.fromWireValue(modeNode.asString()).isEmpty()) {
            errors.add(new FieldError("mode", "must be token, oauth or both"));
        } else {
            mode = McpAuthMode.fromWireValue(modeNode.asString()).get();
        }
        String issuer = optionalString(body, "issuer", errors);
        String audience = optionalString(body, "audience", errors);
        String jwksUri = optionalString(body, "jwksUri", errors);
        if (issuer != null) {
            String problem = urlProblem(issuer, true);
            if (problem != null) {
                errors.add(new FieldError("issuer", problem));
                urlRefused |= problem.startsWith(REFUSED);
            }
        }
        if (jwksUri != null) {
            String problem = urlProblem(jwksUri, true);
            if (problem != null) {
                errors.add(new FieldError("jwksUri", problem));
                urlRefused |= problem.startsWith(REFUSED);
            }
        }
        if (audience != null) {
            String problem = urlProblem(audience, false);
            if (problem != null) {
                errors.add(new FieldError("audience", problem));
            }
        }
        if (mode != null && mode.oauth()) {
            if (issuer == null) {
                errors.add(new FieldError("issuer", "is required while mode is " + mode.wireValue()));
            }
            if (audience == null && publicBaseUrl() == null) {
                errors.add(new FieldError("audience", "is required while server.publicBaseUrl is not set (it"
                        + " defaults to {publicBaseUrl}/mcp)"));
            }
        }
        Map<String, String> scopeMap = scopeMap(body, errors);
        String scopeClaim = scopeClaim(body, errors);
        List<String> allowedAlgs = allowedAlgs(body, errors);
        if (!errors.isEmpty()) {
            if (urlRefused) {
                throw OutboundUrlPolicy.notAllowed(errors);
            }
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED, "The MCP authorization settings are not valid.",
                    errors);
        }
        return new McpAuthSettings(mode.wireValue(), issuer, audience, jwksUri, scopeMap, scopeClaim, allowedAlgs);
    }

    /** The prefix of an outbound URL policy refusal, which turns the 400 into {@code URL_NOT_ALLOWED}. */
    private static final String REFUSED = "is refused by the outbound URL policy: ";

    /**
     * Why {@code value} is not an acceptable URL, or null.
     *
     * @param fetched true for URLs Foresignal fetches (issuer, JWKS), which must pass the outbound URL policy
     */
    private String urlProblem(String value, boolean fetched) {
        URI uri;
        try {
            uri = new URI(value);
        } catch (URISyntaxException e) {
            return "must be an absolute http:// or https:// URL";
        }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!uri.isAbsolute() || uri.isOpaque() || !(scheme.equals("http") || scheme.equals("https"))
                || uri.getHost() == null) {
            return "must be an absolute http:// or https:// URL";
        }
        if (uri.getRawQuery() != null || uri.getRawFragment() != null) {
            return "must not carry a query or fragment";
        }
        if (fetched) {
            String refused = OutboundIdpDocumentFetcher.problem(policy, uri, true);
            if (refused != null) {
                return REFUSED + refused;
            }
        }
        return null;
    }

    private static String optionalString(JsonNode body, String field, List<FieldError> errors) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isString()) {
            errors.add(new FieldError(field, "must be a string or null"));
            return null;
        }
        String value = node.asString().strip();
        if (value.isEmpty()) {
            return null;
        }
        if (value.length() > 2048) {
            errors.add(new FieldError(field, "must be at most 2048 characters"));
            return null;
        }
        return value;
    }

    private static Map<String, String> scopeMap(JsonNode body, List<FieldError> errors) {
        JsonNode node = body.get("scopeMap");
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isObject()) {
            errors.add(new FieldError("scopeMap", "must be an object from identity-provider scope to read or agent"));
            return null;
        }
        if (node.size() > MAX_SCOPES) {
            errors.add(new FieldError("scopeMap", "must have at most " + MAX_SCOPES + " entries"));
            return null;
        }
        Map<String, String> out = new LinkedHashMap<>();
        for (String key : node.propertyNames()) {
            JsonNode value = node.get(key);
            String path = "scopeMap." + key;
            if (!SCOPE_TOKEN.matcher(key).matches()) {
                errors.add(new FieldError(path, "is not a valid scope name (RFC 6749 scope-token, at most 200)"));
                continue;
            }
            if (value == null || !value.isString()) {
                errors.add(new FieldError(path, "must be read or agent"));
                continue;
            }
            String scope = value.asString().strip().toLowerCase(Locale.ROOT);
            if (scope.equals("admin")) {
                errors.add(new FieldError(path, "admin is never mappable: identity-provider tokens reach read and"
                        + " agent tools only"));
            } else if (!scope.equals("read") && !scope.equals("agent")) {
                errors.add(new FieldError(path, "must be read or agent"));
            } else {
                out.put(key, scope);
            }
        }
        return out;
    }

    private static String scopeClaim(JsonNode body, List<FieldError> errors) {
        JsonNode node = body.get("scopeClaim");
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isString() || !McpAuthSettings.SCOPE_CLAIMS.contains(node.asString().strip())) {
            errors.add(new FieldError("scopeClaim", "must be scope or scp"));
            return null;
        }
        return node.asString().strip();
    }

    private static List<String> allowedAlgs(JsonNode body, List<FieldError> errors) {
        JsonNode node = body.get("allowedAlgs");
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isArray() || node.isEmpty()) {
            errors.add(new FieldError("allowedAlgs", "must be a non-empty array of RS256, ES256, EdDSA, Ed25519"));
            return null;
        }
        Set<String> out = new LinkedHashSet<>();
        int i = 0;
        for (JsonNode item : node) {
            String path = "allowedAlgs[" + i++ + "]";
            if (!item.isString() || !McpAuthSettings.SUPPORTED_ALGS.contains(item.asString().strip())) {
                errors.add(new FieldError(path, "must be RS256, ES256, EdDSA or Ed25519"));
            } else {
                out.add(item.asString().strip());
            }
        }
        return new ArrayList<>(out);
    }
}
