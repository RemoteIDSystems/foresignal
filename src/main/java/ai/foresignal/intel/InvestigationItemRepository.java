package ai.foresignal.intel;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code investigation_item} access (prompt 101). */
public interface InvestigationItemRepository extends JpaRepository<InvestigationItemEntity, Long> {

    /** An investigation's items, oldest first. */
    List<InvestigationItemEntity> findByInvestigationIdOrderByIdAsc(String investigationId);

    /** Removes an investigation's items. */
    void deleteByInvestigationId(String investigationId);
}
