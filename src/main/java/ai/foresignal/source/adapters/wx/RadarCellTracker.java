package ai.foresignal.source.adapters.wx;

import ai.foresignal.raster.GridDef;
import ai.foresignal.raster.sample.CellAreas;
import ai.foresignal.raster.sample.FrameView;
import ai.foresignal.raster.sample.GridWindow;
import ai.foresignal.source.spi.Geo;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Storm-cell detection and tracking for {@code wx-cells} (prompt 052; Weather-Imagery-Design §7.2). Pure:
 * no state besides the {@link Track}s the caller keeps.
 *
 * <p><b>Detection</b> per reflectivity frame, inside a cell window:
 * <ol>
 *   <li>the 40 dBZ mask;
 *   <li>a 3×3 binary opening (erosion then dilation), which removes speckle and thin bridges;
 *   <li>8-connected labeling with union-find;
 *   <li>a component holding two or more separate 50 dBZ cores is split: each pixel goes to the core it is
 *       reached from first by an 8-connected breadth-first search inside the component;
 *   <li>per cell: its pixels, maximum reflectivity, area on the sphere and area-weighted centroid.
 * </ol>
 *
 * <p><b>Motion</b> ({@code centroid-v1}): each new cell is matched to the nearest previous centroid within
 * 30 km per 10 minutes of frame gap (one-to-one, nearest pairs first); a track's velocity is the least-squares
 * slope of its last positions (up to 4, so 3 matches), in a local kilometer plane.
 */
public final class RadarCellTracker {

    /** The mask threshold, dBZ. */
    public static final double MASK_DBZ = 40;

    /** The core threshold, dBZ. */
    public static final double CORE_DBZ = 50;

    /** The matching radius per 10 minutes of gap, km. */
    public static final double MATCH_KM_PER_10_MIN = 30;

    /** Positions kept per track: the current one plus the last 3 matches. */
    public static final int TRACK_POSITIONS = 4;

    private RadarCellTracker() {}

    /**
     * One detected cell.
     *
     * @param pixels flat grid cells
     * @param maxDbz the maximum reflectivity
     * @param areaKm2 the area on the sphere
     * @param lon the area-weighted centroid longitude
     * @param lat the area-weighted centroid latitude
     */
    public record Cell(int[] pixels, double maxDbz, double areaKm2, double lon, double lat) {}

    /** Detects the cells of a reflectivity frame inside {@code window}. */
    public static List<Cell> detect(FrameView view, GridWindow window) {
        if (window.isEmpty()) {
            return List.of();
        }
        int w = window.c1() - window.c0() + 1;
        int h = window.r1() - window.r0() + 1;
        int nx = view.nx();
        double maskCode = view.codeAtLeast(MASK_DBZ);
        double coreCode = view.codeAtLeast(CORE_DBZ);
        boolean[] mask = new boolean[w * h];
        boolean[] core = new boolean[w * h];
        for (int y = 0; y < h; y++) {
            int row = window.r0() + y;
            for (int x = 0; x < w; x++) {
                int cell = row * nx + window.c0() + x;
                double code = view.code(cell, 0);
                if (view.classify(code) != FrameView.Kind.VALUE) {
                    continue;
                }
                if (code >= maskCode) {
                    mask[y * w + x] = true;
                    core[y * w + x] = code >= coreCode;
                }
            }
        }
        boolean[] opened = open3x3(mask, w, h);
        for (int k = 0; k < core.length; k++) {
            core[k] &= opened[k];
        }
        int[] components = label(opened, w, h);
        int[] cores = label(core, w, h);
        int[] cellOf = split(components, cores, w, h);
        Map<Integer, List<Integer>> pixels = new LinkedHashMap<>();
        for (int k = 0; k < cellOf.length; k++) {
            if (cellOf[k] != 0) {
                pixels.computeIfAbsent(cellOf[k], c -> new ArrayList<>()).add(k);
            }
        }
        GridDef grid = view.grid();
        List<Cell> out = new ArrayList<>();
        for (List<Integer> members : pixels.values()) {
            int[] flat = new int[members.size()];
            double max = Double.NEGATIVE_INFINITY;
            double area = 0;
            double sx = 0;
            double sy = 0;
            for (int m = 0; m < members.size(); m++) {
                int k = members.get(m);
                int c = window.c0() + k % w;
                int r = window.r0() + k / w;
                flat[m] = r * nx + c;
                max = Math.max(max, view.value(flat[m], 0));
                double a = CellAreas.cellAreaKm2(grid, c, r);
                double[] ll = grid.indexToLonLat(c, r);
                area += a;
                sx += a * ll[0];
                sy += a * ll[1];
            }
            if (area <= 0) {
                continue;
            }
            out.add(new Cell(flat, max, area, sx / area, sy / area));
        }
        return out;
    }

    /** A 3×3 binary opening; cells outside the window count as unset. */
    static boolean[] open3x3(boolean[] mask, int w, int h) {
        boolean[] eroded = new boolean[mask.length];
        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                boolean all = true;
                for (int dy = -1; dy <= 1 && all; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        if (!mask[(y + dy) * w + x + dx]) {
                            all = false;
                            break;
                        }
                    }
                }
                eroded[y * w + x] = all;
            }
        }
        boolean[] dilated = new boolean[mask.length];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (!eroded[y * w + x]) {
                    continue;
                }
                for (int dy = -1; dy <= 1; dy++) {
                    int yy = y + dy;
                    if (yy < 0 || yy >= h) {
                        continue;
                    }
                    for (int dx = -1; dx <= 1; dx++) {
                        int xx = x + dx;
                        if (xx >= 0 && xx < w) {
                            dilated[yy * w + xx] = true;
                        }
                    }
                }
            }
        }
        return dilated;
    }

    /** 8-connected labels (1..n, 0 for background) by union-find. */
    static int[] label(boolean[] mask, int w, int h) {
        int[] parent = new int[mask.length + 1];
        int[] provisional = new int[mask.length];
        int next = 1;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int k = y * w + x;
                if (!mask[k]) {
                    continue;
                }
                int best = 0;
                int[][] neighbors = {{-1, -1}, {0, -1}, {1, -1}, {-1, 0}};
                for (int[] d : neighbors) {
                    int xx = x + d[0];
                    int yy = y + d[1];
                    if (xx < 0 || xx >= w || yy < 0) {
                        continue;
                    }
                    int l = provisional[yy * w + xx];
                    if (l == 0) {
                        continue;
                    }
                    if (best == 0) {
                        best = l;
                    } else {
                        union(parent, best, l);
                    }
                }
                if (best == 0) {
                    best = next;
                    parent[next] = next;
                    next++;
                }
                provisional[k] = best;
            }
        }
        int[] canonical = new int[next];
        int count = 0;
        int[] out = new int[mask.length];
        for (int k = 0; k < mask.length; k++) {
            if (provisional[k] == 0) {
                continue;
            }
            int root = find(parent, provisional[k]);
            if (canonical[root] == 0) {
                canonical[root] = ++count;
            }
            out[k] = canonical[root];
        }
        return out;
    }

    private static int find(int[] parent, int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]];
            x = parent[x];
        }
        return x;
    }

    private static void union(int[] parent, int a, int b) {
        int ra = find(parent, a);
        int rb = find(parent, b);
        if (ra != rb) {
            parent[Math.max(ra, rb)] = Math.min(ra, rb);
        }
    }

    /**
     * Final cell labels: a component's own label, or, for a component with two or more cores, a label per core
     * assigned by a multi-source breadth-first search inside the component.
     */
    static int[] split(int[] components, int[] cores, int w, int h) {
        Map<Integer, java.util.Set<Integer>> coresOf = new HashMap<>();
        for (int k = 0; k < components.length; k++) {
            if (components[k] != 0 && cores[k] != 0) {
                coresOf.computeIfAbsent(components[k], c -> new java.util.TreeSet<>()).add(cores[k]);
            }
        }
        int maxComponent = Arrays.stream(components).max().orElse(0);
        int[] out = new int[components.length];
        int[] coreCell = new int[Arrays.stream(cores).max().orElse(0) + 1];
        int nextCell = maxComponent + 1;
        for (Map.Entry<Integer, java.util.Set<Integer>> e : coresOf.entrySet()) {
            if (e.getValue().size() < 2) {
                continue;
            }
            for (int coreLabel : e.getValue()) {
                coreCell[coreLabel] = nextCell++;
            }
        }
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int k = 0; k < components.length; k++) {
            int comp = components[k];
            if (comp == 0) {
                continue;
            }
            java.util.Set<Integer> cs = coresOf.get(comp);
            if (cs == null || cs.size() < 2) {
                out[k] = comp;
            } else if (cores[k] != 0) {
                out[k] = coreCell[cores[k]];
                queue.add(k);
            }
        }
        while (!queue.isEmpty()) {
            int k = queue.poll();
            int x = k % w;
            int y = k / w;
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    int xx = x + dx;
                    int yy = y + dy;
                    if ((dx == 0 && dy == 0) || xx < 0 || xx >= w || yy < 0 || yy >= h) {
                        continue;
                    }
                    int n = yy * w + xx;
                    if (out[n] == 0 && components[n] == components[k]) {
                        out[n] = out[k];
                        queue.add(n);
                    }
                }
            }
        }
        return out;
    }

    // ------------------------------------------------------------------ tracking

    /** One tracked cell: its id, when it was first seen, and its recent positions. */
    public static final class Track {

        final String id;
        final Instant firstSeen;
        final ArrayDeque<double[]> positions = new ArrayDeque<>();

        /** A track starting at {@code (lon, lat)} at {@code time}. */
        public Track(String id, Instant firstSeen, Instant time, double lon, double lat) {
            this.id = id;
            this.firstSeen = firstSeen;
            add(time, lon, lat);
        }

        /** The id ({@code c-{ulid}}). */
        public String id() {
            return id;
        }

        /** When the cell was first seen. */
        public Instant firstSeen() {
            return firstSeen;
        }

        /** Adds a position, keeping the last {@value #TRACK_POSITIONS}. */
        public void add(Instant time, double lon, double lat) {
            positions.addLast(new double[] {time.toEpochMilli(), lon, lat});
            while (positions.size() > TRACK_POSITIONS) {
                positions.removeFirst();
            }
        }

        /** The last position {@code {epochMs, lon, lat}}. */
        public double[] last() {
            return positions.getLast();
        }

        /** The number of positions. */
        public int size() {
            return positions.size();
        }
    }

    /**
     * Matches new cells to tracks: nearest pairs first, one-to-one, within {@value #MATCH_KM_PER_10_MIN} km
     * per 10 minutes since the track's last position (at least 2 minutes, at most 30).
     *
     * @return for each cell index, the matched track, or no entry
     */
    public static Map<Integer, Track> match(List<Cell> cells, Collection<Track> tracks, Instant time) {
        List<double[]> pairs = new ArrayList<>();
        List<Track> list = new ArrayList<>(tracks);
        for (int c = 0; c < cells.size(); c++) {
            Cell cell = cells.get(c);
            for (int t = 0; t < list.size(); t++) {
                double[] last = list.get(t).last();
                double gapMin = (time.toEpochMilli() - last[0]) / 60_000.0;
                if (gapMin <= 0) {
                    continue;
                }
                double limit = MATCH_KM_PER_10_MIN * Math.max(2, Math.min(30, gapMin)) / 10.0;
                double d = Geo.haversineKm(cell.lat(), cell.lon(), last[2], last[1]);
                if (d <= limit) {
                    pairs.add(new double[] {d, c, t});
                }
            }
        }
        pairs.sort((a, b) -> Double.compare(a[0], b[0]));
        Map<Integer, Track> out = new HashMap<>();
        boolean[] usedTrack = new boolean[list.size()];
        for (double[] p : pairs) {
            int c = (int) p[1];
            int t = (int) p[2];
            if (out.containsKey(c) || usedTrack[t]) {
                continue;
            }
            out.put(c, list.get(t));
            usedTrack[t] = true;
        }
        return out;
    }

    /**
     * A track's motion by least squares over its positions in a local kilometer plane.
     *
     * @return {@code {speedKmh, bearingDeg}} (bearing toward which it moves, 0 = north, clockwise), or null with
     *     fewer than two positions or no elapsed time
     */
    public static double[] motion(Track track) {
        if (track.size() < 2) {
            return null;
        }
        double[] origin = track.last();
        double cos = Math.cos(Math.toRadians(origin[2]));
        int n = track.size();
        double[] t = new double[n];
        double[] x = new double[n];
        double[] y = new double[n];
        int i = 0;
        for (double[] p : track.positions) {
            t[i] = (p[0] - origin[0]) / 1000.0;
            x[i] = (p[1] - origin[1]) * 111.32 * cos;
            y[i] = (p[2] - origin[2]) * 110.57;
            i++;
        }
        double tm = Arrays.stream(t).average().orElse(0);
        double xm = Arrays.stream(x).average().orElse(0);
        double ym = Arrays.stream(y).average().orElse(0);
        double stt = 0;
        double stx = 0;
        double sty = 0;
        for (int k = 0; k < n; k++) {
            stt += (t[k] - tm) * (t[k] - tm);
            stx += (t[k] - tm) * (x[k] - xm);
            sty += (t[k] - tm) * (y[k] - ym);
        }
        if (stt <= 0) {
            return null;
        }
        double vx = stx / stt * 3600;
        double vy = sty / stt * 3600;
        double speed = Math.hypot(vx, vy);
        double bearing = (Math.toDegrees(Math.atan2(vx, vy)) + 360) % 360;
        return new double[] {speed, bearing};
    }

    /** The largest tolerated frame gap for matching. */
    public static Duration maxGap() {
        return Duration.ofMinutes(30);
    }
}
