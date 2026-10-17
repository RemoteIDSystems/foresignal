package ai.foresignal.federation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.provenance.LicenceCatalog;
import ai.foresignal.provenance.SigningKeyService;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.TokenService;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.records.RecordsTestSources;
import ai.foresignal.testsupport.records.RecordsTestSources.StubRetentionState;
import ai.foresignal.testsupport.security.TestTokens;
import java.nio.charset.StandardCharsets;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The federation feed served to peers (prompt 113): items carry their license id and provenance chain; FORBIDDEN,
 * {@code LOCAL_ONLY}, MLAT, private and exercise records never leave (exercise only with {@code includeExercise});
 * a token without the {@code federation} capability is refused with 403; below the purge horizon the feed is 410;
 * the snapshot pages through a pinned watermark; a peer record is re-served with its stored chain plus a hop; an
 * edge's same-operator push carries {@code LOCAL_ONLY} records labelled {@code local-only-operator}; and
 * {@code /info} is signed with the instance key.
 */
@IntegrationTest
@Import(RecordsTestSources.class)
class FederationFeedIT {

    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();

    static final String OPEN = "test-fed-open";
    static final String FORBIDDEN = "test-fed-forbidden";
    static final String LOCAL = "test-fed-local";
    static final String SECRET = "test-fed-secret";
    static final String TRACK = "test-fed-trk";
    static final String KIND = "fed-record";
    static final String TRACK_KIND = "fed-aircraft";

    @Autowired MockMvc mvc;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired StubRetentionState retention;
    @Autowired SourceRegistry registry;
    @Autowired TokenService tokens;
    @Autowired FederationFeedService feed;
    @Autowired InstallIdService installId;
    @Autowired LicenceCatalog catalog;
    @Autowired SigningKeyService signing;

    String federation;
    String exercise;
    String plain;

    @BeforeEach
    void setUp() {
        unregister();
        clock.setInstant(T);
        retention.reset();
        registry.registerDynamic(base(OPEN, KIND, "CC BY 4.0").build());
        registry.registerDynamic(base(FORBIDDEN, KIND, "OpenSky Network terms (non-commercial research)").build());
        registry.registerDynamic(base(LOCAL, KIND, "CC BY 4.0").egress(Egress.LOCAL_ONLY).build());
        registry.registerDynamic(base(SECRET, KIND, "CC BY 4.0").privateSource(true).build());
        registry.registerDynamic(base(TRACK, TRACK_KIND, "CC BY 4.0").historyTier(HistoryTier.TRACK)
                .recordTtl(Duration.ofMinutes(5)).defaultIngestPolicy(IngestPolicy.all(Duration.ofMinutes(2))).build());
        federation = token("fed", federationRestrictions(null, false));
        exercise = token("fed-exercise", federationRestrictions(null, true));
        plain = TestTokens.bearer(TestTokens.create(tokens, "plain-read", Scope.READ));

        records.ingestAt(OPEN, T, point("a", KIND, 1, 2).build(),
                point("drill", KIND, 3, 4).put("exercise", true).build());
        records.ingestAt(FORBIDDEN, T, point("f", KIND, 5, 6).build());
        records.ingestAt(LOCAL, T, point("l", KIND, 7, 8).build());
        records.ingestAt(SECRET, T, point("s", KIND, 9, 10).build());
        records.ingestAt(TRACK, T, point("mlat-1", TRACK_KIND, 11, 12).put("posSource", "mlat").build(),
                point("adsb-1", TRACK_KIND, 13, 14).put("posSource", "adsb_icao").build());
    }

    @AfterEach
    void unregister() {
        retention.reset();
        for (String id : List.of(OPEN, FORBIDDEN, LOCAL, SECRET, TRACK)) {
            if (registry.contains(id)) {
                registry.unregisterDynamic(id);
            }
        }
    }

    static SourceDescriptor.Builder base(String id, String kind, String license) {
        return SourceDescriptor.builder(id)
                .name("Federation test source " + id)
                .attribution("Data: " + id)
                .licence(license)
                .kind(kind)
                .mode(Mode.PUSH)
                .historyTier(HistoryTier.STANDARD)
                .defaultBudget(Budget.of(60, 100_000));
    }

    static NormalizedRecord.Builder point(String id, String kind, double lon, double lat) {
        return NormalizedRecord.builder(id, kind).observedAt(T).point(lon, lat).put("v", id);
    }

    static TokenRestrictions federationRestrictions(List<String> sources, boolean includeExercise) {
        return new TokenRestrictions(sources, null, null, null, null, null, null, null, true,
                includeExercise ? Boolean.TRUE : null);
    }

    String token(String name, TokenRestrictions restrictions) {
        return TestTokens.bearer(TestTokens.create(tokens, name, Scope.READ, restrictions, null));
    }

    JsonNode getJson(String path, String bearer, int expected) throws Exception {
        MvcResult result = mvc.perform(get(path).header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().is(expected)).andReturn();
        return JSON.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    static List<String> externalIds(JsonNode page) {
        List<String> out = new ArrayList<>();
        page.get("items").forEach(item -> out.add(item.get("externalId").asString()));
        return out;
    }

    @Test
    @DisplayName("items carry the license id and a one-hop provenance chain; nothing restricted leaves")
    void itemsAndExclusions() throws Exception {
        JsonNode page = getJson("/v1/federation/changes", federation, 200);
        assertThat(externalIds(page)).containsExactlyInAnyOrder("a", "adsb-1");
        assertThat(page.get("hasMore").asBoolean()).isFalse();
        assertThat(page.get("lastSeq").asLong()).isPositive();
        JsonNode a = null;
        JsonNode adsb = null;
        for (JsonNode item : page.get("items")) {
            if ("a".equals(item.get("externalId").asString())) {
                a = item;
            } else {
                adsb = item;
            }
        }
        String instance = installId.installId();
        assertThat(a.get("ref").asString()).isEqualTo("urn:foresignal:" + instance + ":" + OPEN + ":a");
        assertThat(a.get("sourceId").asString()).isEqualTo(OPEN);
        assertThat(a.get("kind").asString()).isEqualTo(KIND);
        assertThat(a.get("version").asInt()).isEqualTo(1);
        assertThat(a.get("persistReason").asString()).isEqualTo("FIRST");
        assertThat(a.get("historyTier").asString()).isEqualTo("STANDARD");
        assertThat(a.get("tombstone").asBoolean()).isFalse();
        assertThat(a.get("geometry").get("type").asString()).isEqualTo("Point");
        assertThat(a.get("payload").get("v").asString()).isEqualTo("a");
        assertThat(a.get("payload").has(FederationRules.MEMBER)).isFalse();
        assertThat(a.get("licence").asString()).isEqualTo(catalog.terms("CC BY 4.0").id());
        JsonNode chain = a.get("provenance").get("chain");
        assertThat(chain).hasSize(1);
        assertThat(chain.get(0).get("instanceId").asString()).isEqualTo(instance);
        assertThat(chain.get(0).get("sourceId").asString()).isEqualTo(OPEN);
        assertThat(chain.get(0).get("externalId").asString()).isEqualTo("a");
        assertThat(chain.get(0).get("seq").asLong()).isEqualTo(a.get("seq").asLong());
        assertThat(chain.get(0).get("ingestedAt").isString()).isTrue();
        assertThat(a.get("provenance").get("origin")).isEqualTo(chain.get(0));
        assertThat(adsb.get("historyTier").asString()).isEqualTo("TRACK");
    }

    @Test
    @DisplayName("exercise records leave only for a token that sets includeExercise")
    void exercise() throws Exception {
        assertThat(externalIds(getJson("/v1/federation/changes", federation, 200))).doesNotContain("drill");
        assertThat(externalIds(getJson("/v1/federation/changes", exercise, 200))).contains("drill", "a");
    }

    @Test
    @DisplayName("a token without the federation capability is refused with 403 on every feed endpoint")
    void capabilityRequired() throws Exception {
        for (String path : List.of("/v1/federation/changes", "/v1/federation/snapshot", "/v1/federation/bundle")) {
            JsonNode problem = getJson(path, plain, 403);
            assertThat(problem.get("code").asString()).isEqualTo("RESTRICTED");
        }
    }

    @Test
    @DisplayName("the token's restriction columns still apply")
    void restrictionApplies() throws Exception {
        String trackOnly = token("fed-trk", federationRestrictions(List.of(TRACK), false));
        assertThat(externalIds(getJson("/v1/federation/changes", trackOnly, 200))).containsExactly("adsb-1");
    }

    @Test
    @DisplayName("below the purge horizon the change feed and the snapshot are 410 RESYNC_REQUIRED")
    void purgeHorizon() throws Exception {
        retention.setPurgeSeq(OPEN, 1_000_000);
        JsonNode problem = getJson("/v1/federation/changes?after=5", federation, 410);
        assertThat(problem.get("code").asString()).isEqualTo("RESYNC_REQUIRED");
        assertThat(problem.get("purgeHorizon").asLong()).isEqualTo(1_000_000);
        getJson("/v1/federation/snapshot?atSeq=5", federation, 410);
    }

    @Test
    @DisplayName("the snapshot pages current heads through a pinned watermark")
    void snapshotPaging() throws Exception {
        records.ingestAt(OPEN, T, point("b", KIND, 1, 3).build(), point("c", KIND, 1, 4).build());
        JsonNode first = getJson("/v1/federation/snapshot?limit=2", federation, 200);
        // A page holds at most limit heads; refused heads (FORBIDDEN, MLAT) leave it shorter.
        assertThat(first.get("items").size()).isLessThanOrEqualTo(2);
        long atSeq = first.get("atSeq").asLong();
        assertThat(atSeq).isPositive();
        assertThat(first.get("nextCursor").isString()).isTrue();
        List<JsonNode> items = new ArrayList<>();
        first.get("items").forEach(items::add);
        // A head written after the watermark is left out of the pinned snapshot.
        records.ingestAt(OPEN, T, point("late", KIND, 1, 5).build());
        String next = first.get("nextCursor").asString();
        int pages = 1;
        while (next != null) {
            JsonNode page = getJson("/v1/federation/snapshot?limit=2&cursor=" + next, federation, 200);
            assertThat(page.get("atSeq").asLong()).isEqualTo(atSeq);
            page.get("items").forEach(items::add);
            next = page.get("nextCursor").isNull() ? null : page.get("nextCursor").asString();
            pages++;
        }
        assertThat(pages).isGreaterThan(1);
        assertThat(items).extracting(i -> i.get("externalId").asString())
                .containsExactlyInAnyOrder("a", "b", "c", "adsb-1");
        assertThat(items).allSatisfy(i -> assertThat(i.get("persistReason").asString()).isEqualTo("SNAPSHOT"));
    }

    @Test
    @DisplayName("a peer record is served with its stored chain plus this instance's hop, and its origin license")
    void peerRecordChain() throws Exception {
        Map<String, Object> origin = Map.of("instanceId", "other-instance", "sourceId", "usgs-earthquakes",
                "externalId", "us1", "seq", 7, "ingestedAt", "2026-02-28T23:00:00Z");
        records.ingestAt(OPEN, T, point("usgs-earthquakes/us1", KIND, 1, 6)
                .put(FederationRules.MEMBER, Map.of(FederationRules.CHAIN, List.of(origin),
                        FederationRules.ORIGIN_LICENSE, "us-gov-public-domain",
                        FederationRules.ORIGIN_REF, "urn:foresignal:other-instance:usgs-earthquakes:us1",
                        FederationRules.ORIGIN_SEQ, 7))
                .build());
        JsonNode item = null;
        for (JsonNode candidate : getJson("/v1/federation/changes", federation, 200).get("items")) {
            if ("usgs-earthquakes/us1".equals(candidate.get("externalId").asString())) {
                item = candidate;
            }
        }
        assertThat(item).isNotNull();
        JsonNode chain = item.get("provenance").get("chain");
        assertThat(chain).hasSize(2);
        assertThat(chain.get(0).get("instanceId").asString()).isEqualTo("other-instance");
        assertThat(chain.get(1).get("instanceId").asString()).isEqualTo(installId.installId());
        assertThat(item.get("provenance").get("origin").get("instanceId").asString()).isEqualTo("other-instance");
        assertThat(item.get("licence").asString()).isEqualTo("us-gov-public-domain");
        assertThat(item.get("provenance").get("licenceBasis").asString()).isEqualTo("peer-origin");
        assertThat(item.get("payload").has(FederationRules.MEMBER)).isFalse();
    }

    @Test
    @DisplayName("an edge's same-operator push carries LOCAL_ONLY records as local-only-operator; private and MLAT never")
    void sameOperatorPush() throws Exception {
        List<Long> seqs = allSeqs();
        List<Map<String, Object>> own = feed.itemsBySeq(seqs, FederationFeedService.Caller.push(true));
        List<String> ids = own.stream().map(i -> String.valueOf(i.get("externalId"))).toList();
        assertThat(ids).contains("a", "l", "adsb-1", "drill").doesNotContain("s", "f", "mlat-1");
        Map<String, Object> local = own.stream().filter(i -> "l".equals(i.get("externalId"))).findFirst().orElseThrow();
        assertThat(local.get("licence")).isEqualTo(FederationFeedService.LOCAL_ONLY_LICENSE);
        List<String> strangers = feed.itemsBySeq(seqs, FederationFeedService.Caller.push(false)).stream()
                .map(i -> String.valueOf(i.get("externalId"))).toList();
        assertThat(strangers).contains("a").doesNotContain("l", "s", "f", "mlat-1");
    }

    @Test
    @DisplayName("/info is signed with the instance key and names the install id")
    void signedInfo() throws Exception {
        MvcResult result = mvc.perform(get("/v1/federation/info").header(HttpHeaders.AUTHORIZATION, federation))
                .andExpect(status().isOk()).andReturn();
        byte[] body = result.getResponse().getContentAsByteArray();
        String signature = result.getResponse().getHeader("Foresignal-Signature");
        PeerKeys.Verification verification = PeerKeys.verifyWithJwks(body, signature, signing.jwks());
        assertThat(verification.signatureValid()).as(String.valueOf(verification.error())).isTrue();
        JsonNode info = JSON.readTree(body);
        assertThat(info.get("instanceId").asString()).isEqualTo(installId.installId());
        assertThat(info.get("capabilities").toString()).contains("changes", "stream", "snapshot", "ingest");
        assertThat(info.get("jwksUri").asString()).endsWith("/.well-known/jwks.json");
    }

    private List<Long> allSeqs() {
        List<Long> out = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            out.add((long) i);
        }
        return out;
    }
}
