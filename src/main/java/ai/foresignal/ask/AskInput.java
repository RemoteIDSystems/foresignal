package ai.foresignal.ask;

import java.util.Objects;
import java.util.UUID;

/**
 * A validated {@code AskRequest}.
 *
 * @param question the question, 1–4000 characters
 * @param threadId the thread to continue, or null for a new one
 * @param scope the scope ({@link AskScope#NONE} when absent)
 * @param allowActions true to execute proposed actions at once (only honored for {@code agent} scope)
 */
public record AskInput(String question, UUID threadId, AskScope scope, boolean allowActions) {

    /** The longest question. */
    public static final int MAX_QUESTION = 4000;

    /** Requires the question; a null scope becomes {@link AskScope#NONE}. */
    public AskInput {
        Objects.requireNonNull(question, "question");
        scope = scope == null ? AskScope.NONE : scope;
    }
}
