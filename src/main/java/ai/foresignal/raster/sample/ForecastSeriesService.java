package ai.foresignal.raster.sample;

import ai.foresignal.raster.FrameKeys;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.source.spi.ProductSpec;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.springframework.stereotype.Service;

/**
 * Cached point forecast series from the stored model runs (prompt 052; OpenAPI {@code ForecastSeries}):
 * GFS 3-hourly to 120 h and HRRR hourly to 18 h. NWS gridpoint series join in prompt 053.
 *
 * <p>Each provider reads the newest complete run of each of its products (prompt 051) and aligns the
 * steps by valid time: {@code gfs} wind (u/v) for speed and direction, {@code gfs-gust},
 * {@code gfs-t2m} (K → °C) and {@code gfs-apcp} (the run total, differenced step to step into the
 * interval's precipitation); {@code hrrr} wind, {@code hrrr-gust} and {@code hrrr-smoke}. A series is
 * computed once per point and set of runs and then served from a cache, so a new run invalidates it
 * naturally. Reads stored frames only; never fetches.
 */
@Service
public class ForecastSeriesService {

    /** The providers this prompt serves. */
    public static final List<String> PROVIDERS = List.of("gfs", "hrrr");

    /** The furthest step returned. */
    public static final Duration HORIZON = Duration.ofHours(120);

    private final SampleService samples;
    private final Cache<String, Map<String, Object>> cache = Caffeine.newBuilder()
            .maximumSize(500).expireAfterWrite(Duration.ofHours(6)).build();

    /** Creates the service. */
    public ForecastSeriesService(SampleService samples) {
        this.samples = samples;
    }

    /** The products of a provider: {@code [wind, gust, t2m, apcp, smoke]}, null where it has none. */
    static String[] products(String provider) {
        return switch (provider) {
            case "gfs" -> new String[] {"gfs", "gfs-gust", "gfs-t2m", "gfs-apcp", null};
            case "hrrr" -> new String[] {"hrrr", "hrrr-gust", null, null, "hrrr-smoke"};
            default -> throw new IllegalArgumentException("Unknown forecast provider " + provider);
        };
    }

    /**
     * Every provider's series at a point, from now on.
     *
     * @param excludedSources sources the caller may not read; their providers are left out
     */
    public List<Map<String, Object>> series(double lon, double lat, java.util.Set<String> excludedSources) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (String provider : PROVIDERS) {
            if (excludedSources != null && excludedSources.contains(provider)) {
                continue;
            }
            Optional<Map<String, Object>> s = series(provider, lon, lat);
            s.ifPresent(out::add);
        }
        return out;
    }

    /** One provider's series at a point, or empty when the provider's products are not installed. */
    public Optional<Map<String, Object>> series(String provider, double lon, double lat) {
        String[] ids = products(provider);
        if (samples.product(ids[0]).isEmpty()) {
            return Optional.empty();
        }
        StringBuilder key = new StringBuilder(provider).append('|').append(Math.round(lon * 1000)).append('|')
                .append(Math.round(lat * 1000));
        Instant[] runs = new Instant[ids.length];
        for (int i = 0; i < ids.length; i++) {
            if (ids[i] != null && samples.product(ids[i]).isPresent()) {
                runs[i] = samples.latestCompleteRun(ids[i]).orElse(null);
            }
            key.append('|').append(runs[i]);
        }
        Instant now = samples.now();
        Map<String, Object> cached = cache.get(key.toString(), k -> compute(provider, ids, runs, lon, lat));
        return Optional.of(trim(cached, now));
    }

    /** Drops the steps before the current hour from a cached series. */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> trim(Map<String, Object> series, Instant now) {
        Instant cut = now.minus(Duration.ofHours(1));
        List<Map<String, Object>> items = (List<Map<String, Object>>) series.get("items");
        List<Map<String, Object>> kept = new ArrayList<>();
        for (Map<String, Object> item : items) {
            if (!Instant.parse((String) item.get("time")).isBefore(cut)) {
                kept.add(item);
            }
        }
        Map<String, Object> out = new LinkedHashMap<>(series);
        out.put("items", kept);
        if (!kept.isEmpty()) {
            out.put("validFrom", kept.get(0).get("time"));
        }
        return out;
    }

    private Map<String, Object> compute(String provider, String[] ids, Instant[] runs, double lon, double lat) {
        TreeMap<Instant, Map<String, Object>> steps = new TreeMap<>();
        Instant updatedAt = null;
        boolean anyCoverage = false;
        boolean anyRun = false;
        for (int p = 0; p < ids.length; p++) {
            if (ids[p] == null || runs[p] == null) {
                continue;
            }
            anyRun = true;
            Optional<ProductSpec> spec = samples.product(ids[p]);
            if (spec.isEmpty()) {
                continue;
            }
            Double previousTotal = null;
            Instant horizon = runs[p].plus(HORIZON);
            for (RasterFrameRow row : samples.runFrames(ids[p], runs[p])) {
                if (row.validTime().isAfter(horizon)) {
                    break;
                }
                if (updatedAt == null || row.ingestedAt().isAfter(updatedAt)) {
                    updatedAt = row.ingestedAt();
                }
                Optional<FrameView> view = samples.view(row);
                if (view.isEmpty()) {
                    continue;
                }
                FrameView.Sample s = view.get().sample(lon, lat);
                if (s.kind() == FrameView.Kind.NO_COVERAGE) {
                    continue;
                }
                anyCoverage = true;
                Map<String, Object> item = steps.computeIfAbsent(row.validTime(), t -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("time", t.toString());
                    return m;
                });
                if (s.kind() != FrameView.Kind.VALUE) {
                    continue;
                }
                switch (p) {
                    case 0 -> {
                        if (!Double.isNaN(s.second())) {
                            item.put("windSpeedMs", SampleService.round(Math.hypot(s.value(), s.second()), 1));
                            item.put("windFromDeg", ai.foresignal.raster.model.VectorTileService.fromDeg(s.value(),
                                    s.second()));
                        }
                    }
                    case 1 -> item.put("gustMs", SampleService.round(s.value(), 1));
                    case 2 -> item.put("tempC", SampleService.round(s.value() - 273.15, 1));
                    case 3 -> {
                        double total = s.value();
                        if (previousTotal != null) {
                            item.put("precipMm", SampleService.round(Math.max(0, total - previousTotal), 1));
                        }
                        previousTotal = total;
                    }
                    case 4 -> item.put("smokeUgM3", SampleService.round(s.value(), 1));
                    default -> { }
                }
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("provider", provider);
        out.put("run", runs[0] == null ? null : FrameKeys.runIso(runs[0]));
        out.put("updatedAt", (updatedAt == null ? samples.now() : updatedAt).toString());
        if (!steps.isEmpty()) {
            out.put("validFrom", steps.firstKey().toString());
        }
        out.put("status", !anyRun ? "unavailable" : anyCoverage ? "ok" : "no-coverage");
        out.put("items", new ArrayList<>(steps.values()));
        return out;
    }

    /** Empties the cache (tests). */
    public void clear() {
        cache.invalidateAll();
    }
}
