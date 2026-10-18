package ai.foresignal.inbound.edp.binding;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code external_binding} rows (prompt 138). */
public interface ExternalBindingRepository extends JpaRepository<ExternalBindingEntity, String> {

    /** The bindings of a provider, oldest first. */
    List<ExternalBindingEntity> findBySourceIdOrderByCreatedAtAscIdAsc(String sourceId);

    /** The enabled bindings, oldest first. */
    List<ExternalBindingEntity> findByEnabledTrueOrderByCreatedAtAscIdAsc();

    /** The bindings of one target, oldest first. */
    List<ExternalBindingEntity> findByTargetOrderByCreatedAtAscIdAsc(String target);

    /** How many bindings a stream has. */
    long countBySourceIdAndStreamId(String sourceId, String streamId);
}
