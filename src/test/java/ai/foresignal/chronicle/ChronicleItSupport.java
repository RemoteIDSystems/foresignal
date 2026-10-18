package ai.foresignal.chronicle;

import ai.foresignal.collab.AccessResolver;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.PrincipalKind;
import ai.foresignal.security.PrincipalRepository;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenCreatedVia;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.Ulids;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.ask.AskItSupport;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;

/**
 * Shared fixtures of the prompt 131 integration tests: named principals with tokens, the chronicle services, records
 * of the ingest test sources ({@value TestIngestSources#STANDARD} with policy ALL, {@value TestIngestSources#TRACK})
 * and of the Ask sources (the private {@code test-ask-secret}), capture driven directly with
 * {@link ChronicleCaptureService#catchUp()}, and MockMvc helpers. Builds on the Ask fixtures (tokens of each scope and
 * the test clock at {@link #T}).
 */
abstract class ChronicleItSupport extends AskItSupport {

    @Autowired protected MockMvc mvc;
    @Autowired protected PrincipalRepository principalRows;
    @Autowired protected AccessResolver access;
    @Autowired protected ChronicleService chronicles;
    @Autowired protected ChronicleCaptureService capture;
    @Autowired protected ChronicleThreadService threads;
    @Autowired protected ChronicleReplayService replay;
    @Autowired protected ChronicleStorage storage;
    @Autowired protected ChronicleRepository chronicleRows;
    @Autowired protected ChronicleLayerRepository layerRows;
    @Autowired protected ChronicleEventRepository eventRows;

    /**
     * A named principal with one token.
     *
     * @param id the principal id
     * @param handle the handle
     * @param bearer {@code Bearer <token>}
     * @param scope the token's scope
     */
    record Person(String id, String handle, String bearer, Scope scope) {

        /** The object caller this person is (private access only for admins). */
        ObjectCaller caller() {
            return new ObjectCaller(id, "token:" + handle, scope, scope == Scope.ADMIN, Set.of());
        }
    }

    /** A new person principal with a token of {@code scope}. */
    Person person(String handle, Scope scope) {
        PrincipalEntity p = new PrincipalEntity(Ulids.next(clock.instant()), handle, handle.toUpperCase(),
                PrincipalKind.PERSON, clock.instant());
        principalRows.saveAndFlush(p);
        ai.foresignal.security.TokenService.CreatedToken t = tokens.create(
                new ai.foresignal.security.TokenService.TokenSpec(handle + "-token", scope, null,
                        TokenRestrictions.NONE, null, TokenCreatedVia.API, p.getId()));
        return new Person(p.getId(), handle, "Bearer " + t.plaintext(), scope);
    }

    /** A JSON body from single-quoted text. */
    static JsonNode node(String text) {
        return JSON.readTree(text.replace('\'', '"'));
    }

    /** A chronicle created by {@code owner} from single-quoted {@code ChronicleInput} text. */
    ChronicleEntity create(Person owner, String input) {
        return chronicles.create(node(input), owner.caller(), ChronicleVocabulary.ORIGIN_OPERATOR, null);
    }

    /** Activates a chronicle as {@code owner}. */
    ChronicleEntity activate(Person owner, ChronicleEntity c) {
        return chronicles.activate(c.getId(), null, owner.caller(), false);
    }

    /** A {@value TestIngestSources#STANDARD} version (policy ALL) at {@code at}. */
    void standard(String externalId, Instant at, double lon, double lat, Object value) {
        clock.setInstant(at);
        records.ingestAt(TestIngestSources.STANDARD, at, NormalizedRecord.builder(externalId,
                TestIngestSources.STANDARD_KIND).observedAt(at).put("v", value).put("name", "Entity " + externalId)
                .point(lon, lat).build());
    }

    /** A tombstone of a {@value TestIngestSources#STANDARD} entity at {@code at}. */
    void standardGone(String externalId, Instant at) {
        clock.setInstant(at);
        records.ingestAt(TestIngestSources.STANDARD, at, NormalizedRecord.builder(externalId,
                TestIngestSources.STANDARD_KIND).observedAt(at).tombstone(true).build());
    }

    /** The captured versions of a chronicle (0 without a leaf). */
    long captured(String chronicleId) {
        if (!storage.leafExists(chronicleId)) {
            return 0;
        }
        Long n = jdbc.queryForObject("SELECT count(*) FROM chronicle_version WHERE chronicle_id = ?", Long.class,
                chronicleId);
        return n == null ? 0 : n;
    }

    /** The captured versions of one layer. */
    long captured(String chronicleId, String layerId) {
        Long n = jdbc.queryForObject("SELECT count(*) FROM chronicle_version WHERE chronicle_id = ? AND layer_id = ?",
                Long.class, chronicleId, layerId);
        return n == null ? 0 : n;
    }

    /** The captured seqs of one entity, ascending. */
    List<Long> capturedSeqs(String chronicleId, String externalId) {
        return jdbc.queryForList("SELECT seq FROM chronicle_version WHERE chronicle_id = ? AND external_id = ?"
                + " ORDER BY seq", Long.class, chronicleId, externalId);
    }

    /** The global versions of one entity. */
    long globalVersions(String sourceId, String externalId) {
        Long n = jdbc.queryForObject("SELECT count(*) FROM record_version WHERE source_id = ? AND external_id = ?",
                Long.class, sourceId, externalId);
        return n == null ? 0 : n;
    }

    /** The thread item types of a chronicle, oldest first. */
    List<String> threadTypes(String chronicleId) {
        return eventRows.findByChronicleIdOrderByIdAsc(chronicleId).stream().map(ChronicleEventEntity::getType)
                .toList();
    }

    /** The first live layer of a chronicle. */
    ChronicleLayerEntity firstLayer(String chronicleId) {
        return chronicles.liveLayers(chronicleId).getFirst();
    }

    /** A layer input over the {@value TestIngestSources#STANDARD} source with {@code capture}. */
    static String standardLayer(String name, String capture) {
        return "{'name':'" + name + "','selector':{'type':'records','sources':['" + TestIngestSources.STANDARD
                + "']},'capture':" + capture + "}";
    }

    // ------------------------------------------------------------------------- HTTP

    /** Performs {@code request} with {@code bearer} (null: anonymous). */
    MockHttpServletResponse call(String bearer, MockHttpServletRequestBuilder request) throws Exception {
        if (bearer != null) {
            request.header("Authorization", bearer);
        }
        return mvc.perform(request).andReturn().getResponse();
    }

    /** A JSON body on {@code request} (single quotes become double quotes). */
    static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body.replace('\'', '"'));
    }

    /** A merge-patch body on {@code request}. */
    static MockHttpServletRequestBuilder mergePatch(MockHttpServletRequestBuilder request, String body) {
        return request.contentType("application/merge-patch+json").content(body.replace('\'', '"'));
    }

    /** The response body as JSON. */
    static JsonNode body(MockHttpServletResponse response) throws Exception {
        return JSON.readTree(response.getContentAsString());
    }

    /** A map view of a JSON body. */
    @SuppressWarnings("unchecked")
    static Map<String, Object> map(JsonNode node) {
        return JSON.convertValue(node, Map.class);
    }
}
