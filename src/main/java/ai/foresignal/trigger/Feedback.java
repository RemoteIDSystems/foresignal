package ai.foresignal.trigger;

import java.util.List;

/** Alert-quality feedback, counted per rule in {@code feedbackCounts}. */
public enum Feedback {
    /** The alert was worth having. */
    USEFUL("useful"),
    /** The alert was noise. */
    NOISE("noise"),
    /** The alert came too late to act on. */
    LATE("late"),
    /** The alert was wrong. */
    FALSE_POSITIVE("false_positive");

    /** The wire values, in declaration order. */
    public static final List<String> WIRE_VALUES = List.of("useful", "noise", "late", "false_positive");

    private final String wire;

    Feedback(String wire) {
        this.wire = wire;
    }

    /** The wire value stored in {@code alert.feedback}. */
    public String wire() {
        return wire;
    }

    /**
     * Parses a wire value.
     *
     * @throws IllegalArgumentException for anything else
     */
    public static Feedback fromWire(String value) {
        for (Feedback feedback : values()) {
            if (feedback.wire.equals(value)) {
                return feedback;
            }
        }
        throw new IllegalArgumentException("must be one of useful, noise, late, false_positive");
    }
}
