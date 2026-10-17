package ai.foresignal.baserate.reference;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The {@code ReferenceImport} API shapes (prompt 121). */
public final class ReferenceImportViews {

    private ReferenceImportViews() {}

    /** The {@code ReferenceImport} object. */
    public static Map<String, Object> view(ReferenceImportEntity e) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", e.getId());
        out.put("dataset", e.getDataset());
        out.put("params", e.getParams());
        out.put("status", e.getStatus());
        out.put("statusReason", e.getStatusReason());
        Object fraction = e.getProgress().get("fraction");
        out.put("progress", "done".equals(e.getStatus()) ? Double.valueOf(1.0)
                : fraction instanceof Number n ? n.doubleValue() : null);
        out.put("fileSha256", e.getFileSha256());
        out.put("rowsRead", e.getRowsRead());
        out.put("rowsWritten", e.getRowsWritten());
        out.put("periodFrom", e.getPeriodFrom() == null ? null : e.getPeriodFrom().toString());
        out.put("periodTo", e.getPeriodTo() == null ? null : e.getPeriodTo().toString());
        out.put("licence", e.getLicense());
        out.put("egress", e.getEgress());
        out.put("jobId", e.getJobId());
        out.put("startedAt", e.getStartedAt() == null ? null : e.getStartedAt().toString());
        out.put("finishedAt", e.getFinishedAt() == null ? null : e.getFinishedAt().toString());
        out.put("error", e.getError());
        out.put("createdBy", e.getCreatedBy());
        out.put("createdAt", e.getCreatedAt().toString());
        return out;
    }

    /** The {@code ReferenceImportPage} object. */
    public static Map<String, Object> page(List<ReferenceImportEntity> items, String nextCursor) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items.stream().map(ReferenceImportViews::view).toList());
        out.put("nextCursor", nextCursor);
        return out;
    }
}
