package ai.foresignal.collab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.object.AnnotationEntity;
import ai.foresignal.object.AnnotationRepository;
import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.security.Scope;
import ai.foresignal.security.Ulids;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.JsonNode;

/**
 * Tags (prompt 130): bulk assignment of up to 500 references, personal tags invisible to others, the caller's visible
 * tags in envelopes, and the migration of 129 {@code tag} annotations.
 */
@IntegrationTest
@Import(AskTestSources.class)
class TagIT extends CollabItSupport {

    @Autowired AnnotationRepository annotationRows;

    static String quake(int i) {
        return "record:" + AskTestSources.QUAKES + "/t" + i;
    }

    @Test
    @DisplayName("500 references are assigned in one call; more than 500 is refused; invisible ones are skipped")
    void bulkAssign() throws Exception {
        Person jo = person("jo-tags", Scope.READ, null);
        List<String> refs = new ArrayList<>();
        for (int i = 0; i < 499; i++) {
            quake("t" + i, "Place " + i, 4.0, 45.0, 12.0);
            refs.add(quake(i));
        }
        refs.add("situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C");
        String tagId = body(call(jo.bearer(), json(post("/v1/tags"), "{'name':'red-sea'}"))).get("id").asString();
        MockHttpServletResponse assigned = call(jo.bearer(), json(post("/v1/tags/" + tagId + "/assign"),
                "{\"refs\":" + JSON.writeValueAsString(refs) + "}"));
        assertThat(assigned.getStatus()).isEqualTo(200);
        JsonNode result = body(assigned);
        assertThat(result.get("changed").asInt()).isEqualTo(499);
        assertThat(result.get("skipped")).hasSize(1);
        assertThat(result.get("skipped").get(0).get("reason").asString()).isEqualTo("not-visible");
        JsonNode again = body(call(jo.bearer(), json(post("/v1/tags/" + tagId + "/assign"),
                "{'refs':['" + quake(0) + "','bogus']}")));
        assertThat(again.get("unchanged").asInt()).isEqualTo(1);
        assertThat(again.get("skipped").get(0).get("reason").asString()).isEqualTo("invalid-ref");
        refs.add(quake(500));
        refs.add(quake(501));
        assertThat(call(jo.bearer(), json(post("/v1/tags/" + tagId + "/assign"), "{\"refs\":"
                + JSON.writeValueAsString(refs) + "}")).getStatus()).isEqualTo(400);
        JsonNode removed = body(call(jo.bearer(), json(post("/v1/tags/" + tagId + "/unassign"),
                "{'refs':['" + quake(0) + "','" + quake(1) + "']}")));
        assertThat(removed.get("changed").asInt()).isEqualTo(2);
        JsonNode page = body(call(jo.bearer(), get("/v1/tags/" + tagId + "/objects?limit=10")));
        assertThat(page.get("items")).hasSize(10);
        assertThat(page.get("nextCursor").isNull()).isFalse();
    }

    @Test
    @DisplayName("personal tags are invisible to others; shared tags are visible instance-wide")
    void personalTagsAreInvisible() throws Exception {
        Person jo = person("jo-personal", Scope.READ, null);
        Person sam = person("sam-personal", Scope.READ, null);
        String personal = body(call(jo.bearer(), json(post("/v1/tags"), "{'name':'mine'}"))).get("id").asString();
        String shared = body(call(jo.bearer(), json(post("/v1/tags"), "{'name':'desk','namespace':'shared',"
                + "'tlp':'GREEN'}"))).get("id").asString();
        MockHttpServletResponse hidden = call(sam.bearer(), get("/v1/tags/" + personal));
        assertThat(hidden.getStatus()).isEqualTo(404);
        assertThat(body(hidden).get("code").asString()).isEqualTo("UNKNOWN_TAG");
        assertThat(call(admin, get("/v1/tags/" + personal)).getStatus()).isEqualTo(404);
        assertThat(call(sam.bearer(), get("/v1/tags/" + shared)).getStatus()).isEqualTo(200);
        JsonNode list = body(call(sam.bearer(), get("/v1/tags")));
        assertThat(list.get("items")).extracting(n -> n.get("id").asString()).containsExactly(shared);
        assertThat(call(jo.bearer(), json(post("/v1/tags"), "{'name':'MINE'}")).getStatus()).isEqualTo(409);
        assertThat(call(sam.bearer(), json(post("/v1/tags"), "{'name':'mine'}")).getStatus()).isEqualTo(201);
        assertThat(call(sam.bearer(), json(post("/v1/tags"), "{'name':'desk','namespace':'shared'}")).getStatus())
                .isEqualTo(409);
    }

    @Test
    @DisplayName("envelopes carry the caller's visible tags only")
    void envelopesShowVisibleTags() throws Exception {
        quake("t0", "Aden", 4.5, 45.0, 12.8);
        Person jo = person("jo-env", Scope.READ, null);
        Person sam = person("sam-env", Scope.READ, null);
        String personal = body(call(jo.bearer(), json(post("/v1/tags"), "{'name':'watch'}"))).get("id").asString();
        String shared = body(call(jo.bearer(), json(post("/v1/tags"), "{'name':'desk','namespace':'shared',"
                + "'tlp':'GREEN'}"))).get("id").asString();
        call(jo.bearer(), json(post("/v1/tags/" + personal + "/assign"), "{'refs':['" + quake(0) + "']}"));
        call(jo.bearer(), json(post("/v1/tags/" + shared + "/assign"), "{'refs':['" + quake(0) + "']}"));
        ObjectEnvelope forJo = objects.get(ObjectRef.parse(quake(0)), new ResolveContext(jo.caller(), null));
        ObjectEnvelope forSam = objects.get(ObjectRef.parse(quake(0)), new ResolveContext(sam.caller(), null));
        assertThat(forJo.tags()).containsExactlyInAnyOrder("watch", "desk");
        assertThat(forSam.tags()).containsExactly("desk");
        JsonNode objectTags = body(call(sam.bearer(), get(objectPath(quake(0), "/tags"))));
        assertThat(objectTags).hasSize(1);
        assertThat(objectTags.get(0).get("tag").get("name").asString()).isEqualTo("desk");
    }

    @Test
    @DisplayName("129 annotations of kind tag become personal tag assignments of their authors")
    void migratesTagAnnotations() {
        quake("t0", "Aden", 4.5, 45.0, 12.8);
        Person jo = person("jo-migrate", Scope.READ, null);
        AnnotationEntity a = new AnnotationEntity(Ulids.next(clock.instant()), quake(0), "tag", jo.id(),
                clock.instant());
        a.setTags(List.of("legacy-label"));
        annotationRows.saveAndFlush(a);
        assertThat(tagService.migrateTagAnnotations()).isEqualTo(1);
        assertThat(annotationRows.findById(a.getId()).orElseThrow().getDeletedAt()).isNotNull();
        Map<String, List<String>> names = tagService.visibleNames(List.of(quake(0)), jo.caller());
        assertThat(names.get(quake(0))).containsExactly("legacy-label");
        assertThat(tagService.migrateTagAnnotations()).isZero();
    }
}
