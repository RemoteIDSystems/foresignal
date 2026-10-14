package ai.foresignal.source.adapters.faa;

import ai.foresignal.source.kit.feature.ArcGisQueryClient;
import ai.foresignal.source.kit.feature.FeatureFetch;
import ai.foresignal.source.kit.feature.FeatureGeometry;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * The FAA UAS Facility Map grid ({@code faa-uasfm}, prompt 087): the maximum altitude at which the FAA may authorize
 * drone flight in controlled airspace, per grid cell, read through prompt 073's {@link ArcGisQueryClient}
 * ({@code outSR=4326}, {@code f=geojson}, offset paging, the {@code editingInfo.lastEditDate} short-circuit).
 *
 * <p><strong>Layer</strong> (verified 2026-10-14): {@value #LAYER_URL}, the live publication (edited that day,
 * 380,860 polygons, {@code maxRecordCount} 2000, pagination supported). The organization also publishes
 * {@code _V5} (last edited 2023), {@code _Primary} and {@code _Secondary} (2025) and test copies, which are not read.
 * Fields used: {@code CEILING} (feet when {@code UNIT} is "Feet"), {@code UNIT}, {@code MAP_EFF} and {@code LAST_EDIT}
 * (text dates {@code M/d/yyyy}), {@code GLOBALID}, {@code APT1_FAAID…APT5_FAAID}, {@code APT1_LAANC…APT5_LAANC}.
 *
 * <p><strong>Box required:</strong> the grid is national, so {@value #BBOX_PARAM} has no default and is sent as an
 * envelope intersection query; at most {@value #MAX_FEATURES_PARAM} cells (default and ceiling 50,000) per run.
 *
 * <p><strong>Incremental loads.</strong> {@code LAST_EDIT} is a text field ({@code esriFieldTypeString},
 * {@code "5/8/2017"}), so {@code LAST_EDIT > {lastMax}} would compare text, and in a quiet box the newest
 * {@code LAST_EDIT} can be years old (2022-11-15 in the Austin capture). So after a full load each run asks for the
 * cells edited on any day from {@value #OVERLAP_DAYS} days before the last complete read through today, as
 * {@code LAST_EDIT IN ('10/13/2026','10/14/2026',…)} (an exact text match, verified to work on the layer). Every
 * {@value #FULL_RELOAD_DAYS} days (the FAA chart cycle), when the box changes, or when the last read is older than
 * that, the whole box is read again and cells missing from it are tombstoned (only after a complete, non-empty
 * read).
 *
 * <p>Record kind {@code uas-facility-cell}, externalId {@code GLOBALID}, {@code observedAt} = {@code LAST_EDIT}
 * (UPSTREAM), payload {@code ceilingFt, airports} (FAA identifiers), {@code laanc} (any airport LAANC-enabled),
 * {@code mapEffective} (ISO date). Weekly poll, budget 20/min and 400/day, retention 90 days ({@code reference}).
 */
@Component
public class FaaUasfmAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "faa-uasfm";

    /** The record kind. */
    public static final String KIND = "uas-facility-cell";

    /** The layer. */
    public static final String LAYER_URL = "https://services6.arcgis.com/ssFJjBXIUyZDrSYZ/ArcGIS/rest/services/"
            + "FAA_UAS_FacilityMap_Data/FeatureServer/0";

    /** The box parameter (required). */
    public static final String BBOX_PARAM = "bbox";

    /** The feature cap parameter. */
    public static final String MAX_FEATURES_PARAM = "maxFeatures";

    /** The default and largest feature cap. */
    public static final int MAX_FEATURES = 50_000;

    /** The page size (the layer's {@code maxRecordCount}). */
    public static final int PAGE_SIZE = 2000;

    /** Days between full reloads. */
    public static final int FULL_RELOAD_DAYS = 56;

    /** The state key of the newest {@code LAST_EDIT} seen (ISO date; reported, not used to select). */
    public static final String LAST_MAX_KEY = "uasfm:lastMax";

    /** The state key of the UTC date of the last complete read, full or incremental (ISO date). */
    public static final String LAST_RUN_KEY = "uasfm:lastRun";

    /**
     * Days before the last complete read that an incremental read reaches back, in case the FAA publishes an edit some
     * days after the {@code LAST_EDIT} date it carries.
     */
    public static final int OVERLAP_DAYS = 7;

    /** The state key of the last complete full load (ISO instant). */
    public static final String FULL_AT_KEY = "uasfm:fullAt";

    /** The state key of the box the stored state belongs to. */
    public static final String BOX_KEY = "uasfm:box";

    /** The state key of the known cell ids (newline-separated, packed). */
    public static final String IDS_KEY = "uasfm:ids";

    /** The attributes requested. */
    static final List<String> OUT_FIELDS = List.of("CEILING", "UNIT", "MAP_EFF", "LAST_EDIT", "GLOBALID",
            "APT1_FAAID", "APT2_FAAID", "APT3_FAAID", "APT4_FAAID", "APT5_FAAID", "APT1_LAANC", "APT2_LAANC",
            "APT3_LAANC", "APT4_LAANC", "APT5_LAANC");

    /** Retention: the {@code reference} class. */
    static final Duration RETENTION = Duration.ofDays(90);

    private static final DateTimeFormatter US_DATE = DateTimeFormatter.ofPattern("M/d/yyyy", Locale.ROOT);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;
    private final ArcGisQueryClient arcgis = new ArcGisQueryClient();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value FaaSources#BASE_URL_PROPERTY}; blank in production
     */
    public FaaUasfmAdapter(@Value("${" + FaaSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.baseUrl = baseUrlOverride == null ? "" : baseUrlOverride;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** Requires the box, and a feature cap of 1 to 50,000. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        if (params == null || !params.has(BBOX_PARAM)) {
            problems.put(BBOX_PARAM, "is required: the UAS Facility Map grid is national; give west,south,east,north");
        }
        if (params != null && params.has(MAX_FEATURES_PARAM)) {
            Integer max = params.integer(MAX_FEATURES_PARAM);
            if (max == null || max < 1 || max > MAX_FEATURES) {
                problems.put(MAX_FEATURES_PARAM, "must be between 1 and " + MAX_FEATURES);
            }
        }
        return problems;
    }

    /** The layer URL, rebased in tests. */
    URI layerUri() {
        return FaaSources.uri(LAYER_URL, baseUrl);
    }

    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        ValidatedParams params = ctx.params() == null ? ValidatedParams.empty() : ctx.params();
        Map<String, String> problems = checkParams(params);
        if (!problems.isEmpty()) {
            throw new AdapterException(ID + ": " + problems);
        }
        Envelope box = params.bbox(BBOX_PARAM);
        int maxFeatures = params.has(MAX_FEATURES_PARAM) ? params.integer(MAX_FEATURES_PARAM) : MAX_FEATURES;
        AdapterStateStore state = ctx.state();
        String boxKey = boxText(box);
        Instant now = ctx.now();
        LocalDate today = now.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate lastMax = date(state.get(LAST_MAX_KEY).orElse(null));
        LocalDate lastRun = date(state.get(LAST_RUN_KEY).orElse(null));
        Instant fullAt = instant(state.get(FULL_AT_KEY).orElse(null));
        boolean full = fullAt == null || lastRun == null || !boxKey.equals(state.get(BOX_KEY).orElse(null))
                || fullAt.plus(Duration.ofDays(FULL_RELOAD_DAYS)).isBefore(now) || lastRun.isAfter(today)
                || lastRun.plusDays(FULL_RELOAD_DAYS).isBefore(today);
        if (full) {
            // A forced reload must not be skipped by the client's "layer unchanged" short-circuit (a new box, or the
            // 56-day cycle on a layer nobody edited).
            state.remove(ArcGisQueryClient.LAST_EDIT_KEY);
        }
        String where = full ? "1=1" : where(lastRun.minusDays(OVERLAP_DAYS), today);
        FeatureFetch fetch = arcgis.fetch(ctx, request(box, where, maxFeatures));
        if (fetch.unchanged()) {
            return FetchResult.unchanged();
        }
        List<NormalizedRecord> records = new ArrayList<>();
        int rejected = 0;
        LocalDate newest = lastMax;
        for (JsonNode feature : fetch) {
            NormalizedRecord record = record(feature);
            if (record == null) {
                rejected++;
                continue;
            }
            records.add(record);
            LocalDate edited = record.observedAt().atZone(ZoneOffset.UTC).toLocalDate();
            if (newest == null || edited.isAfter(newest)) {
                newest = edited;
            }
        }
        Set<String> known = readIds(state.get(IDS_KEY).orElse(null));
        Set<String> current = new TreeSet<>();
        records.forEach(r -> current.add(r.externalId()));
        List<NormalizedRecord> out = new ArrayList<>(records);
        if (!fetch.complete()) {
            ctx.log().warn("{}: the {} read stopped early ({}); {} cell(s), nothing tombstoned", ctx.sourceId(),
                    full ? "full" : "incremental", fetch.stopReason(), records.size());
            if (!full) {
                known.addAll(current);
                state.put(IDS_KEY, FaaSources.pack(String.join("\n", known)));
            }
            return FetchResult.of(out);
        }
        if (full) {
            if (!current.isEmpty()) {
                for (String id : known) {
                    if (!current.contains(id)) {
                        out.add(NormalizedRecord.tombstone(id, KIND, now));
                    }
                }
                known = current;
            }
            state.put(FULL_AT_KEY, now.toString());
            state.put(BOX_KEY, boxKey);
        } else {
            known.addAll(current);
        }
        state.put(IDS_KEY, FaaSources.pack(String.join("\n", known)));
        if (newest != null) {
            state.put(LAST_MAX_KEY, newest.toString());
        }
        state.put(LAST_RUN_KEY, today.toString());
        ctx.log().debug("{}: {} read, {} cell(s), {} rejected, {} tombstone(s)", ctx.sourceId(),
                full ? "full" : "incremental", records.size(), rejected, out.size() - records.size());
        return full ? FetchResult.complete(out) : FetchResult.of(out);
    }

    /** The query of one run. */
    ArcGisQueryClient.Request request(Envelope box, String where, int maxFeatures) {
        Map<String, String> extra = new LinkedHashMap<>();
        extra.put("geometry", boxText(box));
        extra.put("geometryType", "esriGeometryEnvelope");
        extra.put("inSR", "4326");
        extra.put("spatialRel", "esriSpatialRelIntersects");
        int pages = Math.max(1, (maxFeatures + PAGE_SIZE - 1) / PAGE_SIZE);
        return new ArcGisQueryClient.Request(layerUri(), where, OUT_FIELDS, PAGE_SIZE, pages, maxFeatures, null, null,
                false, Map.of(), extra);
    }

    /**
     * The incremental {@code where}: {@code LAST_EDIT IN (…)} with every day from {@code from} through {@code to} as
     * {@code M/d/yyyy}, the layer's text format.
     */
    static String where(LocalDate from, LocalDate to) {
        List<String> days = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            days.add("'" + US_DATE.format(d) + "'");
        }
        return "LAST_EDIT IN (" + String.join(",", days) + ")";
    }

    /** The record of one feature, or null without a {@code GLOBALID} or a readable {@code LAST_EDIT}. */
    static NormalizedRecord record(JsonNode feature) {
        JsonNode p = feature.get("properties");
        if (p == null || !p.isObject()) {
            return null;
        }
        String id = FaaSources.text(p.get("GLOBALID"));
        LocalDate edited = usDate(FaaSources.text(p.get("LAST_EDIT")));
        if (id == null || edited == null) {
            return null;
        }
        List<String> airports = new ArrayList<>();
        boolean laanc = false;
        for (int i = 1; i <= 5; i++) {
            String faaId = FaaSources.text(p.get("APT" + i + "_FAAID"));
            if (faaId != null) {
                airports.add(faaId);
            }
            JsonNode flag = p.get("APT" + i + "_LAANC");
            if (flag != null && flag.isNumber() && flag.intValue() == 1) {
                laanc = true;
            }
        }
        FeatureGeometry.Result geometry;
        try {
            geometry = FeatureGeometry.fromGeoJson(feature.get("geometry"));
        } catch (UpstreamFormatException e) {
            geometry = FeatureGeometry.Result.NONE;
        }
        LocalDate effective = usDate(FaaSources.text(p.get("MAP_EFF")));
        return NormalizedRecord.builder(id, KIND)
                .observedAt(edited.atStartOfDay(ZoneOffset.UTC).toInstant())
                .geometry(geometry.geometry())
                .put("ceilingFt", ceilingFeet(p.get("CEILING"), FaaSources.text(p.get("UNIT"))))
                .put("airports", airports.isEmpty() ? null : airports)
                .put("laanc", laanc)
                .put("mapEffective", effective == null ? null : effective.toString())
                .build();
    }

    /** The ceiling in feet: as given for "Feet" (or no unit), converted from meters, or null. */
    static Integer ceilingFeet(JsonNode ceiling, String unit) {
        if (ceiling == null || !ceiling.isNumber()) {
            return null;
        }
        double value = ceiling.doubleValue();
        if (unit != null && unit.toLowerCase(Locale.ROOT).startsWith("met")) {
            value = value / 0.3048;
        }
        return (int) Math.round(value);
    }

    /** A {@code M/d/yyyy} date, or null. */
    static LocalDate usDate(String text) {
        if (text == null) {
            return null;
        }
        try {
            return LocalDate.parse(text.strip(), US_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** The box as the query's {@code geometry} and the state's box key: {@code west,south,east,north}. */
    static String boxText(Envelope box) {
        return plain(box.getMinX()) + "," + plain(box.getMinY()) + "," + plain(box.getMaxX()) + ","
                + plain(box.getMaxY());
    }

    private static String plain(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private static LocalDate date(String text) {
        try {
            return text == null ? null : LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static Instant instant(String text) {
        try {
            return text == null ? null : Instant.parse(text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** The known ids stored under {@value #IDS_KEY}. */
    static Set<String> readIds(String value) {
        Set<String> out = new LinkedHashSet<>();
        String text = FaaSources.unpack(value);
        if (text != null) {
            for (String line : text.split("\n")) {
                if (!line.isBlank()) {
                    out.add(line.strip());
                }
            }
        }
        return new TreeSet<>(out);
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("FAA UAS Facility Map")
                .description("The FAA UAS Facility Map grid inside a box: the maximum altitude, in feet above ground,"
                        + " at which the FAA may authorize drone flight in each cell of controlled airspace, the"
                        + " airports concerned and whether LAANC is available. The box is required; changed cells are"
                        + " read weekly and the whole box every 56 days.")
                .kind(KIND)
                .homepage("https://udds-faa.opendata.arcgis.com")
                .attribution(FaaSources.ATTRIBUTION)
                .licence(FaaSources.LICENSE)
                .termsUrl("https://www.faa.gov/web_policies")
                .mode(Mode.POLLING)
                .param(ParamSpec.required(BBOX_PARAM, ParamType.BBOX, "The box to read, west,south,east,north"
                        + " (required: the grid is national)"))
                .param(ParamSpec.optional(MAX_FEATURES_PARAM, ParamType.INTEGER, "The most cells read per run, 1 to"
                        + " 50,000", MAX_FEATURES))
                .interval(Duration.ofDays(1), Duration.ofDays(7))
                .defaultBudget(Budget.of(20, 400))
                .maxResponseBytes(32 * 1024 * 1024)
                .fetchTimeout(Duration.ofSeconds(120))
                .followRedirects(false)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .kindRetention(KIND, RETENTION)
                .zeroResultsNormal(true)
                .titleField("ceilingFt")
                .searchFields("airports")
                .tileProperties("ceilingFt", "laanc");
        FaaSources.integer(b, "ceilingFt", "[ft_i]", "Ceiling", "The facility map altitude, feet above ground");
        FaaSources.array(b, "airports", "Airports", "The FAA identifiers of the airports the cell belongs to");
        FaaSources.bool(b, "laanc", "LAANC", "True when an airport of the cell is LAANC-enabled");
        FaaSources.text(b, "mapEffective", "Effective", "The map's effective date, ISO-8601");
        return b.build();
    }
}
