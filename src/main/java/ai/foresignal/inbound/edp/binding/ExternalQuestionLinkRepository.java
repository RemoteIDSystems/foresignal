package ai.foresignal.inbound.edp.binding;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code external_question_link} rows (prompt 138). */
public interface ExternalQuestionLinkRepository extends JpaRepository<ExternalQuestionLinkEntity, String> {

    /** The links of a provider, oldest first. */
    List<ExternalQuestionLinkEntity> findBySourceIdOrderByCreatedAtAscIdAsc(String sourceId);

    /** The link of one provider question. */
    Optional<ExternalQuestionLinkEntity> findBySourceIdAndStreamIdAndExternalQuestionId(String sourceId,
            String streamId, String externalQuestionId);

    /** The links of one forecaster. */
    List<ExternalQuestionLinkEntity> findByForecasterId(String forecasterId);
}
