package ai.foresignal.ingest;

/**
 * The ingest queue stayed full for the whole submit timeout: the writer is behind, held by the
 * clock guard, or waiting for its lock. Producers back off and retry later.
 */
public class IngestBackpressureException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Creates the exception. */
    public IngestBackpressureException(String message) {
        super(message);
    }
}
