package ai.foresignal.situation;

import org.springframework.data.jpa.repository.JpaRepository;

/** Cached LLM theme classifications (prompt 118). */
public interface ThemeClassificationRepository extends JpaRepository<ThemeClassificationEntity, String> {}
