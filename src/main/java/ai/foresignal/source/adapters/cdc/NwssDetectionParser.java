package ai.foresignal.source.adapters.cdc;

import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;
import tools.jackson.databind.JsonNode;

/**
 * Parses a CDC NWSS detections dataset into {@code wastewater-detection} records (prompt 095).
 *
 * <p>A pure function of its inputs (adapter rule 3). Rows are folded to the latest sample per site and
 * {@code pcr_target} ({@code externalId = {site}:{pcr_target}}); the geometry is the county interior point the
 * adapter resolves from {@code county_fips}. {@code observedAt} is {@code date_updated}, {@code eventTime} is
 * {@code sample_collect_date}. A detection flip ({@code pcr_target_detect} changing) is a significant change
 * because the payload carries {@code detected}.
 *
 * <p><strong>Licence split</strong> as {@link NwssWvalParser}: WastewaterSCAN rows are CC BY-NC 4.0 and are
 * dropped unless {@code includeWastewaterScan} is set.
 */
public final class NwssDetectionParser {

    /** The record kind. */
    public static final String KIND = "wastewater-detection";

    private NwssDetectionParser() {}

    /** The parser options. */
    public record Options(boolean includeWastewaterScan) {}

    /**
     * One detection, the latest sample of its site and target.
     *
     * @param site the site id
     * @param target the {@code pcr_target}
     * @param detected whether the target was detected
     * @param concentration the average concentration, or null
     * @param unit the concentration unit, or null
     * @param sampleCollectDate when the sample was collected
     * @param countyFips the five-digit county FIPS, or null
     * @param populationServed the population served, or null
     * @param source the data source
     * @param observedAt the {@code date_updated} instant
     * @param wastewaterScan true when {@code source} is WastewaterSCAN
     */
    public record Detection(String site, String target, Boolean detected, Double concentration, String unit,
            Instant sampleCollectDate, String countyFips, Long populationServed, String source, Instant observedAt,
            boolean wastewaterScan) {

        /** {@code {site}:{pcr_target}}. */
        public String key() {
            return site + ":" + target;
        }
    }

    /**
     * One parsed dataset.
     *
     * @param detections the kept detections, one per site and target, in order of first appearance
     * @param rows the length of the array
     * @param skipped rows dropped for a missing key or sample date
     * @param wastewaterScanSkipped rows dropped because they were WastewaterSCAN and the parameter was off
     */
    public record Result(List<Detection> detections, int rows, int skipped, int wastewaterScanSkipped) {

        /** Copies the detections. */
        public Result {
            detections = List.copyOf(detections);
        }
    }

    /** Parses a JSON array body. */
    public static Result parse(byte[] json, Options options) {
        return parse(SodaClient.array(json, "nwss-detections"), options);
    }

    /** Parses a decoded JSON array. */
    public static Result parse(JsonNode array, Options options) {
        Map<String, Detection> latest = new LinkedHashMap<>();
        int skipped = 0;
        int wwscan = 0;
        for (JsonNode row : array) {
            if (row == null || !row.isObject()) {
                skipped++;
                continue;
            }
            String site = CdcText.text(row.get("site"));
            String target = CdcText.text(row.get("pcr_target"));
            Instant sample = CdcDates.instant(row.get("sample_collect_date"));
            if (site == null || target == null || sample == null) {
                skipped++;
                continue;
            }
            String source = CdcText.text(row.get("source"));
            boolean wastewaterScan = source != null && source.equalsIgnoreCase(NwssWvalParser.WASTEWATER_SCAN);
            if (wastewaterScan && !options.includeWastewaterScan()) {
                wwscan++;
                continue;
            }
            Detection detection = new Detection(site, target, CdcText.bool(row.get("pcr_target_detect")),
                    CdcText.number(row.get("pcr_target_avg_conc_lin")), CdcText.text(row.get("pcr_target_units")),
                    sample, CdcText.text(row.get("county_fips")), CdcText.integral(row.get("population_served")),
                    source, CdcDates.instant(row.get("date_updated")), wastewaterScan);
            Detection current = latest.get(detection.key());
            if (current == null || newer(detection, current)) {
                latest.put(detection.key(), detection);
            }
        }
        return new Result(new ArrayList<>(latest.values()), array.size(), skipped, wwscan);
    }

    /** True when {@code a} is a later sample than {@code b} (ties keep the later row). */
    static boolean newer(Detection a, Detection b) {
        return a.sampleCollectDate().isAfter(b.sampleCollectDate());
    }

    /** The {@code wastewater-detection} record of one detection. */
    public static NormalizedRecord toRecord(Detection detection, Geometry geometry) {
        NormalizedRecord.Builder b = NormalizedRecord.builder(detection.key(), KIND).observedAt(detection.observedAt())
                .eventTime(detection.sampleCollectDate()).geometry(geometry);
        b.put("detected", detection.detected());
        b.put("concentration", detection.concentration());
        b.put("unit", detection.unit());
        b.put("sampleCollectDate", detection.sampleCollectDate() == null
                ? null : detection.sampleCollectDate().toString());
        b.put("countyFips", detection.countyFips());
        b.put("populationServed", detection.populationServed());
        b.put("source", detection.source());
        if (geometry != null) {
            b.put("geometryApproximate", true);
        }
        if (detection.wastewaterScan()) {
            b.put("licenceOverride", NwssWvalParser.WASTEWATER_SCAN_LICENCE);
        }
        return b.build();
    }
}
