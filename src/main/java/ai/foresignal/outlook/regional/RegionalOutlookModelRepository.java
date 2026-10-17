package ai.foresignal.outlook.regional;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code regional_outlook_model} rows (prompt 126). */
public interface RegionalOutlookModelRepository extends JpaRepository<RegionalOutlookModelEntity, String> {

    /** The fits of one family. */
    List<RegionalOutlookModelEntity> findByFamily(String family);
}
