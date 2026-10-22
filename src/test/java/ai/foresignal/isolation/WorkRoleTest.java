package ai.foresignal.isolation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WorkRoleTest {

    @Test
    @DisplayName("a thread with no role set is BACKGROUND")
    void defaultsToBackground() throws Exception {
        AtomicReference<WorkRole> seen = new AtomicReference<>();
        Thread thread = new Thread(() -> seen.set(WorkRole.current()));
        thread.start();
        thread.join();
        assertThat(seen.get()).isEqualTo(WorkRole.BACKGROUND);
    }

    @Test
    @DisplayName("a scope sets the role and restores the previous one, nested")
    void scopesNest() {
        try (WorkRole.Scope outer = WorkRole.enter(WorkRole.API)) {
            assertThat(WorkRole.current()).isEqualTo(WorkRole.API);
            WorkRole.runAs(WorkRole.COLLECT, () -> assertThat(WorkRole.current()).isEqualTo(WorkRole.COLLECT));
            assertThat(WorkRole.current()).isEqualTo(WorkRole.API);
        }
        assertThat(WorkRole.current()).isEqualTo(WorkRole.BACKGROUND);
    }

    @Test
    @DisplayName("the role is not inherited: a thread started from a request runs as BACKGROUND")
    void notInherited() throws Exception {
        AtomicReference<WorkRole> seen = new AtomicReference<>();
        try (WorkRole.Scope ignored = WorkRole.enter(WorkRole.API)) {
            Thread child = Thread.ofVirtual().start(() -> seen.set(WorkRole.current()));
            child.join();
        }
        assertThat(seen.get()).isEqualTo(WorkRole.BACKGROUND);
    }

    @Test
    @DisplayName("WorkThreads makes named daemon platform threads in their role")
    void workThreads() throws Exception {
        AtomicReference<String> seen = new AtomicReference<>();
        Thread thread = WorkThreads.start(WorkRole.COLLECT, "foresignal-source-test",
                () -> seen.set(Thread.currentThread().getName() + " " + WorkRole.current()));
        thread.join();
        assertThat(thread.isVirtual()).isFalse();
        assertThat(thread.isDaemon()).isTrue();
        assertThat(seen.get()).isEqualTo("foresignal-source-test COLLECT");
        assertThat(WorkThreads.factory(WorkRole.INGEST, "w-").newThread(() -> {}).getName()).isEqualTo("w-0");
    }
}
