package ai.foresignal.exercise;

import ai.foresignal.retention.RetentionRun;
import ai.foresignal.retention.RetentionStep;
import org.springframework.stereotype.Component;

/**
 * Expires exercise alerts (prompt 115) after {@code exercise.alertRetention} (default 7 days), with their
 * notifications, deliveries, push dispatches, action capabilities and CAP messages. Real alerts are untouched.
 */
@Component
public class ExerciseRetentionStep implements RetentionStep {

    private final ExerciseService exercises;

    /** Creates the step. */
    public ExerciseRetentionStep(ExerciseService exercises) {
        this.exercises = exercises;
    }

    @Override
    public String name() {
        return "exercise-alerts";
    }

    @Override
    public int order() {
        return 705;
    }

    @Override
    public void run(RetentionRun run) {
        run.deleted("alert (exercise)", exercises.expire(run.now().minus(exercises.alertRetention())));
    }
}
