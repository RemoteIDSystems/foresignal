package ai.foresignal.source.adapters.wx;

import ai.foresignal.raster.sample.PlaceGridIndex;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The in-memory state of one place's {@code place-weather} record (prompt 052; Weather-Imagery-Design
 * §7.2), and the pure payload builder.
 *
 * <p>The payload is exactly the design's: {@code place}, {@code radiusKm}, the sections {@code radar},
 * {@code hail}, {@code precip}, {@code lightning}, {@code cell}, {@code smoke}, {@code forecast.gfs} and
 * {@code forecast.nws} (prompt 053), plus {@code automated: true} and {@code notAWarning: true}.
 *
 * <p><b>The precip section</b> (prompt 053) joins four inputs: the rate at the place ({@code rateMmH}), and the
 * maxima within the radius of the 1-hour and 24-hour QPE ({@code qpe1hMmMax}, {@code qpe24hMmMax}) and of the
 * flash-flood guidance ratio ({@code ffgRatio1hPctMax}). Each input keeps the section rules on its own (stale
 * after its product's gap, previous values kept); the section's {@code status} is the worst of the rate and of
 * every other input that has had data (unavailable, then no-coverage, then stale), and its {@code asOf} the oldest
 * last-good time among the inputs that are not ok. An input whose product is disabled or not installed leaves
 * the section, metric and status, until it returns.
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

    /** Precip inputs of prompt 053: 1-hour QPE, 24-hour QPE and the FFG ratio (the rate is {@link #precip}). */
    final WxSection qpe1h;
    final WxSection qpe24h;
    final WxSection ffg;

    /** {@code forecast.nws} (prompt 053). */
    final WxSection nws;

    /** {@code forecast.ifs} (prompt 105): IFS gust and precipitation over the next 24 h. */
    WxSection ifs = new WxSection();

    /** {@code marine} (prompt 106): wave height for coastal places. */
    WxSection marine = new WxSection();

    /** {@code aurora} (prompt 106): aurora probability for places at |lat| ≥ 40°. */
    WxSection aurora = new WxSection();

    /** {@code heat} (prompt 106): WPC HeatRisk. */
    WxSection heat = new WxSection();

    /** {@code uv} (prompt 106): EPA UV index. */
    WxSection uv = new WxSection();

    /** {@code forecast.disagreement} (prompt 105), or null when the models agree. */
    Map<String, Object> disagreement;

    /** Previous disagreement spread per field, for hysteresis. */
    Map<String, Double> disagreementSpread = new LinkedHashMap<>();

    /** {@code (validTime ms, nearest50Km)} of recent reflectivity frames, oldest first. */
    final ArrayDeque<long[]> radarHistory = new ArrayDeque<>();

    String radarFrame;
    String hailFrame;
    String precipFrame;
    String qpe1hFrame;
    String qpe24hFrame;
    String ffgFrame;
    Instant smokeRun;
    Instant gfsRun;
    Instant ifsRun;
    Instant marineRun;
    String disagreementKey;

    /** A new place with fresh sections. */
    public PlaceWeatherState(String placeId, String name, double radiusKm, double lon, double lat, int version) {
        this(placeId, name, radiusKm, lon, lat, version, new WxSection(), new WxSection(), new WxSection(),
                new WxSection(), new WxSection(), new WxSection(), new WxSection(), new WxSection(), new WxSection(),
                new WxSection(), new WxSection());
    }

    /** A place with restored sections. */
    PlaceWeatherState(String placeId, String name, double radiusKm, double lon, double lat, int version, WxSection radar,
            WxSection hail, WxSection precip, WxSection lightning, WxSection cell, WxSection smoke, WxSection gfs,
            WxSection qpe1h, WxSection qpe24h, WxSection ffg, WxSection nws) {
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
        this.qpe1h = qpe1h;
        this.qpe24h = qpe24h;
        this.ffg = ffg;
        this.nws = nws;
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
        Object nws = forecast instanceof Map<?, ?> f ? ((Map<String, Object>) f).get("nws") : null;
        Object ifs = forecast instanceof Map<?, ?> f ? ((Map<String, Object>) f).get("ifs") : null;
        Object disagreement = forecast instanceof Map<?, ?> f ? ((Map<String, Object>) f).get("disagreement") : null;
        Object precip = payload.get("precip");
        PlaceWeatherState s = new PlaceWeatherState(placeId, name, radiusKm, lon, lat, version,
                WxSection.restore(strip(payload.get("radar"), "verifiedNearest50Km10m"), observedAt),
                WxSection.restore(payload.get("hail"), observedAt),
                WxSection.restore(precipPart(precip, RATE_KEY, true), observedAt),
                WxSection.restore(strip(payload.get("lightning"), "verifiedNearestKm30m"), observedAt),
                WxSection.restore(payload.get("cell"), observedAt),
                WxSection.restore(payload.get("smoke"), observedAt),
                WxSection.restore(gfs, observedAt),
                WxSection.restore(precipPart(precip, QPE_1H_KEY, false), observedAt),
                WxSection.restore(precipPart(precip, QPE_24H_KEY, false), observedAt),
                WxSection.restore(precipPart(precip, FFG_KEY, false), observedAt),
                WxSection.restore(nws, observedAt));
        s.ifs = WxSection.restore(ifs, observedAt);
        s.marine = WxSection.restore(payload.get("marine"), observedAt);
        s.aurora = WxSection.restore(payload.get("aurora"), observedAt);
        s.heat = WxSection.restore(payload.get("heat"), observedAt);
        s.uv = WxSection.restore(payload.get("uv"), observedAt);
        if (disagreement instanceof Map<?, ?> d) {
            s.disagreement = (Map<String, Object>) d;
            Object spread = d.get("spread");
            if (spread instanceof Map<?, ?> sp) {
                for (Map.Entry<?, ?> e : sp.entrySet()) {
                    if (e.getValue() instanceof Number n) {
                        s.disagreementSpread.put(e.getKey().toString(), n.doubleValue());
                    }
                }
            }
        }
        return s;
    }

    /** {@code precip.rateMmH}. */
    public static final String RATE_KEY = "rateMmH";

    /** {@code precip.qpe1hMmMax}. */
    public static final String QPE_1H_KEY = "qpe1hMmMax";

    /** {@code precip.qpe24hMmMax}. */
    public static final String QPE_24H_KEY = "qpe24hMmMax";

    /** {@code precip.ffgRatio1hPctMax}. */
    public static final String FFG_KEY = "ffgRatio1hPctMax";

    private static final List<String> PRECIP_KEYS = List.of(RATE_KEY, QPE_1H_KEY, QPE_24H_KEY, FFG_KEY);

    /**
     * One input's part of a stored precip section: its status and {@code asOf} with only its own metric.
     *
     * @param primary the rate, restored even without its metric (status only)
     * @return the part, or null when a secondary input's metric is absent (it never had data)
     */
    @SuppressWarnings("unchecked")
    static Object precipPart(Object section, String key, boolean primary) {
        if (!(section instanceof Map<?, ?> m)) {
            return primary ? section : null;
        }
        Map<String, Object> stored = (Map<String, Object>) m;
        if (!primary && !stored.containsKey(key)) {
            return null;
        }
        Map<String, Object> part = new LinkedHashMap<>(stored);
        for (String other : PRECIP_KEYS) {
            if (!other.equals(key)) {
                part.remove(other);
            }
        }
        return part;
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
        qpe1hFrame = null;
        qpe24hFrame = null;
        ffgFrame = null;
        smokeRun = null;
        gfsRun = null;
        ifsRun = null;
        marineRun = null;
        disagreementKey = null;
        disagreement = null;
        disagreementSpread.clear();
        ifs.clear();
        marine.clear();
        aurora.clear();
        heat.clear();
        uv.clear();
    }

    /**
     * The precip section of the payload: the rate and the prompt 053 inputs merged under one status (see the
     * class comment).
     */
    Map<String, Object> precipMap() {
        List<WxSection> counted = new ArrayList<>();
        counted.add(precip);
        for (WxSection part : List.of(qpe1h, qpe24h, ffg)) {
            if (part.ok() || part.lastGood() != null || part.metrics() != null) {
                counted.add(part);
            }
        }
        String status = WxSection.OK;
        Instant asOf = null;
        for (WxSection part : counted) {
            if (part.ok()) {
                continue;
            }
            if (severity(part.status()) > severity(status)) {
                status = part.status();
            }
            if (part.lastGood() != null && (asOf == null || part.lastGood().isBefore(asOf))) {
                asOf = part.lastGood();
            }
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("status", status);
        if (!WxSection.OK.equals(status)) {
            if (asOf != null) {
                m.put("asOf", asOf.toString());
            }
            m.put("statusText", WxSection.text(status));
        }
        for (WxSection part : counted) {
            if (part.metrics() != null) {
                part.metrics().forEach((k, v) -> {
                    if (v != null) {
                        m.put(k, v);
                    }
                });
            }
        }
        return m;
    }

    /** The order of non-ok statuses when sections merge: unavailable, then no-coverage, then stale. */
    static int severity(String status) {
        return switch (status) {
            case WxSection.UNAVAILABLE -> 3;
            case WxSection.NO_COVERAGE -> 2;
            case WxSection.STALE -> 1;
            default -> 0;
        };
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
        p.put("precip", precipMap());
        Map<String, Object> l = lightning.toMap();
        Object n30 = lightning.metric("nearestKm30m");
        l.put("verifiedNearestKm30m", lightning.ok() && n30 instanceof Number n ? n : 0);
        p.put("lightning", l);
        p.put("cell", cell.toMap());
        p.put("smoke", smoke.toMap());
        p.put("marine", marine.toMap());
        p.put("aurora", aurora.toMap());
        p.put("heat", heat.toMap());
        p.put("uv", uv.toMap());
        Map<String, Object> forecast = new LinkedHashMap<>();
        forecast.put("gfs", gfs.toMap());
        forecast.put("ifs", ifs.toMap());
        forecast.put("nws", nws.toMap());
        if (disagreement != null) {
            forecast.put("disagreement", disagreement);
        }
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
