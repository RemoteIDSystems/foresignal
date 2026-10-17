package ai.foresignal.object;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.benchmark.BenchmarkCaseEntity;
import ai.foresignal.benchmark.BenchmarkCaseRepository;
import ai.foresignal.security.Scope;
import ai.foresignal.situation.SituationEntity;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;

/** {@link AnnotationService}, {@link PrecisionService} and {@link BenchmarkCaseProposals} (prompt 129). */
@IntegrationTest
@Import(AskTestSources.class)
class AnnotationIT extends ObjectItSupport {

    @Autowired PrecisionService precision;
    @Autowired BenchmarkCaseProposals proposals;
    @Autowired BenchmarkCaseRepository cases;

    private static JsonNode body(String json) {
        return JSON.readTree(json.replace('\'', '"'));
    }

    private AnnotationEntity verdict(String ref, String value, ObjectCaller caller) {
        return annotations.create(ObjectRef.parse(ref), body("{'kind':'verdict','verdict':'" + value + "'}"),
                new ResolveContext(caller, null));
    }

    @Test
    @DisplayName("read tokens add notes; verdicts need agent scope")
    void notesAndVerdicts() {
        var f = finding("Loitering cluster", "cusum", 43.2, 14.9, false, null);
        ObjectCaller reader = objectCaller("p-reader", Scope.READ, false);
        AnnotationEntity note = annotations.create(ObjectRef.parse("finding:" + f.getId()),
                body("{'kind':'note','body':'Check the port notice','tags':['port']}"), new ResolveContext(reader, null));
        assertThat(note.getKind()).isEqualTo("note");
        assertThat(note.getAuthor()).isEqualTo("p-reader");
        assertThat(note.getTags()).containsExactly("port");
        assertThatThrownBy(() -> verdict("finding:" + f.getId(), "confirmed", reader))
                .isInstanceOf(ForesignalException.class)
                .satisfies(e -> assertThat(((ForesignalException) e).getStatus().value()).isEqualTo(403));
        assertThatThrownBy(() -> annotations.create(ObjectRef.parse("finding:" + f.getId()), body("{'kind':'tag'}"),
                new ResolveContext(reader, null))).isInstanceOf(ValidationException.class);
        AnnotationEntity v = verdict("finding:" + f.getId(), "confirmed", objectCaller("p-agent", Scope.AGENT, false));
        assertThat(v.getTargetComponent()).isEqualTo("finding");
        assertThat(v.getTargetKey()).isEqualTo("cusum 1.0.0");

        Map<String, Object> page = annotations.list(ObjectRef.parse("finding:" + f.getId()), null, 50,
                new ResolveContext(reader, null));
        assertThat((List<?>) page.get("items")).hasSize(2);

        assertThatThrownBy(() -> annotations.patch(note.getId(), body("{'body':'mine now'}"),
                objectCaller("p-other", Scope.AGENT, false))).isInstanceOf(ForesignalException.class);
        assertThat(annotations.patch(note.getId(), body("{'body':'Port notice confirms'}"), reader).getBody())
                .isEqualTo("Port notice confirms");
        annotations.delete(note.getId(), objectCaller("p-admin", Scope.ADMIN, true));
        assertThatThrownBy(() -> annotations.get(note.getId(), reader)).isInstanceOf(NotFoundException.class)
                .satisfies(e -> assertThat(((NotFoundException) e).getCode()).isEqualTo(ErrorCodes.UNKNOWN_ANNOTATION));
    }

    @Test
    @DisplayName("annotations of private objects are private and invisible without private access")
    void privateAnnotations() {
        var f = finding("Private finding", "cusum", 44.0, 13.0, true, null);
        ObjectCaller privateAgent = objectCaller("p-private", Scope.AGENT, true);
        AnnotationEntity a = annotations.create(ObjectRef.parse("finding:" + f.getId()),
                body("{'kind':'note','body':'Sensitive'}"), new ResolveContext(privateAgent, null));
        assertThat(a.isPrivateAnnotation()).isTrue();
        assertThatThrownBy(() -> annotations.get(a.getId(), objectCaller("p-reader", Scope.READ, false)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("precision: the share confirmed among the latest verdicts per producer, with a Wilson interval")
    void precisionWithWilson() {
        ObjectCaller agent = objectCaller("p-agent", Scope.AGENT, false);
        List<AnalyticFindingEntity> fs = List.of(finding("a", "cusum", 43, 14, false, null),
                finding("b", "cusum", 43, 14, false, null), finding("c", "cusum", 43, 14, false, null),
                finding("d", "cusum", 43, 14, false, null), finding("e", "bocpd", 43, 14, false, null));
        verdict("finding:" + fs.get(0).getId(), "rejected", agent);
        clock.setInstant(clock.instant().plusSeconds(1));
        verdict("finding:" + fs.get(0).getId(), "confirmed", agent);
        verdict("finding:" + fs.get(1).getId(), "confirmed", agent);
        verdict("finding:" + fs.get(2).getId(), "rejected", agent);
        verdict("finding:" + fs.get(3).getId(), "needs-review", agent);
        verdict("finding:" + fs.get(4).getId(), "confirmed", agent);
        SituationEntity s = situation("Red Sea shipping disruption", "warning");
        verdict("situation:" + s.getId(), "confirmed", agent);

        JsonNode p = JSON.valueToTree(precision.precision("finding", null, false));
        JsonNode cusum = p.get("components").findParents("key").stream()
                .filter(n -> n.get("key").asString().equals("cusum 1.0.0")).findFirst().orElseThrow();
        assertThat(cusum.get("confirmed").asLong()).isEqualTo(2);
        assertThat(cusum.get("rejected").asLong()).isEqualTo(1);
        assertThat(cusum.get("needsReview").asLong()).isEqualTo(1);
        assertThat(cusum.get("labelled").asLong()).isEqualTo(3);
        assertThat(cusum.get("precision").asDouble()).isEqualTo(2.0 / 3.0);
        double[] wilson = ai.foresignal.analytics.math.Wilson.interval(2, 3);
        assertThat(cusum.get("interval").get("lo").asDouble()).isEqualTo(wilson[0]);
        assertThat(cusum.get("interval").get("hi").asDouble()).isEqualTo(wilson[1]);
        assertThat(p.get("components").findValuesAsString("component")).containsOnly("finding");

        JsonNode all = JSON.valueToTree(precision.precision(null, null, false));
        assertThat(all.get("components").findValuesAsString("component")).contains("finding", "situation");
        assertThatThrownBy(() -> precision.precision("vibes", null, false)).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("verdicts on objects with a time window propose benchmark cases; nothing is added until accepted")
    void benchmarkProposals() {
        var f = finding("Loitering cluster", "cusum", 43.2, 14.9, false, null);
        verdict("finding:" + f.getId(), "confirmed", objectCaller("p-agent", Scope.AGENT, false));
        List<BenchmarkCaseProposalEntity> pending = proposals.pending();
        assertThat(pending).hasSize(1);
        BenchmarkCaseProposalEntity p = pending.get(0);
        assertThat(p.getTargetRef()).isEqualTo("finding:" + f.getId());
        assertThat(p.getWindowTo()).isAfter(p.getWindowFrom());
        assertThat(cases.findBySource("operator")).isEmpty();
        BenchmarkCaseEntity created = proposals.accept(p.getId(), "operator");
        assertThat(created.getSource()).isEqualTo("operator");
        assertThat(proposals.pending()).isEmpty();
        verdict("finding:" + f.getId(), "needs-review", objectCaller("p-agent", Scope.AGENT, false));
        assertThat(proposals.pending()).isEmpty();
    }

    @Test
    @DisplayName("a rejected verdict dismisses a finding or intel event only with an explicit applyStatus")
    void applyStatusOnlyWhenExplicit() {
        ObjectCaller agent = objectCaller("p-agent", Scope.AGENT, false);
        var f = finding("Loitering cluster", "cusum", 43.2, 14.9, false, null);
        verdict("finding:" + f.getId(), "rejected", agent);
        assertThat(findingRows.findById(f.getId()).orElseThrow().getStatus()).isEqualTo("open");
        AnnotationEntity applied = annotations.create(ObjectRef.parse("finding:" + f.getId()),
                body("{'kind':'verdict','verdict':'rejected','body':'Fishing fleet','applyStatus':true}"),
                new ResolveContext(agent, null));
        assertThat(applied.isStatusApplied()).isTrue();
        assertThat(findingRows.findById(f.getId()).orElseThrow().getStatus()).isEqualTo("dismissed");

        var e = intelEvent("Port closure", false, "port-closure");
        annotations.create(ObjectRef.parse("intel-event:" + e.getId()),
                body("{'kind':'verdict','verdict':'rejected','applyStatus':true}"), new ResolveContext(agent, null));
        assertThat(eventRows.findById(e.getId()).orElseThrow().getStatus()).isEqualTo("dismissed");

        var g = finding("Another", "cusum", 43.2, 14.9, false, null);
        assertThatThrownBy(() -> annotations.create(ObjectRef.parse("finding:" + g.getId()),
                body("{'kind':'verdict','verdict':'confirmed','applyStatus':true}"), new ResolveContext(agent, null)))
                .isInstanceOf(ValidationException.class);
        assertThat(findingRows.findById(g.getId()).orElseThrow().getStatus()).isEqualTo("open");
    }
}
