package ai.foresignal.store;

import org.springframework.data.jpa.repository.JpaRepository;

/** {@code retention_state} rows (prompt 019); writes go through native SQL in {@code RetentionStateStore}. */
public interface RetentionStateRepository extends JpaRepository<RetentionStateEntity, RetentionStateId> {}
