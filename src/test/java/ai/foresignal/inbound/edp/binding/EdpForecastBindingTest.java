package ai.foresignal.inbound.edp.binding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.forecast.ForecastEntity;
import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastRepository;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecasterEntity;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.Ledger;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;

/**
 * External forecasts (prompt 138, EXT-3, Intelligence-Design §8.6): question links with orientation, {@code questionRef}
 * items, the 0.02-or-daily cadence, {@code made_at} stamped by the ledger (never from the provider, never before
 * receipt), and ensemble admission only from 30 resolved linked questions.
 */
class EdpForecastBindingTest {

    private static final Instant NOW = BindingFixtures.RECEIVED.plusSeconds(30);
    private static final String QUESTION = "01JA8Q3V7S2K9W4T6Y1B5N0M3Q";
    private static final String FORECASTER = "edp:" + BindingFixtures.SOURCE + ":odds";

    // ------------------------------------------------------------------------- policy

    @Test
    @DisplayName("value: binary takes a probability, numeric quantiles; inverse records 1 − p and cannot invert quantiles")
    void values() {
        assertThat(ExternalForecastPolicy.value(Ledger.BINARY, 0.2, null, "same").probability()).isEqualTo(0.2);
        assertThat(ExternalForecastPolicy.value(Ledger.BINARY, 0.2, null, "inverse").probability())
                .isEqualTo(0.8, org.assertj.core.data.Offset.offset(1e-12));
        assertThat(ExternalForecastPolicy.value(Ledger.BINARY, 1.4, null, "same").probability()).isEqualTo(1.0);
        Map<String, Double> q = quantiles(1, 2, 3, 4, 5);
        q.put("p99", 9.0);
        ForecastInputs.ForecastValue numeric = ExternalForecastPolicy.value(Ledger.NUMERIC, null, q, "same");
        assertThat(numeric.quantiles()).containsOnlyKeys("p05", "p25", "p50", "p75", "p95");
        assertThat(ExternalForecastPolicy.value(Ledger.NUMERIC, null, q, "inverse")).isNull();
        assertThat(ExternalForecastPolicy.value(Ledger.NUMERIC, 0.3, null, "same")).isNull();
        assertThat(ExternalForecastPolicy.value(Ledger.MULTIPLE, 0.3, null, "same")).isNull();
        assertThat(ExternalForecastPolicy.unsupported(Ledger.NUMERIC, null, q, "inverse")).contains("inverted");
        assertThat(ExternalForecastPolicy.unsupported(Ledger.NUMERIC, 0.3, null, "same")).contains("a probability");
    }

    @Test
    @DisplayName("shouldRecord: the first forecast, a move of 0.02 or more, or a day later; not a smaller move")
    void cadence() {
        Instant at = NOW;
        ForecastInputs.ForecastValue standing = ForecastInputs.ForecastValue.binary(0.50);
        assertThat(ExternalForecastPolicy.shouldRecord(null, null, standing, at)).isTrue();
        assertThat(ExternalForecastPolicy.shouldRecord(standing, at.minusSeconds(60),
                ForecastInputs.ForecastValue.binary(0.519), at)).isFalse();
        assertThat(ExternalForecastPolicy.shouldRecord(standing, at.minusSeconds(60),
                ForecastInputs.ForecastValue.binary(0.52), at)).isTrue();
        assertThat(ExternalForecastPolicy.shouldRecord(standing, at.minusSeconds(60),
                ForecastInputs.ForecastValue.binary(0.48), at)).isTrue();
        assertThat(ExternalForecastPolicy.shouldRecord(standing, at.minus(Duration.ofHours(24)), standing, at))
                .isTrue();
        assertThat(ExternalForecastPolicy.shouldRecord(standing, at.minus(Duration.ofHours(23)), standing, at))
                .isFalse();
        ForecastInputs.ForecastValue q = ForecastInputs.ForecastValue.quantiles(quantiles(0, 25, 50, 75, 100));
        assertThat(ExternalForecastPolicy.shouldRecord(q, at.minusSeconds(60),
                ForecastInputs.ForecastValue.quantiles(quantiles(0, 25, 51, 75, 100)), at)).isFalse();
        assertThat(ExternalForecastPolicy.shouldRecord(q, at.minusSeconds(60),
                ForecastInputs.ForecastValue.quantiles(quantiles(0, 25, 52, 75, 100)), at)).isTrue();
    }

    // ------------------------------------------------------------------------- target

    /** The mocks of one target. */
    private record Rig(ForecastBindingTarget target, ForecastService ledger, ForecasterRegistry registry,
            ForecastRepository forecasts, ExternalQuestionLinkRepository links, ForecastQuestionEntity question) {}

    @SuppressWarnings("unchecked")
    private static Rig rig(String type, boolean open) {
        ForecastService ledger = mock(ForecastService.class);
        ForecasterRegistry registry = mock(ForecasterRegistry.class);
        ForecastRepository forecasts = mock(ForecastRepository.class);
        ExternalQuestionLinkRepository links = mock(ExternalQuestionLinkRepository.class);
        ObjectProvider<ForecastService> ledgers = mock(ObjectProvider.class);
        when(ledgers.getIfAvailable()).thenReturn(ledger);
        ObjectProvider<ForecasterRegistry> registries = mock(ObjectProvider.class);
        when(registries.getIfAvailable()).thenReturn(registry);
        ForecastQuestionEntity question = mock(ForecastQuestionEntity.class);
        when(question.getId()).thenReturn(QUESTION);
        when(question.getType()).thenReturn(type);
        when(question.getStatus()).thenReturn(open ? "open" : "closed");
        when(question.acceptsForecasts(any())).thenReturn(open);
        when(ledger.find(QUESTION)).thenReturn(Optional.of(question));
        when(registry.ensure(anyString(), anyString(), anyString(), anyString(), anyMap()))
                .thenAnswer(inv -> new ForecasterEntity(inv.getArgument(0), inv.getArgument(1), inv.getArgument(2),
                        inv.getArgument(3), NOW));
        when(forecasts.findTop1ByQuestionIdAndForecasterIdOrderByMadeAtDescIdDesc(any(), any()))
                .thenReturn(Optional.empty());
        when(ledger.record(any(), any(), any(), any(), any(), any(), any(), any(), eq(false)))
                .thenAnswer(inv -> new ForecastEntity("01JA8Q3V7S2K9W4T6Y1B5N0M3F", QUESTION, FORECASTER,
                        "external", NOW, 42L));
        ForecastBindingTarget target = new ForecastBindingTarget(ledgers, registries, forecasts, links,
                Clock.fixed(NOW, ZoneOffset.UTC));
        return new Rig(target, ledger, registry, forecasts, links, question);
    }

    /** A forecast item naming a provider question. */
    private static BoundItem linkedItem(double p) {
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("question", Map.of("externalId", "q-1", "title", "Will the port close?"));
        extra.put("probability", p);
        extra.put("madeAt", "2026-10-18T05:00:00Z");
        Map<String, Object> times = new LinkedHashMap<>();
        times.put("receivedAt", BindingFixtures.RECEIVED.toString());
        times.put("effectiveMadeAt", BindingFixtures.RECEIVED.toString());
        extra.put("edpTimes", times);
        return BindingFixtures.item("odds", "f-1", "forecast", 10, extra);
    }

    @Test
    @DisplayName("an unlinked provider question is skipped; questionRef items need no link")
    void destinations() {
        Rig rig = rig(Ledger.BINARY, true);
        BoundStream stream = BindingFixtures.stream("odds");
        ExternalBindingEntity binding = BindingFixtures.binding("odds", "forecast", Map.of());
        when(rig.links().findBySourceIdAndStreamIdAndExternalQuestionId(BindingFixtures.SOURCE, "odds", "q-1"))
                .thenReturn(Optional.empty());
        BindingEffect unlinked = rig.target().apply(binding, stream, linkedItem(0.3));
        assertThat(unlinked.action()).isEqualTo("skip:question-not-linked");
        verify(rig.ledger(), never()).record(any(), any(), any(), any(), any(), any(), any(), any(), eq(false));

        BoundItem ref = BindingFixtures.item("odds", "f-2", "forecast", 11, Map.of("questionRef", QUESTION,
                "probability", 0.4));
        BindingEffect recorded = rig.target().apply(binding, stream, ref);
        assertThat(recorded.action()).isEqualTo("forecast-recorded");
        assertThat(recorded.targetRef()).isEqualTo("question:" + QUESTION);
        ArgumentCaptor<Map<String, Object>> inputs = ArgumentCaptor.forClass(Map.class);
        verify(rig.ledger()).record(eq(rig.question()), any(), any(), isNull(), any(), any(), inputs.capture(),
                isNull(), eq(false));
        assertThat(((Map<?, ?>) inputs.getValue().get("edp")).get("via")).isEqualTo("questionRef");
        verify(rig.registry()).ensure(eq(FORECASTER), eq("external"), anyString(), eq("1.0.0"), anyMap());
    }

    @Test
    @DisplayName("an inverse link records 1 − p; the provider's times are kept in inputs.edp and made_at is the ledger's")
    void inverseLink() {
        Rig rig = rig(Ledger.BINARY, true);
        ExternalQuestionLinkEntity link = new ExternalQuestionLinkEntity("01JA8Q3V7S2K9W4T6Y1B5N0M3L",
                BindingFixtures.SOURCE, "odds", "q-1", QUESTION, "inverse", "agent", NOW);
        when(rig.links().findBySourceIdAndStreamIdAndExternalQuestionId(BindingFixtures.SOURCE, "odds", "q-1"))
                .thenReturn(Optional.of(link));
        BoundItem item = linkedItem(0.3);
        BindingEffect effect = rig.target().apply(BindingFixtures.binding("odds", "forecast", Map.of()),
                BindingFixtures.stream("odds"), item);
        assertThat(effect.action()).isEqualTo("forecast-recorded");
        ArgumentCaptor<ForecastInputs.ForecastValue> value = ArgumentCaptor.forClass(ForecastInputs.ForecastValue.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> inputs = ArgumentCaptor.forClass(Map.class);
        verify(rig.ledger()).record(eq(rig.question()), any(), value.capture(), isNull(), any(), any(),
                inputs.capture(), isNull(), eq(false));
        assertThat(value.getValue().probability()).isEqualTo(0.7, org.assertj.core.data.Offset.offset(1e-12));
        Map<?, ?> edp = (Map<?, ?>) inputs.getValue().get("edp");
        assertThat(edp.get("via")).isEqualTo("link");
        assertThat(edp.get("orientation")).isEqualTo("inverse");
        assertThat(edp.get("providerMadeAt")).isEqualTo("2026-10-18T05:00:00Z");
        assertThat(edp.get("receivedAt")).isEqualTo(BindingFixtures.RECEIVED.toString());
        assertThat(Instant.parse(String.valueOf(edp.get("effectiveMadeAt")))).isAfterOrEqualTo(item.receivedAt());
        assertThat(link.getLastForecastAt()).isEqualTo(NOW);
        verify(rig.links()).save(link);
    }

    @Test
    @DisplayName("effectiveMadeAt is never before receipt, even when the provider claims an earlier madeAt")
    void effectiveNeverBeforeReceipt() {
        Map<String, Object> times = new LinkedHashMap<>();
        times.put("receivedAt", BindingFixtures.RECEIVED.toString());
        times.put("effectiveMadeAt", BindingFixtures.RECEIVED.minusSeconds(3_600).toString());
        BoundItem item = BindingFixtures.item("odds", "f-3", "forecast", 12, Map.of("edpTimes", times,
                "questionRef", QUESTION, "probability", 0.5));
        assertThat(item.effective("effectiveMadeAt")).isEqualTo(BindingFixtures.RECEIVED);
        Map<String, Object> inputs = ForecastBindingTarget.inputs(item, BindingFixtures.stream("odds"),
                new ForecastBindingTarget.Destination(QUESTION, "same", null));
        assertThat(((Map<?, ?>) inputs.get("edp")).get("effectiveMadeAt"))
                .isEqualTo(BindingFixtures.RECEIVED.toString());
    }

    @Test
    @DisplayName("a closed question is skipped; a small move within a day is not recorded")
    void skips() {
        Rig closed = rig(Ledger.BINARY, false);
        BoundItem ref = BindingFixtures.item("odds", "f-4", "forecast", 13, Map.of("questionRef", QUESTION,
                "probability", 0.4));
        assertThat(closed.target().apply(BindingFixtures.binding("odds", "forecast", Map.of()),
                BindingFixtures.stream("odds"), ref).action()).isEqualTo("skip:question-closed");

        Rig open = rig(Ledger.BINARY, true);
        ForecastEntity previous = new ForecastEntity("01JA8Q3V7S2K9W4T6Y1B5N0M3P", QUESTION, FORECASTER, "external",
                NOW.minusSeconds(600), 40L);
        previous.setProbability(0.41);
        when(open.forecasts().findTop1ByQuestionIdAndForecasterIdOrderByMadeAtDescIdDesc(QUESTION, FORECASTER))
                .thenReturn(Optional.of(previous));
        assertThat(open.target().apply(BindingFixtures.binding("odds", "forecast", Map.of()),
                BindingFixtures.stream("odds"), ref).action()).isEqualTo("skip:no-material-change");
        Rig numeric = rig(Ledger.NUMERIC, true);
        assertThat(numeric.target().apply(BindingFixtures.binding("odds", "forecast", Map.of()),
                BindingFixtures.stream("odds"), ref).failed()).isTrue();
    }

    @Test
    @DisplayName("ensemble admission: edp forecasters are counted by resolved linked questions, from 30")
    void ensembleThreshold() {
        assertThat(BindingVocabulary.ENSEMBLE_MIN_RESOLVED).isEqualTo(30);
        ExternalForecastCounter counter = new ExternalForecastCounter(mock(org.springframework.jdbc.core.JdbcTemplate.class));
        assertThat(counter.handles(FORECASTER)).isTrue();
        assertThat(counter.handles("operator:admin")).isFalse();
        assertThat(counter.handles(null)).isFalse();
        assertThat(BindingVocabulary.forecasterId("ext-markets", "kalshi")).isEqualTo("edp:ext-markets:kalshi");
    }

    /** Five required quantiles. */
    private static Map<String, Double> quantiles(double p05, double p25, double p50, double p75, double p95) {
        Map<String, Double> q = new LinkedHashMap<>();
        q.put("p05", p05);
        q.put("p25", p25);
        q.put("p50", p50);
        q.put("p75", p75);
        q.put("p95", p95);
        return q;
    }

    /** Unused guard keeping the list import meaningful for future cases. */
    static List<String> orientations() {
        return BindingVocabulary.ORIENTATIONS;
    }
}
