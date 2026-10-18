package ai.foresignal.social;

import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code social_salt} row (prompt 135). */
public interface SocialSaltRepository extends JpaRepository<SocialSaltEntity, String> {}
