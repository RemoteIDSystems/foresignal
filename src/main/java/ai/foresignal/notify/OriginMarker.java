package ai.foresignal.notify;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Marks every outbound notification with this instance's id, so an instance never ingests its own
 * messages back (the loop guard, prompt 018).
 *
 * <h2>Contract</h2>
 *
 * <ul>
 *   <li><b>Headers:</b> every HTTP request a channel sends carries {@value #HEADER}: the install id
 *       ({@code InstallIdService.installId()}).
 *   <li><b>JSON bodies</b> Foresignal defines (the {@code webhook} default body, Apprise, Home
 *       Assistant) carry the top-level member {@value #JSON_FIELD} with the same value.
 *   <li><b>Tags:</b> ntfy messages carry the tag {@code foresignal-origin-<installId>}
 *       ({@link #tag}); Gotify messages carry the extra {@value #GOTIFY_EXTRA} =
 *       {@code {"installId": "<installId>"}}.
 * </ul>
 *
 * <p>Prompt 024's ntfy- and Gotify-compatible publish endpoints and the inbound webhook formats call
 * {@link #isOwn} and drop a message carrying this instance's marker. Markers of other instances pass,
 * so two Foresignal instances can still feed each other.
 */
public final class OriginMarker {

    /** The HTTP header. */
    public static final String HEADER = "X-Foresignal-Origin";

    /** The JSON member. */
    public static final String JSON_FIELD = "foresignalOrigin";

    /** The ntfy tag prefix; the tag is the prefix followed by the install id. */
    public static final String TAG_PREFIX = "foresignal-origin-";

    /** The Gotify extras key. */
    public static final String GOTIFY_EXTRA = "foresignal::origin";

    private OriginMarker() {}

    /** The ntfy tag for {@code installId}. */
    public static String tag(String installId) {
        return TAG_PREFIX + installId;
    }

    /** The Gotify extra value for {@code installId}. */
    public static Map<String, Object> gotifyExtra(String installId) {
        return Map.of("installId", installId);
    }

    /**
     * True when an inbound message carries {@code installId}'s marker in any of its forms.
     *
     * @param installId this instance's id
     * @param header the {@value #HEADER} header value, or null
     * @param jsonField the {@value #JSON_FIELD} member, or null
     * @param tags the message tags (ntfy), or null
     */
    public static boolean isOwn(String installId, String header, Object jsonField, Collection<String> tags) {
        Objects.requireNonNull(installId, "installId");
        if (installId.equals(header == null ? null : header.strip())) {
            return true;
        }
        if (jsonField != null && installId.equals(String.valueOf(jsonField))) {
            return true;
        }
        return tags != null && tags.contains(tag(installId));
    }
}
