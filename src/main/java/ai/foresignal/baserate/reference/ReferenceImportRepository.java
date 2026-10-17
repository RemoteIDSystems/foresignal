package ai.foresignal.baserate.reference;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code reference_import} rows (prompt 121). */
public interface ReferenceImportRepository extends JpaRepository<ReferenceImportEntity, String> {

    /** The imports with an idempotency key, newest first. */
    List<ReferenceImportEntity> findByImportKeyOrderByCreatedAtDesc(String importKey);

    /** The imports of a dataset in the given statuses. */
    List<ReferenceImportEntity> findByDatasetAndStatusIn(String dataset, List<String> statuses);

    /** The imports in the given statuses. */
    List<ReferenceImportEntity> findByStatusIn(List<String> statuses);
}
