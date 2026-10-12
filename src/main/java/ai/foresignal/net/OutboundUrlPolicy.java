package ai.foresignal.net;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.net.SpecialPurposeAddresses.AddressClass;
import ai.foresignal.net.SpecialPurposeAddresses.Classification;
import ai.foresignal.net.SpecialPurposeAddresses.Embedding;
import ai.foresignal.settings.AppSettingChangedEvent;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.OutboundSettings;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * The one SSRF guard for every operator-supplied outbound URL (prompt 035): channel destinations,
 * LLM provider base URLs, push endpoints, the FCM token endpoint, {@code LAN_URL} source parameters,
 * EDP provider URLs and the watchdog URLs.
 *
 * <p>{@link #check(URI, Profile)} answers {@link Allowed} with the addresses to connect to, or
 * {@link Denied} with a reason (code {@code URL_NOT_ALLOWED}: 400 when a URL is saved, a delivery
 * failure when it is used). The steps, in order:
 *
 * <ol>
 *   <li>Shape: an absolute hierarchical URL; {@code https} (or {@code wss}), and {@code http} (or
 *       {@code ws}) only in the LAN profiles or when the caller's field allows it; no user info.
 *   <li>Normalize the host: an IPv6 literal (no zone id) or a strict dotted-quad IPv4 literal. Any
 *       other numeric form ({@code 2130706433}, {@code 0x7f.1}, {@code 017700000001},
 *       {@code 127.1}, leading zeros, a trailing dot) is refused rather than guessed at, because
 *       resolvers disagree on what those mean.
 *   <li>Resolve a host name once through the {@link HostResolver}; <em>every</em> address must pass
 *       (one public and one private answer is refused in {@code PUBLIC_ONLY}).
 *   <li>Judge each address with {@link SpecialPurposeAddresses}: embedded IPv4 (IPv4-mapped,
 *       IPv4-compatible, NAT64, 6to4, Teredo) is extracted and judged; always-blocked classes are
 *       refused; {@code outbound.denyHosts}/{@code denyCidrs} refuse; {@code allowHosts}/
 *       {@code allowCidrs} (and, for {@code PUSH_ENDPOINT}, {@code push.allowedPrivateHosts})
 *       allow; otherwise the profile decides.
 * </ol>
 *
 * <p><strong>Overrides never unblock the always-blocked set</strong> (link-local, cloud metadata,
 * unspecified, multicast, broadcast, documentation, reserved and the other not-forwardable
 * special-purpose blocks): an allow entry lifts only the profile's restriction. Deny wins over
 * allow.
 *
 * <p>The returned {@link Allowed#pinnedAddresses()} are what the caller must connect to, with the
 * original {@code Host} and TLS name and redirects off ({@link PinnedHttp}), so a second DNS answer
 * cannot redirect the request (DNS rebinding). When a JVM or environment HTTP proxy is configured,
 * {@code LAN_ONLY} is refused, and a warning is logged at startup that the proxy, not Foresignal,
 * resolves and connects for proxied requests.
 *
 * <p>The overrides are cached and dropped on {@link AppSettingChangedEvent} for {@code outbound} and
 * {@code server} (which holds {@code push.allowedPrivateHosts}).
 */
@Component
public class OutboundUrlPolicy {

    private static final Logger log = LoggerFactory.getLogger(OutboundUrlPolicy.class);

    /** Redirect hops a caller that follows redirects may take, each re-checked. */
    public static final int MAX_REDIRECTS = 3;

    /** A strict dotted quad: four decimal octets, no leading zeros. */
    private static final Pattern DOTTED_QUAD = Pattern.compile(
            "^(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}$");

    /** A label an IPv4 parser would read as a number: decimal, octal (leading 0) or hexadecimal. */
    private static final Pattern NUMERIC_LABEL = Pattern.compile("^(0x[0-9a-f]*|\\d+)$");

    /** The characters of an IPv6 literal (with an optional dotted-quad tail). */
    private static final Pattern IPV6_LITERAL = Pattern.compile("^[0-9a-f:.]+$");

    /** A DNS host name (letters, digits, hyphens, underscores; an optional trailing dot). */
    private static final Pattern HOST_NAME = Pattern.compile(
            "^[a-z0-9_]([a-z0-9_-]{0,61}[a-z0-9_])?(\\.[a-z0-9_]([a-z0-9_-]{0,61}[a-z0-9_])?)*\\.?$");

    /** The outbound profiles. */
    public enum Profile {
        /** Globally routable addresses only (public webhooks, frontier LLM providers); https only. */
        PUBLIC_ONLY(false, EnumSet.of(AddressClass.GLOBAL)),
        /**
         * Public plus loopback, RFC 1918, unique-local and shared 100.64/10 (channels, LLM
         * {@code baseUrl} such as {@code http://ollama:11434}, the FCM token endpoint, watchdog).
         */
        LAN_ALLOWED(true, EnumSet.of(AddressClass.GLOBAL, AddressClass.LOOPBACK, AddressClass.PRIVATE,
                AddressClass.SHARED)),
        /**
         * The {@code LAN_URL} rule of prompt 004: loopback, RFC 1918 and unique-local only (not
         * shared 100.64/10, not public).
         */
        LAN_ONLY(true, EnumSet.of(AddressClass.LOOPBACK, AddressClass.PRIVATE)),
        /** {@code PUBLIC_ONLY} plus {@code push.allowedPrivateHosts} (Web Push endpoints). */
        PUSH_ENDPOINT(false, EnumSet.of(AddressClass.GLOBAL));

        private final boolean lan;
        private final Set<AddressClass> permitted;

        Profile(boolean lan, Set<AddressClass> permitted) {
            this.lan = lan;
            this.permitted = permitted;
        }

        /** True for the LAN profiles, which also accept plain {@code http}. */
        public boolean lan() {
            return lan;
        }

        /** True when this profile permits {@code addressClass} without an override. */
        public boolean permits(AddressClass addressClass) {
            return permitted.contains(addressClass);
        }
    }

    /** The outcome of a check. */
    public sealed interface Result permits Allowed, Denied {

        /** True for {@link Allowed}. */
        boolean allowed();
    }

    /**
     * The URL may be contacted, at one of these addresses.
     *
     * @param pinnedAddresses every resolved address, all checked, in resolver order
     */
    public record Allowed(List<InetAddress> pinnedAddresses) implements Result {

        /** Copies the addresses; at least one is required. */
        public Allowed {
            pinnedAddresses = List.copyOf(pinnedAddresses);
            if (pinnedAddresses.isEmpty()) {
                throw new IllegalArgumentException("an allowed URL has at least one address");
            }
        }

        /** The address to connect to: the first checked one. */
        public InetAddress pinned() {
            return pinnedAddresses.getFirst();
        }

        @Override
        public boolean allowed() {
            return true;
        }
    }

    /**
     * The URL may not be contacted.
     *
     * @param reason why, naming the host and the offending address but never the path or query
     * @param unresolved true when the only problem is that the host did not resolve (save-time
     *     validation accepts that; a send fails)
     */
    public record Denied(String reason, boolean unresolved) implements Result {

        /** A refusal for a policy reason. */
        public Denied(String reason) {
            this(reason, false);
        }

        /** {@code URL_NOT_ALLOWED}. */
        public String code() {
            return ErrorCodes.URL_NOT_ALLOWED;
        }

        @Override
        public boolean allowed() {
            return false;
        }
    }

    /** The compiled overrides. */
    private record Overrides(List<String> allowHosts, List<Cidr> allowCidrs, List<String> denyHosts,
            List<Cidr> denyCidrs, List<String> pushHosts, List<Cidr> pushCidrs) {

        static final Overrides NONE = new Overrides(List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }

    private final HostResolver resolver;
    private final Supplier<OutboundSettings> outbound;
    private final Supplier<List<String>> pushAllowedHosts;
    private final Optional<String> proxy;
    private volatile Overrides overrides;

    /**
     * The production policy: overrides from {@code app_setting['outbound']}, push hosts from
     * {@code app_setting['server'].push.allowedPrivateHosts}, the process proxy settings.
     */
    @Autowired
    public OutboundUrlPolicy(HostResolver resolver, AppSettingService settings) {
        this(resolver,
                () -> settings.getOrDefault(SettingKeys.OUTBOUND, OutboundSettings.class, OutboundSettings::defaults),
                () -> settings.getOrDefault(SettingKeys.SERVER, ServerSettings.class, ServerSettings::defaults)
                        .effectivePush().allowedPrivateHosts(),
                OutboundProxies.configured());
        proxy.ifPresent(p -> log.warn("An HTTP proxy is configured ({}). LAN_ONLY URLs are refused, and for requests"
                + " that go through the proxy the proxy resolves and connects, so Foresignal's outbound address"
                + " checks are bypassed for proxied profiles.", p));
    }

    /**
     * A policy with explicit inputs, for tests and callers outside Spring.
     *
     * @param resolver the DNS stand-in
     * @param outbound the overrides (read on first use and after {@link #invalidate()})
     * @param pushAllowedHosts {@code push.allowedPrivateHosts}
     * @param proxy the configured proxy, or empty
     */
    public OutboundUrlPolicy(HostResolver resolver, Supplier<OutboundSettings> outbound,
            Supplier<List<String>> pushAllowedHosts, Optional<String> proxy) {
        this.resolver = resolver;
        this.outbound = outbound;
        this.pushAllowedHosts = pushAllowedHosts;
        this.proxy = proxy == null ? Optional.empty() : proxy;
    }

    /** A policy without overrides or proxy, resolving through {@code resolver} (tests). */
    public static OutboundUrlPolicy withoutOverrides(HostResolver resolver) {
        return new OutboundUrlPolicy(resolver, OutboundSettings::defaults, List::of, Optional.empty());
    }

    /** Drops the cached overrides when {@code outbound} or {@code server} changes. */
    @EventListener
    public void onSettingChanged(AppSettingChangedEvent event) {
        if (SettingKeys.OUTBOUND.equals(event.key()) || SettingKeys.SERVER.equals(event.key())) {
            invalidate();
        }
    }

    /** Drops the cached overrides; the next check reloads them. */
    public void invalidate() {
        overrides = null;
    }

    /** {@link #check(URI, Profile, boolean)} without the per-field http allowance. */
    public Result check(URI uri, Profile profile) {
        return check(uri, profile, false);
    }

    /** {@link #check(URI, Profile, boolean)} of a URL string; an unparsable one is denied. */
    public Result check(String url, Profile profile, boolean allowHttp) {
        if (url == null || url.isBlank()) {
            return new Denied("the URL is empty");
        }
        try {
            return check(new URI(url.strip()), profile, allowHttp);
        } catch (URISyntaxException e) {
            return new Denied("the URL is not valid");
        }
    }

    /**
     * Checks {@code uri} under {@code profile}.
     *
     * @param allowHttp true when the caller's field allows plain {@code http} outside the LAN
     *     profiles
     */
    public Result check(URI uri, Profile profile, boolean allowHttp) {
        if (uri == null || !uri.isAbsolute() || uri.isOpaque()) {
            return new Denied("the URL must be absolute, for example https://host/path");
        }
        String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
        boolean secure = scheme.equals("https") || scheme.equals("wss");
        boolean plain = scheme.equals("http") || scheme.equals("ws");
        if (!secure && !plain) {
            return new Denied("the scheme " + scheme + " is not allowed; use https"
                    + (profile.lan() ? " or http" : ""));
        }
        if (plain && !profile.lan() && !allowHttp) {
            return new Denied("the URL must use https (" + profile + " allows plain http only for LAN destinations)");
        }
        if (uri.getRawUserInfo() != null) {
            return new Denied("the URL must not carry user info");
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            return new Denied("the URL has no valid host" + (uri.getRawAuthority() == null ? ""
                    : " (" + safeAuthority(uri.getRawAuthority()) + ")"));
        }
        return checkHost(host, profile);
    }

    /**
     * Checks a bare host (SMTP, which has no URL): the same normalization, resolution, overrides and
     * profile as {@link #check(URI, Profile)}, without the scheme rules.
     */
    public Result checkHost(String rawHost, Profile profile) {
        if (rawHost == null || rawHost.isBlank()) {
            return new Denied("the host is empty");
        }
        String host = rawHost.strip().toLowerCase(Locale.ROOT);
        boolean bracketed = host.startsWith("[") && host.endsWith("]");
        if (bracketed) {
            host = host.substring(1, host.length() - 1);
        }
        if (profile == Profile.LAN_ONLY && proxy.isPresent()) {
            return new Denied("an HTTP proxy is configured (" + proxy.get() + "), so LAN_ONLY URLs are refused: the"
                    + " proxy, not Foresignal, would resolve and connect");
        }
        List<InetAddress> addresses;
        boolean literal;
        if (host.indexOf(':') >= 0) {
            if (host.indexOf('%') >= 0) {
                return new Denied("the host " + host + " carries an IPv6 zone identifier, which is not allowed");
            }
            Optional<InetAddress> parsed = ipv6Literal(host);
            if (parsed.isEmpty()) {
                return new Denied("the host " + host + " is not a valid IPv6 literal");
            }
            addresses = List.of(parsed.get());
            literal = true;
        } else if (bracketed) {
            return new Denied("the host [" + host + "] is not a valid IPv6 literal");
        } else if (looksNumeric(host)) {
            Optional<InetAddress> parsed = strictIpv4(host);
            if (parsed.isEmpty()) {
                return new Denied("the host " + host + " is not a dotted-quad IPv4 address; decimal, octal,"
                        + " hexadecimal and shortened IPv4 forms are refused");
            }
            addresses = List.of(parsed.get());
            literal = true;
        } else {
            if (host.length() > 253 || !HOST_NAME.matcher(host).matches()) {
                return new Denied("the host " + host + " is not a valid host name");
            }
            addresses = null;
            literal = false;
        }
        String name = host.endsWith(".") ? host.substring(0, host.length() - 1) : host;
        Overrides o = overrides();
        if (matchesHost(o.denyHosts(), name)) {
            return new Denied("the host " + name + " is refused by outbound.denyHosts");
        }
        if (!literal) {
            try {
                InetAddress[] resolved = resolver.resolve(name);
                if (resolved == null || resolved.length == 0) {
                    return new Denied("the host " + name + " does not resolve", true);
                }
                addresses = List.copyOf(Arrays.asList(resolved));
            } catch (UnknownHostException e) {
                return new Denied("the host " + name + " does not resolve", true);
            }
        }
        boolean hostAllowed = matchesHost(o.allowHosts(), name)
                || (profile == Profile.PUSH_ENDPOINT && matchesHost(o.pushHosts(), name));
        for (InetAddress address : addresses) {
            String problem = judge(address, profile, hostAllowed, o);
            if (problem != null) {
                String subject = literal ? "the address " + address.getHostAddress() + " is"
                        : "the host " + name + " resolves to " + address.getHostAddress() + ",";
                return new Denied(subject + " " + problem);
            }
        }
        return new Allowed(addresses);
    }

    /**
     * Checks one redirect hop for a caller that follows redirects: {@code location} resolved against
     * {@code current}, refused after {@link #MAX_REDIRECTS} hops.
     *
     * @param hop the number of redirects already followed
     */
    public Result checkRedirect(URI current, String location, Profile profile, boolean allowHttp, int hop) {
        if (hop >= MAX_REDIRECTS) {
            return new Denied("more than " + MAX_REDIRECTS + " redirects");
        }
        if (location == null || location.isBlank()) {
            return new Denied("a redirect without a Location");
        }
        try {
            return check(current.resolve(location.strip()), profile, allowHttp);
        } catch (IllegalArgumentException e) {
            return new Denied("the redirect Location is not a valid URL");
        }
    }

    /**
     * Why {@code url} may not be saved, or null when it may. A host that does not resolve <em>now</em>
     * is accepted (a DNS outage must not block configuration); it is checked again on every send.
     */
    public String saveProblem(String url, Profile profile, boolean allowHttp) {
        Result result = check(url, profile, allowHttp);
        return result instanceof Denied d && !d.unresolved() ? d.reason() : null;
    }

    /** {@link #saveProblem} for a bare host (SMTP). */
    public String saveHostProblem(String host, Profile profile) {
        Result result = checkHost(host, profile);
        return result instanceof Denied d && !d.unresolved() ? d.reason() : null;
    }

    /**
     * Throws 400 {@code URL_NOT_ALLOWED} with {@code field} when {@code url} may not be saved.
     *
     * @throws ValidationException code {@code URL_NOT_ALLOWED}
     */
    public void requireSavable(String field, String url, Profile profile, boolean allowHttp) {
        String problem = saveProblem(url, profile, allowHttp);
        if (problem != null) {
            throw notAllowed(List.of(new FieldError(field, problem)));
        }
    }

    /**
     * A 400 {@code URL_NOT_ALLOWED} carrying {@code errors} (every field-level problem of the request,
     * at least one of which is an outbound URL refusal).
     */
    public static ValidationException notAllowed(List<FieldError> errors) {
        return new ValidationException(ErrorCodes.URL_NOT_ALLOWED,
                "The request names an outbound URL that the outbound URL policy refuses.", errors);
    }

    // ---------- judging ----------

    /** Why {@code address} is refused under {@code profile}, or null when it is allowed. */
    private String judge(InetAddress address, Profile profile, boolean hostAllowed, Overrides o) {
        for (Cidr block : o.denyCidrs()) {
            if (block.contains(address)) {
                return "inside " + block + " from outbound.denyCidrs";
            }
        }
        Classification c = SpecialPurposeAddresses.classify(address);
        if (c.addressClass() == AddressClass.EMBEDDED) {
            Embedding embedding = c.entry().embedding();
            Inet4Address v4 = SpecialPurposeAddresses.embeddedIpv4(address, embedding).orElse(null);
            if (v4 == null) {
                return "an IPv6 address embedding IPv4 that could not be decoded";
            }
            if (embedding == Embedding.COMPATIBLE) {
                return "an IPv4-compatible IPv6 address (deprecated, RFC 4291) embedding " + v4.getHostAddress()
                        + ", which is refused in every profile";
            }
            if (embedding == Embedding.TEREDO) {
                Inet4Address server = SpecialPurposeAddresses.teredoServer(address).orElse(null);
                String serverProblem = server == null ? null : judge(server, profile, hostAllowed, o);
                if (serverProblem != null) {
                    return "a Teredo address whose server " + server.getHostAddress() + " is refused: " + serverProblem;
                }
            }
            String inner = judge(v4, profile, hostAllowed, o);
            return inner == null ? null : "an IPv6 address embedding " + v4.getHostAddress() + " (" + c.entry().name()
                    + "), refused because the embedded address is " + inner;
        }
        if (c.addressClass() == AddressClass.BLOCKED) {
            return c.describe() + ", which is refused in every profile";
        }
        if (hostAllowed) {
            return null;
        }
        for (Cidr block : o.allowCidrs()) {
            if (block.contains(address)) {
                return null;
            }
        }
        if (profile == Profile.PUSH_ENDPOINT) {
            for (Cidr block : o.pushCidrs()) {
                if (block.contains(address)) {
                    return null;
                }
            }
        }
        if (profile.permits(c.addressClass())) {
            return null;
        }
        return c.describe() + ", which " + profile + " does not allow" + hint(profile);
    }

    private static String hint(Profile profile) {
        return switch (profile) {
            case PUSH_ENDPOINT -> "; add it to push.allowedPrivateHosts to use it";
            case PUBLIC_ONLY -> "; add it to outbound.allowHosts or outbound.allowCidrs to use it";
            case LAN_ALLOWED -> "; add it to outbound.allowCidrs to use it";
            case LAN_ONLY -> " (only loopback, private-use and unique-local addresses are allowed)";
        };
    }

    // ---------- host normalization ----------

    /** True when an IPv4 parser would read {@code host} as a number (its last label is numeric). */
    static boolean looksNumeric(String host) {
        String trimmed = host.endsWith(".") ? host.substring(0, host.length() - 1) : host;
        if (trimmed.isEmpty()) {
            return false;
        }
        String[] labels = trimmed.split("\\.", -1);
        return NUMERIC_LABEL.matcher(labels[labels.length - 1]).matches();
    }

    /** The address of a strict dotted quad, without DNS; empty for every other form. */
    static Optional<InetAddress> strictIpv4(String host) {
        if (!DOTTED_QUAD.matcher(host).matches()) {
            return Optional.empty();
        }
        String[] parts = host.split("\\.");
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            bytes[i] = (byte) Integer.parseInt(parts[i]);
        }
        try {
            return Optional.of(InetAddress.getByAddress(bytes));
        } catch (UnknownHostException e) {
            return Optional.empty();
        }
    }

    /** The address of an IPv6 literal, without DNS; empty when {@code host} is not one. */
    static Optional<InetAddress> ipv6Literal(String host) {
        if (!IPV6_LITERAL.matcher(host).matches()) {
            return Optional.empty();
        }
        int dot = host.indexOf('.');
        if (dot >= 0) {
            // An embedded dotted-quad tail must be strict too.
            String tail = host.substring(host.lastIndexOf(':') + 1);
            if (!DOTTED_QUAD.matcher(tail).matches()) {
                return Optional.empty();
            }
        }
        try {
            // A literal containing a colon never triggers a lookup.
            return Optional.of(InetAddress.getByName(host));
        } catch (UnknownHostException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /** True when {@code host} matches an entry: exact, or a subdomain of a {@code *.} entry. */
    static boolean matchesHost(List<String> entries, String host) {
        for (String raw : entries) {
            String entry = normalizeHostEntry(raw);
            if (entry.isEmpty()) {
                continue;
            }
            if (entry.startsWith("*.")) {
                String suffix = entry.substring(1);
                if (host.endsWith(suffix) && host.length() > suffix.length()) {
                    return true;
                }
            } else if (entry.equals(host)) {
                return true;
            }
        }
        return false;
    }

    private static String normalizeHostEntry(String raw) {
        if (raw == null) {
            return "";
        }
        String entry = raw.strip().toLowerCase(Locale.ROOT);
        if (entry.startsWith("[") && entry.endsWith("]")) {
            entry = entry.substring(1, entry.length() - 1);
        }
        return entry.endsWith(".") ? entry.substring(0, entry.length() - 1) : entry;
    }

    private static String safeAuthority(String authority) {
        int at = authority.lastIndexOf('@');
        return at >= 0 ? authority.substring(at + 1) : authority;
    }

    // ---------- overrides ----------

    private Overrides overrides() {
        Overrides current = overrides;
        if (current != null) {
            return current;
        }
        try {
            OutboundSettings settings = outbound.get();
            List<String> push = pushAllowedHosts.get();
            List<String> pushHosts = new ArrayList<>();
            List<Cidr> pushCidrs = new ArrayList<>();
            for (String entry : push == null ? List.<String>of() : push) {
                Optional<Cidr> block = entry != null && (entry.contains("/") || Cidr.literal(entry.strip()).isPresent())
                        ? Cidr.parse(entry) : Optional.empty();
                if (block.isPresent()) {
                    pushCidrs.add(block.get());
                } else if (entry != null) {
                    pushHosts.add(entry);
                }
            }
            Overrides loaded = new Overrides(settings.allowHosts(), cidrs(settings.allowCidrs()), settings.denyHosts(),
                    cidrs(settings.denyCidrs()), List.copyOf(pushHosts), List.copyOf(pushCidrs));
            overrides = loaded;
            return loaded;
        } catch (RuntimeException e) {
            // Not cached: the next check tries again. Without the overrides, the profiles alone decide.
            log.warn("Could not read the outbound URL policy overrides; checking without them", e);
            return Overrides.NONE;
        }
    }

    private static List<Cidr> cidrs(List<String> entries) {
        List<Cidr> out = new ArrayList<>();
        for (String entry : entries) {
            Cidr.parse(entry).ifPresent(out::add);
        }
        return List.copyOf(out);
    }
}
