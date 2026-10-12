package ai.foresignal.testsupport.net;

import ai.foresignal.net.HostResolver;
import ai.foresignal.net.NetConfig;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.net.PinnedHttp;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Outbound URL policy pieces for unit tests and ITs that build clients by hand (prompt 035): a
 * resolver that never reaches real DNS ({@code localhost} answers 127.0.0.1, every other name does
 * not resolve), a policy over it without overrides, and a {@link PinnedHttp} with the production TLS.
 */
public final class TestOutbound {

    private TestOutbound() {}

    /** {@code localhost} is 127.0.0.1; every other name is unknown. Literals never reach a resolver. */
    public static HostResolver offlineResolver() {
        return host -> {
            if ("localhost".equalsIgnoreCase(host)) {
                return new InetAddress[] {InetAddress.getByAddress("localhost", new byte[] {127, 0, 0, 1})};
            }
            throw new UnknownHostException(host);
        };
    }

    /** A policy without overrides over {@link #offlineResolver()}. */
    public static OutboundUrlPolicy offlinePolicy() {
        return OutboundUrlPolicy.withoutOverrides(offlineResolver());
    }

    /** A pinned-connection factory with the production TLS checks. */
    public static PinnedHttp pinnedHttp() {
        return new PinnedHttp(new NetConfig().pinnedTls());
    }
}
