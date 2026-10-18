package ai.foresignal.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.AccessPolicy;
import ai.foresignal.security.AccessRequirement;
import ai.foresignal.security.Scope;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpMethod;

/**
 * The plugin rows of {@link AccessPolicy} (prompt 141): the four operations are {@code admin}, as their
 * {@code x-foresignal-scope} says, through explicit rules rather than the fail-closed default.
 */
class PluginAccessPolicyTest {

    private final AccessPolicy policy = new AccessPolicy();

    @ParameterizedTest(name = "{0} {1} is admin")
    @CsvSource({
        "GET, /v1/plugins",
        "PUT, /v1/plugins",
        "GET, /v1/plugins/geojson-passthrough",
        "PUT, /v1/plugins/geojson-passthrough",
        "DELETE, /v1/plugins/geojson-passthrough"
    })
    void adminRules(String method, String path) {
        AccessRequirement requirement = policy.requirementFor(HttpMethod.valueOf(method), path);

        assertThat(requirement).isInstanceOfSatisfying(AccessRequirement.RequiresScope.class,
                r -> assertThat(r.scope()).isEqualTo(Scope.ADMIN));
    }
}
