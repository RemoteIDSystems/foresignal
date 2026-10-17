package ai.foresignal.analytics.finding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import ai.foresignal.analytics.AnalyticsAccess;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.derive.DerivedContext;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stream.Subscription;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.analytics.AnalyticsTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Findings (prompt 119): dedup and supersede by window, identical re-runs change nothing, a manifest on every finding
 * (MAN-1), the {@code foresignal-analytics} record emitted only on change and tombstoned on dismissal or
 * supersession, raw values of {@code LOCAL_ONLY} series withheld from callers the egress policy refuses and never on a
 * record (EGR-2), private findings hidden (EGR-1), and the {@code analytics} SSE events.
 */
@IntegrationTest
@Import(AnalyticsTestSources.class)
class FindingServiceIT {

    static final Instant NOW = Instant.parse("2026-03-10T03:00:00Z");

    @Autowired FindingService findings;
    @Autowired FindingEventBus bus;
    @Autowired ForesignalAnalyticsGenerator generator;
    @Autowired AnalyticsAccess access;
    @Autowired AppSettingService settings;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void freeze() {
        clock.setInstant(NOW);
    }

    static FindingDraft draft(String severity, Instant windowStart, Instant windowEnd, double effect,
            List<String> localOnly, boolean privateFinding) {
        return new FindingDraft("change-point", "Change point: oil up", severity, null, List.of("YE"), windowStart,
                windowEnd, NOW, List.of("series:an-local/oil"), List.of(), 42, Map.of("effect", effect, "levelBefore",
                        70.0, "levelAfter", 90.0), Map.of("value", 1.0, "unit", "alarms-per-year", "method",
                        "detector-false-alarm-rate", "text", "flagged once a year"),
                List.of(FindingCaveats.notGroundTruth()), "changepoint-cusum", "1.0.0", Map.of("minRelShift", 0.5),
                "explained", privateFinding, "markets", localOnly, localOnly.isEmpty() ? List.of()
                        : List.of("levelBefore", "levelAfter"), "series:an-local/oil");
    }

    @Test
    @DisplayName("an identical re-run changes nothing; an overlapping window supersedes; a disjoint one stands alone")
    void dedupAndSupersede() {
        Instant d1 = NOW.minus(Duration.ofDays(5));
        FindingService.Outcome first = findings.record(draft("notable", d1, d1.plus(Duration.ofDays(2)), 0.6,
                List.of(), false));
        assertThat(first.change()).isEqualTo(FindingService.Change.CREATED);
        assertThat(first.finding().getManifestId()).as("MAN-1").isNotNull();
        assertThat(first.finding().getGeom()).as("placed at its region").isNotNull();

        FindingService.Outcome same = findings.record(draft("notable", d1, d1.plus(Duration.ofDays(2)), 0.6,
                List.of(), false));
        assertThat(same.change()).isEqualTo(FindingService.Change.UNCHANGED);
        assertThat(same.finding().getId()).isEqualTo(first.finding().getId());

        FindingService.Outcome upgraded = findings.record(draft("significant", d1, d1.plus(Duration.ofDays(2)), 0.6,
                List.of(), false));
        assertThat(upgraded.change()).isEqualTo(FindingService.Change.SUPERSEDED);
        AnalyticFindingEntity old = findings.visible(first.finding().getId(), true);
        assertThat(old.getStatus()).isEqualTo("superseded");
        assertThat(old.getSupersededBy()).isEqualTo(upgraded.finding().getId());

        FindingService.Outcome later = findings.record(draft("notable", NOW.minus(Duration.ofHours(10)),
                NOW.minus(Duration.ofHours(5)), 0.6, List.of(), false));
        assertThat(later.change()).isEqualTo(FindingService.Change.CREATED);
        assertThat(findings.list(new FindingService.ListQuery(List.of("change-point"), List.of(), List.of("open"),
                List.of("YE"), "series:an-local/oil", null, null, null, 10), false).items()).hasSize(2);
    }

    @Test
    @DisplayName("the foresignal-analytics record is emitted on create and change only and tombstoned on dismissal")
    void records() {
        DerivedContext ctx = mock(DerivedContext.class);
        generator.reconcile(List.of(), ctx);
        FindingService.Outcome f = findings.record(draft("notable", NOW.minus(Duration.ofDays(2)), NOW, 0.6,
                List.of("an-local"), false));
        generator.onTick(NOW, ctx);
        ArgumentCaptor<NormalizedRecord> emitted = ArgumentCaptor.forClass(NormalizedRecord.class);
        verify(ctx, times(1)).emit(emitted.capture());
        NormalizedRecord record = emitted.getValue();
        assertThat(record.kind()).isEqualTo(ForesignalAnalyticsGenerator.KIND);
        assertThat(record.externalId()).isEqualTo(f.finding().getId());
        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) record.payload().get("stats");
        assertThat(stats).as("raw LOCAL_ONLY values never reach a record").containsKey("effect")
                .doesNotContainKeys("levelBefore", "levelAfter");
        assertThat(record.payload()).containsEntry("automated", true).containsEntry("severity", "notable");

        generator.onTick(NOW.plusSeconds(30), ctx);
        verify(ctx, times(1)).emit(any());

        clock.setInstant(NOW.plusSeconds(60));
        findings.dismiss(f.finding().getId(), "holiday effect", "tester", true);
        generator.onTick(NOW.plusSeconds(90), ctx);
        verify(ctx).tombstone(ForesignalAnalyticsGenerator.KIND, f.finding().getId());
        assertThat(findings.visible(f.finding().getId(), true).getDismissal()).containsEntry("reason",
                "holiday effect");
    }

    @Test
    @DisplayName("info findings are records only with analysis.findings.publishInfo; private ones never")
    void publishable() {
        DerivedContext ctx = mock(DerivedContext.class);
        generator.reconcile(List.of(), ctx);
        findings.record(draft("info", NOW.minus(Duration.ofDays(2)), NOW, 0.6, List.of(), false));
        AnalyticFindingEntity hidden = findings.record(new FindingDraft("co-occurrence", "private", "significant",
                null, List.of("DE"), NOW, NOW, NOW, List.of(), List.of(), 1, Map.of(), Map.of(), List.of(),
                "cooccurrence", "1.0.0", Map.of(), "x", true, "fire", List.of(), List.of(), "DE|a+b")).finding();
        generator.onTick(NOW, ctx);
        verify(ctx, never()).emit(any());
        settings.put(SettingKeys.ANALYSIS_FINDINGS_PUBLISH_INFO, true);
        generator.reconcile(List.of(), ctx);
        generator.onTick(NOW.plusSeconds(30), ctx);
        verify(ctx, times(1)).emit(any());
        assertThatThrownBy(() -> findings.visible(hidden.getId(), false)).isInstanceOf(NotFoundException.class);
        assertThat(findings.list(new FindingService.ListQuery(List.of(), List.of(), List.of(), List.of(), null, null,
                null, null, 10), false).items()).noneMatch(AnalyticFindingEntity::isPrivateFinding);
    }

    @Test
    @DisplayName("raw values are withheld only from callers the egress policy refuses the LOCAL_ONLY source to")
    void localOnly() {
        AnalyticFindingEntity f = findings.record(draft("notable", NOW.minus(Duration.ofDays(2)), NOW, 0.6,
                List.of("an-local"), false)).finding();
        ReadCaller reader = new ReadCaller(Scope.READ, true, false, ReadRestriction.NONE, false);
        assertThat(access.withheld(f, reader, EgressPath.API_READ)).as("API reads are local paths").isFalse();
        assertThat(access.withheld(f, reader, EgressPath.SHARE)).isTrue();
        assertThat(FindingViews.stats(f, true)).doesNotContainKey("levelBefore").containsKey("effect");
        assertThat(FindingViews.stats(f, false)).containsKey("levelBefore");
        assertThat(FindingViews.full(f, null, true)).extractingByKey("evidence").satisfies(e -> assertThat(
                ((Map<?, ?>) e).get("valuesWithheld")).isEqualTo(true));
    }

    @Test
    @DisplayName("finding.created, finding.updated and finding.dismissed reach the analytics topic's bus")
    void events() {
        List<FindingEvent> seen = new CopyOnWriteArrayList<>();
        Subscription subscription = bus.subscribe("finding-it", new FindingListener() {
            @Override
            public void onEvent(FindingEvent event) {
                seen.add(event);
            }
        }, 64);
        try {
            Instant d1 = NOW.minus(Duration.ofDays(5));
            AnalyticFindingEntity a = findings.record(draft("notable", d1, d1.plus(Duration.ofDays(2)), 0.6,
                    List.of(), false)).finding();
            findings.record(draft("significant", d1, d1.plus(Duration.ofDays(2)), 0.7, List.of(), false));
            findings.dismiss(a.getId(), "test", "tester", true);
            Awaitility.await().atMost(Duration.ofSeconds(10)).until(() -> seen.size() >= 4);
            assertThat(seen).extracting(FindingEvent::event).contains(FindingEvent.CREATED, FindingEvent.UPDATED,
                    FindingEvent.DISMISSED);
            assertThat(seen.get(0).payload()).containsKeys("event", "finding", "at");
            assertThat(seen).extracting(FindingEvent::cursor).isSorted();
        } finally {
            subscription.close();
        }
        assertThat(jdbc.queryForObject("SELECT count(*) FROM analytic_finding_log", Long.class)).isEqualTo(4L);
    }
}
