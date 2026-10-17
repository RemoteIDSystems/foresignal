package ai.foresignal.cycle;

import static ai.foresignal.testsupport.llm.FakeLlmClient.call;
import static ai.foresignal.testsupport.llm.FakeLlmClient.text;
import static ai.foresignal.testsupport.llm.FakeLlmClient.toolUse;

import ai.foresignal.analyst.AnalystProfileEntity;
import ai.foresignal.analyst.AnalystProfileRepository;
import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.hypothesis.HypothesisInputs;
import ai.foresignal.hypothesis.HypothesisService;
import ai.foresignal.hypothesis.HypothesisSetEntity;
import ai.foresignal.llm.client.ChatMessage;
import ai.foresignal.llm.client.ChatRequest;
import ai.foresignal.llm.client.ChatResponse;
import ai.foresignal.llm.client.TokenUsage;
import ai.foresignal.security.Ulids;
import ai.foresignal.situation.SituationEntity;
import ai.foresignal.situation.SituationService;
import ai.foresignal.testsupport.ask.AskItSupport;
import ai.foresignal.testsupport.llm.FakeLlmClient;
import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Shared fixtures of the prompt 128 integration tests, on the Ask support (providers, fakes, tokens, clock): the
 * scheduler stopped, the built-in forecasters registered, seeding helpers for situations, hypothesis sets, questions,
 * profiles and cycles, and a scripted model that answers each cycle role by its system prompt.
 */
abstract class CycleItSupport extends AskItSupport {

    @Autowired protected CycleService cycles;
    @Autowired protected CycleRunner runner;
    @Autowired protected CycleScheduler scheduler;
    @Autowired protected AssessmentService assessments;
    @Autowired protected CycleRunRepository runs;
    @Autowired protected AssessmentRepository assessmentRows;
    @Autowired protected HypothesisService hypotheses;
    @Autowired protected ForecastService ledger;
    @Autowired protected ForecasterRegistry forecasters;
    @Autowired protected SituationService situations;
    @Autowired protected AnalystProfileRepository profiles;
    @Autowired protected IntelBoardService board;
    @Autowired protected MockMvc mvc;

    /** The scheduler off, the forecasters registered, the board cache empty. */
    @BeforeEach
    void setUpCycles() {
        scheduler.setEnabled(false);
        forecasters.registerBuiltIns();
        board.clearCache();
    }

    /** An active situation in Yemen with a severity override. */
    protected SituationEntity situation(String title, String severity) {
        return situations.create(JSON.readTree("{\"title\":\"" + title + "\",\"theme\":\"maritime\",\"status\":"
                + "\"active\",\"severityOverride\":\"" + severity + "\",\"regionCodes\":[\"YE\"]}"), "tester",
                "operator", true);
    }

    /** The worked example's exclusive outcome set (daily review), linked to {@code situationId} when given. */
    protected HypothesisSetEntity hypothesisSet(String situationId) {
        String body = "{\"question\":\"Coalition response within 7 days?\",\"kind\":\"exclusive\",\"cadence\":\"daily\","
                + (situationId == null ? "" : "\"situationId\":\"" + situationId + "\",")
                + "\"regionCodes\":[\"YE\"],\"hypotheses\":["
                + "{\"label\":\"within 72 h\",\"statement\":\"A response follows within 72 hours.\",\"type\":\"outcome\","
                + "\"prior\":0.25,\"priorBasis\":{\"type\":\"operator\",\"rationale\":\"Comparable strikes.\"}},"
                + "{\"label\":\"3-7 days\",\"statement\":\"A response follows within 3 to 7 days.\",\"type\":\"outcome\","
                + "\"prior\":0.2,\"priorBasis\":{\"type\":\"operator\",\"rationale\":\"Slower cases.\"}},"
                + "{\"label\":\"none in 7 days\",\"statement\":\"No response within 7 days.\",\"type\":\"outcome\","
                + "\"prior\":0.5,\"priorBasis\":{\"type\":\"operator\",\"rationale\":\"Restraint during talks.\"}},"
                + "{\"label\":\"other\",\"statement\":\"Other / none of these.\",\"type\":\"other\",\"prior\":0.05,"
                + "\"priorBasis\":{\"type\":\"operator\",\"rationale\":\"Residual mass.\"}}],\"indicators\":[],"
                + "\"assumptions\":[{\"text\":\"The coalition can strike at short notice.\",\"linchpin\":true,"
                + "\"status\":\"solid\"}]}";
        return hypotheses.create(HypothesisInputs.set(JSON.readTree(body)), "operator", "tester");
    }

    /** A binary manual question closing in 5 days, linked to a set and situation when given. */
    protected ForecastQuestionEntity question(String title, String setId, String situationId) {
        String links = "{" + (setId == null ? "" : "\"hypothesisSetId\":\"" + setId + "\"")
                + (setId != null && situationId != null ? "," : "")
                + (situationId == null ? "" : "\"situationId\":\"" + situationId + "\"") + "}";
        String body = "{\"title\":\"" + title + "\",\"type\":\"binary\",\"resolutionCriteria\":\"Yes if " + title
                + " by the closing time, as reported by two independent outlets.\",\"resolver\":{\"type\":\"manual\"},"
                + "\"closesAt\":\"" + clock.instant().plus(Duration.ofDays(5)) + "\",\"resolveBy\":\""
                + clock.instant().plus(Duration.ofDays(6)) + "\",\"category\":\"conflict\",\"links\":" + links + "}";
        return ledger.openQuestion(ForecastInputs.question(JSON.readTree(body), clock.instant()), "operator", "tester")
                .question();
    }

    /** An analyst profile on {@code providerId} (null: the chat provider) without actions. */
    protected AnalystProfileEntity profile(String providerId) {
        AnalystProfileEntity p = new AnalystProfileEntity(Ulids.next(clock.instant()), "cycle-profile",
                clock.instant());
        p.setEnabled(false);
        p.setProviderId(providerId);
        p.setEffort("high");
        p.setMaxTokensPerRun(200_000);
        p.setMaxToolCalls(10);
        p.setAllowActions(false);
        p.setAllowedActions(List.of());
        p.setChannelIds(List.of());
        return profiles.save(p);
    }

    /** A cycle created through the service. */
    protected AnalysisCycleEntity cycle(String cadence, String roles, String profileId, String extra) {
        String body = "{\"name\":\"" + cadence + " cycle\",\"cadence\":\"" + cadence + "\",\"schedule\":{\"cron\":"
                + "\"0 6 * * *\",\"timezone\":\"UTC\"},\"roles\":" + roles
                + (profileId == null ? "" : ",\"analystProfileId\":\"" + profileId + "\"")
                + (extra == null ? "" : "," + extra) + ",\"enabled\":false}";
        return cycles.create(CycleInputs.cycle(JSON.readTree(body)));
    }

    /** Runs a cycle synchronously. */
    protected CycleRunEntity runNow(AnalysisCycleEntity cycle) {
        CycleRunEntity run = runner.queue(cycle, "manual");
        return runner.execute(run.getId());
    }

    // ------------------------------------------------------------------------- the scripted model

    static final Pattern SAMPLE = Pattern.compile("Sample (\\d+) of (\\d+)");

    /** Probabilities the scripted forecaster gives samples 1, 2, 3. */
    static final double[] SAMPLES = {0.2, 0.6, 0.3};

    /** Assistant turns since the last user message. */
    static int turnsSinceUser(ChatRequest request) {
        int turns = 0;
        for (ChatMessage m : request.messages()) {
            if (m instanceof ChatMessage.User) {
                turns = 0;
            } else if (m instanceof ChatMessage.Assistant) {
                turns++;
            }
        }
        return turns;
    }

    static String lastUser(ChatRequest request) {
        String out = "";
        for (ChatMessage m : request.messages()) {
            if (m instanceof ChatMessage.User u) {
                out = u.text();
            }
        }
        return out;
    }

    /** The first id after {@code prefix} in {@code text} ("question:01J…"). */
    static String idAfter(String text, String prefix) {
        Matcher m = Pattern.compile(Pattern.quote(prefix) + "([0-9A-Z]{26})").matcher(text);
        return m.find() ? m.group(1) : null;
    }

    /**
     * A scripted model: the analyst records two judgments citing the packet's first question, the red team one note
     * per set with a probability, the forecaster calls get_base_rate and find_analogs then records the outside view,
     * then its sample's probability, and the proposer proposes a question.
     *
     * @param usage the token use reported per turn
     */
    static java.util.function.Function<ChatRequest, ChatResponse> model(TokenUsage usage) {
        return request -> {
            String system = request.system();
            int turns = turnsSinceUser(request);
            String user = lastUser(request);
            if (system.startsWith("You are the analyst of a Foresignal analysis cycle")) {
                if (turns > 0) {
                    return text("done", usage);
                }
                String q = idAfter(user, "question:");
                String set = idAfter(user, "hypothesis-set:");
                return toolUse(usage, call("a1", CycleOutputTools.RECORD_JUDGMENTS, "{\"judgments\":["
                        + "{\"text\":\"A coalition response within 72 hours is likely.\",\"probability\":0.62,"
                        + "\"confidence\":\"moderate\",\"confidenceStatement\":\"Analytic confidence is moderate.\","
                        + "\"citations\":[\"question:" + q + "\"],\"questionId\":\"" + q + "\","
                        + "\"changeExplanation\":\"New indicators were observed.\"},"
                        + "{\"text\":\"Shipping keeps avoiding the strait.\",\"citations\":[\"question:" + q + "\","
                        + "\"nonsense:XYZ\"]},"
                        + "{\"text\":\"The set " + set + " is under review.\",\"citations\":[]}],"
                        + "\"whatWeCannotSee\":\"Airbase activity is not observed.\"}"));
            }
            if (system.startsWith("You are the red team")) {
                if (turns > 0) {
                    return text("done", usage);
                }
                String set = idAfter(user, "\"setId\":\"");
                return toolUse(usage, call("r1", CycleOutputTools.RECORD_RED_TEAM, "{\"setId\":\"" + set + "\","
                        + "\"strongestCase\":\"Talks are progressing.\",\"disconfirmingEvidence\":[{\"text\":"
                        + "\"No strike aircraft were reported.\",\"citations\":[]}],\"assumptionsAtRisk\":"
                        + "[\"Strike capacity is intact.\"],\"premortem\":\"Diplomacy succeeded.\","
                        + "\"probability\":0.35,\"citations\":[]}"));
            }
            if (system.startsWith("You are a forecaster")) {
                if (user.startsWith("Inside view")) {
                    if (turns > 0) {
                        return text("done", usage);
                    }
                    Matcher sample = SAMPLE.matcher(String.valueOf(request.messages().get(0) instanceof
                            ChatMessage.User u ? u.text() : ""));
                    int index = sample.find() ? Integer.parseInt(sample.group(1)) : 1;
                    return toolUse(usage, call("f2", CycleOutputTools.RECORD_FORECAST, "{\"adjustments\":[{\"text\":"
                            + "\"Indicators observed\"}],\"probability\":" + SAMPLES[(index - 1) % 3]
                            + ",\"claims\":[{\"text\":\"Two indicators were observed.\",\"basis\":\"inference\","
                            + "\"citations\":[]}]}"));
                }
                String criteria = user.contains("\"resolutionCriteria\":\"") ? user.substring(user.indexOf(
                        "\"resolutionCriteria\":\"") + 22).split("\"")[0] : "";
                return switch (turns) {
                    case 0 -> toolUse(usage, call("b1", "get_base_rate", "{\"family\":\"conflict:battles\","
                            + "\"regionCodes\":[\"YE\"]}"));
                    case 1 -> toolUse(usage, call("b2", "find_analogs", "{\"regionCode\":\"YE\"}"));
                    case 2 -> toolUse(usage, call("b3", CycleOutputTools.RECORD_OUTSIDE_VIEW, "{\"criteriaQuoted\":\""
                            + criteria + "\",\"referenceClass\":\"Coalition responses since 2015\",\"baseRate\":0.3}"));
                    default -> text("done", usage);
                };
            }
            if (system.startsWith("You are the proposer")) {
                if (turns > 0) {
                    return text("done", usage);
                }
                return toolUse(usage, call("p1", "propose_question", "{\"question\":{\"title\":\"Port closure by"
                        + " Friday?\",\"type\":\"binary\",\"resolutionCriteria\":\"Yes if the port closes.\","
                        + "\"resolver\":{\"type\":\"manual\"},\"closesAt\":\"2026-10-20T00:00:00Z\",\"resolveBy\":"
                        + "\"2026-10-21T00:00:00Z\",\"category\":\"conflict\"},\"rationale\":\"Packet shows closures.\","
                        + "\"citations\":[]}"));
            }
            return text("rewritten text", usage);
        };
    }

    /** Scripts {@code fake} with {@link #model}. */
    static FakeLlmClient scripted(FakeLlmClient fake, TokenUsage usage) {
        return fake.otherwise(model(usage));
    }
}
