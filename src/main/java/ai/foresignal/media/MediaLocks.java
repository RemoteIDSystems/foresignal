package ai.foresignal.media;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

/**
 * Striped locks keyed by SHA-256 (prompt 043). A store that reuses an existing file and inserts its
 * row, and a retention unlink that checks no row references the file, hold the same stripe, so a file
 * is never unlinked between another store's reuse and its insert.
 */
@Component
public class MediaLocks {

    /** The number of stripes. */
    static final int STRIPES = 64;

    private final ReentrantLock[] locks = new ReentrantLock[STRIPES];

    /** Creates the stripes. */
    public MediaLocks() {
        for (int i = 0; i < STRIPES; i++) {
            locks[i] = new ReentrantLock();
        }
    }

    /** Runs {@code work} holding the stripe of {@code sha256}. */
    public <T> T withLock(String sha256, Supplier<T> work) {
        ReentrantLock lock = locks[Math.floorMod(sha256.hashCode(), STRIPES)];
        lock.lock();
        try {
            return work.get();
        } finally {
            lock.unlock();
        }
    }
}
