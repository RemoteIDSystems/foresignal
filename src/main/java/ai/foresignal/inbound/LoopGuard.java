package ai.foresignal.inbound;

import ai.foresignal.notify.OriginMarker;
import ai.foresignal.security.InstallIdService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The inbound half of the notification loop guard (prompt 018's {@link OriginMarker}, prompt 024):
 * a push carrying this instance's own origin marker (the {@code X-Foresignal-Origin} header, a
 * top-level {@code foresignalOrigin} JSON member, the ntfy tag {@code foresignal-origin-<id>} or the
 * Gotify extra {@code foresignal::origin}) is acknowledged but not stored, so a notification channel
 * pointed back at an ingest endpoint can never feed itself. Other instances' markers pass.
 */
@Component
public class LoopGuard {

    private static final Logger log = LoggerFactory.getLogger(LoopGuard.class);

    private final InstallIdService install;
    private final JsonMapper json;

    /** Creates the guard. */
    public LoopGuard(InstallIdService install, JsonMapper json) {
        this.install = install;
        this.json = json;
    }

    /**
     * True when the request carries this instance's marker.
     *
     * @param request the request (for the header)
     * @param body the raw body, read for a top-level {@code foresignalOrigin} when it is a JSON object;
     *     may be null
     * @param tags ntfy tags, or null
     * @param gotifyExtras Gotify extras, or null
     */
    public boolean isOwn(HttpServletRequest request, byte[] body, Collection<String> tags, Map<String, Object> gotifyExtras) {
        String installId = install.installId();
        Object jsonField = body == null ? null : jsonOrigin(body);
        boolean own = OriginMarker.isOwn(installId, request.getHeader(OriginMarker.HEADER), jsonField, tags);
        if (!own && gotifyExtras != null && gotifyExtras.get(OriginMarker.GOTIFY_EXTRA) instanceof Map<?, ?> extra) {
            own = installId.equals(String.valueOf(extra.get("installId")));
        }
        if (own) {
            log.debug("Dropped an inbound push carrying this instance's own origin marker ({})", request.getRequestURI());
        }
        return own;
    }

    private Object jsonOrigin(byte[] body) {
        if (body.length == 0 || body[0] != '{') {
            return null;
        }
        try {
            JsonNode node = json.readTree(body);
            JsonNode field = node == null ? null : node.get(OriginMarker.JSON_FIELD);
            return field == null || field.isNull() ? null : field.asString();
        } catch (RuntimeException e) {
            return null;
        }
    }
}
