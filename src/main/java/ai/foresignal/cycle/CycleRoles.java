package ai.foresignal.cycle;

import ai.foresignal.analyst.AnalystExecutionService;
import ai.foresignal.analyst.AnalystOutputValidator;
import ai.foresignal.analyst.AnalystProfileEntity;
import ai.foresignal.analyst.AnalystProposalEntity;
import ai.foresignal.analyst.AnalystProposalEvents;
import ai.foresignal.analyst.AnalystProposalRepository;
import ai.foresignal.analyst.AnalystRunEntity;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.benchmark.BenchmarkGate;
import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.ForecastInputs.ForecastValue;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecasterEntity;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.forecast.language.EstimativeLanguage;
import ai.foresignal.hypothesis.HypothesisInputs;
import ai.foresignal.hypothesis.HypothesisService;
import ai.foresignal.intel.EventTypes;
import ai.foresignal.llm.LlmProviderEntity;
import ai.foresignal.llm.LlmProviderRepository;
import ai.foresignal.llm.PromptTemplates;
import ai.foresignal.llm.client.ChatMessage;
import ai.foresignal.llm.client.ToolCall;
import ai.foresignal.llm.client.ToolLoop;
import ai.foresignal.llm.client.ToolSpec;
import ai.foresignal.security.Ulids;
import ai.foresignal.situation.SituationService;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * The four LLM roles of a cycle run (prompt 128 §3), each a 028 tool loop through {@link CycleLlm}:
 *
 * <ul>
 *   <li><b>Analyst</b>: 3–7 typed, cited key judgments. A probability needs a ledger question or hypothesis set; its
 *       ICD 203 term is derived from the number; the confidence statement is a separate sentence; text failing
 *       {@link EstimativeLanguage#lint} is regenerated once, then downgraded or dropped ({@link JudgmentLinter}).</li>
 *   <li><b>Red team</b>: for up to three sets ({@link RedTeamSelection}), the case for the runner-up, disconfirming
 *       evidence, assumptions at risk and a premortem; where a set has a ledger question, a forecast as
 *       {@code llm-redteam:{profile}}.</li>
 *   <li><b>Forecaster</b>: up to {@code max_questions} open questions by value of information
 *       ({@link QuestionPriority}); per question and sample an outside-view pass ({@code get_base_rate},
 *       {@code find_analogs}, criteria quoted verbatim) then an inside-view pass; samples aggregated by median
 *       log-odds ({@link SampleAggregator}) and recorded as {@code llm:{profile}:{provider}}, one forecaster per
 *       configured provider. The 122 ledger caps and recalibrates them; they never change hypothesis probabilities or
 *       other forecasters' numbers.</li>
 *   <li><b>Proposer</b>: 102 proposals ({@code propose_hypothesis_set} validated as a 125 draft,
 *       {@code propose_indicator}, {@code propose_watch}, {@code propose_question} validated by 122,
 *       {@code propose_situation_merge}), pending unless the profile allows actions and the 123 benchmark gate passes.</li>
 * </ul>
 */
@Component
public class CycleRoles {

    private static final Logger log = LoggerFactory.getLogger(CycleRoles.class);

    /** The most key judgments kept. */
    public static final int MAX_JUDGMENTS = 7;

    /** The fewest key judgments asked for. */
    public static final int MIN_JUDGMENTS = 3;

    /** The version of the LLM forecasters registered here. */
    public static final String FORECASTER_VERSION = "1.0.0";

    private final CycleLlm llm;
    private final ForecastService ledger;
    private final ForecasterRegistry forecasters;
    private final HypothesisService hypotheses;
    private final SituationService situations;
    private final AnalystProposalRepository proposals;
    private final AnalystExecutionService execution;
    private final ObjectProvider<AnalystProposalEvents> proposalEvents;
    private final ObjectProvider<BenchmarkGate> benchmarks;
    private final ObjectProvider<LlmProviderRepository> providers;
    private final NamedParameterJdbcTemplate jdbc;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the roles. */
    public CycleRoles(CycleLlm llm, ForecastService ledger, ForecasterRegistry forecasters,
            HypothesisService hypotheses, SituationService situations, AnalystProposalRepository proposals,
            AnalystExecutionService execution, ObjectProvider<AnalystProposalEvents> proposalEvents,
            ObjectProvider<BenchmarkGate> benchmarks, ObjectProvider<LlmProviderRepository> providers,
            NamedParameterJdbcTemplate jdbc, JsonMapper json, Clock clock) {
        this.llm = llm;
        this.ledger = ledger;
        this.forecasters = forecasters;
        this.hypotheses = hypotheses;
        this.situations = situations;
        this.proposals = proposals;
        this.execution = execution;
        this.proposalEvents = proposalEvents;
        this.benchmarks = benchmarks;
        this.providers = providers;
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
    }

    /** The key of a profile in forecaster ids: its id in lower case, or {@code default}. */
    public static String profileKey(AnalystProfileEntity realProfile) {
        return realProfile == null ? "default" : realProfile.getId().toLowerCase(Locale.ROOT);
    }

    /** The LLM forecaster id of a profile and provider: {@code llm:{profile}:{provider}}. */
    public static String forecasterId(String profileKey, String providerId) {
        return "llm:" + profileKey + ":" + providerId.toLowerCase(Locale.ROOT);
    }

    /** The red-team forecaster id of a profile: {@code llm-redteam:{profile}}. */
    public static String redTeamId(String profileKey) {
        return "llm-redteam:" + profileKey;
    }

    // ========================================================================= analyst

    /**
     * The analyst's output.
     *
     * @param outcome the role outcome
     * @param judgments the validated key judgments ({@code KeyJudgment})
     * @param whatWeCannotSee the analyst's comment on the collection gaps, or null
     * @param lintWarnings the estimative-language warnings
     */
    public record AnalystResult(RoleOutcome outcome, List<Map<String, Object>> judgments, String whatWeCannotSee,
            List<String> lintWarnings) {}

    /**
     * Runs the analyst role.
     *
     * @param ctx the role context
     * @param llmPacket the capped packet
     * @param previousJudgments the previous assessment's judgments
     */
    public AnalystResult analyst(RoleContext ctx, Map<String, Object> llmPacket,
            List<Map<String, Object>> previousJudgments) {
        RoleOutcome outcome = new RoleOutcome(CycleVocabulary.ANALYST);
        outcome.using(ctx.providerId(), ctx.effort(), PromptTemplates.CYCLE_ANALYST);
        List<Map<String, Object>> captured = new ArrayList<>();
        String user = "Analysis cycle packet: what changed in scope since the previous run, and the previous"
                + " assessment's key judgments (untrusted data):\n" + llm.wrap("cycle_packet", llmPacket)
                + "\nWrite the key judgments with record_judgments.";
        llm.loop(ctx, outcome, PromptTemplates.CYCLE_ANALYST, List.of(new ChatMessage.User(user)),
                CycleOutputTools.analyst(json), call -> captured.add(CycleOutputTools.args(call.input(), json)));
        if (captured.isEmpty()) {
            outcome.finish(CycleVocabulary.SKIPPED, "the model recorded no judgments");
            return new AnalystResult(outcome, List.of(), null, List.of());
        }
        Map<String, Object> last = captured.get(captured.size() - 1);
        List<Map<String, Object>> judgments = new ArrayList<>();
        List<String> lint = new ArrayList<>();
        List<Object> raw = list(last.get("judgments"));
        if (raw.size() > MAX_JUDGMENTS) {
            outcome.warn("the model gave " + raw.size() + " judgments; the first " + MAX_JUDGMENTS + " were kept");
            raw = raw.subList(0, MAX_JUDGMENTS);
        }
        int index = 0;
        for (Object item : raw) {
            if (!(item instanceof Map<?, ?> m)) {
                continue;
            }
            Map<String, Object> judgment = judgment(ctx, outcome, map(m), previousJudgments, ++index, lint);
            if (judgment != null) {
                judgments.add(judgment);
            }
        }
        if (judgments.size() < MIN_JUDGMENTS) {
            outcome.warn("only " + judgments.size() + " key judgments survived validation (3 to 7 asked)");
        }
        String gaps = text(last.get("whatWeCannotSee"));
        if (gaps != null && !JudgmentLinter.violations(gaps, null).isEmpty()) {
            gaps = JudgmentLinter.downgrade(gaps, null);
            lint.add("collection-gap comment downgraded by the wording lint");
        }
        if (judgments.isEmpty()) {
            outcome.finish(CycleVocabulary.SKIPPED, "no key judgment survived validation");
        }
        return new AnalystResult(outcome, judgments, gaps, lint);
    }

    /** One validated key judgment, or null when dropped. */
    Map<String, Object> judgment(RoleContext ctx, RoleOutcome outcome, Map<String, Object> m,
            List<Map<String, Object>> previous, int index, List<String> lint) {
        List<String> warnings = new ArrayList<>();
        String text = text(m.get("text"));
        Double p = m.get("probability") instanceof Number n && n.doubleValue() >= 0 && n.doubleValue() <= 1
                ? n.doubleValue() : null;
        String questionId = text(m.get("questionId"));
        String setId = text(m.get("hypothesisSetId"));
        if (questionId != null && ledger.find(questionId).isEmpty()) {
            warnings.add("unknown questionId " + questionId + " removed");
            questionId = null;
        }
        if (setId != null && hypotheses.find(setId).isEmpty()) {
            warnings.add("unknown hypothesisSetId " + setId + " removed");
            setId = null;
        }
        boolean downgraded = false;
        if (p != null && questionId == null && setId == null) {
            warnings.add("probability removed: a probability-bearing judgment must reference a ledger question or"
                    + " hypothesis set");
            p = null;
            downgraded = true;
        }
        List<String> citations = new ArrayList<>();
        for (String c : strings(m.get("citations"))) {
            if (ctx.citations().valid(c)) {
                if (!citations.contains(c)) {
                    citations.add(c);
                }
            } else {
                warnings.add("dropped citation [" + c + "]: not returned by any tool or the packet");
            }
        }
        if (citations.isEmpty()) {
            warnings.add("no valid citation: the judgment is labeled speculation and carries no probability");
            p = null;
            downgraded = true;
        }
        final Double prob = p;
        final String original = text;
        JudgmentLinter.Result checked = JudgmentLinter.check(text, prob, messages -> llm.complete(ctx, outcome,
                PromptTemplates.CYCLE_ANALYST, regeneratePrompt(original, prob, messages)));
        warnings.addAll(checked.warnings());
        lint.addAll(checked.warnings());
        if (checked.dropped()) {
            outcome.warn("judgment " + index + " dropped: " + String.join("; ", checked.warnings()));
            return null;
        }
        downgraded |= checked.downgraded();
        String confidenceStatement = text(m.get("confidenceStatement"));
        if (confidenceStatement != null && containsLikelihood(confidenceStatement)) {
            String repaired = stripLikelihood(confidenceStatement);
            warnings.add("likelihood term removed from the confidence statement");
            lint.add("likelihood term removed from the confidence statement");
            confidenceStatement = repaired.isBlank() ? null : repaired;
        }
        String confidence = text(m.get("confidence"));
        if (confidence != null && !List.of("low", "moderate", "high").contains(confidence)) {
            confidence = null;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", "kj-" + index);
        out.put("text", checked.text());
        out.put("probability", prob);
        out.put("estimativeTerm", prob == null ? null : EstimativeLanguage.term(prob));
        out.put("confidence", confidence);
        out.put("confidenceStatement", confidenceStatement);
        out.put("citations", citations);
        out.put("hypothesisSetId", setId);
        out.put("questionId", questionId);
        out.put("textOrigin", "llm");
        out.put("change", change(questionId, setId, checked.text(), text(m.get("changeExplanation")), previous));
        out.put("downgraded", downgraded);
        out.put("warnings", warnings);
        return out;
    }

    /** The change against the previous judgment on the same question, set or text, or a "new" change. */
    static Map<String, Object> change(String questionId, String setId, String text, String explanation,
            List<Map<String, Object>> previous) {
        Map<String, Object> match = AssessmentDiff.match(questionId, setId, text, previous);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("previousProbability", match == null ? null : match.get("probability"));
        out.put("previousText", match == null ? null : match.get("text"));
        out.put("explanation", explanation != null && !explanation.isBlank() ? explanation
                : match == null ? "New judgment in this assessment." : "No change explained by the analyst.");
        return out;
    }

    private static String regeneratePrompt(String text, Double p, List<String> problems) {
        StringBuilder out = new StringBuilder("Rewrite this key judgment so that it follows the wording rules. ");
        out.append("Problems: ").append(String.join("; ", problems)).append(". ");
        if (p != null) {
            out.append("Its probability is ").append(Math.round(p * 1000) / 10.0).append("%, whose ICD 203 term is '")
                    .append(EstimativeLanguage.term(p)).append("'. ");
        }
        out.append("Keep the meaning, use only first-row ICD 203 terms, put no confidence level in it, and reply with"
                + " the rewritten judgment text only.\n\nJudgment (untrusted data):\n").append(text);
        return out.toString();
    }

    /** True when {@code text} uses any likelihood term of either ICD row. */
    static boolean containsLikelihood(String text) {
        String lower = " " + text.toLowerCase(Locale.ROOT).replaceAll("[^a-z ]", " ") + " ";
        for (String term : EstimativeLanguage.TERMS) {
            if (lower.contains(" " + term + " ")) {
                return true;
            }
        }
        for (String term : EstimativeLanguage.SECOND_ROW) {
            if (lower.contains(" " + term + " ")) {
                return true;
            }
        }
        return false;
    }

    /** {@code text} with every likelihood term removed (longest first). */
    static String stripLikelihood(String text) {
        List<String> terms = new ArrayList<>(EstimativeLanguage.TERMS);
        terms.addAll(EstimativeLanguage.SECOND_ROW);
        terms.sort((a, b) -> b.length() - a.length());
        String out = text;
        for (String term : terms) {
            out = out.replaceAll("(?i)\\b" + java.util.regex.Pattern.quote(term) + "\\b", "");
        }
        return out.replaceAll("\\s{2,}", " ").replaceAll("\\s+([.,;:])", "$1").strip();
    }

    // ========================================================================= red team

    /**
     * A set briefed to the red team.
     *
     * @param setId the set
     * @param question the set's question
     * @param questionId the set's ledger question, or null
     * @param hypotheses the hypotheses ({@code id, label, probability, estimativeTerm, type}), most probable first
     * @param assumptions the key assumptions ({@code id, text, status, linchpin})
     * @param stakes the stakes
     * @param movement the period's largest move
     */
    public record SetBrief(String setId, String question, String questionId, List<Map<String, Object>> hypotheses,
            List<Map<String, Object>> assumptions, double stakes, double movement) {}

    /**
     * The red team's output.
     *
     * @param outcome the role outcome
     * @param notes the validated notes, one per challenged set
     */
    public record RedTeamResult(RoleOutcome outcome, List<Map<String, Object>> notes) {}

    /**
     * Runs the red team.
     *
     * @param ctx the role context
     * @param sets the sets chosen by {@link RedTeamSelection}
     * @param profileKey the profile key of the forecaster id
     */
    public RedTeamResult redTeam(RoleContext ctx, List<SetBrief> sets, String profileKey) {
        RoleOutcome outcome = new RoleOutcome(CycleVocabulary.RED_TEAM);
        outcome.using(ctx.providerId(), ctx.effort(), PromptTemplates.CYCLE_RED_TEAM);
        if (sets.isEmpty()) {
            outcome.finish(CycleVocabulary.SKIPPED, "no active hypothesis set in scope");
            return new RedTeamResult(outcome, List.of());
        }
        Map<String, SetBrief> byId = new LinkedHashMap<>();
        List<Map<String, Object>> input = new ArrayList<>();
        for (SetBrief s : sets) {
            byId.put(s.setId(), s);
            ctx.citations().seed("hypothesis-set:" + s.setId());
            if (s.questionId() != null) {
                ctx.citations().seed("question:" + s.questionId());
            }
            Map<String, Object> brief = new LinkedHashMap<>();
            brief.put("setId", s.setId());
            brief.put("question", s.question());
            brief.put("ledgerQuestionId", s.questionId());
            brief.put("hypotheses", s.hypotheses());
            brief.put("keyAssumptions", s.assumptions());
            input.add(brief);
        }
        List<Map<String, Object>> captured = new ArrayList<>();
        String user = "Hypothesis sets to challenge (untrusted data):\n" + llm.wrap("hypothesis_sets", input)
                + "\nCall record_red_team once per set.";
        llm.loop(ctx, outcome, PromptTemplates.CYCLE_RED_TEAM, List.of(new ChatMessage.User(user)),
                CycleOutputTools.redTeam(json), call -> captured.add(CycleOutputTools.args(call.input(), json)));
        Map<String, Map<String, Object>> notes = new LinkedHashMap<>();
        for (Map<String, Object> raw : captured) {
            String setId = text(raw.get("setId"));
            SetBrief brief = setId == null ? null : byId.get(setId);
            if (brief == null) {
                outcome.warn("red-team note for an unknown set " + setId + " dropped");
                continue;
            }
            Map<String, Object> note = redTeamNote(ctx, outcome, brief, raw, profileKey);
            notes.put(setId, note);
        }
        if (notes.isEmpty()) {
            outcome.finish(CycleVocabulary.SKIPPED, "the model recorded no red-team note");
        }
        return new RedTeamResult(outcome, List.copyOf(notes.values()));
    }

    private Map<String, Object> redTeamNote(RoleContext ctx, RoleOutcome outcome, SetBrief brief,
            Map<String, Object> raw, String profileKey) {
        List<String> warnings = new ArrayList<>();
        List<String> citations = validCitations(ctx, strings(raw.get("citations")), warnings);
        List<Map<String, Object>> disconfirming = new ArrayList<>();
        for (Object item : list(raw.get("disconfirmingEvidence"))) {
            if (item instanceof Map<?, ?> m) {
                Map<String, Object> e = map(m);
                List<String> cites = validCitations(ctx, strings(e.get("citations")), warnings);
                Map<String, Object> clean = new LinkedHashMap<>();
                clean.put("text", lintText(text(e.get("text")), warnings));
                clean.put("citations", cites);
                clean.put("basis", cites.isEmpty() ? "speculation" : "evidence");
                disconfirming.add(clean);
            } else if (item != null) {
                Map<String, Object> clean = new LinkedHashMap<>();
                clean.put("text", lintText(String.valueOf(item), warnings));
                clean.put("citations", List.of());
                clean.put("basis", "speculation");
                disconfirming.add(clean);
            }
        }
        Map<String, Object> note = new LinkedHashMap<>();
        note.put("setId", brief.setId());
        note.put("question", brief.question());
        note.put("runnerUpHypothesisId", text(raw.get("runnerUpHypothesisId")));
        note.put("strongestCase", lintText(text(raw.get("strongestCase")), warnings));
        note.put("disconfirmingEvidence", disconfirming);
        List<String> assumptions = new ArrayList<>();
        for (String a : strings(raw.get("assumptionsAtRisk"))) {
            assumptions.add(lintText(a, warnings));
        }
        note.put("assumptionsAtRisk", assumptions);
        note.put("premortem", lintText(text(raw.get("premortem")), warnings));
        note.put("citations", citations);
        note.put("forecastId", null);
        if (brief.questionId() != null && (raw.get("probability") instanceof Number
                || raw.get("distribution") instanceof Map<?, ?>)) {
            note.put("forecastId", redTeamForecast(ctx, outcome, brief, raw, profileKey, citations, warnings));
        }
        note.put("warnings", warnings);
        for (String w : warnings) {
            outcome.warn(w);
        }
        return note;
    }

    private String redTeamForecast(RoleContext ctx, RoleOutcome outcome, SetBrief brief, Map<String, Object> raw,
            String profileKey, List<String> citations, List<String> warnings) {
        ForecastQuestionEntity q = ledger.find(brief.questionId()).orElse(null);
        if (q == null || !q.acceptsForecasts(clock.instant())) {
            warnings.add("the set's ledger question does not accept forecasts; no red-team forecast recorded");
            return null;
        }
        ForecastValue value = value(q, raw);
        if (value == null) {
            warnings.add("the red-team probability does not fit the question type; no forecast recorded");
            return null;
        }
        try {
            ForecasterEntity forecaster = forecasters.ensure(redTeamId(profileKey), "llm", "LLM red team ("
                    + profileKey + ")", FORECASTER_VERSION, forecasterConfig(ctx.providerId(), profileKey, "red-team"));
            Map<String, Object> inputs = new LinkedHashMap<>();
            inputs.put("provider", ctx.providerId());
            inputs.put("model", outcome.model());
            inputs.put("effort", ctx.effort());
            inputs.put("promptHash", llm.promptHash(PromptTemplates.CYCLE_RED_TEAM, ctx.profile(),
                    CycleOutputTools.redTeam(json)));
            inputs.put("samples", 1);
            inputs.put("role", "red-team");
            inputs.put("runId", ctx.runId());
            String rationale = "Red team. Strongest case for the runner-up: " + text(raw.get("strongestCase"))
                    + "\nPremortem: " + text(raw.get("premortem"));
            var f = ledger.record(q, forecaster, value, truncate(rationale), refs(citations), List.of(), inputs,
                    cutoff(ctx.providerId()), false);
            outcome.forecastSubmitted();
            return f.getId();
        } catch (ForesignalException | IllegalStateException e) {
            warnings.add("red-team forecast not recorded: " + e.getMessage());
            return null;
        }
    }

    // ========================================================================= forecaster

    /**
     * Runs the forecaster role for every configured provider.
     *
     * @param base the context of the default provider
     * @param providerIds the providers (empty: the default provider only)
     * @param scope the scope
     * @param cadence the cycle cadence
     * @param maxQuestions the question cap
     * @param samples samples per question
     * @param profileKey the profile key
     */
    public RoleOutcome forecaster(RoleContext base, List<String> providerIds, CycleScope scope, String cadence,
            int maxQuestions, int samples, String profileKey) {
        RoleOutcome outcome = new RoleOutcome(CycleVocabulary.FORECASTER);
        outcome.using(base.providerId(), base.effort(), PromptTemplates.CYCLE_FORECASTER);
        List<String> ids = providerIds == null || providerIds.isEmpty() ? List.of(base.providerId())
                : List.copyOf(new LinkedHashSet<>(providerIds));
        boolean anyQuestion = false;
        String stop = null;
        for (String providerId : ids) {
            RoleContext ctx = providerId.equals(base.providerId()) ? base : llm.forProvider(base, providerId)
                    .orElse(null);
            if (ctx == null) {
                outcome.warn("forecaster provider " + providerId + " is not configured; skipped");
                continue;
            }
            ForecasterEntity forecaster = forecasters.ensure(forecasterId(profileKey, providerId), "llm",
                    "LLM forecaster (" + profileKey + ", " + providerId + ")", FORECASTER_VERSION,
                    forecasterConfig(providerId, profileKey, "forecaster"));
            if (!forecaster.isActive()) {
                outcome.warn("forecaster " + forecaster.getId() + " is inactive; skipped");
                continue;
            }
            List<QuestionPriority.Candidate> chosen = QuestionPriority.choose(candidates(scope, cadence,
                    forecaster.getId(), ctx.privateAllowed()), maxQuestions);
            anyQuestion |= !chosen.isEmpty();
            for (QuestionPriority.Candidate c : chosen) {
                ForecastQuestionEntity q = ledger.find(c.questionId()).orElse(null);
                if (q == null || !q.acceptsForecasts(clock.instant())) {
                    continue;
                }
                try {
                    forecastQuestion(ctx, outcome, q, Math.max(1, Math.min(CycleVocabulary.MAX_SAMPLES, samples)),
                            forecaster);
                } catch (RoleSkippedException e) {
                    stop = e.getMessage();
                    break;
                }
            }
            if (stop != null) {
                break;
            }
        }
        if (stop != null) {
            outcome.warn("stopped early: " + stop);
            if (outcome.forecastsSubmitted() == 0) {
                outcome.finish(CycleVocabulary.SKIPPED, stop);
            }
        } else if (!anyQuestion) {
            outcome.finish(CycleVocabulary.SKIPPED, "no open question in scope");
        } else if (outcome.forecastsSubmitted() == 0) {
            outcome.finish(CycleVocabulary.SKIPPED, "no sample produced a valid forecast");
        }
        return outcome;
    }

    /** The open questions in scope that accept forecasts now, with their priority inputs. */
    List<QuestionPriority.Candidate> candidates(CycleScope scope, String cadence, String forecasterId,
            boolean privateAllowed) {
        Instant now = clock.instant();
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("now", Timestamp.from(now))
                .addValue("forecaster", forecasterId);
        Map<String, Instant> last = new HashMap<>();
        jdbc.query("SELECT question_id, max(made_at) AS last FROM forecast WHERE forecaster_id = :forecaster"
                + " GROUP BY question_id", p, rs -> {
                    last.put(rs.getString("question_id"), rs.getTimestamp("last").toInstant());
                });
        List<QuestionPriority.Candidate> out = new ArrayList<>();
        jdbc.query("SELECT q.id, q.resolve_by, q.ensemble_probability, q.category, q.situation_id,"
                + " q.hypothesis_set_id, q.\"private\", s.severity, s.region_codes, s.theme, h.cadence AS set_cadence,"
                + " h.region_codes AS set_regions FROM forecast_question q LEFT JOIN situation s ON s.id = q.situation_id"
                + " LEFT JOIN hypothesis_set h ON h.id = q.hypothesis_set_id WHERE q.status = 'open'"
                + " AND q.opens_at <= :now AND q.closes_at > :now ORDER BY q.id LIMIT 5000", p, rs -> {
                    if (rs.getBoolean("private") && !privateAllowed) {
                        return;
                    }
                    List<String> regions = new ArrayList<>(CyclePacketReader.array(rs, "region_codes"));
                    regions.addAll(CyclePacketReader.array(rs, "set_regions"));
                    if (!scope.admits(regions, rs.getString("theme"), rs.getString("situation_id"),
                            rs.getString("hypothesis_set_id"), rs.getString("set_cadence"), List.of(),
                            rs.getString("category"), cadence)) {
                        return;
                    }
                    Double prob = rs.getObject("ensemble_probability") == null ? null
                            : rs.getDouble("ensemble_probability");
                    out.add(new QuestionPriority.Candidate(rs.getString("id"), rs.getString("severity"), prob,
                            CyclePacketReader.instant(rs, "resolve_by"), last.get(rs.getString("id"))));
                });
        return out;
    }

    /** One sample's result. */
    private record Sample(ForecastValue value, Map<String, Object> outsideView, List<Object> adjustments,
            List<Map<String, Object>> claims, List<String> citations, List<String> toolOrder) {}

    /** Forecasts one question with {@code samples} independent outside-then-inside samples and records the median. */
    void forecastQuestion(RoleContext ctx, RoleOutcome outcome, ForecastQuestionEntity q, int samples,
            ForecasterEntity forecaster) {
        ctx.citations().seed("question:" + q.getId());
        if (q.getHypothesisSetId() != null) {
            ctx.citations().seed("hypothesis-set:" + q.getHypothesisSetId());
        }
        if (q.getSituationId() != null) {
            ctx.citations().seed("situation:" + q.getSituationId());
        }
        Map<String, Object> view = questionView(q);
        List<Sample> done = new ArrayList<>();
        for (int i = 1; i <= samples; i++) {
            Sample s = sample(ctx, outcome, q, view, i, samples);
            if (s != null) {
                done.add(s);
            }
        }
        List<ForecastValue> values = done.stream().map(Sample::value).toList();
        ForecastValue aggregate = SampleAggregator.aggregate(q.getType(), values);
        if (aggregate == null) {
            outcome.warn("question " + q.getId() + ": no valid sample; nothing recorded");
            return;
        }
        List<String> citations = new ArrayList<>();
        List<Map<String, Object>> claims = new ArrayList<>();
        List<Object> referenceClasses = new ArrayList<>();
        List<Object> baseRates = new ArrayList<>();
        List<Object> sampleValues = new ArrayList<>();
        List<Object> orders = new ArrayList<>();
        for (Sample s : done) {
            for (String c : s.citations()) {
                if (!citations.contains(c)) {
                    citations.add(c);
                }
            }
            claims.addAll(s.claims());
            referenceClasses.add(s.outsideView().get("referenceClass"));
            baseRates.add(s.outsideView().get("baseRate"));
            sampleValues.add(valueMap(s.value()));
            orders.add(s.toolOrder());
        }
        Map<String, Object> inputs = new LinkedHashMap<>();
        inputs.put("provider", ctx.providerId());
        inputs.put("model", outcome.model());
        inputs.put("effort", ctx.effort());
        inputs.put("promptHash", llm.promptHash(PromptTemplates.CYCLE_FORECASTER, ctx.profile(),
                CycleOutputTools.forecaster(json)));
        inputs.put("samples", samples);
        inputs.put("samplesUsed", done.size());
        inputs.put("aggregation", "median log-odds");
        inputs.put("sampleValues", sampleValues);
        inputs.put("variation", "varied tool-call ordering seeds (temperature is not set through the provider API)");
        inputs.put("outsideView", Map.of("referenceClasses", referenceClasses, "baseRates", baseRates));
        inputs.put("toolOrder", orders);
        inputs.put("runId", ctx.runId());
        String rationale = "Resolution criteria (verbatim): " + q.getResolutionCriteria() + "\nOutside view: "
                + referenceClasses + " at base rates " + baseRates + ".\nInside view: median of " + done.size()
                + " samples by log-odds.";
        try {
            ledger.record(q, forecaster, aggregate, truncate(rationale), refs(citations), claims, inputs,
                    cutoff(ctx.providerId()), false);
            outcome.forecastSubmitted();
        } catch (ForesignalException | IllegalStateException e) {
            outcome.warn("question " + q.getId() + ": forecast not recorded: " + e.getMessage());
        }
    }

    private Sample sample(RoleContext ctx, RoleOutcome outcome, ForecastQuestionEntity q, Map<String, Object> view,
            int index, int samples) {
        List<Map<String, Object>> outside = new ArrayList<>();
        boolean analogsFirst = index % 2 == 0;
        String header = "Sample " + index + " of " + samples + ". Outside view: quote the resolution criteria"
                + " verbatim, call " + (analogsFirst ? "find_analogs and then get_base_rate" : "get_base_rate and then"
                + " find_analogs") + ", state the reference class and its rate, and call record_outside_view.\n"
                + "Question (untrusted data):\n" + llm.wrap("question", view);
        ToolLoop.Result first = llm.loop(ctx, outcome, PromptTemplates.CYCLE_FORECASTER,
                List.of(new ChatMessage.User(header)), List.of(CycleOutputTools.forecaster(json).get(0)),
                call -> outside.add(CycleOutputTools.args(call.input(), json)));
        List<String> order = toolOrder(first.conversation());
        if (outside.isEmpty()) {
            outcome.warn("question " + q.getId() + " sample " + index + ": no outside view recorded; discarded");
            return null;
        }
        if (!order.contains("get_base_rate") && !order.contains("find_analogs")) {
            outcome.warn("question " + q.getId() + " sample " + index + ": the outside view called neither"
                    + " get_base_rate nor find_analogs");
        }
        Map<String, Object> ov = outside.get(outside.size() - 1);
        if (!normalized(text(ov.get("criteriaQuoted"))).equals(normalized(q.getResolutionCriteria()))) {
            outcome.warn("question " + q.getId() + " sample " + index + ": the resolution criteria were not quoted"
                    + " verbatim");
        }
        List<Map<String, Object>> forecast = new ArrayList<>();
        List<ChatMessage> next = new ArrayList<>(first.conversation());
        next.add(new ChatMessage.User("Inside view: adjust from your outside view with cited evidence (and the"
                + " linked hypothesis set's state, when there is one), then call record_forecast."));
        ToolLoop.Result second = llm.loop(ctx, outcome, PromptTemplates.CYCLE_FORECASTER, next,
                List.of(CycleOutputTools.forecaster(json).get(1)),
                call -> forecast.add(CycleOutputTools.args(call.input(), json)));
        order.addAll(toolOrder(second.conversation().subList(Math.min(first.conversation().size(),
                second.conversation().size()), second.conversation().size())));
        if (forecast.isEmpty()) {
            outcome.warn("question " + q.getId() + " sample " + index + ": no forecast recorded; discarded");
            return null;
        }
        Map<String, Object> f = forecast.get(forecast.size() - 1);
        ForecastValue value = value(q, f);
        if (value == null) {
            outcome.warn("question " + q.getId() + " sample " + index + ": the value does not fit the question;"
                    + " discarded");
            return null;
        }
        List<String> warnings = new ArrayList<>();
        List<String> citations = validCitations(ctx, strings(f.get("citations")), warnings);
        citations.addAll(validCitations(ctx, strings(ov.get("citations")), warnings));
        List<Map<String, Object>> claims = new ArrayList<>();
        Map<String, Object> outsideClaim = new LinkedHashMap<>();
        outsideClaim.put("text", "Outside view: " + text(ov.get("referenceClass")) + (ov.get("baseRate") == null ? ""
                : " at " + ov.get("baseRate")));
        outsideClaim.put("basis", "inference");
        outsideClaim.put("citations", validCitations(ctx, strings(ov.get("citations")), warnings));
        claims.add(outsideClaim);
        for (Object raw : list(f.get("claims"))) {
            if (raw instanceof Map<?, ?> m) {
                Map<String, Object> c = map(m);
                List<String> cites = validCitations(ctx, strings(c.get("citations")), warnings);
                Map<String, Object> claim = new LinkedHashMap<>();
                claim.put("text", text(c.get("text")));
                String basis = text(c.get("basis"));
                claim.put("basis", cites.isEmpty() ? "speculation"
                        : basis == null || !List.of("evidence", "inference", "speculation").contains(basis)
                                ? "inference" : basis);
                claim.put("citations", cites);
                claims.add(claim);
            }
        }
        return new Sample(value, ov, list(f.get("adjustments")), claims, citations, order);
    }

    /** The names of the tools the model called in {@code conversation}, in order. */
    static List<String> toolOrder(List<ChatMessage> conversation) {
        List<String> out = new ArrayList<>();
        for (ChatMessage message : conversation) {
            if (message instanceof ChatMessage.Assistant assistant) {
                for (ToolCall call : assistant.toolCalls()) {
                    out.add(call.name());
                }
            }
        }
        return out;
    }

    /** The question as the forecaster sees it, criteria verbatim. */
    static Map<String, Object> questionView(ForecastQuestionEntity q) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("questionId", q.getId());
        out.put("title", q.getTitle());
        out.put("type", q.getType());
        out.put("outcomes", q.getOutcomes());
        out.put("resolutionCriteria", q.getResolutionCriteria());
        out.put("opensAt", q.getOpensAt() == null ? null : q.getOpensAt().toString());
        out.put("closesAt", q.getClosesAt() == null ? null : q.getClosesAt().toString());
        out.put("resolveBy", q.getResolveBy() == null ? null : q.getResolveBy().toString());
        out.put("category", q.getCategory());
        out.put("hypothesisSetId", q.getHypothesisSetId());
        out.put("situationId", q.getSituationId());
        out.put("ensembleProbability", q.getEnsembleProbability());
        return out;
    }

    /** The forecast value of a tool call for question {@code q}, validated; null when it does not fit. */
    ForecastValue value(ForecastQuestionEntity q, Map<String, Object> raw) {
        ForecastValue value;
        if (Ledger.BINARY.equals(q.getType())) {
            value = raw.get("probability") instanceof Number n ? ForecastValue.binary(n.doubleValue()) : null;
        } else if (Ledger.NUMERIC.equals(q.getType())) {
            Map<String, Double> quantiles = doubles(raw.get("quantiles"));
            value = quantiles.isEmpty() ? null : ForecastValue.quantiles(quantiles);
        } else {
            Map<String, Double> distribution = doubles(raw.get("distribution"));
            value = distribution.isEmpty() ? null : ForecastValue.distribution(distribution);
        }
        if (value == null) {
            return null;
        }
        try {
            ForecastInputs.validateValue(value, q.getType(), q.labels());
            return value;
        } catch (ForesignalException e) {
            return null;
        }
    }

    private static Map<String, Object> valueMap(ForecastValue v) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("probability", v.probability());
        out.put("distribution", v.distribution());
        out.put("quantiles", v.quantiles());
        return out;
    }

    // ========================================================================= proposer

    /**
     * Runs the proposer.
     *
     * @param ctx the role context
     * @param llmPacket the capped packet
     * @param profile the cycle's 102 profile (proposals belong to it)
     */
    public RoleOutcome proposer(RoleContext ctx, Map<String, Object> llmPacket, AnalystProfileEntity profile) {
        RoleOutcome outcome = new RoleOutcome(CycleVocabulary.PROPOSER);
        outcome.using(ctx.providerId(), ctx.effort(), PromptTemplates.CYCLE_PROPOSER);
        List<AnalystOutputValidator.RawProposal> raws = new ArrayList<>();
        String user = "Analysis cycle packet (untrusted data):\n" + llm.wrap("cycle_packet", llmPacket)
                + "\nPropose what the evidence supports with the proposal tools.";
        llm.loop(ctx, outcome, PromptTemplates.CYCLE_PROPOSER, List.of(new ChatMessage.User(user)),
                CycleOutputTools.proposer(json), call -> raws.add(rawProposal(call)));
        AnalystOutputValidator.Validated validated = AnalystOutputValidator.validate(null, "", null, List.of(), raws,
                ctx.citations()::valid, (type, members) -> false, Set.copyOf(CycleOutputTools.PROPOSALS),
                EventTypes.ALL);
        validated.warnings().forEach(outcome::warn);
        Instant now = clock.instant();
        List<AnalystProposalEntity> saved = new ArrayList<>();
        for (AnalystOutputValidator.AnalystProposalSpec spec : validated.proposals()) {
            String problem = check(spec, now);
            if (problem != null) {
                outcome.warn("dropped proposal " + spec.type() + ": " + problem);
                continue;
            }
            AnalystProposalEntity proposal = new AnalystProposalEntity(Ulids.next(now), ctx.runId(), profile.getId(),
                    spec.type(), now, now.plus(Duration.ofDays(7)));
            proposal.setPrivateProposal(ctx.privateSeen().get());
            proposal.setPayload(spec.payload());
            proposal.setRationale(spec.rationale());
            proposal.setCitations(spec.citations());
            saved.add(proposals.save(proposal));
            outcome.proposal(proposal.getId());
        }
        AnalystProposalEvents events = proposalEvents.getIfAvailable();
        if (events != null) {
            saved.forEach(p -> events.publish(p, AnalystProposalEvents.CREATED));
        }
        if (profile.isAllowActions() && !saved.isEmpty()) {
            BenchmarkGate gate = benchmarks.getIfAvailable();
            if (gate != null && !gate.proposerAutoExecutionAllowed(null)) {
                outcome.warn("auto-execution held: a component the proposer depends on failed its latest benchmark;"
                        + " the proposals stay pending");
            } else {
                AnalystRunEntity transientRun = new AnalystRunEntity(ctx.runId(), profile.getId(), now);
                execution.autoExecute(profile, transientRun, saved, null);
            }
        }
        if (saved.isEmpty()) {
            outcome.finish(CycleVocabulary.OK, "no proposal");
        }
        return outcome;
    }

    /** A captured proposal call as a 102 raw proposal. */
    AnalystOutputValidator.RawProposal rawProposal(ToolCall call) {
        Map<String, Object> args = CycleOutputTools.args(call.input(), json);
        Map<String, Object> payload = new LinkedHashMap<>();
        switch (call.name()) {
            case ai.foresignal.analyst.AnalystActions.PROPOSE_HYPOTHESIS_SET -> payload.put("set", args.get("set"));
            case ai.foresignal.analyst.AnalystActions.PROPOSE_INDICATOR -> {
                payload.put("setId", args.get("setId"));
                payload.put("indicator", args.get("indicator"));
            }
            case ai.foresignal.analyst.AnalystActions.PROPOSE_WATCH -> payload.put("rule", args.get("rule"));
            case ai.foresignal.analyst.AnalystActions.PROPOSE_QUESTION -> payload.put("question", args.get("question"));
            default -> {
                payload.put("situationId", args.get("situationId"));
                payload.put("intoId", args.get("intoId"));
                payload.put("reason", args.get("reason"));
            }
        }
        return new AnalystOutputValidator.RawProposal(call.name(), payload, text(args.get("rationale")),
                strings(args.get("citations")));
    }

    /** Why a proposal is invalid, or null: the 125 draft, the 122 question, existing sets and situations. */
    String check(AnalystOutputValidator.AnalystProposalSpec spec, Instant now) {
        Map<String, Object> payload = spec.payload();
        try {
            switch (spec.type()) {
                case ai.foresignal.analyst.AnalystActions.PROPOSE_HYPOTHESIS_SET -> {
                    if (!(payload.get("set") instanceof Map<?, ?> set)) {
                        return "no set";
                    }
                    HypothesisInputs.set(map(set));
                }
                case ai.foresignal.analyst.AnalystActions.PROPOSE_INDICATOR -> {
                    String setId = text(payload.get("setId"));
                    if (setId == null || hypotheses.find(setId).isEmpty()) {
                        return "unknown set " + setId;
                    }
                    if (!(payload.get("indicator") instanceof Map<?, ?> indicator)) {
                        return "no indicator";
                    }
                    HypothesisInputs.indicator(map(indicator), "indicator");
                }
                case ai.foresignal.analyst.AnalystActions.PROPOSE_QUESTION -> {
                    if (!(payload.get("question") instanceof Map<?, ?> question)) {
                        return "no question";
                    }
                    ForecastInputs.question(json.valueToTree(question), now);
                }
                case ai.foresignal.analyst.AnalystActions.PROPOSE_SITUATION_MERGE -> {
                    String a = text(payload.get("situationId"));
                    String b = text(payload.get("intoId"));
                    if (a == null || b == null || a.equals(b)) {
                        return "two different situations are required";
                    }
                    situations.get(a);
                    situations.get(b);
                }
                default -> {
                    if (!(payload.get("rule") instanceof Map<?, ?>)) {
                        return "no rule";
                    }
                }
            }
            return null;
        } catch (ForesignalException e) {
            String message = e.getMessage();
            if (e instanceof ai.foresignal.api.error.ValidationException v && v.getErrors() != null
                    && !v.getErrors().isEmpty()) {
                message = message + " (" + v.getErrors().get(0).field() + ": " + v.getErrors().get(0).message() + ")";
            }
            return message;
        }
    }

    // ========================================================================= helpers

    private Map<String, Object> forecasterConfig(String providerId, String profileKey, String role) {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("provider", providerId);
        config.put("profile", profileKey);
        config.put("role", role);
        return config;
    }

    /** The provider's {@code knowledgeCutoff}, or null. */
    LocalDate cutoff(String providerId) {
        LlmProviderRepository repo = providers.getIfAvailable();
        if (repo == null || providerId == null) {
            return null;
        }
        try {
            return repo.findById(providerId).map(LlmProviderEntity::getKnowledgeCutoff).orElse(null);
        } catch (RuntimeException e) {
            log.debug("No knowledge cutoff for {}: {}", providerId, e.toString());
            return null;
        }
    }

    private static List<String> validCitations(RoleContext ctx, List<String> raw, List<String> warnings) {
        List<String> out = new ArrayList<>();
        for (String c : raw) {
            if (ctx.citations().valid(c)) {
                if (!out.contains(c)) {
                    out.add(c);
                }
            } else {
                warnings.add("dropped citation [" + c + "]: not returned by any tool or the input");
            }
        }
        return out;
    }

    private static String lintText(String text, List<String> warnings) {
        if (text == null) {
            return null;
        }
        JudgmentLinter.Result r = JudgmentLinter.check(text, null, null);
        warnings.addAll(r.warnings());
        return r.dropped() ? "[removed: the text could not be brought within the wording rules]" : r.text();
    }

    private static List<Map<String, Object>> refs(List<String> citations) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (String c : citations) {
            if (ForecastInputs.OBJECT_REF.matcher(c).matches() && out.size() < ForecastInputs.MAX_CITATIONS) {
                out.add(Map.of("ref", c));
            }
        }
        return out;
    }

    private static String truncate(String text) {
        return text.length() > ForecastInputs.MAX_RATIONALE ? text.substring(0, ForecastInputs.MAX_RATIONALE) : text;
    }

    private static String normalized(String text) {
        return text == null ? "" : text.replaceAll("\\s+", " ").strip();
    }

    private static Map<String, Double> doubles(Object raw) {
        Map<String, Double> out = new LinkedHashMap<>();
        if (raw instanceof Map<?, ?> m) {
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (e.getValue() instanceof Number n) {
                    out.put(String.valueOf(e.getKey()), n.doubleValue());
                }
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> map(Map<?, ?> m) {
        Map<String, Object> out = new LinkedHashMap<>();
        m.forEach((k, v) -> out.put(String.valueOf(k), v));
        return out;
    }

    static List<Object> list(Object raw) {
        return raw instanceof List<?> l ? new ArrayList<>(l) : new ArrayList<>();
    }

    static List<String> strings(Object raw) {
        List<String> out = new ArrayList<>();
        for (Object o : list(raw)) {
            if (o != null && !String.valueOf(o).isBlank()) {
                out.add(String.valueOf(o).strip());
            }
        }
        return out;
    }

    static String text(Object raw) {
        return raw == null || String.valueOf(raw).isBlank() ? null : String.valueOf(raw);
    }
}
