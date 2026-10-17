package ai.foresignal.cycle;

import java.util.concurrent.atomic.AtomicLong;

/** The {@code max_tokens_per_cycle} budget shared by every role of one run (prompt 128 §3). Thread-safe. */
public final class TokenBudget {

    private final long limit;
    private final AtomicLong spent = new AtomicLong();

    /** A budget of {@code limit} tokens (input plus output). */
    public TokenBudget(long limit) {
        this.limit = Math.max(0, limit);
    }

    /** The tokens left (never negative). */
    public long remaining() {
        return Math.max(0, limit - spent.get());
    }

    /** True when nothing is left. */
    public boolean exhausted() {
        return remaining() <= 0;
    }

    /** Records {@code tokens} spent. */
    public void spend(long tokens) {
        spent.addAndGet(Math.max(0, tokens));
    }

    /** The tokens spent. */
    public long spent() {
        return spent.get();
    }

    /** The limit. */
    public long limit() {
        return limit;
    }
}
