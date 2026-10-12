package ai.foresignal.briefing;

import static ai.foresignal.testsupport.ingest.TestIngestSources.LATEST;
import static ai.foresignal.testsupport.ingest.TestIngestSources.LATEST_KIND;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK_KIND;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TTL;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TTL_KIND;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.briefing.BriefingItSupport;
import ai.foresignal.testsupport.briefing.BriefingTestSources;
import ai.foresignal.trigger.AlertView;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * {@link DigestStatsService} against records seeded through {@code TestRecords} (prompt 029): counts by
 * persist reason (KEYFRAME, HEARTBEAT and SAMPLE never counted as change), touch-only entities counted
 * active from {@code record_live}, deltas against the previous period, {@code gap} below the retention
 * floor, alerts keyed by id, ranking by {@code rankField}, and notable items carrying the persisted seq.
 */
@IntegrationTest
@Import(BriefingTestSources.class)
class DigestStatsServiceIT extends BriefingItSupport {

    @Autowired DigestStatsService stats;
    @Autowired ai.foresignal.stats.RollupJob rollup;

    static ScopeResolver.Resolved all() {
        return new ScopeResolver.Resolved(List.of(), List.of(), null, Set.of(), false);
    }

    static ScopeResolver.Resolved quakes(boolean health) {
        return new ScopeResolver.Resolved(List.of(), List.of(BriefingTestSources.QUAKE_KIND), null, Set.of(), health);
    }

    DigestStats cursor(long sinceSeq, ScopeResolver.Resolved scope, Set<String> hidden) {
        return stats.compute(new DigestStatsService.Request(sinceSeq, null, null, 0L, scope, 5, hidden, ZoneOffset.UTC,
                null));
    }

    DigestStats period(Instant from, Instant to, ScopeResolver.Resolved scope) {
        return stats.compute(new DigestStatsService.Request(null, from, to, null, scope, 5, Set.of(), ZoneOffset.UTC,
                null));
    }

    static DigestStats.Count count(DigestStats digest, String sourceId) {
        return digest.counts().stream().filter(c -> c.sourceId().equals(sourceId)).findFirst()
                .orElseThrow(() -> new AssertionError("no count for " + sourceId + " in " + digest.counts()));
    }

    Instant t(long seconds) {
        return T.plusSeconds(seconds);
    }

    static NormalizedRecord.Builder rec(String id, String kind, Instant observedAt) {
        return NormalizedRecord.builder(id, kind).observedAt(observedAt);
    }

    @Test
    @DisplayName("counts by persist reason: new, changed, removed; KEYFRAME, HEARTBEAT and SAMPLE are routine")
    void countsByReason() {
        records.ingestAt(STANDARD, T, rec("a", STANDARD_KIND, T).put("v", 1).build());
        records.ingestAt(STANDARD, t(10), rec("a", STANDARD_KIND, t(10)).put("v", 2).build());
        records.ingestAt(STANDARD, t(20), rec("a", STANDARD_KIND, t(20)).tombstone(true).build());
        records.ingestAt(STANDARD, t(30), rec("a", STANDARD_KIND, t(30)).put("v", 3).build());

        records.ingestAt(TTL, T, rec("hb", TTL_KIND, T).put("mag", 5).build());
        IngestOutcome heartbeat = records.ingestAt(TTL, t(580), rec("hb", TTL_KIND, t(580)).put("mag", 5).build());
        assertThat(heartbeat.persisted(PersistReason.HEARTBEAT)).isEqualTo(1);

        records.ingestAt(TRACK, t(0), aircraft(0, 50.0).build());
        IngestOutcome sample = records.ingestAt(TRACK, t(70), aircraft(70, 50.02).build());
        assertThat(sample.persisted(PersistReason.SAMPLE)).isEqualTo(1);

        Duration quarter = Duration.ofDays(7).dividedBy(4);
        records.ingestAt(LATEST, T, rec("k", LATEST_KIND, T).put("name", "a").put("v", 1).build());
        records.ingestAt(LATEST, t(10), rec("k", LATEST_KIND, t(10)).put("name", "a").put("v", 2).build());
        Instant due = T.plus(quarter);
        IngestOutcome keyframe = records.ingestAt(LATEST, due, rec("k", LATEST_KIND, due).put("name", "a").put("v", 2).build());
        assertThat(keyframe.persisted(PersistReason.KEYFRAME)).isEqualTo(1);

        DigestStats digest = cursor(0, all(), Set.of());

        DigestStats.Count standard = count(digest, STANDARD);
        assertThat(standard.kind()).isEqualTo(STANDARD_KIND);
        assertThat(standard.created()).as("FIRST + REAPPEAR").isEqualTo(2);
        assertThat(standard.changed()).isEqualTo(1);
        assertThat(standard.removed()).isEqualTo(1);
        assertThat(standard.routine()).isZero();
        assertThat(standard.entities().created()).as("one entity").isEqualTo(1);
        assertThat(standard.byReason()).containsEntry("FIRST", 1L).containsEntry("REAPPEAR", 1L)
                .containsEntry("TOMBSTONE", 1L);

        for (String routineSource : List.of(TTL, TRACK, LATEST)) {
            DigestStats.Count c = count(digest, routineSource);
            assertThat(c.created()).as(routineSource + " new").isEqualTo(1);
            assertThat(c.changed()).as(routineSource + " never change").isZero();
            assertThat(c.routine()).as(routineSource + " routine").isEqualTo(1);
        }
        assertThat(count(digest, TTL).byReason()).containsEntry("HEARTBEAT", 1L);
        assertThat(count(digest, TRACK).byReason()).containsEntry("SAMPLE", 1L);
        assertThat(count(digest, LATEST).byReason()).containsEntry("KEYFRAME", 1L);
        assertThat(digest.toMap()).containsKeys("fromSeq", "toSeq", "gap", "counts", "active", "alerts", "notable");
    }

    private NormalizedRecord.Builder aircraft(long second, double lat) {
        return rec("ac", TRACK_KIND, t(second)).put("callsign", "ABC123").put("squawk", "1200").put("alt", 10000)
                .put("gs", 0).put("track", 90).point(10.0, lat);
    }

    @Test
    @DisplayName("touch-only entities are active from record_live without any version in the period")
    void touchOnlyActivity() {
        clock.setInstant(T.minus(Duration.ofHours(2)));
        records.ingest(STANDARD, rec("touched", STANDARD_KIND, clock.instant()).put("v", 1).build(),
                rec("stale", STANDARD_KIND, clock.instant()).put("v", 1).build());
        clock.setInstant(T);
        IngestOutcome touch = records.ingest(STANDARD, rec("touched", STANDARD_KIND, T).put("v", 1).build());
        assertThat(touch.touched()).isEqualTo(1);
        assertThat(touch.persisted()).isZero();

        DigestStats digest = period(T.minus(Duration.ofHours(1)), T, all());

        assertThat(digest.counts()).as("no version in the period").isEmpty();
        assertThat(digest.active()).singleElement().satisfies(a -> {
            assertThat(a.sourceId()).isEqualTo(STANDARD);
            assertThat(a.kind()).isEqualTo(STANDARD_KIND);
            assertThat(a.entities()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("deltas against the previous equal-length period")
    void deltas() {
        clock.setInstant(T.minus(Duration.ofHours(48)));
        for (int i = 1; i <= 3; i++) {
            quake("old" + i, "Old " + i, 4.0);
        }
        clock.setInstant(T.minus(Duration.ofHours(1)));
        for (int i = 1; i <= 5; i++) {
            quake("new" + i, "New " + i, 4.0);
        }
        clock.setInstant(T);

        DigestStats digest = period(T.minus(Duration.ofHours(24)), T, quakes(false));

        DigestStats.Count c = count(digest, BriefingTestSources.QUAKES);
        assertThat(c.created()).isEqualTo(5);
        assertThat(c.previous()).isNotNull();
        assertThat(c.previous().created()).isEqualTo(3);
        assertThat(c.previous().active()).isNull();
        assertThat(digest.gap()).isFalse();
        assertThat(digest.health()).as("health not included").isNull();
    }

    @Test
    @DisplayName("gap when sinceSeq is below the retention floor; no delta then")
    void gapBelowFloor() {
        quake("a", "A", 4.0);
        quake("b", "B", 4.0);
        long floor = headSeq(BriefingTestSources.QUAKES, "b");
        retentionState.setLowestRetainedSeq(floor);

        DigestStats below = cursor(0, quakes(true), Set.of());
        assertThat(below.gap()).isTrue();
        assertThat(below.gapReason()).contains("retention floor " + floor);
        assertThat(below.counts()).allSatisfy(c -> assertThat(c.previous()).isNull());
        assertThat(below.health()).as("health included").isNotNull();
        assertThat(below.healthSnapshot()).isTrue();

        DigestStats atFloor = cursor(floor, quakes(true), Set.of());
        assertThat(atFloor.gap()).isFalse();
        assertThat(atFloor.gapReason()).isNull();
    }

    @Test
    @DisplayName("alerts are keyed by id: only alerts after sinceAlertId, critical first, grouped by severity, status, rule")
    void alertsById() {
        quakeWatch("Big", 6.0, "critical");
        quakeWatch("Any", 4.0, "info");
        quake("q1", "First", 4.5);
        fixture.settle();
        long since = jdbc.queryForObject("SELECT max(id) FROM alert", Long.class);
        quake("q2", "Second", 6.5);
        quake("q3", "Third", 5.0);
        fixture.settle();
        long until = jdbc.queryForObject("SELECT max(id) FROM alert", Long.class);

        DigestStats digest = stats.compute(new DigestStatsService.Request(0L, null, null, since, quakes(false), 5,
                Set.of(), ZoneOffset.UTC, null));

        DigestStats.Alerts alerts = digest.alerts();
        assertThat(alerts.sinceAlertId()).isEqualTo(since);
        assertThat(alerts.untilAlertId()).isEqualTo(until);
        assertThat(alerts.total()).isEqualTo(3);
        assertThat(alerts.severity("critical")).isEqualTo(1);
        assertThat(alerts.severity("info")).isEqualTo(2);
        assertThat(alerts.status("firing")).isEqualTo(3);
        assertThat(alerts.byRule()).extracting(DigestStats.RuleCount::ruleName).containsExactly("Any", "Big");
        assertThat(alerts.items()).hasSize(3);
        assertThat(alerts.items().getFirst().view().severity()).isEqualTo("critical");
        assertThat(alerts.items()).extracting(i -> i.view().id()).allSatisfy(id -> assertThat(id).isGreaterThan(since));
        AlertView first = alerts.items().getFirst().view();
        assertThat(alerts.items().getFirst().json()).containsEntry("id", first.id());
        assertThat(alerts.items().getFirst().link()).endsWith("/v1/alerts/" + first.id());
    }

    @Test
    @DisplayName("notable records are ranked by rankField, carry the persisted seq, and hidden sources are counted only")
    void notableRanking() {
        quake("a", "Small", 3.1);
        quake("b", "Large", 6.2);
        quake("c", "Medium", 4.5);
        secretQuake("s", "Secret place", 9.0);

        DigestStats digest = stats.compute(new DigestStatsService.Request(0L, null, null, 0L, quakes(false), 2,
                Set.of(BriefingTestSources.SECRET), ZoneOffset.UTC, null));

        assertThat(digest.notable()).singleElement().satisfies(group -> {
            assertThat(group.kind()).isEqualTo(BriefingTestSources.QUAKE_KIND);
            assertThat(group.items()).extracting(DigestStats.NotableItem::externalId).containsExactly("b", "c");
            DigestStats.NotableItem top = group.items().getFirst();
            assertThat(top.seq()).isEqualTo(headSeq(BriefingTestSources.QUAKES, "b"));
            assertThat(top.title()).isEqualTo("Large");
            assertThat(top.rankValue()).isEqualTo(6.2);
            assertThat(top.rankField()).isEqualTo("mag");
            assertThat(top.link()).endsWith("/v1/records/" + BriefingTestSources.QUAKES + "/b");
        });
        assertThat(count(digest, BriefingTestSources.SECRET).created()).as("counted").isEqualTo(1);
        assertThat(digest.toMap().toString()).doesNotContain("Secret place");

        DigestStats visible = cursor(0, quakes(false), Set.of());
        assertThat(visible.notableItems()).extracting(DigestStats.NotableItem::externalId).contains("s");
    }

    @Test
    @DisplayName("the previous period's counts come from StatsService: still there after its raw versions are deleted")
    void previousFromStats() {
        clock.setInstant(T.minus(Duration.ofHours(48)).plusSeconds(60));
        for (int i = 1; i <= 3; i++) {
            quake("old" + i, "Old " + i, 4.0);
        }
        clock.setInstant(T.minus(Duration.ofHours(1)));
        for (int i = 1; i <= 5; i++) {
            quake("new" + i, "New " + i, 4.0);
        }
        clock.setInstant(T);
        int hours = 0;
        for (int run = 0; run < 100; run++) {
            int rolled = rollup.runOnce().hours();
            hours += rolled;
            if (rolled == 0) {
                break;
            }
        }
        assertThat(hours).as("every closed hour since the first version").isEqualTo(47);
        jdbc.update("DELETE FROM record_version WHERE source_id = ? AND ingested_at < ?", BriefingTestSources.QUAKES,
                java.sql.Timestamp.from(T.minus(Duration.ofHours(24))));

        DigestStats digest = period(T.minus(Duration.ofHours(24)), T, quakes(false));

        DigestStats.Count c = count(digest, BriefingTestSources.QUAKES);
        assertThat(c.created()).isEqualTo(5);
        assertThat(c.previous()).as("from the rollup").isNotNull();
        assertThat(c.previous().created()).isEqualTo(3);
        assertThat(c.previous().changed()).isZero();
        assertThat(c.previous().removed()).isZero();
    }
}
