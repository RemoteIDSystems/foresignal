package ai.foresignal.cli.sse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.SplittableRandom;
import org.junit.jupiter.api.Test;

/** Jittered exponential backoff from 1 s to 60 s with server floors. */
class BackoffTest {

    @Test
    void doublesWithJitterAndCapsAtSixtySeconds() {
        Backoff backoff = new Backoff(new SplittableRandom(1));
        long[] bases = {1000, 2000, 4000, 8000, 16000, 32000, 60000, 60000, 60000};
        for (long base : bases) {
            long delay = backoff.next(null).toMillis();
            assertTrue(delay >= Math.round(base * 0.8) && delay <= Math.min(60000, Math.round(base * 1.2)),
                    "base " + base + " gave " + delay);
        }
        assertEquals(bases.length, backoff.attempts());
    }

    @Test
    void resetStartsOver() {
        Backoff backoff = new Backoff(new SplittableRandom(2));
        backoff.next(null);
        backoff.next(null);
        backoff.reset();
        assertEquals(0, backoff.attempts());
        assertTrue(backoff.next(null).toMillis() <= 1200);
    }

    @Test
    void aServerFloorWinsWhenItIsLonger() {
        Backoff backoff = new Backoff(new SplittableRandom(3));
        assertEquals(Duration.ofSeconds(30), backoff.next(Duration.ofSeconds(30)));
        assertTrue(backoff.next(Duration.ofMillis(10)).toMillis() >= 1600, "a short floor does not shorten");
    }

    @Test
    void manyAttemptsDoNotOverflow() {
        Backoff backoff = new Backoff(Duration.ofSeconds(1), Duration.ofSeconds(60), new SplittableRandom(4));
        for (int i = 0; i < 100; i++) {
            assertTrue(backoff.next(null).toMillis() <= 60000);
        }
    }
}
