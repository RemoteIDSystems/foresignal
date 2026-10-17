package ai.foresignal.forecast;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code forecast_calibration} rows (prompt 122). */
public interface ForecastCalibrationRepository extends JpaRepository<ForecastCalibrationEntity, String> {

    /** The rows of one category. */
    List<ForecastCalibrationEntity> findByCategory(String category);
}
