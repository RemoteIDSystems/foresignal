package ai.foresignal.analyst;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.api.AnalystController;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import tools.jackson.databind.json.JsonMapper;

/** The analyst controllers (prompt 102): profiles CRUD, notes, proposals and evaluation. */
@WebIntegrationTest
@Import(TestIngestSources.class)
class AnalystControllersIT {

    @Autowired AnalystController controller;
    @Autowired AnalystService service;
    @Autowired JsonMapper json;

    @Test
    @DisplayName("a profile is created, listed and read back")
    void profiles() {
        var created = controller.createAnalystProfile(
                json.readTree("{\"name\":\"Watch Texas\",\"enabled\":false,\"schedule\":{\"every\":\"30m\"}}"),
                new MockHttpServletRequest());
        assertThat(created.getStatusCode().is2xxSuccessful()).isTrue();
        String id = String.valueOf(created.getBody().get("id"));
        assertThat(controller.getAnalystProfile(id)).containsEntry("name", "Watch Texas");
        assertThat(controller.listAnalystProfiles()).containsKey("items");
    }

    @Test
    @DisplayName("the evaluation endpoint returns per-profile aggregates")
    void evaluation() {
        @SuppressWarnings("unchecked")
        Map<String, Object> out = controller.getAnalystEvaluation(null, null);
        assertThat(out).containsKeys("since", "profiles");
    }

    @Test
    @DisplayName("notes and proposals lists are pages")
    void lists() {
        assertThat(controller.listAnalystNotes(null, null, null, null, "10", new MockHttpServletRequest())).containsKey("items");
        assertThat(controller.listAnalystProposals(null, "10", new MockHttpServletRequest())).containsKey("items");
        assertThat(controller.listAnalystRuns(null, null, "10")).containsKey("items");
    }
}
