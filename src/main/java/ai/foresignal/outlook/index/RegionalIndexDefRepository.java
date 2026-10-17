package ai.foresignal.outlook.index;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code regional_index_def} rows (prompt 126). */
public interface RegionalIndexDefRepository extends JpaRepository<RegionalIndexDefEntity, String> {

    /** The definitions after a cursor id, by id. */
    List<RegionalIndexDefEntity> findByIdGreaterThanOrderByIdAsc(String after, Pageable page);

    /** The enabled definitions, by id. */
    List<RegionalIndexDefEntity> findByEnabledTrueOrderByIdAsc();

    /** The enabled definitions targeting a family. */
    List<RegionalIndexDefEntity> findByEnabledTrueAndTarget(String target);
}
