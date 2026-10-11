package ai.foresignal.store;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSecretRepository extends JpaRepository<SystemSecretEntity, String> {}
