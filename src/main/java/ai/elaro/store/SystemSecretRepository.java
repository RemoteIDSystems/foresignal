package ai.elaro.store;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSecretRepository extends JpaRepository<SystemSecretEntity, String> {}
