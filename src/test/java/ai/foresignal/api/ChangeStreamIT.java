package ai.foresignal.api;

import static ai.foresignal.testsupport.ingest.TestIngestSources.LATEST;
import static ai.foresignal.testsupport.ingest.TestIngestSources.LATEST_KIND;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TTL;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TTL_KIND;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stream.sse.ChangesTopic;
import ai.foresignal.stream.sse.SseClient;
import ai.foresignal.stream.sse.SseHub;
import ai.foresignal.stream.sse.StreamLimits;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.records.RecordsTestSources;
import ai.foresignal.testsupport.records.RecordsTestSources.StubRetentionState;
import ai.foresignal.testsupport.stream.SseTestClient;
import ai.foresignal.testsupport.stream.SseTestClient.Event;
import ai.foresignal.testsupport.stream.StreamTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code GET /v1/changes/stream} over a real socket (prompt 015). Records are seeded only through {@link TestRecords}. */
@WebIntegrationTest
@Import(StreamTestSources.class)
class ChangeStreamIT {

    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");
    static final Duration WAIT = Duration.ofSeconds(5);
    static final Duration NFR3 = Duration.ofSeconds(2);
    static final JsonMapper JSON = JsonMapper.builder().build();

    @LocalServerPort int port;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired StubRetentionState retention;
    @Autowired SseHub hub;
    @Autowired ChangesTopic changesTopic;
    @Autowired AppSettingService settings;
    @Autowired AdminTokenService adminTokens;

    private final List<SseTestClient> streams = new ArrayList<>();
    private Instant now = T;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        retention.reset();
        settings.invalidateCache();
    }

    @AfterEach
    void tearDown() {
        streams.forEach(SseTestClient::close);
        hub.clients().forEach(SseClient::close);
        retention.reset();
    }

    private SseTestClient open(String query) {
        return open(query, Map.of());
    }

    private SseTestClient open(String query, Map<String, String> headers) {
        SseTestClient stream = SseTestClient.connect("http://localhost:" + port, "/v1/changes/stream" + query, headers);
        streams.add(stream);
        return stream;
    }

    private void ingest(String source, NormalizedRecord... batch) {
        now = now.plusSeconds(10);
        clock.setInstant(now);
        records.ingestAt(source, now, batch);
    }

    private NormalizedRecord.Builder rec(String id, String kind) {
        return NormalizedRecord.builder(id, kind).observedAt(now.plusSeconds(10));
    }

    private static JsonNode data(Event event) {
        return JSON.readTree(event.data());
    }

    private void limits(StreamLimits limits) {
        settings.put(SettingKeys.STREAM_LIMITS, limits);
    }

    @Test
    @DisplayName("responses are text/event-stream with no-cache and X-Accel-Buffering: no")
    void headers() {
        SseTestClient stream = open("");
        assertThat(stream.status()).isEqualTo(200);
        assertThat(stream.header("Content-Type")).startsWith("text/event-stream");
        assertThat(stream.header("Cache-Control")).isEqualTo("no-cache");
        assertThat(stream.header("X-Accel-Buffering")).isEqualTo("no");
        assertThat(stream.header("Content-Encoding")).isNull();
    }

    @Test
    @DisplayName("a change arrives within 2 s of ingest with id: <seq> and the Change fields")
    void changeArrivesQuickly() {
        SseTestClient stream = open("?source=" + STANDARD);
        long start = System.nanoTime();
        ingest(STANDARD, rec("a", STANDARD_KIND).point(5, 6).put("mag", 1).build());
        Event change = stream.next("change", NFR3);
        assertThat(change).isNotNull();
        assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThanOrEqualTo(NFR3);
        JsonNode json = data(change);
        assertThat(change.id()).isEqualTo(json.get("seq").asString());
        assertThat(json.get("source").asString()).isEqualTo(STANDARD);
        assertThat(json.get("externalId").asString()).isEqualTo("a");
        assertThat(json.get("persistReason").asString()).isEqualTo("FIRST");
        assertThat(json.get("historyTier").asString()).isEqualTo("STANDARD");
        assertThat(json.get("prevSeq").isNull()).isTrue();
        assertThat(json.get("geometry").get("type").asString()).isEqualTo("Point");
        assertThat(json.get("bbox").get(0).asDouble()).isEqualTo(5.0);
        assertThat(json.get("leftFilter").asBoolean()).isFalse();
        assertThat(json.has("payload")).isFalse();
    }

    @Test
    @DisplayName("source, kind, externalId and bbox filter the stream")
    void filters() {
        SseTestClient bySource = open("?source=" + TTL);
        SseTestClient byKind = open("?kind=" + STANDARD_KIND);
        SseTestClient byId = open("?externalId=wanted");
        SseTestClient byBox = open("?bbox=0,0,10,10");
        ingest(STANDARD, rec("wanted", STANDARD_KIND).point(50, 50).build(), rec("boxed", STANDARD_KIND).point(5, 5).build());
        ingest(TTL, rec("t", TTL_KIND).point(60, 60).build());

        assertThat(bySource.collect("change", Duration.ofSeconds(2))).extracting(e -> data(e).get("externalId").asString())
                .containsExactly("t");
        // Within one batch the writer assigns seqs after a stable sort by externalId
        // (Core-Data-Model §6.2 step 3), so "boxed" precedes "wanted" whatever the submission order.
        assertThat(byKind.collect("change", Duration.ofSeconds(1))).extracting(e -> data(e).get("externalId").asString())
                .containsExactly("boxed", "wanted");
        assertThat(byId.collect("change", Duration.ofSeconds(1))).extracting(e -> data(e).get("externalId").asString())
                .containsExactly("wanted");
        assertThat(byBox.collect("change", Duration.ofSeconds(1))).extracting(e -> data(e).get("externalId").asString())
                .containsExactly("boxed");
    }

    @Test
    @DisplayName("a tombstone is delivered")
    void tombstone() {
        SseTestClient stream = open("?source=" + STANDARD);
        ingest(STANDARD, rec("gone", STANDARD_KIND).point(5, 5).build());
        assertThat(stream.next("change", WAIT)).isNotNull();
        ingest(STANDARD, rec("gone", STANDARD_KIND).tombstone(true).build());
        Event tombstone = stream.next("change", WAIT);
        assertThat(tombstone).isNotNull();
        assertThat(data(tombstone).get("tombstone").asBoolean()).isTrue();
        assertThat(data(tombstone).get("persistReason").asString()).isEqualTo("TOMBSTONE");
    }

    @Test
    @DisplayName("ghost case: with live=false an entity leaving the box arrives once with leftFilter via its persisted box")
    void ghostLeavesViaPersistedBox() {
        SseTestClient stream = open("?bbox=0,0,10,10&live=false");
        ingest(STANDARD, rec("ghost", STANDARD_KIND).point(5, 5).put("v", 1).build());
        Event inside = stream.next("change", WAIT);
        assertThat(data(inside).get("leftFilter").asBoolean()).isFalse();

        ingest(STANDARD, rec("ghost", STANDARD_KIND).point(50, 50).put("v", 2).build());
        ingest(STANDARD, rec("ghost", STANDARD_KIND).point(60, 60).put("v", 3).build());
        List<Event> after = stream.collect("change", Duration.ofSeconds(2));
        assertThat(after).hasSize(1);
        JsonNode left = data(after.getFirst());
        assertThat(left.get("leftFilter").asBoolean()).isTrue();
        assertThat(left.get("prevPersistedBbox").get(0).asDouble()).isEqualTo(5.0);
        assertThat(left.get("bbox").get(0).asDouble()).isEqualTo(50.0);
    }

    @Test
    @DisplayName("Last-Event-ID replays exactly the missed changes, then the stream is live without duplicates")
    void resumeWithLastEventId() {
        SseTestClient first = open("?source=" + STANDARD);
        ingest(STANDARD, rec("a", STANDARD_KIND).build());
        ingest(STANDARD, rec("b", STANDARD_KIND).build());
        ingest(STANDARD, rec("c", STANDARD_KIND).build());
        List<Event> all = first.collect("change", Duration.ofSeconds(2));
        assertThat(all).hasSize(3);
        first.close();

        SseTestClient resumed = open("?source=" + STANDARD, Map.of("Last-Event-ID", all.getFirst().id()));
        List<Event> replayed = resumed.collect("change", Duration.ofSeconds(2));
        assertThat(replayed).extracting(Event::id).containsExactly(all.get(1).id(), all.get(2).id());
        ingest(STANDARD, rec("d", STANDARD_KIND).build());
        List<Event> live = resumed.collect("change", Duration.ofSeconds(2));
        assertThat(live).extracting(e -> data(e).get("externalId").asString()).containsExactly("d");

        SseTestClient byAfter = open("?source=" + STANDARD + "&after=" + all.get(1).id());
        assertThat(byAfter.collect("change", Duration.ofSeconds(2))).extracting(e -> data(e).get("externalId").asString())
                .containsExactly("c", "d");
    }

    @Test
    @DisplayName("payload=true carries payloads; raster-frame changes carry theirs without it")
    void payloads() {
        SseTestClient withPayload = open("?source=" + STANDARD + "&payload=true");
        SseTestClient without = open("?source=" + STANDARD);
        SseTestClient raster = open("?source=" + RecordsTestSources.RASTER);
        ingest(STANDARD, rec("p", STANDARD_KIND).put("mag", 4.5).build());
        ingest(RecordsTestSources.RASTER, rec("mrms-reflectivity", "raster-frame").put("frameKey", "20260301T000000Z").build());

        assertThat(data(withPayload.next("change", WAIT)).get("payload").get("mag").asDouble()).isEqualTo(4.5);
        assertThat(data(without.next("change", WAIT)).has("payload")).isFalse();
        assertThat(data(raster.next("change", WAIT)).get("payload").get("frameKey").asString())
                .isEqualTo("20260301T000000Z");

        // Replayed changes follow the same rule.
        SseTestClient replay = open("?source=" + STANDARD + "&payload=true&after=0");
        assertThat(data(replay.next("change", WAIT)).get("payload").get("mag").asDouble()).isEqualTo(4.5);
    }

    @Test
    @DisplayName("past replayCap the stream sends reset {replay_cap, resumeAfter} and ends")
    void replayCap() {
        ingest(STANDARD, rec("a", STANDARD_KIND).build(), rec("b", STANDARD_KIND).build(), rec("c", STANDARD_KIND).build());
        limits(new StreamLimits(0, 0, 2, 0, 0, 0, 0, 0));
        SseTestClient stream = open("?source=" + STANDARD + "&after=0");
        Event one = stream.next("change", WAIT);
        Event two = stream.next("change", WAIT);
        Event reset = stream.next("reset", WAIT);
        assertThat(one).isNotNull();
        assertThat(two).isNotNull();
        assertThat(reset).isNotNull();
        assertThat(reset.id()).isNull();
        assertThat(data(reset).get("reason").asString()).isEqualTo("replay_cap");
        assertThat(data(reset).get("resumeAfter").asString()).isEqualTo(two.id());
        assertThat(stream.awaitEnd(WAIT)).isTrue();
    }

    @Test
    @DisplayName("a resume below the floor sends reset {resync_required} at once")
    void resumeBelowFloor() {
        ingest(STANDARD, rec("a", STANDARD_KIND).build());
        retention.setPurgeSeq(STANDARD, 1_000);
        SseTestClient purged = open("?source=" + STANDARD + "&after=1");
        Event reset = purged.next("reset", WAIT);
        assertThat(reset).isNotNull();
        assertThat(data(reset).get("reason").asString()).isEqualTo("resync_required");
        assertThat(data(reset).get("resumeAfter").isNull()).isTrue();
        assertThat(data(reset).get("watermarkSeq").asLong()).isPositive();
        assertThat(purged.awaitEnd(WAIT)).isTrue();

        retention.reset();
        retention.setLowestRetainedSeq(500);
        SseTestClient floor = open("?after=2");
        assertThat(data(floor.next("reset", WAIT)).get("reason").asString()).isEqualTo("resync_required");

        // Live from now never checks the floor.
        SseTestClient live = open("");
        ingest(STANDARD, rec("b", STANDARD_KIND).build());
        assertThat(live.next("change", WAIT)).isNotNull();
    }

    @Test
    @DisplayName("onGap replays the FIFO lane and sends resync {gap} to live=true clients only")
    void gapResyncs() {
        SseTestClient live = open("?source=" + STANDARD);
        SseTestClient noLive = open("?source=" + STANDARD + "&live=false");
        ingest(STANDARD, rec("a", STANDARD_KIND).build());
        Event first = live.next("change", WAIT);
        assertThat(noLive.next("change", WAIT)).isNotNull();

        changesTopic.onGap(Long.parseLong(first.id()));
        Event resync = live.next("resync", WAIT);
        assertThat(resync).isNotNull();
        assertThat(resync.id()).isNull();
        assertThat(data(resync).get("reason").asString()).isEqualTo("gap");
        assertThat(data(resync).get("watermarkSeq").asLong()).isGreaterThanOrEqualTo(Long.parseLong(first.id()));
        assertThat(noLive.collect("resync", Duration.ofSeconds(1))).isEmpty();

        // The replay found nothing new, and later changes still flow without duplicates.
        ingest(STANDARD, rec("b", STANDARD_KIND).build());
        assertThat(live.collect("change", Duration.ofSeconds(2))).extracting(e -> data(e).get("externalId").asString())
                .containsExactly("b");
    }

    @Test
    @DisplayName("a LiveBus drop sends resync {live_dropped} to live=true clients")
    void liveDroppedResyncs() {
        SseTestClient live = open("");
        SseTestClient noLive = open("?live=false");
        assertThat(hub.clients()).hasSize(2);
        changesTopic.onLiveDropped();
        Event resync = live.next("resync", WAIT);
        assertThat(resync).isNotNull();
        assertThat(data(resync).get("reason").asString()).isEqualTo("live_dropped");
        assertThat(noLive.collect("resync", Duration.ofSeconds(1))).isEmpty();
    }

    @Test
    @DisplayName("a live-only update arrives as a live event without an id")
    void liveEvents() {
        SseTestClient stream = open("?source=" + LATEST);
        ingest(LATEST, rec("m", LATEST_KIND).point(1, 1).put("name", "a").put("x", 1).build());
        assertThat(stream.next("change", WAIT)).isNotNull();
        ingest(LATEST, rec("m", LATEST_KIND).point(1, 1).put("name", "a").put("x", 2).build());
        Event live = stream.next("live", WAIT);
        assertThat(live).isNotNull();
        assertThat(live.id()).isNull();
        JsonNode json = data(live);
        assertThat(json.get("sourceId").asString()).isEqualTo(LATEST);
        assertThat(json.get("externalId").asString()).isEqualTo("m");
        assertThat(json.get("version").asInt()).isEqualTo(1);
        assertThat(json.get("leftFilter").asBoolean()).isFalse();

        SseTestClient noLive = open("?source=" + LATEST + "&live=false");
        ingest(LATEST, rec("m", LATEST_KIND).point(1, 1).put("name", "a").put("x", 3).build());
        assertThat(noLive.collect("live", Duration.ofSeconds(2))).isEmpty();
    }

    @Test
    @DisplayName("beyond maxClients a connect is 503 STREAM_LIMIT")
    void maxClients() {
        limits(new StreamLimits(1, 0, 0, 0, 0, 0, 0, 0));
        assertThat(open("").status()).isEqualTo(200);
        SseTestClient refused = open("");
        assertThat(refused.status()).isEqualTo(503);
        // Prompt 022 (015 Issue 4): an error to an Accept: text/event-stream connect is still a
        // problem document, not an empty or event-stream body.
        assertThat(refused.header("Content-Type")).startsWith("application/problem+json");
    }

    @Test
    @DisplayName("beyond maxStreamsPerToken (or per anonymous address) a connect is 429 with Retry-After: 5")
    void perPrincipalLimits() {
        limits(new StreamLimits(0, 0, 0, 0, 0, 0, 1, 1));
        String token = adminTokens.rotate();
        Map<String, String> auth = Map.of("Authorization", "Bearer " + token);
        assertThat(open("", auth).status()).isEqualTo(200);
        SseTestClient refused = open("", auth);
        assertThat(refused.status()).isEqualTo(429);
        assertThat(refused.header("Retry-After")).isEqualTo("5");
        assertThat(refused.header("Content-Type")).startsWith("application/problem+json");

        assertThat(open("").status()).isEqualTo(200);
        assertThat(open("").status()).isEqualTo(429);
    }

    @Test
    @DisplayName("a malformed Last-Event-ID is 400 and an unknown source is 404")
    void badRequests() {
        SseTestClient malformed = open("", Map.of("Last-Event-ID", "c=1"));
        assertThat(malformed.status()).isEqualTo(400);
        assertThat(malformed.header("Content-Type")).startsWith("application/problem+json");
        assertThat(open("?bbox=1,2,3").status()).isEqualTo(400);
        assertThat(open("?near=1,2").status()).isEqualTo(400);
        assertThat(open("?live=maybe").status()).isEqualTo(400);
        assertThat(open("?source=no-such-source").status()).isEqualTo(404);
        assertThat(hub.clients()).isEmpty();
    }
}
