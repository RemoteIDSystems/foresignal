package ai.foresignal.notify;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.user.AuthenticatedSession;
import ai.foresignal.security.user.Permissions;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import org.springframework.http.HttpStatus;

/**
 * Which notification channels a caller may see and manage (prompt R2-009).
 *
 * <p>A channel is <b>personal</b> when {@code owner_principal_id} names a user's principal, and <b>global</b> when it
 * is null: the instance-wide channels an operator runs, which every channel before R2-009 was (the personal notice
 * channels of prompt 130 aside).
 *
 * <ul>
 *   <li>A session sees its own channels and the global ones. It manages its own; it manages the global ones only
 *       with {@code channels.global}. What it creates is its own with {@code self.channels}, global otherwise (or
 *       when the body asks for {@code global: true}, which needs {@code channels.global}).
 *   <li>A session holding {@code users.manage} (an admin) sees and manages every channel, other users' included.
 *   <li>An API token (the machine path) sees and manages every channel and creates global ones, as before R2-009.
 * </ul>
 *
 * @param principalId the session's principal, or null for an API token
 * @param all true when the caller sees and manages every channel
 * @param global true when the caller manages global channels ({@code channels.global})
 * @param personal true when the caller may own channels ({@code self.channels})
 */
public record ChannelScope(String principalId, boolean all, boolean global, boolean personal) {

    /** An API token: every channel; new channels are global. */
    public static final ChannelScope MACHINE = new ChannelScope(null, true, true, false);

    /**
     * The scope of a session.
     *
     * @param session the session
     * @return its scope
     */
    public static ChannelScope of(AuthenticatedSession session) {
        Set<String> keys = session.permissions().keys();
        return new ChannelScope(session.principalId(), keys.contains(Permissions.USERS_MANAGE.key()),
                keys.contains(Permissions.CHANNELS_GLOBAL.key()), keys.contains(Permissions.SELF_CHANNELS.key()));
    }

    /**
     * The scope of a request: its session, else the machine scope (every channel operation needs a token).
     *
     * @param request the request
     * @return its scope
     */
    public static ChannelScope of(HttpServletRequest request) {
        if (request == null) {
            return MACHINE;
        }
        AuthenticatedSession session = AuthenticatedSession.of(request).orElse(null);
        if (session != null) {
            return of(session);
        }
        return CurrentToken.of(request).isPresent() ? MACHINE : new ChannelScope(null, false, false, false);
    }

    /**
     * True when a channel owned by {@code owner} is visible: global channels and the caller's own, or all.
     *
     * @param owner the channel's owner principal, or null for a global channel
     * @return true when visible
     */
    public boolean sees(String owner) {
        return all || owner == null || owner.equals(principalId);
    }

    /**
     * True when the caller may change, test or delete a channel owned by {@code owner}.
     *
     * @param owner the channel's owner principal, or null for a global channel
     * @return true when allowed
     */
    public boolean manages(String owner) {
        if (all) {
            return true;
        }
        return owner == null ? global : owner.equals(principalId);
    }

    /**
     * The owner of a channel this caller creates.
     *
     * @param requestedGlobal the body's {@code global} member, or null when absent
     * @return the owner principal, or null for a global channel
     * @throws ForesignalException {@code 403 PERMISSION_DENIED} when the caller may not create that kind
     */
    public String ownerForNew(Boolean requestedGlobal) {
        if (principalId == null) {
            if (Boolean.FALSE.equals(requestedGlobal)) {
                throw denied(Permissions.SELF_CHANNELS.key(), "An API token creates global channels only.");
            }
            return null;
        }
        boolean wantGlobal = requestedGlobal == null ? !personal : requestedGlobal;
        if (wantGlobal) {
            if (!global && !all) {
                throw denied(Permissions.CHANNELS_GLOBAL.key(),
                        "A global channel needs the channels.global permission, which none of your roles grants.");
            }
            return null;
        }
        if (!personal && !all) {
            throw denied(Permissions.SELF_CHANNELS.key(),
                    "A personal channel needs the self.channels permission, which none of your roles grants.");
        }
        return principalId;
    }

    /**
     * The refusal of a channel the caller can see but not manage: a global channel without {@code channels.global}.
     *
     * @return the exception
     */
    public static ForesignalException globalDenied() {
        return denied(Permissions.CHANNELS_GLOBAL.key(),
                "This is a global channel; changing it needs the channels.global permission, which none of your roles"
                        + " grants.");
    }

    private static ForesignalException denied(String permission, String detail) {
        return new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.PERMISSION_DENIED, detail)
                .withProperty("requiredPermission", permission);
    }
}
