package ai.foresignal.test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.spi.InetAddressResolver;
import java.net.spi.InetAddressResolverProvider;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

/**
 * The test JVM's host-name resolver (checkpoint 041): no test may reach the network outside this
 * machine (no real upstream, notification service or LLM provider).
 *
 * <p>Registered through {@code META-INF/services/java.net.spi.InetAddressResolverProvider}, so it
 * replaces the JDK's resolver for every {@link InetAddress} lookup in the test JVM. Loopback names
 * and this machine's own name resolve as usual; every other name is refused with an
 * {@link UnknownHostException} and recorded, and {@link NoExternalNetworkExtension} fails the test
 * during which it was asked. Address literals never reach a resolver, so tests that judge literal
 * public addresses (the outbound URL policy) are unaffected; tests that need a name use the
 * application's own {@code HostResolver} stand-ins.
 */
public final class LocalOnlyResolverProvider extends InetAddressResolverProvider {

    /** Every name refused since the JVM started, in order. */
    static final List<String> REFUSED = new CopyOnWriteArrayList<>();

    @Override
    public InetAddressResolver get(Configuration configuration) {
        InetAddressResolver builtin = configuration.builtinResolver();
        String self = configuration.lookupLocalHostName().toLowerCase(Locale.ROOT);
        return new InetAddressResolver() {
            @Override
            public Stream<InetAddress> lookupByName(String host, LookupPolicy policy) throws UnknownHostException {
                String name = host.toLowerCase(Locale.ROOT);
                if (isLocal(name) || name.equals(self)) {
                    return builtin.lookupByName(host, policy);
                }
                if (!isReserved(name)) {
                    REFUSED.add(host);
                }
                throw new UnknownHostException(host + ": external name lookups are refused in the test JVM");
            }

            @Override
            public String lookupByAddress(byte[] addr) throws UnknownHostException {
                InetAddress address = InetAddress.getByAddress(addr);
                if (address.isLoopbackAddress() || address.isSiteLocalAddress() || address.isLinkLocalAddress()) {
                    return builtin.lookupByAddress(addr);
                }
                throw new UnknownHostException("reverse lookups of public addresses are refused in the test JVM");
            }
        };
    }

    @Override
    public String name() {
        return "foresignal-test-local-only";
    }

    /**
     * True for the names RFC 2606 and RFC 6761 reserve as never resolvable ({@code .invalid}, {@code .test},
     * {@code .example}, {@code example.com|net|org}). Refusing them gives a test the answer it would get anyway
     * (no address) without a query leaving the machine, so they are refused but not reported.
     */
    static boolean isReserved(String name) {
        String n = name.endsWith(".") ? name.substring(0, name.length() - 1) : name;
        for (String tld : new String[] {"invalid", "test", "example"}) {
            if (n.equals(tld) || n.endsWith("." + tld)) {
                return true;
            }
        }
        for (String domain : new String[] {"example.com", "example.net", "example.org"}) {
            if (n.equals(domain) || n.endsWith("." + domain)) {
                return true;
            }
        }
        return false;
    }

    /** True for the loopback names RFC 6761 reserves. */
    static boolean isLocal(String name) {
        return name.equals("localhost") || name.endsWith(".localhost") || name.equals("localhost.localdomain");
    }
}
