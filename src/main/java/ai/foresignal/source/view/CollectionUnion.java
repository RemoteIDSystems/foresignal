package ai.foresignal.source.view;

/**
 * The collection union (prompt R2-010): which instance-enabled sources the collector pulls.
 *
 * <p>A source is pulled when it is in the instance set ({@code source_settings.enabled}, managed under
 * {@code collection.manage}) <strong>and</strong> it is wanted: at least one active user's view does not disable it,
 * or there is no active user at all. A view is opt-out (a user with no {@code user_source_pref} row for a source sees
 * it), so a source is dropped from collection only when every active user has turned it off. With no active user (a
 * first run, an instance driven by API tokens alone) the instance set is collected as it was before v2.0, so nothing
 * stops collecting before users exist.
 *
 * <p>The union is a set, so a source wanted by ten users is still one scheduled source with one pull per interval:
 * per-user views only filter the shared results ({@link UserSourceViewService#narrow}).
 */
public final class CollectionUnion {

    /**
     * The SQL predicate "the source aliased {@code s} is wanted", for a query over {@code source_settings s}.
     * {@code CollectorScheduler} ands it with {@code s.enabled}.
     */
    public static final String WANTED_SQL = "(not exists (select 1 from \"user\" u where u.status = 'active')"
            + " or exists (select 1 from \"user\" u where u.status = 'active' and not exists"
            + " (select 1 from user_source_pref p where p.user_id = u.id and p.source_id = s.source_id"
            + " and not p.enabled)))";

    private CollectionUnion() {}

    /**
     * The in-memory form of {@link #WANTED_SQL}: how many active users want a source, given how many are active and
     * how many of them disabled it.
     *
     * @param activeUsers the number of active users
     * @param disabledBy how many active users disabled the source
     * @return true when the source is wanted
     */
    public static boolean wanted(long activeUsers, long disabledBy) {
        return activeUsers == 0 || disabledBy < activeUsers;
    }
}
