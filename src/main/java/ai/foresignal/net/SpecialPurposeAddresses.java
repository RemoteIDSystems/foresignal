package ai.foresignal.net;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;

/**
 * The address classification behind {@link OutboundUrlPolicy} (prompt 035): the IANA IPv4 and IPv6
 * Special-Purpose Address Registries (RFC 6890 as updated by RFC 8190), plus multicast, the cloud
 * metadata addresses and the IPv6 space outside global unicast, encoded as one table.
 *
 * <p>Every entry carries the registry's name and reference. The class an entry gets follows the
 * registry's flags:
 *
 * <ul>
 *   <li>{@link AddressClass#BLOCKED}: "Forwardable: False" entries, reserved and deprecated blocks,
 *       documentation ranges, link-local, multicast, limited broadcast and cloud metadata. Refused in
 *       every profile, and no override lifts them.
 *   <li>{@link AddressClass#LOOPBACK}, {@link AddressClass#PRIVATE} and {@link AddressClass#SHARED}:
 *       the ranges a LAN profile may reach (loopback is "Forwardable: False" in the registry, but a
 *       local Ollama or receiver on the same host is the point of the LAN profiles).
 *   <li>{@link AddressClass#NON_GLOBAL}: forwardable but "Globally Reachable: False" (benchmarking,
 *       local-use NAT64, discard-only, SRv6 SIDs). Refused unless an operator override allows them.
 *   <li>{@link AddressClass#GLOBAL}: special-purpose entries that the registry marks globally
 *       reachable (anycast services, AS112), treated like any public address.
 *   <li>{@link AddressClass#EMBEDDED}: IPv6 forms carrying an IPv4 address (IPv4-mapped,
 *       IPv4-compatible, NAT64, 6to4, Teredo). The policy extracts the IPv4 address and judges it.
 * </ul>
 *
 * <p>The most specific matching entry wins, so {@code 100.100.100.200/32} (metadata) beats
 * {@code 100.64.0.0/10} (shared) and {@code 192.0.0.9/32} (PCP anycast, global) beats
 * {@code 192.0.0.0/24} (IETF protocol assignments, not forwardable). An IPv4 address that matches no
 * entry is public. An IPv6 address that matches no entry is public only inside global unicast
 * {@code 2000::/3}; the rest of the IPv6 space is reserved by the IETF.
 */
public final class SpecialPurposeAddresses {

    /** How an address is treated by the profiles. */
    public enum AddressClass {
        /** Publicly routable. */
        GLOBAL("a public address"),
        /** Loopback. */
        LOOPBACK("a loopback address"),
        /** Private use (RFC 1918) or unique local (fc00::/7). */
        PRIVATE("a private-network address"),
        /** Shared address space: carrier-grade NAT and Tailscale (100.64.0.0/10). */
        SHARED("a shared (carrier-grade NAT or tailnet) address"),
        /** Forwardable but not globally reachable. */
        NON_GLOBAL("a non-global special-purpose address"),
        /** Refused everywhere. */
        BLOCKED("a blocked address"),
        /** An IPv6 address embedding an IPv4 address, judged by that address. */
        EMBEDDED("an IPv6 address embedding IPv4");

        private final String description;

        AddressClass(String description) {
            this.description = description;
        }

        /** A phrase such as "a loopback address", for messages. */
        public String description() {
            return description;
        }
    }

    /** How an {@link AddressClass#EMBEDDED} address carries its IPv4 address. */
    public enum Embedding {
        /** Not embedded. */
        NONE,
        /** IPv4-mapped {@code ::ffff:0:0/96} (RFC 4291). */
        MAPPED,
        /** IPv4-compatible {@code ::/96} (RFC 4291, deprecated). */
        COMPATIBLE,
        /** IPv4/IPv6 translation {@code 64:ff9b::/96} (RFC 6052). */
        NAT64,
        /** 6to4 {@code 2002::/16} (RFC 3056): bits 16–47. */
        SIX_TO_FOUR,
        /** Teredo {@code 2001::/32} (RFC 4380): the last 32 bits, inverted, are the client. */
        TEREDO
    }

    /**
     * One table entry.
     *
     * @param block the address block
     * @param name the registry name (or ours, for entries outside the special-purpose registries)
     * @param reference the defining RFC
     * @param addressClass how the profiles treat it
     * @param embedding how it carries an IPv4 address, for {@link AddressClass#EMBEDDED}
     */
    public record Entry(Cidr block, String name, String reference, AddressClass addressClass, Embedding embedding) {

        /** "the Link Local block 169.254.0.0/16 (RFC 3927)", for messages. */
        public String describe() {
            return name + " " + block + " (" + reference + ")";
        }
    }

    /**
     * The classification of one address.
     *
     * @param addressClass the class
     * @param entry the most specific table entry, or null when no entry matched
     */
    public record Classification(AddressClass addressClass, Entry entry) {

        /** A phrase for messages: the entry when there is one, else the class. */
        public String describe() {
            return entry == null ? addressClass.description() : addressClass.description() + ", " + entry.describe();
        }
    }

    /** Global unicast IPv6; anything else that matches no entry is reserved by the IETF. */
    static final Cidr IPV6_GLOBAL_UNICAST = cidr("2000::/3");

    /**
     * The table. Comments give the registry's "Forwardable" / "Globally Reachable" flags (F / G) as
     * of RFC 8190 and the later registry updates; "n/a" where the registry gives none.
     */
    public static final List<Entry> TABLE = List.of(
            // ---------- IPv4 Special-Purpose Address Registry ----------
            // "This network" and "this host on this network". F false, G false.
            entry("0.0.0.0/8", "This network", "RFC 791", AddressClass.BLOCKED),
            // Private-Use. F true, G false.
            entry("10.0.0.0/8", "Private-Use", "RFC 1918", AddressClass.PRIVATE),
            // Shared Address Space (carrier-grade NAT; Tailscale uses it). F true, G false.
            entry("100.64.0.0/10", "Shared Address Space", "RFC 6598", AddressClass.SHARED),
            // Loopback. F false, G false; allowed by the LAN profiles.
            entry("127.0.0.0/8", "Loopback", "RFC 1122", AddressClass.LOOPBACK),
            // Link Local. F false, G false. Always blocked (cloud metadata lives here).
            entry("169.254.0.0/16", "Link Local", "RFC 3927", AddressClass.BLOCKED),
            // Private-Use. F true, G false.
            entry("172.16.0.0/12", "Private-Use", "RFC 1918", AddressClass.PRIVATE),
            // IETF Protocol Assignments. F false, G false (more specific entries below).
            entry("192.0.0.0/24", "IETF Protocol Assignments", "RFC 6890", AddressClass.BLOCKED),
            // IPv4 Service Continuity Prefix (DS-Lite). F true, G false.
            entry("192.0.0.0/29", "IPv4 Service Continuity Prefix", "RFC 7335", AddressClass.NON_GLOBAL),
            // IPv4 dummy address. F false, G false.
            entry("192.0.0.8/32", "IPv4 dummy address", "RFC 7600", AddressClass.BLOCKED),
            // Port Control Protocol Anycast. F true, G true.
            entry("192.0.0.9/32", "Port Control Protocol Anycast", "RFC 7723", AddressClass.GLOBAL),
            // Traversal Using Relays around NAT Anycast. F true, G true.
            entry("192.0.0.10/32", "TURN Anycast", "RFC 8155", AddressClass.GLOBAL),
            // NAT64/DNS64 Discovery. F false, G false.
            entry("192.0.0.170/32", "NAT64/DNS64 Discovery", "RFC 8880", AddressClass.BLOCKED),
            entry("192.0.0.171/32", "NAT64/DNS64 Discovery", "RFC 8880", AddressClass.BLOCKED),
            // Documentation (TEST-NET-1). F false, G false.
            entry("192.0.2.0/24", "Documentation (TEST-NET-1)", "RFC 5737", AddressClass.BLOCKED),
            // AS112-v4. F true, G true.
            entry("192.31.196.0/24", "AS112-v4", "RFC 7535", AddressClass.GLOBAL),
            // AMT. F true, G true.
            entry("192.52.193.0/24", "AMT", "RFC 7450", AddressClass.GLOBAL),
            // Deprecated (6to4 Relay Anycast). Retired by RFC 7526; treated as reserved.
            entry("192.88.99.0/24", "Deprecated (6to4 Relay Anycast)", "RFC 7526", AddressClass.BLOCKED),
            // Private-Use. F true, G false.
            entry("192.168.0.0/16", "Private-Use", "RFC 1918", AddressClass.PRIVATE),
            // Direct Delegation AS112 Service. F true, G true.
            entry("192.175.48.0/24", "Direct Delegation AS112 Service", "RFC 7534", AddressClass.GLOBAL),
            // Benchmarking. F true, G false.
            entry("198.18.0.0/15", "Benchmarking", "RFC 2544", AddressClass.NON_GLOBAL),
            // Documentation (TEST-NET-2). F false, G false.
            entry("198.51.100.0/24", "Documentation (TEST-NET-2)", "RFC 5737", AddressClass.BLOCKED),
            // Documentation (TEST-NET-3). F false, G false.
            entry("203.0.113.0/24", "Documentation (TEST-NET-3)", "RFC 5737", AddressClass.BLOCKED),
            // Reserved. F false, G false.
            entry("240.0.0.0/4", "Reserved", "RFC 1112", AddressClass.BLOCKED),
            // Limited Broadcast. F false, G false.
            entry("255.255.255.255/32", "Limited Broadcast", "RFC 8190", AddressClass.BLOCKED),
            // ---------- IPv4 outside the special-purpose registry ----------
            // Multicast (IPv4 Multicast Address Space Registry).
            entry("224.0.0.0/4", "Multicast", "RFC 5771", AddressClass.BLOCKED),
            // Cloud metadata: AWS, GCP, Azure, OpenStack (inside link-local, listed for the message).
            entry("169.254.169.254/32", "Cloud metadata", "AWS/GCP/Azure", AddressClass.BLOCKED),
            // AWS ECS task metadata (inside link-local).
            entry("169.254.170.2/32", "Cloud metadata", "AWS ECS", AddressClass.BLOCKED),
            // Alibaba Cloud metadata, inside shared address space: more specific, so blocked.
            entry("100.100.100.200/32", "Cloud metadata", "Alibaba Cloud", AddressClass.BLOCKED),

            // ---------- IPv6 Special-Purpose Address Registry ----------
            // Unspecified Address. F false, G false.
            entry("::/128", "Unspecified Address", "RFC 4291", AddressClass.BLOCKED),
            // Loopback Address. F false, G false; allowed by the LAN profiles.
            entry("::1/128", "Loopback Address", "RFC 4291", AddressClass.LOOPBACK),
            // IPv4-compatible (deprecated; not in today's registry but still parsed by stacks).
            embedded("::/96", "IPv4-compatible IPv6 (deprecated)", "RFC 4291", Embedding.COMPATIBLE),
            // IPv4-IPv6 Translation (NAT64 well-known prefix). F true, G true: judged by the IPv4 address.
            embedded("64:ff9b::/96", "IPv4-IPv6 Translation", "RFC 6052", Embedding.NAT64),
            // Local-Use IPv4/IPv6 Translation. F true, G false.
            entry("64:ff9b:1::/48", "Local-Use IPv4/IPv6 Translation", "RFC 8215", AddressClass.NON_GLOBAL),
            // Discard-Only Address Block. F true, G false.
            entry("100::/64", "Discard-Only Address Block", "RFC 6666", AddressClass.NON_GLOBAL),
            // Dummy IPv6 Prefix. F false, G false.
            entry("100:0:0:1::/64", "Dummy IPv6 Prefix", "RFC 9780", AddressClass.BLOCKED),
            // IETF Protocol Assignments. F false, G false (more specific entries below).
            entry("2001::/23", "IETF Protocol Assignments", "RFC 2928", AddressClass.BLOCKED),
            // TEREDO. F true, G n/a: judged by the (obfuscated) client IPv4 address.
            embedded("2001::/32", "TEREDO", "RFC 4380", Embedding.TEREDO),
            // Port Control Protocol Anycast. F true, G true.
            entry("2001:1::1/128", "Port Control Protocol Anycast", "RFC 7723", AddressClass.GLOBAL),
            // TURN Anycast. F true, G true.
            entry("2001:1::2/128", "TURN Anycast", "RFC 8155", AddressClass.GLOBAL),
            // DNS-SD Service Registration Protocol Anycast. F true, G true.
            entry("2001:1::3/128", "DNS-SD SRP Anycast", "RFC 9665", AddressClass.GLOBAL),
            // Benchmarking. F true, G false.
            entry("2001:2::/48", "Benchmarking", "RFC 5180", AddressClass.NON_GLOBAL),
            // AMT. F true, G true.
            entry("2001:3::/32", "AMT", "RFC 7450", AddressClass.GLOBAL),
            // AS112-v6. F true, G true.
            entry("2001:4:112::/48", "AS112-v6", "RFC 7535", AddressClass.GLOBAL),
            // Deprecated (previously ORCHID). Treated as reserved.
            entry("2001:10::/28", "Deprecated (previously ORCHID)", "RFC 4843", AddressClass.BLOCKED),
            // ORCHIDv2. F true, G true.
            entry("2001:20::/28", "ORCHIDv2", "RFC 7343", AddressClass.GLOBAL),
            // Drone Remote ID Protocol Entity Tags. F true, G true.
            entry("2001:30::/28", "Drone Remote ID Protocol Entity Tags (DETs) Prefix", "RFC 9374",
                    AddressClass.GLOBAL),
            // Documentation. F false, G false.
            entry("2001:db8::/32", "Documentation", "RFC 3849", AddressClass.BLOCKED),
            // 6to4. F true, G n/a: judged by the IPv4 address in bits 16-47.
            embedded("2002::/16", "6to4", "RFC 3056", Embedding.SIX_TO_FOUR),
            // Direct Delegation AS112 Service. F true, G true.
            entry("2620:4f:8000::/48", "Direct Delegation AS112 Service", "RFC 7534", AddressClass.GLOBAL),
            // Documentation. F false, G false.
            entry("3fff::/20", "Documentation", "RFC 9637", AddressClass.BLOCKED),
            // Segment Routing (SRv6) SIDs. F true, G false.
            entry("5f00::/16", "Segment Routing (SRv6) SIDs", "RFC 9602", AddressClass.NON_GLOBAL),
            // Unique-Local. F true, G false.
            entry("fc00::/7", "Unique-Local", "RFC 4193", AddressClass.PRIVATE),
            // Link-Local Unicast. F false, G false.
            entry("fe80::/10", "Link-Local Unicast", "RFC 4291", AddressClass.BLOCKED),
            // ---------- IPv6 outside the special-purpose registry ----------
            // Site-local (deprecated by RFC 3879).
            entry("fec0::/10", "Site-Local (deprecated)", "RFC 3879", AddressClass.BLOCKED),
            // Multicast.
            entry("ff00::/8", "Multicast", "RFC 4291", AddressClass.BLOCKED),
            // AWS metadata over IPv6 (inside unique-local: more specific, so blocked).
            entry("fd00:ec2::254/128", "Cloud metadata", "AWS", AddressClass.BLOCKED));

    private SpecialPurposeAddresses() {}

    /**
     * Classifies {@code address} by its most specific table entry. An IPv4-mapped IPv6 address is
     * reported as {@link AddressClass#EMBEDDED} with {@link Embedding#MAPPED}.
     */
    public static Classification classify(InetAddress address) {
        if (address instanceof Inet6Address && isIpv4Mapped(address.getAddress())) {
            Entry mapped = new Entry(new Cidr(address, 128), "IPv4-mapped Address", "RFC 4291",
                    AddressClass.EMBEDDED, Embedding.MAPPED);
            return new Classification(AddressClass.EMBEDDED, mapped);
        }
        Entry best = null;
        for (Entry entry : TABLE) {
            if (entry.block().contains(address) && (best == null || entry.block().prefix() > best.block().prefix())) {
                best = entry;
            }
        }
        if (best != null) {
            return new Classification(best.addressClass(), best);
        }
        if (address instanceof Inet6Address && !IPV6_GLOBAL_UNICAST.contains(address)) {
            Entry reserved = new Entry(IPV6_GLOBAL_UNICAST, "Reserved by IETF (outside global unicast)",
                    "RFC 4291", AddressClass.BLOCKED, Embedding.NONE);
            return new Classification(AddressClass.BLOCKED, reserved);
        }
        return new Classification(AddressClass.GLOBAL, null);
    }

    /**
     * The IPv4 address an {@link AddressClass#EMBEDDED} IPv6 address carries: the last 32 bits for
     * mapped, compatible and NAT64; bits 16–47 for 6to4; the inverted last 32 bits (the client) for
     * Teredo.
     */
    public static Optional<Inet4Address> embeddedIpv4(InetAddress address, Embedding embedding) {
        byte[] b = address.getAddress();
        if (b.length != 16 || embedding == null || embedding == Embedding.NONE) {
            return Optional.empty();
        }
        byte[] v4 = switch (embedding) {
            case MAPPED, COMPATIBLE, NAT64 -> new byte[] {b[12], b[13], b[14], b[15]};
            case SIX_TO_FOUR -> new byte[] {b[2], b[3], b[4], b[5]};
            case TEREDO -> new byte[] {(byte) ~b[12], (byte) ~b[13], (byte) ~b[14], (byte) ~b[15]};
            case NONE -> null;
        };
        return v4 == null ? Optional.empty() : Optional.of(ipv4(v4));
    }

    /** The Teredo server IPv4 address (bits 32–63) of a Teredo address. */
    public static Optional<Inet4Address> teredoServer(InetAddress address) {
        byte[] b = address.getAddress();
        if (b.length != 16) {
            return Optional.empty();
        }
        return Optional.of(ipv4(new byte[] {b[4], b[5], b[6], b[7]}));
    }

    /** True for {@code ::ffff:0:0/96} bytes. */
    static boolean isIpv4Mapped(byte[] bytes) {
        if (bytes.length != 16) {
            return false;
        }
        for (int i = 0; i < 10; i++) {
            if (bytes[i] != 0) {
                return false;
            }
        }
        return (bytes[10] & 0xFF) == 0xFF && (bytes[11] & 0xFF) == 0xFF;
    }

    private static Inet4Address ipv4(byte[] bytes) {
        try {
            return (Inet4Address) InetAddress.getByAddress(bytes);
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Entry entry(String block, String name, String reference, AddressClass addressClass) {
        return new Entry(cidr(block), name, reference, addressClass, Embedding.NONE);
    }

    private static Entry embedded(String block, String name, String reference, Embedding embedding) {
        return new Entry(cidr(block), name, reference, AddressClass.EMBEDDED, embedding);
    }

    private static Cidr cidr(String text) {
        return Cidr.parse(text).orElseThrow(() -> new IllegalStateException("bad built-in block " + text));
    }
}
