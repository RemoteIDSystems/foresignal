package ai.foresignal.egress;

import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.Encrypted;
import ai.foresignal.settings.AppSettingChangedEvent;
import ai.foresignal.settings.SettingKeys;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ProxySelector;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Where the collector asks which egress a request leaves through (prompt R2-018), and reports how it went.
 *
 * <p>{@code BoundSourceHttp} calls {@link #decide} before each request of a pull. With rotation {@code direct} (the
 * default) the answer is always {@link Decision#DIRECT} and nothing else here runs, so the collector behaves exactly as
 * before. Otherwise the {@link RouteSelector} picks among the usable routes (enabled, {@linkplain #availability
 * available} on this host, not held off by {@link EgressHealthRegistry}) the ones this source is not throttled on
 * ({@link EgressBackoff}), honoring its pin or exclusion.
 *
 * <p>Each route gets its own pair of {@link HttpClient}s, built from {@link UpstreamClient#clientBuilder} with only the
 * egress added:
 * <ul>
 *   <li>{@code http} without credentials: the JDK's own HTTP proxy support ({@code ProxySelector});
 *   <li>{@code http} with credentials, and {@code socks5}: the route's {@link EgressTunnel};
 *   <li>{@code interface}: {@code HttpClient.Builder.localAddress}, the bound source address;
 *   <li>{@code wireguard}: none; the route is recorded but unavailable (the server cannot bring a tunnel up).
 * </ul>
 * The clients are rebuilt when the route changes and closed, with their tunnel, when it is deleted.
 */
@Component
public class EgressRouter {

    private static final Logger log = LoggerFactory.getLogger(EgressRouter.class);

    /** A bound interface: an interface name or an address literal. */
    static final Pattern INTERFACE = Pattern.compile("[A-Za-z0-9._:%-]{1,200}");

    /** An IPv4 literal; anything with a colon is taken as IPv6. */
    static final Pattern IPV4 = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3}");

    /**
     * A route in use by a pull.
     *
     * @param route the route
     * @param following its client that follows redirects
     * @param notFollowing its client that does not
     */
    public record Routing(EgressRoute route, HttpClient following, HttpClient notFollowing) {

        /** The client for {@code allowRedirects}. */
        public HttpClient client(boolean allowRedirects) {
            return allowRedirects ? following : notFollowing;
        }
    }

    /**
     * The answer for one request.
     *
     * @param routing the route to leave through, or null for direct (or for a refusal)
     * @param waitUntil when every route this source may use is throttled: the earliest end, else null
     * @param refusal why the request must not be sent at all, or null
     */
    public record Decision(Routing routing, Instant waitUntil, String refusal) {

        /** Leave from the server's own address. */
        public static final Decision DIRECT = new Decision(null, null, null);
    }

    /**
     * Whether a route can be used on this host.
     *
     * @param available true when it can
     * @param reason why not, or null
     */
    public record Availability(boolean available, String reason) {

        static final Availability YES = new Availability(true, null);
    }

    private record Clients(Instant version, Routing routing, EgressTunnel tunnel, Availability availability) {}

    private final EgressRouteStore store;
    private final EgressSettingsService settings;
    private final EgressBackoff backoff;
    private final EgressHealthRegistry health;
    private final CredentialCipher cipher;
    private final RouteSelector selector = new RouteSelector();
    private final Map<UUID, Clients> clients = new ConcurrentHashMap<>();
    private volatile List<EgressRoute> routes;
    private volatile Map<String, RouteSelector.Assignment> assignments;

    /** Creates the router. */
    public EgressRouter(EgressRouteStore store, EgressSettingsService settings, EgressBackoff backoff,
            EgressHealthRegistry health, CredentialCipher cipher) {
        this.store = store;
        this.settings = settings;
        this.backoff = backoff;
        this.health = health;
        this.cipher = cipher;
    }

    /**
     * The egress of the next request of {@code sourceId}'s pull.
     *
     * @param current the route this pull already uses, or null
     */
    public Decision decide(String sourceId, UUID current) {
        EgressSettings policy = settings.get();
        if (policy.direct()) {
            return Decision.DIRECT;
        }
        List<EgressRoute> all = routes();
        List<UUID> pool = all.stream()
                .filter(r -> r.enabled() && clientsFor(r).availability().available() && health.usable(r.id()))
                .map(EgressRoute::id).toList();
        RouteSelector.Selection selection = selector.select(sourceId, policy, pool,
                assignments().getOrDefault(sourceId, RouteSelector.Assignment.NONE), current,
                id -> backoff.until(sourceId, id));
        return switch (selection) {
            case RouteSelector.Direct d -> Decision.DIRECT;
            case RouteSelector.Wait w -> new Decision(null, w.until(), null);
            case RouteSelector.Unavailable u -> new Decision(null, null, u.reason());
            case RouteSelector.Routed r -> all.stream().filter(route -> route.id().equals(r.routeId())).findFirst()
                    .map(route -> new Decision(clientsFor(route).routing(), null, null))
                    .orElseGet(() -> new Decision(null, null, "The egress route " + r.routeId() + " is gone"));
        };
    }

    /** The host pacer's key: the host alone when direct, the host and the route when routed. */
    public static String pacingKey(String host, Routing routing) {
        return routing == null ? host : host + "@egress:" + routing.route().id();
    }

    /** A request through {@code routing} got an answer from the upstream. */
    public void onSuccess(String sourceId, Routing routing, Duration latency) {
        backoff.onSuccess(sourceId, routing.route().id());
        health.success(routing.route(), latency, null);
    }

    /** The upstream answered a request through {@code routing} with a reached connection but an error status. */
    public void onReached(Routing routing, Duration latency) {
        health.success(routing.route(), latency, null);
    }

    /**
     * The upstream throttled {@code sourceId} through {@code routing}: it is held off that route only.
     *
     * @return when it may use the route again
     */
    public Instant onThrottled(String sourceId, Routing routing, Optional<Duration> retryAfter) {
        return backoff.onThrottled(sourceId, routing.route().id(), retryAfter);
    }

    /** A request through {@code routing} could not be carried (the proxy, tunnel or bound address failed). */
    public void onFailure(Routing routing, IOException error) {
        health.failure(routing.route(), error.getMessage() == null ? error.getClass().getSimpleName()
                : error.getMessage());
    }

    /** Whether {@code route} can be used on this host. */
    public Availability availability(EgressRoute route) {
        return clientsFor(route).availability();
    }

    /** The clients of {@code route}, or null when it is unavailable. */
    public Routing routing(EgressRoute route) {
        return clientsFor(route).routing();
    }

    /** Drops the cached routes and assignments, and closes the clients of routes that changed or are gone. */
    public void invalidate() {
        routes = null;
        assignments = null;
        List<EgressRoute> current = store.all();
        clients.forEach((id, c) -> {
            Optional<EgressRoute> now = current.stream().filter(r -> r.id().equals(id)).findFirst();
            if (now.isEmpty() || !now.get().updatedAt().equals(c.version())) {
                close(clients.remove(id));
            }
        });
    }

    /** Forgets the per-source rotation choices when the policy changes. */
    @EventListener
    public void onSettingChanged(AppSettingChangedEvent event) {
        if (SettingKeys.EGRESS.equals(event.key())) {
            selector.reset();
        }
    }

    /** Closes every client and tunnel. */
    @PreDestroy
    public void shutdown() {
        clients.values().forEach(EgressRouter::close);
        clients.clear();
    }

    private List<EgressRoute> routes() {
        List<EgressRoute> cached = routes;
        if (cached == null) {
            cached = List.copyOf(store.all());
            routes = cached;
        }
        return cached;
    }

    private Map<String, RouteSelector.Assignment> assignments() {
        Map<String, RouteSelector.Assignment> cached = assignments;
        if (cached == null) {
            Map<String, RouteSelector.Assignment> loaded = new LinkedHashMap<>();
            store.assignments().forEach(a -> loaded.put(a.sourceId(),
                    new RouteSelector.Assignment(a.routeId(), a.excluded())));
            cached = Collections.unmodifiableMap(loaded);
            assignments = cached;
        }
        return cached;
    }

    private Clients clientsFor(EgressRoute route) {
        Clients existing = clients.get(route.id());
        if (existing != null && existing.version().equals(route.updatedAt())) {
            return existing;
        }
        synchronized (clients) {
            existing = clients.get(route.id());
            if (existing != null && existing.version().equals(route.updatedAt())) {
                return existing;
            }
            Clients built = build(route);
            Clients replaced = clients.put(route.id(), built);
            if (replaced != null) {
                close(replaced);
            }
            return built;
        }
    }

    private Clients build(EgressRoute route) {
        try {
            return switch (route.type()) {
                case EgressRoute.WIREGUARD -> unavailable(route, "A WireGuard profile is host-level: the server cannot"
                        + " bring the tunnel up. Bring it up on the host, then add its interface as an interface route.");
                case EgressRoute.INTERFACE -> bound(route);
                case EgressRoute.HTTP, EgressRoute.SOCKS5 -> proxied(route);
                default -> unavailable(route, "Unknown egress type " + route.type());
            };
        } catch (IOException | RuntimeException e) {
            log.warn("Egress route {} ({}) cannot be used: {}", route.name(), route.id(), e.getMessage());
            return unavailable(route, e.getMessage());
        }
    }

    private Clients proxied(EgressRoute route) throws IOException {
        URI uri = URI.create(route.url());
        Map<String, String> secrets = secrets(route);
        String username = secrets.get(EgressRoute.USERNAME);
        EgressTunnel tunnel = null;
        InetSocketAddress proxy;
        if (EgressRoute.SOCKS5.equals(route.type()) || username != null) {
            tunnel = new EgressTunnel(new EgressTunnel.Upstream(route.type(), uri.getHost(), uri.getPort(), username,
                    secrets.get(EgressRoute.PASSWORD)));
            proxy = tunnel.address();
        } else {
            proxy = new InetSocketAddress(uri.getHost(), uri.getPort());
            if (proxy.isUnresolved()) {
                throw new UnknownHostException("The proxy host " + uri.getHost() + " does not resolve");
            }
        }
        ProxySelector selector = ProxySelector.of(proxy);
        Routing routing = new Routing(route,
                UpstreamClient.clientBuilder(HttpClient.Redirect.NORMAL).proxy(selector).build(),
                UpstreamClient.clientBuilder(HttpClient.Redirect.NEVER).proxy(selector).build());
        return new Clients(route.updatedAt(), routing, tunnel, Availability.YES);
    }

    private Clients bound(EgressRoute route) throws SocketException, UnknownHostException {
        InetAddress address = localAddress(route.boundInterface());
        Routing routing = new Routing(route,
                UpstreamClient.clientBuilder(HttpClient.Redirect.NORMAL).localAddress(address).build(),
                UpstreamClient.clientBuilder(HttpClient.Redirect.NEVER).localAddress(address).build());
        return new Clients(route.updatedAt(), routing, null, Availability.YES);
    }

    /**
     * The local address of a bound interface: an address literal that belongs to an interface of this host, or the
     * first address (IPv4 preferred) of the named interface, which must be up.
     *
     * @throws UnknownHostException when this host has no such address or interface
     */
    static InetAddress localAddress(String bound) throws SocketException, UnknownHostException {
        if (bound == null || !INTERFACE.matcher(bound).matches()) {
            throw new UnknownHostException("The bound interface must be an interface name or an address literal");
        }
        if (IPV4.matcher(bound).matches() || bound.contains(":")) {
            InetAddress address = InetAddress.getByName(bound);
            if (NetworkInterface.getByInetAddress(address) == null) {
                throw new UnknownHostException("No interface of this host has the address " + bound);
            }
            return address;
        }
        NetworkInterface nic = NetworkInterface.getByName(bound);
        if (nic == null || !nic.isUp()) {
            throw new UnknownHostException("This host has no interface " + bound + " that is up");
        }
        List<InetAddress> addresses = nic.inetAddresses().toList();
        return addresses.stream().filter(a -> a instanceof java.net.Inet4Address).findFirst()
                .or(() -> addresses.stream().findFirst())
                .orElseThrow(() -> new UnknownHostException("The interface " + bound + " has no address"));
    }

    private Map<String, String> secrets(EgressRoute route) {
        if (route.secretsCipher() == null) {
            return Map.of();
        }
        return cipher.decryptToMap(new Encrypted(route.secretsCipher(), route.secretsNonce()));
    }

    private static Clients unavailable(EgressRoute route, String reason) {
        return new Clients(route.updatedAt(), null, null, new Availability(false, reason));
    }

    private static void close(Clients c) {
        if (c == null) {
            return;
        }
        if (c.tunnel() != null) {
            c.tunnel().close();
        }
        if (c.routing() != null) {
            c.routing().following().shutdown();
            c.routing().notFollowing().shutdown();
        }
    }
}
