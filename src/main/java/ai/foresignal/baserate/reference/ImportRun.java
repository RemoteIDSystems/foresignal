package ai.foresignal.baserate.reference;

import ai.foresignal.backtest.JobMonitor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One run of a reference import (prompt 121): the import row, its resume state, the writer and the 103 job monitor.
 * {@link #writeChunk} replaces one chunk's weekly rows, adds the chunk to {@code progress.done}, widens the period and
 * saves the row, so a resumed run skips every chunk {@link #done} reports.
 */
public class ImportRun {

    private final ReferenceImportEntity entity;
    private final ReferenceHistoryWriter writer;
    private final ReferenceImportRepository imports;
    private final JobMonitor monitor;

    /**
     * Creates a run.
     *
     * @param entity the import
     * @param writer the history writer
     * @param imports the import rows (null: the row is not saved, as in unit tests)
     * @param monitor the job monitor
     */
    public ImportRun(ReferenceImportEntity entity, ReferenceHistoryWriter writer, ReferenceImportRepository imports,
            JobMonitor monitor) {
        this.entity = entity;
        this.writer = writer;
        this.imports = imports;
        this.monitor = monitor == null ? JobMonitor.NONE : monitor;
    }

    /** The import. */
    public ReferenceImportEntity entity() {
        return entity;
    }

    /** The import's id. */
    public String importId() {
        return entity.getId();
    }

    /** The normalized parameters. */
    public Map<String, Object> params() {
        return entity.getParams();
    }

    /** True when the chunk was completed by an earlier run of this import. */
    public boolean done(String chunk) {
        Object done = entity.getProgress().get("done");
        return done instanceof Collection<?> c && c.contains(chunk);
    }

    /**
     * Records progress and stops a canceled run.
     *
     * @param fraction 0–1
     * @throws JobMonitor.Canceled when the job was canceled
     */
    public void checkpoint(double fraction) {
        monitor.checkpoint(fraction);
        Map<String, Object> progress = new LinkedHashMap<>(entity.getProgress());
        progress.put("fraction", Math.max(0, Math.min(1, fraction)));
        entity.setProgress(progress);
    }

    /** Records the hash of an imported file. */
    public void fileSha256(String sha) {
        entity.setFileSha256(sha);
        save();
    }

    /**
     * Writes one chunk whole and marks it done.
     *
     * @param chunk the chunk's name (e.g. {@code 2019}, {@code YE:2019})
     * @param from the first week (inclusive)
     * @param to the end week (exclusive)
     * @param countries the countries the chunk covers, or empty for all
     * @param counts the weekly counts
     * @param rowsRead the upstream rows read for the chunk
     * @return the weekly rows written
     */
    public int writeChunk(String chunk, LocalDate from, LocalDate to, Collection<String> countries, WeeklyCounts counts,
            long rowsRead) {
        int written = writer.replace(ReferenceFamilies.DATASET_SOURCE.getOrDefault(entity.getDataset(),
                entity.getDataset()), entity.getId(), from, to, countries, counts.rows());
        entity.setRowsRead(entity.getRowsRead() + rowsRead);
        entity.setRowsWritten(entity.getRowsWritten() + written);
        if (entity.getPeriodFrom() == null || from.isBefore(entity.getPeriodFrom())) {
            entity.setPeriodFrom(from);
        }
        if (entity.getPeriodTo() == null || to.isAfter(entity.getPeriodTo())) {
            entity.setPeriodTo(to);
        }
        Map<String, Object> progress = new LinkedHashMap<>(entity.getProgress());
        List<Object> done = new ArrayList<>();
        if (progress.get("done") instanceof Collection<?> c) {
            done.addAll(c);
        }
        if (!done.contains(chunk)) {
            done.add(chunk);
        }
        progress.put("done", done);
        entity.setProgress(progress);
        save();
        return written;
    }

    private void save() {
        if (imports != null) {
            imports.save(entity);
        }
    }
}
