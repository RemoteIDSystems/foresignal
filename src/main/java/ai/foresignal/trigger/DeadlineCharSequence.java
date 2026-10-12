package ai.foresignal.trigger;

/**
 * A {@link CharSequence} that throws {@link Timeout} once a deadline passes, so a regex match over
 * it cannot run away (prompt 017: the gate evaluates {@code matches} with a 10 ms deadline). The
 * regex engine reads characters through {@link #charAt}, so checking the clock there bounds any match.
 */
public final class DeadlineCharSequence implements CharSequence {

    /** Thrown when the deadline has passed. */
    public static final class Timeout extends RuntimeException {

        private static final long serialVersionUID = 1L;

        Timeout() {
            super("regex evaluation exceeded its deadline", null, false, false);
        }
    }

    private final CharSequence text;
    private final long deadlineNanos;
    private int calls;

    /**
     * Wraps {@code text} with a deadline {@code budgetNanos} from now.
     *
     * @param text the text
     * @param budgetNanos the time allowed
     */
    public DeadlineCharSequence(CharSequence text, long budgetNanos) {
        this(text, System.nanoTime() + budgetNanos, true);
    }

    private DeadlineCharSequence(CharSequence text, long deadlineNanos, boolean absolute) {
        this.text = text;
        this.deadlineNanos = deadlineNanos;
    }

    @Override
    public int length() {
        return text.length();
    }

    @Override
    public char charAt(int index) {
        // Reading the clock on every call would dominate short matches; every 64th call is enough.
        if ((++calls & 63) == 0 && System.nanoTime() - deadlineNanos > 0) {
            throw new Timeout();
        }
        return text.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new DeadlineCharSequence(text.subSequence(start, end), deadlineNanos, true);
    }

    @Override
    public String toString() {
        return text.toString();
    }
}
