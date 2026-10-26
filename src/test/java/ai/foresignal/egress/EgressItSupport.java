package ai.foresignal.egress;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.test.WireMockSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The shared fixture of prompt R2-018's egress tests: an admin token, the egress beans, a stub server for the
 * upstream (and the exit-IP service) from {@link WireMockSupport}, and an empty pool with
 * rotation {@code direct} before and after every test, so no route or policy leaks into another class sharing the
 * context.
 */
abstract class EgressItSupport extends WireMockSupport {

    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired AdminTokenService adminTokens;
    @Autowired EgressRouteService routes;
    @Autowired EgressRouteStore store;
    @Autowired EgressSettingsService egressSettings;
    @Autowired EgressRouter router;
    @Autowired EgressBackoff egressBackoff;
    @Autowired EgressHealthRegistry health;
    @Autowired JdbcTemplate jdbc;

    /** {@code Bearer …} of an admin token. */
    String admin;

    /** An admin token, no route, rotation {@code direct}. */
    @BeforeEach
    void egressFixture() {
        admin = "Bearer " + adminTokens.rotate();
        clearPool();
    }

    /** Leaves the pool empty and rotation {@code direct} for the next class. */
    @AfterEach
    void clearEgress() {
        clearPool();
    }

    private void clearPool() {
        for (EgressRoute route : store.all()) {
            routes.delete(route.id());
        }
        jdbc.update("UPDATE source_settings SET egress_route_id = NULL, egress_excluded = false");
        health.clear();
        egressSettings.update(json("{'rotation':'direct'}"));
        router.invalidate();
    }

    /** Parses {@code text}, single quotes standing for double. */
    static JsonNode json(String text) {
        return JSON.readTree(text.replace('\'', '"'));
    }

    /** Creates an {@code http} route with no credentials to {@code 127.0.0.1:port}. */
    EgressRoute httpRoute(String name, int port) {
        return routes.create(json("{'name':'" + name + "','type':'http','url':'http://127.0.0.1:" + port + "'}"));
    }

    /** Sets the rotation policy, with no default route. */
    void rotation(String rotation) {
        egressSettings.update(json("{'rotation':'" + rotation + "'}"));
    }
}
