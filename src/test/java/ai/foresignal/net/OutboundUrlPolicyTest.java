package ai.foresignal.net;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.net.OutboundUrlPolicy.Allowed;
import ai.foresignal.net.OutboundUrlPolicy.Denied;
import ai.foresignal.net.OutboundUrlPolicy.Profile;
import ai.foresignal.net.OutboundUrlPolicy.Result;
import ai.foresignal.net.SpecialPurposeAddresses.AddressClass;
import ai.foresignal.settings.AppSettingChangedEvent;
import ai.foresignal.settings.OutboundSettings;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * {@link OutboundUrlPolicy} (prompt 035): every blocked range and literal form, embedded IPv4, the
 * profile differences, the overrides and DNS answers mixing public and private. DNS is a stand-in
 * ({@link #dns}); nothing here reaches real DNS or the network.
 */
class OutboundUrlPolicyTest {

    /** Host name to answer; a missing name does not resolve. */
    final Map<String, List<InetAddress>> dns = new ConcurrentHashMap<>();
    final AtomicInteger lookups = new AtomicInteger();
    OutboundSettings overrides = OutboundSettings.defaults();
    List<String> pushAllowed = List.of();
    final AtomicInteger overrideReads = new AtomicInteger();

    final HostResolver resolver = host -> {
        lookups.incrementAndGet();
        if (host.indexOf(':') >= 0 || host.matches("^[0-9.]+$")) {
            throw new AssertionError("a literal reached the resolver: " + host);
        }
        List<InetAddress> answer = dns.get(host.toLowerCase(Locale.ROOT));
        if (answer == null) {
            throw new UnknownHostException(host);
        }
        return answer.toArray(InetAddress[]::new);
    };

    final OutboundUrlPolicy policy = new OutboundUrlPolicy(resolver, () -> {
        overrideReads.incrementAndGet();
        return overrides;
    }, () -> pushAllowed, Optional.empty());

    static InetAddress ip(String literal) {
        try {
            return InetAddress.getByName(literal);
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }
    }

    void answer(String host, String... literals) {
        List<InetAddress> out = new ArrayList<>();
        for (String literal : literals) {
            out.add(ip(literal));
        }
        dns.put(host, out);
    }

    /** Checks {@code https://target.test/p?q=1} with {@code target.test} answering {@code address}. */
    Result viaDns(String address, Profile profile) {
        answer("target.test", address);
        return policy.check(URI.create("https://target.test/p?q=1"), profile);
    }

    /** Checks {@code https://[address]/} or {@code https://address/} directly. */
    Result viaLiteral(String address, Profile profile) {
        String host = address.indexOf(':') >= 0 ? "[" + address + "]" : address;
        return policy.check(URI.create("https://" + host + "/p"), profile);
    }

    static Stream<Arguments> everyProfile(String... addresses) {
        List<Arguments> out = new ArrayList<>();
        for (String address : addresses) {
            for (Profile profile : Profile.values()) {
                out.add(Arguments.of(address, profile));
            }
        }
        return out.stream();
    }

    // ---------- the address table ----------

    /** Always blocked: not forwardable, reserved, link-local, multicast, broadcast, metadata. */
    static final String[] ALWAYS_BLOCKED = {
        "0.0.0.0", "0.1.2.3", "169.254.0.1", "169.254.169.254", "169.254.170.2", "100.100.100.200", "192.0.0.8",
        "192.0.0.100", "192.0.0.170", "192.0.0.171", "192.0.2.1", "192.88.99.1", "198.51.100.1", "203.0.113.9",
        "240.0.0.1", "255.255.255.255", "224.0.0.1", "239.255.255.250",
        "::", "fe80::1", "febf::1", "fd00:ec2::254", "fec0::1", "ff02::1", "ff05::2", "2001:db8::1", "3fff::1",
        "2001:10::1", "2001:5::1", "100:0:0:1::1", "4000::1", "e000::1",
        // embedded forms carrying a blocked IPv4 address
        "::ffff:169.254.169.254", "::10.0.0.1", "::8.8.8.8", "64:ff9b::a9fe:a9fe", "2002:a9fe:a9fe::1",
        "2001:0:4136:e378:8000:63bf:5601:5601", "2001::1", "64:ff9b::a9fe:1"};

    static Stream<Arguments> alwaysBlocked() {
        return everyProfile(ALWAYS_BLOCKED);
    }

    @ParameterizedTest(name = "{0} under {1}")
    @MethodSource("alwaysBlocked")
    @DisplayName("always-blocked addresses are refused in every profile, by literal and by DNS")
    void alwaysBlockedEverywhere(String address, Profile profile) {
        assertThat(viaDns(address, profile)).as(address).isInstanceOf(Denied.class);
        assertThat(viaLiteral(address, profile)).as(address).isInstanceOf(Denied.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"169.254.169.254", "fd00:ec2::254", "100.100.100.200", "fe80::1", "0.0.0.0", "224.0.0.1",
        "::ffff:169.254.169.254", "64:ff9b::a9fe:a9fe"})
    @DisplayName("allow overrides never unblock the always-blocked set")
    void overridesNeverUnblock(String address) {
        overrides = new OutboundSettings(List.of("target.test"), List.of("0.0.0.0/0", "::/0"), List.of(), List.of());
        pushAllowed = List.of("target.test", "0.0.0.0/0");
        for (Profile profile : Profile.values()) {
            assertThat(viaDns(address, profile)).as(address + " " + profile).isInstanceOf(Denied.class);
        }
    }

    static Stream<Arguments> nonGlobal() {
        return everyProfile("198.18.0.1", "198.19.255.254", "192.0.0.1", "64:ff9b:1::1", "100::1", "2001:2::1",
                "5f00::1");
    }

    @ParameterizedTest(name = "{0} under {1}")
    @MethodSource("nonGlobal")
    @DisplayName("forwardable but non-global blocks are refused unless an allowCidrs entry covers them")
    void nonGlobalNeedsOverride(String address, Profile profile) {
        assertThat(viaDns(address, profile)).isInstanceOf(Denied.class);
        overrides = new OutboundSettings(List.of(), List.of("198.18.0.0/15", "192.0.0.0/29", "64:ff9b:1::/48",
                "100::/64", "2001:2::/48", "5f00::/16"), List.of(), List.of());
        policy.invalidate();
        assertThat(viaDns(address, profile)).isInstanceOf(Allowed.class);
    }

    static final String[] PRIVATE_AND_LOOPBACK = {"127.0.0.1", "127.255.255.254", "10.1.2.3", "172.16.0.1",
        "172.31.255.254", "192.168.1.10", "::1", "fc00::1", "fd12:3456::1", "::ffff:10.0.0.1", "64:ff9b::a00:1",
        "2002:c0a8:0101::1"};

    @Test
    @DisplayName("loopback, RFC 1918 and unique-local: allowed by LAN_ALLOWED and LAN_ONLY, refused by PUBLIC_ONLY and PUSH_ENDPOINT")
    void privateAndLoopback() {
        for (String address : PRIVATE_AND_LOOPBACK) {
            assertThat(viaDns(address, Profile.LAN_ALLOWED)).as(address).isInstanceOf(Allowed.class);
            assertThat(viaDns(address, Profile.LAN_ONLY)).as(address).isInstanceOf(Allowed.class);
            assertThat(viaDns(address, Profile.PUBLIC_ONLY)).as(address).isInstanceOf(Denied.class);
            assertThat(viaDns(address, Profile.PUSH_ENDPOINT)).as(address).isInstanceOf(Denied.class);
        }
    }

    @Test
    @DisplayName("shared 100.64/10 (CGNAT, Tailscale): LAN_ALLOWED only")
    void shared() {
        for (String address : new String[] {"100.64.0.1", "100.127.255.254", "100.100.100.199"}) {
            assertThat(viaDns(address, Profile.LAN_ALLOWED)).as(address).isInstanceOf(Allowed.class);
            assertThat(viaDns(address, Profile.LAN_ONLY)).as(address).isInstanceOf(Denied.class);
            assertThat(viaDns(address, Profile.PUBLIC_ONLY)).as(address).isInstanceOf(Denied.class);
            assertThat(viaDns(address, Profile.PUSH_ENDPOINT)).as(address).isInstanceOf(Denied.class);
        }
    }

    static final String[] PUBLIC = {"8.8.8.8", "1.1.1.1", "142.250.80.46", "172.32.0.1", "100.128.0.1", "11.0.0.1",
        "192.0.0.9", "192.0.0.10", "192.31.196.1", "192.52.193.1", "192.175.48.1", "2606:4700:4700::1111",
        "2a00:1450:4001:82a::200e", "2001:1::1", "2001:3::1", "2001:4:112::1", "2620:4f:8000::1", "::ffff:8.8.8.8",
        "64:ff9b::808:808", "2002:0808:0808::1", "2001:0:4136:e378:8000:63bf:f7f7:f7f7"};

    @Test
    @DisplayName("public addresses (and embedded public IPv4): every profile but LAN_ONLY")
    void publicAddresses() {
        for (String address : PUBLIC) {
            assertThat(viaDns(address, Profile.PUBLIC_ONLY)).as(address).isInstanceOf(Allowed.class);
            assertThat(viaDns(address, Profile.PUSH_ENDPOINT)).as(address).isInstanceOf(Allowed.class);
            assertThat(viaDns(address, Profile.LAN_ALLOWED)).as(address).isInstanceOf(Allowed.class);
            Result lanOnly = viaDns(address, Profile.LAN_ONLY);
            assertThat(lanOnly).as(address).isInstanceOf(Denied.class);
            assertThat(((Denied) lanOnly).reason()).as(address).contains("LAN_ONLY");
        }
    }

    @Test
    @DisplayName("embedded IPv4 is named in the reason")
    void embeddedReason() {
        Denied nat64 = (Denied) viaLiteral("64:ff9b::a9fe:a9fe", Profile.LAN_ALLOWED);
        assertThat(nat64.reason()).contains("169.254.169.254").contains("IPv4-IPv6 Translation");
        Denied teredo = (Denied) viaLiteral("2001:0:4136:e378:8000:63bf:5601:5601", Profile.LAN_ALLOWED);
        assertThat(teredo.reason()).contains("169.254.169.254").contains("TEREDO");
        Denied sixToFour = (Denied) viaLiteral("2002:0a00:0001::1", Profile.PUBLIC_ONLY);
        assertThat(sixToFour.reason()).contains("10.0.0.1").contains("6to4");
        Denied compatible = (Denied) viaLiteral("::8.8.8.8", Profile.PUBLIC_ONLY);
        assertThat(compatible.reason()).contains("IPv4-compatible").contains("8.8.8.8");
        Denied teredoServer = (Denied) viaLiteral("2001:0:a9fe:a9fe:8000:63bf:f7f7:f7f7", Profile.PUBLIC_ONLY);
        assertThat(teredoServer.reason()).contains("Teredo address whose server 169.254.169.254");
    }

    @Test
    @DisplayName("the most specific table entry wins")
    void mostSpecific() {
        assertThat(SpecialPurposeAddresses.classify(ip("100.100.100.200")).addressClass()).isEqualTo(AddressClass.BLOCKED);
        assertThat(SpecialPurposeAddresses.classify(ip("100.64.0.1")).addressClass()).isEqualTo(AddressClass.SHARED);
        assertThat(SpecialPurposeAddresses.classify(ip("192.0.0.9")).addressClass()).isEqualTo(AddressClass.GLOBAL);
        assertThat(SpecialPurposeAddresses.classify(ip("192.0.0.1")).addressClass()).isEqualTo(AddressClass.NON_GLOBAL);
        assertThat(SpecialPurposeAddresses.classify(ip("192.0.0.200")).addressClass()).isEqualTo(AddressClass.BLOCKED);
        assertThat(SpecialPurposeAddresses.classify(ip("fd00:ec2::254")).addressClass()).isEqualTo(AddressClass.BLOCKED);
        assertThat(SpecialPurposeAddresses.classify(ip("fd00:ec2::255")).addressClass()).isEqualTo(AddressClass.PRIVATE);
        assertThat(SpecialPurposeAddresses.classify(ip("2001:1::1")).addressClass()).isEqualTo(AddressClass.GLOBAL);
        assertThat(SpecialPurposeAddresses.classify(ip("2001:1::9")).addressClass()).isEqualTo(AddressClass.BLOCKED);
        assertThat(SpecialPurposeAddresses.classify(ip("2001::5")).addressClass()).isEqualTo(AddressClass.EMBEDDED);
        assertThat(SpecialPurposeAddresses.classify(ip("8.8.8.8")).entry()).isNull();
        assertThat(SpecialPurposeAddresses.classify(ip("4000::1")).addressClass()).isEqualTo(AddressClass.BLOCKED);
        assertThat(SpecialPurposeAddresses.TABLE).allSatisfy(e -> assertThat(e.block().prefix()).isPositive()
                .isLessThanOrEqualTo(e.block().network().getAddress().length * 8));
    }

    @Test
    @DisplayName("the embedded IPv4 decoders")
    void decoders() {
        assertThat(SpecialPurposeAddresses.embeddedIpv4(ip("64:ff9b::c000:221"),
                SpecialPurposeAddresses.Embedding.NAT64)).contains((java.net.Inet4Address) ip("192.0.2.33"));
        assertThat(SpecialPurposeAddresses.embeddedIpv4(ip("2002:c000:0221::1"),
                SpecialPurposeAddresses.Embedding.SIX_TO_FOUR)).contains((java.net.Inet4Address) ip("192.0.2.33"));
        assertThat(SpecialPurposeAddresses.embeddedIpv4(ip("2001:0:4136:e378:8000:63bf:3fff:fdd2"),
                SpecialPurposeAddresses.Embedding.TEREDO)).contains((java.net.Inet4Address) ip("192.0.2.45"));
        assertThat(SpecialPurposeAddresses.teredoServer(ip("2001:0:4136:e378:8000:63bf:3fff:fdd2")))
                .contains((java.net.Inet4Address) ip("65.54.227.120"));
        assertThat(SpecialPurposeAddresses.embeddedIpv4(ip("8.8.8.8"), SpecialPurposeAddresses.Embedding.NAT64))
                .isEmpty();
    }

    // ---------- literal forms ----------

    @ParameterizedTest
    @ValueSource(strings = {"2130706433", "0x7f000001", "0x7f.0.0.1", "0x7F.0x0.0x0.0x1", "017700000001", "0177.0.0.1",
        "127.1", "127.0.1", "127.0.0.1.", "010.0.0.1", "8.8.8.010", "1.2.3.256", "1.2.3.4.5", "0x08.0x08.0x08.0x08",
        "4294967295", "0", "a.0x10", "999.1.1.1"})
    @DisplayName("non-dotted-quad IPv4 literals are refused before any resolution")
    void obfuscatedIpv4(String host) {
        for (Profile profile : Profile.values()) {
            Result result = policy.checkHost(host, profile);
            assertThat(result).as(host).isInstanceOf(Denied.class);
            assertThat(((Denied) result).unresolved()).isFalse();
        }
        assertThat(lookups).hasValue(0);
    }

    @Test
    @DisplayName("IPv6 literals: zone ids, bad characters and non-strict dotted tails are refused")
    void ipv6Literals() {
        assertThat(policy.checkHost("fe80::1%eth0", Profile.LAN_ALLOWED)).isInstanceOf(Denied.class);
        assertThat(policy.checkHost("[fd00::1%25eth0]", Profile.LAN_ALLOWED)).isInstanceOf(Denied.class);
        assertThat(policy.checkHost("[gggg::1]", Profile.LAN_ALLOWED)).isInstanceOf(Denied.class);
        assertThat(policy.checkHost("::ffff:127.0.0.01", Profile.LAN_ALLOWED)).isInstanceOf(Denied.class);
        assertThat(policy.checkHost("[8.8.8.8]", Profile.PUBLIC_ONLY)).isInstanceOf(Denied.class);
        assertThat(policy.checkHost("[2606:4700:4700::1111]", Profile.PUBLIC_ONLY)).isInstanceOf(Allowed.class);
        assertThat(policy.checkHost("2606:4700:4700::1111", Profile.PUBLIC_ONLY)).isInstanceOf(Allowed.class);
        assertThat(policy.checkHost("8.8.8.8", Profile.PUBLIC_ONLY)).isInstanceOf(Allowed.class);
        assertThat(lookups).hasValue(0);
    }

    @Test
    @DisplayName("names that merely look hexadecimal are host names, resolved normally")
    void hexLookingNames() {
        answer("bad.cafe", "8.8.8.8");
        answer("deadbeef", "10.0.0.1");
        assertThat(policy.check(URI.create("https://bad.cafe/"), Profile.PUBLIC_ONLY)).isInstanceOf(Allowed.class);
        assertThat(policy.check(URI.create("http://deadbeef/"), Profile.LAN_ALLOWED)).isInstanceOf(Allowed.class);
        assertThat(policy.checkHost("bad host", Profile.LAN_ALLOWED)).isInstanceOf(Denied.class);
        assertThat(policy.checkHost("", Profile.LAN_ALLOWED)).isInstanceOf(Denied.class);
        assertThat(policy.checkHost(null, Profile.LAN_ALLOWED)).isInstanceOf(Denied.class);
    }

    // ---------- URL shape ----------

    @Test
    @DisplayName("https everywhere; http only in LAN profiles or with the per-field flag; no user info; no other scheme")
    void schemes() {
        answer("public.test", "8.8.8.8");
        assertThat(policy.check(URI.create("http://public.test/"), Profile.PUBLIC_ONLY)).isInstanceOf(Denied.class);
        assertThat(policy.check(URI.create("http://public.test/"), Profile.PUSH_ENDPOINT)).isInstanceOf(Denied.class);
        assertThat(policy.check(URI.create("http://public.test/"), Profile.PUBLIC_ONLY, true)).isInstanceOf(Allowed.class);
        assertThat(policy.check(URI.create("http://public.test/"), Profile.LAN_ALLOWED)).isInstanceOf(Allowed.class);
        assertThat(policy.check(URI.create("https://public.test/"), Profile.PUBLIC_ONLY)).isInstanceOf(Allowed.class);
        assertThat(policy.check(URI.create("wss://public.test/"), Profile.PUBLIC_ONLY)).isInstanceOf(Allowed.class);
        assertThat(policy.check(URI.create("ws://10.0.0.2:30003"), Profile.LAN_ONLY)).isInstanceOf(Allowed.class);
        assertThat(policy.check(URI.create("ftp://public.test/"), Profile.LAN_ALLOWED)).isInstanceOf(Denied.class);
        assertThat(policy.check(URI.create("file:///etc/passwd"), Profile.LAN_ALLOWED)).isInstanceOf(Denied.class);
        assertThat(policy.check(URI.create("https://user:pw@public.test/"), Profile.LAN_ALLOWED))
                .isInstanceOf(Denied.class);
        assertThat(policy.check(URI.create("mailto:ops@public.test"), Profile.LAN_ALLOWED)).isInstanceOf(Denied.class);
        assertThat(policy.check(URI.create("/relative"), Profile.LAN_ALLOWED)).isInstanceOf(Denied.class);
        assertThat(policy.check(URI.create("https:///nohost"), Profile.LAN_ALLOWED)).isInstanceOf(Denied.class);
        assertThat(policy.check((URI) null, Profile.LAN_ALLOWED)).isInstanceOf(Denied.class);
        assertThat(policy.check("not a url", Profile.LAN_ALLOWED, false)).isInstanceOf(Denied.class);
        assertThat(policy.check(" ", Profile.LAN_ALLOWED, false)).isInstanceOf(Denied.class);
        assertThat(policy.check("https://public.test/x", Profile.PUBLIC_ONLY, false)).isInstanceOf(Allowed.class);
    }

    @Test
    @DisplayName("a refusal names the host and address, never the path or query (they may carry tokens)")
    void reasonsOmitPathAndQuery() {
        answer("hooks.test", "10.0.0.7");
        Denied denied = (Denied) policy.check(URI.create("https://hooks.test/services/T000/SECRET?token=abc"),
                Profile.PUBLIC_ONLY);
        assertThat(denied.reason()).contains("hooks.test").contains("10.0.0.7").doesNotContain("SECRET")
                .doesNotContain("token=abc");
        assertThat(denied.code()).isEqualTo("URL_NOT_ALLOWED");
        assertThat(denied.allowed()).isFalse();
    }

    // ---------- DNS ----------

    @Test
    @DisplayName("every resolved address must pass: one public and one private answer is refused in PUBLIC_ONLY")
    void mixedAnswers() {
        answer("mixed.test", "8.8.8.8", "10.0.0.5");
        assertThat(policy.check(URI.create("https://mixed.test/"), Profile.PUBLIC_ONLY)).isInstanceOf(Denied.class);
        assertThat(policy.check(URI.create("https://mixed.test/"), Profile.PUSH_ENDPOINT)).isInstanceOf(Denied.class);
        assertThat(policy.check(URI.create("https://mixed.test/"), Profile.LAN_ALLOWED)).isInstanceOf(Allowed.class);
        answer("mixed6.test", "2606:4700:4700::1111", "fd00::5");
        assertThat(policy.check(URI.create("https://mixed6.test/"), Profile.PUBLIC_ONLY)).isInstanceOf(Denied.class);
        answer("meta.test", "10.0.0.5", "169.254.169.254");
        assertThat(policy.check(URI.create("https://meta.test/"), Profile.LAN_ALLOWED)).isInstanceOf(Denied.class);
        answer("lanpub.test", "192.168.1.5", "8.8.8.8");
        assertThat(policy.check(URI.create("http://lanpub.test/"), Profile.LAN_ONLY)).isInstanceOf(Denied.class);
    }

    @Test
    @DisplayName("resolved once; every address is returned in resolver order and the first is pinned")
    void resolvedOncePinned() {
        answer("two.test", "8.8.4.4", "8.8.8.8");
        Allowed allowed = (Allowed) policy.check(URI.create("https://two.test/"), Profile.PUBLIC_ONLY);
        assertThat(lookups).hasValue(1);
        assertThat(allowed.pinnedAddresses()).containsExactly(ip("8.8.4.4"), ip("8.8.8.8"));
        assertThat(allowed.pinned()).isEqualTo(ip("8.8.4.4"));
        assertThat(allowed.allowed()).isTrue();
        assertThatThrownBy(() -> new Allowed(List.of())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("a host that does not resolve is denied as unresolved; save-time validation accepts it")
    void unresolved() {
        Denied denied = (Denied) policy.check(URI.create("https://nowhere.test/"), Profile.LAN_ALLOWED);
        assertThat(denied.unresolved()).isTrue();
        assertThat(policy.saveProblem("https://nowhere.test/", Profile.LAN_ALLOWED, false)).isNull();
        assertThat(policy.saveHostProblem("smtp.nowhere.test", Profile.LAN_ALLOWED)).isNull();
        dns.put("empty.test", List.of());
        assertThat(((Denied) policy.checkHost("empty.test", Profile.LAN_ALLOWED)).unresolved()).isTrue();
        assertThat(policy.saveProblem("http://169.254.169.254/latest", Profile.LAN_ALLOWED, false)).contains("169.254");
        answer("smtp.test", "169.254.1.1");
        assertThat(policy.saveHostProblem("smtp.test", Profile.LAN_ALLOWED)).contains("169.254.1.1");
    }

    @Test
    @DisplayName("a rebinding between save and send is caught: each check resolves again")
    void rebinding() {
        answer("rebind.test", "8.8.8.8");
        assertThat(policy.saveProblem("https://rebind.test/hook", Profile.PUBLIC_ONLY, false)).isNull();
        answer("rebind.test", "169.254.169.254");
        assertThat(policy.check(URI.create("https://rebind.test/hook"), Profile.PUBLIC_ONLY)).isInstanceOf(Denied.class);
    }

    // ---------- overrides ----------

    @Test
    @DisplayName("denyHosts (exact and *.suffix) and denyCidrs refuse public hosts; deny wins over allow")
    void denies() {
        answer("evil.test", "8.8.8.8");
        answer("x.bad.test", "8.8.8.8");
        answer("bad.test", "8.8.8.3"); // inside denyCidrs 8.8.8.0/29 (8.8.8.8 is not; checkpoint 041)
        answer("dns.test", "8.8.8.5");
        overrides = new OutboundSettings(List.of("evil.test", "dns.test"), List.of("8.8.8.0/24"),
                List.of("EVIL.test.", "*.bad.test"), List.of("8.8.8.0/29"));
        assertThat(policy.check(URI.create("https://evil.test/"), Profile.LAN_ALLOWED)).isInstanceOf(Denied.class);
        assertThat(policy.check(URI.create("https://x.bad.test/"), Profile.LAN_ALLOWED)).isInstanceOf(Denied.class);
        Denied byCidr = (Denied) policy.check(URI.create("https://bad.test/"), Profile.LAN_ALLOWED);
        assertThat(byCidr.reason()).contains("outbound.denyCidrs");
        assertThat(policy.check(URI.create("https://dns.test/"), Profile.LAN_ALLOWED)).isInstanceOf(Denied.class);
        assertThat(policy.check(URI.create("https://8.8.8.20/"), Profile.PUBLIC_ONLY)).isInstanceOf(Allowed.class);
        assertThat(policy.checkHost("1.1.1.1", Profile.PUBLIC_ONLY)).isInstanceOf(Allowed.class);
    }

    @Test
    @DisplayName("allowHosts and allowCidrs lift the profile restriction for exactly what they name")
    void allows() {
        answer("nas.lan", "192.168.1.5");
        answer("other.lan", "10.0.0.1");
        answer("both.lan", "192.168.1.5", "10.0.0.1");
        answer("box.home.arpa", "10.9.9.9");
        overrides = new OutboundSettings(List.of("nas.lan", "*.home.arpa"), List.of("192.168.0.0/16"), List.of(),
                List.of());
        assertThat(policy.check(URI.create("https://nas.lan/"), Profile.PUBLIC_ONLY)).isInstanceOf(Allowed.class);
        assertThat(policy.check(URI.create("https://box.home.arpa/"), Profile.PUBLIC_ONLY)).isInstanceOf(Allowed.class);
        assertThat(policy.check(URI.create("https://other.lan/"), Profile.PUBLIC_ONLY)).isInstanceOf(Denied.class);
        assertThat(policy.check(URI.create("https://both.lan/"), Profile.PUBLIC_ONLY))
                .as("every address must be allowed").isInstanceOf(Denied.class);
        assertThat(policy.check(URI.create("https://192.168.7.7/"), Profile.PUSH_ENDPOINT)).isInstanceOf(Allowed.class);
        answer("pub.test", "8.8.8.8");
        assertThat(policy.check(URI.create("http://pub.test/"), Profile.LAN_ONLY)).isInstanceOf(Denied.class);
        overrides = new OutboundSettings(List.of(), List.of("8.8.8.0/24"), List.of(), List.of());
        policy.invalidate();
        assertThat(policy.check(URI.create("http://pub.test/"), Profile.LAN_ONLY)).isInstanceOf(Allowed.class);
    }

    @Test
    @DisplayName("push.allowedPrivateHosts applies to PUSH_ENDPOINT only, by exact host or CIDR")
    void pushAllowedHosts() {
        answer("distributor.lan", "192.168.1.20");
        answer("other.lan", "10.1.1.1");
        answer("sub.distributor.lan", "192.168.1.21");
        pushAllowed = List.of("distributor.lan", "10.0.0.0/8", "fd12::7");
        assertThat(policy.check(URI.create("https://distributor.lan/p"), Profile.PUSH_ENDPOINT)).isInstanceOf(Allowed.class);
        assertThat(policy.check(URI.create("https://other.lan/p"), Profile.PUSH_ENDPOINT)).isInstanceOf(Allowed.class);
        assertThat(policy.check(URI.create("https://[fd12::7]/p"), Profile.PUSH_ENDPOINT)).isInstanceOf(Allowed.class);
        assertThat(policy.check(URI.create("https://sub.distributor.lan/p"), Profile.PUSH_ENDPOINT))
                .isInstanceOf(Denied.class);
        assertThat(policy.check(URI.create("https://distributor.lan/p"), Profile.PUBLIC_ONLY)).isInstanceOf(Denied.class);
        Denied hint = (Denied) policy.check(URI.create("https://192.168.9.9/p"), Profile.PUSH_ENDPOINT);
        assertThat(hint.reason()).contains("push.allowedPrivateHosts");
    }

    @Test
    @DisplayName("overrides are cached and reloaded after an outbound or server settings change")
    void cacheInvalidation() {
        answer("nas.lan", "192.168.1.5");
        assertThat(policy.check(URI.create("https://nas.lan/"), Profile.PUBLIC_ONLY)).isInstanceOf(Denied.class);
        overrides = new OutboundSettings(List.of("nas.lan"), List.of(), List.of(), List.of());
        assertThat(policy.check(URI.create("https://nas.lan/"), Profile.PUBLIC_ONLY))
                .as("still the cached overrides").isInstanceOf(Denied.class);
        policy.onSettingChanged(new AppSettingChangedEvent("retention"));
        assertThat(policy.check(URI.create("https://nas.lan/"), Profile.PUBLIC_ONLY)).isInstanceOf(Denied.class);
        policy.onSettingChanged(new AppSettingChangedEvent("outbound"));
        assertThat(policy.check(URI.create("https://nas.lan/"), Profile.PUBLIC_ONLY)).isInstanceOf(Allowed.class);
        int reads = overrideReads.get();
        policy.check(URI.create("https://nas.lan/"), Profile.PUBLIC_ONLY);
        assertThat(overrideReads).hasValue(reads);
        policy.onSettingChanged(new AppSettingChangedEvent("server"));
        policy.check(URI.create("https://nas.lan/"), Profile.PUBLIC_ONLY);
        assertThat(overrideReads).hasValue(reads + 1);
    }

    @Test
    @DisplayName("unreadable overrides: the profiles alone decide, and the read is retried")
    void unreadableOverrides() {
        AtomicInteger calls = new AtomicInteger();
        OutboundUrlPolicy failing = new OutboundUrlPolicy(resolver, () -> {
            calls.incrementAndGet();
            throw new IllegalStateException("database down");
        }, List::of, Optional.empty());
        assertThat(failing.checkHost("8.8.8.8", Profile.PUBLIC_ONLY)).isInstanceOf(Allowed.class);
        assertThat(failing.checkHost("10.0.0.1", Profile.PUBLIC_ONLY)).isInstanceOf(Denied.class);
        assertThat(calls).hasValue(2);
    }

    // ---------- proxy, redirects, save helpers ----------

    @Test
    @DisplayName("with an HTTP proxy configured, LAN_ONLY is refused and the other profiles still check")
    void proxy() {
        OutboundUrlPolicy proxied = new OutboundUrlPolicy(resolver, OutboundSettings::defaults, List::of,
                Optional.of("environment variable HTTPS_PROXY"));
        Denied denied = (Denied) proxied.check(URI.create("http://192.168.1.5/"), Profile.LAN_ONLY);
        assertThat(denied.reason()).contains("proxy").contains("HTTPS_PROXY");
        assertThat(proxied.check(URI.create("http://192.168.1.5/"), Profile.LAN_ALLOWED)).isInstanceOf(Allowed.class);
        assertThat(proxied.check(URI.create("http://169.254.169.254/"), Profile.LAN_ALLOWED)).isInstanceOf(Denied.class);
    }

    @Test
    @DisplayName("proxy detection: JVM properties, useSystemProxies and the environment, named without values")
    void proxyDetection() {
        Properties none = new Properties();
        assertThat(OutboundProxies.configured(Map.of(), none)).isEmpty();
        Properties jvm = new Properties();
        jvm.setProperty("https.proxyHost", "proxy.lan");
        assertThat(OutboundProxies.configured(Map.of(), jvm)).contains("system property https.proxyHost");
        Properties system = new Properties();
        system.setProperty("java.net.useSystemProxies", "true");
        assertThat(OutboundProxies.configured(Map.of(), system))
                .hasValueSatisfying(v -> assertThat(v).contains("useSystemProxies"));
        assertThat(OutboundProxies.configured(Map.of("https_proxy", "http://u:p@proxy:3128"), none))
                .contains("environment variable https_proxy");
        assertThat(OutboundProxies.configured(Map.of("HTTP_PROXY", " "), none)).isEmpty();
    }

    @Test
    @DisplayName("redirect hops are re-checked and limited to three")
    void redirects() {
        answer("a.test", "8.8.8.8");
        answer("b.test", "1.1.1.1");
        URI current = URI.create("https://a.test/start");
        assertThat(policy.checkRedirect(current, "/next", Profile.PUBLIC_ONLY, false, 0)).isInstanceOf(Allowed.class);
        assertThat(policy.checkRedirect(current, "https://b.test/x", Profile.PUBLIC_ONLY, false, 2))
                .isInstanceOf(Allowed.class);
        assertThat(policy.checkRedirect(current, "https://b.test/x", Profile.PUBLIC_ONLY, false,
                OutboundUrlPolicy.MAX_REDIRECTS)).isInstanceOf(Denied.class);
        assertThat(policy.checkRedirect(current, "http://169.254.169.254/latest/meta-data", Profile.LAN_ALLOWED, false, 0))
                .isInstanceOf(Denied.class);
        assertThat(policy.checkRedirect(current, "http://b.test/x", Profile.PUBLIC_ONLY, false, 0))
                .as("a downgrade to http").isInstanceOf(Denied.class);
        assertThat(policy.checkRedirect(current, null, Profile.PUBLIC_ONLY, false, 0)).isInstanceOf(Denied.class);
        assertThat(policy.checkRedirect(current, "ht tp://bad", Profile.PUBLIC_ONLY, false, 0)).isInstanceOf(Denied.class);
    }

    @Test
    @DisplayName("requireSavable throws 400 URL_NOT_ALLOWED on the field")
    void requireSavable() {
        assertThatThrownBy(() -> policy.requireSavable("config.url", "http://169.254.169.254/", Profile.LAN_ALLOWED,
                false))
                .isInstanceOfSatisfying(ValidationException.class, e -> {
                    assertThat(e.getCode()).isEqualTo("URL_NOT_ALLOWED");
                    assertThat(e.getStatus().value()).isEqualTo(400);
                    assertThat(e.getErrors()).singleElement().satisfies(f -> assertThat(f.field()).isEqualTo("config.url"));
                });
        policy.requireSavable("config.url", "http://10.0.0.1/", Profile.LAN_ALLOWED, false);
    }

    @Test
    @DisplayName("withoutOverrides and the profile flags")
    void factoriesAndFlags() {
        OutboundUrlPolicy plain = OutboundUrlPolicy.withoutOverrides(resolver);
        assertThat(plain.checkHost("10.0.0.1", Profile.LAN_ALLOWED)).isInstanceOf(Allowed.class);
        Set<Profile> lan = EnumSet.noneOf(Profile.class);
        for (Profile profile : Profile.values()) {
            if (profile.lan()) {
                lan.add(profile);
            }
        }
        assertThat(lan).containsExactlyInAnyOrder(Profile.LAN_ALLOWED, Profile.LAN_ONLY);
        assertThat(Profile.LAN_ONLY.permits(AddressClass.SHARED)).isFalse();
        assertThat(Profile.LAN_ALLOWED.permits(AddressClass.SHARED)).isTrue();
        assertThat(Profile.PUBLIC_ONLY.permits(AddressClass.BLOCKED)).isFalse();
    }

    @Nested
    @DisplayName("the send-time exception")
    class DeniedException {

        @Test
        @DisplayName("require passes an Allowed through and turns a Denied into URL_NOT_ALLOWED")
        void require() throws Exception {
            Allowed allowed = new Allowed(List.of(ip("8.8.8.8")));
            assertThat(OutboundUrlDeniedException.require(allowed)).isSameAs(allowed);
            assertThatThrownBy(() -> OutboundUrlDeniedException.require(new Denied("the host x is refused")))
                    .isInstanceOfSatisfying(OutboundUrlDeniedException.class, e -> {
                        assertThat(e.getMessage()).isEqualTo("URL_NOT_ALLOWED: the host x is refused");
                        assertThat(e.reason()).isEqualTo("the host x is refused");
                    });
        }
    }
}
