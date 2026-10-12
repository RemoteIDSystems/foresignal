package ai.foresignal.inbound.format;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import org.springframework.http.HttpStatus;

/**
 * 403 {@code DEVICE_ENTITY_MISMATCH} (prompt 039): a device-scoped ingest key ({@code eid_}) pushed a
 * payload that names another entity than the one the key is bound to. Nothing of the request is
 * written.
 */
public class DeviceEntityMismatchException extends ForesignalException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param claimed the id the payload named
     * @param bound the id the key is bound to
     */
    public DeviceEntityMismatchException(String claimed, String bound) {
        super(HttpStatus.FORBIDDEN, ErrorCodes.DEVICE_ENTITY_MISMATCH,
                "This device key is bound to another entity than the one the payload names ('"
                        + abbreviate(claimed) + "').");
    }

    private static String abbreviate(String value) {
        return value == null ? "" : value.length() <= 64 ? value : value.substring(0, 64) + "…";
    }
}
