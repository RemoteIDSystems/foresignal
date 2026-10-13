package ai.foresignal.weather;

import ai.foresignal.source.formats.geojson.GeoJsonReader;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Attaches {@code context.weather} to geolocated alerts before dispatch (prompt 056; Weather-Imagery-Design §7.5).
 *
 * <p><b>When.</b> For an alert whose record version has a geometry, when the global setting
 * {@code weather.alertContext} is {@code auto} (default), the rule's {@code weatherContext} is not {@code off}, and at
 * least one weather input is enabled ({@link WeatherContextService#anyInputEnabled()}).
 *
 * <p><b>How.</b> The trigger engine collects the alerts of a chunk and calls {@link #computeAll} before the
 * transaction that inserts them, so the context is stored with the alert, available to the rule's message template
 * and on every {@code alert.fired} event and notification. Each context runs on its own virtual thread with its own
 * {@value WeatherContextService#BUDGET_MS} ms budget, so a chunk waits about one budget however many alerts it has;
 * a failure is logged and stored as {@code {status: unavailable}}, and never blocks or fails delivery.
 */
@Component
public class AlertWeatherContext {

    private static final Logger log = LoggerFactory.getLogger(AlertWeatherContext.class);

    /** The slack added to the budget when waiting for a context. */
    static final Duration SLACK = Duration.ofMillis(250);

    private final WeatherContextService service;
    private final WeatherSettingsService settings;

    /** Creates the component. */
    public AlertWeatherContext(WeatherContextService service, WeatherSettingsService settings) {
        this.service = service;
        this.settings = settings;
    }

    /**
     * One alert to compute for.
     *
     * @param geometryJson the record version's GeoJSON geometry
     * @param observedAt the alert's {@code observed_at}
     */
    public record Job(String geometryJson, Instant observedAt) {}

    /**
     * True when alerts of a rule get a weather context now.
     *
     * @param ruleWeatherContext the rule's {@code weatherContext} ({@code auto}, {@code off} or null for auto)
     */
    public boolean enabledFor(String ruleWeatherContext) {
        if (WeatherSettings.OFF.equals(ruleWeatherContext)) {
            return false;
        }
        try {
            return settings.get().alertContextOn() && service.anyInputEnabled();
        } catch (RuntimeException e) {
            log.warn("Alert weather context is skipped: the settings could not be read ({})", e.getMessage());
            return false;
        }
    }

    /**
     * The {@code context.weather} maps of {@code jobs}, in order; never throws.
     *
     * @return one map per job ({@code {status: unavailable, …}} when its computation failed)
     */
    public List<Map<String, Object>> computeAll(List<Job> jobs) {
        List<Map<String, Object>> out = new ArrayList<>(jobs.size());
        if (jobs.isEmpty()) {
            return out;
        }
        ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();
        try {
            List<Future<WeatherContext>> futures = new ArrayList<>(jobs.size());
            for (Job job : jobs) {
                futures.add(pool.submit(() -> compute(job)));
            }
            long deadline = System.nanoTime() + WeatherContextService.BUDGET.plus(SLACK).toNanos();
            for (int i = 0; i < jobs.size(); i++) {
                Job job = jobs.get(i);
                WeatherContext context;
                try {
                    context = futures.get(i).get(Math.max(0, deadline - System.nanoTime()), TimeUnit.NANOSECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    context = WeatherContext.unavailable(job.observedAt(), service.now());
                } catch (Exception e) {
                    futures.get(i).cancel(true);
                    log.warn("The weather context of an alert at {} failed: {}", job.observedAt(), e.toString());
                    context = WeatherContext.unavailable(job.observedAt(), service.now());
                }
                out.add(context.toMap());
            }
        } finally {
            pool.shutdownNow();
        }
        return out;
    }

    /** One job: parses the geometry and computes; a failure is an unavailable context. */
    WeatherContext compute(Job job) {
        try {
            Geometry geometry = new GeoJsonReader(1_000_000, 8).read(job.geometryJson());
            return service.compute(geometry, job.observedAt());
        } catch (RuntimeException e) {
            log.warn("The weather context of an alert at {} failed: {}", job.observedAt(), e.toString());
            return WeatherContext.unavailable(job.observedAt(), service.now());
        }
    }
}
