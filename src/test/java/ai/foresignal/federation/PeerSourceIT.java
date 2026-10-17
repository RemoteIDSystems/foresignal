package ai.foresignal.federation;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.provenance.LicenceResolver;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.store.query.FederationQueries;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import ai.foresignal.testsupport.federation.TestJws;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.records.RecordsTestSources;
import java.security.KeyPair;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The PULL runtime of a {@code foresignal-peer} source (prompt 113) against a remote instance served from
 * WireMock fixtures shaped like {@code FederationFeedIT}'s output: the {@code /info} JWS is verified against the
 * remote JWKS and the key pinned on first use (a later key is refused, {@code PEER_KEY_MISMATCH}); STANDARD and
 * TRACK items map to {@code peer-{id}} and {@code peer-{id}-trk} with {@code {originSource}/{originExternal}} ids;
 * {@code _foresignal} is volatile (an identical re-send makes no version); the per-record license comes from
 * {@code originLicence}; a remote tombstone tombstones the local record; a remote {@code reset} leads to a resync
 * that tombstones the local heads absent from the new snapshot.
 */
@IntegrationTest
@Import(RecordsTestSources.class)
class PeerSourceIT extends WireMockSupport {

    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");
    /** The clock during a test; items are observed shortly before it (seq seconds after BASE). */
    static final Instant NOW = T.plus(Duration.ofHours(1));
    static final Instant BASE = NOW.minusSeconds(120);
    static final JsonMapper JSON = JsonMapper.builder().build();
    static final String PEER = "remote";
    static final String REMOTE = "remote-install";
    static final String TOKEN = "fsr_remote_token";

    @Autowired PeerService peers;
    @Autowired PeerStates states;
    @Autowired PeerRuntime runtime;
    @Autowired SourceRegistry registry;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;
    @Autowired FederationQueries queries;
    @Autowired LicenceResolver licenses;

    KeyPair remoteKey;
    AdapterHttpHarness harness;
    Map<String, String> state;

    @BeforeEach
    void setUp() {
        clock.setInstant(NOW);
        remoteKey = TestJws.keyPair();
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(1_000, 100_000));
        harness.reset(PeerDescriptors.standardId(PEER));
        state = new ConcurrentHashMap<>();
        peers.create(JSON.readTree("{\"id\":\"" + PEER + "\",\"name\":\"Remote\",\"baseUrl\":\"" + baseUrl()
                + "\",\"mode\":\"PULL\",\"token\":\"" + TOKEN + "\",\"selection\":{}}"));
    }

    @AfterEach
    void tearDown() {
        if (states.find(PEER).isPresent()) {
            peers.delete(PEER);
        }
    }

    // ------------------------------------------------------------------------- fixtures

    static Map<String, Object> item(String source, String external, long seq, String tier, String kind, double value,
            boolean tombstone) {
        Map<String, Object> origin = FederationRules.hop(REMOTE, source, external, seq, BASE.plusSeconds(seq));
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("seq", seq);
        item.put("ref", RecordRef.format(REMOTE, source, external));
        item.put("sourceId", source);
        item.put("externalId", external);
        item.put("kind", kind);
        item.put("version", 1);
        item.put("persistReason", tombstone ? "TOMBSTONE" : "FIRST");
        item.put("historyTier", tier);
        item.put("observedAt", BASE.plusSeconds(seq).toString());
        item.put("eventTime", null);
        item.put("validUntil", null);
        item.put("tombstone", tombstone);
        item.put("geometry", tombstone ? null : Map.of("type", "Point", "coordinates", List.of(-97.74, 30.27)));
        item.put("payload", tombstone ? Map.of() : Map.of("mag", value));
        item.put("quality", null);
        item.put("licence", "CC-BY-4.0");
        item.put("provenance", Map.of("origin", origin, "chain", List.of(origin)));
        return item;
    }

    static Map<String, Object> quake(String external, long seq, double mag) {
        return item("usgs-earthquakes", external, seq, "STANDARD", "earthquake", mag, false);
    }

    static String snapshot(long atSeq, Map<String, Object>... items) {
        return JSON.writeValueAsString(Map.of("items", List.of(items), "atSeq", atSeq));
    }

    static String changes(long lastSeq, Map<String, Object>... items) {
        return JSON.writeValueAsString(Map.of("items", List.of(items), "lastSeq", lastSeq, "hasMore", false,
                "watermarkSeq", lastSeq));
    }

    static String sse(Map<String, Object>... items) {
        StringBuilder out = new StringBuilder(": open\n\n");
        for (Map<String, Object> item : items) {
            out.append("event: change\nid: ").append(item.get("seq")).append("\ndata: ")
                    .append(JSON.writeValueAsString(item)).append("\n\n");
        }
        return out.toString();
    }

    /** Stubs a remote whose /info is signed by {@code key}. */
    void stubRemote(KeyPair key, String snapshotBody, String changesBody, String streamBody) {
        WIREMOCK.resetAll();
        byte[] info = JSON.writeValueAsBytes(Map.of("instanceId", REMOTE, "instanceName", "Remote", "version", "1",
                "apiVersion", "1.3.0", "jwksUri", "https://elsewhere.example/.well-known/jwks.json",
                "capabilities", List.of("changes", "stream", "snapshot", "ingest"), "time", NOW.toString()));
        WIREMOCK.stubFor(get(urlPathEqualTo("/v1/federation/info")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withHeader("Foresignal-Signature", TestJws.sign(info, key)).withBody(info)));
        WIREMOCK.stubFor(get(urlPathEqualTo("/.well-known/jwks.json")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(JSON.writeValueAsString(TestJws.jwks(key.getPublic())))));
        WIREMOCK.stubFor(get(urlPathEqualTo("/v1/federation/snapshot")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(snapshotBody)));
        WIREMOCK.stubFor(get(urlPathEqualTo("/v1/federation/changes")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(changesBody)));
        WIREMOCK.stubFor(get(urlPathEqualTo("/v1/federation/stream")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/event-stream").withBody(streamBody)));
    }

    /** Runs one session pass and waits for it to end. */
    void pass() {
        String sourceId = PeerDescriptors.standardId(PEER);
        SourceDescriptor descriptor = registry.descriptor(sourceId);
        SourceHttp http = harness.client.forSource(sourceId, descriptor);
        RecordSink sink = record -> records.ingestAt(sourceId, clock.instant(), record);
        StreamHandle handle = runtime.startSession(PEER, new Context(descriptor, http, clock, state), sink);
        await().atMost(Duration.ofSeconds(30)).until(() -> !handle.isRunning());
    }

    boolean tombstoned(String source, String external) {
        return Boolean.TRUE.equals(jdbc.queryForObject("SELECT tombstone FROM record_head WHERE source_id = ? AND"
                + " external_id = ?", Boolean.class, source, external));
    }

    long versions(String source, String external) {
        return jdbc.queryForObject("SELECT count(*) FROM record_version WHERE source_id = ? AND external_id = ?",
                Long.class, source, external);
    }

    long heads(String source, String external) {
        return jdbc.queryForObject("SELECT count(*) FROM record_head WHERE source_id = ? AND external_id = ?",
                Long.class, source, external);
    }

    // ------------------------------------------------------------------------- tests

    @Test
    @DisplayName("first pass: the key is pinned, the snapshot and the stream map to peer-{id} and peer-{id}-trk")
    void replicates() {
        stubRemote(remoteKey, snapshot(20, quake("us1", 10, 5.0),
                        item("adsb", "abc123", 11, "TRACK", "aircraft", 3000, false)),
                changes(20), sse(quake("us2", 21, 4.2)));
        pass();
        PeerEntity peer = states.find(PEER).orElseThrow();
        assertThat(peer.getPinnedKid()).isEqualTo(TestJws.kid(remoteKey.getPublic()));
        assertThat(peer.getRemoteInstanceId()).isEqualTo(REMOTE);
        String std = PeerDescriptors.standardId(PEER);
        String trk = PeerDescriptors.trackId(PEER);
        assertThat(heads(std, "usgs-earthquakes/us1")).isEqualTo(1);
        assertThat(heads(std, "usgs-earthquakes/us2")).isEqualTo(1);
        assertThat(heads(trk, "adsb/abc123")).isEqualTo(1);
        assertThat(state.get(PeerSession.CURSOR_KEY)).isEqualTo("21");
        String payload = jdbc.queryForObject("SELECT payload::text FROM record_head WHERE source_id = ? AND"
                + " external_id = ?", String.class, std, "usgs-earthquakes/us1");
        assertThat(JSON.readTree(payload).get(FederationRules.MEMBER).get(FederationRules.ORIGIN_REF).asString())
                .isEqualTo("urn:foresignal:" + REMOTE + ":usgs-earthquakes:us1");
        assertThat(JSON.readTree(payload).get("mag").asDouble()).isEqualTo(5.0);
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/v1/federation/changes"))
                .withHeader("Authorization", equalTo("Bearer " + TOKEN)));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/v1/federation/stream"))
                .withHeader("Last-Event-ID", equalTo("20")));
    }

    @Test
    @DisplayName("the per-record license is the origin's (_foresignal.originLicence)")
    void originLicence() {
        stubRemote(remoteKey, snapshot(20, quake("us1", 10, 5.0)), changes(20), sse());
        pass();
        Long seq = jdbc.queryForObject("SELECT seq FROM record_head WHERE source_id = ? AND external_id = ?",
                Long.class, PeerDescriptors.standardId(PEER), "usgs-earthquakes/us1");
        List<RecordRow> rows = queries.bySeq(List.of(seq));
        LicenceResolver.Resolution resolution = licenses.resolveAll(rows).get(seq);
        assertThat(resolution.basis()).isEqualTo(LicenceResolver.BASIS_PEER);
        assertThat(resolution.terms().id()).isEqualTo("CC-BY-4.0");
    }

    @Test
    @DisplayName("_foresignal is volatile: an identical re-send with a newer origin seq makes no new version")
    void provenanceNotHashed() {
        stubRemote(remoteKey, snapshot(20, quake("us1", 10, 5.0)), changes(20), sse());
        pass();
        String std = PeerDescriptors.standardId(PEER);
        assertThat(versions(std, "usgs-earthquakes/us1")).isEqualTo(1);
        stubRemote(remoteKey, snapshot(20), changes(30, quake("us1", 30, 5.0)), sse());
        pass();
        assertThat(versions(std, "usgs-earthquakes/us1")).as("identical content").isEqualTo(1);
        stubRemote(remoteKey, snapshot(30), changes(31, quake("us1", 31, 5.5)), sse());
        pass();
        assertThat(versions(std, "usgs-earthquakes/us1")).as("changed content").isEqualTo(2);
    }

    @Test
    @DisplayName("a remote tombstone tombstones the local record")
    void tombstones() {
        stubRemote(remoteKey, snapshot(20, quake("us1", 10, 5.0)), changes(20), sse());
        pass();
        String std = PeerDescriptors.standardId(PEER);
        assertThat(tombstoned(std, "usgs-earthquakes/us1")).isFalse();
        stubRemote(remoteKey, snapshot(20), changes(20),
                sse(item("usgs-earthquakes", "us1", 25, "STANDARD", "earthquake", 0, true)));
        pass();
        assertThat(tombstoned(std, "usgs-earthquakes/us1")).isTrue();
    }

    @Test
    @DisplayName("a remote reset leads to a resync, and local heads absent from the new snapshot are tombstoned")
    void resetResyncs() {
        stubRemote(remoteKey, snapshot(20, quake("us1", 10, 5.0), quake("us2", 11, 4.0)), changes(20), sse());
        pass();
        String std = PeerDescriptors.standardId(PEER);
        assertThat(heads(std, "usgs-earthquakes/us1")).isEqualTo(1);

        stubRemote(remoteKey, snapshot(20), changes(20),
                ": open\n\nevent: reset\ndata: {\"reason\":\"resync_required\",\"resumeAfter\":null}\n\n");
        pass();

        clock.setInstant(NOW.plus(Duration.ofHours(1)));
        stubRemote(remoteKey, snapshot(50, quake("us2", 11, 4.0)), changes(50), sse());
        pass();
        await().atMost(Duration.ofSeconds(30)).until(() -> tombstoned(std, "usgs-earthquakes/us1"));
        assertThat(tombstoned(std, "usgs-earthquakes/us2")).isFalse();
        assertThat(state.get(PeerSession.CURSOR_KEY)).isEqualTo("50");
    }

    @Test
    @DisplayName("a key other than the pinned one stops ingest: failing, PEER_KEY_MISMATCH")
    void keyMismatch() {
        stubRemote(remoteKey, snapshot(20, quake("us1", 10, 5.0)), changes(20), sse());
        pass();
        KeyPair other = TestJws.keyPair();
        stubRemote(other, snapshot(40, quake("us9", 30, 6.0)), changes(40, quake("us9", 30, 6.0)),
                sse(quake("us9", 30, 6.0)));
        pass();
        PeerEntity peer = states.find(PEER).orElseThrow();
        assertThat(peer.getState()).isEqualTo(PeerService.STATE_FAILING);
        assertThat(peer.getHealthReason()).isEqualTo(PeerService.REASON_KEY_MISMATCH);
        assertThat(peer.getPinnedKid()).isEqualTo(TestJws.kid(remoteKey.getPublic()));
        assertThat(heads(PeerDescriptors.standardId(PEER), "usgs-earthquakes/us9")).isZero();
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/v1/federation/changes")));
    }

    /** A stream context over the harness's budgeted client and an in-memory adapter state. */
    static final class Context implements StreamContext {

        private final SourceDescriptor descriptor;
        private final SourceHttp http;
        private final Clock clock;
        private final Map<String, String> state;

        Context(SourceDescriptor descriptor, SourceHttp http, Clock clock, Map<String, String> state) {
            this.descriptor = descriptor;
            this.http = http;
            this.clock = clock;
            this.state = state;
        }

        @Override
        public String sourceId() {
            return descriptor.id();
        }

        @Override
        public SourceDescriptor descriptor() {
            return descriptor;
        }

        @Override
        public SourceHttp http() {
            return http;
        }

        @Override
        public ValidatedParams params() {
            return ValidatedParams.empty();
        }

        @Override
        public Map<String, String> credentials() {
            return Map.of();
        }

        @Override
        public Clock clock() {
            return clock;
        }

        @Override
        public Optional<Instant> lastSuccessAt() {
            return Optional.empty();
        }

        @Override
        public AdapterStateStore state() {
            return new AdapterStateStore() {
                @Override
                public Optional<String> get(String key) {
                    return Optional.ofNullable(state.get(key));
                }

                @Override
                public void put(String key, String value) {
                    state.put(key, value);
                }

                @Override
                public void remove(String key) {
                    state.remove(key);
                }
            };
        }

        @Override
        public void recordReceivedBytes(long bytes) {
            // not metered in the test
        }

        @Override
        public void connected() {
            // nothing to record
        }

        @Override
        public void disconnected(Throwable error) {
            // the pass reports through the peer row
        }
    }
}
