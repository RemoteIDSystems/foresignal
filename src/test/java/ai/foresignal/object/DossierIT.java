package ai.foresignal.object;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import ai.foresignal.baserate.analog.AnalogService;
import ai.foresignal.forecast.ForecastEntity;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastRepository;
import ai.foresignal.security.Scope;
import ai.foresignal.security.Ulids;
import ai.foresignal.situation.SituationEntity;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.JsonNode;

/** {@link DossierService}: the composed read of a selected object (prompt 129). */
@IntegrationTest
@Import(AskTestSources.class)
class DossierIT extends ObjectItSupport {

    @Autowired DossierService dossiers;
    @Autowired ForecastRepository forecastRows;
    @MockitoBean AnalogService analogs;

    static final String STORM = "al092026";

    /** The NHC storm, its cone and its track (067, 075). */
    private void storm() {
        records.ingest("nhc-cyclones", NormalizedRecord.builder(STORM, "cyclone").observedAt(clock.instant())
                .put("stormName", "Ivy").put("classification", "HU").point(-75.0, 25.0).build());
        records.ingest("nhc-cyclone-cones", NormalizedRecord.builder(STORM, "cyclone-cone").observedAt(clock.instant())
                .validUntil(clock.instant().plus(Duration.ofDays(5))).put("stormId", STORM).put("stormName", "Ivy")
                .put("advisoryNumber", "12").point(-74.0, 26.0).build());
        records.ingest("nhc-cyclone-cones", NormalizedRecord.builder(STORM + ":track", "cyclone-track")
                .observedAt(clock.instant()).put("stormId", STORM).put("stormName", "Ivy").put("advisoryNumber", "12")
                .point(-74.5, 25.5).build());
    }

    @Test
    @DisplayName("a storm record: official cone and track labeled official, a ledger question with issuer and cutoff")
    void stormDossier() {
        storm();
        ForecastQuestionEntity q = question("Ivy makes landfall in Florida", null, null);
        ObjectCaller admin = objectCaller("p-admin", Scope.ADMIN, true);
        relationService.create(JSON.readTree("{\"fromRef\":\"question:" + q.getId() + "\",\"toRef\":\"record:"
                + "nhc-cyclones/" + STORM + "\",\"type\":\"about\"}"), new ResolveContext(admin, null));
        ForecastEntity ensemble = new ForecastEntity(Ulids.next(clock.instant()), q.getId(), "ensemble", "ensemble",
                clock.instant(), 0);
        ensemble.setProbability(0.35);
        ensemble.setKnowledgeCutoff(LocalDate.parse("2026-10-12"));
        forecastRows.save(ensemble);
        ForecastEntity llm = new ForecastEntity(Ulids.next(clock.instant()), q.getId(), "llm:weekly:chat", "llm",
                clock.instant(), 0);
        llm.setProbability(0.4);
        llm.setKnowledgeCutoff(LocalDate.parse("2025-06-01"));
        forecastRows.save(llm);

        Map<String, Object> d = dossiers.dossier(ObjectRef.parse("record:nhc-cyclones/" + STORM),
                DossierService.SECTIONS, true, new ResolveContext(admin, null));
        JsonNode tree = JSON.valueToTree(d);
        JsonNode sections = tree.get("sections");
        assertThat(sections.propertyNames()).containsExactlyElementsOf(DossierService.SECTIONS);
        for (String s : DossierService.SECTIONS) {
            assertThat(sections.get(s).get("status").asString()).as(s).isIn("ok", "empty");
        }
        assertThat(sections.get("overview").get("status").asString()).isEqualTo("ok");
        assertThat(sections.get("overview").get("fields").get("kind").asString()).isEqualTo("cyclone");
        JsonNode official = sections.get("forecast").get("official");
        assertThat(official).hasSize(2);
        for (JsonNode o : official) {
            assertThat(o.get("issuer").asString()).isEqualTo("official");
        }
        assertThat(official.findValuesAsString("kind")).containsExactlyInAnyOrder("cone", "track");
        JsonNode brief = sections.get("forecast").get("questions").get(0);
        assertThat(brief.get("issuer").asString()).isEqualTo("foresignal-ensemble");
        assertThat(brief.get("probability").asDouble()).isEqualTo(0.35);
        assertThat(brief.get("estimativeTerm").asString()).isNotBlank();
        assertThat(brief.get("forecasters").findValuesAsString("knowledgeCutoff")).contains("2026-10-12",
                "2025-06-01");
        assertThat(brief.get("forecasters").findValuesAsString("forecasterId")).contains("foresignal-ensemble",
                "llm:weekly:chat");
        assertThat(sections.get("timeline").get("items").get(0).get("kind").asString()).isEqualTo("version");
        assertThat(sections.get("timeline").get("facets").get("bySource").has("nhc-cyclones")).isTrue();
        assertThat(sections.get("imagery").get("status").asString()).isEqualTo("empty");
        assertThat(JSON.writeValueAsString(d).getBytes(StandardCharsets.UTF_8).length)
                .isLessThanOrEqualTo(DossierService.MAX_BYTES);
        assertThat(dossiers.etag(d)).isEqualTo(dossiers.etag(new LinkedHashMap<>(d)));
    }

    @Test
    @DisplayName("a section that fails renders as unavailable while the others still render")
    void partialRendering() {
        SituationEntity s = situation("Red Sea shipping disruption", "warning");
        finding("Loitering cluster", "cusum", 43.2, 14.9, false, s.getId());
        when(analogs.forSituation(anyString(), anyInt(), isNull(), anyBoolean()))
                .thenThrow(new IllegalStateException("signature store offline"));
        Map<String, Object> d = dossiers.dossier(ObjectRef.parse("situation:" + s.getId()),
                List.of("overview", "related", "analysis"), false, context(Scope.READ, false, null));
        JsonNode sections = JSON.valueToTree(d).get("sections");
        assertThat(sections.get("related").get("status").asString()).isEqualTo("unavailable");
        assertThat(sections.get("related").get("message").asString()).contains("could not be computed");
        assertThat(sections.get("overview").get("status").asString()).isEqualTo("ok");
        assertThat(sections.get("analysis").get("status").asString()).isEqualTo("ok");
        assertThat(sections.get("analysis").get("findings")).hasSize(1);
        assertThat(sections.has("imagery")).isFalse();
    }

    @Test
    @DisplayName("the dossier stays within 64 KB: the longest lists are trimmed first and marked truncated")
    void sizeCap() {
        List<Object> big = new ArrayList<>();
        for (int i = 0; i < 2_000; i++) {
            big.add(Map.of("at", "2026-10-12T00:00:00Z", "kind", "version", "title", "Version " + i + " "
                    + "x".repeat(40)));
        }
        Map<String, Object> timeline = new LinkedHashMap<>(DossierService.status("ok", null));
        timeline.put("items", big);
        Map<String, Object> sections = new LinkedHashMap<>();
        sections.put("timeline", timeline);
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("ref", "record:x/y");
        d.put("sections", sections);
        d.put("truncated", false);
        dossiers.cap(d);
        assertThat(JSON.writeValueAsString(d).getBytes(StandardCharsets.UTF_8).length)
                .isLessThanOrEqualTo(DossierService.MAX_BYTES);
        assertThat(d.get("truncated")).isEqualTo(true);
        assertThat(timeline.get("truncated")).isEqualTo(true);
        assertThat((List<?>) timeline.get("items")).isNotEmpty().hasSizeLessThan(2_000);
    }

    @Test
    @DisplayName("include validates section names")
    void includeValidated() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> DossierService.includes(List.of("gossip")))
                .isInstanceOf(ai.foresignal.api.error.ValidationException.class);
        assertThat(DossierService.includes(List.of())).isEqualTo(DossierService.SECTIONS);
    }
}
