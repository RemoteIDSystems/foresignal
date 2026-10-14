package ai.foresignal.inbound;

import ai.foresignal.inbound.device.IngestDeviceEntity;
import ai.foresignal.inbound.device.IngestDeviceService;
import ai.foresignal.inbound.format.IngestFormatRegistry;
import ai.foresignal.inbound.signature.VerifierConfig;
import ai.foresignal.security.IngestCaller;
import ai.foresignal.security.IngestKeyAuthenticator;
import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
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
 *
 * <p>Prompt 039 adds two credentials on {@code /v1/ingest/…}:
 *
 * <ul>
 *   <li>device-scoped keys ({@code eid_}, {@link IngestDeviceService}) in any of the forms above: valid
 *       only for their own source, with the caller bound to the device's entity
 *       ({@link IngestCaller#DEVICE_KEY});
 *   <li>a key in the path, {@code /v1/ingest/{sourceId}/k/{key}/{format}}, taken by
 *       {@link IngestKeyRedactionFilter} before anything logs the URI. A path key wins over header
 *       credentials; when both are present they must be the same key, else the request is refused.
 *       An {@code eik_} key in the path is accepted here and refused later with {@code 404} unless the
 *       source sets {@code allowPathKey} ({@link InboundRequests}).
 * </ul>
 *
 * <p>Prompt 059 adds the WU protocol's {@code PASSWORD} query parameter on {@code /v1/ingest/{sourceId}/wu-pws}
 * (the station key of WU-protocol firmware); its value is redacted like {@code key=} ({@link IngestUrls}). A push
 * with a query key also passes the sender allowlist, like any GET push ({@link InboundRequests}).
 *
 * <p>Prompt 054: a built-in source that {@code acceptsRemoteMedia} (the {@code satdump-*} family) has no
 * custom-source row and so no minted ingest key. On its own {@code /v1/ingest/{sourceId}/media} and
 * {@code /rasters} paths, a key that matches no custom source is compared, in constant time, with the
 * source's encrypted {@code ingestKey} credential ({@link SourceDescriptor#REMOTE_MEDIA_KEY}); a match
 * is an ingest-key caller bound to that source.
 *
 * <p>Prompt 083: a built-in PUSH source that declares the {@code ingestKey} credential (the {@code meshcore-bridge}
 * source) is matched the same way on its own {@code /v1/ingest/{sourceId}/{format}} paths (not in the path, not a
 * compatibility route), so a bridge never needs an admin token.
 */
@Component
public class IngestAuthenticator implements IngestKeyAuthenticator {

    /** The ingest key header. */
    public static final String KEY_HEADER = "X-Foresignal-Ingest-Key";

    /** The Gotify key header. */
    public static final String GOTIFY_HEADER = "X-Gotify-Key";

    /** The format whose query parameter {@link #WU_PASSWORD} carries the ingest key (prompt 059). */
    public static final String WU_FORMAT = "wu-pws";

    /** The WU protocol's station-key parameter; {@code ID} is ignored. */
    public static final String WU_PASSWORD = "PASSWORD";

    private final CustomSourceRepository repository;
    private final CustomSourceVerifiers verifiers;
    private final IngestKeys keys;
    private final IngestDeviceService devices;
    private final ObjectProvider<SourceRegistry> registry;
    private final ObjectProvider<CredentialsAccess> credentials;

    /**
     * Creates the authenticator.
     *
     * @param registry the sources, for the remote-media key (prompt 054), resolved lazily
     * @param credentials the decrypted credentials, for the remote-media key (prompt 054), resolved lazily
     */
    public IngestAuthenticator(CustomSourceRepository repository, CustomSourceVerifiers verifiers, IngestKeys keys,
            IngestDeviceService devices, ObjectProvider<SourceRegistry> registry,
            ObjectProvider<CredentialsAccess> credentials) {
        this.repository = repository;
        this.verifiers = verifiers;
        this.keys = keys;
        this.devices = devices;
        this.registry = registry;
        this.credentials = credentials;
    }

    @Override
    public Result authenticate(HttpServletRequest request) {
        IngestPath path = IngestPath.of(request.getRequestURI());
        Credential credential = credential(request, path);
        String pathKey = IngestKeyRedactionFilter.pathKey(request);
        if (pathKey != null) {
            if (credential != null && !pathKey.equals(credential.key())) {
                return Result.rejected("The key in the path and the key in the headers differ.");
            }
            credential = new Credential(pathKey, IngestCaller.INGEST_KEY);
        }
        if (credential == null) {
            return signatureOnly(path);
        }
        if (credential.key() != null && credential.key().startsWith(IngestDeviceService.PREFIX)) {
            return device(credential.key(), path);
        }
        if (credential.key() == null || !credential.key().startsWith(IngestKeys.PREFIX)) {
            return Result.rejected("The presented ingest key is not valid.");
        }
        Optional<CustomSourceEntity> row = repository.findByIngestKeyHash(keys.hash(credential.key()));
        if (row.isEmpty()) {
            Optional<Result> remote = remoteMediaKey(credential.key(), path);
            if (remote.isPresent()) {
                return remote.get();
            }
        }
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

    /**
     * Prompt 054: the {@code ingestKey} credential of a source that accepts remote media, on that source's
     * own {@code media} or {@code rasters} path only; prompt 083: of a built-in PUSH source that declares the
     * credential, on that source's own format paths.
     */
    Optional<Result> remoteMediaKey(String key, IngestPath path) {
        if (path.sourceId() == null || path.keyInPath() || path.compat() != null || path.format() == null) {
            return Optional.empty();
        }
        boolean mediaPath = SourceDescriptor.REMOTE_MEDIA_SEGMENTS.contains(path.format());
        SourceRegistry sources = registry == null ? null : registry.getIfAvailable();
        CredentialsAccess access = credentials == null ? null : credentials.getIfAvailable();
        if (sources == null || access == null) {
            return Optional.empty();
        }
        Optional<SourceRegistry.Registration> source = sources.find(path.sourceId());
        if (source.isEmpty() || !(mediaPath ? source.get().descriptor().acceptsRemoteMedia()
                : builtInPushWithKey(source.get().descriptor()))) {
            return Optional.empty();
        }
        String stored;
        try {
            stored = access.forSource(path.sourceId()).get(SourceDescriptor.REMOTE_MEDIA_KEY);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
        if (stored == null || stored.isBlank() || !keys.matches(key, keys.hash(stored.strip()))) {
            return Optional.empty();
        }
        String prefix = IngestKeys.prefixOf(key);
        return Optional.of(Result.accepted(new IngestCaller(IngestCaller.INGEST_KEY, path.sourceId(),
                IngestCaller.INGEST_KEY + ":" + prefix, prefix)));
    }

    /**
     * True for a built-in PUSH source that declares the {@code ingestKey} credential (prompt 083): its key may be
     * presented on its own format paths.
     */
    static boolean builtInPushWithKey(SourceDescriptor descriptor) {
        return descriptor.mode() == ai.foresignal.source.spi.Mode.PUSH && !descriptor.custom()
                && descriptor.credentialFields().stream()
                        .anyMatch(field -> SourceDescriptor.REMOTE_MEDIA_KEY.equals(field.name()));
    }

    /**
     * A device-scoped key: only on {@code /v1/ingest/{sourceId}/…} of its own source, and only while
     * not revoked.
     */
    private Result device(String key, IngestPath path) {
        if (path.compat() != null || path.sourceId() == null || path.format() == null) {
            return Result.rejected("A device key is accepted only on /v1/ingest/{sourceId} paths.");
        }
        Optional<IngestDeviceEntity> device = devices.authenticate(key);
        if (device.isEmpty()) {
            return Result.rejected("The presented device key is not valid or has been revoked.");
        }
        IngestDeviceEntity d = device.get();
        if (!path.sourceId().equals(d.getSourceId())) {
            return Result.rejected("The presented device key does not belong to this source.");
        }
        return Result.accepted(new IngestCaller(IngestCaller.DEVICE_KEY, d.getSourceId(),
                "device-key:" + d.getKeyPrefix(), d.getKeyPrefix(), d.getId(), d.getExternalId()));
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
        if (path.compat() == null && WU_FORMAT.equals(path.format())) {
            String password = queryValue(request.getQueryString(), WU_PASSWORD);
            if (password != null && !password.isBlank()) {
                return new Credential(password.strip(), via);
            }
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

    /**
     * The first value of {@code name} in {@code query}, matched case-insensitively and URL-decoded, or null. The
     * query string is parsed directly so a form body is never consumed.
     */
    static String queryValue(String query, String name) {
        if (query == null || query.isEmpty()) {
            return null;
        }
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            String key = eq < 0 ? pair : pair.substring(0, eq);
            if (key.equalsIgnoreCase(name)) {
                String value = eq < 0 ? "" : pair.substring(eq + 1);
                try {
                    return java.net.URLDecoder.decode(value, StandardCharsets.UTF_8);
                } catch (IllegalArgumentException e) {
                    return value;
                }
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
     * @param keyInPath true for {@code /v1/ingest/{sourceId}/k/{key}/{format}} (prompt 039)
     */
    record IngestPath(String sourceId, String format, String compat, boolean keyInPath) {

        /** A path without a key segment. */
        IngestPath(String sourceId, String format, String compat) {
            this(sourceId, format, compat, false);
        }

        static IngestPath of(String uri) {
            String path = uri == null ? "" : uri;
            int query = path.indexOf('?');
            if (query >= 0) {
                path = path.substring(0, query);
            }
            String[] s = path.split("/");
            // ["", "v1", "ingest", "{sourceId}", "{format}"]
            // ["", "v1", "ingest", "{sourceId}", "k", "{key}", "{format}"] (prompt 039)
            if (s.length >= 7 && "v1".equals(s[1]) && "ingest".equals(s[2]) && "k".equals(s[4])) {
                return new IngestPath(s[3], s[6], null, true);
            }
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
