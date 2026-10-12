package ai.foresignal.notify.push;

import ai.foresignal.store.NotificationDeliveryEntity;
import ai.foresignal.store.NotificationDeliveryRepository;
import ai.foresignal.store.PushDispatchEntity;
import ai.foresignal.store.PushDispatchRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * The database operations of push fan-out and dispatch (prompt 031), each a short transaction of
 * its own.
 *
 * <p>{@link #claim} selects due {@code push_dispatch} rows with {@code FOR UPDATE SKIP LOCKED} and
 * leases them, so two workers never send the same push. {@link #settleParent} settles the Push
 * channel's {@code notification_delivery} row once every dispatch of it is terminal: delivered when
 * at least one was sent (or every target was gone), failed when none was sent and at least one
 * failed. It locks the parent row first, so two dispatches finishing together settle it once.
 */
@Component
public class PushDispatchStore {

    /** Dispatch status: waiting for its (next) attempt. */
    public static final String PENDING = "pending";
    /** Dispatch status: accepted by the push service. */
    public static final String SENT = "sent";
    /** Dispatch status: the target is gone (404/410, deleted). */
    public static final String GONE = "gone";
    /** Dispatch status: failed for good. */
    public static final String FAILED = "failed";

    private final PushDispatchRepository repository;
    private final NotificationDeliveryRepository deliveries;
    private final NamedParameterJdbcTemplate jdbc;
    private final TransactionTemplate tx;

    /** Creates the store. */
    public PushDispatchStore(PushDispatchRepository repository, NotificationDeliveryRepository deliveries,
            NamedParameterJdbcTemplate jdbc, PlatformTransactionManager transactions) {
        this.repository = repository;
        this.deliveries = deliveries;
        this.jdbc = jdbc;
        this.tx = new TransactionTemplate(transactions);
    }

    /**
     * Inserts one pending dispatch per target and parks the parent delivery (not due, not leased)
     * until they settle; with no targets the parent is delivered at once. Does nothing when the
     * delivery already fanned out (a re-claim after a crash).
     *
     * @return how many dispatches were inserted
     */
    public int fanOut(NotificationDeliveryEntity delivery, List<PushTarget> targets, Map<String, Object> content,
            boolean downgraded, Instant now) {
        Integer inserted = tx.execute(status -> {
            if (repository.existsByDeliveryId(delivery.getId())) {
                park(delivery.getId(), null);
                return 0;
            }
            List<PushDispatchEntity> rows = new ArrayList<>();
            for (PushTarget target : targets) {
                PushDispatchEntity row = new PushDispatchEntity(delivery.getId(), target.type(), target.id(), content, now);
                row.setDowngraded(downgraded);
                rows.add(row);
            }
            repository.saveAll(rows);
            if (rows.isEmpty()) {
                deliveries.findById(delivery.getId()).ifPresent(d -> {
                    d.setStatus(ai.foresignal.store.DeliveryStatus.DELIVERED);
                    d.setAttempts(d.getAttempts() + 1);
                    d.setDeliveredAt(now);
                    d.setNextAttemptAt(null);
                    d.setLeasedUntil(null);
                    d.setDeferredReason(null);
                    d.setNote("no push targets matched");
                    deliveries.save(d);
                });
            } else {
                park(delivery.getId(), "fanned out to " + rows.size() + " push target" + (rows.size() == 1 ? "" : "s")
                        + (downgraded ? " (quiet hours: very-low urgency, silent)" : ""));
            }
            return rows.size();
        });
        return inserted == null ? 0 : inserted;
    }

    private void park(long deliveryId, String note) {
        jdbc.update("""
                update notification_delivery
                   set next_attempt_at = null, leased_until = null, deferred_reason = null,
                       note = coalesce(:note, note)
                 where id = :id and status = 'PENDING'
                """, new MapSqlParameterSource("id", deliveryId).addValue("note", note));
    }

    /** Claims and leases up to {@code limit} due dispatches, oldest due first. */
    public List<PushDispatchEntity> claim(Instant now, int limit, Instant leaseUntil) {
        List<PushDispatchEntity> claimed = tx.execute(status -> {
            List<Long> ids = jdbc.queryForList("""
                    select id from push_dispatch
                     where status = 'pending' and next_attempt_at <= :now
                       and (leased_until is null or leased_until <= :now)
                     order by next_attempt_at, id
                     limit :limit
                     for update skip locked
                    """, new MapSqlParameterSource("now", Timestamp.from(now)).addValue("limit", limit), Long.class);
            if (ids.isEmpty()) {
                return List.of();
            }
            jdbc.update("update push_dispatch set leased_until = :until where id in (:ids)",
                    new MapSqlParameterSource("until", Timestamp.from(leaseUntil)).addValue("ids", ids));
            List<PushDispatchEntity> rows = new ArrayList<>(repository.findAllById(ids));
            rows.sort(Comparator.comparing(PushDispatchEntity::getNextAttemptAt).thenComparing(PushDispatchEntity::getId));
            return rows;
        });
        return claimed == null ? List.of() : claimed;
    }

    /** Loads, changes and saves one dispatch in its own transaction. */
    public void update(long id, Consumer<PushDispatchEntity> change) {
        tx.executeWithoutResult(status -> repository.findById(id).ifPresent(d -> {
            change.accept(d);
            repository.save(d);
        }));
    }

    /** The dispatches of a delivery, oldest first. */
    public List<PushDispatchEntity> ofDelivery(long deliveryId) {
        return repository.findByDeliveryIdOrderByIdAsc(deliveryId);
    }

    /**
     * How a parent delivery was settled.
     *
     * @param failed true when it failed (none sent, at least one failed)
     * @param note the summary note
     * @param lastError the last dispatch error, for a failure
     */
    public record Settled(boolean failed, String note, String lastError) {}

    /** Settles the parent delivery when every dispatch is terminal; empty when it is not (yet). */
    public Optional<Settled> settleParent(long deliveryId, Instant now) {
        return Optional.ofNullable(tx.execute(status -> {
            List<String> parent = jdbc.queryForList(
                    "select status from notification_delivery where id = :id for update",
                    new MapSqlParameterSource("id", deliveryId), String.class);
            if (parent.isEmpty() || !"PENDING".equals(parent.getFirst())) {
                return null;
            }
            long[] counts = new long[4];
            String[] lastError = {null};
            jdbc.query("select status, last_error from push_dispatch where delivery_id = :id order by id",
                    new MapSqlParameterSource("id", deliveryId), rs -> {
                        switch (rs.getString(1)) {
                            case PENDING -> counts[0]++;
                            case SENT -> counts[1]++;
                            case GONE -> counts[2]++;
                            default -> {
                                counts[3]++;
                                if (rs.getString(2) != null) {
                                    lastError[0] = rs.getString(2);
                                }
                            }
                        }
                    });
            if (counts[0] > 0 || counts[1] + counts[2] + counts[3] == 0) {
                return null;
            }
            String note = "push: " + counts[1] + " sent, " + counts[2] + " gone, " + counts[3] + " failed";
            boolean failed = counts[1] == 0 && counts[3] > 0;
            jdbc.update("""
                    update notification_delivery
                       set status = :status, delivered_at = :at, next_attempt_at = null, leased_until = null,
                           attempts = attempts + 1, note = :note, last_error = :error
                     where id = :id
                    """, new MapSqlParameterSource("id", deliveryId)
                            .addValue("status", failed ? "FAILED" : "DELIVERED")
                            .addValue("at", failed ? null : Timestamp.from(now))
                            .addValue("note", note)
                            .addValue("error", failed ? lastError[0] : null));
            return new Settled(failed, note, lastError[0]);
        }));
    }

    /** Deletes dispatches whose parent delivery no longer exists (retention removed it). */
    public int deleteOrphans() {
        return jdbc.update("""
                delete from push_dispatch p
                 where not exists (select 1 from notification_delivery d where d.id = p.delivery_id)
                """, new MapSqlParameterSource());
    }
}
