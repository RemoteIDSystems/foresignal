package ai.foresignal.testsupport.push;

import ai.foresignal.notify.push.PushHostResolver;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A DNS stand-in for push tests (prompt 031): each host answers the addresses a test set, so a test
 * can simulate public, private and mixed answers and a rebinding between subscription and send.
 * Unknown hosts do not resolve; nothing ever reaches real DNS.
 */
public final class MutableResolver implements PushHostResolver {

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
        if (out == null || out.isEmpty()) {
            throw new UnknownHostException(host);
        }
        return out.toArray(InetAddress[]::new);
    }
}
