package ai.foresignal.reference;

/**
 * A dataset could not be loaded (prompt 040). {@link #code()} is stored in {@code ref_dataset.error} and
 * shown in {@code ReferenceDataset.error}, for example {@code REFERENCE_CHECKSUM_MISMATCH}.
 */
public class ReferenceDataException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** A downloaded or pre-placed file does not have the pinned SHA-256. */
    public static final String CHECKSUM_MISMATCH = "REFERENCE_CHECKSUM_MISMATCH";

    /** The download failed. */
    public static final String DOWNLOAD_FAILED = "REFERENCE_DOWNLOAD_FAILED";

    /** {@code offlineDir} is set and does not hold the file. */
    public static final String FILE_MISSING = "REFERENCE_FILE_MISSING";

    /** The file could not be parsed or stored. */
    public static final String LOAD_FAILED = "REFERENCE_LOAD_FAILED";

    private final String code;

    /** A failure with {@code code} and a message. */
    public ReferenceDataException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /** A failure with {@code code} and a message. */
    public ReferenceDataException(String code, String message) {
        this(code, message, null);
    }

    /** The machine-readable code. */
    public String code() {
        return code;
    }
}
