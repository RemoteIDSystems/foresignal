package ai.foresignal.net;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.settings.PushSettings;
import java.net.InetAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link Cidr} (moved from {@code notify.push} by prompt 035) and the {@code push.allowedPrivateHosts}
 * entry rules it backs (prompt 031).
 */
class CidrTest {

    @Test
    @DisplayName("CIDR parsing and matching")
    void cidr() throws Exception {
        Cidr block = Cidr.parse("192.168.1.77/24").orElseThrow();
        assertThat(block.toString()).isEqualTo("192.168.1.0/24");
        assertThat(block.contains(InetAddress.getByName("192.168.1.255"))).isTrue();
        assertThat(block.contains(InetAddress.getByName("192.168.2.0"))).isFalse();
        assertThat(block.contains(InetAddress.getByName("fd00::1"))).isFalse();
        assertThat(block.contains(null)).isFalse();
        assertThat(block.ipv4()).isTrue();
        assertThat(Cidr.parse("10.0.0.0/12").orElseThrow().contains(InetAddress.getByName("10.15.255.255"))).isTrue();
        assertThat(Cidr.parse("10.0.0.0/12").orElseThrow().contains(InetAddress.getByName("10.16.0.0"))).isFalse();
        assertThat(Cidr.parse("not-an-address")).isEmpty();
        assertThat(Cidr.parse("example.com/24")).isEmpty();
        assertThat(Cidr.parse("10.0.0.0/33")).isEmpty();
        assertThat(Cidr.parse("10.0.0.0/x")).isEmpty();
        assertThat(Cidr.parse(null)).isEmpty();
        assertThat(Cidr.literal("example.com")).isEmpty();
        assertThat(Cidr.literal("2130706433")).as("never a lookup, never a legacy form").isEmpty();
        assertThat(Cidr.parse("[fd00::1]/64").orElseThrow().prefix()).isEqualTo(64);
        assertThat(Cidr.parse("fd00::1").orElseThrow().prefix()).isEqualTo(128);
        assertThat(Cidr.parse("0.0.0.0/0").orElseThrow().contains(InetAddress.getByName("8.8.8.8"))).isTrue();
    }

    @Test
    @DisplayName("allowedPrivateHosts entries: exact hosts and CIDRs with minimum prefixes; no wildcards")
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
}
