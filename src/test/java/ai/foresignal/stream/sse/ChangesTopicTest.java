package ai.foresignal.stream.sse;

import static ai.foresignal.stream.sse.SseFixtures.box;
import static ai.foresignal.stream.sse.SseFixtures.change;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.api.support.ApiProperties;
import ai.foresignal.retention.RetentionState;
import ai.foresignal.security.Scope;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.LiveBus;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Filter parsing, rendering and the resume floor of the {@code changes} topic, without a database. */
class ChangesTopicTest {

    private final SourceRegistry registry = mock(SourceRegistry.class);
    private final RecordQueryService queries = mock(RecordQueryService.class);
    private final RetentionState retention = mock(RetentionState.class);
    private ChangesTopic topic;

    @BeforeEach
    void setUp() {
        SourceDescriptor open = SourceDescriptor.builder("open").name("Open").kind("k").mode(Mode.PUSH).build();
        SourceDescriptor secret =
                SourceDescriptor.builder("secret").name("Secret").kind("k").mode(Mode.PUSH).privateSource(true).build();
        when(registry.descriptors()).thenReturn(List.of(open, secret));
        when(registry.descriptor("open")).thenReturn(open);
        when(registry.find("open")).thenReturn(Optional.of(new SourceRegistry.Registration(open, null, false)));
        topic = new ChangesTopic(queries, new RecordMapper(registry, SseFixtures.JSON),
                new ChangeBus(() -> 42L, new io.micrometer.core.instrument.simple.SimpleMeterRegistry()),
                new LiveBus(new io.micrometer.core.instrument.simple.SimpleMeterRegistry()), retention, registry,
                ApiProperties.defaults());
    }

    @Test
    @DisplayName("identity: changes, key c, order 0, read scope, head is the change watermark")
    void identity() {
        assertThat(topic.name()).isEqualTo("changes");
        assertThat(topic.cursorKey()).isEqualTo("c");
        assertThat(topic.cursorOrder()).isZero();
        assertThat(topic.requiredScope()).isEqualTo(Scope.READ);
        assertThat(topic.filterType()).isEqualTo(ChangeFilter.class);
        assertThat(topic.head()).isEqualTo(42L);
    }

    @Test
    @DisplayName("query parameters parse with defaults; errors carry the topic prefix")
    void parseQuery() {
        TopicParams params = new TopicParams("changes.", Map.of("source", List.of("open"), "bbox", List.of("170,-10,-170,10"),
                "touches", List.of("true")));
        ChangeFilter f = topic.parseQuery(params, StreamPrincipal.anonymous("1.1.1.1"));
        assertThat(f.sources()).containsExactly("open");
        assertThat(f.bbox().crossesAntimeridian()).isTrue();
        assertThat(f.live()).isTrue();
        assertThat(f.touches()).isTrue();
        assertThat(f.payload()).isFalse();
        assertThat(f.excludedSources()).containsExactly("secret");

        assertThatThrownBy(() -> topic.parseQuery(new TopicParams("changes.", Map.of("bbox", List.of("1,2"))),
                StreamPrincipal.anonymous("1.1.1.1")))
                .isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.getErrors()).extracting(err -> err.field()).containsExactly("changes.bbox"));
        assertThatThrownBy(() -> topic.parseQuery(new TopicParams("", Map.of("live", List.of("maybe"))),
                StreamPrincipal.anonymous("1.1.1.1"))).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("a PATCH block replaces the filter whole and is validated strictly")
    void parseJson() {
        StreamPrincipal token = new StreamPrincipal("token:x", Scope.ADMIN, null);
        ChangeFilter f = topic.parseJson(SseFixtures.JSON.readTree(
                "{\"kind\":[\"k\"],\"near\":\"-96.8,32.78\",\"radiusKm\":25,\"live\":false,\"payload\":true}"), token);
        assertThat(f.kinds()).containsExactly("k");
        assertThat(f.near().radiusKm()).isEqualTo(25.0);
        assertThat(f.live()).isFalse();
        assertThat(f.payload()).isTrue();
        assertThat(f.excludedSources()).isEmpty();
        assertThat(topic.parseJson(null, token)).isEqualTo(ChangeFilter.all(Set.of()));

        for (String bad : List.of("[]", "{\"color\":1}", "{\"kind\":\"k\"}", "{\"kind\":[1]}", "{\"bbox\":5}",
                "{\"radiusKm\":\"5\"}", "{\"live\":\"yes\"}", "{\"near\":\"1,2\"}")) {
            assertThatThrownBy(() -> topic.parseJson(SseFixtures.JSON.readTree(bad), token))
                    .as(bad).isInstanceOf(ValidationException.class);
        }
    }

    @Test
    @DisplayName("render applies the client's filter, leftFilter and the payload rule")
    void render() {
        ChangeFilter boxed = SseFixtures.boxFilter("0,0,10,10");
        Set<String> inline = Set.of("raster-frame");
        assertThat(topic.render(boxed, change(1, "open", "k", box(50, 50), null, null, false), null, inline)).isNull();
        FifoEvent left = topic.render(boxed, change(2, "open", "k", box(50, 50), box(5, 5), null, false), null, inline);
        assertThat(left.data()).containsEntry("leftFilter", true).containsEntry("geometry", null);
        assertThat(left.payloadSeq()).isNull();
        assertThat(left.cursor()).isEqualTo(2L);
        assertThat(left.event()).isEqualTo("change");
    }

    @Test
    @DisplayName("the floor is the larger of lowestRetainedSeq and the purge horizon; below it replay asks for a resync")
    void floor() {
        when(retention.lowestRetainedSeq(Set.of())).thenReturn(10L);
        when(queries.purgeHorizon(List.of())).thenReturn(20L);
        assertThat(topic.floor(List.of())).isEqualTo(20L);
        ReplayOutcome outcome = topic.replay(new ReplayRequest(5, true), ChangeFilter.all(Set.of()), e -> true);
        assertThat(outcome).isEqualTo(new ReplayOutcome.ResyncRequired(42L));
    }
}
