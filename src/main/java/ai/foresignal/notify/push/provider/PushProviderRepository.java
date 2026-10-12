package ai.foresignal.notify.push.provider;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Native push providers (prompt 032). */
public interface PushProviderRepository extends JpaRepository<PushProviderEntity, String> {

    /** Every provider, by id. */
    List<PushProviderEntity> findAllByOrderByIdAsc();
}
