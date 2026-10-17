package ai.foresignal.evidence;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.intel.IntelEventMemberEntity;
import ai.foresignal.intel.IntelEventMemberRepository;
import ai.foresignal.intel.IntelEventService;
import ai.foresignal.intel.IntelligenceEventEntity;
import ai.foresignal.security.Ulids;
import ai.foresignal.source.adapters.derived.CorroborationGenerator;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.evidence.EvidenceTestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * EVD-1 end to end (checkpoint 124, focus area 4): a planted duplicate-echo stream (twenty GDELT reports that all
 * cite one outlet) never raises independence, corroboration or confidence through the real path: the
 * {@code EvidenceResolver} and {@code EvidenceGrouper} (one evidence group), {@code EvidenceService}
 * ({@code independentCount}), the 099 corroboration record ({@code evidenceGroups}, {@code credibility}) and the
 * 101 {@code EventScorer} as {@code IntelEventService.rescore} applies it (confidence and evidence quality). The pure
 * forms are in {@link DuplicateInflationTest}.
 */
@IntegrationTest
@Import(EvidenceTestSources.class)
class DuplicateInflationIT {

    private static final Instant T0 = Instant.parse("2026-03-01T12:00:00Z");

    @Autowired EvidenceResolver resolver;
    @Autowired EvidenceStore store;
    @Autowired EvidenceService evidence;
    @Autowired IntelEventService intelEvents;
    @Autowired IntelEventMemberRepository intelMembers;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void freeze() {
        clock.setInstant(T0.plusSeconds(7_200));
        resolver.resetState();
    }

    /** One GDELT report citing the same outlet. */
    private String echo(int i) {
        String id = "echo-" + i;
        Instant at = T0.plusSeconds(i * 120L);
        records.ingestAt(EvidenceTestSources.GDELT, at.plusSeconds(30), NormalizedRecord.builder(id, "conflict-event")
                .observedAt(at).point(44.21 + i * 0.0005, 15.35).put("sourceUrl",
                        "https://www.example-news.org/story-" + i).build());
        return id;
    }

    /** What independence, corroboration and confidence say about the echoes ingested so far. */
    private record Reading(int groups, int independent, Object credibility, Object evidenceGroups,
            Object originators, double confidence, String label, Object evidenceQuality) {}

    private Reading read(List<String> ids) throws Exception {
        resolver.catchUp();
        List<String> events = jdbc.queryForList("SELECT id FROM canonical_event WHERE family = 'conflict' AND status ="
                + " 'open'", String.class);
        assertThat(events).as("one canonical event").hasSize(1);
        EvidenceStore.StoredEvent event = store.event(events.get(0)).orElseThrow();
        assertThat(event.observationCount()).isEqualTo(ids.size());
        List<String> refs = new ArrayList<>();
        for (String id : ids) {
            refs.add(RecordRef.format(EvidenceTestSources.GDELT, id));
        }
        Map<String, Object> corroboration = corroboration(event);
        IntelligenceEventEntity intel = new IntelligenceEventEntity(Ulids.next(T0), "armed-clash", "Clashes", T0);
        for (String id : ids) {
            long seq = jdbc.queryForObject("SELECT max(seq) FROM record_version WHERE source_id = ? AND external_id = ?",
                    Long.class, EvidenceTestSources.GDELT, id);
            IntelEventMemberEntity m = new IntelEventMemberEntity(intel.getId(), "record", EvidenceTestSources.GDELT,
                    id, seq);
            m.setSignalFamily("conflict");
            m.setWeight(0.7);
            m.setAddedAt(T0);
            m.setAddedBy("test");
            intelMembers.save(m);
        }
        intelMembers.flush();
        intelEvents.rescore(intel);
        return new Reading(event.evidenceGroupCount(), evidence.independentCount(refs), corroboration.get("credibility"),
                corroboration.get("evidenceGroups"), corroboration.get("independentOriginators"), intel.getConfidence(),
                intel.getConfidenceLabel(), intel.getEvidenceQuality());
    }

    /** The 099 corroboration payload of a canonical event, built exactly as the generator builds it. */
    @SuppressWarnings("unchecked")
    private Map<String, Object> corroboration(EvidenceStore.StoredEvent event) throws Exception {
        Method payload = CorroborationGenerator.class.getDeclaredMethod("payload", EvidenceStore.StoredEvent.class,
                List.class, List.class, Predicate.class);
        payload.setAccessible(true);
        Predicate<String> all = source -> true;
        return (Map<String, Object>) payload.invoke(null, event, store.links(event.id()), store.groups(event.id()), all);
    }

    @Test
    @DisplayName("twenty echoes of one outlet raise neither independence, corroboration nor confidence over one report")
    void echoStreamInflatesNothing() throws Exception {
        List<String> ids = new ArrayList<>();
        ids.add(echo(0));
        Reading one = read(ids);
        assertThat(one.groups()).isEqualTo(1);
        assertThat(one.independent()).isEqualTo(1);
        assertThat(one.credibility()).isEqualTo(3);

        jdbc.update("DELETE FROM intelligence_event_member");
        for (int i = 1; i < 20; i++) {
            ids.add(echo(i));
        }
        Reading twenty = read(ids);

        assertThat(twenty.groups()).as("evidence groups").isEqualTo(one.groups());
        assertThat(twenty.independent()).as("independent count").isEqualTo(one.independent());
        assertThat(twenty.credibility()).as("corroboration credibility").isEqualTo(one.credibility());
        assertThat(twenty.evidenceGroups()).as("corroboration groups").isEqualTo(one.evidenceGroups());
        assertThat(twenty.originators()).as("independent originators").isEqualTo(one.originators());
        assertThat(twenty.confidence()).as("confidence").isEqualTo(one.confidence());
        assertThat(twenty.label()).isEqualTo(one.label());
        assertThat(twenty.evidenceQuality()).as("evidence quality").isEqualTo(one.evidenceQuality());
        EvidenceStore.StoredEvent event = store.event(jdbc.queryForObject("SELECT id FROM canonical_event WHERE family ="
                + " 'conflict' AND status = 'open'", String.class)).orElseThrow();
        assertThat(event.qualityFlags()).contains(QualityFlags.ECHO, QualityFlags.SINGLE_ORIGINATOR);
    }
}
