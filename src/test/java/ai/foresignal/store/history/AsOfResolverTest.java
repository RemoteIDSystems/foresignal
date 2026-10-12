package ai.foresignal.store.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.ingest.SourceIngestProfile;
import ai.foresignal.ingest.SourceIngestProfiles;
import ai.foresignal.retention.RetentionState;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.ReadQueryRunner;
import ai.foresignal.store.query.SeqClockQuery;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * {@link AsOfResolver} (prompt 037; Core-Data-Model §12): the filter rule, the horizon, a future
 * {@code t}, the per-tier lower bounds, {@code approximate} under SAMPLED, the private gate, and the
 * {@link HistoryFidelity} hook (widening, the downsampled flag and settled ranges).
 */
class AsOfResolverTest {

    static final Instant NOW = Instant.parse("2026-06-01T12:00:00Z");

    static final SourceDescriptor STANDARD = base("std", "event").build();
    static final SourceDescriptor TRACK = base("trk", "aircraft")
            .historyTier(HistoryTier.TRACK)
            .recordTtl(Duration.ofMinutes(5))
            .significantFields("callsign")
            .defaultIngestPolicy(new IngestPolicy.Sampled(Duration.ofSeconds(60), Duration.ofSeconds(240), 1000.0,
                    true, null))
            .build();
    static final SourceDescriptor TRACK_ALL = base("trk-all", "vehicle")
            .historyTier(HistoryTier.TRACK)
            .recordTtl(Duration.ofMinutes(10))
            .build();
    static final SourceDescriptor PRIVATE = base("phone", "phone").privateSource(true).build();

    NamedParameterJdbcTemplate jdbc = mock(NamedParameterJdbcTemplate.class);
    ReadQueryRunner runner = mock(ReadQueryRunner.class);
    SeqClockQuery seqClock = mock(SeqClockQuery.class);
    SourceRegistry registry = mock(SourceRegistry.class);
    SourceIngestProfiles profiles = mock(SourceIngestProfiles.class);
    RetentionState retention = mock(RetentionState.class);
    StubFidelity fidelity = new StubFidelity();
    AsOfResolver resolver;

    static SourceDescriptor.Builder base(String id, String kind) {
        return SourceDescriptor.builder(id).name(id).kind(kind).mode(Mode.PUSH).defaultBudget(Budget.of(60, 1000));
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        when(runner.read(any(Function.class))).thenAnswer(inv -> ((Function<NamedParameterJdbcTemplate, ?>)
                inv.getArgument(0)).apply(jdbc));
        List<SourceDescriptor> all = List.of(STANDARD, TRACK, TRACK_ALL, PRIVATE);
        when(registry.descriptors()).thenReturn(all);
        for (SourceDescriptor d : all) {
            when(registry.descriptor(d.id())).thenReturn(d);
            when(profiles.get(d.id())).thenReturn(SourceIngestProfile.forDescriptor(d));
            when(retention.window(d.id())).thenReturn(d.historyTier() == HistoryTier.TRACK
                    ? Duration.ofDays(3) : Duration.ofDays(7));
        }
        when(seqClock.seqAt(any(), any())).thenReturn(0L);
        resolver = new AsOfResolver(runner, seqClock, registry, profiles, retention, fidelity,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }


    @Test
    @DisplayName("an empty filter is 400 ASOF_FILTER_REQUIRED")
    void filterRequired() {
        assertThatThrownBy(() -> resolver.plan(NOW, List.of(), List.of(), Set.of()))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getCode()).isEqualTo(ErrorCodes.ASOF_FILTER_REQUIRED));
        assertThatThrownBy(() -> resolver.plan(NOW, Set.of()))
                .satisfies(e -> assertThat(((ValidationException) e).getCode()).isEqualTo(ErrorCodes.ASOF_FILTER_REQUIRED));
    }

    @Test
    @DisplayName("t in the future is 400 ASOF_INVALID (014's code)")
    void future() {
        assertThatThrownBy(() -> resolver.plan(NOW.plusSeconds(1), Set.of("std")))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getCode()).isEqualTo(ErrorCodes.ASOF_INVALID));
    }

    @Test
    @DisplayName("t before the latest horizon is 400 ASOF_BEYOND_RETENTION carrying the horizon")
    void horizon() {
        Instant horizon = NOW.minus(Duration.ofDays(7)).plus(Duration.ofDays(7).dividedBy(4)).plus(Duration.ofHours(1));
        assertThat(resolver.horizon("std", NOW)).isEqualTo(horizon);
        assertThatThrownBy(() -> resolver.plan(horizon.minusSeconds(1), Set.of("std")))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> {
                    ValidationException v = (ValidationException) e;
                    assertThat(v.getCode()).isEqualTo(ErrorCodes.ASOF_BEYOND_RETENTION);
                    assertThat(v.getProperties()).containsEntry("horizon", horizon.toString());
                });
        AsOfPlan plan = resolver.plan(horizon, Set.of("std"));
        assertThat(plan.horizon()).isEqualTo(horizon);
        // The track window is shorter, so a plan over both uses the later horizon.
        Instant trackHorizon = resolver.horizon("trk", NOW);
        assertThat(trackHorizon).isAfter(horizon);
        assertThat(resolver.horizon(List.of("std", "trk"), NOW)).isEqualTo(trackHorizon);
        assertThat(resolver.horizon(List.of(), NOW)).isNull();
    }

    @Test
    @DisplayName("S from seq_clock; TRACK L = S(t - ttl - 2 min), STANDARD L = 0")
    void perTierLowerBound() {
        Instant t = NOW.minus(Duration.ofHours(2));
        when(seqClock.seqAt(any(), eq(t))).thenReturn(100L);
        when(seqClock.seqAt(any(), eq(t.minus(Duration.ofMinutes(5)).minus(AsOfResolver.TRACK_LOWER_MARGIN))))
                .thenReturn(40L);
        when(seqClock.seqAt(any(), eq(t.minus(Duration.ofMinutes(10)).minus(AsOfResolver.TRACK_LOWER_MARGIN))))
                .thenReturn(30L);

        AsOfPlan plan = resolver.plan(t, List.of("std", "trk", "trk-all"), List.of(), Set.of());
        assertThat(plan.s()).isEqualTo(100L);
        assertThat(plan.watermarkSeq()).isEqualTo(100L);
        assertThat(plan.lower(HistoryTier.STANDARD)).isZero();
        assertThat(plan.lower(HistoryTier.TRACK)).as("the smallest over the tier's sources").isEqualTo(30L);
        assertThat(plan.sources().get(HistoryTier.TRACK)).extracting(SourceDescriptor::id).containsExactly("trk", "trk-all");
        assertThat(plan.validity("trk")).isEqualTo(Duration.ofMinutes(5));
        assertThat(plan.validity("std")).isNull();
        assertThat(plan.sourceIds()).containsExactly("std", "trk", "trk-all");
        assertThat(plan.empty()).isFalse();
    }

    @Test
    @DisplayName("approximate when a source is SAMPLED, not for ALL sources alone")
    void approximate() {
        Instant t = NOW.minus(Duration.ofHours(1));
        assertThat(resolver.plan(t, Set.of("trk")).approximate()).isTrue();
        assertThat(resolver.plan(t, Set.of("trk-all")).approximate()).isFalse();
        assertThat(resolver.plan(t, Set.of("std")).approximate()).isFalse();
        assertThat(resolver.sampled("trk")).isTrue();
    }

    @Test
    @DisplayName("kinds resolve to their declaring sources")
    void kinds() {
        AsOfPlan plan = resolver.plan(NOW.minus(Duration.ofHours(1)), List.of(), List.of("aircraft"), Set.of());
        assertThat(plan.sourceIds()).containsExactly("trk");
    }

    @Test
    @DisplayName("private sources are dropped without a caller, and kept when the caller passes the gate")
    void privateGate() {
        Instant t = NOW.minus(Duration.ofHours(1));
        AsOfPlan anonymous = resolver.plan(t, Set.of("phone", "std"));
        assertThat(anonymous.sourceIds()).containsExactly("std");
        AsOfPlan agent = resolver.plan(t, List.of("phone", "std"), Set.of());
        assertThat(agent.sourceIds()).containsExactly("phone", "std");
        AsOfPlan none = resolver.plan(t, Set.of("phone"));
        assertThat(none.empty()).isTrue();
        assertThat(none.horizon()).isNull();
    }

    @Test
    @DisplayName("the widening applies only when the widened range is downsampled (prompt 044)")
    void fidelityHook() {
        Instant t = NOW.minus(Duration.ofHours(1));
        fidelity.widening = Duration.ofMinutes(10);
        when(seqClock.seqAt(any(), eq(t))).thenReturn(500L);
        when(seqClock.seqAt(any(), eq(t.minus(Duration.ofMinutes(20)).minus(AsOfResolver.TRACK_LOWER_MARGIN))))
                .thenReturn(200L);
        when(seqClock.seqAt(any(), eq(t.minus(Duration.ofMinutes(10)).minus(AsOfResolver.TRACK_LOWER_MARGIN))))
                .thenReturn(350L);

        AsOfPlan raw = resolver.plan(t, Set.of("trk-all"));
        assertThat(raw.validity("trk-all")).as("raw history: the plain TTL").isEqualTo(Duration.ofMinutes(10));
        assertThat(raw.lower(HistoryTier.TRACK)).isEqualTo(350L);
        assertThat(raw.approximate()).isFalse();
        assertThat(raw.downsampled()).isFalse();
        assertThat(fidelity.lastRange).as("checked over the widened range").containsExactly(200L, 500L);

        fidelity.downsampled = true;
        AsOfPlan thinned = resolver.plan(t, Set.of("trk-all"));
        assertThat(thinned.validity("trk-all")).as("ttl 10 min + widening 10 min").isEqualTo(Duration.ofMinutes(20));
        assertThat(thinned.lower(HistoryTier.TRACK)).isEqualTo(200L);
        assertThat(thinned.downsampled()).isTrue();
        assertThat(thinned.approximate()).isTrue();
    }

    @Test
    @DisplayName("without a widening the downsampled flag still marks the plan approximate")
    void downsampledWithoutWidening() {
        Instant t = NOW.minus(Duration.ofHours(1));
        when(seqClock.seqAt(any(), eq(t))).thenReturn(500L);
        fidelity.downsampled = true;
        AsOfPlan thinned = resolver.plan(t, Set.of("trk-all"));
        assertThat(thinned.downsampled()).isTrue();
        assertThat(thinned.approximate()).isTrue();
        assertThat(thinned.validity("trk-all")).isEqualTo(Duration.ofMinutes(10));
        assertThat(fidelity.lastRange).containsExactly(0L, 500L);
    }

    @Test
    @DisplayName("settled: before finalBefore, or already downsampled")
    void settled() {
        Instant t = NOW.minus(Duration.ofHours(1));
        AsOfPlan plan = resolver.plan(t, Set.of("trk-all"));
        assertThat(resolver.settled(plan)).as("raw history is final everywhere").isTrue();
        fidelity.finalBefore = t;
        assertThat(resolver.settled(plan)).as("pending downsampling").isFalse();
        fidelity.downsampled = true;
        assertThat(resolver.settled(plan)).as("already downsampled").isTrue();
    }

    @Test
    @DisplayName("the plan's validity rule matches Core-Data-Model §12")
    void validRule() {
        AsOfPlan plan = new AsOfPlan(NOW, 1, Map.of(), null, false, 1, Map.of(),
                Map.of("trk", Duration.ofMinutes(5)), false);
        Instant ingested = NOW.minus(Duration.ofMinutes(4));
        assertThat(plan.valid(HistoryTier.TRACK, "trk", ingested, null, NOW)).isTrue();
        assertThat(plan.valid(HistoryTier.TRACK, "trk", ingested, NOW.minusSeconds(1), NOW))
                .as("TRACK ignores an explicit validUntil").isTrue();
        assertThat(plan.valid(HistoryTier.TRACK, "trk", NOW.minus(Duration.ofMinutes(6)), null, NOW)).isFalse();
        assertThat(plan.valid(HistoryTier.STANDARD, "std", NOW.minus(Duration.ofDays(30)), null, NOW))
                .as("no TTL never expires").isTrue();
        assertThat(plan.valid(HistoryTier.STANDARD, "std", ingested, NOW.minusSeconds(1), NOW)).isFalse();
        assertThat(plan.valid(HistoryTier.STANDARD, "trk", NOW.minus(Duration.ofDays(1)), NOW.plusSeconds(1), NOW))
                .as("an explicit validUntil wins on STANDARD").isTrue();
    }

    @Test
    @DisplayName("RawHistoryFidelity is raw everywhere")
    void rawFidelity() {
        RawHistoryFidelity raw = new RawHistoryFidelity();
        assertThat(raw.downsampled(HistoryTier.TRACK, 0, Long.MAX_VALUE)).isFalse();
        assertThat(raw.validityWidening("x")).isZero();
        assertThat(raw.finalBefore(HistoryTier.STANDARD)).isEqualTo(Instant.MAX);
    }

    /** A settable fidelity. */
    static final class StubFidelity implements HistoryFidelity {
        Duration widening = Duration.ZERO;
        boolean downsampled;
        Instant finalBefore = Instant.MAX;
        long[] lastRange;

        @Override
        public boolean downsampled(HistoryTier tier, long fromSeq, long toSeq) {
            lastRange = new long[] {fromSeq, toSeq};
            return downsampled;
        }

        @Override
        public Duration validityWidening(String sourceId) {
            return widening;
        }

        @Override
        public Instant finalBefore(HistoryTier tier) {
            return finalBefore;
        }
    }
}
