package ai.foresignal.source.adapters.internet;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link Cidr} (prompt 092): bitwise containment for IPv4 and IPv6, literals only. */
class CidrTest {

    private static Cidr cidr(String value) {
        return Cidr.parse(value).orElseThrow();
    }

    @Test
    @DisplayName("IPv4 containment is by bits, not by text")
    void ipv4() {
        assertThat(cidr("192.168.1.0/24").strictlyContains(cidr("192.168.1.128/25"))).isTrue();
        assertThat(cidr("192.168.1.0/24").contains(cidr("192.168.10.0/24")))
                .as("192.168.10.0/24 starts with the same text but is not inside").isFalse();
        assertThat(cidr("192.168.1.0/24").strictlyContains(cidr("192.168.1.0/24"))).isFalse();
        assertThat(cidr("10.0.0.0/8").strictlyContains(cidr("10.255.0.0/16"))).isTrue();
        assertThat(cidr("10.0.0.0/9").contains(cidr("10.128.0.0/16"))).isFalse();
    }

    @Test
    @DisplayName("IPv6 containment works whatever the spelling, and families never mix")
    void ipv6() {
        assertThat(cidr("2001:db8::/32").strictlyContains(cidr("2001:0db8:0001::/48"))).isTrue();
        assertThat(cidr("2001:db8::/32").contains(cidr("2001:db9::/48"))).isFalse();
        assertThat(cidr("2001:db8::/32")).isEqualTo(cidr("2001:0DB8:0000::/32"));
        assertThat(cidr("::/0").contains(cidr("1.1.1.0/24"))).isFalse();
    }

    @Test
    @DisplayName("host bits are masked; names, out-of-range octets and bad lengths are refused")
    void parsing() {
        assertThat(cidr("192.168.1.77/24")).isEqualTo(cidr("192.168.1.0/24"));
        assertThat(cidr("192.168.1.77/24").toString()).isEqualTo("192.168.1.0/24");
        assertThat(Cidr.parse("example.com/24")).isEmpty();
        assertThat(Cidr.parse("999.1.1.0/24")).isEmpty();
        assertThat(Cidr.parse("1.1.1.0/33")).isEmpty();
        assertThat(Cidr.parse("1.1.1.0")).isEmpty();
        assertThat(Cidr.parse(null)).isEmpty();
    }
}
