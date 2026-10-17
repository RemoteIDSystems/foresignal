package ai.foresignal.object;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.infra.InfraBuildEntity;
import ai.foresignal.infra.InfraBuildRepository;
import ai.foresignal.infra.InfraEdgeEntity;
import ai.foresignal.infra.InfraEdgeRepository;
import ai.foresignal.infra.InfraNodeEntity;
import ai.foresignal.infra.InfraNodeRepository;
import ai.foresignal.security.Ulids;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.situation.SituationEntity;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/** {@link RelationProjector}: relations materialized from existing structures (prompt 129, invariant OBJ-2). */
@IntegrationTest
@Import(AskTestSources.class)
class RelationProjectorIT extends ObjectItSupport {

    @Autowired InfraBuildRepository builds;
    @Autowired InfraNodeRepository nodes;
    @Autowired InfraEdgeRepository edges;

    static final String Q1 = "record:" + AskTestSources.QUAKES + "/q1";
    static final String Q2 = "record:" + AskTestSources.QUAKES + "/q2";

    private List<RelationEntity> live(String owner) {
        return relationRows.findByOwnerKey(owner).stream().filter(r -> r.getRetiredAt() == null).toList();
    }

    private SituationEntity situationWithMembers(String... externalIds) {
        SituationEntity s = situation("Red Sea shipping disruption", "warning");
        for (String id : externalIds) {
            situations.addMember(s.getId(), JSON.readTree("{\"memberType\":\"record\",\"ref\":\""
                    + AskTestSources.QUAKES + "/" + id + "\",\"seq\":" + headSeq(AskTestSources.QUAKES, id)
                    + ",\"role\":\"core\"}"), "tester", "operator", true);
        }
        return s;
    }

    @Test
    @DisplayName("situation members become member-of; a removed member's relation is retired with its validity closed")
    void situationMembers() {
        quake("q1", "Aden", 4.5, 45.0, 12.8);
        quake("q2", "Hodeidah", 4.6, 42.9, 14.8);
        SituationEntity s = situationWithMembers("q1", "q2");
        String owner = "situation:" + s.getId();
        projector.project(owner);
        List<RelationEntity> rels = live(owner);
        assertThat(rels).extracting(RelationEntity::getFromRef).containsExactlyInAnyOrder(Q1, Q2);
        assertThat(rels).allSatisfy(r -> {
            assertThat(r.getType()).isEqualTo(ObjectTypes.MEMBER_OF);
            assertThat(r.getToRef()).isEqualTo(owner);
            assertThat(r.isProjected()).isTrue();
            assertThat(r.getProvenance()).containsEntry("component", RelationProjector.COMPONENT)
                    .containsEntry("version", RelationProjector.VERSION);
            assertThat(r.getLabel()).isEqualTo("confirmed");
        });

        clock.setInstant(clock.instant().plus(Duration.ofHours(1)));
        String memberId = jdbc.queryForObject("SELECT id FROM situation_member WHERE situation_id = ? AND ref LIKE ?",
                String.class, s.getId(), "%/q2");
        situations.removeMember(s.getId(), memberId, "tester", true);
        projector.project(owner);
        assertThat(live(owner)).extracting(RelationEntity::getFromRef).containsExactly(Q1);
        RelationEntity retired = relationRows.findByOwnerKey(owner).stream().filter(r -> r.getFromRef().equals(Q2))
                .findFirst().orElseThrow();
        assertThat(retired.getRetiredAt()).isEqualTo(clock.instant());
        assertThat(retired.getValidTo()).isEqualTo(clock.instant());
    }

    @Test
    @DisplayName("intel-event members by role: supporting is evidence-for, contradicting evidence-against")
    void intelEventMembers() {
        var e = intelEvent("Port closure", false, "port-closure", "q1|supporting", "q2|contradicting");
        String owner = "intel-event:" + e.getId();
        projector.project(owner);
        Map<String, String> types = live(owner).stream().collect(Collectors.toMap(RelationEntity::getFromRef,
                RelationEntity::getType));
        assertThat(types).containsEntry(Q1, ObjectTypes.EVIDENCE_FOR).containsEntry(Q2, ObjectTypes.EVIDENCE_AGAINST);
        assertThat(live(owner)).allSatisfy(r -> assertThat(r.getConfidence()).isEqualTo(0.7));
    }

    @Test
    @DisplayName("canonical events: observed-by each observation, re-reports of one evidence group same-as the first")
    void canonicalEventReReports() {
        String event = Ulids.next(clock.instant());
        Timestamp now = Timestamp.from(clock.instant());
        jdbc.update("INSERT INTO canonical_event (id, family, event_time, first_observed_at, first_observation_ref,"
                + " observation_count, evidence_group_count, source_ids, status, resolver_version, created_at,"
                + " updated_at) VALUES (?, 'seismic', ?, ?, ?, 2, 1, ARRAY[?], 'open', '1.0.0', ?, ?)", event, now, now,
                Q1, AskTestSources.QUAKES, now, now);
        String group = Ulids.next(clock.instant());
        link(event, "q1", group, clock.instant().minusSeconds(60));
        link(event, "q2", group, clock.instant());
        String owner = "canonical-event:" + event;
        projector.project(owner);
        List<RelationEntity> rels = live(owner);
        assertThat(rels).filteredOn(r -> r.getType().equals(ObjectTypes.OBSERVED_BY))
                .extracting(RelationEntity::getToRef).containsExactlyInAnyOrder(Q1, Q2);
        assertThat(rels).filteredOn(r -> r.getType().equals(ObjectTypes.SAME_AS)).singleElement()
                .satisfies(r -> {
                    assertThat(r.getFromRef()).isEqualTo(Q2);
                    assertThat(r.getToRef()).isEqualTo(Q1);
                });
    }

    @Test
    @DisplayName("entity links are same-as with the link's validity")
    void entityLinks() {
        String entity = Ulids.next(clock.instant());
        Instant from = clock.instant().minus(Duration.ofDays(2));
        Instant to = clock.instant().minus(Duration.ofDays(1));
        jdbc.update("INSERT INTO entity (id, type, provisional, status, created_at, updated_at) VALUES (?, 'vessel',"
                + " false, 'active', ?, ?)", entity, Timestamp.from(from), Timestamp.from(from));
        jdbc.update("INSERT INTO entity_link (entity_id, source_id, external_id, key_type, key_value, confidence,"
                + " valid_from, valid_to) VALUES (?, ?, 'q1', 'mmsi', '244660000', 0.95, ?, ?)", entity,
                AskTestSources.QUAKES, Timestamp.from(from), Timestamp.from(to));
        String owner = "entity:" + entity;
        projector.project(owner);
        RelationEntity r = live(owner).get(0);
        assertThat(r.getType()).isEqualTo(ObjectTypes.SAME_AS);
        assertThat(r.getFromRef()).isEqualTo(Q1);
        assertThat(r.getValidFrom()).isEqualTo(from);
        assertThat(r.getValidTo()).isEqualTo(to);
        assertThat(r.getLabel()).isEqualTo("confirmed");
    }

    @Test
    @DisplayName("104 facility edges become depends-on (dependent to dependency) with their labels")
    void facilityEdges() {
        Instant now = clock.instant();
        InfraBuildEntity build = builds.save(new InfraBuildEntity(now, 2));
        InfraNodeEntity plant = nodes.save(new InfraNodeEntity(build.getId(), AskTestSources.QUAKES, "plant", "power-plant",
                now));
        InfraNodeEntity substation = nodes.save(new InfraNodeEntity(build.getId(), AskTestSources.QUAKES, "sub",
                "substation", now));
        InfraEdgeEntity edge = new InfraEdgeEntity(build.getId(), plant.getId(), substation.getId(), "supplies_power",
                "proximity", "possible", "plant-substation", 1, now);
        edge.setScore(0.4);
        edges.save(edge);
        projector.project(RelationProjector.INFRA_OWNER);
        RelationEntity r = live(RelationProjector.INFRA_OWNER).get(0);
        assertThat(r.getType()).isEqualTo(ObjectTypes.DEPENDS_ON);
        assertThat(r.getFromRef()).isEqualTo("record:" + AskTestSources.QUAKES + "/sub");
        assertThat(r.getToRef()).isEqualTo("record:" + AskTestSources.QUAKES + "/plant");
        assertThat(r.getLabel()).isEqualTo("possible");
        assertThat(r.getConfidence()).isEqualTo(0.4);
    }

    @Test
    @DisplayName("the nightly reconcile repairs a relation deleted by hand and is idempotent")
    void reconcileRepairs() {
        quake("q1", "Aden", 4.5, 45.0, 12.8);
        SituationEntity s = situationWithMembers("q1");
        String owner = "situation:" + s.getId();
        projector.reconcile();
        RelationEntity before = live(owner).get(0);
        relationRows.delete(before);
        assertThat(live(owner)).isEmpty();
        projector.reconcile();
        RelationEntity repaired = live(owner).get(0);
        assertThat(repaired.getProjectionKey()).isEqualTo(before.getProjectionKey());
        long total = relationRows.count();
        projector.reconcile();
        assertThat(relationRows.count()).isEqualTo(total);
    }

    @Test
    @DisplayName("a restarted cursor re-reads the same changes and projects them exactly once")
    void cursorRestartExactlyOnce() {
        quake("q1", "Aden", 4.5, 45.0, 12.8);
        SituationEntity s = situationWithMembers("q1");
        records.ingest("foresignal-intel", NormalizedRecord.builder(s.getId(), "situation")
                .observedAt(clock.instant()).put("title", s.getTitle()).point(43.0, 14.0).build());
        projector.rewind(0);
        assertThat(projector.catchUp()).contains("situation:" + s.getId());
        List<String> ids = live("situation:" + s.getId()).stream().map(RelationEntity::getId).sorted().toList();
        assertThat(ids).hasSize(1);
        long stored = settings.get(SettingKeys.RELATION_CURSOR, Long.class).orElse(0L);
        assertThat(stored).isEqualTo(projector.cursor()).isPositive();
        projector.rewind(0);
        projector.catchUp();
        assertThat(live("situation:" + s.getId()).stream().map(RelationEntity::getId).sorted().toList())
                .isEqualTo(ids);
    }

    private void link(String event, String externalId, String group, Instant observed) {
        jdbc.update("INSERT INTO observation_link (id, canonical_event_id, source_id, external_id, seq, originator_id,"
                + " independence_root, evidence_group_id, link_confidence, link_reason, observed_at, linked_at)"
                + " VALUES (?, ?, ?, ?, 1, 'usgs', 'usgs', ?, 0.9, '{}'::jsonb, ?, ?)", Ulids.next(clock.instant()),
                event, AskTestSources.QUAKES, externalId, group, Timestamp.from(observed), Timestamp.from(observed));
    }
}
