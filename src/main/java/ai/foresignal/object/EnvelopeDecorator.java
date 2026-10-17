package ai.foresignal.object;

import java.util.Map;

/**
 * Adds the collaboration fields to resolved envelopes (prompt 130): the caller's visible tags, the TLP label of a
 * work product and the access-list actions, and drops the work products the caller may not see (unshared objects are
 * invisible, not forbidden). {@link ObjectService} applies every decorator bean after the resolvers and
 * {@link ObjectActions}.
 */
public interface EnvelopeDecorator {

    /**
     * Decorates a batch of envelopes.
     *
     * @param envelopes the resolved envelopes, keyed by the requested reference
     * @param context the caller and {@code asOf}
     * @return the visible envelopes, decorated, under the same keys
     */
    Map<ObjectRef, ObjectEnvelope> decorate(Map<ObjectRef, ObjectEnvelope> envelopes, ResolveContext context);
}
