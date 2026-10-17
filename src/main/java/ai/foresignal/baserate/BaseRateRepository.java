package ai.foresignal.baserate;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code base_rate} rows (prompt 121). */
public interface BaseRateRepository extends JpaRepository<BaseRateEntity, String> {

    /** The rates with a definition key and origin. */
    List<BaseRateEntity> findByDefinitionKeyAndOrigin(String definitionKey, String origin);

    /** The rates with a definition key, any origin. */
    List<BaseRateEntity> findByDefinitionKey(String definitionKey);
}
