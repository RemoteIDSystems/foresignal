package ai.foresignal.object;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.Scope;
import ai.foresignal.security.Ulids;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;

/** {@link RelationService} graph queries: subgraphs, validity and common dependencies (prompt 129). */
@IntegrationTest
@Import(AskTestSources.class)
class GraphQueryTest extends ObjectItSupport {

    @org.springframework.beans.factory.annotation.Autowired RelationQuery relationQuery;

    static String q(String id) {
        return "record:" + AskTestSources.QUAKES + "/" + id;
    }

    private RelationEntity edge(String from, String to, String type, String label, Instant validFrom, Instant validTo,
            Instant createdAt) {
        RelationEntity r = new RelationEntity(Ulids.next(createdAt), q(from), q(to), type, validFrom, createdAt);
        r.setValidTo(validTo);
        r.setConfidence(ObjectTypes.labelRank(label) == 0 ? 1.0 : 0.6);
        r.setLabel(label);
        r.setProvenance(Map.of("operator", "tester"));
        return relationRows.save(r);
    }

    private void nodes(String... ids) {
        double lon = 40;
        for (String id : ids) {
            quake(id, "Node " + id, 4.0, lon, 10.0);
            lon += 0.5;
        }
    }

    @Test
    @DisplayName("depth, type filters and the edge limit bound the subgraph")
    void depthAndTypeLimits() {
        nodes("q1", "q2", "q3", "q4");
        Instant t = clock.instant();
        edge("q1", "q2", ObjectTypes.MEMBER_OF, "confirmed", t.minus(Duration.ofDays(1)), null, t);
        edge("q2", "q3", ObjectTypes.DEPENDS_ON, "likely", t.minus(Duration.ofDays(1)), null, t);
        edge("q3", "q4", ObjectTypes.DEPENDS_ON, "possible", t.minus(Duration.ofDays(1)), null, t);
        ResolveContext ctx = context(Scope.READ, false, null);

        JsonNode one = JSON.valueToTree(relationService.graph(List.of(ObjectRef.parse(q("q1"))), 1, Set.of(), 0, 500,
                ctx));
        assertThat(one.get("edges")).hasSize(1);
        assertThat(one.get("nodes").findValuesAsString("ref")).containsExactlyInAnyOrder(q("q1"), q("q2"));

        JsonNode two = JSON.valueToTree(relationService.graph(List.of(ObjectRef.parse(q("q1"))), 2, Set.of(), 0, 500,
                ctx));
        assertThat(two.get("edges")).hasSize(2);

        JsonNode typed = JSON.valueToTree(relationService.graph(List.of(ObjectRef.parse(q("q2"))), 2,
                Set.of(ObjectTypes.DEPENDS_ON), 0, 500, ctx));
        assertThat(typed.get("edges").findValuesAsString("type")).containsOnly(ObjectTypes.DEPENDS_ON).hasSize(2);

        JsonNode limited = JSON.valueToTree(relationService.graph(List.of(ObjectRef.parse(q("q2"))), 2, Set.of(), 0, 1,
                ctx));
        assertThat(limited.get("edges")).hasSize(1);
        assertThat(limited.get("truncated").asBoolean()).isTrue();

        JsonNode strong = JSON.valueToTree(relationService.graph(List.of(ObjectRef.parse(q("q2"))), 2, Set.of(), 0.9,
                500, ctx));
        assertThat(strong.get("edges")).hasSize(1);

        assertThatThrownBy(() -> relationService.graph(JSON.readTree("{\"refs\":[\"" + q("q1") + "\"],\"depth\":3}"),
                ctx)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> relationService.graph(JSON.readTree("{\"refs\":[\"" + q("q1") + "\"],\"limit\":2001}"),
                ctx)).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("asOf filters by validity and by what was known then")
    void asOfValidity() {
        nodes("q1", "q2", "q5");
        Instant t = clock.instant();
        edge("q1", "q5", ObjectTypes.SAME_AS, "likely", t.minus(Duration.ofDays(2)), t.minus(Duration.ofDays(1)),
                t.minus(Duration.ofDays(3)));
        edge("q1", "q2", ObjectTypes.MEMBER_OF, "confirmed", t.minus(Duration.ofDays(4)), null, t);

        JsonNode now = JSON.valueToTree(relationService.graph(List.of(ObjectRef.parse(q("q1"))), 1, Set.of(), 0, 500,
                context(Scope.READ, false, null)));
        assertThat(now.get("edges").findValuesAsString("toRef")).containsExactly(q("q2"));

        // Nodes created now do not exist at a past asOf, so the validity filter is checked on the query itself.
        List<RelationEntity> then = relationQuery.touching(Set.of(q("q1")), Set.of(), "both",
                t.minus(Duration.ofHours(36)), true, 0, false, 100);
        assertThat(then).extracting(RelationEntity::getToRef).containsExactly(q("q5"));
        List<RelationEntity> thenUnknown = relationQuery.touching(Set.of(q("q1")), Set.of(), "both",
                t.minus(Duration.ofHours(36)), false, 0, false, 100);
        assertThat(thenUnknown).extracting(RelationEntity::getToRef).containsExactlyInAnyOrder(q("q5"), q("q2"));
        assertThat(relationQuery.touching(Set.of(q("q2")), Set.of(), "out", t, false, 0, false, 100)).isEmpty();
        assertThat(relationQuery.touching(Set.of(q("q2")), Set.of(), "in", t, false, 0, false, 100)).hasSize(1);
    }

    @Test
    @DisplayName("common dependencies follow depends-on paths and report the weakest edge label")
    void commonDependencies() {
        nodes("qa", "qb", "qx", "qz");
        Instant t = clock.instant();
        edge("qa", "qx", ObjectTypes.DEPENDS_ON, "likely", t.minus(Duration.ofDays(1)), null, t);
        edge("qx", "qz", ObjectTypes.DEPENDS_ON, "possible", t.minus(Duration.ofDays(1)), null, t);
        edge("qb", "qz", ObjectTypes.DEPENDS_ON, "confirmed", t.minus(Duration.ofDays(1)), null, t);
        JsonNode out = JSON.valueToTree(relationService.commonDependencies(List.of(ObjectRef.parse(q("qa")),
                ObjectRef.parse(q("qb"))), context(Scope.READ, false, null)));
        JsonNode deps = out.get("dependencies");
        assertThat(deps).hasSize(1);
        JsonNode shared = deps.get(0);
        assertThat(shared.get("node").get("ref").asString()).isEqualTo(q("qz"));
        assertThat(shared.get("sharedBy")).hasSize(2);
        assertThat(shared.get("weakestEdgeLabel").asString()).isEqualTo("possible");
        assertThat(shared.get("paths")).hasSize(2);
        for (JsonNode path : shared.get("paths")) {
            if (path.get("from").asString().equals(q("qa"))) {
                assertThat(path.get("hops")).hasSize(2);
                assertThat(path.get("weakestEdgeLabel").asString()).isEqualTo("possible");
            } else {
                assertThat(path.get("hops")).hasSize(1);
                assertThat(path.get("weakestEdgeLabel").asString()).isEqualTo("confirmed");
            }
        }
        assertThatThrownBy(() -> relationService.commonDependencies(JSON.readTree("{\"refs\":[\"" + q("qa") + "\"]}"),
                context(Scope.READ, false, null))).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("ownership chains walk owns/controls edges and surface the shared owner path")
    void ownershipChain() {
        // parentA owns holdco owns target; parentB controls target directly. Target is owned via two chains.
        nodes("parentA", "parentB", "holdco", "target");
        Instant t = clock.instant();
        edge("parentA", "holdco", ObjectTypes.OWNS, "confirmed", t.minus(Duration.ofDays(1)), null, t);
        edge("holdco", "target", ObjectTypes.OWNS, "likely", t.minus(Duration.ofDays(1)), null, t);
        edge("parentB", "target", ObjectTypes.CONTROLS, "confirmed", t.minus(Duration.ofDays(1)), null, t);
        ResolveContext ctx = context(Scope.READ, false, null);

        // Walking out from the two parents: target is reached by both — a shared attribute.
        JsonNode out = JSON.valueToTree(relationService.ownershipChain(
                List.of(ObjectRef.parse(q("parentA")), ObjectRef.parse(q("parentB"))), 6, "out", Set.of(), 0, ctx));
        assertThat(out.get("direction").asString()).isEqualTo("out");
        JsonNode shared = out.get("shared");
        assertThat(shared).hasSize(1);
        assertThat(shared.get(0).get("node").get("ref").asString()).isEqualTo(q("target"));
        assertThat(shared.get(0).get("reachedFrom")).hasSize(2);
        assertThat(shared.get(0).get("weakestEdgeLabel").asString()).isEqualTo("likely");
        assertThat(out.get("chains").findValuesAsString("ref")).contains(q("holdco"), q("target"));
        for (JsonNode path : shared.get(0).get("paths")) {
            if (path.get("from").asString().equals(q("parentA"))) {
                assertThat(path.get("hops")).hasSize(2); // holdco, target
                assertThat(path.get("relationTypes")).hasSize(2);
                assertThat(path.get("relationTypes").get(0).asString()).isEqualTo(ObjectTypes.OWNS);
                assertThat(path.get("weakestEdgeLabel").asString()).isEqualTo("likely");
            } else {
                assertThat(path.get("hops")).hasSize(1); // target directly
                assertThat(path.get("weakestEdgeLabel").asString()).isEqualTo("confirmed");
            }
        }

        // Walking in from the target recovers the whole ownership chain above it.
        JsonNode in = JSON.valueToTree(relationService.ownershipChain(List.of(ObjectRef.parse(q("target"))), 6, "in",
                Set.of(), 0, ctx));
        assertThat(in.get("chains").findValuesAsString("ref"))
                .containsExactlyInAnyOrder(q("holdco"), q("parentA"), q("parentB"));

        // A type filter narrows the walked edges to one kind.
        JsonNode controlled = JSON.valueToTree(relationService.ownershipChain(List.of(ObjectRef.parse(q("target"))), 6,
                "in", Set.of(ObjectTypes.CONTROLS), 0, ctx));
        assertThat(controlled.get("chains").findValuesAsString("ref")).containsExactly(q("parentB"));

        // A non-ownership relation type is rejected.
        assertThatThrownBy(() -> relationService.ownershipChain(
                JSON.readTree("{\"refs\":[\"" + q("target") + "\"],\"types\":[\"depends-on\"]}"), ctx))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("private relations are left out for callers without private access")
    void privateRelationsHidden() {
        nodes("q1", "q2");
        Instant t = clock.instant();
        RelationEntity r = edge("q1", "q2", ObjectTypes.RELATED_TO, "likely", t.minus(Duration.ofDays(1)), null, t);
        r.setPrivateRelation(true);
        relationRows.save(r);
        assertThat(JSON.valueToTree(relationService.graph(List.of(ObjectRef.parse(q("q1"))), 1, Set.of(), 0, 500,
                context(Scope.READ, false, null))).get("edges")).isEmpty();
        assertThat(JSON.valueToTree(relationService.graph(List.of(ObjectRef.parse(q("q1"))), 1, Set.of(), 0, 500,
                context(Scope.AGENT, true, null))).get("edges")).hasSize(1);
    }
}
