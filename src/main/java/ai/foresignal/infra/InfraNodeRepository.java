package ai.foresignal.infra;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code infra_node} access (prompt 104). */
public interface InfraNodeRepository extends JpaRepository<InfraNodeEntity, Long> {

    /** Nodes of a build, newest-first by id. */
    List<InfraNodeEntity> findByBuildIdOrderByIdAsc(long buildId);

    /** Nodes of a build of a facility type. */
    List<InfraNodeEntity> findByBuildIdAndFacilityTypeOrderByIdAsc(long buildId, String facilityType);

    /** Removes a build's nodes. */
    void deleteByBuildId(long buildId);
}
