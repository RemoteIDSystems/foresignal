package ai.foresignal.exchange;

import org.springframework.data.jpa.repository.JpaRepository;

/** Pinned sender keys (prompt 132). */
public interface ExchangeSenderKeyRepository extends JpaRepository<ExchangeSenderKeyEntity, String> {}
