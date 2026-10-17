package ai.foresignal.collab;

import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectService;
import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.PrincipalKind;
import ai.foresignal.security.PrincipalRepository;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenCreatedVia;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.Ulids;
import ai.foresignal.security.audit.AuditService;
import ai.foresignal.testsupport.ask.AskItSupport;
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
 * Shared fixtures of the prompt 130 integration tests: named principals with their own tokens (each with an optional
 * organization), the collaboration services, and MockMvc helpers. Builds on the Ask fixtures (the
 * {@code test-ask-quakes} public source, tokens of each scope and the test clock).
 */
abstract class CollabItSupport extends AskItSupport {

    @Autowired protected MockMvc mvc;
    @Autowired protected PrincipalRepository principalRows;
    @Autowired protected PrincipalAdminService principalAdmin;
    @Autowired protected AccessResolver access;
    @Autowired protected ShareService shares;
    @Autowired protected GroupService groups;
    @Autowired protected TagService tagService;
    @Autowired protected CommentService comments;
    @Autowired protected AttachmentService attachments;
    @Autowired protected EvidenceItemService evidenceItems;
    @Autowired protected SavedSearchService savedSearches;
    @Autowired protected ObjectService objects;
    @Autowired protected InboxItemRepository inboxRows;
    @Autowired protected AuditService audit;

    /**
     * A named principal with one token.
     *
     * @param id the principal id
     * @param handle the handle
     * @param bearer {@code Bearer <token>}
     * @param scope the token's scope
     */
    record Person(String id, String handle, String bearer, Scope scope) {

        /** The object caller this person is. */
        ObjectCaller caller() {
            return new ObjectCaller(id, "token:" + handle, scope, scope == Scope.ADMIN, Set.of());
        }
    }

    /** A new person principal of {@code organization} (null for none) with a token of {@code scope}. */
    Person person(String handle, Scope scope, String organization) {
        PrincipalEntity p = new PrincipalEntity(Ulids.next(clock.instant()), handle, handle.toUpperCase(),
                PrincipalKind.PERSON, clock.instant());
        p.setOrganization(organization, clock.instant());
        principalRows.saveAndFlush(p);
        TokenService.CreatedToken t = tokens.create(new TokenService.TokenSpec(handle + "-token", scope, null,
                TokenRestrictions.NONE, null, TokenCreatedVia.API, p.getId()));
        return new Person(p.getId(), handle, "Bearer " + t.plaintext(), scope);
    }

    // ------------------------------------------------------------------------- HTTP

    /** Performs {@code request} (plain or multipart) with {@code bearer} (null: anonymous). */
    <B extends AbstractMockHttpServletRequestBuilder<B>> MockHttpServletResponse call(String bearer, B request)
            throws Exception {
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

    /** {@code /v1/objects/{ref}} with the reference percent-encoded as one path segment. */
    static java.net.URI objectPath(String ref, String suffix) {
        return java.net.URI.create("/v1/objects/" + ref.replace("%", "%25").replace("/", "%2F") + suffix);
    }

    /** A JSON body from single-quoted text. */
    static JsonNode node(String text) {
        return JSON.readTree(text.replace('\'', '"'));
    }

    /** The personal inbox events of a principal, oldest first. */
    List<String> inboxEvents(String principalId) {
        return inboxRows.findAll().stream().filter(i -> i.getPrincipalId().equals(principalId))
                .sorted(java.util.Comparator.comparing(InboxItemEntity::getId)).map(InboxItemEntity::getEvent)
                .toList();
    }

    /** The audit entries of {@code targetType} after a flush. */
    List<Map<String, Object>> auditOf(String targetType) {
        audit.flush();
        return jdbc.queryForList("SELECT operation_id, target_id, principal_id, detail::text AS detail FROM"
                + " audit_event WHERE target_type = ? ORDER BY at", targetType);
    }
}
