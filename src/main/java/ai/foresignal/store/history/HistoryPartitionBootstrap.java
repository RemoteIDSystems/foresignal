package ai.foresignal.store.history;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Attaches {@code record_version}'s first leaves once Flyway has created the partitioned parent (Architecture §9,
 * §25). Leaves are the one part of the history schema that stays dynamic: the baseline holds the parent and both tier
 * parents but no leaf and no DEFAULT partition, and {@link PartitionManager#ensureAhead()} keeps at least 1.5 blocks
 * of leaves per tier ahead of {@code record_seq}.
 *
 * <p>{@code SchemaConfiguration} creates this bean after the Flyway initializer and makes the
 * {@code EntityManagerFactory} depend on it, so neither Hibernate's validation nor the writer runs before the leaves
 * exist. Idempotent: a restart only adds what is missing.
 */
public class HistoryPartitionBootstrap implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(HistoryPartitionBootstrap.class);

    private final PartitionManager partitionManager;

    /**
     * Creates the bootstrap; it does nothing until {@link #afterPropertiesSet()}.
     *
     * @param partitionManager the leaf manager
     */
    public HistoryPartitionBootstrap(PartitionManager partitionManager) {
        this.partitionManager = partitionManager;
    }

    @Override
    public void afterPropertiesSet() {
        partitionManager.ensureAhead();
        log.info("record_version leaves are attached ahead of record_seq");
    }
}
