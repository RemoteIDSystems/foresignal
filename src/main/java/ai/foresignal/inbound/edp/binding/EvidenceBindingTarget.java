package ai.foresignal.inbound.edp.binding;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.hypothesis.AchEvidenceEntity;
import ai.foresignal.hypothesis.HypothesisEntity;
import ai.foresignal.hypothesis.HypothesisInputs;
import ai.foresignal.hypothesis.HypothesisRepository;
import ai.foresignal.hypothesis.HypothesisService;
import ai.foresignal.hypothesis.HypothesisVocabulary;
import ai.foresignal.object.ObjectTypes;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Target {@code evidence} (prompt 138): matching items become evidence rows of a 125 hypothesis set
 * ({@code ach_evidence}, source {@code record}, ref the item's record ref), deduplicated by the 116 evidence group as
 * every record ref is, so re-published copies never add rows.
 *
 * <p>Configuration: {@code hypothesisSetId} (required), {@code hypothesisId} (optional; the hypothesis the item is
 * rated against, with {@code rating}, default {@code C}), the Admiralty grade {@code sourceReliability} ({@code A}–
 * {@code F}) and {@code informationCredibility} (1–6), default {@code F6}, and {@code relevance} ({@code H}, {@code M},
 * {@code L}; default {@code M}). 125 evidence rows grade credibility as H/M/L, so the Admiralty grade maps to the
 * weaker of its two halves ({@code A}/{@code B} or 1/2 → H, {@code C} or 3 → M, otherwise L) and is kept in the
 * description.
 */
@Component
public class EvidenceBindingTarget implements BindingTargetHandler {

    /** The Admiralty source reliability letters. */
    public static final List<String> RELIABILITY = List.of("A", "B", "C", "D", "E", "F");

    private final ObjectProvider<HypothesisService> hypotheses;
    private final HypothesisRepository hypothesisRows;

    /** Creates the target. */
    public EvidenceBindingTarget(ObjectProvider<HypothesisService> hypotheses, HypothesisRepository hypothesisRows) {
        this.hypotheses = hypotheses;
        this.hypothesisRows = hypothesisRows;
    }

    @Override
    public String target() {
        return BindingVocabulary.EVIDENCE;
    }

    @Override
    public List<FieldError> validate(Map<String, Object> config, BoundStream stream) {
        List<FieldError> errors = new ArrayList<>();
        String setId = BindingConfig.required(config, "hypothesisSetId", errors);
        BindingConfig.oneOf(config, "sourceReliability", RELIABILITY, errors);
        BindingConfig.range(config, "informationCredibility", 1, 6, errors);
        Double credibility = BindingConfig.number(config, "informationCredibility");
        if (credibility != null && credibility != Math.rint(credibility)) {
            errors.add(new FieldError("config.informationCredibility", "must be a whole number from 1 to 6"));
        }
        BindingConfig.oneOf(config, "relevance", HypothesisVocabulary.GRADES, errors);
        BindingConfig.oneOf(config, "rating", HypothesisVocabulary.RATINGS, errors);
        if (setId != null) {
            HypothesisService service = hypotheses.getIfAvailable();
            if (service == null || service.find(setId).isEmpty()) {
                errors.add(new FieldError("config.hypothesisSetId", "no hypothesis set '" + setId + "'"));
            } else {
                String hypothesisId = BindingConfig.text(config, "hypothesisId");
                if (hypothesisId != null && hypothesisRows.findBySetIdOrderByPositionAscIdAsc(setId).stream()
                        .map(HypothesisEntity::getId).noneMatch(hypothesisId::equals)) {
                    errors.add(new FieldError("config.hypothesisId", "is not a hypothesis of set '" + setId + "'"));
                }
            }
        }
        return errors;
    }

    @Override
    public BindingEffect plan(ExternalBindingEntity binding, BoundStream stream, BoundItem item) {
        return BindingEffect.of(item, "evidence-added", ObjectTypes.HYPOTHESIS_SET + ":"
                + BindingConfig.text(binding.getConfig(), "hypothesisSetId"));
    }

    @Override
    public BindingEffect apply(ExternalBindingEntity binding, BoundStream stream, BoundItem item) {
        HypothesisService service = hypotheses.getIfAvailable();
        if (service == null) {
            return BindingEffect.error(item, "hypotheses are not available");
        }
        Map<String, Object> config = binding.getConfig();
        String setId = BindingConfig.text(config, "hypothesisSetId");
        AchEvidenceEntity row = service.addEvidence(setId, draft(config, stream, item),
                "binding:" + binding.getId(), true);
        return BindingEffect.of(item, "evidence-added", "ach-evidence:" + row.getId());
    }

    /** The evidence draft of an item under a binding's configuration. Pure. */
    static HypothesisInputs.EvidenceDraft draft(Map<String, Object> config, BoundStream stream, BoundItem item) {
        String reliability = BindingConfig.text(config, "sourceReliability");
        if (reliability == null) {
            reliability = "F";
        }
        Double c = BindingConfig.number(config, "informationCredibility");
        int credibility = c == null ? 6 : (int) Math.round(c);
        String relevance = BindingConfig.text(config, "relevance");
        Map<String, String> ratings = new LinkedHashMap<>();
        String hypothesisId = BindingConfig.text(config, "hypothesisId");
        if (hypothesisId != null) {
            String rating = BindingConfig.text(config, "rating");
            ratings.put(hypothesisId, rating == null ? "C" : rating);
        }
        String description = summary(item) + " (Admiralty " + reliability + credibility + ", EDP "
                + item.sourceId() + "/" + stream.streamId() + ")";
        return new HypothesisInputs.EvidenceDraft("record", item.ref(),
                description.length() <= 2000 ? description : description.substring(0, 2000),
                grade(reliability, credibility), relevance == null ? "M" : relevance, ratings, List.of());
    }

    /** The H/M/L credibility of an Admiralty grade: the weaker of its two halves. Pure. */
    static String grade(String reliability, int credibility) {
        int r = switch (reliability) {
            case "A", "B" -> 0;
            case "C" -> 1;
            default -> 2;
        };
        int i = credibility <= 2 ? 0 : credibility == 3 ? 1 : 2;
        return HypothesisVocabulary.GRADES.get(Math.max(r, i));
    }

    /** A short description of an item: its title, text, value or kind. */
    static String summary(BoundItem item) {
        for (String field : List.of("title", "text", "name", "category")) {
            String value = item.text(field);
            if (value != null) {
                return value.length() <= 300 ? value : value.substring(0, 300);
            }
        }
        if (item.payload().get("value") != null) {
            return item.kind() + " " + item.text("seriesKey") + " = " + item.payload().get("value") + " ("
                    + item.text("t") + ")";
        }
        return item.kind() + " " + item.itemId();
    }
}
