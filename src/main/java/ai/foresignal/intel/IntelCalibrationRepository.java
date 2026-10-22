package ai.foresignal.intel;

import org.springframework.data.jpa.repository.JpaRepository;

/** The fitted calibrations per scope (prompt 151, {@code intel_calibration}). */
public interface IntelCalibrationRepository extends JpaRepository<IntelCalibrationEntity, String> {
}
