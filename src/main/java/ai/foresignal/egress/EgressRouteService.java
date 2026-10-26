package ai.foresignal.egress;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.Encrypted;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/**
 * The egress pool's CRUD (prompt R2-018): {@code createEgressRoute}, {@code updateEgressRoute},
 * {@code deleteEgressRoute} and the {@code EgressRoute} view.
 *
 * <p>Validation, every problem in one 400:
 * <ul>
 *   <li>{@code name} is required, at most 200 characters, and unique ignoring case;
 *   <li>{@code type} is {@code http}, {@code socks5}, {@code wireguard} or {@code interface};
 *   <li>a proxy has a {@code url} of its own scheme with a host and a port and nothing else (credentials go in
 *       {@code secrets}); its host must pass the outbound URL policy's {@code LAN_ALLOWED} profile
 *       ({@code 400 URL_NOT_ALLOWED});
 *   <li>an {@code interface} route has a {@code boundInterface} (a name or an address literal); no other type has one;
 *   <li>{@code secrets.username} and {@code secrets.password} belong to a proxy (at most 255 bytes each, the SOCKS5
 *       limit; a password needs a username); {@code secrets.wireguardConfig} to a WireGuard route, which needs one.
 * </ul>
 * The secrets are sealed together with {@link CredentialCipher} and never returned; the view lists their names in
 * {@code secretsSet}. On a replace an omitted secret keeps its value, an empty string or null clears it, and a secret
 * the new type cannot use is dropped.
 */
@Service
public class EgressRouteService {

    /** {@code getEgressRoute} and friends for an unknown id. */
    public static final String UNKNOWN_EGRESS = "UNKNOWN_EGRESS";

    /** The longest proxy username or password, in UTF-8 bytes (RFC 1929). */
    static final int MAX_CREDENTIAL = 255;

    /** The longest WireGuard profile. */
    static final int MAX_WIREGUARD = 16 * 1024;

    /** The members of {@code EgressRouteInput}, plus the read-only ones a client may echo back. */
    static final Set<String> MEMBERS = Set.of("name", "type", "enabled", "url", "boundInterface", "secrets", "id",
            "exitIp", "health", "secretsSet", "available", "unavailableReason", "createdAt", "updatedAt");

    private final EgressRouteStore store;
    private final EgressRouter router;
    private final EgressHealthRegistry health;
    private final EgressBackoff backoff;
    private final EgressSettingsService settings;
    private final CredentialCipher cipher;
    private final OutboundUrlPolicy outbound;
    private final Clock clock;

    /** Creates the service. */
    public EgressRouteService(EgressRouteStore store, EgressRouter router, EgressHealthRegistry health,
            EgressBackoff backoff, EgressSettingsService settings, CredentialCipher cipher, OutboundUrlPolicy outbound,
            Clock clock) {
        this.store = store;
        this.router = router;
        this.health = health;
        this.backoff = backoff;
        this.settings = settings;
        this.cipher = cipher;
        this.outbound = outbound;
        this.clock = clock;
    }

    /**
     * The route {@code id}.
     *
     * @throws NotFoundException {@code UNKNOWN_EGRESS}
     */
    public EgressRoute get(UUID id) {
        return store.find(id).orElseThrow(() -> unknown(id));
    }

    /** Creates a route from an {@code EgressRouteInput}. */
    @Transactional
    public EgressRoute create(JsonNode body) {
        Instant now = clock.instant();
        EgressRoute route = parse(body, null, UUID.randomUUID(), now);
        store.save(route);
        router.invalidate();
        return route;
    }

    /**
     * Replaces route {@code id} with an {@code EgressRouteInput}; its health is forgotten.
     *
     * @throws NotFoundException {@code UNKNOWN_EGRESS}
     */
    @Transactional
    public EgressRoute replace(UUID id, JsonNode body) {
        EgressRoute current = get(id);
        EgressRoute route = parse(body, current, id, current.createdAt());
        store.save(route);
        health.forget(current);
        router.invalidate();
        return route;
    }

    /**
     * Deletes route {@code id}: the sources pinned to it fall back to the default route or rotation, and a default
     * route naming it is cleared.
     *
     * @throws NotFoundException {@code UNKNOWN_EGRESS}
     */
    @Transactional
    public void delete(UUID id) {
        EgressRoute route = get(id);
        store.delete(id);
        settings.forget(id);
        backoff.forget(id);
        health.forget(route);
        router.invalidate();
    }

    /** The {@code EgressRoute} view: no secret, with its health and whether this host can use it. */
    public Map<String, Object> view(EgressRoute route) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", route.id());
        out.put("name", route.name());
        out.put("type", route.type());
        out.put("enabled", route.enabled());
        out.put("url", route.url());
        out.put("boundInterface", route.boundInterface());
        out.put("exitIp", route.exitIp());
        out.put("health", health.view(route.id()));
        out.put("secretsSet", secretNames(route));
        EgressRouter.Availability availability = router.availability(route);
        out.put("available", availability.available());
        out.put("unavailableReason", availability.reason());
        out.put("createdAt", route.createdAt());
        out.put("updatedAt", route.updatedAt());
        return out;
    }

    /** The {@code EgressHealth} view: the route's health and every source held off it. */
    public Map<String, Object> healthView(EgressRoute route) {
        Map<String, Object> out = health.view(route.id());
        if (out.get("exitIp") == null) {
            out.put("exitIp", route.exitIp());
        }
        List<Map<String, Object>> sources = new ArrayList<>();
        Instant now = clock.instant();
        for (EgressBackoff.Hold hold : backoff.forRoute(route.id())) {
            if (!hold.until().isAfter(now)) {
                continue;
            }
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("sourceId", hold.sourceId());
            entry.put("backoffUntil", hold.until());
            entry.put("consecutiveThrottles", hold.consecutive());
            sources.add(entry);
        }
        out.put("sources", sources);
        return out;
    }

    private List<String> secretNames(EgressRoute route) {
        if (route.secretsCipher() == null) {
            return List.of();
        }
        Map<String, String> secrets = cipher.decryptToMap(new Encrypted(route.secretsCipher(), route.secretsNonce()));
        return EgressRoute.SECRETS.stream().filter(secrets::containsKey).toList();
    }

    private EgressRoute parse(JsonNode body, EgressRoute current, UUID id, Instant createdAt) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be an EgressRouteInput object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String name : body.propertyNames()) {
            if (!MEMBERS.contains(name)) {
                errors.add(new FieldError(name, "is not a member of EgressRouteInput"));
            }
        }
        String name = text(body, "name", errors);
        if (name == null) {
            errors.add(new FieldError("name", "is required"));
        } else if (name.length() > 200) {
            errors.add(new FieldError("name", "must be at most 200 characters"));
        } else if (store.findByName(name).filter(other -> !other.id().equals(id)).isPresent()) {
            errors.add(new FieldError("name", "is already used by another egress route"));
        }
        String type = text(body, "type", errors);
        if (type == null) {
            errors.add(new FieldError("type", "is required"));
        } else if (!EgressRoute.TYPES.contains(type)) {
            errors.add(new FieldError("type", "must be one of " + String.join(", ", EgressRoute.TYPES)));
            type = null;
        }
        boolean enabled = true;
        if (body.has("enabled") && !body.get("enabled").isNull()) {
            if (!body.get("enabled").isBoolean()) {
                errors.add(new FieldError("enabled", "must be a boolean"));
            } else {
                enabled = body.get("enabled").asBoolean();
            }
        }
        boolean proxy = EgressRoute.HTTP.equals(type) || EgressRoute.SOCKS5.equals(type);
        String url = text(body, "url", errors);
        String proxyHost = null;
        if (proxy) {
            if (url == null) {
                errors.add(new FieldError("url", "is required for a " + type + " route: " + type + "://host:port"));
            } else {
                proxyHost = proxyHost(url, type, errors);
            }
        } else if (type != null && url != null) {
            errors.add(new FieldError("url", "belongs to an http or socks5 route only"));
        }
        String bound = text(body, "boundInterface", errors);
        if (EgressRoute.INTERFACE.equals(type)) {
            if (bound == null) {
                errors.add(new FieldError("boundInterface", "is required for an interface route: an interface name"
                        + " such as eth1, or a local address"));
            } else if (!EgressRouter.INTERFACE.matcher(bound).matches()) {
                errors.add(new FieldError("boundInterface", "must be an interface name or an address literal"));
            }
        } else if (type != null && bound != null) {
            errors.add(new FieldError("boundInterface", "belongs to an interface route only"));
        }
        Map<String, String> secrets = new LinkedHashMap<>();
        if (current != null && current.secretsCipher() != null) {
            secrets.putAll(cipher.decryptToMap(new Encrypted(current.secretsCipher(), current.secretsNonce())));
        }
        JsonNode given = body.get("secrets");
        if (given != null && !given.isNull()) {
            if (!given.isObject()) {
                errors.add(new FieldError("secrets", "must be an object"));
            } else {
                for (String key : given.propertyNames()) {
                    JsonNode value = given.get(key);
                    if (!EgressRoute.SECRETS.contains(key)) {
                        errors.add(new FieldError("secrets." + key, "is not an egress secret"));
                    } else if (value.isNull() || (value.isString() && value.asString().isEmpty())) {
                        secrets.remove(key);
                    } else if (!value.isString()) {
                        errors.add(new FieldError("secrets." + key, "must be a string"));
                    } else {
                        secrets.put(key, value.asString());
                    }
                }
            }
        }
        if (type != null) {
            if (!proxy) {
                if (given != null && given.isObject()) {
                    for (String key : List.of(EgressRoute.USERNAME, EgressRoute.PASSWORD)) {
                        if (given.hasNonNull(key) && !given.get(key).asString("").isEmpty()) {
                            errors.add(new FieldError("secrets." + key, "belongs to an http or socks5 route only"));
                        }
                    }
                }
                secrets.remove(EgressRoute.USERNAME);
                secrets.remove(EgressRoute.PASSWORD);
            }
            if (!EgressRoute.WIREGUARD.equals(type)) {
                if (given != null && given.isObject() && given.hasNonNull(EgressRoute.WIREGUARD_CONFIG)
                        && !given.get(EgressRoute.WIREGUARD_CONFIG).asString("").isEmpty()) {
                    errors.add(new FieldError("secrets.wireguardConfig", "belongs to a wireguard route only"));
                }
                secrets.remove(EgressRoute.WIREGUARD_CONFIG);
            } else {
                String config = secrets.get(EgressRoute.WIREGUARD_CONFIG);
                if (config == null) {
                    errors.add(new FieldError("secrets.wireguardConfig", "is required for a wireguard route"));
                } else if (config.length() > MAX_WIREGUARD || !config.contains("[Interface]")) {
                    errors.add(new FieldError("secrets.wireguardConfig", "must be a WireGuard profile with an"
                            + " [Interface] section, at most " + MAX_WIREGUARD + " characters"));
                }
            }
            for (String key : List.of(EgressRoute.USERNAME, EgressRoute.PASSWORD)) {
                String value = secrets.get(key);
                if (value != null && value.getBytes(StandardCharsets.UTF_8).length > MAX_CREDENTIAL) {
                    errors.add(new FieldError("secrets." + key, "must be at most " + MAX_CREDENTIAL + " bytes"));
                }
            }
            if (secrets.containsKey(EgressRoute.PASSWORD) && !secrets.containsKey(EgressRoute.USERNAME)) {
                errors.add(new FieldError("secrets.password", "needs a username"));
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The egress route is not valid.", errors);
        }
        if (proxyHost != null) {
            String problem = outbound.saveHostProblem(proxyHost, OutboundUrlPolicy.Profile.LAN_ALLOWED);
            if (problem != null) {
                throw OutboundUrlPolicy.notAllowed(List.of(new FieldError("url", problem)));
            }
        }
        byte[] sealed = null;
        byte[] nonce = null;
        if (!secrets.isEmpty()) {
            Encrypted encrypted = cipher.encrypt(secrets);
            sealed = encrypted.ciphertext();
            nonce = encrypted.nonce();
        }
        Instant now = clock.instant();
        boolean sameExit = current != null && current.type().equals(type) && java.util.Objects.equals(current.url(), url)
                && java.util.Objects.equals(current.boundInterface(), bound);
        return new EgressRoute(id, name, type, enabled, url, bound, sealed, nonce,
                sameExit ? current.exitIp() : null, sameExit ? current.exitIpCheckedAt() : null, createdAt,
                current != null && !now.isAfter(current.updatedAt()) ? current.updatedAt().plusNanos(1000) : now);
    }

    /** The host of a proxy URL, or null with an error. */
    static String proxyHost(String url, String type, List<FieldError> errors) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            errors.add(new FieldError("url", "must be a URL such as " + type + "://proxy.example:3128"));
            return null;
        }
        if (!type.equalsIgnoreCase(uri.getScheme())) {
            errors.add(new FieldError("url", "must use the " + type + ":// scheme"));
            return null;
        }
        if (uri.getRawUserInfo() != null) {
            errors.add(new FieldError("url", "must not carry credentials: put them in secrets.username and"
                    + " secrets.password"));
            return null;
        }
        if (uri.getHost() == null || uri.getPort() < 1 || uri.getPort() > 65535) {
            errors.add(new FieldError("url", "must name a host and a port: " + type + "://host:port"));
            return null;
        }
        if ((uri.getRawPath() != null && !uri.getRawPath().isEmpty() && !uri.getRawPath().equals("/"))
                || uri.getRawQuery() != null || uri.getRawFragment() != null) {
            errors.add(new FieldError("url", "must be only " + type + "://host:port"));
            return null;
        }
        return uri.getHost();
    }

    private static String text(JsonNode body, String field, List<FieldError> errors) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isString()) {
            errors.add(new FieldError(field, "must be a string"));
            return null;
        }
        return node.asString().isBlank() ? null : node.asString().strip();
    }

    private static NotFoundException unknown(UUID id) {
        return new NotFoundException(UNKNOWN_EGRESS, "No egress route has the id " + id + ".");
    }
}
