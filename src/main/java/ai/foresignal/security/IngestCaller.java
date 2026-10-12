package ai.foresignal.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Optional;

/**
 * Who is pushing on an ingest path (prompt 024), set by {@link TokenFilter} as a request attribute.
 *
 * <p>An ingest-key caller is bound to one source: {@link #sourceId()} is the source the key belongs
 * to. An admin token caller ({@link #ADMIN_TOKEN}) may push into any push source. A caller that
 * presented no key on a source whose verifier is {@code keyOptional} is {@link #SIGNATURE_ONLY}:
 * the endpoint must then verify the signature before accepting anything.
 *
 * @param via how the caller authenticated
 * @param sourceId the source the credential is bound to, or null for an admin token
 * @param principal the {@code fetch_log.principal}: {@code ingest-key:<prefix>},
 *     {@code compat-ntfy:<prefix>}, {@code compat-gotify:<prefix>}, {@code token:<name>} or
 *     {@code signature:<sourceId>}
 * @param keyPrefix the ingest key's 8-character prefix, or null
 */
public record IngestCaller(String via, String sourceId, String principal, String keyPrefix) {

    /** The request attribute holding the caller. */
    public static final String ATTRIBUTE = "ai.foresignal.ingestCaller";

    /** An ingest key in a header, a bearer or Basic credential. */
    public static final String INGEST_KEY = "ingest-key";

    /** An ingest key through ntfy compatibility ({@code ?auth=} or ntfy headers). */
    public static final String COMPAT_NTFY = "compat-ntfy";

    /** An ingest key through Gotify compatibility ({@code X-Gotify-Key} or {@code ?token=}). */
    public static final String COMPAT_GOTIFY = "compat-gotify";

    /** An admin API token. */
    public static final String ADMIN_TOKEN = "token";

    /** No key; a {@code keyOptional} signature verifier must authenticate the request. */
    public static final String SIGNATURE_ONLY = "signature";

    /** Requires a mode and a principal. */
    public IngestCaller {
        Objects.requireNonNull(via, "via");
        Objects.requireNonNull(principal, "principal");
    }

    /** True when the caller presented an ingest key. */
    public boolean viaKey() {
        return INGEST_KEY.equals(via) || COMPAT_NTFY.equals(via) || COMPAT_GOTIFY.equals(via);
    }

    /** True for an admin token. */
    public boolean admin() {
        return ADMIN_TOKEN.equals(via);
    }

    /** True when only a signature can authenticate this request. */
    public boolean signatureOnly() {
        return SIGNATURE_ONLY.equals(via);
    }

    /**
     * True when this caller may push into {@code target}: an admin token anywhere, a key or a
     * signature-only caller only into its own source.
     */
    public boolean mayPushTo(String target) {
        return admin() || (sourceId != null && sourceId.equals(target));
    }

    /** The caller of {@code request}, if the filter set one. */
    public static Optional<IngestCaller> of(HttpServletRequest request) {
        Object value = request.getAttribute(ATTRIBUTE);
        return value instanceof IngestCaller caller ? Optional.of(caller) : Optional.empty();
    }
}
