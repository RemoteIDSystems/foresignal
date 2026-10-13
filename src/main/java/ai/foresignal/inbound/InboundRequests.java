package ai.foresignal.inbound;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.inbound.format.DeviceEntityMismatchException;
import ai.foresignal.inbound.format.FormatOptions;
import ai.foresignal.inbound.format.NativeReply;
import ai.foresignal.source.LocalAddresses;
import ai.foresignal.source.spi.NormalizedRecord;
import org.springframework.http.HttpMethod;
import ai.foresignal.inbound.format.IngestFormat;
import ai.foresignal.inbound.format.IngestRequest;
import ai.foresignal.inbound.format.ParsedIngest;
import ai.foresignal.inbound.signature.SignatureVerifiers;
import ai.foresignal.inbound.signature.Verification;
import ai.foresignal.inbound.signature.VerifierConfig;
import ai.foresignal.security.IngestCaller;
import ai.foresignal.source.spi.SourceDescriptor;
import jakarta.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The servlet side of every format push (prompt 024): who is calling, the capped body, the
 * signature check and the format's options, then {@link PushIngestService}.
 *
 * <p>Order: the caller must be allowed to push into the source; the body is read with the 5 MB
 * cap ({@code 413}); a verifier configured for the format must verify ({@code 401
 * SIGNATURE_INVALID}), and a caller that presented no key ({@code keyOptional}) needs a valid
 * signature; then the source is admitted (404, {@code 409 SOURCE_DISABLED}, {@code 429}); the
 * format parses (a body it cannot read is a 400 and a health failure); and the records go to the
 * writer.
 *
 * <p>Prompt 039 adds, before the body is read:
 *
 * <ul>
 *   <li><strong>Path keys.</strong> An {@code eik_} key carried in the path
 *       ({@code /v1/ingest/{sourceId}/k/{key}/{format}}) needs the custom source's
 *       {@code allowPathKey}; otherwise the answer is {@code 404} (path keys not enabled). Device keys
 *       ({@code eid_}) are always accepted in the path.
 *   <li><strong>Sender allowlist.</strong> A push with a path key, or by GET, from a key holder must
 *       come from the source's {@code ingestAllowCidrs}, or from a private address (loopback,
 *       RFC 1918, link-local, ULA) when that list is unset; otherwise {@code 403
 *       INGEST_SOURCE_DENIED}. Admin tokens (the operator) are not checked.
 *   <li><strong>Methods.</strong> A GET push needs a format that declares GET ({@code 405} otherwise).
 * </ul>
 *
 * <p>Prompt 059 adds the WU protocol's query key ({@code PASSWORD} on the {@code wu-pws} format) to the pushes
 * that must pass the sender allowlist, and keeps a redacted copy of the request ({@link
 * ai.foresignal.mapping.SampleCapture}) while the source's {@code captureSamples} is on; the copy is taken after
 * the caller is authorized and the body is read, before the signature check and parsing, so a request the
 * mapping cannot read yet is still captured.
 *
 * <p>and after parsing, for a device key, every record must belong to the key's bound entity
 * ({@code 403 DEVICE_ENTITY_MISMATCH} otherwise): formats that read a device id from the payload
 * already compare it ({@link IngestRequest#externalId}), and this check covers every other format.
 * The format's {@link IngestFormat#nativeReply native reply}, if it has one, is returned with the
 * result.
 */
@Component
public class InboundRequests {

    private final CustomSourceRepository customSources;
    private final CustomSourceVerifiers verifierStore;
    private final SignatureVerifiers verifiers;
    private final PushIngestService pushes;
    private final JsonMapper json;
    private final LoopGuard loopGuard;
    private final Clock clock;
    private final ai.foresignal.mapping.SampleCapture samples;

    /** Creates the helper. */
    public InboundRequests(CustomSourceRepository customSources, CustomSourceVerifiers verifierStore,
            SignatureVerifiers verifiers, PushIngestService pushes, JsonMapper json, LoopGuard loopGuard, Clock clock,
            ai.foresignal.mapping.SampleCapture samples) {
        this.customSources = customSources;
        this.verifierStore = verifierStore;
        this.verifiers = verifiers;
        this.pushes = pushes;
        this.json = json;
        this.loopGuard = loopGuard;
        this.clock = clock;
        this.samples = samples;
    }

    /**
     * What a format push produced.
     *
     * @param result the push result
     * @param reply the reply the sending app expects, or null for the {@code IngestOutcome} JSON
     */
    public record Received(PushIngestService.PushResult result, NativeReply reply) {}

    /**
     * Pushes {@code request}'s body into {@code sourceId} through {@code format}.
     *
     * @return the push result
     */
    public PushIngestService.PushResult push(String sourceId, IngestFormat format, HttpServletRequest request) {
        return receive(sourceId, format, request).result();
    }

    /**
     * Pushes {@code request} (its body, or for GET its query) into {@code sourceId} through
     * {@code format}, with the 039 path-key, sender and device checks.
     *
     * @return the push result and the format's native reply
     */
    public Received receive(String sourceId, IngestFormat format, HttpServletRequest request) {
        IngestCaller caller = caller(request, sourceId);
        Optional<CustomSourceEntity> row = customSources.findById(sourceId);
        boolean get = "GET".equalsIgnoreCase(request.getMethod());
        if (get && !format.methods().contains(HttpMethod.GET)) {
            throw new ForesignalException(HttpStatus.METHOD_NOT_ALLOWED, ErrorCodes.METHOD_NOT_ALLOWED,
                    "The ingest format '" + format.id() + "' does not accept GET; use POST.");
        }
        boolean pathKey = IngestKeyRedactionFilter.pathKey(request) != null;
        if (pathKey && !caller.device() && !row.map(CustomSourceEntity::isAllowPathKey).orElse(false)) {
            throw NotFoundException.of("Path-key ingest for source", sourceId);
        }
        boolean queryKey = IngestAuthenticator.WU_FORMAT.equals(format.id())
                && IngestAuthenticator.queryValue(request.getQueryString(), IngestAuthenticator.WU_PASSWORD) != null;
        if ((pathKey || get || queryKey) && !caller.admin()) {
            checkSender(row.map(CustomSourceEntity::getIngestAllowCidrs).orElse(null), request.getRemoteAddr());
        }
        Instant receivedAt = clock.instant();
        byte[] body = get ? new byte[0] : IngestBodyReader.read(request);
        if (row.isPresent() && row.get().capturesSamplesAt(receivedAt)) {
            String uri = request.getRequestURI();
            String segment = uri != null && uri.endsWith("/ingest/" + sourceId) ? null : format.id();
            samples.capture(sourceId, receivedAt, request.getMethod(), segment, request.getContentType(),
                    request.getHeader("Content-Encoding"), request.getQueryString(), body);
        }
        IngestRequest ingest = ingestRequest(request, body, receivedAt);
        if (caller.device()) {
            ingest = ingest.withBoundExternalId(caller.boundExternalId());
        }
        verifySignature(row.orElse(null), format.id(), caller, ingest);

        if (loopGuard.isOwn(request, body, null, null)) {
            return new Received(new PushIngestService.PushResult(ai.foresignal.ingest.IngestOutcome.empty(),
                    IngestOutcomeView.of(null, 0, List.of())), format.nativeReply(ParsedIngest.empty()));
        }
        String url = IngestUrls.redacted(request);
        SourceDescriptor descriptor = pushes.admit(sourceId, caller.principal(), receivedAt, url);
        ParsedIngest parsed;
        try {
            parsed = format.parse(ingest, descriptor, options(row.orElse(null), format.id()));
            if (caller.device()) {
                requireBound(parsed, caller.boundExternalId());
            }
        } catch (DeviceEntityMismatchException e) {
            throw e;
        } catch (ForesignalException e) {
            pushes.recordFailure(sourceId, e);
            throw e;
        }
        PushIngestService.PushResult result = pushes.submit(descriptor, new PushIngestService.PushRequest(
                sourceId, caller.principal(), parsed, receivedAt, body.length, sha256(body), url));
        return new Received(result, format.nativeReply(parsed));
    }

    /**
     * Refuses a sender outside {@code cidrs}, or, when {@code cidrs} is null, outside the private
     * ranges.
     *
     * @throws ForesignalException {@code 403 INGEST_SOURCE_DENIED}
     */
    public static void checkSender(List<String> cidrs, String remoteAddress) {
        if (!senderAllowed(cidrs, remoteAddress)) {
            throw new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.INGEST_SOURCE_DENIED,
                    "This sender address may not push with a key in the URL or by GET; add it to the source's"
                            + " ingestAllowCidrs.");
        }
    }

    /** True when {@code remoteAddress} is inside {@code cidrs} (null: a private address). */
    public static boolean senderAllowed(List<String> cidrs, String remoteAddress) {
        if (cidrs == null) {
            byte[] bytes = Cidr.literal(remoteAddress);
            if (bytes == null) {
                return false;
            }
            try {
                return LocalAddresses.isPrivate(java.net.InetAddress.getByAddress(bytes));
            } catch (java.net.UnknownHostException e) {
                return false;
            }
        }
        for (String cidr : cidrs) {
            if (Cidr.parse(cidr).contains(remoteAddress)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Every record of {@code parsed} must belong to {@code bound}: its externalId is the bound id, or the
     * bound id's OwnTracks region-transition entity ({@code <bound>:transition}, kind
     * {@code tracker-transition}), which the {@code owntracks} format keeps apart so a transition never
     * replaces the position.
     */
    static void requireBound(ParsedIngest parsed, String bound) {
        String transition = bound + ai.foresignal.inbound.formats.location.OwnTracksFormat.TRANSITION_SUFFIX;
        for (NormalizedRecord record : parsed.records()) {
            if (!bound.equals(record.externalId()) && !transition.equals(record.externalId())) {
                throw new DeviceEntityMismatchException(record.externalId(), bound);
            }
        }
    }

    /**
     * The filter's caller for {@code request}, checked against {@code sourceId}.
     *
     * @throws ForesignalException 401 when the caller is missing or bound to another source
     */
    public static IngestCaller caller(HttpServletRequest request, String sourceId) {
        IngestCaller caller = IngestCaller.of(request).orElseThrow(() -> new ForesignalException(
                HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "Ingest requires an ingest key or an admin token."));
        if (sourceId != null && !caller.mayPushTo(sourceId)) {
            throw new ForesignalException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED,
                    "The presented ingest key does not belong to this source.");
        }
        return caller;
    }

    /**
     * Checks a configured verifier. A {@code keyOptional} caller without a key must present a valid
     * signature; a key holder must too unless no verifier is configured.
     *
     * @throws ForesignalException {@code 401 SIGNATURE_INVALID}
     */
    public void verifySignature(CustomSourceEntity row, String format, IngestCaller caller, IngestRequest request) {
        VerifierConfig config = row == null ? null : verifierStore.config(row, format);
        if (config == null || !config.active()) {
            if (caller.signatureOnly()) {
                throw signatureInvalid("This source requires an ingest key.");
            }
            return;
        }
        if (caller.admin() && !caller.signatureOnly()) {
            // An admin token is the operator; a sender signature is not expected from it.
            return;
        }
        String secret = verifierStore.secrets(row).get(format);
        Verification verification = verifiers.verify(config, secret, request);
        if (verification != Verification.VALID) {
            throw signatureInvalid(verification == Verification.MISSING
                    ? "The request carries no " + config.scheme() + " signature."
                    : "The " + config.scheme() + " signature did not verify.");
        }
    }

    /** The source's options for {@code format}; empty when none. */
    public FormatOptions options(CustomSourceEntity row, String format) {
        if (row == null || row.getFormats() == null || row.getFormats().isBlank()) {
            return FormatOptions.empty();
        }
        JsonNode formats = json.readTree(row.getFormats());
        JsonNode options = formats.get(format);
        return options == null || options.isNull() ? FormatOptions.empty() : new FormatOptions(options);
    }

    /** The {@link IngestRequest} of a servlet request and its body. */
    public static IngestRequest ingestRequest(HttpServletRequest request, byte[] body, Instant receivedAt) {
        HttpHeaders headers = new HttpHeaders();
        for (String name : Collections.list(request.getHeaderNames())) {
            headers.put(name, Collections.list(request.getHeaders(name)));
        }
        Map<String, List<String>> query = new LinkedHashMap<>();
        String raw = request.getQueryString();
        if (raw != null && !raw.isEmpty()) {
            for (String pair : raw.split("&")) {
                int eq = pair.indexOf('=');
                String name = decode(eq < 0 ? pair : pair.substring(0, eq));
                String value = eq < 0 ? "" : decode(pair.substring(eq + 1));
                query.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
            }
        }
        MediaType contentType = null;
        if (request.getContentType() != null) {
            try {
                contentType = MediaType.parseMediaType(request.getContentType());
            } catch (org.springframework.util.InvalidMimeTypeException e) {
                contentType = null;
            }
        }
        return new IngestRequest(body, contentType, headers, query, receivedAt);
    }

    /** Hex SHA-256 of {@code body}. */
    public static String sha256(byte[] body) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(body));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private static String decode(String value) {
        try {
            return java.net.URLDecoder.decode(value, java.nio.charset.StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return value;
        }
    }

    private static ForesignalException signatureInvalid(String detail) {
        return new ForesignalException(HttpStatus.UNAUTHORIZED, ErrorCodes.SIGNATURE_INVALID, detail);
    }
}
