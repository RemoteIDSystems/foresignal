package ai.foresignal.briefing;

import ai.foresignal.notify.ChannelService;
import ai.foresignal.notify.NotificationRouter;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

/**
 * The absolute links briefings carry (prompt 029): records, alerts and run renditions under the
 * {@code server} setting's {@code publicBaseUrl}, or {@value NotificationRouter#DEFAULT_BASE_URL} when
 * it is unset, exactly as notification links are built (018).
 */
@Component
public class BriefingLinks {

    private final ChannelService channels;

    /** Creates the link builder. */
    public BriefingLinks(ChannelService channels) {
        this.channels = channels;
    }

    /** The public base URL without a trailing slash. */
    public String baseUrl() {
        String base = channels.serverSettings().normalizedPublicBaseUrl();
        return base == null ? NotificationRouter.DEFAULT_BASE_URL : base;
    }

    /** {@code <base>/v1/records/{source}/{externalId}}, path segments encoded. */
    public String record(String sourceId, String externalId) {
        return baseUrl() + "/v1/records/" + segment(sourceId) + "/" + segment(externalId);
    }

    /** {@code <base>/v1/alerts/{id}}. */
    public String alert(long id) {
        return baseUrl() + "/v1/alerts/" + id;
    }

    /** The HTML rendition of a run: {@code <base>/v1/briefings/{id}/runs/{runId}?format=html}. */
    public String runHtml(UUID briefingId, UUID runId) {
        return baseUrl() + "/v1/briefings/" + briefingId + "/runs/" + runId + "?format=html";
    }

    /** The JSON of a run. */
    public String run(UUID briefingId, UUID runId) {
        return baseUrl() + "/v1/briefings/" + briefingId + "/runs/" + runId;
    }

    private static String segment(String value) {
        return UriUtils.encodePathSegment(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
