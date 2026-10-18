package ai.foresignal.doctor;

import ai.foresignal.retention.RetentionRun;
import ai.foresignal.retention.RetentionStep;
import java.time.Duration;
import org.springframework.stereotype.Component;

/** Deletes adapter-doctor reports (prompt 140) older than {@value #RETENTION_DAYS} days. */
@Component
public class DoctorReportRetentionStep implements RetentionStep {

    /** How long a doctor report is kept. */
    public static final int RETENTION_DAYS = 90;

    private final DoctorReportRepository reports;

    /** Creates the step. */
    public DoctorReportRetentionStep(DoctorReportRepository reports) {
        this.reports = reports;
    }

    @Override
    public String name() {
        return "doctor-reports";
    }

    @Override
    public int order() {
        return 714;
    }

    @Override
    public void run(RetentionRun run) {
        run.deleted("doctor_report", reports.deleteCreatedBefore(run.now().minus(Duration.ofDays(RETENTION_DAYS))));
    }
}
