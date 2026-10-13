package ai.foresignal.source.adapters.goes;

import ai.foresignal.source.spi.FlashBatch;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * {@code lightning-cluster} records from the GLM ring (prompt 048, Weather-Imagery-Design §7.2).
 *
 * <p>After each processed GLM file, {@link #step} runs DBSCAN over the ring flashes of the last
 * {@value #WINDOW_MINUTES} minutes (eps {@value #EPS_KM} km haversine, minPts {@value #MIN_PTS}) with a
 * spatial grid index of eps-sized cells, so each flash only meets the flashes of its 3 × 3 neighborhood
 * and one pass is close to O(n).
 *
 * <p><b>Identity:</b> each cluster is matched to the tracked clusters by nearest centroid within
 * {@value #MATCH_KM} km, greedily, closest pair first; an unmatched cluster gets {@code lc-{ulid}}.
 *
 * <p><b>Payload</b>, quantized so a version is written only on a meaningful change: {@code flashes5m}
 * (the bucket lower bound of the member count: 3, 5, 10, 20, 50, 100, 200), {@code radiusKm} (the
 * farthest member from the centroid, to 1 km, at least 1), {@code maxEnergyFj} (the decade lower bound
 * of the brightest member), {@code label}, {@code automated: true} and {@code notAWarning: true}. The
 * geometry is the centroid rounded to 0.01°. {@code observedAt} is the newest member flash and
 * {@code eventTime} the cluster's first flash, kept across files.
 *
 * <p><b>Lifecycle in data time:</b> a tracked cluster with no members accumulates the coverage of each
 * processed file (the {@code deltaMs} the adapter passes) and is tombstoned after
 * {@value #QUIET_MINUTES} minutes of processed data without members. Files that are not processed add
 * nothing, which is what freezes clusters while the feed is down (Weather-Imagery-Design §7.1: stale
 * is not clear).
 */
final class LightningClusterer {

    /** The record kind. */
    static final String KIND = "lightning-cluster";

    /** DBSCAN neighborhood radius, km. */
    static final double EPS_KM = 8.0;

    /** DBSCAN minimum neighborhood size (the point itself included). */
    static final int MIN_PTS = 3;

    /** The clustering window, minutes. */
    static final int WINDOW_MINUTES = 5;

    /** The clustering window. */
    static final Duration WINDOW = Duration.ofMinutes(WINDOW_MINUTES);

    /** Identity carries over within this distance of centroids, km. */
    static final double MATCH_KM = 15.0;

    /** Minutes of processed data without members before a tombstone. */
    static final int QUIET_MINUTES = 10;

    /** {@link #QUIET_MINUTES} in milliseconds. */
    static final long QUIET_MS = QUIET_MINUTES * 60_000L;

    /** Lower bounds of the {@code flashes5m} buckets. */
    static final int[] FLASH_BUCKETS = {3, 5, 10, 20, 50, 100, 200};

    /** Kilometers per degree of latitude on the mean sphere. */
    static final double KM_PER_DEGREE = Math.toRadians(Geo.EARTH_RADIUS_KM);

    /** A tracked cluster, persisted in the adapter state between runs. */
    static final class Tracked {

        final String id;
        double lat;
        double lon;
        final long firstMs;
        long lastMemberMs;
        long quietMs;
        boolean emitted;

        Tracked(String id, double lat, double lon, long firstMs, long lastMemberMs, long quietMs, boolean emitted) {
            this.id = id;
            this.lat = lat;
            this.lon = lon;
            this.firstMs = firstMs;
            this.lastMemberMs = lastMemberMs;
            this.quietMs = quietMs;
            this.emitted = emitted;
        }
    }

    /**
     * One DBSCAN cluster of the current window.
     *
     * @param lat centroid latitude
     * @param lon centroid longitude
     * @param count members
     * @param earliestMs the earliest member flash
     * @param newestMs the newest member flash
     * @param maxEnergyFj the brightest member
     * @param radiusKm the farthest member from the centroid
     */
    record Summary(double lat, double lon, int count, long earliestMs, long newestMs, double maxEnergyFj,
            double radiusKm) {}

    /**
     * One change produced by a step.
     *
     * @param record the cluster's current record, or its tombstone
     * @param wasEmitted for a tombstone: whether the cluster was ever returned in a fetch result
     */
    record Change(NormalizedRecord record, boolean wasEmitted) {}

    private final Map<String, Tracked> tracked = new LinkedHashMap<>();
    private final Function<Instant, String> ids;

    /** A clusterer with ULID identities. */
    LightningClusterer() {
        this(at -> "lc-" + ClusterIds.ulid(at));
    }

    /** A clusterer with the given identity source (tests). */
    LightningClusterer(Function<Instant, String> ids) {
        this.ids = ids;
    }

    /** The tracked clusters, in insertion order. */
    List<Tracked> tracked() {
        return List.copyOf(tracked.values());
    }

    /** Marks the given clusters as returned in a fetch result. */
    void markEmitted(Iterable<String> clusterIds) {
        for (String id : clusterIds) {
            Tracked t = tracked.get(id);
            if (t != null) {
                t.emitted = true;
            }
        }
    }

    /**
     * Clusters {@code window} (the ring flashes of the last 5 minutes before {@code fileEndMs}), carries
     * identities and retires quiet clusters.
     *
     * @param window the flashes to cluster
     * @param fileEndMs the coverage end of the file just processed
     * @param deltaMs the processed data time this file adds (its coverage, or the time since the last
     *     processed file when that was within the maximum gap)
     * @return the records of the current clusters and the tombstones of retired ones
     */
    List<Change> step(FlashBatch window, long fileEndMs, long deltaMs) {
        List<Summary> clusters = summarize(window, dbscan(window));
        List<Change> out = new ArrayList<>();

        // Greedy matching, closest centroid pair first.
        List<double[]> pairs = new ArrayList<>();
        List<Tracked> previous = new ArrayList<>(tracked.values());
        for (int c = 0; c < clusters.size(); c++) {
            Summary s = clusters.get(c);
            for (int p = 0; p < previous.size(); p++) {
                Tracked t = previous.get(p);
                double d = Geo.haversineKm(s.lat(), s.lon(), t.lat, t.lon);
                if (d <= MATCH_KM) {
                    pairs.add(new double[] {d, c, p});
                }
            }
        }
        pairs.sort(Comparator.comparingDouble((double[] a) -> a[0]).thenComparingDouble(a -> a[1])
                .thenComparingDouble(a -> a[2]));
        Tracked[] matchOf = new Tracked[clusters.size()];
        boolean[] used = new boolean[previous.size()];
        for (double[] pair : pairs) {
            int c = (int) pair[1];
            int p = (int) pair[2];
            if (matchOf[c] == null && !used[p]) {
                matchOf[c] = previous.get(p);
                used[p] = true;
            }
        }

        // Tracked clusters without members accumulate quiet data time.
        for (int p = 0; p < previous.size(); p++) {
            if (used[p]) {
                continue;
            }
            Tracked t = previous.get(p);
            t.quietMs += Math.max(0, deltaMs);
            if (t.quietMs >= QUIET_MS) {
                tracked.remove(t.id);
                Instant at = Instant.ofEpochMilli(Math.max(fileEndMs, t.lastMemberMs));
                out.add(new Change(NormalizedRecord.tombstone(t.id, KIND, at), t.emitted));
            }
        }

        for (int c = 0; c < clusters.size(); c++) {
            Summary s = clusters.get(c);
            Tracked t = matchOf[c];
            if (t == null) {
                t = new Tracked(ids.apply(Instant.ofEpochMilli(s.earliestMs())), s.lat(), s.lon(), s.earliestMs(),
                        s.newestMs(), 0, false);
                tracked.put(t.id, t);
            } else {
                t.lat = s.lat();
                t.lon = s.lon();
                t.lastMemberMs = Math.max(t.lastMemberMs, s.newestMs());
                t.quietMs = 0;
            }
            out.add(new Change(record(t, s), false));
        }
        return out;
    }

    /** The record of tracked cluster {@code t} with the current summary {@code s}. */
    static NormalizedRecord record(Tracked t, Summary s) {
        int bucket = flashBucket(s.count());
        return NormalizedRecord.builder(t.id, KIND)
                .observedAt(Instant.ofEpochMilli(t.lastMemberMs))
                .eventTime(Instant.ofEpochMilli(t.firstMs))
                .point(round2(s.lon()), round2(s.lat()))
                .put("flashes5m", bucket)
                .put("radiusKm", Math.max(1L, Math.round(s.radiusKm())))
                .put("maxEnergyFj", energyBucket(s.maxEnergyFj()))
                .put("label", label(bucket))
                .put("automated", Boolean.TRUE)
                .put("notAWarning", Boolean.TRUE)
                .build();
    }

    /** The label of a {@code flashes5m} bucket: "Lightning cluster, 20+ flashes/5 min". */
    static String label(int bucket) {
        return "Lightning cluster, " + bucket + "+ flashes/5 min";
    }

    /** The largest bucket lower bound not above {@code count}; {@value #MIN_PTS} below the first. */
    static int flashBucket(int count) {
        int bucket = FLASH_BUCKETS[0];
        for (int b : FLASH_BUCKETS) {
            if (count >= b) {
                bucket = b;
            }
        }
        return bucket;
    }

    /**
     * The decade lower bound of {@code energyFj}: 1, 10, 100, 1000 … (a {@code Long} from 1 fJ up, a
     * {@code Double} below), 0 for no energy.
     */
    static Number energyBucket(double energyFj) {
        if (!(energyFj > 0) || Double.isInfinite(energyFj)) {
            return 0L;
        }
        double bucket = Math.pow(10, Math.floor(Math.log10(energyFj)));
        return bucket >= 1 ? (Number) Math.round(bucket) : (Number) bucket;
    }

    /** {@code v} rounded to 0.01. */
    static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    /** The summaries of DBSCAN clusters (index arrays into {@code window}) with at least {@value #MIN_PTS} members. */
    static List<Summary> summarize(FlashBatch window, List<int[]> clusters) {
        List<Summary> out = new ArrayList<>();
        for (int[] members : clusters) {
            if (members.length < MIN_PTS) {
                continue;
            }
            double sumLat = 0;
            double sumLon = 0;
            long earliest = Long.MAX_VALUE;
            long newest = Long.MIN_VALUE;
            double maxEnergy = 0;
            for (int i : members) {
                sumLat += window.lat()[i];
                sumLon += window.lon()[i];
                earliest = Math.min(earliest, window.timesMs()[i]);
                newest = Math.max(newest, window.timesMs()[i]);
                maxEnergy = Math.max(maxEnergy, window.energyFj()[i]);
            }
            double lat = sumLat / members.length;
            double lon = sumLon / members.length;
            double radius = 0;
            for (int i : members) {
                radius = Math.max(radius, Geo.haversineKm(lat, lon, window.lat()[i], window.lon()[i]));
            }
            out.add(new Summary(lat, lon, members.length, earliest, newest, maxEnergy, radius));
        }
        out.sort(Comparator.comparingLong(Summary::earliestMs).thenComparingDouble(Summary::lat)
                .thenComparingDouble(Summary::lon));
        return out;
    }

    /**
     * DBSCAN over {@code flashes} (eps {@value #EPS_KM} km haversine, minPts {@value #MIN_PTS}) with a grid
     * index; returns each cluster's member indices. Noise belongs to no cluster.
     */
    static List<int[]> dbscan(FlashBatch flashes) {
        int n = flashes.size();
        List<int[]> clusters = new ArrayList<>();
        if (n == 0) {
            return clusters;
        }
        double maxAbsLat = 0;
        for (float lat : flashes.lat()) {
            maxAbsLat = Math.max(maxAbsLat, Math.abs(lat));
        }
        double cellLat = EPS_KM / KM_PER_DEGREE;
        double cellLon = cellLat / Math.max(0.01, Math.cos(Math.toRadians(Math.min(89.0, maxAbsLat))));
        Map<Long, List<Integer>> grid = new HashMap<>();
        long[] cellOf = new long[n];
        for (int i = 0; i < n; i++) {
            long cy = (long) Math.floor(flashes.lat()[i] / cellLat);
            long cx = (long) Math.floor(flashes.lon()[i] / cellLon);
            cellOf[i] = key(cy, cx);
            grid.computeIfAbsent(cellOf[i], k -> new ArrayList<>()).add(i);
        }
        final int unvisited = -2;
        final int noise = -1;
        int[] label = new int[n];
        java.util.Arrays.fill(label, unvisited);
        int next = 0;
        for (int i = 0; i < n; i++) {
            if (label[i] != unvisited) {
                continue;
            }
            List<Integer> neighbors = neighbors(flashes, grid, cellLat, cellLon, i);
            if (neighbors.size() < MIN_PTS) {
                label[i] = noise;
                continue;
            }
            int cluster = next++;
            label[i] = cluster;
            List<Integer> members = new ArrayList<>();
            members.add(i);
            java.util.ArrayDeque<Integer> queue = new java.util.ArrayDeque<>(neighbors);
            while (!queue.isEmpty()) {
                int j = queue.poll();
                if (label[j] == noise) {
                    label[j] = cluster;
                    members.add(j);
                }
                if (label[j] != unvisited) {
                    continue;
                }
                label[j] = cluster;
                members.add(j);
                List<Integer> more = neighbors(flashes, grid, cellLat, cellLon, j);
                if (more.size() >= MIN_PTS) {
                    queue.addAll(more);
                }
            }
            clusters.add(members.stream().mapToInt(Integer::intValue).sorted().toArray());
        }
        return clusters;
    }

    private static long key(long cy, long cx) {
        return (cy << 32) ^ (cx & 0xFFFFFFFFL);
    }

    /** The flashes within eps of flash {@code i}, itself included. */
    private static List<Integer> neighbors(FlashBatch f, Map<Long, List<Integer>> grid, double cellLat, double cellLon,
            int i) {
        double lat = f.lat()[i];
        double lon = f.lon()[i];
        long cy = (long) Math.floor(lat / cellLat);
        long cx = (long) Math.floor(lon / cellLon);
        List<Integer> out = new ArrayList<>();
        for (long dy = -1; dy <= 1; dy++) {
            for (long dx = -1; dx <= 1; dx++) {
                List<Integer> cell = grid.get(key(cy + dy, cx + dx));
                if (cell == null) {
                    continue;
                }
                for (int j : cell) {
                    if (j == i || Geo.haversineKm(lat, lon, f.lat()[j], f.lon()[j]) <= EPS_KM) {
                        out.add(j);
                    }
                }
            }
        }
        return out;
    }

    /** The tracked clusters as adapter-state text: one {@code id|lat|lon|firstMs|lastMemberMs|quietMs|emitted} per line. */
    String serialize() {
        StringBuilder out = new StringBuilder();
        for (Tracked t : tracked.values()) {
            if (!out.isEmpty()) {
                out.append('\n');
            }
            out.append(t.id).append('|').append(String.format(Locale.ROOT, "%.5f", t.lat)).append('|')
                    .append(String.format(Locale.ROOT, "%.5f", t.lon)).append('|').append(t.firstMs).append('|')
                    .append(t.lastMemberMs).append('|').append(t.quietMs).append('|').append(t.emitted ? 1 : 0);
        }
        return out.toString();
    }

    /** Restores tracked clusters from {@link #serialize()} text; malformed lines are skipped. */
    void restore(String text) {
        tracked.clear();
        if (text == null || text.isBlank()) {
            return;
        }
        for (String line : text.split("\n")) {
            String[] f = line.split("\\|");
            if (f.length != 7 || f[0].isBlank()) {
                continue;
            }
            try {
                Tracked t = new Tracked(f[0], Double.parseDouble(f[1]), Double.parseDouble(f[2]), Long.parseLong(f[3]),
                        Long.parseLong(f[4]), Long.parseLong(f[5]), "1".equals(f[6]));
                tracked.put(t.id, t);
            } catch (NumberFormatException e) {
                // A malformed line is dropped; its cluster is re-created if it is still active.
            }
        }
    }
}
