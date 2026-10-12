package ai.foresignal.llm;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Runs an action once the current transaction commits, or at once outside a transaction. */
final class AfterCommit {

    private AfterCommit() {}

    /** Runs {@code action} after commit (never after a rollback). */
    static void run(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }
}
