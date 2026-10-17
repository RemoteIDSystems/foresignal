package ai.foresignal.baserate.reference;

import java.time.LocalDate;
import java.util.Map;

/**
 * One reference dataset's importer (prompt 121): it validates and normalizes its parameters, says whether the dataset
 * can be read now, and runs as chunks (a year, or a country-year) that are each written whole by
 * {@link ReferenceHistoryWriter}, so an interrupted import resumes at its first unfinished chunk.
 */
public interface ReferenceImporter {

    /** The dataset ({@code ucdp-ged}, {@code acled}, {@code usgs-comcat} or {@code gdacs}). */
    String dataset();

    /** The reference source the rows are written under. */
    String source();

    /**
     * Validates and normalizes the request parameters (defaults filled in, lists sorted).
     *
     * @param params the request's {@code params}
     * @return the normalized parameters, the idempotency key's input
     * @throws ai.foresignal.api.error.ValidationException 400 for invalid parameters
     */
    Map<String, Object> normalize(Map<String, Object> params);

    /**
     * Whether the dataset can be read with these parameters now.
     *
     * @param params the normalized parameters
     * @return the availability
     */
    Availability availability(Map<String, Object> params);

    /**
     * The period the parameters cover: {@code [from, to)}, both Mondays.
     *
     * @param params the normalized parameters
     * @return the period, or null when only the data can tell (a file)
     */
    LocalDate[] period(Map<String, Object> params);

    /**
     * Runs the import.
     *
     * @param run the run (its import, the resume state, the writer and the job monitor)
     * @throws Exception when the run fails; completed chunks stay written
     */
    void run(ImportRun run) throws Exception;

    /**
     * Whether a dataset can be read.
     *
     * @param state {@code AVAILABLE}, {@code SKIP} (recorded as a {@code skipped} import, e.g. ACLED without
     *     credentials) or {@code UNAVAILABLE} (refused with 409 {@code IMPORT_DATASET_UNAVAILABLE})
     * @param reason why not, or null
     */
    record Availability(State state, String reason) {

        /** The states. */
        public enum State {
            /** The dataset can be read. */
            AVAILABLE,
            /** The import is recorded as skipped. */
            SKIP,
            /** The request is refused. */
            UNAVAILABLE
        }

        /** Available. */
        public static Availability available() {
            return new Availability(State.AVAILABLE, null);
        }

        /** Recorded as skipped, with the reason. */
        public static Availability skip(String reason) {
            return new Availability(State.SKIP, reason);
        }

        /** Refused, with the reason. */
        public static Availability unavailable(String reason) {
            return new Availability(State.UNAVAILABLE, reason);
        }
    }
}
