package ai.foresignal.inbound.edp.binding;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.source.formats.news.ArticleUrls;
import ai.foresignal.source.formats.news.NewsArticles;
import ai.foresignal.source.formats.news.NewsOutletLookup;
import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Target {@code news} (prompt 138): {@code article} documents become 133 {@code news-article} records (external id
 * the SHA-256 of the canonical URL, written under the provider), with the outlet resolved from the URL's domain, so
 * stories, coverage and blindspots include them. As everywhere in 133, no byline or author name is stored, and the
 * text is kept only as a 500-character summary; the provider's item keeps the full document.
 *
 * <p>Configuration: {@code sourceCountry} (ISO 3166-1 alpha-2, used when the outlet is new), optional. The item's
 * {@code properties.sourceCountry} wins over it.
 */
@Component
public class NewsBindingTarget implements BindingTargetHandler {

    private final ObjectProvider<NewsOutletLookup> outlets;
    private final BindingIngest ingest;

    /** Creates the target. */
    public NewsBindingTarget(ObjectProvider<NewsOutletLookup> outlets, BindingIngest ingest) {
        this.outlets = outlets;
        this.ingest = ingest;
    }

    @Override
    public String target() {
        return BindingVocabulary.NEWS;
    }

    @Override
    public List<FieldError> validate(Map<String, Object> config, BoundStream stream) {
        List<FieldError> errors = new ArrayList<>();
        String country = BindingConfig.text(config, "sourceCountry");
        BindingConfig.string(config, "sourceCountry", errors);
        if (country != null && !country.matches("^[A-Z]{2}$")) {
            errors.add(new FieldError("config.sourceCountry", "must be an ISO 3166-1 alpha-2 code"));
        }
        return errors;
    }

    @Override
    public BindingEffect plan(ExternalBindingEntity binding, BoundStream stream, BoundItem item) {
        String type = item.text("documentType");
        if (type != null && !"article".equals(type)) {
            return BindingEffect.skip(item, "document-type-" + type);
        }
        String canonical = ArticleUrls.canonical(item.text("url"));
        if (canonical == null) {
            return BindingEffect.error(item, "the article has no usable url");
        }
        if (title(item) == null) {
            return BindingEffect.error(item, "the article has neither a title nor text");
        }
        return BindingEffect.of(item, "news-article",
                ObjectRef.record(item.sourceId(), ArticleUrls.sha256(canonical), null).format());
    }

    @Override
    public BindingEffect apply(ExternalBindingEntity binding, BoundStream stream, BoundItem item) {
        BindingEffect planned = plan(binding, stream, item);
        if (planned.failed() || planned.skipped()) {
            return planned;
        }
        NormalizedRecord record = NewsArticles.record(article(binding, item, outlets.getIfAvailable()));
        if (record == null) {
            return BindingEffect.error(item, "the article could not be converted");
        }
        ingest.write(item.sourceId(), record);
        return planned;
    }

    /** The 133 article of an EDP {@code article} document; the outlet is resolved when a lookup is given. */
    static NewsArticles.Article article(ExternalBindingEntity binding, BoundItem item, NewsOutletLookup outlets) {
        String url = ArticleUrls.canonical(item.text("url"));
        String country = item.text("sourceCountry");
        if (country == null) {
            country = BindingConfig.text(binding.getConfig(), "sourceCountry");
        }
        String language = item.text("language");
        String outlet = null;
        if (outlets != null && url != null) {
            try {
                outlet = outlets.resolve(ArticleUrls.domain(url), country, language);
            } catch (RuntimeException e) {
                outlet = null;
            }
        }
        Instant seen = item.receivedAt() != null ? item.receivedAt() : item.observedAt();
        Instant published = BoundItem.time(item.payload().get("publishedAt"));
        return new NewsArticles.Article(title(item), url, outlet, language, country, seen, published, List.of(),
                NewsArticles.clip(item.text("text"), NewsArticles.MAX_SUMMARY), image(item));
    }

    /** The title, else the start of the text. */
    static String title(BoundItem item) {
        String title = item.text("title");
        return title != null ? title : NewsArticles.clip(item.text("text"), 200);
    }

    private static String image(BoundItem item) {
        if (item.payload().get("media") instanceof List<?> media) {
            for (Object m : media) {
                if (m instanceof Map<?, ?> mm && "image".equals(String.valueOf(mm.get("type")))
                        && mm.get("url") != null) {
                    return String.valueOf(mm.get("url"));
                }
            }
        }
        return null;
    }
}
