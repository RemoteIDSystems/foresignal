package ai.foresignal.security.user;

import java.util.UUID;

/**
 * One kind of configuration a user can own, as {@code deleteUser} sees it (prompt R2-005).
 *
 * <p>Deleting a user first hands every implementation the user's id: by default ({@code reassign=true}) each moves
 * the user's rows to the target admin, otherwise each deletes them. A prompt that adds an {@code owner_user_id} column
 * (trigger rules R2-008, channels R2-009, {@code user_source_pref} R2-010, personal LLM providers R2-011, saved views,
 * preferences and workspaces R2-012, user-owned API tokens R2-025) registers one Spring bean implementing this, and
 * {@code deleteUser} picks it up without changing.
 *
 * <p>The contract: both methods run inside the deletion's transaction, before the user row is deleted, and must not
 * commit on their own; they touch only rows owned by {@code owner}; and they return how many rows they changed, which
 * the deletion writes to the audit log under {@link #name()}. A {@code null} target means no admin account exists
 * (the instance runs on the bootstrap admin token alone): the rows become instance-owned ({@code owner_user_id}
 * null), which for every table that has the column means "shared, managed by an admin".
 */
public interface UserOwnedConfigReassigner {

    /**
     * A short name for the audit detail, such as {@code triggerRules}.
     *
     * @return the name
     */
    String name();

    /**
     * Moves every row {@code owner} owns to {@code target}.
     *
     * @param owner the user being deleted
     * @param target the admin receiving the rows, or null to make them instance-owned
     * @return how many rows moved
     */
    int reassign(UUID owner, UUID target);

    /**
     * Deletes every row {@code owner} owns.
     *
     * @param owner the user being deleted
     * @return how many rows were deleted
     */
    int delete(UUID owner);
}
