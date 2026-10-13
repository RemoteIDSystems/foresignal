package ai.foresignal.source.adapters.wx;

import ai.foresignal.raster.sample.PlaceGridIndex;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The in-memory state of one place's {@code place-weather} record (prompt 052; Weather-Imagery-Design
 * §7.2), and the pure payload builder.
 *
 * <p>The payload is exactly the design's: {@code place}, {@code radiusKm}, the sections {@code radar},
 * {@code hail}, {@code precip}, {@code lightning}, {@code cell}, {@code smoke} and {@code forecast.gfs}
 * ({@code forecast.nws} joins in prompt 053), plus {@code automated: true} and {@code notAWarning: true}.
 * Frame ids, ages and timestamps are never in it; {@code asOf} appears only while a section is not ok.
 *
 * <p><b>Verified-clear helpers</b> for all-clear rules (the trigger engine's conditions are AND-only):
 * {@code radar.verifiedNearest50Km10m} and {@code lightning.verifiedNearestKm30m} equal their metric while
 * the section is {@code ok}, and are {@code 0} otherwise. So {@code <= X} on them is true when a core is near
 * <em>or</em> the data is unknown, and an {@code exit} rule on them fires only when the data is ok and far.
 */
public final class PlaceWeatherState {

    /** The rolling window of {@code radar.nearest50Km10m}. */
    public static final Duration RADAR_WINDOW = Duration.ofMinutes(10);

    final String placeId;
    String name;
    double radiusKm;
    double lon;
    double lat;
    int version;

    final WxSection radar;
    final WxSection hail;
    final WxSection precip;
    final WxSection lightning;
    final WxSection cell;
    final WxSection smoke;
    final WxSection gfs;

    /** {@code (validTime ms, nearest50Km)} of recent reflectivity frames, oldest first. */
    final ArrayDeque<long[]> radarHistory = new ArrayDeque<>();

    String radarFrame;
    String hailFrame;
    String precipFrame;
    Instant smokeRun;
    Instant gfsRun;

    /** A new place with fresh sections. */
    public PlaceWeatherState(String placeId, String name, double radiusKm, double lon, double lat, int version) {
        this(placeId, name, radiusKm, lon, lat, version, new WxSection(), new WxSection(), new WxSection(),
                new WxSection(), new WxSection(), new WxSection(), new WxSection());
    }

    /** A place with restored sections. */
    PlaceWeatherState(String placeId, String name, double radiusKm, double lon, double lat, int version, WxSection radar,
            WxSection hail, WxSection precip, WxSection lightning, WxSection cell, WxSection smoke, WxSection gfs) {
        this.placeId = placeId;
        this.name = name;
        this.radiusKm = radiusKm;
        this.lon = lon;
        this.lat = lat;
        this.version = version;
        this.radar = radar;
        this.hail = hail;
        this.precip = precip;
        this.lightning = lightning;
        this.cell = cell;
        this.smoke = smoke;
        this.gfs = gfs;
    }

    /** The externalId of a place's record: {@code place:{id}}. */
    public static String externalId(String placeId) {
        return "place:" + placeId;
    }

    /** The place id of an externalId, or null when it is not {@code place:{id}}. */
    public static String placeIdOf(String externalId) {
        return externalId != null && externalId.startsWith("place:") ? externalId.substring(6) : null;
    }

    /**
     * Restores a place from its head payload: the sections with their statuses, metrics and {@code asOf}.
     *
     * @param payload the head's payload
     * @param observedAt the head's observation time
     */
    @SuppressWarnings("unchecked")
    public static PlaceWeatherState restore(String placeId, String name, double radiusKm, double lon, double lat,
            int version, Map<String, Object> payload, Instant observedAt) {
        Object forecast = payload.get("forecast");
        Object gfs = forecast instanceof Map<?, ?> f ? ((Map<String, Object>) f).get("gfs") : null;
        return new PlaceWeatherState(placeId, name, radiusKm, lon, lat, version,
                WxSection.restore(strip(payload.get("radar"), "verifiedNearest50Km10m"), observedAt),
                WxSection.restore(payload.get("hail"), observedAt),
                WxSection.restore(payload.get("precip"), observedAt),
                WxSection.restore(strip(payload.get("lightning"), "verifiedNearestKm30m"), observedAt),
                WxSection.restore(payload.get("cell"), observedAt),
                WxSection.restore(payload.get("smoke"), observedAt),
                WxSection.restore(gfs, observedAt));
    }

    @SuppressWarnings("unchecked")
    private static Object strip(Object section, String key) {
        if (!(section instanceof Map<?, ?> m)) {
            return section;
        }
        Map<String, Object> copy = new LinkedHashMap<>((Map<String, Object>) m);
        copy.remove(key);
        return copy;
    }

    /**
     * Adds a reflectivity frame's {@code nearest50Km} to the rolling window and returns the window minimum.
     *
     * @param validTime the frame's valid time
     * @param nearest50Km the frame's rounded value ({@value WxRound#NONE} for none)
     */
    public double addRadar(Instant validTime, double nearest50Km) {
        long t = validTime.toEpochMilli();
        boolean replaced = false;
        for (long[] e : radarHistory) {
            if (e[0] == t) {
                e[1] = Double.doubleToLongBits(nearest50Km);
                replaced = true;
            }
        }
        if (!replaced) {
            radarHistory.addLast(new long[] {t, Double.doubleToLongBits(nearest50Km)});
        }
        long newest = radarHistory.stream().mapToLong(e -> e[0]).max().orElse(t);
        long cutoff = newest - RADAR_WINDOW.toMillis();
        Iterator<long[]> it = radarHistory.iterator();
        while (it.hasNext()) {
            if (it.next()[0] <= cutoff) {
                it.remove();
            }
        }
        double min = WxRound.NONE;
        for (long[] e : radarHistory) {
            min = Math.min(min, Double.longBitsToDouble(e[1]));
        }
        return min;
    }

    /** Clears what depends on the place's position (it moved). */
    void moved() {
        radarHistory.clear();
        radarFrame = null;
        hailFrame = null;
        precipFrame = null;
        smokeRun = null;
        gfsRun = null;
    }

    /** The place's current payload. */
    public Map<String, Object> payload() {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("place", name);
        p.put("radiusKm", radiusKm == Math.rint(radiusKm) ? (Object) (long) radiusKm : radiusKm);
        Map<String, Object> r = radar.toMap();
        Object n10 = radar.metric("nearest50Km10m");
        r.put("verifiedNearest50Km10m", radar.ok() && n10 instanceof Number n ? n : 0);
        p.put("radar", r);
        p.put("hail", hail.toMap());
        p.put("precip", precip.toMap());
        Map<String, Object> l = lightning.toMap();
        Object n30 = lightning.metric("nearestKm30m");
        l.put("verifiedNearestKm30m", lightning.ok() && n30 instanceof Number n ? n : 0);
        p.put("lightning", l);
        p.put("cell", cell.toMap());
        p.put("smoke", smoke.toMap());
        Map<String, Object> forecast = new LinkedHashMap<>();
        forecast.put("gfs", gfs.toMap());
        p.put("forecast", forecast);
        p.put("automated", true);
        p.put("notAWarning", true);
        return p;
    }

    /** The reflectivity metrics of one scan: thresholds 40, 50 and 60 dBZ. */
    public static Map<String, Object> radarMetrics(PlaceGridIndex.Scan scan, double nearest50Km10m) {
        Map<String, Object> m = new LinkedHashMap<>();
        double max = Double.isNaN(scan.maxInRadius()) ? 0 : Math.max(0, scan.maxInRadius());
        m.put("maxDbzInRadius", WxRound.dbz(max));
        m.put("nearest40Km", WxRound.km(scan.nearestKm()[0]));
        m.put("nearest50Km", WxRound.km(scan.nearestKm()[1]));
        m.put("nearest50Km10m", nearest50Km10m);
        m.put("nearest60Km", WxRound.km(scan.nearestKm()[2]));
        return m;
    }

    /** The hail metrics of one scan: threshold 25 mm. */
    public static Map<String, Object> hailMetrics(PlaceGridIndex.Scan scan) {
        Map<String, Object> m = new LinkedHashMap<>();
        double max = Double.isNaN(scan.maxInRadius()) ? 0 : Math.max(0, scan.maxInRadius());
        m.put("meshMaxMmInRadius", WxRound.whole(max));
        m.put("meshNearest25Km", WxRound.km(scan.nearestKm()[0]));
        return m;
    }

    /** True when a scan shows coverage at the place: at least half the cells in the radius hold a value. */
    public static boolean covered(PlaceGridIndex.Scan scan) {
        return scan.radiusCells() > 0 && scan.coveredInRadius() * 2 >= scan.radiusCells();
    }
}
