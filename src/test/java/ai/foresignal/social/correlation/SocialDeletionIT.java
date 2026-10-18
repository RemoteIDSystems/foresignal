package ai.foresignal.social.correlation;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.ingest.FetchLogEntry;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.ingest.IngestRecord;
import ai.foresignal.social.SocialCollector;
import ai.foresignal.social.SocialPlatformService;
import ai.foresignal.social.SocialPost;
import ai.foresignal.social.SocialPosts;
import ai.foresignal.social.StoredPosts;
import ai.foresignal.social.narrative.NarrativeStore;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.FetchTrigger;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.social.CorrelationTestSupport;
import ai.foresignal.testsupport.social.SocialTestSupport;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Deletion sync (prompts 135–137, checkpoint 139 focus area 14): a post deleted upstream is stored as a tombstoned
 * version (text, links and media removed, {@code deleted: true}, author and topics kept), the record history keeps
 * both versions, deletion sync stops re-checking it and never tombstones it twice, and the chronicle snapshot of a
 * narrative that cited it keeps the claim history while the post's text is gone.
 */
@IntegrationTest
class SocialDeletionIT {

    static final Instant T0 = Instant.now().truncatedTo(ChronoUnit.HOURS).minus(Duration.ofHours(3));
    static final String SOURCE = "bluesky-jetstream";
    static final String POST_ID = "at://did:plc:a/app.bsky.feed.post/1";
    static final String NARRATIVE = "01JA8Q3V7S2K9W4T6Y1B5N0M4C";
    static final String CLAIM = "01JA8Q3V7S2K9W4T6Y1B5N0M4D";
    static final String TEXT = "Explosion at the port of Aden https://example.org/aden";

    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired IngestQueue queue;
    @Autowired SourceService sources;
    @Autowired SocialPlatformService platforms;
    @Autowired StoredPosts stored;
    @Autowired NarrativeStore narratives;
    @Autowired ChronicleSocialWriter chronicles;

    SocialPost post;

    @BeforeEach
    void setUp() throws Exception {
        clock.setInstant(T0.plus(Duration.ofHours(1)));
        SocialTestSupport.clean(jdbc);
        sources.seedDefaults();
        platforms.seed();
        post = new SocialPost("bluesky", POST_ID, null,
                new SocialPost.Author("a.bsky.social", null, "did:plc:a", null, null, null), TEXT, "en", T0, null,
                null, null, List.of("https://example.org/aden"), List.of(), List.of(), List.of(), null, null, null,
                "stream", null);
        submit(SocialPosts.record(post, "a".repeat(64), List.of("red-sea"), T0.plusSeconds(5)), 1);
        CorrelationTestSupport.narrative(narratives, NARRATIVE, T0, 3, List.of(), 5.0);
        jdbc.update("UPDATE social_narrative SET central_post = ? WHERE id = ?", SOURCE + "/" + post.externalId(),
                NARRATIVE);
        CorrelationTestSupport.claim(narratives, CLAIM, NARRATIVE, "explosion", "conflict", T0, 3);
    }

    @Test
    @DisplayName("a deleted post is tombstoned once: text, links and media removed, author and topics kept,"
            + " history kept, no longer a deletion candidate")
    void tombstonesOnce() throws Exception {
        assertThat(stored.deletionCandidates(SOURCE, T0.minus(Duration.ofDays(1)), null, 10))
                .extracting(StoredPosts.Candidate::externalId).containsExactly(post.externalId());

        SocialCollector collector = collector();
        NormalizedRecord tombstone = collector.observeDelete(SOURCE, "bluesky", POST_ID);
        assertThat(tombstone).as("a post the source stored is tombstoned").isNotNull();
        submit(tombstone, 2);

        Map<String, Object> head = stored.payload(SOURCE, post.externalId());
        assertThat(head).isNotNull();
        assertThat(head.get("deleted")).isEqualTo(true);
        assertThat(head.get("text")).isNull();
        assertThat((List<?>) head.get("links")).isEmpty();
        assertThat((List<?>) head.get("media")).isEmpty();
        assertThat(head.get("authorKey")).isEqualTo("a".repeat(64));
        assertThat(head.get("topicIds")).isEqualTo(List.of("red-sea"));

        Long versions = jdbc.queryForObject("SELECT count(*) FROM record_version WHERE source_id = ? AND"
                + " external_id = ?", Long.class, SOURCE, post.externalId());
        assertThat(versions).as("the record history keeps the original and the tombstoned version").isEqualTo(2L);
        assertThat(stored.deletionCandidates(SOURCE, T0.minus(Duration.ofDays(1)), null, 10)).isEmpty();
        assertThat(stored.tombstonedSince(SOURCE, Instant.EPOCH)).isEqualTo(1L);
        assertThat(collector().observeDelete(SOURCE, "bluesky", POST_ID))
                .as("an already deleted post is not tombstoned again").isNull();
        assertThat(collector().observeDelete(SOURCE, "bluesky", "at://did:plc:a/app.bsky.feed.post/never-stored"))
                .as("a post the source never stored is not tombstoned").isNull();
    }

    @Test
    @DisplayName("the chronicle snapshot keeps the narrative's claim history after its central post is deleted")
    void chronicleKeepsClaimHistory() throws Exception {
        NarrativeStore.Narrative n = narratives.narrative(NARRATIVE).orElseThrow();
        Map<String, Object> before = chronicles.snapshot(n, true);
        assertThat(before.get("centralPostText")).isEqualTo(TEXT);
        assertThat((List<?>) before.get("claims")).hasSize(1);

        submit(collector().observeDelete(SOURCE, "bluesky", POST_ID), 2);

        Map<String, Object> after = chronicles.snapshot(narratives.narrative(NARRATIVE).orElseThrow(), true);
        assertThat(after.get("centralPostText")).as("the deleted post's text is gone").isNull();
        assertThat((List<?>) after.get("claims")).as("the claim history stays").hasSize(1);
        @SuppressWarnings("unchecked")
        Map<String, Object> claim = (Map<String, Object>) ((List<?>) after.get("claims")).get(0);
        assertThat(claim.get("id")).isEqualTo(CLAIM);
        assertThat(claim.get("evidenceGroupCount")).isEqualTo(3);
        assertThat(narratives.claims(NARRATIVE)).hasSize(1);
    }

    /** A collector over the red-sea topic reading the stored posts. */
    private SocialCollector collector() {
        return SocialTestSupport.collector(SocialTestSupport.index(Map.of("red-sea",
                SocialTestSupport.block(List.of("aden"), List.of()))), stored, clock);
    }

    /** Ingests one record into the Jetstream source and waits for it. */
    private void submit(NormalizedRecord record, long position) throws Exception {
        queue.submit(SOURCE, FetchLogEntry.ok(SOURCE, FetchTrigger.MANUAL, T0, T0),
                List.of(new IngestRecord(record, T0, position)), Duration.ofSeconds(5)).get(10, TimeUnit.SECONDS);
    }
}
