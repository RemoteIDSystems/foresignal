package ai.foresignal.inbound.edp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** The provider URL boundary (docs/External-Data-Protocol.md §7, invariant EXT-6). No network is used. */
class EdpUrlGuardTest {

    private static InetAddress ip(String literal) {
        try {
            return InetAddress.getByName(literal);
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }
    }

    private static EdpUrlGuard resolvingTo(String literal) {
        return new EdpUrlGuard(host -> new InetAddress[] {ip(literal)});
    }

    @ParameterizedTest
    @ValueSource(strings = {"127.0.0.1", "10.1.2.3", "172.16.0.9", "192.168.1.10", "169.254.10.10", "100.64.0.1",
            "::1", "fe80::1", "fd12:3456::1", "0.0.0.0", "224.0.0.1"})
    @DisplayName("private, loopback, link-local, CGNAT, unspecified and multicast addresses are refused by default")
    void refusedByDefault(String address) {
        assertThatThrownBy(() -> resolvingTo(address).vet(URI.create("https://provider.example.org/items"), false))
                .isInstanceOf(EdpUrlRefusedException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"127.0.0.1", "10.1.2.3", "192.168.1.10", "fd12:3456::1"})
    @DisplayName("the same addresses are accepted when the source allows private networks, over http too")
    void allowedForThisSource(String address) throws Exception {
        EdpUrlGuard.VettedUrl vetted = resolvingTo(address).vet(URI.create("http://nas.lan:8080/items"), true);

        assertThat(vetted.address()).isEqualTo(ip(address));
        assertThat(vetted.port()).isEqualTo(8080);
        assertThat(vetted.host()).isEqualTo("nas.lan");
    }

    @ParameterizedTest
    @ValueSource(strings = {"169.254.169.254", "fd00:ec2::254", "100.100.100.200", "::ffff:169.254.169.254"})
    @DisplayName("cloud metadata addresses are refused even when private networks are allowed")
    void metadataAlwaysRefused(String address) {
        assertThatThrownBy(() -> resolvingTo(address).vet(URI.create("http://metadata.lan/latest"), true))
                .isInstanceOf(EdpUrlRefusedException.class)
                .hasMessageContaining("metadata");
    }

    @Test
    @DisplayName("http is refused unless private networks are allowed; user info and other schemes always")
    void schemes() {
        EdpUrlGuard guard = resolvingTo("93.184.216.34");

        assertThatThrownBy(() -> guard.vet(URI.create("http://provider.example.org/items"), false))
                .isInstanceOf(EdpUrlRefusedException.class).hasMessageContaining("https");
        assertThatThrownBy(() -> guard.vet(URI.create("https://user:pw@provider.example.org/items"), false))
                .isInstanceOf(EdpUrlRefusedException.class);
        assertThatThrownBy(() -> guard.vet(URI.create("ftp://provider.example.org/items"), true))
                .isInstanceOf(EdpUrlRefusedException.class);
        assertThatThrownBy(() -> guard.vet(URI.create("/relative"), true))
                .isInstanceOf(EdpUrlRefusedException.class);
    }

    @Test
    @DisplayName("a public host is accepted and pinned to its first address")
    void publicAccepted() throws Exception {
        EdpUrlGuard.VettedUrl vetted = resolvingTo("93.184.216.34").vet(URI.create("https://provider.example.org/x"), false);

        assertThat(vetted.address().getHostAddress()).isEqualTo("93.184.216.34");
        assertThat(vetted.port()).isEqualTo(443);
    }

    @Test
    @DisplayName("any private address among several refuses the host")
    void mixedAnswers() {
        EdpUrlGuard guard = new EdpUrlGuard(host -> new InetAddress[] {ip("93.184.216.34"), ip("10.0.0.1")});

        assertThatThrownBy(() -> guard.vet(URI.create("https://provider.example.org/x"), false))
                .isInstanceOf(EdpUrlRefusedException.class);
    }

    @Test
    @DisplayName("DNS rebinding: the host is resolved once and the vetted public address is pinned, so a second, "
            + "private answer is never consulted")
    void rebinding() throws Exception {
        AtomicInteger lookups = new AtomicInteger();
        EdpUrlGuard guard = new EdpUrlGuard(host -> new InetAddress[] {
            lookups.incrementAndGet() == 1 ? ip("93.184.216.34") : ip("127.0.0.1")});

        EdpUrlGuard.VettedUrl vetted = guard.vet(URI.create("https://rebind.example.org/items"), false);
        URI pinned = EdpHttpClient.pinnedUri(vetted.uri(), vetted.address());

        assertThat(lookups).hasValue(1);
        assertThat(pinned.getHost()).isEqualTo("93.184.216.34");
        assertThat(pinned.getPath()).isEqualTo("/items");
        // A later lookup (a redirect re-vet or a rebinding attempt) answers private and is refused.
        assertThatThrownBy(() -> guard.vet(URI.create("https://rebind.example.org/items"), false))
                .isInstanceOf(EdpUrlRefusedException.class);
    }

    @Test
    @DisplayName("redirects are followed only to the same host")
    void crossHostRedirect() {
        URI from = URI.create("https://provider.example.org/a");

        assertThat(EdpUrlGuard.sameHost(from, from.resolve("/b"))).isTrue();
        assertThat(EdpUrlGuard.sameHost(from, URI.create("https://PROVIDER.example.org/c"))).isTrue();
        assertThat(EdpUrlGuard.sameHost(from, URI.create("https://evil.example.org/a"))).isFalse();
        assertThat(EdpUrlGuard.sameHost(from, URI.create("http://169.254.169.254/latest"))).isFalse();
    }

    @Test
    @DisplayName("certificate names: exact and single-label wildcard matches only")
    void hostnameMatching() {
        assertThat(EdpHttpClient.dnsNameMatches("provider.example.org", "PROVIDER.example.org")).isTrue();
        assertThat(EdpHttpClient.dnsNameMatches("*.example.org", "provider.example.org")).isTrue();
        assertThat(EdpHttpClient.dnsNameMatches("*.example.org", "a.b.example.org")).isFalse();
        assertThat(EdpHttpClient.dnsNameMatches("*.example.org", "example.org")).isFalse();
        assertThat(EdpHttpClient.dnsNameMatches("*.org", "example.org")).isFalse();
        assertThat(EdpHttpClient.dnsNameMatches("other.example.org", "provider.example.org")).isFalse();
    }
}
