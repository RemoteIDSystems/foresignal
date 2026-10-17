package ai.foresignal.object;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.Scope;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link ObjectActions}: actions by token scope and object state (prompt 129). */
class ObjectActionsTest {

    static ObjectCaller caller(Scope scope) {
        return new ObjectCaller(scope == null ? null : "p-" + scope, "token:t", scope, scope == Scope.ADMIN, Set.of());
    }

    static ObjectEnvelope envelope(String ref, String status) {
        return ObjectEnvelope.builder(ObjectRef.parse(ref)).title("t").status(status).build();
    }

    static List<String> ids(List<ObjectAction> actions) {
        return actions.stream().map(ObjectAction::id).toList();
    }

    @Test
    @DisplayName("anonymous callers get no actions")
    void anonymous() {
        assertThat(ObjectActions.of(envelope("finding:01JA8Q3V7S2K9W4T6Y1B5N0M3C", "open"), caller(null))).isEmpty();
    }

    @Test
    @DisplayName("read tokens may annotate, add to a workspace and propose a watch, but not judge or track")
    void readToken() {
        assertThat(ids(ObjectActions.of(envelope("record:src/a", "current"), caller(Scope.READ))))
                .containsExactly("add-to-workspace", "annotate", "propose-watch");
        assertThat(ids(ObjectActions.of(envelope("finding:01JA8Q3V7S2K9W4T6Y1B5N0M3C", "open"), caller(Scope.READ))))
                .containsExactly("add-to-workspace", "annotate");
    }

    @Test
    @DisplayName("agent tokens add verdicts, tracking and investigations")
    void agentToken() {
        assertThat(ids(ObjectActions.of(envelope("record:src/a", "current"), caller(Scope.AGENT))))
                .containsExactly("add-to-workspace", "annotate", "verdict", "track", "open-investigation");
        assertThat(ids(ObjectActions.of(envelope("entity:01JA8Q3V7S2K9W4T6Y1B5N0M3C", "active"), caller(Scope.AGENT))))
                .contains("track").doesNotContain("propose-watch", "open-investigation");
    }

    @Test
    @DisplayName("object state decides: a firing alert can be acknowledged, an acknowledged one cannot")
    void stateDependent() {
        assertThat(ids(ObjectActions.of(envelope("alert:42", "firing"), caller(Scope.AGENT)))).contains("acknowledge");
        assertThat(ids(ObjectActions.of(envelope("alert:42", "acknowledged"), caller(Scope.AGENT))))
                .doesNotContain("acknowledge");
        assertThat(ids(ObjectActions.of(envelope("finding:01JA8Q3V7S2K9W4T6Y1B5N0M3C", "dismissed"),
                caller(Scope.AGENT)))).doesNotContain("open-investigation");
        ObjectAction ack = ObjectActions.of(envelope("alert:42", "firing"), caller(Scope.AGENT)).stream()
                .filter(a -> a.id().equals("acknowledge")).findFirst().orElseThrow();
        assertThat(ack.href()).isEqualTo("/v1/alerts/42/ack");
        assertThat(ack.scope()).isEqualTo("agent");
    }

    @Test
    @DisplayName("admin tokens may also delete workspaces and annotations; containers are not annotated")
    void adminToken() {
        assertThat(ids(ObjectActions.of(envelope("workspace:01JA8Q3V7S2K9W4T6Y1B5N0M3C", null), caller(Scope.ADMIN))))
                .containsExactly("delete");
        assertThat(ids(ObjectActions.of(envelope("workspace:01JA8Q3V7S2K9W4T6Y1B5N0M3C", null), caller(Scope.READ))))
                .isEmpty();
        assertThat(ids(ObjectActions.of(envelope("situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C", "active"),
                caller(Scope.ADMIN)))).containsExactly("add-to-workspace", "annotate", "verdict",
                        "open-investigation");
    }
}
