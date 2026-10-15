package ai.foresignal.infra;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code infra_edge} access (prompt 104). */
public interface InfraEdgeRepository extends JpaRepository<InfraEdgeEntity, Long> {

    /** Edges of a build. */
    List<InfraEdgeEntity> findByBuildIdOrderByIdAsc(long buildId);

    /** Edges of a build by method. */
    List<InfraEdgeEntity> findByBuildIdAndMethodOrderByIdAsc(long buildId, String method);

    /** Edges of a build by label. */
    List<InfraEdgeEntity> findByBuildIdAndLabelOrderByIdAsc(long buildId, String label);

    /** Removes a build's edges. */
    void deleteByBuildId(long buildId);
}
