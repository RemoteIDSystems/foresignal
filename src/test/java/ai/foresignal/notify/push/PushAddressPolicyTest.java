package ai.foresignal.notify.push;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.settings.PushSettings;
import ai.foresignal.testsupport.push.MutableResolver;
import java.net.InetAddress;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The push endpoint address policy (prompt 031). DNS is a stand-in; nothing is contacted. */
class PushAddressPolicyTest {

    MutableResolver dns;
    PushAddressPolicy policy;

    @BeforeEach
    void setUp() {
        dns = new MutableResolver();
        policy = new PushAddressPolicy(dns);
    }

    PushAddressPolicy.Decision check(String address, List<String> allowed) {
        dns.set("push.test", address);
        return policy.check(URI.create("https://push.test/p/1"), allowed);
    }

    @Test
    @DisplayName("every non-global range is refused")
    void blockedRanges() {
        String[] blocked = {
            "127.0.0.1", "10.1.2.3", "172.16.0.1", "172.31.255.254", "192.168.1.10", "100.64.0.1", "100.127.255.254",
            "169.254.1.1", "169.254.169.254", "100.100.100.200", "0.0.0.0", "0.1.2.3", "224.0.0.1", "239.255.255.250",
            "240.0.0.1", "255.255.255.255", "192.0.0.8", "192.0.2.1", "198.18.0.1", "198.51.100.1", "203.0.113.9",
            "::1", "::", "fe80::1", "fc00::1", "fd12:3456::1", "fd00:ec2::254", "fec0::1", "ff02::1",
            "::ffff:127.0.0.1", "::ffff:10.0.0.1", "::ffff:169.254.169.254", "::10.0.0.1", "::8.8.8.8",
            "64:ff9b::808:808", "64:ff9b:1::1", "2002:c000:0204::1", "2001:0:4136:e378::1", "2001:db8::1", "100::1"};
        for (String address : blocked) {
            PushAddressPolicy.Decision d = check(address, List.of());
            assertThat(d.allowed()).as(address).isFalse();
            assertThat(d.pinned()).as(address).isNull();
            assertThat(d.reason()).as(address).contains("push.test");
        }
    }

    @Test
    @DisplayName("global IPv4 and IPv6 addresses are allowed and pinned")
    void globalAllowed() {
        for (String address : new String[] {"8.8.8.8", "1.1.1.1", "142.250.80.46", "2a00:1450:4001:82a::200e",
            "2606:4700:4700::1111", "172.32.0.1", "100.128.0.1", "11.0.0.1"}) {
            PushAddressPolicy.Decision d = check(address, List.of());
            assertThat(d.allowed()).as(address).isTrue();
            assertThat(d.pinned()).isEqualTo(d.addresses().getFirst());
        }
    }

    @Test
    @DisplayName("DNS answering one public and one private address is refused")
    void mixedAnswer() {
        dns.set("push.test", "8.8.8.8", "10.0.0.5");
        assertThat(policy.check(URI.create("https://push.test/x"), List.of()).allowed()).isFalse();
        dns.set("push.test", "2606:4700:4700::1111", "fd00::5");
        assertThat(policy.check(URI.create("https://push.test/x"), List.of()).allowed()).isFalse();
    }

    @Test
    @DisplayName("allowlist by exact host name, by CIDR covering every address, and by a bare address")
    void allowlist() {
        assertThat(check("192.168.1.20", List.of("push.test")).allowed()).isTrue();
        assertThat(check("192.168.1.20", List.of("other.test")).allowed()).isFalse();
        assertThat(check("192.168.1.20", List.of("sub.push.test")).allowed()).isFalse();
        assertThat(check("192.168.1.20", List.of("192.168.0.0/16")).allowed()).isTrue();
        assertThat(check("192.168.1.20", List.of("192.168.2.0/24")).allowed()).isFalse();
        assertThat(check("192.168.1.20", List.of("192.168.1.20")).allowed()).isTrue();
        assertThat(check("fd12::7", List.of("fd12::/32")).allowed()).isTrue();
        assertThat(check("127.0.0.1", List.of("127.0.0.0/8")).allowed()).isTrue();

        dns.set("push.test", "192.168.1.20", "10.0.0.1");
        assertThat(policy.check(URI.create("https://push.test/x"), List.of("192.168.0.0/16")).allowed())
                .as("every address must match a CIDR").isFalse();
        assertThat(policy.check(URI.create("https://push.test/x"), List.of("192.168.0.0/16", "10.0.0.0/8")).allowed())
                .isTrue();
    }

    @Test
    @DisplayName("metadata addresses stay refused even when allowlisted")
    void metadataAlways() {
        assertThat(check("169.254.169.254", List.of("push.test")).allowed()).isFalse();
        assertThat(check("::ffff:169.254.169.254", List.of("169.254.0.0/16")).allowed()).isFalse();
        assertThat(check("fd00:ec2::254", List.of("fd00::/32")).allowed()).isFalse();
    }

    @Test
    @DisplayName("an unresolvable or missing host is refused")
    void unresolvable() {
        assertThat(policy.check(URI.create("https://nowhere.test/x"), List.of()).allowed()).isFalse();
        assertThat(policy.check((String) null, List.of()).allowed()).isFalse();
    }

    @Test
    @DisplayName("allowlist entries: exact hosts and CIDRs with minimum prefixes; no wildcards")
    void entryValidation() {
        assertThat(PushSettings.allowedHostProblem("distributor.lan")).isNull();
        assertThat(PushSettings.allowedHostProblem("10.0.0.0/8")).isNull();
        assertThat(PushSettings.allowedHostProblem("fd00::/32")).isNull();
        assertThat(PushSettings.allowedHostProblem("192.168.1.5")).isNull();
        assertThat(PushSettings.allowedHostProblem("*.lan")).contains("wildcard");
        assertThat(PushSettings.allowedHostProblem("10.0.0.0/7")).contains("/8");
        assertThat(PushSettings.allowedHostProblem("0.0.0.0/0")).contains("/8");
        assertThat(PushSettings.allowedHostProblem("fd00::/16")).contains("/32");
        assertThat(PushSettings.allowedHostProblem("10.0.0.0/33")).isNotNull();
        assertThat(PushSettings.allowedHostProblem("bad host")).isNotNull();
        assertThat(PushSettings.allowedHostProblem("")).isNotNull();
    }

    @Test
    @DisplayName("CIDR parsing and matching")
    void cidr() throws Exception {
        Cidr block = Cidr.parse("192.168.1.77/24").orElseThrow();
        assertThat(block.toString()).isEqualTo("192.168.1.0/24");
        assertThat(block.contains(InetAddress.getByName("192.168.1.255"))).isTrue();
        assertThat(block.contains(InetAddress.getByName("192.168.2.0"))).isFalse();
        assertThat(block.contains(InetAddress.getByName("fd00::1"))).isFalse();
        assertThat(Cidr.parse("10.0.0.0/12").orElseThrow().contains(InetAddress.getByName("10.15.255.255"))).isTrue();
        assertThat(Cidr.parse("10.0.0.0/12").orElseThrow().contains(InetAddress.getByName("10.16.0.0"))).isFalse();
        assertThat(Cidr.parse("not-an-address")).isEmpty();
        assertThat(Cidr.parse("example.com/24")).isEmpty();
        assertThat(Cidr.literal("example.com")).isEmpty();
        assertThat(Cidr.parse("[fd00::1]/64").orElseThrow().prefix()).isEqualTo(64);
    }
}
