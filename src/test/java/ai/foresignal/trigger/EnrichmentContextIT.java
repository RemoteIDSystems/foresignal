package ai.foresignal.trigger;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK_KIND;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TTL;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TTL_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.search.DocInput;
import ai.foresignal.search.SearchRequest;
import ai.foresignal.search.SearchService;
import ai.foresignal.search.SearchStatusService;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import ai.foresignal.testsupport.enrichment.EnrichmentItSupport;
import ai.foresignal.testsupport.enrichment.EnrichmentTestSources;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;

/**
 * The context of an alert's enrichment (prompt 030), from records seeded through {@code TestRecords}:
 * records of any kind within the radius and window, heartbeats excluded, live-only movement included,
 * the record's version history, the snapshot fallback when the version is gone, nearby alerts, the
 * semantic path, the path for records without geometry, private sources withheld from a non-local
 * provider, the size cap, and contributor sections.
 */
@IntegrationTest
@Import(EnrichmentTestSources.class)
class EnrichmentContextIT extends EnrichmentItSupport {

    private static final Duration WINDOW = Duration.ofMinutes(10);

    private static String id(String sourceId, String externalId) {
        return sourceId + ":" + externalId;
    }

    private static List<String> ids(JsonNode items) {
        List<String> out = new ArrayList<>();
        for (JsonNode item : items) {
            out.add(item.path("id").asString(item.path("docId").asString("")));
        }
        return out;
    }

    private static JsonNode item(JsonNode items, String id) {
        for (JsonNode item : items) {
            if (id.equals(item.path("id").asString(""))) {
                return item;
            }
        }
        throw new AssertionError("no item " + id + " in " + items);
    }

    /** The alerted quake with two earlier versions; returns the rule. */
    private UUID mainQuake() {
        UUID rule = enrichRule("Context", 50, "10m");
        quakeAt("q-main", "Tokyo Bay", 4.0, minutesAgo(40), LON, LAT);
        quakeAt("q-main", "Tokyo Bay", 4.5, minutesAgo(20), LON, LAT);
        return rule;
    }

    private void fireMain() {
        quakeAt("q-main", "Tokyo Bay", 5.5, T, LON, LAT);
    }

    private void vesselNear() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("name", "Harbor vessel");
        recordAt(STANDARD, STANDARD_KIND, "v-near", minutesAgo(5), LON + 0.05, LAT + 0.02, fields);
    }

    @Test
    @DisplayName("nearby records of any kind in radius and window, no heartbeats, live-only movement, history, nearby alerts")
    void radiusWindowKindsHistoryAlerts() {
        UUID rule = mainQuake();
        vesselNear();
        quakeAt("q-far", "Sendai", 4.2, minutesAgo(5), 141.0, 38.3);
        quakeAt("q-old", "Chiba", 4.1, minutesAgo(30), LON + 0.1, LAT);

        // A heartbeat is the only version of "hb" in the window; its live position then moves far away.
        Map<String, Object> mag = Map.of("mag", 5);
        recordAt(TTL, TTL_KIND, "hb", minutesAgo(15), LON + 0.05, LAT, mag);
        IngestOutcome heartbeat = records.ingestAt(TTL, minutesAgo(15).plusSeconds(580),
                NormalizedRecord.builder("hb", TTL_KIND).observedAt(minutesAgo(15).plusSeconds(580)).put("mag", 5)
                        .point(LON + 0.05, LAT).build());
        assertThat(heartbeat.persisted(PersistReason.HEARTBEAT)).isEqualTo(1);
        recordAt(TTL, TTL_KIND, "hb", minutesAgo(1), 145.0, 40.0, mag);

        // "ac" was versioned before the window and has since moved 450 m inside it without a new version.
        Map<String, Object> aircraft = new LinkedHashMap<>();
        aircraft.put("callsign", "ABC123");
        aircraft.put("squawk", "1200");
        aircraft.put("alt", 10000);
        aircraft.put("gs", 0);
        aircraft.put("track", 90);
        recordAt(TRACK, TRACK_KIND, "ac", t(-630), LON + 0.1, LAT, aircraft);
        IngestOutcome moved = records.ingestAt(TRACK, t(-570), withFields(NormalizedRecord.builder("ac", TRACK_KIND)
                .observedAt(t(-570)).point(LON + 0.1, LAT + 0.004), aircraft).build());
        assertThat(moved.persisted()).as("live-only movement").isZero();

        quakeAt("q-other", "Yokohama", 6.0, minutesAgo(5), LON - 0.1, LAT - 0.1);
        fireMain();
        AlertView alert = alertFor(rule, "q-main");
        AlertView other = alertFor(rule, "q-other");

        EnrichmentContext context = contexts.build(alert, 50, WINDOW, remote());
        JsonNode data = JSON.readTree(context.json());

        assertThat(context.json().getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(24 * 1024);
        assertThat(context.anchor().spatial()).isTrue();
        assertThat(context.anchor().time()).isEqualTo(T);
        assertThat(data.get("alert").get("cite").asString()).isEqualTo("[alert:" + alert.id() + "]");
        assertThat(data.get("record").get("from").asString()).isEqualTo("version");
        assertThat(data.get("record").get("seq").asLong()).isEqualTo(alert.seq());

        JsonNode history = data.get("history");
        assertThat(history).hasSize(2);
        assertThat(history.get(0).get("seq").asLong()).isGreaterThan(history.get(1).get("seq").asLong());
        assertThat(history.get(0).get("fields").get("mag").asDouble()).isEqualTo(4.5);
        assertThat(history.get(0).has("persistReason")).isTrue();
        assertThat(history.get(0).get("cite").asString()).startsWith("[" + id(AskTestSources.QUAKES, "q-main") + "@");

        JsonNode nearby = data.get("nearby").get("records");
        assertThat(ids(nearby))
                .contains(id(STANDARD, "v-near"), id(TRACK, "ac"), id(AskTestSources.QUAKES, "q-other"))
                .doesNotContain(id(AskTestSources.QUAKES, "q-far"), id(AskTestSources.QUAKES, "q-old"), id(TTL, "hb"),
                        id(AskTestSources.QUAKES, "q-main"));
        assertThat(item(nearby, id(TRACK, "ac")).get("liveOnly").asBoolean()).isTrue();
        assertThat(item(nearby, id(STANDARD, "v-near")).get("kind").asString()).isEqualTo(STANDARD_KIND);

        JsonNode alerts = data.get("nearby").get("alerts");
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).get("id").asLong()).isEqualTo(other.id());
        assertThat(alerts.get(0).get("state").asString()).isEqualTo("firing");

        assertThat(context.citable().hasAlert(Long.toString(alert.id()))).isTrue();
        assertThat(context.citable().hasAlert(Long.toString(other.id()))).isTrue();
        assertThat(context.citable().hasRecord(id(AskTestSources.QUAKES, "q-main"), alert.seq())).isTrue();
        assertThat(context.citable().hasRecord(id(STANDARD, "v-near"), headSeq(STANDARD, "v-near"))).isTrue();
        assertThat(context.citable().hasRecord(id(TRACK, "ac"), headSeq(TRACK, "ac"))).isTrue();
        assertThat(context.notes()).anyMatch(n -> n.contains("semantic search is not available"));
        assertThat(context.counts().history()).isEqualTo(2);
        assertThat(context.counts().nearbyAlerts()).isEqualTo(1);
        assertThat(context.trimmed()).isZero();
    }

    @Test
    @DisplayName("when retention removed the alerted version, the record and anchor come from the alert's snapshot")
    void snapshotFallback() {
        UUID rule = mainQuake();
        vesselNear();
        fireMain();
        AlertView a = alertFor(rule, "q-main");
        long gone = 9_000_000_000L;
        AlertView dropped = new AlertView(a.id(), a.ruleId(), a.ruleName(), gone, a.sourceId(), a.externalId(), a.kind(),
                a.firedAt(), a.observedAt(), a.message(), a.severity(), a.state(), a.rev(), null, null, null, null, null,
                null, null, null, null, false, null, a.tags(), a.recordSnapshot());

        EnrichmentContext context = contexts.build(dropped, 50, WINDOW, remote());
        JsonNode data = JSON.readTree(context.json());

        JsonNode record = data.get("record");
        assertThat(record.get("from").asString()).isEqualTo("snapshot");
        assertThat(record.get("seq").asLong()).isEqualTo(gone);
        assertThat(record.get("lon").asDouble()).isEqualTo(LON);
        assertThat(record.get("fields").get("mag").asDouble()).isEqualTo(5.5);
        assertThat(context.anchor().spatial()).as("the snapshot geometry anchors the spatial parts").isTrue();
        assertThat(ids(data.get("nearby").get("records"))).contains(id(STANDARD, "v-near"));
        assertThat(context.citable().hasRecord(id(AskTestSources.QUAKES, "q-main"), gone)).isTrue();
    }

    @Test
    @DisplayName("a record without geometry skips the spatial parts and keeps its history")
    void withoutGeometry() {
        UUID rule = enrichRule("No geometry", 50, "10m");
        vesselNear();
        quakeAt("q-nogeo", "Somewhere", 4.0, minutesAgo(3), null, null);
        quakeAt("q-nogeo", "Somewhere", 5.6, T, null, null);
        AlertView alert = alertFor(rule, "q-nogeo");

        EnrichmentContext context = contexts.build(alert, 50, WINDOW, remote());
        JsonNode data = JSON.readTree(context.json());

        assertThat(context.anchor().spatial()).isFalse();
        assertThat(data.get("nearby").get("records")).isEmpty();
        assertThat(data.get("nearby").get("alerts")).isEmpty();
        assertThat(data.get("history")).hasSize(1);
        assertThat(context.notes()).anyMatch(n -> n.contains("no geometry"));
    }

    @Test
    @DisplayName("private-source records are withheld from a non-local provider and sent to a local one")
    void privateWithheld() {
        UUID rule = mainQuake();
        records.ingestAt(AskTestSources.SECRET, minutesAgo(3), NormalizedRecord.builder("home", AskTestSources.QUAKE_KIND)
                .observedAt(minutesAgo(3)).put("place", "Family home").put("mag", 7.0).point(LON + 0.01, LAT).build());
        fireMain();
        AlertView alert = alertFor(rule, "q-main");

        EnrichmentContext remote = contexts.build(alert, 50, WINDOW, remote());
        assertThat(remote.json()).doesNotContain(AskTestSources.SECRET).doesNotContain("Family home");
        assertThat(remote.citable().latestSeq(id(AskTestSources.SECRET, "home"))).isEmpty();

        EnrichmentContext local = contexts.build(alert, 50, WINDOW, local());
        assertThat(ids(JSON.readTree(local.json()).get("nearby").get("records")))
                .contains(id(AskTestSources.SECRET, "home"));
    }

    @Test
    @DisplayName("the serialized context respects the cap, trimming the lowest-ranked items first; trimmed items are not citable")
    void sizeCap() {
        UUID rule = mainQuake();
        for (int i = 0; i < 30; i++) {
            Map<String, Object> fields = new LinkedHashMap<>();
            fields.put("name", "Vessel " + i + " " + "x".repeat(150));
            fields.put("note", "y".repeat(150));
            recordAt(STANDARD, STANDARD_KIND, "v" + i, minutesAgo(9).plusSeconds(i), LON + 0.001 * i, LAT, fields);
        }
        fireMain();
        AlertView alert = alertFor(rule, "q-main");
        int cap = 4096;
        EnrichmentProperties small = new EnrichmentProperties(false, 256, 4, Duration.ofSeconds(20),
                Duration.ofMinutes(10), cap, 600, Duration.ofHours(1));
        EnrichmentContextBuilder builder = new EnrichmentContextBuilder(namedJdbc, recordQueries, recordMapper, shaper,
                alertStore, search, searchStatus, egress, contributors, small, JSON);

        EnrichmentContext context = builder.build(alert, 50, WINDOW, remote());
        JsonNode data = JSON.readTree(context.json());

        assertThat(context.json().getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(cap);
        assertThat(context.trimmed()).isPositive();
        assertThat(data.get("history")).as("history outranks nearby records").hasSize(2);
        assertThat(data.get("nearby").get("records").size()).isLessThan(30);
        assertThat(data.get("notes").toString()).contains("left out");
        List<String> kept = ids(data.get("nearby").get("records"));
        for (int i = 0; i < 30; i++) {
            String id = id(STANDARD, "v" + i);
            assertThat(context.citable().latestSeq(id).isPresent()).as(id).isEqualTo(kept.contains(id));
        }
        // The newest records are kept: the oldest go first.
        assertThat(kept.getFirst()).isEqualTo(id(STANDARD, "v29"));
    }

    @Test
    @DisplayName("semantic path: hybrid matches for the message within radius and window, filtered and capped")
    void semanticPath() {
        UUID rule = mainQuake();
        quakeAt("q-old", "Chiba", 4.1, minutesAgo(30), LON + 0.1, LAT);
        quakeAt("q-far", "Sendai", 4.2, minutesAgo(5), 141.0, 38.3);
        records.ingestAt(AskTestSources.SECRET, minutesAgo(3), NormalizedRecord.builder("home", AskTestSources.QUAKE_KIND)
                .observedAt(minutesAgo(3)).put("place", "Family home").put("mag", 7.0).point(LON + 0.01, LAT).build());
        fireMain();
        AlertView alert = alertFor(rule, "q-main");

        SearchStatusService status = mock(SearchStatusService.class);
        when(status.availability()).thenReturn(new SearchStatusService.Availability(true, null, null));
        SearchService searchMock = mock(SearchService.class);
        List<Map<String, Object>> items = List.of(
                hit(AskTestSources.QUAKES, "q-main", alert.seq(), "Tokyo Bay"),
                hit(AskTestSources.QUAKES, "q-old", headSeq(AskTestSources.QUAKES, "q-old"), "Chiba"),
                hit(AskTestSources.QUAKES, "q-far", headSeq(AskTestSources.QUAKES, "q-far"), "Sendai"),
                hit(AskTestSources.SECRET, "home", headSeq(AskTestSources.SECRET, "home"), "Family home"));
        when(searchMock.search(any(SearchRequest.class), anySet())).thenReturn(Map.of("items", items));
        EnrichmentContextBuilder builder = new EnrichmentContextBuilder(namedJdbc, recordQueries, recordMapper, shaper,
                alertStore, searchMock, status, egress, contributors, enrichmentProperties, JSON);

        EnrichmentContext context = builder.build(alert, 50, WINDOW, remote());
        JsonNode related = JSON.readTree(context.json()).get("related");

        assertThat(ids(related)).containsExactly(id(AskTestSources.QUAKES, "q-old"));
        assertThat(related.get(0).get("cite").asString())
                .isEqualTo("[" + id(AskTestSources.QUAKES, "q-old") + "@" + headSeq(AskTestSources.QUAKES, "q-old") + "]");
        assertThat(context.citable().hasRecord(id(AskTestSources.QUAKES, "q-old"),
                headSeq(AskTestSources.QUAKES, "q-old"))).isTrue();
        ArgumentCaptor<SearchRequest> request = ArgumentCaptor.forClass(SearchRequest.class);
        verify(searchMock).search(request.capture(), anySet());
        SearchRequest sent = request.getValue();
        assertThat(sent.mode()).isEqualTo(SearchRequest.Mode.HYBRID);
        assertThat(sent.docTypes()).containsExactly(DocInput.RECORD);
        assertThat(sent.q()).isEqualTo(alert.message());
        assertThat(sent.bbox()).isNotNull();
        assertThat(sent.bbox().contains(LON, LAT)).isTrue();
        assertThat(sent.since()).isEqualTo(T.minus(WINDOW));
        assertThat(sent.until()).isAfter(T.plus(WINDOW));
        assertThat(sent.observedAxis()).isTrue();
        assertThat(sent.limit()).isEqualTo(20);
    }

    @Test
    @DisplayName("a contributor's section appears under its key; a failing contributor is skipped")
    void contributorSection() {
        UUID rule = mainQuake();
        fireMain();
        AlertView alert = alertFor(rule, "q-main");

        weather.enabled.set(true);
        EnrichmentContext context = contexts.build(alert, 50, WINDOW, remote());
        JsonNode section = JSON.readTree(context.json()).get("sections").get(EnrichmentTestSources.WEATHER_KEY);
        assertThat(section.get("summary").asString()).isEqualTo("Heavy rain near alert " + alert.id());
        assertThat(context.counts().sections()).isEqualTo(1);

        weather.failing.set(true);
        EnrichmentContext failing = contexts.build(alert, 50, WINDOW, remote());
        assertThat(JSON.readTree(failing.json()).get("sections").has(EnrichmentTestSources.WEATHER_KEY)).isFalse();
    }

    @Test
    @DisplayName("the envelope prefilter grows with latitude and is skipped near the poles and across the antimeridian")
    void envelope() {
        var e = EnrichmentContextBuilder.envelope(new org.locationtech.jts.geom.Envelope(LON, LON, LAT, LAT), 50_000);
        assertThat(e).isPresent();
        assertThat(e.get().getMaxY() - LAT).isGreaterThan(0.449).isLessThan(0.46);
        assertThat(e.get().getMaxX() - LON).isGreaterThan(e.get().getMaxY() - LAT);
        assertThat(EnrichmentContextBuilder.envelope(new org.locationtech.jts.geom.Envelope(179.9, 179.9, 0, 0), 50_000))
                .isEmpty();
        assertThat(EnrichmentContextBuilder.envelope(new org.locationtech.jts.geom.Envelope(0, 0, 88.9, 88.9), 50_000))
                .isEmpty();
    }

    private static NormalizedRecord.Builder withFields(NormalizedRecord.Builder b, Map<String, Object> fields) {
        fields.forEach(b::put);
        return b;
    }

    private static Map<String, Object> hit(String source, String externalId, long seq, String title) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("docType", DocInput.RECORD);
        item.put("docId", source + ":" + externalId);
        item.put("score", 0.5);
        item.put("title", title);
        item.put("snippet", title);
        item.put("source", source);
        item.put("kind", AskTestSources.QUAKE_KIND);
        item.put("seq", seq);
        item.put("observedAt", Instant.parse("2026-10-12T11:30:00Z"));
        return item;
    }
}
