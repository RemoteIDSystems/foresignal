package ai.foresignal.outlook;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.derive.DerivedGenerator;
import ai.foresignal.derive.DerivedInputs;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.outlook.index.RegionalIndexDefEntity;
import ai.foresignal.outlook.index.RegionalIndexDefRepository;
import ai.foresignal.outlook.index.RegionalIndexValueEntity;
import ai.foresignal.outlook.index.RegionalIndexValueRepository;
import ai.foresignal.outlook.regional.OutlookViews;
import ai.foresignal.outlook.regional.RegionalOutlookEntity;
import ai.foresignal.outlook.regional.RegionalOutlookRepository;
import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

/**
 * {@code foresignal-outlook} (prompt 126): the DERIVED source that publishes regional outlooks and index values as
 * ordinary records, so 017 rules, SSE, tiles and search use them unchanged.
 *
 * <ul>
 *   <li>Kind {@value OutlookRecords#OUTLOOK_KIND}, {@code externalId = {region}:{family}:{windowStart}}: one record per
 *       window of the latest outlooks (and of older ones when a window resolves), carrying its quantiles, ledger
 *       question, actual once resolved, the skill and p10–p90 coverage the outlook was published with, and the
 *       {@code LOW_REPORTING_DENSITY} and {@code LOW_DETECTABILITY} flags; emitted on change.
 *   <li>Kind {@value OutlookRecords#INDEX_KIND}, {@code externalId = {indexId}:{region}}: the newest value of each
 *       index and region with its decomposition, weight origin and skill; emitted when it moves by
 *       {@value #INDEX_CHANGE_POINTS} point or more.
 * </ul>
 * Outlooks and indices are derived counts and composites, never raw {@code LOCAL_ONLY} values, and never private.
 * Analytic retention class {@code intel} (1825 days).
 */
@Component
public class ForesignalOutlookGenerator implements DerivedGenerator {

    /** The source id. */
    public static final String ID = OutlookRecords.SOURCE_ID;

    /** The move of an index value that re-emits its record. */
    public static final double INDEX_CHANGE_POINTS = 1;

    /** Rows read per page. */
    static final int PAGE = 500;

    /** The overlap of an incremental pass, minutes. */
    static final int OVERLAP_MINUTES = 5;

    private static final GeometryFactory GEOMETRY = new GeometryFactory(new PrecisionModel(), 4326);

    private final SourceDescriptor descriptor = buildDescriptor();
    private final RegionalOutlookRepository outlooks;
    private final RegionalIndexValueRepository values;
    private final RegionalIndexDefRepository defs;
    private final ObjectProvider<CountryCentroids> countries;
    /** Outlook window records: external id → payload hash (null when unknown since a reconcile). */
    private final Map<String, Integer> publishedOutlooks = new HashMap<>();
    /** Index records: external id → value last emitted (null when unknown since a reconcile). */
    private final Map<String, Double> publishedIndices = new HashMap<>();
    private Instant watermark;

    /** Creates the generator. */
    public ForesignalOutlookGenerator(RegionalOutlookRepository outlooks, RegionalIndexValueRepository values,
            RegionalIndexDefRepository defs, ObjectProvider<CountryCentroids> countries) {
        this.outlooks = outlooks;
        this.values = values;
        this.defs = defs;
        this.countries = countries;
    }

    /** The descriptor (analytic retention class {@code intel}: 1825 days). */
    public static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("Regional outlooks and indices (derived)")
                .description("Foresignal's CAST-style regional count outlooks (six 4-week windows with their measured"
                        + " accuracy) and its transparent regional indices (value and decomposition) from prompt 126,"
                        + " as ordinary records so rules, SSE, tiles and search use them unchanged.")
                .kinds(OutlookRecords.OUTLOOK_KIND, OutlookRecords.INDEX_KIND)
                .attribution("Derived by Foresignal from stored versions and reference history")
                .licence("derived")
                .mode(Mode.DERIVED)
                .defaultBudget(Budget.of(1, 1))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(null)
                .zeroResultsNormal(true)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(Duration.ofDays(1825))
                .build();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public DerivedInputs inputs(ValidatedParams params) {
        return new DerivedInputs(Set.of(), Set.of(), Set.of(), Duration.ofSeconds(60), false);
    }

    /** Rebuilds the published sets from the source's own live heads; the next tick is a full pass. */
    @Override
    public synchronized void reconcile(List<HeadSnapshot> ownHeads, DerivedContext ctx) {
        publishedOutlooks.clear();
        publishedIndices.clear();
        for (HeadSnapshot head : ownHeads) {
            if (head.tombstone()) {
                continue;
            }
            if (OutlookRecords.OUTLOOK_KIND.equals(head.kind())) {
                publishedOutlooks.put(head.externalId(), null);
            } else if (OutlookRecords.INDEX_KIND.equals(head.kind())) {
                Object v = head.payload() == null ? null : head.payload().get("value");
                publishedIndices.put(head.externalId(), v instanceof Number n ? n.doubleValue() : null);
            }
        }
        watermark = null;
    }

    @Override
    public synchronized void onTick(Instant now, DerivedContext ctx) {
        boolean full = watermark == null;
        if (full) {
            for (RegionalOutlookEntity o : outlooks.findByLatestTrueOrderByIdAsc()) {
                emitOutlook(o, ctx);
            }
        } else {
            int page = 0;
            Slice<RegionalOutlookEntity> slice;
            do {
                slice = outlooks.findByUpdatedAtGreaterThanEqualOrderByIdAsc(watermark.minus(Duration.ofMinutes(
                        OVERLAP_MINUTES)), PageRequest.of(page++, PAGE));
                for (RegionalOutlookEntity o : slice.getContent()) {
                    emitOutlook(o, ctx);
                }
            } while (slice.hasNext());
        }
        if (full) {
            for (RegionalIndexDefEntity def : defs.findAll()) {
                values.findTop1ByDefIdOrderByDayDesc(def.getId()).ifPresent(newest -> {
                    for (RegionalIndexValueEntity v : values.findByDefIdAndDayOrderByRegionAsc(def.getId(),
                            newest.getDay())) {
                        emitIndex(v, def, ctx);
                    }
                });
            }
        } else {
            Map<String, RegionalIndexDefEntity> byId = new HashMap<>();
            int page = 0;
            Slice<RegionalIndexValueEntity> slice;
            do {
                slice = values.findByComputedAtGreaterThanEqualOrderByIdAsc(watermark.minus(Duration.ofMinutes(
                        OVERLAP_MINUTES)), PageRequest.of(page++, PAGE));
                for (RegionalIndexValueEntity v : slice.getContent()) {
                    RegionalIndexDefEntity def = byId.computeIfAbsent(v.getDefId(), id -> defs.findById(id)
                            .orElse(null));
                    RegionalIndexValueEntity newest = values.findTop1ByDefIdOrderByDayDesc(v.getDefId()).orElse(v);
                    if (def != null && v.getDay().equals(newest.getDay())) {
                        emitIndex(v, def, ctx);
                    }
                }
            } while (slice.hasNext());
        }
        watermark = now;
    }

    private void emitOutlook(RegionalOutlookEntity o, DerivedContext ctx) {
        for (Map<String, Object> window : o.getWindows()) {
            String externalId = OutlookRecords.outlookExternalId(o.getRegion(), o.getFamily(),
                    String.valueOf(window.get("windowStart")));
            Map<String, Object> payload = outlookPayload(o, window);
            int hash = payload.hashCode();
            Integer last = publishedOutlooks.get(externalId);
            if (last == null || last != hash) {
                NormalizedRecord.Builder b = NormalizedRecord.builder(externalId, OutlookRecords.OUTLOOK_KIND)
                        .observedAt(o.getUpdatedAt()).putAll(payload);
                Point p = point(o.getRegion());
                if (p != null) {
                    b.geometry(p);
                }
                ctx.emit(b.build());
                publishedOutlooks.put(externalId, hash);
            }
        }
    }

    private void emitIndex(RegionalIndexValueEntity v, RegionalIndexDefEntity def, DerivedContext ctx) {
        String externalId = OutlookRecords.indexExternalId(v.getDefId(), v.getRegion());
        Double last = publishedIndices.get(externalId);
        if (publishedIndices.containsKey(externalId) && last != null
                && Math.abs(last - v.getValue()) < INDEX_CHANGE_POINTS) {
            return;
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(externalId, OutlookRecords.INDEX_KIND)
                .observedAt(v.getComputedAt()).putAll(indexPayload(v, def));
        Point p = point(v.getRegion());
        if (p != null) {
            b.geometry(p);
        }
        ctx.emit(b.build());
        publishedIndices.put(externalId, v.getValue());
    }

    /** The record payload of one outlook window. */
    public static Map<String, Object> outlookPayload(RegionalOutlookEntity o, Map<String, Object> window) {
        Map<String, Object> payload = new LinkedHashMap<>(OutlookViews.window(window));
        payload.put("region", o.getRegion());
        payload.put("regionLevel", o.getRegionLevel());
        payload.put("family", o.getFamily());
        payload.put("category", o.getCategory());
        payload.put("outlookId", o.getId());
        payload.put("publishedAt", o.getPublishedAt().toString());
        payload.put("modelVersion", o.getModelVersion());
        payload.put("manifestId", o.getManifestId());
        payload.put("skill", o.getSkill());
        payload.put("intervalCoverageP10P90", o.getSkill() == null ? null : o.getSkill().get("intervalCoverageP10P90"));
        payload.put("lowReportingDensity", o.isLowReportingDensity());
        payload.put("lowDetectability", o.isLowDetectability());
        List<String> codes = new ArrayList<>();
        for (Map<String, Object> c : o.getCaveats()) {
            if (c.get("code") != null) {
                codes.add(c.get("code").toString());
            }
        }
        payload.put("caveats", codes);
        payload.put("private", false);
        payload.put("automated", true);
        return payload;
    }

    /** The record payload of an index value. */
    public static Map<String, Object> indexPayload(RegionalIndexValueEntity v, RegionalIndexDefEntity def) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("indexId", v.getDefId());
        payload.put("name", def.getName());
        payload.put("region", v.getRegion());
        payload.put("day", v.getDay().toString());
        payload.put("value", v.getValue());
        payload.put("components", v.getComponents());
        payload.put("floorsApplied", v.getFloorsApplied());
        payload.put("delta24h", v.getDelta24h());
        payload.put("delta7d", v.getDelta7d());
        payload.put("weightsOrigin", def.getWeightsOrigin());
        payload.put("weightsVersion", v.getWeightsVersion());
        payload.put("skill", def.getSkill());
        payload.put("manifestId", v.getManifestId());
        payload.put("private", false);
        payload.put("automated", true);
        return payload;
    }

    /** The label point of a region's country, or null. */
    private Point point(String region) {
        CountryCentroids table = countries.getIfAvailable();
        if (table == null || region == null || region.length() < 2) {
            return null;
        }
        return table.byIso2(region.substring(0, 2)).map(c -> GEOMETRY.createPoint(new Coordinate(c.labelLon(),
                c.labelLat()))).orElse(null);
    }

    /** True when an outlook window record is currently published (tests). */
    synchronized boolean isPublished(String externalId) {
        return publishedOutlooks.containsKey(externalId) || publishedIndices.containsKey(externalId);
    }
}
