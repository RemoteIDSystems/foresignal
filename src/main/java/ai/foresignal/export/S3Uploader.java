package ai.foresignal.export;

import ai.foresignal.net.OutboundUrlPolicy;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

/**
 * A minimal S3 SigV4 uploader on {@code java.net.http} (prompt 111): {@code PutObject} with the
 * endpoint checked by {@link OutboundUrlPolicy}. No AWS SDK. Objects larger than 64 MB are still sent
 * with one {@code PutObject} (S3 accepts up to 5 GiB); the multipart path of the design is a
 * documented deviation.
 */
@Component
public class S3Uploader {

    private static final DateTimeFormatter AMZ_DATE = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
            .withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);

    private final OutboundUrlPolicy policy;
    private final Clock clock;
    private final HttpClient client = HttpClient.newBuilder().build();

    /** Creates the uploader. */
    public S3Uploader(OutboundUrlPolicy policy, Clock clock) {
        this.policy = policy;
        this.clock = clock;
    }

    /** Uploads {@code file} as {@code key}, returning the object's {@code s3Key}. */
    public String upload(ArchiveSettings.S3 s3, Map<String, String> secrets, String key, Path file) {
        if (secrets == null || secrets.get("accessKey") == null || secrets.get("secretKey") == null) {
            throw new IllegalStateException("S3 secrets are not configured");
        }
        String endpoint = s3.endpoint().replaceAll("/+$", "");
        URI base = URI.create(endpoint);
        OutboundUrlPolicy.Result result = policy.check(base,
                OutboundUrlPolicy.Profile.LAN_ALLOWED);
        if (!result.allowed()) {
            throw new IllegalStateException("The archive endpoint is not allowed: "
                    + ((OutboundUrlPolicy.Denied) result).reason());
        }
        String prefix = s3.prefix() == null || s3.prefix().isBlank() ? "" : s3.prefix().replaceAll("/+$", "") + "/";
        String path = s3.pathStyle() ? "/" + s3.bucket() + "/" + prefix + key : "/" + prefix + key;
        URI uri = URI.create(endpoint + encodePath(path));
        String host = uri.getHost() + (uri.getPort() > 0 ? ":" + uri.getPort() : "");
        try {
            byte[] payloadHash = sha256(file);
            String payloadHex = HexFormat.of().formatHex(payloadHash);
            Instant now = clock.instant();
            String amzDate = AMZ_DATE.format(now);
            String date = DATE_ONLY.format(now);
            String region = s3.region() == null ? "us-east-1" : s3.region();
            String scope = date + "/" + region + "/s3/aws4_request";
            String canonicalHeaders = "host:" + host + "\n" + "x-amz-content-sha256:" + payloadHex + "\n"
                    + "x-amz-date:" + amzDate + "\n";
            String signedHeaders = "host;x-amz-content-sha256;x-amz-date";
            String canonicalRequest = "PUT\n" + encodePath(uri.getRawPath()) + "\n" + "\n" + canonicalHeaders + "\n"
                    + signedHeaders + "\n" + payloadHex;
            String stringToSign = "AWS4-HMAC-SHA256\n" + amzDate + "\n" + scope + "\n"
                    + HexFormat.of().formatHex(sha256(canonicalRequest.getBytes(StandardCharsets.UTF_8)));
            byte[] signingKey = signingKey(secrets.get("secretKey"), date, region);
            String signature = HexFormat.of().formatHex(hmac(signingKey, stringToSign));
            String authorization = "AWS4-HMAC-SHA256 Credential=" + secrets.get("accessKey") + "/" + scope
                    + ", SignedHeaders=" + signedHeaders + ", Signature=" + signature;
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .PUT(HttpRequest.BodyPublishers.ofFile(file))
                    .header("Host", host)
                    .header("x-amz-content-sha256", payloadHex)
                    .header("x-amz-date", amzDate)
                    .header("Authorization", authorization)
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new IOException("S3 PutObject returned " + response.statusCode() + ": " + response.body());
            }
            return (prefix + key);
        } catch (IOException e) {
            throw new java.io.UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while uploading to S3", e);
        }
    }

    private static byte[] signingKey(String secret, String date, String region) {
        byte[] kDate = hmac(("AWS4" + secret).getBytes(StandardCharsets.UTF_8), date);
        byte[] kRegion = hmac(kDate, region);
        byte[] kService = hmac(kRegion, "s3");
        return hmac(kService, "aws4_request");
    }

    private static byte[] hmac(byte[] key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("HmacSHA256 is unavailable", e);
        }
    }

    private static byte[] sha256(byte[] bytes) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private static byte[] sha256(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (var in = java.nio.file.Files.newInputStream(file)) {
                byte[] buffer = new byte[1 << 16];
                int read;
                while ((read = in.read(buffer)) >= 0) {
                    digest.update(buffer, 0, read);
                }
            }
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private static String encodePath(String path) {
        StringBuilder out = new StringBuilder();
        for (String segment : path.split("/", -1)) {
            if (!out.isEmpty()) {
                out.append('/');
            }
            out.append(URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20"));
        }
        return out.toString();
    }
}
