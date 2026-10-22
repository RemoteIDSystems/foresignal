package ai.foresignal.source.adapters.cdc;

import ai.foresignal.source.spi.NormalizedRecord;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;
import tools.jackson.databind.JsonNode;

/**
 * Parses the CDC NWSS site viral-activity-level dataset {@code atcp-73re} into {@code wastewater-level} records
 * (prompt 095).
 *
 * <p>A pure function of its inputs (adapter rule 3); the geometry is resolved by the adapter through
 * {@link NwssGeometry} and passed to {@link #toRecord}. {@code externalId} is {@code {site}:{pathogen_target}};
 * {@code observedAt} is {@code date_updated}, {@code eventTime} is {@code week_end}. The rows are folded to
 * <strong>one level per site and pathogen: its latest {@code week_end}</strong> (a later {@code date_updated}
 * breaks a tie), so a batch holding several weeks, or a re-dated older week, never leaves an older week current
 * (2026-10 review).
 *
 * <p><strong>License split.</strong> Rows with {@code source = WastewaterSCAN} are CC BY-NC 4.0 (Stanford) and
 * are dropped unless {@code includeWastewaterScan} is set; when kept they carry a {@code licenceOverride}
 * payload field. Every other row is public-domain US government data.
 */
public final class NwssWvalParser {

    /** The record kind. */
    public static final String KIND = "wastewater-level";

    /** The dataset id. */
    public static final String DATASET = "atcp-73re";

    /** The WastewaterSCAN source value. */
    public static final String WASTEWATER_SCAN = "WastewaterSCAN";

    /** The CC BY-NC override carried by WastewaterSCAN rows. */
    public static final String WASTEWATER_SCAN_LICENCE = "CC BY-NC 4.0 (WastewaterSCAN)";

    private NwssWvalParser() {}

    /** The parser options. */
    public record Options(boolean includeWastewaterScan) {}

    /**
     * One site level.
     *
     * @param site the site id
     * @param pathogen the pathogen target (SARS-CoV-2, Influenza A, RSV)
     * @param wval the viral activity level, one decimal
     * @param category the site's category
     * @param weekEnd the week-ending date
     * @param state the state
     * @param countiesServed the counties served, as sent
     * @param populationServed the population served, or null
     * @param source the data source (CDC or WastewaterSCAN)
     * @param observedAt the {@code date_updated} instant
     * @param eventTime the {@code week_end} instant
     * @param wastewaterScan true when {@code source} is WastewaterSCAN
     */
    public record Level(String site, String pathogen, Double wval, String category, Instant weekEndInstant,
            String state, String countiesServed, Long populationServed, String source, Instant observedAt,
            Instant eventTime, boolean wastewaterScan) {

        /** {@code {site}:{pathogen_target}}. */
        public String key() {
            return site + ":" + pathogen;
        }
    }

    /** One parsed dataset.
     *
     * @param levels the kept levels, one per site and pathogen, in order of first appearance
     * @param rows the length of the array
     * @param skipped rows dropped for a missing key, value or time, or folded into a later week of their key
     * @param wastewaterScanSkipped rows dropped because they were WastewaterSCAN and the parameter was off
     */
    public record Result(List<Level> levels, int rows, int skipped, int wastewaterScanSkipped) {

        /** Copies the levels. */
        public Result {
            levels = List.copyOf(levels);
        }
    }

    /** Parses a JSON array body. */
    public static Result parse(byte[] json, Options options) {
        return parse(SodaClient.array(json, DATASET), options);
    }

    /** Parses a decoded JSON array. */
    public static Result parse(JsonNode array, Options options) {
        Map<String, Level> latest = new LinkedHashMap<>();
        int skipped = 0;
        int wwscan = 0;
        for (JsonNode row : array) {
            if (row == null || !row.isObject()) {
                skipped++;
                continue;
            }
            String site = CdcText.text(row.get("site"));
            String pathogen = CdcText.text(row.get("pathogen_target"));
            Double wval = CdcText.number(row.get("wval"));
            Instant observedAt = CdcDates.instant(row.get("date_updated"));
            Instant eventTime = CdcDates.instant(row.get("week_end"));
            if (site == null || pathogen == null || wval == null || observedAt == null) {
                skipped++;
                continue;
            }
            String source = CdcText.text(row.get("source"));
            boolean wastewaterScan = source != null && source.equalsIgnoreCase(WASTEWATER_SCAN);
            if (wastewaterScan && !options.includeWastewaterScan()) {
                wwscan++;
                continue;
            }
            Level level = new Level(site, pathogen, oneDecimal(wval), CdcText.text(row.get("site_wval_category")),
                    eventTime, CdcText.text(row.get("state")), CdcText.text(row.get("counties_served")),
                    CdcText.integral(row.get("population_served")), source, observedAt, eventTime, wastewaterScan);
            Level current = latest.get(level.key());
            if (current == null) {
                latest.put(level.key(), level);
            } else {
                skipped++;
                if (newer(level, current)) {
                    latest.put(level.key(), level);
                }
            }
        }
        return new Result(new ArrayList<>(latest.values()), array.size(), skipped, wwscan);
    }

    /**
     * True when {@code a} describes a later week than {@code b}; for the same week, a later {@code date_updated}
     * (or, for equal ones, the later row) wins. A level without a week end is older than one with.
     */
    static boolean newer(Level a, Level b) {
        Instant aw = a.weekEndInstant();
        Instant bw = b.weekEndInstant();
        if (aw == null || bw == null) {
            return aw != null || (bw == null && !a.observedAt().isBefore(b.observedAt()));
        }
        if (!aw.equals(bw)) {
            return aw.isAfter(bw);
        }
        return !a.observedAt().isBefore(b.observedAt());
    }

    /** The {@code wastewater-level} record of one level. */
    public static NormalizedRecord toRecord(Level level, Geometry geometry) {
        NormalizedRecord.Builder b = NormalizedRecord.builder(level.key(), KIND).observedAt(level.observedAt())
                .eventTime(level.eventTime()).geometry(geometry);
        b.put("pathogen", level.pathogen());
        b.put("wval", level.wval());
        b.put("category", level.category());
        b.put("weekEnd", date(level.weekEndInstant()));
        b.put("state", level.state());
        b.put("countiesServed", level.countiesServed());
        b.put("populationServed", level.populationServed());
        b.put("source", level.source());
        if (geometry != null) {
            b.put("geometryApproximate", true);
        }
        if (level.wastewaterScan()) {
            b.put("licenceOverride", WASTEWATER_SCAN_LICENCE);
        }
        return b.build();
    }

    /** One decimal, half up. */
    static Double oneDecimal(Double value) {
        return value == null ? null
                : BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private static String date(Instant instant) {
        return instant == null ? null : instant.toString();
    }
}
