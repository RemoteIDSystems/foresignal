package ai.foresignal.source.adapters.satdump;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One pending or recent pass of a SatDump station, as kept in the adapter state (prompt 049).
 *
 * <p>A plain mutable bean with public fields so it round-trips through JSON unchanged; times are epoch
 * milliseconds. {@link PassBook} owns every change.
 */
public final class PassState {

    /** {@code {station}:{satKey}:{aosEpochMinute}}. */
    public String externalId;

    /** The satellite key. */
    public String satKey;

    /** The satellite name as SatDump gave it. */
    public String satellite;

    /** The NORAD number, or null. */
    public Integer norad;

    /** The pipeline id, or null when the station did not say. */
    public String pipelineId;

    /** The receive frequency, or null. */
    public Long frequencyHz;

    /** AOS (tracking start, or the dataset timestamp for a pass minted from a product). */
    public long aos;

    /** LOS, or null while receiving or for a pass minted from a product. */
    public Long los;

    /** The first lock, or null. */
    public Long firstLock;

    /** True when the RF session had a lock. */
    public boolean hadLock;

    /** True when the pass was minted from a product, with no RF session. */
    public boolean fromProduct;

    /** The highest elevation, or null. */
    public Double maxElevationDeg;

    /** The predicted LOS (prompt 064), or null for a pass that was never predicted. */
    public Long predictedLos;

    /** The predicted time of maximum elevation (prompt 064), or null. */
    public Long tca;

    /** The predicted azimuth at AOS, degrees (prompt 064), or null. */
    public Double aosAzimuthDeg;

    /** The predicted azimuth at LOS, degrees (prompt 064), or null. */
    public Double losAzimuthDeg;

    /**
     * {@code predicted} or {@code missed} (prompt 064), {@code receiving}, {@code recorded}, {@code decoded} or
     * {@code failed}.
     */
    public String status;

    /** When the status was set; the record's {@code observedAt}. */
    public long statusAt;

    /** The RF session statistics, empty before LOS. */
    public Map<String, Object> rf = new LinkedHashMap<>();

    /** The instruments of the joined products. */
    public List<String> instruments = new ArrayList<>();

    /** The joined products: {@code {instrument, composite, mediaId, thumbMediaId}}. */
    public List<Map<String, Object>> products = new ArrayList<>();

    /** The TLE from {@code product.cbor} ({@code name}, {@code line1}, {@code line2}), or null. */
    public Map<String, Object> tle;

    /** The pass folders (relative to the inbox path) that were joined. */
    public List<String> datasets = new ArrayList<>();

    /** True once the missing-{@code dataset.json} health reason was raised for this pass. */
    public boolean datasetOverdue;

    /**
     * The pass geometry as WKT (SRID 4326, prompt 054): the GeoTIFF footprint, else the approximate
     * swath; null for the station point.
     */
    public String geometry;

    /** {@code footprint}, {@code swath} or null (the station point). */
    public String geometrySource;
}
