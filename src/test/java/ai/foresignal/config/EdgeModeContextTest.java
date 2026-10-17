package ai.foresignal.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.api.AskController;
import ai.foresignal.api.BriefingsController;
import ai.foresignal.api.BulkExportsController;
import ai.foresignal.api.ExportsController;
import ai.foresignal.api.FederationController;
import ai.foresignal.api.FederationReceiveController;
import ai.foresignal.api.OgcController;
import ai.foresignal.api.OgcFeaturesController;
import ai.foresignal.api.PeersController;
import ai.foresignal.api.ProductsController;
import ai.foresignal.api.RasterTilesController;
import ai.foresignal.api.StaticMapController;
import ai.foresignal.api.WmtsController;
import ai.foresignal.briefing.BriefingScheduler;
import ai.foresignal.federation.FederationStreamService;
import ai.foresignal.federation.PeerService;
import ai.foresignal.ingest.IngestWriter;
import ai.foresignal.search.EmbeddingWorker;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.system.SystemInfoService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.trigger.TriggerEngine;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Edge mode (prompt 113): with {@code foresignal.mode=edge} the embedding worker, the LLM-backed controllers,
 * briefings, raster products and rendering, OGC, exports and the federation feed are absent at bean creation, while
 * the writer, triggers, peers and the edge-relay endpoints stay; {@code /v1/system} reports {@code mode: edge} and
 * those features off; the edge retention defaults (6 h track window, 1 d default) apply when nothing is stored.
 * This class has its own context.
 */
@IntegrationTest
@TestPropertySource(properties = "foresignal.mode=edge")
class EdgeModeContextTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired ApplicationContext context;
    @Autowired SystemInfoService system;
    @Autowired MockMvc mvc;
    @Autowired AppSettingService settings;
    @Autowired EdgeModeDefaults defaults;

    @Test
    @DisplayName("LLM, embedding, raster, briefing, OGC, export and federation-serving beans are absent")
    void heavyBeansAbsent() {
        for (Class<?> type : List.of(EmbeddingWorker.class, AskController.class, BriefingsController.class,
                BriefingScheduler.class, RasterTilesController.class, ProductsController.class, WmtsController.class,
                StaticMapController.class, OgcController.class, OgcFeaturesController.class, ExportsController.class,
                BulkExportsController.class, FederationController.class, FederationStreamService.class)) {
            assertThat(context.getBeanNamesForType(type)).as(type.getSimpleName()).isEmpty();
        }
        for (Class<?> type : List.of(IngestWriter.class, TriggerEngine.class, PeerService.class, PeersController.class,
                FederationReceiveController.class)) {
            assertThat(context.getBeanNamesForType(type)).as(type.getSimpleName()).isNotEmpty();
        }
    }

    @Test
    @DisplayName("features report the edge mode and its disabled capabilities")
    void features() throws Exception {
        assertThat(system.mode()).isEqualTo(EdgeMode.EDGE);
        Map<String, Boolean> features = system.features();
        for (String key : List.of("embeddings", "llm", "rasters", "briefings", "ogc", "exports", "federationServing")) {
            assertThat(features).as(key).containsEntry(key, false);
        }
        assertThat(features).containsEntry("federation", true);
        JsonNode info = JSON.readTree(mvc.perform(get("/v1/system")).andExpect(status().isOk()).andReturn()
                .getResponse().getContentAsString());
        assertThat(info.get("mode").asString()).isEqualTo("edge");
        assertThat(info.get("features").get("exports").asBoolean()).isFalse();
    }

    @Test
    @DisplayName("the edge retention defaults apply when no retention settings are stored, and never overwrite them")
    void retentionDefaults() {
        assertThat(defaults.apply()).isTrue();
        RetentionSettings stored = settings.get(SettingKeys.RETENTION, RetentionSettings.class).orElseThrow();
        assertThat(stored.trackWindow()).isEqualTo(Duration.ofHours(6));
        assertThat(stored.defaultRetention()).isEqualTo(Duration.ofDays(1));
        assertThat(defaults.apply()).as("an existing value is kept").isFalse();
    }
}
