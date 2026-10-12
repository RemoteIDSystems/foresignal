package ai.foresignal.inbound;

import ai.foresignal.inbound.format.IngestFormatRegistry;
import ai.foresignal.inbound.signature.VerifierConfig;
import ai.foresignal.security.IngestCaller;
import ai.foresignal.security.IngestKeyAuthenticator;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * The ingest-key half of {@code INGEST_KEY_OR_ADMIN} (prompt 024).
 *
 * <p>Accepted credentials, each checked against the stored SHA-256 with a constant-time comparison:
 *
 * <ol>
 *   <li>{@code X-Foresignal-Ingest-Key: eik_…}
 *   <li>{@code Authorization: Bearer eik_…}
 *   <li>{@code Authorization: Basic} with the key as the password (any username)
 *   <li>compatibility only: ntfy {@code ?auth=} (base64 of an {@code Authorization} header value) on
 *       {@code /compat/ntfy}, Gotify {@code X-Gotify-Key} or {@code ?token=} on {@code /compat/gotify}
 * </ol>
 *
 * <p>A key is bound to its source: when the path names a source ({@code /v1/ingest/{sourceId}},
 * {@code /v1/external/sources/{sourceId}/…}, {@code /compat/ntfy/{topic}}), a key of another source
 * is refused. With no key at all, a source whose verifier for the path's format is
 * {@code keyOptional} lets the request through as {@link IngestCaller#SIGNATURE_ONLY}; the endpoint
 * then requires a valid signature. The query parameters {@code auth} and {@code token} are redacted
 * from every log line and fetch-log URL ({@link IngestUrls}).
 */
@Component
public class IngestAuthenticator implements IngestKeyAuthenticator {

    /** The ingest key header. */
    public static final String KEY_HEADER = "X-Foresignal-Ingest-Key";

    /** The Gotify key header. */
    public static final String GOTIFY_HEADER = "X-Gotify-Key";

    private final CustomSourceRepository repository;
    private final CustomSourceVerifiers verifiers;
    private final IngestKeys keys;

    /** Creates the authenticator. */
    public IngestAuthenticator(CustomSourceRepository repository, CustomSourceVerifiers verifiers, IngestKeys keys) {
        this.repository = repository;
        this.verifiers = verifiers;
        this.keys = keys;
    }

    @Override
    public Result authenticate(HttpServletRequest request) {
        IngestPath path = IngestPath.of(request.getRequestURI());
        Credential credential = credential(request, path);
        if (credential == null) {
            return signatureOnly(path);
        }
        if (credential.key() == null || !credential.key().startsWith(IngestKeys.PREFIX)) {
            return Result.rejected("The presented ingest key is not valid.");
        }
        Optional<CustomSourceEntity> row = repository.findByIngestKeyHash(keys.hash(credential.key()));
        if (row.isEmpty() || !keys.matches(credential.key(), row.get().getIngestKeyHash())) {
            return Result.rejected("The presented ingest key is not valid.");
        }
        String sourceId = row.get().getSourceId();
        if (path.sourceId() != null && !path.sourceId().equals(sourceId)) {
            return Result.rejected("The presented ingest key does not belong to this source.");
        }
        String prefix = row.get().getIngestKeyPrefix();
        return Result.accepted(new IngestCaller(credential.via(), sourceId, credential.via() + ":" + prefix, prefix));
    }

    /** No credential: a keyOptional verifier on the path's source and format lets it through. */
    private Result signatureOnly(IngestPath path) {
        if (path.sourceId() == null || path.format() == null) {
            return Result.none();
        }
        Optional<CustomSourceEntity> row = repository.findById(path.sourceId());
        if (row.isEmpty()) {
            return Result.none();
        }
        VerifierConfig config = verifiers.config(row.get(), path.format());
        if (config != null && config.active() && config.keyOptional()) {
            return Result.accepted(new IngestCaller(
                    IngestCaller.SIGNATURE_ONLY, path.sourceId(), "signature:" + path.sourceId(), null));
        }
        return Result.none();
    }

    /** The presented key and how it came, or null when none was presented. */
    static Credential credential(HttpServletRequest request, IngestPath path) {
        String via = path.compat() == null ? IngestCaller.INGEST_KEY : path.compat();
        String header = request.getHeader(KEY_HEADER);
        if (header != null && !header.isBlank()) {
            return new Credential(header.strip(), via);
        }
        Credential fromAuthorization = fromAuthorization(request.getHeader(HttpHeaders.AUTHORIZATION), via);
        if (fromAuthorization != null) {
            return fromAuthorization;
        }
        if (IngestCaller.COMPAT_NTFY.equals(path.compat())) {
            String auth = request.getParameter("auth");
            if (auth != null && !auth.isBlank()) {
                try {
                    String decoded = new String(Base64.getDecoder().decode(auth.strip()), StandardCharsets.UTF_8);
                    Credential inner = fromAuthorization(decoded, via);
                    return inner == null ? new Credential(null, via) : inner;
                } catch (IllegalArgumentException e) {
                    try {
                        String decoded = new String(Base64.getUrlDecoder().decode(auth.strip()), StandardCharsets.UTF_8);
                        Credential inner = fromAuthorization(decoded, via);
                        return inner == null ? new Credential(null, via) : inner;
                    } catch (IllegalArgumentException again) {
                        return new Credential(null, via);
                    }
                }
            }
        }
        if (IngestCaller.COMPAT_GOTIFY.equals(path.compat())) {
            String gotify = request.getHeader(GOTIFY_HEADER);
            if (gotify != null && !gotify.isBlank()) {
                return new Credential(gotify.strip(), via);
            }
            String token = request.getParameter("token");
            if (token != null && !token.isBlank()) {
                return new Credential(token.strip(), via);
            }
        }
        return null;
    }

    /** A key from an {@code Authorization} value: {@code Bearer eik_…} or Basic with the key as password. */
    static Credential fromAuthorization(String value, String via) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.strip();
        if (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return new Credential(trimmed.substring(7).strip(), via);
        }
        if (trimmed.regionMatches(true, 0, "Basic ", 0, 6)) {
            try {
                String decoded = new String(Base64.getDecoder().decode(trimmed.substring(6).strip()),
                        StandardCharsets.UTF_8);
                int colon = decoded.indexOf(':');
                return new Credential(colon < 0 ? decoded : decoded.substring(colon + 1), via);
            } catch (IllegalArgumentException e) {
                return new Credential(null, via);
            }
        }
        return null;
    }

    /**
     * A presented credential.
     *
     * @param key the key, or null when the credential was present but unreadable
     * @param via {@link IngestCaller#INGEST_KEY}, {@code COMPAT_NTFY} or {@code COMPAT_GOTIFY}
     */
    record Credential(String key, String via) {}

    /**
     * What an ingest path names.
     *
     * @param sourceId the source named in the path, or null ({@code /compat/ntfy} JSON, Gotify)
     * @param format the ingest format, or null when the path has none
     * @param compat {@code compat-ntfy}, {@code compat-gotify} or null
     */
    record IngestPath(String sourceId, String format, String compat) {

        static IngestPath of(String uri) {
            String path = uri == null ? "" : uri;
            int query = path.indexOf('?');
            if (query >= 0) {
                path = path.substring(0, query);
            }
            String[] s = path.split("/");
            // ["", "v1", "ingest", "{sourceId}", "{format}"]
            if (s.length >= 4 && "v1".equals(s[1]) && "ingest".equals(s[2])) {
                String format = s.length >= 5 ? s[4] : IngestFormatRegistry.DEFAULT_FORMAT;
                return new IngestPath(s[3], format, null);
            }
            // ["", "v1", "external", "sources", "{sourceId}", "items"]
            if (s.length >= 6 && "v1".equals(s[1]) && "external".equals(s[2]) && "sources".equals(s[3])) {
                return new IngestPath(s[4], null, null);
            }
            // ["", "compat", "ntfy", "{topic}", ...]
            if (s.length >= 3 && "compat".equals(s[1]) && "ntfy".equals(s[2])) {
                return new IngestPath(s.length >= 4 ? s[3] : null, null, IngestCaller.COMPAT_NTFY);
            }
            if (s.length >= 3 && "compat".equals(s[1]) && "gotify".equals(s[2])) {
                return new IngestPath(null, null, IngestCaller.COMPAT_GOTIFY);
            }
            return new IngestPath(null, null, null);
        }
    }
}
