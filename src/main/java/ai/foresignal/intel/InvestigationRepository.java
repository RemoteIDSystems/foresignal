package ai.foresignal.intel;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code investigation} access (prompt 101). */
public interface InvestigationRepository extends JpaRepository<InvestigationEntity, String> {

    /** Open investigations, newest first; and all by status. */
    List<InvestigationEntity> findByStatusOrderByUpdatedAtDesc(String status);

    /** The newest display id for a year, for the {@code INV-{yyyy}-{nnnn}} sequence. */
    List<InvestigationEntity> findTop1ByDisplayIdStartingWithOrderByDisplayIdDesc(String prefix);
}
