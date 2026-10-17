package ai.foresignal.object;

import java.time.Instant;

/**
 * The {@code workspace} SSE topic filter of one stream: the workspace it follows and the client's presence identity.
 *
 * @param workspaceId the followed workspace
 * @param clientId the client's id ({@code originClientId} of its own patches)
 * @param clientKind globe, map, analyst, satellite, rf, mobile, agent or other
 * @param principal the stream's principal id
 * @param connectedAt when the client subscribed
 */
public record WorkspaceFilter(String workspaceId, String clientId, String clientKind, String principal,
        Instant connectedAt) {

    /** True when the filter follows {@code id}. */
    public boolean follows(String id) {
        return workspaceId.equals(id);
    }
}
