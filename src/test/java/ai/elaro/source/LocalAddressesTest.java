package ai.elaro.source;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LocalAddressesTest {

    private static InetAddress address(String literal) {
        try {
            return InetAddress.getByName(literal);
        } catch (UnknownHostException e) {
            throw new AssertionError(e);
        }
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "127.0.0.1",
                "127.1.2.3",
                "10.0.0.1",
                "10.255.255.255",
                "172.16.0.1",
                "172.31.255.255",
                "192.168.0.1",
                "169.254.1.1",
                "::1",
                "fd00::1",
                "fc00::1",
                "fe80::1",
            })
    @DisplayName("loopback, RFC 1918, link-local and unique-local addresses are local")
    void acceptsLocalAddresses(String literal) {
        assertThat(LocalAddresses.isPrivate(address(literal))).isTrue();
        assertThat(LocalAddresses.rejectionReason(address(literal))).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"8.8.8.8", "1.1.1.1", "93.184.216.34", "172.32.0.1", "192.167.0.1", "2001:4860:4860::8888"})
    @DisplayName("public addresses are not local")
    void rejectsPublicAddresses(String literal) {
        assertThat(LocalAddresses.isPrivate(address(literal))).isFalse();
        assertThat(LocalAddresses.rejectionReason(address(literal))).contains("public address");
    }

    @ParameterizedTest
    @ValueSource(strings = {"100.64.0.1", "100.100.100.200", "100.127.255.255"})
    @DisplayName("carrier-grade NAT is routable but not the operator's LAN, so it is refused")
    void rejectsCarrierGradeNat(String literal) {
        assertThat(LocalAddresses.isCarrierGradeNat(address(literal))).isTrue();
        assertThat(LocalAddresses.rejectionReason(address(literal))).contains("carrier-grade NAT");
    }

    @ParameterizedTest
    @ValueSource(strings = {"100.63.255.255", "100.128.0.1", "99.64.0.1"})
    @DisplayName("the carrier-grade NAT boundaries are exact")
    void cgnatBoundariesAreExact(String literal) {
        assertThat(LocalAddresses.isCarrierGradeNat(address(literal))).isFalse();
    }

    @Test
    @DisplayName("an IPv4-mapped IPv6 address is judged as the IPv4 address it is")
    void handlesIpv4MappedAddresses() {
        // ::ffff:169.254.169.254 would otherwise slip past an IPv6-only check.
        assertThat(LocalAddresses.isPrivate(address("::ffff:169.254.169.254"))).isTrue();
        assertThat(LocalAddresses.isPrivate(address("::ffff:8.8.8.8"))).isFalse();
        assertThat(LocalAddresses.isCarrierGradeNat(address("::ffff:100.100.100.200"))).isTrue();
    }

    @Test
    void nullIsNeverLocal() {
        assertThat(LocalAddresses.isPrivate(null)).isFalse();
        assertThat(LocalAddresses.rejectionReason(null)).contains("did not resolve");
    }
}
