package ai.foresignal.source.adapters.goes;

import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.GeometryTransformer;

/**
 * {@code fire-cluster} records from the current FDC detections (prompt 048).
 *
 * <p><b>Components:</b> the detection mask is dilated by one pixel and its 8-connected components are
 * labeled; each detection belongs to the component its dilated block falls in. Two detections are
 * therefore in one cluster when a chain of detections links them with column and row steps of at most
 * {@value #LINK_PIXELS} (Chebyshev distance), which is what "8-connected after a 1-pixel dilation"
 * means on the pixel grid.
 *
 * <p><b>Identity</b> carries over from the previous processed scan by pixel overlap: the largest overlap
 * wins, greedily, each previous cluster used once; otherwise {@code fc-{ulid}}.
 *
 * <p><b>Record:</b> geometry is the convex hull of the member pixel centers buffered by 1 km, vertices
 * rounded to 0.01° (a Point for one pixel); payload {@code pixels}, {@code frpTotalMw},
 * {@code frpMaxMw} (2 significant figures), {@code firstSeenAt}, {@code label}. {@code observedAt} is
 * the scan start and {@code eventTime} the cluster's first sighting. A cluster is tombstoned after
 * {@value #MAX_MISSES} processed scans without members; scans that were not processed are never
 * counted, because {@link #step} is only called for processed scans.
 */
final class FireClusterer {

    /** The record kind. */
    static final String KIND = "fire-cluster";

    /** Processed scans without members before a tombstone. */
    static final int MAX_MISSES = 3;

    /** The largest Chebyshev pixel step linking two detections (1-pixel dilation, then 8-connectivity). */
    static final int LINK_PIXELS = 3;

    /** The hull buffer, km. */
    static final double BUFFER_KM = 1.0;

    /** Kilometers per degree of latitude on the mean sphere. */
    static final double KM_PER_DEGREE = Math.toRadians(Geo.EARTH_RADIUS_KM);

    /** A tracked cluster, persisted between scans. */
    static final class Tracked {

        final String id;
        final long firstSeenMs;
        int misses;
        Set<String> pixels;
        boolean emitted;

        Tracked(String id, long firstSeenMs, int misses, Set<String> pixels, boolean emitted) {
            this.id = id;
            this.firstSeenMs = firstSeenMs;
            this.misses = misses;
            this.pixels = pixels;
            this.emitted = emitted;
        }
    }

    /**
     * One change of a step.
     *
     * @param record the current record or a tombstone
     * @param wasEmitted for a tombstone: whether the cluster was ever returned in a fetch result
     */
    record Change(NormalizedRecord record, boolean wasEmitted) {}

    private final Map<String, Tracked> tracked = new LinkedHashMap<>();
    private final Function<Instant, String> ids;

    /** A clusterer with ULID identities. */
    FireClusterer() {
        this(at -> "fc-" + ClusterIds.ulid(at));
    }

    /** A clusterer with the given identity source (tests). */
    FireClusterer(Function<Instant, String> ids) {
        this.ids = ids;
    }

    /** The tracked clusters. */
    List<Tracked> tracked() {
        return List.copyOf(tracked.values());
    }

    /** Marks clusters as returned in a fetch result. */
    void markEmitted(Iterable<String> clusterIds) {
        for (String id : clusterIds) {
            Tracked t = tracked.get(id);
            if (t != null) {
                t.emitted = true;
            }
        }
    }

    /**
     * Clusters the detections of one processed scan.
     *
     * @param detections the scan's detections
     * @param firstSeen each detection pixel's first sighting ({@code {col}:{row}} to instant)
     * @param scanStart the scan's {@code s} time
     */
    List<Change> step(List<FdcParser.Detection> detections, Map<String, Instant> firstSeen, Instant scanStart) {
        List<List<FdcParser.Detection>> components = components(detections);
        List<Tracked> previous = new ArrayList<>(tracked.values());
        List<int[]> pairs = new ArrayList<>();
        for (int c = 0; c < components.size(); c++) {
            Set<String> pixels = pixelsOf(components.get(c));
            for (int p = 0; p < previous.size(); p++) {
                int overlap = 0;
                for (String pixel : previous.get(p).pixels) {
                    if (pixels.contains(pixel)) {
                        overlap++;
                    }
                }
                if (overlap > 0) {
                    pairs.add(new int[] {overlap, c, p});
                }
            }
        }
        pairs.sort(Comparator.comparingInt((int[] a) -> -a[0]).thenComparingInt(a -> a[1]).thenComparingInt(a -> a[2]));
        Tracked[] matchOf = new Tracked[components.size()];
        boolean[] used = new boolean[previous.size()];
        for (int[] pair : pairs) {
            if (matchOf[pair[1]] == null && !used[pair[2]]) {
                matchOf[pair[1]] = previous.get(pair[2]);
                used[pair[2]] = true;
            }
        }
        List<Change> out = new ArrayList<>();
        for (int p = 0; p < previous.size(); p++) {
            if (used[p]) {
                continue;
            }
            Tracked t = previous.get(p);
            t.misses++;
            if (t.misses >= MAX_MISSES) {
                tracked.remove(t.id);
                out.add(new Change(NormalizedRecord.tombstone(t.id, KIND, scanStart), t.emitted));
            }
        }
        for (int c = 0; c < components.size(); c++) {
            List<FdcParser.Detection> members = components.get(c);
            Tracked t = matchOf[c];
            if (t == null) {
                long first = scanStart.toEpochMilli();
                for (FdcParser.Detection d : members) {
                    Instant seen = firstSeen.get(d.pixel());
                    if (seen != null) {
                        first = Math.min(first, seen.toEpochMilli());
                    }
                }
                t = new Tracked(ids.apply(scanStart), first, 0, pixelsOf(members), false);
                tracked.put(t.id, t);
            } else {
                t.misses = 0;
                t.pixels = pixelsOf(members);
            }
            out.add(new Change(record(t, members, scanStart), false));
        }
        return out;
    }

    /** The record of {@code t} with its current members. */
    static NormalizedRecord record(Tracked t, List<FdcParser.Detection> members, Instant scanStart) {
        double total = 0;
        double max = Double.NaN;
        boolean anyFrp = false;
        for (FdcParser.Detection d : members) {
            if (!Float.isNaN(d.frpMw())) {
                total += d.frpMw();
                max = anyFrp ? Math.max(max, d.frpMw()) : d.frpMw();
                anyFrp = true;
            }
        }
        Double frpTotal = anyFrp ? FdcParser.sig2(total) : null;
        Instant first = Instant.ofEpochMilli(t.firstSeenMs);
        return NormalizedRecord.builder(t.id, KIND)
                .observedAt(scanStart)
                .eventTime(first)
                .geometry(geometry(members))
                .put("pixels", members.size())
                .put("frpTotalMw", frpTotal)
                .put("frpMaxMw", anyFrp ? FdcParser.sig2(max) : null)
                .put("firstSeenAt", first.toString())
                .put("label", label(members.size(), frpTotal))
                .build();
    }

    /** "Fire cluster, 4 pixels, 120 MW". */
    static String label(int pixels, Double frpTotalMw) {
        String base = "Fire cluster, " + pixels + (pixels == 1 ? " pixel" : " pixels");
        return frpTotalMw == null ? base : base + ", " + FdcParser.formatMw(frpTotalMw) + " MW";
    }

    /**
     * The convex hull of the member pixel centers buffered by {@value #BUFFER_KM} km, vertices rounded to
     * 0.01°; a Point for one pixel. The hull and buffer are computed in a local equirectangular km plane
     * around the members, so the buffer is a true kilometer at any latitude.
     */
    static Geometry geometry(List<FdcParser.Detection> members) {
        GeometryFactory factory = Geo.factory();
        if (members.size() == 1) {
            FdcParser.Detection d = members.get(0);
            return Geo.point(FdcParser.round4(d.lon()), FdcParser.round4(d.lat()));
        }
        double lat0 = members.stream().mapToDouble(FdcParser.Detection::lat).average().orElse(0);
        double lon0 = members.stream().mapToDouble(FdcParser.Detection::lon).average().orElse(0);
        double kx = KM_PER_DEGREE * Math.cos(Math.toRadians(lat0));
        double ky = KM_PER_DEGREE;
        Coordinate[] local = new Coordinate[members.size()];
        for (int i = 0; i < members.size(); i++) {
            FdcParser.Detection d = members.get(i);
            local[i] = new Coordinate((d.lon() - lon0) * kx, (d.lat() - lat0) * ky);
        }
        Geometry buffered = new ConvexHull(local, factory).getConvexHull().buffer(BUFFER_KM, 4);
        Geometry rounded = transform(buffered, factory, c -> new Coordinate(round2(lon0 + c.x / kx), round2(lat0 + c.y / ky)));
        if (rounded.isEmpty() || !rounded.isValid() || rounded.getArea() <= 0) {
            Geometry repaired = rounded.isEmpty() ? rounded : rounded.buffer(0);
            if (!repaired.isEmpty() && repaired.getArea() > 0 && repaired instanceof Polygon) {
                rounded = repaired;
            } else {
                rounded = transform(buffered, factory,
                        c -> new Coordinate(FdcParser.round4(lon0 + c.x / kx), FdcParser.round4(lat0 + c.y / ky)));
            }
        }
        rounded.setSRID(Geo.SRID);
        return rounded;
    }

    private static Geometry transform(Geometry g, GeometryFactory factory, Function<Coordinate, Coordinate> f) {
        GeometryTransformer transformer = new GeometryTransformer() {
            @Override
            protected org.locationtech.jts.geom.CoordinateSequence transformCoordinates(
                    org.locationtech.jts.geom.CoordinateSequence coords, Geometry parent) {
                // Rounding makes neighboring vertices coincide; drop the repeats (a ring stays closed).
                List<Coordinate> out = new ArrayList<>(coords.size());
                for (int i = 0; i < coords.size(); i++) {
                    Coordinate c = f.apply(coords.getCoordinate(i));
                    if (out.isEmpty() || !out.get(out.size() - 1).equals2D(c)) {
                        out.add(c);
                    }
                }
                return factory.getCoordinateSequenceFactory().create(out.toArray(Coordinate[]::new));
            }
        };
        Geometry result = transformer.transform(g);
        return factory.createGeometry(result);
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    /** The pixel keys of {@code members}. */
    static Set<String> pixelsOf(List<FdcParser.Detection> members) {
        Set<String> out = new TreeSet<>();
        for (FdcParser.Detection d : members) {
            out.add(d.pixel());
        }
        return out;
    }

    /**
     * The components of {@code detections}: union-find over pairs within {@value #LINK_PIXELS} pixels
     * (Chebyshev), with a grid of {@value #LINK_PIXELS}-pixel cells so only neighboring cells are
     * compared. Components are ordered by their first member in file order.
     */
    static List<List<FdcParser.Detection>> components(List<FdcParser.Detection> detections) {
        int n = detections.size();
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
        Map<Long, List<Integer>> grid = new HashMap<>();
        for (int i = 0; i < n; i++) {
            FdcParser.Detection d = detections.get(i);
            grid.computeIfAbsent(cell(d.col() / LINK_PIXELS, d.row() / LINK_PIXELS), k -> new ArrayList<>()).add(i);
        }
        for (int i = 0; i < n; i++) {
            FdcParser.Detection a = detections.get(i);
            int cx = a.col() / LINK_PIXELS;
            int cy = a.row() / LINK_PIXELS;
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    List<Integer> cell = grid.get(cell(cx + dx, cy + dy));
                    if (cell == null) {
                        continue;
                    }
                    for (int j : cell) {
                        if (j <= i) {
                            continue;
                        }
                        FdcParser.Detection b = detections.get(j);
                        if (Math.abs(a.col() - b.col()) <= LINK_PIXELS && Math.abs(a.row() - b.row()) <= LINK_PIXELS) {
                            union(parent, i, j);
                        }
                    }
                }
            }
        }
        Map<Integer, List<FdcParser.Detection>> byRoot = new LinkedHashMap<>();
        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) {
            order[i] = i;
        }
        Arrays.sort(order, Comparator.comparingInt((Integer i) -> detections.get(i).row())
                .thenComparingInt(i -> detections.get(i).col()));
        for (int i : order) {
            byRoot.computeIfAbsent(find(parent, i), k -> new ArrayList<>()).add(detections.get(i));
        }
        return new ArrayList<>(byRoot.values());
    }

    private static long cell(int cx, int cy) {
        return ((long) cy << 32) ^ (cx & 0xFFFFFFFFL);
    }

    private static int find(int[] parent, int i) {
        while (parent[i] != i) {
            parent[i] = parent[parent[i]];
            i = parent[i];
        }
        return i;
    }

    private static void union(int[] parent, int a, int b) {
        int ra = find(parent, a);
        int rb = find(parent, b);
        if (ra != rb) {
            parent[Math.max(ra, rb)] = Math.min(ra, rb);
        }
    }

    /** Tracked clusters as adapter-state text: {@code id|firstSeenMs|misses|emitted|pixel,pixel,…} per line. */
    String serialize() {
        StringBuilder out = new StringBuilder();
        for (Tracked t : tracked.values()) {
            if (!out.isEmpty()) {
                out.append('\n');
            }
            out.append(t.id).append('|').append(t.firstSeenMs).append('|').append(t.misses).append('|')
                    .append(t.emitted ? 1 : 0).append('|').append(String.join(",", t.pixels));
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
            String[] f = line.split("\\|", -1);
            if (f.length != 5 || f[0].isBlank()) {
                continue;
            }
            try {
                Set<String> pixels = new LinkedHashSet<>();
                for (String p : f[4].split(",")) {
                    if (!p.isBlank()) {
                        pixels.add(p);
                    }
                }
                tracked.put(f[0], new Tracked(f[0], Long.parseLong(f[1]), Integer.parseInt(f[2]), pixels, "1".equals(f[3])));
            } catch (NumberFormatException e) {
                // Dropped; re-created if still burning.
            }
        }
    }
}
