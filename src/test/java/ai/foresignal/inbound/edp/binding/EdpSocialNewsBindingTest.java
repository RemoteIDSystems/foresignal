package ai.foresignal.inbound.edp.binding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.inbound.edp.EdpDescriptorBuilder;
import ai.foresignal.inbound.edp.EdpManifestModel;
import ai.foresignal.social.SocialCollector;
import ai.foresignal.social.SocialPost;
import ai.foresignal.social.SocialVocabulary;
import ai.foresignal.source.formats.news.ArticleUrls;
import ai.foresignal.source.formats.news.NewsArticles;
import ai.foresignal.source.formats.news.NewsOutletLookup;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;

/**
 * The social and news targets (prompt 138): {@code social-post} documents become 135 posts of collection
 * {@code import} (sampled, never baselined, unless the stream is declared complete) written under the provider, and
 * {@code article} documents become 133 news articles with the outlet resolved from the URL's domain.
 */
class EdpSocialNewsBindingTest {

    /** A social-post document item. */
    private static BoundItem post(Map<String, Object> extra) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("documentType", "social-post");
        p.put("text", "Explosion reported near the port");
        p.put("url", "https://t.me/example/123");
        p.put("author", Map.of("handle", "example", "name", "Example channel", "accountId", "1001"));
        p.put("language", "en");
        p.put("publishedAt", "2026-10-18T05:30:00Z");
        p.put("media", List.of(Map.of("type", "video", "url", "https://t.me/v.mp4"), Map.of("url", "https://x/a.bin")));
        p.put("engagement", Map.of("views", 1200, "reposts", 4));
        p.put("repostOf", Map.of("postId", "99"));
        p.put("channel", "example");
        p.putAll(extra);
        return BindingFixtures.item("posts", "123", "document", 20, p, 56.4, 26.5);
    }

    @Test
    @DisplayName("post(): author, import collection, createdAt from publishedAt, media, engagement, repost and geo")
    void postMapping() {
        SocialPost post = SocialBindingTarget.post("telegram", post(Map.of()));
        assertThat(post.platform()).isEqualTo("telegram");
        assertThat(post.postId()).isEqualTo("123");
        assertThat(post.externalId()).isEqualTo("telegram:123");
        assertThat(post.collection()).isEqualTo(SocialVocabulary.COLLECTION_IMPORT);
        assertThat(post.author().handle()).isEqualTo("example");
        assertThat(post.author().accountId()).isEqualTo("1001");
        assertThat(post.author().displayName()).isEqualTo("Example channel");
        assertThat(post.createdAt()).isEqualTo(Instant.parse("2026-10-18T05:30:00Z"));
        assertThat(post.media()).extracting(SocialPost.Media::type).containsExactly("video", "other");
        assertThat(post.engagement().views()).isEqualTo(1200L);
        assertThat(post.engagement().reposts()).isEqualTo(4L);
        assertThat(post.repostOf().platform()).isEqualTo("telegram");
        assertThat(post.repostOf().postId()).isEqualTo("99");
        assertThat(post.geo().lon()).isEqualTo(56.4);
        assertThat(post.channel()).isEqualTo("example");
    }

    @Test
    @DisplayName("platform from the binding, else properties.platform; plan skips other documents and refuses bad input")
    void platformsAndPlans() {
        SocialBindingTarget target = new SocialBindingTarget(provider(mock(SocialCollector.class)),
                mock(BindingIngest.class));
        BoundStream stream = BindingFixtures.stream("posts");
        ExternalBindingEntity configured = BindingFixtures.binding("posts", "social", Map.of("platform", "telegram"));
        ExternalBindingEntity bare = BindingFixtures.binding("posts", "social", Map.of());
        assertThat(SocialBindingTarget.platform(configured, post(Map.of()))).isEqualTo("telegram");
        assertThat(SocialBindingTarget.platform(bare, post(Map.of("platform", "tiktok")))).isEqualTo("tiktok");
        assertThat(SocialBindingTarget.platform(bare, post(Map.of("platform", "Not Valid")))).isNull();
        assertThat(target.plan(configured, stream, post(Map.of())).targetRef()).endsWith("/telegram:123");
        assertThat(target.plan(configured, stream, post(Map.of("documentType", "note"))).action())
                .isEqualTo("skip:document-type-note");
        assertThat(target.plan(bare, stream, post(Map.of())).failed()).isTrue();
        ExternalBindingEntity collision = BindingFixtures.binding("posts", "social", Map.of("platform", "posts"));
        assertThat(target.plan(collision, stream, post(Map.of())).failed()).isTrue();
        assertThat(target.validate(Map.of("platform", "Bad Name"), stream)).isNotEmpty();
        assertThat(target.validate(Map.of("platform", "telegram"), stream)).isEmpty();
    }

    @Test
    @DisplayName("apply: sampled and no denominator unless the stream is complete; the record is written under the provider")
    void applyCollectionType() {
        for (boolean complete : new boolean[] {false, true}) {
            SocialCollector collector = mock(SocialCollector.class);
            NormalizedRecord record = NormalizedRecord.builder("telegram:123", SocialVocabulary.KIND).build();
            when(collector.observe(any(), anyString(), anyString(), anyBoolean()))
                    .thenReturn(new SocialCollector.Observation(record, null, "en", "k"));
            BindingIngest ingest = mock(BindingIngest.class);
            SocialBindingTarget target = new SocialBindingTarget(provider(collector), ingest);
            BindingEffect effect = target.apply(BindingFixtures.binding("posts", "social",
                    Map.of("platform", "telegram")), BindingFixtures.stream("posts", complete), post(Map.of()));
            assertThat(effect.action()).isEqualTo("social-post");
            ArgumentCaptor<SocialPost> posted = ArgumentCaptor.forClass(SocialPost.class);
            verify(collector).observe(posted.capture(), eq(BindingFixtures.SOURCE),
                    eq(complete ? SocialVocabulary.COMPLETE : SocialVocabulary.SAMPLED), eq(complete));
            assertThat(posted.getValue().collection()).isEqualTo("import");
            verify(ingest).write(BindingFixtures.SOURCE, record);
        }
        assertThat(SocialVocabulary.feedsBaselines(SocialVocabulary.SAMPLED)).isFalse();
        assertThat(SocialVocabulary.feedsBaselines(SocialVocabulary.COMPLETE)).isTrue();
    }

    @Test
    @DisplayName("apply: a post the collector does not keep is not written")
    void notKept() {
        SocialCollector collector = mock(SocialCollector.class);
        when(collector.observe(any(), anyString(), anyString(), anyBoolean()))
                .thenReturn(new SocialCollector.Observation(null, null, "en", "k"));
        BindingIngest ingest = mock(BindingIngest.class);
        BindingEffect effect = new SocialBindingTarget(provider(collector), ingest).apply(
                BindingFixtures.binding("posts", "social", Map.of("platform", "telegram")),
                BindingFixtures.stream("posts"), post(Map.of()));
        assertThat(effect.skipped()).isTrue();
        verify(ingest, never()).write(any(), any());
    }

    // ------------------------------------------------------------------------- news

    /** An article document item. */
    private static BoundItem article(Map<String, Object> extra) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("documentType", "article");
        p.put("text", "x".repeat(800));
        p.put("title", "Port closed after incident");
        p.put("url", "https://www.Example.org/news/port-closed?utm_source=feed");
        p.put("language", "en");
        p.put("publishedAt", "2026-10-18T04:00:00Z");
        p.put("media", List.of(Map.of("type", "image", "url", "https://example.org/a.jpg")));
        p.putAll(extra);
        return BindingFixtures.item("articles", "a-1", "document", 30, p);
    }

    @Test
    @DisplayName("article(): canonical URL, outlet by domain, 500-character summary, title fallback, seenAt = receipt")
    void articleMapping() {
        NewsOutletLookup outlets = mock(NewsOutletLookup.class);
        String url = "https://www.Example.org/news/port-closed?utm_source=feed";
        when(outlets.resolve(eq(ArticleUrls.domain(ArticleUrls.canonical(url))), any(), eq("en")))
                .thenReturn("outlet-example");
        ExternalBindingEntity binding = BindingFixtures.binding("articles", "news", Map.of("sourceCountry", "GB"));
        NewsArticles.Article a = NewsBindingTarget.article(binding, article(Map.of()), outlets);
        assertThat(a.url()).isEqualTo(ArticleUrls.canonical(url));
        assertThat(a.outletId()).isEqualTo("outlet-example");
        assertThat(a.summary()).hasSize(NewsArticles.MAX_SUMMARY);
        assertThat(a.seenAt()).isEqualTo(BindingFixtures.RECEIVED);
        assertThat(a.publishedAt()).isEqualTo(Instant.parse("2026-10-18T04:00:00Z"));
        assertThat(a.sourceCountry()).isEqualTo("GB");
        assertThat(a.imageUrl()).isEqualTo("https://example.org/a.jpg");
        Map<String, Object> untitled = new LinkedHashMap<>();
        untitled.put("title", null);
        untitled.put("text", "Short body text");
        assertThat(NewsBindingTarget.title(article(untitled))).isEqualTo("Short body text");
    }

    @Test
    @DisplayName("plan refuses an article without a URL; apply writes a news-article record under the provider")
    void newsPlanAndApply() {
        BindingIngest ingest = mock(BindingIngest.class);
        NewsBindingTarget target = new NewsBindingTarget(provider(mock(NewsOutletLookup.class)), ingest);
        BoundStream stream = BindingFixtures.stream("articles");
        ExternalBindingEntity binding = BindingFixtures.binding("articles", "news", Map.of());
        Map<String, Object> noUrl = new LinkedHashMap<>();
        noUrl.put("url", null);
        assertThat(target.plan(binding, stream, article(noUrl)).failed()).isTrue();
        assertThat(target.plan(binding, stream, article(Map.of("documentType", "report"))).skipped()).isTrue();
        BindingEffect effect = target.apply(binding, stream, article(Map.of()));
        assertThat(effect.action()).isEqualTo("news-article");
        ArgumentCaptor<NormalizedRecord> written = ArgumentCaptor.forClass(NormalizedRecord.class);
        verify(ingest).write(eq(BindingFixtures.SOURCE), written.capture());
        assertThat(written.getValue().kind()).isEqualTo(NewsArticles.KIND);
        assertThat(written.getValue().payload()).doesNotContainKeys("author", "byline");
        assertThat(target.validate(Map.of("sourceCountry", "gb"), stream)).isNotEmpty();
    }

    @Test
    @DisplayName("the descriptor declares the derived kinds only for document streams that may carry them")
    void derivedKinds() {
        EdpManifestModel manifest = BindingFixtures.manifest(false);
        assertThat(EdpDescriptorBuilder.derivedKinds(manifest))
                .containsExactlyInAnyOrder(EdpDescriptorBuilder.SOCIAL_POST_KIND, EdpDescriptorBuilder.NEWS_ARTICLE_KIND);
        SourceDescriptor d = EdpDescriptorBuilder.build(manifest, 60);
        assertThat(d.kinds()).contains("social-post", "news-article");
        assertThat(d.hiddenKinds()).contains("social-post", "news-article");
        EdpManifestModel notesOnly = EdpManifestModel.parse(BindingFixtures.JSON.readTree("""
            {"protocol": "foresignal-edp/1.0", "source": {"id": "notes-lab", "name": "Notes", "publisher": {"name": "T"},
             "originator": {"id": "notes-lab", "class": "operator"}}, "transport": {"push": true},
             "streams": [{"id": "notes", "mode": "atomic", "itemType": "document", "kind": "lab-note",
               "documentType": "note"}]}
            """));
        assertThat(EdpDescriptorBuilder.derivedKinds(notesOnly)).isEmpty();
        assertThat(EdpDescriptorBuilder.build(notesOnly, 60).kinds()).containsExactly("lab-note");
    }

    /** An object provider of {@code value}. */
    @SuppressWarnings("unchecked")
    private static <T> ObjectProvider<T> provider(T value) {
        ObjectProvider<T> p = mock(ObjectProvider.class);
        when(p.getIfAvailable()).thenReturn(value);
        return p;
    }
}
