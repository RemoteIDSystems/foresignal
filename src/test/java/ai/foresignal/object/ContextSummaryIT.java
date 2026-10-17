package ai.foresignal.object;

import static org.assertj.core.api.Assertions.assertThat;

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
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;

/** {@link ContextSummaryService}: counts, activity, insights and caching for a viewport (prompt 129). */
@IntegrationTest
@Import(AskTestSources.class)
class ContextSummaryIT extends ObjectItSupport {

    @Autowired ForecastRepository forecastRows;

    static final String REQUEST = "{'viewport':{'bbox':[42.0,12.0,46.0,16.0]},"
            + "'timeRange':{'start':'2026-10-12T00:00:00Z','end':null},'layers':[]}";

    private JsonNode request(String json) {
        return JSON.readTree(json.replace('\'', '"'));
    }

    @Test
    @DisplayName("layer counts aggregate current head ⋈ live in the viewport: all and unexpired, never features")
    void layerCounts() {
        quake("in1", "Aden", 4.0, 45.0, 12.8);
        quake("in2", "Hodeidah", 4.2, 42.9, 14.8);
        quake("out", "Cairo", 4.5, 31.2, 30.0);
        records.ingest(AskTestSources.QUAKES, NormalizedRecord.builder("old", AskTestSources.QUAKE_KIND)
                .observedAt(clock.instant().minus(Duration.ofHours(3)))
                .validUntil(clock.instant().minus(Duration.ofHours(1))).put("place", "Mokha").put("mag", 3.0)
                .point(43.2, 13.3).build());
        Map<String, Object> s = summaries.summarize(request(REQUEST), context(Scope.READ, false, null));
        JsonNode counts = JSON.valueToTree(s.get("layerCounts"));
        assertThat(counts).hasSize(1);
        assertThat(counts.get(0).get("layer").asString()).isEqualTo(AskTestSources.QUAKE_KIND);
        assertThat(counts.get(0).get("count").asLong()).isEqualTo(3);
        assertThat(counts.get(0).get("liveCount").asLong()).isEqualTo(2);
        assertThat(JSON.writeValueAsString(s)).doesNotContain("Hodeidah");

        Map<String, Object> byLayer = summaries.summarize(request(REQUEST.replace("'layers':[]",
                "'layers':['" + AskTestSources.QUAKES + "','vessel','observability:seismic']")),
                context(Scope.READ, false, null));
        JsonNode layered = JSON.valueToTree(byLayer.get("layerCounts"));
        assertThat(layered.findValuesAsString("layer")).containsExactly(AskTestSources.QUAKES, "vessel");
        assertThat(layered.get(1).get("count").asLong()).isZero();
    }

    @Test
    @DisplayName("the activity feed merges alerts, findings and situation changes newest first, with signal families")
    void activityFeed() {
        UUID rule = watch("M4+", 4.0);
        quake("in1", "Aden", 4.5, 45.0, 12.8);
        alertOf(rule);
        clock.setInstant(clock.instant().plus(Duration.ofMinutes(5)));
        finding("Loitering cluster", "cusum", 43.2, 14.9, false, null);
        clock.setInstant(clock.instant().plus(Duration.ofMinutes(5)));
        SituationEntity s = situation("Red Sea shipping disruption", "warning");
        jdbc.update("UPDATE situation SET geom = ST_SetSRID(ST_MakePoint(43.0, 14.0), 4326) WHERE id = ?", s.getId());
        finding("Private cluster", "cusum", 43.4, 14.9, true, null);

        Map<String, Object> out = summaries.summarize(request(REQUEST), context(Scope.READ, false, null));
        JsonNode activity = JSON.valueToTree(out.get("activity"));
        List<String> types = activity.findValuesAsString("type");
        assertThat(types).contains("alert", "finding", "situation-change");
        Instant previous = Instant.MAX;
        for (JsonNode item : activity) {
            Instant at = Instant.parse(item.get("at").asString());
            assertThat(at).isBeforeOrEqualTo(previous);
            previous = at;
        }
        assertThat(JSON.writeValueAsString(activity)).doesNotContain("Private cluster");
        JsonNode findingItem = activity.findParents("type").stream().filter(n -> n.get("type").asString()
                .equals("finding")).findFirst().orElseThrow();
        assertThat(findingItem.get("signalFamily").asString()).isEqualTo("ais-behaviour");
    }

    @Test
    @DisplayName("insights carry issuer, ICD 203 term, base rate and knowledge cutoff for ledger forecasts")
    void insights() {
        SituationEntity s = situation("Red Sea shipping disruption", "warning");
        jdbc.update("UPDATE situation SET geom = ST_SetSRID(ST_MakePoint(43.0, 14.0), 4326) WHERE id = ?", s.getId());
        ForecastQuestionEntity q = question("Shipping resumes", null, s.getId());
        ForecastEntity base = new ForecastEntity(Ulids.next(clock.instant()), q.getId(), "base-rate", "base-rate",
                clock.instant(), 0);
        base.setProbability(0.2);
        forecastRows.save(base);
        ForecastEntity ensemble = new ForecastEntity(Ulids.next(clock.instant()), q.getId(), "ensemble", "ensemble",
                clock.instant(), 0);
        ensemble.setProbability(0.7);
        ensemble.setKnowledgeCutoff(LocalDate.parse("2026-10-11"));
        forecastRows.save(ensemble);
        finding("Loitering cluster", "cusum", 43.2, 14.9, false, s.getId());

        JsonNode insights = JSON.valueToTree(summaries.summarize(request(REQUEST), context(Scope.READ, false, null))
                .get("insights"));
        JsonNode forecast = insights.findParents("kind").stream().filter(n -> "forecast".equals(n.get("kind")
                .asString())).findFirst().orElseThrow();
        assertThat(forecast.get("issuer").asString()).isEqualTo("foresignal-ensemble");
        assertThat(forecast.get("probability").asDouble()).isEqualTo(0.7);
        assertThat(forecast.get("estimativeTerm").asString()).isEqualTo("likely");
        assertThat(forecast.get("baseRate").asDouble()).isEqualTo(0.2);
        assertThat(forecast.get("knowledgeCutoff").asString()).isEqualTo("2026-10-11");
        assertThat(insights.findValuesAsString("kind")).contains("finding");
        JsonNode related = JSON.valueToTree(summaries.summarize(request(REQUEST), context(Scope.READ, false, null))
                .get("related"));
        assertThat(related.findValuesAsString("ref")).contains("situation:" + s.getId());
    }

    @Test
    @DisplayName("summaries are cached per normalized context for 30 seconds and stay within 32 KB")
    void caching() {
        quake("in1", "Aden", 4.0, 45.0, 12.8);
        Map<String, Object> first = summaries.summarize(request(REQUEST), context(Scope.READ, false, null));
        assertThat(first.get("cacheAgeSeconds")).isEqualTo(0L);
        quake("in2", "Hodeidah", 4.2, 42.9, 14.8);
        clock.setInstant(clock.instant().plusSeconds(10));
        Map<String, Object> cached = summaries.summarize(request(REQUEST), context(Scope.READ, false, null));
        assertThat(cached.get("cacheAgeSeconds")).isEqualTo(10L);
        assertThat(JSON.valueToTree(cached.get("layerCounts")).get(0).get("count").asLong()).isEqualTo(1);
        Map<String, Object> otherCaller = summaries.summarize(request(REQUEST), context(Scope.AGENT, true, null));
        assertThat(otherCaller.get("cacheAgeSeconds")).isEqualTo(0L);
        clock.setInstant(clock.instant().plusSeconds(25));
        Map<String, Object> fresh = summaries.summarize(request(REQUEST), context(Scope.READ, false, null));
        assertThat(fresh.get("cacheAgeSeconds")).isEqualTo(0L);
        assertThat(JSON.valueToTree(fresh.get("layerCounts")).get(0).get("count").asLong()).isEqualTo(2);
        assertThat(JSON.writeValueAsString(fresh).getBytes(StandardCharsets.UTF_8).length)
                .isLessThanOrEqualTo(ContextSummaryService.MAX_BYTES);
        assertThat(fresh.get("narrative")).isNull();
    }

    @Test
    @DisplayName("a center and radius viewport and the time range are validated")
    void validation() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> summaries.summarize(request(
                "{'viewport':{'center':[43,14]},'timeRange':{'start':'2026-10-12T00:00:00Z'}}"),
                context(Scope.READ, false, null))).isInstanceOf(ai.foresignal.api.error.ValidationException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> summaries.summarize(request(
                "{'viewport':{'bbox':[42,12,46,16]}}"), context(Scope.READ, false, null)))
                .isInstanceOf(ai.foresignal.api.error.ValidationException.class);
        Map<String, Object> ok = summaries.summarize(request("{'viewport':{'center':[43,14],'radiusKm':200},"
                + "'timeRange':{'start':'2026-10-12T00:00:00Z'}}"), context(Scope.READ, false, null));
        assertThat(ok).containsKeys("layerCounts", "activity", "insights", "related", "generatedAt");
    }
}
