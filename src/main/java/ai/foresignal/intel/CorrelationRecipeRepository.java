package ai.foresignal.intel;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code correlation_recipe} access (prompt 101). */
public interface CorrelationRecipeRepository extends JpaRepository<CorrelationRecipeEntity, String> {

    /** The enabled recipes, ordered by id. */
    List<CorrelationRecipeEntity> findByEnabledTrueOrderByIdAsc();
}
