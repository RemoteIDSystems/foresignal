package ai.foresignal.security.quota;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.security.user.UserAdminService;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Per-user quotas (prompt R2-026).
 *
 * <p>A user's effective quota ({@link #effective}) is their own {@code user_quota} row merged over the instance
 * default ({@code app_setting} key {@value SettingKeys#QUOTA_DEFAULT}); a limit unset in both is unlimited. Nothing is
 * set on a fresh instance, so nobody is limited until an admin sets a quota.
 *
 * <p>Usage is measured from the owning columns, keyed by the user's {@code person} principal (users and principals are
 * one-to-one, R2-001): trigger rules, notification channels and saved views by {@code owner_principal_id};
 * storage as the bytes of the attachments the user uploaded ({@code attachment.size} by {@code uploaded_by}) plus the
 * JSON text of the context and layout of the workspaces the user owns ({@code workspace.owner}).
 *
 * <p>{@link #checkCount} and {@link #checkStorage} are called where a user creates something; over the effective
 * limit they throw {@code 409 QUOTA_EXCEEDED} naming the {@code limit}. A caller that is not a user (an API token
 * without a user, the instance itself) is never limited. The check and the insert are not one transaction, so two
 * creates racing at the limit can both pass; the next create is then refused.
 */
@Service
public class QuotaService implements IngestStateReset {

    private final UserQuotaRepository quotas;
    private final UserRepository users;
    private final AppSettingService settings;
    private final JdbcTemplate jdbc;
    private final Clock clock;
    private final Map<UUID, Quota> own = new ConcurrentHashMap<>();
    private UserRateLimiter rates;

    /**
     * Creates the service.
     *
     * @param quotas the per-user quotas
     * @param users the users, mapping a principal to a user
     * @param settings the instance default
     * @param jdbc the template, for the usage counts
     * @param clock the clock
     */
    public QuotaService(UserQuotaRepository quotas, UserRepository users, AppSettingService settings, JdbcTemplate jdbc,
            Clock clock) {
        this.quotas = quotas;
        this.users = users;
        this.settings = settings;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /**
     * Wires the per-user rate limiter, whose recent request count {@link #usage} reports.
     *
     * @param rates the limiter
     */
    @Autowired(required = false)
    public void setRateLimiter(UserRateLimiter rates) {
        this.rates = rates;
    }

    /**
     * The instance default.
     *
     * @return the default, {@link Quota#UNSET} when never set
     */
    public Quota instanceDefault() {
        return settings.get(SettingKeys.QUOTA_DEFAULT, Quota.class).orElse(Quota.UNSET);
    }

    /**
     * Replaces the instance default.
     *
     * @param quota the default; {@link Quota#UNSET} removes it
     */
    public void setInstanceDefault(Quota quota) {
        if (quota == null || Quota.UNSET.equals(quota)) {
            settings.remove(SettingKeys.QUOTA_DEFAULT);
        } else {
            settings.put(SettingKeys.QUOTA_DEFAULT, quota);
        }
    }

    /**
     * A user's own quota ({@code getUserQuota}): the stored limits, unset members null.
     *
     * @param userId the user
     * @return the quota
     * @throws NotFoundException {@code 404 UNKNOWN_USER}
     */
    @Transactional(readOnly = true)
    public Quota get(UUID userId) {
        requireUser(userId);
        return ownQuota(userId);
    }

    /**
     * Replaces a user's own quota ({@code updateUserQuota}). A quota with every member null deletes the row, so the
     * user follows the instance default entirely.
     *
     * @param userId the user
     * @param quota the limits
     * @return the stored quota
     * @throws NotFoundException {@code 404 UNKNOWN_USER}
     */
    @Transactional
    public Quota replace(UUID userId, Quota quota) {
        requireUser(userId);
        if (Quota.UNSET.equals(quota)) {
            quotas.findById(userId).ifPresent(quotas::delete);
        } else {
            UserQuotaEntity row = quotas.findById(userId).orElse(null);
            if (row == null) {
                quotas.save(new UserQuotaEntity(userId, quota, clock.instant()));
            } else {
                row.set(quota, clock.instant());
                quotas.save(row);
            }
        }
        own.remove(userId);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            // A request between the eviction and the commit could cache the old row again.
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    own.remove(userId);
                }
            });
        }
        return quota;
    }

    /**
     * A user's effective quota: their own limits over the instance default.
     *
     * @param userId the user
     * @return the effective quota; members still null are unlimited
     */
    public Quota effective(UUID userId) {
        return ownQuota(userId).over(instanceDefault());
    }

    /**
     * The {@code QuotaUsage} of a user ({@code getMyQuota}): the effective quota and current usage.
     *
     * @param userId the user
     * @return {@code {quota, usage}}
     */
    @Transactional(readOnly = true)
    public Map<String, Object> usage(UUID userId) {
        UserEntity user = requireUser(userId);
        String principal = user.getPrincipalId();
        Map<String, Object> usage = new LinkedHashMap<>();
        usage.put("alerts", count(QuotaLimit.ALERTS, principal));
        usage.put("channels", count(QuotaLimit.CHANNELS, principal));
        usage.put("savedViews", count(QuotaLimit.SAVED_VIEWS, principal));
        usage.put("storageBytes", count(QuotaLimit.STORAGE, principal));
        usage.put("apiRequestsThisMinute", rates == null ? 0L : rates.requestsThisMinute(userId));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("quota", effective(userId).view());
        out.put("usage", usage);
        return out;
    }

    /**
     * Refuses a create that would take the user owning {@code principal} past a count quota.
     *
     * @param principal the creating caller's principal, or null (nothing is checked)
     * @param limit {@link QuotaLimit#ALERTS}, {@link QuotaLimit#CHANNELS} or {@link QuotaLimit#SAVED_VIEWS}
     * @throws ForesignalException {@code 409 QUOTA_EXCEEDED}
     */
    public void checkCount(String principal, QuotaLimit limit) {
        check(principal, limit, 1);
    }

    /**
     * Refuses a write that would take the user owning {@code principal} past {@code maxStorageBytes}.
     *
     * @param principal the owning principal, or null (nothing is checked)
     * @param addedBytes the bytes the write adds; zero or less is never refused
     * @throws ForesignalException {@code 409 QUOTA_EXCEEDED}
     */
    public void checkStorage(String principal, long addedBytes) {
        if (addedBytes > 0) {
            check(principal, QuotaLimit.STORAGE, addedBytes);
        }
    }

    /**
     * Forgets the cached own quotas and the per-user buckets, so a truncated {@code user_quota} is read again (the
     * test database cleaner calls it after each clean).
     */
    @Override
    public void resetState() {
        own.clear();
        if (rates != null) {
            rates.reset();
        }
    }

    private void check(String principal, QuotaLimit limit, long adding) {
        if (principal == null) {
            return;
        }
        Optional<UserEntity> user = users.findByPrincipalId(principal);
        if (user.isEmpty()) {
            return;
        }
        Long max = effective(user.get().getId()).limitOf(limit);
        if (max == null) {
            return;
        }
        long used = count(limit, principal);
        if (used + adding > max) {
            String what = switch (limit) {
                case ALERTS -> "alert rules";
                case CHANNELS -> "notification channels";
                case SAVED_VIEWS -> "saved views";
                case STORAGE -> "bytes of stored data";
            };
            throw new ForesignalException(HttpStatus.CONFLICT, ErrorCodes.QUOTA_EXCEEDED,
                    "Your quota " + limit.field() + " allows " + max + " " + what + "; you have " + used
                            + (limit == QuotaLimit.STORAGE ? " and this adds " + adding : "") + ".")
                    .withProperty("limit", limit.field())
                    .withProperty("quota", max)
                    .withProperty("usage", used);
        }
    }

    private long count(QuotaLimit limit, String principal) {
        Long value = switch (limit) {
            case ALERTS -> jdbc.queryForObject("SELECT count(*) FROM trigger_rule WHERE owner_principal_id = ?",
                    Long.class, principal);
            case CHANNELS -> jdbc.queryForObject(
                    "SELECT count(*) FROM notification_channel WHERE owner_principal_id = ?", Long.class, principal);
            case SAVED_VIEWS -> jdbc.queryForObject("SELECT count(*) FROM saved_view WHERE owner_principal_id = ?",
                    Long.class, principal);
            case STORAGE -> jdbc.queryForObject("SELECT (SELECT coalesce(sum(size), 0) FROM attachment"
                    + " WHERE uploaded_by = ?) + (SELECT coalesce(sum(octet_length(context::text)"
                    + " + octet_length(layout::text)), 0) FROM workspace WHERE owner = ?)", Long.class, principal,
                    principal);
        };
        return value == null ? 0 : value;
    }

    private Quota ownQuota(UUID userId) {
        return own.computeIfAbsent(userId, id -> quotas.findById(id).map(UserQuotaEntity::quota).orElse(Quota.UNSET));
    }

    private UserEntity requireUser(UUID userId) {
        return users.findById(userId).orElseThrow(UserAdminService::unknownUser);
    }
}
