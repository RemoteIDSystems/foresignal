package ai.foresignal.net;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Resolves a host name to its addresses for {@link OutboundUrlPolicy} (prompt 035).
 *
 * <p>The production bean is the system resolver ({@link NetConfig}). Tests replace it with a stand-in
 * so they can simulate public, private and mixed answers and a rebinding between save and send, and so
 * that no test ever reaches real DNS. The policy never calls it for an IP literal.
 */
@FunctionalInterface
public interface HostResolver {

    /**
     * Every address of {@code host}.
     *
     * @param host a host name, lower case, without brackets
     * @return the addresses, in the resolver's order
     * @throws UnknownHostException when the name does not resolve
     */
    InetAddress[] resolve(String host) throws UnknownHostException;
}
