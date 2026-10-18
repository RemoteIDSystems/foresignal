package ai.foresignal.object;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.analytics.finding.AnalyticFindingRepository;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.hypothesis.HypothesisInputs;
import ai.foresignal.hypothesis.HypothesisService;
import ai.foresignal.hypothesis.HypothesisSetEntity;
import ai.foresignal.intel.IntelEventMemberEntity;
import ai.foresignal.intel.IntelEventMemberRepository;
import ai.foresignal.intel.IntelligenceEventEntity;
import ai.foresignal.intel.IntelligenceEventRepository;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.Ulids;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.situation.SituationEntity;
import ai.foresignal.situation.SituationService;
import ai.foresignal.testsupport.ask.AskItSupport;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;

/**
 * Shared fixtures of the prompt 129 integration tests: the Ask test sources ({@value AskTestSources#QUAKES} public,
 * {@value AskTestSources#SECRET} private), tokens of each scope, and helpers that create situations, findings, intel
 * events, hypothesis sets and questions through the production services or repositories.
 */
abstract class ObjectItSupport extends AskItSupport {

    static final GeometryFactory GEOMETRY = new GeometryFactory(new PrecisionModel(), 4326);

    @Autowired protected MockMvc mvc;
    @Autowired protected ObjectService objects;
    @Autowired protected RelationService relationService;
    @Autowired protected RelationRepository relationRows;
    @Autowired protected RelationProjector projector;
    @Autowired protected AnnotationService annotations;
    @Autowired protected WorkspaceService workspaces;
    @Autowired protected SituationService situations;
    @Autowired protected HypothesisService hypotheses;
    @Autowired protected ForecastService ledger;
    @Autowired protected ForecasterRegistry forecasters;
    @Autowired protected AnalyticFindingRepository findingRows;
    @Autowired protected IntelligenceEventRepository eventRows;
    @Autowired protected IntelEventMemberRepository memberRows;
    @Autowired protected ContextSummaryService summaries;

    @BeforeEach
    void setUpObjects() {
        forecasters.registerBuiltIns();
        summaries.clearCache();
    }

    // ------------------------------------------------------------------------- HTTP

    /** Performs {@code request} with {@code bearer} (null: anonymous). */
    MockHttpServletResponse call(String bearer, MockHttpServletRequestBuilder request) throws Exception {
        if (bearer != null) {
            request.header("Authorization", bearer);
        }
        return mvc.perform(request).andReturn().getResponse();
    }

    /** A JSON body on {@code request}. */
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

    /** {@code GET /v1/objects/{ref}…} with the reference as one encoded segment ({@code /} as {@code %2F}). */
    static MockHttpServletRequestBuilder getObject(String ref, String suffix) {
        return get(java.net.URI.create(objectPath(ref) + suffix));
    }

    /** {@code /v1/objects/{ref}} with the reference percent-encoded as one path segment. */
    static String objectPath(String ref) {
        return "/v1/objects/" + ref.replace("%", "%25").replace("/", "%2F");
    }

    // ------------------------------------------------------------------------- callers

    /** An object caller of {@code scope}. */
    static ObjectCaller objectCaller(String principal, Scope scope, boolean seesPrivate) {
        return new ObjectCaller(principal, "token:" + principal, scope, seesPrivate,
                seesPrivate ? Set.of() : Set.of(AskTestSources.SECRET));
    }

    /** A resolve context of {@code scope} at {@code asOf}. */
    static ResolveContext context(Scope scope, boolean seesPrivate, Instant asOf) {
        return new ResolveContext(objectCaller("p-" + scope, scope, seesPrivate), asOf);
    }

    /** An MCP caller of {@code scope}. */
    static McpCaller mcp(Scope scope, boolean privateAccess, String principal) {
        AuthenticatedToken token = new AuthenticatedToken(UUID.randomUUID(), "object-mcp", scope,
                TokenRestrictions.NONE, null, principal, false, privateAccess);
        return new McpCaller(token, new ReadCaller(scope, privateAccess, false, ReadRestriction.NONE, privateAccess),
                privateAccess ? Set.of() : Set.of(AskTestSources.SECRET), null);
    }

    // ------------------------------------------------------------------------- fixtures

    static Point point(double lon, double lat) {
        return GEOMETRY.createPoint(new Coordinate(lon, lat));
    }

    /** An active situation in Yemen with a severity override. */
    SituationEntity situation(String title, String severity) {
        return situations.create(JSON.readTree("{\"title\":\"" + title + "\",\"theme\":\"maritime\",\"status\":"
                + "\"active\",\"severityOverride\":\"" + severity + "\",\"regionCodes\":[\"YE\"]}"), "tester",
                "operator", true);
    }

    /** An open finding at a point, from a detector, optionally private and linked to a situation. */
    AnalyticFindingEntity finding(String title, String detector, double lon, double lat, boolean priv,
            String situationId) {
        Instant now = clock.instant();
        AnalyticFindingEntity f = new AnalyticFindingEntity(Ulids.next(now), "change-point", title, "warning", now,
                now);
        f.setStatus("open");
        f.setGeom(point(lon, lat));
        f.setRegionCodes(List.of("YE"));
        f.setWindowStart(now.minus(Duration.ofHours(6)));
        f.setWindowEnd(now);
        f.setDetector(Map.of("name", detector, "version", "1.0.0", "params", Map.of()));
        f.setPrivateFinding(priv);
        f.setSituationId(situationId);
        f.setSignalFamily("ais-behaviour");
        f.setMembers(List.of("record:" + AskTestSources.QUAKES + "/q1@1"));
        return findingRows.save(f);
    }

    /** An active intel event at a point with record members (role per member: supporting or contradicting). */
    IntelligenceEventEntity intelEvent(String title, boolean priv, String recipe, String... members) {
        Instant now = clock.instant();
        IntelligenceEventEntity e = new IntelligenceEventEntity(Ulids.next(now), "maritime-disruption", title,
                now.minus(Duration.ofHours(2)));
        e.setStatus("active");
        e.setSeverity("warning");
        e.setGeom(point(43.1, 14.8));
        e.setCentroid(point(43.1, 14.8));
        e.setPrivateEvent(priv);
        e.setRecipeId(recipe);
        e.setConfidence(0.7);
        e.setCreatedAt(now);
        e.setUpdatedAt(now);
        IntelligenceEventEntity saved = eventRows.save(e);
        for (String m : members) {
            String[] parts = m.split("\\|");
            IntelEventMemberEntity member = new IntelEventMemberEntity(saved.getId(), "record", AskTestSources.QUAKES,
                    parts[0], 1);
            member.setRole(parts.length > 1 ? parts[1] : "supporting");
            member.setObservedAt(now.minus(Duration.ofHours(1)));
            member.setAddedAt(now);
            memberRows.save(member);
        }
        return saved;
    }

    /** An exclusive two-outcome hypothesis set, linked to {@code situationId} when given. */
    HypothesisSetEntity hypothesisSet(String situationId) {
        String body = "{\"question\":\"Coalition response within 7 days?\",\"kind\":\"exclusive\",\"cadence\":\"daily\","
                + (situationId == null ? "" : "\"situationId\":\"" + situationId + "\",")
                + "\"regionCodes\":[\"YE\"],\"hypotheses\":["
                + "{\"label\":\"response\",\"statement\":\"A response follows within 7 days.\",\"type\":\"outcome\","
                + "\"prior\":0.45,\"priorBasis\":{\"type\":\"operator\",\"rationale\":\"Comparable strikes.\"}},"
                + "{\"label\":\"none\",\"statement\":\"No response within 7 days.\",\"type\":\"outcome\","
                + "\"prior\":0.5,\"priorBasis\":{\"type\":\"operator\",\"rationale\":\"Restraint.\"}},"
                + "{\"label\":\"other\",\"statement\":\"Other / none of these.\",\"type\":\"other\",\"prior\":0.05,"
                + "\"priorBasis\":{\"type\":\"operator\",\"rationale\":\"Residual mass.\"}}],\"indicators\":[],"
                + "\"assumptions\":[]}";
        return hypotheses.create(HypothesisInputs.set(JSON.readTree(body)), "operator", "tester");
    }

    /** A binary manual question closing in 5 days, linked to a set and situation when given. */
    ForecastQuestionEntity question(String title, String setId, String situationId) {
        String links = "{" + (setId == null ? "" : "\"hypothesisSetId\":\"" + setId + "\"")
                + (setId != null && situationId != null ? "," : "")
                + (situationId == null ? "" : "\"situationId\":\"" + situationId + "\"") + "}";
        String body = "{\"title\":\"" + title + "\",\"type\":\"binary\",\"resolutionCriteria\":\"Yes if " + title
                + " by the closing time, as reported by two independent outlets.\",\"resolver\":{\"type\":\"manual\"},"
                + "\"closesAt\":\"" + clock.instant().plus(Duration.ofDays(5)) + "\",\"resolveBy\":\""
                + clock.instant().plus(Duration.ofDays(6)) + "\",\"category\":\"conflict\",\"links\":" + links + "}";
        return ledger.openQuestion(ForecastInputs.question(JSON.readTree(body), clock.instant()), "operator", "tester")
                .question();
    }

    /** The principal id behind a bearer created by {@link AskItSupport}'s tokens. */
    String principalOf(String bearer) {
        return jdbc.queryForObject("SELECT principal_id FROM api_token WHERE token_hash = ?", String.class,
                hasher.hash(bearer.substring("Bearer ".length())));
    }

    @Autowired private ai.foresignal.security.TokenHasher hasher;
}
