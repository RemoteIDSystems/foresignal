package ai.foresignal.report;

import ai.foresignal.collab.AccessResolver;
import ai.foresignal.collab.InboxItemEntity;
import ai.foresignal.collab.InboxItemRepository;
import ai.foresignal.collab.TagService;
import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.hypothesis.HypothesisInputs;
import ai.foresignal.hypothesis.HypothesisService;
import ai.foresignal.hypothesis.HypothesisSetEntity;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.PrincipalKind;
import ai.foresignal.security.PrincipalRepository;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenCreatedVia;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.Ulids;
import ai.foresignal.situation.SituationEntity;
import ai.foresignal.situation.SituationService;
import ai.foresignal.testsupport.ask.AskItSupport;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;

/**
 * Shared fixtures of the prompt 132 integration tests: named principals with their own tokens (each with an optional
 * organization), the report services, seeding helpers for situations, hypothesis sets and ledger questions, and
 * MockMvc helpers. Builds on the Ask fixtures (the {@code test-ask-quakes} public source, the private
 * {@code test-ask-secret}, tokens of each scope, the scripted chat provider and the test clock).
 */
public abstract class ReportItSupport extends AskItSupport {

    @Autowired protected MockMvc mvc;
    @Autowired protected PrincipalRepository principalRows;
    @Autowired protected AccessResolver access;
    @Autowired protected ReportService reports;
    @Autowired protected ReportDrafter drafter;
    @Autowired protected ReportPublisher publisher;
    @Autowired protected ReportSender sender;
    @Autowired protected ReportRepository reportRows;
    @Autowired protected ReportRevisionRepository revisionRows;
    @Autowired protected SituationService situations;
    @Autowired protected HypothesisService hypotheses;
    @Autowired protected ForecastService ledger;
    @Autowired protected TagService tagService;
    @Autowired protected InboxItemRepository inboxRows;

    /**
     * A named principal with one token.
     *
     * @param id the principal id
     * @param handle the handle
     * @param bearer {@code Bearer <token>}
     * @param scope the token's scope
     */
    public record Person(String id, String handle, String bearer, Scope scope) {

        /** The object caller this person is (private access only for admins). */
        public ObjectCaller caller() {
            return new ObjectCaller(id, "token:" + handle, scope, scope == Scope.ADMIN, Set.of());
        }
    }

    /** A new person principal of {@code organization} (null for none) with a token of {@code scope}. */
    protected Person person(String handle, Scope scope, String organization) {
        PrincipalEntity p = new PrincipalEntity(Ulids.next(clock.instant()), handle, handle.toUpperCase(),
                PrincipalKind.PERSON, clock.instant());
        p.setOrganization(organization, clock.instant());
        principalRows.saveAndFlush(p);
        TokenService.CreatedToken t = tokens.create(new TokenService.TokenSpec(handle + "-token", scope, null,
                TokenRestrictions.NONE, null, TokenCreatedVia.API, p.getId()));
        return new Person(p.getId(), handle, "Bearer " + t.plaintext(), scope);
    }

    // ------------------------------------------------------------------------- fixtures

    /** An active situation in Yemen. */
    protected SituationEntity situation(String title) {
        return situations.create(JSON.readTree("{\"title\":\"" + title + "\",\"theme\":\"maritime\",\"status\":"
                + "\"active\",\"severityOverride\":\"warning\",\"regionCodes\":[\"YE\"]}"), "tester", "operator", true);
    }

    /** An exclusive two-outcome hypothesis set linked to {@code situationId} when given. */
    protected HypothesisSetEntity hypothesisSet(String situationId) {
        String body = "{\"question\":\"Coalition response within 7 days?\",\"kind\":\"exclusive\",\"cadence\":\"daily\","
                + (situationId == null ? "" : "\"situationId\":\"" + situationId + "\",")
                + "\"regionCodes\":[\"YE\"],\"hypotheses\":["
                + "{\"label\":\"response\",\"statement\":\"A response follows within 7 days.\",\"type\":\"outcome\","
                + "\"prior\":0.7,\"priorBasis\":{\"type\":\"operator\",\"rationale\":\"Comparable strikes.\"}},"
                + "{\"label\":\"none\",\"statement\":\"No response within 7 days.\",\"type\":\"other\","
                + "\"prior\":0.3,\"priorBasis\":{\"type\":\"operator\",\"rationale\":\"Restraint.\"}}],"
                + "\"indicators\":[],\"assumptions\":[]}";
        return hypotheses.create(HypothesisInputs.set(JSON.readTree(body)), "operator", "tester");
    }

    /** A binary manual question closing in 5 days, linked to a set and a situation when given. */
    protected ForecastQuestionEntity question(String title, String setId, String situationId) {
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

    /** A report created by {@code owner} from single-quoted {@code ReportInput} text. */
    protected ReportEntity report(Person owner, String input) {
        return reports.create(node(input), owner.caller());
    }

    /** A report published by {@code owner}. */
    protected ReportRevisionEntity publish(Person owner, ReportEntity r) {
        return publisher.publish(r.getId(), owner.caller());
    }

    /** The personal inbox events of a principal, oldest first. */
    protected List<String> inboxEvents(String principalId) {
        List<InboxItemEntity> rows = new ArrayList<>(inboxRows.findAll());
        rows.sort(java.util.Comparator.comparing(InboxItemEntity::getId));
        return rows.stream().filter(i -> i.getPrincipalId().equals(principalId)).map(InboxItemEntity::getEvent)
                .toList();
    }

    // ------------------------------------------------------------------------- HTTP

    /** Performs {@code request} with {@code bearer} (null: anonymous). */
    protected <B extends AbstractMockHttpServletRequestBuilder<B>> MockHttpServletResponse call(String bearer,
            B request) throws Exception {
        if (bearer != null) {
            request.header("Authorization", bearer);
        }
        return mvc.perform(request).andReturn().getResponse();
    }

    /** A JSON body on {@code request} (single quotes become double quotes). */
    protected static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body.replace('\'', '"'));
    }

    /** A merge-patch body on {@code request}. */
    protected static MockHttpServletRequestBuilder mergePatch(MockHttpServletRequestBuilder request, String body) {
        return request.contentType("application/merge-patch+json").content(body.replace('\'', '"'));
    }

    /** The response body as JSON. */
    protected static JsonNode body(MockHttpServletResponse response) throws Exception {
        return JSON.readTree(response.getContentAsString());
    }

    /** The problem code of a response. */
    protected static String code(MockHttpServletResponse response) throws Exception {
        return body(response).get("code").asString();
    }

    /** A JSON body from single-quoted text. */
    protected static JsonNode node(String text) {
        return JSON.readTree(text.replace('\'', '"'));
    }

    /** The block types of a {@code Report} view, in order. */
    protected static List<String> blockTypes(JsonNode report) {
        List<String> out = new ArrayList<>();
        for (JsonNode b : report.get("blocks")) {
            out.add(b.get("type").asString());
        }
        return out;
    }

    /** The first block of {@code type} in a {@code Report} view, or null. */
    protected static JsonNode block(JsonNode report, String type) {
        for (JsonNode b : report.get("blocks")) {
            if (type.equals(b.get("type").asString())) {
                return b;
            }
        }
        return null;
    }

    /** A plain map view of a JSON node. */
    @SuppressWarnings("unchecked")
    protected static Map<String, Object> map(JsonNode n) {
        return JSON.convertValue(n, Map.class);
    }
}
