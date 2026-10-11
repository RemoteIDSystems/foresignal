package ai.elaro.collect;

import static org.assertj.core.api.Assertions.assertThat;

import ai.elaro.collect.BoundSourceHttp.PinnedTarget;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The {@code LAN_URL} pinning rewrite.
 *
 * <p>Tested directly because the property that matters cannot be observed from the outside: a
 * request to {@code http://receiver.local:8080} must <em>connect</em> to the address the operator's
 * URL resolved to during validation, while still <em>sending</em> the original Host. Re-resolving
 * the name on each request is the DNS rebinding hole that validation exists to close — a name that
 * resolved to a receiver on the LAN when it was saved can resolve to anything later.
 */
class PinnedTargetTest {

    @Test
    @DisplayName("the authority becomes the pinned address and the original Host is preserved")
    void rewritesToThePinnedAddress() throws UnknownHostException {
        PinnedTarget target =
                PinnedTarget.of(
                        URI.create("http://receiver.local:8080/data?since=5"),
                        Optional.of(InetAddress.getByName("192.168.1.50")));

        assertThat(target.uri().getHost()).isEqualTo("192.168.1.50");
        assertThat(target.uri().getPort()).isEqualTo(8080);
        assertThat(target.uri().getPath()).isEqualTo("/data");
        assertThat(target.uri().getQuery()).isEqualTo("since=5");
        assertThat(target.hostHeader()).contains("receiver.local:8080");
    }

    @Test
    @DisplayName("an IPv6 literal is bracketed, or the authority would not parse")
    void bracketsIpv6() throws UnknownHostException {
        PinnedTarget target =
                PinnedTarget.of(
                        URI.create("http://receiver.local/status"),
                        Optional.of(InetAddress.getByName("fd00::1")));

        assertThat(target.uri().toString()).startsWith("http://[fd00");
        assertThat(target.uri().getPath()).isEqualTo("/status");
        assertThat(target.hostHeader()).contains("receiver.local");
    }

    @Test
    @DisplayName("an ordinary URL is left exactly as it was, with no Host header forced")
    void leavesUnpinnedUrlsAlone() {
        URI original = URI.create("https://earthquake.usgs.gov/feed.geojson?limit=10");

        PinnedTarget target = PinnedTarget.of(original, Optional.empty());

        assertThat(target.uri()).isEqualTo(original);
        assertThat(target.hostHeader()).isEmpty();
    }

    @Test
    @DisplayName("the scheme, port and fragment survive the rewrite")
    void keepsTheRestOfTheUrl() throws UnknownHostException {
        PinnedTarget target =
                PinnedTarget.of(
                        URI.create("https://receiver.local:9443/a/b#top"),
                        Optional.of(InetAddress.getByName("10.0.0.7")));

        assertThat(target.uri().getScheme()).isEqualTo("https");
        assertThat(target.uri().getPort()).isEqualTo(9443);
        assertThat(target.uri().getPath()).isEqualTo("/a/b");
        assertThat(target.uri().getFragment()).isEqualTo("top");
    }
}
