package ai.foresignal.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import tools.jackson.databind.json.JsonMapper;

/** The runtime index of {@code docs/OpenAPI.json} (prompt 023). */
class SpecOperationIndexTest {

    private final SpecOperationIndex index = new SpecOperationIndex(JsonMapper.builder().build());

    @Test
    @DisplayName("requests resolve to their operationId, preferring literal templates")
    void operationIds() {
        assertThat(index.operations()).isNotEmpty();
        assertThat(index.operationId(HttpMethod.POST, "/v1/triggers")).isEqualTo("createTrigger");
        assertThat(index.operationId(HttpMethod.GET, "/v1/sources/health")).isEqualTo("listSourceHealth");
        assertThat(index.operationId(HttpMethod.GET, "/v1/sources/usgs-earthquakes")).isEqualTo("getSource");
        assertThat(index.operationId(HttpMethod.PUT, "/v1/tokens/current/device")).isEqualTo("updateCurrentTokenDevice");
        assertThat(index.operationId(HttpMethod.DELETE, "/v1/tokens/9f1e")).isEqualTo("revokeToken");
        assertThat(index.operationId(HttpMethod.GET, "/v1/nothing-here")).isNull();
    }

    @Test
    @DisplayName("endpoint patterns match operations the scope can reach")
    void reachability() {
        assertThat(index.matchesReachable("GET", "/v1/records/**", Scope.READ)).isTrue();
        assertThat(index.matchesReachable("*", "/v1/tiles/**", Scope.READ)).isTrue();
        assertThat(index.matchesReachable("POST", "/v1/triggers", Scope.READ)).isFalse();
        assertThat(index.matchesReachable("POST", "/v1/triggers", Scope.AGENT)).isTrue();
        assertThat(index.matchesReachable("GET", "/v1/tokens", Scope.AGENT)).isFalse();
        assertThat(index.matchesReachable("GET", "/v1/tokens", Scope.ADMIN)).isTrue();
        assertThat(index.matchesReachable("GET", "/v1/no-such/**", Scope.ADMIN)).isFalse();
    }
}
