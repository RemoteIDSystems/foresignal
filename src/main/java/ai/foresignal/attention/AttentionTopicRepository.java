package ai.foresignal.attention;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The attention topics (prompt 117). */
public interface AttentionTopicRepository extends JpaRepository<AttentionTopicEntity, String> {

    /** Every topic in id order. */
    List<AttentionTopicEntity> findAllByOrderByIdAsc();

    /** The enabled (or disabled) topics in id order. */
    List<AttentionTopicEntity> findByEnabledOrderByIdAsc(boolean enabled);
}
