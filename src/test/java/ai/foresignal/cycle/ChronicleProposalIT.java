package ai.foresignal.cycle;

import static ai.foresignal.testsupport.llm.FakeLlmClient.call;
import static ai.foresignal.testsupport.llm.FakeLlmClient.text;
import static ai.foresignal.testsupport.llm.FakeLlmClient.toolUse;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.analyst.AnalystActions;
import ai.foresignal.analyst.AnalystProfileEntity;
import ai.foresignal.analyst.AnalystProposalEntity;
import ai.foresignal.analyst.AnalystProposalRepository;
import ai.foresignal.benchmark.BenchmarkGate;
import ai.foresignal.benchmark.BenchmarkRunEntity;
import ai.foresignal.benchmark.BenchmarkRunRepository;
import ai.foresignal.benchmark.BenchmarkThresholds;
import ai.foresignal.chronicle.ChronicleEntity;
import ai.foresignal.chronicle.ChronicleProposals;
import ai.foresignal.chronicle.ChronicleRepository;
import ai.foresignal.chronicle.ChronicleService;
import ai.foresignal.chronicle.ChronicleSizes;
import ai.foresignal.chronicle.ChronicleSuggester;
import ai.foresignal.chronicle.ChronicleVocabulary;
import ai.foresignal.llm.client.ChatRequest;
import ai.foresignal.llm.client.ChatResponse;
import ai.foresignal.llm.client.TokenUsage;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.PrincipalKind;
import ai.foresignal.security.PrincipalRepository;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenCreatedVia;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.Ulids;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.situation.SituationEntity;
import ai.foresignal.situation.SituationEvent;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * Chronicle proposals (prompt 131): a situation that turns active at warning or above gets one proposed chronicle
 * (deterministic suggestion: area, member layers, the situation layer), in the owner's inbox; accepting it activates
 * it and executes its 102 proposal; the 128 proposer (a scripted fake model) proposes one with
 * {@code propose_chronicle}; a profile that allows the action activates at most {@code chronicle.autoActivatePerDay}
 * per day, each capped at {@code chronicle.autoActivateCapGb}.
 */
@IntegrationTest
@Import(AskTestSources.class)
class ChronicleProposalIT extends CycleItSupport {

    static final TokenUsage USAGE = new TokenUsage(100, 50, 0);

    @Autowired ChronicleProposals proposals;
    @Autowired ChronicleService chronicles;
    @Autowired ChronicleRepository chronicleRows;
    @Autowired ChronicleSuggester suggester;
    @Autowired AnalystProposalRepository proposalRows;
    @Autowired AnalysisCycleRepository cycleRows;
    @Autowired PrincipalRepository principalRows;
    @Autowired PrincipalService principals;
    @Autowired BenchmarkRunRepository benchmarkRuns;
    @Autowired BenchmarkThresholds thresholds;
    @Autowired BenchmarkGate gate;

    private SituationEvent activated(SituationEntity s, String severity) {
        return new SituationEvent(1, SituationEvent.ACTIVATED, s.getId(), 1,
                Map.of("title", s.getTitle(), "severity", severity, "status", "active"), "emerging", "info", null,
                clock.instant());
    }

    private List<ChronicleEntity> proposedFor(String situationId) {
        return chronicleRows.findAll().stream().filter(c -> c.getSituationIds().contains(situationId)).toList();
    }

    /**
     * A finished benchmark run in which every component the proposer's auto-execution depends on passed: the 123
     * gate holds auto-execution while any of them is unknown or failing.
     */
    void proposerGateOpen() {
        List<Map<String, Object>> results = new ArrayList<>();
        for (String component : thresholds.dependencies(BenchmarkGate.PROPOSER_AUTO_EXECUTION)) {
            results.add(Map.of("component", component, "status", "pass"));
        }
        BenchmarkRunEntity run = new BenchmarkRunEntity(Ulids.next(clock.instant()), "synthetic", clock.instant());
        run.setStatus("pass");
        run.setSuiteVersion("1.0.0");
        run.setScorerVersion("1.0.0");
        run.setThresholdsVersion("1.0.0");
        run.setTrigger("manual");
        run.setResults(results);
        run.setStartedAt(clock.instant());
        run.setFinishedAt(clock.instant());
        benchmarkRuns.saveAndFlush(run);
        gate.invalidate();
        assertThat(gate.proposerAutoExecutionAllowed(null)).isTrue();
    }

    @Test
    @DisplayName("a situation active at warning gets one proposed chronicle with the deterministic suggestion")
    void proposedOnActivation() {
        SituationEntity s = situation("Red Sea shipping attacks", "warning");
        SituationEntity quiet = situation("Quiet harbor", "info");

        proposals.onEvent(activated(s, "warning"));
        proposals.onEvent(activated(s, "warning"));
        proposals.onEvent(activated(quiet, "info"));

        List<ChronicleEntity> made = proposedFor(s.getId());
        assertThat(made).singleElement().satisfies(c -> {
            assertThat(c.getStatus()).isEqualTo(ChronicleVocabulary.PROPOSED);
            assertThat(c.getOrigin()).isEqualTo(ChronicleVocabulary.ORIGIN_ANALYST);
            assertThat(c.getName()).isEqualTo("Red Sea shipping attacks");
            assertThat(c.getArea()).isNotNull();
            assertThat(c.isLeafCreated()).isFalse();
        });
        assertThat(chronicles.liveLayers(made.getFirst().getId())).extracting(l -> l.getSelector().get("type"))
                .contains("situations");
        assertThat(proposedFor(quiet.getId())).isEmpty();
        String owner = principals.owner().getId();
        assertThat(jdbc.queryForList("SELECT event FROM principal_inbox WHERE principal_id = ?", String.class, owner))
                .contains("chronicle.proposed");

        Map<String, Object> suggestion = suggester.fromSituation(s.getId());
        assertThat(suggestion).containsKeys("draft", "estimate", "rationale").containsEntry("textOrigin",
                "deterministic");
    }

    @Test
    @DisplayName("accepting a proposed chronicle activates it and executes its pending 102 proposal")
    void acceptedToActive() {
        AnalystProfileEntity profile = profile(null);
        AnalysisCycleEntity cycle = cycle("daily", "[\"proposer\"]", profile.getId(), null);
        cycle.setEnabled(true);
        cycleRows.save(cycle);
        SituationEntity s = situation("Strait closure", "critical");

        proposals.onEvent(activated(s, "critical"));

        ChronicleEntity c = proposedFor(s.getId()).getFirst();
        AnalystProposalEntity proposal = proposalRows.findById(c.getProposalId()).orElseThrow();
        assertThat(proposal.getType()).isEqualTo(AnalystActions.PROPOSE_CHRONICLE);
        assertThat(proposal.getStatus()).isEqualTo("pending");
        assertThat(proposal.getPayload()).containsEntry("chronicleId", c.getId());

        ChronicleEntity active = chronicles.activate(c.getId(), null, ObjectCaller.system(), false);
        assertThat(active.getStatus()).isEqualTo(ChronicleVocabulary.ACTIVE);
        assertThat(proposalRows.findById(proposal.getId()).orElseThrow().getStatus()).isEqualTo("executed");
    }

    @Test
    @DisplayName("the 128 proposer's propose_chronicle creates a proposed chronicle linked to its pending proposal")
    void llmProposer() {
        SituationEntity s = situation("Pipeline outage", "warning");
        fakes.client(CHAT).otherwise(proposerModel(s.getId()));
        AnalystProfileEntity profile = profile(null);
        AnalysisCycleEntity cycle = cycle("daily", "[\"proposer\"]", profile.getId(), null);

        CycleRunEntity run = runNow(cycle);

        List<AnalystProposalEntity> made = proposalRows.findAll().stream()
                .filter(p -> run.getId().equals(p.getRunId())).toList();
        assertThat(made).singleElement().satisfies(p -> {
            assertThat(p.getType()).isEqualTo(AnalystActions.PROPOSE_CHRONICLE);
            assertThat(p.getStatus()).isEqualTo("pending");
            assertThat(p.getPayload().get("chronicleId")).isNotNull();
        });
        ChronicleEntity c = chronicleRows.findById(String.valueOf(made.getFirst().getPayload().get("chronicleId")))
                .orElseThrow();
        assertThat(c.getStatus()).isEqualTo(ChronicleVocabulary.PROPOSED);
        assertThat(c.getSituationIds()).containsExactly(s.getId());
        assertThat(c.getProposalId()).isEqualTo(made.getFirst().getId());
    }

    @Test
    @DisplayName("a profile that allows the action auto-activates within the daily cap and the size cap")
    void autoActivationCap() {
        PrincipalEntity owner = new PrincipalEntity(Ulids.next(clock.instant()), "cycle-owner", "Cycle owner",
                PrincipalKind.PERSON, clock.instant());
        principalRows.saveAndFlush(owner);
        TokenService.CreatedToken token = tokens.create(new TokenService.TokenSpec("cycle-owner-token", Scope.AGENT,
                null, TokenRestrictions.NONE, null, TokenCreatedVia.API, owner.getId()));
        AnalystProfileEntity profile = profile(null);
        profile.setAllowActions(true);
        profile.setAllowedActions(List.of(AnalystActions.PROPOSE_CHRONICLE));
        profile.setMaxAutoActionsPerDay(10);
        profile.setOwnerTokenId(token.entity().getId().toString());
        profiles.save(profile);
        AnalysisCycleEntity cycle = cycle("daily", "[\"proposer\"]", profile.getId(), null);
        cycle.setEnabled(true);
        cycleRows.save(cycle);
        settings.put(SettingKeys.CHRONICLE_AUTO_ACTIVATE_PER_DAY, 1.0);
        proposerGateOpen();

        SituationEntity first = situation("First front", "warning");
        SituationEntity second = situation("Second front", "warning");
        proposals.onEvent(activated(first, "warning"));
        proposals.onEvent(activated(second, "warning"));

        ChronicleEntity a = proposedFor(first.getId()).getFirst();
        ChronicleEntity b = proposedFor(second.getId()).getFirst();
        assertThat(a.getStatus()).isEqualTo(ChronicleVocabulary.ACTIVE);
        assertThat(a.getAutoActivatedAt()).isNotNull();
        assertThat(a.getSizeCapBytes()).isLessThanOrEqualTo(5 * ChronicleSizes.GB);
        assertThat(b.getStatus()).as("over the daily cap the chronicle waits for a person").isEqualTo(
                ChronicleVocabulary.PROPOSED);
        AnalystProposalEntity failed = proposalRows.findById(b.getProposalId()).orElseThrow();
        assertThat(failed.getStatus()).isEqualTo("failed");
        assertThat(failed.getReason()).contains("daily cap");
        assertThat(a.getSituationIds()).containsExactly(first.getId());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM shareable_object WHERE object_ref = ? AND owner = ?",
                Long.class, ChronicleService.ref(a.getId()), owner.getId())).isEqualTo(1);
        Optional<ChronicleEntity> again = proposals.proposeForSituation(first.getId(), "again");
        assertThat(again).isEmpty();
        assertThat(Set.copyOf(chronicleRows.findAll().stream().map(ChronicleEntity::getId).toList())).hasSize(2);
    }

    /** A model whose proposer proposes a chronicle for {@code situationId}; the other roles finish at once. */
    static Function<ChatRequest, ChatResponse> proposerModel(String situationId) {
        return request -> {
            if (request.system().startsWith("You are the proposer") && turnsSinceUser(request) == 0) {
                return toolUse(USAGE, call("c1", AnalystActions.PROPOSE_CHRONICLE, "{\"situationId\":\""
                        + situationId + "\",\"rationale\":\"An emerging situation worth recording.\","
                        + "\"citations\":[]}"));
            }
            return text("done", USAGE);
        };
    }
}
