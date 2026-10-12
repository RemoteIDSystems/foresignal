package ai.foresignal.security;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence of {@link PrincipalEntity}. */
public interface PrincipalRepository extends JpaRepository<PrincipalEntity, String> {

    /** The principal with {@code handle}. */
    Optional<PrincipalEntity> findByHandle(String handle);

    /** True when {@code handle} is taken. */
    boolean existsByHandle(String handle);
}
