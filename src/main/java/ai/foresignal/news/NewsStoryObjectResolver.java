package ai.foresignal.news;

import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectResolver;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.ResolveContext;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Resolves {@code news-story:{id}} (prompt 133, contract 1.4): the story's deterministic title, its article, outlet and
 * evidence-group counts, blindspot and corroboration, and the sources of its articles. Stories carry no geometry and
 * are never private (the news sources are public).
 */
@Component
public class NewsStoryObjectResolver implements ObjectResolver {

    private final NewsStoryStore store;

    /** Creates the resolver. */
    public NewsStoryObjectResolver(NewsStoryStore store) {
        this.store = store;
    }

    @Override
    public String type() {
        return ObjectTypes.NEWS_STORY;
    }

    @Override
    public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
        return store.story(ref.id()).filter(s -> context.existedAt(s.createdAt())).map(s -> {
            Set<String> sources = new LinkedHashSet<>();
            for (NewsStoryStore.Article a : store.articles(s.id())) {
                sources.add(a.sourceId());
            }
            List<String> readable = new ArrayList<>();
            for (String source : sources) {
                if (context.caller().canRead(source)) {
                    readable.add(source);
                }
            }
            String summary = s.articleCount() + " articles from " + s.outletCount() + " outlets in "
                    + s.evidenceGroupCount() + " independent evidence groups"
                    + (s.blindspot() == null ? "" : "; " + s.blindspot() + "-side blindspot")
                    + (s.corroborated() ? "; corroborated by non-news evidence" : "");
            return ObjectEnvelope.builder(ref)
                    .title(s.title())
                    .summary(summary)
                    .status(s.corroborated() ? "corroborated" : "uncorroborated")
                    .time(s.firstSeenAt(), s.lastSeenAt(), s.lastSeenAt())
                    .privateObject(false)
                    .provenance(readable, null, null)
                    .self("/v1/news/stories/" + s.id())
                    .build();
        });
    }
}
