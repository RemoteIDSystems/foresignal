package ai.foresignal.api.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The paths {@link TriggerOwnershipInterceptor} checks (prompt R2-008). */
class TriggerOwnershipInterceptorTest {

    static final UUID ID = UUID.fromString("0b6f3c1e-5d1a-4f6e-9a3b-2c4d5e6f7a8b");

    @Test
    @DisplayName("every single-rule path, the KML export included, names its rule")
    void rulePaths() {
        for (String path : new String[] {"/v1/triggers/" + ID, "/v1/triggers/" + ID + "/test",
            "/v1/triggers/" + ID + "/suggestions/s1/apply", "/v1/triggers/" + ID + ".kml",
            "/v1/triggers/" + ID + "/window-state"}) {
            assertThat(TriggerOwnershipInterceptor.ruleId(path)).as(path).contains(ID);
        }
    }

    @Test
    @DisplayName("the collection, analyze-sources and non-UUID ids are left to the controllers")
    void otherPaths() {
        for (String path : new String[] {"/v1/triggers", "/v1/triggers/analyze-sources", "/v1/triggers/9f1e",
            "/v1/triggers/" + ID + "x"}) {
            assertThat(TriggerOwnershipInterceptor.ruleId(path)).as(path).isEmpty();
        }
    }
}
