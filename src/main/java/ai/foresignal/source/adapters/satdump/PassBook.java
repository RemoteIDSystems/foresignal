package ai.foresignal.source.adapters.satdump;

import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The pass lifecycle and the join of RF sessions with decoded products (prompt 049,
 * Weather-Imagery-Design §4.4): one source owns {@code satellite-pass}, so RF statistics and products
 * land in the same record.
 *
 * <ul>
 *   <li>{@code externalId} = {@code {station}:{satKey}:{aosEpochMinute}}, minted from the RF session
 *       (tracking start) when there is one, else from the {@code dataset.json} timestamp.
 *   <li>A decoded product attaches to the pass of the same satKey whose window
 *       [firstLock − 5 min, LOS + 5 min] contains the product timestamp (AOS stands in for a missing
 *       first lock, now for a missing LOS); a session that does not know its satellite (live mode)
 *       matches any satKey. With no match a new pass is minted from the product.
 *   <li>Versions: {@code receiving} at AOS, {@code recorded} at LOS (with {@code rf{}}), then
 *       {@code decoded}, or {@code failed} when no product arrived by LOS + 20 min although the session
 *       had lock. {@code predicted} belongs to prompt 064.
 * </ul>
 *
 * <p>Geometry (prompt 054, §4.4): the actual footprint of the pass's GeoTIFF overlays, else the
 * approximate swath, else the station point. A footprint is never replaced by a swath.
 *
 * <p>Not thread-safe; the station synchronizes. Every method returns the records to emit.
 */
public final class PassBook {

    /** The record kind. */
    public static final String KIND = "satellite-pass";

    /** At AOS. */
    public static final String RECEIVING = "receiving";

    /** At LOS, with the RF statistics. */
    public static final String RECORDED = "recorded";

    /** Products joined. */
    public static final String DECODED = "decoded";

    /** No product by LOS + 20 min although the session had lock. */
    public static final String FAILED = "failed";

    /** The join margin on either side of the RF window. */
    public static final Duration JOIN_MARGIN = Duration.ofMinutes(5);

    /** How long after LOS a product may arrive before the pass fails. */
    public static final Duration DECODE_DEADLINE = Duration.ofMinutes(20);

    /** How long a finished pass is kept for late products. */
    public static final Duration KEEP_FINISHED = Duration.ofHours(24);

    /** The most passes kept. */
    public static final int MAX_PASSES = 50;

    /** The health reason of a pass whose products never arrived (Weather-Imagery-Design §4.3). */
    static final String MISSING_DATASET_REASON =
            "Pass ended %s but no dataset.json after 20 min. Is `finish_processing: true` set, and server_mode off?";

    private final String station;
    private final SatNames names;
    private final List<PassState> passes = new ArrayList<>();

    /**
     * Creates the book.
     *
     * @param station the station name
     * @param names the satellite name table
     */
    public PassBook(String station, SatNames names) {
        this.station = station;
        this.names = names;
    }

    /** The pass id of a satellite and AOS. */
    public String passId(String satKey, Instant aos) {
        return station + ":" + (satKey == null ? "unknown" : satKey) + ":" + Math.floorDiv(aos.getEpochSecond(), 60);
    }

    /** Every pass kept, oldest AOS first. */
    public List<PassState> passes() {
        return List.copyOf(passes);
    }

    /** Replaces the passes with those restored from the adapter state. */
    public void restore(List<PassState> restored) {
        passes.clear();
        if (restored != null) {
            passes.addAll(restored);
        }
        passes.sort(Comparator.comparingLong(p -> p.aos));
    }

    /** The pass with {@code externalId}, if kept. */
    public Optional<PassState> find(String externalId) {
        return passes.stream().filter(p -> p.externalId.equals(externalId)).findFirst();
    }

    /**
     * AOS: a pass opens with status {@code receiving}.
     *
     * @return the {@code receiving} record
     */
    public NormalizedRecord open(RfSession session, double[] point) {
        PassState pass = find(session.passId()).orElseGet(() -> {
            PassState created = new PassState();
            created.externalId = session.passId();
            passes.add(created);
            return created;
        });
        pass.satKey = session.satKey() == null ? "unknown" : session.satKey();
        pass.satellite = session.satellite();
        pass.norad = session.satKey() == null ? null : names.norad(session.satKey());
        pass.aos = session.start().toEpochMilli();
        pass.frequencyHz = session.frequencyHz();
        pass.status = RECEIVING;
        pass.statusAt = session.start().toEpochMilli();
        prune(session.start());
        return record(pass, point);
    }

    /**
     * LOS: the pass becomes {@code recorded} with the session statistics.
     *
     * @return the {@code recorded} record, or empty when the pass was already decoded or is unknown
     */
    public Optional<NormalizedRecord> close(RfSession session, double[] point) {
        Optional<PassState> found = find(session.passId());
        if (found.isEmpty()) {
            return Optional.empty();
        }
        PassState pass = found.get();
        Instant los = session.los() == null ? session.lastActivity() : session.los();
        pass.los = los.toEpochMilli();
        pass.rf = new LinkedHashMap<>(session.aggregate());
        Instant firstLock = session.firstLock();
        pass.firstLock = firstLock == null ? null : firstLock.toEpochMilli();
        pass.hadLock = firstLock != null;
        pass.maxElevationDeg = session.maxElevation() == null ? null : Math.round(session.maxElevation() * 10) / 10.0;
        if (session.frequencyHz() != null) {
            pass.frequencyHz = session.frequencyHz();
        }
        if (DECODED.equals(pass.status)) {
            // Products arrived before LOS was seen: keep decoded, but carry the statistics.
            pass.statusAt = Math.max(pass.statusAt, pass.los);
            return Optional.of(record(pass, point));
        }
        pass.status = RECORDED;
        pass.statusAt = Math.max(pass.statusAt, pass.los);
        return Optional.of(record(pass, point));
    }

    /**
     * A decoded pass folder: joins it to a pass or mints one.
     *
     * @param satKey the product's satellite key
     * @param satellite the satellite name from {@code dataset.json}
     * @param productTime the {@code dataset.json} timestamp
     * @param now the scan time
     * @return the pass the products belong to (not yet updated with them)
     */
    public PassState join(String satKey, String satellite, Instant productTime, Instant now) {
        PassState match = null;
        long best = Long.MAX_VALUE;
        long t = productTime.toEpochMilli();
        for (PassState pass : passes) {
            boolean keyMatches = satKey.equals(pass.satKey) || "unknown".equals(pass.satKey);
            if (!keyMatches) {
                continue;
            }
            long from = (pass.firstLock != null ? pass.firstLock : pass.aos) - JOIN_MARGIN.toMillis();
            // A pass minted from a product has no RF window: a second dataset of the same pass (another
            // instrument) joins it when its timestamp is within a pass length of the first.
            long to = pass.fromProduct ? pass.aos + DECODE_DEADLINE.toMillis()
                    : (pass.los != null ? pass.los : now.toEpochMilli()) + JOIN_MARGIN.toMillis();
            if (t >= from && t <= to) {
                long distance = Math.abs(t - pass.aos);
                if (distance < best) {
                    best = distance;
                    match = pass;
                }
            }
        }
        if (match != null) {
            if ("unknown".equals(match.satKey)) {
                match.satKey = satKey;
                match.norad = names.norad(satKey);
            }
            if (match.satellite == null) {
                match.satellite = satellite;
            }
            return match;
        }
        PassState minted = new PassState();
        minted.externalId = passId(satKey, productTime);
        minted.satKey = satKey;
        minted.satellite = satellite;
        minted.norad = names.norad(satKey);
        minted.aos = t;
        minted.fromProduct = true;
        minted.status = DECODED;
        minted.statusAt = t;
        passes.add(minted);
        prune(now);
        return minted;
    }

    /**
     * Records the products of a joined pass folder: the pass becomes {@code decoded}.
     *
     * @param pass the pass from {@link #join}
     * @param dataset the pass folder, relative to the inbox path
     * @param instruments the products' instruments
     * @param products the product entries
     * @param tle the TLE, or null
     * @param now the scan time
     * @return the {@code decoded} record
     */
    public NormalizedRecord decoded(PassState pass, String dataset, List<String> instruments,
            List<Map<String, Object>> products, Map<String, Object> tle, Instant now, double[] point) {
        if (!pass.datasets.contains(dataset)) {
            pass.datasets.add(dataset);
        }
        pass.products.removeIf(p -> dataset.equals(p.get("dataset")));
        for (Map<String, Object> product : products) {
            Map<String, Object> copy = new LinkedHashMap<>(product);
            copy.put("dataset", dataset);
            pass.products.add(copy);
        }
        for (String instrument : instruments) {
            if (!pass.instruments.contains(instrument)) {
                pass.instruments.add(instrument);
            }
        }
        if (tle != null) {
            pass.tle = tle;
        }
        pass.status = DECODED;
        pass.datasetOverdue = false;
        long at = Math.max(now.toEpochMilli(), Math.max(pass.statusAt, pass.los == null ? 0 : pass.los));
        pass.statusAt = at;
        prune(now);
        return record(pass, point);
    }

    /**
     * Fails the passes whose products are overdue: recorded, with lock, and no product by LOS + 20 min.
     *
     * @return the {@code failed} records
     */
    public List<NormalizedRecord> expire(Instant now, double[] point) {
        List<NormalizedRecord> out = new ArrayList<>();
        for (PassState pass : passes) {
            if (!RECORDED.equals(pass.status) || pass.los == null) {
                continue;
            }
            long deadline = pass.los + DECODE_DEADLINE.toMillis();
            if (now.toEpochMilli() < deadline) {
                continue;
            }
            if (pass.hadLock) {
                pass.status = FAILED;
                pass.statusAt = Math.max(pass.statusAt, deadline);
                pass.datasetOverdue = true;
                out.add(record(pass, point));
            }
        }
        prune(now);
        return out;
    }

    /**
     * The missing-{@code dataset.json} health reason (Weather-Imagery-Design §4.3) of the newest pass
     * that failed for lack of products, or null when the newest finished pass with lock was decoded.
     */
    public String missingDatasetReason() {
        PassState newest = null;
        for (PassState pass : passes) {
            if (!pass.hadLock || pass.los == null) {
                continue;
            }
            if (!FAILED.equals(pass.status) && !DECODED.equals(pass.status)) {
                continue;
            }
            if (newest == null || pass.los > newest.los) {
                newest = pass;
            }
        }
        if (newest == null || !FAILED.equals(newest.status) || !newest.datasetOverdue) {
            return null;
        }
        return String.format(MISSING_DATASET_REASON, Instant.ofEpochMilli(newest.los));
    }

    /** The longest geometry WKT kept in the adapter state; a longer one is simplified further. */
    static final int MAX_GEOMETRY_CHARS = 32_000;

    /**
     * Sets a pass's geometry in the order footprint → approximate swath → station point: a footprint
     * always replaces what was there, a swath only replaces nothing or another swath.
     *
     * @param pass the pass
     * @param geometry the footprint or swath, SRID 4326; null leaves the pass unchanged
     * @param source {@code footprint} or {@code swath}
     * @return true when the geometry changed
     */
    public boolean geometry(PassState pass, org.locationtech.jts.geom.Geometry geometry, String source) {
        if (geometry == null || geometry.isEmpty() || source == null) {
            return false;
        }
        if (PolarOverlays.SWATH.equals(source) && PolarOverlays.FOOTPRINT.equals(pass.geometrySource)) {
            return false;
        }
        String wkt = wkt(geometry, 6);
        if (wkt.length() > MAX_GEOMETRY_CHARS) {
            wkt = wkt(org.locationtech.jts.simplify.TopologyPreservingSimplifier.simplify(geometry, 0.1), 4);
        }
        if (wkt.length() > MAX_GEOMETRY_CHARS) {
            wkt = wkt(geometry.getEnvelope(), 4);
        }
        boolean changed = !wkt.equals(pass.geometry) || !source.equals(pass.geometrySource);
        pass.geometry = wkt;
        pass.geometrySource = source;
        return changed;
    }

    private static String wkt(org.locationtech.jts.geom.Geometry geometry, int decimals) {
        org.locationtech.jts.geom.PrecisionModel precision =
                new org.locationtech.jts.geom.PrecisionModel(Math.pow(10, decimals));
        org.locationtech.jts.geom.Geometry reduced =
                org.locationtech.jts.precision.GeometryPrecisionReducer.reduce(geometry, precision);
        return new org.locationtech.jts.io.WKTWriter().write(reduced);
    }

    /** The geometry of a pass, or null when it has none or it does not parse. */
    static org.locationtech.jts.geom.Geometry parsedGeometry(PassState pass) {
        if (pass.geometry == null) {
            return null;
        }
        try {
            org.locationtech.jts.geom.Geometry g = new org.locationtech.jts.io.WKTReader(
                    ai.foresignal.raster.geotiff.Antimeridian.GEOMETRY).read(pass.geometry);
            return g.isEmpty() ? null : g;
        } catch (org.locationtech.jts.io.ParseException e) {
            return null;
        }
    }

    /** Drops finished passes older than a day and keeps at most {@value #MAX_PASSES}. */
    void prune(Instant now) {
        long cutoff = now.minus(KEEP_FINISHED).toEpochMilli();
        passes.removeIf(p -> (DECODED.equals(p.status) || FAILED.equals(p.status)) && p.statusAt < cutoff);
        passes.sort(Comparator.comparingLong(p -> p.aos));
        while (passes.size() > MAX_PASSES) {
            passes.remove(0);
        }
    }

    /** The record of a pass. */
    public NormalizedRecord record(PassState pass, double[] point) {
        String display = names.displayName(pass.satKey, pass.satellite);
        NormalizedRecord.Builder builder = NormalizedRecord.builder(pass.externalId, KIND)
                .observedAt(Instant.ofEpochMilli(pass.statusAt))
                .eventTime(Instant.ofEpochMilli(pass.aos))
                .put("satellite", display)
                .put("satKey", pass.satKey)
                .put("norad", pass.norad)
                .put("pipelineId", pass.pipelineId)
                .put("frequencyHz", pass.frequencyHz)
                .put("aos", Instant.ofEpochMilli(pass.aos).toString())
                .put("los", pass.los == null ? null : Instant.ofEpochMilli(pass.los).toString())
                .put("maxElevationDeg", pass.maxElevationDeg)
                .put("status", pass.status)
                .put("station", station)
                .put("title", title(display, pass.maxElevationDeg))
                .put("attribution", attribution());
        if (pass.rf != null && !pass.rf.isEmpty()) {
            builder.put("rf", pass.rf);
        }
        if (!pass.instruments.isEmpty()) {
            builder.put("instruments", List.copyOf(pass.instruments));
        }
        if (!pass.products.isEmpty()) {
            List<Map<String, Object>> products = new ArrayList<>();
            for (Map<String, Object> p : pass.products) {
                Map<String, Object> copy = new LinkedHashMap<>(p);
                copy.remove("dataset");
                products.add(copy);
            }
            builder.put("products", products);
        }
        if (pass.tle != null) {
            builder.put("tle", pass.tle);
        }
        org.locationtech.jts.geom.Geometry geometry = parsedGeometry(pass);
        if (geometry != null) {
            builder.geometry(geometry).put("geometrySource", pass.geometrySource);
        } else if (point != null) {
            builder.point(point[0], point[1]);
        }
        return builder.build();
    }

    /** "Meteor-M2-3 pass, max el 54°". */
    static String title(String satellite, Double maxElevation) {
        return maxElevation == null ? satellite + " pass" : satellite + " pass, max el " + Math.round(maxElevation) + "°";
    }

    /** The structured attribution of an operator-owned pass. */
    static Map<String, Object> attribution() {
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("text", "Operator-owned SatDump ground station");
        a.put("licence", SatDumpDescriptor.LICENSE);
        a.put("requiredDisplay", false);
        return a;
    }
}
