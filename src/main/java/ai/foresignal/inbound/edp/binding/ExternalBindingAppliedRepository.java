package ai.foresignal.inbound.edp.binding;

import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code external_binding_applied} rows (prompt 138). */
public interface ExternalBindingAppliedRepository extends JpaRepository<ExternalBindingAppliedEntity, String> {

    /** How many versions a binding applied. */
    long countByBindingId(String bindingId);
}
