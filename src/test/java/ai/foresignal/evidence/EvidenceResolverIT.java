package ai.foresignal.evidence;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.evidence.EvidenceTestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The evidence resolver end to end (prompt 116, EVD-1), seeded only through {@code TestRecords}: one earthquake
 * reported by USGS, EMSC and GDACS (which republishes USGS) is one canonical event with two evidence groups; ten
 * GDELT reports citing one outlet are one group flagged {@code ECHO}; a rewound cursor processes every version
 * exactly once; and the 099 corroboration and evidence service read the same groups.
 */
@IntegrationTest
@Import(EvidenceTestSources.class)
class EvidenceResolverIT {

    private static final Instant T0 = Instant.parse("2026-03-01T12:00:00Z");

    @Autowired EvidenceResolver resolver;
    @Autowired EvidenceStore store;
    @Autowired EvidenceService evidence;
    @Autowired EvidenceEventBus bus;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void freeze() {
        clock.setInstant(T0.plusSeconds(3_600));
        resolver.resetState();
    }

    private void ingest(String source, String id, String kind, Instant at, double lon, double lat,
            Map<String, Object> payload) {
        NormalizedRecord.Builder builder = NormalizedRecord.builder(id, kind).observedAt(at).point(lon, lat);
        payload.forEach(builder::put);
        records.ingestAt(source, at.plusSeconds(30), builder.build());
    }

    private List<String> openEvents(String family) {
        return jdbc.queryForList("SELECT id FROM canonical_event WHERE family = ? AND status = 'open'", String.class,
                family);
    }

    @Test
    @DisplayName("USGS, EMSC and GDACS report one earthquake: one canonical event, two evidence groups")
    void oneEarthquakeTwoGroups() {
        List<EvidenceEvent> published = new ArrayList<>();
        bus.subscribe(published::add);
        ingest(EvidenceTestSources.USGS, "us7000abcd", "earthquake", T0, 30.00, 38.00, Map.of("mag", 5.6));
        ingest(EvidenceTestSources.EMSC, "20260301_0000123", "earthquake", T0.plusSeconds(8), 30.12, 38.05,
                Map.of("mag", 5.4));
        ingest(EvidenceTestSources.GDACS, "EQ-1234-1", "disaster", T0.plusSeconds(3), 30.05, 37.98,
                Map.of("eventType", "EQ", "severity", 5.6, "eventId", 1234));
        resolver.catchUp();

        List<String> events = openEvents("seismic");
        assertThat(events).hasSize(1);
        EvidenceStore.StoredEvent event = store.event(events.get(0)).orElseThrow();
        assertThat(event.observationCount()).isEqualTo(3);
        assertThat(event.evidenceGroupCount()).isEqualTo(2);
        assertThat(event.qualityFlags()).doesNotContain(QualityFlags.SINGLE_ORIGINATOR);
        List<EvidenceStore.StoredGroup> groups = store.groups(event.id());
        assertThat(groups).extracting(EvidenceStore.StoredGroup::originatorRoot).containsExactlyInAnyOrder("usgs", "emsc");
        assertThat(groups).filteredOn(g -> g.originatorRoot().equals("usgs")).singleElement()
                .extracting(EvidenceStore.StoredGroup::memberCount).isEqualTo(2);
        assertThat(published).extracting(EvidenceEvent::type).contains(EvidenceEvent.CREATED);

        String usgsRef = RecordRef.format(EvidenceTestSources.USGS, "us7000abcd");
        String gdacsRef = RecordRef.format(EvidenceTestSources.GDACS, "EQ-1234-1");
        String emscRef = RecordRef.format(EvidenceTestSources.EMSC, "20260301_0000123");
        assertThat(evidence.canonicalEventOf(usgsRef)).contains(event.id());
        assertThat(evidence.sameGroup(usgsRef, gdacsRef)).isTrue();
        assertThat(evidence.sameGroup(usgsRef, emscRef)).isFalse();
        assertThat(evidence.independentCount(List.of(usgsRef, gdacsRef, emscRef))).isEqualTo(2);
        assertThat(evidence.independentCount(List.of(usgsRef, gdacsRef))).isEqualTo(1);
    }

    @Test
    @DisplayName("ten GDELT reports citing one outlet: one group, ECHO and SINGLE_ORIGINATOR")
    void echoedReports() {
        for (int i = 0; i < 10; i++) {
            ingest(EvidenceTestSources.GDELT, "gdelt-" + i, "conflict-event", T0.plusSeconds(i * 300L), 44.21 + i * 0.001,
                    15.35, Map.of("sourceUrl", "https://www.example-news.org/story-" + i));
        }
        resolver.catchUp();
        List<String> events = openEvents("conflict");
        assertThat(events).hasSize(1);
        EvidenceStore.StoredEvent event = store.event(events.get(0)).orElseThrow();
        assertThat(event.observationCount()).isEqualTo(10);
        assertThat(event.evidenceGroupCount()).isEqualTo(1);
        assertThat(event.qualityFlags()).contains(QualityFlags.ECHO, QualityFlags.SINGLE_ORIGINATOR);
        assertThat(store.groups(event.id())).singleElement().extracting(EvidenceStore.StoredGroup::originatorRoot)
                .isEqualTo("cite:example-news.org");
    }

    @Test
    @DisplayName("a rewound cursor (a restart) links every version exactly once")
    void cursorRestartExactlyOnce() {
        ingest(EvidenceTestSources.USGS, "us1", "earthquake", T0, 30.0, 38.0, Map.of("mag", 5.0));
        ingest(EvidenceTestSources.EMSC, "em1", "earthquake", T0.plusSeconds(5), 30.05, 38.0, Map.of("mag", 5.1));
        resolver.catchUp();
        long cursor = resolver.cursor();
        assertThat(cursor).isPositive();
        Integer links = jdbc.queryForObject("SELECT count(*) FROM observation_link", Integer.class);
        resolver.rewind(0);
        resolver.resetState();
        resolver.catchUp();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM observation_link", Integer.class)).isEqualTo(links);
        assertThat(openEvents("seismic")).hasSize(1);
        assertThat(resolver.cursor()).isEqualTo(cursor);
    }

    @Test
    @DisplayName("a revised location of a linked record updates its link and flags LATE_REVISION")
    void lateRevision() {
        ingest(EvidenceTestSources.USGS, "us2", "earthquake", T0, 30.0, 38.0, Map.of("mag", 5.0));
        ingest(EvidenceTestSources.EMSC, "em2", "earthquake", T0.plusSeconds(5), 30.05, 38.0, Map.of("mag", 5.1));
        resolver.catchUp();
        clock.setInstant(T0.plusSeconds(7_200));
        ingest(EvidenceTestSources.USGS, "us2", "earthquake", T0, 30.2, 38.1, Map.of("mag", 5.2));
        resolver.catchUp();
        EvidenceStore.StoredEvent event = store.event(openEvents("seismic").get(0)).orElseThrow();
        assertThat(event.qualityFlags()).contains(QualityFlags.LATE_REVISION);
        assertThat(event.observationCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("a stop does not wait for a pass in flight, and a stopped resolver's pass stops at once")
    void stopDoesNotWaitForAPass() {
        ingest(EvidenceTestSources.USGS, "us7000stop", "earthquake", T0, 30.00, 38.00, Map.of("mag", 5.1));
        try {
            // A pass in flight holds the monitor for its whole run; the stop must not queue behind it (it did, and
            // shutdown waited for a catch-up of the whole history).
            synchronized (resolver) {
                assertThat(CompletableFuture.runAsync(resolver::destroy)).succeedsWithin(Duration.ofSeconds(5));
            }
            long before = resolver.cursor();
            resolver.catchUp();
            assertThat(resolver.cursor()).isEqualTo(before);
        } finally {
            resolver.start();
        }
        resolver.catchUp();
        assertThat(openEvents("seismic")).isNotEmpty();
    }
}
