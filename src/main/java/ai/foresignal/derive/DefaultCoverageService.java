package ai.foresignal.derive;

import ai.foresignal.collect.HealthService;
import ai.foresignal.collect.SourceHealthView;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.spi.Coverage;
import ai.foresignal.store.CoverageSample;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.locationtech.jts.geom.Envelope;
import org.springframework.stereotype.Component;

/** The {@link CoverageService} over the health service and the {@link ObservationFeed} (prompt 099). */
@Component
public class DefaultCoverageService implements CoverageService {

    /** The most observations a density query indexes, so a runaway source cannot blow the heap. */
    static final int DENSITY_LIMIT = 200_000;

    private final HealthService healthService;
    private final ObservationFeed feed;
    private final Clock clock;

    /** Creates the service. */
    public DefaultCoverageService(HealthService healthService, ObservationFeed feed, Clock clock) {
        this.healthService = healthService;
        this.feed = feed;
        this.clock = clock;
    }

    @Override
    public HealthStatus health(String sourceId) {
        try {
            SourceHealthView view = healthService.health(sourceId);
            return view == null || view.status() == null ? HealthStatus.IDLE : view.status();
        } catch (RuntimeException e) {
            return HealthStatus.IDLE;
        }
    }

    @Override
    public Instant lastDataAt(String sourceId) {
        try {
            SourceHealthView view = healthService.health(sourceId);
            if (view != null && view.lastDataAt() != null) {
                return view.lastDataAt();
            }
        } catch (RuntimeException e) {
            // fall through to the feed
        }
        return feed.lastDataAt(sourceId);
    }

    @Override
    public Coverage coverage(String sourceId) {
        try {
            SourceHealthView view = healthService.health(sourceId);
            CoverageSample sample = view == null ? null : view.coverage();
            return from(sample);
        } catch (RuntimeException e) {
            return Coverage.none();
        }
    }

    @Override
    public boolean healthy(String sourceId) {
        return health(sourceId) == HealthStatus.HEALTHY;
    }

    @Override
    public LayerStatus statusFor(Iterable<String> sources, Instant windowStart) {
        if (feed.degradedSince(windowStart)) {
            return LayerStatus.DEGRADED_INPUT;
        }
        boolean any = false;
        boolean anyHealthy = false;
        for (String source : sources) {
            any = true;
            HealthStatus status = health(source);
            if (status == HealthStatus.HEALTHY || status == HealthStatus.DEGRADED) {
                anyHealthy = true;
            }
            if (status == HealthStatus.HEALTHY) {
                return LayerStatus.OK;
            }
        }
        if (!any) {
            return LayerStatus.NO_COVERAGE;
        }
        return anyHealthy ? LayerStatus.OK : LayerStatus.NO_COVERAGE;
    }

    @Override
    public int densityNear(String sourceId, double lon, double lat, double radiusKm, Duration window) {
        if (sourceId == null || !Double.isFinite(lon) || !Double.isFinite(lat) || radiusKm <= 0) {
            return 0;
        }
        Instant since = clock.instant().minus(window == null ? Duration.ZERO : window);
        List<Observation> observations = feed.recent(Set.of(sourceId), Set.of(), since, DENSITY_LIMIT);
        DensityIndex index = new DensityIndex();
        for (Observation observation : observations) {
            Double oLon = observation.longitude();
            Double oLat = observation.latitude();
            if (oLon != null && oLat != null) {
                index.add(oLon, oLat, observation.externalId());
            }
        }
        return index.countNear(lon, lat, radiusKm);
    }

    /** A stored sample as the framework's coverage. */
    static Coverage from(CoverageSample sample) {
        if (sample == null) {
            return Coverage.none();
        }
        Envelope region = null;
        List<Double> box = sample.region();
        if (box != null && box.size() == 4) {
            region = new Envelope(box.get(0), box.get(2), box.get(1), box.get(3));
        }
        return new Coverage(sample.receivers(), sample.expected(), sample.actual(), region);
    }

    /** The input sources of a generator, as a list (helper for {@link #statusFor}). */
    static List<String> list(Collection<String> sources) {
        return List.copyOf(sources);
    }
}
