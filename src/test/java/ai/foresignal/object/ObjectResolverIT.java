package ai.foresignal.object;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.cycle.AssessmentEntity;
import ai.foresignal.cycle.AssessmentRepository;
import ai.foresignal.forecast.ForecastEntity;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastRepository;
import ai.foresignal.hypothesis.HypothesisSetEntity;
import ai.foresignal.intel.InvestigationEntity;
import ai.foresignal.intel.InvestigationRepository;
import ai.foresignal.place.PlaceService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.Ulids;
import ai.foresignal.situation.SituationEntity;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/** {@link ObjectService} and the per-type resolvers (prompt 129). */
@IntegrationTest
@Import(AskTestSources.class)
class ObjectResolverIT extends ObjectItSupport {

    @Autowired AssessmentRepository assessmentRows;
    @Autowired ForecastRepository forecastRows;
    @Autowired InvestigationRepository investigationRows;
    @Autowired PlaceService places;

    @Test
    @DisplayName("every object type resolves to the uniform envelope")
    void envelopeForEachType() {
        quake("q1", "Near Aden", 5.1, 45.0, 12.8);
        SituationEntity s = situation("Red Sea shipping disruption", "warning");
        var f = finding("Loitering cluster", "cusum", 43.2, 14.9, false, s.getId());
        var e = intelEvent("Port closure", false, "port-closure", "q1");
        HypothesisSetEntity set = hypothesisSet(s.getId());
        ForecastQuestionEntity q = question("Port reopens", set.getId(), s.getId());
        ForecastEntity forecast = new ForecastEntity(Ulids.next(clock.instant()), q.getId(), "ensemble", "ensemble",
                clock.instant(), 0);
        forecast.setProbability(0.62);
        forecast.setKnowledgeCutoff(LocalDate.parse("2026-10-01"));
        forecastRows.save(forecast);
        AssessmentEntity a = new AssessmentEntity(Ulids.next(clock.instant()), "daily",
                clock.instant().minus(Duration.ofDays(1)), clock.instant(), clock.instant());
        a.setStatus("ok");
        a.setHeadline("Shipping stays disrupted");
        assessmentRows.save(a);
        InvestigationEntity inv = investigationRows.save(new InvestigationEntity(Ulids.next(clock.instant()),
                "INV-2026-0001", "Port closure follow-up", clock.instant()));
        places.create(JSON.readTree("{\"id\":\"aden\",\"name\":\"Aden\",\"shape\":\"point\",\"geometry\":"
                + "{\"type\":\"Point\",\"coordinates\":[45.0,12.8]},\"radiusKm\":30}"));
        String entityId = Ulids.next(clock.instant());
        jdbc.update("INSERT INTO entity (id, type, display_name, provisional, status, created_at, updated_at)"
                + " VALUES (?, 'vessel', 'MV Example', false, 'active', ?, ?)", entityId,
                Timestamp.from(clock.instant()), Timestamp.from(clock.instant()));
        String canonical = canonicalEvent("q1");
        ObjectCaller admin = objectCaller("p-admin", Scope.ADMIN, true);
        WorkspaceEntity w = workspaces.create(JSON.readTree("{\"name\":\"Red Sea watch\"}"), admin);
        AnnotationEntity note = annotations.create(ObjectRef.parse("situation:" + s.getId()),
                JSON.readTree("{\"kind\":\"note\",\"body\":\"Watch Hodeidah\"}"), new ResolveContext(admin, null));

        Map<String, String> refs = new LinkedHashMap<>();
        refs.put("record:" + AskTestSources.QUAKES + "/q1", "record");
        refs.put("entity:" + entityId, "entity");
        refs.put("canonical-event:" + canonical, "canonical-event");
        refs.put("intel-event:" + e.getId(), "intel-event");
        refs.put("situation:" + s.getId(), "situation");
        refs.put("finding:" + f.getId(), "finding");
        refs.put("hypothesis-set:" + set.getId(), "hypothesis-set");
        refs.put("hypothesis:" + hypotheses.hypothesesOf(set.getId()).get(0).getId(), "hypothesis");
        refs.put("question:" + q.getId(), "question");
        refs.put("forecast:" + forecast.getId(), "forecast");
        refs.put("assessment:" + a.getId(), "assessment");
        refs.put("investigation:" + inv.getId(), "investigation");
        refs.put("investigation:INV-2026-0001", "investigation");
        refs.put("place:aden", "place");
        refs.put("source:" + AskTestSources.QUAKES, "source");
        refs.put("sensor:source:" + AskTestSources.QUAKES, "sensor");
        refs.put("workspace:" + w.getId(), "workspace");
        refs.put("annotation:" + note.getId(), "annotation");
        ResolveContext ctx = new ResolveContext(admin, null);
        for (Map.Entry<String, String> r : refs.entrySet()) {
            ObjectEnvelope env = objects.get(ObjectRef.parse(r.getKey()), ctx);
            assertThat(env.type()).as(r.getKey()).isEqualTo(r.getValue());
            assertThat(env.title()).as(r.getKey()).isNotBlank();
            assertThat(env.links().get("related")).as(r.getKey()).startsWith("/v1/objects/");
            assertThat(env.summary() == null || env.summary().length() <= 280).as(r.getKey()).isTrue();
        }
        ObjectEnvelope record = objects.get(ObjectRef.parse("record:" + AskTestSources.QUAKES + "/q1"), ctx);
        assertThat(record.geometry()).containsEntry("type", "Point");
        assertThat(record.provenance().sourceIds()).containsExactly(AskTestSources.QUAKES);
        assertThat(record.provenance().asOfSeq()).isEqualTo(headSeq(AskTestSources.QUAKES, "q1"));
        assertThat(objects.get(ObjectRef.parse("investigation:INV-2026-0001"), ctx).ref())
                .isEqualTo("investigation:" + inv.getId());
        assertThat(objects.get(ObjectRef.parse("forecast:" + forecast.getId()), ctx).summary())
                .contains("62%").contains("knowledge cutoff 2026-10-01");
    }

    @Test
    @DisplayName("asOf: a record resolves to the version known then, a situation to its snapshot status")
    void asOfHonored() {
        quake("q1", "Near Aden", 4.0, 45.0, 12.8);
        long first = headSeq(AskTestSources.QUAKES, "q1");
        Instant then = clock.instant();
        clock.setInstant(then.plus(Duration.ofHours(2)));
        quake("q1", "Near Aden (revised)", 5.5, 45.1, 12.9);
        long second = headSeq(AskTestSources.QUAKES, "q1");
        assertThat(second).isGreaterThan(first);

        ObjectRef ref = ObjectRef.parse("record:" + AskTestSources.QUAKES + "/q1");
        assertThat(objects.get(ref, context(Scope.READ, false, null)).provenance().asOfSeq()).isEqualTo(second);
        assertThat(objects.get(ref, context(Scope.READ, false, then.plusSeconds(1))).provenance().asOfSeq())
                .isEqualTo(first);
        assertThat(objects.get(ObjectRef.parse(ref.format() + "@" + first), context(Scope.READ, false, null))
                .provenance().asOfSeq()).isEqualTo(first);

        SituationEntity s = situation("Strait closure", "warning");
        jdbc.update("INSERT INTO situation_snapshot (situation_id, day, status, severity, member_counts, new_members,"
                + " region_codes, key_values, asof_seq, created_at) VALUES (?, ?, 'emerging', 'info', '{}'::jsonb, 0,"
                + " ARRAY[]::text[], '{}'::jsonb, 7, ?)", s.getId(), java.sql.Date.valueOf(LocalDate.parse("2026-10-12")),
                Timestamp.from(clock.instant()));
        ObjectEnvelope atNow = objects.get(ObjectRef.parse("situation:" + s.getId()), context(Scope.READ, false, null));
        assertThat(atNow.status()).isEqualTo("active");
        ObjectEnvelope then2 = objects.get(ObjectRef.parse("situation:" + s.getId()), context(Scope.READ, false,
                clock.instant()));
        assertThat(then2.status()).isEqualTo("emerging");
        assertThat(then2.provenance().asOfSeq()).isEqualTo(7L);
        assertThatThrownBy(() -> objects.get(ObjectRef.parse("situation:" + s.getId()), context(Scope.READ, false,
                s.getCreatedAt().minus(Duration.ofDays(1))))).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("private objects are invisible to callers without private access")
    void privateHidden() {
        secret("s1", "Classified site", 44.0, 13.0);
        var f = finding("Private finding", "cusum", 44.0, 13.0, true, null);
        var e = intelEvent("Private event", true, "r1");
        for (String ref : List.of("record:" + AskTestSources.SECRET + "/s1", "finding:" + f.getId(),
                "intel-event:" + e.getId())) {
            assertThatThrownBy(() -> objects.get(ObjectRef.parse(ref), context(Scope.READ, false, null)))
                    .as(ref).isInstanceOf(NotFoundException.class)
                    .satisfies(x -> assertThat(((NotFoundException) x).getCode()).isEqualTo(ErrorCodes.UNKNOWN_OBJECT));
            ObjectEnvelope visible = objects.get(ObjectRef.parse(ref), context(Scope.AGENT, true, null));
            assertThat(visible.privateObject()).as(ref).isTrue();
        }
    }

    @Test
    @DisplayName("batch resolve lists missing and invisible refs instead of failing")
    void batchResolve() {
        quake("q1", "Near Aden", 5.1, 45.0, 12.8);
        var f = finding("Private finding", "cusum", 44.0, 13.0, true, null);
        ObjectService.Resolved r = objects.resolve(List.of("record:" + AskTestSources.QUAKES + "/q1",
                "situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C", "finding:" + f.getId(),
                "record:" + AskTestSources.QUAKES + "/q1"), context(Scope.READ, false, null));
        assertThat(r.items()).extracting(ObjectEnvelope::ref).containsExactly("record:" + AskTestSources.QUAKES + "/q1");
        assertThat(r.missing()).containsExactly("situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C", "finding:" + f.getId());
        assertThat(r.items().get(0).actions()).extracting(ObjectAction::id).contains("annotate");
        assertThatThrownBy(() -> objects.resolve(List.of("bogus"), context(Scope.READ, false, null)))
                .isInstanceOf(ai.foresignal.api.error.ValidationException.class);
    }

    /** A canonical event observed by one quake record. */
    private String canonicalEvent(String externalId) {
        String id = Ulids.next(clock.instant());
        Timestamp now = Timestamp.from(clock.instant());
        jdbc.update("INSERT INTO canonical_event (id, family, event_time, geom, region_codes, first_observed_at,"
                + " first_observation_ref, observation_count, evidence_group_count, quality_flags, source_ids, status,"
                + " resolver_version, created_at, updated_at) VALUES (?, 'seismic', ?, ST_SetSRID(ST_MakePoint(45.0,"
                + " 12.8), 4326), ARRAY['YE'], ?, ?, 1, 1, ARRAY[]::text[], ARRAY[?], 'open', '1.0.0', ?, ?)",
                id, now, now, "record:" + AskTestSources.QUAKES + "/" + externalId, AskTestSources.QUAKES, now, now);
        return id;
    }
}
