package ai.foresignal.infra;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.api.InfraController;
import ai.foresignal.api.InfraRulesController;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

/** The infrastructure controllers and rules settings (prompt 104). */
@WebIntegrationTest
@Import(TestIngestSources.class)
class InfraControllersIT {

    @Autowired InfraController infra;
    @Autowired InfraRulesController rules;
    @Autowired JsonMapper json;

    @Test
    @DisplayName("the status endpoint answers before any build")
    void status() {
        assertThat(infra.getInfraStatus()).isNotNull();
    }

    @Test
    @DisplayName("nodes list is a page")
    void nodes() {
        assertThat(infra.listInfraNodes(null, null, null, null, "10")).containsKey("items");
    }

    @Test
    @DisplayName("rules are read and updated")
    void rulesCrud() {
        assertThat(rules.getInfraRules()).isNotNull();
        rules.updateInfraRules(json.readTree("{\"version\":2}"));
        assertThat(rules.getInfraRules()).containsEntry("version", 2);
    }
}
