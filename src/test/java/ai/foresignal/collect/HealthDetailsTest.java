package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.dto.SourceHealthDto;
import ai.foresignal.api.mapper.HealthMapper;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.adapters.FakeStreamingAdapter;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

/**
 * {@code SourceHealth.details} (prompt 063): the {@link HealthDetailsContributor}s are merged in order, a failing
 * one is skipped, and no details at all is null, which the DTO omits.
 */
class HealthDetailsTest {

    private static final SourceDescriptor DESCRIPTOR = FakeStreamingAdapter.defaultDescriptor("details-test").build();

    @SuppressWarnings("unchecked")
    private static HealthService service(HealthDetailsContributor... contributors) {
        ObjectProvider<HealthRule> rules = mock(ObjectProvider.class);
        when(rules.orderedStream()).thenReturn(Stream.empty());
        ObjectProvider<HealthDetailsContributor> details = mock(ObjectProvider.class);
        when(details.orderedStream()).thenAnswer(invocation -> Stream.of(contributors));
        return new HealthService(null, null, null, null, null, null, null, null, null, rules, details, null, null,
                Clock.systemUTC());
    }

    @Test
    @DisplayName("entries are merged in bean order; a later key wins; null values are dropped")
    void merged() {
        Map<String, Object> withNull = new LinkedHashMap<>();
        withNull.put("b", null);
        withNull.put("c", 3);
        HealthService service = service(
                d -> Map.of("a", 1, "b", 2),
                d -> withNull,
                d -> Map.of("a", 10));

        assertThat(service.details(DESCRIPTOR)).containsExactly(Map.entry("a", 10), Map.entry("b", 2), Map.entry("c", 3));
    }

    @Test
    @DisplayName("a contributor that throws is skipped; nothing at all is null")
    void failingAndEmpty() {
        HealthService service = service(
                d -> {
                    throw new IllegalStateException("boom");
                },
                d -> Map.of());

        assertThat(service.details(DESCRIPTOR)).isNull();
        assertThat(service(d -> Map.of("compressionEnabled", true)).details(DESCRIPTOR))
                .containsEntry("compressionEnabled", true);
    }

    @Test
    @DisplayName("the mapper copies details into the DTO")
    void mapped() {
        SourceHealthView view = new SourceHealthView("s", HealthStatus.HEALTHY, null, null, null, null, null, null, 0, 0,
                0, 0.0, null, null, 0, null, 0, 0, 0, null, null, null, null, null, null,
                Map.of("compressionEnabled", true));

        SourceHealthDto dto = HealthMapper.health(view);

        assertThat(dto.details()).containsEntry("compressionEnabled", true);
        assertThat(List.of(dto.sourceId())).containsExactly("s");
    }
}
