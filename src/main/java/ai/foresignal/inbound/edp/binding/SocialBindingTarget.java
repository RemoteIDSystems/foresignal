package ai.foresignal.inbound.edp.binding;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.social.SocialCollector;
import ai.foresignal.social.SocialPost;
import ai.foresignal.social.SocialVocabulary;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Target {@code social} (prompt 138): {@code social-post} documents become 135 {@code social-post} records
 * ({@code {platform}:{postId}}, written under the provider) through the {@link SocialCollector}, which salts the
 * author key, matches topics and counts volume, so 136 narratives and claims and 137 comparisons include them. The
 * collection is {@code import}: such posts are a sample that never feeds velocity baselines, unless the manifest
 * declares the stream {@code complete}, when they count as a complete path.
 *
 * <p>The platform is {@code config.platform}, else the document's {@code properties.platform}; the post id is the
 * provider's item id. Item properties may carry {@code channel}, {@code links}, {@code hashtags}, {@code mentions}
 * and {@code replyTo}/{@code quoteOf}/{@code repostOf} ({@code {platform?, postId, url?}}), so reposts inherit the
 * original's originator (116).
 */
@Component
public class SocialBindingTarget implements BindingTargetHandler {

    private final ObjectProvider<SocialCollector> collector;
    private final BindingIngest ingest;

    /** Creates the target. */
    public SocialBindingTarget(ObjectProvider<SocialCollector> collector, BindingIngest ingest) {
        this.collector = collector;
        this.ingest = ingest;
    }

    @Override
    public String target() {
        return BindingVocabulary.SOCIAL;
    }

    @Override
    public List<FieldError> validate(Map<String, Object> config, BoundStream stream) {
        List<FieldError> errors = new ArrayList<>();
        String platform = BindingConfig.text(config, "platform");
        BindingConfig.string(config, "platform", errors);
        if (platform != null && !SocialVocabulary.NETWORK.matcher(platform).matches()) {
            errors.add(new FieldError("config.platform", "must be a network slug, e.g. telegram"));
        }
        return errors;
    }

    @Override
    public BindingEffect plan(ExternalBindingEntity binding, BoundStream stream, BoundItem item) {
        String type = item.text("documentType");
        if (type != null && !"social-post".equals(type)) {
            return BindingEffect.skip(item, "document-type-" + type);
        }
        String platform = platform(binding, item);
        if (platform == null) {
            return BindingEffect.error(item, "no platform: set config.platform or the document's properties.platform");
        }
        if (item.text("text") == null) {
            return BindingEffect.error(item, "the document has no text");
        }
        if (SocialVocabulary.externalId(platform, item.itemId()).equals(item.externalId())) {
            return BindingEffect.error(item, "the stream id equals the platform '" + platform + "', so the converted"
                    + " post would replace the provider's item; name the stream differently");
        }
        return BindingEffect.of(item, "social-post",
                ObjectRef.record(item.sourceId(), SocialVocabulary.externalId(platform, item.itemId()), null).format());
    }

    @Override
    public BindingEffect apply(ExternalBindingEntity binding, BoundStream stream, BoundItem item) {
        BindingEffect planned = plan(binding, stream, item);
        if (planned.failed() || planned.skipped()) {
            return planned;
        }
        SocialCollector c = collector.getIfAvailable();
        if (c == null) {
            return BindingEffect.error(item, "the social pipeline is not available");
        }
        SocialPost post = post(platform(binding, item), item);
        String collectionType = stream.complete() ? SocialVocabulary.COMPLETE : SocialVocabulary.SAMPLED;
        SocialCollector.Observation observation = c.observe(post, item.sourceId(), collectionType, stream.complete());
        if (observation.record() == null) {
            return BindingEffect.skip(item, "not-kept");
        }
        ingest.write(item.sourceId(), observation.record());
        return planned;
    }

    /** The platform of a document: the binding's, else {@code properties.platform}; null when neither is valid. */
    static String platform(ExternalBindingEntity binding, BoundItem item) {
        String platform = BindingConfig.text(binding.getConfig(), "platform");
        if (platform == null) {
            platform = item.text("platform");
        }
        return platform != null && SocialVocabulary.NETWORK.matcher(platform).matches() ? platform : null;
    }

    /** The 135 post of an EDP {@code social-post} document, collection {@code import}. Pure. */
    static SocialPost post(String platform, BoundItem item) {
        Map<String, Object> p = item.payload();
        SocialPost.Author author = null;
        if (p.get("author") instanceof Map<?, ?> a) {
            String handle = text(a.get("handle"));
            String accountId = text(a.get("accountId"));
            if (handle != null || accountId != null) {
                author = new SocialPost.Author(handle != null ? handle : accountId, text(a.get("name")), accountId,
                        null, null, null);
            }
        }
        Instant published = BoundItem.time(p.get("publishedAt"));
        Instant createdAt = published != null ? published : item.observedAt();
        List<SocialPost.Media> media = new ArrayList<>();
        if (p.get("media") instanceof List<?> list) {
            for (Object m : list) {
                if (m instanceof Map<?, ?> mm && text(mm.get("url")) != null && media.size() < 20) {
                    media.add(new SocialPost.Media(mediaType(text(mm.get("type"))), text(mm.get("url")),
                            text(mm.get("thumbUrl")), null));
                }
            }
        }
        SocialPost.Engagement engagement = null;
        if (p.get("engagement") instanceof Map<?, ?> e) {
            engagement = new SocialPost.Engagement(count(e.get("replies")), count(e.get("reposts")),
                    count(e.get("quotes")), count(e.get("likes")), count(e.get("views")));
        }
        SocialPost.Geo geo = item.located() ? new SocialPost.Geo(item.lon(), item.lat(), null, null) : null;
        return new SocialPost(platform, item.itemId(), text(p.get("url")), author, text(p.get("text")),
                text(p.get("language")), createdAt, ref(platform, p.get("replyTo")), ref(platform, p.get("quoteOf")),
                ref(platform, p.get("repostOf")), strings(p.get("links")), strings(p.get("hashtags")),
                strings(p.get("mentions")), media, engagement, geo, text(p.get("channel")),
                SocialVocabulary.COLLECTION_IMPORT, null);
    }

    private static SocialPost.PostRef ref(String platform, Object value) {
        if (value instanceof Map<?, ?> m && text(m.get("postId")) != null) {
            String p = text(m.get("platform"));
            return new SocialPost.PostRef(p != null && SocialVocabulary.NETWORK.matcher(p).matches() ? p : platform,
                    text(m.get("postId")), text(m.get("url")));
        }
        return null;
    }

    private static String mediaType(String type) {
        return type != null && List.of("image", "video", "gif", "audio", "link-card").contains(type) ? type : "other";
    }

    private static Long count(Object value) {
        return value instanceof Number n && Double.isFinite(n.doubleValue()) ? n.longValue() : null;
    }

    private static List<String> strings(Object value) {
        List<String> out = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object v : list) {
                if (v != null && !String.valueOf(v).isBlank()) {
                    out.add(String.valueOf(v));
                }
            }
        }
        return out;
    }

    private static String text(Object value) {
        return value == null || value instanceof Map<?, ?> || value instanceof List<?> || String.valueOf(value).isBlank()
                ? null : String.valueOf(value);
    }
}
