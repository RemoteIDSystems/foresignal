package ai.foresignal.collab;

import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code evidence_item} table. */
public interface EvidenceItemRepository extends JpaRepository<EvidenceItemEntity, String> {

    /** Evidence items before a cursor id, newest first. */
    List<EvidenceItemEntity> findByIdLessThanOrderByIdDesc(String before, Limit limit);

    /** The newest evidence items, for the first page (no cursor). */
    List<EvidenceItemEntity> findAllByOrderByIdDesc(Limit limit);

    /** Evidence items citing an attachment (a JSON containment test). */
    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM evidence_item WHERE attachment_ids @>"
            + " CAST(:json AS jsonb)", nativeQuery = true)
    List<EvidenceItemEntity> citingAttachment(@org.springframework.data.repository.query.Param("json") String json);
}
