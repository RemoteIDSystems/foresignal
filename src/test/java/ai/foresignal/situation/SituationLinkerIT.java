package ai.foresignal.situation;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.trigger.TriggerEngine;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The situation linker end to end (prompt 118), seeded only through {@code TestRecords}: a critical intelligence
 * event seeds an emerging situation; a nearby event of a second family from an independent source joins and
 * activates it; an event in an unrelated region does not join; a shared entity joins across more than 800 km; a
 * rewound cursor keeps membership exactly-once; and the dry run equals the live result over the same seq range.
 *
 * <p>The intelligence events are records of kind {@code intelligence-event} in two test sources, which are distinct
 * originators (116), so the two events are two independent evidence groups.
 */
@IntegrationTest
@Import(TestIngestSources.class)
class SituationLinkerIT {

    static final String STANDARD = TestIngestSources.STANDARD;
    static final String OTHER = TestIngestSources.TTL;
    static final Instant T0 = Instant.parse("2026-03-01T12:00:00Z");

    /** Dallas, Texas. */
    static final double[] DALLAS = {-96.80, 32.78};
    /** About 10 km from Dallas. */
    static final double[] NEAR_DALLAS = {-96.72, 32.83};
    /** Denver, Colorado: about 1,070 km from Dallas, same country. */
    static final double[] DENVER = {-104.99, 39.74};
    /** Brasília: another country. */
    static final double[] BRASILIA = {-47.88, -15.79};

    @Autowired SituationLinker linker;
    @Autowired SituationRepository situations;
    @Autowired SituationMemberRepository members;
    @Autowired SituationLogRepository logs;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired ChangeBus changeBus;
    @Autowired TriggerEngine triggers;

    @BeforeEach
    void setUp() {
        clock.setInstant(T0);
    }

    /** Ingests one intelligence-event record. */
    void event(String source, String id, String status, String severity, String family, double[] at, Instant when) {
        clock.setInstant(when);
        records.ingestAt(source, when, NormalizedRecord.builder(id, LinkerInputReader.INTEL_KIND).observedAt(when)
                .point(at[0], at[1])
                .put("type", "maritime-disruption")
                .put("status", status)
                .put("severity", severity)
                .put("signalFamilies", List.of(family))
                .put("title", "Event " + id)
                .put("automated", true)
                .build());
    }

    /** Links everything published so far, after the trigger engine has caught up (its cursor bounds the linker). */
    void link() {
        long target = changeBus.lastPublishedSeq();
        long deadline = System.currentTimeMillis() + 10_000;
        while (triggers.running() && triggers.cursor() < target && System.currentTimeMillis() < deadline) {
            Thread.onSpinWait();
        }
        linker.catchUp();
    }

    long maxSeq() {
        Long seq = jdbc.queryForObject("select max(seq) from record_version", Long.class);
        return seq == null ? 0 : seq;
    }

    SituationEntity holderOf(String ref) {
        List<SituationMemberEntity> found = members.findByMemberTypeAndRef(SituationMemberTypes.INTEL_EVENT, ref);
        assertThat(found).as("memberships of " + ref).hasSize(1);
        return situations.findById(found.get(0).getSituationId()).orElseThrow();
    }

    @Test
    @DisplayName("a critical event seeds an emerging situation; a second family from another source joins and activates it")
    void seedJoinActivate() {
        event(STANDARD, "e1", "active", "critical", ai.foresignal.intel.SignalFamilies.AIS_BEHAVIOUR, DALLAS, T0);
        link();
        List<SituationEntity> all = situations.findAll();
        assertThat(all).hasSize(1);
        SituationEntity seeded = all.get(0);
        assertThat(seeded.getStatus()).isEqualTo(SituationLifecycle.EMERGING);
        assertThat(seeded.getTitle()).startsWith("Maritime disruption — ");
        assertThat(seeded.getTheme()).isEqualTo(SituationThemes.MARITIME);
        assertThat(seeded.getOrigin()).isEqualTo("auto");
        assertThat(seeded.getLinkerVersion()).isEqualTo("1.0.0");
        assertThat(seeded.getSeverity()).isEqualTo("critical");
        assertThat(seeded.getGeom()).isNotNull();
        SituationMemberEntity seed = members.findBySituationIdOrderByAddedAtAscIdAsc(seeded.getId()).get(0);
        assertThat(seed.getJoinReason()).containsEntry("method", "seed").containsEntry("linkerVersion", "1.0.0");
        assertThat(seed.getAddedBy()).isEqualTo("linker:1.0.0");

        event(OTHER, "e2", "active", "warning", "gnss-interference", NEAR_DALLAS, T0.plusSeconds(600));
        link();
        SituationEntity joined = holderOf("e2");
        assertThat(joined.getId()).isEqualTo(seeded.getId());
        assertThat(joined.getStatus()).isEqualTo(SituationLifecycle.ACTIVE);
        assertThat(joined.getSignalFamilies()).containsExactly(ai.foresignal.intel.SignalFamilies.AIS_BEHAVIOUR, "gnss-interference");
        SituationMemberEntity join = members.findByMemberTypeAndRef(SituationMemberTypes.INTEL_EVENT, "e2").get(0);
        assertThat(join.getJoinReason()).containsEntry("method", "linker").containsKeys("score", "components");
        @SuppressWarnings("unchecked")
        Map<String, Object> components = (Map<String, Object>) join.getJoinReason().get("components");
        assertThat(components).containsKeys("region", "distanceKm", "entity", "topic", "textSimilarity", "theme",
                "recencyHours");
        assertThat(((Number) join.getJoinReason().get("score")).doubleValue()).isGreaterThanOrEqualTo(0.45);
        assertThat(logs.findBySituationIdOrderByIdAsc(seeded.getId())).extracting(SituationLogEntity::getEvent)
                .contains(SituationEvent.CREATED, SituationEvent.ACTIVATED);
    }

    @Test
    @DisplayName("an event in an unrelated region does not join; it seeds its own situation")
    void unrelatedRegion() {
        event(STANDARD, "e1", "active", "critical", ai.foresignal.intel.SignalFamilies.AIS_BEHAVIOUR, DALLAS, T0);
        event(OTHER, "e3", "active", "warning", "gnss-interference", BRASILIA, T0.plusSeconds(60));
        link();
        assertThat(holderOf("e3").getId()).isNotEqualTo(holderOf("e1").getId());
        assertThat(situations.count()).isEqualTo(2);
        // A low-severity input that matches nothing is not linked at all.
        event(OTHER, "e5", "emerging", "info", "conflict", BRASILIA, T0.plus(Duration.ofDays(20)));
        link();
        assertThat(members.findByMemberTypeAndRef(SituationMemberTypes.INTEL_EVENT, "e5")).isEmpty();
    }

    @Test
    @DisplayName("a shared entity joins across more than 800 km")
    void sharedEntity() {
        link(STANDARD, "e1");
        link(STANDARD, "e4");
        event(STANDARD, "e1", "active", "critical", ai.foresignal.intel.SignalFamilies.AIS_BEHAVIOUR, DALLAS, T0);
        link();
        event(STANDARD, "e4", "emerging", "info", "nav-warning", DENVER, T0.plusSeconds(600));
        link();
        SituationEntity situation = holderOf("e4");
        assertThat(situation.getId()).isEqualTo(holderOf("e1").getId());
        SituationMemberEntity member = members.findByMemberTypeAndRef(SituationMemberTypes.INTEL_EVENT, "e4").get(0);
        @SuppressWarnings("unchecked")
        Map<String, Object> components = (Map<String, Object>) member.getJoinReason().get("components");
        assertThat(((Number) components.get("entity")).doubleValue()).isEqualTo(0.25);
        assertThat(((Number) components.get("distanceKm")).doubleValue()).isGreaterThan(800);
        assertThat(situation.getEntityIds()).contains("ENT-SHIP-1");
    }

    /** An open 098 entity link of a record to {@code ENT-SHIP-1}. */
    void link(String source, String externalId) {
        jdbc.update("INSERT INTO entity_link (entity_id, source_id, external_id, key_type, key_value, confidence,"
                + " valid_from) VALUES ('ENT-SHIP-1', ?, ?, 'imo', '9999999', 1.0, now()) ON CONFLICT DO NOTHING",
                source, externalId);
    }

    @Test
    @DisplayName("a rewound cursor gives exactly-once membership")
    void exactlyOnce() {
        event(STANDARD, "e1", "active", "critical", ai.foresignal.intel.SignalFamilies.AIS_BEHAVIOUR, DALLAS, T0);
        event(OTHER, "e2", "active", "warning", "gnss-interference", NEAR_DALLAS, T0.plusSeconds(60));
        link();
        long situationsBefore = situations.count();
        long membersBefore = members.count();
        linker.rewind(0);
        link();
        assertThat(situations.count()).isEqualTo(situationsBefore);
        assertThat(members.count()).isEqualTo(membersBefore);
        // A new version of a member event is not linked again either.
        event(STANDARD, "e1", "active", "critical", ai.foresignal.intel.SignalFamilies.AIS_BEHAVIOUR, DALLAS, T0.plusSeconds(120));
        link();
        assertThat(members.count()).isEqualTo(membersBefore);
        assertThat(linker.cursor()).isEqualTo(changeBus.lastPublishedSeq());
    }

    @Test
    @DisplayName("the dry run equals the live result over the same seq range and stores nothing")
    void dryRunEqualsLive() {
        link(STANDARD, "e1");
        link(STANDARD, "e4");
        event(STANDARD, "e1", "active", "critical", ai.foresignal.intel.SignalFamilies.AIS_BEHAVIOUR, DALLAS, T0);
        event(OTHER, "e2", "active", "warning", "gnss-interference", NEAR_DALLAS, T0.plusSeconds(60));
        event(OTHER, "e3", "active", "warning", "gnss-interference", BRASILIA, T0.plusSeconds(120));
        event(STANDARD, "e4", "emerging", "info", "nav-warning", DENVER, T0.plusSeconds(180));
        event(OTHER, "e5", "emerging", "info", "conflict", BRASILIA, T0.plus(Duration.ofDays(20)));
        long to = maxSeq();

        SituationLinker.DryRun dry = linker.dryRun(0, to, true);
        assertThat(situations.count()).isZero();
        assertThat(members.count()).isZero();
        assertThat(dry.decisions()).extracting(SituationLinker.Decision::action)
                .containsExactly("seed", "join", "seed", "join", "skip");

        link();
        Map<String, String> dryToLive = new HashMap<>();
        for (SituationLinker.Decision decision : dry.decisions()) {
            List<SituationMemberEntity> live = members.findByMemberTypeAndRef(decision.input().memberType(),
                    decision.input().ref());
            if ("skip".equals(decision.action())) {
                assertThat(live).isEmpty();
                continue;
            }
            assertThat(live).hasSize(1);
            SituationMemberEntity member = live.get(0);
            String expectedMethod = "seed".equals(decision.action()) ? "seed" : "linker";
            assertThat(member.getJoinReason()).containsEntry("method", expectedMethod);
            assertThat(member.getJoinReason().get("score")).isEqualTo(decision.score());
            String mapped = dryToLive.putIfAbsent(decision.situationId(), member.getSituationId());
            assertThat(mapped == null || mapped.equals(member.getSituationId())).isTrue();
            if ("seed".equals(decision.action())) {
                assertThat(situations.findById(member.getSituationId()).orElseThrow().getTitle())
                        .isEqualTo(decision.seedTitle());
            }
        }
        assertThat(dryToLive).hasSize(2);
    }
}
