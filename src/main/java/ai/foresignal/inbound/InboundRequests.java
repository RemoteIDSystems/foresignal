package ai.foresignal.inbound;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.inbound.format.FormatOptions;
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

    /** Creates the helper. */
    public InboundRequests(CustomSourceRepository customSources, CustomSourceVerifiers verifierStore,
            SignatureVerifiers verifiers, PushIngestService pushes, JsonMapper json, LoopGuard loopGuard, Clock clock) {
        this.customSources = customSources;
        this.verifierStore = verifierStore;
        this.verifiers = verifiers;
        this.pushes = pushes;
        this.json = json;
        this.loopGuard = loopGuard;
        this.clock = clock;
    }

    /**
     * Pushes {@code request}'s body into {@code sourceId} through {@code format}.
     *
     * @return the push result
     */
    public PushIngestService.PushResult push(String sourceId, IngestFormat format, HttpServletRequest request) {
        IngestCaller caller = caller(request, sourceId);
        Instant receivedAt = clock.instant();
        byte[] body = IngestBodyReader.read(request);
        IngestRequest ingest = ingestRequest(request, body, receivedAt);
        Optional<CustomSourceEntity> row = customSources.findById(sourceId);
        verifySignature(row.orElse(null), format.id(), caller, ingest);

        if (loopGuard.isOwn(request, body, null, null)) {
            return new PushIngestService.PushResult(ai.foresignal.ingest.IngestOutcome.empty(),
                    IngestOutcomeView.of(null, 0, List.of()));
        }
        String url = IngestUrls.redacted(request);
        SourceDescriptor descriptor = pushes.admit(sourceId, caller.principal(), receivedAt, url);
        ParsedIngest parsed;
        try {
            parsed = format.parse(ingest, descriptor, options(row.orElse(null), format.id()));
        } catch (ForesignalException e) {
            pushes.recordFailure(sourceId, e);
            throw e;
        }
        return pushes.submit(descriptor, new PushIngestService.PushRequest(
                sourceId, caller.principal(), parsed, receivedAt, body.length, sha256(body), url));
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
