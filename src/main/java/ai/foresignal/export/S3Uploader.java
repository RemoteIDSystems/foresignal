package ai.foresignal.export;

import ai.foresignal.net.OutboundUrlPolicy;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

/**
 * A minimal S3 client for the cold archive (prompt 111) on {@code java.net.http}, signing with AWS
 * Signature Version 4 and no AWS SDK. Works with AWS S3 and S3-compatible stores such as MinIO.
 *
 * <ul>
 *   <li>Files up to {@value #MULTIPART_THRESHOLD} bytes go up with one {@code PutObject}; larger files
 *       with a multipart upload ({@code CreateMultipartUpload}, {@code UploadPart} of
 *       {@value #PART_SIZE} bytes, {@code CompleteMultipartUpload}; {@code AbortMultipartUpload} on
 *       failure).
 *   <li>Every request body is signed with its SHA-256 ({@code x-amz-content-sha256}), which the server
 *       checks, and carries {@code Content-MD5}. The upload is <em>verified</em> when the returned
 *       {@code ETag} equals the MD5 of the file (single part) or the MD5 of the part MD5s followed by
 *       {@code -N} (multipart); the archive deletes a local copy only after a verified upload.
 *   <li>The endpoint is checked by the outbound URL policy (prompt 035) under the {@code LAN_ALLOWED}
 *       profile: public endpoints plus loopback, RFC 1918, unique-local and shared addresses, over
 *       https or http, so a MinIO on the LAN works and link-local or metadata addresses are refused.
 * </ul>
 */
@Component
public class S3Uploader {

    /** Files larger than this use a multipart upload: 64 MiB. */
    public static final long MULTIPART_THRESHOLD = 64L * 1024 * 1024;

    /** The size of every part but the last: 64 MiB (S3's minimum is 5 MiB). */
    public static final long PART_SIZE = 64L * 1024 * 1024;

    private static final DateTimeFormatter AMZ_DATE = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
            .withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);
    private static final Pattern UPLOAD_ID = Pattern.compile("<UploadId>([^<]+)</UploadId>");
    private static final Pattern ETAG = Pattern.compile("<ETag>([^<]+)</ETag>");

    private final OutboundUrlPolicy policy;
    private final Clock clock;
    private final HttpClient client;
    private final long multipartThreshold;
    private final long partSize;

    /** Creates the uploader with the production thresholds. */
    @org.springframework.beans.factory.annotation.Autowired
    public S3Uploader(OutboundUrlPolicy policy, Clock clock) {
        this(policy, clock, MULTIPART_THRESHOLD, PART_SIZE);
    }

    /** Creates the uploader with explicit thresholds (tests use small parts). */
    S3Uploader(OutboundUrlPolicy policy, Clock clock, long multipartThreshold, long partSize) {
        this.policy = policy;
        this.clock = clock;
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20))
                .followRedirects(HttpClient.Redirect.NEVER).build();
        this.multipartThreshold = multipartThreshold;
        this.partSize = partSize;
    }

    /**
     * The outcome of an upload.
     *
     * @param key the object key, the prefix included
     * @param etag the returned ETag, without quotes
     * @param verified true when the ETag matched the file's checksum
     * @param parts the number of parts (1 for a single {@code PutObject})
     */
    public record Result(String key, String etag, boolean verified, int parts) {}

    /**
     * Uploads {@code file} to {@code key} under the configured prefix.
     *
     * @throws IllegalStateException when the endpoint is refused or the secrets are missing
     * @throws UncheckedIOException when a request fails
     */
    public Result upload(ArchiveSettings.S3 s3, Map<String, String> secrets, String key, Path file) {
        if (secrets == null || secrets.get("accessKey") == null || secrets.get("secretKey") == null) {
            throw new IllegalStateException("The S3 access key and secret key are not set");
        }
        Target target = target(s3, key);
        try {
            long size = Files.size(file);
            if (size <= multipartThreshold) {
                return single(target, s3, secrets, file, size);
            }
            return multipart(target, s3, secrets, file, size);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not upload " + file + " to S3", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while uploading to S3", e);
        }
    }

    /** The resolved object URL and key. */
    record Target(String endpoint, String host, String path, String key) {}

    /** Checks the endpoint and builds the object URL (path-style or virtual-hosted). */
    Target target(ArchiveSettings.S3 s3, String key) {
        String endpoint = s3.endpoint().replaceAll("/+$", "");
        String prefix = s3.prefix() == null || s3.prefix().isBlank() ? "" : s3.prefix().replaceAll("^/+|/+$", "") + "/";
        String objectKey = prefix + key;
        URI base = URI.create(endpoint);
        String host = base.getHost() + (base.getPort() > 0 ? ":" + base.getPort() : "");
        String basePath = base.getRawPath() == null ? "" : base.getRawPath();
        String path;
        if (s3.pathStyle()) {
            path = basePath + "/" + encodePath(s3.bucket()) + "/" + encodePath(objectKey);
        } else {
            host = s3.bucket() + "." + host;
            path = basePath + "/" + encodePath(objectKey);
        }
        String origin = base.getScheme() + "://" + host;
        // The host actually contacted (with the bucket for virtual-hosted style) is what the policy checks.
        OutboundUrlPolicy.Result result = policy.check(origin + "/", OutboundUrlPolicy.Profile.LAN_ALLOWED, true);
        if (!result.allowed()) {
            throw new IllegalStateException("The archive endpoint is not allowed: "
                    + ((OutboundUrlPolicy.Denied) result).reason());
        }
        return new Target(origin, host, path, objectKey);
    }

    private Result single(Target target, ArchiveSettings.S3 s3, Map<String, String> secrets, Path file, long size)
            throws IOException, InterruptedException {
        byte[][] digests = digests(file, 0, size);
        HttpRequest request = signed(target, s3, secrets, "PUT", Map.of(), digests[0], digests[1],
                HttpRequest.BodyPublishers.ofFile(file));
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        requireSuccess("PutObject", response);
        String etag = unquote(response.headers().firstValue("ETag").orElse(""));
        boolean verified = etag.equalsIgnoreCase(HexFormat.of().formatHex(digests[1]));
        return new Result(target.key(), etag, verified, 1);
    }

    private Result multipart(Target target, ArchiveSettings.S3 s3, Map<String, String> secrets, Path file, long size)
            throws IOException, InterruptedException {
        byte[] empty = sha256(new byte[0]);
        HttpRequest create = signed(target, s3, secrets, "POST", Map.of("uploads", ""), empty, null,
                HttpRequest.BodyPublishers.noBody());
        HttpResponse<String> created = client.send(create, HttpResponse.BodyHandlers.ofString());
        requireSuccess("CreateMultipartUpload", created);
        Matcher id = UPLOAD_ID.matcher(created.body());
        if (!id.find()) {
            throw new IOException("CreateMultipartUpload returned no UploadId");
        }
        String uploadId = id.group(1);
        List<String> etags = new ArrayList<>();
        MessageDigest md5OfMd5s = md5();
        try {
            int part = 1;
            for (long offset = 0; offset < size; offset += partSize, part++) {
                long length = Math.min(partSize, size - offset);
                byte[][] digests = digests(file, offset, length);
                md5OfMd5s.update(digests[1]);
                long partOffset = offset;
                HttpRequest upload = signed(target, s3, secrets, "PUT",
                        Map.of("partNumber", Integer.toString(part), "uploadId", uploadId), digests[0], digests[1],
                        HttpRequest.BodyPublishers.fromPublisher(
                                HttpRequest.BodyPublishers.ofInputStream(() -> region(file, partOffset, length)), length));
                HttpResponse<String> uploaded = client.send(upload, HttpResponse.BodyHandlers.ofString());
                requireSuccess("UploadPart " + part, uploaded);
                String etag = unquote(uploaded.headers().firstValue("ETag").orElse(""));
                if (!etag.equalsIgnoreCase(HexFormat.of().formatHex(digests[1]))) {
                    throw new IOException("UploadPart " + part + " returned ETag " + etag + ", not the part's MD5");
                }
                etags.add(etag);
            }
            StringBuilder xml = new StringBuilder("<CompleteMultipartUpload>");
            for (int k = 0; k < etags.size(); k++) {
                xml.append("<Part><PartNumber>").append(k + 1).append("</PartNumber><ETag>\"").append(etags.get(k))
                        .append("\"</ETag></Part>");
            }
            xml.append("</CompleteMultipartUpload>");
            byte[] body = xml.toString().getBytes(StandardCharsets.UTF_8);
            HttpRequest complete = signed(target, s3, secrets, "POST", Map.of("uploadId", uploadId), sha256(body),
                    md5(body), HttpRequest.BodyPublishers.ofByteArray(body));
            HttpResponse<String> completed = client.send(complete, HttpResponse.BodyHandlers.ofString());
            requireSuccess("CompleteMultipartUpload", completed);
            if (completed.body() != null && completed.body().contains("<Error>")) {
                throw new IOException("CompleteMultipartUpload failed: " + completed.body());
            }
            Matcher etagMatch = ETAG.matcher(completed.body() == null ? "" : completed.body());
            String etag = etagMatch.find() ? unquote(etagMatch.group(1).replace("&quot;", "\"")) : "";
            String expected = HexFormat.of().formatHex(md5OfMd5s.digest()) + "-" + etags.size();
            return new Result(target.key(), etag, etag.equalsIgnoreCase(expected), etags.size());
        } catch (IOException | RuntimeException | InterruptedException e) {
            abort(target, s3, secrets, uploadId);
            throw e;
        }
    }

    private void abort(Target target, ArchiveSettings.S3 s3, Map<String, String> secrets, String uploadId) {
        try {
            HttpRequest abort = signed(target, s3, secrets, "DELETE", Map.of("uploadId", uploadId),
                    sha256(new byte[0]), null, HttpRequest.BodyPublishers.noBody());
            client.send(abort, HttpResponse.BodyHandlers.discarding());
        } catch (IOException | RuntimeException e) {
            // The bucket's lifecycle rules remove an abandoned upload; the original failure is reported.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** A SigV4-signed request. */
    HttpRequest signed(Target target, ArchiveSettings.S3 s3, Map<String, String> secrets, String method,
            Map<String, String> query, byte[] payloadSha256, byte[] contentMd5, HttpRequest.BodyPublisher body) {
        Instant now = clock.instant();
        String amzDate = AMZ_DATE.format(now);
        String date = DATE_ONLY.format(now);
        String region = s3.region() == null || s3.region().isBlank() ? "us-east-1" : s3.region();
        String payloadHex = HexFormat.of().formatHex(payloadSha256);
        String canonicalQuery = canonicalQuery(query);
        TreeMap<String, String> headers = new TreeMap<>();
        headers.put("host", target.host());
        headers.put("x-amz-content-sha256", payloadHex);
        headers.put("x-amz-date", amzDate);
        if (contentMd5 != null) {
            headers.put("content-md5", Base64.getEncoder().encodeToString(contentMd5));
        }
        StringBuilder canonicalHeaders = new StringBuilder();
        headers.forEach((name, value) -> canonicalHeaders.append(name).append(':').append(value.strip()).append('\n'));
        String signedHeaders = String.join(";", headers.keySet());
        String canonicalRequest = method + "\n" + target.path() + "\n" + canonicalQuery + "\n" + canonicalHeaders
                + "\n" + signedHeaders + "\n" + payloadHex;
        String scope = date + "/" + region + "/s3/aws4_request";
        String stringToSign = "AWS4-HMAC-SHA256\n" + amzDate + "\n" + scope + "\n"
                + HexFormat.of().formatHex(sha256(canonicalRequest.getBytes(StandardCharsets.UTF_8)));
        byte[] signingKey = signingKey(secrets.get("secretKey"), date, region, "s3");
        String signature = HexFormat.of().formatHex(hmac(signingKey, stringToSign));
        String authorization = "AWS4-HMAC-SHA256 Credential=" + secrets.get("accessKey") + "/" + scope
                + ", SignedHeaders=" + signedHeaders + ", Signature=" + signature;
        URI uri = URI.create(target.endpoint() + target.path() + (canonicalQuery.isEmpty() ? "" : "?" + canonicalQuery));
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri).timeout(Duration.ofMinutes(10))
                .method(method, body)
                .header("x-amz-content-sha256", payloadHex)
                .header("x-amz-date", amzDate)
                .header("Authorization", authorization);
        if (contentMd5 != null) {
            builder.header("Content-MD5", headers.get("content-md5"));
        }
        return builder.build();
    }

    /** The SigV4 canonical query string: keys and values URI-encoded, sorted by key. */
    static String canonicalQuery(Map<String, String> query) {
        TreeMap<String, String> sorted = new TreeMap<>();
        query.forEach((k, v) -> sorted.put(encode(k), encode(v)));
        StringBuilder out = new StringBuilder();
        sorted.forEach((k, v) -> {
            if (!out.isEmpty()) {
                out.append('&');
            }
            out.append(k).append('=').append(v);
        });
        return out.toString();
    }

    /** The SigV4 signing key of {@code service} ({@code s3} here). */
    static byte[] signingKey(String secret, String date, String region, String service) {
        byte[] kDate = hmac(("AWS4" + secret).getBytes(StandardCharsets.UTF_8), date);
        byte[] kRegion = hmac(kDate, region);
        byte[] kService = hmac(kRegion, service);
        return hmac(kService, "aws4_request");
    }

    private static void requireSuccess(String operation, HttpResponse<String> response) throws IOException {
        if (response.statusCode() / 100 != 2) {
            throw new IOException(operation + " returned " + response.statusCode() + ": " + response.body());
        }
    }

    /** {@code [sha256, md5]} of {@code length} bytes of {@code file} from {@code offset}. */
    private static byte[][] digests(Path file, long offset, long length) throws IOException {
        MessageDigest sha = sha256Digest();
        MessageDigest md5 = md5();
        try (InputStream in = region(file, offset, length)) {
            byte[] buffer = new byte[1 << 16];
            int read;
            while ((read = in.read(buffer)) >= 0) {
                sha.update(buffer, 0, read);
                md5.update(buffer, 0, read);
            }
        }
        return new byte[][] {sha.digest(), md5.digest()};
    }

    /** An input stream over {@code length} bytes of {@code file} from {@code offset}. */
    private static InputStream region(Path file, long offset, long length) {
        try {
            FileChannel channel = FileChannel.open(file, StandardOpenOption.READ);
            channel.position(offset);
            InputStream raw = Channels.newInputStream(channel);
            return new InputStream() {
                private long remaining = length;

                @Override
                public int read() throws IOException {
                    if (remaining <= 0) {
                        return -1;
                    }
                    int b = raw.read();
                    if (b >= 0) {
                        remaining--;
                    }
                    return b;
                }

                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    if (remaining <= 0) {
                        return -1;
                    }
                    int n = raw.read(b, off, (int) Math.min(len, remaining));
                    if (n > 0) {
                        remaining -= n;
                    }
                    return n;
                }

                @Override
                public void close() throws IOException {
                    raw.close();
                }
            };
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String unquote(String etag) {
        String value = etag.strip();
        return value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")
                ? value.substring(1, value.length() - 1) : value;
    }

    private static byte[] hmac(byte[] key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA256 is unavailable", e);
        }
    }

    private static byte[] sha256(byte[] bytes) {
        return sha256Digest().digest(bytes);
    }

    private static byte[] md5(byte[] bytes) {
        return md5().digest(bytes);
    }

    private static MessageDigest sha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private static MessageDigest md5() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 is unavailable", e);
        }
    }

    /** URI-encodes a path, keeping {@code /} (SigV4 canonical URI for S3: no double encoding). */
    static String encodePath(String path) {
        StringBuilder out = new StringBuilder();
        String[] segments = path.split("/", -1);
        for (int k = 0; k < segments.length; k++) {
            if (k > 0) {
                out.append('/');
            }
            out.append(encode(segments[k]));
        }
        return out.toString();
    }

    /** RFC 3986 percent-encoding of everything but the unreserved characters. */
    static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20").replace("*", "%2A")
                .replace("%7E", "~");
    }
}
