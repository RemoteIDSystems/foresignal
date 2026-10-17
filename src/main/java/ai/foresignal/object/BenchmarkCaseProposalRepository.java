package ai.foresignal.object;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code benchmark_case_proposal} table. */
public interface BenchmarkCaseProposalRepository extends JpaRepository<BenchmarkCaseProposalEntity, String> {

    /** Proposals in a status, oldest first. */
    List<BenchmarkCaseProposalEntity> findByStatusOrderByIdAsc(String status);

    /** The proposals of a verdict. */
    List<BenchmarkCaseProposalEntity> findByAnnotationId(String annotationId);
}
