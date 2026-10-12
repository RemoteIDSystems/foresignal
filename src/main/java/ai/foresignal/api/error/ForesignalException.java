package ai.foresignal.api.error;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;

/**
 * Base class for errors that map directly to an API problem response.
 *
 * <p>Carrying the status and code on the exception keeps the decision where the context is — the
 * service that knows why something failed — instead of in a handler that would have to infer it.
 */
public class ForesignalException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final HttpStatus status;
    private final String code;
    private final transient Map<String, Object> properties = new LinkedHashMap<>();

    public ForesignalException(HttpStatus status, String code, String detail) {
        this(status, code, detail, null);
    }

    public ForesignalException(HttpStatus status, String code, String detail, Throwable cause) {
        super(detail, cause);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    /** The stable machine code placed in the problem's {@code code} property. */
    public String getCode() {
        return code;
    }

    /**
     * Adds a problem extension member, for example {@code purgeHorizon} on {@code RESYNC_REQUIRED}.
     *
     * @param name the member name
     * @param value the value; may be null, which is written as JSON null
     * @return this exception
     */
    public ForesignalException withProperty(String name, Object value) {
        properties.put(name, value);
        return this;
    }

    /** The problem extension members added with {@link #withProperty}, in insertion order. */
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }
}
