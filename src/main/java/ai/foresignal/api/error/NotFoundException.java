package ai.foresignal.api.error;

import org.springframework.http.HttpStatus;

/** 404: the addressed resource does not exist. */
public class NotFoundException extends ForesignalException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(String detail) {
        super(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND, detail);
    }

    /**
     * A 404 with a more specific code than {@code NOT_FOUND} (prompt 101: {@code UNKNOWN_EVENT},
     * {@code UNKNOWN_INVESTIGATION}).
     *
     * @param code the problem's {@code code}
     * @param detail the human-readable summary
     */
    public NotFoundException(String code, String detail) {
        super(HttpStatus.NOT_FOUND, code, detail);
    }

    /** For example {@code NotFoundException.of("Source", "usgs-earthquakes")}. */
    public static NotFoundException of(String what, Object id) {
        return new NotFoundException(what + " '" + id + "' does not exist.");
    }
}
