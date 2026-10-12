package ai.foresignal.notify.action;

import java.util.regex.Pattern;

/**
 * Redaction of capability tokens in paths (prompt 033): {@code /v1/actions/<token>} becomes
 * {@code /v1/actions/eac_…} wherever a path is logged, audited or echoed in a problem's
 * {@code instance}, since the token is the credential.
 */
public final class ActionPaths {

    /** The redacted path. */
    public static final String REDACTED = ActionService.PATH + "eac_…";

    private static final Pattern TOKEN_SEGMENT = Pattern.compile("(/v1/actions/)[^/?#\\s]+");

    private ActionPaths() {}

    /** {@code path} with any capability token replaced by {@code eac_…}; null stays null. */
    public static String redact(String path) {
        if (path == null || !path.contains(ActionService.PATH)) {
            return path;
        }
        return TOKEN_SEGMENT.matcher(path).replaceAll("$1eac_…");
    }

    /** True when {@code path} is a capability path. */
    public static boolean isActionPath(String path) {
        return path != null && path.startsWith(ActionService.PATH);
    }
}
