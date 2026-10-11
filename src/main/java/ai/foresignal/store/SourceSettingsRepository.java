package ai.foresignal.store;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceSettingsRepository extends JpaRepository<SourceSettingsEntity, String> {

    /** The sources the collector should be running. */
    List<SourceSettingsEntity> findByEnabledTrue();
}
