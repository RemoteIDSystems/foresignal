package ai.foresignal.collab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.notify.ChannelService;
import ai.foresignal.security.Scope;
import ai.foresignal.store.NotificationDeliveryEntity;
import ai.foresignal.store.NotificationDeliveryRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.JsonNode;

/**
 * Comment threads (prompt 130): replies, mentions that notify only principals who can see the target (the others are
 * dropped back to the author), reply notifications, the commenter role on work products, and resolving.
 */
@IntegrationTest
@Import(AskTestSources.class)
class CommentThreadIT extends CollabItSupport {

    @Autowired ChannelService channelService;
    @Autowired NotificationDeliveryRepository deliveries;

    @Test
    @DisplayName("mentions notify only principals who can see the target; replies notify the thread; resolve")
    void threads() throws Exception {
        Person jo = person("jo-c", Scope.AGENT, null);
        Person sam = person("sam-c", Scope.READ, null);
        Person kim = person("kim-c", Scope.READ, null);
        Person lee = person("lee-c", Scope.READ, null);
        String id = body(call(jo.bearer(), json(post("/v1/investigations"), "{'title':'Port closures',"
                + "'visibility':'private'}"))).get("id").asString();
        String ref = "investigation:" + id;
        assertThat(call(jo.bearer(), json(post(objectPath(ref, "/share")), "{'grantees':['principal:sam-c'],"
                + "'role':'commenter'}")).getStatus()).isEqualTo(200);
        assertThat(call(jo.bearer(), json(post(objectPath(ref, "/share")), "{'grantees':['principal:" + lee.id()
                + "'],'role':'viewer'}")).getStatus()).isEqualTo(200);

        MockHttpServletResponse created = call(jo.bearer(), json(post(objectPath(ref, "/annotations")),
                "{'kind':'note','body':'@sam-c and @kim-c please check the berth list'}"));
        assertThat(created.getStatus()).isEqualTo(201);
        JsonNode root = body(created);
        assertThat(root.get("mentions")).extracting(JsonNode::asString).containsExactly("sam-c");
        assertThat(root.get("droppedMentions")).extracting(JsonNode::asString).containsExactly("kim-c");
        assertThat(root.get("tlp").asString()).isEqualTo("AMBER");
        assertThat(inboxEvents(sam.id())).contains(MeEvents.MENTION);
        assertThat(inboxEvents(kim.id())).doesNotContain(MeEvents.MENTION);

        String rootId = root.get("id").asString();
        MockHttpServletResponse reply = call(sam.bearer(), json(post("/v1/annotations/" + rootId + "/replies"),
                "{'body':'Two berths are closed.'}"));
        assertThat(reply.getStatus()).isEqualTo(201);
        assertThat(body(reply).get("parentId").asString()).isEqualTo(rootId);
        assertThat(inboxEvents(jo.id())).contains(MeEvents.COMMENT_REPLY);

        assertThat(call(lee.bearer(), json(post("/v1/annotations/" + rootId + "/replies"), "{'body':'me too'}"))
                .getStatus()).isEqualTo(403);
        MockHttpServletResponse hidden = call(kim.bearer(), json(post("/v1/annotations/" + rootId + "/replies"),
                "{'body':'can I?'}"));
        assertThat(hidden.getStatus()).isEqualTo(404);
        assertThat(call(kim.bearer(), get(objectPath(ref, "/annotations?thread=true"))).getStatus()).isEqualTo(404);

        JsonNode threads = body(call(jo.bearer(), get(objectPath(ref, "/annotations?thread=true"))));
        assertThat(threads.get("items")).hasSize(1);
        assertThat(threads.get("items").get(0).get("replyCount").asInt()).isEqualTo(1);
        assertThat(threads.get("items").get(0).get("resolved").asBoolean()).isFalse();

        MockHttpServletResponse resolved = call(sam.bearer(), json(post("/v1/annotations/" + rootId + "/resolve"),
                "{'resolved':true}"));
        assertThat(resolved.getStatus()).isEqualTo(200);
        assertThat(body(resolved).get("resolved").asBoolean()).isTrue();
        assertThat(call(lee.bearer(), json(post("/v1/annotations/" + rootId + "/resolve"), "{'resolved':false}"))
                .getStatus()).isEqualTo(403);
        JsonNode after = body(call(jo.bearer(), get(objectPath(ref, "/annotations?thread=true"))));
        assertThat(after.get("items").get(0).get("resolved").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("on data objects any token comments and anyone mentioned with a token is notified")
    void dataObjects() throws Exception {
        quake("c1", "Aden", 4.5, 45.0, 12.8);
        Person jo = person("jo-d", Scope.READ, null);
        Person sam = person("sam-d", Scope.READ, null);
        String ref = "record:" + AskTestSources.QUAKES + "/c1";
        JsonNode note = body(call(jo.bearer(), json(post(objectPath(ref, "/annotations")),
                "{'kind':'note','body':'cc @sam-d @nobody-here'}")));
        assertThat(note.get("mentions")).extracting(JsonNode::asString).containsExactly("sam-d");
        assertThat(note.get("droppedMentions")).extracting(JsonNode::asString).containsExactly("nobody-here");
        assertThat(inboxEvents(sam.id())).containsExactly(MeEvents.MENTION);
        JsonNode inbox = body(call(sam.bearer(), get("/v1/inbox")));
        assertThat(inbox.get("items").get(0).get("category").asString()).isEqualTo("mention");
        assertThat(inbox.get("items").get(0).get("read").asBoolean()).isFalse();
        assertThat(body(call(sam.bearer(), get("/v1/inbox?unread=true"))).get("items")).isEmpty();
    }

    @Test
    @DisplayName("a mention is copied to instance channels subscribed to 'mention' (Channel.events), not to others")
    void mentionReachesSubscribedInstanceChannel() throws Exception {
        quake("c2", "Aden", 4.5, 45.0, 12.8);
        UUID subscribed = channelService.create(node("{'name':'mentions','type':'webhook',"
                + "'config':{'url':'http://127.0.0.1:9/mentions'},'events':['mention']}")).channel().id();
        UUID alertsOnly = channelService.create(node("{'name':'alerts','type':'webhook',"
                + "'config':{'url':'http://127.0.0.1:9/alerts'},'events':['alert']}")).channel().id();
        Person jo = person("jo-m", Scope.READ, null);
        Person sam = person("sam-m", Scope.READ, null);
        String ref = "record:" + AskTestSources.QUAKES + "/c2";
        assertThat(call(jo.bearer(), json(post(objectPath(ref, "/annotations")), "{'kind':'note','body':'cc @sam-m'}"))
                .getStatus()).isEqualTo(201);
        assertThat(inboxEvents(sam.id())).containsExactly(MeEvents.MENTION);
        assertThat(deliveries.findAll()).filteredOn(d -> d.getChannelId().equals(subscribed))
                .extracting(NotificationDeliveryEntity::getEvent).containsExactly("mention");
        assertThat(deliveries.findAll()).filteredOn(d -> d.getChannelId().equals(alertsOnly)).isEmpty();
        channelService.delete(subscribed);
        channelService.delete(alertsOnly);
    }
}
