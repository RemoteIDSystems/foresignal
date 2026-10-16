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
 * GFS 3-hourly to 120 h and HRRR hourly to 18 h; and (prompt 053) the NWS gridpoint series of a place, hourly to
 * 72 h, read from {@code forecast_series_cache} where {@code nws-gridpoints} stores it.
 *
 * <p>Each provider reads the newest complete run of each of its products (prompt 051) and aligns the
 * steps by valid time: {@code gfs} wind (u/v) for speed and direction, {@code gfs-gust},
 * {@code gfs-t2m} (K → °C) and {@code gfs-apcp} (the run total, differenced step to step into the
 * interval's precipitation); {@code hrrr} wind, {@code hrrr-gust} and {@code hrrr-smoke}. A series is
 * computed once per point and set of runs and then served from a cache, so a new run invalidates it
 * naturally. Reads stored frames only; never fetches.
 *
 * <p>The {@code nws} series exists only for a place (it is cached per place, not per point). Its {@code status}
 * is {@code ok}, or {@code stale} when the forecast's {@code updatedAt} is older than 6 hours; its {@code run} is
 * null. It is left out when the caller may not read {@code nws-gridpoints}.
 */
@Service
public class ForecastSeriesService {

    /** The providers this service serves (prompt 105 adds {@code ifs}; prompt 106 adds {@code aifs}). */
    public static final List<String> PROVIDERS = List.of("gfs", "hrrr", "ifs", "aifs", "nws");

    /** The source behind the {@code nws} provider. */
    public static final String NWS_SOURCE = "nws-gridpoints";

    /** The age after which an NWS series is stale. */
    public static final Duration NWS_MAX_AGE = Duration.ofHours(6);

    /** The furthest step returned. */
    public static final Duration HORIZON = Duration.ofHours(120);

    private final SampleService samples;
    private final ai.foresignal.place.ForecastSeriesCache nwsCache;
    private final Cache<String, Map<String, Object>> cache = Caffeine.newBuilder()
            .maximumSize(500).expireAfterWrite(Duration.ofHours(6)).build();

    /** Creates the service without the NWS cache (tests). */
    public ForecastSeriesService(SampleService samples) {
        this(samples, null);
    }

    /**
     * Creates the service.
     *
     * @param nwsCache the place series cache of prompt 053, or null for none
     */
    @org.springframework.beans.factory.annotation.Autowired
    public ForecastSeriesService(SampleService samples, ai.foresignal.place.ForecastSeriesCache nwsCache) {
        this.samples = samples;
        this.nwsCache = nwsCache;
    }

    /** The products of a provider: {@code [wind, gust, t2m, apcp, smoke]}, null where it has none. */
    static String[] products(String provider) {
        return switch (provider) {
            case "gfs" -> new String[] {"gfs", "gfs-gust", "gfs-t2m", "gfs-apcp", null};
            case "hrrr" -> new String[] {"hrrr", "hrrr-gust", null, null, "hrrr-smoke"};
            case "ifs" -> new String[] {"ifs", "ifs-gust", "ifs-t2m", null, null};
            case "aifs" -> new String[] {"aifs", null, "aifs-t2m", "aifs-tp", null};
            default -> throw new IllegalArgumentException("Unknown forecast provider " + provider);
        };
    }

    /**
     * Every provider's series at a point, from now on.
     *
     * @param excludedSources sources the caller may not read; their providers are left out
     */
    public List<Map<String, Object>> series(double lon, double lat, java.util.Set<String> excludedSources) {
        return series(null, lon, lat, excludedSources);
    }

    /**
     * Every provider's series for a place (or a bare point when {@code placeId} is null), from now on.
     *
     * @param placeId the place, which adds its {@code nws} series when one is cached; or null
     * @param excludedSources sources the caller may not read; their providers are left out
     */
    public List<Map<String, Object>> series(String placeId, double lon, double lat, java.util.Set<String> excludedSources) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (String provider : PROVIDERS) {
            String source = sourceOf(provider);
            if (excludedSources != null && excludedSources.contains(source)) {
                continue;
            }
            Optional<Map<String, Object>> s = "nws".equals(provider) ? nwsSeries(placeId) : series(provider, lon, lat);
            s.ifPresent(out::add);
        }
        return out;
    }

    /**
     * The owning source of a provider (prompt 105: {@code ifs} is owned by {@code ecmwf-ifs}; the {@code aifs}
     * provider is the {@code aifs} source).
     */
    public static String sourceOf(String provider) {
        return switch (provider) {
            case "nws" -> NWS_SOURCE;
            case "ifs" -> "ecmwf-ifs";
            default -> provider;
        };
    }

    /** A place's cached NWS series, trimmed to the current hour on; empty without a place or a cached row. */
    public Optional<Map<String, Object>> nwsSeries(String placeId) {
        if (placeId == null || nwsCache == null) {
            return Optional.empty();
        }
        return nwsCache.find(placeId, "nws").map(entry -> {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("provider", "nws");
            out.put("run", null);
            out.put("updatedAt", entry.updatedAt().toString());
            if (entry.validFrom() != null) {
                out.put("validFrom", entry.validFrom().toString());
            }
            boolean stale = Duration.between(entry.updatedAt(), samples.now()).compareTo(NWS_MAX_AGE) > 0;
            out.put("status", stale ? "stale" : "ok");
            out.put("items", new ArrayList<>(entry.items()));
            return trim(out, samples.now());
        });
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
