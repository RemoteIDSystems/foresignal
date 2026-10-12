package ai.foresignal.retention;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsChangedEvent;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import java.sql.ResultSet;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

/**
 * {@link DownsampleRanges} and {@link DownsampleFidelity} (prompt 044): range lookups, the
 * {@code max(ttl, interval)} widening, {@code finalBefore}, the cached per-source override and the
 * summary {@code /v1/system} reports.
 */
class DownsampleFidelityTest {

    static final Instant NOW = Instant.parse("2026-06-01T12:00:00Z");

    static final SourceDescriptor STANDARD = SourceDescriptor.builder("std").name("std").kind("event")
            .mode(Mode.PUSH).defaultBudget(Budget.of(60, 1000)).build();

    static SourceDescriptor track(String id, Duration ttl) {
        return SourceDescriptor.builder(id).name(id).kind("aircraft").mode(Mode.PUSH)
                .defaultBudget(Budget.of(60, 1000)).historyTier(HistoryTier.TRACK).recordTtl(ttl)
                .significantFields("callsign")
                .defaultIngestPolicy(new IngestPolicy.Sampled(Duration.ofSeconds(60), Duration.ofSeconds(60), 1000.0,
                        true, null))
                .build();
    }

    static final SourceDescriptor SHORT_TTL = track("short", Duration.ofMinutes(3));
    static final SourceDescriptor LONG_TTL = track("long", Duration.ofMinutes(15));

    JdbcTemplate jdbc = mock(JdbcTemplate.class);
    RetentionResolver resolver = mock(RetentionResolver.class);
    SourceRegistry registry = mock(SourceRegistry.class);
    SourceService sources = mock(SourceService.class);
    List<long[]> rows = new ArrayList<>();
    DownsampleRanges ranges;
    DownsampleFidelity fidelity;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        doAnswer(inv -> {
            RowCallbackHandler handler = inv.getArgument(1);
            for (long[] row : rows) {
                ResultSet rs = mock(ResultSet.class);
                when(rs.getShort(1)).thenReturn((short) row[0]);
                when(rs.getLong(2)).thenReturn(row[1]);
                when(rs.getLong(3)).thenReturn(row[2]);
                when(rs.getLong(4)).thenReturn(row[3]);
                handler.processRow(rs);
            }
            return null;
        }).when(jdbc).query(eq(DownsampleRanges.SELECT_DONE), any(RowCallbackHandler.class));
        for (SourceDescriptor d : List.of(STANDARD, SHORT_TTL, LONG_TTL)) {
            when(registry.find(d.id()))
                    .thenReturn(Optional.of(new SourceRegistry.Registration(d, mock(SourceAdapter.class), false)));
        }
        when(resolver.settings()).thenReturn(RetentionSettings.defaults());
        ObjectProvider<SourceService> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(sources);
        when(sources.downsampleIntervalOverride(anyString())).thenReturn(Optional.empty());
        ranges = new DownsampleRanges(jdbc);
        fidelity = new DownsampleFidelity(ranges, resolver, registry, provider, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("ranges: contains and overlaps over half-open leaves, TRACK only in the fidelity")
    void rangesAndOverlap() {
        rows.add(new long[] {1, 1000, 2000, 40});
        rows.add(new long[] {1, 3000, 4000, 60});
        ranges.afterSingletonsInstantiated();

        assertThat(ranges.contains(HistoryTier.TRACK, 1000)).isTrue();
        assertThat(ranges.contains(HistoryTier.TRACK, 1999)).isTrue();
        assertThat(ranges.contains(HistoryTier.TRACK, 2000)).isFalse();
        assertThat(ranges.contains(HistoryTier.TRACK, 999)).isFalse();
        assertThat(ranges.overlaps(HistoryTier.TRACK, 0, 999)).isFalse();
        assertThat(ranges.overlaps(HistoryTier.TRACK, 0, 1000)).isTrue();
        assertThat(ranges.overlaps(HistoryTier.TRACK, 2000, 2999)).isFalse();
        assertThat(ranges.overlaps(HistoryTier.TRACK, 2500, 3500)).isTrue();
        assertThat(ranges.overlaps(HistoryTier.TRACK, 10, 5)).as("empty range").isFalse();
        assertThat(ranges.overlaps(HistoryTier.STANDARD, 0, Long.MAX_VALUE)).isFalse();
        assertThat(ranges.isEmpty()).isFalse();
        assertThat(ranges.summary(HistoryTier.TRACK)).isEqualTo(new DownsampleRanges.Summary(2, 100));
        assertThat(ranges.summary(HistoryTier.STANDARD)).isEqualTo(new DownsampleRanges.Summary(0, 0));
        assertThat(ranges.ranges(HistoryTier.TRACK)).containsKeys(1000L, 3000L);

        assertThat(fidelity.downsampled(HistoryTier.TRACK, 1500, 1500)).isTrue();
        assertThat(fidelity.downsampled(HistoryTier.STANDARD, 1500, 1500)).isFalse();
    }

    @Test
    @DisplayName("a failed reload keeps the previous ranges; resetState reloads")
    void failedReload() {
        rows.add(new long[] {1, 1000, 2000, 40});
        ranges.refresh();
        doThrow(new DataAccessResourceFailureException("down"))
                .when(jdbc).query(eq(DownsampleRanges.SELECT_DONE), any(RowCallbackHandler.class));
        ranges.refresh();
        assertThat(ranges.contains(HistoryTier.TRACK, 1500)).isTrue();
        ranges.resetState();
        verify(jdbc, times(3)).query(eq(DownsampleRanges.SELECT_DONE), any(RowCallbackHandler.class));
    }

    @Test
    @DisplayName("widening is max(ttl, interval) − ttl for TRACK, zero for STANDARD, a long TTL and unknown sources")
    void widening() {
        assertThat(fidelity.validityWidening("short")).as("5m interval over a 3m TTL").isEqualTo(Duration.ofMinutes(2));
        assertThat(fidelity.validityWidening("long")).isZero();
        assertThat(fidelity.validityWidening("std")).isZero();
        when(registry.find("nope")).thenReturn(Optional.empty());
        assertThat(fidelity.validityWidening("nope")).isZero();
    }

    @Test
    @DisplayName("a per-source override is cached until the source's settings change")
    void overrideCache() {
        when(sources.downsampleIntervalOverride("short")).thenReturn(Optional.of(Duration.ofMinutes(10)));
        assertThat(fidelity.interval("short")).isEqualTo(Duration.ofMinutes(10));
        assertThat(fidelity.validityWidening("short")).isEqualTo(Duration.ofMinutes(7));

        when(sources.downsampleIntervalOverride("short")).thenReturn(Optional.empty());
        assertThat(fidelity.interval("short")).as("cached").isEqualTo(Duration.ofMinutes(10));
        fidelity.onSourceSettingsChanged(new SourceSettingsChangedEvent("short", null, null));
        assertThat(fidelity.interval("short")).isEqualTo(Duration.ofMinutes(5));

        when(sources.downsampleIntervalOverride("long")).thenReturn(Optional.of(Duration.ofMinutes(30)));
        assertThat(fidelity.interval("long")).isEqualTo(Duration.ofMinutes(30));
        fidelity.resetState();
        when(sources.downsampleIntervalOverride("long")).thenThrow(new IllegalStateException("db"));
        assertThat(fidelity.interval("long")).as("a failed read falls back to the global interval")
                .isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    @DisplayName("finalBefore: now − trackWindow + 1h for TRACK while enabled, else Instant.MAX")
    void finalBefore() {
        assertThat(fidelity.finalBefore(HistoryTier.TRACK)).isEqualTo(NOW.minus(Duration.ofDays(3)).plus(Duration.ofHours(1)));
        assertThat(fidelity.finalBefore(HistoryTier.STANDARD)).isEqualTo(Instant.MAX);
        when(resolver.settings()).thenReturn(new RetentionSettings(null, null, null, Map.of(), null, null, null, null,
                null, null, null, null, null, RetentionSettings.DOWNSAMPLING_DISABLED, null));
        assertThat(fidelity.finalBefore(HistoryTier.TRACK)).isEqualTo(Instant.MAX);
    }
}
