package ai.foresignal.chronicle.network;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.exchange.CircularReportingDetector;
import ai.foresignal.exchange.ProvenanceChains;
import ai.foresignal.federation.FederationIdentity;
import ai.foresignal.hypothesis.AchEvidenceEntity;
import ai.foresignal.hypothesis.AchEvidenceRepository;
import ai.foresignal.hypothesis.ConfidenceAssessor;
import ai.foresignal.hypothesis.HypothesisEntity;
import ai.foresignal.hypothesis.HypothesisInputs;
import ai.foresignal.hypothesis.HypothesisRepository;
import ai.foresignal.hypothesis.HypothesisSetEntity;
import ai.foresignal.report.ReportEntity;
import ai.foresignal.report.ReportItSupport;
import ai.foresignal.security.Scope;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * Circular reporting (prompt 134, DISC-3): a judgment that comes back through another analyst is marked
 * {@code CIRCULAR_REPORTING}, shares its origin's evidence group, and is counted once by the hypothesis updater and the
 * confidence assessor.
 */
@IntegrationTest
@Import(AskTestSources.class)
class CircularReportingTest extends ReportItSupport {

    @Autowired CircularReportingDetector detector;
    @Autowired ProvenanceChains chains;
    @Autowired FederationIdentity identity;
    @Autowired AchEvidenceRepository evidenceRows;
    @Autowired HypothesisRepository hypothesisRows;
    @Autowired ai.foresignal.collab.ShareService shares;

    private static Map<String, Object> hop(String instance, String principal, String ref, String role) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("instanceId", instance);
        e.put("principal", principal);
        e.put("productRef", ref);
        e.put("manifestId", null);
        e.put("role", role);
        e.put("at", null);
        return e;
    }

    @Test
    @DisplayName("a chain that returns to the consuming analyst is circular; importers and the last hop are not")
    void chainRules() {
        List<Map<String, Object>> chain = List.of(
                hop("fs-a", "alice", "report:X", "origin"),
                hop("fs-b", "bob", "remote:fs-a/report:X", "importer"),
                hop("fs-b", "bob", "report:Y", "author"),
                hop("fs-a", "carol", "remote:fs-b/report:Y", "importer"));
        assertThat(CircularReportingDetector.isCircular(chain, "fs-a", "alice")).isTrue();
        assertThat(CircularReportingDetector.isCircular(chain, "fs-a", "carol")).as("only the importer").isFalse();
        assertThat(CircularReportingDetector.isCircular(chain, "fs-a", "dave")).isFalse();
        assertThat(CircularReportingDetector.isCircular(chain, "fs-c", "alice")).isFalse();
        assertThat(CircularReportingDetector.isCircular(chain, "fs-a", null)).isFalse();
        assertThat(ProvenanceChains.originKey(chain)).isEqualTo("origin:fs-a/report:X");
        List<Map<String, Object>> other = List.of(hop("fs-a", "alice", "report:X", "origin"),
                hop("fs-c", "erin", "report:Z", "author"));
        assertThat(CircularReportingDetector.sharedOrigin(chain, other)).isTrue();
        assertThat(CircularReportingDetector.sharedOrigin(chain, List.of(hop("fs-c", "erin", "report:Z", "origin"))))
                .isFalse();
        assertThat(ProvenanceChains.chained("report:01JA8Q3V7S2K9W4T6Y1B5N0M3C")).isTrue();
        assertThat(ProvenanceChains.chained("record:src/e1")).isFalse();
    }

    @Test
    @DisplayName("the confidence basis reports circular support")
    void assessorBasis() {
        ConfidenceAssessor.Inputs in = new ConfidenceAssessor.Inputs(List.of(), false, false, null, null, null, false,
                2);
        assertThat(ConfidenceAssessor.assess(in).basis()).containsEntry("circularReporting",
                ConfidenceAssessor.CIRCULAR_REPORTING).containsEntry("circularSupports", 2);
        assertThat(ConfidenceAssessor.assess(new ConfidenceAssessor.Inputs(List.of(), false, false, null, null, null,
                false)).basis()).containsEntry("circularReporting", "ok");
    }

    @Test
    @DisplayName("a report that returns through another analyst is marked circular and counted once")
    void returnsThroughAnotherAnalyst() {
        Person alice = person("alice-circ", Scope.AGENT, null);
        Person bob = person("bob-circ", Scope.AGENT, null);
        HypothesisSetEntity set = hypothesisSet(null);
        List<HypothesisEntity> hyps = hypothesisRows.findBySetIdOrderByPositionAscIdAsc(set.getId());

        ReportEntity x = report(alice, "{'title':'Alice: convoy massing','blocks':[{'type':'markdown',"
                + "'markdown':'Convoys mass near the border.'}]}");
        shares.share("report:" + x.getId(), node("{'grantees':['principal:" + bob.id() + "'],'role':'viewer'}"),
                alice.caller(), "test");
        ReportEntity y = report(bob, "{'title':'Bob: response likely','blocks':[{'type':'objects','items':[{'ref':"
                + "'report:" + x.getId() + "'}]}]}");

        List<Map<String, Object>> chainOfY = chains.of("report:" + y.getId());
        assertThat(chainOfY).hasSize(2);
        assertThat(chainOfY.get(0)).containsEntry("productRef", "report:" + x.getId()).containsEntry("principal",
                "alice-circ").containsEntry("instanceId", identity.instanceId());
        assertThat(chainOfY.get(1)).containsEntry("productRef", "report:" + y.getId()).containsEntry("role", "author");
        assertThat(detector.assess("report:" + y.getId(), alice.id()).orElseThrow().circular()).isTrue();
        assertThat(detector.assess("report:" + y.getId(), bob.id()).orElseThrow().circular())
                .as("bob is the last hop").isFalse();

        Map<String, String> ratings = Map.of(hyps.get(0).getId(), "CC", hyps.get(1).getId(), "I");
        AchEvidenceEntity first = hypotheses.addEvidence(set.getId(), new HypothesisInputs.EvidenceDraft("manual",
                "report:" + y.getId(), "Bob's report says a response is likely", "M", "H", ratings, List.of()),
                alice.id(), true, alice.id());
        assertThat(first.getCaveats()).containsExactly(CircularReportingDetector.CIRCULAR_REPORTING);
        assertThat(first.getEvidenceGroupKey()).isEqualTo("origin:" + identity.instanceId() + "/report:" + x.getId());
        assertThat(first.getProvenanceChain()).hasSize(2);

        AchEvidenceEntity second = hypotheses.addEvidence(set.getId(), new HypothesisInputs.EvidenceDraft("manual",
                "report:" + x.getId(), "Alice's own report", "M", "H", ratings, List.of()), alice.id(), true,
                alice.id());
        assertThat(second.getId()).as("the same origin is one evidence group").isEqualTo(first.getId());
        assertThat(second.getRefs()).containsExactly("report:" + y.getId(), "report:" + x.getId());
        List<AchEvidenceEntity> live = evidenceRows.findBySetIdOrderByIdAsc(set.getId()).stream()
                .filter(e -> e.getRemovedAt() == null).toList();
        assertThat(live).hasSize(1);
        Long revisions = jdbc.queryForObject("SELECT count(*) FROM hypothesis_revision WHERE set_id = ?"
                + " AND cause = 'evidence-added'", Long.class, set.getId());
        assertThat(revisions).as("the updater counted the shared origin once").isEqualTo(1L);
    }
}
