package ai.foresignal.doctor;

import java.util.List;

/**
 * One page of doctor reports, newest first (OpenAPI {@code DoctorReportPage}).
 *
 * @param items the reports
 * @param nextCursor the cursor of the next page, or null at the end
 */
public record DoctorReportPage(List<DoctorReportView> items, String nextCursor) {

    /** Copies the list. */
    public DoctorReportPage {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
