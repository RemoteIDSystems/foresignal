package ai.foresignal.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;

/**
 * The prompt 045 raster routes are {@code read} (covered by 042's {@code GET /v1/products/**} and
 * {@code /v1/styles/**} rules), never always-authenticated, and not open to writes.
 */
class RasterAccessPolicyTest {

    private final AccessPolicy policy = new AccessPolicy();

    @ParameterizedTest(name = "READ: GET {0}")
    @ValueSource(strings = {
        "/v1/products/mrms-reflectivity/tiles/20261010T143240Z~1a2b3c4d/7/29/52.png",
        "/v1/products/mrms-reflectivity/tiles/latest/7/29/52@2x.png",
        "/v1/products/mrms-reflectivity/tilejson",
        "/v1/products/mrms-reflectivity/image/latest.png",
        "/v1/products/mrms-reflectivity/image/20261010T143240Z~1a2b3c4d.jpg",
        "/v1/products/loop",
        "/v1/styles",
        "/v1/styles/nws-reflectivity@1",
        "/v1/products/mrms-reflectivity/legend",
        "/v1/products/mrms-reflectivity/legend.png"
    })
    void readRoutes(String path) {
        assertThat(policy.requirementFor(HttpMethod.GET, path))
                .isInstanceOfSatisfying(AccessRequirement.RequiresScope.class, r -> {
                    assertThat(r.scope()).isEqualTo(Scope.READ);
                    assertThat(r.alwaysAuthenticated()).isFalse();
                });
    }

    @ParameterizedTest(name = "not READ: POST {0}")
    @ValueSource(strings = {"/v1/styles", "/v1/products/mrms-reflectivity/tilejson"})
    void writesAreNotRead(String path) {
        AccessRequirement requirement = policy.requirementFor(HttpMethod.POST, path);
        assertThat(requirement).isInstanceOfSatisfying(AccessRequirement.RequiresScope.class,
                r -> assertThat(r.scope()).isNotEqualTo(Scope.READ));
    }
}
