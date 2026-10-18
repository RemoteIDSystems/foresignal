package ai.foresignal.inbound.edp.binding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.inbound.edp.EdpManifestService;
import ai.foresignal.inbound.edp.ExternalEventBus;
import ai.foresignal.notify.NotificationService;
import ai.foresignal.trigger.OperatorRegistry;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Binding previews (prompt 138): {@code POST /v1/external/bindings/{bindingId}/preview} evaluates the stream's last
 * items through the target's {@code plan} only; nothing is applied or saved. Also the filter checks and effect views.
 */
class BindingPreviewTest {

    /** A handler that records which of its methods ran. */
    static final class RecordingHandler implements BindingTargetHandler {

        final List<String> planned = new ArrayList<>();
        int applied;

        @Override
        public String target() {
            return BindingVocabulary.SERIES;
        }

        @Override
        public List<FieldError> validate(Map<String, Object> config, BoundStream stream) {
            return List.of();
        }

        @Override
        public BindingEffect plan(ExternalBindingEntity binding, BoundStream stream, BoundItem item) {
            planned.add(item.externalId());
            return item.text("seriesKey") == null ? BindingEffect.error(item, "no seriesKey")
                    : BindingEffect.of(item, "series-point", "series:x");
        }

        @Override
        public BindingEffect apply(ExternalBindingEntity binding, BoundStream stream, BoundItem item) {
            applied++;
            return plan(binding, stream, item);
        }
    }

    /** The mocks of one preview. */
    private record Rig(BindingService service, ExternalBindingRepository bindings, BindingRecords records,
            BindingStreams streams, RecordingHandler handler, ExternalBindingEntity binding) {}

    @SuppressWarnings("unchecked")
    private static Rig rig(List<Object> filter, boolean streamPresent) {
        ExternalBindingRepository bindings = mock(ExternalBindingRepository.class);
        BindingRecords records = mock(BindingRecords.class);
        BindingStreams streams = mock(BindingStreams.class);
        BindingApplier applier = mock(BindingApplier.class);
        RecordingHandler handler = new RecordingHandler();
        when(applier.handler(BindingVocabulary.SERIES)).thenReturn(handler);
        ExternalBindingEntity binding = BindingFixtures.binding("levels", BindingVocabulary.SERIES, Map.of());
        binding.setFilter(filter);
        when(bindings.findById(binding.getId())).thenReturn(Optional.of(binding));
        BoundStream stream = BindingFixtures.stream("levels");
        when(streams.stream(binding)).thenReturn(streamPresent ? Optional.of(stream) : Optional.empty());
        BoundItem withEdp = BindingFixtures.item("levels", "a", "observation", 1,
                Map.of("seriesKey", "gauge-1", "t", "2026-10-17", "value", 3.1));
        BoundItem other = BindingFixtures.item("levels", "b", "observation", 2,
                Map.of("seriesKey", "gauge-2", "t", "2026-10-17", "value", 1.0));
        BoundItem broken = BindingFixtures.item("levels", "c", "observation", 3, Map.of("t", "2026-10-17"));
        BoundItem foreign = new BoundItem(4, BindingFixtures.SOURCE, "levels:d", "river-level", 1, null, null, null,
                null, null, Map.of("seriesKey", "gauge-1"));
        when(records.recent(anyString(), anyString(), anyString(), anyInt(), anyLong()))
                .thenReturn(List.of(withEdp, other, broken, foreign));
        ObjectProvider<NotificationService> notifications = mock(ObjectProvider.class);
        ObjectProvider<ForecastService> ledger = mock(ObjectProvider.class);
        BindingService service = new BindingService(mock(EdpManifestService.class), bindings,
                mock(ExternalQuestionLinkRepository.class), streams, new BindingFilter(OperatorRegistry.builtIn()),
                applier, mock(BindingRouter.class), records, mock(ExternalCoverageRegistry.class),
                mock(ExternalEventBus.class), notifications, ledger, mock(JdbcTemplate.class), BindingFixtures.JSON,
                mock(PlatformTransactionManager.class), Clock.fixed(BindingFixtures.RECEIVED, ZoneOffset.UTC));
        return new Rig(service, bindings, records, streams, handler, binding);
    }

    @Test
    @DisplayName("a preview plans the last items, applies nothing and saves nothing")
    void noSideEffects() {
        Rig rig = rig(null, true);
        Map<String, Object> preview = rig.service().preview(rig.binding().getId());
        assertThat(preview.get("evaluated")).isEqualTo(3);
        assertThat(preview.get("matched")).isEqualTo(3);
        assertThat((List<?>) preview.get("effects")).hasSize(3);
        assertThat((List<?>) preview.get("errors")).hasSize(1);
        assertThat(rig.handler().planned).containsExactly("levels:a", "levels:b", "levels:c");
        assertThat(rig.handler().applied).isZero();
        verify(rig.bindings(), never()).save(any());
        verify(rig.records()).recent(BindingFixtures.SOURCE, "river-level", "levels", BindingVocabulary.PREVIEW_ITEMS,
                Long.MAX_VALUE);
    }

    @Test
    @DisplayName("a filter lowers matched but not evaluated")
    void filter() {
        Rig rig = rig(List.of(Map.of("path", "seriesKey", "op", "==", "value", "gauge-1")), true);
        Map<String, Object> preview = rig.service().preview(rig.binding().getId());
        assertThat(preview.get("evaluated")).isEqualTo(3);
        assertThat(preview.get("matched")).isEqualTo(1);
        assertThat(rig.handler().planned).containsExactly("levels:a");
    }

    @Test
    @DisplayName("a stream gone from the manifest is reported, with nothing evaluated")
    void vanishedStream() {
        Rig rig = rig(null, false);
        Map<String, Object> preview = rig.service().preview(rig.binding().getId());
        assertThat(preview.get("evaluated")).isEqualTo(0);
        assertThat((List<?>) preview.get("errors")).hasSize(1);
        assertThat(rig.handler().planned).isEmpty();
    }

    @Test
    @DisplayName("filter validation: unknown operator, missing path, extra member, too many conditions")
    void filterValidation() {
        BindingFilter filter = new BindingFilter(OperatorRegistry.builtIn());
        assertThat(filter.validate(null, "filter")).isEmpty();
        assertThat(filter.validate(List.of(Map.of("path", "a", "op", "==", "value", 1)), "filter")).isEmpty();
        assertThat(filter.validate(List.of(Map.of("path", "a", "op", "sounds-like", "value", 1)), "filter"))
                .extracting(FieldError::field).containsExactly("filter[0].op");
        assertThat(filter.validate(List.of(Map.of("op", "==", "value", 1)), "filter"))
                .extracting(FieldError::field).containsExactly("filter[0].path");
        assertThat(filter.validate(List.of(Map.of("path", "a", "op", "==", "value", 1, "x", 2)), "filter"))
                .extracting(FieldError::field).contains("filter[0].x");
        List<Object> many = new ArrayList<>();
        for (int i = 0; i <= BindingFilter.MAX_CONDITIONS; i++) {
            many.add(Map.of("path", "a", "op", "==", "value", i));
        }
        assertThat(filter.validate(many, "filter")).hasSize(1);
        assertThat(filter.validate(List.of("not an object"), "filter")).hasSize(1);
        assertThat(BindingFilter.normalize(List.of(Map.of("op", "==", "path", "a", "value", 1))).get(0))
                .isEqualTo(new java.util.LinkedHashMap<>(Map.of("path", "a", "op", "==", "value", 1)));
    }

    @Test
    @DisplayName("effects: applied, skipped and failed views")
    void effects() {
        BoundItem item = BindingFixtures.item("levels", "a", "observation", 1, Map.of());
        BindingEffect ok = BindingEffect.of(item, "series-point", "series:x");
        assertThat(ok.view()).containsEntry("action", "series-point").containsEntry("targetRef", "series:x")
                .containsEntry("itemRef", item.ref());
        assertThat(ok.skipped()).isFalse();
        assertThat(ok.failed()).isFalse();
        assertThat(BindingEffect.skip(item, "older").action()).isEqualTo("skip:older");
        assertThat(BindingEffect.skip(item, "older").skipped()).isTrue();
        assertThat(BindingEffect.error(item, "bad").failed()).isTrue();
    }
}
