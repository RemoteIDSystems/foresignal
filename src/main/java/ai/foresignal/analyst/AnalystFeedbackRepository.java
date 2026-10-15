package ai.foresignal.analyst;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code analyst_feedback} access (prompt 102). */
public interface AnalystFeedbackRepository extends JpaRepository<AnalystFeedbackEntity, String> {

    /** Feedback on one target. */
    List<AnalystFeedbackEntity> findByTargetAndTargetIdOrderByAtDesc(String target, String targetId);

    /** Feedback on a rating, for aggregates. */
    long countByTargetAndRating(String target, String rating);
}
