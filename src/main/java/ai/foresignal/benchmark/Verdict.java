package ai.foresignal.benchmark;

import java.util.Collection;

/** A benchmark result against the documented thresholds (prompt 123): {@code pass}, {@code warn} or {@code fail}. */
public enum Verdict {

    /** Within the pass threshold. */
    PASS("pass"),
    /** Outside the pass threshold but within the warn threshold, or not measurable. */
    WARN("warn"),
    /** Outside the warn threshold. */
    FAIL("fail");

    private final String wire;

    Verdict(String wire) {
        this.wire = wire;
    }

    /** The wire value. */
    public String wire() {
        return wire;
    }

    /** The worse of two verdicts. */
    public Verdict worse(Verdict other) {
        return other == null || compareTo(other) >= 0 ? this : other;
    }

    /** The worst of several verdicts ({@link #PASS} when there are none). */
    public static Verdict worst(Collection<Verdict> verdicts) {
        Verdict out = PASS;
        for (Verdict v : verdicts) {
            out = out.worse(v);
        }
        return out;
    }

    /** The verdict of a wire value, or null. */
    public static Verdict fromWire(String value) {
        for (Verdict v : values()) {
            if (v.wire.equals(value)) {
                return v;
            }
        }
        return null;
    }
}
