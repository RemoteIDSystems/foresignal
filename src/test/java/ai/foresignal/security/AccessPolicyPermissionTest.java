package ai.foresignal.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

/** {@link AccessPolicy#permissionFor} and the {@link AccessPermissions} table it reads (prompt R2-004). */
class AccessPolicyPermissionTest {

    private final AccessPolicy policy = new AccessPolicy();

    @Test
    @DisplayName("identity, audit and data operations resolve to their permission")
    void resolves() {
        assertThat(policy.permissionFor(HttpMethod.GET, "/v1/users")).contains("users.manage");
        assertThat(policy.permissionFor(HttpMethod.POST, "/v1/users")).contains("users.manage");
        assertThat(policy.permissionFor(HttpMethod.GET, "/v1/audit")).contains("audit.read");
        assertThat(policy.permissionFor(HttpMethod.GET, "/v1/records")).contains("data.read");
        assertThat(policy.permissionFor(HttpMethod.GET, "/v1/sources/collection")).contains("collection.manage");
    }

    @Test
    @DisplayName("a literal segment wins over a template at the same depth, including a row with no permission")
    void literalBeatsTemplate() {
        assertThat(policy.permissionFor(HttpMethod.GET, "/v1/precursors/abc")).contains("intel.read");
        assertThat(policy.permissionFor(HttpMethod.GET, "/v1/precursors/batches")).isEmpty();
    }

    @Test
    @DisplayName("HEAD takes the GET row; a trailing slash and a query string are ignored")
    void normalizes() {
        assertThat(policy.permissionFor(HttpMethod.HEAD, "/v1/records")).contains("data.read");
        assertThat(policy.permissionFor(HttpMethod.GET, "/v1/records/?limit=5")).contains("data.read");
    }

    @Test
    @DisplayName("an operation without a permission, and an unknown path, resolve to none")
    void none() {
        assertThat(policy.permissionFor(HttpMethod.GET, "/v1/me")).isEmpty();
        assertThat(policy.permissionFor(HttpMethod.GET, "/v1/no-such-thing")).isEmpty();
        assertThat(policy.permissionFor(HttpMethod.TRACE, "/v1/records")).isEmpty();
    }

    @Test
    @DisplayName("the permission lookup leaves the scope requirement unchanged")
    void scopeUnchanged() {
        assertThat(policy.requirementFor(HttpMethod.GET, "/v1/audit")).isEqualTo(AccessRequirement.ADMIN);
        assertThat(policy.requirementFor(HttpMethod.GET, "/v1/sources/collection")).isEqualTo(AccessRequirement.ADMIN);
        assertThat(policy.requirementFor(HttpMethod.GET, "/v1/me/sources"))
                .isEqualTo(AccessRequirement.READ_ALWAYS_AUTHENTICATED);
        assertThat(policy.requirementFor(HttpMethod.GET, "/v1/system/first-run")).isEqualTo(AccessRequirement.OPEN);
    }
}
