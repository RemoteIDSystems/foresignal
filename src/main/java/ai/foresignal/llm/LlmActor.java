package ai.foresignal.llm;

import ai.foresignal.security.user.AuthenticatedSession;
import java.util.function.Supplier;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * The user an LLM call is made on behalf of (prompt R2-011): a user's principal, or null for the machine (an API
 * token, a scheduled job with no owner).
 *
 * <p>The actor is what per-user metering ({@code llm_user_usage}) and per-user caps count against, and whose personal
 * provider {@link LlmService#chatProviderFor} prefers. It is, in order: one set explicitly with {@link #call} on this
 * thread (background work done for a user, such as enriching their alert); else the signed-in user of the HTTP request
 * this thread serves; else nobody. A governed client captures the actor when it is created, so a call it makes later on
 * another thread is still attributed to the same user.
 */
public final class LlmActor {

    private static final ThreadLocal<String> EXPLICIT = new ThreadLocal<>();

    private LlmActor() {}

    /**
     * The current actor.
     *
     * @return the user's principal, or null for the machine
     */
    public static String current() {
        String explicit = EXPLICIT.get();
        if (explicit != null) {
            return explicit;
        }
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servlet) {
            return AuthenticatedSession.of(servlet.getRequest()).map(AuthenticatedSession::principalId).orElse(null);
        }
        return null;
    }

    /**
     * Runs {@code work} on behalf of {@code principalId}; a null principal runs it as the current actor.
     *
     * @param principalId the user's principal, or null
     * @param work the work
     * @param <T> the result type
     * @return its result
     */
    public static <T> T call(String principalId, Supplier<T> work) {
        if (principalId == null) {
            return work.get();
        }
        String before = EXPLICIT.get();
        EXPLICIT.set(principalId);
        try {
            return work.get();
        } finally {
            if (before == null) {
                EXPLICIT.remove();
            } else {
                EXPLICIT.set(before);
            }
        }
    }
}
