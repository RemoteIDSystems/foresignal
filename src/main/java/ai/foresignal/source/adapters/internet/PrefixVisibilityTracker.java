package ai.foresignal.source.adapters.internet;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Peer-visibility tracking for one watched BGP prefix (prompt 092, {@code ris-live}), pure and testable.
 *
 * <p>For the watched prefix it keeps, per {@code (host, peer)}, the last announced path or a withdrawal. A peer
 * counts toward the denominator when it was seen for this prefix in the last {@link #RECENT} and is not reported
 * down by a {@code RIS_PEER_STATE} message. The snapshot carries the banded visibility, the origin of each path
 * (its <em>last</em> AS; an AS_SET in that position is flattened and each member flagged), whether an origin is
 * unexpected, and the more-specific prefixes currently announced under the watched one (the subscription asks for
 * them with {@code moreSpecific: true}).
 *
 * <p>While the stream is down the snapshot keeps the last visibility and reports {@code status: no-data}: an
 * outage of the monitor is not an outage of the prefix.
 */
public final class PrefixVisibilityTracker {

    /** How long a peer counts after it was last seen. */
    public static final Duration RECENT = Duration.ofHours(24);

    /** The most more-specific prefixes a snapshot lists. */
    public static final int MAX_MORE_SPECIFICS = 20;

    /** The live status. */
    public static final String STATUS_LIVE = "live";

    /** The status while the stream is disconnected. */
    public static final String STATUS_NO_DATA = "no-data";

    /** One path origin. */
    public record Origin(int asn, boolean asSet) {}

    /** A derived snapshot of one prefix. */
    public record Snapshot(double visibility, List<Origin> origins, boolean unexpectedOrigin,
            List<String> moreSpecifics, String status) {}

    private record Peer(String host, String peer) {}

    private record Seen(String path, boolean announced, Instant at) {}

    private final String prefix;
    private final Map<Peer, Seen> peers = new LinkedHashMap<>();
    private final Set<Peer> down = new LinkedHashSet<>();
    private final Map<String, Map<Peer, Instant>> moreSpecifics = new LinkedHashMap<>();

    /** Creates a tracker for one watched prefix. */
    public PrefixVisibilityTracker(String prefix) {
        this.prefix = prefix;
    }

    /** The watched prefix. */
    public String prefix() {
        return prefix;
    }

    /** Records an announcement of the watched prefix from {@code (host, peer)} with {@code path}. */
    public void announce(String host, String peer, String path, Instant at) {
        Peer key = new Peer(host, normalize(peer));
        down.remove(key);
        peers.put(key, new Seen(path, true, at));
    }

    /** Records a withdrawal of the watched prefix from {@code (host, peer)}. */
    public void withdraw(String host, String peer, Instant at) {
        Peer key = new Peer(host, normalize(peer));
        down.remove(key);
        peers.put(key, new Seen(null, false, at));
    }

    /** Records an announcement of {@code moreSpecific}, a prefix inside the watched one, from {@code (host, peer)}. */
    public void announceMoreSpecific(String moreSpecific, String host, String peer, Instant at) {
        moreSpecifics.computeIfAbsent(moreSpecific, k -> new LinkedHashMap<>()).put(new Peer(host, normalize(peer)), at);
    }

    /** Records a withdrawal of {@code moreSpecific} from {@code (host, peer)}. */
    public void withdrawMoreSpecific(String moreSpecific, String host, String peer) {
        Map<Peer, Instant> announcing = moreSpecifics.get(moreSpecific);
        if (announcing != null) {
            announcing.remove(new Peer(host, normalize(peer)));
            if (announcing.isEmpty()) {
                moreSpecifics.remove(moreSpecific);
            }
        }
    }

    /**
     * Records a peer going down or coming back up. A peer that never carried this prefix is not added to the
     * denominator; one that did leaves it while down.
     */
    public void peerState(String host, String peer, boolean isDown, Instant at) {
        Peer key = new Peer(host, normalize(peer));
        if (isDown) {
            down.add(key);
            for (Map<Peer, Instant> announcing : moreSpecifics.values()) {
                announcing.remove(key);
            }
            moreSpecifics.values().removeIf(Map::isEmpty);
        } else {
            down.remove(key);
        }
    }

    private static String normalize(String peer) {
        return peer == null ? "" : peer.strip();
    }

    /**
     * The snapshot at {@code now}.
     *
     * @param expectedOrigins the expected origin ASNs, or null/empty when unknown
     * @param disconnected true when the stream is down; the status is then {@code no-data} and the visibility is
     *     the last one computed from the peers already seen
     */
    public Snapshot snapshot(Set<Integer> expectedOrigins, Instant now, boolean disconnected) {
        int known = 0;
        int announcing = 0;
        List<Origin> origins = new ArrayList<>();
        boolean unexpected = false;
        boolean checkOrigins = expectedOrigins != null && !expectedOrigins.isEmpty();
        for (Map.Entry<Peer, Seen> entry : peers.entrySet()) {
            Seen seen = entry.getValue();
            if (down.contains(entry.getKey()) || seen.at() == null || seen.at().isBefore(now.minus(RECENT))) {
                continue;
            }
            known++;
            if (!seen.announced() || seen.path() == null || seen.path().isBlank()) {
                continue;
            }
            announcing++;
            List<Origin> pathOrigins = originsOf(seen.path());
            origins.addAll(pathOrigins);
            if (checkOrigins && !pathOrigins.isEmpty()
                    && pathOrigins.stream().noneMatch(o -> expectedOrigins.contains(o.asn()))) {
                unexpected = true;
            }
        }
        origins = new ArrayList<>(new LinkedHashSet<>(origins));
        double visibility = known == 0 ? 0.0 : Math.round(10.0 * announcing / known) / 10.0;
        return new Snapshot(visibility, origins, unexpected, currentMoreSpecifics(now),
                disconnected ? STATUS_NO_DATA : STATUS_LIVE);
    }

    private List<String> currentMoreSpecifics(Instant now) {
        List<String> out = new ArrayList<>();
        Instant cutoff = now.minus(RECENT);
        for (Map.Entry<String, Map<Peer, Instant>> entry : moreSpecifics.entrySet()) {
            boolean live = entry.getValue().entrySet().stream()
                    .anyMatch(e -> !down.contains(e.getKey()) && e.getValue() != null && !e.getValue().isBefore(cutoff));
            if (live) {
                out.add(entry.getKey());
            }
        }
        Collections.sort(out);
        return out.size() > MAX_MORE_SPECIFICS ? new ArrayList<>(out.subList(0, MAX_MORE_SPECIFICS)) : out;
    }

    /** The set of origin ASNs currently announced (for change tests). */
    public Set<Integer> announcedOrigins() {
        Set<Integer> out = new TreeSet<>();
        for (Seen seen : peers.values()) {
            if (seen.announced() && seen.path() != null) {
                for (Origin origin : originsOf(seen.path())) {
                    out.add(origin.asn());
                }
            }
        }
        return out;
    }

    /**
     * The origin of a space-separated AS path: its last element. A trailing AS_SET ({@code {64501,64502}}) yields
     * each member, flagged; an AS_SET earlier in the path is transit and ignored.
     */
    static List<Origin> originsOf(String path) {
        List<Origin> out = new ArrayList<>();
        if (path == null || path.isBlank()) {
            return out;
        }
        String[] tokens = path.strip().split("\\s+");
        String last = tokens[tokens.length - 1];
        boolean asSet = last.contains("{") || last.contains("}");
        String clean = last.replace("{", "").replace("}", "").strip();
        for (String member : clean.split(",")) {
            if (member.isBlank()) {
                continue;
            }
            try {
                out.add(new Origin(Integer.parseInt(member.strip()), asSet));
            } catch (NumberFormatException ignored) {
                // an unparsable member is skipped
            }
        }
        return out;
    }
}
