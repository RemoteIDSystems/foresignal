package ai.foresignal.source.adapters.hydro.nwps;

import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.adapters.hydro.HydroSources;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.source.support.SkipCounter;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * {@code nwps-gauges} (prompt 079): NOAA National Water Prediction Service river gauges in a box, with their official
 * flood category and the flood stages that make a gauge trigger meaningful ({@link NwpsGaugeMapper}).
 *
 * <ul>
 *   <li><strong>Each run:</strong> {@value #GAUGES_URL}{@code ?bbox.xmin=…&bbox.ymin=…&bbox.xmax=…&bbox.ymax=…
 *       &srid=EPSG_4326}; at most {@code maxGauges} gauges (default and ceiling {@value #MAX_GAUGES}, by lid) are
 *       kept;
 *   <li><strong>Thresholds:</strong> {@code /nwps/v1/gauges/{lid}} at most once per gauge per 24 h, cached in state
 *       ({@value #DETAIL_PREFIX}{@code {lid}}); at most {@value #DETAILS_PER_RUN} details per run, unknown gauges
 *       first and then the oldest, which spreads the calls across the day. A 404 caches "no thresholds"; any other
 *       failure keeps the cache and stops this run's refreshes. Entries of gauges no longer listed are removed.
 * </ul>
 */
@Component
public class NwpsGaugesAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "nwps-gauges";

    /** The gauge list. */
    public static final String GAUGES_URL = "https://api.water.noaa.gov/nwps/v1/gauges";

    /** The box parameter. */
    public static final String BBOX_PARAM = "bbox";

    /** The gauge-cap parameter. */
    public static final String MAX_GAUGES_PARAM = "maxGauges";

    /** The default and largest gauge cap. */
    public static final int MAX_GAUGES = 300;

    /** The largest box side, degrees. */
    public static final double MAX_BOX_DEGREES = 10.0;

    /** Threshold details read per run. */
    public static final int DETAILS_PER_RUN = 10;

    /** How long a gauge's thresholds are used before its detail is read again. */
    public static final Duration DETAIL_REFRESH = Duration.ofHours(24);

    /** The state key prefix of a gauge's cached thresholds. */
    public static final String DETAIL_PREFIX = "nwps:gauge:";

    /** The state key of the lids with cached thresholds, comma-separated. */
    public static final String INDEX_KEY = "nwps:lids";

    /** The record TTL. */
    static final Duration TTL = Duration.ofHours(3);

    private final String gaugesUrl;
    private final Supplier<MeterRegistry> meters;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value HydroSources#BASE_URL_PROPERTY}; blank in production
     * @param meters the registry of the skip counter, resolved lazily
     */
    @Autowired
    public NwpsGaugesAdapter(@Value("${" + HydroSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<MeterRegistry> meters) {
        this(baseUrlOverride, HydroSources.meters(meters));
    }

    /**
     * Creates the adapter with a plain supplier (tests).
     *
     * @param baseUrlOverride the test base URL, or blank
     * @param meters supplies the meter registry, or null
     */
    public NwpsGaugesAdapter(String baseUrlOverride, Supplier<MeterRegistry> meters) {
        this.gaugesUrl = HydroSources.uri(GAUGES_URL, baseUrlOverride).toString();
        this.meters = meters;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** Requires a box of at most 10° × 10° and a gauge cap of 1–300. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        String problem = HydroSources.boxProblem(params.has(BBOX_PARAM) ? params.bbox(BBOX_PARAM) : null,
                MAX_BOX_DEGREES);
        if (problem != null) {
            problems.put(BBOX_PARAM, problem);
        }
        Integer max = params.integer(MAX_GAUGES_PARAM);
        if (max != null && (max < 1 || max > MAX_GAUGES)) {
            problems.put(MAX_GAUGES_PARAM, "must be 1–" + MAX_GAUGES + ", but was " + max);
        }
        return problems;
    }

    /** The list URL of {@code box}. */
    public URI listUri(Envelope box) {
        return URI.create(gaugesUrl + "?bbox.xmin=" + box.getMinX() + "&bbox.ymin=" + box.getMinY() + "&bbox.xmax="
                + box.getMaxX() + "&bbox.ymax=" + box.getMaxY() + "&srid=EPSG_4326");
    }

    /** The detail URL of {@code lid}. */
    public URI detailUri(String lid) {
        return URI.create(gaugesUrl + "/" + lid);
    }

    /**
     * Reads the gauges in the box, refreshes due thresholds, and maps them.
     *
     * @throws AdapterException when the parameters are invalid or the list request fails
     * @throws UpstreamFormatException when the list is not a gauge document
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        ValidatedParams params = ctx.params() == null ? ValidatedParams.empty() : ctx.params();
        Map<String, String> problems = checkParams(params);
        if (!problems.isEmpty()) {
            throw new AdapterException(ID + ": " + problems);
        }
        URI list = listUri(params.bbox(BBOX_PARAM));
        JsonNode root = HydroSources.json(EnvSources.body(ctx, ctx.http().get(list), null, list), "NWPS gauge list");
        List<JsonNode> gauges = new ArrayList<>(NwpsGaugeMapper.gauges(root));
        gauges.sort(Comparator.comparing(g -> String.valueOf(HydroSources.text(g.get("lid")))));
        int cap = params.integer(MAX_GAUGES_PARAM, MAX_GAUGES);
        boolean capped = gauges.size() > cap;
        if (capped) {
            ctx.log().warn("{}: {} gauges in the box; only the first {} by lid are kept (maxGauges)", ID,
                    gauges.size(), cap);
            gauges = gauges.subList(0, cap);
        }
        Set<String> lids = new LinkedHashSet<>();
        for (JsonNode g : gauges) {
            String lid = HydroSources.text(g.get("lid"));
            if (lid != null) {
                lids.add(lid.toUpperCase(Locale.ROOT));
            }
        }
        Map<String, NwpsGaugeMapper.Cached> cached = thresholds(ctx, lids, !capped);
        EnvSources.Collector collector = new EnvSources.Collector(null, null);
        for (JsonNode g : gauges) {
            String lid = HydroSources.text(g.get("lid"));
            NwpsGaugeMapper.Cached c = lid == null ? null : cached.get(lid.toUpperCase(Locale.ROOT));
            collector.add(NwpsGaugeMapper.gauge(g, c == null ? null : c.thresholds()));
        }
        EnvSources.Parsed parsed = collector.result();
        SkipCounter.count(meters, HydroSources.SKIPPED_METRIC, HydroSources.SKIPPED_DESCRIPTION, ctx.sourceId(),
                parsed.skipped());
        ctx.log().debug("{}: {} gauge(s), skipped {}", ID, parsed.records().size(), parsed.skipped());
        return capped ? FetchResult.of(parsed.records()) : FetchResult.complete(parsed.records());
    }

    /**
     * The cached thresholds of {@code lids}, after refreshing up to {@value #DETAILS_PER_RUN} due ones.
     *
     * @param prune true when {@code lids} is the whole box, so entries of other gauges may be removed
     */
    Map<String, NwpsGaugeMapper.Cached> thresholds(FetchContext ctx, Set<String> lids, boolean prune) {
        AdapterStateStore state = ctx.state();
        Instant now = ctx.now();
        Map<String, NwpsGaugeMapper.Cached> cached = new LinkedHashMap<>();
        List<String> due = new ArrayList<>();
        for (String lid : lids) {
            NwpsGaugeMapper.Cached c = NwpsGaugeMapper.fromJson(state.get(DETAIL_PREFIX + lid).orElse(null));
            if (c != null) {
                cached.put(lid, c);
            }
            if (c == null || !c.readAt().plus(DETAIL_REFRESH).isAfter(now)) {
                due.add(lid);
            }
        }
        // Unknown gauges first, then the oldest.
        due.sort(Comparator.comparing((String lid) -> cached.containsKey(lid))
                .thenComparing(lid -> cached.containsKey(lid) ? cached.get(lid).readAt() : Instant.EPOCH)
                .thenComparing(lid -> lid));
        int read = 0;
        for (String lid : due) {
            if (read >= DETAILS_PER_RUN) {
                break;
            }
            read++;
            NwpsGaugeMapper.Thresholds fresh;
            try {
                URI uri = detailUri(lid);
                fresh = NwpsGaugeMapper.thresholds(HydroSources.json(
                        EnvSources.body(ctx, ctx.http().get(uri), null, uri), "NWPS gauge " + lid));
            } catch (AdapterException e) {
                if (e.httpStatus().orElse(0) == 404) {
                    ctx.log().info("{}: no detail for gauge {} (404); cached as without thresholds", ID, lid);
                    fresh = NwpsGaugeMapper.Thresholds.NONE;
                } else {
                    ctx.log().warn("{}: the detail of {} could not be read ({}); cached thresholds kept", ID, lid,
                            e.getMessage());
                    break;
                }
            } catch (UpstreamFormatException e) {
                ctx.log().warn("{}: the detail of {} did not parse ({}); cached thresholds kept", ID, lid,
                        e.getMessage());
                continue;
            }
            state.put(DETAIL_PREFIX + lid, NwpsGaugeMapper.toJson(fresh, now));
            cached.put(lid, new NwpsGaugeMapper.Cached(fresh, now));
        }
        Set<String> index = new LinkedHashSet<>();
        for (String lid : state.get(INDEX_KEY).orElse("").split(",")) {
            if (!lid.isBlank()) {
                index.add(lid.strip());
            }
        }
        if (prune) {
            for (String old : index) {
                if (!lids.contains(old)) {
                    state.remove(DETAIL_PREFIX + old);
                }
            }
            index.retainAll(lids);
        }
        index.addAll(cached.keySet());
        String joined = String.join(",", index);
        if (!joined.equals(state.get(INDEX_KEY).orElse(""))) {
            state.put(INDEX_KEY, joined);
        }
        return cached;
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = NwpsGaugeMapper.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("NWPS river gauges")
                .description("NOAA National Water Prediction Service river gauges in a box (at most 10° × 10°, at most"
                        + " 300 gauges): the observed stage and flow with the official flood category, the current"
                        + " forecast category when there is one, and the gauge's action, minor, moderate and major"
                        + " flood stages (read once per gauge per day). Trigger on floodCategory in [minor, moderate,"
                        + " major], or on stageToMinor.")
                .kind(kind)
                .homepage("https://water.noaa.gov/")
                .attribution("NOAA National Water Prediction Service")
                .licence("US-PD")
                .termsUrl("https://api.water.noaa.gov/nwps/v1/docs/")
                .mode(Mode.POLLING)
                .param(ParamSpec.required(BBOX_PARAM, ParamType.BBOX, "The box to read, west,south,east,north, at most"
                        + " 10° × 10°"))
                .param(ParamSpec.optional(MAX_GAUGES_PARAM, ParamType.INTEGER, "The most gauges kept (by lid), 1–300;"
                        + " each costs one threshold request a day", MAX_GAUGES))
                .interval(Duration.ofMinutes(10), Duration.ofMinutes(15))
                .defaultBudget(Budget.of(12, 1500))
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(TTL)
                .defaultRetention(HydroSources.STATE_RETENTION)
                .kindRetention(kind, HydroSources.STATE_RETENTION)
                .tileProperties("name", "floodCategory", "stage")
                .searchFields("name", "rfc", "wfo", "state")
                .titleField("name")
                .rankField("stage");
        HydroSources.text(b, "name", "Name", "Gauge name");
        HydroSources.text(b, "rfc", "RFC", "River Forecast Center abbreviation");
        HydroSources.text(b, "wfo", "WFO", "NWS Weather Forecast Office abbreviation");
        HydroSources.text(b, "state", "State", "State abbreviation");
        HydroSources.text(b, "usgsId", "USGS id", "The co-located USGS site number, when NWPS lists one");
        HydroSources.bool(b, "inService", "In service", "False when NWPS marks the gauge out of service");
        HydroSources.number(b, "stage", null, "Stage", "Observed stage, in stageUnit");
        HydroSources.text(b, "stageUnit", "Stage unit", "The stage unit (ft)");
        HydroSources.number(b, "flow", null, "Flow", "Observed flow, in flowUnit");
        HydroSources.text(b, "flowUnit", "Flow unit", "The flow unit (kcfs)");
        HydroSources.text(b, "floodCategory", "Flood category", "Observed category: no_flooding, action, minor,"
                + " moderate, major, low_threshold, not_defined, obs_not_current or out_of_service");
        HydroSources.text(b, "forecastCategory", "Forecast category", "The current forecast's category; absent when"
                + " no forecast is current");
        HydroSources.number(b, "forecastStage", null, "Forecast stage", "The current forecast stage, in stageUnit");
        HydroSources.timestamp(b, "forecastValidTime", "Forecast valid time", "When the current forecast is valid");
        HydroSources.number(b, "actionStage", null, "Action stage", "Action stage, in stageUnit");
        HydroSources.number(b, "minorStage", null, "Minor flood stage", "Minor flood stage, in stageUnit");
        HydroSources.number(b, "moderateStage", null, "Moderate flood stage", "Moderate flood stage, in stageUnit");
        HydroSources.number(b, "majorStage", null, "Major flood stage", "Major flood stage, in stageUnit");
        HydroSources.number(b, "stageToMinor", null, "Stage to minor flood", "Minor flood stage minus the observed"
                + " stage, in stageUnit; negative while in minor flood or worse");
        b.kindDisplay(kind, new KindDisplay("{name}", "water", "#1864AB", null));
        return b.build();
    }
}
