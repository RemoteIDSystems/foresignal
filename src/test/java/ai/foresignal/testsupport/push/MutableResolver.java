package ai.foresignal.testsupport.push;

import ai.foresignal.net.HostResolver;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A DNS stand-in for push and outbound URL policy tests (prompts 031 and 035): each host answers the
 * addresses a test set, so a test can simulate public, private and mixed answers and a rebinding
 * between save and send. {@code localhost} answers 127.0.0.1 unless a test set it (WireMock stand-ins
 * in the shared context use it); other unknown hosts do not resolve; nothing ever reaches real DNS.
 */
public final class MutableResolver implements HostResolver {

    private final Map<String, List<InetAddress>> answers = new ConcurrentHashMap<>();

    /** Makes {@code host} resolve to {@code addresses} (IP literals). */
    public void set(String host, String... addresses) {
        List<InetAddress> out = new ArrayList<>();
        for (String address : addresses) {
            try {
                out.add(InetAddress.getByName(address));
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException(address, e);
            }
        }
        answers.put(host.toLowerCase(Locale.ROOT), out);
    }

    /** Forgets every answer. */
    public void clear() {
        answers.clear();
    }

    @Override
    public InetAddress[] resolve(String host) throws UnknownHostException {
        List<InetAddress> out = answers.get(host.toLowerCase(Locale.ROOT));
        if (out == null && "localhost".equalsIgnoreCase(host)) {
            return new InetAddress[] {InetAddress.getByAddress("localhost", new byte[] {127, 0, 0, 1})};
        }
        if (out == null || out.isEmpty()) {
            throw new UnknownHostException(host);
        }
        return out.toArray(InetAddress[]::new);
    }
}
