package ai.foresignal.outlook.series;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code series_forecast} rows (prompt 126). */
public interface SeriesForecastRepository extends JpaRepository<SeriesForecastEntity, String> {

    /** The newest forecast of a series. */
    Optional<SeriesForecastEntity> findTop1ByRefOrderByMadeAtDescIdDesc(String ref);

    /** The forecasts of a series made at one time. */
    List<SeriesForecastEntity> findByRefAndMadeAtOrderByHorizonAsc(String ref, Instant madeAt);

    /** The forecast of a series for a horizon and target period. */
    List<SeriesForecastEntity> findByRefAndHorizonAndTargetStart(String ref, int horizon, Instant targetStart);

    /** The unchecked forecasts of one horizon whose target period has ended. */
    List<SeriesForecastEntity> findByExceedanceCheckedFalseAndHorizonAndTargetEndLessThanEqualOrderByIdAsc(
            int horizon, Instant at);

    /** The forecasts of a series with a ledger question. */
    List<SeriesForecastEntity> findByRefAndQuestionIdIsNotNullOrderByMadeAtDesc(String ref);
}
