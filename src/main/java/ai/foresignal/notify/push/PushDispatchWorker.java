package ai.foresignal.notify.push;

import ai.foresignal.notify.DeliveryWorker;
import ai.foresignal.notify.NotificationRouter;
import ai.foresignal.store.NotificationChannelRepository;
import ai.foresignal.store.NotificationDeliveryRepository;
import ai.foresignal.store.PushDispatchEntity;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Sends due {@code push_dispatch} rows (prompt 031).
 *
 * <p>Every poll ({@code foresignal.push.poll-interval}, 2 s) claims due rows with
 * {@code FOR UPDATE SKIP LOCKED} and a lease, then sends each on its own virtual thread: the target
 * is loaded from its {@link PushTargetSource} (a target that is gone or no longer active settles the
 * row as {@code gone}), the first {@link PushTransport} that supports it sends the stored
 * {@link PushMessage}, and the outcome is applied to that row alone:
 *
 * <ul>
 *   <li>sent: {@code sent}; the source records {@code last_success_at};
 *   <li>gone: {@code gone}; the source deletes the target;
 *   <li>retry: due again after the wait ({@code Retry-After}, or the per-host rate limit, which does
 *       not count an attempt);
 *   <li>rejected: {@code failed} at once (including {@code URL_NOT_ALLOWED});
 *   <li>failed: the 018 retry ladder (10 s, 1 min, 5 min, 30 min, 2 h), then {@code failed}.
 * </ul>
 *
 * <p>So a retry never re-sends to targets that already succeeded. After each terminal outcome the
 * parent delivery is settled when all its dispatches are terminal
 * ({@link PushDispatchStore#settleParent}); a failed parent raises {@code system.delivery_failed}
 * (never sent back to the Push channel).
 */
@Component
public class PushDispatchWorker {

    private static final Logger log = LoggerFactory.getLogger(PushDispatchWorker.class);

    private final PushDispatchStore store;
    private final List<PushTargetSource> sources;
    private final List<PushTransport> transports;
    private final NotificationDeliveryRepository deliveries;
    private final NotificationChannelRepository channels;
    private final NotificationRouter router;
    private final PushProperties properties;
    private final Clock clock;
    private final List<PushBadgeProvider> badges;

    /** Creates the worker. */
    public PushDispatchWorker(PushDispatchStore store, List<PushTargetSource> sources, List<PushTransport> transports,
            NotificationDeliveryRepository deliveries, NotificationChannelRepository channels, NotificationRouter router,
            PushProperties properties, Clock clock, List<PushBadgeProvider> badges) {
        this.store = store;
        this.sources = List.copyOf(sources);
        this.transports = List.copyOf(transports);
        this.deliveries = deliveries;
        this.channels = channels;
        this.router = router;
        this.properties = properties;
        this.clock = clock;
        this.badges = badges == null ? List.of() : List.copyOf(badges);
    }

    /** The scheduled poll; does nothing when {@code foresignal.push.scheduled} is false. */
    @Scheduled(fixedDelayString = "${foresignal.push.poll-interval:2s}",
            initialDelayString = "${foresignal.push.poll-interval:2s}")
    public void poll() {
        if (!properties.scheduled()) {
            return;
        }
        try {
            runOnce();
        } catch (RuntimeException e) {
            log.error("Push dispatch poll failed", e);
        }
    }

    /**
     * Claims due dispatches and sends them, returning when all are done.
     *
     * @return how many were claimed
     */
    public int runOnce() {
        Instant now = clock.instant();
        List<PushDispatchEntity> claimed = store.claim(now, properties.batchSize(), now.plus(properties.lease()));
        if (claimed.isEmpty()) {
            return 0;
        }
        Set<Long> parents = new LinkedHashSet<>();
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();
            for (PushDispatchEntity dispatch : claimed) {
                parents.add(dispatch.getDeliveryId());
                futures.add(pool.submit(() -> process(dispatch)));
            }
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    log.error("Push dispatch failed unexpectedly", e);
                }
            }
        }
        for (Long parent : parents) {
            settle(parent);
        }
        return claimed.size();
    }

    /** Sends one claimed dispatch and records its outcome. */
    void process(PushDispatchEntity dispatch) {
        Instant now = clock.instant();
        try {
            Optional<PushTargetSource> source = sources.stream()
                    .filter(s -> s.targetType().equals(dispatch.getTargetType())).findFirst();
            Optional<PushTarget> target = source.flatMap(s -> s.find(dispatch.getTargetId()));
            if (target.isEmpty()) {
                store.update(dispatch.getId(), row -> terminal(row, PushDispatchStore.GONE, null,
                        "the target was removed or is no longer active", now));
                return;
            }
            Optional<PushTransport> transport = transports.stream().filter(t -> t.supports(target.get())).findFirst();
            if (transport.isEmpty()) {
                store.update(dispatch.getId(), row -> terminal(row, PushDispatchStore.FAILED, null,
                        "no transport for target type " + dispatch.getTargetType(), now));
                return;
            }
            PushMessage message = withBadge(target.get(), PushMessage.fromMap(dispatch.getContent()));
            PushResult result = transport.get().send(target.get(), message, PushOptions.of(message, dispatch.isDowngraded())
                    .withDispatchId(dispatch.getId()));
            apply(dispatch, result, now);
            source.get().onOutcome(target.get(), result, now);
        } catch (RuntimeException e) {
            log.error("Push dispatch {} failed unexpectedly; it will be retried", dispatch.getId(), e);
            store.update(dispatch.getId(), row -> failure(row, null, e.getClass().getSimpleName(), now));
        }
    }

    /** {@code message} with the target's badge from a {@link PushBadgeProvider} (prompt 033), if any. */
    PushMessage withBadge(PushTarget target, PushMessage message) {
        if (!PushMessage.VERIFY.equals(message.event())) {
            for (PushBadgeProvider provider : badges) {
                try {
                    Optional<Integer> badge = provider.badgeFor(target);
                    if (badge.isPresent()) {
                        return message.withBadge(badge.get());
                    }
                } catch (RuntimeException e) {
                    log.warn("Could not compute the badge of push target {}: {}", target.type(), e.toString());
                }
            }
        }
        return message;
    }

    private void apply(PushDispatchEntity dispatch, PushResult result, Instant now) {
        store.update(dispatch.getId(), row -> {
            switch (result) {
                case PushResult.Sent sent -> {
                    terminal(row, PushDispatchStore.SENT, sent.status(), null, now);
                    row.setAttempts(row.getAttempts() + 1);
                    row.setSentAt(now);
                }
                case PushResult.Gone gone -> {
                    terminal(row, PushDispatchStore.GONE, gone.status(), "HTTP " + gone.status() + ": subscription gone", now);
                    row.setAttempts(row.getAttempts() + 1);
                }
                case PushResult.Retry retry -> {
                    row.setLeasedUntil(null);
                    row.setLastStatus(retry.status());
                    row.setLastError(cut(retry.reason()));
                    if (retry.counted()) {
                        row.setAttempts(row.getAttempts() + 1);
                    }
                    if (row.getAttempts() >= DeliveryWorker.MAX_ATTEMPTS) {
                        terminal(row, PushDispatchStore.FAILED, retry.status(), retry.reason(), now);
                    } else {
                        row.setNextAttemptAt(now.plus(retry.after()));
                    }
                }
                case PushResult.Rejected rejected -> {
                    String reason = rejected.code() == null ? rejected.reason() : rejected.code() + ": " + rejected.reason();
                    terminal(row, PushDispatchStore.FAILED, rejected.status(), reason, now);
                    row.setAttempts(row.getAttempts() + 1);
                }
                case PushResult.Failed failed -> failure(row, failed.status(), failed.reason(), now);
            }
        });
    }

    private static void failure(PushDispatchEntity row, Integer status, String reason, Instant now) {
        row.setAttempts(row.getAttempts() + 1);
        row.setLastStatus(status);
        row.setLastError(cut(reason));
        row.setLeasedUntil(null);
        if (row.getAttempts() >= DeliveryWorker.MAX_ATTEMPTS) {
            row.setStatus(PushDispatchStore.FAILED);
            row.setNextAttemptAt(null);
        } else {
            Duration wait = DeliveryWorker.LADDER.get(row.getAttempts() - 1);
            row.setNextAttemptAt(now.plus(wait));
        }
    }

    private static void terminal(PushDispatchEntity row, String status, Integer httpStatus, String error, Instant now) {
        row.setStatus(status);
        row.setLastStatus(httpStatus);
        row.setLastError(cut(error));
        row.setNextAttemptAt(null);
        row.setLeasedUntil(null);
    }

    private void settle(long deliveryId) {
        try {
            store.settleParent(deliveryId, clock.instant()).ifPresent(settled -> {
                if (settled.failed()) {
                    deliveries.findById(deliveryId).ifPresent(d -> channels.findById(d.getChannelId())
                            .ifPresent(c -> router.deliveryFailed(c, d, settled.lastError())));
                }
            });
        } catch (RuntimeException e) {
            log.error("Could not settle push delivery {}", deliveryId, e);
        }
    }

    private static String cut(String text) {
        if (text == null) {
            return null;
        }
        return text.length() > 500 ? text.substring(0, 500) : text;
    }
}
