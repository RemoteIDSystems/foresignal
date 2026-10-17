package ai.foresignal.object;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The operations the calling token may perform on an object (docs/Client-Contract.md §3), computed from the access
 * policy (the scope each operation needs) and the object's state. Clients render buttons from the list instead of
 * hard-coding permissions.
 *
 * <ul>
 *   <li>{@code add-to-workspace} and {@code annotate}: any token (workspaces and annotations need one);</li>
 *   <li>{@code verdict}: agent scope, on objects whose verdicts feed precision or review;</li>
 *   <li>{@code track}: agent scope, a 017 watch for a record or an entity;</li>
 *   <li>{@code propose-watch}: a token without agent scope, on a record or an entity: a note tagged
 *       {@code propose-watch} that an agent-scope operator turns into a watch;</li>
 *   <li>{@code acknowledge}: agent scope, a firing alert;</li>
 *   <li>{@code open-investigation}: agent scope, on events, situations, findings, alerts and records.</li>
 * </ul>
 */
public final class ObjectActions {

    /** Types a verdict applies to. */
    static final Set<String> VERDICT_TYPES = Set.of(ObjectTypes.RECORD, ObjectTypes.ENTITY,
            ObjectTypes.CANONICAL_EVENT, ObjectTypes.INTEL_EVENT, ObjectTypes.SITUATION, ObjectTypes.FINDING,
            ObjectTypes.PRECURSOR, ObjectTypes.HYPOTHESIS, ObjectTypes.INDICATOR, ObjectTypes.ALERT,
            ObjectTypes.ASSESSMENT);

    /** Types a 017 watch can track. */
    static final Set<String> TRACK_TYPES = Set.of(ObjectTypes.RECORD, ObjectTypes.ENTITY);

    /** Types an investigation can be opened from. */
    static final Set<String> INVESTIGATE_TYPES = Set.of(ObjectTypes.RECORD, ObjectTypes.INTEL_EVENT,
            ObjectTypes.SITUATION, ObjectTypes.FINDING, ObjectTypes.ALERT, ObjectTypes.CANONICAL_EVENT);

    /** Types that are not annotated or added to a workspace (they are the containers themselves). */
    static final Set<String> CONTAINER_TYPES = Set.of(ObjectTypes.WORKSPACE, ObjectTypes.ANNOTATION);

    private ObjectActions() {}

    /**
     * The actions {@code caller} may perform on {@code envelope}.
     *
     * @param envelope the object's envelope (its type and status decide state-dependent actions)
     * @param caller the calling token
     * @return the actions, in a stable order
     */
    public static List<ObjectAction> of(ObjectEnvelope envelope, ObjectCaller caller) {
        List<ObjectAction> out = new ArrayList<>();
        if (caller == null || caller.anonymous()) {
            return out;
        }
        String type = envelope.type();
        String ref = envelope.ref();
        String annotations = "/v1/objects/" + ref + "/annotations";
        boolean agent = caller.agent();
        if (!CONTAINER_TYPES.contains(type)) {
            out.add(new ObjectAction("add-to-workspace", "Add to workspace", "PATCH",
                    "/v1/workspaces/{workspaceId}/context", "read"));
            out.add(new ObjectAction("annotate", "Annotate", "POST", annotations, "read"));
        }
        if (agent && VERDICT_TYPES.contains(type)) {
            out.add(new ObjectAction("verdict", "Confirm or reject", "POST", annotations, "agent"));
        }
        if (TRACK_TYPES.contains(type)) {
            if (agent) {
                out.add(new ObjectAction("track", "Track", "POST", "/v1/triggers", "agent"));
            } else {
                out.add(new ObjectAction("propose-watch", "Propose a watch", "POST", annotations, "read"));
            }
        }
        if (agent && ObjectTypes.ALERT.equals(type) && "firing".equals(envelope.status())) {
            out.add(new ObjectAction("acknowledge", "Acknowledge", "POST",
                    "/v1/alerts/" + envelope.objectRef().id() + "/ack", "agent"));
        }
        if (agent && INVESTIGATE_TYPES.contains(type) && !"dismissed".equals(envelope.status())) {
            out.add(new ObjectAction("open-investigation", "Open an investigation", "POST", "/v1/investigations",
                    "agent"));
        }
        if (ObjectTypes.ANNOTATION.equals(type) || ObjectTypes.WORKSPACE.equals(type)) {
            String base = ObjectTypes.ANNOTATION.equals(type) ? "/v1/annotations/" : "/v1/workspaces/";
            String id = envelope.objectRef().id();
            if (caller.admin()) {
                out.add(new ObjectAction("delete", "Delete", "DELETE", base + id, "read"));
            }
        }
        return List.copyOf(out);
    }
}
