package ai.foresignal.ask;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code ask_thread} rows. Listing is keyset-paged in {@link AskThreadService}. */
public interface AskThreadRepository extends JpaRepository<AskThreadEntity, UUID> {}
