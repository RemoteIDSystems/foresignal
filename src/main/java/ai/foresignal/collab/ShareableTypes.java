package ai.foresignal.collab;

import ai.foresignal.object.ObjectTypes;
import java.util.List;
import java.util.Set;

/**
 * The object types that carry an owner, a visibility, an access list and a TLP label (prompt 130, OpenAPI
 * {@code ShareableType}, docs/Client-Contract.md §10). {@code chronicle} (131) and {@code report} (132) are listed
 * now; their prompts register their sources.
 */
public final class ShareableTypes {

    /** {@code chronicle} (prompt 131). */
    public static final String CHRONICLE = "chronicle";

    /** {@code report} (prompt 132). */
    public static final String REPORT = "report";

    /** Every shareable type, in contract order. */
    public static final List<String> ALL = List.of(ObjectTypes.INVESTIGATION, ObjectTypes.WORKSPACE, CHRONICLE,
            REPORT, ObjectTypes.SAVED_SEARCH, ObjectTypes.ANNOTATION, ObjectTypes.ATTACHMENT, ObjectTypes.EVIDENCE_ITEM,
            ObjectTypes.TAG);

    private static final Set<String> SET = Set.copyOf(ALL);

    private ShareableTypes() {}

    /** True when {@code type} is shareable. */
    public static boolean isShareable(String type) {
        return type != null && SET.contains(type);
    }
}
