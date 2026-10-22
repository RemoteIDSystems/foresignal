package ai.foresignal.inbound.edp.binding;

import org.springframework.data.jpa.repository.JpaRepository;

/** The on-demand analysis-request correlations (prompt 155). */
public interface AnalysisCorrelationRepository extends JpaRepository<AnalysisCorrelationEntity, String> {
}
