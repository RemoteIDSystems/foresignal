package ai.foresignal.notify;

import ai.foresignal.store.DeferredReason;
import ai.foresignal.store.DeliveryKind;
import ai.foresignal.store.DeliveryStatus;
import ai.foresignal.store.NotificationDeliveryEntity;
import ai.foresignal.store.NotificationDeliveryRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * The delivery worker's database operations (prompt 018), each a short transaction of its own.
 *
 * <p>{@link #claim} selects due rows with {@code FOR UPDATE SKIP LOCKED} and leases them by setting
 * {@code leased_until}, so two workers (or two processes) never send the same delivery: a second
 * worker skips locked rows during the claim and leased rows afterwards. Folding (backlog cap and
 * release digests) marks the folded rows {@code delivered} with {@code folded_into} pointing at the
 * digest delivery and the note {@code folded into delivery <id>}.
 */
@Component
public class DeliveryStore {

    /** Sends whose rows may be folded into a digest: alert sends and digests not yet folded. */
    static final String FOLDABLE = """
            status = 'PENDING' and event = 'alert' and kind in ('send', 'digest') and folded_into is null
            """;

    private final NotificationDeliveryRepository repository;
    private final NamedParameterJdbcTemplate jdbc;
    private final TransactionTemplate tx;

    /** Creates the store. */
    public DeliveryStore(NotificationDeliveryRepository repository, NamedParameterJdbcTemplate jdbc,
            PlatformTransactionManager transactions) {
        this.repository = repository;
        this.jdbc = jdbc;
        this.tx = new TransactionTemplate(transactions);
    }

    /** Claims and leases up to {@code limit} due deliveries, oldest due first. */
    public List<NotificationDeliveryEntity> claim(Instant now, int limit, Instant leaseUntil) {
        List<NotificationDeliveryEntity> claimed = tx.execute(status -> {
            List<Long> ids = jdbc.queryForList("""
                    select id from notification_delivery
                     where status = 'PENDING' and next_attempt_at <= :now
                       and (leased_until is null or leased_until <= :now)
                     order by next_attempt_at, id
                     limit :limit
                     for update skip locked
                    """, new MapSqlParameterSource("now", Timestamp.from(now)).addValue("limit", limit), Long.class);
            if (ids.isEmpty()) {
                return List.of();
            }
            jdbc.update("update notification_delivery set leased_until = :until where id in (:ids)",
                    new MapSqlParameterSource("until", Timestamp.from(leaseUntil)).addValue("ids", ids));
            List<NotificationDeliveryEntity> rows = new ArrayList<>(repository.findAllById(ids));
            rows.sort(Comparator.comparing(NotificationDeliveryEntity::getNextAttemptAt)
                    .thenComparing(NotificationDeliveryEntity::getId));
            return rows;
        });
        return claimed == null ? List.of() : claimed;
    }

    /**
     * Folds the due deliveries a quiet-hours window or a pause released for one channel into one
     * digest: the newest becomes {@code kind=digest}, the others are folded into it.
     *
     * @param claimedIds deliveries this worker already holds (they are leased by it)
     * @return the ids folded away (empty when fewer than two were released)
     */
    public List<Long> foldReleased(UUID channelId, Collection<Long> claimedIds, Instant now) {
        List<Long> folded = tx.execute(status -> {
            List<Long> ids = jdbc.queryForList("select id from notification_delivery where channel_id = :c and "
                    + FOLDABLE + """
                       and deferred_reason in (:reasons) and next_attempt_at <= :now
                       and (id in (:claimed) or leased_until is null or leased_until <= :now)
                     order by id desc
                     for update skip locked
                    """, new MapSqlParameterSource("c", channelId)
                            .addValue("reasons", DeferredReason.RELEASED_AS_DIGEST)
                            .addValue("now", Timestamp.from(now))
                            .addValue("claimed", claimedIds.isEmpty() ? List.of(-1L) : claimedIds), Long.class);
            if (ids.size() < 2) {
                return List.<Long>of();
            }
            return fold(ids.getFirst(), ids.subList(1, ids.size()), now);
        });
        return folded == null ? List.of() : folded;
    }

    /**
     * Folds the oldest pending sends of a channel beyond {@code cap} into one digest (backlog cap).
     * Runs inside the caller's transaction.
     */
    public void foldBacklog(UUID channelId, int cap, Instant now) {
        List<Long> ids = jdbc.queryForList("select id from notification_delivery where channel_id = :c and "
                + FOLDABLE + " and (leased_until is null or leased_until <= :now) order by id desc",
                new MapSqlParameterSource("c", channelId).addValue("now", Timestamp.from(now)), Long.class);
        if (ids.size() <= cap) {
            return;
        }
        List<Long> oldest = ids.subList(Math.max(0, cap - 1), ids.size());
        if (oldest.size() < 2) {
            return;
        }
        fold(oldest.getFirst(), oldest.subList(1, oldest.size()), now);
    }

    /** Makes {@code digestId} a digest and folds {@code others} (and anything folded into them) into it. */
    private List<Long> fold(long digestId, List<Long> others, Instant now) {
        List<Long> folded = List.copyOf(others);
        MapSqlParameterSource p = new MapSqlParameterSource("digest", digestId).addValue("others", folded)
                .addValue("now", Timestamp.from(now))
                .addValue("note", "folded into delivery " + digestId);
        jdbc.update("update notification_delivery set kind = 'digest' where id = :digest", p);
        jdbc.update("""
                update notification_delivery
                   set status = 'DELIVERED', folded_into = :digest, note = :note, delivered_at = :now,
                       next_attempt_at = null, leased_until = null
                 where id in (:others)
                """, p);
        jdbc.update("update notification_delivery set folded_into = :digest, note = :note where folded_into in (:others)", p);
        return folded;
    }

    /** The pending grouping holder of (channel, rule), if one is waiting. Runs in the caller's transaction. */
    public Optional<Long> groupingHolder(UUID channelId, UUID ruleId) {
        List<Long> ids = jdbc.queryForList("""
                select d.id from notification_delivery d join notification n on n.id = d.notification_id
                 where d.channel_id = :c and d.status = 'PENDING' and d.deferred_reason = 'grouping'
                   and d.folded_into is null and n.rule_id = :rule
                 order by d.id limit 1
                """, new MapSqlParameterSource("c", channelId).addValue("rule", ruleId), Long.class);
        return ids.stream().findFirst();
    }

    /** The deliveries folded into {@code digestId}, newest first. */
    public List<NotificationDeliveryEntity> foldedInto(long digestId) {
        return repository.findByFoldedIntoOrderByIdDesc(digestId);
    }

    /** The delivery of {@code notificationId} to {@code channelId}, if any. */
    public Optional<NotificationDeliveryEntity> deliveryOf(UUID channelId, long notificationId) {
        return repository.findByNotificationIdOrderByIdAsc(notificationId).stream()
                .filter(d -> d.getChannelId().equals(channelId)).findFirst();
    }

    /** The delivery with {@code id}, if any. */
    public Optional<NotificationDeliveryEntity> find(long id) {
        return repository.findById(id);
    }

    /** Loads, changes and saves one delivery in its own transaction. */
    public void update(long id, Consumer<NotificationDeliveryEntity> change) {
        tx.executeWithoutResult(status -> repository.findById(id).ifPresent(d -> {
            change.accept(d);
            repository.save(d);
        }));
    }

    /** Defers a delivery to {@code until} for {@code reason}, releasing the lease. */
    public void defer(long id, Instant until, String reason) {
        update(id, d -> {
            d.setNextAttemptAt(until);
            d.setDeferredReason(reason);
            d.setLeasedUntil(null);
        });
    }

    /** Reschedules a delivery without counting an attempt (rate limit), releasing the lease. */
    public void reschedule(long id, Instant at) {
        update(id, d -> {
            d.setNextAttemptAt(at);
            d.setLeasedUntil(null);
        });
    }

    /** Fails a delivery outright (channel deleted or disabled). */
    public void fail(long id, String reason) {
        update(id, d -> {
            d.setStatus(DeliveryStatus.FAILED);
            d.setLastError(reason);
            d.setNextAttemptAt(null);
            d.setLeasedUntil(null);
        });
    }

    /** Fails every delivery folded into a failed digest. */
    public void failFolded(long digestId, String reason) {
        jdbc.update("""
                update notification_delivery set status = 'FAILED', last_error = :reason, delivered_at = null
                 where folded_into = :digest
                """, new MapSqlParameterSource("digest", digestId).addValue("reason", reason));
    }

    /** Settles a delivery as delivered. */
    public void delivered(long id, String kind, String ref, boolean edited, String warning, Instant now) {
        update(id, d -> {
            d.setStatus(DeliveryStatus.DELIVERED);
            d.setKind(kind);
            d.setAttempts(d.getAttempts() + 1);
            d.setDeliveredAt(now);
            d.setNextAttemptAt(null);
            d.setLeasedUntil(null);
            d.setLastError(warning);
            d.setEdited(edited);
            if (ref != null && !DeliveryKind.UPDATE.equals(kind)) {
                d.setProviderMessageRef(ref.length() > 512 ? ref.substring(0, 512) : ref);
            }
        });
    }
}
