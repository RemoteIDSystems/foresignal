package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.news.NewsViews;
import ai.foresignal.object.ObjectCallers;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.restrict.RestrictedRead;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * News stories and articles (OpenAPI tag {@code News}, prompt 133), {@code read} scope: stories with coverage balance,
 * blindspot and corroboration; the story detail with articles grouped by outlet (each outlet's ratings and consensus),
 * linked situations and canonical events; article metadata with outbound links only. Articles of sources outside the
 * caller's restriction are left out ({@link NewsViews.ArticleFilter}).
 */
@RestController
@Tag(name = "News")
public class NewsController {

    private final NewsViews views;
    private final ReadCallerResolver readers;
    private final ObjectCallers callers;

    /** Creates the controller. */
    public NewsController(NewsViews views, ReadCallerResolver readers, ObjectCallers callers) {
        this.views = views;
        this.readers = readers;
        this.callers = callers;
    }

    /** {@code listNewsStories}: newest activity first, keyset on {@code lastSeenAt}, {@code id}. */
    @GetMapping("/v1/news/stories")
    @RestrictedRead("stories are derived coverage counts; article lists honor the restriction")
    @Operation(operationId = "listNewsStories", tags = "News")
    public Map<String, Object> listNewsStories(@RequestParam(required = false) String topic,
            @RequestParam(required = false) String region, @RequestParam(required = false) String situationId,
            @RequestParam(required = false) String since, @RequestParam(required = false) String blindspot,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit) {
        String side = blindspot == null || blindspot.isBlank() ? null
                : QueryParams.oneOf("blindspot", blindspot.strip(), List.of("left", "right", "any"), null);
        if (region != null && !region.isBlank() && !region.strip().matches("^[A-Za-z]{2}(-[A-Za-z0-9]{1,3})?$")) {
            throw ValidationException.forField("region", "'" + region + "' is not a 040 region code");
        }
        int size = QueryParams.intInRange("limit", limit, 1, NewsViews.MAX_LIMIT, NewsViews.DEFAULT_LIMIT);
        return views.stories(topic, region, situationId, QueryParams.instant("since", since), side, cursor, size)
                .toMap();
    }

    /** {@code getNewsStory}: 404 {@code UNKNOWN_STORY}. */
    @GetMapping("/v1/news/stories/{storyId}")
    @RestrictedRead("articles of sources outside the restriction are left out")
    @Operation(operationId = "getNewsStory", tags = "News")
    public Map<String, Object> getNewsStory(@PathVariable String storyId, HttpServletRequest request) {
        return views.storyDetail(storyId, new ResolveContext(callers.of(request), null), EgressPath.API_READ,
                filter(request));
    }

    /** {@code listNewsArticles}: newest first, keyset on {@code seenAt}, {@code id}. */
    @GetMapping("/v1/news/articles")
    @RestrictedRead("articles of sources outside the restriction are left out; spatial restrictions see none")
    @Operation(operationId = "listNewsArticles", tags = "News")
    public Map<String, Object> listNewsArticles(@RequestParam(required = false) String story,
            @RequestParam(required = false) String outlet, @RequestParam(required = false) String since,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit,
            HttpServletRequest request) {
        int size = QueryParams.intInRange("limit", limit, 1, NewsViews.MAX_LIMIT, NewsViews.DEFAULT_LIMIT);
        return views.articles(story, outlet, QueryParams.instant("since", since), cursor, size, filter(request))
                .toMap();
    }

    private NewsViews.ArticleFilter filter(HttpServletRequest request) {
        ReadCaller caller = readers.resolveScoped(request);
        return NewsViews.ArticleFilter.of(caller.restriction(), readers.excludedSources(caller));
    }
}
