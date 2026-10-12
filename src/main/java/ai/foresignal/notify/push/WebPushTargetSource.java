package ai.foresignal.notify.push;

import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.store.PushSubscriptionEntity;
import ai.foresignal.store.PushSubscriptionRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The Web Push targets: {@code active} rows of {@code push_subscription} (prompt 031). Pending
 * (unverified) and disabled subscriptions receive nothing.
 *
 * <p>Outcomes: a send updates {@code last_success_at} and clears the failure counters; a gone
 * subscription (404/410) is deleted; other failures count {@code failure_count} and
 * {@code last_failure_at}; a {@code URL_NOT_ALLOWED} recheck failure counts
 * {@code recheck_failures}, and the third in a row disables the subscription. Each update is one
 * statement in its own transaction (it runs after the dispatch row's transaction).
 */
@Component
public class WebPushTargetSource implements PushTargetSource {

    private static final Logger log = LoggerFactory.getLogger(WebPushTargetSource.class);

    /** Consecutive failed send-time rechecks after which a subscription is disabled. */
    public static final int MAX_RECHECK_FAILURES = 3;

    private final PushSubscriptionRepository subscriptions;
    private final NamedParameterJdbcTemplate jdbc;
    private volatile PushRestrictionGate gate;

    /** Creates the source. */
    public WebPushTargetSource(PushSubscriptionRepository subscriptions, NamedParameterJdbcTemplate jdbc) {
        this.subscriptions = subscriptions;
        this.jdbc = jdbc;
    }

    @Override
    public String targetType() {
        return WebPushTarget.TYPE;
    }

    @Override
    public List<PushTarget> targets(NotificationMessage message) {
        List<PushTarget> out = new ArrayList<>();
        for (PushSubscriptionEntity row : subscriptions.findByStatusOrderByCreatedAtAsc(PushSubscriptionService.ACTIVE)) {
            if (PushFilters.fromMap(row.getFilters()).matches(message) && restrictionAllows(row.getTokenId(), message)) {
                out.add(target(row));
            }
        }
        return out;
    }

    /** The read-restriction gate (prompt 035); optional so slices without it still fan out. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    void setRestrictionGate(PushRestrictionGate gate) {
        this.gate = gate;
    }

    private boolean restrictionAllows(UUID tokenId, NotificationMessage message) {
        PushRestrictionGate g = gate;
        return g == null || g.allows(tokenId, message);
    }

    @Override
    public Optional<PushTarget> find(String targetId) {
        UUID id;
        try {
            id = UUID.fromString(targetId);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        return subscriptions.findById(id)
                .filter(row -> PushSubscriptionService.ACTIVE.equals(row.getStatus()))
                .map(WebPushTargetSource::target);
    }

    /** The target of a row. */
    static WebPushTarget target(PushSubscriptionEntity row) {
        return new WebPushTarget(row.getId(), row.getEndpoint(), row.getP256dh(), row.getAuth(), row.getPayloadFormat(),
                row.getLang());
    }

    @Override
    public void onOutcome(PushTarget target, PushResult result, Instant at) {
        if (!(target instanceof WebPushTarget web)) {
            return;
        }
        try {
            MapSqlParameterSource p = new MapSqlParameterSource("id", web.subscriptionId())
                    .addValue("at", Timestamp.from(at)).addValue("status", result.status());
            switch (result) {
                case PushResult.Sent s -> jdbc.update("""
                        update push_subscription set last_success_at = :at, last_status = :status, failure_count = 0,
                               recheck_failures = 0
                         where id = :id
                        """, p);
                case PushResult.Gone g -> jdbc.update("delete from push_subscription where id = :id", p);
                case PushResult.Rejected r when WebPushTransport.URL_NOT_ALLOWED.equals(r.code()) -> jdbc.update("""
                        update push_subscription
                           set failure_count = failure_count + 1, last_failure_at = :at, last_status = :status,
                               recheck_failures = recheck_failures + 1,
                               status = case when recheck_failures + 1 >= :max then 'disabled' else status end
                         where id = :id
                        """, p.addValue("max", MAX_RECHECK_FAILURES));
                case PushResult.Retry r when !r.counted() -> { }
                default -> jdbc.update("""
                        update push_subscription set failure_count = failure_count + 1, last_failure_at = :at,
                               last_status = :status, recheck_failures = 0
                         where id = :id
                        """, p);
            }
        } catch (RuntimeException e) {
            log.warn("Could not record the push outcome of subscription {}: {}", web.subscriptionId(), e.getMessage());
        }
    }
}
