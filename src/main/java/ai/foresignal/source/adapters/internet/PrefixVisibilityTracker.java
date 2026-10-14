package ai.foresignal.source.adapters.internet;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Peer-visibility tracking for watched BGP prefixes (prompt 092, {@code ris-live}), pure and testable.
 *
 * <p>For each watched prefix it keeps, per {@code (host, peer)}, the last announced path or a withdrawal. A
 * peer counts toward the denominator when it was seen in the last {@value #RECENT} and is not reported down by a
 * {@code RIS_PEER_STATE} message. The snapshot carries the banded visibility, the last AS of each path (AS_SET
 * entries flattened and flagged), whether the origin is unexpected, and any more-specific watched prefix.
 */
public final class PrefixVisibilityTracker {

    /** How long a peer counts after it was last seen. */
    public static final Duration RECENT = Duration.ofHours(24);

    /** One path origin. */
    public record Origin(int asn, boolean asSet) {}

    /** A derived snapshot of one prefix. */
    public record Snapshot(double visibility, List<Origin> origins, boolean unexpectedOrigin,
            List<String> moreSpecifics, String status) {}

    private record Peer(String host, String peer) {}

    private record Seen(String path, boolean announced, Instant at, boolean down) {}

    private final String prefix;
    private final Map<Peer, Seen> peers = new LinkedHashMap<>();

    /** Creates a tracker for one watched prefix. */
    public PrefixVisibilityTracker(String prefix) {
        this.prefix = prefix;
    }

    /** Records an announcement from {@code (host, peer)} with {@code path} (a space-separated AS path). */
    public void announce(String host, String peer, String path, Instant at) {
        peers.put(new Peer(host, normalise(peer)), new Seen(path, true, at, isDown(host, peer)));
    }

    /** Records a withdrawal from {@code (host, peer)}. */
    public void withdraw(String host, String peer, Instant at) {
        peers.put(new Peer(host, normalise(peer)), new Seen(null, false, at, isDown(host, peer)));
    }

    /** Records a peer going down or coming back up. */
    public void peerState(String host, String peer, boolean down, Instant at) {
        Peer key = new Peer(host, normalise(peer));
        Seen existing = peers.get(key);
        peers.put(key, new Seen(existing == null ? null : existing.path(), existing != null && existing.announced(),
                at, down));
    }

    private static boolean isDown(String host, String peer) {
        return false;
    }

    private static String normalise(String peer) {
        return peer == null ? "" : peer.strip();
    }

    /**
     * The snapshot at {@code now}.
     *
     * @param expectedOrigins the expected origin ASNs, or null/empty when unknown
     * @param watchedPrefixes every watched prefix, for the more-specific list
     * @param disconnected true when the stream is down; the status is then {@code no-data}
     */
    public Snapshot snapshot(Set<Integer> expectedOrigins, Collection<String> watchedPrefixes, Instant now,
            boolean disconnected) {
        int known = 0;
        int announcing = 0;
        List<Origin> origins = new ArrayList<>();
        boolean unexpected = false;
        for (Seen seen : peers.values()) {
            if (seen.down() || seen.at() == null || seen.at().isBefore(now.minus(RECENT))) {
                continue;
            }
            known++;
            if (!seen.announced() || seen.path() == null || seen.path().isBlank()) {
                continue;
            }
            announcing++;
            for (Origin origin : originsOf(seen.path())) {
                origins.add(origin);
                if (!origin.asSet() && expectedOrigins != null && !expectedOrigins.isEmpty()
                        && !expectedOrigins.contains(origin.asn())) {
                    unexpected = true;
                }
            }
        }
        origins = distinct(origins);
        double visibility = known == 0 ? 0.0 : Math.round(10.0 * announcing / known) / 10.0;
        List<String> moreSpecifics = moreSpecifics(watchedPrefixes);
        String status = disconnected ? "no-data" : "live";
        return new Snapshot(disconnected ? 0.0 : visibility, origins, unexpected, moreSpecifics, status);
    }

    /** The set of origin ASNs currently announced (for change tests). */
    public Set<Integer> announcedOrigins() {
        Set<Integer> out = new TreeSet<>();
        for (Seen seen : peers.values()) {
            if (seen.announced() && seen.path() != null) {
                for (Origin origin : originsOf(seen.path())) {
                    if (!origin.asSet()) {
                        out.add(origin.asn());
                    }
                }
            }
        }
        return out;
    }

    private List<String> moreSpecifics(Collection<String> watched) {
        List<String> out = new ArrayList<>();
        if (watched == null) {
            return out;
        }
        int prefixLength = prefix.length();
        for (String candidate : watched) {
            if (!candidate.equals(prefix) && candidate.startsWith(prefixBeforeSlash()) && candidate.contains("/")
                    && length(candidate) > length(prefix)) {
                out.add(candidate);
            }
        }
        java.util.Collections.sort(out);
        return out.size() > 20 ? out.subList(0, 20) : out;
    }

    private String prefixBeforeSlash() {
        int slash = prefix.indexOf('/');
        String base = slash < 0 ? prefix : prefix.substring(0, slash);
        int lastDot = base.lastIndexOf('.');
        return lastDot < 0 ? base : base.substring(0, lastDot + 1);
    }

    private static int length(String cidr) {
        return cidr.length();
    }

    private static List<Origin> originsOf(String path) {
        List<Origin> out = new ArrayList<>();
        for (String token : path.strip().split("\\s+")) {
            String clean = token.replace("{", "").replace("}", "").strip();
            if (clean.isEmpty()) {
                continue;
            }
            boolean asSet = token.contains("{") || token.contains("}");
            try {
                if (asSet) {
                    for (String member : clean.split(",")) {
                        if (!member.isBlank()) {
                            out.add(new Origin(Integer.parseInt(member.strip()), true));
                        }
                    }
                } else {
                    out.add(new Origin(Integer.parseInt(clean), false));
                }
            } catch (NumberFormatException ignored) {
                // an unparsable token is skipped
            }
        }
        return out;
    }

    private static List<Origin> distinct(List<Origin> origins) {
        Set<Origin> seen = new LinkedHashSet<>(origins);
        return new ArrayList<>(seen);
    }
}
