package ai.foresignal.inbound.edp.binding;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Applies one binding to one record version, exactly once (prompt 138): the idempotency key is
 * {@code (binding, source, externalId, version)}, recorded in {@code external_binding_applied} in the same transaction
 * as the target's change, so the router, a backfill and a restart never apply a version twice. A failure rolls the
 * target's change back, records the binding's {@code lastError} and marks the version handled, so one bad item never
 * stalls the stream.
 */
@Component
public class BindingApplier {

    private static final Logger log = LoggerFactory.getLogger(BindingApplier.class);

    private final Map<String, BindingTargetHandler> handlers = new LinkedHashMap<>();
    private final ExternalBindingRepository bindings;
    private final ExternalBindingAppliedRepository applied;
    private final TransactionTemplate tx;
    private final Clock clock;

    /** Creates the applier over every target handler. */
    public BindingApplier(List<BindingTargetHandler> handlers, ExternalBindingRepository bindings,
            ExternalBindingAppliedRepository applied, PlatformTransactionManager transactions, Clock clock) {
        for (BindingTargetHandler handler : handlers) {
            this.handlers.put(handler.target(), handler);
        }
        this.bindings = bindings;
        this.applied = applied;
        this.tx = new TransactionTemplate(transactions);
        this.tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.clock = clock;
    }

    /** The handler of {@code target}, or null. */
    public BindingTargetHandler handler(String target) {
        return handlers.get(target);
    }

    /** The idempotency key of a binding and a record version. Pure. */
    public static String key(String bindingId, String sourceId, String externalId, Integer version, long seq) {
        String text = bindingId + "\u0001" + sourceId + "\u0001" + externalId + "\u0001"
                + (version != null ? "v" + version : "s" + seq);
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    /**
     * Applies {@code bindingId} to {@code item} unless that version was already handled.
     *
     * @return the effect, or null when the version was handled before or the binding is no longer routable
     */
    public BindingEffect apply(String bindingId, BoundStream stream, BoundItem item) {
        String key = key(bindingId, item.sourceId(), item.externalId(), item.version(), item.seq());
        if (applied.existsById(key)) {
            return null;
        }
        try {
            return tx.execute(status -> {
                ExternalBindingEntity binding = bindings.findById(bindingId).orElse(null);
                if (binding == null || !binding.routable() || applied.existsById(key)) {
                    return null;
                }
                BindingTargetHandler handler = handlers.get(binding.getTarget());
                if (handler == null) {
                    throw new IllegalStateException("no handler for target " + binding.getTarget());
                }
                BindingEffect effect = handler.apply(binding, stream, item);
                record(binding, key, item, effect);
                return effect;
            });
        } catch (RuntimeException e) {
            String message = e.getMessage() == null ? e.toString() : e.getMessage();
            log.warn("Binding {} could not apply {}: {}", bindingId, item.ref(), message);
            BindingEffect failed = BindingEffect.error(item, message);
            try {
                tx.executeWithoutResult(status -> bindings.findById(bindingId).ifPresent(binding -> {
                    if (!applied.existsById(key)) {
                        record(binding, key, item, failed);
                    }
                }));
            } catch (RuntimeException again) {
                log.warn("Binding {} could not record the failure of {}: {}", bindingId, item.ref(), again.toString());
            }
            return failed;
        }
    }

    /** Marks the version handled and updates the binding's counters, error and latest item. */
    private void record(ExternalBindingEntity binding, String key, BoundItem item, BindingEffect effect) {
        Instant now = clock.instant();
        String action = effect == null ? "skip:none" : effect.action();
        applied.save(new ExternalBindingAppliedEntity(key, binding.getId(), item.externalId(), item.version(),
                item.seq(), action, effect == null ? null : effect.targetRef(), now));
        if (effect != null && effect.failed()) {
            binding.setLastError(item.ref() + ": " + effect.error());
        } else if (effect != null && !effect.skipped()) {
            binding.setApplied(binding.getApplied() + 1);
            binding.setLastAppliedAt(now);
        }
        binding.setUpdatedAt(now);
        bindings.save(binding);
    }
}
