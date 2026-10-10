package ai.elaro.api.error;

import org.springframework.http.HttpStatus;

/** 404: the addressed resource does not exist. */
public class NotFoundException extends ElaroException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(String detail) {
        super(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND, detail);
    }

    /** For example {@code NotFoundException.of("Source", "usgs-earthquakes")}. */
    public static NotFoundException of(String what, Object id) {
        return new NotFoundException(what + " '" + id + "' does not exist.");
    }
}
