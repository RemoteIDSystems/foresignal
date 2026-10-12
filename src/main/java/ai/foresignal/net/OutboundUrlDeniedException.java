package ai.foresignal.net;

import ai.foresignal.api.error.ErrorCodes;
import java.io.IOException;

/**
 * A send refused by {@link OutboundUrlPolicy} (prompt 035). An {@link IOException}, so senders treat
 * it as a delivery failure; its message starts with {@code URL_NOT_ALLOWED} and names the host and
 * address, never the path or query.
 */
public class OutboundUrlDeniedException extends IOException {

    private static final long serialVersionUID = 1L;

    private final String reason;

    /** A refusal with the policy's {@code reason}. */
    public OutboundUrlDeniedException(String reason) {
        super(ErrorCodes.URL_NOT_ALLOWED + ": " + reason);
        this.reason = reason;
    }

    /** The policy's reason, without the code. */
    public String reason() {
        return reason;
    }

    /** Throws when {@code result} is a refusal; returns the allowed result otherwise. */
    public static OutboundUrlPolicy.Allowed require(OutboundUrlPolicy.Result result) throws OutboundUrlDeniedException {
        if (result instanceof OutboundUrlPolicy.Denied denied) {
            throw new OutboundUrlDeniedException(denied.reason());
        }
        return (OutboundUrlPolicy.Allowed) result;
    }
}
