package ai.foresignal.notify.push;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Resolves push endpoint host names (prompt 031). The production bean is the system resolver
 * ({@link PushConfig}); tests replace it to simulate DNS answers, including a rebinding between
 * subscription and send.
 */
@FunctionalInterface
public interface PushHostResolver {

    /** Every address of {@code host}. */
    InetAddress[] resolve(String host) throws UnknownHostException;
}
