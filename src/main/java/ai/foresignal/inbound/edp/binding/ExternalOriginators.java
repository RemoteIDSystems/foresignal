package ai.foresignal.inbound.edp.binding;

import ai.foresignal.evidence.Originator;
import ai.foresignal.evidence.OriginatorRegistry;
import ai.foresignal.inbound.edp.EdpManifestModel;
import ai.foresignal.inbound.edp.EdpManifestService;
import ai.foresignal.inbound.edp.ExternalSourceChangedEvent;
import ai.foresignal.inbound.edp.ExternalSourceEntity;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Puts the originators external providers declare into the 116 {@link OriginatorRegistry} (prompt 138, EDP §3.4):
 * each manifest's {@code source.originator} with its class and {@code aggregates} at startup and whenever a manifest
 * is registered or changed, and item-level {@code origin.originator}s as bound items arrive. Independence is then
 * counted by originator, never by provider: an originator that aggregates a built-in source's originator joins its
 * evidence group.
 */
@Component
public class ExternalOriginators {

    private static final Logger log = LoggerFactory.getLogger(ExternalOriginators.class);

    private final OriginatorRegistry registry;
    private final EdpManifestService manifests;

    /** Creates the component. */
    public ExternalOriginators(OriginatorRegistry registry, EdpManifestService manifests) {
        this.registry = registry;
        this.manifests = manifests;
    }

    /** The originator a manifest declares, with its class and what it aggregates. Pure. */
    public static Originator manifestOriginator(EdpManifestModel manifest) {
        String id = manifest.originatorId() != null ? manifest.originatorId() : manifest.sourceId();
        return new Originator(id, manifest.originatorClass(), null, manifest.aggregates(),
                "declared by EDP provider " + manifest.sourceId(), List.of());
    }

    /**
     * The originator a bound item counts under: the manifest's (with its {@code aggregates}) when the item names it or
     * none, else the item's own {@code origin.originator} and class. Pure.
     */
    public static Originator itemOriginator(BoundItem item, EdpManifestModel manifest) {
        Originator declared = manifestOriginator(manifest);
        String id = item.originator();
        if (id == null || id.equals(declared.id())) {
            return declared;
        }
        return new Originator(id, item.originatorClass(), null, List.of(),
                "item-level origin relayed by EDP provider " + item.sourceId(), List.of());
    }

    /** Registers the originator of {@code item} (and its provider's) and returns the one in force. */
    public Originator register(BoundItem item, EdpManifestModel manifest) {
        registry.registerExternal(manifestOriginator(manifest));
        return registry.registerExternal(itemOriginator(item, manifest));
    }

    /** Registers every registered provider's declared originator. */
    @EventListener(ApplicationReadyEvent.class)
    public void registerAll() {
        try {
            for (ExternalSourceEntity ext : manifests.list(null)) {
                register(ext.getSourceId());
            }
        } catch (RuntimeException e) {
            log.warn("External originators could not be registered: {}", e.toString());
        }
    }

    /** Registers the originator of a provider whose manifest was registered or changed. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onSourceChanged(ExternalSourceChangedEvent change) {
        if (ExternalSourceChangedEvent.REGISTERED.equals(change.change())
                || ExternalSourceChangedEvent.MANIFEST_CHANGED.equals(change.change())) {
            register(change.sourceId());
        }
    }

    private void register(String sourceId) {
        try {
            registry.registerExternal(manifestOriginator(manifests.model(manifests.require(sourceId))));
        } catch (RuntimeException e) {
            log.debug("No originator registered for {}: {}", sourceId, e.toString());
        }
    }
}
