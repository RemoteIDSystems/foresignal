package ai.foresignal.analytics.registry;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code analysis_series} rows (prompt 119). */
public interface AnalysisSeriesRepository extends JpaRepository<AnalysisSeriesEntity, String> {

    /** The rows of one origin. */
    List<AnalysisSeriesEntity> findByOrigin(String origin);

    /** The enabled rows of one frequency, by ref. */
    List<AnalysisSeriesEntity> findByEnabledTrueAndFrequencyOrderByRefAsc(String frequency);

    /** The enabled rows, by ref. */
    List<AnalysisSeriesEntity> findByEnabledTrueOrderByRefAsc();

    /** The number of rows of one origin. */
    long countByOrigin(String origin);
}
