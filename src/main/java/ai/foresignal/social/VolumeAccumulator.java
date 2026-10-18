package ai.foresignal.social;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory hourly counters of the social volume series (prompt 135), filled by {@link SocialCollector} as posts
 * arrive and drained as <em>deltas</em> by the flush job into {@link SocialVolumeStore}.
 *
 * <p>Deltas make the counts additive across flushes and restarts: posts, reposts, engagement, denominators and
 * coverage are exact; distinct authors are exact within one process lifetime of an hour and may count an author twice
 * when the process restarts inside the hour (the store adds the distinct authors seen since the last flush). The
 * distinct-author set of one bucket holds at most {@value #MAX_AUTHORS} keys; beyond that the count grows by one per
 * post (an upper bound).
 */
public final class VolumeAccumulator {

    /** The most author keys remembered per bucket. */
    public static final int MAX_AUTHORS = 200_000;

    /** How long a fully flushed bucket is kept after its hour ended. */
    static final Duration KEEP = Duration.ofHours(2);

    /**
     * A volume bucket.
     *
     * @param scope the scope key ({@code topic:…}, {@code place:…}, {@code entity:…})
     * @param platform the network
     * @param language the language bucket
     * @param hour the hour start
     * @param collectionType {@code complete}, {@code paged} or {@code sampled}
     */
    public record VolumeKey(String scope, String platform, String language, Instant hour, String collectionType) {}

    /**
     * A denominator bucket.
     *
     * @param platform the network
     * @param language the language bucket
     * @param hour the hour start
     */
    public record DenominatorKey(String platform, String language, Instant hour) {}

    /**
     * A coverage bucket.
     *
     * @param platform the network
     * @param regionCode the 040 region
     * @param hour the hour start
     */
    public record CoverageKey(String platform, String regionCode, Instant hour) {}

    /**
     * What one volume bucket gained since the last drain.
     *
     * @param key the bucket
     * @param posts posts
     * @param uniqueAuthors new distinct authors
     * @param reposts reposts
     * @param engagement engagement
     */
    public record VolumeDelta(VolumeKey key, long posts, long uniqueAuthors, long reposts, long engagement) {}

    /**
     * Everything gained since the last drain.
     *
     * @param volumes volume deltas
     * @param denominators denominator deltas
     * @param coverage coverage deltas
     */
    public record Drain(List<VolumeDelta> volumes, Map<DenominatorKey, Long> denominators,
            Map<CoverageKey, Long> coverage) {

        /** True when nothing was gained. */
        public boolean isEmpty() {
            return volumes.isEmpty() && denominators.isEmpty() && coverage.isEmpty();
        }
    }

    private static final class Counts {
        long posts;
        long reposts;
        long engagement;
        long authorsOverflow;
        final Set<String> authors = new HashSet<>();
        long flushedPosts;
        long flushedReposts;
        long flushedEngagement;
        long flushedAuthors;

        long authorCount() {
            return authors.size() + authorsOverflow;
        }
    }

    private static final class Counter {
        long value;
        long flushed;
    }

    private final Map<VolumeKey, Counts> volumes = new ConcurrentHashMap<>();
    private final Map<DenominatorKey, Counter> denominators = new ConcurrentHashMap<>();
    private final Map<CoverageKey, Counter> coverage = new ConcurrentHashMap<>();

    /** The hour start of an instant. */
    public static Instant hourOf(Instant t) {
        return t.truncatedTo(ChronoUnit.HOURS);
    }

    /**
     * Counts one post into the buckets of its scopes.
     *
     * @param scopes the matched scope keys
     * @param platform the network
     * @param language the language bucket
     * @param at the post time (its creation time)
     * @param collectionType the collection type
     * @param authorKey the author key, or null
     * @param engagement the engagement at capture
     */
    public void post(List<String> scopes, String platform, String language, Instant at, String collectionType,
            String authorKey, long engagement) {
        Instant hour = hourOf(at);
        for (String scope : scopes) {
            Counts c = volumes.computeIfAbsent(new VolumeKey(scope, platform, language, hour, collectionType),
                    k -> new Counts());
            synchronized (c) {
                c.posts++;
                c.engagement += Math.max(0, engagement);
                if (authorKey != null) {
                    if (c.authors.size() < MAX_AUTHORS) {
                        c.authors.add(authorKey);
                    } else if (!c.authors.contains(authorKey)) {
                        c.authorsOverflow++;
                    }
                }
            }
        }
    }

    /**
     * Counts one repost (or quote or reply seen on a stream) of a matched post into its scopes.
     *
     * @param scopes the original post's scope keys
     * @param platform the network
     * @param language the original post's language bucket
     * @param at the repost time
     * @param collectionType the collection type
     * @param repost true for a repost (counted in {@code reposts}), false for a quote or reply
     */
    public void amplification(List<String> scopes, String platform, String language, Instant at,
            String collectionType, boolean repost) {
        Instant hour = hourOf(at);
        for (String scope : scopes) {
            Counts c = volumes.computeIfAbsent(new VolumeKey(scope, platform, language, hour, collectionType),
                    k -> new Counts());
            synchronized (c) {
                if (repost) {
                    c.reposts++;
                }
                c.engagement++;
            }
        }
    }

    /** Counts one post seen on a complete stream into its platform-language denominator. */
    public void denominator(String platform, String language, Instant at) {
        Counter c = denominators.computeIfAbsent(new DenominatorKey(platform, language, hourOf(at)),
                k -> new Counter());
        synchronized (c) {
            c.value++;
        }
    }

    /** Counts one post attributed to a region into the platform's coverage. */
    public void coverage(String platform, String regionCode, Instant at) {
        Counter c = coverage.computeIfAbsent(new CoverageKey(platform, regionCode, hourOf(at)), k -> new Counter());
        synchronized (c) {
            c.value++;
        }
    }

    /**
     * Takes everything gained since the last drain, and forgets buckets that ended more than two hours before
     * {@code now} and have nothing left to flush.
     *
     * @param now the current time
     * @return the deltas
     */
    public Drain drain(Instant now) {
        List<VolumeDelta> v = new ArrayList<>();
        Map<DenominatorKey, Long> d = new java.util.LinkedHashMap<>();
        Map<CoverageKey, Long> cov = new java.util.LinkedHashMap<>();
        Instant cutoff = hourOf(now).minus(KEEP);
        for (Iterator<Map.Entry<VolumeKey, Counts>> it = volumes.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<VolumeKey, Counts> e = it.next();
            Counts c = e.getValue();
            synchronized (c) {
                long authors = c.authorCount();
                long dp = c.posts - c.flushedPosts;
                long da = authors - c.flushedAuthors;
                long dr = c.reposts - c.flushedReposts;
                long de = c.engagement - c.flushedEngagement;
                if (dp != 0 || da != 0 || dr != 0 || de != 0) {
                    v.add(new VolumeDelta(e.getKey(), dp, da, dr, de));
                    c.flushedPosts = c.posts;
                    c.flushedAuthors = authors;
                    c.flushedReposts = c.reposts;
                    c.flushedEngagement = c.engagement;
                } else if (e.getKey().hour().isBefore(cutoff)) {
                    it.remove();
                }
            }
        }
        drainCounters(denominators, d, cutoff, k -> k.hour());
        drainCounters(coverage, cov, cutoff, k -> k.hour());
        return new Drain(v, d, cov);
    }

    private static <K> void drainCounters(Map<K, Counter> source, Map<K, Long> out, Instant cutoff,
            java.util.function.Function<K, Instant> hourOf) {
        for (Iterator<Map.Entry<K, Counter>> it = source.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<K, Counter> e = it.next();
            Counter c = e.getValue();
            synchronized (c) {
                long delta = c.value - c.flushed;
                if (delta != 0) {
                    out.put(e.getKey(), delta);
                    c.flushed = c.value;
                } else if (hourOf.apply(e.getKey()).isBefore(cutoff)) {
                    it.remove();
                }
            }
        }
    }

    /**
     * Gives back a drain whose write failed, so the next drain carries it again.
     *
     * @param failed the drain that was not stored
     */
    public void restore(Drain failed) {
        for (VolumeDelta delta : failed.volumes()) {
            Counts c = volumes.computeIfAbsent(delta.key(), k -> new Counts());
            synchronized (c) {
                c.flushedPosts -= delta.posts();
                c.flushedAuthors -= delta.uniqueAuthors();
                c.flushedReposts -= delta.reposts();
                c.flushedEngagement -= delta.engagement();
            }
        }
        failed.denominators().forEach((k, n) -> {
            Counter c = denominators.computeIfAbsent(k, x -> new Counter());
            synchronized (c) {
                c.flushed -= n;
            }
        });
        failed.coverage().forEach((k, n) -> {
            Counter c = coverage.computeIfAbsent(k, x -> new Counter());
            synchronized (c) {
                c.flushed -= n;
            }
        });
    }

    /** The posts counted so far in a volume bucket (tests and live views). */
    public long posts(VolumeKey key) {
        Counts c = volumes.get(key);
        if (c == null) {
            return 0;
        }
        synchronized (c) {
            return c.posts;
        }
    }

    /** The distinct authors counted so far in a volume bucket. */
    public long uniqueAuthors(VolumeKey key) {
        Counts c = volumes.get(key);
        if (c == null) {
            return 0;
        }
        synchronized (c) {
            return c.authorCount();
        }
    }

    /** The posts counted so far in a denominator bucket. */
    public long denominator(DenominatorKey key) {
        Counter c = denominators.get(key);
        if (c == null) {
            return 0;
        }
        synchronized (c) {
            return c.value;
        }
    }
}
