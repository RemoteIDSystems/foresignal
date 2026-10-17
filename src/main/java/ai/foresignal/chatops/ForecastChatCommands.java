package ai.foresignal.chatops;

import ai.foresignal.forecast.ForecastEntity;
import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecastStatus;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.forecast.language.EstimativeLanguage;
import ai.foresignal.security.visibility.SourceVisibility;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * The ledger's chat commands (prompt 122, on the 061 router): {@code /questions} lists open questions and
 * {@code /forecast <questionId> <probability>} records the operator's binary forecast under the chat principal's
 * {@code operator:} forecaster. The router has already checked the chat's scope against the mirrored REST operations.
 */
@Component
public class ForecastChatCommands {

    /** The most questions {@code /questions} lists. */
    public static final int MAX_QUESTIONS = 10;

    private final ForecastService ledger;

    /** Creates the commands. */
    public ForecastChatCommands(ForecastService ledger) {
        this.ledger = ledger;
    }

    /** {@code /questions}: open questions, soonest due first. */
    public ChatOutcome questions(ChatPrincipal principal, ChatRef ref) {
        boolean seesPrivate = SourceVisibility.canSeePrivate(principal.scope(), principal.privateAccess());
        ForecastService.Page page = ledger.list(new ForecastService.ListQuery(List.of(ForecastStatus.OPEN), List.of(),
                null, List.of(), null), null, MAX_QUESTIONS, seesPrivate);
        if (page.items().isEmpty()) {
            return ChatOutcome.ok("No open forecast questions.", ref.messageId());
        }
        StringBuilder out = new StringBuilder("**Open forecast questions**\n");
        for (ForecastQuestionEntity q : page.items()) {
            out.append("- `").append(q.getId()).append("` ").append(ChatText.esc(ChatText.cut(q.getTitle(), 120)));
            if (q.getEnsembleProbability() != null) {
                out.append(" — ensemble ").append(Math.round(q.getEnsembleProbability() * 100)).append("% (")
                        .append(EstimativeLanguage.term(q.getEnsembleProbability())).append(')');
            }
            out.append(", closes ").append(q.getClosesAt().toString(), 0, 10).append('\n');
        }
        out.append("Forecast with /forecast <questionId> <probability>.");
        return ChatOutcome.ok(out.toString(), ref.messageId());
    }

    /** {@code /forecast <questionId> <probability>}: a binary forecast (0–1 or a percentage). */
    public ChatOutcome forecast(ChatPrincipal principal, ChatRef ref, List<String> args) {
        if (args.size() != 2) {
            return ChatCommandService.usage(ChatCommand.FORECAST, ref);
        }
        Double p = probability(args.get(1));
        if (p == null) {
            return ChatOutcome.of(ChatOutcome.INVALID, "The probability must be between 0 and 1, or a percentage.",
                    ref.messageId());
        }
        boolean seesPrivate = SourceVisibility.canSeePrivate(principal.scope(), principal.privateAccess());
        ForecastQuestionEntity q = ledger.get(args.get(0), seesPrivate);
        if (!Ledger.BINARY.equals(q.getType())) {
            return ChatOutcome.of(ChatOutcome.INVALID, "Only binary questions take a forecast from chat; use the API"
                    + " for " + q.getType() + " questions.", ref.messageId());
        }
        ForecastEntity f = ledger.submit(q.getId(), new ForecastInputs.ForecastSubmission(
                ForecastInputs.ForecastValue.binary(p), null, List.of(), null), principal.token(), seesPrivate);
        return ChatOutcome.ok("Recorded " + Math.round(p * 1000) / 10.0 + "% (" + EstimativeLanguage.term(p) + ") on"
                + " \"" + ChatText.esc(ChatText.cut(q.getTitle(), 120)) + "\" as `" + f.getForecasterId() + "`.",
                ref.messageId());
    }

    /** A probability from {@code 0.7}, {@code .7} or {@code 70%}, or null. */
    static Double probability(String text) {
        try {
            String t = text.strip().toLowerCase(Locale.ROOT);
            double p = t.endsWith("%") ? Double.parseDouble(t.substring(0, t.length() - 1)) / 100.0
                    : Double.parseDouble(t);
            return Double.isFinite(p) && p >= 0 && p <= 1 ? p : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
