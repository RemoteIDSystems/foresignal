package ai.foresignal.source.adapters.bluesky;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.social.SocialCollector;
import ai.foresignal.social.SocialPosts;
import ai.foresignal.social.SocialRuntime;
import ai.foresignal.social.StoredPosts;
import ai.foresignal.social.StreamLedger;
import ai.foresignal.social.TermIndex;
import ai.foresignal.social.VolumeAccumulator;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.social.SocialStreamContext;
import ai.foresignal.testsupport.social.SocialTestSupport;
import ai.foresignal.testsupport.websocket.LoopbackWebSocketServer;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code bluesky-jetstream} against a loopback WebSocket (prompt 135): Jetstream v2 post and repost parsing, local term
 * matching and denominators, the seq cursor (and the v1 {@code time_us} shape), resume within the 36-hour window with
 * the gap recorded as replayed, a too-old cursor recorded as not replayed, delete events tombstoning stored posts, and
 * a reconnect cutover that neither drops nor duplicates events.
 */
class JetstreamAdapterTest {

    static final Instant NOW = Instant.parse("2026-10-16T12:30:00Z");
    static final String DID = "did:plc:abc123";

    private static String post(long seq, String rkey, String text, String langs) {
        return "{\"$type\":\"message\",\"payload\":{\"$type\":\"network.bsky.jetstream.subscribeEvents#commit\","
                + "\"seq\":" + seq + ",\"did\":\"" + DID + "\",\"time\":\"2026-10-16T12:29:00Z\",\"rev\":\"r1\","
                + "\"operation\":\"create\",\"collection\":\"app.bsky.feed.post\",\"rkey\":\"" + rkey + "\","
                + "\"record\":{\"$type\":\"app.bsky.feed.post\",\"text\":\"" + text + "\",\"createdAt\":"
                + "\"2026-10-16T12:28:00Z\",\"langs\":[\"" + langs + "\"],\"facets\":[{\"index\":{\"byteStart\":0,"
                + "\"byteEnd\":4},\"features\":[{\"$type\":\"app.bsky.richtext.facet#link\",\"uri\":"
                + "\"https://example.org/a\"}]}]},\"cid\":\"bafy\"}}";
    }

    private static String repost(long seq, String subjectRkey) {
        return "{\"$type\":\"message\",\"payload\":{\"seq\":" + seq + ",\"did\":\"did:plc:other\",\"time\":"
                + "\"2026-10-16T12:29:30Z\",\"rev\":\"r2\",\"operation\":\"create\",\"collection\":"
                + "\"app.bsky.feed.repost\",\"rkey\":\"rp" + seq + "\",\"record\":{\"subject\":{\"uri\":\"at://" + DID
                + "/app.bsky.feed.post/" + subjectRkey + "\",\"cid\":\"bafy\"},\"createdAt\":\"2026-10-16T12:29:30Z\"}}}";
    }

    private static String delete(long seq, String rkey) {
        return "{\"$type\":\"message\",\"payload\":{\"seq\":" + seq + ",\"did\":\"" + DID + "\",\"time\":"
                + "\"2026-10-16T12:29:40Z\",\"rev\":\"r3\",\"operation\":\"delete\",\"collection\":"
                + "\"app.bsky.feed.post\",\"rkey\":\"" + rkey + "\"}}";
    }

    private static void await(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        while (System.nanoTime() < deadline && !condition.getAsBoolean()) {
            Thread.sleep(20);
        }
    }

    private static String param(URI uri, String name) {
        if (uri.getRawQuery() == null) {
            return null;
        }
        for (String pair : uri.getRawQuery().split("&")) {
            if (pair.startsWith(name + "=")) {
                return pair.substring(name.length() + 1);
            }
        }
        return null;
    }

    private record Setup(BlueskyJetstreamAdapter adapter, SocialCollector collector, StreamLedger ledger,
            StoredPosts stored, SocialStreamContext ctx) {}

    private static Setup setup(LoopbackWebSocketServer server) {
        TermIndex index = SocialTestSupport.index(Map.of("red-sea", SocialTestSupport.block(
                List.of("Bab el-Mandeb"), List.of())));
        StoredPosts stored = mock(StoredPosts.class);
        MutableClock clock = new MutableClock(NOW);
        SocialCollector collector = SocialTestSupport.collector(index, stored, clock);
        StreamLedger ledger = mock(StreamLedger.class);
        SocialRuntime runtime = SocialTestSupport.runtime(collector, ledger, stored);
        BlueskyJetstreamAdapter adapter = new BlueskyJetstreamAdapter(server.uri().toString(), runtime);
        return new Setup(adapter, collector, ledger, stored, new SocialStreamContext(adapter.descriptor(), clock, null));
    }

    @Test
    @DisplayName("posts match locally and are stored; every post counts for the denominator; reposts count")
    void parsingMatchingAndDenominators() throws Exception {
        try (LoopbackWebSocketServer server = LoopbackWebSocketServer.start(false)) {
            Setup s = setup(server);
            List<NormalizedRecord> sink = new CopyOnWriteArrayList<>();
            StreamHandle handle = s.adapter().start(s.ctx(), sink::add);
            try {
                server.awaitConnection(Duration.ofSeconds(5));
                URI uri = s.ctx().opened().get(0);
                assertThat(uri.getPath()).endsWith("/xrpc/network.bsky.jetstream.subscribeEvents");
                assertThat(uri.getRawQuery()).contains("kinds=commit").contains("collections=app.bsky.feed.post")
                        .contains("collections=app.bsky.feed.repost").doesNotContain("cursor=");
                assertThat(s.ctx().openHeaders().get(0)).containsEntry("Sec-WebSocket-Protocol", "xrpc.v1.json");
                server.sendText(post(101, "3k1", "Tanker hit near Bab el-Mandeb", "en"));
                server.sendText(post(102, "3k2", "Nothing about shipping here", "en"));
                server.sendText(repost(103, "3k1"));
                await(() -> sink.size() == 1);
                Thread.sleep(200);
                assertThat(sink).hasSize(1);
                NormalizedRecord r = sink.get(0);
                assertThat(r.externalId()).isEqualTo("bluesky:at://" + DID + "/app.bsky.feed.post/3k1");
                assertThat(r.payload()).containsEntry("platform", "bluesky").containsEntry("language", "en")
                        .containsEntry("collection", "stream").containsEntry("topicIds", List.of("red-sea"))
                        .containsEntry("links", List.of("https://example.org/a"))
                        .containsEntry("url", "https://bsky.app/profile/" + DID + "/post/3k1");
                assertThat(SocialPosts.authorKeyOf(r.payload())).hasSize(64);
                Instant hour = Instant.parse("2026-10-16T12:00:00Z");
                VolumeAccumulator acc = s.collector().accumulator();
                assertThat(acc.denominator(new VolumeAccumulator.DenominatorKey("bluesky", "en", hour))).isEqualTo(2);
                VolumeAccumulator.Drain drain = acc.drain(NOW);
                assertThat(drain.volumes()).singleElement().satisfies(d -> {
                    assertThat(d.key().scope()).isEqualTo("topic:red-sea");
                    assertThat(d.posts()).isEqualTo(1);
                    assertThat(d.uniqueAuthors()).isEqualTo(1);
                    assertThat(d.reposts()).isEqualTo(1);
                });
            } finally {
                handle.close();
            }
            verify(s.ledger()).connected(eq(BlueskyJetstreamAdapter.ID), any(), eq(false));
            assertThat(s.ctx().stateValues()).containsEntry("jetstream:seq", "103");
        }
    }

    @Test
    @DisplayName("a stored cursor within 35 h resumes with cursor=seq, drops the inclusive duplicate, gap replayed")
    void cursorResume() throws Exception {
        try (LoopbackWebSocketServer server = LoopbackWebSocketServer.start(false)) {
            Setup s = setup(server);
            s.ctx().stateValues().put("jetstream:seq", "500");
            s.ctx().stateValues().put("jetstream:time", NOW.minus(Duration.ofHours(2)).toString());
            List<NormalizedRecord> sink = new CopyOnWriteArrayList<>();
            StreamHandle handle = s.adapter().start(s.ctx(), sink::add);
            try {
                server.awaitConnection(Duration.ofSeconds(5));
                assertThat(param(s.ctx().opened().get(0), "cursor")).isEqualTo("500");
                server.sendText(post(500, "dup", "Bab el-Mandeb again", "en"));
                server.sendText(post(501, "new", "Bab el-Mandeb update", "en"));
                await(() -> sink.size() == 1);
                Thread.sleep(200);
                assertThat(sink).extracting(NormalizedRecord::externalId)
                        .containsExactly("bluesky:at://" + DID + "/app.bsky.feed.post/new");
            } finally {
                handle.close();
            }
            verify(s.ledger()).gap(eq(BlueskyJetstreamAdapter.ID), eq(NOW.minus(Duration.ofHours(2))), eq(NOW),
                    eq(true), anyString());
            verify(s.ledger()).connected(eq(BlueskyJetstreamAdapter.ID), any(), eq(true));
        }
    }

    @Test
    @DisplayName("a cursor older than the window connects live and records the gap as not replayed")
    void cursorTooOld() throws Exception {
        try (LoopbackWebSocketServer server = LoopbackWebSocketServer.start(false)) {
            Setup s = setup(server);
            s.ctx().stateValues().put("jetstream:seq", "500");
            s.ctx().stateValues().put("jetstream:time", NOW.minus(Duration.ofHours(40)).toString());
            StreamHandle handle = s.adapter().start(s.ctx(), r -> {});
            try {
                server.awaitConnection(Duration.ofSeconds(5));
                assertThat(param(s.ctx().opened().get(0), "cursor")).isNull();
            } finally {
                handle.close();
            }
            verify(s.ledger()).gap(eq(BlueskyJetstreamAdapter.ID), eq(NOW.minus(Duration.ofHours(40))), eq(NOW),
                    eq(false), anyString());
        }
    }

    @Test
    @DisplayName("a delete of a stored post emits its tombstoned version: no text, no media, deleted true")
    void deleteTombstones() throws Exception {
        try (LoopbackWebSocketServer server = LoopbackWebSocketServer.start(false)) {
            Setup s = setup(server);
            String externalId = "bluesky:at://" + DID + "/app.bsky.feed.post/gone";
            when(s.stored().externalIds(eq(BlueskyJetstreamAdapter.ID), anyInt())).thenReturn(List.of(externalId));
            Map<String, Object> payload = new java.util.LinkedHashMap<>();
            payload.put("platform", "bluesky");
            payload.put("postId", "at://" + DID + "/app.bsky.feed.post/gone");
            payload.put("text", "Bab el-Mandeb secret");
            payload.put("media", List.of(Map.of("type", "image", "url", "https://example.org/i.jpg")));
            payload.put("createdAt", "2026-10-16T11:00:00Z");
            payload.put("deleted", false);
            when(s.stored().payload(BlueskyJetstreamAdapter.ID, externalId)).thenReturn(payload);
            List<NormalizedRecord> sink = new CopyOnWriteArrayList<>();
            StreamHandle handle = s.adapter().start(s.ctx(), sink::add);
            try {
                server.awaitConnection(Duration.ofSeconds(5));
                server.sendText(delete(700, "gone"));
                server.sendText(delete(701, "never-stored"));
                await(() -> sink.size() == 1);
                Thread.sleep(200);
            } finally {
                handle.close();
            }
            assertThat(sink).singleElement().satisfies(r -> {
                assertThat(r.externalId()).isEqualTo(externalId);
                assertThat(r.payload()).containsEntry("deleted", true).containsEntry("media", List.of())
                        .doesNotContainKey("text");
                assertThat(r.tombstone()).as("a tombstoned version, not a record tombstone").isFalse();
            });
        }
    }

    @Test
    @DisplayName("reconnect cutover: the resumed stream re-sends from the cursor; nothing is lost or duplicated")
    void reconnectCutover() throws Exception {
        try (LoopbackWebSocketServer server = LoopbackWebSocketServer.start(false)) {
            Setup s = setup(server);
            List<NormalizedRecord> sink = new CopyOnWriteArrayList<>();
            StreamHandle first = s.adapter().start(s.ctx(), sink::add);
            server.awaitConnection(Duration.ofSeconds(5));
            server.sendText(post(10, "a", "Bab el-Mandeb one", "en"));
            server.sendText(post(11, "b", "Bab el-Mandeb two", "en"));
            await(() -> sink.size() == 2);
            first.close();
            assertThat(s.ctx().stateValues()).containsEntry("jetstream:seq", "11");

            StreamHandle second = s.adapter().start(s.ctx(), sink::add);
            try {
                await(() -> server.openConnections().size() == 1 && s.ctx().opened().size() == 2);
                assertThat(param(s.ctx().opened().get(1), "cursor")).isEqualTo("11");
                server.sendText(post(11, "b", "Bab el-Mandeb two", "en"));
                server.sendText(post(12, "c", "Bab el-Mandeb three", "en"));
                await(() -> sink.size() == 3);
                Thread.sleep(200);
            } finally {
                second.close();
            }
            assertThat(sink).extracting(NormalizedRecord::externalId).containsExactly(
                    "bluesky:at://" + DID + "/app.bsky.feed.post/a", "bluesky:at://" + DID + "/app.bsky.feed.post/b",
                    "bluesky:at://" + DID + "/app.bsky.feed.post/c");
            verify(s.ledger(), atLeastOnce()).gap(eq(BlueskyJetstreamAdapter.ID), any(), any(), eq(true),
                    anyString());
            verify(s.ledger(), never()).disconnected(eq(BlueskyJetstreamAdapter.ID), any(), any());
        }
    }

    @Test
    @DisplayName("the v1 shape (kind commit, time_us, commit{...}) is read too")
    void v1Shape() throws Exception {
        try (LoopbackWebSocketServer server = LoopbackWebSocketServer.start(false)) {
            Setup s = setup(server);
            List<NormalizedRecord> sink = new CopyOnWriteArrayList<>();
            StreamHandle handle = s.adapter().start(s.ctx(), sink::add);
            try {
                server.awaitConnection(Duration.ofSeconds(5));
                server.sendText("{\"did\":\"" + DID + "\",\"time_us\":1760617740000000,\"kind\":\"commit\",\"commit\":"
                        + "{\"rev\":\"x\",\"operation\":\"create\",\"collection\":\"app.bsky.feed.post\",\"rkey\":\"v1\","
                        + "\"record\":{\"text\":\"Bab el-Mandeb v1\",\"createdAt\":\"2026-10-16T12:00:00Z\"},"
                        + "\"cid\":\"c\"}}");
                await(() -> sink.size() == 1);
            } finally {
                handle.close();
            }
            assertThat(sink).hasSize(1);
            assertThat(s.ctx().stateValues()).containsEntry("jetstream:seq", "1760617740000000");
        }
    }

    @Test
    @DisplayName("the descriptor: STREAMING, social-post, 30-day retention, no required credential")
    void descriptor() {
        var d = new BlueskyJetstreamAdapter("", (SocialRuntime) null).descriptor();
        assertThat(d.kinds()).containsExactly("social-post");
        assertThat(d.defaultRetention()).isEqualTo(Duration.ofDays(30));
        assertThat(d.credentialFields()).allSatisfy(f -> assertThat(f.required()).isFalse());
        assertThat(BlueskyJetstreamAdapter.uri("wss://jetstream.us-east.bsky.network", 42L).toString()).isEqualTo(
                "wss://jetstream.us-east.bsky.network/xrpc/network.bsky.jetstream.subscribeEvents?kinds=commit"
                        + "&collections=app.bsky.feed.post&collections=app.bsky.feed.repost&cursor=42");
    }
}
