package ai.foresignal.object;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.share.ExtraShareTarget;
import ai.foresignal.share.ShareLinkEntity;
import ai.foresignal.share.ShareLinkService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code workspace} share-link target (prompt 129, {@code shareWorkspace}): a read-only link to a workspace's
 * current context (name, version, context and its deep link). It never shows the layout, members or owner, and a
 * private workspace is never shared.
 */
@Component
public class WorkspaceShareTarget implements ExtraShareTarget {

    /** The target type. */
    public static final String TYPE = "workspace";

    private final WorkspaceRepository workspaces;
    private final JsonMapper json;

    /** Creates the target. */
    public WorkspaceShareTarget(WorkspaceRepository workspaces, JsonMapper json) {
        this.workspaces = workspaces;
        this.json = json;
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public Set<String> check(String ref, ShareLinkService.Caller caller) {
        WorkspaceEntity w = workspaces.findById(ref).orElseThrow(() -> new NotFoundException(
                ErrorCodes.UNKNOWN_WORKSPACE, "No workspace '" + ref + "' exists."));
        if (w.isPrivateWorkspace()) {
            throw new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.RESTRICTED,
                    "A private workspace never leaves on a share link.");
        }
        return Set.of();
    }

    @Override
    public ShareLinkService.Rendered render(ShareLinkEntity link) {
        WorkspaceEntity w = workspaces.findById(link.getTargetRef()).filter(x -> !x.isPrivateWorkspace())
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND,
                        "The shared workspace is no longer available."));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("workspaceId", w.getId());
        data.put("name", w.getName());
        data.put("contextVersion", w.getContextVersion());
        data.put("context", w.getContext());
        try {
            data.put("contextLink", ContextLinks.encode(w.getId(), json.valueToTree(w.getContext())));
        } catch (RuntimeException e) {
            data.put("contextLink", null);
        }
        return new ShareLinkService.Rendered(data, List.of(), w.getName(), List.of());
    }
}
