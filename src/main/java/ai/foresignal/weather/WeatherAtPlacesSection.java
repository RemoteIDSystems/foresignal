package ai.foresignal.weather;

import ai.foresignal.briefing.BriefingSection;
import ai.foresignal.briefing.BriefingSectionContributor;
import ai.foresignal.notify.MarkdownRenderer;
import ai.foresignal.place.PlaceService;
import ai.foresignal.place.PlaceView;
import ai.foresignal.raster.lightning.FlashArchive;
import ai.foresignal.raster.lightning.FlashStore;
import ai.foresignal.raster.sample.ForecastSeriesService;
import ai.foresignal.retention.RetentionState;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.FlashBatch;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.raster.sample.SampleService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * The "Weather at places" section of briefings and digests (prompt 056; Weather-Imagery-Design §7.5, OpenAPI
 * {@code PlaceWeatherDigest}), contributed through the 029 SPI for each place of {@code scope.places} (a briefing)
 * or {@code ?places=} ({@code /v1/digest}), in the place's timezone.
 *
 * <ul>
 *   <li><b>Now:</b> the radar and lightning sections of the place's {@code place-weather} head ({@code wx-places}),
 *       and the active {@code nws-alert} records intersecting the place's area;
 *   <li><b>The period</b> (the briefing lookback): the maxima of {@code radar.maxDbzInRadius} and
 *       {@code hail.meshMaxMmInRadius} over the {@code place-weather} versions in the window (by seq range, plus the
 *       version in force at its start; only {@code ok} sections count), the GLM flash total within the place radius
 *       from the 14-day flash archive, and the hazards ({@code nws-alert}, {@code probsevere-object},
 *       {@code lightning-cluster}, {@code fire-detection}) that entered the area. {@code partial} is true, and the
 *       text says "partial period", when the window starts below the retention floor of {@code wx-places}, or before
 *       the flash archive;
 *   <li><b>Next 24–72 h:</b> the NWS gridpoint summary (the place's {@code nws-forecast} head, prompt 053) and the
 *       GFS and HRRR maxima over the next 72 h (gust, precipitation, smoke), each with its run and valid time.
 * </ul>
 * A stale or unavailable part is labeled ("Lightning: data stale since 14:30"), never omitted or shown as calm.
 * Records of sources the reader may not see are never listed. The markdown is a compact table per place, which the
 * HTML rendition turns into a table; LLM briefings get the same structure as a data block with the "unknown is not
 * clear" instruction.
 */
@Component
@Order(100)
public class WeatherAtPlacesSection implements BriefingSectionContributor {

    private static final Logger log = LoggerFactory.getLogger(WeatherAtPlacesSection.class);

    /** The section key. */
    public static final String KEY = "weatherAtPlaces";

    /** The section heading. */
    public static final String HEADING = "Weather at places";

    /** The products shown when the scope names none: those backing {@code place-weather}. */
    public static final List<String> DEFAULT_PRODUCTS = List.of("mrms-reflectivity", "mrms-mesh-60m",
            "mrms-precip-rate", "hrrr-smoke", "gfs-gust", "gfs-apcp");

    /** The place-weather source. */
    static final String WX_PLACES = "wx-places";

    /** The NWS gridpoint forecast source. */
    static final String NWS_GRID = "nws-gridpoints";

    /** The NWS forecast kind. */
    static final String NWS_FORECAST = "nws-forecast";

    /** The age after which an NWS forecast is stale. */
    static final Duration NWS_MAX_AGE = Duration.ofHours(6);

    /** The forecast window of "next". */
    static final Duration NEXT_WINDOW = Duration.ofHours(72);

    /** Hazard sources that can enter a place's area. */
    static final List<String> HAZARD_SOURCES = List.of("nws-alerts", "mrms-probsevere", "goes-glm", "goes-fdc",
            "hms-fire");

    /** Hazard kinds. */
    static final List<String> HAZARD_KINDS = List.of("nws-alert", "probsevere-object", "lightning-cluster",
            "fire-detection");

    /** The most hazards listed per place. */
    static final int MAX_HAZARDS = 10;

    /** The instruction given to LLM briefings with the section. */
    public static final String LLM_INSTRUCTION = "Weather sections with status other than ok are unknown, not clear;"
            + " never state that weather is clear or calm unless the relevant section is ok. Give run and valid time"
            + " for forecast values. Weather values are automated and are not official warnings.";

    /** The note under the section. */
    static final String FOOTER = "_Automated — not an official warning. Stale or unavailable data is unknown, not calm._";

    private static final DateTimeFormatter LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm zzz", Locale.US);
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm", Locale.US);

    private final PlaceService places;
    private final WeatherRecords records;
    private final ForecastSeriesService forecasts;
    private final RetentionState retention;
    private final Function<String, HealthStatus> health;
    private final ObjectProvider<FlashStore> flashStore;
    private final Clock clock;

    /** Creates the section on the application's services. */
    @Autowired
    public WeatherAtPlacesSection(PlaceService places, WeatherRecords records, ForecastSeriesService forecasts,
            RetentionState retention, SampleService samples, SourceRegistry registry,
            ObjectProvider<FlashStore> flashStore, Clock clock) {
        this(places, records, forecasts, retention,
                id -> registry.contains(id) ? samples.sourceStatus(id) : HealthStatus.DISABLED, flashStore, clock);
    }

    /** Creates the section (tests). */
    public WeatherAtPlacesSection(PlaceService places, WeatherRecords records, ForecastSeriesService forecasts,
            RetentionState retention, Function<String, HealthStatus> health, ObjectProvider<FlashStore> flashStore,
            Clock clock) {
        this.places = places;
        this.records = records;
        this.forecasts = forecasts;
        this.retention = retention;
        this.health = health;
        this.flashStore = flashStore;
        this.clock = clock;
    }

    @Override
    public Optional<BriefingSection> contribute(Context context) {
        if (context.places().isEmpty()) {
            return Optional.empty();
        }
        List<String> products = context.weatherProducts().isEmpty() ? DEFAULT_PRODUCTS : context.weatherProducts();
        Instant now = clock.instant();
        List<PlaceView> views = new ArrayList<>();
        for (String id : context.places()) {
            places.find(id).ifPresent(views::add);
        }
        Map<String, Lightning> lightning = lightningTotals(views, context.periodFrom(), context.periodTo(),
                context.hiddenSources());
        List<Map<String, Object>> digests = new ArrayList<>();
        StringBuilder md = new StringBuilder("## ").append(HEADING).append("\n\n");
        for (PlaceView place : views) {
            Map<String, Object> digest = place(place, context, products, lightning.get(place.id()), now);
            digests.add(digest);
            markdown(md, digest, products);
        }
        md.append(FOOTER).append('\n');
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("places", digests);
        Map<String, Object> llm = new LinkedHashMap<>();
        llm.put("instruction", LLM_INSTRUCTION);
        llm.put("places", digests);
        return Optional.of(new BriefingSection(KEY, data, md.toString(), llm));
    }

    // ------------------------------------------------------------------ one place

    /** The {@code PlaceWeatherDigest} of one place. */
    Map<String, Object> place(PlaceView place, Context context, List<String> products, Lightning lightning,
            Instant now) {
        ZoneId zone = zone(place.timezone());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("placeId", place.id());
        out.put("place", place.name());
        out.put("timezone", zone.getId());
        Set<String> hidden = context.hiddenSources();
        Geometry area = area(place);
        out.put("now", now(place, area, hidden, products, now));
        out.put("period", period(place, area, context, products, lightning));
        out.put("next", next(place, hidden, products, now));
        return out;
    }

    private Map<String, Object> now(PlaceView place, Geometry area, Set<String> hidden, List<String> products,
            Instant now) {
        Map<String, Object> out = new LinkedHashMap<>();
        Map<String, Object> payload = null;
        String unavailable = null;
        if (hidden.contains(WX_PLACES)) {
            unavailable = "place weather is not visible to this reader";
        } else {
            Optional<WeatherRecords.Row> head = records.head(WX_PLACES, "place:" + place.id());
            if (head.isEmpty()) {
                unavailable = "no place-weather record yet; this is not evidence of clear weather";
            } else {
                payload = head.get().payload();
            }
        }
        if (shows(products, "mrms-reflectivity")) {
            out.put("radar", fromPayload(payload, "radar", unavailable, List.of("maxDbzInRadius", "nearest50Km")));
        }
        out.put("lightning", fromPayload(payload, "lightning", unavailable, List.of("nearestKm15m", "count15km15m")));
        List<Map<String, Object>> alerts = new ArrayList<>();
        Map<String, Object> alertsStatus;
        HealthStatus nws = health.apply(WeatherContextService.NWS);
        if (hidden.contains(WeatherContextService.NWS) || nws == HealthStatus.DISABLED) {
            alertsStatus = WeatherSections.unavailable("NWS alerts are disabled or not visible; this is not evidence"
                    + " that no alert is in effect");
        } else {
            for (WeatherRecords.Row row : records.intersecting(WeatherContextService.NWS, WeatherContextService.NWS_KIND,
                    area, now, 20)) {
                Instant expires = instant(row.payload().get("expires"));
                if (expires != null && !expires.isAfter(now)) {
                    continue;
                }
                Map<String, Object> a = new LinkedHashMap<>();
                a.put("id", row.externalId());
                a.put("event", text(row.payload().get("event")));
                a.put("severity", text(row.payload().get("severity")));
                a.put("expires", expires == null ? null : expires.toString());
                alerts.add(a);
            }
            alertsStatus = nws == HealthStatus.FAILING
                    ? WeatherSections.notOk(WeatherSections.STALE, "the nws-alerts source is failing; alerts may be"
                            + " missing", null)
                    : WeatherSections.ok(Map.of());
        }
        out.put("alerts", alerts);
        out.put("alertsStatus", alertsStatus);
        return out;
    }

    /** A section of the place-weather payload: ok with the named metrics, else its status, text and asOf only. */
    @SuppressWarnings("unchecked")
    static Map<String, Object> fromPayload(Map<String, Object> payload, String name, String unavailable,
            List<String> metrics) {
        if (payload == null) {
            return WeatherSections.unavailable(unavailable);
        }
        Object raw = payload.get(name);
        if (!(raw instanceof Map<?, ?> section)) {
            return WeatherSections.unavailable(null);
        }
        Map<String, Object> s = (Map<String, Object>) section;
        String status = WeatherSections.status(s);
        if (!WeatherSections.OK.equals(status)) {
            return WeatherSections.notOk(status, s.get("statusText") == null ? null : s.get("statusText").toString(),
                    instant(s.get("asOf")));
        }
        Map<String, Object> m = new LinkedHashMap<>();
        for (String metric : metrics) {
            m.put(metric, s.get(metric));
        }
        return WeatherSections.ok(m);
    }

    private Map<String, Object> period(PlaceView place, Geometry area, Context context, List<String> products,
            Lightning lightning) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("from", context.periodFrom().toString());
        out.put("to", context.periodTo().toString());
        long fromSeq = context.stats().fromSeq();
        long toSeq = context.stats().toSeq();
        boolean partial = fromSeq < retention.lowestRetainedSeq(Set.of(WX_PLACES));
        List<String> notes = new ArrayList<>();
        Double maxDbz = null;
        Double maxMesh = null;
        if (context.hiddenSources().contains(WX_PLACES)) {
            notes.add("place weather is not visible to this reader");
        } else {
            for (WeatherRecords.Row row : records.versions(WX_PLACES, "place:" + place.id(), fromSeq, toSeq, 5_000)) {
                maxDbz = max(maxDbz, okMetric(row.payload(), "radar", "maxDbzInRadius"));
                maxMesh = max(maxMesh, okMetric(row.payload(), "hail", "meshMaxMmInRadius"));
            }
            if (maxDbz == null) {
                notes.add("radar maximum unknown: no ok radar data in the period");
            }
        }
        if (lightning == null || lightning.total() == null) {
            notes.add("lightning total unavailable: " + (lightning == null ? "no GLM flash archive" : lightning.note()));
        } else if (lightning.partial()) {
            partial = true;
            notes.add("lightning total covers only the days in the 14-day GLM archive");
        }
        out.put("partial", partial);
        if (shows(products, "mrms-reflectivity")) {
            out.put("maxDbz", maxDbz);
        }
        if (shows(products, "mrms-mesh-60m")) {
            out.put("maxMeshMm", maxMesh);
        }
        out.put("lightningTotal", lightning == null ? null : lightning.total());
        List<Map<String, Object>> hazards = new ArrayList<>();
        List<String> sources = HAZARD_SOURCES.stream().filter(s -> !context.hiddenSources().contains(s)).toList();
        for (WeatherRecords.Row row : records.entered(sources, HAZARD_KINDS, area, fromSeq, toSeq, MAX_HAZARDS)) {
            Map<String, Object> h = new LinkedHashMap<>();
            h.put("kind", row.kind());
            h.put("title", hazardTitle(row));
            h.put("at", row.observedAt() == null ? null : row.observedAt().toString());
            h.put("ref", row.sourceId() + ":" + row.externalId() + "@" + row.seq());
            hazards.add(h);
        }
        out.put("hazardsEntered", hazards);
        out.put("notes", notes);
        return out;
    }

    private Map<String, Object> next(PlaceView place, Set<String> hidden, List<String> products, Instant now) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("nws", nws(place, hidden, now));
        Point point = point(place);
        if (products.stream().anyMatch(p -> p.startsWith("gfs"))) {
            out.put("gfs", model("gfs", point, hidden, now));
        }
        if (products.stream().anyMatch(p -> p.startsWith("hrrr"))) {
            out.put("hrrr", model("hrrr", point, hidden, now));
        }
        return out;
    }

    private Map<String, Object> nws(PlaceView place, Set<String> hidden, Instant now) {
        HealthStatus status = health.apply(NWS_GRID);
        if (hidden.contains(NWS_GRID) || status == HealthStatus.DISABLED || status == HealthStatus.FAILING) {
            return WeatherSections.unavailable("the nws-gridpoints source is "
                    + (status == HealthStatus.FAILING ? "failing" : "disabled or not visible"));
        }
        Optional<WeatherRecords.Row> head = records.head(NWS_GRID, "place:" + place.id());
        if (head.isEmpty()) {
            return WeatherSections.unavailable("no NWS forecast for this place yet");
        }
        Map<String, Object> payload = head.get().payload();
        Instant updated = instant(payload.get("forecastUpdated"));
        if (updated == null) {
            updated = head.get().observedAt();
        }
        if (updated != null && Duration.between(updated, now).compareTo(NWS_MAX_AGE) > 0) {
            return WeatherSections.notOk(WeatherSections.STALE, "the NWS forecast is older than 6 h", updated);
        }
        Map<String, Object> m = new LinkedHashMap<>();
        for (String key : List.of("popMax24h", "qpf24hMm", "gustMaxMs24h", "tMaxC24h", "tMinC24h")) {
            m.put(key, payload.get(key));
        }
        m.put("updatedAt", updated == null ? null : updated.toString());
        return WeatherSections.ok(m);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> model(String provider, Point point, Set<String> hidden, Instant now) {
        if (hidden.contains(provider)) {
            return WeatherSections.unavailable(provider.toUpperCase(Locale.ROOT) + " is not visible to this reader");
        }
        Optional<Map<String, Object>> series;
        try {
            series = forecasts.series(provider, point.getX(), point.getY());
        } catch (RuntimeException e) {
            log.debug("The {} series of a briefing place failed: {}", provider, e.toString());
            return WeatherSections.unavailable(provider.toUpperCase(Locale.ROOT) + " could not be read");
        }
        if (series.isEmpty()) {
            return WeatherSections.unavailable(provider.toUpperCase(Locale.ROOT) + " is not installed");
        }
        Map<String, Object> s = series.get();
        String status = s.get("status") == null ? WeatherSections.UNAVAILABLE : s.get("status").toString();
        String run = s.get("run") == null ? null : s.get("run").toString();
        if (WeatherSections.NO_COVERAGE.equals(status)) {
            return WeatherSections.notOk(WeatherSections.NO_COVERAGE, null, null);
        }
        if (!WeatherSections.OK.equals(status) || run == null) {
            return WeatherSections.unavailable(provider.toUpperCase(Locale.ROOT) + " has no complete run");
        }
        Instant runTime = instant(run);
        if (runTime != null && Duration.between(runTime, now).compareTo(WeatherContextService.RUN_MAX_AGE) > 0) {
            return WeatherSections.notOk(WeatherSections.STALE, "the newest complete " + provider.toUpperCase(Locale.ROOT)
                    + " run is older than 12 h", runTime);
        }
        Double gust = null;
        String gustTime = null;
        double precip = 0;
        boolean anyPrecip = false;
        Double smoke = null;
        Instant until = now.plus(NEXT_WINDOW);
        for (Map<String, Object> item : (List<Map<String, Object>>) s.getOrDefault("items", List.of())) {
            Instant t = instant(item.get("time"));
            if (t == null || t.isBefore(now.minus(Duration.ofHours(1))) || t.isAfter(until)) {
                continue;
            }
            if (item.get("gustMs") instanceof Number g && (gust == null || g.doubleValue() > gust)) {
                gust = g.doubleValue();
                gustTime = t.toString();
            }
            if (item.get("precipMm") instanceof Number p) {
                precip += p.doubleValue();
                anyPrecip = true;
            }
            if (item.get("smokeUgM3") instanceof Number sm && (smoke == null || sm.doubleValue() > smoke)) {
                smoke = sm.doubleValue();
            }
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("run", run);
        m.put("gustMaxMs", gust == null ? null : WeatherContextService.round1(gust));
        m.put("precipMm", anyPrecip ? WeatherContextService.round1(precip) : null);
        if ("hrrr".equals(provider)) {
            m.put("smokeMaxUgM3", smoke == null ? null : WeatherContextService.round1(smoke));
        }
        m.put("validTime", gustTime);
        return WeatherSections.ok(m);
    }

    // ------------------------------------------------------------------ lightning totals

    /**
     * A place's GLM flash total over the period.
     *
     * @param total the flashes within the place radius, or null when unknown
     * @param partial true when part of the period is older than the archive
     * @param note why the total is unknown
     */
    record Lightning(Long total, boolean partial, String note) {}

    /**
     * Each place's flash total within its radius over {@code [from, to]}, from the GLM archive in one pass per
     * satellite and day; the largest total over satellites (their fields of view overlap, so they are not summed).
     */
    Map<String, Lightning> lightningTotals(List<PlaceView> views, Instant from, Instant to, Set<String> hidden) {
        Map<String, Lightning> out = new LinkedHashMap<>();
        if (views.isEmpty()) {
            return out;
        }
        HealthStatus glm = health.apply(WeatherContextService.GLM);
        FlashStore store = flashStore == null ? null : flashStore.getIfAvailable();
        if (hidden.contains(WeatherContextService.GLM) || glm == HealthStatus.DISABLED || store == null) {
            views.forEach(v -> out.put(v.id(), new Lightning(null, false, "GOES GLM is disabled or not installed")));
            return out;
        }
        FlashArchive archive = store.archive();
        Instant floor = clock.instant().minus(Duration.ofDays(FlashStore.ARCHIVE_DAYS));
        boolean partial = from.isBefore(floor);
        Instant start = partial ? floor : from;
        try {
            double[][] points = new double[views.size()][];
            double[] radius = new double[views.size()];
            for (int i = 0; i < views.size(); i++) {
                Point p = point(views.get(i));
                points[i] = new double[] {p.getX(), p.getY()};
                radius[i] = views.get(i).radiusKm() == null || views.get(i).radiusKm() <= 0 ? 20.0 : views.get(i).radiusKm();
            }
            long[] best = new long[views.size()];
            for (String satellite : archive.satellites()) {
                long[] counts = new long[views.size()];
                LocalDate day = start.atZone(ZoneOffset.UTC).toLocalDate();
                LocalDate last = to.atZone(ZoneOffset.UTC).toLocalDate();
                while (!day.isAfter(last)) {
                    FlashBatch batch = archive.read(satellite, day, start.toEpochMilli(), to.toEpochMilli());
                    for (int k = 0; k < batch.size(); k++) {
                        for (int i = 0; i < points.length; i++) {
                            if (Geo.haversineKm(points[i][1], points[i][0], batch.lat()[k], batch.lon()[k]) <= radius[i]) {
                                counts[i]++;
                            }
                        }
                    }
                    day = day.plusDays(1);
                }
                for (int i = 0; i < best.length; i++) {
                    best[i] = Math.max(best[i], counts[i]);
                }
            }
            for (int i = 0; i < views.size(); i++) {
                out.put(views.get(i).id(), new Lightning(best[i], partial, null));
            }
        } catch (RuntimeException e) {
            log.warn("The GLM archive could not be read for a briefing: {}", e.toString());
            views.forEach(v -> out.put(v.id(), new Lightning(null, false, "the GLM archive could not be read")));
        }
        return out;
    }

    // ------------------------------------------------------------------ markdown

    @SuppressWarnings("unchecked")
    private static void markdown(StringBuilder md, Map<String, Object> digest, List<String> products) {
        ZoneId zone = zone((String) digest.get("timezone"));
        md.append("### ").append(escape((String) digest.get("place"))).append(" (").append(zone.getId()).append(")\n\n");
        md.append("| Section | Weather |\n|---|---|\n");
        Map<String, Object> now = (Map<String, Object>) digest.get("now");
        if (now.get("radar") instanceof Map<?, ?> radar) {
            row(md, "Now: radar", section((Map<String, Object>) radar, "Radar", zone, s ->
                    "max " + WeatherSummary.number(s.get("maxDbzInRadius")) + " dBZ in the radius; nearest 50 dBZ "
                            + km(s.get("nearest50Km"))));
        }
        row(md, "Now: lightning", section((Map<String, Object>) now.get("lightning"), "Lightning", zone, s ->
                WeatherSummary.number(s.get("count15km15m")) + " GLM flashes within 15 km (15 min); nearest "
                        + km(s.get("nearestKm15m"))));
        Map<String, Object> alertsStatus = (Map<String, Object>) now.get("alertsStatus");
        List<Map<String, Object>> alerts = (List<Map<String, Object>>) now.get("alerts");
        String alertText;
        if (!WeatherSections.isOk(alertsStatus) && alerts.isEmpty()) {
            alertText = label("NWS alerts", alertsStatus, zone);
        } else if (alerts.isEmpty()) {
            alertText = "none active";
        } else {
            List<String> parts = new ArrayList<>();
            for (Map<String, Object> a : alerts) {
                Instant expires = instant(a.get("expires"));
                parts.add(escape(WeatherSummary.clean(String.valueOf(a.get("event"))))
                        + (a.get("severity") == null ? "" : " (" + escape(String.valueOf(a.get("severity")))
                                + (expires == null ? "" : ", until " + LOCAL.format(expires.atZone(zone))) + ")"));
            }
            alertText = String.join("; ", parts) + (WeatherSections.isOk(alertsStatus) ? ""
                    : "; " + label("NWS alerts", alertsStatus, zone));
        }
        row(md, "Now: NWS alerts", alertText);
        Map<String, Object> period = (Map<String, Object>) digest.get("period");
        List<String> p = new ArrayList<>();
        if (period.containsKey("maxDbz")) {
            p.add("max " + (period.get("maxDbz") == null ? "unknown" : WeatherSummary.number(period.get("maxDbz")) + " dBZ"));
        }
        if (period.containsKey("maxMeshMm")) {
            p.add("MESH " + (period.get("maxMeshMm") == null ? "unknown" : WeatherSummary.number(period.get("maxMeshMm"))
                    + " mm"));
        }
        p.add(period.get("lightningTotal") == null ? "lightning total unavailable"
                : WeatherSummary.number(period.get("lightningTotal")) + " GLM flashes in the radius");
        String periodText = String.join("; ", p);
        if (Boolean.TRUE.equals(period.get("partial"))) {
            periodText += " (partial period)";
        }
        row(md, "Period", periodText);
        List<Map<String, Object>> hazards = (List<Map<String, Object>>) period.get("hazardsEntered");
        if (!hazards.isEmpty()) {
            List<String> h = new ArrayList<>();
            for (Map<String, Object> hz : hazards) {
                Instant at = instant(hz.get("at"));
                h.add(escape(WeatherSummary.clean(String.valueOf(hz.get("title")))) + " ("
                        + escape(String.valueOf(hz.get("kind"))) + (at == null ? "" : ", " + CLOCK.format(at.atZone(zone)))
                        + ")");
            }
            row(md, "Hazards that entered", String.join("; ", h));
        }
        Map<String, Object> next = (Map<String, Object>) digest.get("next");
        row(md, "Next: NWS", section((Map<String, Object>) next.get("nws"), "NWS forecast", zone, s ->
                "PoP " + WeatherSummary.number(s.get("popMax24h")) + " %, QPF " + WeatherSummary.number(s.get("qpf24hMm"))
                        + " mm, gusts " + WeatherSummary.number(s.get("gustMaxMs24h")) + " m/s, "
                        + WeatherSummary.number(s.get("tMinC24h")) + " to " + WeatherSummary.number(s.get("tMaxC24h"))
                        + " °C (24 h)"));
        for (String provider : List.of("gfs", "hrrr")) {
            if (next.get(provider) instanceof Map<?, ?> m) {
                String name = provider.toUpperCase(Locale.ROOT);
                row(md, "Next: " + name, section((Map<String, Object>) m, name, zone, s -> model(s, zone)));
            }
        }
        md.append('\n');
    }

    private static String model(Map<String, Object> s, ZoneId zone) {
        List<String> parts = new ArrayList<>();
        if (s.get("gustMaxMs") != null) {
            Instant t = instant(s.get("validTime"));
            parts.add("gusts up to " + WeatherSummary.number(s.get("gustMaxMs")) + " m/s"
                    + (t == null ? "" : " at " + LOCAL.format(t.atZone(zone))));
        }
        if (s.get("precipMm") != null) {
            parts.add(WeatherSummary.number(s.get("precipMm")) + " mm precipitation");
        }
        if (s.get("smokeMaxUgM3") != null) {
            parts.add("smoke up to " + WeatherSummary.number(s.get("smokeMaxUgM3")) + " µg/m³");
        }
        if (parts.isEmpty()) {
            parts.add("no values in the next 72 h");
        }
        return String.join(", ", parts) + " (run " + s.get("run") + ")";
    }

    private static String section(Map<String, Object> s, String name, ZoneId zone,
            Function<Map<String, Object>, String> ok) {
        return WeatherSections.isOk(s) ? ok.apply(s) : label(name, s, zone);
    }

    /** The label of a non-ok part: "Lightning: data stale since 14:30". */
    static String label(String name, Map<String, Object> s, ZoneId zone) {
        String status = WeatherSections.status(s);
        Instant asOf = instant(s.get("asOf"));
        return switch (status) {
            case WeatherSections.STALE -> name + ": data stale" + (asOf == null ? "" : " since "
                    + CLOCK.format(asOf.atZone(zone)));
            case WeatherSections.NO_COVERAGE -> name + ": no coverage (not evidence of clear weather)";
            default -> name + ": unavailable (not evidence of clear weather)";
        };
    }

    private static void row(StringBuilder md, String label, String value) {
        md.append("| ").append(label).append(" | ").append(value.replace("|", "\\|").replace('\n', ' ')).append(" |\n");
    }

    private static String km(Object value) {
        if (value instanceof Number n && n.doubleValue() >= 999) {
            return "none within 100 km";
        }
        return WeatherSummary.number(value) + " km";
    }

    private static String escape(String text) {
        return MarkdownRenderer.escapeMarkdown(text == null ? "" : text).replace("|", "\\|");
    }

    // ------------------------------------------------------------------ helpers

    private static boolean shows(List<String> products, String product) {
        return products.contains(product);
    }

    private static Double okMetric(Map<String, Object> payload, String section, String metric) {
        if (payload.get(section) instanceof Map<?, ?> s && WeatherSections.isOk(s) && s.get(metric) instanceof Number n) {
            return n.doubleValue();
        }
        return null;
    }

    private static Double max(Double a, Double b) {
        if (a == null) {
            return b;
        }
        return b == null ? a : Math.max(a, b);
    }

    private static String hazardTitle(WeatherRecords.Row row) {
        for (String key : List.of("headline", "event", "title", "name")) {
            if (row.payload().get(key) != null) {
                return row.payload().get(key).toString();
            }
        }
        return row.kind() + " " + row.externalId();
    }

    private static Geometry area(PlaceView place) {
        if (place.area() != null && !place.area().isEmpty()) {
            return place.area();
        }
        double radius = place.radiusKm() == null || place.radiusKm() <= 0 ? 20.0 : place.radiusKm();
        return WeatherContextService.bufferKm(point(place), radius);
    }

    private static Point point(PlaceView place) {
        Geometry g = place.geometry() == null || place.geometry().isEmpty() ? place.area() : place.geometry();
        return g instanceof Point p ? p : g.getInteriorPoint();
    }

    private static ZoneId zone(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return ZoneOffset.UTC;
        }
        try {
            return ZoneId.of(timezone);
        } catch (RuntimeException e) {
            return ZoneOffset.UTC;
        }
    }

    private static String text(Object value) {
        return value == null ? null : value.toString();
    }

    private static Instant instant(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value.toString());
        } catch (RuntimeException e) {
            try {
                return java.time.OffsetDateTime.parse(value.toString()).toInstant();
            } catch (RuntimeException inner) {
                return null;
            }
        }
    }
}
