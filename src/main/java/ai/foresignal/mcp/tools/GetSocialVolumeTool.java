package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.social.SocialViews;
import ai.foresignal.social.SocialVocabulary;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code get_social_volume} (read; prompt 135): the hourly or daily mention volume of one attention topic, place or
 * entity per social network, against its seasonal, denominator-scaled baseline ({@code SocialBaseline} 1.0.0), with
 * the velocity ratio, robust z and whether collection was complete. Volume is aggregate counts, never post content.
 */
@Component
@RestrictedRead("volume series are aggregate counts with no record content")
public class GetSocialVolumeTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "topic", JsonSchemas.string("Attention topic id (exactly one of topic, place, entity)"),
            "place", JsonSchemas.string("Place id (036)"),
            "entity", JsonSchemas.string("Entity id or entity:{id} (098)"),
            "platform", JsonSchemas.string("Network: bluesky, mastodon, reddit, youtube, x, threads, instagram"),
            "language", JsonSchemas.string("Language bucket (en, ar, …, other); default every language summed"),
            "metric", JsonSchemas.enumeration("Metric", "posts", "posts", "uniqueAuthors", "reposts", "engagement",
                    "denominator"),
            "interval", JsonSchemas.enumeration("Bucket size", "PT1H", "PT1H", "P1D"),
            "since", JsonSchemas.dateTime("Window start (default 7 days ago)"),
            "until", JsonSchemas.dateTime("Window end (default now)")));

    private final SocialViews views;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the tool. */
    public GetSocialVolumeTool(SocialViews views, JsonMapper json, Clock clock) {
        this.views = views;
        this.json = json;
        this.clock = clock;
    }

    @Override
    public String name() {
        return "get_social_volume";
    }

    @Override
    public String title() {
        return "Get social mention volume";
    }

    @Override
    public String description() {
        return "Returns how much one attention topic, place or entity is being mentioned on each social network, hour"
                + " by hour (or by day), against its seasonal baseline (hour-of-week median over 8 weeks, scaled by the"
                + " platform's own volume so platform growth and outages do not look like attention changes), with"
                + " the velocity ratio, robust z and whether collection was complete. Sampled search paths carry no"
                + " baseline. Social volume measures what people are saying, never what happened.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("SocialVolume");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public boolean readOnlyHint() {
        return true;
    }

    @Override
    public boolean idempotentHint() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String topic = ListNewsStoriesTool.text(args, "topic");
        String place = ListNewsStoriesTool.text(args, "place");
        String entity = ListNewsStoriesTool.text(args, "entity");
        int given = (topic == null ? 0 : 1) + (place == null ? 0 : 1) + (entity == null ? 0 : 1);
        if (given != 1) {
            return ToolResult.error("exactly one of topic, place and entity is required");
        }
        String scopeType = topic != null ? SocialVocabulary.SCOPE_TOPIC : place != null ? SocialVocabulary.SCOPE_PLACE
                : SocialVocabulary.SCOPE_ENTITY;
        String scopeId = topic != null ? topic : place != null ? place
                : entity.startsWith("entity:") ? entity.substring(7) : entity;
        String metric = ListNewsStoriesTool.text(args, "metric");
        metric = metric == null ? SocialVocabulary.POSTS : metric;
        if (!SocialVocabulary.METRICS.contains(metric)) {
            return ToolResult.error("metric must be one of " + SocialVocabulary.METRICS);
        }
        String interval = ListNewsStoriesTool.text(args, "interval");
        interval = interval == null ? "PT1H" : interval;
        if (!List.of("PT1H", "P1D").contains(interval)) {
            return ToolResult.error("interval must be PT1H or P1D");
        }
        try {
            Instant until = instant(ListNewsStoriesTool.text(args, "until"));
            Instant end = until == null ? clock.instant() : until;
            Instant since = instant(ListNewsStoriesTool.text(args, "since"));
            Map<String, Object> out = views.volume(scopeType, scopeId, SocialViews.network("platform",
                    ListNewsStoriesTool.text(args, "platform")), ListNewsStoriesTool.text(args, "language"), metric,
                    interval, since == null ? end.minus(Duration.ofDays(7)) : since, end);
            return ToolResult.json(json.valueToTree(out));
        } catch (DateTimeParseException e) {
            return ToolResult.error("since and until must be ISO-8601 instants");
        } catch (RuntimeException e) {
            return ToolResult.error(e.getMessage());
        }
    }

    private static Instant instant(String text) {
        return text == null ? null : Instant.parse(text);
    }
}
