package ai.foresignal.raster;

import java.time.Duration;
import java.util.Optional;

/**
 * The run cadence of a forecast product source (prompt 051), for frame-age health: a forecast
 * product's frames arrive in bursts, one run every {@code runEvery}, so its age is judged against the
 * run cadence rather than the spacing of its steps.
 *
 * <p>Implemented by the forecast model ingest ({@code ai.foresignal.raster.model.ModelRunIngest});
 * {@link FrameAgeHealthRule} asks every bean.
 */
public interface ForecastRunCadence {

    /**
     * The run cadence of {@code sourceId}.
     *
     * @return the cadence, or empty when this bean does not know the source
     */
    Optional<Duration> runCadence(String sourceId);
}
